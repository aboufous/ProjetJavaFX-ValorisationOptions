package app.simulate;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * SimResultsCard représente la carte "Résultats" située dans la colonne gauche
 * de la page de simulation.
 *
 * <p>Elle affiche :
 * - le prix Call,
 * - le prix Put,
 * - le temps d’exécution,
 * - un bouton de sauvegarde,
 * - un bouton d’export.
 *
 * Cette classe ne réalise aucune logique métier :
 * le SimulateView et le contrôleur gèrent la simulation, les erreurs,
 * la sauvegarde et l’export. Ce composant est purement graphique.
 */
public class SimResultsCard {

    /** Label affichant le prix du Call. */
    public final Label callPrice = new Label("Prix Call : --");

    /** Label affichant le prix du Put. */
    public final Label putPrice  = new Label("Prix Put  : --");

    /** Label affichant le temps d’exécution en ms. */
    public final Label timeExec  = new Label("Temps (ms) : --");

    /** Bouton : sauvegarder dans l’historique. */
    public final Button saveButton = new Button("Sauvegarder");

    /** Bouton : exporter en PDF/CSV. */
    public final Button exportButton = new Button("Exporter");

    /**
     * Construit la carte "Résultats" avec son style.
     *
     * @return Un VBox prêt à être ajouté dans SimulateView.
     */
    public Node build() {

        // --- Styles visuels ---
        callPrice.setStyle("-fx-text-fill:#d4d7e1;");
        putPrice.setStyle("-fx-text-fill:#d4d7e1;");
        timeExec.setStyle("-fx-text-fill:#d4d7e1;");

        saveButton.getStyleClass().add("menu-btn");
        exportButton.getStyleClass().addAll("menu-btn", "primary");

        Label title = new Label("Résultats");
        title.setStyle("-fx-text-fill:white; -fx-font-size:16px; -fx-font-weight:700;");

        VBox box = new VBox(18,
                title,
                callPrice,
                putPrice,
                timeExec,
                saveButton,
                exportButton
        );

        box.setPadding(new Insets(20));
        box.setStyle(
                "-fx-background-color: #1e1f26;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #3a3b44;"
        );

        return box;
    }
}
