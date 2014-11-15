/*
 * Copyright (C) 2010 Francesco Feltrinelli <francesco.feltrinelli@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This is based on a previous file published by <ZXing authors>
 * (http://code.google.com/p/zxing/) and released under the 
 * Apache License, Version 2.0.
 */

package kniz.main_pack.decode;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import kniz.main_pack.Opsis1_1Activity;
import kniz.main_pack.R;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;



//import feltrinelli.project.android.file2qr.R;
//import feltrinelli.project.android.file2qr.engine.NotFileTypeException;
//import feltrinelli.project.android.file2qr.engine.FileFromQrDecoder;
//import feltrinelli.project.android.file2qr.manager.NotificationManager;

public class CameraDecodeActivity extends Activity implements SurfaceHolder.Callback {

    private static final String TAG = "Opsis::CameraDecodeActivity";

	public final static String EXTRA_DECODED_FILE= "DecodedFile";
	public final static int CAMERA_CODE = 2;

	private final static int AUTOFOCUS_DELAY= 3000;
	private final static int PREVIEW_MAX_SIZE= 640;
	
	private static Opsis1_1Activity parentActivity;
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private Camera camera;
	
	private boolean previewRunning = false;

//	private FileFromQrDecoder qrDecoder;
	
	private Handler handler;
	private boolean exiting= false;
	
//	private NotificationManager notificationManager;
	
	private PowerManager.WakeLock wakeLock;
	
	private final int splitNum=4;
	private byte [] qrCodesBytes=null;
	private int shoot=0;
	
	
	private void closeActivity(boolean abort){
		exiting= true;
		handler.removeCallbacks(previewRunnable);
		stopAutofocus();
		if (qrCodesBytes!=null)
		Log.i(TAG,"[CLOSE ACTIVITY]qrcodebytes not null!!!");
		Intent intent = new Intent();
		try {
			intent.putExtra("bytes", new String(qrCodesBytes,"ISO-8859-1"));
			setResult(RESULT_OK, intent);
		} catch (UnsupportedEncodingException e) {
			Log.i(TAG,"[CLOSE ACTIVITY]UnsupportedEncodingException:"+e.toString());
		}
		finish();
	}
	
	private void stopAutofocus(){
		handler.removeCallbacks(autoFocusRunnable);
		if (camera != null) camera.cancelAutoFocus();
	}
	
	private View.OnClickListener tapScreenListener= new View.OnClickListener() {
		public void onClick(View v) {
			closeActivity(true);
		}
	};
	
	private OnClickListener errorQuitListener = new OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			closeActivity(true);
		}
	};
	
	private Runnable previewRunnable= new Runnable() {
		public void run() {
			if (!exiting) camera.setOneShotPreviewCallback(previewCallback);
		}
	};
	
	private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
	
		public void onPreviewFrame(byte[] data, Camera camera) {
			LuminanceSource source= buildLuminanceSourceFromCameraPreview(data, camera.getParameters());
			try {
//				File decodedFile= qrDecoder.
//				parentActivity.setOpsisBytes(tmp);
				qrCodesBytes=decodeFromQR(source);
				closeActivity(false);
			} catch (Exception e) {
				handler.post(previewRunnable);
			}
		}
	};
	
	private Runnable autoFocusRunnable= new Runnable() {
		public void run() {
			if (!exiting) camera.autoFocus(autoFocusCallback);			
		}
	};
	
	private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
		public void onAutoFocus(boolean success, final Camera camera) {
			handler.postDelayed(autoFocusRunnable, AUTOFOCUS_DELAY);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.camera);
		surfaceView = (SurfaceView) findViewById(R.id.surface_camera);
		surfaceView.setOnClickListener(tapScreenListener);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

//		qrDecoder= new FileFromQrDecoder(this);
		handler= new Handler();
		
//		notificationManager= new NotificationManager(this);
	}
	
	@Override
	protected void onResume() {
		
		super.onResume();
		
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
				CameraDecodeActivity.class.getName());
		wakeLock.acquire();
	}
	
	@Override
	protected void onPause() {
		
		if (!exiting) closeActivity(true);
		wakeLock.release();
		
		super.onPause();
	}

	public void surfaceCreated(SurfaceHolder holder) {

		camera = Camera.open();
		if (camera==null){
//			notificationManager.showErrorMessage(R.string.error_camera_preview, errorQuitListener);
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

		if (previewRunning) camera.stopPreview();

		Parameters cameraParameters = camera.getParameters();
		Size previewSize= getBestPreviewSize(cameraParameters);
		cameraParameters.setPreviewSize(previewSize.width, previewSize.height);
		camera.setParameters(cameraParameters);

		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
//			notificationManager.showErrorMessage(R.string.error_camera_preview, errorQuitListener);
		}

		camera.startPreview();
		previewRunning = true;
		
		handler.post(previewRunnable);
		
		String focusMode = camera.getParameters().getFocusMode();
		if (focusMode != Camera.Parameters.FOCUS_MODE_FIXED && 
			focusMode != Camera.Parameters.FOCUS_MODE_INFINITY)
			handler.post(autoFocusRunnable);
	}
	
	private static Comparator<Size> sizeComparator= new Comparator<Size>() {
		public int compare(Size size1, Size size2) {
			int max1= Math.max(size1.width, size1.height);
			int max2= Math.max(size2.width, size2.height);
			
			if (max1 < max2) return -1;
			else if (max1 > max2) return 1;
			else {
				int min1= Math.min(size1.width, size1.height);
				int min2= Math.min(size2.width, size2.height);
				
				if (min1 < min2) return -1;
				else if (min1 > min2) return 1;
				else return 0;
			}
		}
	};
	
	private Size getBestPreviewSize(Parameters cameraParameters){
		
		List<Size> sizes= cameraParameters.getSupportedPreviewSizes(); 
		Collections.sort(sizes,	sizeComparator);
		ListIterator<Size> iterator= sizes.listIterator(sizes.size()-1);
		
		while (iterator.hasPrevious()){
			Size size= iterator.previous();
			int max= Math.max(size.width, size.height);
			if (max <= PREVIEW_MAX_SIZE) return size;
		}
		
		return sizes.get(0);
	}

	public void surfaceDestroyed(SurfaceHolder holder) {

		camera.stopPreview();
		previewRunning = false;
		camera.release();
	}
	
	/**
	 * A factory method to build the appropriate LuminanceSource object based on
	 * the format of the preview buffers, as described by Camera.Parameters.
	 * 
	 * @param previewData
	 *            A preview frame.
	 * @param cameraParameters
	 *            The camera parameters
	 * @return A PlanarYUVLuminanceSource instance.
	 */
	public LuminanceSource buildLuminanceSourceFromCameraPreview(
			byte[] previewData, Parameters cameraParameters) {
		
		int previewFormat = cameraParameters.getPreviewFormat();
	    String previewFormatString = cameraParameters.get("preview-format");
	    Size previewSize= cameraParameters.getPreviewSize();
		
		switch (previewFormat) {
		// This is the standard Android format which all devices are REQUIRED to
		// support.
		// In theory, it's the only one we should ever care about.
		case PixelFormat.YCbCr_420_SP:
			// This format has never been seen in the wild, but is compatible as
			// we only care
			// about the Y channel, so allow it.
		case PixelFormat.YCbCr_422_SP:
			return new PlanarYUVLuminanceSource(previewData, previewSize.width, previewSize.height);
		default:
			// The Samsung Moment incorrectly uses this variant instead of the
			// 'sp' version.
			// Fortunately, it too has all the Y data up front, so we can read
			// it.
			if ("yuv420p".equals(previewFormatString)) {
				return new PlanarYUVLuminanceSource(previewData, previewSize.width, previewSize.height);
			}
		}
		throw new IllegalArgumentException("Unsupported picture format: "
				+ previewFormat + '/' + previewFormatString);
	}
	
	public LuminanceSource buildLuminanceSourceFromImageFile(File qr) throws FileNotFoundException{
		return new RGBLuminanceSource(qr.getAbsolutePath());
	} 
	
	public byte [] decodeFromQR(File qrImageFile) throws NotFoundException, ChecksumException, FormatException, IOException{
		
		return decodeFromQR(buildLuminanceSourceFromImageFile(qrImageFile));
	}
	
	public byte [] decodeFromQR(LuminanceSource qrSource) throws NotFoundException, ChecksumException, FormatException, IOException{
		
		Reader reader = new QRCodeReader();
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(qrSource));
		Result result = reader.decode(bitmap);
		
		byte[] rawBytes= getRawBytes(result);
		
//		FileInfo decodedInfo = decodeFile(rawBytes);
//	
//		File decodedFile= new File(
//				fileManager.getDecodedFolder().getAbsolutePath(),
//				decodedInfo.getFileName());
//	
//		fileManager.writeToFile(decodedFile, decodedInfo.getBody());
		
		return rawBytes;
	}
	
	private byte[] getRawBytes(Result result){
		
		Hashtable hashtable= result.getResultMetadata();
		Vector<byte[]> segments= (Vector<byte[]>) hashtable.get(ResultMetadataType.BYTE_SEGMENTS);
		int byteNum= 0;
		for (byte[] array: segments)
			byteNum += array.length;
		byte[] rawBytes= new byte[byteNum];
		int index=0;
		for (byte[] array: segments)
			for (int i=0; i < array.length; i++,index++)
				rawBytes[index]= array[i];
		
		return rawBytes;
	}
}
