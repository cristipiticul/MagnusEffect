package ro.licj.magnus;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import ro.licj.magnus.util.Point;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
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
  private static final int PIXELS_PER_METER = 100;
  private static int DRAWING_PANEL_WIDTH;
  private static int DRAWING_PANEL_HEIGHT;
  private static Renderer instance = new Renderer();
  private final JFrame window = new JFrame("Banana Kick");
  private GLJPanel drawingPanel;
  private List<Point> trajectory;
  private List<List<Point> > oldTrajectories = new ArrayList<List<Point> >();
  private boolean started = false;
  private Mobile ball;
  private boolean isClosed = false;
  private GLProfile glProfile;
  private Texture ballTexture;
  private volatile double initialSpeed;
  private volatile double initialDirection;
  private volatile boolean restart = false;

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

    JPanel toolboxPanel = new JPanel();
    toolboxPanel.setLayout(new BoxLayout(toolboxPanel, BoxLayout.LINE_AXIS));
    selectedInitialDirection(50);
    selectedInitialSpeed(50);
    toolboxPanel.add(createPropertySelector("Speed", new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        JSlider speedSelector = (JSlider) e.getSource();
        selectedInitialSpeed(speedSelector.getValue());
      }
    }));
    toolboxPanel.add(createPropertySelector("Direction", new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        JSlider directionSelector = (JSlider) e.getSource();
        selectedInitialDirection(directionSelector.getValue());
      }
    }));
    JButton startButton = new JButton("Start");
    toolboxPanel.add(startButton);
    startButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        start();
      }
    });

    JButton restartButton = new JButton("Restart");
    toolboxPanel.add(restartButton);
    restartButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setRestart(true);
      }
    });

    JButton clearButton = new JButton("Clear");
    toolboxPanel.add(clearButton);
    clearButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        oldTrajectories.clear();
      }
    });

    window.getContentPane().add(drawingPanel, BorderLayout.CENTER);
    window.getContentPane().add(toolboxPanel, BorderLayout.SOUTH);
    window.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    window.setVisible(true);
  }


  /**
   * @param selectedDirection a number between 0 and 100.
   */
  private void selectedInitialDirection(int selectedDirection) {
    // Scale to 0 - PI/2
    initialDirection = (double) selectedDirection * Math.PI / 200.0;
  }

  /**
   * @param selectedSpeed a number between 0 and 100.
   */
  private void selectedInitialSpeed(int selectedSpeed) {
    // Scale to 1-10 m/sec.
    initialSpeed = (double) selectedSpeed * 9.0 / 100.0;
    initialSpeed += 1.0;
  }

  private void start() {
    started = true;
  }

  public void stop() {
    started = false;
  }

  private void setup(GL2 gl2, int width, int height) {
    DRAWING_PANEL_WIDTH = width;
    DRAWING_PANEL_HEIGHT = height;
    gl2.glViewport(0, 0, width, height);
  }

  private void loadTextures() {
    try {
      InputStream stream = getClass().getResourceAsStream("/res/football_32x32.png");
      TextureData data = TextureIO.newTextureData(glProfile, stream, false, "png");
      ballTexture = TextureIO.newTexture(data);
    } catch (IOException exc) {
      throw new RuntimeException(exc);
    }
  }

  public void render(GL2 gl2, int width, int height) {
    // Clear the screen
    gl2.glClearColor(0.0f, 1.0f, 1.0f, 1.0f);
    gl2.glClear(GL_COLOR_BUFFER_BIT);

    drawGround(gl2);
    drawTrajectories(gl2);
    if (!hasStarted()) {
      drawVelocityArrow(gl2);
    }
    drawBall(gl2);
  }

  private void drawTrajectories(GL2 gl2) {
    drawTrajectory(gl2, trajectory, 1.0f, 0.0f, 0.0f);
    for (List<Point> oldTrajectory : oldTrajectories) {
      drawTrajectory(gl2, oldTrajectory, 0.5f, 0.5f, 0.5f);
    }
  }

  private void drawVelocityArrow(GL2 gl2) {
    gl2.glPushMatrix();

    gl2.glColor3f(1.0f, 0.0f, 0.0f);

    double speedX = ball.getSpeed().x;
    double speedY = ball.getSpeed().y;
    double speed = ball.getSpeed().length();
    float uniformX = metersToUniformCoordinatesX(speedX / 2.0);
    float uniformY = metersToUniformCoordinatesY(speedY / 2.0);

    double speedSinA = speedY / speed;
    double speedCosA = speedX / speed;
    double sin45 = Math.sqrt(2.0) / 2;

    double len = speed / 20.0;
    float dxLeft = metersToUniformCoordinatesX(-sin45 * len * (speedCosA + speedSinA));
    float dyLeft = metersToUniformCoordinatesY(sin45 * len * (speedCosA - speedSinA));
    float dxRight = metersToUniformCoordinatesX(-sin45 * len * (speedCosA - speedSinA));
    float dyRight = metersToUniformCoordinatesY(-sin45 * len * (speedCosA + speedSinA));

    gl2.glTranslatef(
        -1.0f + metersToUniformCoordinatesX(ball.getPosition().x),
        -1.0f + metersToUniformCoordinatesY(ball.getPosition().y),
        0.0f
    );

    gl2.glBegin(GL_LINES);
    gl2.glVertex2f(0.0f, 0.0f);
    gl2.glVertex2f(uniformX, uniformY);

    gl2.glVertex2f(uniformX, uniformY);
    gl2.glVertex2f(uniformX + dxLeft, uniformY + dyLeft);

    gl2.glVertex2f(uniformX, uniformY);
    gl2.glVertex2f(uniformX + dxRight, uniformY + dyRight);
    gl2.glEnd();

    gl2.glPopMatrix();
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

  private void drawTrajectory(GL2 gl2, List<Point> trajectory, float red, float green, float blue) {
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

    gl2.glColor3f(red, green, blue);
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
    return started;
  }

  public void draw() {
    drawingPanel.display();
  }

  private JPanel createPropertySelector(String propertyName, ChangeListener changeListener) {
    JPanel propertySelectorPanel = new JPanel();
    propertySelectorPanel.setLayout(new BoxLayout(propertySelectorPanel, BoxLayout.PAGE_AXIS));
    propertySelectorPanel.setBorder(new TitledBorder(propertyName));

    propertySelectorPanel.add(new JLabel(propertyName));

    JSlider propertySelector = new JSlider();
    propertySelector.addChangeListener(changeListener);

    propertySelectorPanel.add(propertySelector);
    return propertySelectorPanel;
  }

  public double getInitialSpeed() {
    return initialSpeed;
  }

  public double getInitialDirection() {
    return initialDirection;
  }

  public boolean shouldRestart() {
    return restart;
  }

  public void setRestart(boolean restart) {
    this.restart = restart;
  }

  public void setCurrentTrajectory(List<Point> trajectory) {
    oldTrajectories.add(this.trajectory);
    this.trajectory = trajectory;
  }
}
