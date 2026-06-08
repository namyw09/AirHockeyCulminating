# Agent Guide

## Project Overview

This repository contains a small Java desktop air hockey game made for an ICS3U culminating project.

The playable source is in:

`AirHockeyMadeByYoungandBringjton/src`

The repository also includes `AirhockeyYoungBrighton.zip`, which is the original packaged Eclipse project archive.

## Structure

- `AirHockeyMadeByYoungandBringjton/src/AirHockeyApp.java` is the entry point. It creates `AirHockeyGame`, shows the window, and calls `initComponents()`.
- `AirHockeyMadeByYoungandBringjton/src/AirHockeyGame.java` contains the main game setup, frame loop behavior, input handling, wall collisions, paddle collisions, and goal reset logic.
- `AirHockeyMadeByYoungandBringjton/src/Paddle.java` draws and moves a player paddle inside its allowed half of the rink.
- `AirHockeyMadeByYoungandBringjton/src/Puck.java` moves the puck, bounces it, resets it after goals, and handles paddle hits.
- `AirHockeyMadeByYoungandBringjton/src/Rink.java` draws the background, rink, goals, center line, labels, and player names.
- `AirHockeyMadeByYoungandBringjton/src/framework/Game.java` is the bundled educational game framework. It extends `JFrame`, owns the Swing timer loop, tracks key state, and calls `act()`.
- `AirHockeyMadeByYoungandBringjton/src/framework/GameObject.java` is the base drawable object class. It extends `JComponent` and provides position, size, paint, collision, and `act()` behavior.

## Technology Used

- Java
- Swing and AWT from the standard JDK
- A bundled `framework` package originally based on an educational Pong-style game framework
- Eclipse project metadata through `.project` and `.classpath`

There are no third-party dependencies, package managers, build tools, or test frameworks.

## Build And Run

From the repository root:

```sh
mkdir -p AirHockeyMadeByYoungandBringjton/bin
javac -d AirHockeyMadeByYoungandBringjton/bin $(find AirHockeyMadeByYoungandBringjton/src -name '*.java')
java -cp AirHockeyMadeByYoungandBringjton/bin AirHockeyApp
```

The game opens a Swing window and asks for player names before play begins.

## Development Notes

- Keep game-specific logic in `AirHockeyGame`, `Paddle`, `Puck`, and `Rink`.
- Avoid unnecessary changes to `framework/Game.java` and `framework/GameObject.java` unless changing loop behavior, key handling, or collision primitives.
- Coordinates are pixel-based. Most objects are positioned by top-left corner, while some constructors accept center points and convert internally.
- The game loop runs through a Swing `Timer`; `AirHockeyGame.act()` is called every frame.
- Player 1 uses `W/A/S/D`; Player 2 uses arrow keys.
- Current goal handling resets the puck but does not track score. `AirHockeyGame.handleGoals()` and `Rink.paint()` are the likely places to extend scoring.

## Comment Style

- Keep comments simple and high-schooler friendly, not super formal.
- Start comments with a lowercase letter when it still reads clearly.
- Do not end comments with periods.
- Use short `pre:` and `post:` notes only for bigger methods where they actually help.
- Prefer comments like `// update the scoreboard before drawing` instead of formal textbook wording.
- Do not comment every line or explain obvious code.

## Git Hygiene

- Do not commit generated `.class` files or the `bin/` output directory.
- Keep source files under `src/` as the canonical editable code.
- If the zip archive is regenerated, verify whether it should replace `AirhockeyYoungBrighton.zip` before committing.
