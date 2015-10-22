package ro.licj.magnus;

public class Timer {
  public static final double UPDATE_TIME = 0.025; // 40 UPS
  private double lastGameUpdateTime;

  public Timer() {
    lastGameUpdateTime = getTime();
  }

  public final double getTime() {
    return (double) System.currentTimeMillis() / 1000.0;
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
