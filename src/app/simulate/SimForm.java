package app.simulate;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/**
 * SimForm construit le formulaire d’entrée de la page de simulation.
 *
 * <p>Il contient tous les champs nécessaires à une requête :
 * - S₀ (spot initial)
 * - K (strike)
 * - T (maturité)
 * - σ (volatilité)
 * - r (taux sans risque)
 * - Méthode de pricing (ChoiceBox)
 * - N (nombre de simulations Monte Carlo)
 *
 * Cette classe ne réalise aucune logique métier :
 * elle se contente de construire l’interface et d’exposer les champs.
 *
 * Le SimulateView utilise cette instance pour lire les valeurs du formulaire.
 */
public class SimForm {

    /** Champ : Spot initial S0. */
    public final TextField s0Field = new TextField();

    /** Champ : Strike K. */
    public final TextField kField = new TextField();

    /** Champ : Maturité T. */
    public final TextField tField = new TextField();

    /** Champ : Volatilité σ. */
    public final TextField volField = new TextField();

    /** Champ : Taux sans risque r. */
    public final TextField rField = new TextField();

    /** Champ : Nombre de simulations Monte Carlo N. */
    public final TextField simField = new TextField();

    /** Choix de la méthode de pricing (Black-Scholes, MC-Seq, MC-Par). */
    public final ChoiceBox<String> methodChoice = new ChoiceBox<>();

    /** Style commun appliqué à tous les champs texte. */
    private static final String FIELD_STYLE =
            "-fx-background-color: #2c2d35; -fx-text-fill: white;" +
                    "-fx-background-radius: 10; -fx-border-radius: 10;" +
                    "-fx-font-size: 14px;";

    /**
     * Construit le formulaire complet sous forme d’un GridPane.
     *
     * @return Le nœud JavaFX représentant le formulaire.
     */
    public Node build() {

        // Configuration graphique des champs
        s0Field.setStyle(FIELD_STYLE);
        kField.setStyle(FIELD_STYLE);
        tField.setStyle(FIELD_STYLE);
        volField.setStyle(FIELD_STYLE);
        rField.setStyle(FIELD_STYLE);

        simField.setStyle(FIELD_STYLE);
        simField.setPrefWidth(180);
        simField.setPromptText("ex : 100000");
        simField.setText("100000"); // Valeur par défaut

        // Méthodes disponibles
        methodChoice.getItems().addAll(
                "Black-Scholes",
                "Monte Carlo séquentiel",
                "Monte Carlo parallèle"
        );
        methodChoice.getSelectionModel().selectFirst();

        // Construction du tableau (formulaire)
        GridPane grid = new GridPane();
        grid.setHgap(22);
        grid.setVgap(16);

        // Labels + info (icône "i")
        HBox s0Label     = infoLabel("S₀ :", "Prix initial du sous-jacent S0.\nBornes : 0.0001 – 1 000 000.");
        HBox kLabel      = infoLabel("K :",  "Prix d’exercice (strike).");
        HBox tLabel      = infoLabel("T :",  "Maturité en années.\nBornes : 0.0001 – 50.");
        HBox volLabel    = infoLabel("σ :",  "Volatilité annualisée.\nBornes : 0 – 3.");
        HBox rLabelBox   = infoLabel("r :",  "Taux sans risque.\nBornes : –0.5 à 1.");
        HBox methodLabel = infoLabel("Méthode :", "Algorithme utilisé pour le pricing.");
        HBox simLabelBox = infoLabel("N :", "Nombre de trajectoires Monte Carlo.\nBornes : 10 000 – 10 000 000.");

        // Placement dans le GridPane
        grid.add(s0Label,     0, 0);  grid.add(s0Field,     1, 0);
        grid.add(kLabel,      0, 1);  grid.add(kField,      1, 1);
        grid.add(tLabel,      0, 2);  grid.add(tField,      1, 2);
        grid.add(volLabel,    0, 3);  grid.add(volField,    1, 3);
        grid.add(rLabelBox,   0, 4);  grid.add(rField,      1, 4);
        grid.add(methodLabel, 0, 5);  grid.add(methodChoice,1, 5);
        grid.add(simLabelBox, 0, 6);  grid.add(simField,    1, 6);

        return grid;
    }

    /**
     * Construit un label avec une petite icône d’information "i",
     * affichant une bulle d’aide (Tooltip) au survol.
     *
     * @param txt Texte du label principal (ex : "S₀ :")
     * @param infoTxt Texte affiché dans la bulle d’aide
     * @return Un HBox contenant le label + l’icône d’info.
     */
    private HBox infoLabel(String txt, String infoTxt) {

        Label L = new Label(txt);
        L.setStyle("-fx-text-fill:#d4d7e1; -fx-font-size:14;");
        L.setMinWidth(70);
        L.setPrefWidth(70);
        L.setMaxWidth(70);  // Empêche les "..." sur les labels

        Label info = new Label("i");
        info.setStyle(
                "-fx-text-fill:#9fb4ff; -fx-background-color:#1a2335;" +
                        "-fx-background-radius:50; -fx-padding:2 6;"
        );

        Tooltip.install(info, new Tooltip(infoTxt));

        return new HBox(6, L, info);
    }
}
