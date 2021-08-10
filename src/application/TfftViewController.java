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
	@FXML Slider magSlider1;
	@FXML Slider yMagSlider1;
	@FXML Slider fftSlider1;


	@FXML Slider positionSlider2;
	@FXML Slider magSlider2;
	@FXML Slider yMagSlider2;
	@FXML Slider fftSlider2;






	final int viewCenter = 300;


	WaveRecord audioRec1 = new WaveRecord(16,1,44000,2);
	WaveRecord audioRec2 = new WaveRecord(16,1,44000,2);


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
			fft = new int[256];
		}








	}

	@FXML void initialize(){
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
			return read(buf,0,buf.length);
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
		waveRecord(audioRec1,g1);

		waveFft(audioRec1,gfft1);



	}

	@FXML void testRecord2(){
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





  // FFT-------------------------------------------


void drawFft(GraphicsContext g, int data[]) {
	g.clearRect(0, 0, 400, 400);
	for(int i = 1;i < 127;i++) {
    	int fdata = data[i];
		g.strokeLine(10+i, 110-fdata, 10+i, 110);
	}
}

void waveFft(WaveRecord rec, GraphicsContext g) {
	int [] source = new int[256];
	int [] target = new int[256];
	byte[]  buf = rec.audioBuf;
	int pos = rec.fttStartPos;
	int data;
	for(int i = 0;i < 256; i++) {
		 data = ( (int)(buf[pos*2+i*16+1] <<8) | (0x00ff & buf[pos*2+i*16]));
		 data >>= 3;
		source[i] = data;
		target[i] = 0;


	}
	fix_fft2(source,target);

    	int j,k;
    for (int i = 0; i <  128 ; i++) {

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



int Sinewave[]  = {
  0, 3, 6, 9, 12, 15, 18, 21,
  24, 28, 31, 34, 37, 40, 43, 46,
  48, 51, 54, 57, 60, 63, 65, 68,
  71, 73, 76, 78, 81, 83, 85, 88,
  90, 92, 94, 96, 98, 100, 102, 104,
  106, 108, 109, 111, 112, 114, 115, 117,
  118, 119, 120, 121, 122, 123, 124, 124,
  125, 126, 126, 127, 127, 127, 127, 127,

  127, 127, 127, 127, 127, 127, 126, 126,
  125, 124, 124, 123, 122, 121, 120, 119,
  118, 117, 115, 114, 112, 111, 109, 108,
  106, 104, 102, 100, 98, 96, 94, 92,
  90, 88, 85, 83, 81, 78, 76, 73,
  71, 68, 65, 63, 60, 57, 54, 51,
  48, 46, 43, 40, 37, 34, 31, 28,
  24, 21, 18, 15, 12, 9, 6, 3,

  0, -3, -6, -9, -12, -15, -18, -21,
  -24, -28, -31, -34, -37, -40, -43, -46,
  -48, -51, -54, -57, -60, -63, -65, -68,
  -71, -73, -76, -78, -81, -83, -85, -88,
  -90, -92, -94, -96, -98, -100, -102, -104,
  -106, -108, -109, -111, -112, -114, -115, -117,
  -118, -119, -120, -121, -122, -123, -124, -124,
  -125, -126, -126, -127, -127, -127, -127, -127,

  /*-127, -127, -127, -127, -127, -127, -126, -126,
    -125, -124, -124, -123, -122, -121, -120, -119,
    -118, -117, -115, -114, -112, -111, -109, -108,
    -106, -104, -102, -100, -98, -96, -94, -92,
    -90, -88, -85, -83, -81, -78, -76, -73,
    -71, -68, -65, -63, -60, -57, -54, -51,
    -48, -46, -43, -40, -37, -34, -31, -28,
    -24, -21, -18, -15, -12, -9, -6, -3, */
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
  for (m = 1; m <=255; ++m) {
    l = 256;
    do {
      l >>= 1;
    } while (mr + l >255);
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
  k = 8 - 1;
  while (l < 256) {

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
      wr =  Sinewave [ j + 256 / 4];

      wi = -Sinewave[j];


      wr >>= 1;
      wi >>= 1;

      for (i = m; i < 256; i += istep) {
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
