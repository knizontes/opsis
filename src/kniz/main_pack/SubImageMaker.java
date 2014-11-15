package kniz.main_pack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

public class SubImageMaker {

	private static final int WIDTH=92;
	private static final int HEIGHT=112;
	private static final double AREAFACTOR=1.0;
	
	private Mat greySrc;
	private Mat greyDst;
	private Context context;
	private String filename;
	
	private CascadeClassifier   mCascade;
	private final String TAG            = "Opsis::Activity";


	public SubImageMaker (String filename, Context context){
		this.context = context;
		this.filename=filename;
		getCascade();
				
	}
	
	/*
	 * returns the face to put on the database and from which build an id-vector
	 */
	public Bitmap getFaceForDatabase(){
		Mat tmpMat= new Mat();
		Mat retMat = new Mat();
		Rect roi = new Rect(new Point(0f, 0f), new Point(WIDTH, HEIGHT));
		Size size = new Size(WIDTH*4, HEIGHT);
		//0 parameter fill the matrix with a greyscale image
		greySrc = Highgui.imread(filename, 0);
		setDstMat();
		Imgproc.resize(greyDst,tmpMat,size);
//		retMat=tmpMat.submat(roi);
		Bitmap retval= Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888) ;
		Utils.matToBitmap(tmpMat, retval);
		return retval;
//		return Bitmap.createBitmap(greyDst.cols(), greyDst.rows(), Bitmap.Config.ARGB_8888) ;
	}
	
	
	/*
	 * returns the face in the original picture
	 */
	public Bitmap getFaceBig(){
		Bitmap retval= Bitmap.createBitmap(greySrc.cols(), greySrc.rows(), Bitmap.Config.ARGB_8888) ;
		if (greySrc!= null)
			Utils.matToBitmap(greySrc, retval);
		return retval;
	}
	
	private void setDstMat(){
		if (mCascade != null) {
            int height = greySrc.rows();
            int faceSize = Math.round(height * 0.2f);
            List<Rect> faces = new LinkedList<Rect>();
            Rect maxRect;
            Rect captureRect;
            int x,y,offset;
            
            int maxRectSize=0;
            mCascade.detectMultiScale(greySrc, faces, 1.1, 2, 2 // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                    , new Size(faceSize, faceSize));
            if (faces.size()>0){
            	maxRectSize=faces.get(0).width * faces.get(0).height;
            	maxRect=faces.get(0);
            }
            else return;
            for (int i=0; i<faces.size();++i){
            	if (maxRectSize<(faces.get(i).width * faces.get(i).height)){
            		maxRectSize=faces.get(i).width * faces.get(i).height;
                	maxRect=faces.get(i);
            	}
            }
            
            offset=maxRect.width/20;
//            if ((maxRect.x-(2*offset))>=0)
//            	x=maxRect.x-(2*offset);
//            else
//            	x=maxRect.x;
//            
//            if ((maxRect.y-(7*offset))>=0)
//            	y=maxRect.y-(7*offset);
//            else
//            	y=maxRect.y;
            
            x=maxRect.x;
            y=maxRect.y-(2*offset);
            	
            captureRect = new Rect(x, y, (int)(maxRect.width*AREAFACTOR), (int)((maxRect.width * HEIGHT*AREAFACTOR)/WIDTH));
            greyDst = greySrc.submat(captureRect);            
      }

        
	}
	
	private void getCascade(){
		try {
            InputStream is = context.getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(cascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            mCascade = new CascadeClassifier(cascadeFile.getAbsolutePath());
            if (mCascade.empty()) {
                Log.e(TAG, "Failed to load cascade classifier");
                mCascade = null;
            } else
                Log.i(TAG, "Loaded cascade classifier from " + cascadeFile.getAbsolutePath());

            cascadeFile.delete();
            cascadeDir.delete();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }
	}
}



















