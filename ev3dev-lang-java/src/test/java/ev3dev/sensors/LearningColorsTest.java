package ev3dev.sensors;

import ev3dev.utils.Color;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class LearningColorsTest {

    @Test
    void getDistance() {
    }

    @Test
    void getAverageColor() {
    }

    @Test
    void testGetAverageColor() {
        float[]rgbRedColor = new float[]{255, 0, 0};
        float[]rgbGreenColor = new float[]{0, 255, 0};
        float[]rgbBlueColor = new float[]{0, 0, 255};
        ArrayList<Color> arrayOfColors = new ArrayList<>(3);

        Color red = new Color("red", rgbRedColor);
        Color green = new Color("green", rgbGreenColor);
        Color blue = new Color("blue", rgbBlueColor);

        arrayOfColors.set(0, red);
        arrayOfColors.set(1, green);
        arrayOfColors.set(2, blue);
        float[] average ;
        average = LearningColors.getAverageColor(arrayOfColors);
        System.out.println("\n\tR= "+average[0] +", G= "+average[1]+", B= "+average[2]+"\n");
    }

    @Test
    void isInvalidRGBTriplet() {
    }
}