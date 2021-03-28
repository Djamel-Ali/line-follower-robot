package edu.robots.helloworld;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;

public class HelloWorld {
	public void sayHello() {
		LCD.drawString(" Hello World !", 2, 3);
		LCD.drawString("My name is UP7", 2, 5);
        Button.waitForAnyPress();
	}
}
