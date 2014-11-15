/*
 * Copyright (c) 2002 by Konrad Rzeszutek
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package kniz.main_pack;

import java.io.*;
import java.util.*;
import java.lang.*;

import android.os.Environment;
import android.util.Log;
import android.widget.TextView;


/**
 * Creates the FaceBundle's from the list of images and tries to
 * match against submitted image.
 *
 *
 * @author Konrad Rzeszutek
 * @version 1.0
 */
public class EigenFaceCreator {

	private final String TAG            = "Opsis::EigenFaceCreator";

	
  private File root_dir;
  private static final int MAGIC_SETNR = 40; //in origine 16
  private FaceBundle[] b = null;
  
  private double [] toCreateFace;
  private int toCreateFaceBundleId;
  private int toCreateFaceDim;
  
  private double testDistance;
  
  private Boolean bundleCreated=false;
  
  /**
   * Our threshold for accepting the matched image. Anything above this
   * number is considered as not found in any of the face-spaces.
   */
  public static double THRESHOLD = 6.0;

  /**
   * Our minimum distance observed for the submitted image in the
   * face-spaces.
   *
   */
  public double DISTANCE = Double.MAX_VALUE;

  /**
   * This determines if caching of face-spaces should be activated.
   * Anything above zero means yes. Anything else means no.
   */
  public int USE_CACHE = 1;
  /**
   * Match against the given file.
   *
   * @return  The Identifier of the image in the face-space. If image not
   * found (based on THRESHOLD) null is returned.
 * @throws ClassNotFoundException 
 * @throws IOException 
 * @throws FileNotFoundException 
   */

  public Boolean checkIdCard(BiometricIdCard idCard, String imagePosition) throws FileNotFoundException, IOException, ClassNotFoundException{
	  FaceBundle bundle  = null;
	  int bundleId = idCard.getBundleId();
	  double[] img = readImage(imagePosition);
	  double distance = Double.MAX_VALUE;
	  File cache_dir = Environment.getExternalStoragePublicDirectory("Opsis/cache");
	  String name = "opsis_"+bundleId+".cache";
	  File f = new File(cache_dir.getAbsolutePath()+'/'+ name );
	  bundle = readBundle(f);
//	  bundle=idCard.getBundle();
	  bundle.submitFaceAndEigenVector(img,idCard.getEigenVector());
	  distance = bundle.distanceTest();
	  testDistance=distance;
	  if (distance<THRESHOLD)
		  return true;
	  return false;
	  
  }
  
  public String checkAgainst(String f) throws FileNotFoundException, IOException {
	  Log.i(TAG, "The file image is:"+f);
	    String id = null;
	    if (b != null) {
	      double small = Double.MAX_VALUE;
	      int idx = -1;
	      
	      Log.i(TAG, "[CHECK AGAINST]reading image"+f+"...");
	      double[] img = readImage(f);

	      if(img==null)
	    	  Log.i(TAG, "[CHECK AGAINST]img null...");
	      else
	    	  Log.i(TAG, "[CHECK AGAINST]img not null...");
	      
	      for (int i = 0; i < b.length; i++) {
	        b[i].submitFace(img);
	        if (small > b[i].distance() ) {
	           small = b[i].distance();
	            idx = i;
	        }
	      }
	      DISTANCE = small;
	      
	      toCreateFaceBundleId=b[idx].getId();
	      Log.i(TAG,"[CHECK AGAINST]idx:"+idx+" id:"+b[idx].getId());
	      toCreateFaceDim=b[idx].getToCreateFaceDim();
	      toCreateFace= new double[toCreateFaceDim];
	      b[idx].getToCreateFace(toCreateFace);
	      
	      if (small < THRESHOLD)
	        id = b[idx].getID();
	    }else
	    	  Log.i(TAG, "[CHECK AGAINST]b null...");
	    return id;
	    }
  
  //write the eigenValue matched into toCreateFace
  public void getToCreateFace(double [] toCreateFace){
	  
	  if (this.toCreateFace==null)
	  Log.i(TAG, "[GET TO CREATE FACE]this.toCreateFace null...");
  else
	  Log.i(TAG, "[GET TO CREATE FACE]this.toCreateFace not null...");
  
  if (b[toCreateFaceBundleId]==null)
	  Log.i(TAG, "[GET TO CREATE FACE]toCreateFace selected null...");
  else
	  Log.i(TAG, "[GET TO CREATE FACE]toCreateFace selected not null...");
  Log.i(TAG, "[GET TO CREATE FACE]toCreateFaceDim :"+toCreateFaceDim);
	  System.arraycopy(this.toCreateFace, 0, toCreateFace, 0, toCreateFaceDim);
  }
  
 
  
  
  // returns the eigenValue matched vector dimension
  public int getToCreateFaceDim(){
	  if (b==null)
		  Log.i(TAG, "b null...");
	  else
		  Log.i(TAG, "b not null...");
	  
	  if (b[toCreateFaceBundleId]==null)
		  Log.i(TAG, "b selected null...");
	  else
		  Log.i(TAG, "b selected not null...");
	  return toCreateFaceDim;
  }
  
  //return the Facebundle id
  public int getBundleId(){
	  return toCreateFaceBundleId;
  }

  /**
   * Construct the face-spaces from the given directory. There
   * must be at least sixteen images in that directory and each image
   * must have the same dimensions. The face-space bundles are also
   * cached in that directory for speeding up further initialization.
   *
   * @param n The directory where the training images are located.
   *
   * @throws  FileNotFoundException The <code>n</code> directory does not exist.
   * @throws  IOException Problems reading images from the given directory or saving
   * the cache file (if caching is enabled)
   * @throws  IllegalArgumentException The arguments submitted are wrong.
   * @throws  ClassNotFoundException The cached objects are out-of-date or are
   *  not this version's face-space objects
   *
   */


  public void readFaceBundles(String n) throws FileNotFoundException,
  IOException,  IllegalArgumentException, ClassNotFoundException {
	  if (bundleCreated)
		  return;
	  bundleCreated=true;
		Log.i(TAG, "[READ FACE BUNDLES]getting external storage public dir:"+n+"...");
  root_dir = Environment.getExternalStoragePublicDirectory(n);
  		Log.i(TAG, "[READ FACE BUNDLES]getting external storage public dir for cache...");
  File cache_dir = Environment.getExternalStoragePublicDirectory("Opsis/cache");
  		Log.i(TAG, "[READ FACE BUNDLES]making dirs for cache...");
  cache_dir.mkdirs();
	  
  File[] files= root_dir.listFiles();
  Vector filenames = new Vector();

  
  String[] set = new String[MAGIC_SETNR];

  int i= 0;
  		Log.i(TAG, "[READ FACE BUNDLES]reading filenames...");
  // Sort the list of filenames.
  for ( i = 0; i < files.length; i++) {
      filenames.addElement(files[i].getName());
  }
  Log.i(TAG, "[READ FACE BUNDLES]sorting filenames...");
  Collections.sort((List)filenames);


  
  b = new FaceBundle[(files.length / MAGIC_SETNR)+1];

  // Read each set of 16 images.
  Log.i(TAG, "[READ FACE BUNDLES]reading bundles ...");
  
  for (i = 0; i < b.length; i++) {

    for (int j = 0; j < MAGIC_SETNR;j++) {
      if (filenames.size() > j+MAGIC_SETNR*i) {
        set[j] = (String)filenames.get(j+MAGIC_SETNR*i);
      }
    }
    b[i] = submitSet(n + "/",cache_dir.getAbsolutePath() + "/",set,i);
  }

}
  
  
  
  

  /**
   * Submit a set of sixteen images in the <code>dir</code> directory and
   * construct a face-space object. This can be done either by reading the
   * cached objects (if there are any) or computing the {@link FaceBundle}.
   *
   * @param dir Directory where the images reside
   * @param files String array of the names of the files (ie: "image01.jpg")
   * @throws  FileNotFoundException The <code>dir</code> directory does not exist. Or the <code>files</code> are nonexistent
   * @throws  IOException Problems reading images from the given directory or saving
   * the cache file (if caching is enabled)
   * @throws  IllegalArgumentException The arguments submitted are wrong.
   * @throws  ClassNotFoundException The cached objects are out-of-date or are
   *  not this version's face-space objects
   *
   */

  
  
  private FaceBundle submitSet(String photoDir,String cacheDir, String[] files,int bundleId) throws FileNotFoundException,
  IOException,  IllegalArgumentException, ClassNotFoundException {
	  Log.i(TAG, "[SUBMIT SET]checking file length...");
  if (files.length != MAGIC_SETNR)
    throw new IllegalArgumentException("Can only accept a set of "+MAGIC_SETNR+" files.");

  FaceBundle bundle  = null;
  String name = "opsis_"+bundleId+".cache";
  
  File f = new File(cacheDir + name );

  if (f.exists() && (USE_CACHE > 0)){ /* it's cached */
	  Log.i(TAG, "[SUBMIT SET]reading cache bundle:"+f.getAbsolutePath()+"...");
    bundle = readBundle(f);
    	Log.i(TAG, "[SUBMIT SET]cache bundle red...");
  }
  else {
	  Log.i(TAG, "[SUBMIT SET]computing bundle...");
    bundle = computeBundle(photoDir, files, bundleId);
    if (USE_CACHE > 0){
    	Log.i(TAG, "[SUBMIT SET]saving new bundle cache...");
      saveBundle(f, bundle);
    }
  }

  return bundle;
}

  /**
   * Caches the face-space object in <code>f</code> file.
   *
   * @param f File where to save it
   * @param bundle  The face-space object.
   * @throws  FileNotFoundException The <code>f</code> is invalid.
   * @throws  IOException Problems reading the data.
   *
   */
  private void saveBundle(File f, FaceBundle bundle) throws FileNotFoundException, IOException{

      f.createNewFile();
      FileOutputStream out = new FileOutputStream(f.getAbsolutePath());
      ObjectOutputStream fos = new ObjectOutputStream(out);
      fos.writeObject(bundle);
      fos.close();
      //System.out.println("saved bundle ... "f.getAbsolutePath());


  }

  
  
  /**
   * Read the cache object from file.
   *
   * @param f File where to read from.
   * @throws  ClassNotFoundException The cached objects are out-of-date or are
   *  not this version's face-space objects
   * @throws  FileNotFoundException The <code>f</code> is invalid.
   * @throws  IOException Problems reading the data.
   */
  private FaceBundle readBundle(File f) throws FileNotFoundException, IOException, ClassNotFoundException {

    FileInputStream in = new FileInputStream(f);
    ObjectInputStream fo = new ObjectInputStream(in);
    FaceBundle bundle = (FaceBundle)fo.readObject();
    fo.close();
    Log.i(TAG,"[READ BUNDLE]bundle file:"+f.getAbsolutePath()+" id:"+bundle.getId());
    //System.out.println("read cached bundle..");

    return bundle;
  }

  /**
   * Construct the face-spaces from the given directory.
   *
   * @param dir The directory where to read from.
   * @param id  The names of the files which to read from.
   * @throws  FileNotFoundException The <code>dir</code> directory does not exist. Or the <code>id[]</code> are nonexistent
   * @throws  IOException Problems reading images from the given directory
   * @throws  IllegalArgumentException The image files  are either wrong format or there is an image with the wrong dimensions.

   */
  
  
 
  
  
  
  private FaceBundle computeBundle(String dir, String[] id, int bundleId) throws
    IllegalArgumentException, FileNotFoundException, IOException{

    xxxFile[] files = new xxxFile[MAGIC_SETNR];
    xxxFile file = null;
    String temp = null;
    int width = 0;
    int height = 0;
    int i = 0;

    for (i = 0; i < files.length; i++) {
      temp = id[i].toLowerCase();
      temp = temp.substring(temp.lastIndexOf('.')+1,temp.length());
      if (temp.equals("jpg") || temp.equals("jpeg"))  file = new JPGFile(dir+id[i]);
      else if (temp.equals("ppm") || temp.equals("pnm")) file = new PPMFile(dir+id[i]);
      if (file == null)
        throw new IllegalArgumentException(id[i]+" is not an image file!");

      files[i] = file;

      if (i == 0) {
        width = files[i].getWidth();
        height = files[i].getHeight();
      }
      if ((width != files[i].getWidth()) || (height != files[i].getHeight()) )
        throw new IllegalArgumentException("All image files must have the same width and height!");
    }

    // Then construct our big double[][] array - MxN^2
    double[][] face_v = new double[MAGIC_SETNR][width*height];
    //System.out.println("Generating bundle of ("+face_v.length+" x "+face_v[0].length+"), h:"+height+" w:"+width);

    for (i = 0; i < files.length; i++) {
        //System.arraycopy(files[i].getDouble(),0,face_v[i],0,face_v[i].length);
        face_v[i] = files[i].getDouble();
    }

    // Do the computation!

    return EigenFaceComputation.submit(face_v, width, height, id, false, bundleId);

  }
  
  
//  public double[] readImage(String f,TextView tv) throws FileNotFoundException,
//    IllegalArgumentException, IOException {
//
//	  tv.append("[READIMAGE] invoked...\n");
//      xxxFile file = null;
//      String temp = f.toLowerCase();
//      temp = temp.substring(temp.lastIndexOf('.')+1,temp.length());
//
//	  if (temp.equals("jpg")){  
//		  tv.append("[CHECKAGAINST] temp = "+temp+"\n");
//	      file = new JPGFile(f,tv);
//	      tv.append("[CHECKAGAINST] jpg letta correttamente...\n");
//      }
//      else if (temp.equals("ppm") || temp.equals("pnm")) file = new PPMFile(f);
//      if (file == null)
//        throw new IllegalArgumentException(f+" is not an image file!");
//      return file.getDouble();
//  }
  
  public double[] readImage(String f) throws FileNotFoundException,
  IllegalArgumentException, IOException {

    xxxFile file = null;
    String temp = f.toLowerCase();
    temp = temp.substring(temp.lastIndexOf('.')+1,temp.length());

    if (temp.equals("jpg"))  file = new JPGFile(f);
    else if (temp.equals("ppm") || temp.equals("pnm")) file = new PPMFile(f);
    if (file == null)
      throw new IllegalArgumentException(f+" is not an image file!");
    return file.getDouble();
  }

  public FaceBundle useCache(String fileName) throws FileNotFoundException, IOException, ClassNotFoundException{
	  FaceBundle bundle =null;
	  File f = new File(fileName);
	  if (f.exists()) /* it's cached */
	    bundle = readBundle(f);
	  return bundle;
  }
  

  public double testDistance(){
	  return testDistance;
  }
  
}


class ImageFilter implements FileFilter {

   public boolean accept(File f) {

       if (f.isDirectory()) {
           return true;
       }

       String extension = f.getName();
       int i = extension.lastIndexOf('.');

       if (i > 0 &&  i < extension.length() - 1) {
           extension = extension.substring(i+1).toLowerCase();
       }

       if (extension != null) {
           if ((extension.equals("ppm")) ||
               (extension.equals("pnm")) ||
               (extension.equals("jpg")) ||
               (extension.equals("jpeg"))) {
                   return true;
            } else {
                return false;
            }
        }

        return false;
    }
}
