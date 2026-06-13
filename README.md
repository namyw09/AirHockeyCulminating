# AirHockeyCulminating

Java Swing air hockey game for the ICS3U culminating project.

This project is JDK-only. It uses Java Swing/AWT plus the bundled `framework`
package in `src/framework`, with no Python setup, package manager, or outside
library install needed.

## Built From Milestone

This final version is an extension of our milestone Air Hockey project.

- Milestone `StartPanel` is still `StartPanel`, with the same start-screen idea plus rules and match history.
- Milestone `GamePanel` is still `GamePanel`, with the same main game-loop idea plus scoring, timer, pause, powerups, and fullscreen layout.
- Milestone `Paddle`, `Puck`, and `GameObject` ideas are still here, but now they use the bundled `framework` package so drawing, collision, and timing are cleaner.
- Rink drawing was split into `Rink` so `GamePanel` can focus more on game rules instead of drawing everything itself.
- We kept milestone-style names like `paddle1`, `paddle2`, `startButton`, and `howToPlayButton` so the final code still feels connected to the prototype.

## Build And Run

```sh
mkdir -p bin
javac -d bin $(find src -name '*.java')
java -cp bin AirHockeyApp
```

Import this repository root in Eclipse as an existing Java project.

## Sources And Credits

- Sound effects: https://sfxr.me/
