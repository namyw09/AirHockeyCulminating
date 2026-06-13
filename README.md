# AirHockeyCulminating

Java Swing air hockey game for the ICS3U culminating project.

This project is JDK-only. It uses Java Swing/AWT plus the bundled `framework`
package in `src/framework`, with no Python setup, package manager, or outside
library install needed.

## Build And Run

```sh
mkdir -p bin
javac -d bin $(find src -name '*.java')
java -cp bin AirHockeyApp
```

Import this repository root in Eclipse as an existing Java project.

## Sources And Credits

- Sound effects: https://sfxr.me/
