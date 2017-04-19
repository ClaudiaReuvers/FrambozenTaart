# Nedap Module 2 "Network Systems"

## Installation
Connect your raspberryPi to a power source to make sure it doesn't shut down. Set your networksettings such that you
are connected to the NedapUniversity network. Connect to the Raspberry Pi by typing `ssh pi@<Pi_IPaddress>`, next typ
in the super secret password.

Download the num2 directory from the repository 'FrambozenTaart'. In this folder a build.gradle file is present. If you
want to put the files on a Raspberry Pi with a specified IPaddress, change the host of line 51 to the IPaddress of your
Raspberry Pi. Build a jar file by typing the commando `./gradlew build` in the commandline. If building is successfull,
typ `./gradlew deploy` to put the created .jar-file on your raspberry Pi.

## Starting the Raspberry Pi
To start the Pi, typ `/usr/bin/java -jar /home/pi/NUM2.jar pi`. This will start the Main-class with the argument 'pi',
thereby creating and starting a Pi-class which listens to incomming DNSrequests on port 9876. If an connection is made,
a new Client will be set up at another to communicate with the Client.

## Starting a Client
Start the Main-class with as argument client, this will start a Client to communicate with the Pi. After de IPaddress
of the Pi retrieved, it will automatically perform a handshake with the Pi and afterwards you can chose whether you
want to up- or download a file to/from the file.

## Tests
Provided tests will automatically be performed while building the file using the `./gradlew build` command.

## Bug
After a file is up- or downloaded, both the client and Raspberry Pi will shutdown, and have to be set up again.
