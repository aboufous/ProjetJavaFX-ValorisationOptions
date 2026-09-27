package appli.history;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gère la persistance de l’historique des simulations dans une base
 * SQLite locale ("history.db").
 *
 * Ce module est volontairement minimaliste :
 * - une seule table "simulations_grouped"
 * - chaque ligne contient un JSON complet représentant un HistoryItem
 *
 * Les inserts et lectures sont donc extrêmement simples :
 * - save()  → convertit l'objet HistoryItem en JSON
 * - loadAll() → reconstruit les HistoryItem via HistoryItem.fromJson()
 *
 * Aucun schéma complexe : tout est sérialisé en JSON pour flexibilité maximale.
 */
public class HistoryDB {

    /** URL JDBC vers la base SQLite locale. */
    private static final String DB_URL = "jdbc:sqlite:history.db";

    /**
     * Constructeur : charge le driver SQLite et crée la table si nécessaire.
     */
    public HistoryDB() {
        try {
            // Charge explicitement le driver JDBC SQLite
            Class.forName("org.sqlite.JDBC");
        }
        catch (Exception ignored) {}

        createTable();
    }

    /**
     * Crée la table unique contenant les simulations sauvegardées.
     *
     * Structure :
     *   id    (PK auto-incrément)
     *   json  (TEXT contenant HistoryItem.toJson())
     *
     * Le contenu JSON change selon les versions de l’application :
     * → Aucun problème car on sérialise tout dans un champ string.
     */
    private void createTable() {
        try (Connection c = DriverManager.getConnection(DB_URL)) {

            String sql = """
                CREATE TABLE IF NOT EXISTS simulations_grouped (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    json TEXT
                );
            """;

            c.createStatement().execute(sql);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sauvegarde un HistoryItem dans la base.
     *
     * @param item Objet HistoryItem (timestamp, label, méthodes)
     *
     * Le JSON généré par item.toJson() est stocké directement.
     */
    public void save(HistoryItem item) {
        try (Connection c = DriverManager.getConnection(DB_URL)) {

            String sql = "INSERT INTO simulations_grouped (json) VALUES (?)";
            PreparedStatement st = c.prepareStatement(sql);

            //  Conversion automatique : objet → JSON
            st.setString(1, item.toJson());
            st.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Charge toutes les simulations sauvegardées, triées par ID décroissant
     * (donc les plus récentes en premier).
     *
     * @return Liste complète des HistoryItem
     */
    public List<HistoryItem> loadAll() {

        List<HistoryItem> list = new ArrayList<>();

        try (Connection c = DriverManager.getConnection(DB_URL)) {

            String sql = "SELECT json FROM simulations_grouped ORDER BY id DESC";
            ResultSet rs = c.createStatement().executeQuery(sql);

            while (rs.next()) {
                String json = rs.getString("json");

                //  Reconstruction : JSON → objet HistoryItem
                list.add(HistoryItem.fromJson(json));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
