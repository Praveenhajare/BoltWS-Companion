# BoltWS Companion

An open-source Android companion for live testing and debugging extension development for Kodular and MIT App Inventor.

This project uses Bolt CLI and a PC-to-Android communication workflow to support extension development and testing.

## Features

- Live development workflow for testing compiled extension code.
- PC discovery using UDP.
- PC-to-Android communication using WebSocket.
- Method and parameter inspection.
- Target layout selection.
- Console logs for debugging.

**Note:** Features and compatibility are under active development. Some functionality may require further testing.

## Development Tools

- Java
- Bolt CLI
- Android
- DEX
- UDP and WebSocket

## Usage

1. Open the extension project using Bolt CLI.
2. Run `bolt build` to compile the extension.
3. Run `bolt run` to start live hot-reloading, if supported by your project setup.
4. Connect the Android companion to the development PC.
5. Test the available methods and inspect console logs.

## Project Structure

- `src/` — Java source code.
- `bolt.yml` — Bolt project configuration.
- `fast-live.js` — Live development script, if applicable.
- `parameter-metadata.js` — Parameter metadata handling, if applicable.

## Contributing

Bug reports, suggestions, and code contributions are welcome.

Please test changes before submitting them.

## License

This project is distributed under the MIT License. See the `LICENSE` file for details.