package edu.robots.sensors;

import java.util.ArrayList;
import java.util.Arrays;

import edu.robots.model.*;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

//The main method of this class is : startLearning(int nb_of_color_to_learn, int nb_of_measures_per_color)
//And at the output of the main loop, we could retrieve the average values of the learned colors
//(in the variable: listOfLearnedColors as a list of elements of type Color).

public class LearningColors {

	// Variables
	private static ArrayList<Color> listOfLearnedColors;

	private static double distance;

	private static Color average_color;

	// The maximum distance allowed between 2 mesures of a same color.
	private static final double MAX_ALLOWED_DISTANCE = 70;

	// For Robot Configuration
	public static EV3ColorSensor ev3ColorSensor;

	private SampleProvider sampleProvider;

	// Constructor
	public LearningColors() {
		// important variable (used in the program to follow a line)
		listOfLearnedColors = new ArrayList<>();

		// Initial distance (intermediate var)
		distance = 0;

		/* The average color of all measurements made on a given color (intermediate var)*/
		average_color = new Color("avg_color", new float[] { 0, 0, 0 });

		ev3ColorSensor = new EV3ColorSensor(SensorPort.S1);

		sampleProvider = getEv3ColorSensor().getRGBMode();
	}

	public void startLearning(int nb_of_color_to_learn, int nb_of_measures_per_color) {

		int sampleSize = sampleProvider.sampleSize();
		float[] sample = new float[sampleSize];
		ArrayList<Color> acceptable_measures = new ArrayList<>();

		// The for loop which allows to switch from learning one color to another (in
		// order to learn 'x' color at the end)
		// (knowing that 'x' is the first argument given to method.)
		for (int color_index = 0; color_index < nb_of_color_to_learn; color_index++) {

			// Loop that makes the number of measurements needed for each color
			for (int measure_counter = 0; measure_counter < nb_of_measures_per_color; measure_counter++) {
				do {
					distance = 0;

					do {
						LCD.clear();
						LCD.drawString("Color_" + color_index, 2, 0);
						LCD.drawString("Measure_" + measure_counter, 2, 2);

						// While no one has pressed the OK button yet
						LCD.drawString("'OK' to capture : ", 0, 4);
						Button.ENTER.waitForPress();

						// fetching sample...
						sample = Color.fetchDenormalizedSample(sampleProvider);

						// // First we check that it is a valid RGB triplet.
						if (isInvalidRGBTriplet(sample)) {
							LCD.clear();
							LCD.drawString("RGB invalid !", 1, 2);
							Delay.msDelay(2000);
						}
					} while (isInvalidRGBTriplet(sample));

					// if it's the 1st measure, we put it directly in the array of measures
					if (measure_counter == 0) {

						// Empty the array (acceptable measures) if it contains values (added before),
						// then fill it with the RGB values of this new measurement (which is in
						// 'sample' array).
						if (!acceptable_measures.isEmpty())
							acceptable_measures.clear();

						// Store the measure (as acceptable)
						acceptable_measures.add(0, new Color("measure_0"));
						acceptable_measures.get(0).setRgbValues(sample);

						// Update average color
						average_color.setRgbValues(sample);
					}

					// (measure_counter > 0) : The array of acceptable measures already contains
					// values,
					// ( so we have to calculate the distance of this last measurement from the
					// current average color )
					else {
						distance = Color.getDistance(average_color.getRgbValues(), sample);

						// Repeat the measurements (from the beginning).
						if (distance > MAX_ALLOWED_DISTANCE) {
							measure_counter = 0;
							average_color.reset(); // (average color : R = 0, G = 0, B = 0)
							acceptable_measures.clear();

							LCD.clear();
							LCD.drawString("Wrong measure!", 0, 0);
							LCD.drawString("(Distance == " + distance, 0, 2);
							LCD.drawString("MAX ALLOWED DISTANCE == " + MAX_ALLOWED_DISTANCE, 0, 4);

						}

						// (distance allowed) :Add this last color to the Acceptable Measurements
						// arrayList
						// and update the average color.
						else {
							acceptable_measures.add(measure_counter, new Color("measure_" + measure_counter));
							System.arraycopy(sample, 0, acceptable_measures.get(measure_counter).getRgbValues(), 0, 3);

							// update average color
							average_color.setRgbValues(Color.getAverageColor(acceptable_measures));
						}
					}
					// Reset everything to 0 before proceeding to another measurement.
					Arrays.fill(sample, 0);

				} while (distance > MAX_ALLOWED_DISTANCE);
			}
			// Save the average values of the color just learned.
			listOfLearnedColors.add(color_index, new Color("COLOR_" + color_index));
			listOfLearnedColors.get(color_index).setRgbValues(average_color.getRgbValues());

			// Reset intermediate variables to use them to learn other colors.
			average_color.reset();
		}
	}

	// It happens very rarely (at least for our current sensor) that a value exceeds
	// 255,
	// so it's better to check that.
	static boolean isInvalidRGBTriplet(float[] a_sample) {
		return a_sample[0] > 255 || a_sample[1] > 255 || a_sample[2] > 255;
	}

	public static ArrayList<Color> getListOfLearnedColors() {
		return listOfLearnedColors;
	}

	public static EV3ColorSensor getEv3ColorSensor() {
		return ev3ColorSensor;
	}

	public SampleProvider getSampleProvider() {
		return sampleProvider;
	}

}
