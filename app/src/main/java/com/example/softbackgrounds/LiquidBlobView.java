package com.example.softbackgrounds;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class LiquidBlobView extends GLSurfaceView {

    private final BlobRenderer renderer;

    public LiquidBlobView(Context context) {
        super(context);

        setEGLContextClientVersion(2);

        renderer = new BlobRenderer();
        setRenderer(renderer);

        // Continuous animation.
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        setPreserveEGLContextOnPause(true);
    }

    // Safe to call from your audio-analysis thread.
    public void setAudioData(float bass, float mid, float treble) {
        renderer.setAudioData(bass, mid, treble);
    }

    public void setBlobColors(
            float[] color1, float[] color2, float[] color3) {
        renderer.setBlobColors(color1, color2, color3);
    }

    private static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static final class BlobRenderer
            implements GLSurfaceView.Renderer {

        private static final String VERTEX =
                "attribute vec2 aPosition;" +
                        "varying vec2 vUv;" +
                        "void main() {" +
                        "  vUv = aPosition * 0.5 + 0.5;" +
                        "  gl_Position = vec4(aPosition, 0.0, 1.0);" +
                        "}";

        private static final String FRAGMENT =
                "precision mediump float;" +
                        "varying vec2 vUv;" +
                        "uniform vec2 uResolution;" +
                        "uniform float uTime;" +
                        "uniform float uBass;" +
                        "uniform float uMid;" +
                        "uniform float uTreble;" +
                        "uniform vec3 uColor1;" +
                        "uniform vec3 uColor2;" +
                        "uniform vec3 uColor3;" +

                        "float hash(vec2 p) {" +
                        " p = fract(p * vec2(123.34, 456.21));" +
                        " p += dot(p, p + 45.32);" +
                        " return fract(p.x * p.y);" +
                        "}" +

                        "float noise(vec2 p) {" +
                        " vec2 i = floor(p);" +
                        " vec2 f = fract(p);" +
                        " f = f*f*(3.0-2.0*f);" +
                        " return mix(" +
                        "  mix(hash(i), hash(i+vec2(1.0,0.0)), f.x)," +
                        "  mix(hash(i+vec2(0.0,1.0))," +
                        "      hash(i+vec2(1.0,1.0)), f.x), f.y);" +
                        "}" +

                        "void main() {" +
                        " vec2 p = vUv - 0.5;" +
                        " p.x *= uResolution.x / uResolution.y;" +

                        " float t = uTime;" +
                        " float flow = 0.035 + uMid*0.025;" +
                        " float n1 = noise(p*3.0 + vec2(t*.12,-t*.09));" +
                        " float n2 = noise(p*4.0 + vec2(-t*.08,t*.11));" +

                        " vec2 q = p;" +
                        " q += (vec2(n1,n2)-0.5) *" +
                        "      (0.12 + uBass*0.10);" +

                        " vec2 c1 = vec2(" +
                        "  0.28*sin(t*0.35)," +
                        "  0.12*cos(t*0.31));" +
                        " vec2 c2 = vec2(" +
                        " -0.25*cos(t*0.27)," +
                        "  0.19*sin(t*0.32));" +
                        " vec2 c3 = vec2(" +
                        "  0.12*sin(t*0.22+2.0)," +
                        " -0.23*cos(t*0.29));" +

                        " float pulse = 1.0 + uBass*0.20;" +
                        " float r1 = 0.29*pulse;" +
                        " float r2 = 0.27*(1.0+uMid*0.12);" +
                        " float r3 = 0.25*(1.0+uTreble*0.08);" +

                        " float d1 = length(q-c1);" +
                        " float d2 = length(q-c2);" +
                        " float d3 = length(q-c3);" +

                        " d1 += flow*sin(q.y*7.0+t*0.8);" +
                        " d2 += flow*cos(q.x*6.0-t*0.7);" +
                        " d3 += flow*sin((q.x+q.y)*5.0+t*0.6);" +

                        // Metaball influence field.
                        " float f1 = r1*r1/(d1*d1+0.018);" +
                        " float f2 = r2*r2/(d2*d2+0.018);" +
                        " float f3 = r3*r3/(d3*d3+0.018);" +
                        " float field = f1+f2+f3;" +

                        // Color follows the influence of each blob.
                        " vec3 col = (" +
                        "  uColor1*f1 + uColor2*f2 + uColor3*f3)" +
                        "  / max(field,0.001);" +

                        // Add gentle internal gradient variation.
                        " float shade = 0.84 + 0.16*" +
                        "  sin(field*1.5-t*0.35);" +
                        " col *= shade;" +

                        " float edge = smoothstep(0.30, 0.95, field);" +
                        " float glow = exp(-max(0.0,1.0-field)*2.0);" +
                        " col += col*glow*(0.08+uBass*0.12);" +

                        " vec3 bg = vec3(0.012,0.008,0.035);" +
                        " vec3 finalColor = mix(bg,col,edge);" +

                        " gl_FragColor = vec4(finalColor,1.0);" +
                        "}";

        private final float[] vertices = {
                -1f,-1f,  1f,-1f,
                -1f, 1f,  1f, 1f
        };

        private final FloatBuffer vertexBuffer;

        private int program;
        private int positionHandle;
        private int resolutionHandle;
        private int timeHandle;
        private int bassHandle;
        private int midHandle;
        private int trebleHandle;
        private int color1Handle;
        private int color2Handle;
        private int color3Handle;

        private int width, height;
        private long startNs;

        private volatile float targetBass;
        private volatile float targetMid;
        private volatile float targetTreble;

        private float bass, mid, treble;

        private final float[] color1 = {0.05f, 0.85f, 1f};
        private final float[] color2 = {0.65f, 0.12f, 1f};
        private final float[] color3 = {1f, 0.12f, 0.48f};

        BlobRenderer() {
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    vertices.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(vertices);
            vertexBuffer.position(0);
        }

        void setAudioData(float b, float m, float tr) {
            targetBass = clamp(b);
            targetMid = clamp(m);
            targetTreble = clamp(tr);
        }

        synchronized void setBlobColors(
                float[] c1, float[] c2, float[] c3) {
            if (c1.length < 3 || c2.length < 3 ||
                    c3.length < 3) return;

            for (int i=0; i<3; i++) {
                color1[i] = clamp(c1[i]);
                color2[i] = clamp(c2[i]);
                color3[i] = clamp(c3[i]);
            }
        }

        @Override
        public void onSurfaceCreated(
                javax.microedition.khronos.opengles.GL10 gl,
                javax.microedition.khronos.egl.EGLConfig config) {

            GLES20.glClearColor(0f,0f,0f,1f);
            program = createProgram(VERTEX, FRAGMENT);

            positionHandle = GLES20.glGetAttribLocation(
                    program,"aPosition");
            resolutionHandle = GLES20.glGetUniformLocation(
                    program,"uResolution");
            timeHandle = GLES20.glGetUniformLocation(
                    program,"uTime");
            bassHandle = GLES20.glGetUniformLocation(
                    program,"uBass");
            midHandle = GLES20.glGetUniformLocation(
                    program,"uMid");
            trebleHandle = GLES20.glGetUniformLocation(
                    program,"uTreble");
            color1Handle = GLES20.glGetUniformLocation(
                    program,"uColor1");
            color2Handle = GLES20.glGetUniformLocation(
                    program,"uColor2");
            color3Handle = GLES20.glGetUniformLocation(
                    program,"uColor3");

            startNs = System.nanoTime();
        }

        @Override
        public void onSurfaceChanged(
                javax.microedition.khronos.opengles.GL10 gl,
                int w, int h) {
            width = w;
            height = h;
            GLES20.glViewport(0,0,w,h);
        }

        @Override
        public void onDrawFrame(
                javax.microedition.khronos.opengles.GL10 gl) {

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glUseProgram(program);

            // Smooth audio changes on the render thread.
            bass += (targetBass-bass)*0.12f;
            mid += (targetMid-mid)*0.08f;
            treble += (targetTreble-treble)*0.06f;

            float time = (System.nanoTime()-startNs)
                    / 1_000_000_000f;

            GLES20.glUniform2f(
                    resolutionHandle,width,height);
            GLES20.glUniform1f(timeHandle,time);
            GLES20.glUniform1f(bassHandle,bass);
            GLES20.glUniform1f(midHandle,mid);
            GLES20.glUniform1f(trebleHandle,treble);

            synchronized (this) {
                GLES20.glUniform3fv(
                        color1Handle,1,color1,0);
                GLES20.glUniform3fv(
                        color2Handle,1,color2,0);
                GLES20.glUniform3fv(
                        color3Handle,1,color3,0);
            }

            vertexBuffer.position(0);
            GLES20.glEnableVertexAttribArray(positionHandle);
            GLES20.glVertexAttribPointer(
                    positionHandle,2,GLES20.GL_FLOAT,
                    false,0,vertexBuffer);

            GLES20.glDrawArrays(
                    GLES20.GL_TRIANGLE_STRIP,0,4);

            GLES20.glDisableVertexAttribArray(positionHandle);
        }

        private static int compileShader(int type,String source) {
            int shader = GLES20.glCreateShader(type);
            GLES20.glShaderSource(shader,source);
            GLES20.glCompileShader(shader);

            int[] status = new int[1];
            GLES20.glGetShaderiv(
                    shader,GLES20.GL_COMPILE_STATUS,status,0);

            if (status[0] == 0) {
                String log = GLES20.glGetShaderInfoLog(shader);
                GLES20.glDeleteShader(shader);
                throw new RuntimeException(
                        "Shader compilation failed: " + log);
            }
            return shader;
        }

        private static int createProgram(
                String vertex,String fragment) {
            int vs = compileShader(
                    GLES20.GL_VERTEX_SHADER,vertex);
            int fs = compileShader(
                    GLES20.GL_FRAGMENT_SHADER,fragment);

            int p = GLES20.glCreateProgram();
            GLES20.glAttachShader(p,vs);
            GLES20.glAttachShader(p,fs);
            GLES20.glLinkProgram(p);

            int[] status = new int[1];
            GLES20.glGetProgramiv(
                    p,GLES20.GL_LINK_STATUS,status,0);

            GLES20.glDeleteShader(vs);
            GLES20.glDeleteShader(fs);

            if (status[0] == 0) {
                String log = GLES20.glGetProgramInfoLog(p);
                GLES20.glDeleteProgram(p);
                throw new RuntimeException(
                        "Program linking failed: " + log);
            }
            return p;
        }
    }
}

