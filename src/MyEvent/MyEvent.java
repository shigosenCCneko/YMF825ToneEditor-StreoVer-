package MyEvent;
	import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;


public class MyEvent extends Event{

	private int eventSource =0;
	private int eventValue = 0;;

	public int getEventSource() {
		return eventSource;
	}

	public int getEventValue() {
		return eventValue;
	}





	static {
		MYFILECHANGE_VALUE= new EventType<>(Event.ANY,"MYFILECHANGE_VALUE");
		FILEUPDATE = new EventType<>(Event.ANY,"FILEUPDATE");

	}

	public static final EventType<MyEvent>MYFILECHANGE_VALUE;

	public static final EventType<MyEvent>FILEUPDATE;



//	static {
//		MYCHANGE_VALUE= new EventType<>(Event.ANY,"MYCHANGE_VALUE");
//		UPDATE = new EventType<>(Event.ANY,"UPDATE");
//	}
//
	public void initialize() {

	}


	public MyEvent(EventType<MyEvent> eventType,int val) {
		super(eventType);
		eventValue = val;
	}

	public MyEvent(EventType<MyEvent> eventType,int source, int val) {
		super(eventType);
		this.eventSource = source;
		this.eventValue = val;
	}




	public MyEvent(EventTarget target,EventType<MyEvent> eventType) {
		super(target,target,eventType);
	}

	public MyEvent(EventType<MyEvent> eventType) {
		super(eventType);
	}

	public MyEvent(Object source, EventTarget target, EventType<MyEvent> eventType) {
		super( source, target, eventType);
	}
	public MyEvent(Object source, EventType<MyEvent> eventType) {
		super(eventType);
		this.source = source;
	}

}







