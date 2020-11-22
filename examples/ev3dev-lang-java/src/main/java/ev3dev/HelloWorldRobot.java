package ev3dev;
import lejos.hardware.Button; //   pour le moteur

public class HelloWorldRobot {

    public static void main(String [] args) throws InterruptedException {
        System.out.println("\n\n *********   ###### HELLO WORLD !!! ######\n\n");
        Thread.sleep(5000);
        System.out.println("\n\n *********  ###### GOODBYE, WORLD !!! ######\n\n");
        Thread.sleep(5000);
/* pour faire bouger les moteurs, j'ai ajouté le
   module lejos.hardware.Button, ci-aprés le code  */ 
   
   Motor.A.forward();
		Motor.B.forward();
		
		Delay.msDelay(4000);
		Motor.A.stop();
		Motor.B.stop();
   
    }
   
    
}
