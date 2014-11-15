package kniz.main_pack.encode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import kniz.main_pack.BiometricIdCard;
import kniz.main_pack.R;

public class FrontIdCardBuilder {
	
//	private BiomethricIdCard idCard;
//	private Canvas canvas;
	
	public static Bitmap makeIdCardHumanReadable(BiometricIdCard idCard, Bitmap faceBitmap, Context context){
		Bitmap baseBm = Bitmap.createBitmap(700, 1000, Bitmap.Config.ARGB_8888);
		Bitmap backGroundBm = BitmapFactory.decodeResource(context.getResources(), R.drawable.opsis_background);
		Bitmap toPrintFaceBm = Bitmap.createScaledBitmap(faceBitmap, faceBitmap.getWidth()*2, faceBitmap.getHeight()*2, false);
//		float x = baseBm.getWidth(); 
//		float y = baseBm.getHeight(); 
		Canvas canvas = new Canvas(baseBm);
		Paint backgroundPaint = new Paint();
		Paint titlePaint = new Paint();
		Paint fieldPaint = new Paint();
		Paint valuePaint = new Paint();
		Paint rectPaint = new Paint();
//		Paint paint = new Paint();
		canvas.drawColor(Color.LTGRAY);

		titlePaint.setColor(Color.BLACK);
		rectPaint.setColor(Color.GRAY);
		fieldPaint.setColor(Color.BLACK);
		valuePaint.setColor(Color.BLUE);
		
		titlePaint.setTextSize(50);
		fieldPaint.setTextSize(30);
		valuePaint.setTextSize(25);
		
//		backgroundPaint.setAlpha(90);
		
		backgroundPaint.setAntiAlias(true);
        titlePaint.setAntiAlias(true);
        fieldPaint.setAntiAlias(true);
        valuePaint.setAntiAlias(true);
//		rectPaint.setAntiAlias(true);
        
        canvas.drawBitmap(backGroundBm, 0, 0, backgroundPaint);
        
        canvas.drawText("Carta d'Identità", 150, 50, titlePaint);
        canvas.drawText("Cognome:", 10, 150, fieldPaint);
        canvas.drawText("Nome:", 10, 200, fieldPaint);
        canvas.drawText("Nato a:", 10, 250, fieldPaint);
        canvas.drawText("il:", 10, 300, fieldPaint);
        canvas.drawText("Sesso:", 10, 350, fieldPaint);
        canvas.drawText("Altezza:", 10, 400, fieldPaint);
        canvas.drawText("Residente in:", 10, 450, fieldPaint);
        canvas.drawText("Indirizzo:", 10, 500, fieldPaint);
        canvas.drawText("Data di emissione:", 10, 550, fieldPaint);
        canvas.drawText("Data di scadenza:", 10, 600, fieldPaint);
        canvas.drawText("Cittadinanza:", 10, 650, fieldPaint);
        canvas.drawText("Codice fiscale:", 10, 700, fieldPaint);
        canvas.drawText("Firma:", 10, 750, fieldPaint);
        canvas.drawText("Comune di rilascio:", 10, 850, fieldPaint);
        canvas.drawText("Circoscrizione:", 10, 900, fieldPaint);
        

        canvas.drawText(idCard.getLastName(), 200, 150, valuePaint);
        canvas.drawText(idCard.getFirstName(), 200, 200, valuePaint);
        canvas.drawText(idCard.getBirthPlace(), 200, 250, valuePaint);
        canvas.drawText(idCard.getBirthDate(), 200, 300, valuePaint);
        if (idCard.getSex().charAt(0)=='M')
        	canvas.drawText("maschio", 200, 350, valuePaint);
        else
        	canvas.drawText("femmina", 200, 350, valuePaint);
        canvas.drawText(idCard.getHeight()+" m", 200, 400, valuePaint);
        canvas.drawText(idCard.getMunicipalityOfResidence(), 200, 450, valuePaint);
        canvas.drawText(idCard.getAddress(), 200, 500, valuePaint);
        canvas.drawText(idCard.getIssuingDate(), 270, 550, valuePaint);
        canvas.drawText(idCard.getExpirationDate(), 260, 600, valuePaint);
        canvas.drawText(idCard.getNationality(), 200, 650, valuePaint);
        canvas.drawText(idCard.getFiscalCode(), 210, 700, valuePaint);
        canvas.drawRect(100, 720, 650, 750, rectPaint);
        canvas.drawText("Comune di Roma", 270, 850, valuePaint);
        canvas.drawText(idCard.getIssuingSubMunicipality()+"", 250, 900, valuePaint);
        
        
        canvas.drawBitmap(toPrintFaceBm, 500, 100, valuePaint);
//        canvas.drawBitmap(retval,0,0,paint);
        
        return baseBm;
        
	}

}




