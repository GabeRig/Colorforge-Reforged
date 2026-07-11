package com.pineapplecompany.colorflow.gui.theme;

public enum Theme {
    
    CHERRY_BLOSSOM(
        "Cherry Blossom",
        0xFF0A0A12,  // Background - presque noir
        0xFF1E1E2E,  // Panel - gris foncé visible
        0xFF2D2D44,  // Secondary - gris moyen
        0xFFFF4477,  // Primary accent - rose vif
        0xFFFF6B9D,  // Secondary accent - rose clair
        0xFFFFAACC,  // Highlight - rose très clair
        0xFFFFFFFF,  // Text primary - blanc pur
        0xFFAAAAAA,  // Text secondary - gris clair
        0xFFFF4477   // Border - rose vif
    ),
    
    MIDNIGHT_OCEAN(
        "Midnight Ocean",
        0xFF050510,  // Background
        0xFF101025,  // Panel
        0xFF1A1A3A,  // Secondary
        0xFF00CCFF,  // Primary accent - cyan vif
        0xFF44DDFF,  // Secondary accent
        0xFF88EEFF,  // Highlight
        0xFFFFFFFF,  // Text primary
        0xFFAABBCC,  // Text secondary
        0xFF00CCFF   // Border
    ),
    
    FOREST_GROVE(
        "Forest Grove",
        0xFF0A100A,  // Background
        0xFF1A2A1A,  // Panel
        0xFF2A3D2A,  // Secondary
        0xFF44DD44,  // Primary accent - vert vif
        0xFF66EE66,  // Secondary accent
        0xFF99FF99,  // Highlight
        0xFFFFFFFF,  // Text primary
        0xFFAABBAA,  // Text secondary
        0xFF44DD44   // Border
    ),
    
    SUNSET_EMBER(
        "Sunset Ember",
        0xFF100A05,  // Background
        0xFF251510,  // Panel
        0xFF3D2A1A,  // Secondary
        0xFFFF6600,  // Primary accent - orange vif
        0xFFFF8833,  // Secondary accent
        0xFFFFAA66,  // Highlight
        0xFFFFFFFF,  // Text primary
        0xFFCCAA88,  // Text secondary
        0xFFFF6600   // Border
    ),
    
    ARCTIC_FROST(
        "Arctic Frost",
        0xFFE8E8F0,  // Background - gris très clair
        0xFFFFFFFF,  // Panel - blanc
        0xFFD0D0E0,  // Secondary - gris clair
        0xFF2266FF,  // Primary accent - bleu vif
        0xFF4488FF,  // Secondary accent
        0xFF66AAFF,  // Highlight
        0xFF000000,  // Text primary - noir
        0xFF444466,  // Text secondary - gris foncé
        0xFF2266FF   // Border
    );
    
    private final String name;
    private final int background;
    private final int panelBackground;
    private final int secondaryBackground;
    private final int primaryAccent;
    private final int secondaryAccent;
    private final int highlight;
    private final int textPrimary;
    private final int textSecondary;
    private final int border;
    
    Theme(String name, int background, int panelBackground, int secondaryBackground,
          int primaryAccent, int secondaryAccent, int highlight,
          int textPrimary, int textSecondary, int border) {
        this.name = name;
        this.background = background;
        this.panelBackground = panelBackground;
        this.secondaryBackground = secondaryBackground;
        this.primaryAccent = primaryAccent;
        this.secondaryAccent = secondaryAccent;
        this.highlight = highlight;
        this.textPrimary = textPrimary;
        this.textSecondary = textSecondary;
        this.border = border;
    }
    
    public String getName() { return name; }
    public int getBackground() { return background; }
    public int getPanelBackground() { return panelBackground; }
    public int getSecondaryBackground() { return secondaryBackground; }
    public int getPrimaryAccent() { return primaryAccent; }
    public int getSecondaryAccent() { return secondaryAccent; }
    public int getHighlight() { return highlight; }
    public int getTextPrimary() { return textPrimary; }
    public int getTextSecondary() { return textSecondary; }
    public int getBorder() { return border; }
}