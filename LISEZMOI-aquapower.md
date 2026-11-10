# Aqua Power (Fabric, Minecraft 1.21.11)

5 gourdes inspirees des gourdes inox de la vraie vie (bouchon, goulot, corps colore avec reflet et bande blanche). On les boit (clic droit maintenu 2 s) et elles donnent un pouvoir avec animations, sons et effets. Elles ne sont pas consommees : recharge de 20 secondes.

| Gourde | Pouvoir |
|---|---|
| Tornade | Dash dans la direction du regard, Vitesse 3, chute ralentie, les mobs proches sont aspires vers le haut. Vent, nuages, onde de choc |
| Glacier | Les mobs a 10 blocs sont geles (lenteur 5 + gel + degats). Resistance pour toi. Neige, anneaux de glace |
| Foudre | 6 eclairs autour de toi (degats aux mobs, pas a toi), Vitesse 2 + Force 2. Etincelles electriques |
| Source | Soin de 6 coeurs, Regeneration, Absorption, coeurs et etoiles de totem |
| Tsunami | Vague qui repousse tout a 10 blocs, Grace du dauphin + respiration aquatique 30 s. Eclaboussures et bulles |

Pendant la boisson, une double spirale de particules se resserre autour de toi, avec des notes de plus en plus aigues. A la fin, une animation plein ecran se lance (flash d'ouverture, titre geant, effet propre a chaque pouvoir).

## Obtenir
- Onglet creatif "Outils et utilitaires".
- Craft (sans forme) : bouteille en verre + lingot de fer + ingredient special :
  Tornade = charge de vent, Glacier = glace compacte, Foudre = paratonnerre, Source = pomme en or, Tsunami = coeur de la mer.

## Compiler le .jar

### Option A : sans rien installer (GitHub)
1. Cree un depot GitHub vide et envoie-y tout le contenu de ce dossier (y compris le dossier cache `.github`).
2. Onglet **Actions** > le workflow "build" se lance tout seul.
3. Telecharge l'artefact **aquapower-jar** (zip contenant le .jar).

### Option B : en local
Java 21 + Gradle 9.2.1, puis `gradle build`. Le mod est dans `build/libs/aquapower-1.0.0.jar` (pas celui en `-sources`).

## Installer
Dans le dossier `mods` : `aquapower-1.0.0.jar` + Fabric API pour 1.21.11 (Fabric Loader 0.18+). Le mod doit etre sur le serveur ET chez les joueurs qui veulent voir l'animation d'ecran (en solo, c'est automatique).

## Personnaliser
- `PowerEffects.java` : tout ce que fait chaque pouvoir (durees, degats, particules, sons).
- `AquaPowerClient.java` : les animations d'ecran.
- `BottleItem.java` : duree de boisson (`DRINK_TICKS`) et recharge (`COOLDOWN_TICKS`).
- `Power.java` : noms et couleurs.
- Les textures sont dans `assets/aquapower/textures/item/` (16x16, remplace-les par les tiennes).

## Attention
- Pas de stroboscope volontairement. La Foudre flashe environ 3 fois par seconde au maximum.
- Code ecrit d'apres la doc et les mappings de 1.21.11, mais pas compile ni teste ici. S'il y a une erreur de compilation (un nom de classe ou de son qui aurait change), envoie-moi le message et je corrige.
