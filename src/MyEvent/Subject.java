package MyEvent;

import java.util.Enumeration;
import java.util.Vector;

import javafx.event.EventType;

class Subject {
	Vector<Observer> observers;
	
	public void attach(Observer o) {
		observers.addElement(o);
	}
	
	public void detatch(Observer o) {
		observers.removeElement(o);
		
	}
	
	public void notifyChange(EventType<MyDataEvent> e,eventSource source,int ch, int op,int val) {
		for(Enumeration<Observer> i = observers.elements(); i.hasMoreElements();) {
			Observer o = i.nextElement();
			o.update( e,source , ch, op, val);
		}
	}
}
