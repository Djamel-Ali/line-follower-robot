package edu.robots.behaviours;

import java.util.ArrayList;

import edu.robots.model.Color;
import edu.robots.sensors.LearningColors;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;


public class PIDLineFollower {

	// Attributes
	
	private final static int NB_OF_COLORS_TO_LEARN = 4;
	
	/* (it's equal to 1 when he's on the line, -1 on the background, 0 on the frontier (50% line, 50% background)).*/
	private static int I_AM_IN_LINE = 0;
	private static final double MAXIMUM_TOLERATED_DISTANCE = 85; /*between two colors*/
	private static float[] sample;
	private static final LearningColors learningColors = new LearningColors();
	private static ArrayList<Color> listOfLearnedColors;
	
	/* The value of 'medianColor' will be calculated as the average of the values of 'calculatedMedianColor' and 'capturedMedianColor */
	private static Color lineColor, backgroundColor, medianColor, calculatedMedianColor, capturedMedianColor, stopColor;

	// Motors;
	private static EV3LargeRegulatedMotor rightMotor;
	private static EV3LargeRegulatedMotor leftMotor;

	private float leftMotorSpeed, rightMotorSpeed, motorSpeed;

	/* PID main variables */
	static double turn = 0, error, last_error = 0, proportionality_constant_P, proportionality_constant_I,
				  proportionality_constant_D, derivative = 0;
	static int target_power, integral = 0;
	static String the_closest_color;

	public PIDLineFollower() {
		listOfLearnedColors = new ArrayList<>(NB_OF_COLORS_TO_LEARN);
		rightMotor = new EV3LargeRegulatedMotor(MotorPort.D);
		leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);

		// Defining motor speed
		motorSpeed = 100;
		leftMotor.setSpeed(motorSpeed);
		rightMotor.setSpeed(motorSpeed);
	};

	// Learn the colors and calculate the values of the parameters kp, ki and kd to
	// use to follow the line.
	public static void getReady() {
		// get the list of learned colors
		listOfLearnedColors = LearningColors.getListOfLearnedColors();

		// Color identification
		
		//line
		lineColor = listOfLearnedColors.get(0);
		lineColor.setName("LINE");
		
		// background
		backgroundColor = listOfLearnedColors.get(1);
		backgroundColor.setName("BACKGROUND");
		
		// median
		capturedMedianColor = listOfLearnedColors.get(2);
		medianColor = getMedianColor(lineColor, backgroundColor, capturedMedianColor);
		
		// stop
		stopColor = listOfLearnedColors.get(3);
		stopColor.setName("STOP");
		
		// Calcule de la distance max qui peut être enregistrée:
		double max_distance = Math.max(Color.getDistance(lineColor.getRgbValues(), medianColor.getRgbValues()),
				Color.getDistance(backgroundColor.getRgbValues(), medianColor.getRgbValues()));

		double avg_slope = getAverageMaxMotorSpeed(leftMotor, rightMotor) / max_distance;

//----------------------------------------------------------------------------------
/*

// ** P : 
// *  Avec P : robot réagissant Proportionnellement à la valeur de l'erreur.
// *  Sans 'Integral' le robot n'a pas de mémoire, et sans 'dérivé' il n'anticipe pas l'avenir,
// *  il ne prend en compte que la valeur de l'erreur qui vient de se produire.
 
				target_power = (int) (0.174f * getAverageMaxMotorSpeed(leftMotor, rightMotor));
				proportionality_constant_P = 0.154374961f * avg_slope; 
				proportionality_constant_I = 0.007f * avg_slope;
				proportionality_constant_D = 0.7f * avg_slope;

*/	
//----------------------------------------------------------------------------------

//----------------------------------------------------------------------------------
		
// ** PI: 
// *  Avec P : robot réagissant Proportionnellement à la valeur de l'erreur.
// *  Sans 'La dérivée' le robot n'anticipe pas l'avenir 
// * (les bonnes valeurs pour l'algo PI définies à partir de plusieurs essais) (avec integral = 0.9f)

				target_power = (int) (0.239f * getAverageMaxMotorSpeed(leftMotor, rightMotor));
				proportionality_constant_P = 0.07f * avg_slope;
				proportionality_constant_I = 0.007f * avg_slope;

//----------------------------------------------------------------------------------

//----------------------------------------------------------------------------------
/*

// * PID program : robot (réagissant Proportionnellement à la valeur de l'erreur) avec mémoire (Integral) et capacité d'anticiper l'avenir (Dérivé)
// * (paramètres définis à partir de plusieurs essais)
// * Moyenne du dt = 0,773679996
		 
				target_power = (int) (0.17f * getAverageMaxMotorSpeed(leftMotor, rightMotor));// tp= 0.3f*... est bonne aussi au lieu de 0.17f
				proportionality_constant_P = 0.1543749f * avg_slope; // (kp = kc = 0.16f * avg_slope; Tp=0.17f*max_speed)~~~~(kp= kc = 0.154374961*slope; Tp=0.17f*max_speed)
				proportionality_constant_I = 0.007f * avg_slope;
				proportionality_constant_D = 0.5 * avg_slope;

*/
//----------------------------------------------------------------------------------

//----------------------------------------------------------------------------------
/*
 				
// PID program : (comme le précidant, mais le processus pour définir ces valeurs est différent).
// * (paramètres définis en suivant les étapes suggérée dans cet article : http://www.inpharmix.com/jps/PID_Controller_For_Lego_Mindstorms_Robots.html)
				target_power = (int) (0.174f * getAverageMaxMotorSpeed(leftMotor, rightMotor));// tp= 0.3f*... est bonne au lieu de 0.17f
				proportionality_constant_P = 0.154374961f * avg_slope; // (kp = kc = 0.16f * avg_slope; Tp=0.17f*max_speed)~~~~(kp= kc = 0.154374961*slope; Tp=0.17f*max_speed)
				proportionality_constant_I = 0.007f * avg_slope;
				proportionality_constant_D = 0.7f * avg_slope;

// constantes définis en suivant les étapes suggérées dans l'article de référence.
		double kc = 0.1543749 * avg_slope;
		double pc = 0.5;
		double dt = 0.773679996;

// target_power = (int) (0.17f * getAverageMaxMotorSpeed(leftMotor, rightMotor));// tp= 0.3f*... est bonne au lieu de 0.17f
// proportionality_constant_P = 0.09f * kc; (et hauteur = 6 mm)

*/
//----------------------------------------------------------------------------------

		target_power = (int) (0.3f * getAverageMaxMotorSpeed(leftMotor, rightMotor));

		LCD.clear();
		LCD.drawString("'OK' to start ?", 1, 4);
		Button.ENTER.waitForPressAndRelease();
	}

	public void followTheLine() {

		LCD.clear();
		LCD.drawString("FOLLOWING LINE...", 1, 4);

		// While the 'Esc' button is not pressed, the robot continues to follow the line
		do {
			// Detect a color and get the RGB values
			sample = Color.fetchDenormalizedSample(learningColors.getSampleProvider());

			// what is the current color reading ?
			// Calculate the distances (in order to find out which color this sample is
			// closest to (line or background or median or stop or other).
			the_closest_color = Color.getTheClosestColor(sample, lineColor, backgroundColor, medianColor,
					stopColor, MAXIMUM_TOLERATED_DISTANCE);

			if (the_closest_color.equalsIgnoreCase(lineColor.getName()))
				I_AM_IN_LINE = 1; /* il faut qu'il tourne à droite (car c'est un suiveur de ligne à droite) */
			else if (the_closest_color.equalsIgnoreCase(backgroundColor.getName()))
				I_AM_IN_LINE = -1; /* il faut qu'il tourne à gauche (car c'est un suiveur de ligne à droite) */
			else if (the_closest_color.equalsIgnoreCase(medianColor.getName()))
				I_AM_IN_LINE = 0; /* il faut qu'il continue tout droit */
			
			// if OTHER OR STOP_COLOR
			else break;

			double sample_median_distance = Color.getDistance(sample, medianColor.getRgbValues());

			// calculate the error
			error = sample_median_distance * I_AM_IN_LINE;

			if (error == 0)
				integral = 0;
			else
				// update 'integral' variable
				integral = (int) (0.9f * integral + error);

			// calculate the derivative
			derivative = error - last_error;

			// update 'turn' variable
			turn = proportionality_constant_P * error + proportionality_constant_I * integral
					+ proportionality_constant_D * derivative;

			// the power level for the left motor
			leftMotorSpeed = target_power + (float) turn;

			// the power level for the right motor
			rightMotorSpeed = target_power - (float) turn;

			setMotorsSpeed(leftMotorSpeed, rightMotorSpeed);

			// update last error
			last_error = error;
			
			if (Button.ESCAPE.isDown())
				break;

		} while (Button.ESCAPE.isUp());
		
		Sound.twoBeeps();
		// stop motors (Esc has been clicked, or STOP_COLOR or OTHER color has been detected)
		leftMotor.stop();
		rightMotor.stop();
	}

	private void setMotorsSpeed(float _leftMotorSpeed, float _rightMotorSpeed) {
		if (_leftMotorSpeed > 0 && _rightMotorSpeed > 0) {

			// set speed (with the new power level)
			leftMotor.setSpeed(_leftMotorSpeed);
			rightMotor.setSpeed(_rightMotorSpeed);

			// Make the motors move forward
			leftMotor.forward();
			rightMotor.forward();
		} else {
			if (_leftMotorSpeed < 0 && _rightMotorSpeed > 0) {

				_leftMotorSpeed = -_leftMotorSpeed;

				// set speed (with the new power level)
				leftMotor.setSpeed(_leftMotorSpeed);
				rightMotor.setSpeed(_rightMotorSpeed);

				
				leftMotor.backward();
				rightMotor.forward();
			} else if (_rightMotorSpeed < 0 && _leftMotorSpeed > 0) {

				_rightMotorSpeed = -_rightMotorSpeed;

				// set speed (with the new power level)
				leftMotor.setSpeed(_leftMotorSpeed);
				rightMotor.setSpeed(_rightMotorSpeed);

				leftMotor.forward();
				rightMotor.backward();
			} else {
				_leftMotorSpeed = -_leftMotorSpeed;
				_rightMotorSpeed = -_rightMotorSpeed;

				// set speed (with the new power level)
				leftMotor.setSpeed(_leftMotorSpeed);
				rightMotor.setSpeed(_rightMotorSpeed);

				// Make the motors move backward
				leftMotor.backward();
				rightMotor.backward();
			}
		}
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

	public static LearningColors getLearningColors() {
		return learningColors;
	}
}
