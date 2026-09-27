package app.ui;

public final class Styles {
    private Styles(){}

    public static String global() {
        return """
            .root {
              -fx-font-family: 'Segoe UI','Inter','Arial',sans-serif;
              -fx-background-color: linear-gradient(to bottom, #0b0e14, #0f1116);
            }

            .fullscreen { -fx-padding: 0; }

            .center-col {
              -fx-spacing: 22;
              -fx-alignment: center;
            }

            .hero-title {
              -fx-font-size: 38;
              -fx-font-weight: 900;
              -fx-fill: white;
              -fx-text-alignment: center;
            }
            .hero-sub {
              -fx-fill: #cfd3dc;
              -fx-font-size: 13;
              -fx-text-alignment: center;
            }

            /* Colonne de menu sans carte autour */
            .menu-col {
              -fx-spacing: 14;
              -fx-alignment: center;
            }

            .menu-btn {
              -fx-background-color: rgba(255,255,255,0.08);
              -fx-text-fill: white;
              -fx-background-radius: 12;
              -fx-font-weight: 700;
              -fx-padding: 14 18;
              -fx-font-size: 15;
              -fx-effect: dropshadow( gaussian , rgba(0,0,0,0.0) , 0,0,0,0 );
            }
            .menu-btn:hover { -fx-background-color: rgba(255,255,255,0.14); }
            .menu-btn.primary { -fx-background-color: #3a6df0; }
            .menu-btn.primary:hover { -fx-opacity: 0.95; }

            .hint { -fx-text-fill: #aab1bf; -fx-font-size: 12; }
            """;
    }
}
