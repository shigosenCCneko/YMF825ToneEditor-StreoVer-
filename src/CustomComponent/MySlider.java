package CustomComponent;



import java.io.IOException;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
/*
 *  Custom Slider with NameLabel and ValueLabel
 */


public class MySlider extends VBox  {
	
    @FXML private Label nameLabel;
    @FXML private Label valueLabel;
    @FXML private Slider slider;
    Event myEvent = null;
	
    public MySlider() {
    	super();
	
    	FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("Myslider.fxml"));
    	fxmlLoader.setRoot(this);
    	fxmlLoader.setController(this);
    	// set FXMLLoader's classloader!
    	fxmlLoader.setClassLoader(getClass().getClassLoader());
        try {
            fxmlLoader.load();            
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }  

        /* accessibleTextに値が設定されたらパラメータを読みに行く */
        this.accessibleTextProperty().addListener(accessibleTextListener);
myEvent = new MySliderEvent(MySliderEvent.UPDATE); //初期化
        /* sliderの値が変化した場合 */
        slider.valueProperty().addListener((
        	ObservableValue<? extends Number> ov,Number old_val,
    		Number new_val) ->{
    			sliderChangeValue(new_val.intValue());
    	}); 	  
        
        
    }
    
    
    /* パラメータの処理     "name,min,max,val"   */
	 ChangeListener<String> accessibleTextListener = new ChangeListener<String>() {
		@Override 
		public void changed(ObservableValue<?extends String>observable,String oldValue,String newValue) {
			String data[] = newValue.split(",");

		   	nameLabel.setText( data[0]);
		   	int i = data.length;
		   	if((i > 1) && (i <= 4)){
		   		slider.setMin(Double.parseDouble(data[1]));
		   		slider.setMax(Double.parseDouble(data[2]));
		   		Double val = Double.parseDouble(data[3]);
		  		slider.setValue(val);
		   	}
		}	
	};
 

    protected void sliderChangeValue(int i) {
    	//int i = 0;

    	//i = (int) slider.getValue();
    	valueLabel.setText(i + "");
    	myEvent = new MySliderEvent(MySliderEvent.MYCHANGE_VALUE,i);
    	Node node = this;
    	node.fireEvent(myEvent);

    }
 	
        
    public void setLabelName(String name) {
    	nameLabel.setText(name);
    }
    
    public void setValue(Double val) {
    	slider.setValue( val);
    }

    public Double getValue() {
    	return slider.getValue();
    }
}
