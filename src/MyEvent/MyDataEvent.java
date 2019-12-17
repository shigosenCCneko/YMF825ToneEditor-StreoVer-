package MyEvent;

import javafx.event.Event;
import javafx.event.EventType;

public class MyDataEvent extends Event {
	
			
			private eventSource source = eventSource.Atck;
			private int eventValue = 0;
			
			@Override
			public eventSource getSource() {
				return source;
			}
			
			public int getValue() {
				return eventValue;
			}
			
			public void setValue(eventSource s,int  v) {
				source = s;
				eventValue = v;
			}
			
			static {
				MYFILECHANGE_VALUE= new EventType<>(Event.ANY,"MYFILECHANGE_VALUE");
				DATA_UPDATE = new EventType<>(Event.ANY,"DATAUPDATE");
				OPDATA_CHANGE = new EventType<>(Event.ANY,"OPDATA_CHANGE");
				CHANGECHANNEL = new EventType<>(Event.ANY,"CHANGE_CHANNEL");
			}
			
			public static final EventType<MyDataEvent>MYFILECHANGE_VALUE;
			public static final EventType<MyDataEvent>DATA_UPDATE;			
			public static final EventType<MyDataEvent>OPDATA_CHANGE;
			public static final EventType<MyDataEvent>CHANGECHANNEL;
//			private MyDataListener listener = null;
			
			public MyDataEvent(EventType<MyEvent>eventType,eventSource s ,int v) {
				super(eventType);
				source = s;
				eventValue = v;
			}
				
		
			
			public MyDataEvent(EventType<MyDataEvent> eventType) {
				super(eventType);		
				
			}
			
						
			
//			public void addListener(MyDataListener listener) {
//				this.listener = listener;
//			}
}
