#ifndef PARAMETERS_H
#define PARAMETERS_H

// Comment the next line out to use the Arduino
//#define SANGUINO

// Set 1s where you have endstops; 0s where you don't
#define ENDSTOPS_MIN_ENABLED 1
#define ENDSTOPS_MAX_ENABLED 0

//our command string length
#define COMMAND_SIZE 128

// Uncomment the next line to run stand-alone tests on the machine (also see the 
// ends of this, the process_string, the extruder, and the stepper_control tabs).
//#define TEST_MACHINE
// If that works, comment out the next line to test the rest:
#define COMMS_TEST

#define INCHES_TO_MM 25.4

// define the parameters of our machine. Working params for printer.
#define X_STEPS_PER_MM   282.60311
#define X_STEPS_PER_INCH (X_STEPS_PER_MM*INCHES_TO_MM)
#define X_MOTOR_STEPS    400
#define INVERT_X_DIR 0


#define Y_STEPS_PER_MM   282.60311
#define Y_STEPS_PER_INCH (Y_STEPS_PER_MM*INCHES_TO_MM)
#define Y_MOTOR_STEPS    400
#define INVERT_Y_DIR 0

#define Z_STEPS_PER_MM   282.60311
#define Z_STEPS_PER_INCH (Z_STEPS_PER_MM*INCHES_TO_MM)
#define Z_MOTOR_STEPS    400
#define INVERT_Z_DIR 0

// For when we have a stepper-driven extruder
// E_STEPS_PER_MM is the number of steps needed to 
// extrude 1mm out of the nozzle.

#define E_STEPS_PER_MM   0.52/2   // 5mm diameter drive - empirically adjusted
#define E_STEPS_PER_INCH (E_STEPS_PER_MM*INCHES_TO_MM)
#define E_MOTOR_STEPS    400

//our maximum feedrates
#define FAST_XY_FEEDRATE 200.0
#define FAST_Z_FEEDRATE  200.0

// Units in curve section
#define CURVE_SECTION_MM 0.5
#define CURVE_SECTION_INCHES (CURVE_SECTION_MM*INCHES_TO_MM)

// Set to one if enable pins are inverting
// RepRap enable pins are *not* inverting.
#define INVERT_ENABLE_PINS 0

#if INVERT_ENABLE_PINS == 1
#define ENABLE_ON LOW
#else
#define ENABLE_ON HIGH
#endif

// Set to one if sensor outputs inverting (ie: 1 means open, 0 means closed)
// RepRap opto endstops are *not* inverting.
#define ENDSTOPS_INVERTING 1

#define MILL_OFF_ANG 0
#define MILL_ON_ANG 50

#define NUM_REGISTERS 315

#endif
