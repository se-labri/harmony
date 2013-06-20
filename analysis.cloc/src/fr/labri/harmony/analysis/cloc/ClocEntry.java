package fr.labri.harmony.analysis.cloc;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class ClocEntry {
	
	@Id @GeneratedValue
	private int id;

	private String language;
	
	private int code;

	private int blank;

	private int comment;

	private int file;
	
	public ClocEntry() {
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getBlank() {
		return blank;
	}

	public void setBlank(int blank) {
		this.blank = blank;
	}

	public int getComment() {
		return comment;
	}

	public void setComment(int comment) {
		this.comment = comment;
	}

	public int getFile() {
		return file;
	}

	public void setFile(int file) {
		this.file = file;
	}

}
