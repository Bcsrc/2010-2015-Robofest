package java101;
/*
 * Copyright 2008 CJC
 * Stop L2Bot if it detects white wall or paper
 */
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import javax.swing.*;
import jmfdemo.FrameGrabber;
import jmfdemo.FrameGrabberException;
import java.util.Timer;
import java.util.TimerTask;

public class BarCodeScanner extends JFrame implements KeyListener {
  private static final long serialVersionUID = 1L;
  private FrameGrabber vision;
  private BufferedImage originalImage, bwImage;
  private BufferedImage redImage, blueImage, greenImage ;
  private BufferedImage greyImage ;
  private BufferedImage gradImage ;
  private BufferedImage edgeImage ;
  protected VideoPanel videoPanel = new VideoPanel();
  private Timer timer = new Timer();
  private int threshold = 180;
  private int edgeThreshold = 60;
  private int eb = 2 ; 				// edge finding border.  Buffer on edge of image for look back/ahead.
  private int greyDelta = 80 ;
  private int greyDeltaMin = 35;
  static final int width = 160; 
  static final int height = 120; 
  private int newColor ;
  private int[][] redRaster ;
  private int[][] greenRaster ;
  private int[][] blueRaster ;
  private int[][] greyRaster ;
  private int[][] bwRaster ;
  private int[][] xGradient ;
  private int[][] yGradient ;
  private int[][] magGradient ;
  private int[][] edgeRaster ;
  private int[] NWcorner = {0, 0} ;
  private int[] NEcorner = {0, 0} ;
  private int[] SWcorner = {0, 0} ;
  private int[] SEcorner = {0, 0} ;
  private int[] lPoint = {0, 0} ;
  private int[] rPoint = {0, 0} ;

  private int[] cell11 = {0, 0} ;
  private int[] cell12 = {0, 0} ;
  private int[] cell13 = {0, 0} ;
  private int[] cell21 = {0, 0} ;
  private int[] cell22 = {0, 0} ;
  private int[] cell23 = {0, 0} ;
  
  private int cell11val ;
  private int cell12val ;
  private int cell13val ;
  private int cell21val ;
  private int cell22val ;
  private int cell23val ;

  private int pageHeightmm = 216 ;
  private int pageRow1mm = 66 ;
  private int pageRow2mm = 146 ;
  private int pageWidthmm = 279 ;
  private int pageCol1mm = 59 ;
  private int pageCol2mm = 139 ;
  private int pageCol3mm = 219 ;

  public BarCodeScanner() {
 	  
	videoPanel.setBackground(Color.white);

    // Place panels in the frame
    setLayout(new BorderLayout()); // needed???
    add(videoPanel, BorderLayout.CENTER);
    setVisible(true);
    addKeyListener(this);
    timer.schedule(   new GetAImageTimerTask(), 
                      1000, // initial delay
                      33 // rate, (int)1000ms/fps
                  );
    // allocate space for gradient arrays
    redRaster = new int[width][height];
    greenRaster = new int[width][height];
    blueRaster = new int[width][height];
    greyRaster = new int[width][height];
    bwRaster = new int[width][height];
    xGradient = new int[width][height];
    yGradient = new int[width][height];
    magGradient = new int[width][height];
    edgeRaster = new int[width][height];

  }
  
  class GetAImageTimerTask extends TimerTask {
    public void run() {  
    	Thread.currentThread().setPriority(Thread.MAX_PRIORITY); // needed??
        videoPanel.showImage();
    }
  }
  
  public void keyPressed (KeyEvent e) {
		switch (e.getKeyCode())
		{ case KeyEvent.VK_DOWN:	
			threshold-=8;
			if (threshold < 0) threshold = 0;
			break;
		  case KeyEvent.VK_UP:
			threshold+=8;
			if (threshold > 255) threshold = 255;
			break;
		  case KeyEvent.VK_LEFT:	
				greyDelta-=1;
				if (greyDelta < 0) greyDelta = 0;
				break;
		  case KeyEvent.VK_RIGHT:
			  greyDelta+=1;
				if (greyDelta > 255) greyDelta = 255;
				break;
		  case KeyEvent.VK_ESCAPE:
			System.exit(0) ;
			break;
		}
		System.out.println ("Threshold: " + threshold);
		System.out.println ("Grey Delta: " + greyDelta);
}
	
  public void keyReleased (KeyEvent e) {}
  public void keyTyped (KeyEvent e) {}
  
  class VideoPanel extends JPanel {
	  private static final long serialVersionUID = 1L;
	  int i; 
	 
	  public VideoPanel() {
	    try {
			vision = new FrameGrabber();
			vision.start();
		} catch (FrameGrabberException fge) {
			System.out.println(fge.getMessage());
		}
		bwImage = new BufferedImage(160, 120, BufferedImage.TYPE_INT_ARGB);
		redImage = new BufferedImage(160, 120, BufferedImage.TYPE_INT_ARGB);
		greenImage = new BufferedImage(160, 120, BufferedImage.TYPE_INT_ARGB);
		blueImage = new BufferedImage(160, 120, BufferedImage.TYPE_INT_ARGB);
		greyImage = new BufferedImage(160, 120, BufferedImage.TYPE_INT_ARGB);
		gradImage = new BufferedImage(160, 120, BufferedImage.TYPE_INT_ARGB);
		edgeImage = new BufferedImage(160, 120, BufferedImage.TYPE_INT_ARGB);
	  }

	  protected void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    
	    // First Row
	    if (originalImage != null)
	       g.drawImage(originalImage, 10, 10, width, height, null);
	    if (bwImage != null)
		   g.drawImage(bwImage, 180, 10, width, height, null);
	    if (greyImage != null)
	    	g.drawImage(greyImage, 350, 10, width, height, null);
	    
	    // Second Row
	    if (redImage != null)
	    	g.drawImage(redImage, 10, 175, width, height, null);
	    if (greenImage != null)
	    	g.drawImage(greenImage, 180, 175, width, height, null);
	    if (blueImage != null)
	    	g.drawImage(blueImage, 350, 175, width, height, null);
	    
	    // Third Row
	    if (gradImage != null)
	    	g.drawImage(gradImage, 10, 340, width, height, null);
	    // Third Row
	    if (edgeImage != null)
	    	g.drawImage(edgeImage, 180, 340, width, height, null);
	    
		// edges around the rectangle
	    g.setColor(Color.red);
		g.drawLine(180+NWcorner[0], 340+NWcorner[1], 180+NEcorner[0], 340+NEcorner[1]) ;
		g.drawLine(180+NWcorner[0], 340+NWcorner[1], 180+SWcorner[0], 340+SWcorner[1]) ;
		g.drawLine(180+SWcorner[0], 340+SWcorner[1], 180+SEcorner[0], 340+SEcorner[1]) ;
		g.drawLine(180+SEcorner[0], 340+SEcorner[1], 180+NEcorner[0], 340+NEcorner[1]) ;

		// Draw dots at the corners
	    g.setColor(Color.blue);
		g.drawOval(180+NWcorner[0]-3, 340+NWcorner[1]-3, 6, 6) ;
		g.drawOval(180+NEcorner[0]-3, 340+NEcorner[1]-3, 6, 6) ;
		g.drawOval(180+SWcorner[0]-3, 340+SWcorner[1]-3, 6, 6) ;
		g.drawOval(180+SEcorner[0]-3, 340+SEcorner[1]-3, 6, 6) ;

		// Draw dots in the middle of where we believe the cells are
		g.drawOval(180+cell11[0]-3, 340+cell11[1]-3, 6, 6) ;
		g.drawOval(180+cell12[0]-3, 340+cell12[1]-3, 6, 6) ;
		g.drawOval(180+cell13[0]-3, 340+cell13[1]-3, 6, 6) ;
		g.drawOval(180+cell21[0]-3, 340+cell21[1]-3, 6, 6) ;
		g.drawOval(180+cell22[0]-3, 340+cell22[1]-3, 6, 6) ;
		g.drawOval(180+cell23[0]-3, 340+cell23[1]-3, 6, 6) ;

	  }

	  public void showImage() { // run for each frame
		  originalImage = vision.getBufferedImage();
		  cnvrt2bw(originalImage);
		  repaint();
	  }
	  /** create the Black and Wite Image. This is not complete */
	  private void cnvrt2bw(BufferedImage x) {	
		  int RGBval ;
		  int Rval ;
		  int Gval ;
		  int Bval ;
		  int greyVal ;
		  int i, j; 
		  int wi, hi ;
		  int bit11, bit12, bit13 ;
		  int bit21, bit22, bit23 ;
		  
		  
		   // Create discrete rasters from the sensed image
		  for( wi = 0; wi < width; wi++) {
			for( hi = 0; hi < height; hi++) {
				RGBval = x.getRGB(wi,hi) ;
				Rval = (RGBval & 0xff0000) >> 16 ;
				Gval = (RGBval & 0xff00)   >> 8 ;
				Bval = (RGBval & 0xff)     >> 0 ;
				greyVal = ( Rval + Gval + Bval ) / 3 ;

				if(	Rval > threshold && 
					Gval > threshold && 
					Bval > threshold  ) 
				{
					bwRaster[wi][hi] = 0xff;
				} else {
					bwRaster[wi][hi] = 0;
				}
				redRaster[wi][hi]   = Rval ;
				greenRaster[wi][hi] = Gval ;
				blueRaster[wi][hi]  = Bval ;
				greyRaster[wi][hi]  = greyVal ;
			}// end for
		  } // end for

		  // Compute gradients for edge detection.
		  for( wi = 0; wi < width; wi++) {
			for( hi = 0; hi < height; hi++) {
				if (wi == 0 || hi == 0 || wi == width-1 || hi == height-1)
				{	xGradient[wi][hi] = 0 ;
					yGradient[wi][hi] = 0 ;
				}
				else 
				{	xGradient[wi][hi] = ( greyRaster[wi+1][hi] - greyRaster[wi-1][hi] ) / 2 ;
					yGradient[wi][hi] = ( greyRaster[wi][hi+1] - greyRaster[wi][hi-1] ) / 2 ;
					magGradient[wi][hi] = Math.max( Math.abs(xGradient[wi][hi]), Math.abs(yGradient[wi][hi])) ;
					if (magGradient[wi][hi] > 0xff) magGradient[wi][hi] = 0xff ;
				}
				if (magGradient[wi][hi] > edgeThreshold) {
					edgeRaster[wi][hi] = magGradient[wi][hi] ;
				}
				else {
					edgeRaster[wi][hi] = 0 ;
				}
			} // end for
		  } // end for
		  
		  // Attempt to find the corners of the paper.
		  // This assumes that the white paper is against a dark background.
		  
		  // NW Corner
		  // Start at upper left corner of the image.  Scan with a diagonal line from NW to SE.
		  outer:
		  for ( i = eb ; i < width + height - 3* eb - 1 ; i++ ){
			  
			  if (i < height-eb) {
				  wi = eb ;
				  hi = i ;
			  }
			  else {
				  wi = i - height + 2*eb + 1 ;
				  hi = height - eb - 1 ;
			  }
			  while ( wi < width-eb && hi >= eb ) {
				  if ( edgeRaster[wi][hi] > edgeThreshold ) {
					  int lb=0, la=0 ; // look back/ahead sums.
					  for (j=1 ; j<=eb ; j++){
						  lb += greyRaster[wi-j][hi-j] ;
						  la += greyRaster[wi+1][hi+1] ;
					  }
					  if (lb < la) {
						  // corner found!
						  NWcorner[0] = wi ;
						  NWcorner[1] = hi ;
						  System.out.println("NW corner found at (" + wi + ", " + hi + ")")	;
						  break outer ;
					  }
				  }
				  wi++; hi--  ;
			  } // while
		  }

		  // SE Corner
		  // Start at lower right corner of the image.  Scan with a diagonal line from NW to SE.
		  outer:
		  for ( i = width + height - 3*eb - 1 ; i >= eb ; i-- ){
			  
			  if (i < height-eb) {
				  wi = eb ;
				  hi = i ;
			  }
			  else {
				  wi = i - height + 2*eb + 1 ;
				  hi = height - eb - 1 ;
			  }
			  while ( wi < width-eb && hi >= eb ) {
				  if ( edgeRaster[wi][hi] > edgeThreshold ) {
					  int lb=0, la=0 ; // look back/ahead sums.
					  for (j=1 ; j<=eb ; j++){
						  lb += greyRaster[wi+j][hi+j] ;
						  la += greyRaster[wi-1][hi-1] ;
					  }
					  if (lb < la) {
						  // corner found!
						  SEcorner[0] = wi ;
						  SEcorner[1] = hi ;
						  System.out.println("SE corner found at (" + wi + ", " + hi + ")")	;
						  break outer ;
					  }
				  }
				  wi++; hi--  ;
			  } // while
		  }

		  //NE corner
		  // Start at upper right corner of the image.  Scan with a diagonal line from NE to SW.
		  outer:
		  for ( i = eb ; i < width + height - 3* eb - 1 ; i++ ){
			  
			  if (i < height-eb) {
				  wi = width - eb - 1 ; ;
				  hi = i ;
			  }
			  else {
				  wi = height + width - i - 2*eb - 2 ;
				  hi = height - eb - 1 ;
			  }
			  while ( wi >= eb && hi >= eb ) {
				  if ( edgeRaster[wi][hi] > edgeThreshold ) {
					  int lb=0, la=0 ; // look back/ahead sums.
					  for (j=1 ; j<=eb ; j++){
						  lb += greyRaster[wi+j][hi-j] ;
						  la += greyRaster[wi-1][hi+1] ;
					  }
					  if (lb < la) {
						  // corner found!
						  NEcorner[0] = wi ;
						  NEcorner[1] = hi ;
						  System.out.println("NE corner found at (" + wi + ", " + hi + ")")	;
						  break outer ;
					  }
				  }
				  wi--; hi--  ;
			  } // while
		  }

		  //SW corner
		  // Start at lower left corner of the image.  Scan with a diagonal line from SW to NE.
		  outer:
		  for ( i = width + height - 3*eb - 1 ; i >= eb ; i-- ){
			  
			  if (i < height-eb) {
				  wi = width - eb - 1 ; ;
				  hi = i ;
			  }
			  else {
				  wi = height + width - i - 2*eb - 2 ;
				  hi = height - eb - 1 ;
			  }
			  while ( wi >= eb && hi >= eb ) {
				  if ( edgeRaster[wi][hi] > edgeThreshold ) {
					  int lb=0, la=0 ; // look back/ahead sums.
					  for (j=1 ; j<=eb ; j++){
						  lb += greyRaster[wi-j][hi+j] ;
						  la += greyRaster[wi+1][hi-1] ;
					  }
					  if (lb < la) {
						  // corner found!
						  SWcorner[0] = wi ;
						  SWcorner[1] = hi ;
						  System.out.println("SW corner found at (" + wi + ", " + hi + ")")	;
						  break outer ;
					  }
				  }
				  wi--; hi--  ;
			  } // while
		  }

		  // Generate images.
		  for( wi = 0; wi < width; wi++) {
				for( hi = 0; hi < height; hi++) {
					
				if ( bwRaster[wi][hi] == 255 ) newColor = 0xffffffff ;
				else newColor = 0xff000000 ;
				bwImage.setRGB(wi,hi, newColor) ;

				newColor = 	( ( 0xff ^ magGradient[wi][hi] ) << 16) | 
							( ( 0xff ^ magGradient[wi][hi] ) << 8)  | 
							( ( 0xff ^ magGradient[wi][hi] ) << 0)  | 
							0xff000000  ;
				gradImage.setRGB(wi,hi, newColor) ;
				
				newColor = 	( ( 0xff ^ edgeRaster[wi][hi] ) << 16) | 
							( ( 0xff ^ edgeRaster[wi][hi] ) << 8)  | 
							( ( 0xff ^ edgeRaster[wi][hi] ) << 0)  | 
							0xff000000  ;
				edgeImage.setRGB(wi,hi, newColor) ;

				newColor = 	(greyRaster[wi][hi] << 16) | 
							(greyRaster[wi][hi] << 8)  | 
							(greyRaster[wi][hi] << 0)  | 
							0xff000000  ;
				greyImage.setRGB(wi,hi, newColor) ;

				newColor = (redRaster[wi][hi] << 16) | 0xff000000  ;
				redImage.setRGB(wi,hi, newColor) ;

				newColor = (greenRaster[wi][hi] << 8) | 0xff000000  ;
				greenImage.setRGB(wi,hi, newColor) ;
				
				newColor = (blueRaster[wi][hi] << 0) | 0xff000000  ;
				blueImage.setRGB(wi,hi, newColor) ;			
			} // end for
		  } // end for
		  
		  // Read the bar code
		  
		  // Top Row
		  // Left interpolation point
		  lPoint[0] = NWcorner[0] + (SWcorner[0] - NWcorner[0])*pageRow1mm / pageHeightmm ;
		  lPoint[1] = NWcorner[1] + (SWcorner[1] - NWcorner[1])*pageRow1mm / pageHeightmm ;
		  
		  // Right interpolation point
		  rPoint[0] = NEcorner[0] + (SEcorner[0] - NEcorner[0])*pageRow1mm / pageHeightmm ;
		  rPoint[1] = NEcorner[1] + (SEcorner[1] - NEcorner[1])*pageRow1mm / pageHeightmm ;
		  
		  //Cell (1,1)
		  cell11[0] = lPoint[0] + (rPoint[0] - lPoint[0])*pageCol1mm / pageWidthmm ;
		  cell11[1] = lPoint[1] + (rPoint[1] - lPoint[1])*pageCol1mm / pageWidthmm ;
		  
		  //Cell (1,2)
		  cell12[0] = lPoint[0] + (rPoint[0] - lPoint[0])*pageCol2mm / pageWidthmm ;
		  cell12[1] = lPoint[1] + (rPoint[1] - lPoint[1])*pageCol2mm / pageWidthmm ;

		  //Cell (1,3)
		  cell13[0] = lPoint[0] + (rPoint[0] - lPoint[0])*pageCol3mm / pageWidthmm ;
		  cell13[1] = lPoint[1] + (rPoint[1] - lPoint[1])*pageCol3mm / pageWidthmm ;

		  // Bottom Row
		  // Left interpolation point
		  lPoint[0] = NWcorner[0] + (SWcorner[0] - NWcorner[0])*pageRow2mm / pageHeightmm ;
		  lPoint[1] = NWcorner[1] + (SWcorner[1] - NWcorner[1])*pageRow2mm / pageHeightmm ;
		  
		  // Right interpolation point
		  rPoint[0] = NEcorner[0] + (SEcorner[0] - NEcorner[0])*pageRow2mm / pageHeightmm ;
		  rPoint[1] = NEcorner[1] + (SEcorner[1] - NEcorner[1])*pageRow2mm / pageHeightmm ;
		  
		  //Cell (2,1)
		  cell21[0] = lPoint[0] + (rPoint[0] - lPoint[0])*pageCol1mm / pageWidthmm ;
		  cell21[1] = lPoint[1] + (rPoint[1] - lPoint[1])*pageCol1mm / pageWidthmm ;
		  
		  //Cell (2,2)
		  cell22[0] = lPoint[0] + (rPoint[0] - lPoint[0])*pageCol2mm / pageWidthmm ;
		  cell22[1] = lPoint[1] + (rPoint[1] - lPoint[1])*pageCol2mm / pageWidthmm ;

		  //Cell (2,3)
		  cell23[0] = lPoint[0] + (rPoint[0] - lPoint[0])*pageCol3mm / pageWidthmm ;
		  cell23[1] = lPoint[1] + (rPoint[1] - lPoint[1])*pageCol3mm / pageWidthmm ;
		  
		  cell11val = greyRaster[ cell11[0] ][ cell11[1] ] ;
		  cell12val = greyRaster[ cell12[0] ][ cell12[1] ] ;
		  cell13val = greyRaster[ cell13[0] ][ cell13[1] ] ;
		  cell21val = greyRaster[ cell21[0] ][ cell21[1] ] ;
		  cell22val = greyRaster[ cell22[0] ][ cell22[1] ] ;
		  cell23val = greyRaster[ cell23[0] ][ cell23[1] ] ;

		  System.out.println(cell11val + ", " + cell12val + ", " + cell13val) ;
		  System.out.println(cell21val + ", " + cell22val + ", " + cell23val) ;
		  
		  int max = 0 ;
		  int min = 0xff ;
		  if (cell11val > max) max = cell11val ;
		  if (cell12val > max) max = cell12val ;
		  if (cell13val > max) max = cell13val ;
		  if (cell21val > max) max = cell21val ;
		  if (cell22val > max) max = cell22val ;
		  if (cell23val > max) max = cell23val ;

		  if (cell11val < min) min = cell11val ;
		  if (cell12val < min) min = cell12val ;
		  if (cell13val < min) min = cell13val ;
		  if (cell21val < min) min = cell21val ;
		  if (cell22val < min) min = cell22val ;
		  if (cell23val < min) min = cell23val ;
		  
		  int cellThreshold = (max + min) / 2 ;
		  threshold = cellThreshold ;

		  bit11 = cell11val < cellThreshold ? 1 : 0 ;
		  bit12 = cell12val < cellThreshold ? 1 : 0 ;
		  bit13 = cell13val < cellThreshold ? 1 : 0 ;
		  bit21 = cell21val < cellThreshold ? 1 : 0 ;
		  bit22 = cell22val < cellThreshold ? 1 : 0 ;
		  bit23 = cell23val < cellThreshold ? 1 : 0 ;
		  
		  System.out.printf("%d%d%d\n", bit11, bit12, bit13) ;
		  System.out.printf("%d%d%d\n", bit21, bit22, bit23) ;
		  
	  }
  }
  
  public static void main(String[] args) {
	  BarCodeScanner frame = new BarCodeScanner();
	    frame.setTitle("Color -> Black and White Webcam");
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setSize(360 + 180, 175 + 175 + 175) ;
	    frame.setVisible(true);
  } 
}
