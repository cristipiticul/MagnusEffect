package ro.licj.magnus;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import ro.licj.magnus.util.Point;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;

import static com.jogamp.opengl.GL2.*;
import static ro.licj.magnus.util.UnitConverter.metersToUniformCoordinatesX;
import static ro.licj.magnus.util.UnitConverter.metersToUniformCoordinatesY;

/**
 * For user interface.
 */
public class Renderer {

  private static final int WINDOW_WIDTH = 1024;
  private static final int WINDOW_HEIGHT = 720;
  private static int DRAWING_PANEL_WIDTH;
  private static int DRAWING_PANEL_HEIGHT;
  private static final int PIXELS_PER_METER = 100;
  private static Renderer instance = new Renderer();
  private final JFrame window = new JFrame("Banana Kick");
  private GLJPanel drawingPanel;
  private List<Point> trajectory;
  private boolean started = false;
  private Mobile ball;
  private boolean isClosed = false;
  private GLProfile glProfile;
  private Texture ballTexture;

  private Renderer() {
  }

  public static int getDrawingPanelWidth() {
    return DRAWING_PANEL_WIDTH;
  }

  public static int getDrawingPanelHeight() {
    return DRAWING_PANEL_HEIGHT;
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
      @Override
      public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height) {
        setup(glautodrawable.getGL().getGL2(), width, height);
      }

      @Override
      public void init(GLAutoDrawable glautodrawable) {
        GL2 gl2 = glautodrawable.getGL().getGL2();
        gl2.glMatrixMode(GL_MODELVIEW);
        loadTextures();
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
    DRAWING_PANEL_WIDTH = width;
    DRAWING_PANEL_HEIGHT = height;
    gl2.glViewport(0, 0, width, height);
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

  private void loadTextures() {
    try {
      InputStream stream = getClass().getResourceAsStream("/res/football_32x32.png");
      TextureData data = TextureIO.newTextureData(glProfile, stream, false, "png");
      ballTexture = TextureIO.newTexture(data);
    }
    catch (IOException exc) {
      throw new RuntimeException(exc);
    }
  }

  public void render(GL2 gl2, int width, int height) {
    // Clear the screen
    gl2.glClearColor(0.0f, 1.0f, 1.0f, 1.0f);
    gl2.glClear(GL_COLOR_BUFFER_BIT);

    drawGround(gl2);
    drawTrajectory(gl2);
    drawBall(gl2);
  }

  private void drawGround(GL2 gl2) {
    float groundLevel = metersToUniformCoordinatesY(Game.getGroundY());
    gl2.glColor3f(0.0f, 0.7f, 0.0f);
    gl2.glBegin(GL_QUADS);
    gl2.glVertex2f(-1.0f, -1.0f);
    gl2.glVertex2f(-1.0f, -1.0f + groundLevel);
    gl2.glVertex2f(1.0f, -1.0f + groundLevel);
    gl2.glVertex2f(1.0f, -1.0f);
    gl2.glEnd();
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

    gl2.glEnableClientState(GL_VERTEX_ARRAY);
    gl2.glVertexPointer(2, GL_FLOAT, 0, pointBuffer);

    ShortBuffer indicesBuffer = ShortBuffer.allocate(numberOfPoints * 4 - 4);
    for (short i = 0; i < 2 * numberOfPoints - 2; i++) {
      indicesBuffer.put(i);
      indicesBuffer.put((short) (i + 1));
    }
    indicesBuffer.flip();

    gl2.glColor3f(1.0f, 0.0f, 0.0f);
    gl2.glDrawElements(GL_LINES, 2 * numberOfPoints - 2, GL_UNSIGNED_SHORT, indicesBuffer);

    gl2.glDisableClientState(GL_VERTEX_ARRAY);
  }

  private void drawBall(GL2 gl2) {

    gl2.glColor3f(1.0f, 1.0f, 1.0f);
    gl2.glEnable(GL_TEXTURE_2D);

    gl2.glEnable(GL_BLEND);
    gl2.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    gl2.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    gl2.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    gl2.glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, GL_TRUE);

    ballTexture.enable(gl2);
    ballTexture.bind(gl2);

    gl2.glPushMatrix();
    gl2.glTranslatef(
        -1.0f + metersToUniformCoordinatesX(ball.getPosition().x),
        -1.0f + metersToUniformCoordinatesY(ball.getPosition().y),
        0.0f
    );
    gl2.glScalef(
        metersToUniformCoordinatesX(ball.getSize().x) / 2.0f,
        metersToUniformCoordinatesY(ball.getSize().y) / 2.0f,
        1.0f
    );
    gl2.glRotatef((float) ball.getAngle(), 0.0f, 0.0f, 1.0f);

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

    gl2.glDisable(GL_BLEND);
    gl2.glDisable(GL_TEXTURE_2D);
  }

  public boolean shouldClose() {
    return isClosed;
  }

  public boolean hasStarted() {
    return true;
//    return started;
  }

  public void draw() {
    drawingPanel.display();
  }
}
