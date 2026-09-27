package app.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import java.awt.Desktop;
import java.net.URI;
import javafx.scene.control.ScrollPane;

/**
 * AboutView affiche une page d'information et d'aide sur l'application.
 *
 * <p>Elle présente :
 * - une description générale du projet,
 * - un résumé des méthodes de pricing utilisées,
 * - une explication des onglets graphiques,
 * - des conseils d'utilisation,
 * - des ressources externes,
 * - et les crédits auteurs.
 *
 * La vue est construite dynamiquement dans getView()
 * et affichée via le Router.
 */
public class AboutView {

    /** Router permettant la navigation vers d'autres pages. */
    private final Router router;

    /**
     * Constructeur : AboutView a besoin du Router pour pouvoir revenir à l'accueil.
     */
    public AboutView(Router router) {
        this.router = router;
    }

    /**
     * Construit et retourne la vue JavaFX correspondant à la page "À propos".
     *
     * @return Le nœud racine de la vue (BorderPane).
     */
    public Parent getView() {

        // Conteneur principal en BorderPane
        BorderPane root = new BorderPane();
        root.getStylesheets().add("data:text/css," + Styles.global());           // Styles CSS inline
        root.getStylesheets().add(
                getClass().getResource("/app/ui/about.css").toExternalForm()     // Fichier CSS externe
        );
        root.getStyleClass().add("about-root");

        // Conteneur vertical contenant toute la page (bouton retour + titre + carte)
        VBox container = new VBox(22);
        container.setPadding(new Insets(25));

        // --- BARRE SUPÉRIEURE (icône retour) ---
        HBox topbar = new HBox(10);
        Button back = new Button("← Retour");
        back.getStyleClass().add("menu-btn");
        topbar.getChildren().add(back);

        // Titre principal de la page
        Text title = new Text("À propos & Aide");
        title.getStyleClass().add("about-title");

        // Carte principale contenant toutes les sections d'information
        VBox card = new VBox(20);
        card.getStyleClass().add("about-card");

        // ---------------------------------------------
        // SECTION 1 : Présentation générale
        // ---------------------------------------------
        Label introTitle = new Label("📘 Présentation générale");
        introTitle.getStyleClass().add("about-subtitle");

        Label intro1 = new Label(
                "Cette application permet de valoriser des options européennes (Call & Put) "
                        + "en utilisant des méthodes analytiques (Black–Scholes) et numériques (Monte Carlo)."
        );
        intro1.getStyleClass().add("about-text");
        intro1.setWrapText(true); // Coupe automatique le texte

        Label intro2 = new Label(
                "Elle sert d’outil pédagogique complet : trajectoires simulées, distribution statistique, "
                        + "convergence, variance, et intervalle de confiance (IC95%). "
                        + "Tout le pipeline du pricing est visualisé et expliqué."
        );
        intro2.getStyleClass().add("about-text");
        intro2.setWrapText(true);

        // ---------------------------------------------
        // SECTION 2 : Méthodes de pricing
        // ---------------------------------------------
        Label methodTitle = new Label("🧠 Méthodes disponibles");
        methodTitle.getStyleClass().add("about-subtitle");

        Label m1 = new Label(
                "• Black–Scholes (méthode analytique) — rapide, exacte, sans variance. "
                        + "Point de référence pour valider les méthodes Monte-Carlo."
        );
        m1.getStyleClass().add("about-text"); m1.setWrapText(true);

        Label m2 = new Label(
                "• Monte Carlo séquentiel — simulation trajectoire par trajectoire, modèle log-normal, "
                        + "simple et pédagogique mais plus lent."
        );
        m2.getStyleClass().add("about-text"); m2.setWrapText(true);

        Label m3 = new Label(
                "• Monte Carlo parallèle — version multi-thread optimisée, idéale pour 200k, 500k ou 1M simulations."
        );
        m3.getStyleClass().add("about-text"); m3.setWrapText(true);

        // ---------------------------------------------
        // SECTION 3 : Description des graphiques
        // ---------------------------------------------
        Label graphsTitle = new Label("📊 Description des onglets de visualisation");
        graphsTitle.getStyleClass().add("about-subtitle");

        Label g1 = new Label("• Payoff — représentation théorique du payoff Call & Put.");
        Label g2 = new Label("• Trajectoires — exemples de trajectoires simulées S(t) pour visualiser la volatilité.");
        Label g3 = new Label("• Convergence — rapprochement Monte-Carlo vs Black-Scholes lorsque N augmente.");
        Label g4 = new Label("• Distribution S(T) — histogramme de S(T), généralement log-normal.");
        Label g5 = new Label("• Distribution Payoff — histogramme des payoffs max(S−K,0) ou max(K−S,0).");
        Label g6 = new Label("• Erreur & IC 95% — variance, écart-type, erreur standard, intervalle de confiance.");

        for (Label l : new Label[]{g1,g2,g3,g4,g5,g6}) {
            l.getStyleClass().add("about-text");
            l.setWrapText(true);
        }

        // ---------------------------------------------
        // SECTION 4 : Conseils d'utilisation
        // ---------------------------------------------
        Label tipsTitle = new Label("💡 Conseils d’utilisation");
        tipsTitle.getStyleClass().add("about-subtitle");

        Label t1 = new Label("• Commencer avec peu de simulations (ex : 50 000).");
        Label t2 = new Label("• Monter graduellement vers 200k–1M pour stabiliser les distributions.");
        Label t3 = new Label("• Utiliser Monte-Carlo parallèle si votre CPU a plusieurs cœurs.");
        Label t4 = new Label("• Comparer Monte-Carlo vs Black-Scholes pour valider les résultats.");
        Label t5 = new Label("• Regarder l’onglet IC95% pour juger la fiabilité du prix.");

        for (Label l : new Label[]{t1,t2,t3,t4,t5}) {
            l.getStyleClass().add("about-text");
            l.setWrapText(true);
        }

        // ---------------------------------------------
        // SECTION 5 : Liens externes
        // ---------------------------------------------
        Label linksTitle = new Label("🔗 Ressources externes");
        linksTitle.getStyleClass().add("about-subtitle");

        Hyperlink link1 = new Hyperlink("Documentation Black–Scholes (Wikipedia)");
        link1.getStyleClass().add("about-link");
        link1.setOnAction(_ -> openLink("https://fr.wikipedia.org/wiki/Formule_de_Black-Scholes"));

        Hyperlink link2 = new Hyperlink("Méthode de Monte Carlo — Explications");
        link2.getStyleClass().add("about-link");
        link2.setOnAction(_ -> openLink("https://en.wikipedia.org/wiki/Monte_Carlo_method"));

        // ---------------------------------------------
        // SECTION 6 : Crédits
        // ---------------------------------------------
        Label creditTitle = new Label("👥 Auteurs & Crédits");
        creditTitle.getStyleClass().add("about-subtitle");

        Label c1 = new Label("• Interface client JavaFX : Adam Boufous");
        Label c2 = new Label("• Moteur de calcul & serveur : Célia Glinel");
        Label c3 = new Label("• Version du logiciel : 1.0.0");

        for (Label l : new Label[]{c1,c2,c3}) {
            l.getStyleClass().add("about-text");
            l.setWrapText(true);
        }

        // Ajout de toutes les sections dans la carte
        card.getChildren().addAll(
                introTitle, intro1, intro2,
                methodTitle, m1, m2, m3,
                graphsTitle, g1, g2, g3, g4, g5, g6,
                tipsTitle, t1, t2, t3, t4, t5,
                linksTitle, link1, link2,
                creditTitle, c1, c2, c3
        );

        // Ajout au conteneur principal
        container.getChildren().addAll(topbar, title, card);

        // ScrollPane pour permettre de scroller toute la page
        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);           // Le contenu prend toute la largeur
        scroll.getStyleClass().add("about-scroll");

        root.setCenter(scroll);

        // Bouton retour → revient à la HomeView
        back.setOnAction(_ -> router.goTo(new HomeView(router).getView()));

        return root;
    }

    /**
     * Ouvre un lien dans le navigateur par défaut.
     * @param url URL à ouvrir
     */
    private void openLink(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ignored) {
            // En cas d'erreur : rien, on ignore (navigation facultative)
        }
    }
}
