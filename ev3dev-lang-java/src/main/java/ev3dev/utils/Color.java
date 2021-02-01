package ev3dev.utils;

import java.util.Arrays;

public class Color {

    // Attributes
    private final String name;
    private float[] rgbValues;

    // Constructor
    public Color(String name, float[] rgbValues) {
        this.name = name;
        this.rgbValues = rgbValues;
    }

    public Color(String name) {
        this.name = name;
        this.rgbValues = new float[]{0,0,0};
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public float[] getRgbValues() {
        return rgbValues;
    }

    public void setRgbValues(float[] _rgbValues) {
        // (arrayclone is the most suitable and efficient)
        System.arraycopy(_rgbValues, 0, rgbValues, 0, _rgbValues.length);
    }

    public void reset() {
        Arrays.fill(rgbValues, 0);
    }

    @Override
    public String toString() {
        return "\n\t  NAME == " + getName() +" :"+
                "\n\t  R == " + rgbValues[0] +
                "\n\t  G == " + rgbValues[1] +
                "\n\t  B == " + rgbValues[2];
    }
}
