package appli.engine;


/**
 * Implémentation du modèle de Black-Scholes pour la valorisation d'options européennes.
 * Ce modèle fournit un prix théorique exact sous certaines hypothèses (rendements log-normaux,
 * taux et volatilité constants, pas de coûts de transaction, etc.).
 *
 * NOTE : Les formules (d1, d2, price) sont des implémentations standards du modèle.
 *
 */
public class BlackScholesPricer {

    // --- Paramètres du Modèle (Les inputs reçus du client) ---
    private final double S;  // Spot Price (S): Prix actuel de l'actif sous-jacent.
    private final double K;  // Strike Price (K): Prix d'exercice de l'option.
    private final double T;  // Maturity (T): Temps restant jusqu'à l'échéance (en années).
    private final double r;  // Risk-Free Rate (r): Taux d'intérêt sans risque (annuel).
    private final double sigma; // Volatility (sigma): Volatilité implicite de l'actif (écart-type annuel).

    /**
     * Constructeur pour initialiser l'instance de valorisation avec les paramètres de marché.
     */
    public BlackScholesPricer(double spot, double strike, double maturity, double rate, double volatility) {
        this.S = spot;
        this.K = strike;
        this.T = maturity;
        this.r = rate;
        this.sigma = volatility;
    }

    /**
     * Calcule le d1 de la formule de Black-Scholes.
     * d1 est la mesure standardisée de la probabilité que l'option soit exercée, ajustée
     * pour le taux sans risque et la volatilité.
     */
    private double calculateD1() {
        // Gestion des cas limites pour éviter NaN (si l'échéance est nulle ou la volatilité nulle)
        if (T <= 0 || sigma <= 0) return 0; 
        
        // Numérateur: ln(S/K) + (r + sigma^2 / 2) * T
        double numerator = Math.log(S / K) + (r + 0.5 * sigma * sigma) * T;
        
        // Dénominateur: sigma * sqrt(T)
        double denominator = sigma * Math.sqrt(T);
        
        return numerator / denominator;
    }

    /**
     * Calcule le d2 de la formule de Black-Scholes.
     * d2 est lié à la probabilité neutre au risque que l'option expire dans la monnaie.
     */
    private double calculateD2(double d1) {
        // d2 = d1 - sigma * sqrt(T)
        return d1 - sigma * Math.sqrt(T);
    }

    /**
     * Valorise une option CALL européenne.
     * Formule: C = S * N(d1) - K * exp(-rT) * N(d2)
     *
     * @return Le prix de l'option Call.
     */
    public double priceCall() {
        // 1. Calculer d1 et d2
        double d1 = calculateD1();
        double d2 = calculateD2(d1);

        // 2. Utiliser la CDF de la loi normale standard (N(d))
        // Appelle de la classe Stats.java
        double Nd1 = Stats.normalCdf(d1);
        double Nd2 = Stats.normalCdf(d2);

        // 3. Appliquer la formule BS
        // Terme S * N(d1) = Valeur actuelle attendue de la réception de l'actif.
        // Terme K * exp(-r * T) * N(d2) = Valeur actuelle attendue du paiement du strike.
        double callPrice = S * Nd1 - K * Math.exp(-r * T) * Nd2;

        return callPrice;
    }

    /**
     * Valorise une option PUT européenne.
     * Formule (par parité Call-Put): P = K * exp(-rT) * N(-d2) - S * N(-d1)
     *
     * @return Le prix de l'option Put.
     */
    public double pricePut() {
        // 1. Calculer d1 et d2
        double d1 = calculateD1();
        double d2 = calculateD2(d1);

        // 2. Utiliser la CDF de la loi normale standard pour les valeurs négatives
        double N_d1 = Stats.normalCdf(-d1);
        double N_d2 = Stats.normalCdf(-d2);

        // 3. Appliquer la formule BS pour le Put
        double putPrice = K * Math.exp(-r * T) * N_d2 - S * N_d1;

        return putPrice;
    }
}