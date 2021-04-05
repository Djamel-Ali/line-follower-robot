package edu.robots.model;

import java.util.ArrayList;
import java.util.Arrays;

import lejos.robotics.SampleProvider;

public class Color {

	// Attributes
	private String name;
	private float[] rgbValues;

	// Constructor
	public Color(String name, float[] rgbValues) {
		this.name = name;
		this.rgbValues = rgbValues;
	}

	public Color(String name) {
		this.name = name;
		this.rgbValues = new float[] { 0, 0, 0 };
	}

	// Getters and Setters
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float[] getRgbValues() {
		return rgbValues;
	}

	public void setRgbValues(float[] _rgbValues) {
		// (arrayclone is the most suitable and efficient)
		System.arraycopy(_rgbValues, 0, rgbValues, 0, _rgbValues.length);
	}

	public void reset() {
		Arrays.fill(rgbValues, 0);
	}

	public static double getDistance(float[] rgb_color1, float[] rgb_color2) {
		return Math.sqrt(Math.pow(rgb_color1[0] - rgb_color2[0], 2) + Math.pow(rgb_color1[1] - rgb_color2[1], 2)
				+ Math.pow(rgb_color1[2] - rgb_color2[2], 2));
	}

	// Returns the closest color to the one he just captured (Returns either the
	// line "LINE",
	// or the background "BACKGROUND" or OTHER if the captured color is very far
	// from the two previous ones)
	public static String getTheClosestColor(float[] _sample, Color _lineColor, Color _backgroundColor,
			Color _medianColor, double _MAXIMUM_TOLERATED_DISTANCE) throws NullPointerException {

		// calculate the distance from the sample to {line, background, median}
		double distance_Sample_line = Color.getDistance(_sample, _lineColor.getRgbValues());
		double distance_Sample_background = Color.getDistance(_sample, _backgroundColor.getRgbValues());
		double distance_Sample_median = Color.getDistance(_sample, _medianColor.getRgbValues());

		// find the min :
		double tempMin = Math.min(distance_Sample_line, distance_Sample_background);
		double min = Math.min(tempMin, distance_Sample_median);
		 
		 
		// identify the closest color :
		if (min <= _MAXIMUM_TOLERATED_DISTANCE) {
			if (min == distance_Sample_line) {
				return "LINE";
			} else if (min == distance_Sample_background) {
				return "BACKGROUND";
			} else {
				return "FRONTIER";
			}
		} else {
			return "OTHER";
		}
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

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("[");

		str.append(Color.class.getSimpleName());
		str.append("]");
		str.append(", name='");
		str.append(name);
		str.append("'");
		str.append(", rgbValues=");
		str.append(Arrays.toString(rgbValues));

		return str.toString();

	}

	public static float[] fetchDenormalizedSample(SampleProvider _sampleProvider) {

		// local variables :
		float[] my_sample = new float[_sampleProvider.sampleSize()];

		_sampleProvider.fetchSample(my_sample, 0);
		my_sample[0] = my_sample[0] * 255;
		my_sample[1] = my_sample[1] * 255;
		my_sample[2] = my_sample[2] * 255;

		return my_sample;
	}

}
