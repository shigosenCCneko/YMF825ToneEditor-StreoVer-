package application;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.stage.Window;



public class TfftViewController implements Observer{










	GraphicsContext g1;
	GraphicsContext g2;
	GraphicsContext gfft1;
	GraphicsContext gfft2;

	@FXML Button closeButton;

	@FXML Button testrecord;
	@FXML Button testwrite;

	@FXML Button testrecord2;
	@FXML Button testwrite2;

	@FXML Canvas canvas1;
	@FXML Canvas canvas2;

	@FXML Canvas fftCanvas1;
	@FXML Canvas fftCanvas2;


	@FXML Slider positionSlider1;
	@FXML Label mag1;
	@FXML Slider magSlider1;
	@FXML Slider yMagSlider1;
	@FXML Slider fftSlider1;


	@FXML Slider positionSlider2;
	@FXML Label mag2;
	@FXML Slider magSlider2;
	@FXML Slider yMagSlider2;
	@FXML Slider fftSlider2;


	@FXML RadioButton syncFFT;
	@FXML RadioButton realTimeFFT;



	final int SAMPRING_RATE = 44000;
	final int viewCenter = 300;
	final int N_WAVE = 4096;
	final int LOG2_N_WAVE = 12;
	final int FFTviewStep = 4;

	int FFTsampringMag =4;
	  RealTimeFFT realTimeFft;


	WaveRecord audioRec1 = new WaveRecord(16,1,SAMPRING_RATE,2);
	WaveRecord audioRec2 = new WaveRecord(16,1,SAMPRING_RATE,2);
	boolean syncFFTchannel = false;
	int syncFFToffset = 0;

	
	final  int stepSize = 512;
	AudioInput auin;
	byte[] auBuf = new byte[stepSize * 2];
	byte[] auBuf1 = new byte[stepSize * 2];
	byte[] auBuf2 = new byte[stepSize * 2];
	
	byte[] readbuf;
	byte[] writebuf;
	
	
	int fftMag = 30;

	class WaveRecord {
		byte [] audioBuf;
		int audioBit;
		int audioChannel;
		int audioHz;
		double audioSec;

		int wavePosition;
		int waveMag;
		double waveYMag;

		int fttStartPos;
		int []fft;


		WaveRecord(int bitwidth, int channelval, int samplingRate, double bufTime){
			audioBuf = new byte[(int)((bitwidth/8) * channelval * samplingRate *bufTime)];

			audioBit = bitwidth;
			audioChannel = channelval;
			audioHz = samplingRate;
			audioSec = bufTime;

			wavePosition = 0;
			waveMag = 50;
			waveYMag = 32;

			fttStartPos = 0;
			fft = new int[N_WAVE];


		}








	}

	@FXML void initialize(){
		readbuf = auBuf1;
		writebuf = auBuf2;
		g1 = canvas1.getGraphicsContext2D();
		g2 = canvas2.getGraphicsContext2D();

		gfft1 = fftCanvas1.getGraphicsContext2D();
		gfft2 = fftCanvas2.getGraphicsContext2D();

		positionSlider1.valueProperty().addListener((
	        	ObservableValue<? extends Number> ov,Number old_val,
	    		Number new_val) ->{
	    			audioRec1.wavePosition = new_val.intValue();
	    			changePosition(audioRec1,g1);
	    	});


		magSlider1.valueProperty().addListener((
	        	ObservableValue<? extends Number> ov,Number old_val,
	    		Number new_val) ->{

	    			int premag = old_val.intValue();
	    			int mag = new_val.intValue();
	    			int pos = audioRec1.wavePosition;
	    			int ofs = mag * viewCenter - premag * viewCenter ;

	    			mag1.setText(Integer.toString(mag+1));

	    			pos = pos - ofs;
	    			if(pos <= 0) pos = 0;
	    			audioRec1.wavePosition = pos;

	    			positionSlider1.setValue(pos);

	    			audioRec1.waveMag = new_val.intValue()+1;
	    			changeMag(audioRec1,g1);
	    	});

		yMagSlider1.valueProperty().addListener((
	        	ObservableValue<? extends Number> ov,Number old_val,
	    		Number new_val) ->{
	    			audioRec1.waveYMag = new_val.doubleValue()+1;
	    			changeYMag(audioRec1,g1);
	    	});

		fftSlider1.valueProperty().addListener((
	        	ObservableValue<? extends Number> ov,Number old_val,
	    		Number new_val) ->{
	    			audioRec1.fttStartPos = new_val.intValue();
	    			waveFft(audioRec1,gfft1);


	    	});



		positionSlider2.valueProperty().addListener((
	        	ObservableValue<? extends Number> ov,Number old_val,
	    		Number new_val) ->{
	    			audioRec2.wavePosition = new_val.intValue();
	    			changePosition(audioRec2,g2);
	    	});


		magSlider2.valueProperty().addListener((
	        	ObservableValue<? extends Number> ov,Number old_val,
	    		Number new_val) ->{

	    			int premag = old_val.intValue();
	    			int mag = new_val.intValue();
	    			mag2.setText(Integer.toString(mag+1));
	    			int pos = audioRec2.wavePosition;
	    			int ofs = mag * viewCenter - premag * viewCenter ;
	    			pos = pos - ofs;
	    			if(pos <= 0) pos = 0;
	    			audioRec2.wavePosition = pos;

	    			positionSlider2.setValue(pos);

	    			audioRec2.waveMag = new_val.intValue()+1;
	    			changeMag(audioRec2,g2);
	    	});

		yMagSlider2.valueProperty().addListener((
	        	ObservableValue<? extends Number> ov,Number old_val,
	    		Number new_val) ->{
	    			audioRec2.waveYMag = new_val.doubleValue()+1;
	    			changeYMag(audioRec2,g2);
	    	});

		fftSlider2.valueProperty().addListener((
	        	ObservableValue<? extends Number> ov,Number old_val,
	    		Number new_val) ->{
	    			audioRec2.fttStartPos = new_val.intValue();
	    			waveFft(audioRec2,gfft2);

	    		if(syncFFTchannel) {
	    			int pos = new_val.intValue() + syncFFToffset;
	    			if(pos >= 0 ) {
	    				audioRec1.fttStartPos = pos;
	    				waveFft(audioRec1,gfft1);
	    			}

	    		}



	    	});




	}















































	@Override
	public void update(EventType<MyDataEvent> e, eventSource source, int ch, int op, int val) {
		// TODO 自動生成されたメソッド・スタブ

	}


	@FXML
	void onCloseAction(ActionEvent event) {

		Scene scene = ((Node) event.getSource()).getScene();
		Window window = scene.getWindow();
		window.hide();

	}

	private class AudioInput{
		AudioFormat fmt;
		TargetDataLine dataline;
		AudioInputStream inputStream;

		AudioInput(int bit,int channel,int hz){
			try {
				fmt = new AudioFormat(hz,bit,channel,true,false);
				DataLine.Info info = new DataLine.Info(TargetDataLine.class, fmt);
				dataline = (TargetDataLine)AudioSystem.getLine(info);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}

		void start() {
			try {
				dataline.open(fmt);
			}catch(LineUnavailableException e) {
				System.err.println("cannot open audio");
			}
			dataline.start();
			inputStream = new AudioInputStream(dataline);
		}

		int read(byte[] buf) {
			return read(buf  ,0,buf.length);
		}

		int read(byte[] buf,int offset, int length) {
			int n = 0;
			try {
				n = inputStream.read(buf,offset,length);

			}catch(IOException e) {
				System.err.println("audio read error");
			}
			return n;
		}

		void stop() {
			dataline.stop();
			dataline.close();
		}

	}

	private class AudioOutput{
		AudioFormat fmt;
		SourceDataLine dataline;

		AudioOutput(int bit, int channel,int hz){
			try {
				fmt = new AudioFormat(hz,bit,channel,true,false);
				DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
				dataline = (SourceDataLine)AudioSystem.getLine(info);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}

		void start() {
			try {
				dataline.open(fmt);
			}catch(LineUnavailableException e) {
				System.err.println("cannot open audio");

			}
			dataline.start();
		}

		int write(byte[] data) {
			return write(data,0,data.length);
		}
		int write(byte[] data, int offset, int length) {
			return dataline.write(data, offset, length);
		}
		void stop() {
			dataline.drain();
			dataline.stop();
			dataline.close();

		}
	}

	@FXML void testRecord(){
		unsetFFTsync();
		waveRecord(audioRec1,g1);

		waveFft(audioRec1,gfft1);



	}

	@FXML void testRecord2(){
		unsetFFTsync();
		waveRecord(audioRec2,g2);

		waveFft(audioRec2,gfft2);
	}

	void waveRecord(WaveRecord rec ,GraphicsContext g) {
		byte [] buf = rec.audioBuf;
		AudioInput in = new AudioInput(rec.audioBit,rec.audioChannel,rec.audioHz);
		in.start();
		in.read(buf);
		in.stop();

		waveClear(g);
		drawWave(rec,g);

	}

	void changePosition(WaveRecord rec, GraphicsContext g) {
			waveClear(g);
		drawWave(rec,g);
	}

	void changeMag(WaveRecord rec, GraphicsContext g){
		waveClear(g);
		drawWave(rec,g);

	}

	private void changeYMag(WaveRecord rec, GraphicsContext g) {
		waveClear(g);
		drawWave(rec,g);

	}


	void drawWave(WaveRecord rec, GraphicsContext g) {
		int size =  rec.audioBuf.length;
		byte [] buf = rec.audioBuf;



		int px = 0;
		int pdata = 0;
		int x = 0;
		for(int i = rec.wavePosition * 2;i < size ;i+= rec.waveMag * 2) {
			int data = ( (int)(buf[i+1] <<8) | (0x00ff & buf[i]));


			data /= rec.waveYMag;

			g.strokeLine(px, pdata+60, x, data+60);
			pdata = data;
			px = x;
			x+= 1;


		}
	}

	@FXML void testWrite() {
		waveWrite(audioRec1);
	}


	@FXML void testWrite2() {
		waveWrite(audioRec2);

	}

	void waveWrite(WaveRecord rec) {
		AudioOutput out = new AudioOutput(rec.audioBit,rec.audioChannel,rec.audioHz);
		out.start();
		out.write(rec.audioBuf);
		out.stop();

	}

  private void waveClear(GraphicsContext g) {
		g.clearRect(0, 0, 1000, 200);
		g.strokeLine(0, 60, 800, 60);
  }

// FFT 別スレッド -------------------------------------------------


  @FXML void realTimeFft(){
	  if(realTimeFFT.isSelected()) {
		  realTimeFft = new RealTimeFFT();
		  realTimeFft.start();

	  }else {
//		  System.out.println("interrupt send");
//		  System.out.println(realTimeFft);
		  realTimeFft.exit();
//		  realTimeFft.interrupt();
		  auin.stop(); 
	  }

  }
  
  class DataRead implements Runnable{

	  public void run() {

			  auin.read(writebuf);

	  }
	  

	  
  }

  class RealTimeFFT extends Thread{
	  boolean loopContinue = true;
	  int[] fftRecord = new int[4096];
	  int readChannel = 0;
	  WaveRecord rec = audioRec2;
	  int writePos = 0;
	//  int readBufSize = 256;
	  
	  

//	  byte[] readBuf = new byte[stepSize *2];

	  int [] source = new int[N_WAVE];
	  int [] target = new int[N_WAVE];	  
	  
	  public void exit() {
			loopContinue = false;
	  }


	  public void run() {
		  

		  DataRead dr = new DataRead();
		  Thread t;
		  
		  
		  
//		  AudioInput in = new AudioInput(rec.audioBit,rec.audioChannel,rec.audioHz/FFTsampringMag*2);
//in.start();
		auin = new AudioInput(rec.audioBit,rec.audioChannel,rec.audioHz/FFTsampringMag*2);
auin.start();
	  		while(loopContinue) {

	  		//auin.read(readBuf);
	  		t = new Thread(dr);
	  		t.start();	


	  		int data;
	  		for(int i = 0;i < stepSize * 2; i+=2) {
	  			 data = ( (int)(readbuf[i+1] <<8) | (0x00ff & readbuf[i]));
// data >>= 3;
	  			fftRecord[writePos++] = data;
	  			//if(writePos == N_WAVE) writePos = 0;
	  			writePos &= 0xfff;
	  			
	  		}
	  		int p = writePos;
	  		for(int i =0;i<N_WAVE;i++) {
	  			source[i] = fftRecord[p++];
	  			p &= 0xfff;
	  			target[i] = 0;
	  		}
	  		fix_fft2(source,target);

	  	    	int j,k;
	  	    for (int i = 0; i <  (N_WAVE/2) ; i++) {

	  	        j = source[i];
	  	        k = target[i] ;
	  	        source[i] =(int) Math.sqrt(j*j+k*k);
	  	    }
	  		gfft2.clearRect(0, 0, 530, 400);
	  		int offset = 4;
	  		int a,b;
	  		
	  		for(int i = 1;i < (N_WAVE-FFTviewStep-offset);i+= FFTviewStep) {
	  			
	//  	    	int fdata = (source[i+offset]+source[i+offset+1]+source[i+offset+2]+source[i+offset+3])/4;
	//  			gfft2.strokeLine(5+i/FFTviewStep, 110-fdata, 5+i/FFTviewStep, 110);
	  			
	  			if(source[i+offset] > source[i+offset+1]) {
	  				a = source[i+offset];
	  			}else {
	  				a = source[i+offset+1];
	  			}
	  			if(source[i+offset+2] > source[i+offset+3]) {
	  				b = source[i+offset+2];
	  			}else {
	  				b = source[i+offset+3];
	  			}
	  			if(a < b) {
	  				a = b;
	  			}
	  			a = (int) Math.sqrt((double)a * fftMag);
	  	    	
	  			
	  			
	  			
	  	    	gfft2.strokeLine(5+i/FFTviewStep, 110- a, 5+i/FFTviewStep, 110);
	  	  			
	  			
	  			
	  			
	  		}
	  		try {
	  			t.join();
	  		}catch(InterruptedException e) {
	  			
	  		}
	  		byte[] pre = readbuf;
	  		readbuf = writebuf;
	  		writebuf = pre;
	  		

	  	}

//-------
auin.stop(); 
  }

  }

  // FFT-------------------------------------------


  @FXML void setFFTmag2(){
	  FFTsampringMag = 2;
  }

  @FXML void setFFTmag4() {
	  FFTsampringMag = 4;
  }

  @FXML void setFFTmag8() {
	  FFTsampringMag = 8;
  }


  void unsetFFTsync() {
	  syncFFT.setSelected(false);
	  syncFFTchannel = false;


  }

@FXML void setFFTsync() {
	if(syncFFT.isSelected() ) {
		syncFFTchannel = true;
		syncFFToffset = (int) (fftSlider1.getValue() - fftSlider2.getValue());
	}else {
		syncFFTchannel = false;
		syncFFToffset = 0;
	}


}

void drawFft(GraphicsContext g, int data[]) {
	g.clearRect(0, 0, 530, 400);
	int offset = 4;
	int a,b;
	
	for(int i = 1;i < (N_WAVE-FFTviewStep-offset);i+= FFTviewStep) {
//    	int fdata = (data[i+offset]+data[i+offset+1]+data[i+offset+2]+data[i+offset+3])/4;
//		g.strokeLine(5+i/FFTviewStep, 110-fdata, 5+i/FFTviewStep, 110);
		
		if(data[i+offset] > data[i+offset+1]) {
			a = data[i+offset];
		}else {
			a = data[i+offset+1];
		}
		if(data[i+offset+2] > data[i+offset+3]) {
			b = data[i+offset+2];
		}else {
			b = data[i+offset+3];
		}
		if(a < b) {
			a = b;
		}
a = (int) Math.sqrt((double)a * fftMag);  	
    	g.strokeLine(5+i/FFTviewStep, 110- a, 5+i/FFTviewStep, 110);
	}
}

void waveFft(WaveRecord rec, GraphicsContext g) {
	int [] source = new int[N_WAVE];
	int [] target = new int[N_WAVE];
	byte[]  buf = rec.audioBuf;
	int pos = rec.fttStartPos;
	int data;
	for(int i = 0;i < N_WAVE; i++) {
		 data = ( (int)(buf[pos*2+i*FFTsampringMag+1] <<8) | (0x00ff & buf[pos*2+i*FFTsampringMag]));
//data >>= 3;
		source[i] = data;
		target[i] = 0;


	}
	fix_fft2(source,target);

    	int j,k;
    for (int i = 0; i <  (N_WAVE/2) ; i++) {

        j = source[i];
        k = target[i] ;
        rec.fft[i] =(int) Math.sqrt(j*j+k*k);
    }
    drawFft(g,rec.fft);
//
//    for(int i = 1;i < 127;i++) {
//    	int fdata = source[i];
//    	g.strokeLine(600+i,120-fdata,600+i,120);
//    }

}




//#define N_WAVE      256    /* full length of Sinewave[] */
//#define LOG2_N_WAVE 8      /* log2(N_WAVE) */




/*
  Since we only use 3/4 of N_WAVE, we define only
  this many samples, in order to conserve data space.
*/



int SineWaveN_WAVE[] = {
		   0,   0,   0,   1,   1,   1,   1,   1,   2,   2,   2,   2,   2,   3,   3,   3,
		   3,   3,   4,   4,   4,   4,   4,   4,   5,   5,   5,   5,   5,   6,   6,   6,
		   6,   6,   7,   7,   7,   7,   7,   8,   8,   8,   8,   8,   9,   9,   9,   9,
		   9,  10,  10,  10,  10,  10,  11,  11,  11,  11,  11,  11,  12,  12,  12,  12,
		  12,  13,  13,  13,  13,  13,  14,  14,  14,  14,  14,  15,  15,  15,  15,  15,
		  16,  16,  16,  16,  16,  17,  17,  17,  17,  17,  17,  18,  18,  18,  18,  18,
		  19,  19,  19,  19,  19,  20,  20,  20,  20,  20,  21,  21,  21,  21,  21,  22,
		  22,  22,  22,  22,  22,  23,  23,  23,  23,  23,  24,  24,  24,  24,  24,  25,
		  25,  25,  25,  25,  26,  26,  26,  26,  26,  26,  27,  27,  27,  27,  27,  28,
		  28,  28,  28,  28,  29,  29,  29,  29,  29,  30,  30,  30,  30,  30,  30,  31,
		  31,  31,  31,  31,  32,  32,  32,  32,  32,  33,  33,  33,  33,  33,  33,  34,
		  34,  34,  34,  34,  35,  35,  35,  35,  35,  36,  36,  36,  36,  36,  36,  37,
		  37,  37,  37,  37,  38,  38,  38,  38,  38,  39,  39,  39,  39,  39,  39,  40,
		  40,  40,  40,  40,  41,  41,  41,  41,  41,  41,  42,  42,  42,  42,  42,  43,
		  43,  43,  43,  43,  44,  44,  44,  44,  44,  44,  45,  45,  45,  45,  45,  46,
		  46,  46,  46,  46,  46,  47,  47,  47,  47,  47,  48,  48,  48,  48,  48,  48,
		  49,  49,  49,  49,  49,  49,  50,  50,  50,  50,  50,  51,  51,  51,  51,  51,
		  51,  52,  52,  52,  52,  52,  53,  53,  53,  53,  53,  53,  54,  54,  54,  54,
		  54,  54,  55,  55,  55,  55,  55,  56,  56,  56,  56,  56,  56,  57,  57,  57,
		  57,  57,  57,  58,  58,  58,  58,  58,  58,  59,  59,  59,  59,  59,  60,  60,
		  60,  60,  60,  60,  61,  61,  61,  61,  61,  61,  62,  62,  62,  62,  62,  62,
		  63,  63,  63,  63,  63,  63,  64,  64,  64,  64,  64,  64,  65,  65,  65,  65,
		  65,  65,  66,  66,  66,  66,  66,  66,  67,  67,  67,  67,  67,  67,  68,  68,
		  68,  68,  68,  68,  69,  69,  69,  69,  69,  69,  70,  70,  70,  70,  70,  70,
		  71,  71,  71,  71,  71,  71,  72,  72,  72,  72,  72,  72,  72,  73,  73,  73,
		  73,  73,  73,  74,  74,  74,  74,  74,  74,  75,  75,  75,  75,  75,  75,  75,
		  76,  76,  76,  76,  76,  76,  77,  77,  77,  77,  77,  77,  78,  78,  78,  78,
		  78,  78,  78,  79,  79,  79,  79,  79,  79,  80,  80,  80,  80,  80,  80,  80,
		  81,  81,  81,  81,  81,  81,  81,  82,  82,  82,  82,  82,  82,  83,  83,  83,
		  83,  83,  83,  83,  84,  84,  84,  84,  84,  84,  84,  85,  85,  85,  85,  85,
		  85,  85,  86,  86,  86,  86,  86,  86,  86,  87,  87,  87,  87,  87,  87,  87,
		  88,  88,  88,  88,  88,  88,  88,  89,  89,  89,  89,  89,  89,  89,  90,  90,
		  90,  90,  90,  90,  90,  90,  91,  91,  91,  91,  91,  91,  91,  92,  92,  92,
		  92,  92,  92,  92,  93,  93,  93,  93,  93,  93,  93,  93,  94,  94,  94,  94,
		  94,  94,  94,  94,  95,  95,  95,  95,  95,  95,  95,  96,  96,  96,  96,  96,
		  96,  96,  96,  97,  97,  97,  97,  97,  97,  97,  97,  98,  98,  98,  98,  98,
		  98,  98,  98,  99,  99,  99,  99,  99,  99,  99,  99, 100, 100, 100, 100, 100,
		 100, 100, 100, 100, 101, 101, 101, 101, 101, 101, 101, 101, 102, 102, 102, 102,
		 102, 102, 102, 102, 102, 103, 103, 103, 103, 103, 103, 103, 103, 103, 104, 104,
		 104, 104, 104, 104, 104, 104, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105,
		 106, 106, 106, 106, 106, 106, 106, 106, 106, 107, 107, 107, 107, 107, 107, 107,
		 107, 107, 108, 108, 108, 108, 108, 108, 108, 108, 108, 108, 109, 109, 109, 109,
		 109, 109, 109, 109, 109, 109, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110,
		 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 112, 112, 112, 112, 112,
		 112, 112, 112, 112, 112, 112, 113, 113, 113, 113, 113, 113, 113, 113, 113, 113,
		 113, 114, 114, 114, 114, 114, 114, 114, 114, 114, 114, 114, 114, 115, 115, 115,
		 115, 115, 115, 115, 115, 115, 115, 115, 115, 116, 116, 116, 116, 116, 116, 116,
		 116, 116, 116, 116, 116, 116, 117, 117, 117, 117, 117, 117, 117, 117, 117, 117,
		 117, 117, 117, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118,
		 118, 119, 119, 119, 119, 119, 119, 119, 119, 119, 119, 119, 119, 119, 119, 120,
		 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 121,
		 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121,
		 122, 122, 122, 122, 122, 122, 122, 122, 122, 122, 122, 122, 122, 122, 122, 122,
		 122, 122, 123, 123, 123, 123, 123, 123, 123, 123, 123, 123, 123, 123, 123, 123,
		 123, 123, 123, 123, 123, 123, 123, 124, 124, 124, 124, 124, 124, 124, 124, 124,
		 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 125,
		 125, 125, 125, 125, 125, 125, 125, 125, 125, 125, 125, 125, 125, 125, 125, 125,
		 125, 125, 125, 125, 125, 125, 125, 125, 125, 125, 125, 125, 126, 126, 126, 126,
		 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126,
		 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126,
		 126, 126, 126, 126, 126, 126, 126, 127, 127, 127, 127, 127, 127, 127, 127, 127,
		 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127,
		 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127,
		 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127,
		 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127,
		 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127,
		 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127,
		 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 126, 126, 126, 126, 126, 126,
		 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126,
		 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126,
		 126, 126, 126, 126, 126, 125, 125, 125, 125, 125, 125, 125, 125, 125, 125, 125,
		 125, 125, 125, 125, 125, 125, 125, 125, 125, 125, 125, 125, 125, 125, 125, 125,
		 125, 125, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124,
		 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 123, 123, 123, 123, 123, 123,
		 123, 123, 123, 123, 123, 123, 123, 123, 123, 123, 123, 123, 123, 123, 123, 122,
		 122, 122, 122, 122, 122, 122, 122, 122, 122, 122, 122, 122, 122, 122, 122, 122,
		 122, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121,
		 121, 121, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120,
		 120, 120, 119, 119, 119, 119, 119, 119, 119, 119, 119, 119, 119, 119, 119, 119,
		 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 118, 117, 117,
		 117, 117, 117, 117, 117, 117, 117, 117, 117, 117, 117, 116, 116, 116, 116, 116,
		 116, 116, 116, 116, 116, 116, 116, 116, 115, 115, 115, 115, 115, 115, 115, 115,
		 115, 115, 115, 115, 114, 114, 114, 114, 114, 114, 114, 114, 114, 114, 114, 114,
		 113, 113, 113, 113, 113, 113, 113, 113, 113, 113, 113, 112, 112, 112, 112, 112,
		 112, 112, 112, 112, 112, 112, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111,
		 111, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 109, 109, 109, 109, 109,
		 109, 109, 109, 109, 109, 108, 108, 108, 108, 108, 108, 108, 108, 108, 108, 107,
		 107, 107, 107, 107, 107, 107, 107, 107, 106, 106, 106, 106, 106, 106, 106, 106,
		 106, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 104, 104, 104, 104, 104,
		 104, 104, 104, 103, 103, 103, 103, 103, 103, 103, 103, 103, 102, 102, 102, 102,
		 102, 102, 102, 102, 102, 101, 101, 101, 101, 101, 101, 101, 101, 100, 100, 100,
		 100, 100, 100, 100, 100, 100,  99,  99,  99,  99,  99,  99,  99,  99,  98,  98,
		  98,  98,  98,  98,  98,  98,  97,  97,  97,  97,  97,  97,  97,  97,  96,  96,
		  96,  96,  96,  96,  96,  96,  95,  95,  95,  95,  95,  95,  95,  94,  94,  94,
		  94,  94,  94,  94,  94,  93,  93,  93,  93,  93,  93,  93,  93,  92,  92,  92,
		  92,  92,  92,  92,  91,  91,  91,  91,  91,  91,  91,  90,  90,  90,  90,  90,
		  90,  90,  90,  89,  89,  89,  89,  89,  89,  89,  88,  88,  88,  88,  88,  88,
		  88,  87,  87,  87,  87,  87,  87,  87,  86,  86,  86,  86,  86,  86,  86,  85,
		  85,  85,  85,  85,  85,  85,  84,  84,  84,  84,  84,  84,  84,  83,  83,  83,
		  83,  83,  83,  83,  82,  82,  82,  82,  82,  82,  81,  81,  81,  81,  81,  81,
		  81,  80,  80,  80,  80,  80,  80,  80,  79,  79,  79,  79,  79,  79,  78,  78,
		  78,  78,  78,  78,  78,  77,  77,  77,  77,  77,  77,  76,  76,  76,  76,  76,
		  76,  75,  75,  75,  75,  75,  75,  75,  74,  74,  74,  74,  74,  74,  73,  73,
		  73,  73,  73,  73,  72,  72,  72,  72,  72,  72,  72,  71,  71,  71,  71,  71,
		  71,  70,  70,  70,  70,  70,  70,  69,  69,  69,  69,  69,  69,  68,  68,  68,
		  68,  68,  68,  67,  67,  67,  67,  67,  67,  66,  66,  66,  66,  66,  66,  65,
		  65,  65,  65,  65,  65,  64,  64,  64,  64,  64,  64,  63,  63,  63,  63,  63,
		  63,  62,  62,  62,  62,  62,  62,  61,  61,  61,  61,  61,  61,  60,  60,  60,
		  60,  60,  60,  59,  59,  59,  59,  59,  58,  58,  58,  58,  58,  58,  57,  57,
		  57,  57,  57,  57,  56,  56,  56,  56,  56,  56,  55,  55,  55,  55,  55,  54,
		  54,  54,  54,  54,  54,  53,  53,  53,  53,  53,  53,  52,  52,  52,  52,  52,
		  51,  51,  51,  51,  51,  51,  50,  50,  50,  50,  50,  49,  49,  49,  49,  49,
		  49,  48,  48,  48,  48,  48,  48,  47,  47,  47,  47,  47,  46,  46,  46,  46,
		  46,  46,  45,  45,  45,  45,  45,  44,  44,  44,  44,  44,  44,  43,  43,  43,
		  43,  43,  42,  42,  42,  42,  42,  41,  41,  41,  41,  41,  41,  40,  40,  40,
		  40,  40,  39,  39,  39,  39,  39,  39,  38,  38,  38,  38,  38,  37,  37,  37,
		  37,  37,  36,  36,  36,  36,  36,  36,  35,  35,  35,  35,  35,  34,  34,  34,
		  34,  34,  33,  33,  33,  33,  33,  33,  32,  32,  32,  32,  32,  31,  31,  31,
		  31,  31,  30,  30,  30,  30,  30,  30,  29,  29,  29,  29,  29,  28,  28,  28,
		  28,  28,  27,  27,  27,  27,  27,  26,  26,  26,  26,  26,  26,  25,  25,  25,
		  25,  25,  24,  24,  24,  24,  24,  23,  23,  23,  23,  23,  22,  22,  22,  22,
		  22,  22,  21,  21,  21,  21,  21,  20,  20,  20,  20,  20,  19,  19,  19,  19,
		  19,  18,  18,  18,  18,  18,  17,  17,  17,  17,  17,  17,  16,  16,  16,  16,
		  16,  15,  15,  15,  15,  15,  14,  14,  14,  14,  14,  13,  13,  13,  13,  13,
		  12,  12,  12,  12,  12,  11,  11,  11,  11,  11,  11,  10,  10,  10,  10,  10,
		   9,   9,   9,   9,   9,   8,   8,   8,   8,   8,   7,   7,   7,   7,   7,   6,
		   6,   6,   6,   6,   5,   5,   5,   5,   5,   4,   4,   4,   4,   4,   4,   3,
		   3,   3,   3,   3,   2,   2,   2,   2,   2,   1,   1,   1,   1,   1,   0,   0,
		   0,   0,   0,   0,   0,   0,   0,   0,  -1,  -1,  -1,  -1,  -1,  -2,  -2,  -2,
		  -2,  -2,  -3,  -3,  -3,  -3,  -3,  -3,  -4,  -4,  -4,  -4,  -4,  -5,  -5,  -5,
		  -5,  -5,  -6,  -6,  -6,  -6,  -6,  -7,  -7,  -7,  -7,  -7,  -8,  -8,  -8,  -8,
		  -8,  -9,  -9,  -9,  -9,  -9, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11,
		 -11, -12, -12, -12, -12, -12, -13, -13, -13, -13, -13, -14, -14, -14, -14, -14,
		 -15, -15, -15, -15, -15, -16, -16, -16, -16, -16, -16, -17, -17, -17, -17, -17,
		 -18, -18, -18, -18, -18, -19, -19, -19, -19, -19, -20, -20, -20, -20, -20, -21,
		 -21, -21, -21, -21, -21, -22, -22, -22, -22, -22, -23, -23, -23, -23, -23, -24,
		 -24, -24, -24, -24, -25, -25, -25, -25, -25, -25, -26, -26, -26, -26, -26, -27,
		 -27, -27, -27, -27, -28, -28, -28, -28, -28, -29, -29, -29, -29, -29, -29, -30,
		 -30, -30, -30, -30, -31, -31, -31, -31, -31, -32, -32, -32, -32, -32, -32, -33,
		 -33, -33, -33, -33, -34, -34, -34, -34, -34, -35, -35, -35, -35, -35, -35, -36,
		 -36, -36, -36, -36, -37, -37, -37, -37, -37, -38, -38, -38, -38, -38, -38, -39,
		 -39, -39, -39, -39, -40, -40, -40, -40, -40, -40, -41, -41, -41, -41, -41, -42,
		 -42, -42, -42, -42, -43, -43, -43, -43, -43, -43, -44, -44, -44, -44, -44, -45,
		 -45, -45, -45, -45, -45, -46, -46, -46, -46, -46, -47, -47, -47, -47, -47, -47,
		 -48, -48, -48, -48, -48, -48, -49, -49, -49, -49, -49, -50, -50, -50, -50, -50,
		 -50, -51, -51, -51, -51, -51, -52, -52, -52, -52, -52, -52, -53, -53, -53, -53,
		 -53, -53, -54, -54, -54, -54, -54, -55, -55, -55, -55, -55, -55, -56, -56, -56,
		 -56, -56, -56, -57, -57, -57, -57, -57, -57, -58, -58, -58, -58, -58, -59, -59,
		 -59, -59, -59, -59, -60, -60, -60, -60, -60, -60, -61, -61, -61, -61, -61, -61,
		 -62, -62, -62, -62, -62, -62, -63, -63, -63, -63, -63, -63, -64, -64, -64, -64,
		 -64, -64, -65, -65, -65, -65, -65, -65, -66, -66, -66, -66, -66, -66, -67, -67,
		 -67, -67, -67, -67, -68, -68, -68, -68, -68, -68, -69, -69, -69, -69, -69, -69,
		 -70, -70, -70, -70, -70, -70, -71, -71, -71, -71, -71, -71, -71, -72, -72, -72,
		 -72, -72, -72, -73, -73, -73, -73, -73, -73, -74, -74, -74, -74, -74, -74, -74,
		 -75, -75, -75, -75, -75, -75, -76, -76, -76, -76, -76, -76, -77, -77, -77, -77,
		 -77, -77, -77, -78, -78, -78, -78, -78, -78, -79, -79, -79, -79, -79, -79, -79,
		 -80, -80, -80, -80, -80, -80, -80, -81, -81, -81, -81, -81, -81, -82, -82, -82,
		 -82, -82, -82, -82, -83, -83, -83, -83, -83, -83, -83, -84, -84, -84, -84, -84,
		 -84, -84, -85, -85, -85, -85, -85, -85, -85, -86, -86, -86, -86, -86, -86, -86,
		 -87, -87, -87, -87, -87, -87, -87, -88, -88, -88, -88, -88, -88, -88, -89, -89,
		 -89, -89, -89, -89, -89, -89, -90, -90, -90, -90, -90, -90, -90, -91, -91, -91,
		 -91, -91, -91, -91, -92, -92, -92, -92, -92, -92, -92, -92, -93, -93, -93, -93,
		 -93, -93, -93, -93, -94, -94, -94, -94, -94, -94, -94, -95, -95, -95, -95, -95,
		 -95, -95, -95, -96, -96, -96, -96, -96, -96, -96, -96, -97, -97, -97, -97, -97,
		 -97, -97, -97, -98, -98, -98, -98, -98, -98, -98, -98, -99, -99, -99, -99, -99,
		 -99, -99, -99, -99,-100,-100,-100,-100,-100,-100,-100,-100,-101,-101,-101,-101,
		-101,-101,-101,-101,-101,-102,-102,-102,-102,-102,-102,-102,-102,-102,-103,-103,
		-103,-103,-103,-103,-103,-103,-104,-104,-104,-104,-104,-104,-104,-104,-104,-104,
		-105,-105,-105,-105,-105,-105,-105,-105,-105,-106,-106,-106,-106,-106,-106,-106,
		-106,-106,-107,-107,-107,-107,-107,-107,-107,-107,-107,-107,-108,-108,-108,-108,
		-108,-108,-108,-108,-108,-108,-109,-109,-109,-109,-109,-109,-109,-109,-109,-109,
		-110,-110,-110,-110,-110,-110,-110,-110,-110,-110,-110,-111,-111,-111,-111,-111,
		-111,-111,-111,-111,-111,-111,-112,-112,-112,-112,-112,-112,-112,-112,-112,-112,
		-112,-113,-113,-113,-113,-113,-113,-113,-113,-113,-113,-113,-113,-114,-114,-114,
		-114,-114,-114,-114,-114,-114,-114,-114,-114,-115,-115,-115,-115,-115,-115,-115,
		-115,-115,-115,-115,-115,-115,-116,-116,-116,-116,-116,-116,-116,-116,-116,-116,
		-116,-116,-116,-117,-117,-117,-117,-117,-117,-117,-117,-117,-117,-117,-117,-117,
		-117,-118,-118,-118,-118,-118,-118,-118,-118,-118,-118,-118,-118,-118,-118,-119,
		-119,-119,-119,-119,-119,-119,-119,-119,-119,-119,-119,-119,-119,-119,-119,-120,
		-120,-120,-120,-120,-120,-120,-120,-120,-120,-120,-120,-120,-120,-120,-120,-120,
		-121,-121,-121,-121,-121,-121,-121,-121,-121,-121,-121,-121,-121,-121,-121,-121,
		-121,-121,-122,-122,-122,-122,-122,-122,-122,-122,-122,-122,-122,-122,-122,-122,
		-122,-122,-122,-122,-122,-122,-122,-123,-123,-123,-123,-123,-123,-123,-123,-123,
		-123,-123,-123,-123,-123,-123,-123,-123,-123,-123,-123,-123,-123,-123,-123,-124,
		-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,
		-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-125,-125,-125,-125,
		-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,
		-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,
		-125,-125,-125,-125,-125,-125,-125,-126,-126,-126,-126,-126,-126,-126,-126,-126,
		-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,
		-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,
		-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,
		-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,
		-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,
		-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,
		-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-125,-125,-125,-125,-125,-125,
		-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,
		-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,
		-125,-125,-125,-125,-125,-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,
		-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,
		-124,-124,-123,-123,-123,-123,-123,-123,-123,-123,-123,-123,-123,-123,-123,-123,
		-123,-123,-123,-123,-123,-123,-123,-123,-123,-123,-122,-122,-122,-122,-122,-122,
		-122,-122,-122,-122,-122,-122,-122,-122,-122,-122,-122,-122,-122,-122,-122,-121,
		-121,-121,-121,-121,-121,-121,-121,-121,-121,-121,-121,-121,-121,-121,-121,-121,
		-121,-120,-120,-120,-120,-120,-120,-120,-120,-120,-120,-120,-120,-120,-120,-120,
		-120,-120,-119,-119,-119,-119,-119,-119,-119,-119,-119,-119,-119,-119,-119,-119,
		-119,-119,-118,-118,-118,-118,-118,-118,-118,-118,-118,-118,-118,-118,-118,-118,
		-117,-117,-117,-117,-117,-117,-117,-117,-117,-117,-117,-117,-117,-117,-116,-116,
		-116,-116,-116,-116,-116,-116,-116,-116,-116,-116,-116,-115,-115,-115,-115,-115,
		-115,-115,-115,-115,-115,-115,-115,-115,-114,-114,-114,-114,-114,-114,-114,-114,
		-114,-114,-114,-114,-113,-113,-113,-113,-113,-113,-113,-113,-113,-113,-113,-113,
		-112,-112,-112,-112,-112,-112,-112,-112,-112,-112,-112,-111,-111,-111,-111,-111,
		-111,-111,-111,-111,-111,-111,-110,-110,-110,-110,-110,-110,-110,-110,-110,-110,
		-110,-109,-109,-109,-109,-109,-109,-109,-109,-109,-109,-108,-108,-108,-108,-108,
		-108,-108,-108,-108,-108,-107,-107,-107,-107,-107,-107,-107,-107,-107,-107,-106,
		-106,-106,-106,-106,-106,-106,-106,-106,-105,-105,-105,-105,-105,-105,-105,-105,
		-105,-104,-104,-104,-104,-104,-104,-104,-104,-104,-104,-103,-103,-103,-103,-103,
		-103,-103,-103,-102,-102,-102,-102,-102,-102,-102,-102,-102,-101,-101,-101,-101,
		-101,-101,-101,-101,-101,-100,-100,-100,-100,-100,-100,-100,-100, -99, -99, -99,
		 -99, -99, -99, -99, -99, -99, -98, -98, -98, -98, -98, -98, -98, -98, -97, -97,
		 -97, -97, -97, -97, -97, -97, -96, -96, -96, -96, -96, -96, -96, -96, -95, -95,
		 -95, -95, -95, -95, -95, -95, -94, -94, -94, -94, -94, -94, -94, -93, -93, -93,
		 -93, -93, -93, -93, -93, -92, -92, -92, -92, -92, -92, -92, -92, -91, -91, -91,
		 -91, -91, -91, -91, -90, -90, -90, -90, -90, -90, -90, -89, -89, -89, -89, -89,
		 -89, -89, -89, -88, -88, -88, -88, -88, -88, -88, -87, -87, -87, -87, -87, -87,
		 -87, -86, -86, -86, -86, -86, -86, -86, -85, -85, -85, -85, -85, -85, -85, -84,
		 -84, -84, -84, -84, -84, -84, -83, -83, -83, -83, -83, -83, -83, -82, -82, -82,
		 -82, -82, -82, -82, -81, -81, -81, -81, -81, -81, -80, -80, -80, -80, -80, -80,
		 -80, -79, -79, -79, -79, -79, -79, -79, -78, -78, -78, -78, -78, -78, -77, -77,
		 -77, -77, -77, -77, -77, -76, -76, -76, -76, -76, -76, -75, -75, -75, -75, -75,
		 -75, -74, -74, -74, -74, -74, -74, -74, -73, -73, -73, -73, -73, -73, -72, -72,
		 -72, -72, -72, -72, -71, -71, -71, -71, -71, -71, -71, -70, -70, -70, -70, -70,
		 -70, -69, -69, -69, -69, -69, -69, -68, -68, -68, -68, -68, -68, -67, -67, -67,
		 -67, -67, -67, -66, -66, -66, -66, -66, -66, -65, -65, -65, -65, -65, -65, -64,
		 -64, -64, -64, -64, -64, -63, -63, -63, -63, -63, -63, -62, -62, -62, -62, -62,
		 -62, -61, -61, -61, -61, -61, -61, -60, -60, -60, -60, -60, -60, -59, -59, -59,
		 -59, -59, -59, -58, -58, -58, -58, -58, -57, -57, -57, -57, -57, -57, -56, -56,
		 -56, -56, -56, -56, -55, -55, -55, -55, -55, -55, -54, -54, -54, -54, -54, -53,
		 -53, -53, -53, -53, -53, -52, -52, -52, -52, -52, -52, -51, -51, -51, -51, -51,
		 -50, -50, -50, -50, -50, -50, -49, -49, -49, -49, -49, -48, -48, -48, -48, -48,
		 -48, -47, -47, -47, -47, -47, -47, -46, -46, -46, -46, -46, -45, -45, -45, -45,
		 -45, -45, -44, -44, -44, -44, -44, -43, -43, -43, -43, -43, -43, -42, -42, -42,
		 -42, -42, -41, -41, -41, -41, -41, -40, -40, -40, -40, -40, -40, -39, -39, -39,
		 -39, -39, -38, -38, -38, -38, -38, -38, -37, -37, -37, -37, -37, -36, -36, -36,
		 -36, -36, -35, -35, -35, -35, -35, -35, -34, -34, -34, -34, -34, -33, -33, -33,
		 -33, -33, -32, -32, -32, -32, -32, -32, -31, -31, -31, -31, -31, -30, -30, -30,
		 -30, -30, -29, -29, -29, -29, -29, -29, -28, -28, -28, -28, -28, -27, -27, -27,
		 -27, -27, -26, -26, -26, -26, -26, -25, -25, -25, -25, -25, -25, -24, -24, -24,
		 -24, -24, -23, -23, -23, -23, -23, -22, -22, -22, -22, -22, -21, -21, -21, -21,
		 -21, -21, -20, -20, -20, -20, -20, -19, -19, -19, -19, -19, -18, -18, -18, -18,
		 -18, -17, -17, -17, -17, -17, -16, -16, -16, -16, -16, -16, -15, -15, -15, -15,
		 -15, -14, -14, -14, -14, -14, -13, -13, -13, -13, -13, -12, -12, -12, -12, -12,
		 -11, -11, -11, -11, -11, -10, -10, -10, -10, -10, -10,  -9,  -9,  -9,  -9,  -9,
		  -8,  -8,  -8,  -8,  -8,  -7,  -7,  -7,  -7,  -7,  -6,  -6,  -6,  -6,  -6,  -5,
		  -5,  -5,  -5,  -5,  -4,  -4,  -4,  -4,  -4,  -3,  -3,  -3,  -3,  -3,  -3,  -2,
		  -2,  -2,  -2,  -2,  -1,  -1,  -1,  -1,  -1,   0,   0,   0,   0,   0,   0,   0,

};



/*
  FIX_MPY() - fixed-point multiplication & scaling.
  Substitute inline assembly for hardware-specific
  optimization suited to a particluar DSP processor.
  Scaling ensures that result remains 16-bit.
*/
int FIX_MPY(int a, int b)
{

  int c = (int)a * (int)b + 64;
  a = (c >> 7);

  return a;
}
int FIX_MPY1(int a, int b, int c, int d)
{


 return( ((int)a * (int)b - (int)c *(int) d) +64 )>> 7;


}

int FIX_MPY2(int a, int b, int c, int d)
{


  return( ((int)a * (int)b + (int)c *(int) d) +64) >> 7;


}
/*
  fix_fft() - perform forward/inverse fast Fourier transform.
  fr[n],fi[n] are real and imaginary arrays, both INPUT AND
  RESULT (in-place FFT), with 0 <= n < 2**m; set inverse to
  0 for forward transform (FFT), or 1 for iFFT.
*/
int fix_fft2(int fr[], int fi[])
{

  int m, mr,  i, j, l, k, istep;
  int qr, qi, tr, ti, wr, wi;



  mr = 0;

  /* decimation in time - re-order data */
  for (m = 1; m <=(N_WAVE-1); ++m) {
    l = N_WAVE;
    do {
      l >>= 1;
    } while (mr + l >(N_WAVE -1));
    mr = (mr & (l - 1)) + l;

    if (mr <= m)
      continue;
    tr = fr[m];
    fr[m] = fr[mr];
    fr[mr] = tr;
    ti = fi[m];
    fi[m] = fi[mr];
    fi[mr] = ti;
  }

  l = 1;
  k = LOG2_N_WAVE - 1;
  while (l < N_WAVE) {

      /*
        fixed scaling, for proper normalization --
        there will be log2(n) passes, so this results
        in an overall factor of 1/n, distributed to
        maximize arithmetic accuracy.
      */


    /*
      it may not be obvious, but the shift will be
      performed on each data point exactly once,
      during this pass.
    */
    istep = l << 1;
    for (m = 0; m < l; ++m) {
      j = m << k;
      /* 0 <= j < N_WAVE/2 */
      wr =  SineWaveN_WAVE [ j + N_WAVE / 4];

      wi = -SineWaveN_WAVE[j];


      wr >>= 1;
      wi >>= 1;

      for (i = m; i < N_WAVE; i += istep) {
        j = i + l;

//        tr = FIX_MPY(wr, fr[j]) - FIX_MPY(wi, fi[j]);
 //       ti = FIX_MPY(wr, fi[j]) + FIX_MPY(wi, fr[j]);

        tr = FIX_MPY1(wr, fr[j],wi, fi[j]);
        ti = FIX_MPY2(wr, fi[j],wi, fr[j]);


        qr = fr[i];
        qi = fi[i];

        qr >>= 1;
        qi >>= 1;

        fr[j] = qr - tr;
        fi[j] = qi - ti;
        fr[i] = qr + tr;
        fi[i] = qi + ti;
      }
    }
    --k;
    l = istep;
  }
  return 0;
}



}
