package application;

import javax.sound.midi.MidiMessage;

public class MyMidiReceiver implements javax.sound.midi.Receiver {
	ConectMidi ymf;
	
	public MyMidiReceiver(ConectMidi ymf825){
	ymf = ymf825;	

	}


	@Override
	public void close() {

	}

	@Override
	public void send(MidiMessage arg0, long arg1) {
		byte [] buf ;
		buf = arg0.getMessage();

		ymf.sys_exmes_recv(buf);
	}

}
