package ev3dev.functionalities;

import ev3dev.actuators.lego.motors.EV3LargeRegulatedMotor;
import ev3dev.model.Color;
import ev3dev.sensors.Button;
import ev3dev.sensors.LearningColors;
import ev3dev.sensors.ev3.EV3ColorSensor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

import java.util.ArrayList;

import static ev3dev.sensors.Button.ENTER;

public class CurvedLineFollower {

    // Attributes
    private final static int NB_OF_COLORS_TO_LEARN = 2;
    private final static int NB_OF_MEASURES_PER_COLOR = 6;
    float[] sample;
    private final SampleProvider sampleProvider;
    private final EV3LargeRegulatedMotor leftMotor, rightMotor;
    static Color backgroundColor, lineColor;
    private ArrayList<Color> listOfLearnedColors;
    private static final double MAXIMUM_TOLERATED_DISTANCE = 63;
    private final LearningColors learningColors;

    //Robot Definition
    int motorSpeed;
    private int leftMotorSpeed, rightMotorSpeed;
    private static EV3ColorSensor ev3ColorSensor;


    // Constructor
    public CurvedLineFollower() {
        // RGB sample variable
        learningColors = new LearningColors();
        listOfLearnedColors = new ArrayList<>(NB_OF_COLORS_TO_LEARN);
        ev3ColorSensor = new EV3ColorSensor(SensorPort.S1);
        sampleProvider = ev3ColorSensor.getRGBMode();
        int sampleSize = sampleProvider.sampleSize();
        this.sample = new float[sampleSize];


        // Get the motors left and right
        this.leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);
        this.rightMotor = new EV3LargeRegulatedMotor(MotorPort.D);

        // Set the speed of the motors
        motorSpeed = 400;
        leftMotor.setSpeed(motorSpeed);
        rightMotor.setSpeed(motorSpeed);
    }

    public static void main(String[] args) throws RuntimeException {
        // Variables
        String theClosestColor;
        float angleFactor = 0.40f;
        long elapsed_time_on_the_background = 0;

        // Initialize the brick
        CurvedLineFollower curvedLineFollower = new CurvedLineFollower();

        // Start of the learning phase
        curvedLineFollower.learningColors.startLearning(NB_OF_COLORS_TO_LEARN, NB_OF_MEASURES_PER_COLOR);
        // End of the learning phase

        // get the list of learned colors
        curvedLineFollower.listOfLearnedColors = LearningColors.getListOfLearnedColors();

        // Color identification
        lineColor = curvedLineFollower.listOfLearnedColors.get(0);
        lineColor.setName("LINE");
        backgroundColor = curvedLineFollower.listOfLearnedColors.get(1);
        backgroundColor.setName("BACKGROUND");

        System.out.println("\n LINE : " + lineColor);
        System.out.println("\n BACKGROUND : " + backgroundColor);
        System.out.println("\n\n\n\n\n\n");

        // While no one has pressed the OK button yet ...
        //todo: check if there is no problem without the while loop
//        while (ENTER.isUp()) {
        System.out.println("\n Click OK to begin following the line: \n");
//            int id_of_pressed_btn = Button.waitForAnyPress();
//        }
        ENTER.waitForPressAndRelease();
        System.out.println("\n Start following the line : ");

        // While the brick is detecting
        while (Button.ESCAPE.isUp()) {

            // Detect a color and get the RGB values
            curvedLineFollower.sampleProvider.fetchSample(curvedLineFollower.sample, 0);

            // Calculate the distances
            theClosestColor = Color.getTheClosestColor
                    (curvedLineFollower.sample, lineColor, backgroundColor, null,  MAXIMUM_TOLERATED_DISTANCE);

            // Is it color to follow
            if (theClosestColor.equalsIgnoreCase(lineColor.getName())) {
                elapsed_time_on_the_background = 0;
                curvedLineFollower.leftMotor.setSpeed(curvedLineFollower.motorSpeed);
                curvedLineFollower.rightMotor.setSpeed((int) (angleFactor * curvedLineFollower.motorSpeed));
            }
            // Is it color to avoid
            else {
                if (elapsed_time_on_the_background == 0)
                    // Start calculating time in avoid color
                    elapsed_time_on_the_background = System.currentTimeMillis();

                curvedLineFollower.leftMotor.setSpeed((int) (angleFactor * curvedLineFollower.motorSpeed));
                curvedLineFollower.rightMotor.setSpeed(curvedLineFollower.motorSpeed);

                // If time is bigger than we search for line
                // The machine is lost
                if (System.currentTimeMillis() - elapsed_time_on_the_background > 3000) {
                    elapsed_time_on_the_background = 0;
                    curvedLineFollower.lookForLine(curvedLineFollower);
                }
            }

            // Make the motors move forward
            curvedLineFollower.leftMotor.forward();
            curvedLineFollower.rightMotor.forward();
        }

        // stop motors
        curvedLineFollower.leftMotor.stop();
        curvedLineFollower.rightMotor.stop();
    }

    // Look for the line when lost
    public void lookForLine(CurvedLineFollower curvedLineFollower) {
        // Variables
        String the_closest_color;
        long timeTurning = 0;
        int timeToSpeedUp = 3000;
        float turnPercentAvoid = 0.50f;

        while (Button.ESCAPE.isUp()) {

            // Detect a color and get the RGB values
            curvedLineFollower.sampleProvider.fetchSample(sample, 0);

            // Calculate the distances
            the_closest_color = Color.getTheClosestColor(curvedLineFollower.sample, lineColor, backgroundColor,null, MAXIMUM_TOLERATED_DISTANCE);

            // Is it color to Avoid
            if (the_closest_color.equalsIgnoreCase(backgroundColor.getName())) {
                if (timeTurning == 0)
                    timeTurning = System.currentTimeMillis();
                leftMotor.setSpeed((int) (turnPercentAvoid * motorSpeed));
                rightMotor.setSpeed(motorSpeed);

                // Make the circle radius bigger
                if (System.currentTimeMillis() - timeTurning > timeToSpeedUp) {
                    timeTurning = 0;
                    timeToSpeedUp += 2000;
                    turnPercentAvoid += 0.10f;
                }
            }
            // Is it color to Follow
            else {
                curvedLineFollower.leftMotor.setSpeed(motorSpeed);
                curvedLineFollower.leftMotor.forward();
                curvedLineFollower.rightMotor.setSpeed(motorSpeed);
                curvedLineFollower.rightMotor.backward();
                Delay.msDelay(300);
                curvedLineFollower.leftMotor.stop(true);
                curvedLineFollower.rightMotor.stop(true);
                return;
            }

            // Make the motors move forward
            leftMotor.forward();
            rightMotor.forward();
        }
    }
}
