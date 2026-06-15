# Ochroniarz symulator (Security Guard Simulator)

## Description
A 3D first-person security guard simulator built with Godot 4.5 and the **Godot Kotlin/JVM** plugin. Patrol a shop, watch for thieves stealing beer, engage in stamina-based melee combat, and drag knocked-out perpetrators to an incident drop zone. The game runs across multiple in-game shifts with a save/load system.

## Quick start

```bash
# 1. Clone the repository
git clone <repository-url>
cd ochroniarz-symulator

# 2. Set JAVA_PATH to JDK 21 (see "Environment configuration" below)

# 3. Generate Godot script registration files
./gradlew generateGodotRegistration

# 4. Download Godot Kotlin/JVM editor from GitHub releases (see "Godot editor" below)
# 5. Open project.godot in the custom editor and press F5
```

## Prerequisites

### Java 21 (JDK)
A **JDK 21** distribution is required. The Gradle toolchain uses it to compile Java source code, and the Godot Kotlin/JVM plugin launches the JVM with it.

| Distribution | Download |
|---|---|
| **Eclipse Temurin** (recommended) | [adoptium.net](https://adoptium.net/temurin/releases/?version=21) |
| **Oracle JDK** | [oracle.com/java](https://www.oracle.com/java/technologies/downloads/#java21) |
| **Amazon Corretto** | [aws.amazon.com/corretto](https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/downloads-list.html) |

Verify the installation:
```bash
java -version
# Expected: openjdk version "21" ... 2025-XX-XX
```

### Godot Kotlin/JVM plugin — custom Godot editor
> **IMPORTANT:** This project requires a **custom Godot build** from the `godot-kotlin-jvm` project. The official Godot editor from godotengine.org will **not** work.

| Component | Version |
|---|---|
| **Godot editor (custom build)** | `0.14.3-4.5.1` — download from [GitHub releases](https://github.com/utopia-rise/godot-kotlin-jvm/releases/tag/0.14.3-4.5.1) |
| **Gradle plugin** | `com.utopia-rise.godot-kotlin-jvm` version `0.14.3-4.5.1` (resolved automatically) |
| **Gradle** | 8.14.4 (bundled via `gradlew` / `gradlew.bat`) |

On the release page, download the archive matching your OS — e.g. `godot-linux.x86_64-release.zip`, `godot-windows.x86_64-release.zip`, or `godot-macos.zip`.

> **No IDE plugin available:** Due to a JetBrains plugin policy change, version 0.14.x does not ship an IDE plugin. All Gradle tasks must be run from the terminal.

### Supported operating systems
The Godot Kotlin/JVM plugin supports these editor/export targets:

| Platform | Editor | Export |
|---|---|---|
| Windows x86_64 | Yes | Yes |
| Linux x86_64 | Yes | Yes |
| macOS x86_64 / arm64 | Yes | Yes |
| Android (arm64-v8a, x86_64) | — | Yes |
| iOS (arm64) | — | Yes |

## Installation and setup

### 1. Environment configuration

Set the `JAVA_PATH` environment variable to point to your JDK 21 `java` executable. This tells Godot which JVM to use when loading the Kotlin/JVM plugin.

**Windows (PowerShell) — current session:**
```powershell
$env:JAVA_PATH = "C:\Program Files\Java\jdk-21\bin\java.exe"
```

**Windows (PowerShell) — permanent (machine-wide):**
```powershell
[System.Environment]::SetEnvironmentVariable('JAVA_PATH','C:\Program Files\Java\jdk-21\bin\java.exe','Machine')
```

**Windows (Command Prompt):**
```cmd
set JAVA_PATH=C:\Program Files\Java\jdk-21\bin\java.exe
```

**Linux (bash):**
```bash
export JAVA_PATH=/usr/lib/jvm/jdk-21/bin/java
```

**macOS (zsh):**
```zsh
export JAVA_PATH=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home/bin/java
```

To make the variable permanent on Linux/macOS, add the `export` line to `~/.bashrc`, `~/.zshrc`, or `~/.profile`.

> **Verify:** Restart your terminal and run `echo $JAVA_PATH` (macOS/Linux) or `echo %JAVA_PATH%` (Windows CMD) to confirm the variable is set.

### 2. Project configuration

#### Step 1 — Generate script registration files
```bash
./gradlew generateGodotRegistration
```
On Windows:
```cmd
gradlew.bat generateGodotRegistration
```

This generates `.gdj` files inside `scripts/`. These files tell Godot which Java classes are available as scripts. Re-run this task whenever you add, rename, or remove Java classes.

#### Step 2 — (Optional) Build the project
Gradle compiles Java sources and packages them automatically when Godot launches. To build manually:
```bash
./gradlew build
```

### 3. Running the project

1. **Launch the custom Godot Kotlin/JVM editor** (the build downloaded from the GitHub releases, **not** the official Godot executable).
2. Click **Import** and select the file `project.godot` from the project folder.
3. Wait for the JVM plugin to initialise — the editor output log will show messages from the plugin.
4. Press **F5** or click the **Play** button.

**Expected result:** The main menu appears. You can start a new game, load a saved game, adjust settings, or quit.

> **First launch** is slower because Gradle downloads dependencies and the plugin resolves all Java classes. Subsequent launches are faster thanks to caching.

> **Trouble launching?** If the game window never appears or Godot hangs, check `source/godot_kotlin_configuration.json` — if `wait_for_debugger` is `true` (the default), the JVM waits for a debugger on port 5005 before starting. Set it to `false` or attach a debugger (see troubleshooting below).

### 4. Exporting the game

An export preset for **Linux x86_64** is already configured in `export_presets.cfg`. To export:
1. In the Godot editor, go to **Project → Export**.
2. Select the **Linux** preset and click **Export Project**.
3. Choose an output path — the default is `exports/Ochroniarz symulator.x86_64`.

Export templates for the Kotlin/JVM build must be downloaded separately from the [GitHub releases page](https://github.com/utopia-rise/godot-kotlin-jvm/releases/tag/0.14.3-4.5.1). Look for assets named `godot-export-templates-...`.

For other platforms (Windows, macOS, Android, iOS), additional configuration in `build.gradle.kts` and `export_presets.cfg` is required.

## Project structure

```
├── source/                        # Java source code (main game logic)
│   ├── Civilian/                  #   Civilian NPC (non-hostile, 70% of spawns)
│   ├── Enemy/                     #   Thief NPC (hostile, 30% of spawns)
│   ├── Game/                      #   GameLoop, managers, save/load, world objects
│   │   └── Rzeczy/                #     IncidentDropZone, Puddle
│   ├── Level/                     #   Level loading, world objects (doors, PC)
│   │   └── Rzeczy/                #     3D assets and scene files for the shop
│   ├── Menu/                      #   Main menu, pause menu, settings
│   ├── NPC/                       #   Base NPC class with finite-state machine
│   ├── Player/                    #   First-person controller, HUD, stamina
│   ├── Spawner/                   #   Periodic NPC spawner
│   └── Transition/                #   Fade in/out scene transitions
├── scripts/                       # Auto-generated Godot registration files (.gdj)
├── scenes/                        # Additional Godot scenes (puddles, drop zone)
├── gradle/wrapper/                # Gradle Wrapper (gradlew + gradlew.bat)
├── source/Menu/Blur.gdshader      # Gaussian blur shader for menu backgrounds
├── build.gradle.kts               # Gradle build script (Godot Kotlin/JVM plugin)
├── settings.gradle.kts            # Gradle project name and plugins
├── gradle.properties              # Gradle JVM arguments (-Xmx3G)
├── project.godot                  # Godot project configuration
├── godot_kotlin_configuration.json# JVM startup and debug settings
└── export_presets.cfg             # Export preset for Linux x86_64
```

## Key features

- **First-person 3D movement** — WASD + mouse look, sprint (Shift) with stamina drain, jump (Space)
- **Melee combat** — Left-click attack with stamina cost; knock out thieves
- **NPC AI with finite-state machine** — Civilians (70%) and thieves (30%) navigate the shop: queue at entrance, patrol shelves, pay at cashier, exit. Thieves can skip the cashier and escape
- **Drag & drop** — Drag knocked-out enemies to an incident drop zone to register the crime
- **Shift system** — 8 in-game hours (3 real minutes per hour), day counter, escape limit (game over at 5 escapes)
- **Save / Load** — JSON-based persistence via Godot `FileAccess`
- **Full menu system** — Main menu, pause menu, settings (volume sliders per bus, fullscreen toggle)
- **Gaussian blur shader** — Real-time blur effect behind menu panels
- **Slip physics** — Puddles cause the player to slip when sprinting through them
- **Interactable objects** — Doors (open/close with cooldown), PC terminal (start shift), incident drop zone

## Configuration reference

### `godot_kotlin_configuration.json`

| Key | Value | Description |
|---|---|---|
| `wait_for_debugger` | `true` | JVM pauses on startup and waits for a debugger to attach on port 5005. **Set to `false` for normal gameplay.** |
| `debug_port` | `5005` | JDWP debug port for remote debugging from IntelliJ / VS Code. |
| `debug_address` | `*` | Listen address for the debugger (all interfaces). |
| `vm_type` | `auto` | JVM selection strategy (`auto`, `jdk`, or `jre`). |
| `use_debug` | `false` | Use debug JVM build if `true`. |
| `disable_gc` | `false` | Disable Kotlin object garbage collection (not recommended). |
| `max_string_size` | `-1` | Maximum string size Godot can pass to JVM (`-1` = unlimited). |

### Controls (from `project.godot`)

| Action | Key |
|---|---|
| Move forward | **W** |
| Move backward | **S** |
| Move left | **A** |
| Move right | **D** |
| Jump | **Space** |
| Sprint | **Shift** |
| Interact | **F** |
| Attack | **Left mouse button** |

## Troubleshooting

| Problem | Likely cause | Solution |
|---|---|---|
| **Godot does not start / JVM not found** | `JAVA_PATH` is not set or points to the wrong path | Set `JAVA_PATH` to the full path of `java` inside a JDK 21 installation. Restart Godot. |
| **"Java 21 required" error during Gradle build** | `JAVA_HOME` or toolchain points to an older JDK | Install JDK 21 and ensure `java -version` prints version 21. |
| **Game window hangs on startup** | `wait_for_debugger` is `true` in `godot_kotlin_configuration.json` | Set `"wait_for_debugger": false` and restart Godot. |
| **".gdj file not found" in editor** | Registration files are missing or out of date | Run `./gradlew generateGodotRegistration` to regenerate them. |
| **Gradle runs out of memory** | Default heap is insufficient for large projects | Increase `org.gradle.jvmargs=-Xmx4G` or higher in `gradle.properties`. |
| **Build fails with "Kotlin compiler plugin" error** | Kotlin version incompatibility | The plugin version in `build.gradle.kts` defines the Kotlin version. See [gradle-plugin-configuration](https://godot-kotl.in/en/user-guide/advanced/gradle-plugin-configuration/). |
| **No such method / class not found at runtime** | `.gdj` files are stale after renaming/moving Java classes | Regenerate registration files and restart Godot. |
| **ClassCastException between Godot built-in types** | Type mismatch in JNI bridge | Ensure you are importing `godot.*` types and not mixing Kotlin/Java wrappers incorrectly. |
| **Windows antivirus blocks Gradle** | Real-time scanning interferes with the JVM | Add an exclusion for the project folder and `%USERPROFILE%\.gradle\`. |
| **Export fails — no export templates** | Kotlin/JVM export templates not installed | Download the matching export templates from the [GitHub releases page](https://github.com/utopia-rise/godot-kotlin-jvm/releases/tag/0.14.3-4.5.1). |

## FAQ

**Q: Can I use the official Godot editor?**
No. The Godot Kotlin/JVM plugin requires a **custom engine build** available on the [GitHub releases page](https://github.com/utopia-rise/godot-kotlin-jvm/releases). The official Godot editor does not include the JVM module.

**Q: Is Java fully supported by Godot Kotlin/JVM?**
Java support is **experimental** but functional. The plugin's primary language is Kotlin; Java classes are supported via the same registration system. See the [official docs on JVM language support](https://godot-kotl.in/en/contribution/support-for-other-jvm-based-languages/).

**Q: How do I debug Java scripts?**
Attach a remote debugger to port `5005` (set in `godot_kotlin_configuration.json`). In IntelliJ, create a **Remote JVM Debug** configuration with `-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005`. Ensure `wait_for_debugger` is `true` while debugging and `false` for normal runs.

**Q: How do I add a new Java class and make it visible to Godot?**
1. Create the `.java` file under `source/` (e.g., `source/MyClass.java`).
2. Add the `@RegisterClass` annotation to your class.
3. Run `./gradlew generateGodotRegistration` to update the `.gdj` files.
4. Restart Godot — the new class appears in the script list.

**Q: The game over screen shows — how do I restart?**
The game-over screen appears when 5 thieves escape. Press the button in the HUD to return to the main menu and start a fresh game.

## Java code example

A minimal script registered with Godot follows this pattern:

```java
package mygame;

import godot.*;
import godot.annotation.RegisterClass;
import godot.annotation.RegisterFunction;

@RegisterClass
public class MyNode extends Node2D {

    @RegisterFunction
    public void _ready() {
        GD.print("Hello from Java!");
    }

    @RegisterFunction
    @Override
    public void _process(double delta) {
        // Called every frame
    }
}
```

> **Note:** Only default (no-arg) constructors can be registered. Use `_ready()` for initialisation logic.

## Contributing / Development

1. The project uses **Java 21** with the **Godot Kotlin/JVM** plugin `0.14.3-4.5.1`. Source files are in `source/`.
2. After adding or renaming Java classes, regenerate `.gdj` files:
   ```bash
   ./gradlew generateGodotRegistration
   ```
3. Keep `scripts/` in version control so team members can run the project without triggering a full Gradle build.
4. Code comments are currently in **Polish** — please maintain this convention.
5. Test changes by running from the Godot editor. Check the editor output log for JVM errors.
6. If you encounter issues with the plugin, consult the [official documentation](https://godot-kotl.in/) or ask on the [Discord server](https://discord.gg/zpb5Ru7v9x).

## License

This project is currently **unlicensed**. No license file has been provided.
