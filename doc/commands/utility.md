# Commandes Utilitaires

Ce document détaille les commandes utilitaires et d'information dans **MAW (Minestom Async WorldEdit)**.

---

## Sommaire
- [//size](#size)
- [//count](#count)

---

## `//size`
*(Alias : `/size`, `size`)*

Affiche les informations détaillées sur la sélection actuelle du joueur.

### Syntaxe
```text
//size
```

### Informations retournées
- Dimensions de la boîte englobante : `Largeur (X) x Hauteur (Y) x Longueur (Z)`.
- Volume total en nombre de blocs.
- Coordonnées des points minimaux et maximaux de la sélection.

---

## `//count`
*(Alias : `/count`, `count`)*

Compte le nombre de blocs dans la sélection qui correspondent à un masque ou un type de bloc donné.

### Syntaxe
```text
//count <masque_ou_bloc>
```

### Exemples
- `//count stone` : Compte le nombre de blocs de pierre dans la sélection.
- `//count #air` : Compte le nombre de blocs d'air.
- `//count !air` : Compte tous les blocs solides/existants.
- `//count dirt,grass_block` : Compte tous les blocs de terre et d'herbe.
