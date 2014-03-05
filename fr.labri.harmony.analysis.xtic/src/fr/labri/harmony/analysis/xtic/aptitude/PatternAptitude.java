package fr.labri.harmony.analysis.xtic.aptitude;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import fr.labri.CountersSkills;
import fr.labri.harmony.analysis.xtic.Developer;
import fr.labri.harmony.analysis.xtic.ListTimedScore;
import fr.labri.harmony.analysis.xtic.aptitude.filter.ContentFilter;
import fr.labri.harmony.analysis.xtic.aptitude.filter.FileFilter;
import fr.labri.harmony.analysis.xtic.aptitude.filter.KindFilter;
import fr.labri.harmony.analysis.xtic.aptitude.filter.TreeFilter;
import fr.labri.harmony.core.model.Action;

@Entity
public class PatternAptitude {

	@Id
	@GeneratedValue
	private int id;

	private String description = "";
	private String idName;
	@ManyToOne
	private Aptitude aptitude;
	@Transient
	private KindFilter kind;
	@Transient
	private List<FileFilter> files;
	@Transient
	private List<ContentFilter> contents;

	@Transient
	private Parser parser;
	@Transient
	private List<TreeFilter> queries;
	

	public PatternAptitude() {
	}

	public PatternAptitude(String id, String desc, KindFilter kind, List<FileFilter> files,
			List<ContentFilter> content, Parser parser, List<TreeFilter> queries) {
		this.idName = id;
		this.description = desc;
		this.kind = kind;
		this.files = files;
		this.contents = content;
		this.parser = parser;
		this.queries = queries;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idName == null) ? 0 : idName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PatternAptitude other = (PatternAptitude) obj;
		if (idName == null) {
			if (other.idName != null)
				return false;
		} else if (!idName.equals(other.idName))
			return false;
		return true;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}


	public String getIdName() {
		return idName;
	}

	public void setIdName(String idName) {
		this.idName = idName;
	}


	public Aptitude getAptitude() {
		return aptitude;
	}

	public void setAptitude(Aptitude aptitude) {
		this.aptitude = aptitude;
	}
	
	public KindFilter getKind() {
		return kind;
	}

	public void setKind(KindFilter kind) {
		this.kind = kind;
	}

	public List<FileFilter> getFiles() {
		return files;
	}

	public void setFiles(List<FileFilter> files) {
		this.files = files;
	}

	public List<TreeFilter> getQueries() {
		return queries;
	}

	public void setQueries(List<TreeFilter> queries) {
		this.queries = queries;
	}


	public boolean acceptKind(Action a) {
		if(this.kind.executeFilter(null, a.getKind().name())==1)
			return true;
		return false;
	}

	public boolean acceptFile(Action src, Action tgt) {
		for(FileFilter ff : this.files) {
			if(src!=null && tgt!=null) {
				if(ff.executeFilter(src.getItem().getNativeId(), tgt.getItem().getNativeId())==1)
					return true;
			}
			else if(src==null && tgt!=null) { 
				if(ff.executeFilter("", tgt.getItem().getNativeId())==1)
					return true;
			}
			else if(src!=null && tgt==null) 
				if(ff.executeFilter(src.getItem().getNativeId(), "")==1)
					return true;
		}
		return false;
	}

	public List<ContentFilter> getContents() {
		return contents;
	}

	public void setContents(List<ContentFilter> contents) {
		this.contents = contents;
	}

	public Parser getParser() {
		return parser;
	}

	public void setParser(Parser parser) {
		this.parser = parser;
	}

	public boolean needContentNewFile() {
		for (ContentFilter pc : this.contents) {
			if(pc.getDirection().equals("both") || pc.getDirection().equals("target"))
				return true;
		}
		return false;
	}

	public boolean needContentOldFile() {
		for (ContentFilter pc : this.contents) {
			if(pc.getDirection().equals("both") || pc.getDirection().equals("source"))
				return true;
		}
		return false;
	}

	public boolean needDiffOldFile() {
		for(TreeFilter tf : this.queries) {
			if(tf.getDirection().equals("both") || tf.getDirection().equals("source"))
				return true;
		}
		return false;
	}

	public boolean needDiffNewFile() {
		for(TreeFilter tf : this.queries) {
			if(tf.getDirection().equals("both") || tf.getDirection().equals("target"))
				return true;
		}
		return false;
	}

	public ListTimedScore scoreFor(Developer dev) { // TODO Accept A formula
		CountersSkills<PatternAptitude> skills = dev.getSkills();
		return skills.get(this);
	}
}
