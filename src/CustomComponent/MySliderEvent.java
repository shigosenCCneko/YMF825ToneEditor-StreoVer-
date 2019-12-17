	package CustomComponent;
	import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
	
	
public final class MySliderEvent extends Event{
	
	private int eventSource =0;
	private int eventValue = 0;;
	
	public int getEventSource() {
		return eventSource;
	}
	
	public int getEventValue() {
		return eventValue;
	}
	

	
	
	
	static {
		MYCHANGE_VALUE= new EventType<>(Event.ANY,"MYCHANGE_VALUE");
		UPDATE = new EventType<>(Event.ANY,"UPDATE");
	}
	
	public static final EventType<MySliderEvent>MYCHANGE_VALUE;
	
	public static final EventType<MySliderEvent>UPDATE;
	
//	static {
//		MYCHANGE_VALUE= new EventType<>(Event.ANY,"MYCHANGE_VALUE");
//		UPDATE = new EventType<>(Event.ANY,"UPDATE");
//	}
//	
	public void initialize() {
		
	}
		
	
	public MySliderEvent(EventType<MySliderEvent> eventType,int val) {
		super(eventType);		
		eventValue = val;
	}
	
	public MySliderEvent(EventType<MySliderEvent> eventType,int source, int val) {
		super(eventType);
		this.eventSource = source;
		this.eventValue = val;
	}
	
	
	
	
	public MySliderEvent(EventTarget target,EventType<MySliderEvent> eventType) {
		super(target,target,eventType);
	}
	
	public MySliderEvent(EventType<MySliderEvent> eventType) {
		super(eventType);
	}
	
	public MySliderEvent(Object source, EventTarget target, EventType<MySliderEvent> eventType) {
		super( source, target, eventType);
	}
	public MySliderEvent(Object source, EventType<MySliderEvent> eventType) {
		super(eventType);
		this.source = source;
	}

}
	
	


	


