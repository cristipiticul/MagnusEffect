package ro.licj.magnus;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import ro.licj.magnus.shaders.ShaderProgram;
import ro.licj.magnus.util.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.glfw.Callbacks.errorCallbackPrint;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * For user interface.
 */
public class Renderer {
  // We need to strongly reference callback instances.
  private GLFWErrorCallback errorCallback;
  private GLFWKeyCallback keyCallback;

  private static final int WINDOW_WIDTH = 1024;
  private static final int WINDOW_HEIGHT = 720;
  private static int PIXELS_PER_METER = 100;
  private List<Point> trajectory;
  private boolean started = false;

  public static int getWindowWidth() {
    return WINDOW_WIDTH;
  }

  public static int getWindowHeight() {
    return WINDOW_HEIGHT;
  }

  public static int getPixelsPerMeter() {
    return PIXELS_PER_METER;
  }

  // The window handle
  private long window;

  private static Renderer instance = new Renderer();
  private int textureID;
  private int textureUniformLocation;
  private int matrixUniformLocation;

  private Mobile ball;

  public static Renderer getInstance() {
    return instance;
  }

  private Renderer() {
  }

  public void init(Mobile ball, List<Point> trajectory) {
    this.ball = ball;
    this.trajectory = trajectory;
    // Setup an error callback. The default implementation
    // will print the error message in System.err.
    glfwSetErrorCallback(errorCallback = errorCallbackPrint(System.err));

    // Initialize GLFW. Most GLFW functions will not work before doing this.
    if (glfwInit() != GL11.GL_TRUE)
      throw new IllegalStateException("Unable to initialize GLFW");

    // Configure our window
    glfwDefaultWindowHints(); // optional, the current window hints are already the defaultglfwDefaultWindowHints();
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
    glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
    glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);

    // Create the window
    window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Banana Kick", NULL,
        NULL);
    if (window == NULL)
      throw new RuntimeException("Failed to create the GLFW window");

    // Setup a key callback. It will be called every time a key is pressed, repeated or released.
    glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
      @Override
      public void invoke(long window, int key, int scanCode, int action,
          int mods) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
          glfwSetWindowShouldClose(window,
              GL_TRUE); // We will detect this in our rendering loop
        if (key == GLFW_KEY_KP_SUBTRACT && action == GLFW_PRESS) {
          if (PIXELS_PER_METER >= 60) {
            PIXELS_PER_METER -= 10;
          }
        }
        if (key == GLFW_KEY_KP_ADD && action == GLFW_PRESS) {
          if (PIXELS_PER_METER <= 1000) {
            PIXELS_PER_METER += 10;
          }
        }
        if (key == GLFW_KEY_ENTER && action == GLFW_PRESS) {
          started = true;
        }
      }
    });

    // Get the resolution of the primary monitor
    ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
    // Center our window
    glfwSetWindowPos(
        window,
        (GLFWvidmode.width(vidmode) - WINDOW_WIDTH) / 2,
        (GLFWvidmode.height(vidmode) - WINDOW_HEIGHT) / 2
    );

    // Make the OpenGL context current
    glfwMakeContextCurrent(window);
    // Enable v-sync
    glfwSwapInterval(1);

    // Make the window visible
    glfwShowWindow(window);
  }

  public void terminate() {
    ShaderProgram.disposeAll();

    if (window != NULL) {
      // Release window and window callbacks
      glfwDestroyWindow(window);
      keyCallback.release();
    }

    // Terminate GLFW and release the GLFWerrorfun
    glfwTerminate();
    errorCallback.release();
  }

  public void prepareForDrawing() {
    GL.createCapabilities();

    ShaderProgram.initAll();

    textureID = glGenTextures();
    glBindTexture(GL_TEXTURE_2D, textureID);
    PNGTexture football = PNGTexture.FOOTBALL;
    glTexImage2D(GL_TEXTURE_2D, 0, football.getInternalFormat(),
        football.getWidth(), football.getHeight(), 0,
        football.getInternalFormat(), GL_UNSIGNED_BYTE,
        football.getByteBuffer());
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
    glGenerateMipmap(GL_TEXTURE_2D);

    textureUniformLocation = glGetUniformLocation(ShaderProgram.BALL_TEXTURE_SHADER.getID(),
        "myTextureSampler");
    matrixUniformLocation = glGetUniformLocation(ShaderProgram.BALL_TEXTURE_SHADER.getID(), "MVP");
  }

  public void render(double delta) {
    // Clear the screen
    glClearColor(0.0f, 1.0f, 1.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    drawTrajectory();
    drawBall();

    glfwSwapBuffers(window); // swap the color buffers
  }

  private void drawTrajectory() {
    if (trajectory.size() <= 1) {
      return;
    }
    FloatBuffer pointBuffer = BufferUtils.createFloatBuffer(trajectory.size() * 4 - 4);
    for (int i = 0; i < trajectory.size(); i++) {
      Point current = trajectory.get(i);
      float x = -1.0f + UnitConverter.metersToUniformCoordinatesX(current.x);
      float y = -1.0f + UnitConverter.metersToUniformCoordinatesY(current.y);
      pointBuffer.put(x);
      pointBuffer.put(y);
      // The first and last points must be put only once.
      if (i != 0 && i != trajectory.size() - 1) {
        pointBuffer.put(x);
        pointBuffer.put(y);
      }
    }
    pointBuffer.flip();

    ShaderProgram.TRAJECTORY_LINE_SHADER.bind();

    int vaoID = glGenVertexArrays();
    glBindVertexArray(vaoID);

    int bufferID = glGenBuffers();
    glBindBuffer(GL_ARRAY_BUFFER, bufferID);
    glBufferData(GL_ARRAY_BUFFER, pointBuffer, GL_STATIC_DRAW);
    glEnableVertexAttribArray(0);
    glBindBuffer(GL_ARRAY_BUFFER, bufferID);
    glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);

    glDrawArrays(GL_LINES, 0, trajectory.size() * 2 - 2);

    glDisableVertexAttribArray(0);
    glBindVertexArray(0);

    glDeleteVertexArrays(vaoID);
    glDeleteBuffers(bufferID);

    ShaderProgram.unbind();
  }

  private void drawBall() {
    // Use our program
    ShaderProgram.BALL_TEXTURE_SHADER.bind();

    // Generate and bind a Vertex Array
    int vaoID = glGenVertexArrays();
    glBindVertexArray(vaoID);

    // The vertices of our Triangle
    float[] vertices = new float[] {
        1.0f, 1.0f,    // Top-right coordinate
        -1.0f, -1.0f,    // Bottom-left coordinate
        1.0f, -1.0f,    // Bottom-right coordinate

        -1.0f, 1.0f,    // Top-left coordinate
        1.0f, 1.0f,    // Top-right coordinate
        -1.0f, -1.0f     // Bottom-left coordinate
    };

    float[] model =
        Matrix3f.multiply(
            Matrix3f.translationMatrix(
                -1.0f + UnitConverter.metersToUniformCoordinatesX(ball.getPosition().x),
                -1.0f + UnitConverter.metersToUniformCoordinatesY(ball.getPosition().y)
            ),
            Matrix3f.multiply(
                Matrix3f.scaleMatrix(
                    UnitConverter.metersToUniformCoordinatesX(ball.getSize().x) / 2.0f,
                    UnitConverter.metersToUniformCoordinatesY(ball.getSize().y) / 2.0f
                ),
                Matrix3f.rotationMatrix(ball.getAngle())
            )
        );

    float[] view = new float[] {
        1.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 1.0f
    };

    float[] projection = new float[] {
        1.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 1.0f
    };

    float[] modelView = Matrix3f.multiply(model, view);
    float[] modelViewProjection = Matrix3f.multiply(modelView, projection);

    FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(3 * 3);
    matrixBuffer.put(modelViewProjection);
    matrixBuffer.flip();
    glUniformMatrix3fv(matrixUniformLocation, true, matrixBuffer);
/*
    float[] colors = new float[]
        {
            1, 0, 0, 1,  // Red color, for the first vertex
            0, 1, 0, 1,  // Green color, for the second vertex
            0, 0, 1, 0   // Blue color, for the third vertex
        };

    FloatBuffer colorsBuffer = BufferUtils.createFloatBuffer(colors.length);
    colorsBuffer.put(colors).flip();

    int vboColID = glGenBuffers();
    glBindBuffer(GL_ARRAY_BUFFER, vboColID);
    glBufferData(GL_ARRAY_BUFFER, colorsBuffer, GL_STATIC_DRAW);

    glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 0);
    glEnableVertexAttribArray(1);
*/
    float[] UVCoordinates = new float[]
        {
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,

            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f
        };
    FloatBuffer UVBuffer = BufferUtils.createFloatBuffer(UVCoordinates.length);
    UVBuffer.put(UVCoordinates).flip();
    int vboUVCoordID = glGenBuffers();
    glBindBuffer(GL_ARRAY_BUFFER, vboUVCoordID);
    glBufferData(GL_ARRAY_BUFFER, UVBuffer, GL_STATIC_DRAW);
    glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
    glEnableVertexAttribArray(1);

    // Create a FloatBuffer of vertices
    FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
    verticesBuffer.put(vertices).flip();

    // Create a Buffer Object and upload the vertices buffer
    int vboID = glGenBuffers();
    glBindBuffer(GL_ARRAY_BUFFER, vboID);
    glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);

    // Point the buffer at location 0, the location we set
    // inside the vertex shader. You can use any location
    // but the locations should match
    glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
    glBindVertexArray(0);

    // Bind the vertex array and enable our location
    glBindVertexArray(vaoID);
    glEnableVertexAttribArray(0);

    glBindTexture(GL_TEXTURE_2D, textureID);
    glUniform1i(textureUniformLocation, 0);

    // For transparency to work, we need to turn on GL_BLEND.
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    // Draw a triangle of 3 vertices
    glDrawArrays(GL_TRIANGLES, 0, 6);

    // Disable transparency.
    glDisable(GL_BLEND);

    // Disable our location
    glDisableVertexAttribArray(0);
    glBindVertexArray(0);

    // Un-bind our program
    ShaderProgram.unbind();

    glDeleteBuffers(vboID);
    glDeleteBuffers(vboUVCoordID);
    glDeleteVertexArrays(vaoID);
  }

  public boolean shouldClose() {
    return glfwWindowShouldClose(window) == GL_TRUE;
  }

  public void getUserInput() {
    // Poll for window events. The key callback above will only be
    // invoked during this call.
    glfwPollEvents();
  }

  public boolean hasStarted() {
    return started;
  }
}
