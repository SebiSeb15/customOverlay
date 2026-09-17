package seb.sebiseb.customoverlay.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public class PlaceholderResolver {

    public static String resolve(String template, Minecraft client) {
        String result = template;

        result = result.replace("{fps}", String.valueOf(client.getFps()));

        if (client.player != null) {
            BlockPos pos = client.player.blockPosition();
            result = result.replace("{x}", String.valueOf(pos.getX()));
            result = result.replace("{y}", String.valueOf(pos.getY()));
            result = result.replace("{z}", String.valueOf(pos.getZ()));
            result = result.replace("{facing}", client.player.getDirection().getName());

            result = result.replace("{local_x}", String.valueOf(pos.getX() & 15));
            result = result.replace("{local_z}", String.valueOf(pos.getZ() & 15));

            result = result.replace("{chunk_x}", String.valueOf(pos.getX() >> 4));
            result = result.replace("{chunk_z}", String.valueOf(pos.getZ() >> 4));

            if (client.level != null) {
                Holder<Biome> biomeHolder = client.level.getBiome(pos);
                String biomeName = biomeHolder.unwrapKey()
                        .map(key -> key.identifier().getPath())
                        .orElse("unknown");
                result = result.replace("{biome}", biomeName);

                ResourceKey<Level> dimKey = client.level.dimension();
                int convX, convZ;
                String otherDimName;

                if (dimKey.equals(Level.OVERWORLD)) {
                    convX = pos.getX() / 8;
                    convZ = pos.getZ() / 8;
                    otherDimName = "nether";
                } else if (dimKey.equals(Level.NETHER)) {
                    convX = pos.getX() * 8;
                    convZ = pos.getZ() * 8;
                    otherDimName = "overworld";
                } else {
                    convX = 0;
                    convZ = 0;
                    otherDimName = "n/a";
                }

                result = result.replace("{other_dim}", otherDimName);
                result = result.replace("{conv_x}", String.valueOf(convX));
                result = result.replace("{conv_z}", String.valueOf(convZ));
            } else {
                result = result.replace("{biome}", "?");
            }
        } else {
            result = result.replace("{x}", "?").replace("{y}", "?").replace("{z}", "?")
                    .replace("{facing}", "?").replace("{biome}", "?")
                    .replace("{local_x}", "?").replace("{local_z}", "?")
                    .replace("{chunk_x}", "?").replace("{chunk_z}", "?")
                    .replace("{other_dim}", "?").replace("{conv_x}", "?").replace("{conv_z}", "?");
        }

        if (client.level != null) {
            result = result.replace("{dimension}", client.level.dimension().identifier().getPath());
            result = result.replace("{time}", String.valueOf(Math.floorMod(client.level.getDefaultClockTime(), 24000L)));
        } else {
            result = result.replace("{dimension}", "?").replace("{time}", "?");
        }

        return result;
    }
}
