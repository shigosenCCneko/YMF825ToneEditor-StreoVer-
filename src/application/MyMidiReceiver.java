package application;

import javax.sound.midi.MidiMessage;

public class MyMidiReceiver implements javax.sound.midi.Receiver {
	ConectMidi ymf;
	
	public MyMidiReceiver(ConectMidi ymf825){
	ymf = ymf825;	
	System.out.println("set Receiver");
	}


	@Override
	public void close() {
		// TODO 自動生成されたメソッド・スタブ
			System.out.println("midi Recevi END");
	}

	@Override
	public void send(MidiMessage arg0, long arg1) {
		byte [] buf ;
		// TODO 自動生成されたメソッド・スタブ
		//System.out.println(arg0);
		buf = arg0.getMessage();

		ymf.sys_exmes_recv(buf);
	}

}
