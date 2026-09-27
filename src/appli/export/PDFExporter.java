package appli.export;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.chart.Chart;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Utilitaire responsable de la génération de rapports PDF.
 *
 * Ce composant permet :
 *  - d'ajouter des titres,
 *  - d'ajouter du texte simple,
 *  - d'ajouter du texte "wrapé" automatiquement (retour à la ligne),
 *  - d'exporter des graphiques JavaFX (Chart) sous forme d'images dans le PDF.
 *
 * Il s'appuie sur la bibliothèque PDFBox 3.x (API bas niveau) et
 * sur SwingFXUtils pour convertir les Chart JavaFX en BufferedImage.
 */
public class PDFExporter {

    // Hauteur logique d'une page PDF (en points)
    private static final int PAGE_HEIGHT = 800;

    // Marge gauche
    private static final int MARGIN = 40;

    // Document PDF global
    private PDDocument doc;

    // Page courante
    private PDPage page;

    // "Crayon" pour dessiner/écrire sur la page courante
    private PDPageContentStream stream;

    // Position verticale courante (curseur)
    private float cursorY;

    // Polices PDFBox 3.x
    private final PDType1Font FONT_NORMAL =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private final PDType1Font FONT_BOLD =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    /**
     * Constructeur.
     * Initialise un nouveau document PDF et crée une première page.
     */
    public PDFExporter() throws Exception {
        doc = new PDDocument();
        addPage();
    }

    // ---------------------------------------------------
    //  GESTION DES PAGES
    // ---------------------------------------------------

    /**
     * Ajoute une nouvelle page au document et réinitialise le contexte d'écriture.
     */
    private void addPage() throws Exception {
        // Si un flux d'écriture existait pour la page précédente, on le ferme.
        if (stream != null) stream.close();

        // Création et ajout d'une nouvelle page au document
        page = new PDPage();
        doc.addPage(page);

        // Nouveau "content stream" pour écrire sur cette page
        stream = new PDPageContentStream(doc, page);

        // On place le curseur légèrement en dessous du haut de la page
        cursorY = PAGE_HEIGHT - 50;
    }

    /**
     * Vérifie qu'il reste suffisamment de place pour écrire/afficher un bloc de hauteur donnée.
     * Si ce n'est pas le cas, on crée une nouvelle page.
     *
     * @param needed hauteur nécessaire (en points)
     */
    private void ensureSpace(int needed) throws Exception {
        // Si le prochain bloc descendrait en dessous de 60 → nouvelle page
        if (cursorY - needed < 60) {
            addPage();
        }
    }

    /**
     * Descend le curseur vertical d'une hauteur donnée.
     *
     * @param h hauteur en points (plus h est grand, plus on descend bas)
     */
    private void newLine(float h) {
        cursorY -= h;
    }

    // ---------------------------------------------------
    //  BLOCS DE TEXTE
    // ---------------------------------------------------

    /**
     * Ajoute un titre en gras, de grande taille.
     *
     * @param text texte du titre
     */
    public void addTitle(String text) throws Exception {
        // On s'assure qu'il reste de la place pour le bloc titre
        ensureSpace(40);

        stream.beginText();
        stream.setFont(FONT_BOLD, 22);
        // On positionne le texte à la marge gauche et à la hauteur courante
        stream.newLineAtOffset(MARGIN, cursorY);
        stream.showText(text);
        stream.endText();

        // On descend le curseur pour laisser de l'espace sous le titre
        newLine(35);
    }

    /**
     * Ajoute un texte long avec retour à la ligne automatique
     * (wrap par largeur max en points).
     *
     * @param text      le texte brut à écrire
     * @param fontSize  la taille de police utilisée
     * @param maxWidth  largeur maximale avant de couper la ligne
     */
    public void addWrappedText(String text, float fontSize, float maxWidth) throws Exception {
        if (text == null || text.isEmpty()) return;

        StringBuilder line = new StringBuilder();

        // On parcourt le texte caractère par caractère
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            line.append(c);

            // Largeur réelle du texte courant (en points)
            float width = FONT_NORMAL.getStringWidth(line.toString()) / 1000 * fontSize;

            // Si la ligne dépasse la largeur max : on l'écrit et on repart à zéro
            if (width > maxWidth) {
                writeWrappedLine(line.toString(), fontSize);
                line = new StringBuilder();
            }
        }

        // On écrit la dernière ligne restante
        if (line.length() > 0) {
            writeWrappedLine(line.toString(), fontSize);
        }
    }

    /**
     * Écrit une seule ligne déjà "wrapée" (pré-coupée) dans le PDF.
     *
     * @param text      ligne de texte à écrire
     * @param fontSize  taille de police
     */
    private void writeWrappedLine(String text, float fontSize) throws Exception {
        // On s'assure qu'on a la place pour cette ligne
        ensureSpace((int)(fontSize + 6));

        stream.beginText();
        stream.setFont(FONT_NORMAL, fontSize);
        stream.newLineAtOffset(MARGIN, cursorY);
        stream.showText(text);
        stream.endText();

        // On descend d'un cran (ligne suivante)
        newLine(fontSize + 4);
    }

    /**
     * Ajoute une ligne de texte simple (sans wrap automatique).
     * Utilisé pour les petites chaînes (labels, valeurs, etc.).
     *
     * @param text texte à afficher
     */
    public void addText(String text) throws Exception {
        // 22 points de hauteur suffisent pour une ligne de texte normal
        ensureSpace(22);

        stream.beginText();
        stream.setFont(FONT_NORMAL, 12);
        stream.newLineAtOffset(MARGIN, cursorY);
        stream.showText(text);
        stream.endText();

        newLine(18);
    }

    // ---------------------------------------------------
    //  EXPORT DES GRAPHIQUES
    // ---------------------------------------------------

    /**
     * Capture un graphique JavaFX (Chart), le convertit en PNG,
     * puis l'insère en tant qu'image dans le PDF.
     *
     * @param title  titre affiché avant le graphique (simple texte)
     * @param chart  graphique JavaFX à exporter (LineChart, BarChart, etc.)
     */
    public void addChart(String title, Chart chart) throws Exception {
        if (chart == null) return;

        // Titre du graphique
        addText(title);

        // On s'assure d'avoir une grande zone libre (image assez haute)
        ensureSpace(350);

        // 1) Snapshot JavaFX → Image Java (BufferedImage)
        BufferedImage img = SwingFXUtils.fromFXImage(chart.snapshot(null, null), null);

        // 2) On écrit l'image dans un fichier temporaire (PDFBox lit depuis un File)
        File tmp = File.createTempFile("chart", ".png");
        ImageIO.write(img, "png", tmp);

        // 3) On crée un objet image PDFBox à partir du fichier
        PDImageXObject image = PDImageXObject.createFromFileByContent(tmp, doc);

        // 4) On calcule les dimensions d'affichage en conservant le ratio
        float maxWidth = 500;
        float ratio = maxWidth / img.getWidth();          // facteur d'échelle
        float displayHeight = img.getHeight() * ratio;    // hauteur ajustée

        // 5) On dessine l'image sur la page
        stream.drawImage(image, MARGIN, cursorY - displayHeight, maxWidth, displayHeight);

        // 6) On descend le curseur sous l'image
        newLine(displayHeight + 30);
    }

    // ---------------------------------------------------
    //  SAUVEGARDE DU DOCUMENT
    // ---------------------------------------------------

    /**
     * Sauvegarde le document PDF sur disque et libère les ressources.
     *
     * @param path chemin complet du fichier de sortie (ex: "rapport.pdf")
     */
    public void save(String path) throws Exception {
        // On ferme le flux d'écriture de la dernière page
        if (stream != null) stream.close();

        // Sauvegarde physique sur disque
        doc.save(path);

        // Fermeture du document (libère la mémoire)
        doc.close();
    }
}
