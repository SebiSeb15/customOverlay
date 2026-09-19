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
        config.lines.add(new HudLine("fps", "FPS: {fps}", 2, 0));
        config.lines.add(new HudLine("coords", "XYZ: {x} / {y} / {z}", 2, 0));        config.lines.add(new HudLine("facing", "Facing: {facing}", 2, 0));
        config.lines.add(new HudLine("biome", "Biome: {biome}", 2, 0));
        config.lines.add(new HudLine("dimension", "Dimension: {dimension}", 2, 0));
        config.lines.add(new HudLine("time", "Time: {time}", 2, 0));
        config.lines.add(new HudLine("chunk_relative", "Chunk: {chunk_x}/{chunk_z} local: {local_x}/{local_z}", 2, 0));
        config.lines.add(new HudLine("dim_conv", "{other_dim}: {conv_x} / {conv_z}", 2, 0));
        config.lines.add(new HudLine("entities", "E: {e_rendered}/{e_total}", 2, 0));
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