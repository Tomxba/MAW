# Architecture de MAW

Ce document détaille l'architecture interne et les choix d'optimisation de **MAW (Minestom Async WorldEdit)**.

---

## 1. Principes Inspirés de FAWE (FastAsyncWorldEdit)

Le projet FAWE a démontré que l'édition de blocs à grande échelle dans Minecraft nécessite deux principes fondamentaux :
1. **Ne jamais bloquer le thread principal du serveur** pendant le calcul des géométries, le parsing des motifs ou l'itération des coordonnées.
2. **Ne jamais modifier les blocs un par un** de manière aléatoire ou séquentielle sans regroupement spatial.

---

## 2. Pipeline d'Exécution

```
[Commande Joueur] ──> [PlayerSession]
                              │
                              ▼
                      [MawAsyncEngine]
             (Virtual Threads / Asynchrone)
             - Calculs géométriques
             - Tirage des motifs aléatoires
             - Application des masques
             - Tri par coordonnées de Chunks
                              │
                              ▼
                     [ChunkChangeQueue]
             - Groupement des blocs par Chunk
             - Préparation de l'historique ChangeSet
                              │
                              ▼
                      [TickDispatcher]
             - Application par lot (Batching)
             - Répartition par budget de temps (Time-budgeting)
                              │
                              ▼
                      [Instance Minestom]
```

### Explications des composants :

- **`MawAsyncEngine`** : Exploite les *Virtual Threads* natifs de Java 21 / 25 pour un parallélisme optimal et ultra-léger sans bloquer les ticks de boucle de jeu de Minestom.
- **`ChunkChangeQueue`** : Regroupe les coordonnées des blocs selon leur index de chunk calculé par projection 64-bit :
  `chunkIndex = ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL)`.
  Cette structure garantit que tous les blocs d'un même chunk sont traités ensemble.
- **`TickDispatcher`** : Découpe l'ensemble des modifications en tranches de chunks par tick (configurable via `MawConfig`). Même si l'opération concerne 10 000 000 de blocs, le serveur conserve une cadence de 20.0 TPS stable.
- **`AbsoluteBlockBatch` & `ChunkBatch`** : Met à profit le système de batchs de Minestom pour appliquer les changements en masse au niveau des sections et palettes de blocs plutôt qu'au cas par cas.
- **`HistoryManager` & `ChangeSet`** : Enregistre les changements de façon unitaire et inversible sous forme de records `BlockChange`, assurant une empreinte mémoire minimale lors des opérations `//undo` et `//redo`.
