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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.jogamp.opengl.GL2.*;
import static ro.licj.magnus.util.UnitConverter.*;

/**
 * For user interface.
 */
public class Renderer {

  private static final int WINDOW_WIDTH = 1024;
  private static final int WINDOW_HEIGHT = 720;
  private static final int PIXELS_PER_METER_MAX = 125;
  private static final int PIXELS_PER_METER_MIN = 25;
  private static final float BALL_POSITION_UNIFORM_MAX = 0.75f;
  private static final float BALL_POSITION_UNIFORM_MIN = -0.75f;
  private static int PIXELS_PER_METER = 75;
  private static int DRAWING_PANEL_WIDTH;
  private static int DRAWING_PANEL_HEIGHT;
  private static Renderer instance = new Renderer();
  private final JFrame window = new JFrame("Banana Kick");
  private GLJPanel drawingPanel;
  private List<Point> trajectory;
  private List<List<Point>> oldTrajectories = new ArrayList<List<Point>>();
  private boolean started = false;
  private Mobile ball;
  private boolean isClosed = false;
  private GLProfile glProfile;
  private Texture ballTexture;
  private volatile double initialSpeed;
  private volatile double initialDirection;
  private volatile boolean restart = false;
  private JLabel speedLabel = new JLabel("");
  private JLabel directionLabel = new JLabel("");
  private JLabel dragCoefficientLabel = new JLabel("");
  private float cameraX;
  private float cameraY;
  private double dragCoefficient;

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

  public double getDragCoefficient() {
    return dragCoefficient;
  }

  public void init(Mobile ball, List<Point> trajectory) {
    this.ball = ball;
    this.trajectory = trajectory;

    glProfile = GLProfile.getDefault();
    GLCapabilities glcapabilities = new GLCapabilities(glProfile);
    glcapabilities.setDoubleBuffered(true);
    drawingPanel = new GLJPanel(glcapabilities);

    JPanel toolboxPanel = new JPanel();
    toolboxPanel.setLayout(new BoxLayout(toolboxPanel, BoxLayout.LINE_AXIS));

    JPanel selectorsPanel = new JPanel();
    selectorsPanel.setLayout(new BoxLayout(selectorsPanel, BoxLayout.PAGE_AXIS));
    toolboxPanel.add(selectorsPanel);

    JPanel buttonsPanel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.insets = new Insets(5,0,5,0);
    toolboxPanel.add(buttonsPanel);

    selectedInitialSpeed(50);
    JPanel speedSelector = createPropertySelector("Speed", new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        JSlider speedSelector = (JSlider) e.getSource();
        selectedInitialSpeed(speedSelector.getValue());
      }
    });
    speedSelector.add(speedLabel);
    selectorsPanel.add(speedSelector);

    selectedInitialDirection(50);
    JPanel directionSelector = createPropertySelector("Direction", new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        JSlider directionSelector = (JSlider) e.getSource();
        selectedInitialDirection(directionSelector.getValue());
      }
    });
    directionSelector.add(directionLabel);
    selectorsPanel.add(directionSelector);

    selectedDragCoefficient(50);
    JPanel dragCoefficientSelector = createPropertySelector("Drag coefficient", new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        JSlider dragCoefficientSelector = (JSlider) e.getSource();
        selectedDragCoefficient(dragCoefficientSelector.getValue());
      }
    });
    dragCoefficientSelector.add(dragCoefficientLabel);
    selectorsPanel.add(dragCoefficientSelector);

    JButton startButton = new JButton("Start");
    startButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        start();
      }
    });
    constraints.gridx = 0;
    constraints.gridy = 0;
    buttonsPanel.add(startButton, constraints);

    JButton restartButton = new JButton("Restart");
    restartButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setRestart(true);
      }
    });
    constraints.gridx = 0;
    constraints.gridy = 1;
    buttonsPanel.add(restartButton, constraints);

    JButton clearButton = new JButton("Clear");
    clearButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        oldTrajectories.clear();
      }
    });
    constraints.gridx = 0;
    constraints.gridy = 2;
    buttonsPanel.add(clearButton, constraints);

    JButton zoomInButton = new JButton("Zoom in");
    zoomInButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        zoomIn();
      }
    });
    constraints.gridx = 0;
    constraints.gridy = 3;
    buttonsPanel.add(zoomInButton, constraints);

    JButton zoomOutButton = new JButton("Zoom out");
    zoomOutButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        zoomOut();
      }
    });
    constraints.gridx = 0;
    constraints.gridy = 4;
    buttonsPanel.add(zoomOutButton, constraints);

    window.getContentPane().add(drawingPanel, BorderLayout.CENTER);

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

    window.getContentPane().add(toolboxPanel, BorderLayout.SOUTH);
    window.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    window.setVisible(true);
  }

  private void zoomIn() {
    if (PIXELS_PER_METER + 25 <= PIXELS_PER_METER_MAX) {
      PIXELS_PER_METER += 25;
    } else {
      PIXELS_PER_METER = PIXELS_PER_METER_MAX;
    }
    resetCamera();
  }

  private void zoomOut() {
    if (PIXELS_PER_METER - 25 >= PIXELS_PER_METER_MIN) {
      PIXELS_PER_METER -= 25;
    } else {
      PIXELS_PER_METER = PIXELS_PER_METER_MIN;
    }
    resetCamera();
  }

  private void resetCamera() {
    cameraX = 0.0f;
    cameraY = 0.0f;
  }

  /**
   * @param selectedDirection a number between 0 and 100.
   */
  private void selectedInitialDirection(int selectedDirection) {
    // Scale to 0 - PI/2
    double initialDirectionDegrees = (double) selectedDirection * 90.0 / 100.0;
    initialDirection = (double) selectedDirection * Math.PI / 200.0;
    directionLabel.setText(String.format("%.2f", initialDirectionDegrees) + " °");
  }

  /**
   * @param selectedSpeed a number between 0 and 100.
   */
  private void selectedInitialSpeed(int selectedSpeed) {
    // Scale to 1-35 m/sec.
    initialSpeed = (double) selectedSpeed * 34.0 / 100.0;
    initialSpeed += 1.0;
    speedLabel.setText(String.format("%.2f", initialSpeed) + " m/s");
  }

  private void selectedDragCoefficient(int selectedDragCoeff) {
    // Scale to 0-0.14
    dragCoefficient = (double) selectedDragCoeff * 0.14 / 100.0;
    dragCoefficientLabel.setText(String.format("%.2f", dragCoefficient));
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

    gl2.glLoadIdentity();
    float lineWidth = (float) PIXELS_PER_METER / 25;
    if (lineWidth <= 1.0f) {
      lineWidth = 1.0f;
    }
    gl2.glLineWidth(lineWidth);
    synchronized (Game.getBallLock()) {
      float ballX = positionMetersToUniformCoordinatesX(ball.getPosition().x);
      if (ballX - cameraX >= BALL_POSITION_UNIFORM_MAX) {
        cameraX = -BALL_POSITION_UNIFORM_MAX + ballX;
      } else if (ballX - cameraX <= BALL_POSITION_UNIFORM_MIN) {
        cameraX = -BALL_POSITION_UNIFORM_MIN + ballX;
      }

      float ballY = positionMetersToUniformCoordinatesY(ball.getPosition().y);
      if (-cameraY + ballY >= BALL_POSITION_UNIFORM_MAX) {
        cameraY = -BALL_POSITION_UNIFORM_MAX + ballY;
      } else if (ballY - cameraY <= BALL_POSITION_UNIFORM_MIN) {
        cameraY = -BALL_POSITION_UNIFORM_MIN + ballY;
      }
      if (cameraY <= 0.0f) {
        cameraY = 0.0f;
      }
      if (cameraX <= 0.0f) {
        cameraX = 0.0f;
      }
      gl2.glTranslatef(-cameraX, -cameraY, 0.0f);
      drawGround(gl2);
      drawTrajectories(gl2);
      if (!hasStarted()) {
        drawVelocityArrow(gl2);
      }
      drawBall(gl2);
    }
  }

  private void drawTrajectories(GL2 gl2) {
    for (List<Point> oldTrajectory : oldTrajectories) {
      drawTrajectory(gl2, oldTrajectory, 0.5f, 0.5f, 0.5f);
    }
    drawTrajectory(gl2, trajectory, 1.0f, 0.0f, 0.0f);
  }

  private void drawVelocityArrow(GL2 gl2) {
    gl2.glPushMatrix();

    gl2.glColor3f(0.0f, 0.0f, 1.0f);

    double speedX = ball.getSpeed().x;
    double speedY = ball.getSpeed().y;
    double speed = ball.getSpeed().length();
    float uniformX = metersToUniformCoordinatesX(speedX / 4.0);
    float uniformY = metersToUniformCoordinatesY(speedY / 4.0);

    double speedSinA = speedY / speed;
    double speedCosA = speedX / speed;
    double sin30 = 1.0 / 2.0;
    double cos30 = Math.sqrt(3.0) / 2.0;

    double len = speed / 40.0;
    float dxLeft = metersToUniformCoordinatesX(-len * (cos30 * speedCosA + sin30 * speedSinA));
    float dyLeft = metersToUniformCoordinatesY(len * (sin30 * speedCosA - cos30 * speedSinA));
    float dxRight = metersToUniformCoordinatesX(-len * (cos30 * speedCosA - sin30 * speedSinA));
    float dyRight = metersToUniformCoordinatesY(-len * (sin30 * speedCosA + cos30 * speedSinA));

    gl2.glTranslatef(
        positionMetersToUniformCoordinatesX(ball.getPosition().x),
        positionMetersToUniformCoordinatesY(ball.getPosition().y),
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
    float groundLevel = positionMetersToUniformCoordinatesY(Game.getGroundY() + 0.2f);
    float groundMax = metersToUniformCoordinatesX(Game.getMaxGroundX());
    gl2.glColor3f(0.0f, 0.7f, 0.0f);
    gl2.glBegin(GL_QUADS);
    gl2.glVertex2f(-1.0f, -1.0f);
    gl2.glVertex2f(-1.0f, groundLevel);
    gl2.glVertex2f(-1.0f + groundMax, groundLevel);
    gl2.glVertex2f(-1.0f + groundMax, -1.0f);
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
      float x = positionMetersToUniformCoordinatesX(point.x);
      float y = positionMetersToUniformCoordinatesY(point.y);
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
        positionMetersToUniformCoordinatesX(ball.getPosition().x),
        positionMetersToUniformCoordinatesY(ball.getPosition().y),
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
