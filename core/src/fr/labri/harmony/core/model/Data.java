package fr.labri.harmony.core.model;

public interface Data {
	
	public static int SOURCE = 0;
	
	public static int EVENT = 1;
	
	public static int AUTHOR = 2;
	
	public static int ACTION = 3;
	
	public static int ITEM = 4;
	
	int getElementId();

	void setElementId(int id);
	
	int getElementKind();
	
	void setElementKind(int kind);

}
