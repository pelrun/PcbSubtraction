#ifndef PINS_H
#define PINS_H

#ifdef SANGUINO
/****************************************************************************************
* Sanguino pin assignment
*
****************************************************************************************/

//cartesian bot pins
#define X_STEP_PIN (byte)15
#define X_DIR_PIN (byte)18
#define X_MIN_PIN (byte)20
#define X_MAX_PIN (byte)21
#define X_ENABLE_PIN (byte)19

#define Y_STEP_PIN (byte)23
#define Y_DIR_PIN (byte)22
#define Y_MIN_PIN (byte)25
#define Y_MAX_PIN (byte)26
#define Y_ENABLE_PIN (byte)19

#define Z_STEP_PIN (byte)29
#define Z_DIR_PIN (byte)30
#define Z_MIN_PIN (byte)1
#define Z_MAX_PIN (byte)2
#define Z_ENABLE_PIN (byte)31

//extruder pins
#define EXTRUDER_0_MOTOR_SPEED_PIN   (byte)12
#define EXTRUDER_0_MOTOR_DIR_PIN     (byte)16
#define EXTRUDER_0_HEATER_PIN        (byte)14
#define EXTRUDER_0_FAN_PIN           (byte)3
#define EXTRUDER_0_TEMPERATURE_PIN  (byte)4    // Analogue input
#define EXTRUDER_0_VALVE_DIR_PIN     (byte)17
#define EXTRUDER_0_VALVE_ENABLE_PIN  (byte)13  // Valve needs to be redesigned not to need this
#define EXTRUDER_0_STEP_ENABLE_PIN  (byte)-1  // 3 - Conflicts with the fan; set -ve if no stepper

#define EXTRUDER_1_MOTOR_SPEED_PIN   (byte)4
#define EXTRUDER_1_MOTOR_DIR_PIN    (byte)0
#define EXTRUDER_1_HEATER_PIN        (byte)24
#define EXTRUDER_1_FAN_PIN           (byte)7
#define EXTRUDER_1_TEMPERATURE_PIN  (byte)3  // Analogue input
#define EXTRUDER_1_VALVE_DIR_PIN    (byte) 6
#define EXTRUDER_1_VALVE_ENABLE_PIN (byte)5   // Valve needs to be redesigned not to need this 
#define EXTRUDER_1_STEP_ENABLE_PIN  (byte)-1  // 7 - Conflicts with the fan; set -ve if no stepper

#else
/****************************************************************************************
* Arduino pin assignment
*
****************************************************************************************/

#define X_STEP_PIN (byte)6
#define X_DIR_PIN (byte)7
#define X_MIN_PIN (byte)8
#define X_MAX_PIN (byte)9

#define Y_STEP_PIN (byte)10
#define Y_DIR_PIN (byte)11
#define Y_MIN_PIN (byte)12
#define Y_MAX_PIN (byte)13

#define Z_STEP_PIN (byte)2
#define Z_DIR_PIN (byte)3
#define Z_MIN_PIN (byte)4
#define Z_MAX_PIN (byte)5

//mill pin
#define MILL_PIN (byte)19 //a5


//extruder pins
#define EXTRUDER_0_MOTOR_SPEED_PIN  (byte)-1
#define EXTRUDER_0_MOTOR_DIR_PIN    (byte)-1
#define EXTRUDER_0_HEATER_PIN       (byte)-1
#define EXTRUDER_0_FAN_PIN          (byte)-1
#define EXTRUDER_0_TEMPERATURE_PIN  (byte)0  // Analogue input
#define EXTRUDER_0_VALVE_DIR_PIN             (byte)-1       //NB: Conflicts with Max Z!!!!
#define EXTRUDER_0_VALVE_ENABLE_PIN          (byte)-1 
#define EXTRUDER_0_STEP_ENABLE_PIN  -1 // 5 - NB conflicts with the fan; set -ve if no stepper

#endif
#endif
