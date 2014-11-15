package kniz.main_pack.encode;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
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

import android.content.Context;
import android.os.Environment;

import kniz.main_pack.BiometricIdCard;
import kniz.main_pack.Opsis;
import kniz.main_pack.R;

public class OpsisEncoder {

	private Context presentContext;
	private ByteArrayOutputStream bioIdCardStream;
	private ByteArrayOutputStream opsisStream;
	private int issuingMunicipalityId;
	private int issuingSubMunicipality;
	
	public OpsisEncoder(Context context) {
		presentContext = context;
	}
	
	
	public Opsis generateOpsis(BiometricIdCard idCard) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		Opsis retval = null;
		issuingMunicipalityId = idCard.getIssuingMunicipality();
		issuingSubMunicipality = idCard.getIssuingSubMunicipality();
		String lastName = idCard.getLastName();
		bioIdCardStream = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bioIdCardStream);
		oos.writeObject(idCard);
		oos.close();
		encode();

		retval= new Opsis();
		retval.setEncodedIdCard(bioIdCardStream);
		retval.setIssuingMunicipalityId(issuingMunicipalityId);
		retval.setIssuingSubMunicipalityId(issuingSubMunicipality);
		retval.setLastName(lastName);
		
		return retval;
	}
	
	private void encode() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException  {
		InputStream fis;
		if (issuingMunicipalityId==BiometricIdCard.ROMA)
        	 fis = presentContext.getResources().openRawResource(R.raw.opsis_priv_key_0000_00);
		else if (issuingMunicipalityId==BiometricIdCard.MILANO)
			fis = presentContext.getResources().openRawResource(R.raw.opsis_priv_key_0001_00);
		else
			fis = presentContext.getResources().openRawResource(R.raw.opsis_priv_key_0002_00);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        fis.close();
        
        byte[] privateKeyBytes = baos.toByteArray();
        baos.close();
       
        // from PKCS8 private key conversion
       
        PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(ks);
       
        
        byte[] plainFile;


        plainFile = bioIdCardStream.toByteArray();
        
        
        int pieces = (baos.size()/373)+1;
        int rest = baos.size()%373;
              
        //encode src bytes
        // initialize cifer using RSA algorithm, ECB mode and PKCS1 padding
        Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        c.init(Cipher.ENCRYPT_MODE, privateKey);
       
//        File path = Environment.getExternalStoragePublicDirectory("Opsis/IdCards");
//        path.mkdirs();
//     	FileOutputStream fos = new FileOutputStream(destPath+lastName+".opsis");
     	byte[] encodeFile;
     	
        if(pieces == 1){
        	
        	byte[] lastpiece = new byte[rest];
        	for(int j=0; j<373; j++)
                lastpiece[j] = plainFile[j];
         
            encodeFile = c.doFinal(lastpiece);

            opsisStream = new ByteArrayOutputStream(encodeFile.length);
            opsisStream.write(encodeFile);
//            fos.write(encodeFile);
//            fos.close();
        }
        
        if(pieces > 1){
        	
            byte[] filepiece = new byte[373];
        	byte[] lastpiece = new byte[rest];
        	Boolean ended=false;
    		for(int k=0; k<pieces; k++){

        		if(k<(pieces-1)){
        			for(int j=0; j<373; j++){
        				if ((j+k*373)>=plainFile.length){
        					ended=true;
        					break;
        				}
        				filepiece[j] = plainFile[j+k*373];
        			}
        			encodeFile = c.doFinal(filepiece);
                    opsisStream = new ByteArrayOutputStream(encodeFile.length);
                    opsisStream.write(encodeFile);
                    if (ended) return;
        		}
        		else{
        			for(int j=0; j<rest; j++)
        				lastpiece[j] = plainFile[j+k*373];
        
        			encodeFile = c.doFinal(lastpiece);
                    opsisStream = new ByteArrayOutputStream(encodeFile.length);
                    opsisStream.write(encodeFile);
        		}
        	}
        }        
}
	
	
}
