# Drapeaux (Flags) et Gestion des Objets Autour

Ce document détaille les drapeaux supportés par les commandes **MAW** ainsi que le comportement vis-à-vis des objets et entités environnants.

---

## Sommaire
- [Drapeau de physique (-u)](#drapeau-de-physique--u)
- [Drapeau des entités (-e)](#drapeau-des-entités--e)
- [Drapeau sans air (-a)](#drapeau-sans-air--a)
- [Drapeau creux (-h)](#drapeau-creux--h)

---

## Drapeau de physique (`-u`)

### Contexte
Dans les serveurs Minecraft classiques, la modification simultanée de milliers de blocs déclenche des calculs de physique en cascade (lumière, chute de blocs, mise à jour des liaisons de redstone, recalcul des états de barrières/escaliers). Ces calculs sont la première cause d'effondrement du TPS (lag violent).

### Comportement dans MAW
- **Par défaut (`sans -u`)** : Les mises à jour de blocs voisins sont **désactivées** pour garantir une vitesse d'exécution maximale et zéro lag.
- **Avec `-u`** : MAW effectue une passe optimisée uniquement sur les **blocs de bordure** de la sélection afin de recalculer les connexions des blocs contigus (murs, barrières, vitres) sans saturer le serveur.

---

## Drapeau des entités (`-e`)

### En mode Remplissage / Suppression (`//set`, `//replace`)
- **Prévention du spam d'items** : Lors de la destruction de blocs (ex: coffres, bibliothèques, minerais), les drops d'items au sol sont nettoyés pour éviter de submerger le serveur avec des milliers d'entités d'items.
- **Nettoyage des entités orphelines** : Si des cadres d'objets, tableaux ou porte-armures perdent leur bloc de support dans la sélection, ils sont retirés proprement.

### En mode Presse-papier (`//copy`, `//paste`)
- **`//copy -e`** : Enregistre toutes les entités présentes dans le parallélépipède de sélection (sauf les joueurs) avec leurs coordonnées relatives.
- **`//paste -e`** : Recrée les entités sauvegardées aux nouvelles coordonnées cibles relatives au joueur.

---

## Drapeau sans air (`-a`)
- Utilisé avec `//paste -a`.
- Permet de coller une structure sans remplacer les blocs existants du monde par les blocs d'air du presse-papier. Idéal pour coller des arbres, des ruines ou des structures ajourées dans un terrain naturel.

---

## Drapeau creux (`-h`)
- Utilisé avec `//sphere -h` ou `//cyl -h`.
- Génère uniquement la coque extérieure d'un bloc d'épaisseur, laissant l'intérieur intact ou vide.
