package appli.export;

import appli.net.PricingResponse;
import javafx.scene.chart.Chart;
import java.util.Map;

/**
 * ExportPDF : Génère un rapport PDF complet contenant :
 *  - Résultats chiffrés (Call / Put / Temps / Variance)
 *  - Paramètres au format JSON
 *  - Graphiques (Payoff, trajectoires, distributions, convergence, stats)
 *
 * Le rendu final est produit via la classe PDFExporter.
 */
public class ExportPDF {

    /**
     * Génère un fichier PDF complet à partir des résultats Monte Carlo / BS.
     *
     * @param results  Map<méthode, résultats> (call, put, temps, variance…)
     * @param params   Map<méthode, paramètres JSON envoyés au serveur>
     * @param payoff   Graphique des payoffs
     * @param paths    Graphique des trajectoires GBM
     * @param distST   Histogramme de S(T)
     * @param distPay  Histogramme des payoffs
     * @param conv     Courbe de convergence
     * @param stats    Graphique regroupant erreurs / IC95%
     * @param filepath Chemin du fichier PDF final
     */
    public static void generate(
            Map<String, PricingResponse> results,
            Map<String, String> params,
            Chart payoff, Chart paths, Chart distST,
            Chart distPay, Chart conv, Chart stats,
            String filepath
    ) {

        try {
            PDFExporter pdf = new PDFExporter();

            // ---- Titre principal ----
            pdf.addTitle("Rapport de Simulation d’Options");

            // ---- Résultats numériques ----
            for (var e : results.entrySet()) {
                String method = e.getKey();
                PricingResponse r = e.getValue();

                pdf.addText("Méthode : " + method);
                pdf.addText("Call : " + r.call);
                pdf.addText("Put : " + r.put);
                pdf.addText("Temps(ms) : " + r.timeMs);
                pdf.addText("Variance : " + r.variance);

                // Paramètres JSON (avec retour à la ligne automatique)
                pdf.addWrappedText("Paramètres : " + params.get(method), 12f, 450f);

                pdf.addText(""); // Ligne vide
            }

            // ---- Graphiques ----
            pdf.addChart("Payoff", payoff);
            pdf.addChart("Trajectoires", paths);
            pdf.addChart("Distribution S(T)", distST);
            pdf.addChart("Distribution Payoff", distPay);
            pdf.addChart("Convergence", conv);
            pdf.addChart("Erreur / IC95%", stats);

            // ---- Finalisation ----
            pdf.save(filepath);
            System.out.println("PDF sauvegardé : " + filepath);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
