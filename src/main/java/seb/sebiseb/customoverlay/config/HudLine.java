package seb.sebiseb.customoverlay.config;

public class HudLine {
    public String id;
    public boolean enabled = true;
    public String template = "Texte: {fps}";
    public int x = 10;
    public int y = 10;
    public int color = 0xFFFFFFFF; // blanc opaque (format ARGB)
    public float scale = 1.0f;
    public boolean shadow = true;

    public HudLine() {
    }

    public HudLine(String id, String template, int x, int y) {
        this.id = id;
        this.template = template;
        this.x = x;
        this.y = y;
    }
}
