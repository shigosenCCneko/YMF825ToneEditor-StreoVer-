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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
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
	ContextMenu menu;
	private MyDataListener listener;
	private int operatorNo = 0;
	
	
	static int operatorAtk = 15;
	static int operatorDecy = 0;
	static int operatorSus = 0;
	static int operatorSl  = 0;
	static int operatorRel = 15;
	static int operatorMul = 1;
	static int operatorDt = 0;
	static int operatorTlv = 63;
	static int operatorKsl = 0;
	static int operatorDam = 0;
	static int operatorDvb = 0;
	
	static int operatorEam = 0;
	static int operatorEvb = 0;
	static int operatorKsr = 0;
	static int operatorXof = 0;
	static int operatorWave = 0;
	static int operatorFb = 0;
	
	
	
	final int defaultAtk = 15;
	final int defaultDecy = 0;
	final int defaultSus = 0;
	final int defaultSl  = 0;
	final int defaultRel = 15;
	final int defaultMul = 1;
	final int defaultDt = 0;
	final int defaultTlv = 63;
	final int defaultKsl = 0;
	final int defaultDam = 0;
	final int defaultDvb = 0;
	
	final int defaultEam = 0;
	final int defaultEvb = 0;
	final int defaultKsr = 0;
	final int defaultXof = 0;
	final int defaultWave = 0;
	final int defaultFb = 0;
	

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

		waveSelect.setOnScroll((ScrollEvent event)-> {
        	double deltaY = event.getDeltaY();
        	double multiY = event.getMultiplierY();

        	double cnt = -(int)(deltaY/multiY);
        	double max = options.size();

        	int i = options.indexOf(    waveSelect.getValue());
        	i = i + (int)cnt;
        	if(i >=0 && i < max) {
        		if(waveSelect.isFocused() == true) {
        			changeValue(i, eventSource.Wave);
        			valueLabel.setText(i + "");
        			waveSelect.setValue(options.get(i));
        		}
        	}
		});

		/*
		 * 右クリックのみメニュー選択可にするためLabelにsetOnMouseClickedを付ける
		 */
		
		MenuItem[]menui = new MenuItem[7];
		Label[]labeli = new Label[5];
		
		labeli[0] = new Label("copy");
		labeli[1] = new Label("paste");
		labeli[2] = new Label("ADSRcopy");
		labeli[3] = new Label("ADSRpaste");
		labeli[4] = new Label("clear");
		
		labeli[0].setOnMouseClicked(e-> {
			if(e.getButton() == MouseButton.PRIMARY) {
				editCopy(e);}});
		labeli[1].setOnMouseClicked(e-> {
			if(e.getButton() == MouseButton.PRIMARY) {
				editPaste(e);}});
		
		labeli[2].setOnMouseClicked(e-> {
			if(e.getButton() == MouseButton.PRIMARY) {			
			adsrCopy(e);}});
		labeli[3].setOnMouseClicked(e-> {
			if(e.getButton() == MouseButton.PRIMARY) {		
				adsrPaste(e);}});
		labeli[4].setOnMouseClicked(e-> {
			if(e.getButton() == MouseButton.PRIMARY) {
		editClear(e);}});
		
		
		menui[0] = new CustomMenuItem(labeli[0]);
		menui[1] = new CustomMenuItem(labeli[1]);
		menui[2] = new SeparatorMenuItem();
		menui[3] = new CustomMenuItem(labeli[2]);
		menui[4] = new CustomMenuItem(labeli[3]);
		menui[5] = new SeparatorMenuItem();
		menui[6] = new CustomMenuItem(labeli[4]);
		menu = new ContextMenu();
		menu.getItems().addAll(menui);
		
			
		this.addEventHandler(MouseEvent.MOUSE_CLICKED, e-> editMenu(e));


	}

	void editMenu(MouseEvent e) {
		if(e.getButton() == MouseButton.SECONDARY ) {
			menu.show(this,e.getScreenX(),e.getScreenY());
		}
	
	}
	
	
	void adsrCopy(MouseEvent e) {
		 operatorAtk = (int)(sliderAtk.getValue()+0.0);
		 operatorDecy = (int)(sliderDec.getValue()+0.0);
		 operatorSus = (int)(sliderSus.getValue()+0.0);
		 operatorSl  = (int)(sliderSl.getValue()+0.0);
		 operatorRel = (int)(sliderRel.getValue()+0.0);		
		
	}
	
	void adsrPaste(MouseEvent e) {
		
		adsrNotify();
		
	}
	void editCopy(MouseEvent e) {
		
		 operatorAtk = (int)(sliderAtk.getValue()+0.0);
		 operatorDecy = (int)(sliderDec.getValue()+0.0);
		 operatorSus = (int)(sliderSus.getValue()+0.0);
		 operatorSl  = (int)(sliderSl.getValue()+0.0);
		 operatorRel = (int)(sliderRel.getValue()+0.0);
		 operatorMul = (int)(sliderMul.getValue()+0.0);
		 operatorDt = (int)(sliderDT.getValue()+0.0);
		 operatorTlv = (int)(sliderTLV.getValue()+0.0);
		 operatorKsl = (int)(sliderKSL.getValue()+0.0);
		 operatorDam = (int)(sliderDAM.getValue()+0.0);
		 operatorDvb = (int)(sliderDVB.getValue()+0.0);
		
		 operatorEam = eamRadioButton.isSelected() == true ? 1:0;
		 operatorEvb = evbRadioButton.isSelected() == true ? 1:0;
		 operatorKsr = ksrRadioButton.isSelected() == true ? 1:0;
		 operatorXof = xofRadioButton.isSelected() == true ? 1:0;
		 operatorWave = options.indexOf(    waveSelect.getValue());
		// operatorFb = 
	}
	
	void editPaste(MouseEvent e) {

		allNotify();

	}
	
	void editClear(MouseEvent e) {
		
	 

		sliderAtk.setValue(defaultAtk+0.0);
		sliderDec.setValue(defaultDecy+0.0);
		sliderSus.setValue(defaultSus+0.0);
		sliderSl.setValue(defaultSl+0.0);
		sliderRel.setValue(defaultRel+0.0);
		sliderMul.setValue(defaultMul+0.0);
		sliderDT.setValue(defaultDt+0.0);
		sliderTLV.setValue(defaultTlv+0.0);
		sliderKSL.setValue(defaultKsl+0.0);
		sliderDAM.setValue(defaultDam+0.0);
		sliderDVB.setValue(defaultDvb+0.0);
		setWave(defaultWave);
		
		eamRadioButton.setSelected(defaultEam == 1 ? true:false);
		evbRadioButton.setSelected(defaultEvb == 1 ? true:false);
		ksrRadioButton.setSelected(defaultKsr == 1 ? true:false);
		xofRadioButton.setSelected(defaultXof == 1 ? true:false);
		
		
		
		
		
		changeValue(defaultAtk,eventSource.Atck);
		changeValue(defaultDecy,eventSource.Decy);
		changeValue(defaultSl,eventSource.SL);
		changeValue(defaultSus,eventSource.Sus);
		changeValue(defaultRel,eventSource.Rel);
		changeValue(defaultMul,eventSource.Mul);
		changeValue(defaultDt,eventSource.DT);
		changeValue(defaultTlv,eventSource.Tlv);
		changeValue(defaultKsl,eventSource.Ksl);
		changeValue(defaultDam,eventSource.Dam);
		changeValue(defaultDvb,eventSource.Dvb);
		
		changeValue(defaultEam,eventSource.EAM);
		changeValue(defaultEvb,eventSource.EVB);
		changeValue(defaultKsr,eventSource.Ksr);
		changeValue(defaultXof,eventSource.XOF);
		changeValue(defaultWave,eventSource.Wave);

	}
	
	
	void adsrNotify() {
		sliderAtk.setValue(operatorAtk+0.0);
		sliderDec.setValue(operatorDecy+0.0);
		sliderSus.setValue(operatorSus+0.0);
		sliderSl.setValue(operatorSus+0.0);
		sliderRel.setValue(operatorRel+0.0);
		
		changeValue(operatorAtk,eventSource.Atck);
		changeValue(operatorDecy,eventSource.Decy);
		changeValue(operatorSl,eventSource.SL);
		changeValue(operatorSus,eventSource.Sus);
		changeValue(operatorRel,eventSource.Rel);
	}
	
	
	void allNotify() {
		sliderAtk.setValue(operatorAtk+0.0);
		sliderDec.setValue(operatorDecy+0.0);
		sliderSus.setValue(operatorSus+0.0);
		sliderSl.setValue(operatorSus+0.0);
		sliderRel.setValue(operatorRel+0.0);
		sliderMul.setValue(operatorMul+0.0);
		sliderDT.setValue(operatorDt+0.0);
		sliderTLV.setValue(operatorTlv+0.0);
		sliderKSL.setValue(operatorKsl+0.0);
		sliderDAM.setValue(operatorDam+0.0);
		sliderDVB.setValue(operatorDvb+0.0);
		setWave(operatorWave);
		
		eamRadioButton.setSelected(operatorEam == 1?true:false);
		evbRadioButton.setSelected(operatorEvb == 1?true:false);
		ksrRadioButton.setSelected(operatorKsr == 1?true:false);
		xofRadioButton.setSelected(operatorXof == 1?true:false);
		
		
		
		
		
		changeValue(operatorAtk,eventSource.Atck);
		changeValue(operatorDecy,eventSource.Decy);
		changeValue(operatorSl,eventSource.SL);
		changeValue(operatorSus,eventSource.Sus);
		changeValue(operatorRel,eventSource.Rel);
		changeValue(operatorMul,eventSource.Mul);
		changeValue(operatorDt,eventSource.DT);
		changeValue(operatorTlv,eventSource.Tlv);
		changeValue(operatorKsl,eventSource.Ksl);
		changeValue(operatorDam,eventSource.Dam);
		changeValue(operatorDvb,eventSource.Dvb);
		
		changeValue(operatorEam,eventSource.EAM);
		changeValue(operatorEvb,eventSource.EVB);
		changeValue(operatorKsr,eventSource.Ksr);
		changeValue(operatorXof,eventSource.XOF);
		changeValue(operatorWave,eventSource.Wave);
														
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