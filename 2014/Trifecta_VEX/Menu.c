
task main()
{
	bLCDBacklight=true;
	displayLCDString(1, 0, " B    B-M     M");
	int button;
	int tme1 = 12;
	string S;
//	while((button=nLCDButtons)==0){}
//	clearLCDLine (1);
	//if (button==1) {
	//	displayLCDString(1, 0, "Stackhouse");}
		//else if (button==2) {
			//displayLCDString(1, 0, "Rodman");}
	//	else if (button==4) {
		//	displayLCDString(1, 0, "Aliens");}
		//wait10Msec(1000);

	displayLCDNumber(1, 5, tme1);
	while(true)
	{
	while((button=nLCDButtons)==0){}


		sprintf(S, "%d",tme1);
		displayLCDString(1, 0, " -     GO     +");
		clearLCDLine (0);
		if (button==2) {
			displayLCDString(0, 0, "Go");

		}
		else if (button==1) {
			displayLCDString(0, 0, S);
			tme1--;

			//sprintf(S, "%d",tme1);
			//displayLCDCenteredString(0, S);
		}
		else if (button==4) {
			displayLCDString(0, 0, S);

		tme1++;
		}
		else if (tme1 > 14) {
			tme1= 14;
		}
		//sprintf(S, "%d",tme1);
		//displayLCDCenteredString(0, S);
		wait1Msec(500);
	}

	wait1Msec(1000);
  displayLCDCenteredString(0, S);
}
