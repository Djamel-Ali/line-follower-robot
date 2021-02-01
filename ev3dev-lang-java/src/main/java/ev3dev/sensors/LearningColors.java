package ev3dev.sensors;

import ev3dev.utils.Color;
import ev3dev.sensors.ev3.EV3ColorSensor;
import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;

import static ev3dev.sensors.Button.ENTER;
import static java.lang.Math.sqrt;

// The main method of this class is : startLearning(int nb_of_color_to_learn, int nb_of_measures_per_color)
// And at the output of the main loop, we could retrieve the average values of the learned colors
// (in the variable: listOfLearnedColors as a list of elements of type Color).

@Slf4j
public class LearningColors {

    // Variables
    private static ArrayList<Color> listOfLearnedColors;

    private static double distance;

    private static Color average_color;

    // Some initial configurations
    private static final int FIVE_SECONDS = 5000;

    // The maximum distance allowed between 2 colors.
    private static final double MAX_DISTANCE_ALLOWED = 70;

    // For Robot Configuration
    private static EV3ColorSensor color1;


    // Constructor
    public LearningColors() {
        listOfLearnedColors = new ArrayList<>();

        // Initial distance
        distance = 0;

        // The average color of all measurements made on a given color.
        average_color = new Color("average_color", new float[]{0, 0, 0});
    }


    public static void main(String[] args) {

        color1 = new EV3ColorSensor(SensorPort.S1);
        LearningColors learningColors = new LearningColors();
        learningColors.startLearning(2, 3);

    }

    public void startLearning(int nb_of_color_to_learn, int nb_of_measures_per_color) {

        SampleProvider sampleProvider = color1.getRGBMode();

        System.out.println("\n\t PREPARE FOR RGB MODE" +
                "\n\t " + nb_of_color_to_learn + " COLOR(S) TO LEARN,\n\t " + nb_of_measures_per_color + " MEASURE(S) PER COLOR \n");
        int sampleSize = sampleProvider.sampleSize();
        float[] sample = new float[sampleSize];
        ArrayList<Color> acceptable_measures = new ArrayList<>();

        // The for loop which allows to switch from learning one color to another (in order to learn 'x' color at the end)
        // (knowing that 'x' is the first argument given to method.)
        for (int color_index = 0; color_index < nb_of_color_to_learn; color_index++) {

            // Loop that makes the number of measurements needed for each color
            for (int measure_counter = 0; measure_counter < nb_of_measures_per_color; measure_counter++) {
                int id_of_pressed_btn = -1;
                do {
                    distance = 0;
                    System.out.println("\n\t####### Color : " + color_index + "; Measure : " + measure_counter + " ####### \n");

                    // While no one has pressed the OK button yet
                    while (ENTER.isUp()) {
                        System.out.println("\n===> Bring the sensor close to the color and click OK :\n");
                        id_of_pressed_btn = Button.waitForAnyPress();
                    }

                    // First we check that it is a valid RGB triplet.
                    do {
                        System.out.println("\n\tRECUPERATION DE L'ECHANTILLION... \n");

                        sampleProvider.fetchSample(sample, 0);
                        if (isInvalidRGBTriplet(sample))
                            System.out.println("\nThe captured RGB triplet is invalid !");
                    } while (isInvalidRGBTriplet(sample));

                    // if it's the 1st measure, we put it directly in the array of measures
                    if (measure_counter == 0) {

                        // Empty the array (acceptable measures) if it contains values (added before),
                        // then fill it with the RGB values of this new measurement (which is in 'sample' array).
                        if (!acceptable_measures.isEmpty()) acceptable_measures.clear();
                        acceptable_measures.add(0, new Color("measure_" + measure_counter));
                        //System.arraycopy(sample, 0, acceptable_measures.get(measure_counter).getRgbValues(), 0, 3);
                        acceptable_measures.get(measure_counter).setRgbValues(sample);

                        // Update average color
                        average_color.setRgbValues(sample);
                    }

                    // (j > 0) : The array of acceptable measures already contains values, ( so we have to
                    //  calculate the distance of this last measurement from the current average color )
                    else {
                        distance = getDistance(average_color.getRgbValues(), sample);

                        // Repeat the measurements (from the beginning).
                        if (distance > MAX_DISTANCE_ALLOWED) {
                            measure_counter = 0;
                            average_color.reset(); // (average color : R = 0, G = 0, B = 0)
                            acceptable_measures.clear();

                            System.out.println("\n### ( distance == " + distance + " > MAX_DISTANCE_ALLOWED == " +
                                    MAX_DISTANCE_ALLOWED + " ) ###\n### Repeat the measurements (from the beginning) ###\n");
                        }

                        // Add this last color to the Acceptable Measurements table
                        // and update the average color.
                        else {
                            acceptable_measures.add(measure_counter, new Color("measure_" + measure_counter));
                            System.arraycopy(sample, 0, acceptable_measures.get(measure_counter).getRgbValues(), 0, 3);

                            // update average color
                            average_color.setRgbValues(getAverageColor(acceptable_measures));

                            System.out.println("\n   Measure " + measure_counter + " accepted\n");
                        }
                    }
                    if(distance < MAX_DISTANCE_ALLOWED)
                    {
                        System.out.println("\n\t MEASURE == " + measure_counter + ",\n\t DISTANCE == " + distance +
                                "\n\t AVERAGE COLOR : " + average_color + "\n\n\t ACCEPTABLE_MEASURE[" + measure_counter + "] : "
                                + acceptable_measures.get(measure_counter));
                    }

                    // Reset everything to 0 before proceeding to another measurement.
                    Arrays.fill(sample, 0);

                } while (distance > MAX_DISTANCE_ALLOWED);
            }
            //  Save the average values of the color just learned.
//            listOfLearnedColors.add(color_index, average_color);
            listOfLearnedColors.add(color_index, new Color("COLOR_" + color_index));
            listOfLearnedColors.get(color_index).setRgbValues(average_color.getRgbValues());

            // Reset intermediate variables to use them to learn other colors.
            average_color.reset();
        }
        System.out.println("\nSummary of the average values of the learned colors : \n" +
                                "____________________________________");
        for(Color c : listOfLearnedColors){
            System.out.println(c);
        }
    }


    static double getDistance(float[] rgb_color1, float[] rgb_color2) {
        return sqrt(Math.pow(rgb_color1[0] - rgb_color2[0], 2) +
                Math.pow(rgb_color1[1] - rgb_color2[1], 2) +
                Math.pow(rgb_color1[2] - rgb_color2[2], 2));
    }

    public static float[] getAverageColor(ArrayList<Color> arrayOfColors) {

        float[] average = new float[3];
        float totalRed = 0;
        float totalGreen = 0;
        float totalBlue = 0;
        int counter = 0;

        for (Color arrayOfColor : arrayOfColors) {
            totalRed += arrayOfColor.getRgbValues()[0];
            totalGreen += arrayOfColor.getRgbValues()[1];
            totalBlue += arrayOfColor.getRgbValues()[2];
            counter++;
        }

        try {
            average[0] = totalRed / counter;
            average[1] = totalGreen / counter;
            average[2] = totalBlue / counter;
        } catch (ArithmeticException exception) {
            System.out.println("\n\t -- Can't divide a number by 0\n");
            exception.printStackTrace();
        } catch (NullPointerException exception) {
            System.out.println("\n\t -- NullPointerException in getAverageColor method\n");
            exception.printStackTrace();
        }
        return average;
    }


    // It happens very rarely (at least for our current sensor) that a value exceeds 255,
    // so it's better to check that.
    static boolean isInvalidRGBTriplet(float[] a_sample) {
        return a_sample[0] > 255 || a_sample[1] > 255 || a_sample[2] > 255;
    }


    public static ArrayList<Color> getListOfLearnedColors() {
        return listOfLearnedColors;
    }
}
