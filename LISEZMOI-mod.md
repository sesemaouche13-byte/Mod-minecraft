# Live FX (Fabric, Minecraft 1.21.11)

Mod 100 % client : tu assignes des touches (Options > Controles > "Live FX") et chaque touche lance un effet plein ecran, avec en plus un son / des particules / des mobs.

## Touches par defaut
| Touche | Effet |
|---|---|
| H | Flash blanc (+ tonnerre) |
| J | Explosion (onde de choc + BOOM) |
| K | Arc-en-ciel |
| U | Glitch |
| I | Danger : bords rouges + 3 zombies |
| O | Gros popup "GG !" |
| Y | Confettis |
| N | Gel (teinte glace + lenteur 5 s) |
| V | Lignes retro / scanlines |
| B | Pluie Matrix |
| FIN | Arreter tous les effets |

Tout est rebindable dans le menu des controles. Les sons, particules, zombies et la lenteur sont des commandes envoyees au jeu : il faut les triches activees (monde solo avec triches, ou etre op). Pour n'avoir que les effets visuels, mets `RUN_COMMANDS = false` dans `LiveFxClient.java`.

## Obtenir le .jar

### Option A : sans rien installer (GitHub)
1. Cree un depot GitHub vide.
2. Envoie tout le contenu de ce dossier dedans (y compris le dossier cache `.github`).
3. Onglet **Actions** > le workflow "build" se lance tout seul (sinon "Run workflow").
4. Quand il est vert, ouvre-le et telecharge l'artefact **livefx-jar** (c'est un zip qui contient le .jar).

### Option B : en local
1. Installe Java 21 et Gradle 9.2.1.
2. Dans ce dossier : `gradle build`
3. Le mod est dans `build/libs/livefx-1.0.0.jar` (pas celui en `-sources`).

## Installer
Dans ton dossier `mods` (Fabric deja installe pour 1.21.11) mets :
- `livefx-1.0.0.jar`
- Fabric API pour 1.21.11

## Personnaliser
Tout est dans `src/main/java/com/livefx/LiveFxClient.java` :
- `registerEffects()` : touche par defaut, duree et commandes de chaque effet.
- `POPUP_TEXT` : le texte du popup.
- Un effet = une methode de dessin (flash, boom, rainbow...), facile a copier pour en creer un nouveau.

## Lier aux cadeaux TikTok
Le mod reagit aux touches du clavier. Un outil de live qui sait simuler une touche a chaque cadeau (par exemple TikFinity) peut donc appuyer sur H, J, K... a ta place.

## Attention
- Pas de stroboscope volontairement (risque pour les spectateurs sensibles). Le glitch (U) scintille un peu : retire-le si ton public est sensible.
- Code ecrit d'apres la doc Fabric 1.21.11 mais pas compile ni teste ici. S'il y a une erreur de compilation, envoie-moi le message et je corrige.
