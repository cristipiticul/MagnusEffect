package ro.licj.magnus;

import ro.licj.magnus.ui.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ColorPallette {

  public static float difference(ro.licj.magnus.ui.Color c1, ro.licj.magnus.ui.Color c2) {
    float rez = Math.max(Math.max(Math.abs(c1.r - c2.r), Math.abs(c1.g - c2.g)), Math.abs(c1.b - c2.b));
    return rez;
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent windowevent) {
        frame.dispose();
      }
    });

    JPanel colorsPanel = new JPanel(new GridBagLayout());
    frame.getContentPane().add(colorsPanel, BorderLayout.CENTER);
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.BOTH;
    constraints.insets = new Insets(10, 10, 10, 10);
    colorsPanel.setBackground(new Color(0.0f, 1.0f, 1.0f));

//    frame.setSize(600, 600);

    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 7; j++) {
        constraints.gridx = j;
        constraints.gridy = i;
        ro.licj.magnus.ui.Color col = ColorGenerator.getInstance().next();
        JLabel label = new JLabel("  " + ColorGenerator.getInstance().getCurrentColorIndex() + "  ");
//        label.setBorder(new LineBorder(new Color(0, 0, 0)));
        label.setBackground(new Color(col.r, col.g, col.b));
        label.setOpaque(true);
        colorsPanel.add(label, constraints);
      }
    }
    colorsPanel.repaint();
    colorsPanel.revalidate();
    frame.pack();
    frame.setVisible(true);
  }
}
