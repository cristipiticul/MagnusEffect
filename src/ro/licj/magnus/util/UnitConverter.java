package ro.licj.magnus.util;

import ro.licj.magnus.Renderer;

public class UnitConverter {
    public static float metersToUniformCoordinatesX(float metersX) {
        return metersX * Renderer.getPixelsPerMeter() * 2.0f / Renderer.getDrawingPanelWidth();
    }

    public static float metersToUniformCoordinatesY(float metersY) {
        return metersY * Renderer.getPixelsPerMeter() * 2.0f / Renderer.getDrawingPanelHeight();
    }

    public static float metersToUniformCoordinatesX(double metersX) {
        return metersToUniformCoordinatesX((float) metersX);
    }

    public static float metersToUniformCoordinatesY(double metersY) {
        return metersToUniformCoordinatesY((float) metersY);
    }
}
