package CustomComponent;
 


import java.io.IOException;

import MyEvent.MyDataEvent;
import MyEvent.MyDataListener;
import MyEvent.eventSource;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;


public class OperatorPanel extends Pane 
{

    	
	@FXML MySlider sliderAtk;	
	@FXML MySlider sliderDec;
	@FXML MySlider sliderSus;
	@FXML MySlider sliderSl;
	@FXML MySlider sliderRel;
	@FXML MySlider sliderMul;
	@FXML MySlider sliderDT;
	@FXML MySlider sliderTLV;
	@FXML MySlider sliderKSL;
	@FXML MySlider sliderDAM;
	@FXML MySlider sliderDVB;
	
	@FXML Label valueLabel;
	@FXML Label operatorName;
	
	@FXML RadioButton eamRadioButton;
	@FXML RadioButton evbRadioButton;
	@FXML RadioButton ksrRadioButton;
	@FXML RadioButton xofRadioButton;
	
	@FXML ComboBox<String> waveSelect;

	ObservableList<String> options;

	private MyDataListener listener;	
	private int operatorNo = 0;

    public  OperatorPanel(){
    	 
      	super();
   	
      	FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("OperatorPanel.fxml"));
    	fxmlLoader.setRoot(this);
    	fxmlLoader.setController(this);

        try {
            fxmlLoader.load();            
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }    
        
        /* accessibleTextに値が設定されたらパラメータを読みに行く */
        this.accessibleTextProperty().addListener(accessibleTextListener);
        this.setBorder(new Border(new BorderStroke(Color.BLACK,BorderStrokeStyle.SOLID
        		,CornerRadii.EMPTY,BorderWidths.DEFAULT)));
        
    }
    
    /* パラメータの処理     "name,min,max,val"   */
	 ChangeListener<String> accessibleTextListener = new ChangeListener<String>() {
		@Override 
		public void changed(ObservableValue<?extends String>observable,String oldValue,String newValue) {
			String data[] = newValue.split(",");
		   	int i = data.length;
		   	if(i == 3){
		   		operatorName.setText(data[0]);
		   		operatorName.setTextFill(Color.valueOf(data[1]));
		   		operatorNo = Integer.parseInt(data[2]);
		   	}
		}	
	};
 

	public void initialize() {
		
		sliderAtk.addEventHandler(MySliderEvent.MYCHANGE_VALUE, 
				event->	changeValue(event.getEventValue(),eventSource.Atck)	);
		
		sliderDec.addEventHandler(MySliderEvent.MYCHANGE_VALUE, 
				event->	changeValue(event.getEventValue(),eventSource.Decy)	);
		
		sliderSus.addEventHandler(MySliderEvent.MYCHANGE_VALUE, 
				event->	changeValue(event.getEventValue(),eventSource.Sus)	);
		
		sliderSl.addEventHandler(MySliderEvent.MYCHANGE_VALUE, 
				event->	changeValue(event.getEventValue(),eventSource.SL)	);
		
		sliderRel.addEventHandler(MySliderEvent.MYCHANGE_VALUE, 
				event->	changeValue(event.getEventValue(),eventSource.Rel)	);
		
		sliderMul.addEventHandler(MySliderEvent.MYCHANGE_VALUE, 
				event->	changeValue(event.getEventValue(),eventSource.Mul)	);
		
		sliderDT.addEventHandler(MySliderEvent.MYCHANGE_VALUE, 
				event->	changeValue(event.getEventValue(),eventSource.DT)	);
		
		sliderTLV.addEventHandler(MySliderEvent.MYCHANGE_VALUE, 
				event->	changeValue(event.getEventValue(),eventSource.Tlv)	);
		
		sliderKSL.addEventHandler(MySliderEvent.MYCHANGE_VALUE, 
				event->	changeValue(event.getEventValue(),eventSource.Ksl)	);
		
		sliderDAM.addEventHandler(MySliderEvent.MYCHANGE_VALUE, 
				event->	changeValue(event.getEventValue(),eventSource.Dam)	);
		
		sliderDVB.addEventHandler(MySliderEvent.MYCHANGE_VALUE, 
				event->	changeValue(event.getEventValue(),eventSource.Dvb)	);
		

		/* 波形選択ComboBoxの初期化 */
		options = FXCollections.observableArrayList();
		for(int i = 0;i < 32;i++){
			String target = ("waveImg/img" + i +".png");
			options.add(target);
		}			
		waveSelect.setItems(options);
		waveSelect.setCellFactory(c->new StatusListCell());
		waveSelect.setButtonCell(new StatusListCell());
		waveSelect.setValue(options.get(0));

	}

	
	@FXML
	void eamSelect(){
		int i;
		if(eamRadioButton.isSelected() == true) {
			i = 1;
		}else {
			i = 0;
		}
		changeValue(i,eventSource.EAM);
	}
	
	@FXML
	void evbSelect(){
		int i;
		if(evbRadioButton.isSelected() == true) {
			i = 1;
		}else {
			i = 0;
		}
		changeValue(i,eventSource.EVB);
	}
	@FXML 
	public void ksrSelect() {
		int i;
		if(ksrRadioButton.isSelected() == true) {
			i = 1;
		}else {
			i = 0;
		}
		changeValue(i,eventSource.Ksr);			
	}
	@FXML
	void xofSelect() {
		int i;
		if(xofRadioButton.isSelected() == true) {
			i = 1;
		}else {
			i = 0;
		}
		changeValue(i,eventSource.XOF);
	}
	
	
	@FXML
	void selectedWaveform() {
		int i = options.indexOf(    waveSelect.getValue());
		changeValue(i, eventSource.Wave);
		valueLabel.setText(i+1 + "");
		
	}
	
	void changeValue(int val,eventSource source) {
		//System.out.println((val + "") + (source) );			
		if(listener != null)
		listener.changeValue( MyDataEvent.OPDATA_CHANGE,source,operatorNo,val);
	
	}

	
	
	/* set valer to control */
	
	public void setAtack(Double val){
		sliderAtk.setValue(val);
	}
	public void setDecy(Double val){
		sliderDec.setValue(val);
	}
	public void setSus(Double val){
		sliderSus.setValue(val);
	}
	public void setSL(Double val) {
		sliderSl.setValue(val);
	}
	public void setRel(Double val){
		sliderRel.setValue(val);
	}
	public void setMul(Double val){
		sliderMul.setValue(val);
	}
	public void setTlv(Double val){
		sliderTLV.setValue(val);
	}
	public void setKsl(Double val){
		sliderKSL.setValue(val);
	}
	public void setEAM(boolean b){
		eamRadioButton.setSelected(b);
	
	}
	public void setEVB(boolean b){
		evbRadioButton.setSelected(b);
	}
	public void setXOF(boolean b){
		xofRadioButton.setSelected(b);
	}
	public void setKSR(boolean b){
		ksrRadioButton.setSelected(b);
	}
	public void setWave( int val){
		waveSelect.setValue(options.get(val));	
	}
	public void setDT(Double val){
		sliderDT.setValue(val);
	}
	public void setSusLevel(Double val){
		sliderSus.setValue(val);
	}
	
	public void setDAM(Double val){
		sliderDAM.setValue(val);
	}
	public void setDVB(Double val){
		sliderDVB.setValue(val);
	}
	
	public void addListener(MyDataListener listener) {
		this.listener = listener;
	}

 
}