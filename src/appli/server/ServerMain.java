package appli.server;

import appli.net.*;
import appli.engine.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Point d'entrée principal de l'application Serveur ("pricing-engine-server").
 *
 * Rôle :
 * 1. Initialiser les ressources partagées (Pool de calcul).
 * 2. Ouvrir le port d'écoute (8080).
 * 3. Accepter les connexions entrantes et déléguer chaque client à un thread dédié.
 *
 * Architecture : Parallélisme à deux niveaux.
 * - Niveau 1 : Gestion multi-clients (clientPool).
 * - Niveau 2 : Calcul parallèle Monte Carlo (sharedCalculatorPool).
 *
 */
public class ServerMain {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Démarrage du Serveur de Valorisation (Port 8080) ===");
        
        // --- 1. Création des Ressources Partagées ---
        // Niveau 2 : Pool de Calcul (Cœurs Physiques pour éviter Hyper-Threading)
        int cores = Runtime.getRuntime().availableProcessors() / 2;
        if (cores < 1) cores = 1;
        ExecutorService sharedCalculatorPool = Executors.newFixedThreadPool(cores);
        System.out.println(">> Pool de Calcul initialisé avec " + cores + " threads.");
        
        // Niveau 1 : Pool de Gestion Clients
        ExecutorService clientPool = Executors.newFixedThreadPool(100);

        // --- 2. Phase d'Échauffement (Warm-up) ---
        // On lance un calcul "à blanc" pour forcer le JIT à optimiser le code
        // et pour initialiser les ThreadLocalRandom des threads de calcul.
        System.out.println(">> Échauffement du moteur en cours...");
        long startWarmup = System.currentTimeMillis();
        
        // Petit calcul (100k sims) pour chauffer
        MonteCarloPricer warmer = new MonteCarloPricer(100, 100, 1, 0.05, 0.2, 100_000);
        warmer.priceParallel(sharedCalculatorPool);
        
        long endWarmup = System.currentTimeMillis();
        System.out.println(">> Moteur chaud et prêt (temps d'échauffement: " + (endWarmup - startWarmup) + "ms).");
        
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println(">> Serveur prêt. En attente de clients...");
            
            // Boucle infinie d'écoute
            while (true) {
                // Bloque jusqu'à ce qu'un client se connecte
                Socket clientSocket = serverSocket.accept();
                System.out.println(">> Nouveau client connecté : " + clientSocket.getInetAddress());
                
                // On délègue la gestion de ce client au pool "Niveau 1"
                // On lui passe le pool "Niveau 2" pour qu'il puisse lancer des calculs rapides.
                clientPool.submit(new ClientHandler(clientSocket, sharedCalculatorPool));
            }
        }
    }
}
