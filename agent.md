# Agent Guide

## Project Overview

This repository contains a small Java desktop air hockey game made for an ICS3U culminating project.

The playable source is in:

`src`

The repository also includes `AirhockeyYoungBrighton.zip`, which is the original packaged Eclipse project archive.

## Structure

- `src/AirHockeyApp.java` is the entry point. It creates `AirHockeyGame`, shows the window, and calls `initComponents()`.
- `src/AirHockeyGame.java` contains the main game setup, frame loop behavior, input handling, wall collisions, paddle collisions, and goal reset logic.
- `src/Paddle.java` draws and moves a player paddle inside its allowed half of the rink.
- `src/Puck.java` moves the puck, bounces it, resets it after goals, and handles paddle hits.
- `src/Rink.java` draws the background, rink, goals, center line, labels, and player names.
- `src/framework/Game.java` is the bundled educational game framework. It extends `JFrame`, owns the Swing timer loop, tracks key state, and calls `act()`.
- `src/framework/GameObject.java` is the base drawable object class. It extends `JComponent` and provides position, size, paint, collision, and `act()` behavior.

## Technology Used

- Java
- Swing and AWT from the standard JDK
- A bundled `framework` package originally based on an educational Pong-style game framework
- Eclipse project metadata through `.project` and `.classpath`

There are no third-party dependencies, package managers, build tools, or test frameworks.

## Build And Run

From the repository root:

```sh
mkdir -p bin
javac -d bin $(find src -name '*.java')
java -cp bin AirHockeyApp
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

## Culminating Project Requirements

Use this checklist when planning, editing, documenting, or reviewing the project.

### Software Life Cycle

- Follow the full software life cycle: problem definition, analysis, design, implementation, testing, and maintenance.
- Create a project plan before coding and by the due date. Include the task list and the order of completion.
- Optional features can be listed separately as "if time permits" work.
- Create a prototype and submit the milestone to Google Classroom by the due date to show progress according to the plan.
- Ensure program correctness by developing valid and invalid test data to eliminate syntax, run-time, and logic errors.
- Have other users test the program and record useful feedback.

### Proposal And Documentation

- Write a clear project proposal that states the project idea, planned features, task list, and GUI design.
- Include "how to" instructions for the user, such as JOptionPane help text, help files, or README files.
- Comment code clearly and include proper method documentation using the required style:

```java
/**
 * pre: describe what must be true before the method runs
 * post: describe what is true after the method finishes
 */
```

- Use JavaDoc to create API documentation from internal comments.

### Research

- Use resource materials effectively to learn any programming skills needed for the project.
- Document sources by listing the URLs used in a comment block at the top of the program.

### Creativity, Effort, And Fun

- Keep the difficulty and challenge appropriate for the time frame.
- Aim for a game that feels fun, complete, and full featured.

### Programming Expectations

- The program must function correctly.
- Use meaningful variable and object names that follow Java naming conventions:
  - Classes begin with uppercase letters.
  - Variables begin with lowercase letters.
  - Constants use `CONSTANTS_UPPERCASE`.
- Indent code properly.
- Add comments that explain important code.
- Use constants where appropriate.
- Make effective use of loops, decision structures, and predefined methods.
- Use arrays, `ArrayList`, or another appropriate data structure.
- Use programmer-defined methods to reduce repetitive code and improve readability.
- Read from and/or write to a file.
- Keep the code efficient, modular, reusable, and maintainable.

### UI And UX

- Make the interface easy to use and intuitive.
- Keep the GUI professional looking and well designed.
- If not using the provided Board class, pay extra attention to GUI design quality.
