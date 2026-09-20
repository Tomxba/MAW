<div align="right">
  <a href="README.md">English</a> | <strong>Français</strong>
</div>

# MAW (Minestom Async WorldEdit)

MAW est une bibliothèque d'édition de monde asynchrone ultra-performante pour le serveur Minecraft **Minestom**, inspirée de FastAsyncWorldEdit (FAWE). Elle est conçue pour offrir des performances maximales sans impacter la boucle de jeu (maintien à 20.0 TPS), même lors de modifications portant sur des millions de blocs.

## Pourquoi MAW ?
- **100% Asynchrone :** Exploite les **Virtual Threads de Java 25** pour exécuter tous les calculs géométriques en tâche de fond.
- **Chunk-Sorting :** Regroupement automatique des modifications par coordonnées de chunk pour optimiser la mémoire.
- **Time-Budgeting :** Étalement des opérations massives sur plusieurs ticks pour préserver les performances du serveur.
- **Historique optimisé :** Gestion intelligente des Undo/Redo avec une faible empreinte mémoire.

## Installation et Démarrage Rapide

### Prérequis
- Java 25 ou supérieur
- Minestom (2026.09.12-26.2 ou version ultérieure)

### 1. Ajout de la dépendance (Gradle)
```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Tomxba:MAW:1.0.0'
}
```

### 2. Initialisation en 1 ligne
```java
import fr.maw.Maw;
import net.minestom.server.MinecraftServer;

public class MyServer {
    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();

        // Initialisation de MAW
        Maw.init(MinecraftServer.getGlobalEventHandler());

        server.start("0.0.0.0", 25565);
    }
}
```

## Fonctionnalités & Outils Disponibles

MAW propose l'intégralité de la suite d'outils WorldEdit / FAWE, réimplémentée nativement pour Minestom :

| Catégorie | Commandes & Capacités | Documentation |
| :--- | :--- | :--- |
| **Sélections & Modificateurs** | `//wand`, `//pos1`, `//pos2`, `//hpos1`, `//hpos2`, `//expand`, `//contract`, `//shift`, `//inset`, `//outset`, `//chunk`, `//sel`, `//desel`, `//distr` | [`doc/commands/selection.md`](doc/commands/selection.md) |
| **Opérations de Région** | `//set`, `//replace`, `//walls`, `//faces`, `//outline`, `//hollow`, `//move`, `//stack`, `//naturalize`, `//overlay`, `//smooth`, `//line`, `//center`, `//fall`, `//forest`, `//flora` | [`doc/commands/region.md`](doc/commands/region.md) |
| **Formes & Génération** | `//sphere`, `//hsphere`, `//cyl`, `//hcyl`, `//pyramid`, `//hpyramid`, `//cone`, `//hcone`, `//torus`, `//htorus`, `//ellipsoid`, `//hellipsoid` | [`doc/commands/generation.md`](doc/commands/generation.md) |
| **Presse-papier & Schématiques** | `//copy`, `//cut`, `//paste`, `//rotate`, `//flip`, `//schem save`, `//schem load`, `//schem list`, `//schem delete`, `//clearclipboard` (format `.maw`) | [`doc/commands/clipboard.md`](doc/commands/clipboard.md), [`doc/commands/schematic.md`](doc/commands/schematic.md) |
| **Pinceaux Interactifs (Brushes)** | `/brush sphere`, `cyl`, `smooth`, `clipboard`, `gravity`, `raise`, `lower`, `flatten`, `paint`, `/mask`, `/size`, `/range`, `/mat` | [`doc/commands/brush-and-tool.md`](doc/commands/brush-and-tool.md) |
| **Outils Spécialisés** | `/tool info`, `repl`, `cycler`, `tree`, `deltree`, `lrbuild`, `floodfill`, `farwand`, `/none` | [`doc/commands/brush-and-tool.md`](doc/commands/brush-and-tool.md) |
| **Environnement & Fluides** | `//drain`, `//fixwater`, `//fixlava`, `//extinguish` (`//ex`), `//snow`, `//thaw`, `//green`, `//butcher`, `//remove` | [`doc/commands/environment-and-biome.md`](doc/commands/environment-and-biome.md) |
| **Gestion des Biomes** | `//setbiome`, `//biomeinfo` (support dynamique des biomes Minecraft 1.21+) | [`doc/commands/environment-and-biome.md`](doc/commands/environment-and-biome.md) |
| **Navigation Rapide** | `//up`, `//ceil`, `//jumpto` (`/j`), `//thru`, `//unstuck`, `//ascend`, `//descend`, `//top` | [`doc/commands/navigation.md`](doc/commands/navigation.md) |
| **Session & Historique** | `//undo`, `//redo`, `//clearhistory`, `//gmask`, `//fast`, `/maw reload` | [`doc/commands/history.md`](doc/commands/history.md), [`doc/patterns-and-masks.md`](doc/patterns-and-masks.md) |

> 📄 Pour la documentation détaillée sur l'architecture, les gardes de permissions et le système de batching, explorez le répertoire [`doc/`](doc/).

## Compilation & Tests
```bash
./gradlew check
./gradlew jar
```

## Licence
Distribué sous licence MIT.

