# Motifs (Patterns) et Masques (Masks)

Ce document explique la syntaxe et les fonctionnalités des motifs et des masques supportés par **MAW**.

---

## Motifs (Patterns)

Un motif définit le ou les blocs qui seront placés lors d'une opération (`//set`, `//replace`, `//sphere`, etc.).

### 1. Bloc simple
Indiquez simplement le nom du bloc (l'espace de noms `minecraft:` est facultatif) :
- `stone` ou `minecraft:stone`
- `dirt`
- `glass`

### 2. États et propriétés de blocs (Block States)
Vous pouvez spécifier des propriétés de blocs entre crochets `[...]` :
- `oak_stairs[facing=north]`
- `oak_stairs[facing=east,half=top]`
- `chest[facing=south,type=single]`
- `redstone_wire[power=15]`

### 3. Motifs aléatoires pondérés
Combinez plusieurs blocs en définissant leurs pourcentages respectifs avec le caractère `%` et des virgules `,` :
- `50%stone,50%dirt` : 50% de pierre, 50% de terre.
- `70%stone_bricks,20%cracked_stone_bricks,10%mossy_stone_bricks`
- `stone,dirt,cobblestone` : Poids équiprobables (1:1:1).

---

## Masques (Masks)

Un masque définit les conditions requises pour qu'un bloc soit remplacé lors d'une opération (notamment avec `//replace` et `//count`).

### 1. Masques prédéfinis
- `#air` ou `air` : Cible tous les blocs d'air.
- `#existing` ou `#solid` : Cible tous les blocs qui ne sont pas de l'air.

### 2. Masque par type de bloc
- `stone` : Cible uniquement la pierre.
- `stone,dirt,granite` : Cible la pierre, la terre ou le granite.

### 3. Négation (`!`)
Préfixez un masque par `!` pour inverser son critère :
- `!air` : Cible tout sauf l'air (identique à `#existing`).
- `!stone` : Cible tous les blocs sauf la pierre.
- `!bedrock` : Protège la bedrock en remplaçant tout le reste.
