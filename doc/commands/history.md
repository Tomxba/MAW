# Commandes d'Historique (Undo / Redo)

Ce document détaille la gestion de l'historique et des annulations dans **MAW (Minestom Async WorldEdit)**.

---

## Sommaire
- [//undo](#undo)
- [//redo](#redo)
- [//clearhistory](#clearhistory)
- [Gestion de la mémoire](#gestion-de-la-m%C3%A9moire)

---

## `//undo`
*(Alias : `/undo`, `undo`)*

Annule la dernière opération WorldEdit réalisée par le joueur.

### Syntaxe
```text
//undo [nombre_operations]
```

### Exemples
- `//undo` : Annule la dernière opération.
- `//undo 3` : Annule les 3 dernières opérations consécutives.

> [!NOTE]
> L'annulation est exécutée de manière asynchrone sans geler la boucle principale du serveur.

---

## `//redo`
*(Alias : `/redo`, `redo`)*

Rétablit la dernière opération précédemment annulée.

### Syntaxe
```text
//redo [nombre_operations]
```

### Exemples
- `//redo` : Rétablit la dernière opération annulée.
- `//redo 2` : Rétablit les 2 dernières opérations.

---

## `//clearhistory`
*(Alias : `/clearhistory`, `clearhistory`)*

Vide l'intégralité de l'historique d'annulation et de rétablissement du joueur courant, libérant ainsi la mémoire associée.

### Syntaxe
```text
//clearhistory
```

---

## Gestion de la mémoire

Pour garantir qu'un grand nombre d'éditions massives ne provoque pas de saturation de la mémoire (OOM) :
- Chaque joueur dispose d'une limite maximale d'étapes d'historique (configurable via `MawConfig.builder().maxHistoryPerPlayer(20)`).
- Les deltas de blocs stockent directement les états de blocs précédents sous format compact.
- L'historique d'un joueur est automatiquement libéré dès sa déconnexion du serveur (`PlayerDisconnectEvent`).
