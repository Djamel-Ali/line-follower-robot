package edu.robots.mainclass;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import edu.robots.actuators.RunMotors;
import edu.robots.fonctionalities.LineFollower;
import edu.robots.fonctionalities.PIDLineFollower;
import edu.robots.helloworld.HelloWorld;
import edu.robots.sensors.LearningColors;

public class MainClass {

	public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {

		// Hello World test
//		HelloWorld hello = new HelloWorld();
//		hello.sayHello();
		
		// Motors test
//		RunMotors runMotors = new RunMotors();
//        runMotors.startTurning();
		
		// Sensors test
//		LearningColors learningColors = new LearningColors();
//		learningColors.startLearning(2, 3);
		
		// Simple 'S' shape line follower
//      LineFollower lineFollower = new LineFollower();
//      lineFollower.followTheLine();
		
		// 'S' shape line follower (Using PID algorithm)
      PIDLineFollower PIDlineFollower = new PIDLineFollower();
      PIDlineFollower.followTheLine();
	}

}
