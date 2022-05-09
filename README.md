# Elevator Control System project
This project simulates the event calls for an ordinary elevator using UDP message passing and Java's built in asynchronous class features

# Project Structure

### Elevator Subsystem

* `ButtonLight.java`: This class represents a button light for a single floor. The light is located inside of an elevator. The light is turned on when the passenger presses the button in the elevator and is turned off when the destination floor is reached.


* `ElevatorHelper.java`: This class has operations that pertain to the Elevators that are in the Elevator Subsystem. In here we have fields for speed, distance, time, acceleration and etc. An operation that helps calculate travel time to a specific floor is included.


* `ElevatorState.java`: This class declares states of our elevator as enums and changes between states consistently.


* `ElevatorStatusObj.java`: This class is an implementation of how the Scheduler and Elevator will communicate with each other using data structures.


* `ElevatorSubsystem.java`: This class is an implementation of the functionality of the Elevator Subsystem. In this class, events from the scheduler are handled by run().


* `ElevatorListener.java`: This interface provides a way for views to connect to a controller linked to Elevator Subsystem models.


* `ElevatorStatusObjController.java`: This class provides a way for views to be linked to a Elevator Subsystem model.


* `ElevatorFrame.java`: This class contains a GUI implementation for all Elevator Subsystems.

### Floor Subsystem

* `EventObj.java`: This class provides an implementation of an object meant for storing the details of Elevator system-related events. It also contains getter and setter methods for local variables, methods to create a datagram packet using the information in the object, a method for populating an event object using a datagram packet and a method for comparing event objects.


* `Floor.java`: This class provides an implementation for the Floors in the FloorSubsystem and declares methods to get information such as floor info(ID) and light info(on/off).


* `FloorSubsystem.java`: This class provides an implementation for the Floor subsystem. The floor subsystem reads events from a file and pushes them (one at a time) to the Scheduler.


* `FloorListener.java`: This interface provides a way for views to connect to a controller linked to a Floor Subsystem model.


* `FloorController.java`: This class provides a way for views to be linked to a Floor Subsystem model.


* `FloorFrame.java`: This class contains a GUI implementation for the Floor Subsystem.

### Scheduler

* `Scheduler.java`: This class is an implementation of the scheduler. The scheduler creates two threads for communicating with the Floors and the Elevators each directly. It is able to store Passenger’s choice of elevator destination floor events through buffers.


* `SchedulerListener.java`: This interface provides a way for views to connect to a controller linked to a scheduler model.


* `SchedulerController.java`: This class provides a way for views to be linked to a Scheduler model.


* `SchedulerFrame.java`: This class contains a GUI implementation for the Scheduler.

### Exceptions

* `ElevatorStateProgressionError.java`: Checking exceptions for incorrect state progression.


* `ElevatorStatusObjConversionException.java`: Checking exceptions for incorrect ElevatorStatusObj conversion.


* `ElevatorStatusObjInitializationException.java`: Checking exceptions for incorrect ElevatorStatusObj initialization.


* `EventObjConversionException.java`: Checking exceptions for incorrect EventObj conversion.


* `FloorInitializationException.java`: Checking exceptions for FloorInitializationException.


* `FloorLightNotAccessibleException.java`: Checking exceptions for incorrect accessing of a light on a floor.


* `SchedulerStateProgressionException.java`: Checking exceptions for incorrect state progression.

### Tests

* `ElevatorHelperTest.java`: Tests for the ElevatorHelper Class. Tests for the time it takes for the elevator to travel and stop on floors.


* `ElevatorStatusObjTest.java`: Tests for the ElevatorStatusObj Class, in particular it tests for the ElevatorSchedulerMessage.


* `ElevatorSubsystemTest.java`: Tests for the ElevatorSubsystem Class, in particular for the get_current_floor to return the correct floor.


* `EventObjTest.java`: Tests for the EventObj Class, in particular for the EventObj functions to be correct when performed asynchronously.


* `FloorSubsystemTest.java`: Tests for the FloorSubsystem Class, in particular for testing each input file and generate an eventQueue.


* `SchedulerTest.java`: Tests for the Scheduler Class, in particular for different cases the Scheduler could see, such as if both elevators are idle, or if one is going up, and one is going down, and if both are going up or going down.


* `IntegrationTest.java`: General tests for the integration of all three subsystems.

### Util

* `PacketBuilder.java`: This class is designed for the incremental building of packets.


* `ThreadConsoleHelper.java`: This class assists with printing information in console including providing methods for the printing of threads.

### Constants

* `AnsiConstants.java`: Constants used for colouring console outputs.


* `BuildingConfigConstants.java`: Constants read from building.config, contains the values required to initialize the system including number of floors and number of floors.


* `ElevatorConfigConstants.java`: Constants read from elevator.config, contains how long it takes for elevator to perform actions.


* `SystemConfigConstants.java`: Constants read from system.config, contains the constants for the entire system including networking IPs and ports for UDP packets and sockets.

# Set-up Instructions

Follow these instructions for setup in Eclipse:

1. Extract source code zip file (<FILE NAME HERE>.zip)
2. Open Eclipse
3. Import the project into Eclipse:
  
File->Import->Existing projects into workspace->Select archive file->(Path to <FILE HERE>, source code)

4. Install ANSI Escape for Eclipse (Help -> Eclipse Market Place -> Search for "ANSI Escape" -> Install plug-in). This is required for correctly formated output.
5. Run Scheduler.java, ElevatorSubsystem.java and FloorSubsystem.java as Java Applications in that order.

The code is run as three separate programs that communicate via the User Datagram Protocol (UDP), and the code can be configured to run on separate platforms.

To do this, you must alter the IP addresses and ports given in the `system.config` file.

# Running the code

As previously stated, the code consists of three separate programs that run concurrently.

To execute the code properly, you must:

* Execute the main function in `SchedulerSubsystem.java`

* Execute the main function in `ElevatorSubsystem.java`

* Execute the main function in `FloorSubsystem.java`

# Testing

All tests are included in the source distributable. They can be found within the `src/tests` folder.

To run the tests, ensure that `JUnit4` is being used, and simply execute each individual testing file.

Make sure that for the tests that involve sockets, such as `IntegrationTest.java`, `FloorSubsystemTest.java`, and 
`ElevatorSubsystemTest.java` that each individual test is run one at a time, or else conflicts will arise from
the socket interfaces.

Also note that for some tests to run properly the building configuration (`building.config`) file must have
the number of elevators set to 4, and the number of floors set to 22.

The test files are listed below.

* `FloorSubsystemTest.java` -> tests the Floor subsystem's ability to read data.
  

* `ElevatorSubsystemTest.java` -> tests the Elevator subsystem's ability to switch floors.
  

* `ElevatorHelperTest.java` -> tests the calculations for Elevator timings.
  

* `SchedulerTest.java` -> tests the Scheduler's ability to schedule events properly.
  

* `ElevatorStatusObjTest.java` -> tests the functionality behind the ElevatorSchedulerMessage.
  

* `EventObjTest.java` -> test converting a datagram packet to a EventObj.
  

* `IntegrationTest.java` -> tests the integration of all three subsystems.

# Additional Information

A demo of the system running with explanations from the team can be found in the attached video file.

All diagrams (class diagram, timing diagrams, state machine diagrams and sequence diagrams) can be found in the folder docs/diagrams/final/.
