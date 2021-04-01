# A Line Follower Robot (using leJOS EV3) :
___
  #### The main steps followed for the realization of this project :
## STEP I : Hello World and other basic programs

## STEP II : Run Motors

## STEP III : Straight line follower

## STEP IV : Curved Line follower

## STEP V : PID Line follower

## STEP VI : Adding 'Find the Line' feature

## Prerequisites

Follow the instructions given on the official website (http://lejos.org/ev3.php).

Once you have all steps done, continue with the next section.

## Getting Started

Using the EV3 brick menu, go to:

> **programs -> MainClass.java -> execute program.**

### - Phase 1 : Learning the colors 

(by default 3 measures per color to learn, but you can change this in the program PIDLineFollower.java by modifying the static variables concerned).

As this program is a "border" follower between the line, and the background, the robot needs to learn 3 colors :
* The color of the line.
* The color of the background.
* The color of the border (50% on the line and 50% on the background).

We can also start by making it learn the background color and then the line color, but the border color must always be the last to be learned.

### - Phase 2 : Start following the line.

* Now you just have to place the robot on the border (so that the sensor sees 50% of the line and 50% of the background).

* Click on the '**ENTER**' button in the middle so that the robot starts to follow the line.


* Click on the '**CANCEL**' button in the top left corner to stop the robot.

#### _NOTE :_ 
 _If no one clicks on the 'CANCEL' button the robot continues to follow the line, and if at some point it gets lost in the background, it spirals until it finds the line; and so it never stops by itself (unless it runs out of battery of course ;) )._

___
___

## Documentation

The project has the following technical documentation

http://www.lejos.org/ev3/docs/

### Java documentation

https://docs.oracle.com/javase/7/docs/

https://docs.oracle.com/javase/8/docs/

- (Because the version of the JRE (Java Runtime Environment) installed in the memory card is: Oracle Java SE Embedded version 7 Update 60)).
- The official website to download it:

  Java for LEGO® Mindstorms® EV3 : https://www.oracle.com/java/technologies/javaseemeddedev3-downloads.html.

___
___
--
##### By : Djamel ALI & Hamza IDRISSOU
