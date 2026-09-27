package app.ui;

import appli.history.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import java.util.List;

/**
 * HistoryView affiche la liste des simulations enregistrées dans l’historique.
 *
 * <p>Elle liste les éléments stockés dans HistoryDB et permet :
 * - d'afficher chaque simulation sous forme de carte,
 * - de revoir les valeurs Call / Put / Variance pour chaque méthode,
 * - de recharger une simulation dans SimulateView (“Charger”),
 * - de revenir à l'accueil.
 *
 * La vue est retournée sous forme de Parent pour être utilisée par le Router.
 */
public class HistoryView {

    /** Router pour naviguer vers d'autres écrans. */
    private final Router router;

    public HistoryView(Router router) {
        this.router = router;
    }

    /**
     * Construit et retourne la vue JavaFX de la page "Historique".
     *
     * @return Le nœud racine de l’interface (BorderPane).
     */
    public Parent getView() {

        // Chargement des données de l'historique
        HistoryDB db = new HistoryDB();
        List<HistoryItem> items = db.loadAll();

        // Conteneur principal
        BorderPane root = new BorderPane();
        root.getStylesheets().add("data:text/css," + Styles.global());

        // Conteneur vertical : bouton retour + titre + liste scrollable
        VBox container = new VBox(16);
        container.setPadding(new Insets(20));

        // ---- Barre supérieure : bouton retour + Titre ----
        Button back = new Button("← Accueil");
        back.getStyleClass().add("menu-btn");
        back.setOnAction(_ -> router.goTo(new HomeView(router).getView()));

        Text title = new Text("Historique des simulations");
        title.setStyle("-fx-font-size: 26; -fx-fill: white; -fx-font-weight: 700;");

        VBox header = new VBox(10, back, title);

        // ---- Liste des simulations ----
        VBox listBox = new VBox(12);

        if (items.isEmpty()) {
            // Message si aucun historique n'est disponible
            listBox.getChildren().add(new Label("Aucune simulation enregistrée pour le moment."));
        } else {
            // Pour chaque élément : création d'une carte graphique
            for (HistoryItem it : items) {
                listBox.getChildren().add(makeCard(it));
            }
        }

        // ScrollPane pour scroller la liste
        ScrollPane scroll = new ScrollPane(listBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent;");

        // Assemblage principal
        container.getChildren().addAll(header, scroll);
        root.setCenter(container);

        return root;   // ⬅ retourne un Parent, pas une Scene
    }

    // =====================================================================
    // Création d’une carte de simulation
    // =====================================================================

    /**
     * Construit une “carte” contenant les informations d'un HistoryItem :
     * - nom de la sauvegarde
     * - timestamp
     * - résultats pour chaque méthode (Call, Put, Variance)
     * - bouton Charger
     *
     * @param it Élément d’historique à afficher
     * @return Une carte UI sous forme de Pane
     */
    private Pane makeCard(HistoryItem it) {

        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color:#1e1f26; -fx-background-radius:12;");

        // ---- Titre / nom de la sauvegarde ----
        Label label = new Label(
                (it.label == null || it.label.isEmpty()) ? "(Sans nom)" : it.label
        );
        label.setStyle("-fx-text-fill:white; -fx-font-size:18; -fx-font-weight:700;");

        // ---- Timestamp ----
        Label time = new Label(it.timestamp);
        time.setStyle("-fx-text-fill:#9aa3b8; -fx-font-size:12;");

        // ---- Liste des résultats par méthode ----
        VBox methodList = new VBox(6);

        for (HistoryItem.MethodResult mr : it.methods) {

            VBox card = new VBox(2);
            card.setStyle(
                    "-fx-background-color:#2a2c33; "
                            + "-fx-background-radius:8; "
                            + "-fx-padding:8;"
            );

            Label lMethod = new Label("Méthode : " + mr.method);
            lMethod.setStyle("-fx-text-fill:#cfd8ff; -fx-font-size:14; -fx-font-weight:700;");

            Label lCall = new Label("Call = " + mr.call);
            Label lPut  = new Label("Put  = " + mr.put);
            Label lVar  = new Label(
                    mr.variance == null ? "Variance : --" : "Variance : " + mr.variance
            );

            lCall.setStyle("-fx-text-fill:#d4d7e1;");
            lPut.setStyle("-fx-text-fill:#d4d7e1;");
            lVar.setStyle("-fx-text-fill:#d4d7e1;");

            card.getChildren().addAll(lMethod, lCall, lPut, lVar);
            methodList.getChildren().add(card);
        }

        // ---- Bouton Charger ----
        Button load = new Button("Charger");
        load.getStyleClass().add("primary");

        load.setOnAction(_ -> {
            // Création de la vue de simulation
            SimulateView sv = new SimulateView(router);

            // On génère la vue AVANT le chargement des données
            Parent view = sv.getView();

            // Chargement des paramètres depuis l'historique
            sv.loadFromHistory(it);

            // Navigation vers la vue préconfigurée
            router.goTo(view);
        });

        HBox buttonZone = new HBox(load);
        buttonZone.setAlignment(Pos.CENTER_RIGHT);

        // Assemblage final de la carte
        box.getChildren().addAll(label, time, methodList, buttonZone);
        return box;
    }
}
