package MyEvent;

import java.util.EventListener;

import javafx.event.EventType;



public interface MyDataListener extends EventListener{
	
		

		public void changeValue(EventType<MyDataEvent> e);
		public void changeValue(EventType<MyDataEvent> e,eventSource source,int op,int val);
		

}
