package ev3dev;

import ev3dev.actuators.RunMotors;
import ev3dev.model.Color;
import ev3dev.sensors.Button;
import ev3dev.sensors.ev3.EV3ColorSensor;
import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

public class MainClass {

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {
        RunMotors runMotors = new RunMotors();
        runMotors.startTurning();
//        ArrayList<Color> colorsList = new ArrayList<>(3);
//        ArrayList<Color> bis = new ArrayList<>(2);
//        EV3ColorSensor ev3ColorSensor = new EV3ColorSensor(SensorPort.S1);
//        SampleProvider sampleProvider = ev3ColorSensor.getRGBMode();
//        int sampleSize = sampleProvider.sampleSize();
//        float[] sample = new float[sampleSize];
//
//        for (int i = 0; i < 3; i++){
//           System.out.println("\n Press OK to fetch sample  : ");
//            Button.ENTER.waitForPressAndRelease();
//            sampleProvider.fetchSample(sample, 0);
//            colorsList.add(i,new Color("color_"+i));
//            colorsList.get(i).setRgbValues(sample);
//            if(i == 0 || i == 1)
//            {
//                bis.add(i , new Color("color_"+i));
//                bis.get(i).setRgbValues(sample);
//            }
//        }
//        float[] medianRGB = Color.getAverageColor(bis);
//        Color calculatedMedian = new Color("median", medianRGB);
//
//        System.out.println("line == " + colorsList.get(0));
//        System.out.println("background == " + colorsList.get(1));
//        System.out.println("median captured == " + colorsList.get(2));
//        System.out.println("median calculated == " + calculatedMedian);
    }
}
