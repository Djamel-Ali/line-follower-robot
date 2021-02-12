package ev3dev.functionalities;

import ev3dev.actuators.lego.motors.EV3LargeRegulatedMotor;
import ev3dev.model.Color;
import ev3dev.sensors.Button;
import ev3dev.sensors.LearningColors;
import ev3dev.sensors.ev3.EV3ColorSensor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import static ev3dev.sensors.Button.ENTER;

public class StraightLineFollower {

    // Attributes :
    private final static int NB_OF_COLORS_TO_LEARN = 2;
    private final static int NB_OF_MEASURES_PER_COLOR = 3;
    private static float[] sample;
    private static final char LINE = 'L', BACKGROUND = 'B', FRONTIER = 'F';
    private static final double MAXIMUM_TOLERATED_DISTANCE = 40;
    private final SampleProvider sampleProvider;
    private static EV3ColorSensor ev3ColorSensor;
    private final LearningColors learningColors;
    private ArrayList<Color> listOfLearnedColors;
    private static Color lineColor, backgroundColor;
//    private Motors motors;

    EV3LargeRegulatedMotor leftMotor, rightMotor;
    private int leftMotorSpeed, rightMotorSpeed;

    // Distance between the line color and the background color.
    private final double distanceLineBackground;

    public StraightLineFollower() throws RemoteException, MalformedURLException, NotBoundException {
        learningColors = new LearningColors();
        listOfLearnedColors = new ArrayList<>(NB_OF_COLORS_TO_LEARN);
        distanceLineBackground = 0;
        ev3ColorSensor = new EV3ColorSensor(SensorPort.S1);
        sampleProvider = ev3ColorSensor.getRGBMode();
        int sampleSize = sampleProvider.sampleSize();
        sample = new float[sampleSize];

        // Initialization of motor speed
//        motors = new Motors(195, 195);
        this.leftMotor = new EV3LargeRegulatedMotor(MotorPort.D);
        this.rightMotor = new EV3LargeRegulatedMotor(MotorPort.A);


        //To Stop the motor in case of pkill java for example
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Emergency Stop");
            this.leftMotor.stop();
            this.rightMotor.stop();
        }));

        System.out.println("Defining the Stop mode");
        this.leftMotor.brake();
        this.rightMotor.brake();

        System.out.println("Defining motor speed");
        leftMotorSpeed = 200;
        rightMotorSpeed = 200;
        this.leftMotor.setSpeed(leftMotorSpeed);
        this.rightMotor.setSpeed(rightMotorSpeed);
    }

    public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {

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
            String closestColor = getTheClosestColor(sample);

            // Display the detected color
            System.out.println("\n\n ## " + closestColor + " ## \n\n");

            if (closestColor.equalsIgnoreCase(lineColor.getName())) {
                if (elapsed_time_on_the_line == 0)
                    elapsed_time_on_the_line = System.currentTimeMillis();
                else if (System.currentTimeMillis() - elapsed_time_on_the_line > 500) {
                    straightLineFollower.leftMotorSpeed = 400;
                    straightLineFollower.rightMotorSpeed = 400;
                } else if (System.currentTimeMillis() - elapsed_time_on_the_line > 200) {
                    straightLineFollower.leftMotorSpeed = 350;
                    straightLineFollower.rightMotorSpeed = 350;
                } else {
                    straightLineFollower.leftMotorSpeed = 200;
                    straightLineFollower.rightMotorSpeed = 200;
                }
                elapsed_time_on_the_background = 0;
                straightLineFollower.leftMotor.setSpeed(straightLineFollower.leftMotorSpeed);
                straightLineFollower.rightMotor.setSpeed(straightLineFollower.rightMotorSpeed);

                // Go Forward with the motors
                straightLineFollower.leftMotor.forward();
                straightLineFollower.rightMotor.forward();
            }

            // He has moved away from the color to follow (He goes out of line)
            else {
                if (elapsed_time_on_the_background == 0)
                    elapsed_time_on_the_background = System.currentTimeMillis();
//                else if (System.currentTimeMillis() - elapsed_time_on_the_background > 100){
//                    straightLineFollower.rightMotor.stop();
//                }
                elapsed_time_on_the_line = 0;

                // the robot turns around itself
                straightLineFollower.leftMotorSpeed = 200;
                straightLineFollower.rightMotorSpeed = 0;
                straightLineFollower.leftMotor.setSpeed(straightLineFollower.leftMotorSpeed);
                straightLineFollower.rightMotor.setSpeed(straightLineFollower.rightMotorSpeed);
                straightLineFollower.leftMotor.forward();
                straightLineFollower.rightMotor.forward();
            }
        }

        straightLineFollower.rightMotor.stop();
        straightLineFollower.rightMotor.stop();
    }

    // Returns the closest color to the one he just captured (Returns either the line "LINE" or the background "BACKGROUND")
    static String getTheClosestColor(float[] _sample) throws NullPointerException {
        double distance = Color.getDistance(_sample, lineColor.getRgbValues());
        if (distance < MAXIMUM_TOLERATED_DISTANCE) {
            // LINE (So keep following it (straight))
            return "LINE";
        } else return "BACKGROUND";
    }
}
