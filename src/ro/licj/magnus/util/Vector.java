package ro.licj.magnus.util;

public class Vector {
  public double x;
  public double y;

  public Vector(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public static Vector product(Vector a, Vector b) {
    return new Vector(a.x * b.x, a.y * b.y);
  }

  public static Vector product(double alpha, Vector a) {
    return new Vector(alpha * a.x, alpha * a.y);
  }

  public static Vector sum(Vector a, Vector b) {
    return new Vector(a.x + b.x, a.y + b.y);
  }

  public double length() {
    return Math.sqrt(x * x + y * y);
  }
}
