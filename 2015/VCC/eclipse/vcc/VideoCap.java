import java.awt.image.BufferedImage;
import org.opencv.core.Core;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import static org.opencv.highgui.VideoCapture.*;
import static org.opencv.highgui.Highgui.* ;

public class VideoCap {
    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    VideoCapture cap;
    Mat2Image mat2Img = new Mat2Image();

    VideoCap(){
        cap = new VideoCapture();
        cap.open(0);
    } 
 
    BufferedImage getOneFrame() {
        cap.read(mat2Img.mat);
        Imgproc.cvtColor(mat2Img.mat, mat2Img.mat, Imgproc.COLOR_BGR2RGB);
        return mat2Img.getImage(mat2Img.mat);
    }
}