package app.ui;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Router gère la navigation globale d’une application JavaFX.
 *
 * <p>Le principe utilisé ici :
 * - Une seule scène (Scene) est créée au lancement.
 * - À chaque navigation, on remplace simplement le "root" de la scène.
 *
 * Cela rend l’application plus stable :
 * - pas de clignotement entre scènes,
 * - maintien du mode plein écran à chaque transition,
 * - pas besoin de recréer des scènes ou des stages.
 *
 * Le Router est appelé par les vues (HomeView, SimulateView, etc.)
 * pour passer d’un écran à un autre.
 */
public class Router {

    /** Fenêtre principale de l'application (Primary Stage). */
    private final Stage stage;

    /** Scène unique de l’application. Seul son root change. */
    private final Scene mainScene;

    /**
     * Constructeur.
     *
     * @param stage La fenêtre principale JavaFX.
     *
     * Le Router crée ici une unique scène 1400x900 avec un root temporaire (Pane).
     * Cette scène servira pour toutes les vues.
     */
    public Router(Stage stage) {
        this.stage = stage;

        // Création d'une scène vide au départ.
        // Le root (Pane) sera remplacé immédiatement par goTo().
        this.mainScene = new Scene(new Pane(), 1400, 900);

        // On attache la scène à la fenêtre principale.
        stage.setScene(mainScene);
    }

    /**
     * Passe à une nouvelle vue en remplaçant le root de la scène.
     *
     * @param root Le Parent JavaFX représentant la nouvelle vue.
     *
     * Après le changement de root, le plein écran est réactivé proprement
     * via Platform.runLater(), car JavaFX n’autorise pas certaines actions
     * (comme repasser fullscreen) pendant le cycle de rendu actuel.
     */
    public void goTo(Parent root) {
        // Remplace immédiatement l'arbre graphique affiché.
        mainScene.setRoot(root);

        // Remet la fenêtre en plein écran après la transition.
        Platform.runLater(() -> {
            stage.setFullScreenExitHint("");             // Supprime le texte "Press ESC to exit..."
            stage.setFullScreenExitKeyCombination(null); // Empêche la sortie en appuyant sur ESC
            stage.setFullScreen(true);                   // Active le mode plein écran
        });
    }
}
