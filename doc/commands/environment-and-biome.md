# Commandes d'Environnement et de Biomes

Ce document détaille l'utilisation des commandes de gestion de l'environnement, des fluides et des biomes dans **MAW (Minestom Async WorldEdit)**.

---

## Sommaire
- [Gestion des Fluides](#gestion-des-fluides)
  - [//drain](#drain)
  - [//fixwater](#fixwater)
  - [//fixlava](#fixlava)
- [Altérations Environnementales](#altérations-environnementales)
  - [//extinguish](#extinguish)
  - [//snow](#snow)
  - [//thaw](#thaw)
  - [//green](#green)
- [Gestion des Entités](#gestion-des-entités)
  - [//butcher](#butcher)
  - [//remove](#remove)
- [Gestion des Biomes](#gestion-des-biomes)
  - [//setbiome](#setbiome)
  - [//biomeinfo](#biomeinfo)

---

## Gestion des Fluides

### `//drain`
*(Alias : `/drain`, `drain`)*

Assèche les blocs d'eau, de lave et les blocs gorgés d'eau (*waterlogged*) dans un rayon donné autour du joueur.

```text
//drain <rayon>
```

### `//fixwater` / `//fixlava`
*(Alias : `/fixwater`, `/fixlava`)*

Nivelle et transforme les fluides en écoulement en sources stables dans un rayon donné.

```text
//fixwater <rayon>
//fixlava <rayon>
```

---

## Altérations Environnementales

### `//extinguish`
*(Alias : `/extinguish`, `//ex`, `/ex`)*

Éteint tous les feux (`minecraft:fire`, `minecraft:soul_fire`) dans un rayon donné.

```text
//extinguish [rayon]
//ex 20
```

### `//snow`
*(Alias : `/snow`, `snow`)*

Simule une chute de neige : gèle les plans d'eau en glace et dépose une couche de neige au sommet des blocs exposés.

```text
//snow [rayon]
```

### `//thaw`
*(Alias : `/thaw`, `thaw`)*

Fait fondre la neige et transforme la glace en eau dans un rayon donné.

```text
//thaw [rayon]
```

### `//green`
*(Alias : `/green`, `green`)*

Transforme les blocs de terre (`minecraft:dirt`, `minecraft:coarse_dirt`) exposés à l'air en blocs d'herbe (`minecraft:grass_block`).

```text
//green [rayon]
```

---

## Gestion des Entités

### `//butcher`
*(Alias : `/butcher`, `butcher`)*

Supprime les créatures vivantes (monstres et animaux) dans un rayon donné sans affecter les joueurs.

```text
//butcher [-p] [-n] [rayon]
```

### `//remove`
*(Alias : `/remove`, `remove`)*

Supprime des types d'entités spécifiques (ex: objets au sol, flèches, bateaux).

```text
//remove <type> [rayon]
```

---

## Gestion des Biomes

### `//setbiome`
*(Alias : `/setbiome`, `setbiome`)*

Change le biome de tous les blocs de la sélection active de façon asynchrone.

```text
//setbiome [-p] <biome>
```
- `-p` : Applique le biome aux coordonnées exactes du joueur si aucune sélection n'est active.
- `<biome>` : Identifiant du biome (ex: `minecraft:desert`, `minecraft:plains`, `minecraft:cherry_grove`).

### `//biomeinfo`
*(Alias : `/biomeinfo`, `biomeinfo`)*

Affiche le biome actuel au niveau des pieds du joueur ou des points de sélection.

```text
//biomeinfo
```
