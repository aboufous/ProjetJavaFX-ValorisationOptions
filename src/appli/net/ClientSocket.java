package appli.net;

import java.io.*;
import java.net.Socket;

/**
 * Client léger pour communiquer avec le serveur de calcul via TCP.
 *
 * Rôle :
 * -------
 *  - ouvrir une connexion socket (host, port),
 *  - envoyer une requête encodée en JSON (une ligne),
 *  - lire la réponse JSON (une ligne),
 *  - proposer une API synchrone (send) et asynchrone (sendAsync).
 *
 * Utilisation :
 *  - La méthode send(...) est bloquante : elle ouvre la socket, envoie la requête,
 *    attend la réponse, puis ferme la connexion.
 *  - La méthode sendAsync(...) exécute le même envoi dans un thread séparé afin
 *    de ne pas bloquer le thread JavaFX.
 */
public class ClientSocket {

    private final String host; // Adresse du serveur (ex: "localhost")
    private final int port;    // Port du serveur (ex: 5000)

    /**
     * Construit un client configuré pour un serveur donné.
     *
     * @param host adresse du serveur (IP ou nom de domaine)
     * @param port port TCP sur lequel écoute le serveur
     */
    public ClientSocket(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Envoie une requête JSON au serveur de manière SYNCHRONE.
     *
     * Étapes :
     *  1) ouverture d'une socket TCP (host, port)
     *  2) envoi de la chaîne JSON suivie d'un saut de ligne
     *  3) lecture d'une ligne en réponse (JSON)
     *  4) fermeture de la socket
     *
     * @param json chaîne JSON représentant la requête (une seule ligne)
     * @return la réponse JSON reçue du serveur (une seule ligne)
     * @throws Exception en cas d'erreur réseau (connexion impossible, I/O, etc.)
     */
    public String send(String json) throws Exception {
        // Ouverture de la connexion TCP
        Socket socket = new Socket(host, port);

        // Flux d'entrée : pour lire la réponse (ligne de JSON)
        BufferedReader in =
                new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Flux de sortie : pour envoyer la requête JSON
        // autoFlush = true → println() envoie immédiatement la ligne
        PrintWriter out =
                new PrintWriter(socket.getOutputStream(), true);

        // Envoi de la requête JSON (protocole : 1 requête = 1 ligne)
        out.println(json);

        // Lecture de la réponse JSON (protocole : 1 réponse = 1 ligne)
        String response = in.readLine();

        // Fermeture de la connexion (simple, pas de réutilisation de socket)
        socket.close();

        return response;
    }

    /**
     * Envoie une requête JSON au serveur de manière ASYNCHRONE.
     *
     * Un nouveau thread est créé pour :
     *  - appeler send(json) (opération bloquante),
     *  - transmettre la réponse au callback lorsqu'elle est reçue.
     *
     * En cas d'erreur, le callback est appelé avec null.
     *
     * @param json      requête JSON à envoyer
     * @param callback  fonction appelée avec la chaîne réponse (ou null si erreur)
     */
    public void sendAsync(String json, java.util.function.Consumer<String> callback) {
        new Thread(() -> {
            try {
                String resp = send(json);
                callback.accept(resp);
            }
            catch (Exception e) {
                // En cas d'erreur réseau, on signale l'échec avec null
                callback.accept(null);
            }
        }).start();
    }
}
