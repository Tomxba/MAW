# Commandes de Brushes et Outils Spécialisés

Ce document détaille l'utilisation des brushes et outils interactifs dans **MAW (Minestom Async WorldEdit)**.

---

## Sommaire
- [Pinceaux (Brushes)](#pinceaux-brushes)
  - [/brush sphere](#brush-sphere)
  - [/brush cyl](#brush-cyl)
  - [/brush smooth](#brush-smooth)
  - [/brush clipboard](#brush-clipboard)
  - [/brush gravity](#brush-gravity)
  - [/brush raise / lower](#brush-raise--lower)
  - [/brush flatten](#brush-flatten)
  - [/brush paint](#brush-paint)
  - [/mask, /size, /range, /mat](#paramètres-du-pinceau)
- [Outils Spécialisés](#outils-spécialisés)
  - [/tool info](#tool-info)
  - [/tool repl](#tool-repl)
  - [/tool cycler](#tool-cycler)
  - [/tool tree](#tool-tree)
  - [/tool deltree](#tool-deltree)
  - [/tool lrbuild](#tool-lrbuild)
  - [/tool floodfill](#tool-floodfill)
  - [/tool farwand](#tool-farwand)
  - [/none](#délier-un-outil)

---

## Pinceaux (Brushes)

Les brushes s'appliquent sur l'objet tenu en main principale via un **clic droit**.

### `/brush sphere`
*(Alias : `/b sphere`, `/brush s`)*

Lie un pinceau sphérique à l'objet tenu.

```text
/brush sphere [-h] <pattern> [rayon]
```
- `-h` : Crée une sphère creuse.
- `<pattern>` : Bloc ou mélange de blocs à appliquer.
- `[rayon]` : Rayon de la sphère (par défaut 3, maximum configurable).

### `/brush cyl`
*(Alias : `/b cyl`, `/brush c`)*

Lie un pinceau cylindrique à l'objet tenu.

```text
/brush cyl [-h] <pattern> [rayon] [hauteur]
```
- `-h` : Crée un cylindre creux.
- `[hauteur]` : Hauteur verticale du cylindre (par défaut 1).

### `/brush smooth`
*(Alias : `/b smooth`)*

Adoucit le relief du terrain ciblé par moyenne des hauteurs locales.

```text
/brush smooth [rayon] [itérations]
```

### `/brush clipboard`
*(Alias : `/b clipboard`, `/brush paste`)*

Colle le presse-papier actuel à l'emplacement ciblé par le pinceau.

```text
/brush clipboard [-a]
```
- `-a` : Ignore les blocs d'air présents dans le clipboard.

### `/brush gravity`
*(Alias : `/b gravity`)*

Fait tomber les blocs soumis à la simulation de gravité vers le bas dans la zone ciblée.

```text
/brush gravity [rayon]
```

### `/brush raise` / `/brush lower`
*(Alias : `/b raise`, `/b lower`)*

Élève ou abaisse le relief du terrain ciblé de façon graduelle et naturelle.

```text
/brush raise [rayon] [hauteur]
/brush lower [rayon] [hauteur]
```

### `/brush flatten`
*(Alias : `/b flatten`)*

Aplatit le terrain environnant au niveau d'élévation du bloc cliqué.

```text
/brush flatten [rayon]
```

### `/brush paint`
*(Alias : `/b paint`)*

Peint uniquement la surface exposée à l'air sans modifier l'intérieur du terrain.

```text
/brush paint <pattern> [rayon]
```

### Paramètres du Pinceau

- `/mask [masque]` : Définit le masque appliqué au pinceau tenu (ex: `/mask grass_block,dirt`). Tapez `/mask` sans argument pour réinitialiser.
- `/size <rayon>` : Modifie le rayon du pinceau actuel.
- `/range <distance>` : Modifie la portée maximale du pinceau (distance de clic raycast).
- `/mat <pattern>` : Change le matériau appliqué par le pinceau tenu.

---

## Outils Spécialisés

Les outils spécialisés s'activent avec `/tool <nom>` sur l'objet tenu en main.

### `/tool info`
*(Alias : `/tool inspect`)*

Affiche dans le chat des informations exhaustives sur le bloc cliqué (identifiant, coordonnées exactes, propriétés d'état de bloc, biome).

### `/tool repl`
*(Alias : `/tool replace`)*

Remplace instantanément le bloc cliqué par le pattern configuré :
```text
/tool repl <pattern>
```

### `/tool cycler`

Fait défiler les états et propriétés du bloc ciblé (orientation des escaliers, état allumé/éteint d'une lampe, direction d'une bûche, etc.).

### `/tool tree`

Génère instantanément un arbre à l'emplacement cliqué :
```text
/tool tree [oak|birch|spruce|jungle|acacia|dark_oak]
```

### `/tool deltree`

Supprime instantanément un arbre complet (tronc et feuillage connecté) d'un seul clic.

### `/tool lrbuild`
*(Long Range Build)*

Pose le pattern configuré ou détruit le bloc à longue distance (clic gauche = détruire, clic droit = poser) :
```text
/tool lrbuild <left_pattern> <right_pattern>
```

### `/tool floodfill`

Réchampit et remplit une poche connectée de blocs contigus identiques par le pattern sélectionné :
```text
/tool floodfill <pattern> [portée_max]
```

### `/tool farwand`

Permet d'utiliser la baguette de sélection WorldEdit à distance sans devoir toucher physiquement le bloc (clic gauche = pos1, clic droit = pos2).

### Délier un outil

```text
/tool none
/none
```
Délie tout brush ou outil associé à l'objet actuellement tenu en main.
