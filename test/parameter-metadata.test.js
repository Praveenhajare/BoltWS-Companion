'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { mergeVerifiedParameterSidecar } = require('../parameter-metadata');

const dexMd5 = 'a'.repeat(32);
const components = [{
    type: 'example.Component',
    methods: [{
        name: 'Build',
        params: [{ name: 'label', type: 'text' }, { name: 'items', type: 'list' }]
    }]
}];

function sidecar(overrides = {}) {
    return {
        version: 1,
        dexMd5,
        parameters: [{
            componentType: 'example.Component',
            methodName: 'Build',
            parameterIndex: 1,
            elementType: 'color',
            blockType: 'color-list',
            elementFormat: 'hex-string',
            evidence: 'Extension API contract: color list values are HEX strings.',
            ...overrides
        }]
    };
}

test('merges verified metadata into the exact method parameter index', () => {
    const result = mergeVerifiedParameterSidecar(components, sidecar({
        defaultValue: ['#EF4444', '#00FF00']
    }), dexMd5);
    assert.equal(result.applied, 1);
    assert.equal(result.components[0].methods[0].params[0].elementType, undefined);
    assert.equal(result.components[0].methods[0].params[1].elementType, 'color');
    assert.equal(result.components[0].methods[0].params[1].elementFormat, 'hex-string');
    assert.deepEqual(result.components[0].methods[0].params[1].defaultValue, ['#EF4444', '#00FF00']);
    assert.equal(components[0].methods[0].params[1].elementType, undefined);
});

test('missing sidecar leaves legacy metadata unchanged', () => {
    const result = mergeVerifiedParameterSidecar(components, null, dexMd5);
    assert.equal(result.applied, 0);
    assert.equal(result.components, components);
});

test('rejects a checksum mismatch without merging', () => {
    const mismatched = sidecar();
    mismatched.dexMd5 = 'b'.repeat(32);
    assert.throws(() => mergeVerifiedParameterSidecar(components, mismatched, dexMd5), /checksum/);
});

test('rejects unsupported element formats and mixed default values', () => {
    assert.throws(() => mergeVerifiedParameterSidecar(components,
        sidecar({ elementFormat: 'guess-from-list' }), dexMd5), /Unsupported or missing elementFormat/);
    assert.throws(() => mergeVerifiedParameterSidecar(components,
        sidecar({ defaultValue: ['#EF4444', 0x00FF00] }), dexMd5), /must be a #RRGGBB/);
});

test('requires evidence and rejects invalid homogeneous defaults', () => {
    assert.throws(() => mergeVerifiedParameterSidecar(components,
        sidecar({ evidence: '' }), dexMd5), /Evidence is required/);
    assert.throws(() => mergeVerifiedParameterSidecar(components,
        sidecar({ elementFormat: 'argb-int', defaultValue: [0xFFFFFF + 1.5] }), dexMd5), /must be an integer/);
    assert.throws(() => mergeVerifiedParameterSidecar(components,
        sidecar({ elementFormat: 'rgb-int', defaultValue: [-1] }), dexMd5), /RGB24 range/);
    const explicitEmptyDefault = mergeVerifiedParameterSidecar(components,
        sidecar({ defaultValue: [] }), dexMd5);
    assert.deepEqual(explicitEmptyDefault.components[0].methods[0].params[1].defaultValue, []);
});

test('rejects a bad method or parameter index instead of attaching metadata elsewhere', () => {
    assert.throws(() => mergeVerifiedParameterSidecar(components,
        sidecar({ methodName: 'Missing' }), dexMd5), /missing or ambiguous/);
    assert.throws(() => mergeVerifiedParameterSidecar(components,
        sidecar({ parameterIndex: 2 }), dexMd5), /outside/);
    assert.throws(() => mergeVerifiedParameterSidecar(components,
        sidecar({ parameterIndex: 0 }), dexMd5), /not declared as a list/);
});

test('rejects ambiguous overloads unless parameter count disambiguates them', () => {
    const overloaded = [{
        type: 'example.Component',
        methods: [
            { name: 'Build', params: [{ type: 'list' }, { type: 'text' }] },
            { name: 'Build', params: [{ type: 'list' }] }
        ]
    }];
    assert.throws(() => mergeVerifiedParameterSidecar(overloaded, sidecar({ parameterIndex: 0 }), dexMd5), /ambiguous/);
    const result = mergeVerifiedParameterSidecar(overloaded,
        sidecar({ parameterIndex: 0, methodParameterCount: 1 }), dexMd5);
    assert.equal(result.components[0].methods[1].params[0].elementFormat, 'hex-string');
    assert.equal(result.components[0].methods[0].params[0].elementFormat, undefined);
});

test('Main and Provider execution route verified color lists through the same typed converter', () => {
    const source = fs.readFileSync(path.join(__dirname, '../src/com/supertech/BoltWS/BoltWS.java'), 'utf8');
    assert.match(source, /return convertVerifiedColorList\(rawValue, metadata\);/);
    assert.match(source, /convertMethodArgument\(rawValue, targetMethod, i, pType\)/);
    assert.match(source, /arguments\[input\.parameterIndex\] = convertVerifiedColorList\(input\);/);
    assert.match(source, /Color-list metadata is incomplete or unsupported/);
});
