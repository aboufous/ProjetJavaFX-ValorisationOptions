package appli.engine;

import java.util.concurrent.Callable;
import java.util.SplittableRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente une unité de calcul (Worker) utilisée dans une simulation Monte Carlo.
 *
 * Cette classe implémente Callable afin de pouvoir être exécutée en parallèle
 * dans un pool de threads via ExecutorService.
 *
 * Chaque SimulationTask :
 *  - réalise un sous-ensemble du total des simulations (ex : 250 000 sur 1 million)
 *  - simule l'évolution de l'actif sous dynamique GBM (Geometric Brownian Motion)
 *  - calcule les payoffs Call et Put associés
 *  - accumule les sommes nécessaires au calcul global (sum, sumSq)
 *  - optionnellement, collecte des statistiques lourdes (trajectoires, histogrammes, convergence)
 *
 * Cette approche permet un schéma Map/Reduce :
 *  → MAP : plusieurs SimulationTask en parallèle
 *  → REDUCE : agrégation dans MonteCarloPricer
 *
 * NOTE :
 *  Utilisation de SplittableRandom pour :
 *     - éviter toute contention entre threads
 *     - obtenir une génération aléatoire extrêmement rapide
 *
 */
public class SimulationTask implements Callable<SimulationResult> {

    // Paramètres financiers immuables
    private final double S, K, T, r, sigma;

    // Nombre de simulations pour CE worker
    private final long numSimulations;

    // true = collecte des statistiques lourdes (graphes, convergence)
    // Ce flag est mis à true POUR UN SEUL THREAD dans MonteCarloPricer.
    private final boolean collectDetailedStats;

    public SimulationTask(double s, double k, double t, double r, double sigma,
                          long numSimulations, boolean collectDetailedStats) {
        this.S = s;
        this.K = k;
        this.T = t;
        this.r = r;
        this.sigma = sigma;
        this.numSimulations = numSimulations;
        this.collectDetailedStats = collectDetailedStats;
    }

    @Override
    public SimulationResult call() {

        SimulationResult res = new SimulationResult();

        // ---------------- RNG PAR THREAD ----------------
        // SplittableRandom = extrêmement rapide + indépendant par thread
        SplittableRandom rng = new SplittableRandom(System.nanoTime() + Thread.currentThread().getId());

        // ---------------- ACCUMULATEURS ------------------
        double sumCall   = 0.0;
        double sumCallSq = 0.0;

        double sumPut    = 0.0;
        double sumPutSq  = 0.0;

        // ---------------- GBM TERMINAL -------------------
        double drift = (r - 0.5 * sigma * sigma) * T;
        double vol   = sigma * Math.sqrt(T);

        // ---------------- GBM TRAJECTOIRES (dt = 1 jour) ---------------
        double dt        = T / 365.0;
        double driftStep = (r - 0.5 * sigma * sigma) * dt;
        double volStep   = sigma * Math.sqrt(dt);

        // ======================================================
        //                   BOUCLE PRINCIPALE
        // ======================================================
        for (long i = 0; i < numSimulations; i++) {

            // Tirage normal N(0,1) via Box-Muller
            double u1 = rng.nextDouble();
            double u2 = rng.nextDouble();
            double z = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2 * Math.PI * u2);

            // Prix terminal
            double st = S * Math.exp(drift + vol * z);

            // Payoffs
            double payoffCall = Math.max(st - K, 0.0);
            double payoffPut  = Math.max(K - st, 0.0);

            // Accumulation brute (Map)
            sumCall   += payoffCall;
            sumCallSq += payoffCall * payoffCall;

            sumPut    += payoffPut;
            sumPutSq  += payoffPut * payoffPut;

            // ======================================================
            //           COLLECTE DES STATISTIQUES LOURDES
            //        (uniquement pour le thread collecteur)
            // ======================================================
            if (collectDetailedStats) {

                // 1) Histogrammes S(T) + payoff
                if (i < 1000) {
                    res.sampleST.add(st);
                    res.samplePayoffs.add(payoffCall);
                }

                // 2) Trajectoires (20 max)
                if (i < 20) {
                    List<Double> path = new ArrayList<>();
                    path.add(S);

                    double currentS = S;

                    for (int d = 0; d < 365; d++) {

                        double uu1 = rng.nextDouble();
                        double uu2 = rng.nextDouble();
                        double zs = Math.sqrt(-2.0 * Math.log(uu1)) * Math.sin(2 * Math.PI * uu2);

                        currentS *= Math.exp(driftStep + volStep * zs);
                        path.add(currentS);
                    }

                    res.trajectories.add(path);
                }

                // 3) Convergence (tous les 1000)
                if ((i + 1) % 1000 == 0) {
                    double tempMean  = sumCall / (i + 1);
                    double tempPrice = tempMean * Math.exp(-r * T);

                    res.convergence.add(new SimulationResult.ConvergencePoint(i + 1, tempPrice));
                }
            }
        }

        // ======================================================
        //              TRANSFERT DES SOMMES BRUTES
        // ======================================================
        res.sumCall   = sumCall;
        res.sumCallSq = sumCallSq;

        res.sumPut    = sumPut;
        res.sumPutSq  = sumPutSq;

        return res;
    }
}
