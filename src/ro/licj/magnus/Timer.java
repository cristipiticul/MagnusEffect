package ro.licj.magnus;

//import static org.lwjgl.glfw.GLFW.*;

public class Timer {
  private double lastGameUpdateTime;
  public static final double UPDATE_TIME = 0.025; // 40 UPS

  public Timer() {
    lastGameUpdateTime = getTime();
  }

  public final double getTime() {
    return 0.0;
    //return glfwGetTime();
  }

  public boolean shouldUpdateGame() {
    return getTime() - lastGameUpdateTime >= UPDATE_TIME;
  }

  public void update() {
    lastGameUpdateTime += UPDATE_TIME;
  }

  /**
   * @return The time since the last update.
   */
  public double getDelta() {
    return getTime() - lastGameUpdateTime;
  }
}
