package ro.licj.magnus;

import ro.licj.magnus.ui.ColorGenerator;
import ro.licj.magnus.util.Point;
import ro.licj.magnus.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Game {

  private static final double GROUND_Y = 1.0;
  private static final Point INITIAL_BALL_POSITION = new Point(1.0, GROUND_Y + 0.12);
  private static final float MAX_GROUND_X = 1000.0f;
  private static final double MAGNUS_COEFFICIENT = 0.01;
  private static double dragCoefficient = 0.142;
  private static Game instance = new Game();
  private final Object ballLock = new Object();
  private final Object trajectoriesLock = new Object();
  private Renderer renderer = Renderer.getInstance();
  private Mobile ball = new Mobile(new Point(INITIAL_BALL_POSITION), 0.45, new Vector(0.23, 0.23));
  private boolean gameOver = false;
  private List<Trajectory> trajectories = new ArrayList<Trajectory>();
  private Trajectory currentTrajectory;

  private Game() {
  }

  public static Game getInstance() {
    return instance;
  }

  public static Point getInitialBallPosition() {
    return INITIAL_BALL_POSITION;
  }

  public static void main(String[] args) {
    Game.getInstance().run();
  }

  public static double getGroundY() {
    return GROUND_Y;
  }

  public static float getMaxGroundX() {
    return MAX_GROUND_X;
  }

  public Object getBallLock() {
    return ballLock;
  }

  public Object getTrajectoriesLock() {
    return trajectoriesLock;
  }

  public void run() {
    currentTrajectory = new Trajectory(ColorGenerator.getInstance().next());
    try {
      renderer.init(ball, trajectories);
      restart();
      loop();
    } catch (Exception ex) {
      System.out.println("An error has occured: " + ex.toString());
      ex.printStackTrace();
    }
  }

  private void loop() {
    Timer timer = new Timer();

    while (!renderer.shouldClose()) {
      while (timer.shouldUpdateGame()) {
        timer.update();
        update();
      }

      renderer.draw();
    }
  }

  private void update() {
    if (renderer.shouldRestart()) {
      restart();

      renderer.setSpeedX(0.0);
      renderer.setSpeedY(0.0);
      renderer.setMagnusForce(0.0);
      renderer.refreshInformationPanel();

      renderer.resetRestartFlag();
      renderer.stop();
    }
    if (renderer.shouldClearTrajectories()) {
      clearTrajectories();
      renderer.resetClearTrajectoriesFlag();
    }
    if (renderer.hasStarted()) {
      if (!gameOver) {
        synchronized (ballLock) {
          Vector gravitationalForce = new Vector(0.0, -9.81 * ball.getMass());
          Vector frictionForce = Vector.product(-dragCoefficient, ball.getSpeed());
          Vector magnusForceTmp = Vector.product(MAGNUS_COEFFICIENT * ball.getAngularVelocity(), ball.getSpeed());
          Vector magnusForce = new Vector(-magnusForceTmp.y, magnusForceTmp.x);

          renderer.setSpeedX(ball.getSpeed().x);
          renderer.setSpeedY(ball.getSpeed().y);
          renderer.setMagnusForce(magnusForce.length());
          renderer.refreshInformationPanel();

          ball.applyForce(Vector.sum(Vector.sum(gravitationalForce, frictionForce), magnusForce));
          ball.update();
          if (ball.getPosition().x <= 0.0) {
            gameOver = true;
          }
          double dy = ball.getPosition().y - ball.getSize().y / 2 - GROUND_Y;
          if (dy <= 0.0) {
            double t = dy / ball.getSpeed().y;
            double dx = t * ball.getSpeed().x;
            Point newPosition = ball.getPosition();
            newPosition.x -= dx;
            newPosition.y -= dy;
            ball.setPosition(newPosition);
            gameOver = true;
          }
          currentTrajectory.addPoint(ball.getPosition());

          renderer.getTrajectoriesTableModel().refreshCell(trajectories.size() - 1, 1);
          renderer.getTrajectoriesTableModel().refreshCell(trajectories.size() - 1, 2);
        }
      }
    } else {
      double initialSpeedX = renderer.getInitialSpeed() * Math.cos(renderer.getInitialDirection());
      double initialSpeedY = renderer.getInitialSpeed() * Math.sin(renderer.getInitialDirection());
      dragCoefficient = renderer.getDragCoefficient();
      synchronized (ballLock) {
        ball.setSpeed(new Vector(initialSpeedX, initialSpeedY));
        ball.setAngularVelocity(renderer.getAngularVelocity());
      }
    }
  }

  private void clearTrajectories() {
    synchronized (trajectoriesLock) {
      trajectories.clear();
    }
    renderer.getTrajectoriesTableModel().removeAllElements();
    currentTrajectory = new Trajectory(ColorGenerator.getInstance().current());
    if (!gameOver) {
      currentTrajectory.addPoint(ball.getPosition());
      synchronized (trajectoriesLock) {
        trajectories.add(currentTrajectory);
      }
      renderer.getTrajectoriesTableModel().addElement(currentTrajectory);
    }
  }

  private void restart() {
    currentTrajectory = new Trajectory(ColorGenerator.getInstance().next());
    gameOver = false;
    ball.setPosition(new Point(INITIAL_BALL_POSITION));
    currentTrajectory.addPoint(ball.getPosition());
    synchronized (trajectoriesLock) {
      trajectories.add(currentTrajectory);
    }
    renderer.getTrajectoriesTableModel().addElement(currentTrajectory);
  }
}
