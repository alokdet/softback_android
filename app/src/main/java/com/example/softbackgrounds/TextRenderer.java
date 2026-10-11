package com.example.softbackgrounds;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.text.style.MetricAffectingSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import java.util.List;
import java.util.Locale;

/** Figma-oriented text layout/rendering using Android StaticLayout. */
public final class TextRenderer {
    private TextRenderer() {}

    public static void draw(Canvas canvas, SceneNode node) {
        draw(canvas, node, null);
    }

    public static void draw(Canvas canvas, SceneNode node, Context context) {
        if (node == null) return;
        SceneNode.TextDef style = node.textStyle;
        String value = style != null && style.characters != null ? style.characters : node.text;
        if (value == null || value.isEmpty()) return;

        float size = style != null && style.fontSize > 0 ? style.fontSize : node.fontSize;
        if (size <= 0) size = 16f;

        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        paint.setColor(resolveColor(node));
        paint.setTextSize(size);
        paint.setTypeface(FontResolver.resolve(
                context,
                style == null ? null : style.fontFamily,
                style == null ? null : style.fontStyle
        ));
        paint.setLetterSpacing(0f);

        String renderedText = applyTextCase(value, style == null ? null : style.textCase);
        SpannableString spannable = new SpannableString(renderedText);
        applyBaseSpans(spannable, style, paint, context);
        applyRuns(spannable, style, size, context);

        // Text can also have gradient fills. Do this after spans so a
        // ForegroundColorSpan does not replace the gradient shader.
        if (node.fills != null) {
            for (SceneNode.PaintDef fill : node.fills) {
                if (fill != null && fill.type != null
                        && fill.type.toLowerCase(Locale.US).startsWith("gradient_")) {
                    if (PaintRenderer.applyGradientShader(
                            paint, fill, new android.graphics.RectF(0, 0, node.width, node.height))) {
                        break;
                    }
                }
            }
        }

        int width = Math.max(1, Math.round(node.width));
        Layout.Alignment alignment = alignment(style == null ? null : style.textAlignHorizontal);

        float naturalLineHeight = naturalLineHeight(paint);
        float[] spacing = lineSpacing(style, size, naturalLineHeight);

        StaticLayout layout = StaticLayout.Builder.obtain(
                        spannable, 0, spannable.length(), paint, width)
                .setAlignment(alignment)
                .setIncludePad(false)
                .setLineSpacing(spacing[0], spacing[1])
                .setBreakStrategy(StaticLayout.BREAK_STRATEGY_HIGH_QUALITY)
                .setHyphenationFrequency(StaticLayout.HYPHENATION_FREQUENCY_NORMAL)
                .build();

        float y = verticalOffset(style == null ? null : style.textAlignVertical,
                node.height, layout.getHeight());
        canvas.save();
        canvas.translate(0f, y);
        layout.draw(canvas);
        canvas.restore();
    }

    private static float naturalLineHeight(TextPaint paint) {
        Paint.FontMetricsInt fm = paint.getFontMetricsInt();
        return Math.max(1f, fm.descent - fm.ascent);
    }

    /**
     * Figma PERCENT is based on font size, while StaticLayout's multiplier is
     * based on Android's natural font metrics. Convert between the two instead
     * of simply using PERCENT/100 as the multiplier.
     */
    private static float[] lineSpacing(SceneNode.TextDef style, float size, float natural) {
        if (style == null || style.lineHeightUnit == null || style.lineHeight <= 0f) {
            return new float[]{0f, 1f};
        }

        String unit = style.lineHeightUnit.toLowerCase(Locale.US);
        float target;
        if ("pixels".equals(unit)) {
            target = style.lineHeight;
        } else if ("percent".equals(unit) || "font_size_%".equals(unit)
                || "fontsize_%".equals(unit)) {
            target = size * style.lineHeight / 100f;
        } else {
            // AUTO uses the font's natural line height.
            return new float[]{0f, 1f};
        }

        if (target <= 0f) return new float[]{0f, 1f};
        return new float[]{0f, Math.max(0.01f, target / natural)};
    }

    private static void applyBaseSpans(SpannableString s, SceneNode.TextDef style,
                                       TextPaint paint, Context context) {
        if (style == null) return;
        if (style.textDecoration != null) {
            if ("underline".equalsIgnoreCase(style.textDecoration)) {
                s.setSpan(new UnderlineSpan(), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if ("strikethrough".equalsIgnoreCase(style.textDecoration)) {
                s.setSpan(new StrikethroughSpan(), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        if (style.letterSpacingUnit != null && "pixels".equalsIgnoreCase(style.letterSpacingUnit)) {
            paint.setLetterSpacing(style.fontSize > 0 ? style.letterSpacing / style.fontSize : 0f);
        } else if (style.letterSpacingUnit != null && "percent".equalsIgnoreCase(style.letterSpacingUnit)) {
            paint.setLetterSpacing(style.letterSpacing / 100f);
        }
        int baseColor = firstFillColor(style, null);
        if (baseColor != 0) {
            s.setSpan(new ForegroundColorSpan(baseColor), 0, s.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private static void applyRuns(SpannableString s, SceneNode.TextDef style,
                                  float baseSize, Context context) {
        if (style == null || style.runs == null) return;
        for (SceneNode.TextRun run : style.runs) {
            int start = Math.max(0, Math.min(s.length(), run.start));
            int end = Math.max(start, Math.min(s.length(), run.end));
            if (start >= end) continue;

            if (run.fontFamily != null || run.fontStyle != null) {
                Typeface typeface = FontResolver.resolve(context, run.fontFamily, run.fontStyle);
                s.setSpan(new TypefaceSpanCompat(typeface), start, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (run.fontSize > 0 && baseSize > 0) {
                s.setSpan(new RelativeSizeSpan(run.fontSize / baseSize), start, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (run.textDecoration != null) {
                if ("underline".equalsIgnoreCase(run.textDecoration)) {
                    s.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if ("strikethrough".equalsIgnoreCase(run.textDecoration)) {
                    s.setSpan(new StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            int color = firstFillColor(run.fills, null);
            if (color != 0) {
                s.setSpan(new ForegroundColorSpan(color), start, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (run.fontStyle != null) {
                String lower = run.fontStyle.toLowerCase(Locale.US);
                if (lower.contains("bold") || lower.contains("black") || lower.contains("heavy")
                        || lower.contains("semibold") || lower.contains("medium")) {
                    s.setSpan(new StyleSpan(lower.contains("italic")
                                    ? Typeface.BOLD_ITALIC : Typeface.BOLD),
                            start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (lower.contains("italic")) {
                    s.setSpan(new StyleSpan(Typeface.ITALIC), start, end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            if (run.letterSpacingUnit != null && run.letterSpacing != 0f && run.fontSize > 0) {
                float em = "percent".equalsIgnoreCase(run.letterSpacingUnit)
                        ? run.letterSpacing / 100f
                        : run.letterSpacing / run.fontSize;
                s.setSpan(new LetterSpacingSpan(em), start, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static final class TypefaceSpanCompat extends MetricAffectingSpan {
        private final Typeface typeface;
        TypefaceSpanCompat(Typeface typeface) { this.typeface = typeface; }
        @Override public void updateDrawState(TextPaint tp) { tp.setTypeface(typeface); }
        @Override public void updateMeasureState(TextPaint tp) { tp.setTypeface(typeface); }
    }

    private static final class LetterSpacingSpan extends MetricAffectingSpan {
        private final float value;
        LetterSpacingSpan(float value) { this.value = value; }
        @Override public void updateDrawState(TextPaint tp) { tp.setLetterSpacing(value); }
        @Override public void updateMeasureState(TextPaint tp) { tp.setLetterSpacing(value); }
    }

    private static Layout.Alignment alignment(String value) {
        if ("center".equalsIgnoreCase(value)) return Layout.Alignment.ALIGN_CENTER;
        if ("right".equalsIgnoreCase(value)) return Layout.Alignment.ALIGN_OPPOSITE;
        if ("justified".equalsIgnoreCase(value)) return Layout.Alignment.ALIGN_NORMAL;
        return Layout.Alignment.ALIGN_NORMAL;
    }

    private static float verticalOffset(String value, float boxHeight, int layoutHeight) {
        if ("center".equalsIgnoreCase(value)) return Math.max(0f, (boxHeight - layoutHeight) / 2f);
        if ("bottom".equalsIgnoreCase(value)) return Math.max(0f, boxHeight - layoutHeight);
        return 0f;
    }

    private static int resolveColor(SceneNode node) {
        if (node.fills != null) {
            for (SceneNode.PaintDef p : node.fills) {
                int c = firstFillColor(java.util.Collections.singletonList(p), null);
                if (c != 0) return c;
            }
        }
        return node.color != 0 ? node.color : Color.BLACK;
    }

    private static int firstFillColor(SceneNode.TextDef style, Integer fallback) {
        if (style != null && style.runs != null) {
            for (SceneNode.TextRun r : style.runs) {
                int c = firstFillColor(r.fills, fallback);
                if (c != 0) return c;
            }
        }
        return fallback == null ? 0 : fallback;
    }

    private static int firstFillColor(List<SceneNode.PaintDef> fills, Integer fallback) {
        if (fills != null) {
            for (SceneNode.PaintDef p : fills) {
                if (p != null && p.color != null && p.opacity > 0) {
                    return PaintRenderer.color(p.color, p.opacity);
                }
            }
        }
        return fallback == null ? 0 : fallback;
    }

    private static String applyTextCase(String value, String textCase) {
        if (textCase == null) return value;
        if ("uppercase".equalsIgnoreCase(textCase)) return value.toUpperCase(Locale.getDefault());
        if ("lower".equalsIgnoreCase(textCase)) return value.toLowerCase(Locale.getDefault());
        return value;
    }
}
