package kniz.main_pack.decode;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import kniz.main_pack.BiometricIdCard;
import kniz.main_pack.Opsis;
import kniz.main_pack.R;
import android.content.Context;
import android.util.Log;

public class OpsisDecoder {
	
	private static final String TAG = new String ("Opsis::decode.decoder");

	private Context presentContext;
	private byte [] toDecode;
	private ByteArrayOutputStream decoded;
	private int issuingMunicipalityId;
	private int issuingSubMunicipalityId;

	public OpsisDecoder (Context context){
		presentContext = context;
	}
	
	public BiometricIdCard opsisToBiometricIdCard(Opsis opsisToRead) throws StreamCorruptedException, IOException, ClassNotFoundException{
		toDecode=null;
		BiometricIdCard retval;
		issuingMunicipalityId = opsisToRead.getIssuingMunicipalityId();
		issuingSubMunicipalityId = opsisToRead.getIssuingSubMunicipalityId();
		toDecode = opsisToRead.getEncodedIdCard().toByteArray();
		if (toDecode == null)
			Log.i(TAG, "[OPSIS TO BIOMETRIC ID CARD]to decode data field in the opsis is null..." );
		ByteArrayInputStream bais = new ByteArrayInputStream(toDecode);
		ObjectInputStream ois = new ObjectInputStream(bais);
		retval = (BiometricIdCard) ois.readObject();
		ois.close();
		bais.close();
		if (retval == null)
			Log.i(TAG, "[OPSIS TO BIOMETRIC ID CARD]return idcard is null..." );
		return retval;
		
	}
	
	public void decode() throws NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidKeyException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException{
		// read X509 coded public key
		InputStream fis;
		if(issuingMunicipalityId==BiometricIdCard.ROMA)
			fis = presentContext.getResources().openRawResource(R.raw.opsis_pub_key_0000_00);
		else if (issuingMunicipalityId==BiometricIdCard.MILANO)
			fis = presentContext.getResources().openRawResource(R.raw.opsis_pub_key_0001_00);
		else
			fis = presentContext.getResources().openRawResource(R.raw.opsis_pub_key_0002_00);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        fis.close();
        
        byte[] publicKeyBytes = baos.toByteArray();
        baos.close();
        
        //from X509 public key conversion
        X509EncodedKeySpec ks = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(ks);
       
       
        byte[] codFile;
        codFile = baos.toByteArray();
        // DECODIFICA
        Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        c.init(Cipher.DECRYPT_MODE, publicKey);
        
        int pieces = (baos.size()/128);
        int remainder = baos.size()%128;
              
        byte[] encodeFile = null;
        
        
        if(pieces == 1){
        	
        	byte[] lastpiece = new byte[remainder];
        	for(int j=0; j<128; j++)
                lastpiece[j] = codFile[j];
         
            encodeFile = c.doFinal(lastpiece);

        }
        
        if(pieces > 1){
        	
            byte[] filepiece = new byte[128];
        	
        	for(int k=0; k<pieces; k++){

        			for(int j=0; j<128; j++)
        				filepiece[j] = codFile[j+k*128];
        
        			encodeFile = c.doFinal(filepiece);
            }

        }
        decoded = new ByteArrayOutputStream(encodeFile.length);
        decoded.write(encodeFile);
	}

}
