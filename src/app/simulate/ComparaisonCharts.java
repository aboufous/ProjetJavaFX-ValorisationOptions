package app.simulate;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;

/**
 * ComparisonCharts regroupe les 4 graphiques de comparaison utilisés dans SimulateView :
 *
 * - Call (BarChart)
 * - Put  (BarChart)
 * - Temps d'exécution (ms)
 * - Variance
 *
 * Chaque méthode de pricing produit un point (une barre) dans chaque graphique.
 * Les séries sont mises à jour via updateComparisonCharts().
 *
 * Cette classe ne fait aucun calcul métier : elle gère uniquement l'affichage.
 */
public class ComparaisonCharts {

    /** Série : prix du Call par méthode. */
    public final XYChart.Series<String, Number> callSeries = new XYChart.Series<>();

    /** Série : prix du Put par méthode. */
    public final XYChart.Series<String, Number> putSeries  = new XYChart.Series<>();

    /** Série : temps d'exécution (ms) par méthode. */
    public final XYChart.Series<String, Number> timeSeries = new XYChart.Series<>();

    /** Série : variance par méthode (non définie pour Black–Scholes). */
    public final XYChart.Series<String, Number> varSeries  = new XYChart.Series<>();

    /**
     * Construit l'ensemble des 4 graphiques sous forme d'un HBox,
     * encapsulé dans un ScrollPane horizontal.
     *
     * @return un Node contenant tous les graphiques de comparaison.
     */
    public Node build() {

        // ---------------------------------------------------------
        // CALL
        // ---------------------------------------------------------
        CategoryAxis ax1 = new CategoryAxis();
        NumberAxis ay1 = new NumberAxis();

        BarChart<String, Number> chCall = new BarChart<>(ax1, ay1);
        chCall.getData().add(callSeries);
        chCall.setLegendVisible(false);
        chCall.setTitle("Call");
        chCall.getStyleClass().addAll("trading-chart", "call-chart");

        // ---------------------------------------------------------
        // PUT
        // ---------------------------------------------------------
        CategoryAxis ax2 = new CategoryAxis();
        NumberAxis ay2 = new NumberAxis();

        BarChart<String, Number> chPut = new BarChart<>(ax2, ay2);
        chPut.getData().add(putSeries);
        chPut.setLegendVisible(false);
        chPut.setTitle("Put");
        chPut.getStyleClass().addAll("trading-chart", "put-chart");

        // ---------------------------------------------------------
        // TEMPS (ms)
        // ---------------------------------------------------------
        CategoryAxis ax3 = new CategoryAxis();
        NumberAxis ay3 = new NumberAxis();

        BarChart<String, Number> chTime = new BarChart<>(ax3, ay3);
        chTime.getData().add(timeSeries);
        chTime.setLegendVisible(false);
        chTime.setTitle("Temps (ms)");
        chTime.getStyleClass().addAll("trading-chart", "time-chart");

        // ---------------------------------------------------------
        // VARIANCE
        // ---------------------------------------------------------
        CategoryAxis ax4 = new CategoryAxis();
        NumberAxis ay4 = new NumberAxis();

        BarChart<String, Number> chVar = new BarChart<>(ax4, ay4);
        chVar.getData().add(varSeries);
        chVar.setLegendVisible(false);
        chVar.setTitle("Variance");
        chVar.getStyleClass().addAll("trading-chart", "var-chart");

        // ---------------------------------------------------------
        // RANGÉE HORIZONTALE
        // ---------------------------------------------------------
        HBox line = new HBox(30, chCall, chPut, chTime, chVar);
        line.setPadding(new Insets(10));

        ScrollPane scroll = new ScrollPane(line);
        scroll.setFitToHeight(true);

        return scroll;
    }

    // -------------------------------------------------------------------------
    // MÉTHODES UTILISÉES PAR SimulateView POUR METTRE À JOUR LES SÉRIES
    // -------------------------------------------------------------------------

    /**
     * Ajoute ou remplace la valeur pour une méthode dans une série donnée.
     *
     * @param series  Série à mettre à jour.
     * @param method  Nom de la méthode (ex : "Black-Scholes").
     * @param value   Valeur numérique à associer.
     */
    private void putOrReplace(XYChart.Series<String, Number> series,
                              String method,
                              Number value) {

        // Cherche si la méthode existe déjà dans la série
        for (XYChart.Data<String, Number> d : series.getData()) {
            if (d.getXValue().equals(method)) {
                d.setYValue(value); // mise à jour
                return;
            }
        }

        // Sinon, on ajoute une nouvelle entrée
        series.getData().add(new XYChart.Data<>(method, value));
    }

    /**
     * Met à jour les 4 graphiques de comparaison à partir de la réponse reçue
     * du serveur.
     *
     * @param resp       Résultat du pricing.
     * @param methodName Nom de la méthode choisie.
     */
    public void updateComparisonCharts(appli.net.PricingResponse resp, String methodName) {

        putOrReplace(callSeries, methodName, resp.call);
        putOrReplace(putSeries,  methodName, resp.put);
        putOrReplace(timeSeries, methodName, resp.timeMs);

        // Variance uniquement pour les méthodes Monte Carlo
        if (!methodName.equals("Black-Scholes") && resp.variance != null) {
            putOrReplace(varSeries, methodName, resp.variance);
        }
    }

    /**
     * Vide tous les graphiques (utilisé lorsqu’on recharge l’historique).
     */
    public void clearAll() {
        callSeries.getData().clear();
        putSeries.getData().clear();
        timeSeries.getData().clear();
        varSeries.getData().clear();
    }
}
