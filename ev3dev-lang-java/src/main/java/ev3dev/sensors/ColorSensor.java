package ev3dev.sensors;

import ev3dev.model.Color;
import ev3dev.sensors.ev3.EV3ColorSensor;
import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

import static ev3dev.sensors.Button.ENTER;
import static java.lang.Math.sqrt;


@Slf4j
public class ColorSensor {
    //Robot Configuration
    private static EV3ColorSensor color1 = new EV3ColorSensor(SensorPort.S1);

    //Configuration
    private static final int FIVE_SECONDS = 5000;

    // The maximum distance allowed between 2 colors.
    private static final double MAX_DISTANCE = 60;

    // Initial distance
    private static double distance = 0;

    // The average color of all measurements
    private static Color average_color = new Color("average_color", new float[]{0, 0, 0});


    public static void main(String[] args) {

        SampleProvider sampleProvider = color1.getRGBMode();

        LOGGER.info("\n\t PREPARE FOR RGB MODE \n");
        int sampleSize = sampleProvider.sampleSize();
        float[] sample = new float[sampleSize];
        Color[] acceptable_measures = new Color[3];

        // Takes some samples and prints them
        /*
         * (void fetchSample(float[] sample, int offset)
         * Fetches a sample from a sensor or filter.
         * Parameters:
         * sample - The array to store the sample in.
         * offset - The elements of the sample are stored in the array starting at the offset position.)
         */

        // Takes some samples and prints them
        for (int i = 0; i < 3; i++) {
            int step, id_of_pressed_btn = -1;


            do {
                step = i + 1;
                LOGGER.info("\n\n\t #######   " + step + "   ####### \n\n");

                // While no one has pressed the OK button yet
                while (ENTER.isUp()) {
                    LOGGER.info("\n\n===> Bring the sensor close to the color and click OK :\n");
                    id_of_pressed_btn = Button.waitForAnyPress();
                }

                LOGGER.info("\n\tRECUPERATION DE L'ECHANTILLION... \n");

                sampleProvider.fetchSample(sample, 0);

                // if it's the 1st measure, we put it directly in the array of measures
                if (i == 0) {

                    // Empty the array (acceptable measures) if it contains values (added before),
                    // then fill it with the RGB values of this new measurement (which is in 'sample' array).
                    if (ArrayUtils.isNotEmpty(acceptable_measures)) Arrays.fill(acceptable_measures, null);
                    acceptable_measures[i] = new Color("measure_" + step, sample);
                    LOGGER.info(String.valueOf(acceptable_measures[i]));
                    Delay.msDelay(FIVE_SECONDS*2);

                    // Update average color
                    average_color.setRgbValues(sample);
                }

                // (i > 0) : The array of acceptable measures already contains values, ( so we have to
                //  calculate the distance of this last measurement from the current average color )
                else {
                    distance = getDistance(average_color.getRgbValues(), sample);
                    if (distance > MAX_DISTANCE)
                    // Repeat the measurements (from the beginning).
                    {
                        i = 0;
                        average_color.reset(); // (average color : R = 0, G = 0, B = 0)
                        Arrays.fill(acceptable_measures, null);
                        LOGGER.info("\n\nRepeat the measurements (from the beginning)\n");

                    } else {
                        // This last color is added to the Acceptable Measurements table
                        // and the average color is updated.
                        acceptable_measures[i] = new Color("measure_" + step, sample);
                        average_color.setRgbValues(getAverageColor(average_color.getRgbValues(), sample));
                        LOGGER.info("\n\n   Measure " + step + " accepted\n");
                        Delay.msDelay(2500);
                    }
                }
                // Reset everything to 0 before proceeding to another measurement.
                Arrays.fill(sample,0);
            } while (distance > MAX_DISTANCE);

            LOGGER.info("\n **********   Measure   " + step + "   ***********\n");

            LOGGER.info("\n\n\t R == " + (int) sample[3 * i] + "\n");
            LOGGER.info("\n\n\t G == " + (int) sample[3 * i + 1] + "\n");
            LOGGER.info("\n\n\t B == " + (int) sample[3 * i + 2] + "\n\n");

            LOGGER.info("\n\n **********   End of Measure  " + step + "   **********\n");

            Delay.msDelay(FIVE_SECONDS+2000);
        }
    }

    static double getDistance(float[] rgb_color1, float[] rgb_color2) {
        return sqrt(Math.pow(rgb_color1[0] - rgb_color2[0], 2) +
                Math.pow(rgb_color1[1] - rgb_color2[1], 2) +
                Math.pow(rgb_color1[2] - rgb_color2[2], 2));
    }

    static float[] getAverageColor(float[] rgb_color1, float[] rgb_color2) {
        float[] average = new float[3];
        average[0] = (rgb_color1[0] + rgb_color2[0]) / 2;
        average[1] = (rgb_color1[1] + rgb_color2[1]) / 2;
        average[2] = (rgb_color1[2] + rgb_color2[2]) / 2;
        return average;
    }
}
