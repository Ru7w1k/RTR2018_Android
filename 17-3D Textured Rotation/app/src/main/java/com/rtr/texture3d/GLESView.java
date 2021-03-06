package com.rtr.texture3d;

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

import android.opengl.Matrix; // Matrix

import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.opengl.GLUtils;

public class GLESView extends GLSurfaceView implements GLSurfaceView.Renderer, OnGestureListener, OnDoubleTapListener {

    private final Context context;
    private GestureDetector gestureDetector;

    private int vertexShaderObject;
    private int fragmentShaderObject;
    private int shaderProgramObject;

    private int[] vaoPyramid  = new int[1];
    private int[] vboPyramidPosition  = new int[1];
    private int[] vboPyramidTexcoords  = new int[1];
    private int[] vaoCube = new int[1];
    private int[] vboCubePosition = new int[1];
    private int[] vboCubeTexcoords = new int[1];
    private int mvpUniform;

    private float[] perspectiveProjectionMatrix = new float[16];

    private int[] texture_stone = new int[1];
    private int[] texture_kundali = new int[1];
    private int samplerUniform;

    private float anglePyramid;
    private float angleCube;

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

        //// VERTEX SHADER ////////////////////////////////////////////////
        // create shader object
        vertexShaderObject = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);

        // shader source code
        final String vertexShaderSourceCode = String.format(
            "#version 320 es" +
            "\n" +
            "in vec4 vPosition;" +
            "in vec2 vTexcoord;" +
            "uniform mat4 u_mvp_matrix;" +
            "out vec2 out_Texcoord;" +
            "void main(void)" +
            "{" +
            "   gl_Position = u_mvp_matrix * vPosition;" +
            "   out_Texcoord = vTexcoord;" +
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
            "in vec2 out_Texcoord;" +
            "uniform sampler2D u_sampler;" +
            "out vec4 FragColor;" +
            "void main (void)" +
            "{" +
            "   FragColor = texture(u_sampler, out_Texcoord);" +
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
        GLES32.glBindAttribLocation(shaderProgramObject, GLESMacros.AMC_ATTRIBUTE_TEXCOORD0, "vTexcoord");

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
                szInfo = GLES32.glGetProgramInfoLog(shaderProgramObject);
                System.out.println("RTR: Program Linking: " + szInfo);
                uninitialize();
                System.exit(0);
            }
        }

        // get unifrom locations
        mvpUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_mvp_matrix");
        samplerUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_sampler");

        // pyramid Position
        final float[] pyramidVertices = new float[] {
            /* Front */
             0.0f,  1.0f,  0.0f,
            -1.0f, -1.0f,  1.0f,
             1.0f, -1.0f,  1.0f,
           
            /* Right */
             0.0f,  1.0f,  0.0f,
             1.0f, -1.0f,  1.0f,
             1.0f, -1.0f, -1.0f,
           
            /* Left */
             0.0f,  1.0f,  0.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f,  1.0f,
           
            /* Back */
             0.0f,  1.0f,  0.0f,
             1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f
        };

        // cube Position
        final float[] cubeVertices = new float[] {
            /* Top */
             1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f,  1.0f,
             1.0f,  1.0f,  1.0f,

            /* Bottom */
             1.0f, -1.0f,  1.0f,
            -1.0f, -1.0f,  1.0f,
            -1.0f, -1.0f, -1.0f,
             1.0f, -1.0f, -1.0f,

            /* Front */
             1.0f,  1.0f,  1.0f,
            -1.0f,  1.0f,  1.0f,
            -1.0f, -1.0f,  1.0f,
             1.0f, -1.0f,  1.0f,

            /* Back */
             1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f,  1.0f, -1.0f,
             1.0f,  1.0f, -1.0f,

             /* Right */
             1.0f,  1.0f, -1.0f,
             1.0f,  1.0f,  1.0f,
             1.0f, -1.0f,  1.0f,
             1.0f, -1.0f, -1.0f,

            /* Left */
            -1.0f,  1.0f,  1.0f,
            -1.0f,  1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f,  1.0f
        };

        // pyramid texture
        final float[] pyramidTexcoords = new float[] {
            /* Front */
            0.5f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,

            /* Right */
            0.5f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,

            /* Left */
            0.5f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,

            /* Back */
            0.5f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f
        };

        // cube texture
        final float[] cubeTexcoords = new float[] {
            /* Top */
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,

            /* Bottom */
            1.0f, 1.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,

            /* Front */
            1.0f, 1.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,

            /* Back */
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,

            /* Right */
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,

            /* Left */
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f
        };

        //// pyramid ///////////////////////

        // create vao
        GLES32.glGenVertexArrays(1, vaoPyramid, 0);
        GLES32.glBindVertexArray(vaoPyramid[0]);

        // Pyramid vertices
        GLES32.glGenBuffers(1, vboPyramidPosition, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboPyramidPosition[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBufferPyramid = ByteBuffer.allocateDirect(pyramidVertices.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBufferPyramid.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer positionBufferPyramid = byteBufferPyramid.asFloatBuffer();

        // 4. put data in this COOKED buffer
        positionBufferPyramid.put(pyramidVertices);

        // 5. set the array at 0th position of buffer
        positionBufferPyramid.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, pyramidVertices.length * 4, positionBufferPyramid, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION); 

        // Pyramid colors
        GLES32.glGenBuffers(1, vboPyramidTexcoords, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboPyramidTexcoords[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBufferPyramidColor = ByteBuffer.allocateDirect(pyramidTexcoords.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBufferPyramidColor.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer textureBufferPyramid = byteBufferPyramidColor.asFloatBuffer();

        // 4. put data in this COOKED buffer
        textureBufferPyramid.put(pyramidTexcoords);

        // 5. set the array at 0th position of buffer
        textureBufferPyramid.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, pyramidTexcoords.length * 4, textureBufferPyramid, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_TEXCOORD0, 2, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_TEXCOORD0); 

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
        GLES32.glBindVertexArray(0);

        //// cube /////////////////

        // create vao
        GLES32.glGenVertexArrays(1, vaoCube, 0);
        GLES32.glBindVertexArray(vaoCube[0]);

        // Cube vertices
        GLES32.glGenBuffers(1, vboCubePosition, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboCubePosition[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBufferCube = ByteBuffer.allocateDirect(cubeVertices.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBufferCube.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer positionBufferCube = byteBufferCube.asFloatBuffer();

        // 4. put data in this COOKED buffer
        positionBufferCube.put(cubeVertices);

        // 5. set the array at 0th position of buffer
        positionBufferCube.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, cubeVertices.length * 4, positionBufferCube, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION); 

        // Cube colors
        GLES32.glGenBuffers(1, vboCubeTexcoords, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboCubeTexcoords[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBufferCubeColor = ByteBuffer.allocateDirect(cubeTexcoords.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBufferCubeColor.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer textureBufferCube = byteBufferCubeColor.asFloatBuffer();

        // 4. put data in this COOKED buffer
        textureBufferCube.put(cubeTexcoords);

        // 5. set the array at 0th position of buffer
        textureBufferCube.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, cubeTexcoords.length * 4, textureBufferCube, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_TEXCOORD0, 2, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_TEXCOORD0); 

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
        GLES32.glBindVertexArray(0);

        //////////////////////////////////////////////////////////////////////

        // clear the depth buffer
        GLES32.glClearDepthf(1.0f);

        // clear the screen by OpenGL
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // enable depth
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);
        GLES32.glDepthFunc(GLES32.GL_LEQUAL);

        // texture
        GLES32.glEnable(GLES32.GL_TEXTURE_2D);
        texture_stone[0] = loadGLTexture(R.raw.stone);
        texture_kundali[0] = loadGLTexture(R.raw.kundali);


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

        //declaration of matrices
        float[] translationMatrix = new float[16];
        float[] rotationMatrix = new float[16];
        float[] scaleMatrix = new float[16];
        float[] modelViewMatrix = new float[16];
        float[] modelViewProjectionMatrix = new float[16];

        //// Pyramid //////////////////////////////////////

        // intialize above matrices to identity
        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.setIdentityM(modelViewProjectionMatrix, 0);

        // perform necessary transformations
        Matrix.translateM(translationMatrix, 0,
            -1.5f, 0.0f, -6.0f);

        Matrix.setRotateM(rotationMatrix, 0,
            anglePyramid, 0.0f, 1.0f, 0.0f);

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

        // send necessary matrices to shader in respective uniforms
        GLES32.glUniformMatrix4fv(mvpUniform, 1, false, modelViewProjectionMatrix, 0);

        // bind with textures
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, texture_stone[0]);
        GLES32.glUniform1i(samplerUniform, 0);

        // bind with vao (this will avoid many binding to vbo)
        GLES32.glBindVertexArray(vaoPyramid[0]);

        // draw necessary scene
        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, 12);

        // unbind vao
        GLES32.glBindVertexArray(0);

        ////////////////////////////////////////////////////

        //// Cube //////////////////////////////////////////

        // intialize above matrices to setIdentityM
        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.setIdentityM(modelViewProjectionMatrix, 0);

        // perform necessary transformations
        Matrix.translateM(translationMatrix, 0,
            1.5f, 0.0f, -6.0f);

        Matrix.setRotateM(rotationMatrix, 0,
            angleCube, 1.0f, 1.0f, 1.0f);

        Matrix.scaleM(scaleMatrix, 0,
            0.8f, 0.8f, 0.8f);

        // do necessary matrix multiplication
        Matrix.multiplyMM(modelViewMatrix, 0,
            modelViewMatrix, 0,
            translationMatrix, 0);

        Matrix.multiplyMM(modelViewMatrix, 0,
            modelViewMatrix, 0,
            rotationMatrix, 0);

        Matrix.multiplyMM(modelViewMatrix, 0,
            modelViewMatrix, 0,
            scaleMatrix, 0);

        Matrix.multiplyMM(modelViewProjectionMatrix, 0,
            perspectiveProjectionMatrix, 0,
            modelViewMatrix, 0);

        // send necessary matrices to shader in respective uniforms
        GLES32.glUniformMatrix4fv(mvpUniform, 1, false, modelViewProjectionMatrix, 0);

        // bind with textures
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, texture_kundali[0]);
        GLES32.glUniform1i(samplerUniform, 0);

        // bind with vao (this will avoid many binding to vbo)
        GLES32.glBindVertexArray(vaoCube[0]);

        // draw necessary scene
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,  0, 4);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,  4, 4);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,  8, 4);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 12, 4);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 16, 4);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 20, 4);

        // unbind vao
        GLES32.glBindVertexArray(0);

        ////////////////////////////////////////////////////

        // unuse program
        GLES32.glUseProgram(0);
        update();
        requestRender();  // ~ swapBuffers
    }

    private void uninitialize() {
        if (vboPyramidPosition[0] != 0) {
            GLES32.glDeleteBuffers(1, vboPyramidPosition, 0);
            vboPyramidPosition[0] = 0;
        }

        if (vboPyramidTexcoords[0] != 0) {
            GLES32.glDeleteBuffers(1, vboPyramidTexcoords, 0);
            vboPyramidTexcoords[0] = 0;
        }

        if (vaoPyramid[0] != 0) {
            GLES32.glDeleteVertexArrays(1, vaoPyramid, 0);
            vaoPyramid[0] = 0;
        }

        if (vboCubePosition[0] != 0) {
            GLES32.glDeleteBuffers(1, vboCubePosition, 0);
            vboCubePosition[0] = 0;
        }

        if (vboCubeTexcoords[0] != 0) {
            GLES32.glDeleteBuffers(1, vboCubeTexcoords, 0);
            vboCubeTexcoords[0] = 0;
        }

        if (vaoCube[0] != 0) {
            GLES32.glDeleteVertexArrays(1, vaoCube, 0);
            vaoCube[0] = 0;
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

    private int loadGLTexture(int imageFileResourceID) {
        int[] texture = new int[1];
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imageFileResourceID, options);

        GLES32.glPixelStorei(GLES32.GL_UNPACK_ALIGNMENT, 4);
        GLES32.glGenTextures(1, texture, 0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, texture[0]);

        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR_MIPMAP_LINEAR);

        GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES32.glGenerateMipmap(GLES32.GL_TEXTURE_2D);

        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);
        return texture[0];
    }

    private void update() {
        if (anglePyramid >= 360.0f)
        {
            anglePyramid = 0.0f;
        }
        else
        {
            anglePyramid += 2.0f;
        }

        if (angleCube >= 360.0f)
        {
            angleCube = 0.0f;
        }
        else
        {
            angleCube += 2.0f;
        }
    }
}



