package ev3dev.functionalities;

import ev3dev.sensors.Button;
import ev3dev.sensors.LearningColors;
import ev3dev.model.Color;
import lejos.utility.Delay;

import java.io.IOException;
import java.util.ArrayList;

import static ev3dev.sensors.Button.ENTER;

public class StraightLineFollower {

    // Attributes :
    private final static int NB_OF_COLORS_TO_LEARN = 2;
    private final static int NB_OF_MEASURES_PER_COLOR = 3;
    private final LearningColors learningColors;
    private ArrayList<Color> listOfLearnedColors;

    // distance between the line color and the background color.
    private double distanceLineBackground;

    public StraightLineFollower() {
        learningColors = new LearningColors();
        listOfLearnedColors = new ArrayList<>(NB_OF_COLORS_TO_LEARN);
        distanceLineBackground = 0;
    }

    public static void main(String[] args) throws IOException {

        StraightLineFollower straightLineFollower = new StraightLineFollower();

        // Start of the learning phase
        straightLineFollower.learningColors.startLearning(NB_OF_COLORS_TO_LEARN, NB_OF_MEASURES_PER_COLOR);
        // End of the learning phase

        // get the list of learned colors
        straightLineFollower.listOfLearnedColors = LearningColors.getListOfLearnedColors();

        // Color identification
        Color lineColor = straightLineFollower.listOfLearnedColors.get(0);
        Color backgroundColor = straightLineFollower.listOfLearnedColors.get(1);
        float[] rgbMedianColor = LearningColors.getAverageColor(straightLineFollower.listOfLearnedColors);

        // [Color to follow]
        Color medianColor = new Color("median_color", rgbMedianColor);

        straightLineFollower.distanceLineBackground =
                LearningColors.getDistance(lineColor.getRgbValues(), backgroundColor.getRgbValues());
        System.out.println("\n Line : " + lineColor);
        System.out.println("\n Background : " + backgroundColor);
        System.out.println("\n 50% Line ; 50% Background : " + medianColor);
        System.out.println("\n Distance : "+ straightLineFollower.distanceLineBackground );
        System.out.println("\n Frontier : "+ straightLineFollower.distanceLineBackground/2 );

        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");

        // While no one has pressed the OK button yet ...
        while (ENTER.isUp()) {
            System.out.println("\n Click OK to begin following the line: \n");
            int id_of_pressed_btn = Button.waitForAnyPress();
        }
        System.out.println("\n Start following the line : ");
    }


}
