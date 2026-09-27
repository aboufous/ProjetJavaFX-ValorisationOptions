package app.graphics;

import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;

/**
 * Logo vectoriel centré, courbe "pricing" confinée dans le cercle.
 */
public class FinanceLogo extends StackPane {

    public FinanceLogo(double size) {
        double r = size / 2.0;

        // Cercle de fond
        Circle bg = new Circle(r);
        bg.setFill(Color.web("#121621"));
        bg.setStroke(Color.web("#1f2533"));
        bg.setStrokeWidth(1.0);

        // Effet ombre douce
        DropShadow ds = new DropShadow();
        ds.setColor(Color.color(0, 0, 0, 0.35));
        ds.setRadius(8);
        setEffect(ds);

        // Axes
        Line xAxis = new Line(-r * 0.55, r * 0.25, r * 0.55, r * 0.25);
        xAxis.setStroke(Color.web("#2a3347"));
        xAxis.setStrokeWidth(1.2);

        Line yAxis = new Line(-r * 0.4, r * 0.45, -r * 0.4, -r * 0.4);
        yAxis.setStroke(Color.web("#2a3347"));
        yAxis.setStrokeWidth(1.2);

        // Courbe de pricing
        Polyline curve = new Polyline(
                -r * 0.40,  r * 0.20,
                -r * 0.20,  r * 0.05,
                0.0,       -r * 0.05,
                r * 0.20,  -r * 0.15,
                r * 0.35,  -r * 0.10
        );
        curve.setStroke(Color.web("#3a6df0"));
        curve.setStrokeWidth(3.0);
        curve.setFill(Color.TRANSPARENT);

        // Petits points colorés
        Circle p1 = dot(-r * 0.20,  r * 0.05, "#8099ff");
        Circle p2 = dot(0.0,      -r * 0.05, "#88ffe0");
        Circle p3 = dot(r * 0.20, -r * 0.15, "#ffd166");

        // Groupe de dessin centré
        Group drawing = new Group(xAxis, yAxis, curve, p1, p2, p3);

        // Clipping : on découpe le dessin dans le cercle
        Circle clip = new Circle(r);
        drawing.setClip(clip);

        // Empilement
        getChildren().addAll(bg, drawing);
        setMinSize(size, size);
        setPrefSize(size, size);
        setMaxSize(size, size);
    }

    private Circle dot(double x, double y, String colorHex) {
        Circle c = new Circle(4.2);
        c.setFill(Color.web(colorHex));
        c.setStroke(Color.web("#0b0e14"));
        c.setStrokeWidth(1.0);
        c.setTranslateX(x);
        c.setTranslateY(y);
        return c;
    }
}
