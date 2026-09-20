# Commandes de Navigation

Ce document détaille l'utilisation des commandes de déplacement et téléportation rapide dans **MAW (Minestom Async WorldEdit)**.

---

## Sommaire
- [//up](#up)
- [//ceil](#ceil)
- [//jumpto](#jumpto)
- [//thru](#thru)
- [//unstuck](#unstuck)
- [//ascend](#ascend)
- [//descend](#descend)
- [//top](#top)

---

## `//up`
*(Alias : `/up`, `up`)*

Élève le joueur verticalement d'un nombre donné de blocs et place un bloc de verre sous ses pieds pour le soutenir.

```text
//up <hauteur>
//up 10
```

---

## `//ceil`
*(Alias : `/ceil`, `ceil`)*

Téléporte le joueur directement au plafond de la pièce ou caverne dans laquelle il se trouve, plaçant un bloc de verre sous ses pieds si nécessaire.

```text
//ceil [dégagement]
```

---

## `//jumpto`
*(Alias : `/jumpto`, `/j`, `j`)*

Téléporte instantanément le joueur sur le bloc exact ciblé par son regard via raycasting haute performance.

```text
/jumpto
/j
```

---

## `//thru`
*(Alias : `/thru`, `thru`)*

Traverse le mur ou l'obstacle situé face au joueur vers l'espace libre situé immédiatement derrière.

```text
//thru
```

---

## `//unstuck`
*(Alias : `/unstuck`, `unstuck`)*

Extirpe un joueur bloqué dans des blocs solides et le téléporte sur le premier espace libre disponible au-dessus.

```text
//unstuck
```

---

## `//ascend` / `//descend`
*(Alias : `/ascend`, `/descend`)*

Monte ou descend au niveau du sol libre situé immédiatement au-dessus ou en-dessous (ex: changement d'étage d'un bâtiment).

```text
//ascend
//descend
```

---

## `//top`
*(Alias : `/top`, `top`)*

Téléporte le joueur sur le bloc de surface le plus élevé à sa colonne horizontale actuelle.

```text
//top
```
