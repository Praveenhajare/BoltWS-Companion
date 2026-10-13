const WebSocket = require('ws');
const fs = require('fs');
const path = require('path');
const dgram = require('dgram');
const crypto = require('crypto');
const { mergeVerifiedParameterSidecar } = require('./parameter-metadata');

const admZipPath = path.join(__dirname, 'node_modules', 'adm-zip');
let AdmZip;
try {
    AdmZip = fs.existsSync(admZipPath) ? require(admZipPath) : require('adm-zip');
} catch (e) {
    AdmZip = require('adm-zip');
}

const WS_PORT = 9000;
const UDP_PORT = 9001;
const CURRENT_DIR = process.cwd();

function getBufferMd5(buffer) {
    return crypto.createHash('md5').update(buffer).digest('hex');
}

function parseComponentsMetadata(metadataEntry) {
    const parsed = JSON.parse(metadataEntry.getData().toString('utf8'));
    return Array.isArray(parsed) ? parsed : (parsed && typeof parsed === 'object' ? [parsed] : null);
}

function applyOptionalParameterSidecar(aixPath, dexBuffer, components) {
    const sidecarPath = `${aixPath}.bolt-metadata.json`;
    if (!fs.existsSync(sidecarPath)) {
        return { components, sidecarApplied: 0, sidecarDiagnostic: null };
    }
    try {
        const sidecar = JSON.parse(fs.readFileSync(sidecarPath, 'utf8'));
        const merged = mergeVerifiedParameterSidecar(components, sidecar, getBufferMd5(dexBuffer));
        return { components: merged.components, sidecarApplied: merged.applied, sidecarDiagnostic: null };
    } catch (error) {
        return {
            components,
            sidecarApplied: 0,
            sidecarDiagnostic: `${sidecarPath}: ${error.message}`
        };
    }
}

function getBlockMetadataForDex(zip, dexBuffer, aixPath) {
    const entries = zip.getEntries();
    const metadataEntry = entries.find(entry => /(^|\/)components\.json$/i.test(entry.entryName));
    if (!metadataEntry) return null;

    const expectedMd5 = getBufferMd5(dexBuffer);
    const baseComponents = parseComponentsMetadata(metadataEntry);
    if (!Array.isArray(baseComponents)) return null;
    const metadataWithSidecar = applyOptionalParameterSidecar(aixPath, dexBuffer, baseComponents);
    for (const entry of entries) {
        if (entry.entryName.endsWith('.dex') && getBufferMd5(entry.getData()) === expectedMd5) {
            return {
                aixPath,
                components: metadataWithSidecar.components,
                sidecarApplied: metadataWithSidecar.sidecarApplied,
                sidecarDiagnostic: metadataWithSidecar.sidecarDiagnostic
            };
        }
        if (entry.entryName.endsWith('.jar')) {
            try {
                const nestedZip = new AdmZip(entry.getData());
                for (const nestedEntry of nestedZip.getEntries()) {
                    if (nestedEntry.entryName.endsWith('.dex')
                            && getBufferMd5(nestedEntry.getData()) === expectedMd5) {
                        return {
                            aixPath,
                            components: metadataWithSidecar.components,
                            sidecarApplied: metadataWithSidecar.sidecarApplied,
                            sidecarDiagnostic: metadataWithSidecar.sidecarDiagnostic
                        };
                    }
                }
            } catch (errNested) {
                // Some bundled JAR entries are not ZIP-readable; continue checking other entries.
            }
        }
    }
    return null;
}

function findMatchingAixMetadata(dexBuffer) {
    const outDir = path.join(CURRENT_DIR, 'out');
    if (!fs.existsSync(outDir)) return null;
    for (const file of fs.readdirSync(outDir)) {
        if (!file.endsWith('.aix') && !file.endsWith('.zip')) continue;
        try {
            const metadata = getBlockMetadataForDex(
                new AdmZip(path.join(outDir, file)),
                dexBuffer,
                path.join(outDir, file)
            );
            if (metadata) return metadata;
        } catch (err) {
            console.error(`AIX metadata वाचता आली नाही (${file}):`, err.message);
        }
    }
    return null;
}

function getDexFromAix(aixPath) {
    const zip = new AdmZip(aixPath);
    const zipEntries = zip.getEntries();

    for (const entry of zipEntries) {
        if (entry.entryName.endsWith('.dex')) {
            const buffer = entry.getData();
            return {
                buffer,
                source: `${aixPath} -> ${entry.entryName}`,
                blockMetadata: getBlockMetadataForDex(zip, buffer, aixPath)
            };
        }
    }

    for (const entry of zipEntries) {
        if (!entry.entryName.endsWith('.jar')) continue;
        try {
            const nestedZip = new AdmZip(entry.getData());
            const dexEntry = nestedZip.getEntries().find(candidate => candidate.entryName.endsWith('.dex'));
            if (dexEntry) {
                const buffer = dexEntry.getData();
                return {
                    buffer,
                    source: `${aixPath} -> ${entry.entryName} -> ${dexEntry.entryName}`,
                    blockMetadata: getBlockMetadataForDex(zip, buffer, aixPath)
                };
            }
        } catch (errNested) {
            // Ignore non-ZIP JAR entries and continue looking for a DEX.
        }
    }
    return null;
}

// १. UDP Auto-Discovery Responder
const udpServer = dgram.createSocket('udp4');
udpServer.on('message', (msg, rinfo) => {
    if (msg.toString().includes('BOLT_DISCOVER')) {
        const response = Buffer.from('BOLT_OK');
        udpServer.send(response, rinfo.port, rinfo.address, () => {
            console.log(`📡 UDP Ping ला प्रतिसाद दिला: ${rinfo.address}:${rinfo.port}`);
        });
    }
});
udpServer.bind(UDP_PORT, () => {
    console.log(`📡 UDP Discovery Listener Port ${UDP_PORT} वर सज्ज आहे.`);
});

// २. DEX बायनरी शोधणे (AIX आणि आतील JAR सह)
function getDexBuffer() {
    const outDir = path.join(CURRENT_DIR, 'out');
    if (fs.existsSync(outDir)) {
        const aixFiles = fs.readdirSync(outDir)
            .filter(file => file.endsWith('.aix') || file.endsWith('.zip'))
            .map(file => path.join(outDir, file))
            .sort((left, right) => fs.statSync(right).mtimeMs - fs.statSync(left).mtimeMs);
        for (const aixPath of aixFiles) {
            try {
                const result = getDexFromAix(aixPath);
                if (result) return result;
            } catch (err) {
                console.error(`AIX उघडताना त्रुटी (${aixPath}):`, err.message);
            }
        }
    }

    // Compiled project outputs are preferred above; use a loose DEX only as fallback.
    const directSearch = (dir) => {
        if (!fs.existsSync(dir)) return null;
        try {
            const files = fs.readdirSync(dir);
            for (const file of files) {
                const fullPath = path.join(dir, file);
                const stat = fs.statSync(fullPath);
                if (stat.isDirectory() && !fullPath.includes('node_modules') && !fullPath.includes('.git')) {
                    const res = directSearch(fullPath);
                    if (res) return res;
                } else if (file === 'classes.dex') {
                    return { buffer: fs.readFileSync(fullPath), source: fullPath };
                }
            }
        } catch (e) {}
        return null;
    };

    const direct = directSearch(CURRENT_DIR);
    if (direct) {
        direct.blockMetadata = findMatchingAixMetadata(direct.buffer);
        return direct;
    }

    return null;
}

// ३. WebSocket Server
const wss = new WebSocket.Server({ port: WS_PORT });
let connectedClient = null;

console.log(`⚡ FAST Live Server Port ${WS_PORT} वर सुरू झाला आहे (PID ${process.pid})...`);
console.log(`📂 प्रोजेक्ट फोल्डर: ${CURRENT_DIR}`);
console.log('ℹ️ Metadata transport साठी हा helper वापरा; bolt run देखील port 9000 वापरतो.');

wss.on('connection', (ws) => {
    connectedClient = ws;
    console.log(`📱 फोन कनेक्ट: ${ws._socket.remoteAddress}:${ws._socket.remotePort}`);

    const pingInterval = setInterval(() => {
        if (ws.readyState === WebSocket.OPEN) {
            ws.ping();
        } else {
            clearInterval(pingInterval);
        }
    }, 5000);

    attemptSendDex(3);

    ws.on('close', (code) => {
        clearInterval(pingInterval);
        console.log(`❌ फोन डिस्कनेक्ट झाला (Code: ${code}).`);
        if (connectedClient === ws) connectedClient = null;
    });

    ws.on('error', (err) => {
        console.error('सॉकेट त्रुटी:', err.message);
    });
});

function attemptSendDex(retriesLeft) {
    if (!connectedClient || connectedClient.readyState !== WebSocket.OPEN) return;

    const res = getDexBuffer();
    if (res && res.buffer) {
        connectedClient.send(res.buffer);
        const dexMd5 = getBufferMd5(res.buffer);
        if (res.blockMetadata) {
            const components = res.blockMetadata.components;
            const componentSummary = Array.isArray(components)
                ? components.map(component => `${component.type}: methods=${(component.methods || []).length}, blockProperties=${(component.blockProperties || []).length}`).join('; ')
                : 'invalid components metadata';
            console.log(`[METADATA] AIX=${res.blockMetadata.aixPath}; DEX=${dexMd5}; ${componentSummary}`);
            if (res.blockMetadata.sidecarApplied > 0) {
                console.log(`[METADATA-SIDECAR] Applied verified parameter metadata to ${res.blockMetadata.sidecarApplied} parameter(s).`);
            }
            if (res.blockMetadata.sidecarDiagnostic) {
                console.error(`[METADATA-SIDECAR-ERR] ${res.blockMetadata.sidecarDiagnostic}; sending base AIX metadata only.`);
            }
            connectedClient.send(JSON.stringify({
                type: 'bolt.parameter-metadata',
                version: 1,
                dexMd5,
                aixPath: res.blockMetadata.aixPath,
                dexSource: res.source,
                components
            }));
        } else {
            console.log(`[METADATA] No AIX components.json matches DEX=${dexMd5} `
                + `(DEX source: ${res.source}).`);
        }
        console.log(`🚀 बायनरी पाठवली (${res.buffer.length} bytes) [स्त्रोत: ${res.source}] -> फोन अपडेट झाला!`);
    } else if (retriesLeft > 0) {
        setTimeout(() => attemptSendDex(retriesLeft - 1), 600);
    } else {
        console.log('ℹ️ out फोल्डरमधील .aix मधून बायनरी काढता आली नाही.');
    }
}

// ४. Watcher
const outWatchDir = path.join(CURRENT_DIR, 'out');
if (!fs.existsSync(outWatchDir)) {
    try { fs.mkdirSync(outWatchDir, { recursive: true }); } catch (e) {}
}

let debounceTimer = null;
try {
    fs.watch(outWatchDir, (eventType, filename) => {
        if (filename && filename.endsWith('.aix')) {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                console.log('🔄 नवीन AIX बिल्ड तयार झाले, बायनरी वाचत आहे...');
                attemptSendDex(5);
            }, 800);
        }
    });
} catch (e) {}
