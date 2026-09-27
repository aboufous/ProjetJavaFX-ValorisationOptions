package appli.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Implémentation du moteur de valorisation d'options par Monte Carlo.
 *
 * Cette classe supporte deux modes :
 *  1. Séquentiel (single-thread)
 *  2. Parallèle (multi-thread via ExecutorService)
 *
 * Le modèle sous-jacent utilisé est le GBM (Geometric Brownian Motion) :
 *      S(T) = S(0) * exp( (r - 0.5*sigma²)*T + sigma*sqrt(T)*Z )
 * avec Z ~ N(0,1).
 *
 * Structure :
 *  - Map : exécution de SimulationTask sur plusieurs threads
 *  - Reduce : agrégation des sommes et variances
 *  - Un seul thread collecte les graphiques afin d’éviter les duplications mémoire
 *
 */
public class MonteCarloPricer {

    private final double S, K, T, r, sigma;
    private final long numSimulations;

    public MonteCarloPricer(double s, double k, double t, double r, double sigma, long simulations) {
        this.S = s;
        this.K = k;
        this.T = t;
        this.r = r;
        this.sigma = sigma;
        this.numSimulations = simulations;
    }

    // ========================================================================
    //  MODE SÉQUENTIEL
    // ========================================================================
    public SimulationResult priceSequential() {
        long start = System.currentTimeMillis();   // <-- AJOUT

        try {
            SimulationTask task = new SimulationTask(S, K, T, r, sigma, numSimulations, true);
            SimulationResult partial = task.call();

            SimulationResult res = finalizeFromRawSums(
                    partial.sumCall,
                    partial.sumCallSq,
                    partial.sumPut,
                    partial.sumPutSq,
                    partial
            );

            res.executionTime = System.currentTimeMillis() - start;  // <-- AJOUT
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new SimulationResult();
        }
    }


    // ========================================================================
    //  MODE PARALLÈLE
    // ========================================================================
    public SimulationResult priceParallel(ExecutorService executor) throws Exception {

        long start = System.currentTimeMillis();   // <-- AJOUT

        int numThreads = Runtime.getRuntime().availableProcessors() / 2;
        if (numThreads < 1) numThreads = 1;

        long simsPerThread = numSimulations / numThreads;
        long remainder = numSimulations % numThreads;

        List<Callable<SimulationResult>> tasks = new ArrayList<>();

        // Création des tâches
        for (int i = 0; i < numThreads; i++) {
            long simsForThisThread = simsPerThread + (i < remainder ? 1 : 0);
            boolean collectStats = (i == 0);  // Un seul thread collecte les graphiques
            tasks.add(new SimulationTask(S, K, T, r, sigma, simsForThisThread, collectStats));
        }

        List<Future<SimulationResult>> futures = executor.invokeAll(tasks);

        double totalCallSum = 0.0, totalCallSq = 0.0;
        double totalPutSum  = 0.0, totalPutSq  = 0.0;
        SimulationResult template = null;

        // Agrégation (reduce)
        for (Future<SimulationResult> f : futures) {
            SimulationResult partial = f.get();

            totalCallSum += partial.sumCall;
            totalCallSq  += partial.sumCallSq;

            totalPutSum  += partial.sumPut;
            totalPutSq   += partial.sumPutSq;

            if (template == null || !partial.sampleST.isEmpty()) {
                template = partial;
            }
        }

        SimulationResult res = finalizeFromRawSums(
                totalCallSum,
                totalCallSq,
                totalPutSum,
                totalPutSq,
                template
        );

        res.executionTime = System.currentTimeMillis() - start;  // <-- AJOUT
        return res;
    }


    // ========================================================================
    //  FONCTION D’AGRÉGATION COMMUNE
    // ========================================================================
    private SimulationResult finalizeFromRawSums(
            double totalCallSum, double totalCallSq,
            double totalPutSum,  double totalPutSq,
            SimulationResult template) {

        SimulationResult res = new SimulationResult();

        // Copier les données graphiques depuis le thread collecteur
        if (template != null) {
            res.sampleST      = template.sampleST;
            res.samplePayoffs = template.samplePayoffs;
            res.trajectories  = template.trajectories;
            res.convergence   = template.convergence;
        }

        double n = (double) numSimulations;

        // CALL
        double meanCall = totalCallSum / n;
        double varCall  = (totalCallSq / n) - meanCall * meanCall;
        if (varCall < 0) varCall = 0;
        double stdCall = Math.sqrt(varCall);

        // PUT
        double meanPut = totalPutSum / n;
        double varPut  = (totalPutSq / n) - meanPut * meanPut;
        if (varPut < 0) varPut = 0;
        double stdPut = Math.sqrt(varPut);

        double discount = Math.exp(-r * T);

        res.callPrice = meanCall * discount;
        res.putPrice  = meanPut  * discount;

        // Statistiques (call)
        res.meanPayoff = meanCall;
        res.variance   = varCall;
        res.stdPayoff  = stdCall;
        res.ic95       = 1.96 * (stdCall / Math.sqrt(n));

        return res;
    }
}
