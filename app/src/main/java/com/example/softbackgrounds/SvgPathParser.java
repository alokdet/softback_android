package com.example.softbackgrounds;

import android.graphics.Path;

/** Small dependency-free SVG path parser for Figma vectorPath data. */
public final class SvgPathParser {
    private SvgPathParser() {}

    public static Path parse(String data) {
        Path out = new Path();
        if (data == null || data.trim().isEmpty()) return out;
        Parser p = new Parser(data);
        p.parse(out);
        return out;
    }

    private static final class Parser {
        final String s; int i; char cmd; float cx, cy, sx, sy; float lastCx, lastCy;
        Parser(String s) { this.s = s; }
        void parse(Path out) {
            while (true) {
                skip(); if (i >= s.length()) return;
                char ch = s.charAt(i);
                if (isCmd(ch)) { cmd = ch; i++; }
                else if (cmd == 0) { i++; continue; }
                switch (Character.toUpperCase(cmd)) {
                    case 'M': move(out); break; case 'L': line(out); break;
                    case 'H': horiz(out); break; case 'V': vert(out); break;
                    case 'C': cubic(out); break; case 'S': smoothCubic(out); break;
                    case 'Q': quad(out); break; case 'T': smoothQuad(out); break;
                    case 'A': arc(out); break; case 'Z': close(out); break;
                    default: skipUnknown();
                }
            }
        }
        void move(Path p) { float x=num(), y=num(); if (rel()) {x+=cx;y+=cy;} p.moveTo(x,y); cx=x;cy=y;sx=x;sy=y; cmd=rel()?'l':'L'; }
        void line(Path p) { while (hasNum()) {float x=num(),y=num();if(rel()){x+=cx;y+=cy;}p.lineTo(x,y);cx=x;cy=y;lastCx=lastCy=0;} }
        void horiz(Path p) { while(hasNum()){float x=num();if(rel())x+=cx;p.lineTo(x,cy);cx=x;lastCx=lastCy=0;} }
        void vert(Path p) { while(hasNum()){float y=num();if(rel())y+=cy;p.lineTo(cx,y);cy=y;lastCx=lastCy=0;} }
        void cubic(Path p) { while(hasNum()){float x1=num(),y1=num(),x2=num(),y2=num(),x=num(),y=num();if(rel()){x1+=cx;y1+=cy;x2+=cx;y2+=cy;x+=cx;y+=cy;}p.cubicTo(x1,y1,x2,y2,x,y);lastCx=x2;lastCy=y2;cx=x;cy=y;} }
        void smoothCubic(Path p) { while(hasNum()){float x2=num(),y2=num(),x=num(),y=num();if(rel()){x2+=cx;y2+=cy;x+=cx;y+=cy;}float x1=2*cx-lastCx,y1=2*cy-lastCy;p.cubicTo(x1,y1,x2,y2,x,y);lastCx=x2;lastCy=y2;cx=x;cy=y;} }
        void quad(Path p) { while(hasNum()){float x1=num(),y1=num(),x=num(),y=num();if(rel()){x1+=cx;y1+=cy;x+=cx;y+=cy;}p.quadTo(x1,y1,x,y);lastCx=x1;lastCy=y1;cx=x;cy=y;} }
        void smoothQuad(Path p) { while(hasNum()){float x=num(),y=num();if(rel()){x+=cx;y+=cy;}float x1=2*cx-lastCx,y1=2*cy-lastCy;p.quadTo(x1,y1,x,y);lastCx=x1;lastCy=y1;cx=x;cy=y;} }
        void arc(Path p) { while(hasNum()){float rx=num(),ry=num(),rot=num(),large=num(),sweep=num(),x=num(),y=num();if(rel()){x+=cx;y+=cy;}arcTo(p,rx,ry,rot,large!=0,sweep!=0,x,y);cx=x;cy=y;lastCx=lastCy=0;} }
        void close(Path p){p.close();cx=sx;cy=sy;lastCx=lastCy=0;if(i<s.length() && !isCmd(s.charAt(i))) skip();}
        boolean rel(){return Character.isLowerCase(cmd);}
        boolean hasNum(){skip();return i<s.length()&&!isCmd(s.charAt(i));}
        float num(){skip();int st=i;boolean dot=false,exp=false;if(i<s.length()&&(s.charAt(i)=='+'||s.charAt(i)=='-'))i++;while(i<s.length()){char c=s.charAt(i);if(Character.isDigit(c)){i++;continue;}if(c=='.'&&!dot&&!exp){dot=true;i++;continue;}if((c=='e'||c=='E')&&!exp){exp=true;i++;if(i<s.length()&&(s.charAt(i)=='+'||s.charAt(i)=='-'))i++;continue;}break;}try{return Float.parseFloat(s.substring(st,i));}catch(Exception e){return 0;}}
        void skip(){while(i<s.length()){char c=s.charAt(i);if(Character.isWhitespace(c)||c==',')i++;else break;}}
        void skipUnknown(){while(i<s.length()&&!isCmd(s.charAt(i)))i++;}
        static boolean isCmd(char c){return "MmZzLlHhVvCcSsQqTtAa".indexOf(c)>=0;}

        void arcTo(Path p,float rx,float ry,float angle,boolean large,boolean sweep,float x2,float y2){
            if (rx==0||ry==0){p.lineTo(x2,y2);return;}
            rx=Math.abs(rx);ry=Math.abs(ry);
            double phi=Math.toRadians(angle%360); double cos=Math.cos(phi),sin=Math.sin(phi);
            double dx=(cx-x2)/2,dy=(cy-y2)/2;
            double xp=cos*dx+sin*dy, yp=-sin*dx+cos*dy;
            double lam=(xp*xp)/(rx*rx)+(yp*yp)/(ry*ry); if(lam>1){double k=Math.sqrt(lam);rx*=k;ry*=k;}
            double rx2=rx*rx,ry2=ry*ry; double num=rx2*ry2-rx2*yp*yp-ry2*xp*xp; double den=rx2*yp*yp+ry2*xp*xp;
            double coef=(num<0?0:Math.sqrt(num/den))*(large==sweep?-1:1);
            double cxp=coef*(rx*yp/ry), cyp=coef*(-ry*xp/rx);
            double mx=(cx+x2)/2,my=(cy+y2)/2; double ccx=cos*cxp-sin*cyp+mx, ccy=sin*cxp+cos*cyp+my;
            double ux=(xp-cxp)/rx,uy=(yp-cyp)/ry,vx=(-xp-cxp)/rx,vy=(-yp-cyp)/ry;
            double start=Math.atan2(uy,ux), delta=Math.atan2(ux*vy-uy*vx,ux*vx+uy*vy);
            if(!sweep&&delta>0)delta-=Math.PI*2;if(sweep&&delta<0)delta+=Math.PI*2;
            int n=(int)Math.ceil(Math.abs(delta)/(Math.PI/2)); double step=delta/n;
            double t=start;
            for(int k=0;k<n;k++){double t2=t+step;double alpha=4.0/3.0*Math.tan((t2-t)/4.0);double x1=rx*Math.cos(t)-alpha*rx*Math.sin(t),y1=ry*Math.sin(t)+alpha*ry*Math.cos(t);double x2c=rx*Math.cos(t2)+alpha*rx*Math.sin(t2),y2c=ry*Math.sin(t2)-alpha*ry*Math.cos(t2);double ex=rx*Math.cos(t2),ey=ry*Math.sin(t2);float p1x=(float)(cos*x1-sin*y1+ccx),p1y=(float)(sin*x1+cos*y1+ccy),p2x=(float)(cos*x2c-sin*y2c+ccx),p2y=(float)(sin*x2c+cos*y2c+ccy),exf=(float)(cos*ex-sin*ey+ccx),eyf=(float)(sin*ex+cos*ey+ccy);p.cubicTo(p1x,p1y,p2x,p2y,exf,eyf);t=t2;}
        }
    }
}
