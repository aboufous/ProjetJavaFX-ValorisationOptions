package appli;

import app.ui.HomeView;
import app.ui.Router;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Classe principale de l'application JavaFX (client).
 *
 * <p>MainApp est le point d'entrée JavaFX :
 * - elle initialise la fenêtre principale (Stage),
 * - crée le Router (gestion des écrans),
 * - instancie la vue d'accueil (HomeView),
 * - et affiche l'application.
 *
 * Le cycle JavaFX :
 *  - La JVM appelle main()
 *  - main() appelle launch()
 *  - launch() appelle automatiquement start(Stage)
 *
 * C’est dans start() que l'application commence réellement.
 */
public class MainApp extends Application {

    /**
     * Méthode appelée automatiquement par JavaFX au démarrage.
     *
     * @param stage La fenêtre principale (Primary Stage) fournie par JavaFX.
     */
    public void start(Stage stage) {

        // Création du Router (gestionnaire de navigation entre les vues).
        Router router = new Router(stage);

        // Instanciation de la page d'accueil, en lui donnant accès au Router.
        HomeView home = new HomeView(router);

        // Navigation : on affiche la vue d'accueil dans la scène principale.
        router.goTo(home.getView());

        // Titre affiché dans la barre de la fenêtre (utile hors plein écran).
        stage.setTitle("Valorisation d’options européennes ");

        // Affiche la fenêtre à l'écran.
        stage.show();
    }

    /**
     * Point d'entrée de l'application Java.
     *
     * @param args Arguments de la ligne de commande (rarement utilisés en JavaFX).
     */
    public static void main(String[] args) {
        launch(args); // Démarre le cycle JavaFX → appelle start()
    }
}
