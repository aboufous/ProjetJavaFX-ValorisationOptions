package appli.engine;

/**
 * Classe utilitaire contenant des fonctions statistiques nécessaires au pricing des options.
 *
 * NOTE : L'algorithme d'approximation du CDF (Abramowitz et Stegun) est une formule
 * académique standard. L'implémentation a été validée et optimisée en complément
 * avec l'assistance d'une IA (Gemini) pour assurer la précision.
 *
 * @author Adam Boufous, Célia Glinel
 */
public class Stats {

    // --- Constantes pour l'approximation polynomiale de la CDF ---
    // Ces valeurs proviennent de la formule 26.2.17 du manuel d'Abramowitz et Stegun.
    private static final double C_1 = 0.319381530;
    private static final double C_2 = -0.356563782;
    private static final double C_3 = 1.781477937;
    private static final double C_4 = -1.821255978;
    private static final double C_5 = 1.330274429;
    private static final double P_PARAM = 0.2316419;

    /**
     * Calcule la fonction de densité de probabilité (PDF) de la loi normale standard N(0, 1).
     * @param x La valeur à évaluer (z-score).
     * @return La valeur de la PDF.
     */
    public static double normalPdf(double x) {
        // Formule : (1 / sqrt(2*pi)) * exp(-x^2 / 2)
        return Math.exp(-0.5 * x * x) / Math.sqrt(2 * Math.PI);
    }

    /**
     * Calcule la fonction de répartition cumulée (CDF) de la loi normale standard N(0, 1).
     * fonction N(x) utilisée directement dans les formules de Black-Scholes (pour d1 et d2).
     * Utilise une approximation polynomiale de grande précision (approximation d'Abramowitz et Stegun).
     *
     * @param x La valeur pour laquelle calculer la probabilité cumulée (souvent d1 ou d2).
     * @return La probabilité cumulée P(Z <= x).
     */
    public static double normalCdf(double x) {
        if (x > 6.0) {
            // Optimisation : Pour les grandes valeurs positives, la probabilité est si proche
            // de 1.0 que nous pouvons la retourner directement pour éviter des calculs inutiles.
            return 1.0; 
        }
        if (x < -6.0) {
             // Optimisation : Pour les grandes valeurs négatives, la probabilité est
             // si proche de 0.0 que nous pouvons la retourner directement.
            return 0.0;
        }

        // Calcule le terme d'erreur 'd' basé sur l'approximation polynomiale
        double t = 1.0 / (1.0 + P_PARAM * Math.abs(x));
        double t_powers = t * (C_1 + t * (C_2 + t * (C_3 + t * (C_4 + t * C_5))));
        double d = normalPdf(x) * t_powers; // d = PDF(x) * (approximation polynomiale)

        if (x >= 0.0) {
            // Pour x positif, N(x) = 1 - d
            return 1.0 - d;
        } else {
            // Pour x négatif, N(x) = d (en utilisant la symétrie de la courbe normale)
            return d;
        }
    }
}