package app.simulate;

import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.paint.Color;

/**
 * MethodChartsView regroupe tous les graphiques liés à la méthode
 * sélectionnée dans SimulateView.
 *
 * <p>Ce composant contient 6 onglets :
 * - Payoff théorique Call & Put
 * - Trajectoires simulées S(t)
 * - Convergence Monte-Carlo
 * - Distribution du prix final S(T)
 * - Distribution des payoffs
 * - Erreur & IC 95%
 *
 * Cette classe ne gère aucune logique métier : elle expose simplement
 * les graphiques prêts à être remplis par SimulateView ou SimulateController.
 */
public class MethodChartsView {

    /** Graphique Payoff Call/Put. */
    public LineChart<Number, Number> payoffChart;

    /** Graphique des trajectoires S(t). */
    public LineChart<Number, Number> pathsChart;

    /** Graphique de convergence. */
    public LineChart<Number, Number> convChart;

    /** Distribution de S(T). */
    public BarChart<String, Number> distSTChart;

    /** Distribution du payoff. */
    public BarChart<String, Number> distPayChart;

    /** Graphique de l’erreur / IC95%. */
    public LineChart<Number, Number> statsChart;

    /**
     * Construit le TabPane complet contenant les 6 graphiques.
     * @return un TabPane prêt à être inséré dans SimulateView.
     */
    public Node build() {

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(
                buildPayoffTab(),
                buildPathsTab(),
                buildConvTab(),
                buildDistSTTab(),
                buildDistPayTab(),
                buildStatsTab()
        );

        return tabPane;
    }

    // --------------------------------------------------------------
    //  ONGLET 1 — PAYOFF
    // --------------------------------------------------------------
    private Tab buildPayoffTab() {

        Tab tab = new Tab("Payoff");

        NumberAxis ax = new NumberAxis();
        NumberAxis ay = new NumberAxis();

        ax.setLabel("Prix final S(T)");
        ay.setLabel("Payoff");

        ax.setTickLabelFill(Color.web("#d4d7e1"));
        ay.setTickLabelFill(Color.web("#d4d7e1"));

        LineChart<Number, Number> chart = new LineChart<>(ax, ay);
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setTitle("Payoff théorique — Call & Put");

        payoffChart = chart;
        tab.setContent(chart);

        return tab;
    }

    // --------------------------------------------------------------
    //  ONGLET 2 — TRAJECTOIRES
    // --------------------------------------------------------------
    private Tab buildPathsTab() {

        Tab tab = new Tab("Trajectoires");

        NumberAxis ax = new NumberAxis();
        NumberAxis ay = new NumberAxis();

        ax.setLabel("Temps (jours)");
        ay.setLabel("Prix S(t)");

        ax.setTickLabelFill(Color.web("#d4d7e1"));
        ay.setTickLabelFill(Color.web("#d4d7e1"));

        LineChart<Number, Number> chart = new LineChart<>(ax, ay);
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setTitle("Trajectoires simulées de l’actif S(t)");

        // Corrige le titre (rendu tardif)
        Platform.runLater(() -> {
            Node t = chart.lookup(".chart-title");
            if (t != null)
                t.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        });

        pathsChart = chart;
        tab.setContent(chart);

        return tab;
    }

    // --------------------------------------------------------------
    //  ONGLET 3 — CONVERGENCE
    // --------------------------------------------------------------
    private Tab buildConvTab() {

        Tab tab = new Tab("Convergence");

        NumberAxis ax = new NumberAxis();
        NumberAxis ay = new NumberAxis();

        ax.setLabel("Nombre de simulations");
        ay.setLabel("Estimation");

        ax.setTickLabelFill(Color.web("#d4d7e1"));
        ay.setTickLabelFill(Color.web("#d4d7e1"));

        LineChart<Number, Number> chart = new LineChart<>(ax, ay);
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.getStyleClass().add("conv-chart");
        chart.setTitle("Convergence Monte Carlo");

        // Correction du titre sombre
        Platform.runLater(() -> {
            Node t = chart.lookup(".chart-title");
            if (t != null)
                t.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        });

        chart.setLegendVisible(false);
        chart.setLegendSide(Side.BOTTOM);

        convChart = chart;
        tab.setContent(chart);

        return tab;
    }

    // --------------------------------------------------------------
    //  ONGLET 4 — DISTRIBUTION S(T)
    // --------------------------------------------------------------
    private Tab buildDistSTTab() {

        Tab tab = new Tab("Distribution S(T)");

        CategoryAxis ax = new CategoryAxis();
        NumberAxis ay = new NumberAxis();

        ax.setLabel("Prix final S(T)");
        ay.setLabel("Occurrences");

        ax.setTickLabelFill(Color.web("#d4d7e1"));
        ay.setTickLabelFill(Color.web("#d4d7e1"));

        BarChart<String, Number> chart = new BarChart<>(ax, ay);
        chart.setLegendVisible(false);
        chart.setTitle("Distribution du prix final S(T)");
        chart.getStyleClass().add("distst-chart");

        distSTChart = chart;
        tab.setContent(chart);

        return tab;
    }

    // --------------------------------------------------------------
    //  ONGLET 5 — DISTRIBUTION PAYOFF
    // --------------------------------------------------------------
    private Tab buildDistPayTab() {

        Tab tab = new Tab("Distribution Payoff");

        CategoryAxis ax = new CategoryAxis();
        NumberAxis ay = new NumberAxis();

        ax.setLabel("Payoff");
        ay.setLabel("Fréquence");

        ax.setTickLabelFill(Color.web("#d4d7e1"));
        ay.setTickLabelFill(Color.web("#d4d7e1"));

        BarChart<String, Number> chart = new BarChart<>(ax, ay);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setTitle("Distribution du Payoff simulé");
        chart.getStyleClass().add("distpay-chart");

        distPayChart = chart;
        tab.setContent(chart);

        return tab;
    }

    // --------------------------------------------------------------
    //  ONGLET 6 — ERREUR / IC 95%
    // --------------------------------------------------------------
    private Tab buildStatsTab() {

        Tab tab = new Tab("Erreur / IC 95%");

        NumberAxis ax = new NumberAxis();
        NumberAxis ay = new NumberAxis();

        ax.setLabel("Itérations / Points");
        ay.setLabel("Erreur IC 95%");
        ax.setTickLabelFill(Color.web("#d4d7e1"));
        ay.setTickLabelFill(Color.web("#d4d7e1"));

        LineChart<Number, Number> chart = new LineChart<>(ax, ay);
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.getStyleClass().add("error-chart");
        chart.setLegendVisible(false);
        chart.setTitle("Intervalle de Confiance 95% — Erreur estimée");

        statsChart = chart;
        tab.setContent(chart);

        return tab;
    }
}
