package kniz.main_pack;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigInteger;

import org.apache.http.entity.ByteArrayEntity;

import android.util.Log;

public class Opsis implements Serializable{

	private final String TAG = new String ("Opsis::Opsis");
	
	private BigInteger encodedIdCard;
	private int issuingMunicipalityId;
	private int issuingSubMunicipalityId;
	private String lastName;
	
	public int getIssuingSubMunicipalityId() {
		return issuingSubMunicipalityId;
	}

	public void setIssuingSubMunicipalityId(int issuingSubMunicipalityId) {
		this.issuingSubMunicipalityId = issuingSubMunicipalityId;
	}

	public void setEncodedIdCard(ByteArrayOutputStream encodedData ){
		//encodedIdCard = 
		encodedIdCard = new BigInteger(encodedData.toByteArray());
//		System.arraycopy(encodedData, 0, encodedIdCard, 0, encodedData.size());
//		System.arraycopy(encodedData, 0, encodedIdCard, 0, encodedData.length);
	}
	
	public void setIssuingMunicipalityId(int municipalityId){
		issuingMunicipalityId=municipalityId;
	}
	
	public void copyEncodedIdCard(byte [] encodedData){
		if (encodedIdCard==null)
			Log.i(TAG, "[COPY ENCODED ID CARD] encoded data null in this opsis...");
		encodedIdCard = new BigInteger(encodedData);
//		encodedData= new byte [encodedIdCard.length];
//		System.arraycopy(encodedIdCard, 0, encodedData, 0, encodedIdCard.length);
	}
	
	public BigInteger getEncodedIdCard(){
		return encodedIdCard;
	}
	
	public int getIssuingMunicipalityId(){
		return issuingMunicipalityId;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public Boolean emptyEncodedIdCard(){
		return (encodedIdCard==null);
	}
	
	
	
	
	
	
}
