package ev3dev.actuators;

import ev3dev.actuators.lego.motors.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Motors {
    // Vars
    private final EV3LargeRegulatedMotor leftMotor, rightMotor;
    private int leftMotorSpeed, rightMotorSpeed;

    public Motors(int _leftMotorSpeed, int _rightMotorSpeed) throws RemoteException, MalformedURLException, NotBoundException {
        System.out.println("Creating Motor A & D");
        leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);
        rightMotor = new EV3LargeRegulatedMotor(MotorPort.D);
        leftMotorSpeed = _leftMotorSpeed;
        rightMotorSpeed = _rightMotorSpeed;

        //To Stop the motor in case of pkill java for example
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Emergency Stop");
            leftMotor.stop();
            rightMotor.stop();
        }));

        // Defining the Stop mode
        System.out.println("Defining the Stop mode");
        leftMotor.brake();
        rightMotor.brake();

        // Defining motor speed
        System.out.println("Defining motor speed");
        leftMotor.setSpeed(leftMotorSpeed);
        rightMotor.setSpeed(rightMotorSpeed);
    }

    // Go Forward with the motors
    public void moveForward(){
        leftMotor.forward();
        rightMotor.forward();
    }

    // Stop motors
    public void stopMotors(){
        leftMotor.stop();
        rightMotor.stop();
    }

    // Go Backward with the motors
    public void moveBackward(){
        leftMotor.backward();
        rightMotor.backward();
    }


    public EV3LargeRegulatedMotor getLeftMotor() {
        return leftMotor;
    }

    public EV3LargeRegulatedMotor getRightMotor() {
        return rightMotor;
    }

    public int getLeftMotorSpeed() {
        return leftMotorSpeed;
    }

    public void setLeftMotorSpeed(int leftMotorSpeed) {
        this.leftMotorSpeed = leftMotorSpeed;
    }

    public int getRightMotorSpeed() {
        return rightMotorSpeed;
    }

    public void setRightMotorSpeed(int rightMotorSpeed) {
        this.rightMotorSpeed = rightMotorSpeed;
    }
}
