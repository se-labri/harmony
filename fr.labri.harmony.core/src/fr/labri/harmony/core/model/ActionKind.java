package fr.labri.harmony.core.model;

public enum ActionKind {

	Create,

	Delete,

	Edit;

	public static String getShortName(ActionKind kind) {
		switch (kind) {
		case Create:
			return "A";
		case Delete:
			return "D";
		case Edit:
			return "E";
		default: 
			return null;
		}
	}

	public String getShortName() {
		switch(this) {
		case Create:
			return "A";
		case Delete:
			return "D";
		case Edit:
			return "E";
		default:
			return null;
		}
	}
}
