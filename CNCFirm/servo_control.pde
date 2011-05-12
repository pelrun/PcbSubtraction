#include "parameters.h"
#include "pins.h"

void init_servos()
{
	pinMode(MILL_PIN, OUTPUT);
	turn_mill_off();
}
void turn_mill_on(){
  for(int i=0;i<100;i++){
    go_to_angle(MILL_PIN, MILL_ON_ANG);
    delay(20);
  }
}
void turn_mill_off(){
  for(int i=0;i<100;i++){
    go_to_angle(MILL_PIN, MILL_OFF_ANG);
    delay(20);
  }
}
void go_to_angle(int pinNumber, int angle){
  if(angle <= 180 && angle >= 0){
    send_pulse(pinNumber, (600+(angle*9.72)));
  }
}

void send_pulse(int pinNumber, int pulseWidth){
  digitalWrite(pinNumber, HIGH);
  delayMicroseconds(pulseWidth);
  digitalWrite(pinNumber, LOW);
}

