package application;

import DataClass.Ymf825ToneData;
import MyEvent.MyDataEvent;
import MyEvent.Observer;
import MyEvent.eventSource;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.stage.Window;

public class EnvelopeViewerController implements Observer {


		private int totalLevel[];
		private int attackRate[];
		private int decayRate[];
		private int sustainLevel[];
		private int sustainRate[];
		private int releaseRate[];

		private int traceModeTl[];

		private int keyOffTime=300;
		@FXML Slider noteOffPos;
		@FXML Slider tracePos;
		@FXML Slider tone_slider;

		@FXML RadioButton OP_1;
		@FXML RadioButton OP_2;
		@FXML RadioButton OP_3;
		@FXML RadioButton OP_4;
		@FXML RadioButton rdbtnX;
		@FXML RadioButton toneTraceRadioButton;
		@FXML Canvas canvas;

		private int algorithmNo = 0;
		private int is_career[][] = {
				{0,1,0,0},
				{1,1,0,0},
				{1,1,1,1},
				{0,0,0,1},
				{0,0,0,1},
				{0,1,0,1},
				{1,0,0,1},
				{0,0,1,1}	};

		private Ymf825ToneData toneData;


		private boolean envToneMode = false;
		private int toneNo = 60;
		private int timbrePosition = 0;
		private double timbreTlValue = 0;
		private int channelNo = 0;
		private int carValue[] = new int[1000];	
		private int carOP4Value[] = new int[1000];



		private double viewerMagni = 1.0;

		GraphicsContext g;

	@FXML void initialize(){
		toneData = Ymf825ToneData.getInstance();
		toneData.attach(this);
		//byte buf[] = new byte[30];
		//int ch = PanelController.getPanelChannel();
		//toneData.getToneData(ch, buf);


		totalLevel = new int[4];
		attackRate = new int[4];
		decayRate = new int[4];
		sustainLevel = new int[4];
		sustainRate = new int[4];
		releaseRate = new int[4];

		traceModeTl = new int[4];

		g = canvas.getGraphicsContext2D();


		noteOffPos.valueProperty().addListener((
	        	ObservableValue<? extends Number> ov,Number old_val,
	    		Number new_val) ->{
	    			noteOffPosition(new_val.intValue());
	    	});

		tracePos.valueProperty().addListener((
	        	ObservableValue<? extends Number> ov,Number old_val,
	    		Number new_val) ->{
	    			tracePosition(new_val.intValue());
	    	});
		tone_slider.setValue(toneNo);
		tone_slider.valueProperty().addListener((
	        	ObservableValue<? extends Number> ov,Number old_val,
	    		Number new_val) ->{
	    			noteNoSelect(new_val.intValue());
	    	});
	}





	@FXML
	void onCloseAction(ActionEvent event) {
		Scene scene = ((Node) event.getSource()).getScene();
		Window window = scene.getWindow();
		window.hide();

	}

	@FXML void opSelectButton(){
		rePaint();
	}


	@FXML void set3XView() {
		if(rdbtnX.isSelected() == true) {
			viewerMagni = 3;
		}else {
			viewerMagni = 1;
		}
		rePaint();

	}



	@FXML void setTraceMode() {

		if(toneTraceRadioButton.isSelected() == true) {
			envToneMode = true;
			timbrePosition = 0;
			tracePos.setValue(timbrePosition);
			setOperatorValue();

			toneData.notifyStop(true);
			for(int i = 0;i < 4;i++){

				toneData.setValue(eventSource.Atck, i, channelNo, 15);
				toneData.setValue(eventSource.Decy, channelNo, i, 0);
				toneData.setValue(eventSource.Sus, channelNo, i, 0);
				toneData.setValue(eventSource.Tlv, channelNo, i, 63);
				

			}
			toneData.notifyStop(false);
			toneData.setContinuousSoundMode(true,toneNo);		
		}else {
			envToneMode = false;
			toneData.setContinuousSoundMode(false,toneNo);

			for(int i = 0;i<4;i++){
				toneData.setValue(eventSource.Atck, i, channelNo, attackRate[i]);
				toneData.setValue(eventSource.Decy, channelNo, i, decayRate[i]);
				toneData.setValue(eventSource.Sus, channelNo, i, sustainRate[i]);
				toneData.setValue(eventSource.Tlv, channelNo, i, totalLevel[i]);

			}


		}

	}

	private void changeTlv(int op,int tlv){
		toneData.setTraceTlv(eventSource.Tlv,channelNo,op, tlv);

	}




	void tracePosition(int i) {
		timbrePosition = i;
		rePaint();

	}

	void noteOffPosition(int i) {
		keyOffTime = i;
		rePaint();

	}

	void noteNoSelect(int i) {
		toneNo= i;
		if(envToneMode == true) {
			toneData.setContinuousToneNo(i);
		}
	}
	private void rePaint() {
		g.clearRect(0, 0, 800, 300);
		paintLine();
	}


	public void setOperatorValue() {
		int ch = PanelController.getPanelChannel();
		for(int i = 0;i < 4;i++) {
			totalLevel[i] = toneData.getValue(ch, i, eventSource.Tlv);

			attackRate[i] = toneData.getValue(ch, i, eventSource.Atck);
			decayRate[i] = toneData.getValue(ch, i, eventSource.Decy);
			sustainLevel[i] = toneData.getValue(ch, i, eventSource.SL);
			sustainRate[i] = toneData.getValue(ch, i, eventSource.Sus);
			releaseRate[i] = toneData.getValue(ch, i, eventSource.Rel);

		}
		algorithmNo = toneData.getAlgorithmNo(ch);


	}

/* ---------------  receive change notify ----------------------- */
	@Override
	public void update(EventType<MyDataEvent> e, eventSource source, int ch, int op, int val) {
		if(e ==  MyEvent.MyDataEvent.DATA_UPDATE) {
			channelNo = PanelController.getPanelChannel();
			setOperatorValue();
			rePaint();
		}else if(e == MyEvent.MyDataEvent.CHANGECHANNEL) {
			channelNo = PanelController.getPanelChannel();
			setOperatorValue();
			rePaint();



		}else if(e == MyEvent.MyDataEvent.OPDATA_CHANGE) {

			if(channelNo == ch) {
				switch(source) {
				case Connect:
					algorithmNo = toneData.getAlgorithmNo(ch);
					break;

				case Tlv:
					totalLevel[op] = val;
					break;
				case Atck:
					attackRate[op] = val;
					break;
				case Decy:
					decayRate[op] = val;
					break;
				case SL:
					sustainLevel[op] = val;
					break;
				case Sus:
					sustainRate[op] = val;
					break;
				case Rel:
					releaseRate[op] = val;
					break;
					default:
						return;
				}
				rePaint();


			}

		}
	}
/* -------------- Draw Line ----------------------------------*/
	void line(GraphicsContext g,int sx,int sy, int ex, int ey){
		double scale = 0.5;
		sy *= 0.8;
		ey *= 0.8;
		g.strokeLine((int)(sx*scale)+15, 230-sy, (int)(ex*scale)+15, 230-ey);
	}

	public void paintLine(){

		int i = 0;
		double scale;
		double scale_mag;
		double startX;
		double startY;
		double endX ;
		double endY;
		double slope,step,magni;

		Color timbleColor = Color.DARKGRAY;
		int Stroke = 5;
		int carStroke = 4;
		int modStroke = 3;

		g.setLineWidth(Stroke);
		g.setStroke(Color.WHITE);

		line(g,keyOffTime,0,keyOffTime,250);
		line(g,0,0,1000,0);

		scale = 2.00;
		scale_mag = 0.25;

		double TlDbRate = 0.75;
		double SlDbRate = -3;

		double maxhight = 250.0;
		double maxTlVal = 63;
		double maxSlVal = 15;


		double TlMag;
		double SlMag;


	for(i = 3;i>=0;i--){
		boolean career;
		if((algorithmNo > 1) || i < 2){

		if( is_career[ algorithmNo][i] == 1){
			career = true;

			TlMag = maxhight/Math.pow(1.122,maxTlVal*TlDbRate);
			SlMag = 1.0;

		}else{
			career = false;
			TlMag = maxhight /maxTlVal;
			SlMag = maxhight /maxSlVal;

		}



		RELEASE:if(true){


		if(career) {
			g.setLineWidth(carStroke--);
		}else {
			g.setLineWidth(modStroke--);			
		}
		switch(i){
		case 0:
			g.setStroke( Color.LIME   );
			if(OP_1.isSelected() == false){
				traceModeTl[i] = 63;
				continue;

			}
			break;
		case 1:
			g.setStroke( Color.RED);
			if(OP_2.isSelected() == false){
				traceModeTl[i] = 63;
				continue;
			}
			break;
		case 2:
			g.setStroke(Color.BLUE);
			if(OP_3.isSelected() == false){
				traceModeTl[i] = 63;
				continue;
			}
			break;
		case 3:
			g.setStroke( Color.BLACK);
			if(OP_4.isSelected() == false){
				traceModeTl[i] = 63;
				continue;
			}
			break;
		default:

		}



		/* attack phase--------------------------------------- */
		scale_mag = 0.375 * viewerMagni;
		startX = 0;
		startY = 0;
		endX =  Math.pow(scale,(15 - attackRate[i]))*scale_mag;
		if(career){
			endY = Math.pow(1.122, (maxTlVal-totalLevel[i])*TlDbRate)*TlMag;

		}else{
			endY =  (maxTlVal-totalLevel[i])*TlMag;

		}

		magni = endY/maxhight;
		if(endX <= keyOffTime){
if(attackRate[i] == 0) {
	endY = startY;
}

			line(g,(int)startX, (int)startY,(int)endX,(int) endY);
			fillRealEnvelope(i,career,g,startX,startY,endX,endY);
			if(timbrePosition < endX){
				slope = endY/endX;
				step = timbrePosition;
				timbreTlValue = slope * step;

			}

		}else{
			slope = (endY-startY)/(endX-startX);

			step = keyOffTime;
			endX = keyOffTime;
			endY = slope*step;
if(attackRate[i] == 0) {
	endY = startY;
}

			line(g,(int)startX, (int)startY,(int)endX,(int) endY);
			fillRealEnvelope(i,career,g,startX,startY,endX,endY);
			if(timbrePosition < endX){
				step = timbrePosition;
				timbreTlValue = slope * step;

			}
			break RELEASE;



		}
		/*decay phase -------------------------------------------- */
		scale_mag = scale_mag * 1.534;
//		scale_mag = scale_mag * 1.52;
		startX = endX;
		startY = endY;

		endX = startX + Math.pow(scale,15-decayRate[i])*scale_mag;
		endY = 0;
		slope = (endX-startX)/(endY-startY);
		if(career){
			endY = Math.pow(1.122,(sustainLevel[i])*SlDbRate)*maxhight*magni;
		}else{
			endY = (15-sustainLevel[i])*magni * SlMag;


		}


		step = (endY-startY);
		endX = startX + (slope*step);

		if(endX <= keyOffTime){
if(decayRate[i] == 0) {
	endY = startY;
}

			line(g,(int)startX,(int) startY,(int) endX,(int) endY);
			fillRealEnvelope(i,career,g,startX,startY,endX,endY);
			if(startX <= timbrePosition && timbrePosition < endX){
				slope = (endY - startY)/(endX - startX);
				step = timbrePosition - startX;
				timbreTlValue = startY + slope * step;

			}else{
				if(endY == 0 && endX <= timbrePosition ){
					timbreTlValue = 0;
					endY = 0;
				}
			}

		}else{
			endX = startX + Math.pow(scale,15-decayRate[i])*scale_mag;;
			endY = 0;
			slope = (endY-startY)/(endX-startX);
			step = keyOffTime - startX;
			endX = keyOffTime;
			endY = startY + slope*step;
if(decayRate[i] == 0) {
	endY = startY;
}

			line(g,(int)startX,(int) startY,(int) endX,(int) endY);
			fillRealEnvelope(i,career,g,startX,startY,endX,endY);
			if(startX <= timbrePosition && timbrePosition < endX){
				step = timbrePosition - startX;
				timbreTlValue = startY + slope * step;

			}

			break RELEASE;
		}
		if(endY <= 0){
			break RELEASE;
		}
		/* sustain phase ------------------------------------------ */
		startX = endX;
		startY = endY;

		endX = Math.pow(scale,15-sustainRate[i])*scale_mag + startX;
		endY = startY-16*16;
			slope = (endY-startY)/(endX-startX);
			step =  (keyOffTime - startX);
			endY = startY+ (slope*step);
if(sustainRate[i] == 0) {
	endY = startY;
}

		if(endY > 0 ){


			endX = keyOffTime;

			line(g,(int)startX,(int)startY,(int)endX,(int)endY);
			fillRealEnvelope(i,career,g,startX,startY,endX,endY);
			if(startX <= timbrePosition && timbrePosition < endX){
				if(timbrePosition < 0){
					timbreTlValue = 63;
				}else{
				slope = (endY - startY)/(endX - startX);
				step = timbrePosition - startX;
				timbreTlValue = startY + slope * step;
				}

			}



		}else{
			endY = startY - 16 * 16 ;
			slope = (endX-startX)/(endY-startY);
			step = -startY;
			endY = 0;

			endX = startX+(slope*step);

			line(g,(int)startX,(int)startY,(int)endX,(int)endY);
			fillRealEnvelope(i,career,g,startX,startY,endX,endY);
			if(endX < 999){
				for(int pos = (int)endX;pos <999;pos++){
					carValue[pos] = 0;
					if(i == 3) {
						carOP4Value[pos] = 0;
					}
				}
			}
			if(startX <= timbrePosition && timbrePosition < endX){
				slope = 1/slope;
				step = timbrePosition - startX;
				timbreTlValue = startY + slope * step;

			}else{
				if(timbrePosition >= endX){
					timbreTlValue = 0;
				}

			}


			break RELEASE;

		}
	}
	/* RELEASE -----------------------------------------------------*/

		if(endY > 0){


			startX = endX;
			startY = endY;

			endY = startY-16 * 16*magni;
			endX = startX + Math.pow(scale,15-releaseRate[i])*scale_mag;
			slope = (endX-startX)/(endY-startY);
			step  = -startY;
			endX = startX+(slope*step);
			endY = 0;
if(releaseRate[i] ==0) {
	endY = startY;
}

			line(g,(int)startX,(int)startY,(int)endX,(int)endY);
			fillRealEnvelope(i,career,g,startX,startY,endX,endY);
			if(endX < 999){
				for(int pos = (int)endX;pos <999;pos++){
					carValue[pos] = 0;
					if(i == 3) {
						carOP4Value[pos] = 0;
					}
				}
			}
			if(startX <= timbrePosition ){
				slope = 1/slope;
				step = timbrePosition - startX;
				timbreTlValue  = startY + slope * step;

			}
		}



		int tlval = 63 - (int)(timbreTlValue / 3.92);
		if(tlval > 63)
			tlval = 63;
		if(tlval < 0)
				tlval = 0;

		if(timbrePosition == -1){
			tlval = 63;
		}
		traceModeTl[i] = tlval;

	}

	}

		if(envToneMode == true){
			for(i = 0;i<4;i++){
				changeTlv(i,traceModeTl[i]);
			}

			g.setStroke(timbleColor);
			line(g,timbrePosition,0,timbrePosition,250);
		}


	}

	private int  fillRealEnvelope(int opNo,boolean isCareer, GraphicsContext g,double startX,double startY,double endX,double endY){

		int pos;
		double slope = (endY-startY)/(endX-startX);
		double width = g.getLineWidth();
		g.setLineWidth(1);
		
		if(endX>999){
			endX = 999;
		}

		for( pos = (int)startX; pos < endX; pos++){
			int valY = (int)(slope * (pos-startX))+(int)startY;

			if(isCareer == false){
				switch(algorithmNo) {
				case 3:
					if(opNo == 0){
						valY = valY * carOP4Value[pos]/250;		
						if((pos % 3) == 0) {					
							line(g,pos,0,pos,valY);
						}						
	
					}else{
							valY = valY * carValue[pos]/250;
							line(g,pos,0,pos,valY);						
		
					}					
					
					break;
				case 5:
					valY = valY * carValue[pos]/250;					
					if(opNo == 0){
						if((pos % 3) == 0) {					
							line(g,pos,0,pos,valY);
						}						
	
					}else {
						line(g,pos,0,pos,valY);						
					}
	
					break;
				default:
					valY = valY * carValue[pos]/250;
					line(g,pos,0,pos,valY);					
					
					break;
				
				}

			}
			carValue[pos] = valY;
			if(opNo == 3) {
				carOP4Value[pos] = valY;
			}
			if(valY<0){
				break;
			}

		}
		g.setLineWidth(width);
		return pos;

	}
	
	
	

}