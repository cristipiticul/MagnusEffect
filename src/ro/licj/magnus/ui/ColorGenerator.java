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
    /*for (int i = 0; i < 27; i++) {
      if (i % 3 == 1 || i == 8 || i == 6 || i == 17 || i == 15) {
        continue;
      }
      int b = i % 3;
      int g = (i / 3) % 3;
      int r = (i / 9) % 3;
      colors.add(new Color(0.5f + (float) r / 4.0f, 0.5f + (float) g / 4.0f, 0.5f + (float) b / 4.0f));
    }*/
    colors.add(new Color(255.0f / 256.0f,   125.0f / 256.0f,  30.0f / 256.0f));
    colors.add(new Color(228.0f / 256.0f,   155.0f / 256.0f, 243.0f / 256.0f));
    colors.add(new Color(119.0f / 256.0f,    71.0f / 256.0f,   0.0f / 256.0f));
    colors.add(new Color(  0.0f / 256.0f,    74.0f / 256.0f,  49.0f / 256.0f));
    colors.add(new Color(120.0f / 256.0f,    44.0f / 256.0f, 159.0f / 256.0f));
    colors.add(new Color(193.0f / 256.0f,     0.0f / 256.0f,   0.0f / 256.0f));
    colors.add(new Color(193.0f / 256.0f,   167.0f / 256.0f,   0.0f / 256.0f));
    colors.add(new Color(  0.0f / 256.0f,    90.0f / 256.0f, 193.0f / 256.0f));
    colors.add(new Color(116.0f / 256.0f,   193.0f / 256.0f,   0.0f / 256.0f));
    Collections.shuffle(colors);
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
