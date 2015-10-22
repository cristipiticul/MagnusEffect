package ro.licj.magnus.util;

public class Point {
  public double x;
  public double y;

  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public Point(Point p) {
    x = p.x;
    y = p.y;
  }

  public String toString() {
    return "(" + x + ", " + y + ")";
  }
}
