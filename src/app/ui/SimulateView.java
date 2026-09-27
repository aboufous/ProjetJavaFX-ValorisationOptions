package app.ui;

import app.simulate.*;
import app.simulate.*;
import appli.export.*;
import appli.history.*;
import appli.net.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

/**
 * SimulateView assemble toute l’interface de la page de simulation.
 *
 * <p>Elle regroupe :
 * - le formulaire de paramètres (SimForm)
 * - la carte Résultats (SimResultsCard)
 * - les 6 graphiques de la méthode sélectionnée (MethodChartsView)
 * - les 4 graphiques de comparaison (ComparisonCharts)
 * - la logique métier asynchrone via SimulateController
 * - le chargement des anciens résultats via HistoryLoader
 *
 * La vue est désormais propre, modulaire, lisible et entièrement documentée.
 */
public class SimulateView {

    /** Router utilisé pour revenir à l’accueil. */
    private final Router router;

    /** Résultats de simulation → utilisés pour sauvegarde et export. */
    private final java.util.Map<String, PricingResponse> lastResults = new java.util.HashMap<>();

    /** Paramètres JSON → conservés pour export PDF / CSV. */
    private final java.util.Map<String, String> lastParams = new java.util.HashMap<>();

    // Sous-composants (classes modulaires extraites)
    private SimForm form;
    private SimResultsCard resultsCard;
    private MethodChartsView charts;
    private ComparaisonCharts compare;
    private SimulateController controller;
    private HistoryLoader historyLoader;

    // Containers utilisés pour injecter erreurs, résultats, etc.
    private VBox leftColumn;
    private VBox resultsCardContainer;

    /**
     * Constructeur.
     * @param router Router global permettant la navigation.
     */
    public SimulateView(Router router) {
        this.router = router;
    }

    /**
     * Construit et assemble toute l’interface de la page.
     *
     * @return Le root JavaFX (Parent) prêt à être affiché.
     */
    public Parent getView() {

        BorderPane root = new BorderPane();
        root.getStylesheets().add("data:text/css," + Styles.global());

        // Chargement optionnel du thème trading.css
        try {
            root.getStylesheets().add(
                    getClass().getResource("trading.css").toExternalForm()
            );
        } catch (Exception ignored) {}

        // ---------------------------------------------------------------------
        // TOP — bouton Retour
        // ---------------------------------------------------------------------
        Button back = new Button("← Retour");
        back.getStyleClass().add("menu-btn");
        back.setOnAction(e -> router.goTo(new HomeView(router).getView()));

        HBox topBar = new HBox(back);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 0, 0, 10));

        root.setTop(topBar);

        // ---------------------------------------------------------------------
        // TITRE
        // ---------------------------------------------------------------------
        Text title = new Text("Simulation d’options");
        title.setStyle("-fx-font-size: 26; -fx-fill: white; -fx-font-weight: 700;");

        // ---------------------------------------------------------------------
        // FORMULAIRE (SimForm)
        // ---------------------------------------------------------------------
        form = new SimForm();              // structure du formulaire
        Node formNode = form.build();      // noeud JavaFX construit

        Button simulateButton = new Button("SIMULER");
        simulateButton.getStyleClass().addAll("menu-btn", "primary");

        VBox formCard = new VBox();
        formCard.setSpacing(20);
        formCard.setPadding(new Insets(25));
        formCard.setStyle("-fx-background-color:#1e1f26; -fx-background-radius:18; -fx-border-color:#3a3b44;");
        formCard.getChildren().addAll(formNode, simulateButton);

        // ---------------------------------------------------------------------
        // CARTE RÉSULTATS (SimResultsCard)
        // ---------------------------------------------------------------------
        resultsCard = new SimResultsCard();
        Node resultsNode = resultsCard.build();   // noeud Vue

        resultsCardContainer = new VBox(resultsNode);

        // ---------------------------------------------------------------------
        // COLONNE GAUCHE (titre + formulaire + résultats)
        // ---------------------------------------------------------------------
        leftColumn = new VBox();
        leftColumn.setSpacing(18);
        leftColumn.setPadding(new Insets(15, 10, 15, 25));
        leftColumn.setPrefWidth(420);
        leftColumn.getChildren().addAll(title, formCard, resultsCardContainer);

        // ---------------------------------------------------------------------
        // GRAPHES MÉTHODE — 6 onglets (MethodChartsView)
        // ---------------------------------------------------------------------
        charts = new MethodChartsView();
        Node chartsNode = charts.build();

        Label graphTitle = new Label("Visualisation — méthode sélectionnée");
        graphTitle.setStyle("-fx-text-fill:white; -fx-font-size:18;");

        VBox graphMethodCard = new VBox();
        graphMethodCard.setSpacing(15);
        graphMethodCard.setPadding(new Insets(20));
        graphMethodCard.setStyle("-fx-background-color:#05060a; -fx-background-radius:18; -fx-border-color:#293446;");
        graphMethodCard.getChildren().addAll(graphTitle, chartsNode);

        // ---------------------------------------------------------------------
        // GRAPHES DE COMPARAISON (ComparisonCharts)
        // ---------------------------------------------------------------------
        compare = new ComparaisonCharts();
        Node compareNode = compare.build();

        VBox rightZone = new VBox();
        rightZone.setSpacing(20);
        rightZone.setPadding(new Insets(15, 25, 15, 10));
        rightZone.getChildren().addAll(graphMethodCard, compareNode);

        VBox.setVgrow(graphMethodCard, Priority.ALWAYS);

        // ---------------------------------------------------------------------
        // LAYOUT GLOBAL
        // ---------------------------------------------------------------------
        HBox mainRow = new HBox(leftColumn, rightZone);
        mainRow.setSpacing(25);

        root.setCenter(mainRow);

        // ---------------------------------------------------------------------
        // CONTROLLER — gère simulation, erreurs, loader, MAJ des graphes
        // ---------------------------------------------------------------------
        controller = new SimulateController(
                form,
                resultsCard,
                charts,
                compare,
                resultsCardContainer,
                leftColumn,
                lastResults,
                lastParams
        );

        // ---------------------------------------------------------------------
        // HISTORY LOADER — recharge des anciennes simulations
        // ---------------------------------------------------------------------
        historyLoader = new HistoryLoader(
                form,
                compare,
                lastResults,
                lastParams
        );

        // ---------------------------------------------------------------------
        // ACTION SIMULER
        // ---------------------------------------------------------------------
        simulateButton.setOnAction(e -> controller.runSimulation());

        // ---------------------------------------------------------------------
        // ACTION SAVE
        // ---------------------------------------------------------------------
        resultsCard.saveButton.setOnAction(e -> openSaveDialog());

        // ---------------------------------------------------------------------
        // ACTION EXPORT
        // ---------------------------------------------------------------------
        resultsCard.exportButton.setOnAction(e -> openExportDialog());

        return root;
    }

    /**
     * Chargement des paramètres + données d’une simulation depuis l’historique.
     */
    public void loadFromHistory(HistoryItem item) {
        historyLoader.load(item);
    }

    // =========================================================================
    // DIALOGUE — Sauvegarde
    // =========================================================================
    private void openSaveDialog() {

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Sauvegarder la simulation");

        Label nameLabel = new Label("Nom :");
        TextField nameField = new TextField();
        nameField.setPromptText("Obligatoire");

        VBox methodBox = new VBox(10);
        java.util.List<CheckBox> methodChecks = new java.util.ArrayList<>();

        // Méthodes disponibles
        for (String m : lastResults.keySet()) {
            CheckBox cb = new CheckBox(m);
            cb.setSelected(true);
            cb.setStyle("-fx-text-fill:black;");
            methodChecks.add(cb);
            methodBox.getChildren().add(cb);
        }

        VBox box = new VBox(
                nameLabel, nameField,
                new Label("Méthodes à sauvegarder :"), methodBox
        );
        box.setSpacing(15);
        box.setPadding(new Insets(20));

        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        // Empêche validation sans nom
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (nameField.getText().trim().isEmpty()) event.consume();
        });

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {

                String label = nameField.getText().trim();
                HistoryDB db = new HistoryDB();

                java.util.List<HistoryItem.MethodResult> selected = new java.util.ArrayList<>();

                for (CheckBox cb : methodChecks) {
                    if (cb.isSelected()) {
                        String m = cb.getText();
                        PricingResponse resp = lastResults.get(m);
                        String params = lastParams.get(m);

                        selected.add(new HistoryItem.MethodResult(
                                m, resp.call, resp.put, resp.timeMs, resp.variance, params
                        ));
                    }
                }

                HistoryItem item = new HistoryItem(
                        java.time.LocalDateTime.now().toString(),
                        label,
                        selected
                );

                db.save(item);
            }
            return null;
        });

        dialog.showAndWait();
    }

    // =========================================================================
    // DIALOGUE — Export PDF / CSV
    // =========================================================================
    private void openExportDialog() {

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Exporter la simulation");

        Label nameLabel = new Label("Nom du fichier :");
        TextField nameField = new TextField();
        nameField.setPromptText("ex: simulation_001");

        CheckBox pdfCheck = new CheckBox("PDF");
        CheckBox csvCheck = new CheckBox("CSV");
        pdfCheck.setSelected(true);

        HBox formatBox = new HBox(12, pdfCheck, csvCheck);

        VBox methodBox = new VBox(8);
        java.util.List<CheckBox> methodChecks = new java.util.ArrayList<>();

        for (String m : lastResults.keySet()) {
            CheckBox cb = new CheckBox(m);
            cb.setSelected(true);
            cb.setStyle("-fx-text-fill:black;");
            methodChecks.add(cb);
            methodBox.getChildren().add(cb);
        }

        VBox content = new VBox(
                nameLabel, nameField,
                new Label("Format d’export :"), formatBox,
                new Label("Méthodes à exporter :"), methodBox
        );
        content.setSpacing(15);
        content.setPadding(new Insets(20));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        // Empêcher validation sans nom
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (nameField.getText().trim().isEmpty()) event.consume();
        });

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {

                String baseName = nameField.getText().trim();

                java.util.Map<String, PricingResponse> selectedResults = new java.util.HashMap<>();
                java.util.Map<String, String> selectedParams = new java.util.HashMap<>();

                for (CheckBox cb : methodChecks) {
                    if (cb.isSelected()) {
                        String m = cb.getText();
                        selectedResults.put(m, lastResults.get(m));
                        selectedParams.put(m, lastParams.get(m));
                    }
                }

                // CSV
                if (csvCheck.isSelected()) {
                    FileChooser chooser = new FileChooser();
                    chooser.setTitle("Enregistrer CSV");
                    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier CSV", "*.csv"));
                    chooser.setInitialFileName(baseName + ".csv");

                    java.io.File f = chooser.showSaveDialog(null);
                    if (f != null)
                        ExportCSV.generate(selectedResults, selectedParams, f.getAbsolutePath());
                }

                // PDF
                if (pdfCheck.isSelected()) {
                    FileChooser chooser = new FileChooser();
                    chooser.setTitle("Enregistrer PDF");
                    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf"));
                    chooser.setInitialFileName(baseName + ".pdf");

                    java.io.File f = chooser.showSaveDialog(null);
                    if (f != null)
                        ExportPDF.generate(
                                selectedResults, selectedParams,
                                charts.payoffChart,
                                charts.pathsChart,
                                charts.distSTChart,
                                charts.distPayChart,
                                charts.convChart,
                                charts.statsChart,
                                f.getAbsolutePath()
                        );
                }
            }
            return null;
        });

        dialog.showAndWait();
    }
}
