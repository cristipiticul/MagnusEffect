package ro.licj.magnus;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import ro.licj.magnus.util.Point;
import ro.licj.magnus.util.Vector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

class OneTriangle {
  protected static void setup( GL2 gl2, int width, int height ) {
    gl2.glMatrixMode( GL2.GL_PROJECTION );
    gl2.glLoadIdentity();

    // coordinate system origin at lower left with width and height same as the window
    GLU glu = new GLU();
    glu.gluOrtho2D( 0.0f, width, 0.0f, height );

    gl2.glMatrixMode( GL2.GL_MODELVIEW );
    gl2.glLoadIdentity();

    gl2.glViewport( 0, 0, width, height );
  }

  protected static void render( GL2 gl2, int width, int height ) {
    gl2.glClear( GL.GL_COLOR_BUFFER_BIT );

    // draw a triangle filling the window
    gl2.glLoadIdentity();
    gl2.glBegin( GL.GL_TRIANGLES );
    gl2.glColor3f( 1, 0, 0 );
    gl2.glVertex2f( 0, 0 );
    gl2.glColor3f( 0, 1, 0 );
    gl2.glVertex2f( width, 0 );
    gl2.glColor3f( 0, 0, 1 );
    gl2.glVertex2f( width / 2, height );
    gl2.glEnd();
  }
}

public class Game {

  public static void main(String args[]) {
    GLProfile glprofile = GLProfile.getDefault();
    GLCapabilities glcapabilities = new GLCapabilities( glprofile );
    GLJPanel gljpanel = new GLJPanel( glcapabilities );

    gljpanel.addGLEventListener( new GLEventListener() {

      @Override
      public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {
        OneTriangle.setup( glautodrawable.getGL().getGL2(), width, height );
      }

      @Override
      public void init( GLAutoDrawable glautodrawable ) {
      }

      @Override
      public void dispose( GLAutoDrawable glautodrawable ) {
      }

      @Override
      public void display( GLAutoDrawable glautodrawable ) {
        OneTriangle.render( glautodrawable.getGL().getGL2(), glautodrawable.getSurfaceWidth(), glautodrawable.getSurfaceHeight() );
      }
    });

    final JFrame jframe = new JFrame( "One Triangle Swing GLJPanel" );
    jframe.addWindowListener( new WindowAdapter() {
      public void windowClosing( WindowEvent windowevent ) {
        jframe.dispose();
        System.exit( 0 );
      }
    });

    jframe.getContentPane().add( gljpanel, BorderLayout.CENTER );
    jframe.setSize( 640, 480 );
    jframe.setVisible( true );
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
