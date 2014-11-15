package kniz.main_pack.encode;

//import java.io.ByteArrayInputStream;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.io.UnsupportedEncodingException;
//import java.util.Hashtable;

import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Message;
import android.util.Log;

//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.EncodeHintType;
//import com.google.zxing.MultiFormatWriter;
//import com.google.zxing.Writer;
import com.google.zxing.WriterException;
//import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.ByteMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.QRCode;



public class QrCodeEncoder {
	
	public final static String TAG = "Opsis::QrCodeEncoder";
	public final static int QR_DIMENSION=500;
	private static final int QUIET_ZONE_SIZE = 4;
	

	
	public static Bitmap createQrCode (byte [] b) throws WriterException {
		if (b==null)
			Log.i(TAG, "[CREATE QR CODE] byte array null...");
		Bitmap retval=null;
		ByteMatrix qrCodeBM = qrCodeByteMatrix(b);
		int width = qrCodeBM.getWidth();
		int height = qrCodeBM.getHeight();
		byte[][] array = qrCodeBM.getArray();
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int grey = array[y][x] & 0xff;
				// pixels[y * width + x] = (0xff << 24) | (grey << 16) |
				// (grey << 8) | grey;
				pixels[y * width + x] = 0xff000000 | (0x00010101 * grey);
			}
		}

		retval = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		retval.setPixels(pixels, 0, width, 0, 0, width, height);
//		Message message = Message.obtain(handler, R.id.encode_succeeded);
//		message.obj = bitmap;
//		message.sendToTarget();
		return retval;
		
		
		
		
	}
	
	public static ByteMatrix qrCodeByteMatrix (byte [] b) throws WriterException{
		ErrorCorrectionLevel errorCorrectionLevel = ErrorCorrectionLevel.L;

	    QRCode code = new QRCode();
	    QrByteEncoder.encode(b, errorCorrectionLevel, code);
//	    renderResult(code, width, height);
	    ByteMatrix input = code.getMatrix();
	    int inputWidth = input.getWidth();
	    int inputHeight = input.getHeight();
	    int qrWidth = inputWidth + (QUIET_ZONE_SIZE << 1);
	    int qrHeight = inputHeight + (QUIET_ZONE_SIZE << 1);
	    int outputWidth = Math.max(QR_DIMENSION, qrWidth);
	    int outputHeight = Math.max(QR_DIMENSION, qrHeight);

	    int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
	    // Padding includes both the quiet zone and the extra white pixels to accommodate the requested
	    // dimensions. For example, if input is 25x25 the QR will be 33x33 including the quiet zone.
	    // If the requested size is 200x160, the multiple will be 4, for a QR of 132x132. These will
	    // handle all the padding from 100x100 (the actual QR) up to 200x160.
	    int leftPadding = (outputWidth - (inputWidth * multiple)) / 2;
	    int topPadding = (outputHeight - (inputHeight * multiple)) / 2;

	    ByteMatrix output = new ByteMatrix(outputWidth, outputHeight);
	    byte[][] outputArray = output.getArray();

	    // We could be tricky and use the first row in each set of multiple as the temporary storage,
	    // instead of allocating this separate array.
	    byte[] row = new byte[outputWidth];

	    // 1. Write the white lines at the top
	    for (int y = 0; y < topPadding; y++) 
	      for (int x=0; x<outputArray[y].length;++x)
	    	  outputArray[y][x]=(byte) 255;
	    

	    // 2. Expand the QR image to the multiple
	    byte[][] inputArray = input.getArray();
	    for (int y = 0; y < inputHeight; y++) {
	      // a. Write the white pixels at the left of each row
	      for (int x = 0; x < leftPadding; x++) {
	        row[x] = (byte) 255;
	      }

	      // b. Write the contents of this row of the barcode
	      int offset = leftPadding;
	      for (int x = 0; x < inputWidth; x++) {
	        byte value = (inputArray[y][x] == 1) ? 0 : (byte) 255;
	        for (int z = 0; z < multiple; z++) {
	          row[offset + z] = value;
	        }
	        offset += multiple;
	      }

	      // c. Write the white pixels at the right of each row
	      offset = leftPadding + (inputWidth * multiple);
	      for (int x = offset; x < outputWidth; x++) {
	        row[x] = (byte) 255;
	      }

	      // d. Write the completed row multiple times
	      offset = topPadding + (y * multiple);
	      for (int z = 0; z < multiple; z++) {
	        System.arraycopy(row, 0, outputArray[offset + z], 0, outputWidth);
	      }
	    }

	    // 3. Write the white lines at the bottom
	    int offset = topPadding + (inputHeight * multiple);
	    for (int y = offset; y < outputHeight; y++) 
	      for (int x=0; x<outputArray[y].length;++x)
	    	  outputArray[y][x]= (byte) 255;
	    return output;
	}
	
	

}
