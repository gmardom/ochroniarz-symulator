# Ochroniarz symulator (Security Guard Simulator)

## Description
A 3D first-person security guard simulator built with Godot 4.5 and the Godot Kotlin JVM plugin. The player patrols a shop as a security guard, watches for thieves who steal beer, engages them in stamina-based melee combat, and drags knocked-out perpetrators to an incident drop zone to resolve crimes across multiple in-game shifts.

## Prerequisites
- **Java 21** (JDK 21) — required by the Godot Kotlin JVM plugin toolchain
- **Godot 4.5** — Mono version is **not** required; use the standard `.NET`-free Godot 4.5 executable
- **Godot Kotlin JVM plugin v0.14.3-4.5.1** — resolved automatically by Gradle
- **Gradle 8.14.4** — bundled via the Gradle Wrapper (`gradlew` / `gradlew.bat`)

## Installation and setup

### 1. Environment configuration

Set the `JAVA_PATH` environment variable to point to your JDK 21 installation. This tells Godot which JVM to use when loading the Kotlin JVM plugin.

**Windows (PowerShell):**
```powershell
$env:JAVA_PATH = "C:\Program Files\Java\jdk-21\bin\java.exe"
```

**Windows (Command Prompt):**
```cmd
set JAVA_PATH=C:\Program Files\Java\jdk-21\bin\java.exe
```

**Linux / macOS:**
```bash
export JAVA_PATH=/usr/lib/jvm/jdk-21/bin/java
```

> **Note:** Verify your Java version with `java -version`. It must report **21** (e.g., `openjdk version "21"`).

### 2. Project configuration

```bash
# Clone the repository
git clone <repository-url>
cd ochroniarz-symulator

# Generate Godot script registration files (`.gdj`)
./gradlew generateGodotRegistration
```

On Windows use `gradlew.bat` instead:
```cmd
gradlew.bat generateGodotRegistration
```

> The `.gdj` registration files are auto-generated from the Java sources and tell Godot which classes are available as scripts. Run the generation task whenever new Java classes are added or moved.

### 3. Running the project

1. Open Godot 4.5 and click **Import**.
2. Navigate to the project folder and select `project.godot`.
3. Once the project opens, the JVM plugin will automatically detect `JAVA_PATH` and start the JVM.
4. Click the **Play** button (or press **F5**) to run the game.

**Expected result:** The main menu appears. You can start a new game, load a saved game, adjust settings, or quit.

> **First launch may be slower** as Gradle downloads dependencies and the Godot Kotlin JVM plugin resolves the Java source code.

## Project structure

```
├── source/                    # Java source code (main game logic)
│   ├── Civilian/              # Civilian NPC (non-hostile)
│   ├── Enemy/                 # Thief/enemy NPC (hostile, steals beer)
│   ├── Game/                  # Game loop, managers, save system, world objects
│   │   └── Rzeczy/            # IncidentDropZone, Puddle
│   ├── Level/                 # Level loading, interactable objects (doors, PC)
│   │   └── Rzeczy/            # 3D assets and scene files for the shop level
│   ├── Menu/                  # Main menu, pause menu, settings (audio, video)
│   ├── NPC/                   # Base NPC class with state machine
│   ├── Player/                # First-person controller, HUD, stamina, raycast
│   ├── Spawner/               # NPC spawner
│   └── Transition/            # Scene transition effects
├── scripts/                   # Auto-generated Godot registration files (.gdj)
├── scenes/                    # Additional Godot scenes (puddles, drop zone)
├── gradle/wrapper/            # Gradle Wrapper (gradlew, gradlew.bat)
├── build.gradle.kts           # Gradle build script (Godot Kotlin JVM plugin)
├── settings.gradle.kts        # Gradle project settings
├── gradle.properties          # Gradle JVM arguments
├── project.godot              # Godot project configuration
├── godot_kotlin_configuration.json  # JVM debug/configuration settings
└── export_presets.cfg         # Export presets (Linux x86_64)
```

## Key features

- **First-person 3D movement** — WASD + mouse look with sprint and stamina management
- **Melee combat** — Attack thieves with stamina-based strikes; knock them out
- **NPCAI with state machine** — Civilians (70%) and thieves (30%) with full navigation: queue, patrol shelves, pay at cashier, exit; thieves can steal and escape
- **Drag & drop** — Drag knocked-out enemies to an incident drop zone to resolve the crime
- **Shift system** — 8 in-game hours (3 real-time minutes per hour) with day tracking
- **Save / Load** — Persistent game state saved as JSON via Godot `FileAccess`
- **Menu system** — Main menu, pause menu, settings (volume sliders, fullscreen toggle)
- **Blur shader** — Real-time Gaussian blur for menu backgrounds
- **Slip physics** — Puddles cause the player to slip when sprinting

## Troubleshooting

| Problem | Solution |
|---|---|
| **Godot cannot find JVM** | Verify `JAVA_PATH` points to a JDK 21 `java` executable. Restart Godot after changing the variable. |
| **Build fails with Java version error** | Ensure `java -version` reports **21**. Install JDK 21 from [Adoptium](https://adoptium.net/) or your package manager. |
| **Gradle out of memory** | Increase `org.gradle.jvmargs` in `gradle.properties` (default is `-Xmx3G`). |
| **".gdj file not found" errors** | Run `./gradlew generateGodotRegistration` to regenerate script registration files. |
| **Godot Kotlin JVM plugin version mismatch** | The plugin version is pinned in `build.gradle.kts`. See the [plugin documentation](https://godot-kotl.in/) for compatible versions. |
| **Windows Antivirus blocking Gradle** | Add an exclusion for the project folder or the Gradle cache directory (`%USERPROFILE%\.gradle`). |

## Contributing / Development

1. The project uses **Java 21** with the **Godot Kotlin JVM** plugin. Java source files reside in `source/`.
2. After adding or renaming Java classes, regenerate `.gdj` files: `./gradlew generateGodotRegistration`.
3. Keep the `scripts/` directory in version control so other developers can run the project without Gradle.
4. Code comments are currently in **Polish** — follow the existing style when contributing.
5. Test your changes by running the game from the Godot editor.

## License

This project is currently **unlicensed**. No license file has been provided.
