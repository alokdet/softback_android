package com.example.softbackgrounds;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RenderEffect;
import android.graphics.RenderNode;
import android.graphics.Shader;
import android.os.Build;

import java.util.Locale;

/**
 * Renders Figma layer effects.
 *
 * Android RenderEffect is a RenderNode/View feature, not a Paint feature.  The
 * effect pipeline therefore records the node content into a RenderNode and
 * draws that RenderNode back to the current hardware canvas.
 */
public final class EffectRenderer {
    private EffectRenderer() {}

    public interface ContentDrawer {
        void draw(Canvas canvas);
    }

    public static void drawEffects(Canvas canvas, SceneNode node, ContentDrawer drawer) {
        if (node == null || drawer == null || node.effects == null || node.effects.isEmpty()) {
            drawer.draw(canvas);
            return;
        }

        boolean hasDropShadow = false;
        SceneNode.EffectDef shadow = null;
        SceneNode.EffectDef blur = null;

        for (SceneNode.EffectDef effect : node.effects) {
            if (!isVisible(effect)) continue;
            String type = normalize(effect.type);
            if ("drop_shadow".equals(type) && shadow == null) {
                shadow = effect;
                hasDropShadow = true;
            } else if (("layer_blur".equals(type) || "background_blur".equals(type)) && blur == null) {
                blur = effect;
            }
        }

        // RenderEffect requires a hardware accelerated canvas and API 31+.
        // SoftCanvasView must therefore remain hardware accelerated on these devices.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && canvas.isHardwareAccelerated()) {
            if (hasDropShadow) {
                drawDropShadow(canvas, node, shadow, drawer);
            }

            if (blur != null && ("layer_blur".equals(normalize(blur.type))
                    || "background_blur".equals(normalize(blur.type)))) {
                drawBlurred(canvas, node, blur, drawer);
            } else {
                drawer.draw(canvas);
            }
            return;
        }

        // Software / older Android fallback.
        if (hasDropShadow) {
            drawSimpleShadowFallback(canvas, node, shadow);
        }
        drawer.draw(canvas);
    }

    private static void drawDropShadow(Canvas canvas, SceneNode node,
                                       SceneNode.EffectDef effect, ContentDrawer drawer) {
        float extra = Math.max(4f, effect.radius * 2f
                + Math.max(Math.abs(effect.offsetX), Math.abs(effect.offsetY)) + 4f);

        RenderNode shadowNode = recordNode(node, drawer, extra);
        if (shadowNode == null) {
            drawer.draw(canvas);
            return;
        }

        RenderEffect colorize = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            colorize = RenderEffect.createColorFilterEffect(shadowColorFilter(effect));
        }
        RenderEffect blur = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            blur = RenderEffect.createBlurEffect(
                    Math.max(0f, effect.radius),
                    Math.max(0f, effect.radius),
                    Shader.TileMode.CLAMP);
        }
        RenderEffect offset = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            offset = RenderEffect.createOffsetEffect(effect.offsetX, effect.offsetY);
        }

        // Source content -> colorize -> blur -> offset.
        RenderEffect chain = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            chain = RenderEffect.createChainEffect(
                    offset,
                    RenderEffect.createChainEffect(blur, colorize));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            shadowNode.setRenderEffect(chain);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            canvas.drawRenderNode(shadowNode);
        }
        drawer.draw(canvas);
    }

    private static void drawBlurred(Canvas canvas, SceneNode node,
                                    SceneNode.EffectDef effect, ContentDrawer drawer) {
        float radius = Math.max(0f, effect.radius);
        if (radius <= 0f) {
            drawer.draw(canvas);
            return;
        }

        float extra = Math.max(4f, radius * 2f + 4f);
        RenderNode blurNode = recordNode(node, drawer, extra);
        if (blurNode == null) {
            drawer.draw(canvas);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            blurNode.setRenderEffect(RenderEffect.createBlurEffect(
                    radius, radius, Shader.TileMode.CLAMP));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            canvas.drawRenderNode(blurNode);
        }
    }

    private static RenderNode recordNode(SceneNode node, ContentDrawer drawer, float extra) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null;

        int left = (int) Math.floor(-extra);
        int top = (int) Math.floor(-extra);
        int right = (int) Math.ceil(Math.max(0f, node.width) + extra);
        int bottom = (int) Math.ceil(Math.max(0f, node.height) + extra);

        int width = Math.max(1, right - left);
        int height = Math.max(1, bottom - top);

        RenderNode renderNode = new RenderNode("SoftBackEffect");
        renderNode.setPosition(left, top, right, bottom);

        Canvas recording = renderNode.beginRecording(width, height);
        recording.translate(-left, -top);
        drawer.draw(recording);
        renderNode.endRecording();
        return renderNode;
    }

    private static void drawSimpleShadowFallback(Canvas canvas, SceneNode node,
                                                  SceneNode.EffectDef effect) {
        android.graphics.Path path = GeometryRenderer.createPath(node);
        if (path == null) return;

        android.graphics.Paint shadow = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        shadow.setStyle(android.graphics.Paint.Style.FILL);
        shadow.setColor(shadowColor(effect));
        shadow.setShadowLayer(Math.max(0f, effect.radius),
                effect.offsetX, effect.offsetY, shadowColor(effect));
        canvas.drawPath(path, shadow);
    }

    private static ColorFilter shadowColorFilter(SceneNode.EffectDef effect) {
        return new PorterDuffColorFilter(shadowColor(effect), PorterDuff.Mode.SRC_IN);
    }

    private static int shadowColor(SceneNode.EffectDef effect) {
        if (effect == null || effect.color == null) return Color.BLACK;
        int a = Math.round(clamp01(effect.opacity) * 255f);
        return Color.argb(a,
                Math.round(clamp01(effect.color.r) * 255f),
                Math.round(clamp01(effect.color.g) * 255f),
                Math.round(clamp01(effect.color.b) * 255f));
    }

    private static boolean isVisible(SceneNode.EffectDef effect) {
        return effect != null && effect.opacity > 0f && effect.type != null;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.US).replace('-', '_');
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
