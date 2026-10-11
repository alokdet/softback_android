package com.example.softbackgrounds;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Resolves Figma font family/style names to Android typefaces.
 *
 * Figma does not expose font binaries through the Plugin API, so exact fonts
 * must be supplied by the Android project (assets/fonts or assets). The
 * resolver automatically finds files such as Inter-Regular.ttf,
 * Roboto-Medium.ttf, Akshar-SemiBold.ttf, etc.
 */
public final class FontResolver {
    private static final Map<String, Typeface> CACHE = new HashMap<>();
    private static final List<String> FONT_ASSETS = new ArrayList<>();
    private static boolean indexed;

    private FontResolver() {}

    public static synchronized Typeface resolve(Context context, String family, String style) {
        String requestedFamily = family == null ? "" : family.trim();
        String requestedStyle = style == null ? "Regular" : style.trim();
        String cacheKey = normalize(requestedFamily) + "|" + normalize(requestedStyle);
        Typeface cached = CACHE.get(cacheKey);
        if (cached != null) return cached;

        if (context != null && !TextUtils.isEmpty(requestedFamily)) {
            ensureIndexed(context);
            String exact = findAsset(requestedFamily, requestedStyle);
            if (exact == null) exact = findAsset(requestedFamily, "Regular");
            if (exact != null) {
                try {
                    Typeface typeface = Typeface.createFromAsset(context.getAssets(), exact);
                    CACHE.put(cacheKey, typeface);
                    return typeface;
                } catch (RuntimeException ignored) {
                    // Fall through to Android/system resolution.
                }
            }
        }

        int flags = styleFlags(requestedStyle);
        Typeface fallback = Typeface.create(
                TextUtils.isEmpty(requestedFamily) ? "sans-serif" : requestedFamily,
                flags
        );
        CACHE.put(cacheKey, fallback);
        return fallback;
    }

    private static int styleFlags(String style) {
        String s = normalize(style);
        boolean italic = s.contains("italic") || s.contains("oblique");
        boolean bold = s.contains("bold") || s.contains("black") || s.contains("heavy")
                || s.contains("semibold") || s.contains("demibold") || s.contains("medium");
        if (bold && italic) return Typeface.BOLD_ITALIC;
        if (bold) return Typeface.BOLD;
        if (italic) return Typeface.ITALIC;
        return Typeface.NORMAL;
    }

    private static String findAsset(String family, String style) {
        String f = normalize(family);
        String st = normalize(style);
        String best = null;
        int bestScore = Integer.MIN_VALUE;

        for (String asset : FONT_ASSETS) {
            String file = asset.substring(asset.lastIndexOf('/') + 1);
            String base = file.substring(0, file.lastIndexOf('.'));
            String n = normalize(base);

            // Family must be represented in the filename. This intentionally
            // avoids accidentally using a different font family.
            if (!n.contains(f)) continue;

            int score = 0;
            if (n.equals(f + st) || n.equals(f + " " + st)) score += 100;
            if (n.contains(st)) score += 50;
            if ("regular".equals(st) && (n.contains("regular") || n.contains("normal"))) score += 40;
            if ("medium".equals(st) && n.contains("medium")) score += 40;
            if ("semibold".equals(st) && (n.contains("semibold") || n.contains("demibold"))) score += 40;
            if (st.contains("italic") && n.contains("italic")) score += 30;
            if (st.contains("bold") && n.contains("bold")) score += 30;
            if (n.endsWith(f)) score += 10;

            if (score > bestScore) {
                bestScore = score;
                best = asset;
            }
        }
        return best;
    }

    private static void ensureIndexed(Context context) {
        if (indexed) return;
        indexed = true;
        try {
            walk(context.getAssets(), "");
        } catch (IOException ignored) {
        }
        Collections.sort(FONT_ASSETS);
    }

    private static void walk(android.content.res.AssetManager assets, String directory) throws IOException {
        String[] entries = assets.list(directory);
        if (entries == null) return;
        for (String entry : entries) {
            String path = directory.length() == 0 ? entry : directory + "/" + entry;
            if (entry.toLowerCase(Locale.US).endsWith(".ttf")
                    || entry.toLowerCase(Locale.US).endsWith(".otf")) {
                FONT_ASSETS.add(path);
            } else {
                String[] children = assets.list(path);
                if (children != null && children.length > 0) walk(assets, path);
            }
        }
    }

    private static String normalize(String value) {
        if (value == null) return "";
        return value.toLowerCase(Locale.US)
                .replace("-", "")
                .replace("_", "")
                .replace(" ", "")
                .replace(".", "");
    }
}
