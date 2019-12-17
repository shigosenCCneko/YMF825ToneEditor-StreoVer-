package application;
 


import DataClass.Ymf825ToneData;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
 

public class Panel extends Application 
{
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
      System.setProperty("prism.allowhidpi","false");
        System.setProperty( "prism.lcdtext" , "false" );
         
        FXMLLoader      fxmlLoader  = new FXMLLoader( getClass().getResource( "Panel.fxml" ) );
        Pane    root        = (Pane) fxmlLoader.load();
        Scene   scene       = new Scene( root , 650 , 800 );
        
        System.out.println("sc=" + System.getProperty("prism.allwhidpi"));          

        
  Screen screen = Screen.getPrimary();
  System.out.println("sc=" + screen);      
        
        
  

  
  
  
  
        
        Scale scale = new Scale(0.8,0.8);
        

        
        
        
        
        scale.setPivotX(0);
        scale.setPivotY(0);
        //scene.getRoot().getTransforms().setAll(scale);
        root.setMinSize(650, 800);
        
        
       primaryStage.addEventHandler(WindowEvent.WINDOW_SHOWN ,new EventHandler<WindowEvent>() {
     	   @Override
     	   public void handle(WindowEvent window) {
     

     	   }	
        });	 
        primaryStage.setResizable(false);
        //primaryStage.toFront();
        primaryStage.setTitle("YMF825 Tone Editor");
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
        	@Override
        	public void handle(WindowEvent t) {
        		   System.out.println("window close");
        		   
        		   
      		   Alert alert = new Alert(AlertType.NONE,"",ButtonType.OK,
      				   									 ButtonType.CANCEL);
      		   alert.setTitle("終了");
      		   alert.getDialogPane().setContentText( "終了してもよろしいですか？" );
      		   
      		 ButtonType              button  = alert.showAndWait().orElse( ButtonType.CANCEL );      		
// System.out.println(button.getButtonData());
      		 if(button.getButtonData() != ButtonData.OK_DONE) {
      			 System.out.println("not end");
      			 t.consume();  			 
      		 }else {
      			 System.out.println("end OK");
      			 Platform.exit();
      		 }

        	
        	}
        });

        primaryStage.setScene( scene );
        primaryStage.show();
        

 
    }
    @Override
    public void stop() {
    	/* 終了処理 */
    	//System.out.println("end application");
    	Ymf825ToneData.getInstance().close();
    	javafx.application.Platform.exit();
    }

 
 
}