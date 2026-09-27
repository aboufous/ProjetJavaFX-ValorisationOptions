package appli.net;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe représentant une requête de pricing envoyée au serveur.
 *
 * Cette classe est utilisée côté client (JavaFX) pour construire un message JSON
 * conforme au protocole défini entre le client et le serveur :
 *
 * {
 *   "method": "Black-Scholes" | "MC-Seq" | "MC-Par",
 *   "params": {
 *      "spot":        double,
 *      "strike":      double,
 *      "maturity":    double,
 *      "rate":        double,
 *      "volatility":  double,
 *      "simulations": long
 *   }
 * }
 *
 * - "method" indique au serveur quelle méthode de valorisation exécuter.
 * - "params" contient tous les paramètres numériques nécessaires au calcul.
 *
 * Le serveur utilise ensuite PricingRequest.fromJson(...) pour reconstruire
 * l'objet côté backend.
 */
public class PricingRequest {

    /** Nom de la méthode à appeler côté serveur (ex : "MC-Seq"). */
    private final String method;

    /** Paramètres dynamiques : spot, strike, maturity, rate, volatility, simulations. */
    private final Map<String, Object> params = new HashMap<>();


    /**
     * Constructeur.
     *
     * @param method méthode demandée (Black-Scholes, MC-Seq, MC-Par)
     */
    public PricingRequest(String method) {
        this.method = method;
    }


    /**
     * Ajoute un paramètre à la requête.
     *
     * @param key   Nom du paramètre (spot, strike, volatility, etc.)
     * @param value Valeur associée (double, long, String...)
     *
     * Les valeurs numériques sont stockées telles quelles.
     * Les valeurs textuelles seront encadrées de guillemets dans le JSON final.
     */
    public void set(String key, Object value) {
        params.put(key, value);
    }


    /**
     * Génère le JSON complet à envoyer au serveur.
     *
     * Construit manuellement un objet JSON au format :
     *
     * {
     *   "method":"MC-Seq",
     *   "params":{
     *       "spot":100.0,
     *       "strike":100.0,
     *       "maturity":1.0,
     *       "rate":0.05,
     *       "volatility":0.2,
     *       "simulations":500000
     *   }
     * }
     *
     * @return chaîne JSON conforme au protocole client/serveur.
     */
    public String toJson() {

        StringBuilder sb = new StringBuilder();

        sb.append("{");

        // Champ "method"
        sb.append("\"method\":\"").append(method).append("\",");

        // Bloc "params"
        sb.append("\"params\":{");

        int i = 0;
        for (var entry : params.entrySet()) {

            // "clé": valeur
            sb.append("\"").append(entry.getKey()).append("\":");

            Object v = entry.getValue();

            // Si numérique → sans guillemets
            if (v instanceof Number) {
                sb.append(v);
            }
            // Sinon → guillemets
            else {
                sb.append("\"").append(v).append("\"");
            }

            // Virgule sauf pour le dernier paramètre
            if (i < params.size() - 1) sb.append(",");
            i++;
        }

        sb.append("}}"); // ferme "params" puis l'objet global

        return sb.toString();
    }
}
