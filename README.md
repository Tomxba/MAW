<div align="right">
  <strong>English</strong> | <a href="README.fr.md">Français</a>
</div>

# MAW (Minestom Async WorldEdit)

MAW is a high-performance asynchronous world-editing library built from scratch for **Minestom**, inspired by FastAsyncWorldEdit (FAWE). It is designed to maximize performance without impacting the server's game loop (maintaining a steady 20.0 TPS), even when processing operations involving millions of blocks.

## Why MAW?
- **100% Asynchronous:** Leverages modern **Java 25 Virtual Threads** to execute heavy geometric calculations and volume iterations in the background.
- **Chunk-Sorting:** Automatically groups modifications by chunk coordinates to optimize memory and Minestom block palettes.
- **Time-Budgeting:** Spreads massive operations across multiple consecutive ticks to prevent server TPS drops.
- **Optimized History:** Compact storage of block deltas for fast Undo/Redo actions with minimal memory footprint.

## Quick Start

### Prerequisites
- Java 25 or higher
- Minestom (2026.09.12-26.2 or newer)

### 1. Add Dependency (Gradle)
```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Tomxba:MAW:1.0.0'
}
```

### 2. Initialization (1 line)
```java
import fr.maw.Maw;
import net.minestom.server.MinecraftServer;

public class MyServer {
    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();

        // Initialize MAW
        Maw.init(MinecraftServer.getGlobalEventHandler());

        server.start("0.0.0.0", 25565);
    }
}
```

## Features & Toolsets

MAW delivers the full WorldEdit / FAWE toolset rebuilt natively for Minestom:

| Category | Commands & Capabilities | Documentation |
| :--- | :--- | :--- |
| **Selection & Modifiers** | `//wand`, `//pos1`, `//pos2`, `//hpos1`, `//hpos2`, `//expand`, `//contract`, `//shift`, `//inset`, `//outset`, `//chunk`, `//sel`, `//desel`, `//distr` | [`doc/commands/selection.md`](doc/commands/selection.md) |
| **Region Operations** | `//set`, `//replace`, `//walls`, `//faces`, `//outline`, `//hollow`, `//move`, `//stack`, `//naturalize`, `//overlay`, `//smooth`, `//line`, `//center`, `//fall`, `//forest`, `//flora` | [`doc/commands/region.md`](doc/commands/region.md) |
| **Shapes & Generation** | `//sphere`, `//hsphere`, `//cyl`, `//hcyl`, `//pyramid`, `//hpyramid`, `//cone`, `//hcone`, `//torus`, `//htorus`, `//ellipsoid`, `//hellipsoid` | [`doc/commands/generation.md`](doc/commands/generation.md) |
| **Clipboards & Schematics** | `//copy`, `//cut`, `//paste`, `//rotate`, `//flip`, `//schem save`, `//schem load`, `//schem list`, `//schem delete`, `//clearclipboard` (`.maw` format) | [`doc/commands/clipboard.md`](doc/commands/clipboard.md), [`doc/commands/schematic.md`](doc/commands/schematic.md) |
| **Interactive Brushes** | `/brush sphere`, `cyl`, `smooth`, `clipboard`, `gravity`, `raise`, `lower`, `flatten`, `paint`, `/mask`, `/size`, `/range`, `/mat` | [`doc/commands/brush-and-tool.md`](doc/commands/brush-and-tool.md) |
| **Specialized Tools** | `/tool info`, `repl`, `cycler`, `tree`, `deltree`, `lrbuild`, `floodfill`, `farwand`, `/none` | [`doc/commands/brush-and-tool.md`](doc/commands/brush-and-tool.md) |
| **Environment & Fluids** | `//drain`, `//fixwater`, `//fixlava`, `//extinguish` (`//ex`), `//snow`, `//thaw`, `//green`, `//butcher`, `//remove` | [`doc/commands/environment-and-biome.md`](doc/commands/environment-and-biome.md) |
| **Biome Management** | `//setbiome`, `//biomeinfo` (supports Minecraft 1.21+ dynamic biomes) | [`doc/commands/environment-and-biome.md`](doc/commands/environment-and-biome.md) |
| **Navigation** | `//up`, `//ceil`, `//jumpto` (`/j`), `//thru`, `//unstuck`, `//ascend`, `//descend`, `//top` | [`doc/commands/navigation.md`](doc/commands/navigation.md) |
| **Session & History** | `//undo`, `//redo`, `//clearhistory`, `//gmask`, `//fast`, `/maw reload` | [`doc/commands/history.md`](doc/commands/history.md), [`doc/patterns-and-masks.md`](doc/patterns-and-masks.md) |

> 📄 For architectural details, permission guards, and batching mechanisms, explore the [`doc/`](doc/) directory.

## Build & Test
```bash
./gradlew check
./gradlew jar
```

## License
Distributed under the MIT License.

