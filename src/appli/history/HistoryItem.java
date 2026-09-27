package appli.history;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

/**
 * Représente un ensemble complet de résultats d'une simulation sauvegardée.
 *
 * Un HistoryItem correspond à UNE entrée dans l’historique :
 *  - un timestamp (date/heure)
 *  - un label donné par l’utilisateur
 *  - la liste des méthodes (Black-Scholes, MC-Seq, MC-Par) exécutées à ce moment
 *
 * Chaque méthode est stockée sous forme d'un objet MethodResult.
 */
public class HistoryItem {

    /** Date/heure au format ISO. Exemple : "2025-11-17T15:32:10" */
    public final String timestamp;

    /** Label donné par l’utilisateur ("Simu 1", "Comparaison 200k", etc.) */
    public final String label;

    /** Liste des résultats pour TOUTES les méthodes sélectionnées lors de la sauvegarde. */
    public final List<MethodResult> methods;

    /**
     * Constructeur d’un élément d’historique.
     *
     * @param timestamp  Date/heure au format ISO
     * @param label      Nom donné par l’utilisateur
     * @param methods    Liste des résultats des méthodes (Call/Put/Variance/etc.)
     */
    public HistoryItem(String timestamp, String label, List<MethodResult> methods) {
        this.timestamp = timestamp;
        this.label = label;
        this.methods = methods;
    }

    // =====================================================================
    //  CONVERSION VERS JSON POUR SAUVEGARDE EN BASE
    // =====================================================================

    /**
     * Convertit l’objet HistoryItem en JSON.
     * Ce JSON est stocké dans la base SQLite comme une chaîne unique.
     *
     * @return JSON complet de l’item sous forme de String.
     */
    public String toJson() {

        JSONObject root = new JSONObject();
        root.put("timestamp", timestamp);
        root.put("label", label);

        // Tableau JSON contenant les résultats de chaque méthode
        JSONArray arr = new JSONArray();

        for (MethodResult m : methods) {

            JSONObject o = new JSONObject();
            o.put("method", m.method);
            o.put("call", m.call);
            o.put("put", m.put);
            o.put("timeMs", m.timeMs);

            // variance est null pour Black-Scholes → on met JSON null
            if (m.variance != null)
                o.put("variance", m.variance);
            else
                o.put("variance", JSONObject.NULL);

            // paramètres envoyés au serveur (S0, K, T, sigma…)
            o.put("params", m.paramsJson);

            arr.put(o);
        }

        root.put("methods", arr);
        return root.toString();
    }

    // =====================================================================
    //  RECONSTRUCTION D'UN HistoryItem À PARTIR DU JSON DE LA BASE
    // =====================================================================

    /**
     * Reconstruit un HistoryItem depuis le JSON stocké en base.
     *
     * @param json JSON complet de l’entrée
     * @return un objet HistoryItem prêt à l'emploi
     */
    public static HistoryItem fromJson(String json) {

        JSONObject root = new JSONObject(json);

        String timestamp = root.getString("timestamp");
        String label = root.getString("label");

        // Liste reconstituée des MethodResult
        JSONArray arr = root.getJSONArray("methods");
        java.util.List<MethodResult> list = new java.util.ArrayList<>();

        for (int i = 0; i < arr.length(); i++) {

            JSONObject o = arr.getJSONObject(i);

            list.add(new MethodResult(
                    o.getString("method"),
                    o.getDouble("call"),
                    o.getDouble("put"),
                    o.getLong("timeMs"),

                    // variance = null pour Black-Scholes
                    o.isNull("variance") ? null : o.getDouble("variance"),

                    o.getString("params")
            ));
        }

        return new HistoryItem(timestamp, label, list);
    }

    // =====================================================================
    //  CLASS INTERNE : RÉSULTAT D'UNE MÉTHODE DE PRICING
    // =====================================================================

    /**
     * Représente le résultat d’UNE méthode de pricing
     * (Black-Scholes / MC-Seq / MC-Par).
     *
     * Les champs sont immuables (final), ce qui garantit
     * la stabilité lors de la sauvegarde/rechargement.
     */
    public static class MethodResult {

        /** Nom de la méthode ("Black-Scholes", "MC-Seq", "MC-Par") */
        public final String method;

        /** Prix du Call */
        public final double call;

        /** Prix du Put */
        public final double put;

        /** Temps d'exécution (ms) */
        public final long timeMs;

        /**
         * Variance Monte-Carlo :
         *  - null pour Black-Scholes
         *  - valeur double pour MC
         */
        public final Double variance;

        /** JSON contenant les paramètres envoyés au serveur (S0/K/T/sim/etc.) */
        public final String paramsJson;

        /**
         * Constructeur d'une méthode sauvegardée.
         *
         * @param method     Nom de la méthode
         * @param call       Prix Call
         * @param put        Prix Put
         * @param timeMs     Temps d'exécution
         * @param variance   Variance Monte Carlo (null si BS)
         * @param paramsJson Paramètres utilisés pour cette méthode
         */
        public MethodResult(String method, double call, double put,
                            long timeMs, Double variance, String paramsJson)
        {
            this.method = method;
            this.call = call;
            this.put = put;
            this.timeMs = timeMs;
            this.variance = variance;
            this.paramsJson = paramsJson;
        }
    }
}
