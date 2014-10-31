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

public class BarCodeReader_woodham extends JFrame implements KeyListener {
  private static final long serialVersionUID = 1L;
  private FrameGrabber vision;
  private BufferedImage image1, image2;
  protected VideoPanel videoPanel = new VideoPanel();
  private Timer timer = new Timer();
  private int threshold = 180;
  static final int width = 160; 
  static final int height = 120; 
  
  static final int waitingForBC = 1 ;
  static final int lookingForBCCleared = 2 ;
  static final int lookingForLM = 3 ;
  private int state = waitingForBC ;
  private int numWhitePix = 0;			
  private int A1 = 0;	//added 11-30-2009
  private int A2 = 0;	//added 11-30-2009
  private int A3 = 0;	//added 11-30-2009
  private int B1 = 0;	//added 11-30-2009
  private int B2 = 0;	//added 11-30-2009
  private int B3 = 0;	//added 11-30-2009
  private int landmark_count = 0; 	// added 12-1-2009
  private int direction_code = 0; 	// added 12-1-2009
  				// direction_code = 0 means direction unknown
  				// direction_code = 1 means turn right
  				// direction_code = 2 means turn left
  LoCoMoCo1 mc = null;
  static boolean barCodeFound = false ;
  
  public BarCodeReader_woodham() {
    mc = new LoCoMoCo1( "COM1", 2400 );

    if (mc.port == null) {
          System.out.println("Required COM port unavailable.");
          return;
    }
	  
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
  }
  
  class GetAImageTimerTask extends TimerTask {
    public void run() {  
    	Thread.currentThread().setPriority(Thread.MAX_PRIORITY); // needed??
        videoPanel.showImage();
         // landmark counting section added 12-1-2009
        if(A1 < 1) {
			if (A2 < 1){
				if (A3 > 0) {landmark_count = 1; //ROW A is 001
				}else {landmark_count = 0; //ROW A is 000
				}
			}else {}
		}else {}
		if(A1 < 1) {
			if (A2 > 0){
				if (A3 > 0) {landmark_count = 3; //ROW A is 011
				}else {landmark_count = 2; //ROW A is 010
				}
			}else {}
		}else {}
		if(A1 > 0) {
			if (A2 < 1){
				if (A3 > 0) {landmark_count = 5; //ROW A is 101
				}else {landmark_count = 4; //ROW A is 100
				}
			}else {}
		}else {}
		if(A1 > 0) {
			if (A2 > 0){
				if (A3 > 0) {landmark_count = 7; //ROW A is 111
				}else {landmark_count = 6; //ROW A is 110
				}
			}else {}
		}else {}  // end of landmark counting section added 12-1-2009
		// direction checking section added 12-1-2009
		if(B1 < 1) {
			if (B2 > 0){
				if (B3 > 0) {direction_code = 1; //ROW B is 011 (right)				
				}else {}
			}else {}
		}else {}
		if(B1 > 0) {
			if (B2 > 0){
				if (B3 < 1) {direction_code = 2; //ROW B is 110 (left)
				}else {}
			}else {}
		}else {}  // end of direction checking section added 12-1-2009
		// program output print statements:
        System.out.println (numWhitePix);					//added 11-21-2009
        System.out.println ("ROW A = " + A1 + A2 + A3);		//added 11-30-2009
        System.out.println ("ROW B = " + B1 + B2 + B3);		//added 11-30-2009
        System.out.println ("Number of landmarks:" + landmark_count);		//added 12-1-2009
        System.out.println ("Direction Code:" + direction_code);		//added 12-1-2009
        
		  int rowA = A1*4 + A2*2 + A3 ;
		  int rowB = B1*4 + B2*2 + B3 ;

		  switch (state){
		  case waitingForBC:
			  System.out.println("state = waitingForBC");
			  if (rowA != 0 && ( rowB == 3 || rowB == 6 )  )
			  {
				  System.out.println ("Barcode found " + rowA + ", " + rowB) ;
				  state = lookingForBCCleared ;
			  }
			  mc.stop();
			  break ;
		  case lookingForBCCleared:
			  System.out.println("state = lookingForBCCleared");

			  if (numWhitePix > 15000)
			  {
				  state = lookingForLM ;
			  }
			  mc.forward();

			  break ;
		  case lookingForLM:
			  System.out.println("state = lookingForLM");
			  mc.stop();
			  break ;

		  }

        
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
		  case KeyEvent.VK_ESCAPE:
				state =  waitingForBC ;
				mc.stop();
				break;
		}
		System.out.println ("Threshold: " + threshold);
  }
	
  public void keyReleased (KeyEvent e) {}
  public void keyTyped (KeyEvent e) {}
  
  class VideoPanel extends JPanel {
	  private static final long serialVersionUID = 1L;
	  //private int numWhitePix = 0;
	 
	  public VideoPanel() {
	    try {
			vision = new FrameGrabber();
			vision.start();
		} catch (FrameGrabberException fge) {
			System.out.println(fge.getMessage());
		}
		image2 = new BufferedImage(160, 120, BufferedImage.TYPE_INT_ARGB);
	  }

	  protected void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    if (image1 != null)
	       g.drawImage(image1, 10, 10, width, height, null);
	    if (image2 != null)
		   g.drawImage(image2, 180, 10, width, height, null);
	  }

	  public void showImage() { // run for each frame
		  image1 = vision.getBufferedImage();
		  cnvrt2bw(image1);
		  repaint();
	  }
	  /** create the Black and White Image. This is not complete */
	  private void cnvrt2bw(BufferedImage x) {	
		   // reset variables to zero
		  numWhitePix=0;
		   A1=0;	//added 11-30-2009
		   A2=0;	//added 11-30-2009
		   A3=0;	//added 11-30-2009
		   B1=0;	//added 11-30-2009
		   B2=0;	//added 11-30-2009
		   B3=0;	//added 11-30-2009
		   landmark_count=0; //added 12-1-2009
		   direction_code=0; //added 12-1-2009
		  for(int wi = 0; wi < width; wi++) {
			for(int hi = 0; hi < height; hi++) {
				Color pixel = new Color(x.getRGB(wi,hi));
				if(pixel.getRed() > threshold || pixel.getGreen() > threshold || pixel.getBlue() > threshold) {
					image2.setRGB(wi, hi, Color.white.getRGB());
					numWhitePix++;                                      // added 11/21/2009
				} else {
					image2.setRGB(wi,hi,Color.black.getRGB());
					// the following nested if statements were added 11/30/2009
					// these statements check for black pixels at the center of each barcode region  
					if(wi > 26) {
						if (wi < 28){
							if (hi > 29) {
								if (hi <31){A1=1;			
								}else{}
							}else {}
						}else {}
					}else {}
					if(wi > 79) {
						if (wi < 81){
							if (hi > 29) {
								if (hi <31){A2=1;			
								}else{}
							}else {}
						}else {}
					}else {}
					if(wi > 132) {
						if (wi < 134){
							if (hi > 29) {
								if (hi <31){A3=1;			
								}else{}
							}else {}
						}else {}
					}else {}
					if(wi > 26) {
						if (wi < 28){
							if (hi > 89) {
								if (hi <91){B1=1;			
								}else{}
							}else {}
						}else {}
					}else {}
					if(wi > 79) {
						if (wi < 81){
							if (hi > 89) {
								if (hi <91){B2=1;			
								}else{}
							}else {}
						}else {}
					}else {}
					if(wi > 132) {
						if (wi < 134){
							if (hi > 89) {
								if (hi <91){B3=1;			
								}else{}
							}else {}
						}else {}
					}else {} // end of nested if statements added 11/30/2009
				}
			}
		  } // end for
	  }
  }
  
  public static void main(String[] args) {
	    BarCodeReader_woodham frame = new BarCodeReader_woodham();
	    frame.setTitle("Color -> Black and White Webcam");
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setSize(360, 175);
	    frame.setVisible(true);
  } 
}
