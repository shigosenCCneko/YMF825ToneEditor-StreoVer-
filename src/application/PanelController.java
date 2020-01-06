package application;

import java.io.IOException;

import CustomComponent.MySlider;
import CustomComponent.MySliderEvent;
import CustomComponent.OperatorPanel;
import DataClass.Ymf825ToneData;
import MyEvent.MyDataEvent;
import MyEvent.MyDataListener;
import MyEvent.Observer;
import MyEvent.eventSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import toneData.DefaultTone;


public class PanelController implements MyDataListener , Observer{

		@FXML OperatorPanel operator1;
		@FXML OperatorPanel operator2;
		@FXML OperatorPanel operator3;
		@FXML OperatorPanel operator4;
		OperatorPanel[]     operatorArray;

		@FXML MySlider		feedBack1;
		@FXML MySlider		feedBack2;
		@FXML MySlider		sliderLFO;
		@FXML MySlider		sliderBO;


		@FXML ComboBox<String> toneSelectBox;
		ObservableList<String> toneOptions;

		@FXML ComboBox<String>	channelSelectBox;
		ObservableList<String> channelOptions;

		@FXML ComboBox<String>	AlgoSelectBox;
		ObservableList<String> algoOptions;



		@FXML Button	changePairButton;
		@FXML Button	copyToneButton;
		@FXML Button	swapToneButton;

		@FXML Button	eepchIncButton;
		@FXML Button	eepchDecButton;
		@FXML Button	eepReadButton;
		@FXML Button	eepWeiteButton;

		@FXML Button	resetLeft;
		@FXML Button	leftDtInc;
		@FXML Button	leftDtTlChange;

		@FXML Button	readFromTextButton;
		@FXML Button	writeToTextButton;

		@FXML CheckBox envelopeViewer;
		@FXML CheckBox sysExSelect;
		@FXML CheckBox forDomino;
		@FXML CheckBox leftParamSel;



		@FXML TextField		eepch;
		@FXML TextField		toneDataText;

		@FXML HBox			fourOpBox;

		

		/*
		 * SeneBuilder用 OperatorPanelの代わり
		 */
		@FXML Pane  dummy1;
		@FXML Pane  dummy2;
		@FXML Pane  dummy3;
		@FXML Pane  dummy4;

		@FXML private MenuFieldController menuControll;
		static PanelController parent;
		
		static Ymf825ToneData  toneData;
		private byte[] toneMemory = new byte[30];

		private int currentChannel = 0;
		private	 int eeprom_ch = 0;
		private DefaultTone defaultTone = new DefaultTone();
		
		private Stage envelopeEditor;
		private Parent envelopeRoot;
		private FXMLLoader envelopeLoader;
		


		public PanelController()throws IOException{


			envelopeLoader = new FXMLLoader(getClass().getResource("envelopeViewer.fxml"));
			envelopeRoot = (Parent)envelopeLoader.load();
			envelopeEditor	= new Stage();
			envelopeEditor.setScene(new Scene(envelopeRoot));
			envelopeEditor.setResizable(false);		//リサイズ不可
			envelopeEditor.setAlwaysOnTop(true);
			envelopeEditor.setTitle("Envelope Viewer");
			envelopeEditor.setOnCloseRequest((e) -> {
			    e.consume(); // consume()でEventをストップ
			});
		}



		public void initialize() {

			DefaultTone ymf825Tone = new DefaultTone();

			/* アルゴリズム選択ComboBoxの初期化 */
			algoOptions = FXCollections.observableArrayList();
			for(int i = 0;i < 8;i++){
				String target = ("image/" + i +".png");
				algoOptions.add(target);
			}
			AlgoSelectBox.setItems(algoOptions);
			AlgoSelectBox.setCellFactory(c->new StatusListCell());
			AlgoSelectBox.setButtonCell(new StatusListCell());
			AlgoSelectBox.setValue(algoOptions.get(0));
			
	        /* channel Select Box 初期化 */
			channelOptions = FXCollections.observableArrayList();
			for(int i = 1;i<17;i++) {
				String target = ("CH" + i);
				channelOptions.add(target);
			}
			channelSelectBox.setItems(channelOptions);
			channelSelectBox.setValue("CH1");

			toneOptions = FXCollections.observableArrayList();
			for(int i = 0; i < 128;i++) {
				toneOptions.add(ymf825Tone.getToneName(i));
			}
			toneSelectBox.setItems(toneOptions);


			toneData = Ymf825ToneData.getInstance();
			//toneData.addListener(this);

			operatorArray = new OperatorPanel[4];
			operatorArray[0] = operator1;
			operatorArray[1] = operator2;
			operatorArray[2] = operator3;
			operatorArray[3] = operator4;
			operator1.addListener(this);
			operator2.addListener(this);
			operator3.addListener(this);
			operator4.addListener(this);

			feedBack1.addEventHandler(MySliderEvent.MYCHANGE_VALUE,
					event->	changeValue(null,eventSource.FeedBK,0,  event.getEventValue())	);
			feedBack2.addEventHandler(MySliderEvent.MYCHANGE_VALUE,
					event->	changeValue(null,eventSource.FeedBK2,2,event.getEventValue() 	));
			sliderLFO.addEventHandler(MySliderEvent.MYCHANGE_VALUE,
					event->	changeValue(null,eventSource.Lfo,0,  event.getEventValue()  )	);
			sliderBO.addEventHandler(MySliderEvent.MYCHANGE_VALUE,
					event->	changeValue(null,eventSource.BO,0,  event.getEventValue()    )	);

			
			Ymf825ToneData.getInstance().attach(this);

			forDomino.setVisible(false);
			setPanel();
			parent = this;
		}


/* ------------------------------------------*/

		@FXML void changeChannel() {
			int i = channelOptions.indexOf(channelSelectBox.getValue());
			channelSelectBox.setPromptText(channelOptions.get(i));
			currentChannel = i;
			setPanel();
			toneData.setEditChannel(currentChannel);
		}
		@FXML void changePareChannel() {
			currentChannel += 8;
			if(currentChannel >15) {
				currentChannel -= 16;
				
			}

			channelSelectBox.setValue(channelOptions.get(currentChannel));
			channelSelectBox.setPromptText(channelOptions.get(currentChannel));			
			setPanel();
			toneData.setEditChannel(currentChannel);
		}
		
		static public int getPanelChannel() { /* singletonモデルでは無いがインスタンスは1つなのでさぼった*/
			return parent.currentChannel;
		}		
		
		
		@FXML void changeAlgo() {
			int i = algoOptions.indexOf(AlgoSelectBox.getValue());
			if(i < 2) {
				fourOpBox.setVisible(false);
			}else {
				fourOpBox.setVisible(true);
			}
			changeValue(null,eventSource.Connect,0,i);
			toneData.setEditChannel(currentChannel); //現在編集中のチャンネル通知		


		}
		@FXML void changeTone() {
			int i = toneOptions.indexOf(toneSelectBox.getValue());
			toneSelectBox.setPromptText(toneOptions.get(i));
			byte buf[] = new byte[30];
			defaultTone.getDefTone825(i, buf);
			toneData.setTone(currentChannel, buf);
			setPanel();

		}
		
		
		@FXML void envelopeViewerShow() {
			if(envelopeViewer.isSelected()== true) {
				envelopeEditor.show();
				toneData.setEditChannel(currentChannel); //現在編集中のチャンネル通知
			}else {
				envelopeEditor.hide();
				
			}
			
			
		}

/*  ------------ EEPROM action --------------- */
		/* EEPROMからカレントチャンネルへEEPROMtextフィールドナンバーの音色を読み込む*/
		@FXML void  eepromReadData(){
			byte [] buf = new byte[32];
			int ch;

			ch =  Integer.parseInt(eepch.getText());

				if(ch != 0){
					eeprom_ch = ch -1;
										
					toneData.get_eepreg(eeprom_ch,buf);
					toneData.setTone(currentChannel, buf);
					//ymf.resetAlg(channelNo);
					setPanel();
				}
		}
				
		/* EEPROMへカレントチャンネルデータを書きこむ*/
		@FXML  int eepromWrite(){
			int ch;
			ch =  Integer.parseInt(eepch.getText());
			if(ch ==0){
				return 1;
			}
			ch--;
			toneData.writeEeprom(currentChannel,ch);

			return 0;

		}
		
		@FXML void incEepCh(){
			eeprom_ch++;
			if(eeprom_ch > 31) {
				eeprom_ch = 0;
			}
			eepch.setText("" + (eeprom_ch + 1));
		}
		
		@FXML void decEepCh(){
			eeprom_ch--;
			if(eeprom_ch < 0) {
				eeprom_ch = 31;
			}
			eepch.setText("" + (eeprom_ch + 1));
			
		}
/*  ------------ copy swap Tone Editing------- */

		@FXML void copyTone() {
			//System.out.println("copyTone");
			byte[] data = new byte[30];
			
			toneData.getToneData(currentChannel,data);
			for(int i = 0;i<30;i++) {
				toneMemory[i] = data[i];
			}
			setPanel();
		}


		@FXML void swapTone(){
			//System.out.println("swap tone");
			byte[] data = new byte[30];			
			toneData.getToneData(currentChannel,data);
			toneData.setTone(currentChannel, toneMemory);
			for(int i = 0;i<30;i++) {
				toneMemory[i] = data[i];
			}
			setPanel();

		}



/* -------     DT TL tone effect  -------   */
		@FXML void leftDtChange() {
			for(int i = 0;i < 16;i++) {
				toneData.left_dt_add(i);
			}

		}

		@FXML void leftDtTlChange() {
			for(int i = 0;i < 16;i++) {
				toneData.left_dttl_add(i);
			}


		}

		@FXML void leftParamReset() {
			for(int i = 0;i < 16;i++) {
				toneData.reset_left_param(i);
			}

		}

		@FXML void leftParamOnly() {
			if(leftParamSel.isSelected()){
				toneData.SelectLeftChannelToneOnly(true);

			}else{
				toneData.SelectLeftChannelToneOnly(false);

			}
			
		}

/* --------- text area option ---------- */
		@FXML void readFromText() {
			/* テキストフィールドからカレントチャンネルへ音色データを読み込む　*/

				byte [] buf = new byte[30];
				String s;
				String str = toneDataText.getText();
				String[] token = str.split(",",0);
				int cnt = token.length;
				int data,k,l;
				if(cnt >= 30 ){
					if(cnt < 41){
						for(int i =0;i < cnt;i++){
							s = token[i].trim();
							buf[i] =(byte) (0x00ff & Integer.parseInt(s));
						}
					}else{
						for(int i = 0;i<30;i++){
							buf[i] = 0;
						}


						s = token[0].trim();


						data = Integer.parseInt(s,16);
						buf[0] = (byte)(0x00ff & ((data & 0x60)>>5));
						buf[1] = (byte)(0x00ff & ((data & 0x18)<<3));
						buf[1]|= (byte)(0x00ff & (data & 0x07));
						for(int j = 0;j<4;j++){
							k = 7 * j;
							l = 10 * j;
							s = token[1+l].trim(); // Key Control
							data = Integer.parseInt(s,16);
							buf[8+k] |= (byte)(0x00ff & ((data & 0x70 )>> 4));	//FB
							buf[2+k] |= (byte)(0x00ff & (data & 0x08 ));		//XOF
							buf[2+k] |= (byte)(0x00ff & ((data & 0x04)>>2));	//KSR
							buf[5+k] |= (byte)(0x00ff & ((data & 0x03)));		//KSL

							s = token[2+l].trim(); // Attack Rate
							data = Integer.parseInt(s,16);
							buf[4+k] |= (byte)(0x00ff & ((data & 0x0f)<<4));	//AR


							s = token[3+l].trim(); // Decay Rate
							data = Integer.parseInt(s,16);
							buf[3+k] |= (byte)(0x00ff & ((data & 0x0f)));	//DR

							s = token[4+l].trim(); // Sustain Rate
							data = Integer.parseInt(s,16);
							buf[2+k] |= (byte)(0x00ff & ((data & 0x0f)<<4));	//SR

							s = token[5+l].trim(); // Release Rate
							data = Integer.parseInt(s,16);
							buf[3+k] |= (byte)(0x00ff & ((data & 0x0f)<<4));	//RR

							s = token[6+l].trim(); // Sustain Level
							data = Integer.parseInt(s,16);
							buf[4+k] |= (byte)(0x00ff & (data & 0x0f));	//SL

							s = token[7+l].trim(); // Total Level
							data = Integer.parseInt(s,16);
							buf[5+k] |= (byte)(0x00ff & ((data & 0x3f)<<2));	//TL

							s = token[8+l].trim(); // Modulation
							data = Integer.parseInt(s,16);
							buf[6+k] |= (byte)(0x00ff & data );	//DAM EAM DVB EVB

							s = token[9+l].trim(); // Pitch
							data = Integer.parseInt(s,16);
							buf[7+k] |= (byte)(0x00ff & ((data & 0x0f)<<4));	//MT
							buf[7+k] |= (byte)(0x00ff & ((data & 0x70)>>4));	//DT

							s = token[10+l].trim(); // Wave Shape
							data = Integer.parseInt(s,16);
							buf[8+k] |= (byte)(0x00ff & ((data & 0x1f)<<3));	//WS
						}



					}
					toneData.setOpData(currentChannel, buf);
				}
				setPanel();
			}
	

		@FXML void writeToText() {
			/* テキストフィールドへカレントチャンネルの音色データを書きだす */
			

			if(sysExSelect.isSelected() == true){
				setcopypasteDataSysEx();
			}else{
				setcopypastDataRow();
			}
		}

		
		private void setcopypastDataRow(){
			byte [] buf = new byte[30];
			StringBuilder st = new StringBuilder();
			toneData.getToneData(currentChannel, buf);
			int cnt = 2;

			st.append( String.format("%3d,",buf[0]));
			st.append(String.format("%3d,",(buf[1] & 0x00ff)));

			for(int i = 0;i < 4;i++){
				for(int j = 0;j<7;j++){
					st.append(String.format("%3d,",(buf[cnt++]&0x00ff)));

				}

			}
			toneDataText.setText(new String(st));



		}

		private void setcopypasteDataSysEx(){
			byte []buf = new byte[30];
			int []dat = new int[41];
			int j,k;
			int data;
			for(int i = 0;i<41;i++){
				dat[i] = 0;
			}
			StringBuilder st = new StringBuilder();

			toneData.getToneData(currentChannel, buf);

			data = 0x00ff & buf[0];
			dat[0] |= (data & 0x07) << 5;			//BO
			data = 0x00ff & buf[1];
			dat[0] |= ((data & 0xc0)>>3);	//LFO
			dat[0] |= ((data & 0x07));		//ALG



			for(int i = 0;i < 4;i++){
				j = i * 7;
				k = i *10;


				data = 0x00ff & buf[2+j];
				dat[4+k] |= ((data & 0xf0)>>4);	//SR
				dat[1+k] |= ((data & 0x08));	//XOF
				dat[1+k] |= ((data & 0x01)<<2);	//KSR

				data = 0x00ff & buf[3+j];	//RR,DR
				dat[5+k] |= ((data & 0xf0)>>4);	//RR
				dat[3+k] |= ((data & 0x0f));	//DR

				data = 0x00ff & buf[4+j];	//AR,SL
				dat[2+k] |= ((data & 0xf0)>>4);	//AR
				dat[6+k] |= ((data & 0x0f));	//SL

				data = 0x00ff & buf[5+j];	//TL,KSL
				dat[7+k] |= ((data & 0xfc)>>2);	//TL
				dat[1+k] |= ((data & 0x03));	//KSL

				data = 0x00ff & buf[6+j];	//DAM,EAM,DVB,EVB
				dat[8+k] |= ((data & 0x7f));	//

				data = 0x00ff & buf[7+j];	//MULTI,DT
				dat[9+k] |= ((data & 0xf0)>>4);	//MUL
				dat[9+k] |= ((data & 0x07)<<4);

				data = 0x00ff & buf[8+j];	//WS FB
				dat[10+k] |= ((data & 0xf8)>>3); //WS
				dat[1+k]  |= ((data & 0x07)<<4); //FB

			}
			if(forDomino.isSelected() == true){
				st.append(" 43h 7fh 02h 00h 00h ");
				st.append(String.format("%02Xh ", currentChannel));
				for(int i = 0;i <41;i++){
					st.append(String.format("%02Xh ",dat[i]));
				}
			}else{
				for(int i = 0;i<41;i++){
					st.append( String.format("%02X,",dat[i]));
				}
			}
			toneDataText.setText(new String(st));


		}
		@FXML void sysExSelect() {
			if(sysExSelect.isSelected() == true) {
				forDomino.setVisible(true);
			}else {
				forDomino.setVisible(false);
			}

		}
		@FXML void forDomino() {

		}







		@Override
		public void changeValue(EventType<MyDataEvent> e) {
			if(e == MyDataEvent.DATA_UPDATE) {
	//			setPanel();
			}
		}

		/* 値が変更された */
		@Override
		public void changeValue(EventType<MyDataEvent> e, eventSource source, int opNo,int val) {
			// TODO 自動生成されたメソッド・スタブ
			//System.out.print(source);
			//System.out.println( opNo + "=" + val);
			toneData.setValue(source, currentChannel, opNo, val);

		}






		void setPanel() {
			toneData.notifyStop(true);
			

			feedBack1.setValue(
					(double) toneData.getValue(currentChannel, 0, eventSource.FeedBK));
			feedBack2.setValue(
					(double) toneData.getValue(currentChannel, 2, eventSource.FeedBK2));
			sliderBO.setValue(
					(double) toneData.getValue(currentChannel, 0, eventSource.BO));

			AlgoSelectBox.setValue(algoOptions.get(
					toneData.getValue(currentChannel, 0, eventSource.Connect)));
			sliderLFO.setValue(
					(double) toneData.getValue(currentChannel, 0, eventSource.Lfo));

			for(int opno = 0;opno < 4;opno++) {
				operatorArray[opno].setAtack(
						(double) toneData.getValue(currentChannel, opno, eventSource.Atck));
				operatorArray[opno].setDecy(
						(double) toneData.getValue(currentChannel, opno, eventSource.Decy));
				operatorArray[opno].setSus(
						(double) toneData.getValue(currentChannel, opno, eventSource.Sus));
				operatorArray[opno].setRel(
						(double) toneData.getValue(currentChannel, opno, eventSource.Rel));
				operatorArray[opno].setMul(
						(double) toneData.getValue(currentChannel, opno, eventSource.Mul));
				operatorArray[opno].setKsl(
						(double) toneData.getValue(currentChannel, opno, eventSource.Ksl));
				operatorArray[opno].setTlv(
						(double) toneData.getValue(currentChannel, opno, eventSource.Tlv));
				operatorArray[opno].setKSR(
						1 == (double) toneData.getValue(currentChannel, opno, eventSource.Ksr));
				operatorArray[opno].setWave(
						 toneData.getValue(currentChannel, opno, eventSource.Wave));
				operatorArray[opno].setSL(
						(double) toneData.getValue(currentChannel, opno, eventSource.SL));
				operatorArray[opno].setDT(
						(double) toneData.getValue(currentChannel, opno, eventSource.DT));
				operatorArray[opno].setDAM(
						(double) toneData.getValue(currentChannel, opno, eventSource.Dam));
				operatorArray[opno].setDVB(
						(double) toneData.getValue(currentChannel, opno, eventSource.Dvb));
				operatorArray[opno].setEAM(
						1==(double) toneData.getValue(currentChannel, opno, eventSource.EAM));
				operatorArray[opno].setEVB(
						1==(double) toneData.getValue(currentChannel, opno, eventSource.EVB));
				operatorArray[opno].setXOF(
						1==(double) toneData.getValue(currentChannel, opno, eventSource.XOF));

			}

			toneData.notifyStop(false);
		}




		@Override
		public void update(EventType<MyDataEvent> e, eventSource source, int ch,int op, int val) {
			if(e == MyEvent.MyDataEvent.DATA_UPDATE) {
				setPanel();
			}
		}





}


