package kniz.main_pack.decode;

//import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.Vector;



import kniz.main_pack.Opsis;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.common.HybridBinarizer;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class QrCodeDecoder {
	
    private static final String TAG = "Opsis::QrCodeDecoder";


	public static byte [] decodeQRCode() throws NotFoundException, ChecksumException, FormatException, StreamCorruptedException, IOException, ClassNotFoundException{
		Opsis retval=null;
		File path = Environment.getExternalStoragePublicDirectory("Opsis/QRCodeTest");
		
		Bitmap bMap = BitmapFactory.decodeFile(path.getAbsolutePath()+"/testQRCode.jpg");
		if (bMap==null)
			Log.i(TAG,"[DECODE QR CODE] bmap null...");
		LuminanceSource source = new RGBLuminanceSource(bMap);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

		Reader reader = new MultiFormatReader();
		Result result;

		result = reader.decode(bitmap);
		
	    
	    Vector byteSegments = (Vector) result.getResultMetadata().get(ResultMetadataType.BYTE_SEGMENTS);  
	    
	    int tam = 0;
	    for (Object o : byteSegments) {
	        byte[] bs = (byte[])o;
	        tam += bs.length;
	    }
	    byte[] resultBytes = new byte[tam];
	    
//	    ByteArrayInputStream bis = new ByteArrayInputStream (resultBytes);
//	    ObjectInputStream ois = new ObjectInputStream (bis);
//	    retval = (Opsis)ois.readObject();
	    return resultBytes;
	      
	}
	
}
