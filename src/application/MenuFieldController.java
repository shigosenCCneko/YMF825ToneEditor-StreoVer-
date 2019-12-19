package application;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import DataClass.Ymf825ToneData;
import MyEvent.MyDataEvent;
import MyEvent.MyDataListener;
import MyEvent.eventSource;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MenuFieldController implements MyDataListener{


	@FXML MenuBar	menuBar;
	@FXML MenuItem  loadToneSet;
	@FXML MenuItem  saveToneSet;
	@FXML MenuItem  deviceToneSet;

	@FXML MenuItem  loadTone;
	@FXML MenuItem  saveTone;
	@FXML MenuItem  deviceTone;

	@FXML MenuItem  copy12to34;
	@FXML MenuItem  copy12to34clear;
	@FXML MenuItem  copy12to23;
	@FXML MenuItem  copy1to234;
	@FXML MenuItem  viewSoftModulation;
	@FXML MenuItem  reset;

	@FXML MenuItem  monoMode;
	@FXML MenuItem  polyMode;
	@FXML MenuItem  d8polyMode;

	Ymf825ToneData toneData;
	FXMLLoader softModuLoader;
	Parent		softModuRoot;
	Stage	    softModuEditor;
	String workDir;

public MenuFieldController() throws IOException{
	softModuLoader = new FXMLLoader(getClass().getResource("SoftwareModulation.fxml"));

	softModuRoot = (Parent)softModuLoader.load();

	softModuEditor	= new Stage();
	softModuEditor.setScene(new Scene(softModuRoot));	
	softModuEditor.setResizable(false);
	softModuEditor.setAlwaysOnTop(true);
	softModuEditor.setTitle("Software Modulation");
	
	
}

	public void initialize() {
		toneData = Ymf825ToneData.getInstance();
	//	toneData.addListener(this);
	
		
		
		Properties properties = new Properties();

			try{
				InputStream istream = new FileInputStream("ymf825.properties");
				properties.load(istream);
				workDir =  properties.getProperty("workDir");

				istream.close();
			}catch(IOException e){
				e.printStackTrace();
			}

		
	}

	
	
	@FXML void loadToneSet() {
		System.out.println("Load tone set");
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Load Tone Set");
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("SDS", "*.sds"),
		        new FileChooser.ExtensionFilter("ALL", "*.*"));
		
		File dir = new File(workDir);
		if(dir.exists() == false) {
			dir = new File(System.getProperty("user.home"));
		}	
		fileChooser.setInitialDirectory(dir); 
		
		
        File file = fileChooser.showOpenDialog(null);
	
		if(file != null) {		
			try {
				FileInputStream fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis);
				byte rbuf[] = new byte[480];
				int len = bis.read(rbuf);
				
				if(len == 480){
					Ymf825ToneData aaa = Ymf825ToneData.getInstance();										
					aaa.setToneSet(rbuf);									
				}
				bis.close();
				
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
	}

	@FXML void saveToneSet() {
		Ymf825ToneData ymf825 = Ymf825ToneData.getInstance();
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Tone Set");
		fileChooser.getExtensionFilters().addAll(
		        new FileChooser.ExtensionFilter("SDS", "*.sds"));
		
 
		
		File dir = new File(workDir);
		if(dir.exists() == false) {
			dir = new File(System.getProperty("user.home"));
		}	
		fileChooser.setInitialDirectory(dir); 
		File file = fileChooser.showSaveDialog(null);

		if(file != null) {
			byte buf[] = ymf825.getToneDataSet();
			byte wbuf[]= new byte [480];
			for(int i = 0;i < 480;i++){
				wbuf[i] = (byte)(buf[i] & 0xff);
			}
			try{
				
				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				bos.write(wbuf);
				bos.flush();
				bos.close();
			}catch(IOException err){
				System.out.println(err);
			}
		}
	}

	@FXML void deviceToneSet() {
		byte [] buf = new byte[32];
		int ch = PanelController.getPanelChannel();
		
		for(int i = 0;i<16;i++){
			if(ch != i) {
				toneData.get_tonememory(i, buf);
				toneData.setOpData(i,  buf);
				//toneData.setTone(i, buf);
			}	
		}
		toneData.get_tonememory(ch, buf);
		toneData.setTone(ch,  buf);			//Panelの表示のため
		

	}

	
	@FXML void loadTone() {
		//System.out.println("Load tone ");
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Load Tone");
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("SDS", "*.sd1"),
		        new FileChooser.ExtensionFilter("ALL", "*.*"));

		File dir = new File(workDir);
		if(dir.exists() == false) {
			dir = new File(System.getProperty("user.home"));
		}	
		fileChooser.setInitialDirectory(dir); 
		
        File file = fileChooser.showOpenDialog(null);
	
		if(file != null) {		
			try {
				FileInputStream fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis);
				byte rbuf[] = new byte[30];
				int len = bis.read(rbuf);
			
				if(len == 30){
					Ymf825ToneData aaa = Ymf825ToneData.getInstance();										
					aaa.setTone(PanelController.getPanelChannel(), rbuf);		

					
				}
				bis.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	@FXML void saveTone() {
		Ymf825ToneData ymf825 = Ymf825ToneData.getInstance();
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Tone");
		fileChooser.getExtensionFilters().addAll(
		        new FileChooser.ExtensionFilter("SDS", "*.sd1"));
		
		File dir = new File(workDir);
		if(dir.exists() == false) {
			dir = new File(System.getProperty("user.home"));
		}	
		fileChooser.setInitialDirectory(dir); 
		File file = fileChooser.showSaveDialog(null);

		if(file != null) {
			byte buf[] = new byte[30];
			ymf825.getToneData(PanelController.getPanelChannel(),buf);
			byte wbuf[]= new byte [30];
			for(int i = 0;i < 30;i++){
				wbuf[i] = (byte)(buf[i] & 0xff);
			}
			try{
				
				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				bos.write(wbuf);
				bos.flush();
				bos.close();
			}catch(IOException err){
				System.out.println(err);
			}
		}
	}		


	@FXML void deviceTone() {
		byte [] buf = new byte[32];
		
		int ch = PanelController.getPanelChannel();
		toneData.get_tonememory(ch, buf);
		toneData.setTone(ch,  buf);

		
	}

	private void copyOp(int source,int target,byte buf[]){
		int from = 2+source*7;
		int to = 2 + target* 7; 
		for(int i = 0;i < 7;i++){
			buf[to+i] = buf[from+i];
		}
		
	}
	private void clearOp(int opno,byte buf[]){
		byte source[] = { 2, 80, -16, -4, 0, 16, 3};
		int to = 2+opno*7;
		for(int i = 0;i<7;i++){
			buf[to+i] = source[i];
		}
	}	
	

	
	@FXML void copy12to34() {
		byte buf[] = new byte[30];
		toneData.getToneData(PanelController.getPanelChannel(),buf);
		copyOp(0,2,buf);
		copyOp(1,3,buf);
		toneData.setTone(PanelController.getPanelChannel(),buf);
	}
	@FXML void copy12to34Clear() {
		byte buf[] = new byte[30];
		toneData.getToneData(PanelController.getPanelChannel(),buf);
		copyOp(0,2,buf);
		copyOp(1,3,buf);
		clearOp(0,buf);
		clearOp(1,buf);
		toneData.setTone(PanelController.getPanelChannel(),buf);
	}
	@FXML void copy12to23() {
		byte buf[] = new byte[30];	
		toneData.getToneData(PanelController.getPanelChannel(),buf);
		copyOp(1,2,buf);
		copyOp(0,1,buf);
		clearOp(0,buf);
		clearOp(3,buf);
		toneData.setTone(PanelController.getPanelChannel(),buf);	
		
	}
	@FXML void copy1to234() {
		byte buf[] = new byte[30];
		toneData.getToneData(PanelController.getPanelChannel(),buf);
		copyOp(0,1,buf);
		copyOp(0,2,buf);
		copyOp(0,3,buf);
		toneData.setTone(PanelController.getPanelChannel(),buf);
	}
	@FXML void viewSoftwareModulation() {
		softModuEditor.show();

	}
	@FXML void resetYMF825() {
		toneData.reset();

	}
	@FXML void monoMode() {
		toneData.monoMode();
	}
	@FXML void polyMode() {
		toneData.polyMode();
	}
	@FXML void d8polyMode() {
		toneData.d8polyMode();
	}



	@Override
	public void changeValue(EventType<MyDataEvent> e) {
		System.out.println("change from listener");
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public void changeValue(EventType<MyDataEvent> e, eventSource source,int opNo, int val) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

}


