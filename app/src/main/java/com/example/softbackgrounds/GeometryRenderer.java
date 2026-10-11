package com.example.softbackgrounds;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;

/** Converts SLDF geometry into Android Paths. */
public final class GeometryRenderer {
    private GeometryRenderer() {}

    public static Path createPath(SceneNode node) {
        if (node == null) return null;
        String type = node.type == null ? "" : node.type.toLowerCase();
        if ("vector".equals(type) && node.vector != null) return createVectorPath(node);
        if ("booleanoperation".equals(type)) return createBooleanPath(node);

        float w = node.width, h = node.height;
        Path path = new Path();
        switch (type) {
            case "rectangle": case "frame": case "group": case "component":
            case "componentset": case "instance": case "section":
                addRoundedRect(path, node, w, h); break;
            case "ellipse": path.addOval(new RectF(0,0,w,h), Path.Direction.CW); break;
            case "line": path.moveTo(0,h/2f); path.lineTo(w,h/2f); break;
            case "polygon": addRegularPolygon(path,w/2f,h/2f,Math.min(w,h)/2f,node.pointCount>2?node.pointCount:6); break;
            case "star": addStar(path,w/2f,h/2f,Math.min(w,h)/2f,starInner(node),node.pointCount>2?node.pointCount:5); break;
            default: return null;
        }
        return path;
    }

    private static Path createVectorPath(SceneNode node) {
        Path result = new Path();
        if (node.vector.paths == null) return result;
        for (SceneNode.VectorPath vp : node.vector.paths) {
            Path p = SvgPathParser.parse(vp.data);
            if ("evenodd".equalsIgnoreCase(vp.windingRule)) p.setFillType(Path.FillType.EVEN_ODD);
            result.addPath(p);
        }
        return result;
    }

    private static Path createBooleanPath(SceneNode node) {
        if (node.children == null || node.children.isEmpty()) return null;
        Path result = transformedPath(node.children.get(0));
        for (int i=1;i<node.children.size();i++) {
            Path next = transformedPath(node.children.get(i));
            if (next == null) continue;
            Path.Op op = booleanOp(node.booleanOperation);
            if (!result.op(next, op)) return result;
        }
        return result;
    }

    private static Path transformedPath(SceneNode child) {
        Path p = createPath(child);
        if (p == null) return null;
        if (child.transform != null) {
            Matrix m = new Matrix();
            m.setValues(new float[]{child.transform.a,child.transform.c,child.transform.tx, child.transform.b,child.transform.d,child.transform.ty, 0,0,1});
            p.transform(m);
        }
        return p;
    }

    private static Path.Op booleanOp(String operation) {
        if ("subtract".equalsIgnoreCase(operation)) return Path.Op.DIFFERENCE;
        if ("intersect".equalsIgnoreCase(operation)) return Path.Op.INTERSECT;
        if ("exclude".equalsIgnoreCase(operation)) return Path.Op.XOR;
        return Path.Op.UNION;
    }

    private static float starInner(SceneNode node) {
        if (node.innerRadius > 0f && node.width > 0f) return node.innerRadius;
        return Math.min(node.width,node.height)*0.22f;
    }

    private static void addRoundedRect(Path path, SceneNode node, float w, float h) {
        if (node.corners != null) {
            float[] r={node.corners.topLeft,node.corners.topLeft,node.corners.topRight,node.corners.topRight,node.corners.bottomRight,node.corners.bottomRight,node.corners.bottomLeft,node.corners.bottomLeft};
            path.addRoundRect(new RectF(0,0,w,h),r,Path.Direction.CW);
        } else { float r=node.cornerRadius==null?0f:node.cornerRadius; path.addRoundRect(new RectF(0,0,w,h),r,r,Path.Direction.CW); }
    }
    private static void addRegularPolygon(Path path,float cx,float cy,float radius,int sides){
        for(int i=0;i<sides;i++){double a=-Math.PI/2d+i*Math.PI*2d/sides;float x=cx+(float)Math.cos(a)*radius,y=cy+(float)Math.sin(a)*radius;if(i==0)path.moveTo(x,y);else path.lineTo(x,y);}path.close();
    }
    private static void addStar(Path path,float cx,float cy,float outer,float inner,int points){
        for(int i=0;i<points*2;i++){double a=-Math.PI/2d+i*Math.PI/points;float r=i%2==0?outer:inner,x=cx+(float)Math.cos(a)*r,y=cy+(float)Math.sin(a)*r;if(i==0)path.moveTo(x,y);else path.lineTo(x,y);}path.close();
    }
    public static void drawFill(Canvas canvas,Path path,SceneNode node,PaintRendererFacade facade){facade.drawFills(canvas,path,node);}
    public interface PaintRendererFacade { void drawFills(Canvas canvas,Path path,SceneNode node); }
}
