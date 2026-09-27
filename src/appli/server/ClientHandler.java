package appli.server;

import appli.engine.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * Gestionnaire de client (Thread dédié).
 *
 * Cette classe est instanciée pour chaque connexion entrante.
 * Son rôle est de gérer le cycle de vie COMPLET d'une requête :
 *
 *   1) Lecture du JSON envoyé par le client
 *   2) Parsing → conversion en PricingRequest
 *   3) Routage vers le moteur correspondant :
 *        - Black-Scholes (formule analytique)
 *        - Monte Carlo Séquentiel
 *        - Monte Carlo Parallèle (via ExecutorService)
 *   4) Production du SimulationResult
 *   5) Sérialisation en JSON (PricingResponse.success)
 *   6) Envoi au client
 *
 * Cette classe ne contient AUCUNE logique mathématique.
 * Elle est purement dédiée à la communication réseau.
 *
 */
public class ClientHandler implements Runnable {

    private final Socket socket;                   // Connexion avec ce client
    private final ExecutorService calculatorPool;  // Pool de threads pour MC parallèle

    public ClientHandler(Socket socket, ExecutorService calculatorPool) {
        this.socket = socket;
        this.calculatorPool = calculatorPool;
    }

    @Override
    public void run() {
        try (
                // Flux d’entrée : lecture du JSON envoyé par le client
                BufferedReader in =
                        new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Flux de sortie : envoi du JSON de réponse
                PrintWriter out =
                        new PrintWriter(socket.getOutputStream(), true)
        ) {
            // ==============================
            // 1) Lecture BLOQUANTE du JSON
            // ==============================
            String jsonLine = in.readLine();
            if (jsonLine == null) return;  // Client déconnecté proprement

            System.out.println("Reçu : " + jsonLine);

            try {
                // ==============================
                // 2) Parsing JSON → PricingRequest
                // ==============================
                PricingRequest req = PricingRequest.fromJson(jsonLine);
                String method = req.method;

                System.out.println("Méthode reçue = " + method);

                SimulationResult result;

                // ==============================
                // 3) ROUTAGE vers le bon pricer
                // ==============================
                if (method.equals("Black-Scholes")) {

                    System.out.println("  → Black-Scholes");

                    BlackScholesPricer bs =
                            new BlackScholesPricer(
                                    req.spot, req.strike, req.maturity,
                                    req.rate, req.volatility
                            );

                    // Construction manuelle du résultat analytique
                    result = new SimulationResult();
                    result.callPrice = bs.priceCall();
                    result.putPrice  = bs.pricePut();

                    // Pas de statistiques pour Black-Scholes (formule exacte)
                    result.meanPayoff = result.callPrice;
                    result.stdPayoff = 0.0;
                    result.variance  = 0.0;
                    result.ic95      = 0.0;

                    result.sampleST      = java.util.Collections.emptyList();
                    result.samplePayoffs = java.util.Collections.emptyList();
                    result.trajectories  = java.util.Collections.emptyList();
                    result.convergence   = java.util.Collections.emptyList();
                }

                else if (method.equals("MC-Seq")) {

                    System.out.println("  → Monte Carlo Séquentiel");

                    MonteCarloPricer pricer =
                            new MonteCarloPricer(
                                    req.spot, req.strike, req.maturity,
                                    req.rate, req.volatility, req.simulations
                            );

                    // Le temps est mesuré DIRECTEMENT dans priceSequential()
                    result = pricer.priceSequential();
                }

                else if (method.equals("MC-Par")) {

                    System.out.println("  → Monte Carlo Parallèle");

                    MonteCarloPricer pricer =
                            new MonteCarloPricer(
                                    req.spot, req.strike, req.maturity,
                                    req.rate, req.volatility, req.simulations
                            );

                    // Le temps est mesuré DANS priceParallel()
                    result = pricer.priceParallel(calculatorPool);
                }

                else {
                    throw new RuntimeException("Méthode inconnue : " + method);
                }

                // ==============================
                // 4) Sérialisation JSON + Envoi
                // ==============================
                String jsonResponse = PricingResponse.success(result);
                out.println(jsonResponse);

                System.out.println("Réponse envoyée (" + result.executionTime + " ms)");

            } catch (Exception e) {

                // ==============================
                // ERREURS LOGIQUES
                // ==============================
                // Exemple : mauvaise méthode, paramètres invalides...
                out.println(PricingResponse.error(e.getMessage()));
                e.printStackTrace();
            }

        } catch (Exception e) {

            // ==============================
            // ERREURS RÉSEAU (déconnexions, IO)
            // ==============================
            e.printStackTrace();

        } finally {
            // Toujours fermer le socket
            try { socket.close(); } catch (Exception ignored) {}
        }
    }
}
