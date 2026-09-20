# Commandes de Région et Remplissage

Ce document détaille les commandes de manipulation de blocs dans une sélection pour **MAW (Minestom Async WorldEdit)**.

---

## Sommaire
- [//set](#set)
- [//replace](#replace)
- [//walls](#walls)
- [//faces / //outline](#faces----outline)
- [//hollow](#hollow)
- [Drapeaux (-u et -e)](#drapeaux--u-et--e)

---

## `//set`
*(Alias : `/set`, `set`)*

Remplit l'intégralité de la sélection active avec le ou les blocs définis par le motif (*pattern*).

### Syntaxe
```text
//set <pattern> [-u] [-e]
```

### Exemples
- `//set stone` : Remplit la sélection avec de la pierre.
- `//set oak_stairs[facing=east]` : Remplit avec des escaliers en chêne orientés vers l'est.
- `//set 50%stone,30%cobblestone,20%andesite` : Remplit avec un mélange aléatoire pondéré.
- `//set stone -u` : Remplit et actualise les blocs voisins.

---

## `//replace`
*(Alias : `/replace`, `replace`)*

Remplace les blocs correspondant à un masque par un nouveau motif au sein de la sélection.

### Syntaxe
- `//replace <to-pattern> [-u] [-e]` : Remplace tous les blocs non-air par le motif.
- `//replace <from-mask> <to-pattern> [-u] [-e]` : Remplace uniquement les blocs correspondant à `from-mask`.

### Exemples
- `//replace dirt` : Remplace tous les blocs existants (sauf l'air) par de la terre.
- `//replace stone cobblestone` : Remplace toute la pierre par de la pierre taillée.
- `//replace !air glass` : Remplace tout ce qui n'est pas de l'air par du verre.
- `//replace dirt,grass_block 50%sand,50%gravel` : Remplace la terre et l'herbe par un mélange de sable et gravier.

---

## `//walls`
*(Alias : `/walls`, `walls`)*

Génère les 4 parois verticales autour du périmètre de la sélection actuelle.

### Syntaxe
```text
//walls <pattern> [-u] [-e]
```

### Exemples
- `//walls stone_bricks` : Crée 4 murs de briques de pierre.
- `//walls glass` : Crée 4 murs de verre.

---

## `//faces` / `//outline`
*(Alias : `/faces`, `faces`, `/outline`, `outline`)*

Construit les 6 faces extérieures (murs + sol + plafond) de la sélection, formant une boîte creuse.

### Syntaxe
```text
//faces <pattern> [-u] [-e]
//outline <pattern> [-u] [-e]
```

### Exemples
- `//faces obsidian` : Crée un cube creux en obsidienne.

---

## `//hollow`
*(Alias : `/hollow`, `hollow`)*

Évide l'intérieur de la sélection en laissant intacte une coque extérieure de l'épaisseur spécifiée.

### Syntaxe
```text
//hollow [épaisseur] [motif_remplissage] [-u] [-e]
```
*Par défaut, l'épaisseur est de 1 bloc et l'intérieur est remplacé par de l'air.*

### Exemples
- `//hollow` : Évide la sélection en remplaçant l'intérieur par de l'air (épaisseur 1).
- `//hollow 2` : Évide la sélection en laissant une bordure de 2 blocs d'épaisseur.
- `//hollow 1 water` : Remplit l'intérieur avec de l'eau tout en conservant les parois extérieures.

---

## `//move`
*(Alias : `/move`, `move`)*

Déplace les blocs de la sélection active dans une direction donnée, en effaçant l'emplacement d'origine (remplacé par de l'air).

### Syntaxe
```text
//move [distance] [direction]
```
- `[distance]` : Nombre de blocs de déplacement (par défaut 1).
- `[direction]` : `north`, `south`, `east`, `west`, `up`, `down`, ou direction du regard par défaut.

---

## `//stack`
*(Alias : `/stack`, `stack`)*

Répète la sélection active un certain nombre de fois dans une direction donnée.

### Syntaxe
```text
//stack [répétitions] [direction]
```

---

## `//naturalize`
*(Alias : `/naturalize`, `naturalize`)*

Régénère un profil de sol naturel sur le relief de la sélection : 1 couche supérieure d'herbe, 3 couches de terre en-dessous, et de la pierre pour le reste.

### Syntaxe
```text
//naturalize
```

---

## `//overlay`
*(Alias : `/overlay`, `overlay`)*

Pose un motif de bloc sur toutes les surfaces exposées vers le haut dans la sélection (ex: déposer de la neige ou de la mousse sur le relief).

### Syntaxe
```text
//overlay <pattern>
```

---

## `//smooth`
*(Alias : `/smooth`, `smooth`)*

Lisse et adoucit les variations brusques de hauteur du terrain dans la sélection.

### Syntaxe
```text
//smooth [itérations]
```

---

## `//line`
*(Alias : `/line`, `line`)*

Trace une ligne droite 3D entre la position 1 et la position 2 avec l'algorithme de Bresenham 3D.

### Syntaxe
```text
//line <pattern> [épaisseur]
```

---

## `//center`
*(Alias : `/center`, `center`)*

Identifie et remplit le bloc ou les blocs situés au centre exact de la boîte englobante.

### Syntaxe
```text
//center <pattern>
```

---

## `//fall`
*(Alias : `/fall`, `fall`)*

Fait chuter tous les blocs suspendus dans le vide de la sélection vers le sol le plus bas.

### Syntaxe
```text
//fall
```

---

## `//forest` / `//flora`
*(Alias : `/forest`, `forest`, `/flora`, `flora`)*

- `//forest [type] [densité]` : Plante des arbres adaptés (oak, birch, spruce, jungle, etc.) sur les surfaces viables.
- `//flora [densité]` : Fait pousser un tapis d'herbes hautes et de fleurs sauvages sur les surfaces d'herbe.

---

## Drapeaux (-u et -e)

Toutes les commandes de région supportent les drapeaux :
- **`-u` (Update)** : Déclenche la mise à jour des blocs adjacents (connexions de barrières, vitres, escaliers, etc.). Désactivé par défaut pour préserver les performances.
- **`-e` (Entities)** : Active la gestion avancée des entités de la zone (suppression des entités dépendantes des blocs détruits).

