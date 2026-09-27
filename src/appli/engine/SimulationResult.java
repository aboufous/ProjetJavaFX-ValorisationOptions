package appli.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Conteneur de données pour stocker les résultats d'une simulation Monte Carlo.
 *
 * Cette classe sert de "valise" pour transporter l'ensemble des informations
 * générées par une SimulationTask, puis agrégées dans MonteCarloPricer.
 *
 * Elle regroupe :
 *
 *  1) Les SOMMES BRUTES Monte Carlo (Σ payoff et Σ payoff², Call et Put)
 *     → utilisées pour l’agrégation statistique (mean, variance) dans MonteCarloPricer.
 *
 *  2) Les résultats financiers finaux :
 *        - prix Call MC
 *        - prix Put MC
 *        - temps d’exécution
 *
 *  3) Les statistiques globales :
 *        - moyenne du payoff
 *        - variance
 *        - écart-type
 *        - intervalle de confiance IC95%
 *
 *  4) Les données graphiques (remplies uniquement par UN worker) :
 *        - échantillons de S_T (distribution finale)
 *        - échantillons de payoffs
 *        - 20 trajectoires complètes
 *        - courbe de convergence
 *
 * Rôle :
 * -------
 *   Cette classe constitue le format d’échange standard :
 *      Moteur Monte Carlo  →  Serveur  →  Client JavaFX (via JSON).
 *
 */
public class SimulationResult {

    // -------------------------------------------------------------
    // SOMMES MONTE CARLO (utilisées pour l’agrégation parallèle)
    // -------------------------------------------------------------
    public double sumCall;      // Σ payoff Call
    public double sumCallSq;    // Σ payoff Call²

    public double sumPut;       // Σ payoff Put
    public double sumPutSq;     // Σ payoff Put²

    // -------------------------------------------------------------
    // Résultats financiers (calculés DANS MonteCarloPricer)
    // -------------------------------------------------------------
    public double callPrice;    // Prix MC final du Call
    public double putPrice;     // Prix MC final du Put

    public long executionTime;  // Temps de calcul total (ms)

    // -------------------------------------------------------------
    // Statistiques globales (calculées dans MonteCarloPricer)
    // -------------------------------------------------------------
    public double meanPayoff;   // E[X]
    public double stdPayoff;    // sqrt(Var[X])
    public double variance;     // Var[X]
    public double ic95;         // IC95 = 1.96 * std / sqrt(N)

    // -------------------------------------------------------------
    // Données graphiques (remplies uniquement si collectDetailedStats = true)
    // -------------------------------------------------------------

    // 1. Distribution de S(T)
    public List<Double> sampleST = new ArrayList<>();

    // 2. Distribution des payoffs CALL
    public List<Double> samplePayoffs = new ArrayList<>();

    // 3. 20 trajectoires complètes S(t)
    public List<List<Double>> trajectories = new ArrayList<>();

    // 4. Courbe de convergence : (n simulations, prix estimé)
    public List<ConvergencePoint> convergence = new ArrayList<>();


    /**
     * Représente un point de la courbe de convergence :
     *   - n : nombre de simulations effectuées
     *   - estimate : prix estimé à ce stade
     */
    public static class ConvergencePoint {
        public long n;
        public double estimate;

        public ConvergencePoint(long n, double estimate) {
            this.n = n;
            this.estimate = estimate;
        }
    }
}
