# Codex Guide

## What This Is

This is an Eclipse-style Java Swing air hockey game. It uses only the JDK standard library plus a bundled educational `framework` package.

## Important Paths

- Main class: `src/AirHockeyApp.java`
- Main game logic: `src/AirHockeyGame.java`
- Game objects: `src/Paddle.java`, `Puck.java`, and `Rink.java`
- Framework code: `src/framework/Game.java` and `GameObject.java`
- Generated output: `bin`
- Original project archive: `AirhockeyYoungBrighton.zip`

## How The Code Works

`AirHockeyApp` starts the program. `AirHockeyGame` extends `framework.Game`, configures the window, prompts for player names, creates the rink, paddles, and puck, then handles each frame in `act()`.

`framework.Game` owns the Swing `Timer`, keyboard listeners, object list, and JFrame setup. It calls the game's `act()` method and then calls each added `GameObject`'s `act()` method.

`Puck`, `Paddle`, and `Rink` extend `framework.GameObject`, which is based on `JComponent`. Drawing happens in each object's `paint(Graphics g)` method.

## Common Commands

Compile:

```sh
mkdir -p bin
javac -d bin $(find src -name '*.java')
```

Run:

```sh
java -cp bin AirHockeyApp
```

## Change Guidance

- Preserve the simple beginner-friendly Java style unless asked to refactor.
- Prefer focused changes in the concrete game classes before changing the framework.
- Keep generated build artifacts out of git.
- There are currently no automated tests. Verify changes by compiling and, when useful, launching the Swing app locally.
- Be careful with drawing order: the current code adds `puck`, then paddles, then `rink`, and the framework uses Swing components with null layout.
- Be careful with player bounds: Player 1 is constrained to the left half of the rink and Player 2 to the right half.

## Framework Usage — Extend Game or GameObject Whenever Possible

Any new game entity (powerup, obstacle, effect indicator, etc.) should extend `framework.GameObject` rather than being a plain data class or a raw `JComponent`. Any new screen that has a game loop should extend `framework.Game`.

**Why:** `GameObject` gives you `collides()` (AABB collision against any other `GameObject`), `setX/setY/setSize`, `repaint()`, and the `act()` hook — all wired into the framework's render and update loop for free. Rolling a plain class or a raw `JComponent` means re-implementing positioning, collision, and repaint integration by hand.

**Z-order rule:** objects added first render on top (lower component index = painted last = front). The permanent draw order is: `puck (front) → playerPaddle → opponentPaddle → rink (back)`. When adding a new `GameObject` dynamically at runtime, call:
```java
add(obj);
getContentPane().setComponentZOrder(obj, getContentPane().getComponentCount() - 2);
```
This inserts it just above the rink so it appears on the ice but below the puck and paddles.

**`Game` subclass checklist:**
- Override `setup()` to create and `add()` all objects
- Override `act()` to run one frame of logic
- Call `setDelay(16)` for ~60 fps
- Use `WKeyPressed()`, `AKeyPressed()`, etc. for input

**`GameObject` subclass checklist:**
- Call `setSize(w, h)` and `setX/setY` in the constructor to establish bounds
- Override `paint(Graphics g)` — coordinates are local (0, 0 is top-left of the component)
- Override `act()` — leave empty if the object is driven externally (like `Paddle`)
- Use `collides(other)` for AABB overlap checks against any other `GameObject`

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
