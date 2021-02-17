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

public class LineFollower {
    // Attributes :
    private final static int NB_OF_COLORS_TO_LEARN = 3;
    private final static int NB_OF_MEASURES_PER_COLOR = 6;
    private static int I_AM_IN_LINE = 1; // (it is -1 when it is not on the line)
    private static final double MAXIMUM_TOLERATED_DISTANCE = 110;
    private static float[] sample;
    private final SampleProvider sampleProvider;
    private static EV3ColorSensor ev3ColorSensor;
    private final LearningColors learningColors;
    private ArrayList<Color> listOfLearnedColors;
    private static Color lineColor, backgroundColor, medianColor, calculatedMedianColor, capturedMedianColor;
//    private Motors motors;

    //Robot Definition (and initialization):
    private static EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);
    private static EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.D);
    private int leftMotorSpeed, rightMotorSpeed, motorSpeed, targetPower;

    // Distance between the line color and the background color.
    private double distanceLineBackground, offset;

    public LineFollower() {
        learningColors = new LearningColors();
        listOfLearnedColors = new ArrayList<>(NB_OF_COLORS_TO_LEARN);
        distanceLineBackground = 0;
        ev3ColorSensor = new EV3ColorSensor(SensorPort.S1);
        sampleProvider = ev3ColorSensor.getRGBMode();
        int sampleSize = sampleProvider.sampleSize();
        sample = new float[sampleSize];

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
        motorSpeed = 100;
        leftMotor.setSpeed(motorSpeed);
        rightMotor.setSpeed(motorSpeed);
    }

    public static void main(String[] args) {
        // * Proportionality constant is the factor that you have to multiply the error (x value) by
        //   to convert it into a Turn (y value).
        // * The turns range from -1 (hard turn to the left) to +1 (hard turn to the right)
        // and a zero turn means we are going straight.
        double turn, error, proportionality_constant/*, ki = 0.05*/;
        int target_power/*, integral = 0*/;
        String the_closest_color;

        // Initialization
        LineFollower lineFollower = new LineFollower();

        // Start of the learning phase
        lineFollower.learningColors.startLearning(NB_OF_COLORS_TO_LEARN, NB_OF_MEASURES_PER_COLOR);
        // End of the learning phase

        // get the list of learned colors
       lineFollower.listOfLearnedColors = LearningColors.getListOfLearnedColors();

        // Color identification
        lineColor = lineFollower.listOfLearnedColors.get(0);
        lineColor.setName("LINE");
        backgroundColor = lineFollower.listOfLearnedColors.get(1);
        backgroundColor.setName("BACKGROUND");
        capturedMedianColor = lineFollower.listOfLearnedColors.get(2);
        medianColor = getMedianColor(lineColor, backgroundColor, capturedMedianColor);
//        float[] f =  {(float)12.166667, (float)16.166666, (float)7.6666665};
//        lineColor = new Color("LINE", f);
//
//        f =  new float[]{(float)191.83333, (float)191.83333, (float)129.16667};
//        backgroundColor = new Color("BACKGROUND",f);
//
//        f = new float[]{(float)99.5, (float)98.66667, (float)66.458336};
//        medianColor = new Color("FRONTIER", f);

        System.out.println("\n\n\t Affichages:");
        System.out.println("\t LINE : " +  lineColor);
        System.out.println("\t BACKGROUND : " +  backgroundColor);
        System.out.println("\t FRONTIER : " +  medianColor);

        // the max distance is the average of the two distances color of the line - color of the border
        // and color of the background - color of the border
//        double max_distance = getMaxDistance();
        double max_distance = Math.max(Color.getDistance(lineColor.getRgbValues(), medianColor.getRgbValues()),
                Color.getDistance(backgroundColor.getRgbValues(), medianColor.getRgbValues()));

        // slope = m = k= proportionality_constant = (change in y)/(change in x)
        // example of points here : (-100, 1) and (100, -1)
        // => for example : m =  ( 1- (-1)) / (-100 - 100 ) = -1/100 = -0.01
        // the two points are : (-max_distance, 1) and (max_distance, -1); with error changes in x, turn changes in y.
        // in our case : proportionality_constant = 2 / (-max_distance - max_distance);
//        proportionality_constant = -1 / (4*max_distance);

        // error == distance(captured-median) * I_AM_IN_LINE ,
        // with I_AM_IN_LINE == 1 in line, I_AM_IN_LINE == -1 in background  I_AM_IN_LINE == 0 in frontier
        // So error is in { (- max_distance) ; (max_distance) }
        // Turn = K*(error)
        // One motor will get a power level of targetPower+Turn, the other motor will get a power level of targetPower-Turn.

        /*One motor (we'll assume it is the motor on the left of the robot plugged into port A)
        will always get the Tp+Turn value as it's power level. The other motor
        (right side of robot, port C) will always get Tp-Turn as it's power level.*/

//        lineFollower.distanceLineBackground = Color.getDistance(lineColor.getRgbValues(), backgroundColor.getRgbValues());
//        lineFollower.offset = lineFollower.distanceLineBackground/2;

        // We'll guess that we want the power to go from ' motor.getMaxSpeed()/2 ' to 0 when the error goes from
        // 0 to ' - max_distance '. That means the Kp (the slope remember, the change in y divided by the change in x) is;

        //Kp = (0 - motor.getMaxSpeed()/2)/(- max_distance - 0)
        // in our case : proportionality_constant = (-target_power)/(-max_distance);
        target_power = (int)getAverageMaxMotorSpeed(leftMotor ,rightMotor)/18;
        proportionality_constant = 885*target_power/(1000*max_distance);
        System.out.println("proportionality_constant" + proportionality_constant);
        System.out.println("target_power" + target_power);
        System.out.println("max_distance" + max_distance);

        /*We will use the Kp value to convert an error value into a turn value. In words our conversion is
        "for every 1 unit change in the error we will increase the power of one motor by kp".
        The other motor's power gets decreased by kp.*/

        System.out.println("\n Click OK to begin following the line: \n");
        ENTER.waitForPressAndRelease();
        System.out.println("\n Start following the line : ");

        // While the brick is detecting
        while (Button.ESCAPE.isUp()){
            // Detect a color and get the RGB values
            lineFollower.sampleProvider.fetchSample(sample, 0);
            System.out.println("\t SAMPLE : R = " +  sample[0] + "; G = "+ sample[1] + "; B = "+ sample[2]);

            // what is the current color reading ?
            // Calculate the distances (in order to find out which color this sample is closest to (line or background or other).
            the_closest_color = Color.getTheClosestColor(sample, backgroundColor, lineColor, medianColor, MAXIMUM_TOLERATED_DISTANCE);
            double dis = Color.getDistance(sample, medianColor.getRgbValues());
            System.out.println("\t Closest_color-Median DISTANCE == " + dis);
            System.out.println("\t the_closest_color found : " +  the_closest_color);

//            if(the_closest_color.equalsIgnoreCase("OTHER")){
//                System.out.println("\n\n\t COLOR == OTHER \n");
//                leftMotor.setSpeed(250);
//                rightMotor.setSpeed(250);
//                leftMotor.forward();
//                rightMotor.forward();
////                leftMotor.stop();
////                rightMotor.stop();
////                return;
//            }
//            else{
                if(the_closest_color.equalsIgnoreCase(lineColor.getName())) I_AM_IN_LINE= 1;
                else if(the_closest_color.equalsIgnoreCase(backgroundColor.getName()) ) I_AM_IN_LINE= -1;
                if(the_closest_color.equalsIgnoreCase(medianColor.getName()) || the_closest_color.equalsIgnoreCase("OTHER")) I_AM_IN_LINE= 0;

                // calculate the error
                error = Color.getDistance(sample, medianColor.getRgbValues())/2 * I_AM_IN_LINE;

                // update ki
                //integral+= error;

                // the "P term", how much we want to change the motors' power
                turn = proportionality_constant * error /*+ ki*integral*/;

                // the power level for the A motor
                lineFollower.leftMotorSpeed = target_power + (int)turn;

                // the power level for the C motor
                lineFollower.rightMotorSpeed = target_power - (int)turn;

                if(lineFollower.leftMotorSpeed > 0 && lineFollower.rightMotorSpeed > 0){

                    // set speed (with the new power level)
                    leftMotor.setSpeed(lineFollower.leftMotorSpeed);
                    rightMotor.setSpeed(lineFollower.rightMotorSpeed);

                    // Make the motors move forward
                    leftMotor.forward();
                    rightMotor.forward();
                }
                else {
                    if(lineFollower.leftMotorSpeed < 0 && lineFollower.rightMotorSpeed > 0){

                        lineFollower.leftMotorSpeed = -lineFollower.leftMotorSpeed;

                        // set speed (with the new power level)
                        leftMotor.setSpeed(lineFollower.leftMotorSpeed);
                        rightMotor.setSpeed(lineFollower.rightMotorSpeed);

                        // Make the motors move forward
                        leftMotor.backward();
                        rightMotor.forward();
                    }
                    else if(lineFollower.rightMotorSpeed < 0 && lineFollower.leftMotorSpeed > 0){

                        lineFollower.rightMotorSpeed = -lineFollower.rightMotorSpeed;

                        // set speed (with the new power level)
                        leftMotor.setSpeed(lineFollower.leftMotorSpeed);
                        rightMotor.setSpeed(lineFollower.rightMotorSpeed);

                        rightMotor.backward();
                        leftMotor.forward();
                    }
                    else{
                        lineFollower.leftMotorSpeed = -lineFollower.leftMotorSpeed;
                        lineFollower.rightMotorSpeed = -lineFollower.rightMotorSpeed;

                        // set speed (with the new power level)
                        leftMotor.setSpeed(lineFollower.leftMotorSpeed);
                        rightMotor.setSpeed(lineFollower.rightMotorSpeed);
                        leftMotor.backward();
                        rightMotor.backward();

                    }
                }


//            }
        }
        // stop motors
        leftMotor.stop();
        rightMotor.stop();
    }

    static Color getMedianColor(Color _lineColor, Color _backgroundColor, Color _capturedMedianColor) {

        ArrayList<Color> tempList = new ArrayList<>(2);
        tempList.add(0, _lineColor);
        tempList.add(1, _backgroundColor);
        float[] calculatedMedianColorRGB = Color.getAverageColor(tempList);
        calculatedMedianColor = new Color("calculatedMedianColor", calculatedMedianColorRGB);
        tempList.clear();
        tempList.add(0, _capturedMedianColor);
        tempList.add(1, calculatedMedianColor);
        float[] medianColorRGB = Color.getAverageColor(tempList);
        return new Color("FRONTIER", medianColorRGB);
    }

    static float getAverageMaxMotorSpeed(EV3LargeRegulatedMotor _leftMotor, EV3LargeRegulatedMotor _rightMotor){
        return (_leftMotor.getMaxSpeed()+_rightMotor.getMaxSpeed())/2;
    }
}
