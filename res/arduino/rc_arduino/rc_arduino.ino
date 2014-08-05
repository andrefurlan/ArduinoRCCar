/*
 PROJECT: ArduDroid 
 PROGRAMMER: Hazim Bitar (techbitar at gmail dot com)
 DATE: Oct 31, 2013
 FILE: ardudroid.ino
 LICENSE: Public domain
*/
#include <SoftwareSerial.h>// import the serial library

#define START_CMD_CHAR '*'
#define END_CMD_CHAR '#'
#define DIV_CMD_CHAR '|'
#define CMD_DIGITALWRITE 10
#define CMD_ANALOGWRITE 11
#define CMD_TEXT 12
#define CMD_READ_ARDUDROID 13
#define MAX_COMMAND 20  // max command number code. used for error checking.
#define MIN_COMMAND 10  // minimum command number code. used for error checking. 
#define IN_STRING_LENGHT 40
#define MAX_ANALOGWRITE 255
#define PIN_HIGH 3
#define PIN_LOW 2

SoftwareSerial serial(10, 11); // RX, TX
int pinNumber;
void setup() {
  serial.begin(9600);
  serial.println("Ok, blue tooth is connected");
  
}

void loop()
{
    if(serial.available()){
      pinNumber = serial.read();
      if((pinNumber > 0)&&(pinNumber < 10)){
        pinMode(pinNumber, OUTPUT);
        if (digitalRead(pinNumber) == LOW){
          digitalWrite(pinNumber, HIGH);
        }else{
          digitalWrite(pinNumber, LOW);
        }
      }
    }
}

