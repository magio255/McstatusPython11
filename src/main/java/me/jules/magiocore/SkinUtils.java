package me.jules.magiocore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class SkinUtils {
    private static final Random random = new Random();
    private static final String[] DEFAULT_SKINS = {
        "http://textures.minecraft.net/texture/316314f4e92e2124584e036b5391e457f9273573752c00227187e14f85e4922e", // Steve
        "http://textures.minecraft.net/texture/c234a9493976865239e17b32402166687ef3e3d1eb3f678972590209673", // Alex
        "http://textures.minecraft.net/texture/d6228399e52514104e1a0694119864f1d4f4e2b0200889c256a00d8324f99767", // Programmer Art Steve
        "http://textures.minecraft.net/texture/14878a87693d256875880f089f280a52097960309e32a68be052445b9ec8b746", // Programmer Art Alex
        "http://textures.minecraft.net/texture/cd092e071e62688758364f33b1e326c547847c2090b8f418d18471c6f39386d3", // Ari
        "http://textures.minecraft.net/texture/2656911c0f64c67923485c2c77f0a8d4624b5849968434861b054238e821", // Efe
        "http://textures.minecraft.net/texture/a59074068564a50d268d875323e20ec42217c9b0e27f05c3176669c64639918c", // Kai
        "http://textures.minecraft.net/texture/8816c8053c0762a193630f576e828a2a5df679ef49877b47000e3032541c6f8f", // Makena
        "http://textures.minecraft.net/texture/d6332a62886f443b791409f87f87961b17b2b7336f32819a5585f67b5853f92a", // Noor
        "http://textures.minecraft.net/texture/6638069a5316315c613e54c8789d6e499d63c5d6f461e71239c063c87e7952a", // Sunny
        "http://textures.minecraft.net/texture/d63ea00787e9140f7d5a5745166299b828a7e029c73087ef63567b5853f92a" // Zuri
    };

    public static CompletableFuture<List<Component>> getHeadRows(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL skinUrl = player.getPlayerProfile().getTextures().getSkin();
                if (skinUrl == null) {
                    return getInternalHeadRows(new URL(DEFAULT_SKINS[random.nextInt(DEFAULT_SKINS.length)]));
                }
                return getInternalHeadRows(skinUrl);
            } catch (Exception e) {
                try {
                    return getInternalHeadRows(new URL(DEFAULT_SKINS[random.nextInt(DEFAULT_SKINS.length)]));
                } catch (Exception ex) {
                    return getFallbackSteveHead();
                }
            }
        });
    }

    private static List<Component> getInternalHeadRows(URL url) throws IOException {
        BufferedImage image = ImageIO.read(url);
        List<Component> rows = new ArrayList<>();

        for (int y = 8; y < 16; y++) {
            Component row = Component.empty();
            for (int x = 8; x < 16; x++) {
                int rgb = image.getRGB(x, y);

                // Hat layer support
                int overlayX = x + 32;
                int overlayY = y;
                int overlayRgb = image.getRGB(overlayX, overlayY);

                if (((overlayRgb >> 24) & 0xFF) > 0) {
                    rgb = overlayRgb;
                }

                row = row.append(Component.text("█").color(TextColor.color(rgb)));
            }
            rows.add(row);
        }
        return rows;
    }

    private static List<Component> getFallbackSteveHead() {
        String[] steve = {
            "&#2d1e15████████",
            "&#2d1e15████████",
            "&#2d1e15█&#ae7c5d██████&#2d1e15█",
            "&#ae7c5d████████",
            "&#ae7c5d█&#ffffff█&#1a2d7c█&#ae7c5d██&#1a2d7c█&#ffffff█&#ae7c5d█",
            "&#ae7c5d███&#3e2718████",
            "&#ae7c5d██&#3e2718████&#ae7c5d██",
            "&#ae7c5d████████"
        };
        List<Component> rows = new ArrayList<>();
        for (String line : steve) {
            rows.add(FontUtils.parse(line));
        }
        return rows;
    }
}
