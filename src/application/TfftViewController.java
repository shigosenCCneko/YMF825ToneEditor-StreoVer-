package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import DataClass.Ymf825ToneData;
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
	@FXML RadioButton realTimeWave;

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



	@FXML Button dataWrite;
	@FXML Button fftSerch;
	@FXML Button nextSerchData;
	@FXML Label surchDataNo;

	final int SAMPRING_RATE = 44000;
	final int viewCenter = 300;
	final int N_WAVE = 4096;
	final int LOG2_N_WAVE = 12;
	final int FFTviewStep = 4;
	int SineWaveN_WAVE2[];
	double colorSqrtTable[];
	int drawSqrtTable[];
//	int sqrtTable[][];


	RealTimeWave realTimeWavethread;


	int FFTsampringMag =4;
	RealTimeFFT realTimeFFTthread;
	RunFFT runFFT;


	WaveRecord audioRec1 = new WaveRecord(16,1,SAMPRING_RATE,2.0);
	WaveRecord audioRec2 = new WaveRecord(16,1,SAMPRING_RATE,2.0);
	boolean syncFFTchannel = false;
	int syncFFToffset = 0;



	//音色検索セット
	int candidataVal = 0;
	List <OpeCandi> candidate = new ArrayList<OpeCandi>();
	int opIndex = 0;


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
//		sqrtTable = new int[5000][5000];
//		for(sqri = 0; sqri < 4096;sqri++) {
//			for(sqrj = 0;sqrj < 4096;sqrj++) {
//				sqrtTable[sqri][sqrj] = (int)Math.sqrt(sqri*sqri + sqrj*sqrj);
//			}
//		}









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
		if(tfftButton1.isSelected()) {
			waveFft2(audioRec1,gfft1);
		}else {
			waveFft(audioRec1,gfft1);
		}



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

  @FXML void realTimeRaveRun() {

	  if(realTimeWave.isSelected()) {
		  if(realTimeFFTthread != null) {
			  realTimeFFTthread.exit();
			  audioin.stop();
			  realTimeFFT.setSelected(false);


		  }

		  realTimeWavethread = new RealTimeWave();
		  realTimeWavethread.setPriority(Thread.MAX_PRIORITY);
		  realTimeWavethread.start();

	  }else {

		  realTimeWavethread.exit();
//
		  audioin.stop();
//			if(tfftButton2.isSelected() && (realTimeFFT.isSelected() == false)) {
//				waveFft2(audioRec2,gfft2);
//			}else {
//				waveFft(audioRec2,gfft2);
//			}
	  }

  }




  class RealTimeWave extends Thread {
	  boolean loopContinue = true;
	  int[] fftRecord = new int[4096];
	  int readChannel = 0;
	  WaveRecord rec = audioRec2;
	  int writePos = 0;


	  public void exit() {
			loopContinue = false;

	  }


	  public void run() {


		  DataRead dr = new DataRead();
		  Thread t;


		audioin = new AudioInput(rec.audioBit,rec.audioChannel,rec.audioHz/FFTsampringMag*2);
		audioin.start();

	  		while(loopContinue) {

	  		g2.clearRect(0, 0, 200, 200);
	  		//auin.read(readBuf);
	  		t = new Thread(dr);
	  		t.start();



			int px = 0;
			int pdata = 0;
			int x = 0;
			for(int i = 0;i < stepSize * 2 ;i+= 2) {
				int data = ( (int)(readbuf[i+1] <<8) | (0x00ff & readbuf[i]));

				data /= rec.waveYMag;

				g2.strokeLine(px, pdata+60, x, data+60);
				pdata = data;
				px = x;
				x+= 1;
				if(x > 200) {
					break;
				}
			}


	  		try {
	  			t.join();
	  		}catch(InterruptedException e) {

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


  			audioin.stop();
  			try {
				this.sleep(100);
			} catch (InterruptedException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
	  }

  }










// FFT 別スレッド -------------------------------------------------


  @FXML void realTimeFft(){
	  if(realTimeFFT.isSelected()) {
		  if(realTimeWavethread != null) {
			  realTimeWavethread.exit();
			  audioin.stop();

			  realTimeWave.setSelected(false);
		  }


		  realTimeFFTthread = new RealTimeFFT();
		  realTimeFFTthread.setPriority(Thread.MAX_PRIORITY);
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
	try {
		this.sleep(100);
	} catch (InterruptedException e) {
		// TODO 自動生成された catch ブロック
		e.printStackTrace();
	}
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

	class OpeCandi{
		double difference;
	
		int alg;
		int mulop1;
		int mulop2;
		int mulop3;
		int mulop4;
	
		int tlvop1;
		int tlvop2;
		int tlvop3;
		int tlvop4;
	
	
	}


	void setOpTlData(int ch ,int opIndex) {
	
		Ymf825ToneData  toneData;
		toneData = Ymf825ToneData.getInstance();
		
			
		toneData.setValue(eventSource.Connect, ch, 0,candidate.get(opIndex).alg);

		toneData.setValue(eventSource.Mul, ch, 0, candidate.get(opIndex).mulop1);
		toneData.setValue(eventSource.Mul, ch, 1, candidate.get(opIndex).mulop2);
		toneData.setValue(eventSource.Mul, ch, 2, candidate.get(opIndex).mulop3);
		toneData.setValue(eventSource.Mul, ch, 3, candidate.get(opIndex).mulop4);

		toneData.setValue(eventSource.Tlv, ch, 0, candidate.get(opIndex).tlvop1);
		toneData.setValue(eventSource.Tlv, ch, 1, candidate.get(opIndex).tlvop2);
		toneData.setValue(eventSource.Tlv, ch, 2, candidate.get(opIndex).tlvop3);
		toneData.setValue(eventSource.Tlv, ch, 3, candidate.get(opIndex).tlvop4);


		byte [] tbuf = new byte[32];
		toneData.get_tonememory(0, tbuf);
		toneData.setTone(0,  tbuf);	  //値をPanelに標示
		surchDataNo.setText(    Integer.toString(opIndex)      );
	}


	@FXML void nextSercDataSet() {
			opIndex++;
			if(opIndex>4) {
				opIndex = 0;
			}
			setOpTlData(0,opIndex);
	
	
	}
	
	
	/*  ----------------------        候補音色の検索------------------		*/

	@FXML void fftSarchPara() {
	
	
	

	    //ノイズ除去
		WaveRecord rec;
		rec = audioRec1;
		
		int dat[] = new int[N_WAVE/4];
		for(int i = 1;i < N_WAVE/4 -1;i++) {
			dat[i] = (rec.fft[i-1]+rec.fft[i]*2+rec.fft[i+1])/4;
		}
		
		
		  int cnt = 0;
		  int maxCnt = 21;
		  int maxOvertone = 50;
		  int sp = 30;
		  int pre2 = dat[sp-4];
		  int pre1 = dat[sp-3];
		  int peak = dat[sp-2];
		  int af1  = dat[sp-1];
		  int af2 = dat[sp];
	
		  int fftsource[] = new int[maxOvertone+1];
		  int ffttarget[] = new int [maxOvertone+1];
	
		  for(int i = 0; i < maxOvertone+1;i++) {
			  fftsource[i] = 0;
	
		  }
	
		  int ave = 0;
		  //int base = 49;  //C4固定時
		  int base = 0;
		  int clip = 40;
		  String strdat;
		  int overtones;
	
		  cnt = 0;
		  strdat = "";
		  int peakData;
		  for(int i = sp+1 ; i < N_WAVE/4-4;i++) {
	
			   pre2 = dat[i-4];
			   pre1 = dat[i-3];
			   peak = dat[i-2];
			   af1  = dat[i-1];
			   af2 = dat[i];
	
	
			  ave = (pre2  + af2)/2;
	
			  if(pre1 >= peak) continue;
			  if(af1 >  peak) continue;
	
			  if(af1  < af2 )continue;
			  if(pre1 < pre2)continue;
	
			  if(peak < ave +3)continue;
	
			  if(i > 100)clip = 35;
			  if(i > 150) clip = 30;
			  if( i > 200)clip = 5;
			  if( i > 400)clip = 3;
	
			  if(peak > clip) {
				  if(base == 0) {
					  base = i -2;
				  }
	
				  cnt++;
				  overtones = (int)((i-2.0)/base+0.5);
				  peakData = dat[i-2];
	
				  strdat = strdat +  overtones + "," + peakData + ",  ";

				 if(peakData >fftsource[overtones]) {
					 fftsource[overtones] = peakData;
				 }
				 i += base / 3;
	
				  if(cnt > maxCnt) break;
			  	}
	
		  }
	
	
		  String filename = "H:\\fftDataH\\overtonedata2.txt";
//		  String filename = "H:\\fftDataH\\fftdata-alg5.txt";
		  
		  File file = new File(filename);
			BufferedReader br = null;
		  try {
			  br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	
		  int index;
		  String line;
		  String[] data;
		  int alg;
		  int mulop1,mulop2,mulop3,mulop4;
		  int tlvop1,tlvop2,tlvop3,tlvop4;
		  int fftDataVal;
	
	
		  double miniDiff = 1000000;  //十分大きい数
		  double fifthDiff = miniDiff;
		  double difference = 0;
		  int maxCompareOvertones = 20;
	
	
		  int mop1 = 0,mop2=0,mop3=0,mop4 = 0,top1=0,top2=0,top3=0,top4=0;
	
		  candidate.clear();
		  candidataVal = 0;
		  
		  try {
			while((line = br.readLine()) != null){
				
				  for(int i = 0; i < maxOvertone+1;i++) {
					  ffttarget[i] = 0;
				  }

				  data = line.split(",");				  
				  alg    = Integer.parseInt(data[0].trim());
				  mulop1 = Integer.parseInt(data[1].trim());
				  mulop2 = Integer.parseInt(data[2].trim());
				  mulop3 = Integer.parseInt(data[3].trim());
				  mulop4 = Integer.parseInt(data[4].trim());
	
				  tlvop1 = Integer.parseInt(data[5].trim());
				  tlvop2 = Integer.parseInt(data[6].trim());
				  tlvop3 = Integer.parseInt(data[7].trim());
				  tlvop4 = Integer.parseInt(data[8].trim());
	
				  fftDataVal = Integer.parseInt(data[9].trim());
	
	
				  for(int i = 10;i < fftDataVal * 2 + 10;i+=2) {
					  index = Integer.parseInt(data[i].trim());
					  ffttarget[index] = Integer.parseInt(data[i+1].trim());
	
				  }
	
	
	
				  // 比較ルーチン

				  difference = 0;
	
				  double targetTotal,sourceTotal,mag;
				  targetTotal = 0;
				  sourceTotal = 0;
				  
				  for(int i = 1;i <= maxCompareOvertones;i++) {
					  targetTotal += ffttarget[i];
					  sourceTotal += fftsource[i];
				  }
				  
				  mag = targetTotal/sourceTotal;
				  for(int i =1;i<=maxCompareOvertones;i++) {
					  difference += Math.abs( ffttarget[i]/mag - fftsource[i]);
				  }
	
				  if(difference < fifthDiff) {
	
						OpeCandi newdata = new OpeCandi();
						newdata.difference = difference;
						
						newdata.alg = alg;
						newdata.mulop1 = mulop1;
						newdata.mulop2 = mulop2;
						newdata.mulop3 = mulop3;
						newdata.mulop4 = mulop4;
	
						newdata.tlvop1 = tlvop1;
						newdata.tlvop2 = tlvop2;
						newdata.tlvop3 = tlvop3;
						newdata.tlvop4 = tlvop4;
	
						candidate.add(newdata);
						Collections.sort(
								candidate,
								new Comparator<OpeCandi>() {
							@Override
							public int compare(OpeCandi obj1 ,OpeCandi obj2) {
								return (int)(obj1.difference - obj2.difference);
							}
						});
	
					  if(candidataVal < 5) {
	
						candidataVal++;
						fifthDiff = candidate.get(candidate.size()-1).difference;
						miniDiff = candidate.get(0).difference;
					}else {
						fifthDiff = candidate.get(4).difference;
						miniDiff = candidate.get(0).difference;
						candidate.remove(5);
	
					}
	
	
					  System.out.printf("%4d  ", (int)difference);
					  for(int i = 1; i <= maxCompareOvertones;i++) {
						  System.out.printf("%4d,", (int)(ffttarget[i]/mag));
					  }
					  System.out.println("");
					  miniDiff = difference;
				  }
	
	
			}
			System.out.print("\n      ");
			for(int i = 1; i < maxCnt;i++) {
				System.out.printf("%4d," , fftsource[i]);
			}
			System.out.println("");
	
	
	
	
			for(int i = 0; i < 5;i++) {
				System.out.printf("%3d - %3d,%3d,%3d,%3d  %3d,%3d,%3d,%3d\n",
						candidate.get(i).alg,
						candidate.get(i).mulop1,
						candidate.get(i).mulop2,
						candidate.get(i).mulop3,
						candidate.get(i).mulop4,
	
						candidate.get(i).tlvop1,
						candidate.get(i).tlvop2,
						candidate.get(i).tlvop3,
						candidate.get(i).tlvop4
						);
			}
			
			opIndex = 0;
			setOpTlData(0,opIndex);
	
	
	
		  } catch (IOException e) {
			  // TODO 自動生成された catch ブロック
			  e.printStackTrace();
		  }
	
	
		  try {
			  br.close();
		  } catch (IOException e) {
			  // TODO 自動生成された catch ブロック
			  e.printStackTrace();
		  }
	
	
	
	}

	
	

//------------------------------   fft照合データの作成保存 --------------------------
	
	
	@FXML void fftDataWrite() {  
	
	
		int fftalg = 4; //アルゴリズム
		
		
		int maxCnt = 40;
	
	
		int opMaxVal = 5;
		int opTlMax = 60;
		int opTlMin = 10;
		int opTlStep = 2;
	
	
	/* test operator set */
	opMaxVal = 9;
	opTlMax = 56;
	opTlMin = 16;
	opTlStep = 4;
	
	opMaxVal = 9;
	opTlMax = 48;
	opTlMin = 20;
	opTlStep = 4;
	
	
	opMaxVal = 5;
	opTlMax = 50;
	opTlMin = 38;
	opTlStep = 4;
	
		int fftop1mul = 1;   //開始オペレータの初期値
		int fftop2mul = 1;
		int fftop3mul = 1;
		int fftop4mul = 1;
	
		int fftTl1 = opTlMax;
		int fftTl2 = opTlMax;
		int fftTl3 = opTlMax;
		int fftTl4 = 0;
	
	
	
	
	Ymf825ToneData  toneData;
	
		toneData = Ymf825ToneData.getInstance();
		toneData.setValue(eventSource.Connect, 0, 0, fftalg);
	
		String strpara;
		fftTl1 = opTlMax + opTlStep;
	while(true) {

// alg =4 data 		

		fftTl1 -= opTlStep;
		if(fftTl1 < opTlMin) {
	
			fftTl1 = opTlMax;
			fftTl2 -= opTlStep;
			if(fftTl2 < opTlMin) {
				fftTl2 = opTlMax;
				fftTl3 -= opTlStep;
				if(fftTl3 < opTlMin) {
					fftTl3 = opTlMax;
	
	
					fftop1mul++;
					if(fftop1mul > opMaxVal) {
						fftop1mul = 1;
						fftop2mul++;
						if(fftop2mul > opMaxVal) {
							fftop2mul = 1;
							fftop3mul++;
						}
					}
				}
			}
		}
		if(fftop3mul >opMaxVal)break;

		
//alg = 5 data
/*		
		fftTl3 -= opTlStep;
		if(fftTl3 < opTlMin) {
			
			fftTl3 = opTlMax;
			fftTl1 -= opTlStep;
			if(fftTl1 < opTlMin) {
				
				fftTl1 = opTlMax;
				fftTl2 -= opTlStep;
				if(fftTl2 < opTlMin) {
					fftTl2 = opTlMax;
					
					fftop3mul++;
					if(fftop3mul > opMaxVal) {
						fftop3mul = 1;
						fftop1mul++;
						if(fftop1mul > opMaxVal) {
							fftop1mul = 1;
							fftop2mul++;
						}
					}
				}
			}
		}
		if(fftop2mul >opMaxVal)break;
*/
		
		
		
		
		
		
		
		strpara = String.format("%2d,%2d,%2d,%2d,%2d,  %2d,%2d,%2d,%2d,    ",
				fftalg,
				fftop1mul,fftop2mul,fftop3mul,fftop4mul,
				fftTl1, fftTl2, fftTl3, fftTl4);
	
		toneData.setValue(eventSource.Mul, 0, 0, fftop1mul);
		toneData.setValue(eventSource.Mul, 0, 1, fftop2mul);
		toneData.setValue(eventSource.Mul, 0, 2, fftop3mul);
		toneData.setValue(eventSource.Mul, 0, 3, fftop4mul);
	
		toneData.setValue(eventSource.Tlv, 0, 0, fftTl1);
		toneData.setValue(eventSource.Tlv, 0, 1, fftTl2);
		toneData.setValue(eventSource.Tlv, 0, 2, fftTl3);
		toneData.setValue(eventSource.Tlv, 0, 3, fftTl4);
	
		byte [] tbuf = new byte[32];
		toneData.get_tonememory(0, tbuf);
		toneData.setTone(0,  tbuf);	  //値をPanelに標示
	
	
	//System.out.println(strpara);
	try {
		Thread.sleep(500);
	} catch (InterruptedException e1) {
		// TODO 自動生成された catch ブロック
		e1.printStackTrace();
	}
	
		byte [] buf = new byte[4096*4];
		AudioInput in = new AudioInput(audioRec2.audioBit,audioRec2.audioChannel,audioRec2.audioHz);
		in.start();
		in.read(buf);
		in.stop();
	
		for(int i = 0;i < 4096*4;i++) {
			audioRec2.audioBuf[i] = buf[i];
		}
		audioRec2.fttStartPos = 0;
	
		//waveFft(audioRec2, gfft2);
		fftOnly(audioRec2, gfft2);
		// data test save
	
	
	
	    //ノイズ除去
		WaveRecord rec;
		rec = audioRec2;
		int dat[] = new int[N_WAVE/4];
		for(int i = 1;i < N_WAVE/4 -1;i++) {
			dat[i] = (rec.fft[i-1]+rec.fft[i]*2+rec.fft[i+1])/4;
		}
	
	
	  int cnt = 0;
	
	  int sp = 30;
	  int pre2 = dat[sp-4];
	  int pre1 = dat[sp-3];
	  int peak = dat[sp-2];
	  int af1  = dat[sp-1];
	  int af2 = dat[sp];
	
	  int ave = 0;
	  //int base = 49;  //C4固定時
	  int base = 0;
	  int clip = 40;
	  String strdat;
	
	  cnt = 0;
	  strdat = "";
	  for(int i = sp+1 ; i < N_WAVE/4-4;i++) {
	
		   pre2 = dat[i-4];
		   pre1 = dat[i-3];
		   peak = dat[i-2];
		   af1  = dat[i-1];
		   af2 = dat[i];
	
	
		  ave = (pre2  + af2)/2;
	
		  if(pre1 >= peak) continue;
		  if(af1 >  peak) continue;
	
		  if(af1  < af2 )continue;
		  if(pre1 < pre2)continue;
	
		  if(peak < ave +3)continue;
	
		  if(i > 100)clip = 35;
		  if(i > 150) clip = 30;
		  if( i > 200)clip = 5;
		  if( i > 400)clip = 3;
	
		  if(peak > clip) {
			  if(base == 0) {
				  base = i -2;
			  }
	
			  cnt++;
	
			 // System.out.print("(" + (i-2) + ")" + (int)((i-2.0)/base+0.5) + ",");
			 // System.out.print((int)((i-2.0)/base+0.5) + "(" + dat[i-2] + "),");
			 // System.out.print((int)((i-2.0)/base+0.5) +  ",");
	
			 strdat = strdat +  (int)((i-2.0)/base+0.5) + "," + dat[i-2] + ",  ";
			 i += base / 3;
	
			  if(cnt > maxCnt) break;
		  	}
	
	  }
	
	
	
	  if(cnt == maxCnt){
		  fftTl1 = opTlMin ;
		  if(fftTl2 == opTlMax) {
			  fftTl2 = opTlMin;
		  }
	  }
	  if(cnt <4) {
		  fftTl1 -= opTlStep * 2 ;
	  }
	
	  if(cnt >3 && cnt <= maxCnt) {
		//ファイルへデータ追加
	
	
	
	System.out.println(strpara + cnt + " , " + strdat);
	
	
	  String strf;
	  int d;
	  File file = new File("H:\\fftDataH\\fftdata.txt");
	  FileWriter filewriter = null;
		try {
			filewriter = new FileWriter(file,true);  //上書き
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	  BufferedWriter bw = new BufferedWriter(filewriter);
	
	
	
	  	strf =strpara + cnt + " , " + strdat;
	  	try {
				bw.write(strf);
				bw.newLine();
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
	
	
	  try {
			bw.close();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	  } // if(cnt > 2)
	
	
	
	                  }  //while(true){の対
	
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

/* データセット作成時のFFT処理のみ */

	void fftOnly(WaveRecord rec, GraphicsContext g) {
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
