package ro.licj.magnus;

import ro.licj.magnus.util.Point;
import ro.licj.magnus.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Game {

  private static final double AIR_DENSITY = 1.2;
  private static final double DRAG_COEFFICIENT = 0.47;
  private static final double BALL_CROSS_SURFACE = 0.0336;
  private static final double GROUND_Y = 1.0;
  private static final Point INITIAL_BALL_POSITION = new Point(1.0, GROUND_Y + 0.12);
  private static Game instance = new Game();
  private Renderer renderer = Renderer.getInstance();
  private Mobile ball = new Mobile(new Point(INITIAL_BALL_POSITION), 0.42, new Vector(0.23, 0.23));
  private boolean gameOver = false;
  private List<Point> trajectory = new ArrayList<Point>();

  private Game() {
  }

  public static Game getInstance() {
    return instance;
  }

  public static void main(String[] args) {
    Game.getInstance().run();
  }

  public static double getGroundY() {
    return GROUND_Y;
  }

  public void run() {
    ball.setSpeed(new Vector(1.0, 10.0));
    trajectory.add(new Point(ball.getPosition().x, ball.getPosition().y));
    try {
      renderer.init(ball, trajectory);
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
      trajectory = new ArrayList<Point>();
      renderer.setCurrentTrajectory(trajectory);
      gameOver = false;
      ball.setPosition(new Point(INITIAL_BALL_POSITION));
      renderer.setRestart(false);
      renderer.stop();
    }
    if (renderer.hasStarted()) {
      if (!gameOver) {
        if (ball.getPosition().y - ball.getSize().y / 2 <= GROUND_Y) {
          gameOver = true;
          return;
        }
        Vector gravitationalForce = new Vector(0.0, -9.81 * ball.getMass());
        double coeff = -AIR_DENSITY * DRAG_COEFFICIENT * BALL_CROSS_SURFACE / 2;
        Vector frictionForce = Vector.product(coeff, Vector.product(ball.getSpeed(), ball.getSpeed()));

        ball.applyForce(Vector.sum(gravitationalForce, frictionForce));
        ball.updatePosition();
        trajectory.add(new Point(ball.getPosition().x, ball.getPosition().y));
      }
    } else {
      double initialSpeedX = renderer.getInitialSpeed() * Math.cos(renderer.getInitialDirection());
      double initialSpeedY = renderer.getInitialSpeed() * Math.sin(renderer.getInitialDirection());
      ball.setSpeed(new Vector(initialSpeedX, initialSpeedY));
    }
  }
}
