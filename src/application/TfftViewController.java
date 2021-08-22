package application;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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
	@FXML Slider fftMagSlider1;
	@FXML RadioButton tfftButton1;


	@FXML Slider positionSlider2;
	@FXML Label mag2;
	@FXML Slider magSlider2;
	@FXML Slider yMagSlider2;
	@FXML Slider fftSlider2;
	@FXML Slider fftMagSlider2;
	@FXML RadioButton tfftButton2;


	@FXML RadioButton syncFFT;
	@FXML RadioButton realTimeFFT;
	@FXML Pane fftPane;
	@FXML HBox fftHBox;



	final int SAMPRING_RATE = 44000;
	final int viewCenter = 300;
	final int N_WAVE = 4096;
	final int LOG2_N_WAVE = 12;
	final int FFTviewStep = 4;
	int SineWaveN_WAVE2[];
	double colorSqrtTable[];
	int drawSqrtTable[];
//	int sqrtTable[][];



	int FFTsampringMag =4;
	  RealTimeFFT realTimeFFTthread;
	  RunFFT runFFT;


	WaveRecord audioRec1 = new WaveRecord(16,1,SAMPRING_RATE,2.0);
	WaveRecord audioRec2 = new WaveRecord(16,1,SAMPRING_RATE,2.0);
	boolean syncFFTchannel = false;
	int syncFFToffset = 0;

	final   int BasicStepSize = 2048;
	int stepSize = BasicStepSize;
	long	fftDrawWaitTime = 100;
	AudioInput audioin;
	byte[] auBuf = new byte[stepSize * 2];
	byte[] auBuf1 = new byte[stepSize * 2];
	byte[] auBuf2 = new byte[stepSize * 2];

	byte[] readbuf;
	byte[] writebuf;


	final int defaultfftMag = 100;
	int realtimefftMag = defaultfftMag;


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
		int fftYMag;


		WaveRecord(int bitwidth, int channelval, int samplingRate, double bufTime){
			audioBuf = new byte[(int)((bitwidth/8) * channelval * samplingRate *bufTime)];

			audioBit = bitwidth;
			audioChannel = channelval;
			audioHz = samplingRate;
			audioSec = bufTime;

			wavePosition = 0;
			waveMag = 50;
			waveYMag = 32;
			fftYMag = defaultfftMag;

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



		SineWaveN_WAVE2 = new int[N_WAVE];
		for(int i = 0; i < N_WAVE;i++) {
			int d = (int) (Math.sin(Math.PI * i / (N_WAVE/2)) * 1023+ 0.5);
			SineWaveN_WAVE2[i] = d;
		}

		int sqri,sqrj,data;

//		sqrtTable = new int[4096][4096];
//		for(sqri = 0; sqri < 4096;sqri++) {
//			for(sqrj = 0;sqrj < 4096;sqrj++) {
//				sqrtTable[sqri][sqrj] = (int)Math.sqrt(sqri*sqri + sqrj*sqrj);
//			}
//		}
//








		colorSqrtTable = new double[176];
		for( sqri = 0; sqri < 176;sqri++) {


				colorSqrtTable[sqri] = Math.sqrt(sqri);

		}

		drawSqrtTable = new int[175 * 175];
		for(int i = 0; i < 175*175;i++) {
			data = (int)Math.sqrt(i );
			drawSqrtTable[i] = data;
		}



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



		fftMagSlider1.valueProperty().addListener((
	        	ObservableValue<? extends Number> ov,Number old_val,
	    		Number new_val) ->{
	    			audioRec1.fftYMag = new_val.intValue();
	    			if(tfftButton1.isSelected()) {
	    				waveFft2(audioRec1,gfft1);
	    			}else {
	    				waveFft(audioRec1,gfft1);
	    			}


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

		fftMagSlider2.valueProperty().addListener((
	        	ObservableValue<? extends Number> ov,Number old_val,
	    		Number new_val) ->{
	    			audioRec2.fftYMag = new_val.intValue();
	    			realtimefftMag = new_val.intValue();
	    			if(tfftButton2.isSelected() && (realTimeFFT.isSelected() == false)) {
	    				waveFft2(audioRec2,gfft2);
	    			}else {
	    				waveFft(audioRec2,gfft2);
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
//		double posx,posy;
//		double height;
//		double width;
//		width = fftCanvas2.getWidth();
//		height = fftCanvas2.getHeight();
//		posx = fftCanvas2.getLayoutX();
//		posy = fftCanvas2.getLayoutY();
//		fftCanvas2.setDisable(true);
//		fftCanvas2 = null;
//
//		fftCanvas2 = new Canvas();
//		fftCanvas2.setWidth(width);
//		fftCanvas2.setHeight(height);
//		fftCanvas2.setLayoutX(posx);
//		fftCanvas2.setLayoutY(posy);
//
//		fftPane.getChildren().add(fftCanvas2);
//		gfft2 = fftCanvas2.getGraphicsContext2D();



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
		if(tfftButton2.isSelected()) {
			waveFft2(audioRec2,gfft2);
		}else {
			waveFft(audioRec2,gfft2);
		}

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



  @FXML void tfftDraw1() {
	  if(tfftButton1.isSelected()) {
		waveFft2(audioRec1,gfft1);
	  }else {
		  waveFft(audioRec1,gfft1);
	  }
  }

  @FXML void tfftDraw2() {
	  if(tfftButton2.isSelected()) {
		  waveFft2(audioRec2,gfft2);
	  }else {
		  waveFft(audioRec2,gfft2);
	  }

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
			if(tfftButton2.isSelected() && (realTimeFFT.isSelected() == false)) {
				waveFft2(audioRec2,gfft2);
			}else {
				waveFft(audioRec2,gfft2);
			}

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
	  		int sp = 0;

	  		for(int i = 0;i < stepSize * 2 ; i+=2) {
	  			 data = ( (int)(readbuf[i+1] <<8) | (0x00ff & readbuf[i]));
// data >>= 3;
	  			fftRecord[writePos++] = data;
	  			//if(writePos == N_WAVE) writePos = 0;
	  			writePos &= 0xfff;

	  			source[sp] = data;
	  			target[sp] = 0;
	  			sp++;
	  			sp &= 0xfff;


	  		}
	  		int p = writePos;
	  		for(int i =sp;i<N_WAVE;i++) {
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
//	  	        if(j < 0)j = -j;
//	  	        if(k <0) k = -k;
//	  	        source[i] = sqrtTable[j][k];
	  	        source[i] =(int) Math.sqrt(j*j+k*k);
	  	    }

	  	    gfft2.clearRect(0, 0, 530, 400);
	  		DrawFFTLine drline = new DrawFFTLine();
	  		drline.start(source);



	  		try {
	  			t.join();
	  		}catch(InterruptedException e) {

	  		}

	  		try {
				runFFT.join();
			} catch (InterruptedException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			}


	  		try {
				Thread.sleep(4);
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
		int a,b,c,d,max;
		int cnt = 0;


		void start(int source[]) {

			for(int i = offset;i < (N_WAVE/2 -FFTviewStep-  -offset);i+= FFTviewStep) {

				max = source[i+offset];
				b = source[i+offset+1];
				c = source[i+offset+2];
				d = source[i+offset+3];

				if( max > b) {
					if(max > c) {
						if(max <d)
							max = d;
					}else {
						if(c > d) {
							max = c;
						}else {
							max = d;
						}
					}
				}else {
					if(b > c) {
						if(b > d) {
							max = b;
						}else {
							max = d;
						}
					}else {
						if(c > d) {
							max = c;
						}else {
							max = d;
						}
					}

				}




//				if(source[i+offset] > source[i+offset+1]) {
//					max = source[i+offset];
//				}else {
//					max = source[i+offset+1];
//				}
//				if(source[i+offset+2] > source[i+offset+3]) {
//					b = source[i+offset+2];
//				}else {
//					b = source[i+offset+3];
//				}
//				if(max < b) {
//					max = b;
//				}






				//max = (int) Math.sqrt((double)max * fftMag);

				int index = max * realtimefftMag;
				if(index > 30624) {
					index = 30624;
				}

			 	max = (int) drawSqrtTable[index];

				// Line Draw


				//double c = Math.sqrt(max);
				double c = (double)colorSqrtTable[max];
				gfft2.setStroke(new Color(c/13.3,0,(13.3-c)/13.3,1));
		    	gfft2.strokeLine(5+i/FFTviewStep, 175- max, 5+i/FFTviewStep, 175);

				cnt++;

				if(cnt > 400)break;

			}
				try {
					TimeUnit.NANOSECONDS.sleep(fftDrawWaitTime);
				} catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
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

void drawFft(GraphicsContext g, int data[],int fftMag) {
	g.clearRect(0, 0, 530, 400);
	int offset = 4;
	int a,b;

	for(int i = 1;i < (N_WAVE-FFTviewStep-offset);i+= FFTviewStep) {

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
	int fftPosStep = 5;
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
    drawFft(g,rec.fft,rec.fftYMag);


}


void waveFft2(WaveRecord rec,GraphicsContext g) {
	int [] source = new int[N_WAVE];
	int [] target = new int[N_WAVE];
	byte[]  buf = rec.audioBuf;
	int pos = 0;
	int data;
	g.clearRect(0, 0, 530, 400);

	runFFT = new RunFFT();


	for(int ypos =0; ypos < 175;ypos++) {

		// データ転送
		for(int i = 0;i < N_WAVE; i++) {
			data = ( (int)(buf[pos*2+i*FFTsampringMag+1] <<8) | (0x00ff & buf[pos*2+i*FFTsampringMag]));
			source[i] = data;
			target[i] = 0;
		}
		while(runFFT.isAlive()) {
			;
		}
		runFFT = new RunFFT();
		runFFT.start(source,target);
    	int j,k;
    for (int i = 0; i <  (N_WAVE/2) ; i++) {

        j = source[i];
        k = target[i] ;
        source[i] =(int) Math.sqrt(j*j+k*k);
    }



    	int a,b,c,d,max,offset;
    	offset = 0;

    	try {
			runFFT.join();
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}


    	for(int i = 1;i < (N_WAVE / 2 -FFTviewStep-offset);i+= FFTviewStep) {

			max = source[i+offset];
			b = source[i+offset+1];
			c = source[i+offset+2];
			d = source[i+offset+3];

			if( max > b) {
				if(max > c) {
					if(max <d)
						max = d;
				}else {
					if(c > d) {
						max = c;
					}else {
						max = d;
					}
				}
			}else {
				if(b > c) {
					if(b > d) {
						max = b;
					}else {
						max = d;
					}
				}else {
					if(c > d) {
						max = c;
					}else {
						max = d;
					}
				}

			}






    		int index = max * rec.fftYMag;




			//int index = a * realtimefftMag;
			if(index > 30624) {
				index = 30624;
			}

		 	a = (int) drawSqrtTable[index];

			// Line Draw

			//double c = Math.sqrt(max);
			double cl = (double)colorSqrtTable[a];
    		// = (int) Math.sqrt((double)a * rec.fftYMag);
    		//if(a > 175) a = 175;

    	//double c = Math.sqrt(a-20);

    	g.setStroke(new Color( (cl/13.3)*(cl/13.3),cl/13.3/2,(13.3-cl)/13.3*(cl/13.3),1));
		g.strokeLine(5+i/FFTviewStep, ypos, 5+i/FFTviewStep, ypos+1);



    	}
		pos += 400;

	}


}


















class RunFFT extends Thread{

	  public void start(int fr[],int fi[]) {

		  fix_fft2( fr,  fi);

	  }






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
      wr =  SineWaveN_WAVE2 [ j + N_WAVE / 4];

      wi = -SineWaveN_WAVE2[j];


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
