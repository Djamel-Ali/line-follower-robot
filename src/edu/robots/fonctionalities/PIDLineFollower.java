package edu.robots.fonctionalities;

import java.util.ArrayList;

import edu.robots.model.Color;
import edu.robots.sensors.LearningColors;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;

public class PIDLineFollower {

	// Attributes
	private final static int NB_OF_COLORS_TO_LEARN = 3;
	private final static int NB_OF_MEASURES_PER_COLOR = 3;
	private static int I_AM_IN_LINE = 1; // (it's equal to 1 when he's on the line, -1 on the background, 0 on the
											// frontier)
	private static final double MAXIMUM_TOLERATED_DISTANCE = 110;
	private static float[] sample;
	private final LearningColors learningColors;
	private ArrayList<Color> listOfLearnedColors;
	private static Color lineColor, backgroundColor, medianColor, calculatedMedianColor, capturedMedianColor;

	// Motors;
	private EV3LargeRegulatedMotor rightMotor;
	private EV3LargeRegulatedMotor leftMotor;

	private int leftMotorSpeed, rightMotorSpeed, motorSpeed, targetPower;

	// Distance between the line color and the background color.
	private double distanceLineBackground, offset;

	public PIDLineFollower() {
		learningColors = new LearningColors();
		listOfLearnedColors = new ArrayList<>(NB_OF_COLORS_TO_LEARN);
		distanceLineBackground = 0;
		rightMotor = new EV3LargeRegulatedMotor(MotorPort.D);
		leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);

		// Defining motor speed
		motorSpeed = 100;
		leftMotor.setSpeed(motorSpeed);
		rightMotor.setSpeed(motorSpeed);
	};

	public void followTheLine() {
		double turn, error, proportionality_constant;
		int target_power;
		String the_closest_color;

		// Start of the learning phase
		learningColors.startLearning(NB_OF_COLORS_TO_LEARN, NB_OF_MEASURES_PER_COLOR);
		// End of the learning phase

		// get the list of learned colors
		listOfLearnedColors = LearningColors.getListOfLearnedColors();

		// Color identification
		lineColor = listOfLearnedColors.get(0);
		lineColor.setName("LINE");
		backgroundColor = listOfLearnedColors.get(1);
		backgroundColor.setName("BACKGROUND");
		capturedMedianColor = listOfLearnedColors.get(2);
		medianColor = getMedianColor(lineColor, backgroundColor, capturedMedianColor);

		// Calcule de la distance max qui peut être enregistrée:
		double max_distance = Math.max(Color.getDistance(lineColor.getRgbValues(), medianColor.getRgbValues()),
				Color.getDistance(backgroundColor.getRgbValues(), medianColor.getRgbValues()));

		target_power = (int) (0.145f * getAverageMaxMotorSpeed(leftMotor, rightMotor));
		proportionality_constant = 0.804f * target_power / max_distance;

		LCD.clear();
		LCD.drawString("'OK' to start ?", 1, 4);
		Button.ENTER.waitForPressAndRelease();
		// System.out.println("\n Start following the line : ");

		// While the brick is detecting
		while (Button.ESCAPE.isUp()) {
			// Detect a color and get the RGB values
			sample = Color.fetchDenormalizedSample(learningColors.getSampleProvider());
//			System.out.println("\t SAMPLE : R = " + sample[0] + "; G = " + sample[1] + "; B = " + sample[2]);

			// what is the current color reading ?
			// Calculate the distances (in order to find out which color this sample is
			// closest to (line or background or other).
			the_closest_color = Color.getTheClosestColor(sample, backgroundColor, lineColor, medianColor,
					MAXIMUM_TOLERATED_DISTANCE);
			double sample_median_distance = Color.getDistance(sample, medianColor.getRgbValues());
//			System.out.println("\t Sample-Median Distance == " + sample_median_distance);
//			System.out.println("\t the_closest_color found : " + the_closest_color);

			if (the_closest_color.equalsIgnoreCase(lineColor.getName()))
				I_AM_IN_LINE = 1;
			else if (the_closest_color.equalsIgnoreCase(backgroundColor.getName())
					|| the_closest_color.equalsIgnoreCase("OTHER"))
				I_AM_IN_LINE = -1;
			if (the_closest_color.equalsIgnoreCase(medianColor.getName()))
				I_AM_IN_LINE = 0;

			// calculate the error
			error = sample_median_distance * I_AM_IN_LINE;

			turn = proportionality_constant * error;

			// the power level for the left motor
			leftMotorSpeed = target_power + (int) turn;

			// the power level for the right motor
			rightMotorSpeed = target_power - (int) turn;

			if (leftMotorSpeed > 0 && rightMotorSpeed > 0) {

				// set speed (with the new power level)
				leftMotor.setSpeed(leftMotorSpeed);
				rightMotor.setSpeed(rightMotorSpeed);

				// Make the motors move forward
				leftMotor.forward();
				rightMotor.forward();
			} else {
				if (leftMotorSpeed < 0 && rightMotorSpeed > 0) {

					leftMotorSpeed = -leftMotorSpeed;

					// set speed (with the new power level)
					leftMotor.setSpeed(leftMotorSpeed);
					rightMotor.setSpeed(rightMotorSpeed);

					// Make the motors move forward
					leftMotor.backward();
					rightMotor.forward();
				} else if (rightMotorSpeed < 0 && leftMotorSpeed > 0) {

					rightMotorSpeed = -rightMotorSpeed;

					// set speed (with the new power level)
					leftMotor.setSpeed(leftMotorSpeed);
					rightMotor.setSpeed(rightMotorSpeed);

					rightMotor.backward();
					leftMotor.forward();
				} else {
					leftMotorSpeed = -leftMotorSpeed;
					rightMotorSpeed = -rightMotorSpeed;

					// set speed (with the new power level)
					leftMotor.setSpeed(leftMotorSpeed);
					rightMotor.setSpeed(rightMotorSpeed);
					leftMotor.backward();
					rightMotor.backward();
				}
			}
		}
		// stop motors (Esc has been clicked)
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

	static float getAverageMaxMotorSpeed(EV3LargeRegulatedMotor _leftMotor, EV3LargeRegulatedMotor _rightMotor) {
		return (_leftMotor.getMaxSpeed() + _rightMotor.getMaxSpeed()) / 2;
	}
}
