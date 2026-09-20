# Commandes de Schématiques et Presse-papier

Ce document détaille l'utilisation des commandes de schématiques (fichiers `.maw`) et du presse-papier étendu dans **MAW (Minestom Async WorldEdit)**.

---

## Sommaire
- [//schem save](#schem-save)
- [//schem load](#schem-load)
- [//schem list](#schem-list)
- [//schem delete](#schem-delete)
- [//clearclipboard](#clearclipboard)

---

## `//schem save`
*(Alias : `//schematic save`, `/schem save`)*

Sauvegarde le contenu actuel du presse-papier joueur sur le disque sous format optimisé `.maw`.

```text
//schem save <nom>
//schematic save chateau_v1
```

> [!NOTE]
> Les schématiques sont sauvegardées dans le répertoire `schematics/` du serveur et conservent toutes les propriétés d'états de blocs et l'origine relative.

---

## `//schem load`
*(Alias : `//schematic load`, `/schem load`)*

Charge un fichier de schématique depuis le disque vers le presse-papier du joueur.

```text
//schem load <nom>
```
Une fois chargée, la construction peut être collée à volonté avec `//paste`.

---

## `//schem list`
*(Alias : `//schematic list`, `/schem list`)*

Affiche la liste de tous les fichiers de schématiques disponibles dans le répertoire du serveur.

```text
//schem list
```

---

## `//schem delete`
*(Alias : `//schematic delete`, `/schem delete`)*

Supprime définitivement un fichier de schématique du disque.

```text
//schem delete <nom>
```

---

## `//clearclipboard`
*(Alias : `/clearclipboard`, `clearclipboard`)*

Vide le presse-papier du joueur et libère immédiatement la mémoire associée.

```text
//clearclipboard
```
