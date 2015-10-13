package ro.licj.magnus;

import org.newdawn.slick.*;
import ro.licj.magnus.util.Point;
import ro.licj.magnus.util.Vector;

import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Game extends BasicGame {

  private Image img;
  public Game(String gamename)
  {
    super(gamename);
  }

  @Override
  public void init(GameContainer gc) throws SlickException {
    img = new Image("res/football.png").getScaledCopy(0.5f);
  }

  @Override
  public void update(GameContainer gc, int i) throws SlickException {}

  @Override
  public void render(GameContainer gc, Graphics g) throws SlickException
  {
    g.drawString("Howdy!", 10, 10);
    img.draw(20, 20);
    img.rotate(0.5f);
  }

  public static void main(String[] args)
  {
    try
    {
      AppGameContainer appgc;
      appgc = new AppGameContainer(new Game("Gameee"));
      appgc.setDisplayMode(640, 480, false);
      appgc.start();
    }
    catch (SlickException ex)
    {
      Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
/*
  private static Game instance = new Game();

  private Game() {
  }

  public static Game getInstance() {
    return instance;
  }

  private static final double AIR_DENSITY = 1.2;
  private static final double DRAG_COEFFICIENT = 0.47;
  private static final double BALL_CROSS_SURFACE = 0.0336;

  private Renderer renderer = Renderer.getInstance();
  private Mobile ball = new Mobile(new Point(1.0, 1.15), 0.42, new Vector(0.23, 0.23));
  private double groundY = 1.0;
  private boolean gameOver = false;

  private List<Point> trajectory = new ArrayList<Point>();

  public void run() {
    ball.setSpeed(new Vector(1.0, 10.0));
    try {
      renderer.init(ball, trajectory);
      loop();
    }
    catch (Exception ex) {
      System.out.println("An error has occured: " + ex.toString());
      ex.printStackTrace();
    }
    finally {
      renderer.terminate();
    }
  }



  private void loop() {
    Timer timer = new Timer();

    renderer.prepareForDrawing();

    while (!renderer.shouldClose()) {
      if (timer.shouldUpdateGame() && renderer.hasStarted()) {
        timer.update();
        update();
      }

      renderer.render(timer.getDelta());
      renderer.getUserInput();
    }
  }

  private void update() {
    if (!gameOver) {
      trajectory.add(new Point(ball.getPosition().x, ball.getPosition().y));

      if (ball.getPosition().y - ball.getSize().y / 2 <= groundY) {
        Vector newSpeed = ball.getSpeed();
        newSpeed.y = -newSpeed.y;
        ball.setSpeed(newSpeed);
        // gameOver = true;
      }
      Vector gravitationalForce = new Vector(0.0, -9.81 * ball.getMass());
      double coeff = - AIR_DENSITY * DRAG_COEFFICIENT * BALL_CROSS_SURFACE / 2;
      Vector frictionForce = Vector.product(coeff, Vector.product(ball.getSpeed(), ball.getSpeed()));

      ball.applyForce(Vector.sum(gravitationalForce, frictionForce));
      ball.updatePosition();
    }
  }

  public static void main(String[] args) {
    Game.getInstance().run();
  }*/
}
