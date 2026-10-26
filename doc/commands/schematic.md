# Commandes de Schématiques et Presse-papier

Ce document détaille l'utilisation des commandes de schématiques et du presse-papier étendu dans **MAW (Minestom Async WorldEdit)**.

## Dossier et formats

Les schématiques vivent dans un dossier configurable (`MawConfig.schematicsDirectory`, défaut `schematics/` à côté du serveur) ; MAW **écrit son chemin absolu dans le journal au démarrage**, pour savoir où déposer un fichier. `//schem load` et `//schem list` lisent trois formats :

- **`.maw`** — le format texte propre à MAW, écrit par `//schem save`, qui conserve les états de blocs et l'origine relative ;
- **`.schem`** — le format **Sponge** (versions 2 et 3) que produisent WorldEdit et la plupart des outils : on peut déposer un `.schem` **téléchargé** et le `//schem load`. Un bloc qu'une autre version ne connaît pas devient de l'air plutôt que de faire échouer tout le chargement ;
- **`.schematic`** — s'il est au format Sponge, il est lu comme un `.schem` ; le vieux format **MCEdit** (identifiants numériques, avant Minecraft 1.13) n'a pas de palette et est refusé avec un message qui invite à le réexporter en `.schem`.

Après un `//schem load`, `//paste` pose la construction (son coin minimum à la position du joueur) ; `//paste -a` ignore l'air du cadre, `//rotate` / `//flip` l'orientent.

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

Sauvegarde le contenu actuel du presse-papier joueur sur le disque sous le format `.maw` de MAW.

```text
//schem save <nom>
//schematic save chateau_v1
```

> [!NOTE]
> `//schem save` écrit toujours au format `.maw` (états de blocs et origine relative conservés), dans le dossier des schématiques. Les `.schem` / `.schematic` ne sont que **lus** (déposés à la main), jamais écrits.

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
