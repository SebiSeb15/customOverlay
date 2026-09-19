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

    private static final int POPUP_W = 110;
    private static final int POPUP_H = 104;
    private final List<AbstractWidget> pickerWidgets = new ArrayList<>();
    private int activePickerX, activePickerY;

    @Override
    protected void init() {
        pickerWidgets.clear();
        activeColorPickerLine = null;
        activeColorHexBox = null;

        int headerHeight = 30;
        int footerHeight = 40;
        int listHeight = this.height - headerHeight - footerHeight;

        this.listWidget = new LineListWidget(this.minecraft, this.width, listHeight, headerHeight, 26);
        this.addRenderableWidget(this.listWidget);
        StringWidget titleWidget = new StringWidget(width/2-this.font.width(title)/2, 10, this.width, this.font.lineHeight, this.title, this.font);
        this.addRenderableWidget(titleWidget);

        List<HudLine> lines = CustomoverlayClient.CONFIG.lines;
        for (int i = 0; i < lines.size(); i++) {
            this.listWidget.addLineEntry(lines.get(i), i);
        }

        int startX = (this.width - 320) / 2;
        int buttonY = this.height - 30;

        this.addRenderableWidget(Button.builder(Component.literal("+ Ajouter une ligne"), b -> {
            lines.add(new HudLine("nouvelle_ligne", "{fps}", 2, 0));
            rebuildWidgets();
        }).bounds(startX, buttonY, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Sauvegarder & Fermer"), b -> {
            CustomoverlayClient.saveConfig();
            this.onClose();
        }).bounds(startX + 170, buttonY, 150, 20).build());
    }

    private <T extends AbstractWidget> T addPickerWidget(T widget) {
        pickerWidgets.add(widget);
        return this.addRenderableWidget(widget);
    }

    private void openColorPicker(HudLine line, int x, int y) {
        closeColorPicker();

        // Empêche la popup de sortir de l'écran
        x = Math.clamp(x, 4, this.width - POPUP_W - 4);
        y = Math.clamp(y, 4, this.height - POPUP_H - 4);

        activeColorPickerLine = line;
        activePickerX = x;
        activePickerY = y;

        addPickerWidget(new PickerBackground(x, y, POPUP_W, POPUP_H, line));

        EditBox hex = new EditBox(this.font, x + 6, y + 6, 75, 16, Component.literal("HexColor"));
        hex.setMaxLength(8);
        hex.setValue(String.format("%08X", line.color));
        hex.setResponder(val -> {
            // filtre de characters
            String cleaned = val.replaceAll("[^0-9a-fA-F]", "");
            if (!cleaned.equals(val)) {
                hex.setValue(cleaned);
                return;
            }
            line.color = parseColorHex(cleaned, line.color);
        });
        activeColorHexBox = addPickerWidget(hex);
        this.setFocused(hex);

        for (int i = 0; i < PALETTE.length; i++) {
            addPickerWidget(new ColorSwatch(
                    x + 6 + (i % 4) * 18,
                    y + 26 + (i / 4) * 18,
                    16, PALETTE[i],
                    chosen -> {
                        line.color = 0xFF000000 | chosen;
                        activeColorHexBox.setValue(String.format("%08X", line.color));
                    }));
        }
    }


    private void closeColorPicker() {
        pickerWidgets.forEach(this::removeWidget);
        pickerWidgets.clear();
        activeColorPickerLine = null;
        activeColorHexBox = null;
        this.setFocused(null);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        if (activeColorPickerLine != null) {
            // La popup est prioritaire : la liste en dessous ne reçoit rien
            for (AbstractWidget w : pickerWidgets) {
                if (w.mouseClicked(event, doubleClick)) {
                    this.setFocused(w);
                    return true;
                }
            }
            boolean inside = event.x() >= activePickerX && event.x() < activePickerX + POPUP_W
                    && event.y() >= activePickerY && event.y() < activePickerY + POPUP_H;
            if (!inside) closeColorPicker();   // clic ailleurs = fermer
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    // Fond + aperçu de la popup, en widget : plus de double rendu dans extractRenderState
    private static class PickerBackground extends AbstractWidget {
        private final HudLine line;

        PickerBackground(int x, int y, int w, int h, HudLine line) {
            super(x, y, w, h, Component.empty());
            this.line = line;
            this.active = false;   // ne capte pas les clics, c'est mouseClicked de l'écran qui s'en charge
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mx, int my, float a) {
            g.fill(getX(), getY(), getX() + width, getY() + height, 0xFF222222);
            g.outline(getX(), getY(), width, height, 0xFFFFFFFF);
            g.fill(getX() + 86, getY() + 6, getX() + 102, getY() + 22, line.color);
            g.outline(getX() + 86, getY() + 6, 16, 16, 0xFFFFFFFF);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {}
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
        private final Consumer<ColorButton> onPress;

        public ColorButton(int width, int height, HudLine line, Consumer<ColorButton> onPress) {
            super(0, 0, width, height, Component.empty());
            this.line = line;
            this.onPress = onPress;
        }

        @Override
        public void onClick(@NonNull MouseButtonEvent event, boolean doubleClick) {
            onPress.accept(this);
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

    private class LineListWidget extends ContainerObjectSelectionList<LineListWidget.LineEntry> {

        public LineListWidget(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        private int idW()       { return width / 10; }
        private int templateW() { return width / 3; }

        @Override
        public int getRowWidth() {
            return 104 + idW() + templateW();
        }

        @Override
        protected int scrollBarX() {
            return width - 30;
        }

        public void addLineEntry(HudLine line, int index) {
            this.addEntry(new LineEntry(line, index));
        }

        public class LineEntry extends Entry<LineEntry> {

            private final List<AbstractWidget> widgets = new ArrayList<>();
            private final Button upButton, downButton, deleteButton;
            private final Checkbox checkbox;
            private final EditBox idBox, templateBox;
            private final ColorButton colorButton;

            public LineEntry(HudLine line, int index) {
                List<HudLine> lines = CustomoverlayClient.CONFIG.lines;

                upButton = Button.builder(Component.literal("▲"), b -> {
                    Collections.swap(lines, index, index - 1);
                    rebuildWidgets();
                }).bounds(0, 0, 16, 10).build();
                upButton.active = index > 0;   // un bouton inactif ne reçoit pas de clic

                downButton = Button.builder(Component.literal("▼"), b -> {
                    Collections.swap(lines, index, index + 1);
                    rebuildWidgets();
                }).bounds(0, 0, 16, 10).build();
                downButton.active = index < lines.size() - 1;

                checkbox = Checkbox.builder(Component.literal(""), font)
                        .selected(line.enabled)
                        .onValueChange((cb, checked) -> line.enabled = checked)
                        .build();

                idBox = new EditBox(font, 0, 0, idW(), 20, Component.literal("ID"));
                idBox.setMaxLength(32);
                idBox.setValue(line.id);
                idBox.setResponder(v -> line.id = v);

                templateBox = new EditBox(font, 0, 0, templateW(), 20, Component.literal("template"));
                templateBox.setMaxLength(256);
                templateBox.setValue(line.template);
                templateBox.setResponder(v -> line.template = v);

                colorButton = new ColorButton(20, 20, line, btn -> {
                    if (activeColorPickerLine == line) {
                        closeColorPicker();
                    } else {
                        openColorPicker(line, btn.getX(), btn.getY() + 22);
                    }
                });

                deleteButton = Button.builder(Component.literal("✕"), b -> {
                    lines.remove(index);
                    rebuildWidgets();
                }).bounds(0, 0, 20, 20).build();

                widgets.addAll(List.of(upButton, downButton, checkbox, idBox, templateBox, colorButton, deleteButton));
            }

            @Override
            public void extractContent(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                       boolean hovering, float partialTick) {
                int x = getContentX();
                int y = getContentY();

                upButton.setPosition(x, y);
                downButton.setPosition(x, y + 10);
                checkbox.setPosition(x + 22, y);
                idBox.setPosition(x + 46, y);
                templateBox.setPosition(x + 52 + idW(), y);
                colorButton.setPosition(x + 58 + idW() + templateW(), y);
                deleteButton.setPosition(x + 84 + idW() + templateW(), y);

                for (AbstractWidget w : widgets) {
                    w.extractRenderState(graphics, mouseX, mouseY, partialTick);
                }
            }

            @Override
            public List<? extends GuiEventListener> children() { return widgets; }

            @Override
            public List<? extends NarratableEntry> narratables() { return widgets; }
        }
    }
}