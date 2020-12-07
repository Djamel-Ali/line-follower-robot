# A Line Follower Robot (using ev3dev-lang-java) :
___
## PART I : Hello World

This repository contains an example ready to be used with
The ev3dev-lang-java libraries (this example displays 'Hello World').

## Prerequisites

The Prerequisites to use this project are:

- Your MINDSTORMS Brick needs to have installed latest `Debian Stretch` version. https://www.ev3dev.org/docs/getting-started/
- To increase the EV3 CPU Speed, read the following article: https://lechnology.com/2018/06/overclocking-lego-mindstorms-ev3-part-2/
- Your MINDSTORMS Brick needs to be connected to the same LAN than your laptop. http://www.ev3dev.org/docs/getting-started/#step-5-set-up-a-network-connection

Note: Update the EV3Dev kernel
https://www.ev3dev.org/docs/tutorials/upgrading-ev3dev/

```
sudo apt-get update
sudo apt-get install linux-image-ev3dev-ev3
```

Once you have all steps done, continue with the next section.

## Getting Started
### Checking it out

Before proceeding further, you need to change the brick connection parameters
in the `config.gradle` file in the main directory.

You can build the project and upload all programs with their dependencies (In our case we have only one program) with this commands:
```sh
./gradlew deploy
```

You can then run them/it from the ev3dev menu in the `examples` subdirectory
(by following these steps for example) :
```sh
ssh robot@ev3dev.local
cd examples/
./ev3dev-lang-java-0.6.1.sh 
```

Alternatively, you can also work only with one project at a time
(in case there are several, and you don't want to deploy all of them each time):
```sh
./gradlew :ev3dev-lang-java:deploy    # only upload 'ev3dev-lang-java' project
./gradlew :ev3dev-lang-java:run       # only run already uploaded build of 'ev3dev-lang-java' project
./gradlew :ev3dev-lang-java:deployRun # only upload and run 'ev3dev-lang-java' project
```

Or by using the EV3 brick (Using its buttons, we go to: **File Browser / ev3dev-lang-java-0.6.1.sh** then we click on the central button (the Enter button) to execute it).

To change the class to be run in some example project, modify its `config.gradle` file.
If you want to run the program from ev3dev menu, you will have to re-upload it.

To remove all samples from the brick, just run:
```sh
./gradlew undeploy
```

## Javadocs

The project has the following technical documentation

http://ev3dev-lang-java.github.io/docs/api/latest/index.html


___
##### By : Djamel ALI & Hamza IDRISSOU


