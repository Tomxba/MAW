# Commandes de Sélection

Ce document détaille l'utilisation des commandes de sélection dans **MAW (Minestom Async WorldEdit)**.

---

## Sommaire
- [//wand](#wand)
- [//pos1](#pos1)
- [//pos2](#pos2)
- [//hpos1](#hpos1)
- [//hpos2](#hpos2)

---

## `//wand`
*(Alias : `/wand`, `wand`)*

Donne au joueur l'outil de sélection (par défaut une hache en bois).

### Utilisation
1. Tapez `//wand` dans le chat.
2. Équipez l'outil reçu en main principale.
3. **Clic gauche** sur un bloc pour définir la **Position 1**.
4. **Clic droit** sur un bloc pour définir la **Position 2**.

> [!NOTE]
> Le clic gauche avec l'outil de sélection annule automatiquement la destruction du bloc, vous permettant de sélectionner sans détruire le monde.

---

## `//pos1`
*(Alias : `/pos1`, `pos1`)*

Définit la première position de sélection.

### Syntaxe
- `//pos1` : Définit la position 1 à l'emplacement exact des pieds du joueur.
- `//pos1 <x> <y> <z>` : Définit la position 1 aux coordonnées spécifiées (coordonnées absolues ou relatives avec `~`).

### Exemples
```text
//pos1
//pos1 100 64 -200
//pos1 ~ ~1 ~
```

---

## `//pos2`
*(Alias : `/pos2`, `pos2`)*

Définit la seconde position de sélection.

### Syntaxe
- `//pos2` : Définit la position 2 à l'emplacement exact des pieds du joueur.
- `//pos2 <x> <y> <z>` : Définit la position 2 aux coordonnées spécifiées.

### Exemples
```text
//pos2
//pos2 150 80 -150
```

---

## `//hpos1`
*(Alias : `/hpos1`, `hpos1`)*

Définit la position 1 sur le bloc ciblé par le regard du joueur (raycast jusqu'à 100 blocs).

### Syntaxe
```text
//hpos1
```

---

## `//hpos2`
*(Alias : `/hpos2`, `hpos2`)*

Définit la position 2 sur le bloc ciblé par le regard du joueur (raycast jusqu'à 100 blocs).

### Syntaxe
```text
//hpos2
```

---

## `//expand`
*(Alias : `/expand`, `expand`)*

Étend la sélection cubique active dans une direction donnée.

### Syntaxe
- `//expand <montant>` : Étend dans la direction du regard du joueur.
- `//expand <montant> <direction>` : Étend dans la direction spécifiée (`north`, `south`, `east`, `west`, `up`, `down`, `me`).
- `//expand <montant> <montant_inverse> <direction>` : Étend dans la direction et son opposée.
- `//expand vert` : Étend la sélection de la couche minimale du monde (-64) jusqu'au plafond (319).

---

## `//contract`
*(Alias : `/contract`, `contract`)*

Réduit la sélection cubique active dans une direction donnée.

### Syntaxe
- `//contract <montant>` : Réduit dans la direction du regard.
- `//contract <montant> <direction>` : Réduit selon la direction spécifiée.

---

## `//shift`
*(Alias : `/shift`, `shift`)*

Déplace la zone de sélection sans déplacer les blocs du monde.

### Syntaxe
- `//shift <montant>` : Déplace la boîte dans la direction du regard.
- `//shift <montant> <direction>` : Déplace la boîte dans la direction indiquée.

---

## `//inset`
*(Alias : `/inset`, `inset`)*

Rétrécit la boîte de sélection uniformément sur tous les côtés (ou uniquement horizontalement avec `-h`, verticalement avec `-v`).

### Syntaxe
```text
//inset [-h|-v] <montant>
```

---

## `//outset`
*(Alias : `/outset`, `outset`)*

Étend la boîte de sélection uniformément sur tous les côtés (ou uniquement horizontalement avec `-h`, verticalement avec `-v`).

### Syntaxe
```text
//outset [-h|-v] <montant>
```

---

## `//chunk`
*(Alias : `/chunk`, `chunk`)*

Sélectionne l'intégralité du chunk (16x16 de Y=-64 à Y=319) dans lequel se trouve le joueur.

### Syntaxe
```text
//chunk
```

---

## `//sel` / `//desel`
*(Alias : `/sel`, `/desel`)*

- `//sel cuboid` : Définit le mode de sélection cubique.
- `//desel` : Désélectionne la zone actuelle en réinitialisant pos1 et pos2.

---

## `//distr`
*(Alias : `/distr`, `distr`)*

Analyse la sélection et affiche la distribution des blocs sous forme de statistiques (nombre de blocs, pourcentages par type).

### Syntaxe
```text
//distr
```

