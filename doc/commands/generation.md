# Commandes de Génération de Formes

Ce document détaille les commandes de génération géométrique dans **MAW (Minestom Async WorldEdit)**.

---

## Sommaire
- [//sphere et //hsphere](#sphere-et-hsphere)
- [//cyl et //hcyl](#cyl-et-hcyl)
- [Drapeaux (-u, -e, -h)](#drapeaux)

---

## `//sphere` et `//hsphere`
*(Alias : `/sphere`, `sphere`, `/hsphere`, `hsphere`)*

Génère une sphère ou un ellipsoïde centré sur la position du joueur.

### Syntaxe
- `//sphere [-h] <pattern> <radius> [-u] [-e]`
- `//hsphere <pattern> <radius> [-u] [-e]`

Le paramètre `radius` peut être :
- Une valeur unique pour une sphère (ex: `10`).
- Trois valeurs séparées par une virgule pour un ellipsoïde : `rx,ry,rz` (ex: `10,5,15`).

Le drapeau `-h` ou la commande `//hsphere` génère une sphère creuse (épaisseur de 1 bloc).

### Exemples
- `//sphere stone 5` : Génère une sphère pleine de rayon 5 en pierre.
- `//hsphere glass 15` : Génère une sphère creuse de rayon 15 en verre.
- `//sphere 50%glowstone,50%sea_lantern 10,5,10` : Génère un dôme/ellipsoïde lumineux aplati.

---

## `//cyl` et `//hcyl`
*(Alias : `/cyl`, `cyl`, `/hcyl`, `hcyl`)*

Génère un cylindre plein ou creux dont la base est centrée sur la position du joueur.

### Syntaxe
- `//cyl [-h] <pattern> <radius> [hauteur] [-u] [-e]`
- `//hcyl <pattern> <radius> [hauteur] [-u] [-e]`

Le paramètre `radius` peut être :
- Une valeur unique pour un cercle parfait (ex: `8`).
- Deux valeurs séparées par une virgule pour une base elliptique : `rx,rz` (ex: `12,6`).

`hauteur` est optionnel (valeur par défaut : 1 bloc).

### Exemples
- `//cyl stone 5 10` : Génère un cylindre plein de rayon 5 et de hauteur 10 blocs en pierre.
- `//hcyl glass 10 20` : Génère une tour cylindrique creuse en verre de hauteur 20 blocs.
- `//cyl water 10` : Crée un disque d'eau d'un bloc de hauteur.

---

## `//pyramid` et `//hpyramid`
*(Alias : `/pyramid`, `/hpyramid`)*

Génère une pyramide pleine ou creuse à degrés dont la pointe s'élève vers le haut.

### Syntaxe
```text
//pyramid [-h] <pattern> <taille>
//hpyramid <pattern> <taille>
```
- `<taille>` : Hauteur et demi-largeur de base de la pyramide.

---

## `//cone` et `//hcone`
*(Alias : `/cone`, `/hcone`)*

Génère un cône circulaire plein ou creux.

### Syntaxe
```text
//cone [-h] <pattern> <rayon> <hauteur>
//hcone <pattern> <rayon> <hauteur>
```

---

## `//torus` et `//htorus`
*(Alias : `/torus`, `/htorus`)*

Génère un tore circulaire (forme d'anneau/donut) en 3D.

### Syntaxe
```text
//torus [-h] <pattern> <rayon_majeur> <rayon_tube>
//htorus <pattern> <rayon_majeur> <rayon_tube>
```
- `<rayon_majeur>` : Distance du centre au centre du tube.
- `<rayon_tube>` : Épaisseur du rayon de section circulaire du tube.

---

## `//ellipsoid` et `//hellipsoid`
*(Alias : `/ellipsoid`, `/hellipsoid`)*

Génère un ellipsoïde 3D plein ou creux selon 3 rayons distincts.

### Syntaxe
```text
//ellipsoid [-h] <pattern> <rx> <ry> <rz>
//hellipsoid <pattern> <rx> <ry> <rz>
```

---

## Drapeaux
- **`-h`** : Génère une forme creuse (coque de 1 bloc).
- **`-u`** : Active la mise à jour des blocs adjacents.
- **`-e`** : Gestion des entités dans la zone d'impact.

