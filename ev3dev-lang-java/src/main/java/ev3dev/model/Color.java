package ev3dev.model;

import java.util.Arrays;
import java.util.StringJoiner;

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
        return new StringJoiner(", ", Color.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("rgbValues=" + Arrays.toString(rgbValues))
                .toString();
    }
}
