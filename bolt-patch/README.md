# BoltWS — bolt.jar Patch (Parameter Metadata Fix)

## 🎯 समस्या काय होती
`bolt run` कडून आलेला DEX मध्ये parameter names चुकीचे दिसत होते
(`Parameter 1 (String)`), कारण `bolt run` फक्त raw DEX bytes पाठवतो —
`components.json` मधले खऱ्या नावे (`host`, `port`) पाठवत नव्हते.

## ✅ उपाय (काय बदललं)
`bolt-cli` च्या `RunCommand.kt` मधील **`sendLatestDex()`** function मध्ये —
DEX bytes पाठवल्यानंतर आता `bolt.parameter-metadata` JSON message देखील पाठवतो.

### नेमके कुठे बदल केला (file + function)
- **File:** `src/main/kotlin/com/techhamara/bolt/cli/commands/RunCommand.kt`
  (TechHamara/bolt-cli repo)
- **Function:** `sendLatestDex()` (मूळ lines ~181-208)

### जुणं code:
```kotlin
client.send(dexBytes)
Thread.sleep(100)
client.send("RELOAD:AnyComponent")
```

### नविनं code (concept):
```kotlin
val metadataEnvelope = buildParameterMetadataEnvelope(projectDir, dexBytes, logger)
client.send(dexBytes)
Thread.sleep(100)
if (metadataEnvelope != null) {
    client.send(metadataEnvelope)   // <-- नवं: parameter metadata
    Thread.sleep(100)
}
client.send("RELOAD:AnyComponent")
```

### नवीन helper function जोडला:
`buildParameterMetadataEnvelope()` —
1. `.bolt/staging/**/components.json` वाचतो (खऱ्या parameter names)
2. `dexBytes` चा MD5 compute करतो (`%02x` lowercase hex — client शी match)
3. `{type, version:1, dexMd5, aixPath, components}` envelope तयार करतो

पूर्ण modified source: **`RunCommand.kt`** (या folder मध्ये).

## 📁 या folder मध्ये काय आहे
| File | काय |
|------|-----|
| `RunCommand.kt` | modified source (reference + re-compile साठी) |
| `classes/RunCommand*.class` | compiled bytecode (7 files) — JAR मध्ये inject होतात |
| `inject.ps1` | bolt.jar मध्ये classes inject करणारा PowerShell script |
| `repatch-bolt.bat` | एका click मध्ये सर्व करणारा script |

## 🔧 कसे पुन्हा apply करायचं (`bolt upgrade` नंतर)

1. `bolt run` session **बंद** करा (JAR lock होतो).
2. `bolt-patch\repatch-bolt.bat` **double-click** करा.
3. ते स्वयंचलित: backup → inject → verify.
4. मग `bolt run` पुन्हा सुरू करा.

> जर bolt-cli नव्या version मध्ये `RunCommand.kt` बरीच बदलली असेल तर
> `RunCommand.kt` मधला बदल manually पुन्हा करून re-compile करावं लागेल
> (kotlinc 1.9.22 + bolt.jar classpath वर).

## ⚠️ मुद्दे
- `bolt upgrade` केल्यावर हे patch **पुसून जातं** → पुन्हा `repatch-bolt.bat` चालवा.
- Client (`BoltWS.java`) मध्ये **कोणताही बदल नाही** — ते आधीच `bolt.parameter-metadata` handle करतो.
- Backup नेहमी `bolt.jar.backup-<timestamp>` म्हणून तयार होतो.
