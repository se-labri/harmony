package fr.labri.harmony.core.model;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * An action is a performed on an {@link Item}, during an {@link Event}.
 * Typically, in a VCS it can be a modification performed on a file.
 * 
 * The kind of the action can be any value of the {@link ActionKind} enumeration.
 *
 */
@Entity
public class Action extends SourceElement {

	public final static String RENAME_KEY = "renamed";
	public final static String CHURN_KEY = "churn";
	
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((event == null) ? 0 : event.hashCode());
		result = prime * result + ((item == null) ? 0 : item.hashCode());
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		result = prime * result + ((parentEvent == null) ? 0 : parentEvent.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		Action other = (Action) obj;
		if (event == null) {
			if (other.event != null) return false;
		} else if (!event.equals(other.event)) return false;
		if (item == null) {
			if (other.item != null) return false;
		} else if (!item.equals(other.item)) return false;
		if (kind != other.kind) return false;
		if (parentEvent == null) {
			if (other.parentEvent != null) return false;
		} else if (!parentEvent.equals(other.parentEvent)) return false;
		return true;
	}
    
    

}
