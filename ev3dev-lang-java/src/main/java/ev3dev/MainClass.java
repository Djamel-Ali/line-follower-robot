package ev3dev;

import ev3dev.actuators.RunMotors;

public class MainClass {

    public static void main(String[] args) {
        RunMotors runMotors = new RunMotors();
        runMotors.startTurning();
    }
}
