# customOverlay

> Un mod qui ajoute un overlay façon **F3** avec des informations et un format entièrement **customisables**.

![Minecraft](https://img.shields.io/badge/Minecraft-26.2-green)
![Loader](https://img.shields.io/badge/loader-Fabric-orange)

<img width="2560" height="1440" alt="capture d'écran du jeu" src="https://github.com/user-attachments/assets/432c1f0f-140c-4141-bee1-6213607f5a0f" />

---

## Présentation

**customOverlay** affiche à l'écran un overlay similaire à l'écran de debug de Minecraft (F3), mais dont tu choisis
toi-même le contenu et la mise en forme. Tu n'affiches que les informations qui t'intéressent, comme tu le veux.

## Fonctionnalités

- Overlay inspiré de F3, affiché en jeu
- Choix des informations affichées : (coordonnées, FPS, biome, direction, etc.)
- Format personnalisable : (couleurs, ordre, etc.)

  <img width="247" height="230" alt="collorPicker" src="https://github.com/user-attachments/assets/b77d572d-5fd0-437b-bda6-d6b219d43fd5" />

- Garde en memoire les lignes pour les activer et desactiver selon la situation



## Compatibilité

| Élément            | Version        |
| ------------------ | -------------- |
| Minecraft          | 26.2   |
| Mod loader         | Fabric |
| Java               | 25     |

## Installation

1. Installe le mod loader correspondant [Fabric](https://fabricmc.net/use/installer/).
2. Télécharge le fichier `.jar` du mod depuis la page [Releases](https://github.com/SebiSeb15/customOverlay/releases).
3. Place le `.jar` dans le dossier `mods` de ton installation Minecraft.
4. Lance le jeu.*

   ou ajoute le à ton profile Modrinth

## Utilisation

- **Afficher / masquer l'overlay** : F4 (touche par défaut)
- **Ouvrir la configuration** : O (touche par défaut)

## Configuration

fichier de configuration dans : `config/customoverlay.json`.

  <img width="253" height="228" alt="menu de configuration" src="https://github.com/user-attachments/assets/38c14118-8e96-4b77-8fed-3795f67620e4" />
  
### Personnaliser l'affichage

| Variable | Description | Exemple |
| ----------------- | ----------- | ------- |
| `{fps}`     | FPS |  "FPS: {fps}" |
| `{x}`, `{y}`, `{z}`    | position du joueur | "XYZ: {x} / {y} / {z}" |
| `{facing}`     | dirrection du regard du joueur|"Facing: {facing}" |
| `{biome}`     | Biome actuel |"Biome: {biome}" |
| `{dimension}`     | dimension actuelle |"Dimension: {dimension}" |
| `{time}`     | temps du jeu (de0 à 24000) |"Time: {time}"|
| `{chunk_x}`, `{chunk_z}`| coordonées du chunk actuel |"Chunk: {chunk_x}/{chunk_z} |
| `{local_x}`, `{local_z}`| coordonées relatives au chunk actuel |local: {local_x}/{local_z}" |
| `{other_dim}`, `{conv_x} / {conv_z} `| nether<>overworld :  coordonnées converties|"{other_dim}: {conv_x} / {conv_z}"|
| `{e_rendered}`, `{e_total}`     | Nombre d'entitées affichées par le client / nombre d'entitées chargées |"E: {e_rendered}/{e_total}" |

<img width="2560" height="1440" alt="2026-09-19_13 30 01" src="https://github.com/user-attachments/assets/dca46c71-0a91-45ef-bd65-be4b9a938360" />

Exemple :
<p align="center">
<img width="748" height="96"  alt="exemple de confihuration" src="https://github.com/user-attachments/assets/86897ac2-5592-4d47-b67e-92b01e1918e6" />
<img width="358" height="59"  alt="rendu de la configuratio" src="https://github.com/user-attachments/assets/c5ec9a89-c46c-4e47-8a99-6a69f2d51a0d" />
</p>
## Compiler le projet

Le projet utilise [Gradle](https://gradle.org/).

```bash
git clone https://github.com/SebiSeb15/customOverlay.git
cd customOverlay
./gradlew build
```

Le `.jar` généré se trouve dans `build/libs/`.

> Sous Windows, utilise `gradlew.bat build`.

## Contribuer

Les suggestions et les pull requests sont les bienvenues.

// =^ᴥ^=

Copyright © 2026 SebiSeb15
