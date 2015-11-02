package ro.licj.magnus;

import ro.licj.magnus.ui.Color;
import ro.licj.magnus.util.Point;

import java.util.ArrayList;
import java.util.List;

public class Trajectory {
  private List<Point> points;
  private Color color;
  private double maxX;
  private double maxY;

  public Trajectory(Color color) {
    this.color = color;
    points = new ArrayList<Point>();
  }

  public synchronized void addPoint(Point point) {
    points.add(new Point(point));
    if (point.x > maxX) {
      maxX = point.x;
    }
    if (point.y > maxY) {
      maxY = point.y;
    }
  }

  public synchronized List<Point> getPoints() {
    return points;
  }

  public synchronized Color getColor() {
    return color;
  }

  public synchronized double getMaxX() {
    return maxX;
  }

  public synchronized double getMaxY() {
    return maxY;
  }
}
