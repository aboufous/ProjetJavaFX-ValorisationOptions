package appli.server;

/**
 * Représente la requête de calcul envoyée par le client.
 *
 * Cette classe agit comme un DTO ("Data Transfer Object") côté serveur.
 * Elle contient tous les paramètres nécessaires au pricing :
 *  - method      : "Black-Scholes" | "MC-Seq" | "MC-Par"
 *  - spot        : prix initial S
 *  - strike      : prix d'exercice K
 *  - maturity    : maturité T en années
 *  - rate        : taux sans risque r
 *  - volatility  : volatilité sigma
 *  - simulations : nombre de trajectoires (si Monte Carlo)
 *
 * Le serveur reçoit un JSON au format exact :
 *
 * {
 *   "method": "MC-Seq",
 *   "params": {
 *      "spot": 100,
 *      "strike": 100,
 *      "maturity": 1.0,
 *      "rate": 0.05,
 *      "volatility": 0.2,
 *      "simulations": 500000
 *   }
 * }
 *
 * Pour respecter la contrainte du projet (pas de dépendance externe
 * de type Gson / Jackson), le parsing JSON est réalisé "manuellement"
 * via indexOf, substring, etc.
 */
public class PricingRequest {

    // Champs directement utilisés par le moteur côté serveur.
    public String method;
    public double spot;
    public double strike;
    public double maturity;
    public double rate;
    public double volatility;
    public long simulations;

    /**
     * Convertit une chaîne JSON brute envoyée par le client en un objet PricingRequest.
     * On utilise un parseur minimaliste basé sur des opérations sur chaînes :
     *  indexOf(), substring()…
     *
     * @param json chaîne JSON complète envoyée par ClientSocket
     * @return un objet PricingRequest rempli
     */
    public static PricingRequest fromJson(String json) {

        PricingRequest req = new PricingRequest();

        // Nettoyage basique (supprime les retours à la ligne, espaces inutiles)
        String clean = json.replace("\n", "").trim();

        // ==========================================================
        // 1) Extraction du champ "method"
        // ==========================================================
        req.method = extractString(clean, "method");

        // ==========================================================
        // 2) Extraction du bloc "params": { ... }
        // ==========================================================
        int pStart = clean.indexOf("\"params\"");
        if (pStart == -1) return req;  // JSON invalide → retourne req vide

        int braceStart = clean.indexOf("{", pStart);
        int braceEnd   = clean.indexOf("}", braceStart);

        if (braceStart == -1 || braceEnd == -1) return req;

        // Sous-chaîne interne contenant uniquement les paramètres
        String params = clean.substring(braceStart + 1, braceEnd);

        // ==========================================================
        // 3) Lecture des champs numériques dans le bloc params
        // ==========================================================
        req.spot        = extractDouble(params, "spot");
        req.strike      = extractDouble(params, "strike");
        req.maturity    = extractDouble(params, "maturity");
        req.rate        = extractDouble(params, "rate");
        req.volatility  = extractDouble(params, "volatility");
        req.simulations = (long) extractDouble(params, "simulations");

        return req;
    }

    /**
     * Extrait un double à partir d'un JSON rudimentaire.
     * Exemple :
     *   src = "spot:100,strike:120"
     *   key = "spot"
     *   → retourne 100.0
     *
     * @param src chaîne dans laquelle chercher
     * @param key clé recherchée
     * @return double extrait ou 0 si absent
     */
    private static double extractDouble(String src, String key) {
        try {
            int k = src.indexOf("\"" + key + "\"");
            if (k == -1) return 0;

            int colon = src.indexOf(":", k);
            int comma = src.indexOf(",", colon);

            if (comma == -1) comma = src.length();

            String raw = src.substring(colon + 1, comma).trim();
            return Double.parseDouble(raw);

        } catch (Exception e) {
            return 0; // Valeur de secours si mal formé
        }
    }

    /**
     * Extrait une chaîne du JSON sans guillemets.
     *
     * Exemple :
     *    src = {"method":"MC-Seq"}
     *    key = "method"
     *    → retourne "MC-Seq"
     */
    private static String extractString(String src, String key) {
        try {
            int k = src.indexOf("\"" + key + "\"");
            if (k == -1) return "";

            int colon = src.indexOf(":", k);
            int comma = src.indexOf(",", colon);

            if (comma == -1) comma = src.length();

            String raw = src.substring(colon + 1, comma).trim();
            return raw.replace("\"", "");

        } catch (Exception e) {
            return ""; // Valeur de secours
        }
    }
}
