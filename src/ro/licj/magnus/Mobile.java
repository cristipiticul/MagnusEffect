package ro.licj.magnus;

import ro.licj.magnus.util.Point;
import ro.licj.magnus.util.Vector;

public class Mobile {

  private double mass;
  private Point position;

  private double angle;
  private double frequency; // in revolutions/second

  private Vector speed;

  private Vector size;

  public Mobile(Point initialPosition, double mass, Vector size) {
    position = initialPosition;
    this.mass = mass;
    this.size = size;
    this.angle = 0.0;
    this.speed = new Vector(0.0, 0.0);
  }

  public double getMass() {
    return mass;
  }

  public void setMass(double mass) {
    this.mass = mass;
  }

  public Point getPosition() {
    return position;
  }

  public void setPosition(Point position) {
    this.position = position;
  }

  public double getAngle() {
    return angle;
  }

  public Vector getSpeed() {
    return speed;
  }

  public void setSpeed(Vector speed) {
    this.speed = speed;
  }

  public Vector getSize() {
    return size;
  }

  public void update() {
    updateAngle();
    updatePosition();
  }

  private void updateAngle() {
    angle += frequency * 2 * Math.PI * Timer.UPDATE_TIME;
    while (angle > 2 * Math.PI) {
      angle -= 2 * Math.PI;
    }
  }

  private void updatePosition() {
    position.x += speed.x * Timer.UPDATE_TIME;
    position.y += speed.y * Timer.UPDATE_TIME;
  }

  public void applyForce(Vector force) {
    speed.x += (force.x / mass) * Timer.UPDATE_TIME;
    speed.y += (force.y / mass) * Timer.UPDATE_TIME;
  }

  public void setFrequency(double frequency) {
    this.frequency = frequency;
  }

  public double getFrequency() {
    return frequency;
  }
}
