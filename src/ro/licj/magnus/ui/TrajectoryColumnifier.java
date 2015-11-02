package ro.licj.magnus.ui;

import ro.licj.magnus.Game;
import ro.licj.magnus.Trajectory;

public class TrajectoryColumnifier implements Columnifier<Trajectory> {

  private static final String[] COLUMN_NAMES = new String[]{"Color", "X max", "Y max"};
  private static final Class[] COLUMN_TYPES = new Class[]{Color.class, String.class, String.class};

  private static Columnifier<Trajectory> instance = new TrajectoryColumnifier();

  public static Columnifier<Trajectory> getInstance() {
    return instance;
  }

  private TrajectoryColumnifier() {}

  @Override
  public String[] getColumnNames() {
    return COLUMN_NAMES;
  }

  @Override
  public Class[] getColumnTypes() {
    return COLUMN_TYPES;
  }

  @Override
  public Object getColumnOfElement(Trajectory element, int columnIndex) {
    switch (columnIndex) {
      case 0:
        return element.getColor();
      case 1:
        return String.format("%.2f", element.getMaxX() - Game.getInitialBallPosition().x);
      case 2:
        return String.format("%.2f", element.getMaxY() - Game.getInitialBallPosition().y);
      default:
        throw new RuntimeException("Information table error: column index should be between 0 and 2. It is: " + columnIndex + ".");
    }
  }
}
