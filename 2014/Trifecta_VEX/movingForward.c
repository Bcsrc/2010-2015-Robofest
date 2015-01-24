#pragma config(Sensor, in3,		 lightSensor,					sensorReflection)
#pragma config(Motor,	 port2,						rightMotor,		 tmotorNormal, openLoop, reversed)
#pragma config(Motor,	 port3,						leftMotor,		 tmotorNormal, openLoop)
#define R 112 //mm


task main()
{
	clearLCDLine(0);
	while(nLCDButtons == 0) {}
	wait1Msec(2000);

	while(SensorValue[in3] < 2000)
	{
		clearLCDLine(0);
		displayLCDNumber(0, 0, SensorValue[in3]) ;
		motor[leftMotor]= -25;
		motor[rightMotor]= -25;
	}
	motor[leftMotor]= 0;
	motor[rightMotor]= 0;
}
