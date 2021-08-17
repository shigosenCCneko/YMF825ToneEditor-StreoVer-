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
import javafx.scene.paint.Color;
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
	  RealTimeFFT realTimeFFTthread;
	  RunFFT runFFT;


	WaveRecord audioRec1 = new WaveRecord(16,1,SAMPRING_RATE,2);
	WaveRecord audioRec2 = new WaveRecord(16,1,SAMPRING_RATE,2);
	boolean syncFFTchannel = false;
	int syncFFToffset = 0;

	final   int BasicStepSize = 512;
	int stepSize = BasicStepSize;
	long	fftDrawWaitTime = 4;
	AudioInput audioin;
	byte[] auBuf = new byte[stepSize * 2];
	byte[] auBuf1 = new byte[stepSize * 2];
	byte[] auBuf2 = new byte[stepSize * 2];

	byte[] readbuf;
	byte[] writebuf;


	int fftMag = 100;

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
		if(realTimeFFTthread != null) {
			realTimeFFT.setSelected(false);
		  realTimeFFTthread.exit();
		  audioin.stop();
		}
		unsetFFTsync();
		waveRecord(audioRec1,g1);

		waveFft(audioRec1,gfft1);



	}

	@FXML void testRecord2(){
		if(realTimeFFTthread != null) {
			realTimeFFT.setSelected(false);
		  realTimeFFTthread.exit();
		  audioin.stop();
		}

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
		  realTimeFFTthread = new RealTimeFFT();
		  realTimeFFTthread.start();


	  }else {

		  realTimeFFTthread.exit();
//
		  audioin.stop();

	  }

  }

  class DataRead implements Runnable{

	  public void run() {

			  audioin.read(writebuf);


	  }



  }

  class RealTimeFFT extends Thread {
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
		audioin = new AudioInput(rec.audioBit,rec.audioChannel,rec.audioHz/FFTsampringMag*2);
audioin.start();
	  		while(loopContinue) {

	  		//auin.read(readBuf);
	  		t = new Thread(dr);
	  		t.start();


	  		int data;
	  		for(int i = 0;i < stepSize * 2 ; i+=2) {
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
	  		runFFT = new RunFFT();
	  		runFFT.start(source,target);
	  		//fix_fft2(source,target);

	  	    	int j,k;
	  	    for (int i = 0; i <  (N_WAVE/2) ; i++) {

	  	        j = source[i];
	  	        k = target[i] ;
	  	        source[i] =(int) Math.sqrt(j*j+k*k);
	  	    }
	  		gfft2.clearRect(0, 0, 530, 400);

	  		DrawFFTLine drline = new DrawFFTLine();
	  		drline.start(source);
	  	    //gfft2.setFill(Color.BLACK);
	  		//gfft2.fillRect(0, 0, 500, 180);

//	  		int offset = 4;
//	  		int a,b;
//	  		int cnt = 0;
//	  		for(int i = offset;i < (N_WAVE/2 -FFTviewStep-  -offset);i+= FFTviewStep) {
//
//
//
//	  			if(source[i+offset] > source[i+offset+1]) {
//	  				a = source[i+offset];
//	  			}else {
//	  				a = source[i+offset+1];
//	  			}
//	  			if(source[i+offset+2] > source[i+offset+3]) {
//	  				b = source[i+offset+2];
//	  			}else {
//	  				b = source[i+offset+3];
//	  			}
//	  			if(a < b) {
//	  				a = b;
//	  			}
//	  			a = (int) Math.sqrt((double)a * fftMag);
//
//
//	  			// Line Draw
//	  			if(a>175)a = 175;
//	  			double c = Math.sqrt(a);
//	  			gfft2.setStroke(new Color(c/13.3,0,(13.3-c)/13.3,1));
//	  	    	gfft2.strokeLine(5+i/FFTviewStep, 175- a, 5+i/FFTviewStep, 175);
//
//
//	  			cnt++;
//	  			if(cnt > 400)break;
//
//	  		}


	  		try {
	  			t.join();
	  		}catch(InterruptedException e) {

	  		}

	  		try {
				this.sleep(fftDrawWaitTime);
			} catch (InterruptedException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
	  		byte[] pre = readbuf;
	  		readbuf = writebuf;
	  		writebuf = pre;


	  	}
  			gfft2.setStroke( Color.BLACK);

//-------
audioin.stop();
  }

  }



  class DrawFFTLine{
		int offset = 4;
		int a,b;
		int cnt = 0;
		void start(int source[]) {
		for(int i = offset;i < (N_WAVE/2 -FFTviewStep-  -offset);i+= FFTviewStep) {



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


			// Line Draw
			if(a>175)a = 175;
			double c = Math.sqrt(a);
			gfft2.setStroke(new Color(c/13.3,0,(13.3-c)/13.3,1));
	    	gfft2.strokeLine(5+i/FFTviewStep, 175- a, 5+i/FFTviewStep, 175);


			cnt++;
			if(cnt > 400)break;

		}
		}
  }

  //-------------------- nomal FFT-------------------------------------------


  @FXML void setFFTmag2(){
	  FFTsampringMag = 2;
	  stepSize = BasicStepSize * 2;
	  auBuf1 = new byte[stepSize * 2];
	  auBuf2 = new byte[stepSize * 2];
		readbuf = auBuf1;
		writebuf = auBuf2;
  }

  @FXML void setFFTmag4() {
	  FFTsampringMag = 4;
	  stepSize = BasicStepSize;
	  auBuf1 = new byte[stepSize * 2];
	  auBuf2 = new byte[stepSize * 2];
		readbuf = auBuf1;
		writebuf = auBuf2;
  }

  @FXML void setFFTmag8() {
	  FFTsampringMag = 8;
	  stepSize = BasicStepSize/2 ;
	  auBuf1 = new byte[stepSize * 2];
	  auBuf2 = new byte[stepSize * 2];
		readbuf = auBuf1;
		writebuf = auBuf2;
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
if(a > 175) a = 175;
	double c = Math.sqrt(a);
	g.setStroke(new Color(c/13.3,0,(13.3-c)/13.3,1));


    	g.strokeLine(5+i/FFTviewStep, 175- a, 5+i/FFTviewStep, 175);

	}
	g.setStroke(Color.BLACK);
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
		runFFT = new RunFFT();
		runFFT.start(source,target);
	//fix_fft2(source,target);

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
		   0,   2,   3,   5,   6,   8,   9,  11,  13,  14,  16,  17,  19,  20,  22,  24,
		   25,  27,  28,  30,  31,  33,  35,  36,  38,  39,  41,  42,  44,  45,  47,  49,
		   50,  52,  53,  55,  56,  58,  60,  61,  63,  64,  66,  67,  69,  71,  72,  74,
		   75,  77,  78,  80,  82,  83,  85,  86,  88,  89,  91,  92,  94,  96,  97,  99,
		  100, 102, 103, 105, 107, 108, 110, 111, 113, 114, 116, 117, 119, 121, 122, 124,
		  125, 127, 128, 130, 131, 133, 135, 136, 138, 139, 141, 142, 144, 145, 147, 149,
		  150, 152, 153, 155, 156, 158, 159, 161, 163, 164, 166, 167, 169, 170, 172, 173,
		  175, 176, 178, 180, 181, 183, 184, 186, 187, 189, 190, 192, 193, 195, 196, 198,
		  200, 201, 203, 204, 206, 207, 209, 210, 212, 213, 215, 216, 218, 220, 221, 223,
		  224, 226, 227, 229, 230, 232, 233, 235, 236, 238, 239, 241, 242, 244, 246, 247,
		  249, 250, 252, 253, 255, 256, 258, 259, 261, 262, 264, 265, 267, 268, 270, 271,
		  273, 274, 276, 277, 279, 280, 282, 283, 285, 286, 288, 289, 291, 292, 294, 295,
		  297, 298, 300, 301, 303, 304, 306, 307, 309, 310, 312, 313, 315, 316, 318, 319,
		  321, 322, 324, 325, 327, 328, 330, 331, 333, 334, 336, 337, 339, 340, 342, 343,
		  345, 346, 348, 349, 351, 352, 353, 355, 356, 358, 359, 361, 362, 364, 365, 367,
		  368, 370, 371, 373, 374, 375, 377, 378, 380, 381, 383, 384, 386, 387, 389, 390,
		  391, 393, 394, 396, 397, 399, 400, 402, 403, 404, 406, 407, 409, 410, 412, 413,
		  415, 416, 417, 419, 420, 422, 423, 425, 426, 427, 429, 430, 432, 433, 435, 436,
		  437, 439, 440, 442, 443, 444, 446, 447, 449, 450, 452, 453, 454, 456, 457, 459,
		  460, 461, 463, 464, 466, 467, 468, 470, 471, 473, 474, 475, 477, 478, 479, 481,
		  482, 484, 485, 486, 488, 489, 491, 492, 493, 495, 496, 497, 499, 500, 502, 503,
		  504, 506, 507, 508, 510, 511, 512, 514, 515, 516, 518, 519, 521, 522, 523, 525,
		  526, 527, 529, 530, 531, 533, 534, 535, 537, 538, 539, 541, 542, 543, 545, 546,
		  547, 549, 550, 551, 553, 554, 555, 557, 558, 559, 560, 562, 563, 564, 566, 567,
		  568, 570, 571, 572, 574, 575, 576, 577, 579, 580, 581, 583, 584, 585, 586, 588,
		  589, 590, 592, 593, 594, 595, 597, 598, 599, 601, 602, 603, 604, 606, 607, 608,
		  609, 611, 612, 613, 614, 616, 617, 618, 619, 621, 622, 623, 624, 626, 627, 628,
		  629, 631, 632, 633, 634, 636, 637, 638, 639, 640, 642, 643, 644, 645, 647, 648,
		  649, 650, 651, 653, 654, 655, 656, 657, 659, 660, 661, 662, 663, 665, 666, 667,
		  668, 669, 671, 672, 673, 674, 675, 676, 678, 679, 680, 681, 682, 684, 685, 686,
		  687, 688, 689, 690, 692, 693, 694, 695, 696, 697, 699, 700, 701, 702, 703, 704,
		  705, 707, 708, 709, 710, 711, 712, 713, 714, 716, 717, 718, 719, 720, 721, 722,
		  723, 724, 726, 727, 728, 729, 730, 731, 732, 733, 734, 735, 737, 738, 739, 740,
		  741, 742, 743, 744, 745, 746, 747, 748, 750, 751, 752, 753, 754, 755, 756, 757,
		  758, 759, 760, 761, 762, 763, 764, 765, 766, 767, 768, 769, 771, 772, 773, 774,
		  775, 776, 777, 778, 779, 780, 781, 782, 783, 784, 785, 786, 787, 788, 789, 790,
		  791, 792, 793, 794, 795, 796, 797, 798, 799, 800, 801, 802, 803, 804, 805, 806,
		  806, 807, 808, 809, 810, 811, 812, 813, 814, 815, 816, 817, 818, 819, 820, 821,
		  822, 823, 824, 824, 825, 826, 827, 828, 829, 830, 831, 832, 833, 834, 835, 835,
		  836, 837, 838, 839, 840, 841, 842, 843, 844, 844, 845, 846, 847, 848, 849, 850,
		  851, 851, 852, 853, 854, 855, 856, 857, 858, 858, 859, 860, 861, 862, 863, 863,
		  864, 865, 866, 867, 868, 868, 869, 870, 871, 872, 873, 873, 874, 875, 876, 877,
		  877, 878, 879, 880, 881, 881, 882, 883, 884, 885, 885, 886, 887, 888, 889, 889,
		  890, 891, 892, 892, 893, 894, 895, 895, 896, 897, 898, 898, 899, 900, 901, 901,
		  902, 903, 904, 904, 905, 906, 907, 907, 908, 909, 909, 910, 911, 912, 912, 913,
		  914, 914, 915, 916, 917, 917, 918, 919, 919, 920, 921, 921, 922, 923, 923, 924,
		  925, 925, 926, 927, 927, 928, 929, 929, 930, 931, 931, 932, 933, 933, 934, 935,
		  935, 936, 937, 937, 938, 938, 939, 940, 940, 941, 941, 942, 943, 943, 944, 945,
		  945, 946, 946, 947, 948, 948, 949, 949, 950, 950, 951, 952, 952, 953, 953, 954,
		  954, 955, 956, 956, 957, 957, 958, 958, 959, 959, 960, 961, 961, 962, 962, 963,
		  963, 964, 964, 965, 965, 966, 966, 967, 967, 968, 968, 969, 969, 970, 970, 971,
		  971, 972, 972, 973, 973, 974, 974, 975, 975, 976, 976, 977, 977, 978, 978, 978,
		  979, 979, 980, 980, 981, 981, 982, 982, 983, 983, 983, 984, 984, 985, 985, 986,
		  986, 986, 987, 987, 988, 988, 988, 989, 989, 990, 990, 990, 991, 991, 992, 992,
		  992, 993, 993, 993, 994, 994, 995, 995, 995, 996, 996, 996, 997, 997, 997, 998,
		  998, 998, 999, 999, 999,1000,1000,1000,1001,1001,1001,1002,1002,1002,1003,1003,
		 1003,1004,1004,1004,1005,1005,1005,1005,1006,1006,1006,1007,1007,1007,1007,1008,
		 1008,1008,1008,1009,1009,1009,1010,1010,1010,1010,1011,1011,1011,1011,1011,1012,
		 1012,1012,1012,1013,1013,1013,1013,1013,1014,1014,1014,1014,1015,1015,1015,1015,
		 1015,1015,1016,1016,1016,1016,1016,1017,1017,1017,1017,1017,1017,1018,1018,1018,
		 1018,1018,1018,1019,1019,1019,1019,1019,1019,1019,1019,1020,1020,1020,1020,1020,
		 1020,1020,1020,1021,1021,1021,1021,1021,1021,1021,1021,1021,1021,1022,1022,1022,
		 1022,1022,1022,1022,1022,1022,1022,1022,1022,1022,1022,1022,1023,1023,1023,1023,
		 1023,1023,1023,1023,1023,1023,1023,1023,1023,1023,1023,1023,1023,1023,1023,1023,
		 1023,1023,1023,1023,1023,1023,1023,1023,1023,1023,1023,1023,1023,1023,1023,1023,
		 1023,1023,1023,1023,1023,1022,1022,1022,1022,1022,1022,1022,1022,1022,1022,1022,
		 1022,1022,1022,1022,1021,1021,1021,1021,1021,1021,1021,1021,1021,1021,1020,1020,
		 1020,1020,1020,1020,1020,1020,1019,1019,1019,1019,1019,1019,1019,1019,1018,1018,
		 1018,1018,1018,1018,1017,1017,1017,1017,1017,1017,1016,1016,1016,1016,1016,1015,
		 1015,1015,1015,1015,1015,1014,1014,1014,1014,1013,1013,1013,1013,1013,1012,1012,
		 1012,1012,1011,1011,1011,1011,1011,1010,1010,1010,1010,1009,1009,1009,1008,1008,
		 1008,1008,1007,1007,1007,1007,1006,1006,1006,1005,1005,1005,1005,1004,1004,1004,
		 1003,1003,1003,1002,1002,1002,1001,1001,1001,1000,1000,1000, 999, 999, 999, 998,
		  998, 998, 997, 997, 997, 996, 996, 996, 995, 995, 995, 994, 994, 993, 993, 993,
		  992, 992, 992, 991, 991, 990, 990, 990, 989, 989, 988, 988, 988, 987, 987, 986,
		  986, 986, 985, 985, 984, 984, 983, 983, 983, 982, 982, 981, 981, 980, 980, 979,
		  979, 978, 978, 978, 977, 977, 976, 976, 975, 975, 974, 974, 973, 973, 972, 972,
		  971, 971, 970, 970, 969, 969, 968, 968, 967, 967, 966, 966, 965, 965, 964, 964,
		  963, 963, 962, 962, 961, 961, 960, 959, 959, 958, 958, 957, 957, 956, 956, 955,
		  954, 954, 953, 953, 952, 952, 951, 950, 950, 949, 949, 948, 948, 947, 946, 946,
		  945, 945, 944, 943, 943, 942, 941, 941, 940, 940, 939, 938, 938, 937, 937, 936,
		  935, 935, 934, 933, 933, 932, 931, 931, 930, 929, 929, 928, 927, 927, 926, 925,
		  925, 924, 923, 923, 922, 921, 921, 920, 919, 919, 918, 917, 917, 916, 915, 914,
		  914, 913, 912, 912, 911, 910, 909, 909, 908, 907, 907, 906, 905, 904, 904, 903,
		  902, 901, 901, 900, 899, 898, 898, 897, 896, 895, 895, 894, 893, 892, 892, 891,
		  890, 889, 889, 888, 887, 886, 885, 885, 884, 883, 882, 881, 881, 880, 879, 878,
		  877, 877, 876, 875, 874, 873, 873, 872, 871, 870, 869, 868, 868, 867, 866, 865,
		  864, 863, 863, 862, 861, 860, 859, 858, 858, 857, 856, 855, 854, 853, 852, 851,
		  851, 850, 849, 848, 847, 846, 845, 844, 844, 843, 842, 841, 840, 839, 838, 837,
		  836, 835, 835, 834, 833, 832, 831, 830, 829, 828, 827, 826, 825, 824, 824, 823,
		  822, 821, 820, 819, 818, 817, 816, 815, 814, 813, 812, 811, 810, 809, 808, 807,
		  806, 806, 805, 804, 803, 802, 801, 800, 799, 798, 797, 796, 795, 794, 793, 792,
		  791, 790, 789, 788, 787, 786, 785, 784, 783, 782, 781, 780, 779, 778, 777, 776,
		  775, 774, 773, 772, 771, 769, 768, 767, 766, 765, 764, 763, 762, 761, 760, 759,
		  758, 757, 756, 755, 754, 753, 752, 751, 750, 748, 747, 746, 745, 744, 743, 742,
		  741, 740, 739, 738, 737, 735, 734, 733, 732, 731, 730, 729, 728, 727, 726, 724,
		  723, 722, 721, 720, 719, 718, 717, 716, 714, 713, 712, 711, 710, 709, 708, 707,
		  705, 704, 703, 702, 701, 700, 699, 697, 696, 695, 694, 693, 692, 690, 689, 688,
		  687, 686, 685, 684, 682, 681, 680, 679, 678, 676, 675, 674, 673, 672, 671, 669,
		  668, 667, 666, 665, 663, 662, 661, 660, 659, 657, 656, 655, 654, 653, 651, 650,
		  649, 648, 647, 645, 644, 643, 642, 640, 639, 638, 637, 636, 634, 633, 632, 631,
		  629, 628, 627, 626, 624, 623, 622, 621, 619, 618, 617, 616, 614, 613, 612, 611,
		  609, 608, 607, 606, 604, 603, 602, 601, 599, 598, 597, 595, 594, 593, 592, 590,
		  589, 588, 586, 585, 584, 583, 581, 580, 579, 577, 576, 575, 574, 572, 571, 570,
		  568, 567, 566, 564, 563, 562, 560, 559, 558, 557, 555, 554, 553, 551, 550, 549,
		  547, 546, 545, 543, 542, 541, 539, 538, 537, 535, 534, 533, 531, 530, 529, 527,
		  526, 525, 523, 522, 521, 519, 518, 516, 515, 514, 512, 511, 510, 508, 507, 506,
		  504, 503, 502, 500, 499, 497, 496, 495, 493, 492, 491, 489, 488, 486, 485, 484,
		  482, 481, 479, 478, 477, 475, 474, 473, 471, 470, 468, 467, 466, 464, 463, 461,
		  460, 459, 457, 456, 454, 453, 452, 450, 449, 447, 446, 444, 443, 442, 440, 439,
		  437, 436, 435, 433, 432, 430, 429, 427, 426, 425, 423, 422, 420, 419, 417, 416,
		  415, 413, 412, 410, 409, 407, 406, 404, 403, 402, 400, 399, 397, 396, 394, 393,
		  391, 390, 389, 387, 386, 384, 383, 381, 380, 378, 377, 375, 374, 373, 371, 370,
		  368, 367, 365, 364, 362, 361, 359, 358, 356, 355, 353, 352, 351, 349, 348, 346,
		  345, 343, 342, 340, 339, 337, 336, 334, 333, 331, 330, 328, 327, 325, 324, 322,
		  321, 319, 318, 316, 315, 313, 312, 310, 309, 307, 306, 304, 303, 301, 300, 298,
		  297, 295, 294, 292, 291, 289, 288, 286, 285, 283, 282, 280, 279, 277, 276, 274,
		  273, 271, 270, 268, 267, 265, 264, 262, 261, 259, 258, 256, 255, 253, 252, 250,
		  249, 247, 246, 244, 242, 241, 239, 238, 236, 235, 233, 232, 230, 229, 227, 226,
		  224, 223, 221, 220, 218, 216, 215, 213, 212, 210, 209, 207, 206, 204, 203, 201,
		  200, 198, 196, 195, 193, 192, 190, 189, 187, 186, 184, 183, 181, 180, 178, 176,
		  175, 173, 172, 170, 169, 167, 166, 164, 163, 161, 159, 158, 156, 155, 153, 152,
		  150, 149, 147, 145, 144, 142, 141, 139, 138, 136, 135, 133, 131, 130, 128, 127,
		  125, 124, 122, 121, 119, 117, 116, 114, 113, 111, 110, 108, 107, 105, 103, 102,
		  100,  99,  97,  96,  94,  92,  91,  89,  88,  86,  85,  83,  82,  80,  78,  77,
		   75,  74,  72,  71,  69,  67,  66,  64,  63,  61,  60,  58,  56,  55,  53,  52,
		   50,  49,  47,  45,  44,  42,  41,  39,  38,  36,  35,  33,  31,  30,  28,  27,
		   25,  24,  22,  20,  19,  17,  16,  14,  13,  11,   9,   8,   6,   5,   3,   2,
		    0,  -1,  -2,  -4,  -5,  -7,  -8, -10, -12, -13, -15, -16, -18, -19, -21, -23,
		  -24, -26, -27, -29, -30, -32, -34, -35, -37, -38, -40, -41, -43, -44, -46, -48,
		  -49, -51, -52, -54, -55, -57, -59, -60, -62, -63, -65, -66, -68, -70, -71, -73,
		  -74, -76, -77, -79, -81, -82, -84, -85, -87, -88, -90, -91, -93, -95, -96, -98,
		  -99,-101,-102,-104,-106,-107,-109,-110,-112,-113,-115,-116,-118,-120,-121,-123,
		 -124,-126,-127,-129,-130,-132,-134,-135,-137,-138,-140,-141,-143,-144,-146,-148,
		 -149,-151,-152,-154,-155,-157,-158,-160,-162,-163,-165,-166,-168,-169,-171,-172,
		 -174,-175,-177,-179,-180,-182,-183,-185,-186,-188,-189,-191,-192,-194,-195,-197,
		 -199,-200,-202,-203,-205,-206,-208,-209,-211,-212,-214,-215,-217,-219,-220,-222,
		 -223,-225,-226,-228,-229,-231,-232,-234,-235,-237,-238,-240,-241,-243,-245,-246,
		 -248,-249,-251,-252,-254,-255,-257,-258,-260,-261,-263,-264,-266,-267,-269,-270,
		 -272,-273,-275,-276,-278,-279,-281,-282,-284,-285,-287,-288,-290,-291,-293,-294,
		 -296,-297,-299,-300,-302,-303,-305,-306,-308,-309,-311,-312,-314,-315,-317,-318,
		 -320,-321,-323,-324,-326,-327,-329,-330,-332,-333,-335,-336,-338,-339,-341,-342,
		 -344,-345,-347,-348,-350,-351,-352,-354,-355,-357,-358,-360,-361,-363,-364,-366,
		 -367,-369,-370,-372,-373,-374,-376,-377,-379,-380,-382,-383,-385,-386,-388,-389,
		 -390,-392,-393,-395,-396,-398,-399,-401,-402,-403,-405,-406,-408,-409,-411,-412,
		 -414,-415,-416,-418,-419,-421,-422,-424,-425,-426,-428,-429,-431,-432,-434,-435,
		 -436,-438,-439,-441,-442,-443,-445,-446,-448,-449,-451,-452,-453,-455,-456,-458,
		 -459,-460,-462,-463,-465,-466,-467,-469,-470,-472,-473,-474,-476,-477,-478,-480,
		 -481,-483,-484,-485,-487,-488,-490,-491,-492,-494,-495,-496,-498,-499,-501,-502,
		 -503,-505,-506,-507,-509,-510,-511,-513,-514,-515,-517,-518,-520,-521,-522,-524,
		 -525,-526,-528,-529,-530,-532,-533,-534,-536,-537,-538,-540,-541,-542,-544,-545,
		 -546,-548,-549,-550,-552,-553,-554,-556,-557,-558,-559,-561,-562,-563,-565,-566,
		 -567,-569,-570,-571,-573,-574,-575,-576,-578,-579,-580,-582,-583,-584,-585,-587,
		 -588,-589,-591,-592,-593,-594,-596,-597,-598,-600,-601,-602,-603,-605,-606,-607,
		 -608,-610,-611,-612,-613,-615,-616,-617,-618,-620,-621,-622,-623,-625,-626,-627,
		 -628,-630,-631,-632,-633,-635,-636,-637,-638,-639,-641,-642,-643,-644,-646,-647,
		 -648,-649,-650,-652,-653,-654,-655,-656,-658,-659,-660,-661,-662,-664,-665,-666,
		 -667,-668,-670,-671,-672,-673,-674,-675,-677,-678,-679,-680,-681,-683,-684,-685,
		 -686,-687,-688,-689,-691,-692,-693,-694,-695,-696,-698,-699,-700,-701,-702,-703,
		 -704,-706,-707,-708,-709,-710,-711,-712,-713,-715,-716,-717,-718,-719,-720,-721,
		 -722,-723,-725,-726,-727,-728,-729,-730,-731,-732,-733,-734,-736,-737,-738,-739,
		 -740,-741,-742,-743,-744,-745,-746,-747,-749,-750,-751,-752,-753,-754,-755,-756,
		 -757,-758,-759,-760,-761,-762,-763,-764,-765,-766,-767,-768,-770,-771,-772,-773,
		 -774,-775,-776,-777,-778,-779,-780,-781,-782,-783,-784,-785,-786,-787,-788,-789,
		 -790,-791,-792,-793,-794,-795,-796,-797,-798,-799,-800,-801,-802,-803,-804,-805,
		 -805,-806,-807,-808,-809,-810,-811,-812,-813,-814,-815,-816,-817,-818,-819,-820,
		 -821,-822,-823,-823,-824,-825,-826,-827,-828,-829,-830,-831,-832,-833,-834,-834,
		 -835,-836,-837,-838,-839,-840,-841,-842,-843,-843,-844,-845,-846,-847,-848,-849,
		 -850,-850,-851,-852,-853,-854,-855,-856,-857,-857,-858,-859,-860,-861,-862,-862,
		 -863,-864,-865,-866,-867,-867,-868,-869,-870,-871,-872,-872,-873,-874,-875,-876,
		 -876,-877,-878,-879,-880,-880,-881,-882,-883,-884,-884,-885,-886,-887,-888,-888,
		 -889,-890,-891,-891,-892,-893,-894,-894,-895,-896,-897,-897,-898,-899,-900,-900,
		 -901,-902,-903,-903,-904,-905,-906,-906,-907,-908,-908,-909,-910,-911,-911,-912,
		 -913,-913,-914,-915,-916,-916,-917,-918,-918,-919,-920,-920,-921,-922,-922,-923,
		 -924,-924,-925,-926,-926,-927,-928,-928,-929,-930,-930,-931,-932,-932,-933,-934,
		 -934,-935,-936,-936,-937,-937,-938,-939,-939,-940,-940,-941,-942,-942,-943,-944,
		 -944,-945,-945,-946,-947,-947,-948,-948,-949,-949,-950,-951,-951,-952,-952,-953,
		 -953,-954,-955,-955,-956,-956,-957,-957,-958,-958,-959,-960,-960,-961,-961,-962,
		 -962,-963,-963,-964,-964,-965,-965,-966,-966,-967,-967,-968,-968,-969,-969,-970,
		 -970,-971,-971,-972,-972,-973,-973,-974,-974,-975,-975,-976,-976,-977,-977,-977,
		 -978,-978,-979,-979,-980,-980,-981,-981,-982,-982,-982,-983,-983,-984,-984,-985,
		 -985,-985,-986,-986,-987,-987,-987,-988,-988,-989,-989,-989,-990,-990,-991,-991,
		 -991,-992,-992,-992,-993,-993,-994,-994,-994,-995,-995,-995,-996,-996,-996,-997,
		 -997,-997,-998,-998,-998,-999,-999,-999,-1000,-1000,-1000,-1001,-1001,-1001,-1002,-1002,
		 -1002,-1003,-1003,-1003,-1004,-1004,-1004,-1004,-1005,-1005,-1005,-1006,-1006,-1006,-1006,-1007,
		 -1007,-1007,-1007,-1008,-1008,-1008,-1009,-1009,-1009,-1009,-1010,-1010,-1010,-1010,-1010,-1011,
		 -1011,-1011,-1011,-1012,-1012,-1012,-1012,-1012,-1013,-1013,-1013,-1013,-1014,-1014,-1014,-1014,
		 -1014,-1014,-1015,-1015,-1015,-1015,-1015,-1016,-1016,-1016,-1016,-1016,-1016,-1017,-1017,-1017,
		 -1017,-1017,-1017,-1018,-1018,-1018,-1018,-1018,-1018,-1018,-1018,-1019,-1019,-1019,-1019,-1019,
		 -1019,-1019,-1019,-1020,-1020,-1020,-1020,-1020,-1020,-1020,-1020,-1020,-1020,-1021,-1021,-1021,
		 -1021,-1021,-1021,-1021,-1021,-1021,-1021,-1021,-1021,-1021,-1021,-1021,-1022,-1022,-1022,-1022,
		 -1022,-1022,-1022,-1022,-1022,-1022,-1022,-1022,-1022,-1022,-1022,-1022,-1022,-1022,-1022,-1022,
		 -1022,-1022,-1022,-1022,-1022,-1022,-1022,-1022,-1022,-1022,-1022,-1022,-1022,-1022,-1022,-1022,
		 -1022,-1022,-1022,-1022,-1022,-1021,-1021,-1021,-1021,-1021,-1021,-1021,-1021,-1021,-1021,-1021,
		 -1021,-1021,-1021,-1021,-1020,-1020,-1020,-1020,-1020,-1020,-1020,-1020,-1020,-1020,-1019,-1019,
		 -1019,-1019,-1019,-1019,-1019,-1019,-1018,-1018,-1018,-1018,-1018,-1018,-1018,-1018,-1017,-1017,
		 -1017,-1017,-1017,-1017,-1016,-1016,-1016,-1016,-1016,-1016,-1015,-1015,-1015,-1015,-1015,-1014,
		 -1014,-1014,-1014,-1014,-1014,-1013,-1013,-1013,-1013,-1012,-1012,-1012,-1012,-1012,-1011,-1011,
		 -1011,-1011,-1010,-1010,-1010,-1010,-1010,-1009,-1009,-1009,-1009,-1008,-1008,-1008,-1007,-1007,
		 -1007,-1007,-1006,-1006,-1006,-1006,-1005,-1005,-1005,-1004,-1004,-1004,-1004,-1003,-1003,-1003,
		 -1002,-1002,-1002,-1001,-1001,-1001,-1000,-1000,-1000,-999,-999,-999,-998,-998,-998,-997,
		 -997,-997,-996,-996,-996,-995,-995,-995,-994,-994,-994,-993,-993,-992,-992,-992,
		 -991,-991,-991,-990,-990,-989,-989,-989,-988,-988,-987,-987,-987,-986,-986,-985,
		 -985,-985,-984,-984,-983,-983,-982,-982,-982,-981,-981,-980,-980,-979,-979,-978,
		 -978,-977,-977,-977,-976,-976,-975,-975,-974,-974,-973,-973,-972,-972,-971,-971,
		 -970,-970,-969,-969,-968,-968,-967,-967,-966,-966,-965,-965,-964,-964,-963,-963,
		 -962,-962,-961,-961,-960,-960,-959,-958,-958,-957,-957,-956,-956,-955,-955,-954,
		 -953,-953,-952,-952,-951,-951,-950,-949,-949,-948,-948,-947,-947,-946,-945,-945,
		 -944,-944,-943,-942,-942,-941,-940,-940,-939,-939,-938,-937,-937,-936,-936,-935,
		 -934,-934,-933,-932,-932,-931,-930,-930,-929,-928,-928,-927,-926,-926,-925,-924,
		 -924,-923,-922,-922,-921,-920,-920,-919,-918,-918,-917,-916,-916,-915,-914,-913,
		 -913,-912,-911,-911,-910,-909,-908,-908,-907,-906,-906,-905,-904,-903,-903,-902,
		 -901,-900,-900,-899,-898,-897,-897,-896,-895,-894,-894,-893,-892,-891,-891,-890,
		 -889,-888,-888,-887,-886,-885,-884,-884,-883,-882,-881,-880,-880,-879,-878,-877,
		 -876,-876,-875,-874,-873,-872,-872,-871,-870,-869,-868,-867,-867,-866,-865,-864,
		 -863,-862,-862,-861,-860,-859,-858,-857,-857,-856,-855,-854,-853,-852,-851,-850,
		 -850,-849,-848,-847,-846,-845,-844,-843,-843,-842,-841,-840,-839,-838,-837,-836,
		 -835,-834,-834,-833,-832,-831,-830,-829,-828,-827,-826,-825,-824,-823,-823,-822,
		 -821,-820,-819,-818,-817,-816,-815,-814,-813,-812,-811,-810,-809,-808,-807,-806,
		 -805,-805,-804,-803,-802,-801,-800,-799,-798,-797,-796,-795,-794,-793,-792,-791,
		 -790,-789,-788,-787,-786,-785,-784,-783,-782,-781,-780,-779,-778,-777,-776,-775,
		 -774,-773,-772,-771,-770,-768,-767,-766,-765,-764,-763,-762,-761,-760,-759,-758,
		 -757,-756,-755,-754,-753,-752,-751,-750,-749,-747,-746,-745,-744,-743,-742,-741,
		 -740,-739,-738,-737,-736,-734,-733,-732,-731,-730,-729,-728,-727,-726,-725,-723,
		 -722,-721,-720,-719,-718,-717,-716,-715,-713,-712,-711,-710,-709,-708,-707,-706,
		 -704,-703,-702,-701,-700,-699,-698,-696,-695,-694,-693,-692,-691,-689,-688,-687,
		 -686,-685,-684,-683,-681,-680,-679,-678,-677,-675,-674,-673,-672,-671,-670,-668,
		 -667,-666,-665,-664,-662,-661,-660,-659,-658,-656,-655,-654,-653,-652,-650,-649,
		 -648,-647,-646,-644,-643,-642,-641,-639,-638,-637,-636,-635,-633,-632,-631,-630,
		 -628,-627,-626,-625,-623,-622,-621,-620,-618,-617,-616,-615,-613,-612,-611,-610,
		 -608,-607,-606,-605,-603,-602,-601,-600,-598,-597,-596,-594,-593,-592,-591,-589,
		 -588,-587,-585,-584,-583,-582,-580,-579,-578,-576,-575,-574,-573,-571,-570,-569,
		 -567,-566,-565,-563,-562,-561,-559,-558,-557,-556,-554,-553,-552,-550,-549,-548,
		 -546,-545,-544,-542,-541,-540,-538,-537,-536,-534,-533,-532,-530,-529,-528,-526,
		 -525,-524,-522,-521,-520,-518,-517,-515,-514,-513,-511,-510,-509,-507,-506,-505,
		 -503,-502,-501,-499,-498,-496,-495,-494,-492,-491,-490,-488,-487,-485,-484,-483,
		 -481,-480,-478,-477,-476,-474,-473,-472,-470,-469,-467,-466,-465,-463,-462,-460,
		 -459,-458,-456,-455,-453,-452,-451,-449,-448,-446,-445,-443,-442,-441,-439,-438,
		 -436,-435,-434,-432,-431,-429,-428,-426,-425,-424,-422,-421,-419,-418,-416,-415,
		 -414,-412,-411,-409,-408,-406,-405,-403,-402,-401,-399,-398,-396,-395,-393,-392,
		 -390,-389,-388,-386,-385,-383,-382,-380,-379,-377,-376,-374,-373,-372,-370,-369,
		 -367,-366,-364,-363,-361,-360,-358,-357,-355,-354,-352,-351,-350,-348,-347,-345,
		 -344,-342,-341,-339,-338,-336,-335,-333,-332,-330,-329,-327,-326,-324,-323,-321,
		 -320,-318,-317,-315,-314,-312,-311,-309,-308,-306,-305,-303,-302,-300,-299,-297,
		 -296,-294,-293,-291,-290,-288,-287,-285,-284,-282,-281,-279,-278,-276,-275,-273,
		 -272,-270,-269,-267,-266,-264,-263,-261,-260,-258,-257,-255,-254,-252,-251,-249,
		 -248,-246,-245,-243,-241,-240,-238,-237,-235,-234,-232,-231,-229,-228,-226,-225,
		 -223,-222,-220,-219,-217,-215,-214,-212,-211,-209,-208,-206,-205,-203,-202,-200,
		 -199,-197,-195,-194,-192,-191,-189,-188,-186,-185,-183,-182,-180,-179,-177,-175,
		 -174,-172,-171,-169,-168,-166,-165,-163,-162,-160,-158,-157,-155,-154,-152,-151,
		 -149,-148,-146,-144,-143,-141,-140,-138,-137,-135,-134,-132,-130,-129,-127,-126,
		 -124,-123,-121,-120,-118,-116,-115,-113,-112,-110,-109,-107,-106,-104,-102,-101,
		  -99, -98, -96, -95, -93, -91, -90, -88, -87, -85, -84, -82, -81, -79, -77, -76,
		  -74, -73, -71, -70, -68, -66, -65, -63, -62, -60, -59, -57, -55, -54, -52, -51,
		  -49, -48, -46, -44, -43, -41, -40, -38, -37, -35, -34, -32, -30, -29, -27, -26,
		  -24, -23, -21, -19, -18, -16, -15, -13, -12, -10,  -8,  -7,  -5,  -4,  -2,  -1,

};





class RunFFT extends Thread{

	  public void start(int fr[],int fi[]) {

		  fix_fft2( fr,  fi);




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


        tr = (wr * fr[j] - wi * fi[j] + 64 ) >> 10;
        ti = (wr * fi[j] + wi * fr[j] + 64 ) >> 10;


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

}
