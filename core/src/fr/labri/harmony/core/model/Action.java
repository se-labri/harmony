package fr.labri.harmony.core.model;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Action extends SourceElement {

	@Id @GeneratedValue
	private int id;
	
	@Enumerated
    private ActionKind kind;

    @ManyToOne
    @JoinColumn(name="eventId", nullable=false)
    private Event event;

    @ManyToOne
    @JoinColumn(name="parentEventId", nullable=true)
    private Event parentEvent;

    @ManyToOne
    @JoinColumn(name="itemId", nullable=false)
    private Item item;

	public Action() {
        super();
    }

    public Action(Item item, ActionKind kind, Event event, Event parentEvent, Source source) {
        this();
        this.item = item;
        this.kind = kind;
        this.event = event;
        this.parentEvent = parentEvent;
        setSource(source);
    }
    
    public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

    public ActionKind getKind() {
        return kind;
    }

    public void setKind(ActionKind kind) {
        this.kind = kind;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Event getParentEvent() {
        return parentEvent;
    }

    public void setParentEvent(Event parentEvent) {
        this.parentEvent = parentEvent;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
    
    public String toString() {
		return kind + " " + item;
	}

}
