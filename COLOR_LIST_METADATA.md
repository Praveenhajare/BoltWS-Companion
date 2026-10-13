# Universal color-list metadata sidecar

Place an optional sidecar next to an AIX file named `<aix-path>.bolt-metadata.json`.
For example, `out/example.aix` uses `out/example.aix.bolt-metadata.json`.

The sidecar is an explicit record of an extension API contract; it must not be
generated from a `YailList` type or a parameter name alone. Its DEX MD5 binds it
to the binary that BoltWS sends.

```json
{
  "version": 1,
  "dexMd5": "<lowercase-md5-of-the-matching-dex>",
  "parameters": [
    {
      "componentType": "<component type from components.json>",
      "methodName": "<method name from components.json>",
      "methodParameterCount": 2,
      "parameterIndex": 0,
      "blockType": "color-list",
      "elementType": "color",
      "elementFormat": "hex-string",
      "evidence": "<reference to the authoritative API contract>",
      "defaultValue": ["#112233", "#AABBCC"]
    }
  ]
}
```

`methodParameterCount` is optional when the method name is unique; it is
required to disambiguate overloads. `defaultValue` is optional and must be an
array matching the declared format. The supported formats are `hex-string`
(`#RRGGBB` or `#AARRGGBB` strings), `argb-int` (integer color values), and
`rgb-int` (integers from 0 through 16777215). An absent default remains absent.

Invalid, unsupported, conflicting, or checksum-mismatched sidecars are ignored
atomically. The PC producer logs the reason and sends the original AIX metadata
unchanged. The WebSocket `bolt.parameter-metadata` protocol remains version 1;
the merged fields are optional properties inside the existing parameter
metadata objects.
