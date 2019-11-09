package com.rtr.dynamicIndia;

// added by me
import android.opengl.GLSurfaceView;              // SurfaceView with support of OpenGL
import android.opengl.GLES32;                     // OpenGL ES 3.2
import javax.microedition.khronos.opengles.GL10;  // OpenGL Extension for basic features of OpenGL ES
import javax.microedition.khronos.egl.EGLConfig;  // Embedded Graphics Library

import android.content.Context;                           // Context
import android.view.MotionEvent;                          // MotionEvent
import android.view.GestureDetector;                      // GestureDetector
import android.view.GestureDetector.OnGestureListener;    // OnGestureListener
import android.view.GestureDetector.OnDoubleTapListener;  // OnDoubleTapListener

import java.nio.ByteBuffer;   // ByteBuffer
import java.nio.ByteOrder;    // ByteOrder
import java.nio.FloatBuffer;  // FloatBuffer

import java.lang.Math;
import android.opengl.Matrix; // Matrix


public class GLESView extends GLSurfaceView implements GLSurfaceView.Renderer, OnGestureListener, OnDoubleTapListener {

    private final Context context;
    private GestureDetector gestureDetector;

    private int vertexShaderObject;
    private int fragmentShaderObject;
    private int shaderProgramObject;

    private int state = 0;

    private int[] vaoI = new int[1];
    private int[] vboPosI = new int[1];
    private int[] vboColI = new int[1];
    private int[] vaoN = new int[1];
    private int[] vboPosN = new int[1];
    private int[] vboColN = new int[1];
    private int[] vaoD = new int[1];
    private int[] vboPosD = new int[1];
    private int[] vboColD = new int[1];
    private int[] vaoi = new int[1];
    private int[] vboPosi = new int[1];
    private int[] vboColi = new int[1];
    private int[] vaoA = new int[1];
    private int[] vboPosA = new int[1];
    private int[] vboColA = new int[1];
    private int[] vaoPlane = new int[1];
    private int[] vboPosPlane = new int[1];
    private int[] vboColPlane = new int[1];
    private int mvpUniform;

    private float xOffsetI;
    private float xOffsetA;
    private float yOffsetI2;
    private float alphaD;
    private float yOffsetN;

    private float xOffsetPlane = 0.0f;
    private float yOffsetPlane = 0.0f;
    private float planeAngle = 0.0f;
    private float rotationAngle = 0.0f;

    private FloatBuffer colorBufferD;

    private float[] perspectiveProjectionMatrix = new float[16];

    private float letterThickness = 2.0f;
    private float speed = 0.1f;

    private float rO = 0.0f, gO = 0.0f, bO = 0.0f, rG = 0.0f, gG = 0.0f, bG = 0.0f;

	public GLESView(Context drawingContext) {
		super(drawingContext);
		context = drawingContext;

        setEGLContextClientVersion(3);   // highest supported 3.x
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); 

        gestureDetector = new GestureDetector(context, this, null, false);
        gestureDetector.setOnDoubleTapListener(this);
	}

    // handling 'onTouchEvent' is the most important
    // because it triggers all gesture and tap events
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // code
        int eventaction = event.getAction();  // not required for now
        if (!gestureDetector.onTouchEvent(event))
            super.onTouchEvent(event);
        return(true);
    }

    // abstract method from OnDoubleTapEventListener so must be implemented
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return(true);
    }

    // abstract method from OnDoubleTapListener so must be implemented
    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        // do not write anything here, because already written 'onDoubleTap'
        return(true);
    }

    // abstract method from OnDoubleTapListener so must be implemented
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return(true);
    }

    // abstract method from OnGestureListener so must implement
    @Override
    public boolean onDown(MotionEvent e) {
        // do not write anything here, because already written in 'onSingleTapConfirmed'
        return(true);
    }

    // abstract method from OnGestureListener so must be implemented
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return(true);
    }

    // abstract method from OnGestureListener so must be implemented
    @Override
    public void onLongPress(MotionEvent e) {
    }

    // abstract method from OnGestureListener so must be implemented
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        uninitialize();
        System.exit(0);
        return(true);
    }

    // abstract method from OnGestureListener so must be implemented
    @Override
    public void onShowPress(MotionEvent e) {

    }

    // abstract method from OnGestureListener so must be implemented
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return(true);
    }

    ///// implementation of GLSurfaceView.Renderer methods
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        String version = gl.glGetString(GL10.GL_VERSION);
        System.out.println("RTR: OpenGL Version: " + version);

        // version = gl.glGetString(GL10.GL_SHADING_LANGUAGE_VERSION);
        // System.out.println("RTR: Shading Language Version: " + version);

        String vendor = gl.glGetString(GL10.GL_VENDOR);
        System.out.println("RTR: Vendor: " + vendor);

        String renderer = gl.glGetString(GL10.GL_RENDERER);
        System.out.println("RTR: Renderer: " + renderer);

        initialize();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        resize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        display();
    }

    /////////// Rendering Functions //////////////////////////////////////////////////// 

    private void initialize() {

        float height = (float)Math.tan(45.0 / 2.0 / 180.0 * Math.PI) * 50.0f;
        float width = height * 3040.0f / 1440.0f;

        //// VERTEX SHADER ////////////////////////////////////////////////
        // create shader object
        vertexShaderObject = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);

        // shader source code
        final String vertexShaderSourceCode = String.format(
            "#version 320 es" +
            "\n" +
            "in vec4 vPosition;" +
            "in vec4 vColor;" +
            "uniform mat4 u_mvp_matrix;" +
            "out vec4 out_Color;" +
            "void main(void)" +
            "{" +
            "   gl_Position = u_mvp_matrix * vPosition;" +
            "   out_Color = vColor;" +
            "}"
        );

        // attach shader source code to shader object
        GLES32.glShaderSource(vertexShaderObject, vertexShaderSourceCode);

        // compile shader source code
        GLES32.glCompileShader(vertexShaderObject);

        // compilation errors
        int[] iShaderCompileStatus = new int[1];
        int[] iInfoLogLength = new int[1];
        String szInfo = null;

        GLES32.glGetShaderiv(vertexShaderObject, GLES32.GL_COMPILE_STATUS, iShaderCompileStatus, 0);

        if (iShaderCompileStatus[0] == GLES32.GL_FALSE) {
            GLES32.glGetShaderiv(vertexShaderObject, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0) {
                szInfo = GLES32.glGetShaderInfoLog(vertexShaderObject);
                System.out.println("RTR: Vertex Shader: " + szInfo);
                uninitialize();
                System.exit(0);
            }

        }

        //// FRAGMENT SHADER ////////////////////////////////////////////////
        // create shader object
        fragmentShaderObject = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);

        // shader source code
        final String fragmentShaderSourceCode = String.format(
            "#version 320 es" +
            "\n" +
            "precision highp float;" +
            "in vec4 out_Color;" +
            "out vec4 FragColor;" +
            "void main(void)" +
            "{" +
            "   FragColor = out_Color;" +
            "}"
        );

        // attach shader source code to shader object
        GLES32.glShaderSource(fragmentShaderObject, fragmentShaderSourceCode);

        // compile shader source code
        GLES32.glCompileShader(fragmentShaderObject);

        // compilation errors
        iShaderCompileStatus[0] = 0;
        iInfoLogLength[0] = 0;
        szInfo = null;

        GLES32.glGetShaderiv(fragmentShaderObject, GLES32.GL_COMPILE_STATUS, iShaderCompileStatus, 0);

        if (iShaderCompileStatus[0] == GLES32.GL_FALSE) {
            GLES32.glGetShaderiv(fragmentShaderObject, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0) {
                szInfo = GLES32.glGetShaderInfoLog(fragmentShaderObject);
                System.out.println("RTR: Fragment Shader: " + szInfo);
                uninitialize();
                System.exit(0);
            }
        }

        // create shader program object
        shaderProgramObject = GLES32.glCreateProgram();

        // attach vertex shader to shader program
        GLES32.glAttachShader(shaderProgramObject, vertexShaderObject);

        // attach fragment shader to shader program
        GLES32.glAttachShader(shaderProgramObject, fragmentShaderObject);

        // pre-linking binding to vertex attribute
        GLES32.glBindAttribLocation(shaderProgramObject, GLESMacros.AMC_ATTRIBUTE_POSITION, "vPosition");
        GLES32.glBindAttribLocation(shaderProgramObject, GLESMacros.AMC_ATTRIBUTE_COLOR, "vColor");

        // link the shader program
        GLES32.glLinkProgram(shaderProgramObject);

        // linking errors
        int[] iProgramLinkStatus = new int[1];
        iInfoLogLength[0] = 0;
        szInfo = null;

        GLES32.glGetProgramiv(shaderProgramObject, GLES32.GL_LINK_STATUS, iProgramLinkStatus, 0);
        if (iProgramLinkStatus[0] == GLES32.GL_FALSE)
        {
            GLES32.glGetProgramiv(shaderProgramObject, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0)
            {
                szInfo = GLES32.glGetShaderInfoLog(shaderProgramObject);
                System.out.println("RTR: Program Linking: " + szInfo);
                uninitialize();
                System.exit(0);
            }
        }

        // get unifrom locations
        mvpUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_mvp_matrix");

        // calculations related to letters
        float ltrWidth = (width * 2.0f) / 7.0f;
        float ltrHeight = (height * 2.0f) / 7.0f;

        // letters data
        final float[] verticesI = getCoordsI(-width + (ltrWidth * 1.0f), height - ltrHeight, ltrWidth, (5.0f * ltrHeight));
        final float[] verticesN = getCoordsN(-width + (ltrWidth * 2.0f), height - ltrHeight, ltrWidth, (5.0f * ltrHeight));
        final float[] verticesD = getCoordsD(-width + (ltrWidth * 3.0f), height - ltrHeight, ltrWidth, (5.0f * ltrHeight));
        final float[] verticesi = getCoordsI(-width + (ltrWidth * 4.0f), height - ltrHeight, ltrWidth, (5.0f * ltrHeight));
        final float[] verticesA = getCoordsA(-width + (ltrWidth * 5.0f), height - ltrHeight, ltrWidth, (5.0f * ltrHeight));

        final float[] verticesPlane = getCoordsPlane(-width - (ltrWidth / 2.0f), height, ltrWidth, 0);

        final float[] colorsI = new float[] {
            1.0f,         153.0f/256.0f, 51.0f/256.0f,
            1.0f,         153.0f/256.0f, 51.0f/256.0f,
            19.0f/256.0f, 136.0f/256.0f,  8.0f/256.0f,
            19.0f/256.0f, 136.0f/256.0f,  8.0f/256.0f
        };

        final float[] colorsN = new float[] {
            1.0f,         153.0f/256.0f, 51.0f/256.0f,
            1.0f,         153.0f/256.0f, 51.0f/256.0f,
            19.0f/256.0f, 136.0f/256.0f,  8.0f/256.0f,
            19.0f/256.0f, 136.0f/256.0f,  8.0f/256.0f,
            1.0f,         153.0f/256.0f, 51.0f/256.0f,
            1.0f,         153.0f/256.0f, 51.0f/256.0f,
            19.0f/256.0f, 136.0f/256.0f,  8.0f/256.0f,
            19.0f/256.0f, 136.0f/256.0f,  8.0f/256.0f,
            1.0f,         153.0f/256.0f, 51.0f/256.0f,
            1.0f,         153.0f/256.0f, 51.0f/256.0f,
            19.0f/256.0f, 136.0f/256.0f,  8.0f/256.0f,
            19.0f/256.0f, 136.0f/256.0f,  8.0f/256.0f
        };

        final float[] colorsD = new float[] {
            1.0f,         153.0f/256.0f, 51.0f/256.0f,
            1.0f,         153.0f/256.0f, 51.0f/256.0f,
            1.0f,         153.0f/256.0f, 51.0f/256.0f,
            1.0f,         153.0f/256.0f, 51.0f/256.0f,
            19.0f/256.0f, 136.0f/256.0f,  8.0f/256.0f,
            19.0f/256.0f, 136.0f/256.0f,  8.0f/256.0f,
            19.0f/256.0f, 136.0f/256.0f,  8.0f/256.0f,
            19.0f/256.0f, 136.0f/256.0f,  8.0f/256.0f,
            1.0f,         153.0f/256.0f, 51.0f/256.0f,
            1.0f,         153.0f/256.0f, 51.0f/256.0f,
            19.0f/256.0f, 136.0f/256.0f,  8.0f/256.0f,
            19.0f/256.0f, 136.0f/256.0f,  8.0f/256.0f,
            1.0f,         153.0f/256.0f, 51.0f/256.0f,
            1.0f,         153.0f/256.0f, 51.0f/256.0f,
            19.0f/256.0f, 136.0f/256.0f,  8.0f/256.0f,
            19.0f/256.0f, 136.0f/256.0f,  8.0f/256.0f
        };

        final float[] colorsA = new float[] {
            1.0f,         153.0f/256.0f, 51.0f/256.0f,
            1.0f,         153.0f/256.0f, 51.0f/256.0f,
            19.0f/256.0f, 136.0f/256.0f,  8.0f/256.0f,
            19.0f/256.0f, 136.0f/256.0f,  8.0f/256.0f,
            1.0f,         153.0f/256.0f, 51.0f/256.0f,
            1.0f,         153.0f/256.0f, 51.0f/256.0f,
            19.0f/256.0f, 136.0f/256.0f,  8.0f/256.0f,
            19.0f/256.0f, 136.0f/256.0f,  8.0f/256.0f,
            1.0f,         153.0f/256.0f, 51.0f/256.0f,
            1.0f,         153.0f/256.0f, 51.0f/256.0f,
            1.0f,                  1.0f,         1.0f,
            1.0f,                  1.0f,         1.0f,
            19.0f/256.0f, 136.0f/256.0f,  8.0f/256.0f,
            19.0f/256.0f, 136.0f/256.0f,  8.0f/256.0f
        };

        final float[] colorsPlane = new float[] {
            186.0f / 256.0f, 226.0f / 256.0f, 238.0f / 256.0f, 1.0f,
            186.0f / 256.0f, 226.0f / 256.0f, 238.0f / 256.0f, 1.0f,
            186.0f / 256.0f, 226.0f / 256.0f, 238.0f / 256.0f, 1.0f,
            186.0f / 256.0f, 226.0f / 256.0f, 238.0f / 256.0f, 1.0f,
            186.0f / 256.0f, 226.0f / 256.0f, 238.0f / 256.0f, 1.0f,
            186.0f / 256.0f, 226.0f / 256.0f, 238.0f / 256.0f, 1.0f,
            186.0f / 256.0f, 226.0f / 256.0f, 238.0f / 256.0f, 1.0f,
            186.0f / 256.0f, 226.0f / 256.0f, 238.0f / 256.0f, 1.0f,
            186.0f / 256.0f, 226.0f / 256.0f, 238.0f / 256.0f, 1.0f,
            186.0f / 256.0f, 226.0f / 256.0f, 238.0f / 256.0f, 1.0f,
            186.0f / 256.0f, 226.0f / 256.0f, 238.0f / 256.0f, 1.0f,
            186.0f / 256.0f, 226.0f / 256.0f, 238.0f / 256.0f, 1.0f,
            186.0f / 256.0f, 226.0f / 256.0f, 238.0f / 256.0f, 1.0f,
            186.0f / 256.0f, 226.0f / 256.0f, 238.0f / 256.0f, 1.0f,
            186.0f / 256.0f, 226.0f / 256.0f, 238.0f / 256.0f, 1.0f,
            186.0f / 256.0f, 226.0f / 256.0f, 238.0f / 256.0f, 1.0f,
             45.0f / 256.0f,  88.0f / 256.0f, 102.0f / 256.0f, 1.0f,
             45.0f / 256.0f,  88.0f / 256.0f, 102.0f / 256.0f, 1.0f,
             45.0f / 256.0f,  88.0f / 256.0f, 102.0f / 256.0f, 1.0f,
             45.0f / 256.0f,  88.0f / 256.0f, 102.0f / 256.0f, 1.0f,
             45.0f / 256.0f,  88.0f / 256.0f, 102.0f / 256.0f, 1.0f,
             45.0f / 256.0f,  88.0f / 256.0f, 102.0f / 256.0f, 1.0f,
             45.0f / 256.0f,  88.0f / 256.0f, 102.0f / 256.0f, 1.0f,
             45.0f / 256.0f,  88.0f / 256.0f, 102.0f / 256.0f, 1.0f,
             45.0f / 256.0f,  88.0f / 256.0f, 102.0f / 256.0f, 1.0f,
             45.0f / 256.0f,  88.0f / 256.0f, 102.0f / 256.0f, 1.0f,
             45.0f / 256.0f,  88.0f / 256.0f, 102.0f / 256.0f, 1.0f,
             45.0f / 256.0f,  88.0f / 256.0f, 102.0f / 256.0f, 1.0f,
             45.0f / 256.0f,  88.0f / 256.0f, 102.0f / 256.0f, 1.0f,
             45.0f / 256.0f,  88.0f / 256.0f, 102.0f / 256.0f, 1.0f,
            1.0f, 153.0f / 256.0f, 51.0f / 256.0f, 1.0f,
            1.0f, 153.0f / 256.0f, 51.0f / 256.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 0.0f,
            19.0f / 256.0f, 136.0f / 256.0f, 8.0f / 256.0f, 1.0f,
            19.0f / 256.0f, 136.0f / 256.0f, 8.0f / 256.0f, 0.0f,
        };

        ////////// create vao for letter I  /////////////////////////////////////////////////////
        
        GLES32.glGenVertexArrays(1, vaoI, 0);
        GLES32.glBindVertexArray(vaoI[0]);

        // position buffer for I
        GLES32.glGenBuffers(1, vboPosI, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboPosI[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(verticesI.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBuffer.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer positionBufferI = byteBuffer.asFloatBuffer();

        // 4. put data in this COOKED buffer
        positionBufferI.put(verticesI);

        // 5. set the array at 0th position of buffer
        positionBufferI.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, verticesI.length * 4, positionBufferI, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

        // color buffer for I
        GLES32.glGenBuffers(1, vboColI, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboColI[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBufferColor = ByteBuffer.allocateDirect(colorsI.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBufferColor.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer colorBufferI = byteBufferColor.asFloatBuffer();

        // 4. put data in this COOKED buffer
        colorBufferI.put(colorsI);

        // 5. set the array at 0th position of buffer
        colorBufferI.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, colorsI.length * 4, colorBufferI, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_COLOR, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_COLOR);

        //////////////////////////////////////////////////////////////////////////////////////////////////////

        ////////// create vao for letter N  /////////////////////////////////////////////////////
        
        GLES32.glGenVertexArrays(1, vaoN, 0);
        GLES32.glBindVertexArray(vaoN[0]);

        // position buffer for I
        GLES32.glGenBuffers(1, vboPosN, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboPosN[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBufferPosN = ByteBuffer.allocateDirect(verticesN.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBufferPosN.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer positionBufferN = byteBufferPosN.asFloatBuffer();

        // 4. put data in this COOKED buffer
        positionBufferN.put(verticesN);

        // 5. set the array at 0th position of buffer
        positionBufferN.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, verticesN.length * 4, positionBufferN, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

        // color buffer for I
        GLES32.glGenBuffers(1, vboColN, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboColN[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBufferColN = ByteBuffer.allocateDirect(colorsN.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBufferColN.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer colorBufferN = byteBufferColN.asFloatBuffer();

        // 4. put data in this COOKED buffer
        colorBufferN.put(colorsN);

        // 5. set the array at 0th position of buffer
        colorBufferN.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, colorsN.length * 4, colorBufferN, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_COLOR, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_COLOR);

        //////////////////////////////////////////////////////////////////////////////////////////////////////

        ////////// create vao for letter D  /////////////////////////////////////////////////////
        
        GLES32.glGenVertexArrays(1, vaoD, 0);
        GLES32.glBindVertexArray(vaoD[0]);

        // position buffer for I
        GLES32.glGenBuffers(1, vboPosD, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboPosD[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBufferPosD = ByteBuffer.allocateDirect(verticesD.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBufferPosD.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer positionBufferD = byteBufferPosD.asFloatBuffer();

        // 4. put data in this COOKED buffer
        positionBufferD.put(verticesD);

        // 5. set the array at 0th position of buffer
        positionBufferD.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, verticesD.length * 4, positionBufferD, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

        // color buffer for I
        GLES32.glGenBuffers(1, vboColD, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboColD[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBufferColD = ByteBuffer.allocateDirect(colorsD.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBufferColD.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        colorBufferD = byteBufferColD.asFloatBuffer();

        // 4. put data in this COOKED buffer
        colorBufferD.put(colorsD);

        // 5. set the array at 0th position of buffer
        colorBufferD.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, colorsD.length * 4, colorBufferD, GLES32.GL_DYNAMIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_COLOR, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_COLOR);

        //////////////////////////////////////////////////////////////////////////////////////////////////////

        ////////// create vao for letter i  /////////////////////////////////////////////////////
        
        GLES32.glGenVertexArrays(1, vaoi, 0);
        GLES32.glBindVertexArray(vaoi[0]);

        // position buffer for I
        GLES32.glGenBuffers(1, vboPosi, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboPosi[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBufferPosi = ByteBuffer.allocateDirect(verticesi.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBufferPosi.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer positionBufferi = byteBufferPosi.asFloatBuffer();

        // 4. put data in this COOKED buffer
        positionBufferi.put(verticesi);

        // 5. set the array at 0th position of buffer
        positionBufferi.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, verticesi.length * 4, positionBufferi, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

        // color buffer for I
        GLES32.glGenBuffers(1, vboColi, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboColi[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBufferColi = ByteBuffer.allocateDirect(colorsI.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBufferColi.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer colorBufferi = byteBufferColi.asFloatBuffer();

        // 4. put data in this COOKED buffer
        colorBufferi.put(colorsI);

        // 5. set the array at 0th position of buffer
        colorBufferi.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, colorsI.length * 4, colorBufferi, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_COLOR, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_COLOR);

        //////////////////////////////////////////////////////////////////////////////////////////////////////

        ////////// create vao for letter A  /////////////////////////////////////////////////////
        
        GLES32.glGenVertexArrays(1, vaoA, 0);
        GLES32.glBindVertexArray(vaoA[0]);

        // position buffer for I
        GLES32.glGenBuffers(1, vboPosA, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboPosA[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBufferPosA = ByteBuffer.allocateDirect(verticesA.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBufferPosA.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer positionBufferA = byteBufferPosA.asFloatBuffer();

        // 4. put data in this COOKED buffer
        positionBufferA.put(verticesA);

        // 5. set the array at 0th position of buffer
        positionBufferA.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, verticesA.length * 4, positionBufferA, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

        // color buffer for I
        GLES32.glGenBuffers(1, vboColA, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboColA[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBufferColA = ByteBuffer.allocateDirect(colorsA.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBufferColA.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer colorBufferA = byteBufferColA.asFloatBuffer();

        // 4. put data in this COOKED buffer
        colorBufferA.put(colorsA);

        // 5. set the array at 0th position of buffer
        colorBufferA.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, colorsA.length * 4, colorBufferA, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_COLOR, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_COLOR);

        //////////////////////////////////////////////////////////////////////////////////////////////////////

        ////////// create vao for letter plane  /////////////////////////////////////////////////////
        
        GLES32.glGenVertexArrays(1, vaoPlane, 0);
        GLES32.glBindVertexArray(vaoPlane[0]);

        // position buffer for Plane
        GLES32.glGenBuffers(1, vboPosPlane, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboPosPlane[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBufferPosPlane = ByteBuffer.allocateDirect(verticesPlane.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBufferPosPlane.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer positionBufferPlane = byteBufferPosPlane.asFloatBuffer();

        // 4. put data in this COOKED buffer
        positionBufferPlane.put(verticesPlane);

        // 5. set the array at 0th position of buffer
        positionBufferPlane.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, verticesPlane.length * 4, positionBufferPlane, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

        // color buffer for Plane
        GLES32.glGenBuffers(1, vboColPlane, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboColPlane[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBufferColPlane = ByteBuffer.allocateDirect(colorsPlane.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBufferColPlane.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer colorBufferPlane = byteBufferColPlane.asFloatBuffer();

        // 4. put data in this COOKED buffer
        colorBufferPlane.put(colorsPlane);

        // 5. set the array at 0th position of buffer
        colorBufferPlane.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, colorsPlane.length * 4, colorBufferPlane, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_COLOR, 4, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_COLOR);

        //////////////////////////////////////////////////////////////////////////////////////////////////////

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
        GLES32.glBindVertexArray(0);

        /////////////////////////////////////////////////////////////////////////////////////////////////////

        //float _width = (3040.0f * 2.0f) / 7.0f;

        xOffsetI = 0.5f * width;
        xOffsetA = 0.5f * width;
        yOffsetI2 = 0.5f * width;
        alphaD = 0.0f;
        yOffsetN = 0.5f * width;

        // clear the depth buffer
        GLES32.glClearDepthf(1.0f);

        // clear the screen by OpenGL
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // enable blend
        GLES32.glEnable(GLES32.GL_BLEND);
        GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA);

        // enable depth
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);
        GLES32.glDepthFunc(GLES32.GL_LEQUAL);

        Matrix.setIdentityM(perspectiveProjectionMatrix, 0);
    }

    private void resize(int width, int height) {
        System.out.println("RTR: resize called with: " + width + " " + height);
        if (height == 0)
        {
            height = 1;
        }

        GLES32.glViewport(0, 0, width, height);

        Matrix.perspectiveM(perspectiveProjectionMatrix, 0,
            45.0f,
            (float)width / (float)height,
            0.1f, 100.0f);
    }

    private void display() {
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);

        // use shader program
        GLES32.glUseProgram(shaderProgramObject);

        System.out.println("RTR: state: " + state);

        float height = (float)Math.tan(45.0f / 2.0f / 180.0f * Math.PI) * 50.0f;
        float width = height * 3040.0f / 1440.0f;

        float ltrWidth = (width * 2.0f) / 7.0f;
        float ltrHeight = (height * 2.0f) / 7.0f;

        /* State will control the animation */
        if (state >= 0) /* State 0 */
        {
            if (drawI(ltrWidth, (5.0f * ltrHeight)))
                if (state < 1) state = 1;
        }

        if (state >= 1) /* State 1 */
        {
            if (drawA(ltrWidth, (5.0f * ltrHeight)))
                if (state < 2) state = 2;
        }

        if (state >= 2) /* State 2 */
        {
            if (drawN(ltrWidth, (5.0f * ltrHeight)))
                if (state < 3)state = 3;
        }

        if (state >= 3) /* State 3 */
        {
            if (drawI2(ltrWidth, (5.0f * ltrHeight)))
                if (state < 4) state = 4;
        }

        if (state >= 4) /* State 4 */
        {
            if (drawD(ltrWidth, (5.0f * ltrHeight)))
                if (state < 5) state = 5;
        }

        if (state >= 6) /* State 6 */
        {

            if (drawFlag(ltrWidth, (5.0f * ltrHeight)))
                if (state < 7) state = 7;
        }

        if (state >= 5) /* State 5*/
        {

            if (drawPlanes(-width - (ltrWidth / 2.0f), height, ltrWidth, 0))
                if (state < 6) state = 6;
        }

        // unuse program
        GLES32.glUseProgram(0);
        requestRender();  // ~ swapBuffers
    }

    private void uninitialize() {
        if (vboColI[0] != 0) {
            GLES32.glDeleteBuffers(1, vboColI, 0);
            vboColI[0] = 0;
        }

        if (vboPosI[0] != 0) {
            GLES32.glDeleteBuffers(1, vboPosI, 0);
            vboPosI[0] = 0;
        }

        if (vaoI[0] != 0) {
            GLES32.glDeleteVertexArrays(1, vaoI, 0);
            vaoI[0] = 0;
        }

        if (vboColN[0] != 0) {
            GLES32.glDeleteBuffers(1, vboColN, 0);
            vboColN[0] = 0;
        }

        if (vboPosN[0] != 0) {
            GLES32.glDeleteBuffers(1, vboPosN, 0);
            vboPosN[0] = 0;
        }

        if (vaoN[0] != 0) {
            GLES32.glDeleteVertexArrays(1, vaoN, 0);
            vaoN[0] = 0;
        }

        if (vboColD[0] != 0) {
            GLES32.glDeleteBuffers(1, vboColD, 0);
            vboColD[0] = 0;
        }

        if (vboPosD[0] != 0) {
            GLES32.glDeleteBuffers(1, vboPosD, 0);
            vboPosD[0] = 0;
        }

        if (vaoD[0] != 0) {
            GLES32.glDeleteVertexArrays(1, vaoD, 0);
            vaoD[0] = 0;
        }

        if (vboColi[0] != 0) {
            GLES32.glDeleteBuffers(1, vboColi, 0);
            vboColi[0] = 0;
        }

        if (vboPosi[0] != 0) {
            GLES32.glDeleteBuffers(1, vboPosi, 0);
            vboPosi[0] = 0;
        }

        if (vaoi[0] != 0) {
            GLES32.glDeleteVertexArrays(1, vaoi, 0);
            vaoi[0] = 0;
        }

        if (vboColA[0] != 0) {
            GLES32.glDeleteBuffers(1, vboColA, 0);
            vboColA[0] = 0;
        }

        if (vboPosA[0] != 0) {
            GLES32.glDeleteBuffers(1, vboPosA, 0);
            vboPosA[0] = 0;
        }

        if (vaoA[0] != 0) {
            GLES32.glDeleteVertexArrays(1, vaoA, 0);
            vaoA[0] = 0;
        }

        if (vboColPlane[0] != 0) {
            GLES32.glDeleteBuffers(1, vboColPlane, 0);
            vboColPlane[0] = 0;
        }

        if (vboPosPlane[0] != 0) {
            GLES32.glDeleteBuffers(1, vboPosPlane, 0);
            vboPosPlane[0] = 0;
        }

        if (vaoPlane[0] != 0) {
            GLES32.glDeleteVertexArrays(1, vaoPlane, 0);
            vaoPlane[0] = 0;
        }

        if (shaderProgramObject != 0) {
            int[] shaderCount = new int[1];
            int shaderNumber;

            GLES32.glUseProgram(shaderProgramObject);
            GLES32.glGetProgramiv(shaderProgramObject, GLES32.GL_ATTACHED_SHADERS, shaderCount, 0);

            int[] shaders = new int[shaderCount[0]];

            GLES32.glGetAttachedShaders(shaderProgramObject, shaderCount[0], shaderCount, 0, shaders, 0);
            
            for (shaderNumber = 0; shaderNumber < shaderCount[0]; shaderNumber++) {
                // detach shader
                GLES32.glDetachShader(shaderProgramObject, shaders[shaderNumber]);

                // delete shader
                GLES32.glDeleteShader(shaders[shaderNumber]);
                shaders[shaderNumber] = 0;
            }

            GLES32.glUseProgram(0);
            GLES32.glDeleteProgram(shaderProgramObject);
            shaderProgramObject = 0;
        }
    }

    private float[] getCoordsI(float x, float y, float width, float height) {
        
        float[] coords = new float[4*3];

        /* Push x to center of the area */
        x = x + (letterThickness / 2.0f) + (width / 2.0f);

        coords[0] = x;
        coords[1] = y;
        coords[2] = 0.0f;

        coords[3] = x - letterThickness;
        coords[4] = y;
        coords[5] = 0.0f;

        coords[6] = x - letterThickness;
        coords[7] = y - height;
        coords[8] = 0.0f;

        coords[9]  = x;
        coords[10] = y - height;
        coords[11] = 0.0f;

        return coords;
    }

    private float[] getCoordsN(float x, float y, float width, float height) {
        float[] coords = new float[12*3];

        float _x = x + (2.0f * letterThickness);

        /* 1st Quad */
        coords[0] = _x;
        coords[1] = y;
        coords[2] = 0.0f;

        coords[3] = _x - letterThickness;
        coords[4] = y;
        coords[5] = 0.0f;

        coords[6] = _x - letterThickness;
        coords[7] = y - height;
        coords[8] = 0.0f;

        coords[9]  = _x;
        coords[10] = y - height;
        coords[11] = 0.0f;

        /* 2nd Quad */
        coords[12] = _x;
        coords[13] = y;
        coords[14] = 0.0f;

        coords[15] = _x - letterThickness;
        coords[16] = y;
        coords[17] = 0.0f;

        _x = x + (width - (1.0f * letterThickness));

        coords[18] = _x - letterThickness;
        coords[19] = y - height;
        coords[20] = 0.0f;

        coords[21] = _x;
        coords[22] = y - height;
        coords[23] = 0.0f;

        /* 3rd Quad */
        coords[24] = _x;
        coords[25] = y;
        coords[26] = 0.0f;

        coords[27] = _x - letterThickness;
        coords[28] = y;
        coords[29] = 0.0f;

        coords[30] = _x - letterThickness;
        coords[31] = y - height;
        coords[32] = 0.0f;

        coords[33] = _x;
        coords[34] = y - height;
        coords[35] = 0.0f;

        return coords;
    }

    private float[] getCoordsD(float x, float y, float width, float height) {
        float _x, _y;
        float[] coords = new float[16*3];

        /* Top Quad */
        _x = x + letterThickness;
    
        coords[0] = _x;
        coords[1] = y;
        coords[2] = 0.0f;

        coords[3] = _x;
        coords[4] = y - letterThickness;
        coords[5] = 0.0f;

        _x = x + (width - letterThickness);

        coords[6] = _x;
        coords[7] = y - letterThickness;
        coords[8] = 0.0f;

        coords[9]  = _x;
        coords[10] = y;
        coords[11] = 0.0f;

        /* Bottom Quad */
        _x =  x + letterThickness;
        _y = -y + letterThickness;

        coords[12] = _x;
        coords[13] = _y;
        coords[14] = 0.0f;

        coords[15] = _x;
        coords[16] = _y - letterThickness;
        coords[17] = 0.0f;

        _x = x + (width - letterThickness);

        coords[18] = _x;
        coords[19] = _y - letterThickness;
        coords[20] = 0.0f;

        coords[21] = _x;
        coords[22] = _y;
        coords[23] = 0.0f; 

        /* Left Quad */
        _x = x + (3.0f * letterThickness);
        coords[24] = _x;
        coords[25] = y;
        coords[26] = 0.0f;

        coords[27] = _x - letterThickness;
        coords[28] = y;
        coords[29] = 0.0f;

        coords[30] = _x - letterThickness;
        coords[31] = y - height;
        coords[32] = 0.0f;

        coords[33] = _x;
        coords[34] = y - height;
        coords[35] = 0.0f;

        /* Right Quad */
        _x = x + (width - letterThickness);

        coords[36] = _x;
        coords[37] = y;
        coords[38] = 0.0f;

        coords[39] = _x - letterThickness;
        coords[40] = y;
        coords[41] = 0.0f;

        coords[42] = _x - letterThickness;
        coords[43] = y - height;
        coords[44] = 0.0f;

        coords[45] = _x;
        coords[46] = y - height;
        coords[47] = 0.0f;

        return coords;
    }

    private float[] getCoordsA(float x, float y, float width, float height) {
        float _x = x + (letterThickness / 2.0f) + (width / 2.0f);
        float _y = y;
        float[] coords = new float[14*3];

        /* Left Quad */
        coords[0] = _x;
        coords[1] = _y;
        coords[2] = 0.0f;

        coords[3] = _x - letterThickness;
        coords[4] = _y;
        coords[5] = 0.0f;

        _x = x + (2.0f * letterThickness);
        _y = -y;

        coords[6] = _x - letterThickness;
        coords[7] = _y;
        coords[8] = 0.0f;

        coords[9]  = _x;
        coords[10] = _y;
        coords[11] = 0.0f;

        /* Right Quad */
        _x = x + (letterThickness / 2.0f) + (width / 2.0f);
        _y = y;

        coords[12] = _x;
        coords[13] = _y;
        coords[14] = 0.0f;

        coords[15] = _x - letterThickness;
        coords[16] = _y;
        coords[17] = 0.0f;

        _x = x + width - letterThickness;
        _y = -y;

        coords[18] = _x - letterThickness;
        coords[19] = _y;
        coords[20] = 0.0f;

        coords[21] = _x;
        coords[22] = _y;
        coords[23] = 0.0f; 

        /* Flag inside A */
        _x = x + ((width/2.0f) - (2.0f * letterThickness));

        coords[24] = x + ((width / 2.0f) - (2.0f * letterThickness));
        coords[25] = 0.3f;
        coords[26] = 0.0f;

        coords[27] = x + ((width / 2.0f) + (2.0f * letterThickness));
        coords[28] = 0.3f;
        coords[29] = 0.0f;

        coords[30] = x + ((width / 2.0f) - (2.0f * letterThickness));
        coords[31] = 0.0f;
        coords[32] = 0.0f;

        coords[33] = x + ((width / 2.0f) + (2.0f * letterThickness));
        coords[34] = 0.0f;
        coords[35] = 0.0f;

        coords[36] = x + ((width / 2.0f) - (2.0f * letterThickness));
        coords[37] = -0.3f;
        coords[38] = 0.0f;

        coords[39] = x + ((width / 2.0f) + (2.0f * letterThickness));
        coords[40] = -0.3f;
        coords[41] = 0.0f;

        return coords;
    }

    private float[] getCoordsPlane(float x, float y, float width, float height) {

        float[] coords = new float[36*3];

        int i = 0;
        float thickness = width / 5.0f;
        
        /* Plane Body (QUAD) */
        coords[i++] = -2.0f*thickness;
        coords[i++] = thickness/2.0f;
        coords[i++] = 0.0f;

        coords[i++] = -2.0f*thickness;
        coords[i++] = -thickness / 2.0f;
        coords[i++] = 0.0f;

        coords[i++] = 3.0f*thickness;
        coords[i++] = -thickness / 2.0f;
        coords[i++] = 0.0f;

        coords[i++] = 2.0f*thickness;
        coords[i++] = thickness / 2.0f;
        coords[i++] = 0.0f;

        /* TRIANGLES */
        /* Rear */
        coords[i++] = -thickness;
        coords[i++] = 0.0f;
        coords[i++] = 0.0f;

        coords[i++] = -2.5f*thickness;
        coords[i++] = thickness / 2.2f;
        coords[i++] = 0.0f;

        coords[i++] = -2.5f*thickness;
        coords[i++] = -thickness / 2.2f;
        coords[i++] = 0.0f;

        /* Top */
        coords[i++] = -1.8f*thickness;
        coords[i++] = thickness/2.0f;
        coords[i++] = 0.0f;

        coords[i++] = -1.8f*thickness;
        coords[i++] = thickness*1.5f;
        coords[i++] = 0.0f;

        coords[i++] = -0.5f*thickness;
        coords[i++] = thickness / 2.0f;
        coords[i++] = 0.0f;

        /* Right */
        coords[i++] = -0.5f*thickness;
        coords[i++] = -thickness / 2.0f;
        coords[i++] = 0.0f;

        coords[i++] = -0.5f*thickness;
        coords[i++] = -thickness * 1.5f;
        coords[i++] = 0.0f;

        coords[i++] = 2.0f*thickness;
        coords[i++] = -thickness / 2.2f;
        coords[i++] = 0.0f;
        
        /* Left */
        coords[i++] = -0.5f*thickness;
        coords[i++] = thickness / 2.0f;
        coords[i++] = 0.0f;

        coords[i++] = -0.5f*thickness;
        coords[i++] = thickness;
        coords[i++] = 0.0f;

        coords[i++] = thickness;
        coords[i++] = thickness / 2.0f;
        coords[i++] = 0.0f;

        /* I */
        coords[i++] = -0.6f*thickness;
        coords[i++] = thickness / 3.0f;
        coords[i++] = 0.0f;

        coords[i++] = -0.6f*thickness;
        coords[i++] = -thickness / 3.0f;
        coords[i++] = 0.0f;

        /* A */
        coords[i++] = 0.0f;
        coords[i++] = thickness / 3.0f;
        coords[i++] = 0.0f;

        coords[i++] = -0.25f*thickness;
        coords[i++] = -thickness / 3.0f;
        coords[i++] = 0.0f;

        coords[i++] = 0.0f;
        coords[i++] = thickness / 3.0f;
        coords[i++] = 0.0f;

        coords[i++] = 0.25f*thickness;
        coords[i++] = -thickness / 3.0f;
        coords[i++] = 0.0f;

        coords[i++] = -0.16f;
        coords[i++] = -0.2f;
        coords[i++] = 0.0f;

        coords[i++] = 0.16f;
        coords[i++] = -0.2f;
        coords[i++] = 0.0f;

        /* F */
        coords[i++] = 0.5f*thickness;
        coords[i++] = thickness / 3.0f;
        coords[i++] = 0.0f;

        coords[i++] = 0.5f*thickness;
        coords[i++] = -thickness / 3.0f;
        coords[i++] = 0.0f;

        coords[i++] = 0.5f*thickness;
        coords[i++] = thickness / 4.0f;
        coords[i++] = 0.0f;

        coords[i++] = 0.9f*thickness;
        coords[i++] = thickness / 4.0f;
        coords[i++] = 0.0f;

        coords[i++] = 0.5f*thickness;
        coords[i++] = -0.2f;
        coords[i++] = 0.0f;

        coords[i++] = 0.9f*thickness;
        coords[i++] = -0.2f;
        coords[i++] = 0.0f; 

        /* Tail Flag */
        coords[i++] = -2.5f*thickness;
        coords[i++] = 0.3f;
        coords[i++] = 0.0f;

        coords[i++] = -3.5f*thickness;
        coords[i++] = 0.3f;
        coords[i++] = 0.0f;

        coords[i++] = -2.5f*thickness;
        coords[i++] = 0.0f;
        coords[i++] = 0.0f;

        coords[i++] = -3.5f*thickness;
        coords[i++] = 0.0f;
        coords[i++] = 0.0f;

        coords[i++] = -2.5f*thickness;
        coords[i++] = -0.3f;
        coords[i++] = 0.0f;

        coords[i++] = -3.5f*thickness;
        coords[i++] = -0.3f;
        coords[i++] = 0.0f;

        return coords;
    }

    public boolean drawI(float width, float height) {

        //declaration of matrices
        float[] translationMatrix = new float[16];
        float[] modelViewMatrix = new float[16];
        float[] modelViewProjectionMatrix = new float[16];

        // intialize above matrices to identity
        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.setIdentityM(modelViewProjectionMatrix, 0);

        // perform necessary transformations
        Matrix.translateM(translationMatrix, 0,
            -xOffsetI, 0.0f, -50.1f);

        // do necessary matrix multiplication
        Matrix.multiplyMM(modelViewMatrix, 0,
            modelViewMatrix, 0,
            translationMatrix, 0);

        Matrix.multiplyMM(modelViewProjectionMatrix, 0,
            perspectiveProjectionMatrix, 0,
            modelViewMatrix, 0);

        GLES32.glUniformMatrix4fv(mvpUniform, 1, false, modelViewProjectionMatrix, 0);

        GLES32.glBindVertexArray(vaoI[0]);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 0, 4);
        GLES32.glBindVertexArray(0);

        if (xOffsetI > 0.0f)
        {
            xOffsetI -= speed;
            return false;
        }
        return true;
    }

    public boolean drawN(float width, float height) {

        //declaration of matrices
        float[] translationMatrix = new float[16];
        float[] modelViewMatrix = new float[16];
        float[] modelViewProjectionMatrix = new float[16];

        // intialize above matrices to identity
        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.setIdentityM(modelViewProjectionMatrix, 0);

        // perform necessary transformations
        Matrix.translateM(translationMatrix, 0,
            0.0f, yOffsetN, -50.1f);

        // do necessary matrix multiplication
        Matrix.multiplyMM(modelViewMatrix, 0,
            modelViewMatrix, 0,
            translationMatrix, 0);

        Matrix.multiplyMM(modelViewProjectionMatrix, 0,
            perspectiveProjectionMatrix, 0,
            modelViewMatrix, 0);

        GLES32.glUniformMatrix4fv(mvpUniform, 1, false, modelViewProjectionMatrix, 0);

        GLES32.glBindVertexArray(vaoN[0]);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 0, 4);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 4, 4);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 8, 4);
        GLES32.glBindVertexArray(0);

        if (yOffsetN > 0.0)
        {
            yOffsetN -= speed;
            return false;
        }

        return true;

    }

    public boolean drawD(float width, float height) {

        float range = 1.0f + (10.0f * width);

        float[] colorsD = new float[] {
            rO, gO, bO,
            rO, gO, bO,
            rO, gO, bO,
            rO, gO, bO,
            rG, gG, bG,
            rG, gG, bG,
            rG, gG, bG,
            rG, gG, bG,
            rO, gO, bO,
            rO, gO, bO,
            rG, gG, bG,
            rG, gG, bG,
            rO, gO, bO,
            rO, gO, bO,
            rG, gG, bG,
            rG, gG, bG
        };

        // 4. put data in this COOKED buffer
        colorBufferD.put(colorsD);

        // 5. set the array at 0th position of buffer
        colorBufferD.position(0);

        // bind with vao (this will avoid many binding to vbo)
        GLES32.glBindVertexArray(vaoD[0]);

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboColD[0]);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, colorsD.length * 4, colorBufferD, GLES32.GL_DYNAMIC_DRAW);

        // draw necessary scene
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 0, 4);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 4, 4);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 8, 4);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 12, 4);
        GLES32.glBindVertexArray(0);

        if (alphaD < range)
        {
            alphaD += speed * 5.0f;
            float delta = (alphaD / range);

            rO = 1.0f * delta;
            gO = 153.0f / 256.0f * delta;
            bO = 51.0f / 256.0f * delta;

            rG = 19.0f / 256.0f * delta;
            gG = 136.0f / 256.0f * delta;
            bG = 8.0f / 256.0f * delta;

            return false;
        }
        return true;
    }

    public boolean drawI2(float width, float height) {

        //declaration of matrices
        float[] translationMatrix = new float[16];
        float[] modelViewMatrix = new float[16];
        float[] modelViewProjectionMatrix = new float[16];

        // intialize above matrices to identity
        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.setIdentityM(modelViewProjectionMatrix, 0);

        // perform necessary transformations
        Matrix.translateM(translationMatrix, 0,
            0.0f, -yOffsetI2, -50.1f);

        // do necessary matrix multiplication
        Matrix.multiplyMM(modelViewMatrix, 0,
            modelViewMatrix, 0,
            translationMatrix, 0);

        Matrix.multiplyMM(modelViewProjectionMatrix, 0,
            perspectiveProjectionMatrix, 0,
            modelViewMatrix, 0);

        GLES32.glUniformMatrix4fv(mvpUniform, 1, false, modelViewProjectionMatrix, 0);

        GLES32.glBindVertexArray(vaoi[0]);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 0, 4);
        GLES32.glBindVertexArray(0);

        if (yOffsetI2 > 0.0)
        {
            yOffsetI2 -= speed;
            return false;
        }
        return true;
    }

    public boolean drawA(float width, float height) {
        
        //declaration of matrices
        float[] translationMatrix = new float[16];
        float[] modelViewMatrix = new float[16];
        float[] modelViewProjectionMatrix = new float[16];

        // intialize above matrices to identity
        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.setIdentityM(modelViewProjectionMatrix, 0);

        // perform necessary transformations
        Matrix.translateM(translationMatrix, 0,
            xOffsetA, 0.0f, -50.1f);

        // do necessary matrix multiplication
        Matrix.multiplyMM(modelViewMatrix, 0,
            modelViewMatrix, 0,
            translationMatrix, 0);

        Matrix.multiplyMM(modelViewProjectionMatrix, 0,
            perspectiveProjectionMatrix, 0,
            modelViewMatrix, 0);

        GLES32.glUniformMatrix4fv(mvpUniform, 1, false, modelViewProjectionMatrix, 0);

        GLES32.glBindVertexArray(vaoA[0]);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 0, 4);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 4, 4);
        
        /*glLineWidth(10.0f);
        glDrawArrays(GL_LINES, 8, 6);*/
        GLES32.glBindVertexArray(0);

        if (xOffsetA > 0.0)
        {
            xOffsetA -= speed;
            return false;
        }
        return true;
    }

    public boolean drawPlane(float x, float y, float width, float angle, int pos) {
    
        boolean bDone = false;

        //declaration of matrices
        float[] translationMatrix = new float[16];
        float[] rotationMatrix = new float[16];
        float[] modelViewMatrix = new float[16];
        float[] modelViewProjectionMatrix = new float[16];

        // intialize above matrices to identity
        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.setIdentityM(modelViewProjectionMatrix, 0);

        // perform necessary transformations
        Matrix.translateM(translationMatrix, 0,
            x, y, -50.1f);

        Matrix.setRotateM(rotationMatrix, 0,
            angle, 0.0f, 0.0f, 1.0f);

        // do necessary matrix multiplication
        Matrix.multiplyMM(modelViewMatrix, 0,
            modelViewMatrix, 0,
            translationMatrix, 0);

        Matrix.multiplyMM(modelViewMatrix, 0,
            modelViewMatrix, 0,
            rotationMatrix, 0);

        Matrix.multiplyMM(modelViewProjectionMatrix, 0,
            perspectiveProjectionMatrix, 0,
            modelViewMatrix, 0);

        GLES32.glUniformMatrix4fv(mvpUniform, 1, false, modelViewProjectionMatrix, 0);

        GLES32.glBindVertexArray(vaoPlane[0]);
        /* tail */
        GLES32.glLineWidth(10.0f);
        GLES32.glDrawArrays(GLES32.GL_LINES, 30, 6);

        /* body */
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 0, 4);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 4, 12);

        /* letters */
        GLES32.glLineWidth(4.0f);
        GLES32.glDrawArrays(GLES32.GL_LINES, 16, 14);

        GLES32.glBindVertexArray(0);   
        return bDone;
    }

    public boolean drawPlanes(float x, float y, float width, float angle) {

        System.out.println("RTR: drawPlanes: " + xOffsetPlane);
        boolean bDone = false;

        /* Upper */
        drawPlane(x + xOffsetPlane, y - yOffsetPlane, width, -rotationAngle, 1);

        /* Lower */
        drawPlane(x + xOffsetPlane, -y + yOffsetPlane, width, rotationAngle, -1);

        /* Middle */
        bDone = drawPlane(x + xOffsetPlane, 0, width, 0, 0);

        /* X travel */
        if (xOffsetPlane < 4.0f*(-x))
            xOffsetPlane += 0.25f;

        /* Y travel */
        if (xOffsetPlane <= 0.8f*(-x))
        {
            planeAngle = (Math.abs(xOffsetPlane) / (-x) * 0.9f) * 90.0f;
            yOffsetPlane = y * (float)Math.sin(planeAngle * (float)Math.PI / 180.0f);
            rotationAngle = 45.0f - ((Math.abs(xOffsetPlane) / (-x) / 0.9f) * 45.0f);
        }
        else if (xOffsetPlane < 1.0f*(-x))
        {
            if (planeAngle < 90.0f)
                planeAngle += 0.01f;

            if (yOffsetPlane > 0.0f)
                yOffsetPlane -= 0.5f;

            yOffsetPlane = y * (float)Math.sin(planeAngle * (float)Math.PI / 180.0f);
            rotationAngle = 0.0f;
        }
        else if (xOffsetPlane >= 1.4f*(-x))
        {
            planeAngle = (xOffsetPlane - 1.4f*(-x)) / (-x) / 0.5f * 90.0f;
            yOffsetPlane = y * (float)Math.sin((90.0 - planeAngle) * (float)Math.PI / 180.0f);
            rotationAngle = -(xOffsetPlane - 1.4f*(-x)) / (-x) / 0.5f * 45.0f;
        }
        
        if(xOffsetPlane >= 1.6f*(-x))
            bDone = true;

        return bDone;

    }

    public boolean drawFlag(float width, float height) {
    
        //declaration of matrices
        float[] translationMatrix = new float[16];
        float[] modelViewMatrix = new float[16];
        float[] modelViewProjectionMatrix = new float[16];

        // intialize above matrices to identity
        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.setIdentityM(modelViewProjectionMatrix, 0);

        // perform necessary transformations
        Matrix.translateM(translationMatrix, 0,
            0.0f, 0.0f, -50.1f);

        // do necessary matrix multiplication
        Matrix.multiplyMM(modelViewMatrix, 0,
            modelViewMatrix, 0,
            translationMatrix, 0);

        Matrix.multiplyMM(modelViewProjectionMatrix, 0,
            perspectiveProjectionMatrix, 0,
            modelViewMatrix, 0);

        GLES32.glUniformMatrix4fv(mvpUniform, 1, false, modelViewProjectionMatrix, 0);

        GLES32.glBindVertexArray(vaoA[0]);
        GLES32.glLineWidth(10.0f);
        GLES32.glDrawArrays(GLES32.GL_LINES, 8, 6);
        GLES32.glBindVertexArray(0);
        return true;
    }

}



