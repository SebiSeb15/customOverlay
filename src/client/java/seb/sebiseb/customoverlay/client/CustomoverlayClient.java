// =^ᴥ^=

package seb.sebiseb.customoverlay.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import seb.sebiseb.customoverlay.config.HudConfig;
import seb.sebiseb.customoverlay.config.HudLine;

import java.nio.file.Path;

public class CustomoverlayClient implements ClientModInitializer {

    public static HudConfig CONFIG;
    public static boolean overlayVisible = true;
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("customoverlay.json");

    @Override
    public void onInitializeClient() {
        CONFIG = HudConfig.load(CONFIG_PATH);

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("customoverlay", "main_overlay"),
                (graphics, tickCounter) -> {
                    if (!overlayVisible) return;
                    Minecraft client = Minecraft.getInstance();

                    int currentY = 2;

                    for (HudLine line : CONFIG.lines) {
                        if (!line.enabled) continue;

                        String text = PlaceholderResolver.resolve(line.template, client);
                        int lineHeight = (int) (client.font.lineHeight * line.scale) + 2;
                        int drawY = currentY + line.y;

                        if (line.scale != 1.0f) {
                            graphics.pose().pushMatrix();
                            graphics.pose().scale(line.scale, line.scale);
                            int scaledX = (int) (line.x / line.scale);
                            int scaledY = (int) (drawY / line.scale);
                            graphics.text(client.font, text, scaledX, scaledY, line.color, line.shadow);
                            graphics.pose().popMatrix();
                        } else {
                            graphics.text(client.font, text, line.x, drawY, line.color, line.shadow);
                        }

                        currentY = drawY + lineHeight;
                    }
                }
        );
        KeyMapping.Category CATEGORY = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("customoverlay", "main_category")
        );

        KeyMapping openConfigKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.customoverlay.open_config",
                        InputConstants.Type.KEYSYM,
                        InputConstants.KEY_O,
                        CATEGORY
                )
        );
        KeyMapping toggleOverlayKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.customoverlay.toggle_overlay",
                        InputConstants.Type.KEYSYM,
                        InputConstants.KEY_F4,
                        CATEGORY
                )
        );

        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigKey.consumeClick()) {
                client.setScreenAndShow(new seb.sebiseb.customoverlay.client.gui.CustomOverlayConfigScreen(client.gui.screen()));
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigKey.consumeClick()) {
                client.setScreenAndShow(new seb.sebiseb.customoverlay.client.gui.CustomOverlayConfigScreen(client.gui.screen()));
            }
            while (toggleOverlayKey.consumeClick()) {
                overlayVisible = !overlayVisible;
            }
        });
    }



    public static void saveConfig() {
        CONFIG.save(CONFIG_PATH);
    }
}

