package appli.net;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente la réponse JSON envoyée par le serveur après un calcul de pricing.
 *
 * Le client reçoit toujours un JSON au format :
 *
 *  SUCCESS :
 *  {
 *      "status": "OK",
 *      "call": 12.34,
 *      "put": 7.89,
 *      "timeMs": 15,
 *      "meanPayoff": 10.12,
 *      "stdPayoff": 5.12,
 *      "variance": 26.25,
 *      "ic95": 0.12,
 *      "sampleST": [...],
 *      "samplePayoffs": [...],
 *      "trajectories": [...],
 *      "convergence": [
 *          {"n":1000, "estimate":12.40},
 *          ...
 *      ]
 *  }
 *
 *  ERREUR :
 *  {
 *      "status": "ERROR",
 *      "message": "Description du problème"
 *  }
 *
 * Cette classe convertit automatiquement ce JSON en un objet Java
 * utilisable par l'interface JavaFX (histogrammes, trajectoires, convergence, etc.).
 */
public class PricingResponse {

    // ---------------------------------------------------------
    // Champs communs (SUCCESS ou ERROR)
    // ---------------------------------------------------------
    public boolean ok;              // status = "OK" ou "ERROR"
    public String errorMessage;     // message d'erreur éventuel

    // ---------------------------------------------------------
    // Résultats financiers de base
    // ---------------------------------------------------------
    public double call;             // Prix Call MC
    public double put;              // Prix Put MC
    public long timeMs;             // Temps de calcul côté serveur

    // ---------------------------------------------------------
    // Statistiques (optionnelles selon la méthode)
    // ---------------------------------------------------------
    public Double variance;         // Var[X] du payoff
    public Double meanPayoff;       // E[X]
    public Double stdPayoff;        // sqrt(Var)
    public Double ic95;             // Intervalle de confiance 95%

    // ---------------------------------------------------------
    // Données graphiques pour la visualisation
    // ---------------------------------------------------------
    public List<Double> sampleST = new ArrayList<>();
    public List<Double> samplePayoffs = new ArrayList<>();
    public List<List<Double>> trajectories = new ArrayList<>();

    /**
     * Point de convergence (n simulations, estimate)
     */
    public static class ConvPoint {
        public int n;
        public double estimate;
        public ConvPoint(int n, double estimate) {
            this.n = n;
            this.estimate = estimate;
        }
    }

    public List<ConvPoint> convergence = new ArrayList<>();


    // ---------------------------------------------------------
    // Constructeurs
    // ---------------------------------------------------------

    public PricingResponse() {}

    /** Constructeur spécial utilisé pour l'historique local */
    public PricingResponse(double call, double put, long timeMs, Double variance) {
        this.ok = true;
        this.call = call;
        this.put = put;
        this.timeMs = timeMs;
        this.variance = variance;
    }


    // ---------------------------------------------------------
    // Parsing JSON renvoyé par le serveur
    // ---------------------------------------------------------

    /**
     * Convertit une chaîne JSON brute envoyée par le serveur
     * en un objet PricingResponse exploitable par l'UI.
     */
    public static PricingResponse fromJson(String json) {

        JSONObject obj = new JSONObject(json);
        PricingResponse r = new PricingResponse();

        // -------------------------
        // 1) Vérification du status
        // -------------------------
        String status = obj.getString("status");
        r.ok = status.equals("OK");

        if (!r.ok) {
            r.errorMessage = obj.optString("message", "Erreur inconnue");
            return r; // pas d'autres champs disponibles en cas d'erreur
        }

        // -------------------------
        // 2) Champs de base
        // -------------------------
        r.call      = obj.getDouble("call");
        r.put       = obj.getDouble("put");
        r.timeMs    = obj.getLong("timeMs");
        r.variance  = obj.optDouble("variance", Double.NaN);

        // -------------------------
        // 3) Statistiques avancées
        // -------------------------
        r.meanPayoff = obj.has("meanPayoff") ? obj.getDouble("meanPayoff") : null;
        r.stdPayoff  = obj.has("stdPayoff")  ? obj.getDouble("stdPayoff")  : null;
        r.ic95       = obj.has("ic95")       ? obj.getDouble("ic95")       : null;

        // -------------------------
        // 4) S(T) samples
        // -------------------------
        if (obj.has("sampleST")) {
            JSONArray arr = obj.getJSONArray("sampleST");
            for (int i = 0; i < arr.length(); i++)
                r.sampleST.add(arr.getDouble(i));
        }

        // -------------------------
        // 5) Payoffs samples
        // -------------------------
        if (obj.has("samplePayoffs")) {
            JSONArray arr = obj.getJSONArray("samplePayoffs");
            for (int i = 0; i < arr.length(); i++)
                r.samplePayoffs.add(arr.getDouble(i));
        }

        // -------------------------
        // 6) Trajectoires GBM
        // -------------------------
        if (obj.has("trajectories")) {
            JSONArray trajArray = obj.getJSONArray("trajectories");
            for (int i = 0; i < trajArray.length(); i++) {
                JSONArray arr = trajArray.getJSONArray(i);
                List<Double> path = new ArrayList<>();
                for (int j = 0; j < arr.length(); j++)
                    path.add(arr.getDouble(j));
                r.trajectories.add(path);
            }
        }

        // -------------------------
        // 7) Courbe de convergence
        // -------------------------
        if (obj.has("convergence")) {
            JSONArray convArr = obj.getJSONArray("convergence");
            for (int i = 0; i < convArr.length(); i++) {
                JSONObject p = convArr.getJSONObject(i);
                r.convergence.add(
                        new ConvPoint(p.getInt("n"), p.getDouble("estimate"))
                );
            }
        }

        return r;
    }
}
