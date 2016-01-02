package ro.licj.magnus;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import ro.licj.magnus.ui.*;
import ro.licj.magnus.util.Point;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
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
  private static final int PIXELS_PER_METER_MAX = 125;
  private static final int PIXELS_PER_METER_MIN = 5;
  private static final float BALL_POSITION_UNIFORM_MAX = 0.75f;
  private static final float BALL_POSITION_UNIFORM_MIN = -0.75f;
  private static final float CLOUD_SIZE_X_MAX = 1.0f;
  private static final float CLOUD_SIZE_Y_MAX = CLOUD_SIZE_X_MAX / 2.0f;
  private static final float CLOUD_SIZE_X_MIN = 0.8f;
  private static final float CLOUD_SIZE_Y_MIN = CLOUD_SIZE_X_MIN / 2.0f;
  private static int PIXELS_PER_METER = 75;
  private static int DRAWING_PANEL_WIDTH;
  private static int DRAWING_PANEL_HEIGHT;
  private static Renderer instance = new Renderer();
  private final JFrame window = new JFrame("Banana Kick");
  private GLJPanel drawingPanel;
  private boolean started = false;
  private Mobile ball;
  private boolean isClosed = false;
  private GLProfile glProfile;
  private Texture ballTexture;
  private Texture cloudsTexture;
  private volatile double initialSpeed;
  private volatile double initialDirection;
  private volatile boolean restart = false;
  private volatile boolean clear = false;
  private JLabel speedLabel = new JLabel("");
  private JLabel directionLabel = new JLabel("");
  private JLabel dragCoefficientLabel = new JLabel("");
  private JLabel angularVelocityLabel = new JLabel("");
  private float cameraX;
  private float cameraY;
  private double dragCoefficient;
  private GenericTableModel<Trajectory> trajectoriesTableModel;
  private List<Trajectory> trajectories;
  private double angularVelocity;
  private JLabel speedXJLabel = new JLabel();
  private JLabel speedYJLabel = new JLabel();
  private JLabel magnusForceJLabel = new JLabel();
  private volatile double speedX = 0.0;
  private volatile double speedY = 0.0;
  private volatile double magnusForce = 0.0;

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

  public void init(Mobile ball, List<Trajectory> trajectories) {
    this.ball = ball;
    this.trajectories = trajectories;

    window.getContentPane().add(createTrajectoriesTablePanel(), BorderLayout.EAST);

    window.getContentPane().add(createToolboxPanel(), BorderLayout.SOUTH);

    initDrawingPanel();
    window.getContentPane().add(drawingPanel, BorderLayout.CENTER);

    window.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent windowevent) {
        window.dispose();
        isClosed = true;
      }
    });

    window.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    window.setExtendedState(Frame.MAXIMIZED_BOTH);
    window.setVisible(true);
  }

  private JScrollPane createTrajectoriesTablePanel() {
    trajectoriesTableModel = new GenericTableModel<Trajectory>(TrajectoryColumnifier.getInstance());
    JTable trajectoriesJTable = new JTable(trajectoriesTableModel);
    trajectoriesJTable.setDefaultRenderer(ro.licj.magnus.ui.Color.class, new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        ro.licj.magnus.ui.Color displayedColor = (ro.licj.magnus.ui.Color) value;

        c.setBackground(new java.awt.Color(displayedColor.r, displayedColor.g, displayedColor.b));

        return c;
      }
    });
    JScrollPane panel = new JScrollPane(trajectoriesJTable);
    panel.setPreferredSize(new Dimension(150, 0));

    return panel;
  }

  private JPanel createToolboxPanel() {
    JPanel toolboxPanel = new JPanel();
    toolboxPanel.setLayout(new BoxLayout(toolboxPanel, BoxLayout.LINE_AXIS));

    toolboxPanel.add(createInformationPanel());
    toolboxPanel.add(createSelectorsPanel());
    toolboxPanel.add(createButtonsPanel());

    return toolboxPanel;
  }

  private JPanel createInformationPanel() {
    JPanel informationPanelContainer = new JPanel(new BorderLayout());

    JPanel informationPanel = new JPanel();
    informationPanel.setBorder(new EmptyBorder(30, 0, 30, 0));
    informationPanel.setLayout(new GridLayout(3, 2, 5, 5));
    JLabel speedXTextJLabel = new JLabel("Speed X:");
    speedXTextJLabel.setHorizontalAlignment(JLabel.RIGHT);
    JLabel speedYTextJLabel = new JLabel("Speed Y:");
    speedYTextJLabel.setHorizontalAlignment(JLabel.RIGHT);
    JLabel magnusForceTextJLabel = new JLabel("Magnus force:");
    magnusForceTextJLabel.setHorizontalAlignment(JLabel.RIGHT);
    refreshInformationPanel();
    informationPanel.add(speedXTextJLabel);
    informationPanel.add(speedXJLabel);
    informationPanel.add(speedYTextJLabel);
    informationPanel.add(speedYJLabel);
    informationPanel.add(magnusForceTextJLabel);
    informationPanel.add(magnusForceJLabel);

    informationPanelContainer.add(informationPanel, BorderLayout.NORTH);

    return informationPanelContainer;
  }

  public void refreshInformationPanel() {
    speedXJLabel.setText(String.format("%.2f m/s", speedX));
    speedYJLabel.setText(String.format("%.2f m/s", speedY));
    magnusForceJLabel.setText(String.format("%.2f N", magnusForce));
  }

  private JPanel createButtonsPanel() {
    JPanel buttonsPanel = new JPanel(new GridBagLayout());
    GridBagConstraints buttonConstraints = new GridBagConstraints();
    buttonConstraints.fill = GridBagConstraints.HORIZONTAL;
    buttonConstraints.insets = new Insets(5, 5, 5, 5);

    JButton startButton = new JButton("Start");
    startButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        start();
      }
    });
    buttonConstraints.gridx = 0;
    buttonConstraints.gridy = 0;
    buttonsPanel.add(startButton, buttonConstraints);

    JButton restartButton = new JButton("Restart");
    restartButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        restart = true;
      }
    });
    buttonConstraints.gridx = 1;
    buttonConstraints.gridy = 0;
    buttonsPanel.add(restartButton, buttonConstraints);

    JButton clearButton = new JButton("Clear");
    clearButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        clear = true;
      }
    });
    buttonConstraints.gridx = 2;
    buttonConstraints.gridy = 0;
    buttonsPanel.add(clearButton, buttonConstraints);

    JButton zoomInButton = new JButton("Zoom in");
    zoomInButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        zoomIn();
      }
    });
    buttonConstraints.gridx = 0;
    buttonConstraints.gridy = 1;
    buttonsPanel.add(zoomInButton, buttonConstraints);

    JButton zoomOutButton = new JButton("Zoom out");
    zoomOutButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        zoomOut();
      }
    });
    buttonConstraints.gridx = 1;
    buttonConstraints.gridy = 1;
    buttonsPanel.add(zoomOutButton, buttonConstraints);
    return buttonsPanel;
  }

  private JPanel createSelectorsPanel() {
    JPanel selectorsPanel = new JPanel();
    selectorsPanel.setLayout(new BoxLayout(selectorsPanel, BoxLayout.LINE_AXIS));

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

    selectedAngularVelocity(50);
    JPanel angularVelocitySelector = createPropertySelector("Angular frequency", new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        JSlider angularVelocitySelector = (JSlider) e.getSource();
        selectedAngularVelocity(angularVelocitySelector.getValue());
      }
    });
    angularVelocitySelector.add(angularVelocityLabel);
    selectorsPanel.add(angularVelocitySelector);

    return selectorsPanel;
  }

  private void initDrawingPanel() {
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
  }

  private void zoomIn() {
    if (PIXELS_PER_METER + 5 <= PIXELS_PER_METER_MAX) {
      PIXELS_PER_METER += 5;
    } else {
      PIXELS_PER_METER = PIXELS_PER_METER_MAX;
    }
    resetCamera();
  }

  private void zoomOut() {
    if (PIXELS_PER_METER - 5 >= PIXELS_PER_METER_MIN) {
      PIXELS_PER_METER -= 5;
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
   * @param selectedDirectionPercent a number between 0 and 100.
   */
  private void selectedInitialDirection(int selectedDirectionPercent) {
    // Scale to 0 - PI/2
    double initialDirectionDegrees = (double) selectedDirectionPercent * 90.0 / 100.0;
    initialDirection = (double) selectedDirectionPercent * Math.PI / 200.0;
    directionLabel.setText(String.format("%.2f", initialDirectionDegrees) + " °");
  }

  /**
   * @param selectedSpeedPercent a number between 0 and 100.
   */
  private void selectedInitialSpeed(int selectedSpeedPercent) {
    // Scale to 1-35 m/sec.
    initialSpeed = (double) selectedSpeedPercent * 34.0 / 100.0;
    initialSpeed += 1.0;
    speedLabel.setText(String.format("%.2f", initialSpeed) + " m/s");
  }

  private void selectedDragCoefficient(int selectedDragCoeffPercent) {
    // Scale to 0-0.142
    dragCoefficient = (double) selectedDragCoeffPercent * 0.142 / 100.0;
    dragCoefficientLabel.setText(String.format("%.2f", dragCoefficient));
  }

  private void selectedAngularVelocity(int angularVelocityPercent) {
    // Scale to 0-10 rotations/s = 0-10*2pi rad/s
    double angularVelocityRotations = (double) angularVelocityPercent / 10.0;
    angularVelocity = angularVelocityRotations * 2 * Math.PI;
    angularVelocityLabel.setText(String.format("%.2f", angularVelocityRotations) + " rev/s");
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
      throw new RuntimeException("Failed to load the ball texture!", exc);
    }
    try {
      InputStream stream = getClass().getResourceAsStream("/res/clouds.png");
      TextureData data = TextureIO.newTextureData(glProfile, stream, false, "png");
      cloudsTexture = TextureIO.newTexture(data);
    } catch (IOException exc) {
      throw new RuntimeException("Failed to load the ball texture!", exc);
    }
  }

  public void render(GL2 gl2, int width, int height) {
    // Clear the screen
    gl2.glClearColor(0.0f, 1.0f, 1.0f, 1.0f);
    gl2.glClear(GL_COLOR_BUFFER_BIT);

    gl2.glLoadIdentity();
    float lineWidth = (float) PIXELS_PER_METER / 25;
    if (lineWidth <= 2.0f) {
      lineWidth = 2.0f;
    }
    gl2.glLineWidth(lineWidth);
    synchronized (Game.getInstance().getBallLock()) {
      updateCameraPosition();
      drawClouds(gl2);

      gl2.glTranslatef(-cameraX, -cameraY, 0.0f);
      drawGround(gl2);
      drawTrajectories(gl2);
      if (!hasStarted()) {
        drawVelocityArrow(gl2);
      }
      drawBall(gl2);
    }
  }

  private void drawClouds(GL2 gl2) {
    gl2.glEnable(GL_TEXTURE_2D);

    gl2.glEnable(GL_BLEND);
    gl2.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    gl2.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    gl2.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

    cloudsTexture.enable(gl2);
    cloudsTexture.bind(gl2);

    float size_x = CLOUD_SIZE_X_MIN + (getPixelsPerMeter() - PIXELS_PER_METER_MIN) * (CLOUD_SIZE_X_MAX - CLOUD_SIZE_X_MIN) / (float) (PIXELS_PER_METER_MAX - PIXELS_PER_METER_MIN);
    float size_y = (CLOUD_SIZE_Y_MIN + (getPixelsPerMeter() - PIXELS_PER_METER_MIN) * (CLOUD_SIZE_Y_MAX - CLOUD_SIZE_Y_MIN) / (float) (PIXELS_PER_METER_MAX - PIXELS_PER_METER_MIN)) * getDrawingPanelWidth() / getDrawingPanelHeight();

    float move_based_on_zoom_x = - (getPixelsPerMeter() - PIXELS_PER_METER_MIN) / (float) (4.0f * (PIXELS_PER_METER_MAX - PIXELS_PER_METER_MIN));
    float move_based_on_zoom_y = - move_based_on_zoom_x / 2.0f;

    float move_based_on_camera_x = - cameraX * 2 / PIXELS_PER_METER;
    float move_based_on_camera_y = - cameraY * 2 / PIXELS_PER_METER;

    float move_x = move_based_on_zoom_x + move_based_on_camera_x;
    float move_y = move_based_on_zoom_y + move_based_on_camera_y;


    for (int cloudNumber = 0; cloudNumber < 3; cloudNumber++) {
      gl2.glPushMatrix();

      gl2.glTranslatef(
          move_x - 1.0f + size_x / 2.0f + size_x * (2 - cloudNumber),
          move_y + 1.0f - size_y / 2.0f,
          0.0f
      );

      gl2.glScalef(
          size_x / 2.0f,
          size_y / 2.0f,
          1.0f
      );

      gl2.glBegin(GL_TRIANGLES);
      gl2.glTexCoord2f(0.0f, (1.0f - 0.25f * cloudNumber));
      gl2.glVertex2f(-1.0f, 1.0f);
      gl2.glTexCoord2f(0.0f, (0.75f - 0.25f * cloudNumber));
      gl2.glVertex2f(-1.0f, -1.0f);
      gl2.glTexCoord2f(1.0f, (0.75f - 0.25f * cloudNumber));
      gl2.glVertex2f(1.0f, -1.0f);

      gl2.glTexCoord2f(0.0f, (1.0f - 0.25f * cloudNumber));
      gl2.glVertex2f(-1.0f, 1.0f);
      gl2.glTexCoord2f(1.0f, (1.0f - 0.25f * cloudNumber));
      gl2.glVertex2f(1.0f, 1.0f);
      gl2.glTexCoord2f(1.0f, (0.75f - 0.25f * cloudNumber));
      gl2.glVertex2f(1.0f, -1.0f);
      gl2.glEnd();

      gl2.glPopMatrix();
    }

    gl2.glDisable(GL_BLEND);
    gl2.glDisable(GL_TEXTURE_2D);
  }

  private void updateCameraPosition() {
    float ballX = positionMetersToUniformCoordinatesX(ball.getPosition().x);
    if (ballX - cameraX >= BALL_POSITION_UNIFORM_MAX) {
      cameraX = -BALL_POSITION_UNIFORM_MAX + ballX;
    } else if (ballX - cameraX <= BALL_POSITION_UNIFORM_MIN) {
      cameraX = -BALL_POSITION_UNIFORM_MIN + ballX;
    }
    if (cameraX <= 0.0f) {
      cameraX = 0.0f;
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
  }

  private void drawTrajectories(GL2 gl2) {
    synchronized (Game.getInstance().getTrajectoriesLock()) {
      for (Trajectory trajectory : trajectories) {
        drawTrajectory(gl2, trajectory);
      }
    }
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

  private void drawTrajectory(GL2 gl2, Trajectory trajectory) {
    synchronized (trajectory) {
      List<Point> trajectoryPoints = trajectory.getPoints();
      if (trajectoryPoints.size() <= 1) {
        return;
      }

      int numberOfPoints = trajectoryPoints.size();
      float[] tmp = new float[2 * numberOfPoints];
      for (int i = 0; i < numberOfPoints; i++) {
        Point point = trajectoryPoints.get(i);
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

      gl2.glColor3f(trajectory.getColor().r, trajectory.getColor().g, trajectory.getColor().b);
      gl2.glDrawElements(GL_LINES, 2 * numberOfPoints - 2, GL_UNSIGNED_SHORT, indicesBuffer);

      gl2.glDisableClientState(GL_VERTEX_ARRAY);
    }
  }

  private void drawBall(GL2 gl2) {

    gl2.glColor3f(1.0f, 1.0f, 1.0f);
    gl2.glEnable(GL_TEXTURE_2D);

    gl2.glEnable(GL_BLEND);
    gl2.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    gl2.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    gl2.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

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
    gl2.glRotatef((float) (ball.getAngle() * 180.0 / Math.PI), 0.0f, 0.0f, 1.0f);

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

  public double getAngularVelocity() {
    return angularVelocity;
  }

  public boolean shouldRestart() {
    return restart;
  }

  public void resetRestartFlag() {
    restart = false;
  }

  public boolean shouldClearTrajectories() {
    return clear;
  }

  public void resetClearTrajectoriesFlag() {
    clear = false;
  }

  public GenericTableModel<Trajectory> getTrajectoriesTableModel() {
    return trajectoriesTableModel;
  }

  public void setSpeedX(double speedX) {
    this.speedX = speedX;
  }

  public void setSpeedY(double speedY) {
    this.speedY = speedY;
  }

  public void setMagnusForce(double magnusForce) {
    this.magnusForce = magnusForce;
  }
}
