package kniz.main_pack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class BiometricIdCard implements Serializable{
	
	private final String TAG            = "Opsis::BiomethricIdCard";
	
	
	
	
	private double [] eigenVector;
//	private double[][] eigVector = null;

	private int bundleId;
	private FaceBundle fb;
	
	private int issuingMunicipality; 			//comune di rilascio
	private int issuingSubMunicipality=0;
	


	private String lastName; 					//congnome
	private String firstName; 					//nome
	private String birthPlace;					//luogo di nascita
	private Date birthDate;						//data di nascita
	private int sex;
	private double height;
	private String municipalityOfResidence;		//comune di residenza
	private String address;						
	private Date issuingDate;
	private Date expirationDate;
	private String nationality;
	private String fiscalCode;
	private Boolean validityToTravel;
	
	
	public static final int MALE=0;
	public static final int FEMALE=1;
	
	public static final int ROMA=0;
	public static final int MILANO=1;
	public static final int NAPOLI=2;
	
	
	
	public BiometricIdCard (String firstName, String lastName, String birthPlace,String birthDate,
			String sex, String height, String residence, String address, String nationality, String fiscalCode,
			String validityToTravel, double [] eigenVector, int bundleId){
		this.eigenVector = new double[eigenVector.length];
		System.arraycopy(eigenVector, 0, this.eigenVector, 0, eigenVector.length);
		this.bundleId=bundleId;
		this.firstName = new String (firstName);
		this.lastName= new String(lastName);
		this.birthPlace = new String (birthPlace);
		this.birthDate = parseDate(birthDate);
		this.height = Double.parseDouble(height);
		this.issuingDate = new Date();
		this.expirationDate = new Date (issuingDate.getYear()+10, issuingDate.getMonth(), issuingDate.getDay());
		this.municipalityOfResidence=new String (residence);
		this.address= new String (address);
		this.nationality= new String (nationality);
		this.fiscalCode = new String (fiscalCode);
		this.validityToTravel = parseValidityToTravel(validityToTravel);
		if (municipalityOfResidence.equals("Roma"))
			issuingMunicipality=ROMA;
		else if (municipalityOfResidence.equals("Milano"))
			issuingMunicipality=MILANO;
		else
			issuingMunicipality=NAPOLI;
	}
	
	private Date parseDate (String dateToParse){
		StringBuilder[] date = new StringBuilder [3];
		int cur=0;
		for (int i=0; i<3; ++i)
			date[i]=new StringBuilder();
		for (int i=0; i<dateToParse.length(); ++i){
			if (dateToParse.charAt(i)=='/'){
				++cur;
				continue;
			}
			date[cur].append(dateToParse.charAt(i));
		}
		Log.i(TAG,"[BIOMETHRIC ID CARD] date:"+date[0].toString()+"/"+date[1].toString()+"/"+date[2].toString()+
				" from:"+dateToParse);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		try {
			Date retval = sdf.parse(dateToParse);
			Log.i(TAG,"[PARSE DATE]date:"+sdf.format(retval));
			return retval;
		} catch (ParseException e) {
			Log.i(TAG,"[PARSE DATE] sdf date error:"+e.toString());
		}
		return null;
		//new Date(Integer.parseInt(date[2].toString()), Integer.parseInt(date[1].toString()), 
				//Integer.parseInt(date[0].toString()));
	}
	
	private int parseSex(String sex){
		if (sex.equals("Male"))
			return MALE;
		return FEMALE;
	}
	
	private Boolean parseValidityToTravel(String validityToTravel){
		if (validityToTravel.equals("Valid"))
			return true;
		return false;
	}
	
	
	public BiometricIdCard (BiometricIdCard idCard){
		eigenVector = new double[idCard.getEigenVector().length];
		System.arraycopy(idCard.getEigenVector(), 0, eigenVector, 0, idCard.getEigenVector().length);
		bundleId=idCard.getBundleId();
		lastName= new String(idCard.getLastName());
//		this.firstName= new String(lastName);
	}
	
	public String getLastName(){
		return (lastName);
	}
	
	public String getFirstName(){
		return (firstName);
	}
	
	public String getBirthPlace(){
		return birthPlace;
	}
	
	public String getBirthDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy");
		return sdf.format(birthDate);
	}
	
	public String getSex(){
		if (sex==MALE)
			return "MALE";
		return "FEMALE";
	}
	
	public String getHeight(){
		return (""+height);
	}
	
	public String getMunicipalityOfResidence(){
		return municipalityOfResidence;
	}
	
	public String getAddress(){
		return address;
	}
	
	public String getIssuingDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy");
		return sdf.format(issuingDate);
	}
	
	public String getExpirationDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy");
		return sdf.format(expirationDate);
	}
	
	public String getNationality(){
		return nationality;
	}
	
	public String getFiscalCode(){
		return fiscalCode;
	}
	
	public String getValidityToTravel(){
		if (validityToTravel)
			return "Valida per l'espatrio";
		return "Non valida per l'espatrio";
	}

	public double[] getEigenVector() {
		return eigenVector;
	}
	
	public void setBundle(FaceBundle bundle){
		fb=bundle;
	}
	
	public FaceBundle getBundle(){
		return fb;
	}


	public int getBundleId() {
		return bundleId;
	}


	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public int getIssuingSubMunicipality() {
		return issuingSubMunicipality;
	}
	
	public int getIssuingMunicipality() {
		return issuingMunicipality;
	}
	

}
