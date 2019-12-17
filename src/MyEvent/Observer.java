package MyEvent;

import javafx.event.EventType;

public interface Observer {
	public void update(EventType<MyDataEvent> e,eventSource source,int ch,int op,int val);

}
