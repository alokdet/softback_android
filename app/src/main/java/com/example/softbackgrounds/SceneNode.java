package com.example.softbackgrounds;

import java.util.ArrayList;
import java.util.List;

/**
 * SLDF scene node.
 *
 * The model intentionally keeps Figma concepts separate from Android rendering
 * concepts. The renderer can therefore evolve without changing the wire format.
 */
public class SceneNode {

    // Identity
    public String id;
    public String type;
    public String name;

    // Backward-compatible geometry fields.
    public float x;
    public float y;
    public float width;
    public float height;

    // Preferred transform representation. Values are the 2D affine matrix:
    // [a c tx]
    // [b d ty]
    // [0 0 1 ]
    public Transform transform;

    // Legacy transform fields retained for v1 documents and existing runtime code.
    public float rotation;
    public float scaleX = 1f;
    public float scaleY = 1f;

    // Appearance
    public float opacity = 1f;
    public boolean visible = true;
    /** Legacy resolved solid color used by v1 renderer paths. */
    public int color = 0;
    public String blendMode;

    // Paints. `fill` remains for v1 compatibility.
    public List<PaintDef> fills = new ArrayList<>();
    public PaintDef fill;
    public List<PaintDef> strokes = new ArrayList<>();
    public StrokeDef stroke;

    // Effects
    public List<EffectDef> effects = new ArrayList<>();

    // Geometry
    public CornerRadiusDef corners;
    public Float cornerRadius;
    public boolean clipsContent;
    public boolean isMask;
    public VectorGeometry vector;
    public int pointCount = 0;
    public float innerRadius = 0f;
    public String booleanOperation;

    // Layout
    public LayoutDef layout;

    // Text. Legacy fields remain so existing code keeps working.
    public String text;
    public float fontSize;
    public TextDef textStyle;

    // Assets
    public ImageDef image;

    // Prototype / interaction data will be added in the next phase.

    // Child nodes
    public final List<SceneNode> children = new ArrayList<>();

    public SceneNode() {
    }

    public void addChild(SceneNode child) {
        children.add(child);
    }

    public static class Transform {
        public float a = 1f;
        public float b = 0f;
        public float c = 0f;
        public float d = 1f;
        public float tx = 0f;
        public float ty = 0f;
    }

    public static class PaintDef {
        public String type;
        public FillColor color;
        public float opacity = 1f;

        // Gradient data.
        public List<GradientStop> gradientStops = new ArrayList<>();
        public List<GradientHandle> gradientHandlePositions = new ArrayList<>();
        // Optional 2x3 affine gradient transform exported by newer Figma APIs.
        // Stored as [a,b,c,d,tx,ty].
        public float[] gradientTransform;

        // Image paint data.
        public String assetId;
        public String scaleMode;
        // [a,b,c,d,tx,ty], preserving Figma's 2D image transform.
        public float[] imageTransform;
    }

    /** @deprecated Use PaintDef. Kept to deserialize existing v1 documents. */
    @Deprecated
    public static class Fill extends PaintDef {
    }

    public static class FillColor {
        public float r;
        public float g;
        public float b;
    }

    public static class GradientStop {
        public float position;
        public FillColor color;
        public float opacity = 1f;
    }

    public static class GradientHandle {
        public float x;
        public float y;
    }

    public static class StrokeDef {
        public float weight = 1f;
        public String align;
        public String cap;
        public String join;
        public float[] dashPattern;
    }

    public static class EffectDef {
        public String type;
        public FillColor color;
        public float opacity = 1f;
        public float offsetX;
        public float offsetY;
        public float radius;
        public float spread;
    }

    public static class CornerRadiusDef {
        public float topLeft;
        public float topRight;
        public float bottomRight;
        public float bottomLeft;
    }

    public static class VectorGeometry {
        public List<VectorPath> paths = new ArrayList<>();
    }

    public static class VectorPath {
        public String data;
        public String windingRule;
    }

    public static class LayoutDef {
        public String mode;
        public String wrap;
        public float paddingTop;
        public float paddingRight;
        public float paddingBottom;
        public float paddingLeft;
        public float itemSpacing;
        public float counterAxisSpacing;
        public String primaryAxisAlignItems;
        public String counterAxisAlignItems;
        public String primaryAxisSizingMode;
        public String counterAxisSizingMode;
        public float layoutGrow;
        public String layoutAlign;
        public boolean absolutePositioning;
        public boolean strokesIncludedInLayout;
    }

    public static class TextDef {
        public String characters;
        public String fontFamily;
        public String fontStyle;
        public float fontSize;
        public String textAlignHorizontal;
        public String textAlignVertical;
        public String textCase;
        public String textDecoration;
        public float letterSpacing;
        public String letterSpacingUnit;
        public float lineHeight;
        public String lineHeightUnit;
        public float paragraphSpacing;
        public String textAutoResize;
        public List<TextRun> runs = new ArrayList<>();
        public float paragraphIndent;
        public float hangingPunctuation;
    }

    public static class TextRun {
        public int start;
        public int end;
        public String fontFamily;
        public String fontStyle;
        public float fontSize;
        public String textCase;
        public String textDecoration;
        public float letterSpacing;
        public String letterSpacingUnit;
        public float lineHeight;
        public String lineHeightUnit;
        public List<PaintDef> fills = new ArrayList<>();
        public String hyperlink;
    }

    public static class ImageDef {
        public String assetId;
        public String scaleMode;
        // [a,b,c,d,tx,ty], preserving Figma's 2D image transform.
        public float[] imageTransform;
    }
}
