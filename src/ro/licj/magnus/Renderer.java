package ro.licj.magnus;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.gl2.GLUgl2;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import ro.licj.magnus.util.*;
import ro.licj.magnus.util.Point;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;

import static com.jogamp.opengl.GL2.*;
import static ro.licj.magnus.util.UnitConverter.*;

/**
 * For user interface.
 */
public class Renderer {

    private static final int WINDOW_WIDTH = 1024;
    private static final int WINDOW_HEIGHT = 720;
    private static final int PIXELS_PER_METER = 100;
    private static Renderer instance = new Renderer();
    private final JFrame window = new JFrame("One Triangle Swing GLJPanel");
    private GLJPanel drawingPanel;
    private List<Point> trajectory;
    private boolean started = false;
    private int textureID;
    private int textureUniformLocation;
    private int matrixUniformLocation;
    private Mobile ball;
    private boolean isClosed = false;
    private GLProfile glProfile;

    private Renderer() {
    }

    public static int getWindowWidth() {
        return WINDOW_WIDTH;
    }

    public static int getWindowHeight() {
        return WINDOW_HEIGHT;
    }

    public static int getPixelsPerMeter() {
        return PIXELS_PER_METER;
    }

    public static Renderer getInstance() {
        return instance;
    }

    public void init(Mobile ball, List<Point> trajectory) {
        this.ball = ball;
        this.trajectory = trajectory;

        glProfile = GLProfile.getDefault();
        GLCapabilities glcapabilities = new GLCapabilities(glProfile);
        glcapabilities.setDoubleBuffered(true);
        drawingPanel = new GLJPanel(glcapabilities);

        drawingPanel.addGLEventListener(new GLEventListener() {
            private int kkt = 0;

            @Override
            public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height) {
                setup(glautodrawable.getGL().getGL2(), width, height);
            }

            @Override
            public void init(GLAutoDrawable glautodrawable) {
            }

            @Override
            public void dispose(GLAutoDrawable glautodrawable) {
            }

            @Override
            public void display(GLAutoDrawable glautodrawable) {
                render(glautodrawable.getGL().getGL2(), glautodrawable.getSurfaceWidth(), glautodrawable.getSurfaceHeight());
                glautodrawable.swapBuffers();
            }
        });


        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowevent) {
                window.dispose();
                isClosed = true;
            }
        });

        window.getContentPane().add(drawingPanel, BorderLayout.CENTER);
        window.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        window.setVisible(true);

        drawingPanel.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    started = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
    }

    private void setup(GL2 gl2, int width, int height) {

    }

    public void terminate() {
//        ShaderProgram.disposeAll();
//
//        if (window != NULL) {
//            // Release window and window callbacks
//            glfwDestroyWindow(window);
//            keyCallback.release();
//        }
//
//        // Terminate GLFW and release the GLFWerrorfun
//        glfwTerminate();
//        errorCallback.release();
    }

    public void prepareForDrawing() {
//        GL.createCapabilities();
//
//        ShaderProgram.initAll();
//
//        textureID = glGenTextures();
//        glBindTexture(GL_TEXTURE_2D, textureID);
//        PNGTexture football = PNGTexture.FOOTBALL;
//        glTexImage2D(GL_TEXTURE_2D, 0, football.getInternalFormat(),
//                football.getWidth(), football.getHeight(), 0,
//                football.getInternalFormat(), GL_UNSIGNED_BYTE,
//                football.getByteBuffer());
//        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
//        glGenerateMipmap(GL_TEXTURE_2D);
//
//        textureUniformLocation = glGetUniformLocation(ShaderProgram.BALL_TEXTURE_SHADER.getID(),
//                "myTextureSampler");
//        matrixUniformLocation = glGetUniformLocation(ShaderProgram.BALL_TEXTURE_SHADER.getID(), "MVP");
    }

    public void render(GL2 gl2, int width, int height) {
//        // Clear the screen
        gl2.glClearColor(0.0f, 1.0f, 1.0f, 1.0f);
        gl2.glClear(GL_COLOR_BUFFER_BIT);

        drawTrajectory(gl2);
        drawBall(gl2);
    }

    private void drawTrajectory(GL2 gl2) {
        if (trajectory.size() <= 1) {
            return;
        }
        int numberOfPoints = trajectory.size();
        float[] tmp = new float[2 * numberOfPoints];
        for (int i = 0; i < numberOfPoints; i++) {
            Point point = trajectory.get(i);
            float x = -1.0f + metersToUniformCoordinatesX(point.x);
            float y = -1.0f + metersToUniformCoordinatesY(point.y);
            tmp[2 * i] = x;
            tmp[2 * i + 1] = y;
        }
        FloatBuffer pointBuffer = GLBuffers.newDirectFloatBuffer(tmp);

        gl2.glColor3f(1.0f, 0.0f, 0.0f);

        gl2.glEnableClientState(GL_VERTEX_ARRAY);
        gl2.glVertexPointer(2, GL_FLOAT, 0, pointBuffer);

        ShortBuffer indicesBuffer = ShortBuffer.allocate(numberOfPoints * 4 - 4);
        for (short i = 0; i < 2 * numberOfPoints - 2; i++) {
            indicesBuffer.put(i);
            indicesBuffer.put((short) (i + 1));
        }
        indicesBuffer.flip();

        gl2.glDrawElements(GL_LINES, 2 * numberOfPoints - 2, GL_UNSIGNED_SHORT, indicesBuffer);
    }

    private void drawBall(GL2 gl2) {

        gl2.glColor3f(1.0f, 1.0f, 1.0f);
        gl2.glEnable(GL_TEXTURE_2D);
        Texture ballTexture = null;
        try {
            InputStream stream = getClass().getResourceAsStream("/res/football_32x32.png");
            TextureData data = TextureIO.newTextureData(glProfile, stream, false, "png");
            ballTexture = TextureIO.newTexture(data);
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }

        // Set material properties.
        //float[] rgba = {1f, 1f, 1f};
        //gl2.glMaterialfv(GL_FRONT, GL_AMBIENT, rgba, 0);
        //gl2.glMaterialfv(GL_FRONT, GL_SPECULAR, rgba, 0);
        //gl2.glMaterialf(GL_FRONT, GL_SHININESS, 0.5f);
        gl2.glEnable(GL_BLEND);
        gl2.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        gl2.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl2.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl2.glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, GL_TRUE);

        ballTexture.enable(gl2);
        ballTexture.bind(gl2);

        gl2.glMatrixMode(GL_MODELVIEW);
        gl2.glPushMatrix();
        gl2.glLoadIdentity();
        gl2.glTranslatef(
                -1.0f + metersToUniformCoordinatesX(ball.getPosition().x) - metersToUniformCoordinatesX(ball.getSize().x) / 2.0f,
                -1.0f + metersToUniformCoordinatesY(ball.getPosition().y) - metersToUniformCoordinatesY(ball.getSize().y) / 2.0f,
                0.0f
        );
        //gl2.glRotatef((float) ball.getAngle(), 0.0f, 0.0f, 1.0f);
        gl2.glScalef(
                metersToUniformCoordinatesX(ball.getSize().x) / 2.0f,
                metersToUniformCoordinatesY(ball.getSize().y) / 2.0f,
                1.0f
        );

        gl2.glBegin(GL_TRIANGLES);
        gl2.glTexCoord2f(0.0f, 1.0f);
        gl2.glVertex2f(-1.0f, 1.0f);
        gl2.glTexCoord2f(0.0f, 0.0f);
        gl2.glVertex2f(-1.0f, -1.0f);
        gl2.glTexCoord2f(1.0f, 0.0f);
        gl2.glVertex2f(1.0f, -1.0f);


        gl2.glTexCoord2f(0.0f, 1.0f);
        gl2.glVertex2f(-1.0f, 1.0f);
        gl2.glTexCoord2f(1.0f, 1.0f);
        gl2.glVertex2f(1.0f, 1.0f);
        gl2.glTexCoord2f(1.0f, 0.0f);
        gl2.glVertex2f(1.0f, -1.0f);
        gl2.glEnd();

        gl2.glPopMatrix();
//        ShaderProgram.BALL_TEXTURE_SHADER.bind();
//
//        // Generate and bind a Vertex Array
//        int vaoID = glGenVertexArrays();
//        glBindVertexArray(vaoID);
//
//        // The vertices of our Triangle
//        float[] vertices = new float[]{
//                1.0f, 1.0f,    // Top-right coordinate
//                -1.0f, -1.0f,    // Bottom-left coordinate
//                1.0f, -1.0f,    // Bottom-right coordinate
//
//                -1.0f, 1.0f,    // Top-left coordinate
//                1.0f, 1.0f,    // Top-right coordinate
//                -1.0f, -1.0f     // Bottom-left coordinate
//        };
//
//        float[] model =
//                Matrix3f.multiply(
//                        Matrix3f.translationMatrix(
//                                -1.0f + UnitConverter.metersToUniformCoordinatesX(ball.getPosition().x),
//                                -1.0f + UnitConverter.metersToUniformCoordinatesY(ball.getPosition().y)
//                        ),
//                        Matrix3f.multiply(
//                                Matrix3f.scaleMatrix(
//                                        UnitConverter.metersToUniformCoordinatesX(ball.getSize().x) / 2.0f,
//                                        UnitConverter.metersToUniformCoordinatesY(ball.getSize().y) / 2.0f
//                                ),
//                                Matrix3f.rotationMatrix(ball.getAngle())
//                        )
//                );
//
//        float[] view = new float[]{
//                1.0f, 0.0f, 0.0f,
//                0.0f, 1.0f, 0.0f,
//                0.0f, 0.0f, 1.0f
//        };
//
//        float[] projection = new float[]{
//                1.0f, 0.0f, 0.0f,
//                0.0f, 1.0f, 0.0f,
//                0.0f, 0.0f, 1.0f
//        };
//
//        float[] modelView = Matrix3f.multiply(model, view);
//        float[] modelViewProjection = Matrix3f.multiply(modelView, projection);
//
//        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(3 * 3);
//        matrixBuffer.put(modelViewProjection);
//        matrixBuffer.flip();
//        glUniformMatrix3fv(matrixUniformLocation, true, matrixBuffer);
//
//        float[] UVCoordinates = new float[]
//                {
//                        1.0f, 1.0f,
//                        0.0f, 0.0f,
//                        1.0f, 0.0f,
//
//                        0.0f, 1.0f,
//                        1.0f, 1.0f,
//                        0.0f, 0.0f
//                };
//        FloatBuffer UVBuffer = BufferUtils.createFloatBuffer(UVCoordinates.length);
//        UVBuffer.put(UVCoordinates).flip();
//        int vboUVCoordID = glGenBuffers();
//        glBindBuffer(GL_ARRAY_BUFFER, vboUVCoordID);
//        glBufferData(GL_ARRAY_BUFFER, UVBuffer, GL_STATIC_DRAW);
//        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
//        glEnableVertexAttribArray(1);
//
//        // Create a FloatBuffer of vertices
//        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
//        verticesBuffer.put(vertices).flip();
//
//        // Create a Buffer Object and upload the vertices buffer
//        int vboID = glGenBuffers();
//        glBindBuffer(GL_ARRAY_BUFFER, vboID);
//        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
//
//        // Point the buffer at location 0, the location we set
//        // inside the vertex shader. You can use any location
//        // but the locations should match
//        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
//        glBindVertexArray(0);
//
//        // Bind the vertex array and enable our location
//        glBindVertexArray(vaoID);
//        glEnableVertexAttribArray(0);
//
//        glBindTexture(GL_TEXTURE_2D, textureID);
//        glUniform1i(textureUniformLocation, 0);
//
//        // For transparency to work, we need to turn on GL_BLEND.
//        glEnable(GL_BLEND);
//        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//
//        // Draw a triangle of 3 vertices
//        glDrawArrays(GL_TRIANGLES, 0, 6);
//
//        // Disable transparency.
//        glDisable(GL_BLEND);
//
//        // Disable our location
//        glDisableVertexAttribArray(0);
//        glBindVertexArray(0);
//
//        // Un-bind our program
//        ShaderProgram.unbind();
//
//        glDeleteBuffers(vboID);
//        glDeleteBuffers(vboUVCoordID);
//        glDeleteVertexArrays(vaoID);
    }

    public boolean shouldClose() {
        return isClosed;
    }

    public boolean hasStarted() {
        return started;
    }

    public void draw() {
        drawingPanel.display();
    }
}
