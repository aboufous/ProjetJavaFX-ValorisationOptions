package appli.export;

import appli.net.PricingResponse;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Utilitaire d'export au format CSV pour les résultats de pricing.
 *
 * Cette classe prend :
 *  - un ensemble de résultats (par méthode de calcul),
 *  - les paramètres utilisés (sous forme JSON),
 *  - un nom de fichier,
 * et génère un fichier CSV lisible (exploitable dans Excel, LibreOffice, etc.).
 *
 * Séparateur utilisé : ';' (convention française pour les CSV).
 */
public class ExportCSV {

    // ========================
    //  EXPORT CSV PRINCIPAL
    // ========================

    /**
     * Génère un fichier CSV à partir d'un ensemble de résultats.
     *
     * Chaque ligne correspond à une méthode (Black-Scholes, MC-Seq, MC-Par, ...).
     *
     * Colonnes :
     *   - Méthode
     *   - Call
     *   - Put
     *   - Temps(ms)
     *   - Variance
     *   - Paramètres JSON (sanitisés)
     *
     * @param results    map : nom de la méthode → PricingResponse
     * @param paramsJson map : nom de la méthode → chaîne JSON des paramètres
     * @param filename   nom du fichier CSV à créer (ex : "export_001.csv")
     */
    public static void generate(Map<String, PricingResponse> results,
                                Map<String, String> paramsJson,
                                String filename) {
        try {
            // Ouvre le fichier en écriture (écrase s'il existe déjà)
            PrintWriter pw = new PrintWriter(new FileWriter(filename));

            // Ligne d'en-tête
            pw.println("Méthode;Call;Put;Temps(ms);Variance;Paramètres JSON");

            // Une ligne par méthode de pricing
            for (var entry : results.entrySet()) {
                String method = entry.getKey();
                PricingResponse r = entry.getValue();

                // On remplace les ';' dans le JSON pour ne pas casser le CSV
                String jsonParams = paramsJson.get(method);
                if (jsonParams == null) jsonParams = "";
                jsonParams = jsonParams.replace(";", ",");

                pw.printf("%s;%.4f;%.4f;%d;%.6f;%s%n",
                        method,
                        r.call,
                        r.put,
                        r.timeMs,
                        r.variance,
                        jsonParams
                );
            }

            pw.close();
            System.out.println("CSV exporté : " + filename);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========================================
    //  EXPORT AVEC NOM SANS EXTENSION
    // ========================================

    /**
     * Génère un CSV en ajoutant automatiquement l'extension ".csv"
     * au nom de base fourni.
     *
     * Exemple :
     *   baseName = "resultats_001"
     *   → fichier "resultats_001.csv"
     *
     * @param baseName nom de base (sans extension)
     * @param results  map résultats
     * @param params   map paramètres JSON
     */
    public static void generateWithName(String baseName,
                                        Map<String, PricingResponse> results,
                                        Map<String, String> params) {

        String filename = baseName + ".csv";
        generate(results, params, filename);
    }
}
