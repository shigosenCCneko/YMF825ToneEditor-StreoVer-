package DataClass;

import java.util.Enumeration;
import java.util.Vector;

import MyEvent.MyDataEvent;
import MyEvent.Observer;
import MyEvent.eventSource;
import application.ConectMidi;
import javafx.event.EventType;




public class Ymf825ToneData  {
static  final int  DATA_LEN = 30;
static  final int  TONSET_LEN = 480;

static final int OFS_BO 			= 0;
static final int OFS_LFO_ALG 	= 1;
static final int OFS_SR_XOF_KSR	= 2;
static final int OFS_RR_DR 		= 3;
static final int OFS_AR_SL 		= 4;
static final int OFS_TL_KSL		= 5;
static final int OFS_DAMEAM_DVBEVB = 6;
static final int OFS_MULTI_DT 	= 7;
static final int OFS_WS_FB		= 8;


	private byte toneData[] = new byte[TONSET_LEN];
//	private MyDataListener listener;
	private ConectMidi midiDev;
	private int editChannelNo = 0;
	private boolean continuousSoundMode = false;
	private int noteNo;
	private boolean notifyStop = false;


	Vector<Observer> observers;

	private Ymf825ToneData() {

		midiDev= new ConectMidi();
		toneDataInit();
		observers = null;

	}

/* Singleton 定義 */
	public static class Ymf825ToneDataInstanceHolder{
		private static final Ymf825ToneData INSTANCE = new Ymf825ToneData();


	}

	public static Ymf825ToneData getInstance() {
		return Ymf825ToneDataInstanceHolder.INSTANCE;
	}


/* 変更通知 */
	public void attach(Observer o) {
		if(observers == null) {
			observers = new Vector<Observer>();
		}
		observers.addElement(o);
	}

	public void detatch(Observer o) {
		observers.removeElement(o);

	}

	public void setEditChannel(int ch){
		notifyChange(MyDataEvent.CHANGECHANNEL,eventSource.ToneChange,0,0,0);
		editChannelNo = ch;


	}
	public int getEditChannel() {
		return editChannelNo;
	}


	public void left_dt_add(int ch){
		midiDev.left_dt_add( ch);
	}

	public void left_dttl_add(int ch){
		midiDev.left_dttl_add( ch);
	}

	public void reset_left_param(int ch){
		midiDev.reset_left_param( ch);

	}
	public void SelectLeftChannelToneOnly(boolean i) {
		midiDev.SelectLeftChannelToneOnly(i);
	}

	public void get_eepreg(int ch,byte[] buf){
		//get_usb_ymreg(ch,buf);
		midiDev.get_eepreg(ch,buf);
	}


	public void writeEeprom(int ch,int eepromCh){
		int i;
		byte[] buf = new byte[DATA_LEN];

		getToneData(ch,buf);
		for(i = 0;i<DATA_LEN;i++){
			midiDev.write_eepreg(eepromCh,i,buf[i]);


		}
	}

	public void get_tonememory(int ch,byte[] buf){
		midiDev.get_tonememory(ch, buf);
	}

	public void get_softwaremodulation_parameter(int ch,byte[] buf){
		midiDev.get_softwaremodulation_parameter(ch, buf);
	}


	public int getAlgorithmNo(int ch){
		int i;
		i =  getValue(ch,0,eventSource.Connect);
		return i;

	}



	public  byte[] getToneDataSet() {
		return toneData;

	}

	public   void getToneData(int no, byte[] buf) {
			no = no * DATA_LEN;
			for(int i = 0; i < DATA_LEN ; i++) {
				buf[i] = toneData[no + i];
			}

	}

	public void setOpData(int ch,  byte buf[]){		/* Panelから書き込む場合 */
		int adr,i;
		adr = ch * DATA_LEN;
		for(i = 0;i < DATA_LEN;i++){
			toneData[adr+ i] = buf[i];
			midiDev.write_tonearray(adr+i,(0x00ff & buf[i]));

		}
		midiDev.writeBurstToneReg();

	}


	public  void setTone(int ch,byte data[]) {
		int adr = ch * DATA_LEN;
		for(int i = 0; i < DATA_LEN;i ++) {
			toneData[adr + i] = data[i];
			midiDev.write_tonearray(i, data[i]);
		}
		midiDev.writeBurstToneReg();
//		listener.changeValue( MyDataEvent.DATA_UPDATE);	//Panelへ通知
		notifyChange(MyDataEvent.DATA_UPDATE,eventSource.ToneChange,0,0,0);

	}

	public  void setToneSet(byte data[]) {
		for(int i = 0; i<TONSET_LEN;i++) {
			toneData[i] = data[i];
			midiDev.write_tonearray(i, data[i]);
		}
		midiDev.writeBurstToneReg();
//		listener.changeValue( MyDataEvent.DATA_UPDATE);	//Panelへ通知
		notifyChange(MyDataEvent.DATA_UPDATE,eventSource.ToneChange,0,0,0);
	}


	/* get parameter value */
	public  int getValue(int ch,int opno, eventSource source) {
		int val  = 0;
		int offset = ch * DATA_LEN + opno * 7;

		switch(source) {

		case Atck:
			val = toneData[OFS_AR_SL + offset];
			val >>= 4;
			val &= 0x000f;
			break;

		case Decy:
			val = toneData[OFS_RR_DR + offset];
			val &= 0x000f;
			break;

		case Sus:
			val = toneData[OFS_SR_XOF_KSR + offset];
			val >>= 4;
			val &= 0x000f;
			break;

		case Rel:
			val = toneData[OFS_RR_DR + offset];

			val >>= 4;
			val &= 0x000f;
			break;

		case Mul:
			val = toneData[OFS_MULTI_DT + offset];
			val >>= 4;
			val &= 0x000f;
			break;

		case Ksl:
			val = toneData[OFS_TL_KSL + offset];
			val &= 0x0003;
			break;

		case Tlv:
			val = toneData[OFS_TL_KSL + offset];
			val &= 0x00fc;
			val >>= 2;
			break;

		case Ksr:
			val = toneData[OFS_SR_XOF_KSR + offset];
			val &= 0x0001;
			break;

		case Wave:
			val = toneData[OFS_WS_FB + offset];
			val &= 0x00f8;
			val >>= 3;
			break;

		case FeedBK:
		case FeedBK2:
			val = toneData[OFS_WS_FB + offset];
			val &= 0x0007;
			break;

		case BO:
			val = toneData[OFS_BO + offset] & 0x0003;
			break;

		case Connect:
			val = toneData[OFS_LFO_ALG + offset];
			val &= 0x0007;
			break;

		case SL:
			val = toneData[OFS_AR_SL + offset];
			val &= 0x000f;
			break;

		case DT:
			val = toneData[OFS_MULTI_DT + offset];
			val &= 0x0007;
			break;

		case Lfo:
			val = toneData[OFS_LFO_ALG + offset];
			val >>= 6;
			val &= 0x0003;
			break;

		case Dam:
			val = toneData[OFS_DAMEAM_DVBEVB + offset];
			val &= 0x0060;
			val >>= 5;
			break;

		case Dvb:
			val = toneData[OFS_DAMEAM_DVBEVB + offset];
			val &= 0x0006;
			val >>= 1;
			break;

		case EAM:
			val = toneData[OFS_DAMEAM_DVBEVB + offset];
			val &= 0x0010;
			val >>= 4;
			val &= 0x0001;
			break;

		case EVB:
			val = toneData[OFS_DAMEAM_DVBEVB + offset];
			val &= 0x0001;
			break;


		case XOF:
			val = toneData[OFS_SR_XOF_KSR + offset];
			val &= 0x0008;
			val >>= 3;
			break;


		default:
			break;

		}
		return val;
	}





	/* set parameter valur */

	public void notifyStop(boolean i) {
		notifyStop = i;
	}

	public void setTraceTlv(eventSource source,int ch,int opno,int val) {
		int adr,data;
		int offset = ch * DATA_LEN + opno * 7;

		if(source == eventSource.Tlv) {
			adr = OFS_TL_KSL + offset;
			data = toneData[adr];
			data &= 0x0003;
			val <<= 2;
			data |= val;
			//toneData[adr] = (byte) data;
			midiDev.set_tonedata(adr,data);

		}
	}

	public  void setValue(eventSource source,int ch, int opno, int val) {
		int offset = ch * DATA_LEN + opno * 7;
		int adr,data;
		adr = 0;
		val &= 0x00ff;

		if(notifyStop == false) {
			notifyChange(MyDataEvent.OPDATA_CHANGE,source,ch,opno,val);
		}

			switch(source) {

			case Atck:

				adr = OFS_AR_SL + offset;
				data = toneData[adr];
				data &= 0x000f;
				val <<= 4;
				data |= val;
				toneData[adr] = (byte) data;
				break;

			case Decy:
				adr = OFS_RR_DR + offset;
				data = toneData[adr];
				data &= 0x00f0;
				val &= 0x000f;
				data |= val;
				toneData[adr] = (byte) data;
				break;

			case Sus:
				adr = OFS_SR_XOF_KSR + offset;
				data = toneData[adr];
				data &= 0x000f;
				val <<= 4;
				data |= val;
				toneData[adr] = (byte) data;
				break;

			case Rel:
				adr = OFS_RR_DR + offset;
				data = toneData[adr];
				data &= 0x000f;
				val &= 0x000f;
				val <<= 4;
				data |= val;
				toneData[adr] = (byte)data;
				break;

			case Mul:
				adr = OFS_MULTI_DT + offset;
				data = toneData[adr];
				data &= 0x000f;
				val <<= 4;
				data |= val;
				toneData[adr] = (byte)data;
				break;

			case Ksl:
				adr = OFS_TL_KSL + offset;
				data = toneData[adr];
				data &= 0x00f8;
				data |= val;
				toneData[adr] = (byte)data;
				break;

			case Tlv:
				adr = OFS_TL_KSL + offset;
				data = toneData[adr];
				data &= 0x0003;
				val <<= 2;
				data |= val;
				toneData[adr] = (byte) data;
				break;

			case Ksr:
				adr = OFS_SR_XOF_KSR + offset;
				data = toneData[adr];
				data &= 0x00fe;
				data |= val;
				toneData[adr] = (byte)data;
				break;

			case Wave:
				adr = OFS_WS_FB + offset;
				data = toneData[adr];
				data &= 0x0007;
				val <<= 3;
				data |= val;
				toneData[adr] = (byte) data;
				break;

			case FeedBK:
			case FeedBK2:
				adr = OFS_WS_FB + offset;
				data = toneData[adr];
				data &= 0x00f8;
				data |= val;
				toneData[adr] = (byte) data;
				break;

			case BO:
				adr = OFS_BO + offset;
				data = val & 0x003;
				toneData[adr] = (byte)data;

				break;

			case Connect:
				adr = OFS_LFO_ALG + offset;
				data = toneData[adr];
				data &= 0x00f8;
				data |= val;
				toneData[adr] = (byte)data;
				break;

			case SL:
				adr = OFS_AR_SL + offset;
				data = toneData[adr];
				data &= 0x00f0;
				data |= val;
				toneData[adr] = (byte)data;
				break;

			case DT:
				adr = OFS_MULTI_DT + offset;
				data = toneData[adr];
				data &= 0x00f8;
				data |= val;
				toneData[adr] = (byte)data;
				break;

			case Lfo:
				adr = OFS_LFO_ALG + offset;
				data = toneData[adr];
				data &= 0x003f;
				val <<= 6;
				data |= val;
				toneData[adr]= (byte)data;

				break;

			case Dam:
				adr = OFS_DAMEAM_DVBEVB + offset;
				data = toneData[adr];
				data &= 0x009f;
				val <<= 5;
				data |= val;
				toneData[adr] = (byte)data;
				break;

			case Dvb:
				adr = OFS_DAMEAM_DVBEVB + offset;
				data = toneData[adr];
				data &= 0x00f9;
				val <<= 1;
				data |= val;
				toneData[adr] = (byte)data;
				break;

			case EAM:
				adr = OFS_DAMEAM_DVBEVB + offset;
				data = toneData[adr];
				data &= 0x00ef;
				val &= 0x0001;
				val <<= 4;
				data |= val;
				toneData[adr] = (byte)data;
				break;

			case EVB:
				adr = OFS_DAMEAM_DVBEVB + offset;
				data = toneData[adr];
				data &= 0x00fe;
				val &= 0x0001;
				data |= val;
				toneData[adr] = (byte)data;
				break;



			case XOF:
				adr = OFS_SR_XOF_KSR + offset;
				data = toneData[adr];
				data &= 0x00f7;
				val <<= 3;
				data |= val;
				toneData[adr] = (byte)data;
				break;



			default:
				return;
				//break;

			}
			
			if(continuousSoundMode == false) {

					midiDev.set_tonedata(adr, data);

			}else {
				if((source != eventSource.Atck) && (source != eventSource.Decy) && (source != eventSource.Sus)) {
					midiDev.set_tonedata(adr,data);
				}
			}


	}



	void toneDataInit(){

			for(int i= 0;i <16;i++){
				toneData[(DATA_LEN*i)] = 0x01;	//BO
				toneData[(DATA_LEN*i)+1] =(byte) 0x83;	//LFO,ALG

				toneData[(DATA_LEN*i)+2] = 0x00;  //sr,xof,ks4r
				toneData[(DATA_LEN*i)+3] = 0x7F;	//RR,DR
				toneData[(DATA_LEN*i)+4] = (byte) 0xF4;	//ar,sl
				toneData[(DATA_LEN*i)+5] = (byte) 0xBB;	//TL,KSL
				toneData[(DATA_LEN*i)+6] = 0x00;	//DAM,EAM,DVB,EVB
				toneData[(DATA_LEN*i)+7] = 0x10;	//MUL,DT
				toneData[(DATA_LEN*i)+8] = 0x40;	//WS,FB

				toneData[(DATA_LEN*i)+9] = 0x00;
				toneData[(DATA_LEN*i)+10] = (byte) 0xAF;
				toneData[(DATA_LEN*i)+11] = (byte) 0xA0;
				toneData[(DATA_LEN*i)+12] = 0x0E;
				toneData[(DATA_LEN*i)+13] = 0x03;
				toneData[(DATA_LEN*i)+14] = 0x10;
				toneData[(DATA_LEN*i)+15] = 0x40;

				toneData[(DATA_LEN*i)+16] = 0x00;
				toneData[(DATA_LEN*i)+17] = 0x2F;
				toneData[(DATA_LEN*i)+18] = (byte) 0xF3;
				toneData[(DATA_LEN*i)+19] = (byte) 0x9B;
				toneData[(DATA_LEN*i)+20] = 0x00;
				toneData[(DATA_LEN*i)+21] = 0x20;
				toneData[(DATA_LEN*i)+22] = 0x41;

				toneData[(DATA_LEN*i)+23] = 0x00;  //sr,xof,ks4r
		//toneData[(YMF825DATLEN*i)+24] = 0xAF;
				toneData[(DATA_LEN*i)+24] = 0x7F;
				toneData[(DATA_LEN*i)+25] = (byte) 0xA0;
				toneData[(DATA_LEN*i)+26] = 0x0E;
				toneData[(DATA_LEN*i)+27] = 0x01;
				toneData[(DATA_LEN*i)+28] = 0x10;
				toneData[(DATA_LEN*i)+29] = 0x40;

			}

			midiDev.wait_millsec(5);		//FOR USB-HID
			for(int i = 0;i < TONSET_LEN;i++){
				midiDev.write_tonearray(i,toneData[i]);
				midiDev.wait_millsec(1);
			}
			midiDev.writeBurstToneReg();

	}

	/* ----------------- 演奏モード設定 ----------*/

	public void monoMode() {
		midiDev.playMode(0);
	}

	public void polyMode() {
		midiDev.playMode(1);
	}

	public void d8polyMode() {
		midiDev.playMode(3);
	}

/* -------------------------------------------- */


public void noteOn(int ch,int noteNo,int vel){
	midiDev.send_command(16,ch,noteNo,vel);
}

public void noteOff(int ch,int noteNo){
	midiDev.send_command(17,ch,noteNo,0);
}


public void setContinuousSoundMode(boolean i,int no){
	continuousSoundMode = i;
	noteNo = no;
	//midiDev.setContinuousSoundMode(i);
	if(i == true) {
		midiDev.noteOn(editChannelNo, noteNo, 120);
	}else {
		midiDev.noteOff(editChannelNo, noteNo);
	}
}
public void setContinuousToneNo(int val) {
	midiDev.noteOff(editChannelNo, noteNo);
	midiDev.noteOn(editChannelNo, val, 120);
	noteNo = val;

}
/* ---------- Software modulation function ----- */

public void SmodulationSeneAllParameter(int channel,int sinPitch
		,int sinDepth,int waveNo,int modulateRate,int delayValue) {
	
	
	midiDev.send_command(19, channel, sinPitch,0);
	midiDev.send_command(20, channel, sinDepth*2, 0);		
	midiDev.send_command(21, channel, waveNo, 0);
	midiDev.send_command(22, channel, modulateRate, 0);
	midiDev.send_command(23, channel, delayValue, 0);	
}

public void changeSmodulation(int midiChannelNo,int modulation) {
	midiDev.send_command(18, midiChannelNo, modulation,0);
}

public void changeSinPitch(int midiChannelNo,int sinPitch) {
	midiDev.send_command(19, midiChannelNo, sinPitch,0);
}

public void changeSinDepth(int midiChannelNo,int sinDepth) {
	midiDev.send_command(20, midiChannelNo , sinDepth*2, 0);
}

public void changeSmodulationWaveTable(int midiChannelNo, int waveNo) {
	midiDev.send_command(21,midiChannelNo, waveNo, 0);
}

public void changeSmodulateRate(int midiChannelNo, int modulateRate) {
	midiDev.send_command(22, midiChannelNo, modulateRate, 0);
}

public void changeSmodulateDelay(int midiChannelNo,int delayValue) {
	midiDev.send_command(23, midiChannelNo, delayValue, 0);
}


/* --------------------------------------------- */
	public void close() {
		midiDev.close();
	}

	public void reset() {
		midiDev.YmReset();
		toneDataInit();
	}

//	public void addListener(MyDataListener listener) {
//		this.listener = listener;
//	}

	public void notifyChange(EventType<MyDataEvent> e,eventSource source,int ch, int op,int val) {
		for(Enumeration<Observer> i = observers.elements(); i.hasMoreElements();) {
			Observer o = i.nextElement();
			o.update( e,source ,ch, op, val);
		}
	}

}

