# Commandes de Presse-papier (Clipboard) et Transformations

Ce document détaille les commandes de copie, de collage et de transformation pour **MAW (Minestom Async WorldEdit)**.

---

## Sommaire
- [//copy](#copy)
- [//paste](#paste)
- [//rotate](#rotate)
- [//flip](#flip)

---

## `//copy`
*(Alias : `/copy`, `copy`)*

Copie la sélection active dans le presse-papier du joueur, avec un décalage relatif par rapport à la position actuelle du joueur.

### Syntaxe
```text
//copy [-e]
```

### Options
- **`-e`** : Inclut les entités présentes dans la sélection (hors joueurs) dans la copie.

### Exemple
1. Placez-vous à l'entrée d'un bâtiment sélectionné.
2. Exécutez `//copy`. Le point d'entrée servira de point d'ancrage lors du futur collage (`//paste`).

---

## `//paste`
*(Alias : `/paste`, `paste`)*

Colle le contenu du presse-papier à la position actuelle du joueur.

### Syntaxe
```text
//paste [-a] [-e] [-u]
```

### Options
- **`-a` (Air)** : Ignore les blocs d'air présents dans le presse-papier lors du collage (évite d'écraser les blocs existants avec de l'air).
- **`-e` (Entities)** : Colle également les entités copiées (si elles avaient été capturées avec `//copy -e`).
- **`-u` (Update)** : Met à jour les blocs adjacents lors du collage.

### Exemple
- `//paste -a` : Colle le bâtiment sans remplacer les blocs existants par de l'air.

---

## `//rotate`
*(Alias : `/rotate`, `rotate`)*

Fait pivoter le contenu du presse-papier dans le sens horaire autour de l'axe vertical (Y).

### Syntaxe
```text
//rotate <angle>
```
L'angle doit être un multiple de 90 degrés (`90`, `180`, `270`).

> [!TIP]
> La rotation ajuste également l'orientation des blocs orientables (ex: les escaliers tournés vers le nord seront orientés vers l'est après une rotation de 90°).

### Exemple
- `//rotate 90` : Fait pivoter le presse-papier d'un quart de tour vers la droite.

---

## `//flip`
*(Alias : `/flip`, `flip`)*

Applique une symétrie axiale (miroir) au presse-papier selon la direction choisie.

### Syntaxe
```text
//flip [north|south|east|west|up|down]
```
Si aucune direction n'est fournie, la direction par défaut est `north`.

### Exemple
- `//flip east` : Inverse le contenu de gauche à droite sur l'axe est-ouest.
