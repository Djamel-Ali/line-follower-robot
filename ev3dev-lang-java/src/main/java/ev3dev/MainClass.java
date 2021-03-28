package ev3dev;

import ev3dev.functionalities.LineFollower;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class MainClass {

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {
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
        LineFollower lineFollower = new LineFollower();
        lineFollower.followTheLine();
    }
}

