package app.simulate;

import appli.net.ClientSocket;
import appli.net.PricingRequest;
import appli.net.PricingResponse;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;



import java.util.Map;

/**
 * SimulateController centralise toute la logique métier de SimulateView :
 *
 * <p>
 * - Validation des champs du formulaire<br>
 * - Construction de la requête JSON<br>
 * - Envoi asynchrone au serveur<br>
 * - Gestion des erreurs réseau ou de validation<br>
 * - Mise à jour des graphiques (6 onglets + comparaison)<br>
 * - Mise à jour de la carte "Résultats"<br>
 *
 * Cette classe sépare complètement :
 * - la logique métier/serveur,
 * - de l'interface utilisateur (SimulateView).
 *
 * Elle ne crée aucune interface : elle reçoit des références vers :
 * - SimForm
 * - SimResultsCard
 * - MethodChartsView
 * - ComparisonCharts
 */
public class SimulateController {

    private final SimForm form;
    private final SimResultsCard resultsCard;
    private final MethodChartsView charts;
    private final ComparaisonCharts compare;
    private final Pane resultsCardContainer;
    private final VBox leftColumn;

    /** Données lastResults/lastParams gérées autrefois dans SimulateView. */
    public final Map<String, PricingResponse> lastResults;
    public final Map<String, String> lastParams;

    /** Overlay Loader (spinner). */
    private final javafx.scene.layout.StackPane overlay = new javafx.scene.layout.StackPane();

    /**
     * Constructeur.
     *
     * @param form          Le formulaire contenant tous les TextField.
     * @param resultsCard   La carte "Résultats" (Call/Put/Time).
     * @param charts        Les 6 onglets graphiques.
     * @param compare       Les 4 bar-charts de comparaison.
     * @param resultsCardContainer Le VBox contenant la carte (nécessaire pour showLoader()).
     * @param leftColumn    La colonne gauche (nécessaire pour showError()).
     * @param lastResults   Map des derniers résultats (clé = méthode).
     * @param lastParams    Map des paramètres JSON (clé = méthode).
     */
    public SimulateController(
            SimForm form,
            SimResultsCard resultsCard,
            MethodChartsView charts,
            ComparaisonCharts compare,
            Pane resultsCardContainer,
            VBox leftColumn,
            Map<String, PricingResponse> lastResults,
            Map<String, String> lastParams
    ) {
        this.form = form;
        this.resultsCard = resultsCard;
        this.charts = charts;
        this.compare = compare;
        this.resultsCardContainer = resultsCardContainer;
        this.leftColumn = leftColumn;
        this.lastResults = lastResults;
        this.lastParams = lastParams;
    }

    // -------------------------------------------------------------------------
    // VALIDATION
    // -------------------------------------------------------------------------

    /** Conversion + validation : "champ > 0". */
    private double parsePositive(javafx.scene.control.TextField f, String name) throws Exception {
        double v = Double.parseDouble(f.getText());
        if (v <= 0) throw new Exception(name + " doit être > 0");
        return v;
    }

    /** Vérifie qu'une valeur est dans un intervalle. */
    private void checkRange(double val, double min, double max, String name) throws Exception {
        if (val < min || val > max)
            throw new Exception(name + " doit être entre " + min + " et " + max);
    }

    // -------------------------------------------------------------------------
    // UI HELPERS : ERREURS + LOADER
    // -------------------------------------------------------------------------

    /** Affiche un message d'erreur sous le titre. */
    private void showError(String msg) {
        Label err = new Label(msg);
        err.setStyle("-fx-text-fill:red;");
        VBox b = new VBox(err);
        b.getStyleClass().add("error-box");
        leftColumn.getChildren().add(1, b);
    }

    private void showLoader() {
        javafx.scene.control.ProgressIndicator p = new javafx.scene.control.ProgressIndicator();
        overlay.getChildren().setAll(p);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.3);");
        resultsCardContainer.getChildren().add(overlay);
    }

    private void hideLoader() {
        resultsCardContainer.getChildren().remove(overlay);
    }

    // -------------------------------------------------------------------------
    // UI UPDATE : RESULTATS + GRAPHIQUES
    // -------------------------------------------------------------------------

    private void updateUI(PricingResponse resp) {
        resultsCard.callPrice.setText("Prix Call : " + resp.call);
        resultsCard.putPrice.setText("Prix Put  : " + resp.put);
        resultsCard.timeExec.setText("Temps (ms) : " + resp.timeMs);
    }

    /** Payoff Call/Put théorique. */
    public void updatePayoffChart(double K) {
        LineChart<Number, Number> chart = charts.payoffChart;
        chart.getData().clear();

        XYChart.Series<Number, Number> c = new XYChart.Series<>();
        XYChart.Series<Number, Number> p = new XYChart.Series<>();

        c.setName("Payoff Call");
        p.setName("Payoff Put");

        for (int s = 50; s <= 150; s += 2) {
            c.getData().add(new XYChart.Data<>(s, Math.max(0, s - K)));
            p.getData().add(new XYChart.Data<>(s, Math.max(0, K - s)));
        }

        chart.getData().addAll(c, p);
    }

    /** Trajectoires S(t). */
    public void updatePaths(PricingResponse resp) {
        var chart = charts.pathsChart;
        chart.getData().clear();

        if (resp.trajectories.isEmpty()) return;

        for (var path : resp.trajectories) {
            XYChart.Series<Number, Number> s = new XYChart.Series<>();
            for (int t = 0; t < path.size(); t++)
                s.getData().add(new XYChart.Data<>(t, path.get(t)));
            chart.getData().add(s);
        }
    }

    /** Convergence Monte Carlo. */
    public void updateConvergence(PricingResponse resp) {
        var chart = charts.convChart;
        chart.getData().clear();

        if (resp.convergence.isEmpty()) return;

        XYChart.Series<Number, Number> serie = new XYChart.Series<>();
        serie.setName("Estimation");

        for (var p : resp.convergence)
            serie.getData().add(new XYChart.Data<>(p.n, p.estimate));

        chart.getData().add(serie);
    }

    /** Distribution de S(T). */
    public void updateDistST(PricingResponse resp) {
        var chart = charts.distSTChart;
        chart.getData().clear();

        if (resp.sampleST.isEmpty()) return;

        int bins = 20;
        double min = resp.sampleST.stream().min(Double::compare).get();
        double max = resp.sampleST.stream().max(Double::compare).get();
        if (max == min) max = min + 1e-8;

        double step = (max - min) / bins;
        int[] counts = new int[bins];

        for (double v : resp.sampleST) {
            int idx = Math.min((int)((v - min) / step), bins - 1);
            if (idx < 0) idx = 0;
            counts[idx]++;
        }

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (int i = 0; i < bins; i++) {
            double a = min + i * step;
            double b = a + step;
            serie.getData().add(new XYChart.Data<>(String.format("%.2f - %.2f", a, b), counts[i]));
        }

        chart.getData().add(serie);
    }

    /** Distribution des payoffs. */
    public void updateDistPayoff(PricingResponse resp) {
        var chart = charts.distPayChart;
        chart.getData().clear();

        if (resp.samplePayoffs.isEmpty()) return;

        int bins = 20;
        double min = resp.samplePayoffs.stream().min(Double::compare).get();
        double max = resp.samplePayoffs.stream().max(Double::compare).get();
        double step = (max - min) / bins;
        if (step == 0) step = 1;

        int[] counts = new int[bins];

        for (double v : resp.samplePayoffs) {
            int i = Math.min((int)((v - min) / step), bins - 1);
            counts[i]++;
        }

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (int i = 0; i < bins; i++) {
            double a = min + i * step;
            double b = a + step;
            serie.getData().add(new XYChart.Data<>(String.format("%.2f – %.2f", a, b), counts[i]));
        }

        chart.getData().add(serie);
    }

    /** Erreur / IC 95%. */
    public void updateStats(PricingResponse resp) {
        var chart = charts.statsChart;
        chart.getData().clear();

        XYChart.Series<Number, Number> serie = new XYChart.Series<>();
        serie.setName("IC95");

        double base = resp.ic95;
        for (int n = 1; n <= 20; n++)
            serie.getData().add(new XYChart.Data<>(n, base / Math.sqrt(n)));

        chart.getData().add(serie);
    }

    // -------------------------------------------------------------------------
    // ACTION PRINCIPALE : LANCER UNE SIMULATION
    // -------------------------------------------------------------------------

    /**
     * Appelée par SimulateView lorsque l’utilisateur clique sur "SIMULER".
     * Cette méthode valide les champs, crée la requête, envoie au serveur,
     * et met à jour toute l’interface.
     */
    public void runSimulation() {

        // Nettoyage des anciens messages d’erreur
        leftColumn.getChildren().removeIf(n -> n.getStyleClass().contains("error-box"));

        try {
            // ---------------------------
            // VALIDATION DES PARAMÈTRES
            // ---------------------------
            double S0 = parsePositive(form.s0Field, "S0");
            checkRange(S0, 0.0001, 1_000_000, "S0");

            double K = parsePositive(form.kField, "K");
            checkRange(K, 0.0001, 1_000_000, "K");

            double T = parsePositive(form.tField, "T");
            checkRange(T, 0.0001, 50, "Maturité");

            double sig = parsePositive(form.volField, "Volatilité");
            checkRange(sig, 0.0001, 3, "Volatilité");

            double r = Double.parseDouble(form.rField.getText());
            checkRange(r, -0.5, 1, "Taux sans risque");

            long sims;
            try {
                sims = Long.parseLong(form.simField.getText());
            } catch (Exception ex) {
                throw new Exception("N doit être un entier valide.");
            }
            if (sims < 10_000)
                throw new Exception("N doit être entre 10 000 et 10 000 000");

            // ---------------------------
            // CHOIX DE LA MÉTHODE
            // ---------------------------
            String methodId =
                    form.methodChoice.getValue().equals("Black-Scholes") ? "Black-Scholes" :
                            form.methodChoice.getValue().equals("Monte Carlo séquentiel") ? "MC-Seq" :
                                    "MC-Par";

            // ---------------------------
            // CONSTRUCTION REQUÊTE JSON
            // ---------------------------
            PricingRequest req = new PricingRequest(methodId);
            req.set("spot", S0);
            req.set("strike", K);
            req.set("maturity", T);
            req.set("rate", r);
            req.set("volatility", sig);
            req.set("simulations", sims);

            showLoader();

            // ---------------------------
            // ENVOI ASYNCHRONE AU SERVEUR
            // ---------------------------
            ClientSocket socket = new ClientSocket("localhost", 8080);
            socket.sendAsync(req.toJson(), respJson -> {

                Platform.runLater(() -> {

                    hideLoader();

                    if (respJson == null) {
                        showError("Erreur réseau : impossible de contacter le serveur.");
                        return;
                    }

                    PricingResponse resp = PricingResponse.fromJson(respJson);

                    if (!resp.ok) {
                        showError(resp.errorMessage != null ? resp.errorMessage : "Erreur serveur.");
                        return;
                    }

                    // -----------------------
                    // MISE À JOUR UI + CHARTS
                    // -----------------------
                    updateUI(resp);
                    updatePayoffChart(K);
                    updatePaths(resp);
                    updateConvergence(resp);
                    updateDistST(resp);
                    updateDistPayoff(resp);
                    updateStats(resp);

                    // Mise à jour de la comparaison
                    compare.updateComparisonCharts(resp, form.methodChoice.getValue());

                    // Mise à jour lastResults / lastParams
                    lastResults.put(form.methodChoice.getValue(), resp);
                    lastParams.put(form.methodChoice.getValue(), req.toJson());
                });
            });

        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }
}
