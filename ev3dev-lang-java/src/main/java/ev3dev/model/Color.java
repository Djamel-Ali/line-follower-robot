package ev3dev.model;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public class Color {

    // Attributes
    private final String name;
    private float [] rgbValues;

    // Constructor
    public Color(String name, float[] rgbValues) {
        this.name = name;
        this.rgbValues = rgbValues;
    }


    // Getters and Setters
    public String getName() {
        return name;
    }

    public float[] getRgbValues() {
        return rgbValues;
    }

    public void setRgbValues(float[] rgbValues) {
        this.rgbValues = rgbValues;
    }

    public void reset(){
        Arrays.fill(rgbValues, 0);
    }

    //    @Override
    //    public String toString() {
    //        return   "\n\n\t  NAME == " + getName() + "\n" +
    //                 "\n\n\t  R == " + (int) rgbValues[0] + "\n" +
    //                 "\n\n\t  G == " + (int) rgbValues[1] + "\n" +
    //                 "\n\n\t  B == " + (int) rgbValues[2] + "\n\n";
    //    }
}
