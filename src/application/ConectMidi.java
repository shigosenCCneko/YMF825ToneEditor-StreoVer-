package application;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Transmitter;

public class ConectMidi {
	
	
	private final int  YMF825DATLEN = 30;

	private MidiDevice midiInput;
	private MidiDevice midiOutput;
	private Receiver midiSendReceiver;
	private Transmitter midiReceivTransmitter;
	private int midiIn,midiOut;


	private String midiInDeviceName = "aMIDIIN2 (LUFA Dual MIDI Demo)";
//	private static String midiInDeviceName = "Keystation Mini 32)";
	private String midiOutDeviceName ="aMIDIOUT2 (LUFA Dual MIDI Demo)";
	private static String midiInDevices [];
	private static String midiOutDevices[];
	private byte midiExmesBuff[] = new byte[30];
	
	//private boolean continuousSoundMode = false;

	
	public enum Readtype{eeprom,tonememory,softwaremodulation};	

	

	public ConectMidi() {
		//dumpDeviceInfo();
		getProperties();	
		connect_midi();

		YmReset();
	}
	
	public void wait_millsec(int i){
		try{
			Thread.sleep(i);

		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}
	public void YmReset(){
		//usbInit();
		wait_millsec(100);
		send_command(0,0,0,0);

		wait_millsec(700);



		//ymRegInit();
		wait_millsec(100);


	}
	
	
	public void send_command(int command,int ch,int dat1,int dat2){
		midi_send_command(command,ch,dat1,dat2);
	}
	
	
	
	public void set_tonedata(int addr,int data){


		int ch;
		
		ch = addr/YMF825DATLEN;
		addr = addr - ch * YMF825DATLEN;

		send_command(10,ch,addr,data);

}
	public void writeBurstToneReg(){
		send_command(9,0,0,0);
	}	
	public void write_tonearray(int addr,int data){

			int ch,adr;
			ch = addr/YMF825DATLEN;
			adr = addr - ch * YMF825DATLEN;

			send_command(11,ch,adr,data);

	}	
	
	
	

	public void get_eepreg(int ch,byte[] buf){
		//get_usb_ymreg(ch,buf);
		get_memof_ymreg(ch,buf,Readtype.eeprom);
	}
	public void write_eepreg(int ch,int ofs,byte data) {
		wait_millsec(10);
		send_command(5,ch,ofs,data);
	}

	public void get_tonememory(int ch,byte[] buf){
		get_memof_ymreg(ch,buf,Readtype.tonememory);
	}
	
	public void get_softwaremodulation_parameter(int ch,byte[] buf){
		get_memof_ymreg(ch,buf,Readtype.softwaremodulation);
	}
		
	
	
	
	private synchronized void get_memof_ymreg(int ch,byte[] buf,Readtype type){
		switch(type){
		case eeprom:
			send_command(6,ch,0,0);
			break;
		
		case tonememory:
			send_command(7,ch,0,0);
			break;
		case softwaremodulation:
			send_command(8,ch,0,0);
		}


		try{
			this.wait();	//返信があるまでスレッド停止

		}catch (InterruptedException e){
			e.printStackTrace();
		}

		for(int i = 0;i < YMF825DATLEN;i++){
			buf[i] = midiExmesBuff[i];
		}

	}

	/* システムエクルシーブメッセージが来たらMyMidiReceiverから呼ばれるメソッド
	 * 音色データの呼び出しに使う
	 */

	public synchronized void sys_exmes_recv(byte[] buf){
		int p = 0;
		for(int i = 1;i < 61;i+=2){
			midiExmesBuff[p++] = (byte) ((buf[i]<<4)+buf[i+1]);
		}

		this.notifyAll();

	}
	
	
	
	
	public static void dumpDeviceInfo(){
		ArrayList<MidiDevice> devices = getDevices();
		midiInDevices = new String[devices.size()];
		midiOutDevices = new String[devices.size()];
		int recev,trans;
		for(int i = 0;i< devices.size();i++){
			MidiDevice device = devices.get(i);
			MidiDevice.Info info = device.getDeviceInfo();
			System.out.println(info.toString());


			recev = device.getMaxReceivers();
			trans = device.getMaxTransmitters();

				if( recev == 0){
					System.out.println("Transmit Only");
					midiInDevices[i] = info.toString();
				}
				if( trans == 0){
					System.out.println("receiv ONLY");
					midiOutDevices[i] = info.toString();
				}

			//System.out.println(info.getDescription());


	        System.out.println("[" + i + "] devinfo: " + info.toString());
	    //    System.out.println("  name:"        + info.getName());
	    //    System.out.println("  vendor:"      + info.getVendor());
	    //    System.out.println("  version:"     + info.getVersion());
	     //   System.out.println("  description:" + info.getDescription());



			System.out.println("");


		}
	}
	
	public static ArrayList<MidiDevice> getDevices(){
		ArrayList<MidiDevice> devices = new ArrayList<MidiDevice>();

		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();

		for(int i =0;i < infos.length;i++){
			MidiDevice.Info info = infos[i];
			MidiDevice dev = null;

			try{
				dev = MidiSystem.getMidiDevice(info);
				devices.add(dev);
			}catch(SecurityException e){
				System.err.println(e.getMessage());

			}catch (MidiUnavailableException e){
				System.err.println(e.getMessage());
			}
		}
		return devices;

	}
	
	public String[] getMidiInDeviceList(){
		return midiInDevices;
	}
	public String[] getMidiOutDeviceList(){
		return midiOutDevices;
	}


	public static int getMidiDeviceNo(String inf,ArrayList<MidiDevice>devices){

		for(int i = 0;i< devices.size();i++){
			MidiDevice device = devices.get(i);
			MidiDevice.Info info = device.getDeviceInfo();
			if(info.toString().equals(inf)){
				return i;
			}
		}
		return -1;
	}
	
	
	private void connect_midi(){
		ArrayList<MidiDevice> devices = getDevices();
		MidiDevice dev;




		midiIn = getMidiDeviceNo(midiInDeviceName,devices);
		dev = devices.get(midiIn);
		if(midiOutDeviceName.contentEquals(midiInDeviceName)){
			if(dev.getMaxTransmitters() == 0){

				midiOut = midiIn;
				for(int i = midiOut +1 ;i <devices.size();i++){
					MidiDevice device = devices.get(i);
					MidiDevice.Info info = device.getDeviceInfo();
					if(info.toString().contentEquals(midiOutDeviceName)){
						midiIn = i;
						break;
					}
				}
			}else{
				for(int i = midiIn +1 ;i <devices.size();i++){
					MidiDevice device = devices.get(i);
					MidiDevice.Info info = device.getDeviceInfo();
					if(info.toString().contentEquals(midiOutDeviceName)){
						midiOut = i;
						break;
					}
				}

			}

		}else{
			midiOut =  getMidiDeviceNo(midiOutDeviceName,devices);

		}


	//	System.out.println(midiIn);
	//	System.out.println(midiOut);

		midiInput = devices.get(midiIn);
		midiOutput = devices.get(midiOut);


		try{

			if(! midiInput.isOpen()){
				midiInput.open();
			}

			if(! midiOutput.isOpen()){
				midiOutput.open();
			}



			//Transmitter trans = midi_input.getTransmitter();
			midiSendReceiver = midiOutput.getReceiver();
			MyMidiReceiver myrecv = new MyMidiReceiver(this);
			//trans.setReceiver(myrecv);
			midiReceivTransmitter = midiInput.getTransmitter();
			midiReceivTransmitter.setReceiver(myrecv);

		} catch(MidiUnavailableException e){
			System.err.println(e.getMessage());
		}

		


	}
	
	
	public void close() {
		//System.out.println("close midi device");
		midiReceivTransmitter.close();
		midiSendReceiver.close();
		midiInput.close();
		midiOutput.close();
	}
	
	void midi_send_command(int command,int ch,int dat1,int dat2){

		byte buff[] = new byte[12];



		buff[0] = (byte)0xf0;
		buff[9] = (byte)0xf7;

		buff[1] = (byte)((command & 0xf0)>>4);
		buff[2] = (byte)(command & 0x0f);

		buff[3] = (byte)((ch & 0xf0)>>4);
		buff[4] = (byte)(ch & 0x0f);

		buff[5] = (byte)((dat1 & 0xf0)>>4);
		buff[6] = (byte)(dat1 & 0x0f);

		buff[7] = (byte)((dat2 & 0xf0)>>4);
		buff[8] = (byte)(dat2 & 0x0f);
		send_exmessage(buff,10);

	}
	
	public void send_exmessage(byte[] buf,int len){
		SysexMessage sxsm = new SysexMessage();
		try {
			sxsm.setMessage(buf,len);
			midiSendReceiver.send(sxsm,midiOut);

		} catch (InvalidMidiDataException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}



	}




	private void getProperties(){
		Properties properties = new Properties();

		try{
			InputStream istream = new FileInputStream("ymf825.properties");
			properties.load(istream);
			midiInDeviceName = properties.getProperty("midiInDeviceName");
			midiOutDeviceName = properties.getProperty("midiOutDeviceName");

			istream.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
	
	
	public void left_dt_add(int ch){
		 send_command( 12,ch,0,0);
	}

	public void left_dttl_add(int ch){
			send_command(13,ch,0,0);
	}

	public void reset_left_param(int ch){
			send_command(15,ch,0,0);

	}
	
	public void SelectLeftChannelToneOnly(boolean i){
		if(i == true){
			send_command(14,1,0,0);
		}else{
			send_command(14,0,0,0);
		}
	}

	public void noteOn(int ch,int noteNo,int vel){
		send_command(16,ch,noteNo,vel);
	}

	public void noteOff(int ch,int noteNo){
		send_command(17,ch,noteNo,0);
	}


	//public void setContinuousSoundMode(boolean i){
	//	continuousSoundMode = i;
		
	//}

	public void playMode(int i){
		send_command(2,i,0,0);

		for(int ch = 0;ch < 16;ch++){
			//resetAlg(i);   // ymfのアルゴリズム配列の再構築
		}



	}


	
	
	
	
	
	
	
	
	
}
