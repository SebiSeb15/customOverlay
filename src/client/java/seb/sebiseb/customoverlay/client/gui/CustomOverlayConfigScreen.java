package seb.sebiseb.customoverlay.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import seb.sebiseb.customoverlay.client.CustomoverlayClient;
import seb.sebiseb.customoverlay.config.HudLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class CustomOverlayConfigScreen extends Screen {

    private final Screen parent;
    private HudLine activeColorPickerLine = null;
    private EditBox activeColorHexBox = null;
    private int activePickerX = 0;
    private int activePickerY = 0;

    private LineListWidget listWidget;

    private static final int[] PALETTE = {
            0x000000, 0x0000AA, 0x00AA00, 0x00AAAA,
            0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
            0x555555, 0x5555FF, 0x55FF55, 0x55FFFF,
            0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF
    };

    public CustomOverlayConfigScreen(Screen parent) {
        super(Component.literal("Custom Overlay - Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        int headerHeight = 30;
        int footerHeight = 40;
        int listHeight = this.height - headerHeight - footerHeight;

        // 1. Liste déroulante au centre (prend tout l'espace entre le haut et les boutons du bas)
        this.listWidget = new LineListWidget(this.minecraft, this.width, listHeight, headerHeight, 26);
        this.addRenderableWidget(this.listWidget);

        List<HudLine> lines = CustomoverlayClient.CONFIG.lines;
        for (int i = 0; i < lines.size(); i++) {
            this.listWidget.addLineEntry(lines.get(i), i);
        }

        int startX = (this.width - 320) / 2;
        int bottomY = this.height - 30;
        // Boutons généraux
        Button addButton = Button.builder(Component.literal("+ Ajouter une ligne"), button -> {
            HudLine newLine = new HudLine("nouvelle_ligne", "{fps}", 2, 0);
            lines.add(newLine);
            closeColorPicker();
            this.rebuildWidgets();
        }).bounds(startX, bottomY + 10, 150, 20).build();
        this.addRenderableWidget(addButton);

        Button saveButton = Button.builder(Component.literal("Sauvegarder & Fermer"), button -> {
            CustomoverlayClient.saveConfig();
            this.onClose();
        }).bounds(startX+170, bottomY + 10, 150, 20).build();
        this.addRenderableWidget(saveButton);

        // Si la pop-up était ouverte avant une réinitialisation de l'écran, on la rouvre proprement
        if (activeColorPickerLine != null) {
            HudLine savedLine = activeColorPickerLine;
            int savedX = activePickerX;
            int savedY = activePickerY;
            this.activeColorPickerLine = null; // reset pour forcer openColorPicker
            openColorPicker(savedLine, savedX, savedY);
        }
    }

    private void openColorPicker(HudLine line, int x, int y) {
        // Nettoyer d'abord si une pop-up était déjà là
        if (activeColorPickerLine != null) {
            closeColorPicker();
        }

        this.activeColorPickerLine = line;
        this.activePickerX = x;
        this.activePickerY = y;

        // Création du champ Hexa
        this.activeColorHexBox = new EditBox(this.font, x + 6, y + 6, 75, 16, Component.literal("HexColor"));
        this.activeColorHexBox.setMaxLength(8);
        this.activeColorHexBox.setValue(String.format("%08X", line.color));
        // Gestion de la saisie + filtrage
        this.activeColorHexBox.setResponder(val -> {
            // Supprime tout ce qui n'est pas 0-9, a-f, A-F
            String cleaned = val.replaceAll("[^0-9a-fA-F]", "");
            // Si des caractères invalides ont été tapés, on réinjecte la chaîne nettoyée
            if (!cleaned.equals(val)) {
                this.activeColorHexBox.setValue(cleaned);
                return;
            }
            // Met à jour la couleur si la chaîne est valide
            line.color = parseColorHex(cleaned, line.color);
        });

        this.addRenderableWidget(this.activeColorHexBox);
        this.setFocused(this.activeColorHexBox);

        // Ajout des boutons Swatch de la palette en tant que widgets
        int gridX = x + 6;
        int gridY = y + 26;
        for (int i = 0; i < PALETTE.length; i++) {
            int row = i / 4;
            int col = i % 4;
            int colorVal = PALETTE[i];

            ColorSwatch swatch = new ColorSwatch(
                    gridX + col * 18,
                    gridY + row * 18,
                    16,
                    colorVal,
                    chosen -> {
                        line.color = 0xFF000000 | chosen;
                        if (activeColorHexBox != null) {
                            activeColorHexBox.setValue(String.format("%08X", line.color));
                        }
                    }
            );
            this.addRenderableWidget(swatch);
        }
    }

    private void closeColorPicker() {
        this.activeColorPickerLine = null;
        this.activeColorHexBox = null;
        this.rebuildWidgets(); // Nettoie et régénère la liste des widgets sans la pop-up
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // 1. Dessine d'abord TOUS les widgets normaux de l'arrière-plan
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        // 2. Si le pop-up est ouvert, on le dessine PAR-DESSUS tous les autres widgets
        if (activeColorPickerLine != null) {
            int width = 110;
            int height = 104;
            // Fond gris opaque + contour blanc qui recouvre les boutons en dessous
            graphics.fill(activePickerX, activePickerY, activePickerX + width, activePickerY + height, 0xFF222222);
            graphics.outline(activePickerX, activePickerY, width, height, 0xFFFFFFFF);
            // Aperçu de la couleur
            graphics.fill(activePickerX + 86, activePickerY + 6, activePickerX + 102, activePickerY + 22, activeColorPickerLine.color);
            graphics.outline(activePickerX + 86, activePickerY + 6, 16, 16, 0xFFFFFFFF);
            // Force le rendu de l'EditBox au premier plan
            if (activeColorHexBox != null) {
                activeColorHexBox.extractRenderState(graphics, mouseX, mouseY, partialTick);
            }
            // Force le rendu des Swatchs (palette 4x4) au premier plan
            for (GuiEventListener child : this.children()) {
                if (child instanceof ColorSwatch swatch) {
                    swatch.extractRenderState(graphics, mouseX, mouseY, partialTick);
                }
            }
        }
    }

    private static int parseColorHex(String hex, int fallback) {
        String cleaned = hex.trim().replace("#", "");
        try {
            if (cleaned.length() == 6) {
                return 0xFF000000 | (int) Long.parseLong(cleaned, 16);
            } else if (cleaned.length() == 8) {
                return (int) Long.parseLong(cleaned, 16);
            }
        } catch (NumberFormatException ignored) {
        }
        return fallback;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class ColorButton extends AbstractWidget {
        private final HudLine line;
        private final Runnable onPress;

        public ColorButton(int x, int y, int width, int height, HudLine line, Runnable onPress) {
            super(x, y, width, height, Component.empty());
            this.line = line;
            this.onPress = onPress;
        }

        @Override
        public void onClick(@NonNull MouseButtonEvent event, boolean doubleClick) {
            onPress.run();
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            graphics.fill(getX(), getY(), getX() + width, getY() + height, line.color);
            graphics.outline(getX(), getY(), width, height, isHovered() ? 0xFFFFFFFF : 0xFF000000);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {}
    }

    private static class ColorSwatch extends AbstractWidget {
        private final int colorValue;
        private final Consumer<Integer> onClick;

        public ColorSwatch(int x, int y, int size, int colorValue, Consumer<Integer> onClick) {
            super(x, y, size, size, Component.empty());
            this.colorValue = colorValue;
            this.onClick = onClick;
        }

        @Override
        public void onClick(@NonNull MouseButtonEvent event, boolean doubleClick) {
            onClick.accept(colorValue);
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF000000 | colorValue);
            if (isHovered()) {
                graphics.outline(getX() - 1, getY() - 1, width + 2, height + 2, 0xFFFFFFFF);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {}
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        // 1. Priorité au Pop-up de couleur
        if (activeColorPickerLine != null) {
            // ... (votre gestion du pop-up reste identique)
        }

        // 2. Traitement standard : transmet le clic à la liste déroulante qui relaiera à LineEntry
        if (this.listWidget != null && this.listWidget.isMouseOver(event.x(), event.y())) {
            if (this.listWidget.mouseClicked(event, doubleClick)) {
                this.setFocused(this.listWidget);
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    private class LineListWidget extends ContainerObjectSelectionList<LineListWidget.LineEntry> {

        public LineListWidget(net.minecraft.client.Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }
        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            LineEntry entry = this.getEntryAtPosition(event.x(), event.y());
            if (entry != null) {
                if (entry.mouseClicked(event, doubleClick)) {
                    this.setSelected(entry);
                    this.setFocused(entry);
                    return true;
                }
            }
            return super.mouseClicked(event, doubleClick);
        }

        @Override
        protected int scrollBarX() {
            return width - 30;
        }

        public void addLineEntry(HudLine line, int index) {
            this.addEntry(new LineEntry(line, index));
        }

        public class LineEntry extends Entry<LineEntry> {

            private final Button upButton;
            private final Button downButton;
            private final Checkbox checkbox;
            private final EditBox idBox;
            private final EditBox templateBox;
            private ColorButton colorButton = null;
            private final Button deleteButton;

            private final List<GuiEventListener> childrenList = new ArrayList<>();
            private final List<net.minecraft.client.gui.narration.NarratableEntry> narratables = new java.util.ArrayList<>();

            public LineEntry(HudLine line, int index) {

                List<HudLine> lines = CustomoverlayClient.CONFIG.lines;
                // Position Y temporaire (sera ajustée dynamiquement lors du rendu)
                int y = 0;
                int x = 0;
                // 1. Flèches ▲ / ▼
                this.upButton = Button.builder(Component.literal("▲"), button -> {
                    if (index > 0) {
                        Collections.swap(lines, index, index - 1);
                        closeColorPicker();
                        rebuildWidgets();
                    }
                }).bounds(x, y, 16, 10).build();
                this.upButton.active = index > 0;

                this.downButton = Button.builder(Component.literal("▼"), button -> {
                    if (index < lines.size() - 1) {
                        Collections.swap(lines, index, index + 1);
                        closeColorPicker();
                        rebuildWidgets();
                    }
                }).bounds(x, y + 10, 16, 10).build();
                this.downButton.active = index < lines.size() - 1;
                // 2. Checkbox
                this.checkbox = Checkbox.builder(Component.literal(""), font)
                        .pos(x, y)
                        .selected(line.enabled)
                        .onValueChange((cb, checked) -> line.enabled = checked)
                        .build();
                // 3. Champ ID
                this.idBox = new EditBox(font, x, y, width/10, 20, Component.literal("ID"));
                this.idBox.setMaxLength(32);
                this.idBox.setValue(line.id);
                this.idBox.setResponder(newId -> line.id = newId);
                // 4. Champ Template
                this.templateBox = new EditBox(font, x, y, width/3, 20, Component.literal("template"));
                this.templateBox.setMaxLength(256);
                this.templateBox.setValue(line.template);
                this.templateBox.setResponder(newValue -> line.template = newValue);
                // 5. Bouton Couleur
                this.colorButton = new ColorButton(x, y, 20, 20, line, () -> {
                    if (activeColorPickerLine == line) {
                        closeColorPicker();
                    } else {
                        openColorPicker(line, colorButton.getX(), colorButton.getY() + 22);
                    }
                });
                // 6. Bouton Supprimer [X]
                this.deleteButton = Button.builder(Component.literal("✕"), button -> {
                    lines.remove(index);
                    closeColorPicker();
                    rebuildWidgets();
                }).bounds(x, y, 20, 20).build();
                // Enregistrement des composants pour l'interaction des clics
                childrenList.add(upButton);
                childrenList.add(downButton);
                childrenList.add(checkbox);
                childrenList.add(idBox);
                childrenList.add(templateBox);
                childrenList.add(colorButton);
                childrenList.add(deleteButton);

                narratables.add(upButton);
                narratables.add(downButton);
                narratables.add(checkbox);
                narratables.add(idBox);
                narratables.add(templateBox);
                narratables.add(colorButton);
                narratables.add(deleteButton);
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                double mouseX = event.x();
                double mouseY = event.y();
                int button = event.button();
                // Seul le clic gauche (0) nous intéresse pour les boutons
                if (button != 0) return false;

                int entryY = getContentY();
                int rowWidth = 72+width/3+6+width/10+6;
                int x = (width - rowWidth) / 2;
                // Repositionnement dynamique des widgets à la bonne position X/Y
                upButton.setPosition(x, entryY);
                downButton.setPosition(x, entryY + 10);
                x += 22;
                checkbox.setPosition(x, entryY);
                x += 24;
                idBox.setPosition(x, entryY);
                x += width/10+6;
                templateBox.setPosition(x, entryY);
                x += width/3+6;
                colorButton.setPosition(x, entryY);
                x += 26;
                deleteButton.setPosition(x, entryY);

                // Test de survol et appel direct aux signatures standards des widgets (double, double, int)
                if (upButton.isMouseOver(mouseX, mouseY) && upButton.mouseClicked(event,doubleClick)) return true;
                if (downButton.isMouseOver(mouseX, mouseY) && downButton.mouseClicked(event,doubleClick)) return true;
                if (checkbox.isMouseOver(mouseX, mouseY) && checkbox.mouseClicked(event,doubleClick)) return true;

                if (idBox.isMouseOver(mouseX, mouseY)) {
                    idBox.mouseClicked(event,doubleClick);
                    this.setFocused(idBox);
                    return true;
                }

                if (templateBox.isMouseOver(mouseX, mouseY)) {
                    templateBox.mouseClicked(event,doubleClick);
                    this.setFocused(templateBox);
                    return true;
                }

                if (colorButton.isMouseOver(mouseX, mouseY) && colorButton.mouseClicked(event,doubleClick)) return true;
                if (deleteButton.isMouseOver(mouseX, mouseY) && deleteButton.mouseClicked(event,doubleClick)) return true;

                return false;
            }

            @Override
            public void extractContent(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
                // La position Y globale de la ligne est calculée par la liste déroulante
                int entryY = getContentY();
                int rowWidth = 72+width/3+6+width/10+6;
                int x = (width - rowWidth) / 2;
                // Repositionnement dynamique des widgets à la bonne position X/Y
                upButton.setPosition(x, entryY);
                downButton.setPosition(x, entryY + 10);
                x += 22;
                checkbox.setPosition(x, entryY);
                x += 24;
                idBox.setPosition(x, entryY);
                x += width/10+6;
                templateBox.setPosition(x, entryY);
                x += width/3+6;
                colorButton.setPosition(x, entryY);
                x += 26;
                deleteButton.setPosition(x, entryY);
                // Rendu de chaque composant
                upButton.extractRenderState(graphics, mouseX, mouseY, partialTick);
                downButton.extractRenderState(graphics, mouseX, mouseY, partialTick);
                checkbox.extractRenderState(graphics, mouseX, mouseY, partialTick);
                idBox.extractRenderState(graphics, mouseX, mouseY, partialTick);
                templateBox.extractRenderState(graphics, mouseX, mouseY, partialTick);
                colorButton.extractRenderState(graphics, mouseX, mouseY, partialTick);
                deleteButton.extractRenderState(graphics, mouseX, mouseY, partialTick);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return Collections.emptyList();
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return Collections.emptyList();
            }
        }
    }
}