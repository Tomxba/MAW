# MAW - Minestom Async WorldEdit

**MAW** est une bibliothèque d'édition de monde asynchrone pour le serveur Minecraft **[Minestom](https://minestom.net/)**, inspirée de **FastAsyncWorldEdit (FAWE)**.

Elle est spécialement conçue pour offrir des performances maximales sans impacter la boucle de jeu du serveur (maintien à 20.0 TPS), même lors de modifications portant sur des millions de blocs.

---

## Sommaire

1. [Présentation du projet](#1-présentation-du-projet)
2. [Pourquoi MAW ?](#2-pourquoi-maw-)
3. [Installation et Démarrage Rapide](#3-installation-et-démarrage-rapide)
4. [Résumé des Commandes Disponibles](#4-résumé-des-commandes-disponibles)
   - [Sélection](#sélection)
   - [Région et Remplissage](#région-et-remplissage)
   - [Génération de Formes](#génération-de-formes)
   - [Presse-papier et Transformations](#presse-papier-et-transformations)
   - [Historique](#historique)
   - [Utilitaires](#utilitaires)
5. [Motifs (Patterns) et Masques (Masks)](#5-motifs-patterns-et-masques-masks)
6. [Gestion des Objets Autour et Drapeaux](#6-gestion-des-objets-autour-et-drapeaux)
7. [Documentation Détaillée](#7-documentation-détaillée)
8. [Compilation & Tests](#8-compilation--tests)

---

## 1. Présentation du projet

Dans l'écosystème Minestom, l'absence d'une implémentation moderne et native de WorldEdit laissait les développeurs sans solution simple et performante pour l'édition de cartes en jeu. Les anciens ports reposaient sur l'API lourde d'EngineHub et sont désormais obsolètes.

**MAW** est développé de zéro, spécifiquement pour Minestom et en exploitant **Java 25** (notamment les *Virtual Threads*). Il s'intègre sous forme de dépendance dans votre projet et s'active en une seule ligne de code.

---

## 2. Pourquoi MAW ?

- **Traitement 100% Asynchrone** : Tous les calculs géométriques et itérations de volumes s'exécutent en tâche de fond sur des *Virtual Threads*.
- **Découpage par Chunks (Chunk-Sorting)** : Les modifications sont automatiquement regroupées par coordonnées de chunk pour maximiser l'efficacité de la mémoire et des palettes de blocs de Minestom.
- **Régulation des Ticks (Time-Budgeting)** : Pour les très grandes sélections, l'application est étalée sur plusieurs ticks consécutifs sans jamais faire chuter le TPS du serveur.
- **Historique optimisé (Undo/Redo)** : Stockage compact des deltas de blocs permettant des annulations quasi instantanées avec une faible empreinte mémoire.
- **Gestion intelligente des objets et de la physique** : Prise en compte configurable des entités et des connexions de blocs pour éviter le lag induit par les paquets réseau et les calculs physiques.

Pour plus de détails techniques, consultez [doc/architecture.md](doc/architecture.md).

---

## 3. Installation et Démarrage Rapide

### Prérequis
- **Java 25** (ou supérieur)
- **Minestom** (2026.09.12-26.2 ou version ultérieure)

### 1. Ajout de la dépendance

#### Gradle (Groovy)
```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Tomxba:MAW:1.0.0'
}
```

### 2. Initialisation en 1 ligne dans votre code Minestom

Il suffit d'appeler `Maw.init(...)` lors du démarrage de votre serveur en lui passant l'écouteur d'événements global :

```java
import fr.maw.Maw;
import net.minestom.server.MinecraftServer;

public class MyServer {
    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();

        // Initialisation de MAW en une ligne
        Maw.init(MinecraftServer.getGlobalEventHandler());

        server.start("0.0.0.0", 25565);
    }
}
```

#### Configuration personnalisée (optionnelle)
```java
MawConfig config = MawConfig.builder()
    .maxBlocksPerOperation(5_000_000) // Limite max de blocs
    .maxHistoryPerPlayer(30)          // Nombre max d'annulations
    .maxChunksPerTick(15)             // Limite de chunks par tick
    .defaultUpdatePhysics(false)      // Physique désactivée par défaut
    .defaultManageEntities(true)      // Nettoyage automatique des entités
    .wandItemNamespace("minecraft:wooden_axe")
    .build();

Maw.init(MinecraftServer.getGlobalEventHandler(), config);
```

---

## 4. Résumé des Commandes Disponibles

Chaque commande est documentée en détail avec exemples et paramètres dans le dossier [`doc/`](doc/).

### Sélection
| Commande | Description |
| :--- | :--- |
| `//wand` | Obtient la hache de sélection (clic gauche = pos1, clic droit = pos2). |
| `//pos1 [x y z]` | Définit la position 1 sur vos pieds ou aux coordonnées données. |
| `//pos2 [x y z]` | Définit la position 2 sur vos pieds ou aux coordonnées données. |
| `//hpos1` | Définit la position 1 sur le bloc ciblé à distance. |
| `//hpos2` | Définit la position 2 sur le bloc ciblé à distance. |

📄 *Voir [doc/commands/selection.md](doc/commands/selection.md) pour le guide complet de sélection.*

---

### Région et Remplissage
| Commande | Description |
| :--- | :--- |
| `//set <pattern> [-u] [-e]` | Remplit la sélection avec le motif spécifié. |
| `//replace [mask] <pattern> [-u] [-e]` | Remplace les blocs filtrés par le motif. |
| `//walls <pattern> [-u] [-e]` | Construit les 4 murs verticaux autour de la sélection. |
| `//faces <pattern> [-u] [-e]` | Construit les 6 faces extérieures (boîte creuse). |
| `//hollow [épaisseur] [pattern] [-u] [-e]` | Évide la sélection en conservant les parois. |

📄 *Voir [doc/commands/region.md](doc/commands/region.md) pour les détails et exemples.*

---

### Génération de Formes
| Commande | Description |
| :--- | :--- |
| `//sphere [-h] <pattern> <rayon> [-u] [-e]` | Génère une sphère ou un ellipsoïde plein ou creux (`-h`). |
| `//hsphere <pattern> <rayon> [-u] [-e]` | Raccourci pour générer une sphère creuse. |
| `//cyl [-h] <pattern> <rayon> [hauteur] [-u] [-e]` | Génère un cylindre ou disque plein ou creux (`-h`). |
| `//hcyl <pattern> <rayon> [hauteur] [-u] [-e]` | Raccourci pour générer un cylindre creux. |

📄 *Voir [doc/commands/generation.md](doc/commands/generation.md) pour les détails et exemples.*

---

### Presse-papier et Transformations
| Commande | Description |
| :--- | :--- |
| `//copy [-e]` | Copie la sélection par rapport à la position du joueur. |
| `//paste [-a] [-e] [-u]` | Colle le presse-papier à la position actuelle du joueur. |
| `//rotate <angle>` | Fait pivoter le presse-papier de 90°, 180° ou 270°. |
| `//flip [direction]` | Inverse le presse-papier selon l'axe choisi (north, south, east, west, up, down). |

📄 *Voir [doc/commands/clipboard.md](doc/commands/clipboard.md) pour les détails et exemples.*

---

### Historique
| Commande | Description |
| :--- | :--- |
| `//undo [nombre]` | Annule une ou plusieurs actions précédentes. |
| `//redo [nombre]` | Rétablit une ou plusieurs actions annulées. |
| `//clearhistory` | Efface l'historique de la session pour libérer la mémoire. |

📄 *Voir [doc/commands/history.md](doc/commands/history.md) pour les détails et exemples.*

---

### Utilitaires
| Commande | Description |
| :--- | :--- |
| `//size` | Affiche le volume et les coordonnées de la sélection. |
| `//count <bloc/masque>` | Compte le nombre de blocs correspondants dans la sélection. |

📄 *Voir [doc/commands/utility.md](doc/commands/utility.md) pour les détails et exemples.*

---

## 5. Motifs (Patterns) et Masques (Masks)

MAW propose un moteur d'analyse de motifs complet et intuitif :
- **Blocs simples** : `stone`, `minecraft:obsidian`, `white_wool`
- **États de blocs** : `oak_stairs[facing=south,half=top]`, `lever[powered=true]`
- **Motifs pondérés aléatoires** : `60%stone,30%cobblestone,10%gravel`
- **Masques de sélection** : `#air`, `#existing`, `!bedrock`, `sand,gravel`

📄 *Consultez [doc/patterns-and-masks.md](doc/patterns-and-masks.md) pour la documentation des motifs et masques.*

---

## 6. Gestion des Objets Autour et Drapeaux

Toutes les opérations d'altération du monde acceptent les drapeaux suivants :
- **`-u`** : Met à jour les blocs voisins sur les bordures de la sélection (connexions d'escaliers, barrières, etc.).
- **`-e`** : Traite les entités (capture et restitution dans le presse-papier, nettoyage des entités suspendues lors des suppressions, blocage du drop d'items intempestif).
- **`-a`** : Ignore les blocs d'air lors du collage avec `//paste`.
- **`-h`** : Raccourci pour générer des formes creuses avec `//sphere` et `//cyl`.

📄 *Consultez [doc/flags.md](doc/flags.md) pour plus d'informations.*

---

## 7. Documentation Détaillée

- 📖 [Architecture et Performances](doc/architecture.md)
- 📌 [Commandes de Sélection](doc/commands/selection.md)
- 🧱 [Commandes de Région et Remplissage](doc/commands/region.md)
- 🔮 [Commandes de Génération](doc/commands/generation.md)
- 📋 [Commandes de Presse-papier](doc/commands/clipboard.md)
- ⏪ [Commandes d'Historique](doc/commands/history.md)
- 📊 [Commandes Utilitaires](doc/commands/utility.md)
- 🎨 [Motifs et Masques](doc/patterns-and-masks.md)
- 🚩 [Drapeaux et Entités](doc/flags.md)

---

## 8. Compilation & Tests

Pour compiler le projet et exécuter les tests automatisés :

```bash
# Compilation et vérification des tests
./gradlew check

# Création du JAR de la librairie
./gradlew jar
```

---

## Licence

Distribué sous licence MIT. Consultez le fichier [LICENSE](LICENSE) pour plus d'informations.
