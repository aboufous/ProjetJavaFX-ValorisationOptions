package app.simulate;

import appli.history.HistoryItem;
import appli.net.PricingResponse;

import javafx.scene.chart.*;


/**
 * HistoryLoader recharge une simulation sauvegardée (HistoryItem)
 * dans les composants du module de simulation :
 *
 * - Remplit les champs du formulaire (S0, K, T, sigma, r, N)
 * - Recharge les séries de comparaison (Call, Put, Time, Variance)
 * - Recharge lastResults et lastParams (pour re-sauvegarder ensuite)
 * - Sélectionne la bonne méthode dans le ChoiceBox
 *
 * Cette classe ne crée aucune interface graphique.
 * Elle se contente de remplir les objets fournis par SimulateView.
 */
public class HistoryLoader {

    private final SimForm form;
    private final ComparaisonCharts compare;
    private final java.util.Map<String, PricingResponse> lastResults;
    private final java.util.Map<String, String> lastParams;

    /**
     * @param form        Le formulaire (TextFields + ChoiceBox)
     * @param compare     Les graphiques de comparaison Call/Put/Time/Var
     * @param lastResults Map où stocker les anciens résultats (pour sauvegarde)
     * @param lastParams  Map où stocker les anciens paramètres JSON
     */
    public HistoryLoader(
            SimForm form,
            ComparaisonCharts compare,
            java.util.Map<String, PricingResponse> lastResults,
            java.util.Map<String, String> lastParams
    ) {
        this.form = form;
        this.compare = compare;
        this.lastResults = lastResults;
        this.lastParams = lastParams;
    }

    /**
     * Recharge complètement l’interface et les données
     * à partir d’un objet HistoryItem précédemment sauvegardé.
     *
     * @param item élément d’historique contenant :
     *             - paramètres JSON,
     *             - résultats Call/Put/Var,
     *             - nom de méthode,
     *             - horodatage.
     */
    public void load(HistoryItem item) {

        if (item.methods.isEmpty())
            return;

        // ------------------------------------------
        // 1. Chargement des paramètres dans le FORMULAIRE
        // ------------------------------------------
        HistoryItem.MethodResult first = item.methods.get(0);

        form.s0Field.setText(extractParam(first.paramsJson, "S0"));
        form.kField.setText(extractParam(first.paramsJson, "K"));
        form.tField.setText(extractParam(first.paramsJson, "T"));
        form.volField.setText(extractParam(first.paramsJson, "sigma"));
        form.rField.setText(extractParam(first.paramsJson, "r"));

        String sims = extractParam(first.paramsJson, "simulations");
        form.simField.setText(sims != null ? sims : "100000");

        // ------------------------------------------
        // 2. Sélection de la méthode
        // ------------------------------------------
        form.methodChoice.setValue(first.method);

        // ------------------------------------------
        // 3. Nettoyage des graphiques existants
        // ------------------------------------------
        compare.clearAll();
        lastResults.clear();
        lastParams.clear();

        // ------------------------------------------
        // 4. Remplissage avec TOUTES les méthodes sauvegardées
        // ------------------------------------------
        for (HistoryItem.MethodResult mr : item.methods) {

            // Mise à jour des graphiques de comparaison
            putOrReplace(compare.callSeries, mr.method, mr.call);
            putOrReplace(compare.putSeries, mr.method, mr.put);
            putOrReplace(compare.timeSeries, mr.method, mr.timeMs);

            if (mr.variance != null)
                putOrReplace(compare.varSeries, mr.method, mr.variance);


            // Reconstruction des objets PricingResponse (pour sauvegarde)
            PricingResponse resp = new PricingResponse(
                    mr.call,
                    mr.put,
                    mr.timeMs,
                    mr.variance
            );

            // Les stocker pour un futur "Sauvegarder"
            lastResults.put(mr.method, resp);
            lastParams.put(mr.method, mr.paramsJson);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Ajoute ou remplace une valeur dans une série existante. */
    private void putOrReplace(XYChart.Series<String, Number> series,
                              String method, Number value) {

        for (XYChart.Data<String, Number> d : series.getData()) {
            if (d.getXValue().equals(method)) {
                d.setYValue(value);
                return;
            }
        }

        series.getData().add(new XYChart.Data<>(method, value));
    }

    /**
     * Extrait une valeur d’un champ JSON (format simplifié)
     * dans la partie "params".
     */
    private String extractParam(String json, String key) {
        try {
            int paramsStart = json.indexOf("\"params\"");
            if (paramsStart == -1) return null;

            int braceStart = json.indexOf("{", paramsStart);
            int braceEnd = json.indexOf("}", braceStart);
            if (braceStart == -1 || braceEnd == -1) return null;

            String paramsJson = json.substring(braceStart + 1, braceEnd);

            key = "\"" + key + "\"";
            int k = paramsJson.indexOf(key);
            if (k == -1) return null;

            int colon = paramsJson.indexOf(":", k);
            int comma = paramsJson.indexOf(",", colon);
            if (comma == -1) comma = paramsJson.length();

            String raw = paramsJson.substring(colon + 1, comma).trim();
            return raw.replace("\"", "");
        }
        catch (Exception e) {
            return null;
        }
    }
}
