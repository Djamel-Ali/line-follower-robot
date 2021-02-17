package ev3dev.functionalities;


import ev3dev.actuators.lego.motors.EV3LargeRegulatedMotor;
import ev3dev.model.Color;
import ev3dev.sensors.Button;
import ev3dev.sensors.LearningColors;
import ev3dev.sensors.ev3.EV3ColorSensor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;

import java.util.ArrayList;

import static ev3dev.sensors.Button.ENTER;

public class StraightLineFollower {

    // Attributes :
    private final static int NB_OF_COLORS_TO_LEARN = 2;
    private final static int NB_OF_MEASURES_PER_COLOR = 5;
    private static final double MAXIMUM_TOLERATED_DISTANCE = 63;
    private static float[] sample;
    private final SampleProvider sampleProvider;
    private static EV3ColorSensor ev3ColorSensor;
    private final LearningColors learningColors;
    private ArrayList<Color> listOfLearnedColors;
    private static Color lineColor, backgroundColor;
//    private Motors motors;

    //Robot Definition
    private static EV3LargeRegulatedMotor leftMotor, rightMotor;
    private int leftMotorSpeed, rightMotorSpeed;

    // Distance between the line color and the background color.
    private double distanceLineBackground;

    public StraightLineFollower() {
        learningColors = new LearningColors();
        listOfLearnedColors = new ArrayList<>(NB_OF_COLORS_TO_LEARN);
        distanceLineBackground = 0;
        ev3ColorSensor = new EV3ColorSensor(SensorPort.S1);
        sampleProvider = ev3ColorSensor.getRGBMode();
        int sampleSize = sampleProvider.sampleSize();
        sample = new float[sampleSize];

        // Initialization of motor speed
        leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);
        rightMotor = new EV3LargeRegulatedMotor(MotorPort.D);


        //To Stop the motor in case of pkill java for example
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Emergency Stop");
            leftMotor.stop();
            rightMotor.stop();
        }));

        System.out.println("Defining the Stop mode");
        leftMotor.brake();
        rightMotor.brake();

        System.out.println("Defining motor speed");
        leftMotorSpeed = 200;
        rightMotorSpeed = 200;
        leftMotor.setSpeed(leftMotorSpeed);
        rightMotor.setSpeed(rightMotorSpeed);
    }

    public static void main(String[] args) throws RuntimeException {

        StraightLineFollower straightLineFollower = new StraightLineFollower();

        // Start of the learning phase
        straightLineFollower.learningColors.startLearning(NB_OF_COLORS_TO_LEARN, NB_OF_MEASURES_PER_COLOR);
        // End of the learning phase

        // get the list of learned colors
        straightLineFollower.listOfLearnedColors = LearningColors.getListOfLearnedColors();

        // Color identification
        lineColor = straightLineFollower.listOfLearnedColors.get(0);
        lineColor.setName("LINE");
        backgroundColor = straightLineFollower.listOfLearnedColors.get(1);
        backgroundColor.setName("BACKGROUND");
        straightLineFollower.distanceLineBackground = Color.getDistance(lineColor.getRgbValues(), backgroundColor.getRgbValues());

        System.out.println("\n LINE : " + lineColor);
        System.out.println("\n BACKGROUND : " + backgroundColor);
        System.out.println("\n Distance : " + straightLineFollower.distanceLineBackground);
        System.out.println("\n Frontier (its distance from the two colors) : " + straightLineFollower.distanceLineBackground / 2);

        System.out.println("\n\n\n\n\n\n");

        // While no one has pressed the OK button yet ...
        while (ENTER.isUp()) {
            System.out.println("\n Click OK to begin following the line: \n");
            int id_of_pressed_btn = Button.waitForAnyPress();
        }
        System.out.println("\n Start following the line : ");

        // Initializing time variables
        long elapsed_time_on_the_line = 0, elapsed_time_on_the_background = 0;

        // As long as we have not yet clicked on ESCAPE, the robot continues to capture color samples
        while (Button.ESCAPE.isUp()) {
            straightLineFollower.sampleProvider.fetchSample(sample, 0);
            String closestColor = Color.getTheClosestColor(sample, lineColor, backgroundColor, null,  MAXIMUM_TOLERATED_DISTANCE);

//             Display the detected color
            System.out.println("\n\n ## " + closestColor + " ## \n\n");

            if (closestColor.equalsIgnoreCase(lineColor.getName())) {
                if (elapsed_time_on_the_line == 0)
                    elapsed_time_on_the_line = System.currentTimeMillis();
                else if (System.currentTimeMillis() - elapsed_time_on_the_line > 1000) {
                    straightLineFollower.leftMotorSpeed = 530;
                    straightLineFollower.rightMotorSpeed = 530;
                } else if (System.currentTimeMillis() - elapsed_time_on_the_line > 500) {
                    straightLineFollower.leftMotorSpeed = 320;
                    straightLineFollower.rightMotorSpeed = 320;
                } else if (System.currentTimeMillis() - elapsed_time_on_the_line > 250) {
                    straightLineFollower.leftMotorSpeed = 180;
                    straightLineFollower.rightMotorSpeed = 180;
                } else {
                    straightLineFollower.leftMotorSpeed = 100;
                    straightLineFollower.rightMotorSpeed = 100;
                }
                elapsed_time_on_the_background = 0;
                leftMotor.setSpeed(straightLineFollower.leftMotorSpeed);
                rightMotor.setSpeed(straightLineFollower.rightMotorSpeed);

                // Go Forward with the motors
                leftMotor.forward();
                rightMotor.forward();
            }

            // He has moved away from the color to follow (He goes out of line)
            else if (closestColor.equalsIgnoreCase(backgroundColor.getName())){
                rightMotor.stop();
                leftMotor.stop();

                leftMotor.setSpeed(300);
                rightMotor.setSpeed(100);
                elapsed_time_on_the_background = System.currentTimeMillis();
                do{
                    // turn around itself
                    leftMotor.forward();
                    rightMotor.backward();
                    straightLineFollower.sampleProvider.fetchSample(sample, 0);
                    closestColor = Color.getTheClosestColor(sample, lineColor, backgroundColor,null,  MAXIMUM_TOLERATED_DISTANCE);
                } while (closestColor.equalsIgnoreCase(backgroundColor.getName()));
//                while (System.currentTimeMillis() - elapsed_time_on_the_background < 380);
                leftMotor.stop();
                rightMotor.stop();
                elapsed_time_on_the_background = System.currentTimeMillis();
                lookForLine(straightLineFollower, elapsed_time_on_the_background);
//                if (elapsed_time_on_the_background == 0)
//                    elapsed_time_on_the_background = System.currentTimeMillis();
////                else if (System.currentTimeMillis() - elapsed_time_on_the_background > 100){
////                    straightLineFollower.rightMotor.stop();
////                }
//                elapsed_time_on_the_line = 0;
//
//                // the robot turns around itself
//                straightLineFollower.leftMotorSpeed = 200;
//                straightLineFollower.rightMotorSpeed = 0;
//                leftMotor.setSpeed(straightLineFollower.leftMotorSpeed);
//                rightMotor.setSpeed(straightLineFollower.rightMotorSpeed);
//                leftMotor.forward();
//                rightMotor.forward();
            }
            // i.e closestColor  == OTHER
            else{
                rightMotor.stop();
                leftMotor.stop();
            }
        }
        rightMotor.stop();
        leftMotor.stop();
    }

    private static void lookForLine(StraightLineFollower _straightLineFollower, long _start_time_in_the_background) {
        String theClosestColor;
        // essayer de trouver la ligne sur sa gauche
        _straightLineFollower.leftMotorSpeed = 7 ;
        _straightLineFollower.rightMotorSpeed = 200;
        leftMotor.setSpeed(_straightLineFollower.leftMotorSpeed);
        rightMotor.setSpeed(_straightLineFollower.rightMotorSpeed);
        rightMotor.forward();
        leftMotor.backward();
        if (System.currentTimeMillis() - _start_time_in_the_background > 200) {
            leftMotor.stop();
            rightMotor.stop();
            _straightLineFollower.sampleProvider.fetchSample(sample, 0);
            theClosestColor = Color.getTheClosestColor(sample, lineColor, backgroundColor, null, MAXIMUM_TOLERATED_DISTANCE);

            // Display the detected color
            System.out.println("\n\n ## (SEARCHING PHASE) : " + theClosestColor + " ## \n\n");

            if (theClosestColor.equalsIgnoreCase(backgroundColor.getName())) {
                _straightLineFollower.leftMotorSpeed = 200;
                _straightLineFollower.rightMotorSpeed = 7;
                rightMotor.setSpeed(_straightLineFollower.rightMotorSpeed);
                leftMotor.setSpeed(_straightLineFollower.leftMotorSpeed);
                leftMotor.forward();
                rightMotor.backward();
                if (System.currentTimeMillis() - _start_time_in_the_background > 400) {
                    rightMotor.stop();
                    leftMotor.stop();
                    _straightLineFollower.sampleProvider.fetchSample(sample, 0);
                    theClosestColor = Color.getTheClosestColor(sample, lineColor, backgroundColor, null,  MAXIMUM_TOLERATED_DISTANCE);

                    // Display the detected color
                    System.out.println("\n\n ## (SEARCHING PHASE) : " + theClosestColor + " ## \n\n");
                    if (theClosestColor.equalsIgnoreCase(backgroundColor.getName())) {

                        // continue turning around itself until he detects the line (spiral shape turns)
                        _straightLineFollower.leftMotorSpeed = 400;
                        _straightLineFollower.rightMotorSpeed = 380;
                        rightMotor.setSpeed(_straightLineFollower.rightMotorSpeed);
                        leftMotor.setSpeed(_straightLineFollower.leftMotorSpeed);
                        do {
                            leftMotor.forward();
                            rightMotor.forward();
                            _straightLineFollower.sampleProvider.fetchSample(sample, 0);
                            theClosestColor = Color.getTheClosestColor(sample, lineColor, backgroundColor, null, MAXIMUM_TOLERATED_DISTANCE);
                        } while (theClosestColor.equalsIgnoreCase(backgroundColor.getName()));
                    }
                }
            }
        }
    }

//    // Returns the closest color to the one he just captured (Returns either the line "LINE" or the background "BACKGROUND")
//    private static String getTheClosestColor(float[] _sample) throws NullPointerException {
//
//        // calculate the distance from the color of the line
//        double distance = Color.getDistance(_sample, lineColor.getRgbValues());
//        if (distance < MAXIMUM_TOLERATED_DISTANCE) {
//
//            // LINE (So keep following it (straight))
//            return "LINE";
//
//        } else {
//            // calculate the distance from the color of the background
//            distance = Color.getDistance(_sample, backgroundColor.getRgbValues());
//            if (distance < MAXIMUM_TOLERATED_DISTANCE) {
//
//                // BACKGROUND (So he must search the line)
//                return "BACKGROUND";
//            } else return "OTHER";
//        }
//    }
}