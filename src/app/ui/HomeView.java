package app.ui;

import app.graphics.FinanceLogo;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * HomeView représente l'écran d'accueil du client JavaFX.
 *
 * <p>Cet écran contient :
 * - un bandeau supérieur avec le logo INSA,
 * - un logo animé central (FinanceLogo),
 * - un titre "Valorisation d’options européennes",
 * - trois boutons de menu (Simuler / Historique / À propos),
 * - une petite signature en bas ("Célia Glinel & Adam Boufous"),
 * - et des animations d'entrée.
 *
 * La vue est construite dynamiquement dans getView(),
 * qui retourne un Parent utilisable par le Router.
 */
public class HomeView {

    /** Référence au Router pour permettre la navigation vers les autres vues. */
    private final Router router;

    /**
     * Constructeur : HomeView a besoin du Router
     * pour pouvoir déclencher navigation depuis les boutons du menu.
     */
    public HomeView(Router router) {
        this.router = router;
    }

    /**
     * Construit et retourne l'arbre JavaFX représentant l'écran d'accueil.
     *
     * @return La vue complète sous la forme d'un Parent (BorderPane).
     */
    public Parent getView() {

        // ====== LAYOUT GLOBAL ======
        BorderPane root = new BorderPane();
        root.getStylesheets().add("data:text/css," + Styles.global()); // Style global inline
        root.getStyleClass().add("fullscreen");                        // Classe CSS custom

        // ====== BANDEAU SUPÉRIEUR ======
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(12, 24, 0, 24)); // Marges internes (haut/gauche/droite)

        // Logo INSA
        ImageView insaLogo = new ImageView(new Image("file:src/resources/logoinsa.png"));
        insaLogo.setFitHeight(85);
        insaLogo.setPreserveRatio(true); // Maintient le ratio d'origine

        topBar.getChildren().add(insaLogo);
        root.setTop(topBar);

        // ====== CONTENU CENTRAL ======
        VBox centerCol = new VBox(20);
        centerCol.getStyleClass().add("center-col");
        centerCol.setAlignment(Pos.CENTER);

        // Logo graphique animé
        FinanceLogo logo = new FinanceLogo(96);
        logo.setOpacity(0); // Start invisible → animation fade-in

        Text title = new Text("Valorisation d’options européennes");
        title.getStyleClass().add("hero-title");

        VBox menuCol = new VBox(14);
        menuCol.getStyleClass().add("menu-col");
        menuCol.setFillWidth(true);

        // ====== BOUTONS ======
        Button bSimulate = new Button("Simuler");
        bSimulate.getStyleClass().addAll("menu-btn", "primary");
        bSimulate.setMaxWidth(520);
        bSimulate.setMinWidth(260);
        bSimulate.setPrefHeight(54);

        Button bHistory = new Button("Historique & sauvegardes");
        bHistory.getStyleClass().add("menu-btn");
        bHistory.setMaxWidth(520);
        bHistory.setMinWidth(260);
        bHistory.setPrefHeight(54);

        Button bAbout = new Button("À propos / Aide");
        bAbout.getStyleClass().add("menu-btn");
        bAbout.setMaxWidth(520);
        bAbout.setMinWidth(260);
        bAbout.setPrefHeight(54);

        menuCol.getChildren().addAll(bSimulate, bHistory, bAbout);

        Label hint = new Label("Célia Glinel & Adam Boufous");
        hint.getStyleClass().add("hint");

        centerCol.getChildren().addAll(logo, title, menuCol, hint);
        root.setCenter(centerCol);

        // ====== NAVIGATION ======
        bSimulate.setOnAction(_ ->
                router.goTo(new SimulateView(router).getView())
        );

        bHistory.setOnAction(_ ->
                router.goTo(new HistoryView(router).getView())
        );

        bAbout.setOnAction(_ ->
                router.goTo(new AboutView(router).getView())
        );

        // ====== ANIMATIONS ======
        FadeTransition fadeLogo = new FadeTransition(Duration.millis(700), logo);
        fadeLogo.setFromValue(0);
        fadeLogo.setToValue(1);

        TranslateTransition slideMenu =
                new TranslateTransition(Duration.millis(550), menuCol);
        slideMenu.setFromY(18);
        slideMenu.setToY(0);

        FadeTransition fadeTitle =
                new FadeTransition(Duration.millis(800), title);
        fadeTitle.setFromValue(0);
        fadeTitle.setToValue(1);

        // Lance toutes les animations en parallèle
        new ParallelTransition(fadeLogo, fadeTitle, slideMenu).play();

        return root;
    }
}
