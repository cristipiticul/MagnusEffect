package ro.licj.magnus.util;

public class Matrix3f {
  public static float[] multiply(float[] a, float[] b) {
    float[] result = new float[9];

    result[0] = a[0] * b[0] + a[1] * b[3] + a[2] * b[6];
    result[1] = a[0] * b[1] + a[1] * b[4] + a[2] * b[7];
    result[2] = a[0] * b[2] + a[1] * b[5] + a[2] * b[8];

    result[3] = a[3] * b[0] + a[4] * b[3] + a[5] * b[6];
    result[4] = a[3] * b[1] + a[4] * b[4] + a[5] * b[7];
    result[5] = a[3] * b[2] + a[4] * b[5] + a[5] * b[8];

    result[6] = a[6] * b[0] + a[7] * b[3] + a[8] * b[6];
    result[7] = a[6] * b[1] + a[7] * b[4] + a[8] * b[7];
    result[8] = a[6] * b[2] + a[7] * b[5] + a[8] * b[8];

    return result;
  }

  public static float[] rotationMatrix(double angle) {
    return new float[]{
        (float) Math.cos(angle), (float) -Math.sin(angle), 0.0f,
        (float) Math.sin(angle), (float) Math.cos(angle), 0.0f,
        0.0f, 0.0f, 1.0f
    };
  }

  public static float[] scaleMatrix(float scaleX, float scaleY) {
    return new float[]{
        scaleX, 0.0f, 0.0f,
        0.0f, scaleY, 0.0f,
        0.0f, 0.0f, 1.0f
    };
  }

  public static float[] scaleMatrix(double scaleX, double scaleY) {
    return scaleMatrix((float) scaleX, (float) scaleY);
  }

  public static float[] translationMatrix(float dx, float dy) {
    return new float[]{
        1.0f, 0.0f, dx,
        0.0f, 1.0f, dy,
        0.0f, 0.0f, 1.0f
    };
  }

  public static float[] translationMatrix(double dx, double dy) {
    return translationMatrix((float) dx, (float) dy);
  }

  public static float[] identityMatrix() {
    return new float[]{
        1.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 1.0f
    };
  }
}
