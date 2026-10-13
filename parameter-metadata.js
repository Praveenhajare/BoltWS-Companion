'use strict';

const crypto = require('crypto');

const FORMAT_ALIASES = new Map([
    ['hex', 'hex-string'],
    ['hexstring', 'hex-string'],
    ['string', 'hex-string'],
    ['argb', 'argb-int'],
    ['argbint', 'argb-int'],
    ['androidcolor', 'argb-int'],
    ['colorint', 'argb-int'],
    ['colourint', 'argb-int'],
    ['rgb', 'rgb-int'],
    ['rgb24', 'rgb-int'],
    ['rgbint', 'rgb-int']
]);

function normalizeToken(value) {
    return String(value == null ? '' : value).toLowerCase().replace(/[\s_-]/g, '');
}

function normalizeFormat(value) {
    return FORMAT_ALIASES.get(normalizeToken(value)) || null;
}

function isListBlockType(value) {
    const type = normalizeToken(value);
    return type === 'list' || type === 'yaillist' || type === 'colorlist'
        || type === 'colourlist' || type.startsWith('list<');
}

function parseColorDefault(value, format) {
    if (!Array.isArray(value)) throw new Error('defaultValue must be a JSON array.');
    return value.map((item, index) => {
        if (format === 'hex-string') {
            if (typeof item !== 'string' || !/^#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?$/.test(item)) {
                throw new Error(`defaultValue[${index}] must be a #RRGGBB or #AARRGGBB string.`);
            }
            return item.toUpperCase();
        }
        if (typeof item !== 'number' || !Number.isInteger(item)) {
            throw new Error(`defaultValue[${index}] must be an integer for ${format}.`);
        }
        if (format === 'rgb-int' && (item < 0 || item > 0xFFFFFF)) {
            throw new Error(`defaultValue[${index}] is outside the RGB24 range.`);
        }
        if (format === 'argb-int' && (item < -0x80000000 || item > 0xFFFFFFFF)) {
            throw new Error(`defaultValue[${index}] is outside the ARGB32 range.`);
        }
        return item;
    });
}

function mergeVerifiedParameterSidecar(components, sidecar, expectedDexMd5) {
    if (sidecar == null) return { components, applied: 0 };
    if (!Array.isArray(components)) throw new Error('Base components metadata must be an array.');
    if (!sidecar || typeof sidecar !== 'object' || Array.isArray(sidecar)) {
        throw new Error('Sidecar root must be a JSON object.');
    }
    if (sidecar.version !== 1) throw new Error('Unsupported sidecar version.');
    if (typeof sidecar.dexMd5 !== 'string'
            || sidecar.dexMd5.toLowerCase() !== String(expectedDexMd5 || '').toLowerCase()) {
        throw new Error('Sidecar DEX checksum does not match the received DEX.');
    }
    if (!Array.isArray(sidecar.parameters)) throw new Error('Sidecar parameters must be an array.');

    const merged = JSON.parse(JSON.stringify(components));
    const appliedKeys = new Set();
    for (const entry of sidecar.parameters) {
        if (!entry || typeof entry !== 'object' || Array.isArray(entry)) {
            throw new Error('Each sidecar parameter entry must be an object.');
        }
        const componentType = typeof entry.componentType === 'string' ? entry.componentType.trim() : '';
        const methodName = typeof entry.methodName === 'string' ? entry.methodName.trim() : '';
        const parameterIndex = entry.parameterIndex;
        if (!componentType || !methodName || !Number.isInteger(parameterIndex) || parameterIndex < 0) {
            throw new Error('Each entry needs componentType, methodName, and a non-negative parameterIndex.');
        }
        if (typeof entry.evidence !== 'string' || !entry.evidence.trim()) {
            throw new Error(`Evidence is required for ${componentType}.${methodName}[${parameterIndex}].`);
        }
        const elementType = normalizeToken(entry.elementType);
        if (elementType !== 'color' && elementType !== 'colour') {
            throw new Error(`Unsupported elementType for ${componentType}.${methodName}[${parameterIndex}].`);
        }
        const blockType = normalizeToken(entry.blockType);
        if (blockType !== 'colorlist' && blockType !== 'colourlist') {
            throw new Error(`blockType must explicitly identify a color-list parameter for ${componentType}.${methodName}[${parameterIndex}].`);
        }
        const elementFormat = normalizeFormat(entry.elementFormat);
        if (!elementFormat) {
            throw new Error(`Unsupported or missing elementFormat for ${componentType}.${methodName}[${parameterIndex}].`);
        }
        const key = `${componentType}\u0000${methodName}\u0000${parameterIndex}`;
        if (appliedKeys.has(key)) throw new Error(`Duplicate sidecar entry for ${componentType}.${methodName}[${parameterIndex}].`);

        const matchingComponents = merged.filter(component => component && component.type === componentType);
        if (matchingComponents.length !== 1) throw new Error(`Component metadata match count is ${matchingComponents.length} for ${componentType}.`);
        const methods = Array.isArray(matchingComponents[0].methods) ? matchingComponents[0].methods : [];
        const matchingMethods = methods.filter(method => method && method.name === methodName);
        if (Number.isInteger(entry.methodParameterCount)) {
            for (let i = matchingMethods.length - 1; i >= 0; i--) {
                if (!Array.isArray(matchingMethods[i].params)
                        || matchingMethods[i].params.length !== entry.methodParameterCount) matchingMethods.splice(i, 1);
            }
        }
        if (matchingMethods.length !== 1) {
            throw new Error(`Method match is missing or ambiguous for ${componentType}.${methodName}; provide methodParameterCount when overloaded.`);
        }
        const params = matchingMethods[0].params;
        if (!Array.isArray(params) || parameterIndex >= params.length) {
            throw new Error(`Parameter index ${parameterIndex} is outside ${componentType}.${methodName}.`);
        }
        const parameter = params[parameterIndex];
        if (!parameter || !isListBlockType(parameter.type)) {
            throw new Error(`${componentType}.${methodName}[${parameterIndex}] is not declared as a list parameter in components metadata.`);
        }

        if (parameter.elementType != null && normalizeToken(parameter.elementType) !== elementType) {
            throw new Error(`Conflicting elementType already exists for ${componentType}.${methodName}[${parameterIndex}].`);
        }
        if (parameter.elementFormat != null && normalizeFormat(parameter.elementFormat) !== elementFormat) {
            throw new Error(`Conflicting elementFormat already exists for ${componentType}.${methodName}[${parameterIndex}].`);
        }
        const existingBlockType = normalizeToken(parameter.blockType);
        const genericListBlockType = existingBlockType === 'list' || existingBlockType === 'yaillist';
        if (parameter.blockType != null && existingBlockType !== blockType && !genericListBlockType) {
            throw new Error(`Conflicting blockType already exists for ${componentType}.${methodName}[${parameterIndex}].`);
        }
        if (Object.prototype.hasOwnProperty.call(entry, 'defaultValue')) {
            const validatedDefault = parseColorDefault(entry.defaultValue, elementFormat);
            if (parameter.defaultValue != null) {
                const baseDefault = typeof parameter.defaultValue === 'string'
                    ? JSON.parse(parameter.defaultValue) : parameter.defaultValue;
                const validatedBase = parseColorDefault(baseDefault, elementFormat);
                if (JSON.stringify(validatedBase) !== JSON.stringify(validatedDefault)) {
                    throw new Error(`Conflicting defaultValue already exists for ${componentType}.${methodName}[${parameterIndex}].`);
                }
            } else {
                parameter.defaultValue = validatedDefault;
            }
        }
        parameter.elementType = 'color';
        parameter.elementFormat = elementFormat;
        parameter.blockType = 'color-list';
        appliedKeys.add(key);
    }
    return { components: merged, applied: appliedKeys.size };
}

module.exports = { mergeVerifiedParameterSidecar, normalizeFormat };
