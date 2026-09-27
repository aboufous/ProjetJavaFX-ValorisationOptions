# Valorisation d'options européennes — Java / JavaFX

Application client/serveur de **pricing d'options européennes** (call et put) selon trois méthodes, avec visualisation interactive des résultats.

> Projet académique en binôme — INSA Rouen Normandie (2025–2026)

## Fonctionnalités

- **Trois méthodes de pricing**
  - Black–Scholes analytique (formule fermée)
  - Monte Carlo séquentiel (mouvement brownien géométrique, intervalle de confiance à 95 %)
  - Monte Carlo parallèle multithread (schéma Map/Reduce avec `ExecutorService`)
- **Architecture client/serveur** : serveur TCP multithread, échanges en JSON
- **Interface JavaFX** : trajectoires simulées, distribution des payoffs, convergence et IC 95 %, comparaison des méthodes
- **Historique** des simulations dans une base SQLite
- **Export** des résultats en PDF (avec graphiques) et CSV

## Résultats

| Mesure | Résultat |
|---|---|
| Écart Monte Carlo / Black–Scholes | < 0,3 % sur 10⁶ simulations |
| Temps de calcul (10⁶ simulations) | 831 ms en séquentiel → 232 ms en parallèle (**× 3,6**) |

## Modèle

Le sous-jacent suit un mouvement brownien géométrique :

S(T) = S(0) · exp((r − σ²/2)·T + σ·√T·Z), avec Z ~ N(0, 1)

Le prix Monte Carlo est la moyenne actualisée des payoffs simulés, comparée au prix exact de Black–Scholes.

## Organisation du code

```
src/
├── app/                # Interface JavaFX
│   ├── ui/             # Vues (accueil, simulation, historique, à propos)
│   ├── simulate/       # Formulaire, résultats, graphiques
│   └── graphics/
└── appli/
    ├── engine/         # Black–Scholes, Monte Carlo séquentiel et parallèle
    ├── server/         # Serveur TCP multithread
    ├── net/            # Client socket
    ├── history/        # Persistance SQLite
    ├── export/         # Export PDF et CSV
    └── MainApp.java
lib/                    # Dépendances (.jar) : SQLite JDBC, JSON, PDFBox
```

## Lancer le projet

**Prérequis** : Java 23 et [JavaFX 23](https://openjfx.io) (SDK décompressé, par exemple dans `C:/javafx-sdk-23`). Toutes les autres dépendances sont fournies dans `lib/`.

**1. Serveur**

```bash
mkdir out
javac -cp "lib/*" -d out src/appli/server/*.java src/appli/engine/*.java src/appli/net/*.java src/appli/export/*.java src/appli/history/*.java
java -cp "out;lib/*" appli.server.ServerMain
```

Le serveur écoute sur `localhost:8080`.

**2. Client JavaFX** (dans un second terminal)

```bash
javac --module-path "C:/javafx-sdk-23/lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base -cp "lib/*;out" -d out src/app/ui/*.java src/app/simulate/*.java src/app/graphics/*.java src/appli/net/*.java src/appli/engine/*.java src/appli/export/*.java src/appli/history/*.java src/appli/MainApp.java
java --module-path "C:/javafx-sdk-23/lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base -cp "out;lib/*" appli.MainApp
```

Sous macOS / Linux, remplacer `;` par `:` dans le classpath.

Le guide détaillé est disponible dans [`docs/Guide_installation.pdf`](docs/Guide_installation.pdf).

## Technologies

Java 23 · JavaFX · Threads (`ExecutorService`) · Sockets TCP · JSON · SQLite · PDFBox

