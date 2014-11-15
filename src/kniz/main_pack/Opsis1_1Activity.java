package kniz.main_pack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
//import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;


import kniz.main_pack.Jama.QRDecomposition;
import kniz.main_pack.decode.CameraDecodeActivity;
import kniz.main_pack.decode.OpsisDecoder;
import kniz.main_pack.decode.QrCodeDecoder;
import kniz.main_pack.encode.FrontIdCardBuilder;
import kniz.main_pack.encode.OpsisEncoder;
import kniz.main_pack.encode.QrCodeEncoder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class Opsis1_1Activity extends Activity {
	private final String TAG            = "Opsis::Activity";
	
	public static final int JPEG = 0;
	public static final int PNG = 1;

	public static final int DESCRIPTION=0;
	public static final int BUILDDESCRIPTION=1;
	public static final int BUILDDESCRIPTIONPICTURE=2;
	public static final int SHOWIMAGE=3;
	public static final int USEDESCRIPTION=4;
	public static final int LEARNING=5;
	public static final int VERIFYPICTURE=6;
	public static final int SHOWMATCHINGIDENTITY=7;
	public static final int TAKEPICTURETOTEST=8;
	public static final int SHOWPICTURETOTEST=9;
	public static final int CHECKRESPONSE=10;
	public static final int SHOOTQRCODE=11;
	public static final int WAIT=12;
	public static final int QRCODEPROGRESS=13;
	public static final int SHOWIDCARD=14;
	public static final int SHOWFRONTALIDCARD=15;
	public static final int ERROR=-1;
	
	public static final int PICKOPSISCARD=0;
	public static final int USEDBPICTURE=1;
	public static final int CREATEQRCODE=2;
	public static final int GETOPSISBYTE=3;
	
	public static final int SHOOTNUM=4;
	
	private final Context presentActivity = this;
	private MenuItem mItemQuit;
	private static int presentContentView = DESCRIPTION;
	private static String error;
	
	private Uri opsisUri;
//	private double [] newFaceImage;
	private String fileDir;
	private Bitmap dbImage;
    private Bitmap toDisplayBmp;
    private Bitmap frontalIdCard;
    private static EigenFaceCreator efc = new EigenFaceCreator();
    private byte [] [] decodedBytes;

	private Boolean imageAlreadyPresent=false;
    
	
	//Error layout components
	private TextView errorTextView;
	
	//----------------------BUILD SECTION-----------------
	private String faceTestName;
	private String lastName;
	private String firstName;
	private String birthPlace;
	private String birthDate;
	private String sex;
	private String height;
	private String municipality;
	private String address;
	private String nationality;
	private String fiscalCode;
	private String validityToTravel;



//	//Learning layout components
//	private EditText faceSpacePosition; 
//	private RadioGroup learningRadioGroup;
//	private RadioButton learningRadioChoice;
//	private Button startLearningButton;
//	
//	private OnClickListener learningListener; 
	
	//Description layout components
	private RadioGroup descriptionRadioGroup;
	private RadioButton descriptionRadioChoice;
	private Button descriptionButton;
	
	private OnClickListener descriptionListener;
	
	//Build description layout components
	private Button buildDescriptionButton;
	private EditText buildDescriptionFirstNameText;
	private EditText buildDescriptionLastNameText;
	private EditText buildDescriptionBirthPlaceText;
	private EditText buildDescriptionBirthDateText;
	private EditText buildDescriptionHeightText;
	private EditText buildDescriptionMunicipalityOfIssueText;
	private EditText buildDescriptionAddressText;
	private EditText buildDescriptionNationalityText;
	private EditText buildDescriptionFiscalCodeText;
	
	private RadioGroup buildDescriptionSexRadioGroup;
	private RadioButton buildDescriptionSexRadioChoice;
	private RadioGroup buildDescriptionValidityToTravelRadioGroup;
	private RadioButton buildDescriptionValidityToTravelRadioChoice;
	private RadioGroup buildDescriptionRadioGroup;
	private RadioButton buildDescriptionRadioChoice;
	
	
	private OnClickListener buildDescriptionListener;
	

	
	//description FdView buildDescriptionPicture components
	private FdView buildDescriptionPictureView;
	private OnTouchListener buildDescriptionPictureListener;
	
	//show_face components
	private Button show_faceRejectButton;
	private Button show_faceAcceptButton;
	private ProgressDialog show_faceShootProgressDialog;
	private ProgressDialog show_faceCropProgressDialog;
	private int show_faceShootProgressDialogId;
	private int show_faceCropProgressDialogId;
	private ImageView show_faceImageView;
	
	private OnClickListener show_faceAcceptListener;
	private OnClickListener show_faceRejectListener;
	
	//show frontal id card components
	private TextView showFrontalIdCardTextView;
	private ImageView showFrontalIdCardImageView;
	private Button showFrontalIdCardRejectButton;
	private Button showFrontalIdCardAcceptButton;

	private OnClickListener showFrontalIdCardRejectButtonListener;
	private OnClickListener showFrontalIdCardAcceptButtonListener;
	
	
	
	
	//----------------------TEST SECTION-----------------
	
	private BiometricIdCard toTestIdCard;
	
	
	//takePictureToTestView components
	private FdView takePictureToTestView;
	private OnTouchListener takePictureToTestListener; 
	
	//show_picture_to_test components
	private Button show_toTestFaceRejectButton;
	private Button show_toTestFaceAcceptButton;
	private ImageView show_toTestFaceImageView;
	
	private OnClickListener show_toTestFaceAcceptListener;
	private OnClickListener show_toTestFaceRejectListener;
	
	//check_response components
	private Button checkResponseRejectButton;
	private Button checkResponseAcceptButton;
	private ImageView checkResponseImageView;
	private ImageView checkResponseResultImageView;
	
	private OnClickListener checkResponseAcceptListener;
	private OnClickListener checkResponseRejectListener;
	
	//shoot qrcode components
	private SurfaceView shootQrCodeView;
	private OnTouchListener shootQrCodeListener;
	private int shoot=0;
	private Handler qrCodeHandler;
	private PreviewCallback qrCodePreviewCallback;
	private Runnable qrCodePreviewRunnable;
	private AutoFocusCallback qrCodeAutoFocusCallback;
	private Runnable qrCodeAutoFocusRunnable;
	private Boolean stopQrCodeSearch=false;
	private byte [] opsisBytes=null;
	
	//qrCodeProgress components
	private TextView qrCodeProgressTitleTextView;
	private TextView qrCodeProgressInstructionTextView;
	private Button qrCodeProgressButton;
	
	private OnClickListener qrCodeProgressButtonListener;
	private Boolean getQrCodePicture =false;
	
	//show id card components
	private TextView showIdCardFirstNameTextView;
	private TextView showIdCardLastNameTextView;
	private TextView showIdCardBirthPlaceTextView;
	private TextView showIdCardBirthDateTextView;
	private TextView showIdCardSexTextView;
	private TextView showIdCardHeightTextView;
	private TextView showIdCardMunicipalityOfResidenceTextView;
	private TextView showIdCardSubMunicipalityOfResidenceTextView;
	private TextView showIdCardAddressTextView;
	private TextView showIdCardIssuingDateTextView;
	private TextView showIdCardExpirationDateTextView;
	private TextView showIdCardNationalityTextView;
	private TextView showIdCardFiscalCodeTextView;
	private TextView showIdCardValidityToTravelTextView;
	private Button showIdCardVerifyButton;
	private Button showIdCardBackButton;
	
	private OnClickListener showIdCardVerifyButtonListener;
	private OnClickListener showIdCardBackButtonListener;
	
	
//	//wait view
//	private 
	
	
	
	
	
	private Camera mCamera;
    private Camera.PictureCallback jpegCallback;
    
	
	
	
	
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        startListeners();
		updateContentView();       		
	}
	
	/*
	 * automatic view setter
	 * 
	 *  ids: 
	 *  	0 : learning
	 *  	1 : description
	 *  	2 : buildDescription
	 *  	3 : buildDescriptionPicture
	 * 		
	 * 		-1: error
	 */
	
	public void updateContentView(){
		
		//learning layout
//		if (presentContentView==LEARNING){
//			setContentView(R.layout.learning);
//	        faceSpacePosition = (EditText) findViewById(R.id.cacheName);
//	        learningRadioGroup = (RadioGroup) findViewById(R.id.learningRadioGroup);
//	        startLearningButton = (Button) findViewById(R.id.startLearnButton);
//	        //listeners
//	        startLearningButton.setOnClickListener(learningListener);
//
//		}

		//description layout
		if (presentContentView==DESCRIPTION){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			setContentView(R.layout.description);
			descriptionRadioGroup = (RadioGroup) findViewById(R.id.descriptionRadioGroup);
			descriptionButton = (Button) findViewById(R.id.descriptionButton);
			//listeners
			descriptionButton.setOnClickListener(descriptionListener);
		}
		
		//build description layout
		else if (presentContentView==BUILDDESCRIPTION){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			setContentView(R.layout.build_description);
			
	        buildDescriptionButton = (Button) findViewById(R.id.buildDescriptionButton);
	        buildDescriptionFirstNameText= (EditText) findViewById(R.id.BuildDescriptionFirstName);
	        buildDescriptionLastNameText= (EditText) findViewById(R.id.BuildDescriptionLastName);
	        buildDescriptionBirthPlaceText= (EditText) findViewById(R.id.BuildDescriptionBirthPlace);
	        buildDescriptionBirthDateText= (EditText) findViewById(R.id.BuildDescriptionBirthDate);
	        buildDescriptionHeightText= (EditText) findViewById(R.id.BuildDescriptionHeight);
	        buildDescriptionMunicipalityOfIssueText= (EditText) findViewById(R.id.BuildDescriptionMunicipalityOfResidence);
	        buildDescriptionAddressText= (EditText) findViewById(R.id.BuildDescriptionAddress);
	        buildDescriptionNationalityText= (EditText) findViewById(R.id.BuildDescriptionNationality);
	        buildDescriptionFiscalCodeText= (EditText) findViewById(R.id.BuildDescriptionFiscalCode);
	        
	        buildDescriptionRadioGroup = (RadioGroup) findViewById(R.id.buildDescriptionRadioGroup);
	        buildDescriptionSexRadioGroup = (RadioGroup) findViewById(R.id.buildDescriptionSexRadioGroup);
	        buildDescriptionValidityToTravelRadioGroup = (RadioGroup) findViewById(R.id.buildDescriptionValidityToTravelGroup);
	        
	        //listeners
	        buildDescriptionButton.setOnClickListener(buildDescriptionListener);
	        
	        buildDescriptionFirstNameText.clearFocus();
	        buildDescriptionLastNameText.clearFocus();
	        buildDescriptionBirthPlaceText.clearFocus();
	        buildDescriptionBirthDateText.clearFocus();
	        buildDescriptionHeightText.clearFocus();
	        buildDescriptionMunicipalityOfIssueText.clearFocus();
	        buildDescriptionNationalityText.clearFocus();
	        buildDescriptionFiscalCodeText.clearFocus();
	        
	        
		}
		
		//build description fdview dynamic layout
		else if (presentContentView==BUILDDESCRIPTIONPICTURE){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		    buildDescriptionPictureView = new FdView(this);
	        setContentView(buildDescriptionPictureView);
	        
	        //listeners
	        buildDescriptionPictureView.setOnTouchListener(buildDescriptionPictureListener);
		}
		
		else if (presentContentView==SHOWIMAGE){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			setContentView(R.layout.show_face);
			show_faceAcceptButton = (Button) findViewById(R.id.show_faceAcceptImageButton);
			show_faceRejectButton = (Button) findViewById(R.id.show_faceRejectImageButton);
			show_faceImageView = (ImageView) findViewById(R.id.show_faceImageView);
			show_faceShootProgressDialog = new ProgressDialog(this);
			show_faceCropProgressDialog = new ProgressDialog(this);
			
			show_faceShootProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			show_faceShootProgressDialog.setMessage("Wait for the camera to shoot...");
			show_faceShootProgressDialog.setCancelable(true);
			
			show_faceCropProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			show_faceCropProgressDialog.setMessage("Wait for the crop to be done...");
			show_faceShootProgressDialog.setCancelable(true);
			
			//listeners
			show_faceAcceptButton.setOnClickListener(show_faceAcceptListener);
			show_faceRejectButton.setOnClickListener(show_faceRejectListener);
			
			show_faceAcceptButton.setEnabled(false);
			show_faceRejectButton.setEnabled(false);

			
			
			
		}
		else if (presentContentView==TAKEPICTURETOTEST){
			if (mCamera!=null){
	    		mCamera.stopPreview();
	    		mCamera.release();
	    		mCamera=null;
	    	}
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		    takePictureToTestView = new FdView(this);
	        setContentView(takePictureToTestView);
	        
	        //listeners
	        takePictureToTestView.setOnTouchListener(takePictureToTestListener);
		}
		
		else if (presentContentView==SHOWPICTURETOTEST){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			setContentView(R.layout.show_to_test_face);
			show_toTestFaceAcceptButton = (Button) findViewById(R.id.show_toTestFaceAcceptImageButton);
			show_toTestFaceRejectButton = (Button) findViewById(R.id.show_toTestFaceRejectImageButton);
			show_toTestFaceImageView = (ImageView) findViewById(R.id.show_toTestFaceImageView);
			
			//listeners
			show_toTestFaceAcceptButton.setOnClickListener(show_toTestFaceAcceptListener);
			show_toTestFaceRejectButton.setOnClickListener(show_toTestFaceRejectListener);
			
			show_toTestFaceAcceptButton.setEnabled(false);
			show_toTestFaceRejectButton.setEnabled(false);
	
		}
		
		else if (presentContentView==CHECKRESPONSE){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			setContentView(R.layout.check_response);
			checkResponseAcceptButton = (Button) findViewById(R.id.checkResponseAcceptButton);
			checkResponseRejectButton = (Button) findViewById(R.id.checkResponseRejectButton);
			checkResponseImageView = (ImageView) findViewById(R.id.checkResponseImageView);
			checkResponseResultImageView = (ImageView) findViewById(R.id.checkResponseResultImageView);
			
			//listeners
			checkResponseAcceptButton.setOnClickListener(checkResponseAcceptListener);
			checkResponseRejectButton.setOnClickListener(checkResponseRejectListener);
			
			checkResponseAcceptButton.setEnabled(false);
			checkResponseRejectButton.setEnabled(false);
			checkResponseImageView.setImageBitmap(dbImage);
		}
		
		else if (presentContentView==SHOOTQRCODE){
			if (mCamera!=null){
	    		mCamera.stopPreview();
	    		mCamera.release();
	    		mCamera=null;
	    	}
			
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//		    shootQrCodeView = new CvView(this);
			
	        setContentView(R.layout.camera);
	        shootQrCodeView = (SurfaceView) findViewById(R.id.surface_camera);
	        shootQrCodeView.setOnTouchListener(shootQrCodeListener);
		}
		
		else if (presentContentView==WAIT){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			setContentView(R.layout.wait);
		}
		
		else if (presentContentView==QRCODEPROGRESS){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			setContentView(R.layout.qrcode_progress);
			qrCodeProgressTitleTextView= (TextView)findViewById(R.id.qrcode_progressTitle);
			qrCodeProgressInstructionTextView= (TextView) findViewById(R.id.qrcode_progressInstructions);
			qrCodeProgressButton= (Button) findViewById(R.id.qrcode_progressButton);
			if(qrCodeProgressButton==null)
				Log.i(TAG,"[SET PRESENT CONTENT VIEW]qrCodeProgressButton null");
			else if (qrCodeProgressButtonListener==null)
				Log.i(TAG,"[SET PRESENT CONTENT VIEW]qrCodeProgressButtonListener null");
			qrCodeProgressButton.setOnClickListener(qrCodeProgressButtonListener);
		}
		
		else if (presentContentView==SHOWIDCARD){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			setContentView(R.layout.show_id_card);
			showIdCardFirstNameTextView = (TextView)findViewById(R.id.showIdCardFirstName);
			showIdCardLastNameTextView = (TextView)findViewById(R.id.showIdCardLastName);
			showIdCardBirthPlaceTextView = (TextView)findViewById(R.id.showIdCardBirthPlace);
			showIdCardBirthDateTextView = (TextView)findViewById(R.id.showIdCardBirthDate);
			showIdCardSexTextView = (TextView)findViewById(R.id.showIdCardSex);
			showIdCardHeightTextView= (TextView)findViewById(R.id.showIdCardHeight);
			showIdCardMunicipalityOfResidenceTextView = (TextView)findViewById(R.id.showIdCardMunicipalityOfResidence);
			showIdCardSubMunicipalityOfResidenceTextView = (TextView)findViewById(R.id.showIdCardSubMunicipalityOfResidence);
			showIdCardAddressTextView = (TextView)findViewById(R.id.showIdCardAddress);
			showIdCardIssuingDateTextView = (TextView)findViewById(R.id.showIdCardIssuingDate);
			showIdCardExpirationDateTextView = (TextView)findViewById(R.id.showIdCardExpirationDate);
			showIdCardNationalityTextView = (TextView)findViewById(R.id.showIdCardNationality);
			showIdCardFiscalCodeTextView = (TextView)findViewById(R.id.showIdCardFiscalCode);
			showIdCardValidityToTravelTextView = (TextView)findViewById(R.id.showIdCardValidityToTravel);
			showIdCardBackButton = (Button)findViewById(R.id.showIdCardBackButton);
			showIdCardVerifyButton = (Button)findViewById(R.id.showIdCardVerifyButton);

			if (showIdCardVerifyButton==null)
				Log.i(TAG,"[SET PRESENT CONTENT VIEW - SHOW ID CARD] showIdCardVerifyButton null");
			if (showIdCardVerifyButtonListener==null)
				Log.i(TAG,"[SET PRESENT CONTENT VIEW - SHOW ID CARD] showIdCardVerifyButtonListener null");
			if (showIdCardBackButton==null)
				Log.i(TAG,"[SET PRESENT CONTENT VIEW - SHOW ID CARD] showIdCardBackButton null");
			if (showIdCardBackButtonListener==null)
				Log.i(TAG,"[SET PRESENT CONTENT VIEW - SHOW ID CARD] showIdCardBackButtonListener null");
			
			showIdCardVerifyButton.setOnClickListener(showIdCardVerifyButtonListener);
			showIdCardBackButton.setOnClickListener(showIdCardBackButtonListener);		
		}
		
		else if (presentContentView==SHOWFRONTALIDCARD){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			setContentView(R.layout.show_frontal_id_card);
			
			showFrontalIdCardImageView= (ImageView) findViewById(R.id.showFrontalIdCardImageView);
			showFrontalIdCardRejectButton = (Button) findViewById(R.id.showFrontalIdCardRejectImageButton);
			showFrontalIdCardAcceptButton = (Button) findViewById(R.id.showFrontalIdCardAcceptImageButton);
			
			showFrontalIdCardRejectButton.setOnClickListener(showFrontalIdCardRejectButtonListener);
			showFrontalIdCardAcceptButton.setOnClickListener(showFrontalIdCardAcceptButtonListener);
			
			showFrontalIdCardImageView.setImageBitmap(frontalIdCard);
		}
		
		else {
			setContentView(R.layout.error);
			errorTextView = (TextView) findViewById(R.id.errorTextView);
			errorTextView.setText("Exception :"+error);
		}
	}
	
	public void startListeners(){
		
		descriptionListener = new OnClickListener(){
			public void onClick(View v){
				descriptionRadioChoice = (RadioButton) findViewById(descriptionRadioGroup.getCheckedRadioButtonId());
				if (descriptionRadioChoice.getText().toString().equals("Build face description file")){
					setPresentContentView(BUILDDESCRIPTION);
				}
				else{
//					CameraDecodeActivity cameraDecodeActivity = new CameraDecodeActivity((Opsis1_1Activity)presentActivity);
					setPresentContentView(QRCODEPROGRESS);
					qrCodeProgressTitleTextView.setText("Shoot QrCode");
					qrCodeProgressInstructionTextView.setText("\n\nWhen you are ready to shoot the qrcode press next and" +
							" put your qrCode in front of the camera, then wait for Opsis to read the code");
//					startActivity(new Intent(presentActivity, CameraDecodeActivity.class));
//					setPresentContentView(SHOOTQRCODE);setPresentContentView(WAIT);
				}
				
			}
		};
		
		buildDescriptionListener = new OnClickListener(){
			public void onClick(View v){
				faceTestName= new String(buildDescriptionLastNameText.getText().toString()
						+ buildDescriptionFirstNameText.getText().toString());
				
				firstName = buildDescriptionFirstNameText.getText().toString();
				lastName = new String (buildDescriptionLastNameText.getText().toString());
				birthPlace = new String (buildDescriptionBirthPlaceText.getText().toString());
				birthDate = new String (buildDescriptionBirthDateText.getText().toString());
				height = new String (buildDescriptionHeightText.getText().toString());
				municipality = new String (buildDescriptionMunicipalityOfIssueText.getText().toString());
				address = new String (buildDescriptionAddressText.getText().toString());
				nationality = new String (buildDescriptionNationalityText.getText().toString());
				fiscalCode = new String (buildDescriptionFiscalCodeText.getText().toString());
				
				
				buildDescriptionSexRadioChoice 
					= (RadioButton) findViewById(buildDescriptionSexRadioGroup.getCheckedRadioButtonId());
				sex = new String (buildDescriptionSexRadioChoice.getText().toString());
				buildDescriptionValidityToTravelRadioChoice 
					= (RadioButton) findViewById(buildDescriptionValidityToTravelRadioGroup.getCheckedRadioButtonId());
				validityToTravel = new String (buildDescriptionValidityToTravelRadioChoice.getText().toString());
				buildDescriptionRadioChoice 
					= (RadioButton) findViewById(buildDescriptionRadioGroup.getCheckedRadioButtonId());
				if(buildDescriptionRadioChoice.getText().equals("take a new picture")){
					setPresentContentView(BUILDDESCRIPTIONPICTURE);
				}
				else{
					imageAlreadyPresent=true;
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(Intent.createChooser(intent, "Complete action using"), USEDBPICTURE);
				}
				
			}
		};
		
		buildDescriptionPictureListener = new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				setPresentContentView(SHOWIMAGE);
				
				
				startCamera(faceTestName, presentContentView);
				shoot();
//				doCrop();
				return true;
			}
		};
		
		show_faceAcceptListener = new OnClickListener() {
			public void onClick(View v) {
				if (! imageAlreadyPresent)
					saveBitmap("Opsis/eigen", faceTestName, dbImage, JPEG);
				
				Opsis opsis = null;
				BiometricIdCard idCard=null;
				OpsisEncoder oe = new OpsisEncoder(presentActivity);
				idCard=getIdCard();
				try {
					opsis = oe.generateOpsis(idCard);
					if (opsis.emptyEncodedIdCard())
						Log.i(TAG,"[ON CLICK LISTENER show face accept] the encodedIdCard field in the opsis is null...");
					else Log.i(TAG,"[ON CLICK LISTENER show face accept]the encodedIdCard field in the opsis is not null...");
					saveOpsis(opsis);
					frontalIdCard = FrontIdCardBuilder.makeIdCardHumanReadable(idCard,dbImage,presentActivity);
					saveBitmap("Opsis/FrontalIdCard", lastName+firstName, frontalIdCard, PNG);
				} catch (InvalidKeyException e) {
					error= new String("[ON CLICK LISTENER show face accept] InvalidKeyException:"+e.toString());
					setPresentContentView(ERROR);
				} catch (NoSuchAlgorithmException e) {
					error= new String("[ON CLICK LISTENER show face accept] NoSuchAlgorithmException:"+e.toString());
					setPresentContentView(ERROR);
				} catch (InvalidKeySpecException e) {
					error= new String("[ON CLICK LISTENER show face accept] InvalidKeySpecException:"+e.toString());
					setPresentContentView(ERROR);
				} catch (NoSuchPaddingException e) {
					error= new String("[ON CLICK LISTENER show face accept] NoSuchPaddingException:"+e.toString());
					setPresentContentView(ERROR);
				} catch (IllegalBlockSizeException e) {
					error= new String("[ON CLICK LISTENER show face accept] IllegalBlockSizeException:"+e.toString());
					setPresentContentView(ERROR);
				} catch (BadPaddingException e) {
					error= new String("[ON CLICK LISTENER show face accept] BadPaddingException:"+e.toString());
					setPresentContentView(ERROR);
				} catch (IOException e) {
					error= new String("[ON CLICK LISTENER show face accept] IOException:"+e.toString());
					setPresentContentView(ERROR);
				}
				
				
//				BiomethricIdCard idCard=null;
//				idCard=new BiomethricIdCard(getIdCard(faceTestName));
//				saveIdCard(idCard); 
				setPresentContentView(SHOWFRONTALIDCARD);
			}
		};
		
		show_faceRejectListener = new OnClickListener() {
			public void onClick(View v) {
				setPresentContentView(BUILDDESCRIPTIONPICTURE);
			}
		};
		
		takePictureToTestListener = new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				setPresentContentView(SHOWPICTURETOTEST);
				if (toTestIdCard==null)
					Log.i(TAG,"[TAKE PICTURE TO TEST LISTENER] toTestIdCard is null...");
				
				startCamera("test", presentContentView);
				shoot();
				return false;
			}
		};
		
		show_toTestFaceAcceptListener = new OnClickListener() {
			public void onClick(View v) {
				
				//do verify
				setPresentContentView(CHECKRESPONSE);
				
				saveBitmap("Opsis/test", toTestIdCard.getLastName(), dbImage, JPEG);
				try {
					Bitmap responseB;
					efc.readFaceBundles("Opsis/eigen");
					if (efc.checkIdCard(toTestIdCard, "Opsis/test/"/*Ema.jpg")){*/+"test"+".jpg")){
						responseB = BitmapFactory.decodeResource(getResources(), R.drawable.accept);
						checkResponseResultImageView.setImageBitmap(responseB);
					}
					else{
						responseB = BitmapFactory.decodeResource(getResources(), R.drawable.reject);
						checkResponseResultImageView.setImageBitmap(responseB);
					}
					Toast.makeText(presentActivity, "Distance:"+efc.testDistance(), Toast.LENGTH_LONG).show();
					checkResponseAcceptButton.setEnabled(true);
					checkResponseRejectButton.setEnabled(true);
				} catch (FileNotFoundException e) {
					error= new String("[START LISTENER SHOW TEST FACE] FileNotFoundException:"+e.toString());
					setPresentContentView(ERROR);
				} catch (IOException e) {
					error= new String("[START LISTENER SHOW TEST FACE] IOException:"+e.toString());
					setPresentContentView(ERROR);
				} catch (ClassNotFoundException e) {
					error= new String("[START LISTENER SHOW TEST FACE] ClassNotFoundException:"+e.toString());
					setPresentContentView(ERROR);
				}
				
				
			}
		};
		
		show_toTestFaceRejectListener = new OnClickListener() {
			public void onClick(View v) {
				setPresentContentView(TAKEPICTURETOTEST);
			}
		};
		
		checkResponseAcceptListener = new OnClickListener() {
			public void onClick(View v) {
				setPresentContentView(DESCRIPTION);
			}
		};
		
		checkResponseRejectListener = new OnClickListener() {
			public void onClick(View v) {
				setPresentContentView(TAKEPICTURETOTEST);
			}
		};
		
		shootQrCodeListener = new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				setPresentContentView(WAIT);
				startCamera("testQRCode", SHOOTQRCODE);
				shoot();
				++shoot;
//				setPresentContentView(TAKEPICTURETOTEST);
				return false;
			}
		};
		
		qrCodeProgressButtonListener = new OnClickListener() {
			public void onClick(View v) {
				Intent intent= new Intent(presentActivity, CameraDecodeActivity.class);
				startActivityForResult(intent,GETOPSISBYTE);
				
    		    qrCodeProgressTitleTextView.setText("Ready to test the identity!");
				qrCodeProgressInstructionTextView.setText("Press next to take a picture of the person you want to identify. " +
						"When you get the face into the green rectangle touch the screen to take the picture to test");
				getQrCodePicture=true;

//				else {
//					
//					setPresentContentView(TAKEPICTURETOTEST);
//				}
			}
			
		};
		
		showIdCardVerifyButtonListener = new OnClickListener() {
			public void onClick(View arg0) {
				setPresentContentView(TAKEPICTURETOTEST);
			}
		};
		
		
		showIdCardBackButtonListener = new OnClickListener() {
			public void onClick(View v) {
				setPresentContentView(DESCRIPTION);
			}
		};
		
		showFrontalIdCardRejectButtonListener = new OnClickListener() {
			public void onClick(View v) {
				setPresentContentView(BUILDDESCRIPTION);
			}
		};
		
		showFrontalIdCardAcceptButtonListener = new OnClickListener() {
			public void onClick(View v) {
				setPresentContentView(DESCRIPTION);
			}
		};
		
		
		
		
		
	}
	
//	public void saveIdCard(BiomethricIdCard idCard){
//		File path = Environment.getExternalStoragePublicDirectory("Opsis/IdCards");
//		path.mkdirs();
//		Log.i(TAG, "[SAVE ID CARD]path:"+path.getAbsolutePath());
//		if (idCard==null)
//			Log.i(TAG, "[SAVE ID CARD]idcard null...");
//		
//		Log.i(TAG, "[SAVE ID CARD]last name:"+idCard.getLastName());
//		
//		File dst = new File(path,idCard.getLastName()+".opsis");
//		try {
//			dst.createNewFile();
//			FileOutputStream out = new FileOutputStream(dst.getAbsolutePath());
//			ObjectOutputStream fos = new ObjectOutputStream(out);
//			fos.writeObject(idCard);
//			fos.close();
//		} catch (IOException e) {
//			error= new String("[SAVE ID CARD] IOException:"+e.toString());
//			presentContentView=ERROR;
//			updateContentView();
//		}
//	}
	
	public void setPresentContentView(int contentView){
		presentContentView = contentView;
		updateContentView();
	}
	
	public void saveOpsis (Opsis opsis){
		File path = Environment.getExternalStoragePublicDirectory("Opsis/IdCards");
//		BitMatrix bm = null;
		path.mkdirs();
		Log.i(TAG, "[SAVE OPSIS]path:"+path.getAbsolutePath());
		if (opsis==null)
			Log.i(TAG, "[SAVE OPSIS]opsis null...");
		else if (opsis.emptyEncodedIdCard())
			Log.i(TAG,"[SAVE OPSIS]opsis ecoded field null...");
		Log.i(TAG, "[SAVE OPSIS]card name:"+lastName+firstName);
		
//		File dst = new File(path,opsis.getLastName()+".opsis");
		
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream obos = new ObjectOutputStream(bout);
			obos.writeObject(opsis);
			obos.close();
			
//			int splitNum=4;
//			int byteStreamLength=bout.toByteArray().length;
//			int splitSize = byteStreamLength/splitNum;
			
//			int curr=0;
//			for (int i=0; i< splitNum; ++i){
//				byte [] tmp=null;
//				if (i<(splitNum-1)){
//					tmp= new byte [splitSize];
//					System.arraycopy(bout.toByteArray(), curr, tmp, 0, splitSize);
//				}
//				else{
//					tmp= new byte [byteStreamLength-curr];
//					System.arraycopy(bout.toByteArray(), curr, tmp, 0, byteStreamLength-curr);
//				}
//				curr+=splitSize;
//			
//				File dst = new File (path, opsis.getLastName()+"_"+i+".png");
//				dst.createNewFile();
//				FileOutputStream out = new FileOutputStream(dst.getAbsolutePath());
//				Log.i(TAG,"[SAVE OPSIS]printing QrCode to " + dst.getAbsolutePath());
//				Bitmap qrCode = QrCodeEncoder.createQrCode(tmp);
//				qrCode.compress(Bitmap.CompressFormat.PNG, 100, out);
//			}
			
			
			File dst = new File (path, lastName+firstName+".png");
			dst.createNewFile();
			FileOutputStream out = new FileOutputStream(dst.getAbsolutePath());
			Log.i(TAG,"[SAVE OPSIS]printing QrCode to " + dst.getAbsolutePath());
			if (bout.size()<10)
				Log.i(TAG, "[SAVE OPSIS]bout empty");
			Bitmap bm = QrCodeEncoder.createQrCode(bout.toByteArray());
			if (bm==null)
				Log.i(TAG, "[SAVE OPSIS]bm null");
			bm.compress(Bitmap.CompressFormat.PNG, 100, out);
//			String opsisToString = QrCodeEncoder.createQrCode(bout.toByteArray());
//			String opsisToString = new String(bout.toByteArray(), "ISO-8859-1");
			
			
			
//			Intent qrCodeIntent = new Intent();
//			qrCodeIntent.setType("la.droid.qr.encode");
//			qrCodeIntent.putExtra("la.droid.qr.code", opsisToString);
//			qrCodeIntent.putExtra( "la.droid.qr.size" , 1500);
//			qrCodeIntent.putExtra( "la.droid.qr.image" , true);
//			intent.setAction(Intent.ACTION_GET_CONTENT);
//			startActivityForResult(Intent.createChooser(qrCodeIntent, "Complete action using"), CREATEQRCODE);
//			bm.
//			MatrixToImageWriter.writeToFile(bm, "PNG", dst);
			Log.i(TAG,"[SAVE OPSIS]QrCode printed...");
	        
			
		} catch (IOException e) {
			Log.i(TAG,"[SAVE OPSIS] IOException:"+e.toString());
			error= new String("[SAVE OPSIS] IOException:"+e.toString());
			setPresentContentView(ERROR);	
		} catch (WriterException e) {
			Log.i(TAG,"[SAVE OPSIS] WriterException:"+e.toString());
			error= new String("[SAVE OPSIS] WriterException:"+e.toString());
			setPresentContentView(ERROR);
		} 
	}
	
	
//	public BiomethricIdCard readIdCard(Uri idCardUri){
//		String temp = idCardUri.toString();
//		temp= temp.substring(temp.lastIndexOf('/'));
//		temp= temp.substring(1);
//		File fPath = Environment.getExternalStoragePublicDirectory("Opsis/IdCards");
//		File f=new File(fPath,temp);
//		FileInputStream in;
//		BiomethricIdCard idCard=null;
//		try {
//			in = new FileInputStream(f);
//	    ObjectInputStream fo = new ObjectInputStream(in);
//	    idCard = (BiomethricIdCard)fo.readObject();
//	    fo.close();
//		} catch (FileNotFoundException e) {
//			error= new String("[READ ID CARD] fileNotFoundException:"+e.toString());
//			presentContentView=ERROR;
//			updateContentView();
//		} catch (StreamCorruptedException e) {
//			error= new String("[READ ID CARD] StreamCorruptedException:"+e.toString());
//			presentContentView=ERROR;
//			updateContentView();
//		} catch (IOException e) {
//			error= new String("[READ ID CARD] IOException:"+e.toString());
//			presentContentView=ERROR;
//			updateContentView();
//		} catch (ClassNotFoundException e) {
//			error= new String("[READ ID CARD] ClassNotFoundException:"+e.toString());
//			presentContentView=ERROR;
//			updateContentView();
//		}
//		return idCard;
//	}
	
	

	
	public Opsis readOpsis(Uri opsisUri){
		String temp = opsisUri.toString();
		temp= temp.substring(temp.lastIndexOf('/'));
		temp= temp.substring(1);
		File fPath = Environment.getExternalStoragePublicDirectory("Opsis/IdCards");
		File f=new File(fPath,temp);
		Log.i(TAG,"[READ OPSIS] opsis file dir:"+f.getAbsolutePath());
		FileInputStream in;
		 Opsis opsis = null;
		try {
			in = new FileInputStream(f);
	    ObjectInputStream fo = new ObjectInputStream(in);
	    opsis = (Opsis)fo.readObject();
	    if (opsis==null)
			Log.i(TAG,"[READ OPSIS] opsis null...");
	    else
	    	Log.i(TAG,"[READ OPSIS] opsis not null...");
	    fo.close();
	    return opsis;
		} catch (FileNotFoundException e) {
			Log.i(TAG,"[READ OPSIS] fileNotFoundException:"+e.toString());
			error= new String("[READ OPSIS] fileNotFoundException:"+e.toString());
			presentContentView=ERROR;
			updateContentView();
		} catch (StreamCorruptedException e) {
			Log.i(TAG,"[READ OPSIS] StreamCorruptedException:"+e.toString());
			error= new String("[READ OPSIS] StreamCorruptedException:"+e.toString());
			presentContentView=ERROR;
			updateContentView();
		} catch (IOException e) {
			Log.i(TAG,"[READ OPSIS] IOException:"+e.toString());
			error= new String("[READ OPSIS] IOException:"+e.toString());
			presentContentView=ERROR;
			updateContentView();
		} catch (ClassNotFoundException e) {
			Log.i(TAG,"[READ OPSIS] ClassNotFoundException:"+e.toString());
			error= new String("[READ OPSIS] ClassNotFoundException:"+e.toString());
			presentContentView=ERROR;
			updateContentView();
		}
		Log.i(TAG,"[READ OPSIS] a null opsis has been returned...");
		return null;
	}
	
	
	public BiometricIdCard getIdCard() {
		BiometricIdCard idCard;
		double [] eigenVector;
		int bundleId;
		
		String result;
		try {
			Log.i(TAG, "[GET ID CARD]Reading faceBundles...");
			efc.readFaceBundles("Opsis/eigen");
			Log.i(TAG, "[GET ID CARD]Testing file:"+"Opsis/eigen/"+faceTestName+".jpg");
			result = new String(efc.checkAgainst("Opsis/eigen/"+faceTestName+".jpg"));
			Log.i(TAG, "[GET ID CARD]The result image from "+"Opsis/eigen/"+faceTestName+".jpg"+" is:"+result);
			Toast.makeText(this, "The result image is:"+result+" with distance:"+efc.DISTANCE, Toast.LENGTH_LONG).show();
			Log.i(TAG, "[GET ID CARD]create eigenVector...");
			eigenVector= new double [efc.getToCreateFaceDim()];
			Log.i(TAG, "[GET ID CARD]filling eigenVector...");
			efc.getToCreateFace(eigenVector);
			if (eigenVector==null)
				Log.i(TAG, "[GET ID CARD]eigenvector null...");
			Log.i(TAG, "[GET ID CARD]getting bundle id...");
			bundleId=efc.getBundleId();
			Log.i(TAG, "[GET ID CARD]bundle id:"+bundleId);
			idCard = new BiometricIdCard(firstName, lastName, birthPlace, birthDate, sex, height, 
					municipality, address, nationality, fiscalCode, validityToTravel, eigenVector, bundleId);
			Log.i(TAG, "[GET ID CARD]success!");
			return idCard;
		} 
		catch (FileNotFoundException e) {
			Log.i(TAG, "[GET ID CARD] FileNotFoundException:"+e.toString());
			error= new String("[GET ID CARD] FileNotFoundException:"+e.toString());
			presentContentView=ERROR;
			updateContentView();
			return null;
		} 
		catch (IOException e) {
			Log.i(TAG, "[GET ID CARD] IOException:"+e.toString());
			error= new String("[GET ID CARD] IOException:"+e.toString());
			presentContentView=ERROR;
			updateContentView();
			return null;
		} catch (IllegalArgumentException e) {
			Log.i(TAG, "[GET ID CARD] IllegalArgumentException:"+e.toString());
			error= new String("[GET ID CARD] IllegalArgumentException:"+e.toString());
			presentContentView=ERROR;
			updateContentView();
			return null;
		} catch (ClassNotFoundException e) {
			Log.i(TAG, "[GET ID CARD] ClassNotFoundException:"+e.toString());
			error= new String("[GET ID CARD] ClassNotFoundException:"+e.toString());
			presentContentView=ERROR;
			updateContentView();
			return null;
		}
		catch (Exception e) {
			Log.i(TAG, "[GET ID CARD] Exception:"+e.toString());
			error= new String("[GET ID CARD] Exception:"+e.toString());
			presentContentView=ERROR;
			updateContentView();
			return null;
		}
		
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
        mItemQuit = menu.add("Quit");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == mItemQuit)
            terminate();
        return true;
    }
    
    public void terminate()
    {
       super.onDestroy();
       System.runFinalizersOnExit(true);
       System.exit(0);
    }
	
    

    public void startCamera(String fileName, int view){
    	if (mCamera!=null){
    		mCamera.stopPreview();
    		mCamera.release();
    		mCamera=null;
    	}
        mCamera = Camera.open();
        Camera.Parameters params = mCamera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
		List <int[]> supportedFpsRange = params.getSupportedPreviewFpsRange();
		int [] tmp= {Integer.MAX_VALUE,Integer.MAX_VALUE};
		for (int i=0; i<supportedFpsRange.size();++i)
			if (tmp[1]>supportedFpsRange.get(i)[1])
				tmp = supportedFpsRange.get(i);
		
		Log.i(TAG,"[START CAMERA] The minimum framerate range is between "+tmp[0]+" and "+tmp[1]+" FpS");
		params.setPreviewFpsRange(tmp[0], tmp[1]);
		
		if (view==SHOOTQRCODE){
			params.setPreviewSize(800, 800);
			params.setPictureSize(800, 800);
			mCamera.setParameters(params);
			mCamera.startPreview();
			setQRCodeCameraCallback();
		}
		else{
			mCamera.setParameters(params);
			mCamera.startPreview();
			setCameraCallback(fileName, view);
		}
		
		
    }
    
    

     
    
    
    public void setQRCodeCameraCallback(){
//    	Activity activity = this;
    	jpegCallback = new Camera.PictureCallback() {
			
			public void onPictureTaken(byte[] data, Camera camera) {
				File path = Environment.getExternalStoragePublicDirectory(
		            "Opsis/QRCodeTest/");
		    File file = new File(path, "testQRCode_"+shoot+".jpg");
		    if (file.exists()) file.delete();
			
			FileOutputStream outStream; 
			try {
				path.mkdirs();
				outStream= new FileOutputStream(file);	
				outStream.write(data);
				outStream.flush();
				outStream.close();
				
				if (mCamera!=null){
		    		mCamera.stopPreview();
		    		mCamera.release();
		    		mCamera=null;
		    	}
				
				fileDir = new String (file.getAbsoluteFile().toString());
				fileDir = fileDir.substring(4);
				Toast.makeText(presentActivity, "The QR-Code image has been successfully written in "
						+fileDir, Toast.LENGTH_LONG).show();
				
				if (shoot==0)
					decodedBytes= new byte [SHOOTNUM][0];
				decodedBytes[shoot]=QrCodeDecoder.decodeQRCode();
				
				
//				if (opsis==null){
//					Log.i(TAG,"[SET QR CODE CAMERA CALLBACK] Opsis null...");
//					error= new String ("[SET QR CODE CAMERA CALLBACK] Opsis null...");
//					setPresentContentView(ERROR);
//					return;
//				}
//				OpsisDecoder od = new OpsisDecoder(presentActivity);
//				toTestIdCard = od.opsisToBiometricIdCard(opsis);
//				setPresentContentView(TAKEPICTURETOTEST);
			}catch(Exception e){
				Log.i(TAG,"[SET QR CODE CAMERA CALLBACK]Exception "+e.toString());
				error= new String ("[SET QR CODE CAMERA CALLBACK] FileNotFoundException "+e.toString());
				setPresentContentView(ERROR);
			}
//			catch (FileNotFoundException e) {
////					Toast.makeText(presentActivity, "Exception:"+e.toString(), Toast.LENGTH_SHORT).show();
//					Log.i(TAG,"[SET QR CODE CAMERA CALLBACK]FileNotFoundException "+e.toString());
//					error= new String ("[SET QR CODE CAMERA CALLBACK] FileNotFoundException "+e.toString());
//					setPresentContentView(ERROR);
//				} catch (IOException e) {
//					Log.i(TAG,"[SET QR CODE CAMERA CALLBACK]IOException "+e.toString());
//					error= new String ("[SET QR CODE CAMERA CALLBACK] IOException "+e.toString());
//					setPresentContentView(ERROR);
//				} catch (ClassNotFoundException e) {
//					Log.i(TAG,"[SET QR CODE CAMERA CALLBACK]ClassNotFoundException "+e.toString());
//					error= new String ("[SET QR CODE CAMERA CALLBACK] ClassNotFoundException "+e.toString());
//					setPresentContentView(ERROR);
//				} catch (NotFoundException e) {
//					Log.i(TAG,"[SET QR CODE CAMERA CALLBACK]NotFoundException "+e.toString());
//					error= new String ("[SET QR CODE CAMERA CALLBACK] NotFoundException "+e.toString());
//					setPresentContentView(ERROR);
//				} catch (ChecksumException e) {
//					Log.i(TAG,"[SET QR CODE CAMERA CALLBACK]ChecksumException "+e.toString());
//					error= new String ("[SET QR CODE CAMERA CALLBACK] ChecksumException "+e.toString());
//					setPresentContentView(ERROR);
//				} catch (FormatException e) {
//					Log.i(TAG,"[SET QR CODE CAMERA CALLBACK]FormatException "+e.toString());
//					error= new String ("[SET QR CODE CAMERA CALLBACK] FormatException "+e.toString());
//					setPresentContentView(ERROR);
//				} finally {
//				}
			}
		};
    }
    

    public void setCameraCallback(final String fileName, final int view){
    	final Activity presentActivity=this;
   	 jpegCallback = new Camera.PictureCallback() {
   			public void onPictureTaken(byte[] data, Camera camera) {    						    
   			    File path = Environment.getExternalStoragePublicDirectory(
   			            "Opsis/test/");
   			    File file = new File(path, fileName+".jpg");
   			    if (file.exists()) file.delete();
   				
   				FileOutputStream outStream; 
   				try {
   					path.mkdirs();
   					outStream= new FileOutputStream(file);	
   					outStream.write(data);
   					outStream.flush();
   					outStream.close();
   					
   					if (mCamera!=null){
   			    		mCamera.stopPreview();
   			    		mCamera.release();
   			    		mCamera=null;
   			    	}
   					
   					fileDir = new String (file.getAbsoluteFile().toString());
   					fileDir = fileDir.substring(4);
   					Toast.makeText(presentActivity, "The face of "+fileName+" has been successfully written in "
   							+fileDir, Toast.LENGTH_LONG).show();
   					
   					
   					
   					
   					doCrop();
   					if (view==SHOWIMAGE){
   	   					presentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
   	   					show_faceImageView.setImageBitmap(toDisplayBmp);
   	   					show_faceAcceptButton.setEnabled(true);
   	   					show_faceRejectButton.setEnabled(true);
   					}
   					else{
   	   					presentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
   	   					show_toTestFaceImageView.setImageBitmap(toDisplayBmp);
   	   					show_toTestFaceAcceptButton.setEnabled(true);
   	   					show_toTestFaceRejectButton.setEnabled(true);
   					}
					
   				} catch (FileNotFoundException e) {
   					Toast.makeText(presentActivity, "Exception:"+e.toString(), Toast.LENGTH_SHORT).show();
   					e.printStackTrace();
   				} catch (IOException e) {
   					Toast.makeText(presentActivity, "Exception:"+e.toString(), Toast.LENGTH_SHORT).show();
   					e.printStackTrace();
   				} finally {
   				}
   			}
   		};
    }
    
    private void doCrop(){
    	SubImageMaker sim = new SubImageMaker(fileDir, this);
    	dbImage = Bitmap.createBitmap(sim.getFaceForDatabase());
    	toDisplayBmp = Bitmap.createBitmap(dbImage);
    }
    
    
    
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	switch(requestCode) {
//        case(PICKOPSISCARD):
//        	opsisUri=data.getData();
//        	Log.i(TAG, "[ON ACTIVITY RESULT]opsis uri:"+opsisUri.toString());
//        	Opsis opsis=QrCodeDecoder.decodeQRCode();
//        	if (opsis==null)
//        		Log.i(TAG, "[ON ACTIVITY RESULT]opsis null...");
//        	else
//        		Log.i(TAG, "[ON ACTIVITY RESULT]opsis not null...");
//        	OpsisDecoder od = new OpsisDecoder(presentActivity);
//        	try {
//				toTestIdCard = od.opsisToBiometricIdCard(opsis);
//				presentContentView=TAKEPICTURETOTEST;
//	        	updateContentView();
//			} catch (StreamCorruptedException e) {
//				Log.i(TAG,"[ON ACTIVITY RESULT] StreamCorruptedException:"+e.toString());
//				error= new String("[ON ACTIVITY RESULT] StreamCorruptedException:"+e.toString());
//				presentContentView=ERROR;
//				updateContentView();
//			} catch (IOException e) {
//				Log.i(TAG,"[ON ACTIVITY RESULT] IOException:"+e.toString());
//				error= new String("[ON ACTIVITY RESULT] IOException:"+e.toString());
//				presentContentView=ERROR;
//				updateContentView();
//			} catch (ClassNotFoundException e) {
//				Log.i(TAG,"[ON ACTIVITY RESULT] ClassNotFoundException:"+e.toString());
//				error= new String("[ON ACTIVITY RESULT] ClassNotFoundException:"+e.toString());
//				presentContentView=ERROR;
//				updateContentView();
//			}
//        	if (toTestIdCard==null)
//        		Log.i(TAG, "[ON ACTIVITY RESULT]testtoidcard null");
//        	else 
//        		Log.i(TAG, "[ON ACTIVITY RESULT]testtoidcard not null");
//        	
//        	
//        break;
    	
    	case(GETOPSISBYTE):
    		byte[] byteResult;
			try {
				byteResult = data.getExtras().getString("bytes").getBytes("ISO-8859-1");
			} catch (UnsupportedEncodingException e) {
				Log.i(TAG,"[ON ACTIVITY RESULT-GETOPSYSBYTE]UnsupportedEncodingException:"+e.toString());
		    	error= new String ("[ON ACTIVITY RESULT-GETOPSYSBYTE]UnsupportedEncodingException:"+e.toString());
		    	setPresentContentView(ERROR);
		    	return;
			}
			if (byteResult==null)
				Log.i(TAG,"[ON ACTIVITY RESULT-GETOPSYSBYTE]byteresult null");
    		setOpsisBytes(byteResult);
    		
    		ByteArrayInputStream bis = new ByteArrayInputStream (opsisBytes);
		    ObjectInputStream ois;
		    
			try {
				ois = new ObjectInputStream (bis);
			} catch (StreamCorruptedException e) {
				Log.i(TAG,"[START LISTENERS - QRCODEPROGRESS]StreamCorruptedException:"+e.toString());
		    	error= new String ("[START LISTENERS - QRCODEPROGRESS]StreamCorruptedException:"+e.toString());
		    	setPresentContentView(ERROR);
		    	return;
			} catch (IOException e) {
				Log.i(TAG,"[START LISTENERS - QRCODEPROGRESS]IOException:"+e.toString());
		    	error= new String ("[START LISTENERS - QRCODEPROGRESS]IOException:"+e.toString());
		    	setPresentContentView(ERROR);
		    	return;
			}
			
		    Opsis opsis;
			try {
				opsis = (Opsis)ois.readObject();
			} catch (OptionalDataException e) {
				Log.i(TAG,"[START LISTENERS - QRCODEPROGRESS]OptionalDataException:"+e.toString());
		    	error= new String ("[START LISTENERS - QRCODEPROGRESS]IOException:"+e.toString());
		    	setPresentContentView(ERROR);
		    	return;
			} catch (ClassNotFoundException e) {
				Log.i(TAG,"[START LISTENERS - QRCODEPROGRESS]ClassNotFoundException:"+e.toString());
		    	error= new String ("[START LISTENERS - QRCODEPROGRESS]ClassNotFoundException:"+e.toString());
		    	setPresentContentView(ERROR);
		    	return;
			} catch (IOException e) {
				Log.i(TAG,"[START LISTENERS - QRCODEPROGRESS]IOException:"+e.toString());
		    	error= new String ("[START LISTENERS - QRCODEPROGRESS]IOException:"+e.toString());
		    	setPresentContentView(ERROR);
		    	return;
			}
		    if (opsis==null){
		    	Log.i(TAG,"[START LISTENERS - QRCODEPROGRESS]opsis null");
		    	error= new String ("[START LISTENERS - QRCODEPROGRESS]opsis null");
		    	setPresentContentView(ERROR);
		    	return;
		    }
		    
		    OpsisDecoder od = new OpsisDecoder(presentActivity);
		    try {
				toTestIdCard=od.opsisToBiometricIdCard(opsis);
			} catch (StreamCorruptedException e) {
				Log.i(TAG,"[START LISTENERS - QRCODEPROGRESS]StreamCorruptedException:"+e.toString());
		    	error= new String ("[START LISTENERS - QRCODEPROGRESS]StreamCorruptedException:"+e.toString());
		    	setPresentContentView(ERROR);
		    	return;
			} catch (IOException e) {
				Log.i(TAG,"[START LISTENERS - QRCODEPROGRESS]IOException:"+e.toString());
		    	error= new String ("[START LISTENERS - QRCODEPROGRESS]IOException:"+e.toString());
		    	setPresentContentView(ERROR);
		    	return;
			} catch (ClassNotFoundException e) {
				Log.i(TAG,"[START LISTENERS - QRCODEPROGRESS]ClassNotFoundException:"+e.toString());
		    	error= new String ("[START LISTENERS - QRCODEPROGRESS]ClassNotFoundException:"+e.toString());
		    	setPresentContentView(ERROR);
		    	return;
			}
//			kkk mostra identità
			setPresentContentView(SHOWIDCARD);
			showIdCardFirstNameTextView.setText(toTestIdCard.getFirstName());
			showIdCardLastNameTextView.setText(toTestIdCard.getLastName());
			showIdCardBirthPlaceTextView.setText(toTestIdCard.getBirthPlace());
			showIdCardBirthDateTextView.setText(toTestIdCard.getBirthDate());
			showIdCardSexTextView.setText(toTestIdCard.getSex());
			showIdCardHeightTextView.setText(toTestIdCard.getHeight());
			showIdCardMunicipalityOfResidenceTextView.setText(toTestIdCard.getMunicipalityOfResidence());
			showIdCardSubMunicipalityOfResidenceTextView.setText(""+toTestIdCard.getIssuingSubMunicipality());
			showIdCardAddressTextView.setText(toTestIdCard.getAddress());
			showIdCardIssuingDateTextView.setText(toTestIdCard.getIssuingDate());
			showIdCardExpirationDateTextView.setText(toTestIdCard.getExpirationDate());
			showIdCardNationalityTextView.setText(toTestIdCard.getNationality());
			showIdCardFiscalCodeTextView.setText(toTestIdCard.getFiscalCode());
			showIdCardValidityToTravelTextView.setText(toTestIdCard.getValidityToTravel());
//    		if (shoot>=SHOOTNUM){
//    			setPresentContentView(WAIT);
//    			ByteArrayInputStream bis = new ByteArrayInputStream (opsisBytes);
//    		    ObjectInputStream ois;
//    		    
//				try {
//					ois = new ObjectInputStream (bis);
//				} catch (StreamCorruptedException e) {
//					Log.i(TAG,"[ON ACTIVITY RESULT-GETOPSYSBYTE]StreamCorruptedException:"+e.toString());
//			    	error= new String ("[ON ACTIVITY RESULT-GETOPSYSBYTE]StreamCorruptedException:"+e.toString());
//			    	setPresentContentView(ERROR);
//			    	return;
//				} catch (IOException e) {
//					Log.i(TAG,"[ON ACTIVITY RESULT-GETOPSYSBYTE]IOException:"+e.toString());
//			    	error= new String ("[ON ACTIVITY RESULT-GETOPSYSBYTE]IOException:"+e.toString());
//			    	setPresentContentView(ERROR);
//			    	return;
//				}
//				
//    		    Opsis opsis;
//				try {
//					opsis = (Opsis)ois.readObject();
//				} catch (OptionalDataException e) {
//					Log.i(TAG,"[ON ACTIVITY RESULT-GETOPSYSBYTE]OptionalDataException:"+e.toString());
//			    	error= new String ("[ON ACTIVITY RESULT-GETOPSYSBYTE]IOException:"+e.toString());
//			    	setPresentContentView(ERROR);
//			    	return;
//				} catch (ClassNotFoundException e) {
//					Log.i(TAG,"[ON ACTIVITY RESULT-GETOPSYSBYTE]ClassNotFoundException:"+e.toString());
//			    	error= new String ("[ON ACTIVITY RESULT-GETOPSYSBYTE]ClassNotFoundException:"+e.toString());
//			    	setPresentContentView(ERROR);
//			    	return;
//				} catch (IOException e) {
//					Log.i(TAG,"[ON ACTIVITY RESULT-GETOPSYSBYTE]IOException:"+e.toString());
//			    	error= new String ("[ON ACTIVITY RESULT-GETOPSYSBYTE]IOException:"+e.toString());
//			    	setPresentContentView(ERROR);
//			    	return;
//				}
//    		    if (opsis==null){
//    		    	Log.i(TAG,"[ON ACTIVITY RESULT-GETOPSYSBYTE]opsis null");
//    		    	error= new String ("[ON ACTIVITY RESULT-GETOPSYSBYTE]opsis null");
//    		    	setPresentContentView(ERROR);
//    		    	return;
//    		    }
//    		    
//    		    OpsisDecoder od = new OpsisDecoder(presentActivity);
//    		    try {
//					toTestIdCard=od.opsisToBiometricIdCard(opsis);
//				} catch (StreamCorruptedException e) {
//					Log.i(TAG,"[ON ACTIVITY RESULT-GETOPSYSBYTE]StreamCorruptedException:"+e.toString());
//			    	error= new String ("[ON ACTIVITY RESULT-GETOPSYSBYTE]StreamCorruptedException:"+e.toString());
//			    	setPresentContentView(ERROR);
//			    	return;
//				} catch (IOException e) {
//					Log.i(TAG,"[ON ACTIVITY RESULT-GETOPSYSBYTE]IOException:"+e.toString());
//			    	error= new String ("[ON ACTIVITY RESULT-GETOPSYSBYTE]IOException:"+e.toString());
//			    	setPresentContentView(ERROR);
//			    	return;
//				} catch (ClassNotFoundException e) {
//					Log.i(TAG,"[ON ACTIVITY RESULT-GETOPSYSBYTE]ClassNotFoundException:"+e.toString());
//			    	error= new String ("[ON ACTIVITY RESULT-GETOPSYSBYTE]ClassNotFoundException:"+e.toString());
//			    	setPresentContentView(ERROR);
//			    	return;
//				}
//				
//    		    setPresentContentView(QRCODEPROGRESS);
//    		    qrCodeProgressTitleTextView.setText("Ready to test the identity!");
//				qrCodeProgressInstructionTextView.setText("Press next to take a picture of the person you want to identify. " +
//						"When you get the face into the green rectangle touch the screen to take the picture to test");
//    		
    		    
//    		}
//    		else {
//    			qrCodeProgressTitleTextView.setText("Shoot QrCode \t\t step "+(shoot+1));
//				qrCodeProgressInstructionTextView.setText("When you are ready to shoot the qrcode"+(shoot+1)+" press next and" +
//						" put your qrCode in front of the camera, then wait for Opsis to read the code");
//    		}
//    		++shoot;
    	break;
    	
    	case(CREATEQRCODE):
    		String result = data.getExtras().getString("la.droid.qr.result");
    		Toast.makeText(presentActivity, "qr code written in:"+result, Toast.LENGTH_LONG);
    	break;
        
        case(USEDBPICTURE):
        	Uri toUsePictureUri = data.getData();
        	String s = new String(getRealPathFromURI(toUsePictureUri));
        	Log.i(TAG, "[ON ACTIVITY RESULT]image uri:"+s);
        	String temp = s.substring(s.lastIndexOf('/'));
        	temp= temp.substring(1,temp.length()-4);
        	Log.i(TAG, "[ON ACTIVITY RESULT]face name:"+temp);
        	faceTestName= new String(temp);
        	dbImage = BitmapFactory.decodeFile(s);
        	toDisplayBmp = BitmapFactory.decodeFile(s);
        	setPresentContentView(SHOWIMAGE);
        	show_faceImageView.setImageBitmap(toDisplayBmp);
			show_faceAcceptButton.setEnabled(true);
			show_faceRejectButton.setEnabled(true);
        	
        break;
        }
    }
    
    public String getRealPathFromURI(Uri contentUri) {

        // can post image
        String [] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery( contentUri,
                        proj, // Which columns to return
                        null,       // WHERE clause; which rows to return (all rows)
                        null,       // WHERE clause selection arguments (none)
                        null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
}
    
    private void saveBitmap (String path,String filename, Bitmap bmp, int format){
    	
    	File pathFile = Environment.getExternalStoragePublicDirectory(path);
    	pathFile.mkdirs();
    	File file;
    	if (format==JPEG)
    		file = new File(pathFile, filename+".jpg");
    	else 
    		file = new File(pathFile, filename+".png");
    	
    	try {
    	       FileOutputStream out = new FileOutputStream(file);
    	       if (format==JPEG)
    	    	   bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
    	       else
    	    	   bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
    	} catch (Exception e) {
    			presentContentView=ERROR;
    			updateContentView();
    	}
    }
    
    
     public void shoot(){
 		Camera.AutoFocusCallback focusCB= new Camera.AutoFocusCallback() {
 			@Override
 			public void onAutoFocus(boolean success, Camera camera) {}
 		};
 		mCamera.autoFocus(focusCB);
		mCamera.takePicture(null, null, jpegCallback);
		
 	 }
     
     
     public void setOpsisBytes(byte [] b){
    	 opsisBytes= new byte [b.length];
    	 System.arraycopy(b, 0, opsisBytes, 0, b.length);
     }
     
}