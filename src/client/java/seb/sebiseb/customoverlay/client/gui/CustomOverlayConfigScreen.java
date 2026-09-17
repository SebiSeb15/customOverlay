package seb.sebiseb.customoverlay.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import seb.sebiseb.customoverlay.client.CustomoverlayClient;
import seb.sebiseb.customoverlay.config.HudLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomOverlayConfigScreen extends Screen {

    private final Screen parent;
    private final List<HudLine> previewLines = new ArrayList<>();
    private final List<Integer> previewY = new ArrayList<>();

    public CustomOverlayConfigScreen(Screen parent) {
        super(Component.literal("Custom Overlay - Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        previewLines.clear();
        previewY.clear();

        int y = 30;
        List<HudLine> lines = CustomoverlayClient.CONFIG.lines;

        for (int i = 0; i < lines.size(); i++) {
            HudLine line = lines.get(i);
            final int index = i;

            Checkbox checkbox = Checkbox.builder(Component.literal(line.id), this.font)
                    .pos(10, y)
                    .selected(line.enabled)
                    .onValueChange((cb, checked) -> line.enabled = checked)
                    .build();
            this.addRenderableWidget(checkbox);

            EditBox templateBox = new EditBox(this.font, 100, y, 150, 18, Component.literal("template"));
            templateBox.setValue(line.template);
            templateBox.setResponder(newValue -> line.template = newValue);
            this.addRenderableWidget(templateBox);

            EditBox colorBox = new EditBox(this.font, 260, y, 70, 18, Component.literal("color"));
            colorBox.setValue(String.format("%08X", line.color));
            colorBox.setMaxLength(8);
            colorBox.setResponder(newValue -> line.color = parseColorHex(newValue, line.color));
            this.addRenderableWidget(colorBox);

            previewLines.add(line);
            previewY.add(y);

            Button upButton = Button.builder(Component.literal("▲"), button -> {
                if (index > 0) {
                    Collections.swap(lines, index, index - 1);
                    this.clearWidgets();
                    this.init();
                }
            }).bounds(360, y, 20, 18).build();
            upButton.active = index > 0;
            this.addRenderableWidget(upButton);

            Button downButton = Button.builder(Component.literal("▼"), button -> {
                if (index < lines.size() - 1) {
                    Collections.swap(lines, index, index + 1);
                    this.clearWidgets();
                    this.init();
                }
            }).bounds(385, y, 20, 18).build();
            downButton.active = index < lines.size() - 1;
            this.addRenderableWidget(downButton);

            Button deleteButton = Button.builder(Component.literal("✕"), button -> {
                lines.remove(index);
                this.clearWidgets();
                this.init();
            }).bounds(410, y, 20, 18).build();
            this.addRenderableWidget(deleteButton);

            y += 24;
        }

        Button addButton = Button.builder(Component.literal("+ Ajouter une ligne"), button -> {
            HudLine newLine = new HudLine("line_" + (lines.size() + 1), "{fps}", 10, 10 + lines.size() * 10);
            lines.add(newLine);
            this.clearWidgets();
            this.init();
        }).bounds(10, y + 10, 150, 20).build();
        this.addRenderableWidget(addButton);

        Button saveButton = Button.builder(Component.literal("Sauvegarder & Fermer"), button -> {
            CustomoverlayClient.saveConfig();
            this.onClose();
        }).bounds(170, y + 10, 150, 20).build();
        this.addRenderableWidget(saveButton);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        for (int i = 0; i < previewLines.size(); i++) {
            int py = previewY.get(i);
            graphics.fill(335, py, 355, py + 18, previewLines.get(i).color);
            graphics.outline(335, py, 20, 18, 0xFFFFFFFF);
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
}