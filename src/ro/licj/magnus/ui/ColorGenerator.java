package ro.licj.magnus.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ColorGenerator {
  private static ColorGenerator instance = new ColorGenerator();

  private List<Color> colors = new ArrayList<Color>();
  private int currentColor = -1;

  private ColorGenerator() {
    for (int i = 0; i < 27; i++) {
      if (i % 3 == 1 || i == 8 || i == 6 || i == 17 || i == 15) {
        continue;
      }
      int b = i % 3;
      int g = (i / 3) % 3;
      int r = (i / 9) % 3;
      colors.add(new Color(0.5f + (float) r / 4.0f, 0.5f + (float) g / 4.0f, 0.5f + (float) b / 4.0f));
    }
//    Collections.shuffle(colors);
  }

  public static ColorGenerator getInstance() {
    return instance;
  }

  public Color next() {
    currentColor++;
    if (currentColor >= colors.size()) {
      currentColor = 0;
    }
    return colors.get(currentColor);
  }

  public int getCurrentColorIndex() {
    return currentColor;
  }

  public Color current() {
    return colors.get(currentColor);
  }
}
