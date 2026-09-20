# Restreindre l'édition : `EditGuard`

Par défaut, MAW laisse **tout joueur** utiliser **toutes** les commandes, sur **n'importe quelle instance**, et écrire **n'importe quel bloc, n'importe où**. C'est voulu pour un serveur de confiance, mais un serveur de construction ouvert au public a besoin de poser des limites : qui peut éditer, où, et avec quels blocs. `EditGuard` est le point d'accroche prévu pour cela.

```java
MawConfig config = MawConfig.builder()
    .editGuard(new MyGuard())
    .build();

Maw.init(MinecraftServer.getGlobalEventHandler(), config);
```

Sans garde (`EditGuard.ALLOW_ALL`), rien ne change : aucun coût, aucune vérification.

---

## Les trois méthodes

```java
public interface EditGuard {

    /** Le joueur peut-il utiliser les commandes de MAW dans cette instance ? */
    default boolean canEdit(Player player, Instance instance) { return true; }

    /** Ce qu'on lui dit quand canEdit refuse. */
    default Component denialMessage(Player player, Instance instance) { ... }

    /** Peut-il poser ce bloc à cet endroit ? */
    default boolean allowsChange(Player player, Instance instance, int x, int y, int z, Block block) { return true; }
}
```

| Méthode | Appelée | Effet du refus |
| :-- | :-- | :-- |
| `canEdit` | Sur le thread du serveur, quand le joueur tape une commande de MAW | La commande **n'est pas exécutée** ; le joueur reçoit `denialMessage`. |
| `allowsChange` | Pour **chaque bloc** d'une opération, depuis les threads de calcul (parfois des millions de fois) | Le bloc est **ignoré** (ni écrit, ni compté dans `maxBlocksPerOperation`, ni dans l'historique) ; le reste de l'opération continue et le joueur est prévenu du nombre de blocs refusés. |

`allowsChange` doit donc être **rapide et thread-safe**. C'est elle qui **confine une opération à une région** (`x`, `y`, `z` dans la zone autorisée) et qui **garde des blocs interdits hors du monde** (`//set command_block` est refusé bloc par bloc, pas seulement l'objet).

> [!NOTE]
> `canEdit` n'est appliquée que si `Maw.init(...)` reçoit le gestionnaire d'événements du serveur (le premier argument) : c'est lui qui permet d'écouter les commandes tapées. Sans lui, MAW le signale au démarrage et seule `allowsChange` est appliquée.

> [!NOTE]
> Une opération lit l'ancien bloc d'une position avant de savoir si elle a le droit d'y écrire. Un chunk qui n'est **pas chargé** se lit donc comme du **vide** (de l'air) au lieu de faire échouer l'opération : une sélection peut dépasser le monde qui existe, et c'est `allowsChange` qui refuse alors ses blocs. Écrire dans un chunk non chargé reste à la charge du serveur (chargez la zone d'édition à l'avance).

`//undo` et `//redo` ne passent pas par `allowsChange` : ils rétablissent ce que le joueur a déjà écrit, sous le contrôle de `canEdit`.

---

## Savoir si MAW travaille sur une instance : `Maw.isBusy`

Une opération est calculée sur des threads de fond, puis appliquée à l'instance **sur plusieurs ticks** si elle est grande. Pendant ce temps le monde est entre deux états : le **sauvegarder** à cet instant enregistrerait une opération à moitié appliquée.

```java
if (Maw.getInstance().isBusy(instance)) {
    // réessayer plus tard
}
```

`isBusy(instance)` est vrai tant que :
- une opération est **en cours de calcul** (pour toute instance : elle n'a pas encore dit où elle écrira) ;
- ou des changements sont **encore appliqués** à cette instance.

Un échec pendant l'application termine le résultat (`CompletableFuture`) en erreur : une instance n'est jamais déclarée occupée pour toujours.

> [!NOTE]
> Minestom écrit un lot de blocs sur ses propres threads et ne dit qu'il a fini qu'au **tick suivant de l'instance**. Le résultat d'une opération (et donc `isBusy`) attend cette confirmation : les blocs sont bien écrits quand il se termine. Conséquence : un serveur dont les instances ne tiquent pas (un test, par exemple) ne verrait pas les opérations se terminer ; elles échouent au bout d'une minute. Dans un test, appelez `instance.tick(...)`.

---

## Exemple : une zone et une liste de blocs interdits

```java
final class BuildAreaGuard implements EditGuard {

    private final Set<Key> forbidden = Set.of(Key.key("minecraft:command_block") /* ... */);

    @Override
    public boolean canEdit(Player player, Instance instance) {
        return areaOf(instance) != null && mayBuild(player, instance);
    }

    @Override
    public boolean allowsChange(Player player, Instance instance, int x, int y, int z, Block block) {
        Area area = areaOf(instance);
        return area != null && area.contains(x, y, z) && !forbidden.contains(block.key());
    }
}
```

Voir aussi [Architecture et Performances](architecture.md).
