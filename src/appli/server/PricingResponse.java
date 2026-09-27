package appli.server;

import appli.engine.*;
import java.util.List;
import java.util.Locale;

/**
 * Classe utilitaire chargée de transformer un objet SimulationResult
 * (produit par le moteur Monte Carlo / Black-Scholes côté serveur)
 * en une chaîne JSON structurée et lisible par le client JavaFX.
 *
 * Le rôle de cette classe est PUREMENT SÉRIEL : aucune logique métier.
 *
 * Deux types de réponses peuvent être générés :
 *
 *  ✔ SUCCESS :
 *    {
 *       "status": "OK",
 *       "call": 12.34,
 *       "put": 4.56,
 *       "timeMs": 15,
 *       "meanPayoff": ...,
 *       "stdPayoff":  ...,
 *       "variance":   ...,
 *       "ic95":       ...,
 *       "sampleST": [...],
 *       "samplePayoffs": [...],
 *       "trajectories": [...],
 *       "convergence": [...]
 *    }
 *
 *  ✔ ERROR :
 *    {
 *       "status": "ERROR",
 *       "message": "description"
 *    }
 *
 * Le JSON est construit manuellement (StringBuilder) pour garantir
 * une dépendance zéro à des bibliothèques externes (Gson, Jackson…)
 * et offrir le contrôle total sur le format.
 *
 */
public class PricingResponse {

    /**
     * Construit un JSON de succès à partir d'un objet SimulationResult.
     * Les valeurs numériques sont formatées avec Locale.US pour forcer l'utilisation
     * du point décimal (.) — indispensable pour un protocole JSON correct.
     *
     * @param res résultats complets du pricing
     * @return chaîne JSON prête à être envoyée via le socket
     */
    public static String success(SimulationResult res) {

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        sb.append("\"status\": \"OK\",");

        // Résultats financiers
        sb.append(String.format(Locale.US, "\"call\": %.4f,", res.callPrice));
        sb.append(String.format(Locale.US, "\"put\": %.4f,", res.putPrice));
        sb.append(String.format(Locale.US, "\"timeMs\": %d,", res.executionTime));

        // Statistiques
        sb.append(String.format(Locale.US, "\"meanPayoff\": %.4f,", res.meanPayoff));
        sb.append(String.format(Locale.US, "\"stdPayoff\": %.4f,", res.stdPayoff));
        sb.append(String.format(Locale.US, "\"variance\": %.4f,", res.variance));
        sb.append(String.format(Locale.US, "\"ic95\": %.4f,", res.ic95));

        // Histogrammes (List<Double>)
        sb.append("\"sampleST\": ").append(listToJson(res.sampleST)).append(",");
        sb.append("\"samplePayoffs\": ").append(listToJson(res.samplePayoffs)).append(",");

        // Trajectoires (List<List<Double>>)
        sb.append("\"trajectories\": [");
        for (int i = 0; i < res.trajectories.size(); i++) {
            sb.append(listToJson(res.trajectories.get(i)));
            if (i < res.trajectories.size() - 1) sb.append(",");
        }
        sb.append("],");

        // Convergence : liste d'objets { "n": ..., "estimate": ... }
        sb.append("\"convergence\": [");
        for (int i = 0; i < res.convergence.size(); i++) {
            SimulationResult.ConvergencePoint p = res.convergence.get(i);

            sb.append(String.format(Locale.US,
                    "{\"n\": %d, \"estimate\": %.4f}",
                    p.n, p.estimate
            ));

            if (i < res.convergence.size() - 1) sb.append(",");
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    /**
     * Génère une réponse JSON d'erreur standardisée.
     * @param message description de l'erreur
     * @return JSON au format : {"status":"ERROR","message":"..."}
     */
    public static String error(String message) {
        // Remplacement simple pour éviter de casser le JSON avec des guillemets
        return String.format(
                "{\"status\": \"ERROR\", \"message\": \"%s\"}",
                message.replace("\"", "'")
        );
    }

    /**
     * Convertit une liste de doubles en tableau JSON :
     * Exemple : [1.23, 4.56, 7.89]
     *
     * @param list liste de Double
     * @return chaîne JSON correspondant au tableau
     */
    private static String listToJson(List<Double> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(String.format(Locale.US, "%.2f", list.get(i)));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
