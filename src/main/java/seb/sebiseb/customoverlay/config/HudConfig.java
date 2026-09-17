package seb.sebiseb.customoverlay.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class HudConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public List<HudLine> lines = new ArrayList<>();

    public static HudConfig createDefault() {
        HudConfig config = new HudConfig();
        config.lines.add(new HudLine("fps", "FPS: {fps}", 10, 10));
        config.lines.add(new HudLine("coords", "XYZ: {x} / {y} / {z}", 10, 20));
        config.lines.add(new HudLine("facing", "Facing: {facing}", 10, 30));
        config.lines.add(new HudLine("biome", "Biome: {biome}", 10, 40));
        config.lines.add(new HudLine("dimension", "Dimension: {dimension}", 10, 50));
        config.lines.add(new HudLine("time", "Time: {time}", 10, 60));
        config.lines.add(new HudLine("nether_conv", "Chunk: {chunk_x}/{chunk_z} local: {local_x}/{local_z}", 10, 70));
        config.lines.add(new HudLine("dim_conv", "{other_dim}: {conv_x} / {conv_z}", 10, 80));
        return config;
    }

    public static HudConfig load(Path path) {
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                HudConfig loaded = GSON.fromJson(reader, HudConfig.class);
                if (loaded != null && loaded.lines != null) {
                    return loaded;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        HudConfig fresh = createDefault();
        fresh.save(path);
        return fresh;
    }

    public void save(Path path) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}