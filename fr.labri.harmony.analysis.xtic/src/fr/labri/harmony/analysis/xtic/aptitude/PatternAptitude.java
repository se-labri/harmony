package fr.labri.harmony.analysis.xtic.aptitude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import fr.labri.CountersSkills;
import fr.labri.harmony.analysis.xtic.Developer;
import fr.labri.harmony.analysis.xtic.ListTimedScore;
import fr.labri.harmony.analysis.xtic.PatternContent;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.ActionKind;


@Entity
public class PatternAptitude {
	
	@Id
	@GeneratedValue
	private int id;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private String description = "";

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		if (id != other.id)
			return false;
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

	private String idName;

	public String getIdName() {
		return idName;
	}

	public void setIdName(String idName) {
		this.idName = idName;
	}
	
	@ManyToOne
	private Aptitude aptitude;
	
	public Aptitude getAptitude() {
		return aptitude;
	}

	public void setAptitude(Aptitude aptitude) {
		this.aptitude = aptitude;
	}

	public void setQueries(Query queries) {
		this.queries = queries;
	}

	public PatternAptitude() {
	}

	public PatternAptitude(String id, String desc, String action, Map<String, String> files,
			List<PatternContent> content, Parser parser, Query query) {
		this.idName = id;
		this.description = desc;
		this.action = action;
		for (String file : files.keySet())
			this.mime.put(Pattern.compile(file), files.get(file));
		this.contents = content;
		this.parser = parser;
		this.queries = query;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	@Transient
	private String action;
	@Transient
	private Map<Pattern, String> mime = new HashMap<Pattern, String>();
	
	public Map<Pattern, String> getMime() {
		return mime;
	}

	public void setMime(Map<Pattern, String> mime) {
		this.mime = mime;
	}

	@Transient
	private List<PatternContent> contents = new ArrayList<PatternContent>();

	@Transient
	private Parser parser;
	@Transient
	private Query queries;

	public boolean acceptAction(Action a) {
		return ((a.getKind().equals(ActionKind.Create) && action
				.equals("added")) || (a.getKind().equals(ActionKind.Edit) && action
						.equals("modified")));
	}

	public boolean acceptFile(Action a) {
		for (Pattern pat : mime.keySet()) {
			if (mime.get(pat).equals("target")) {
				Matcher m = pat.matcher(a.getItem().getNativeId().trim());
				if (m.find())
					return true;
			}
		}
		return false;
	}

	public List<PatternContent> getContents() {
		return contents;
	}

	public void setContents(List<PatternContent> contents) {
		this.contents = contents;
	}

	public Parser getParser() {
		return parser;
	}

	public void setParser(Parser parser) {
		this.parser = parser;
	}

	public Query getQueries() {
		return queries;
	}

	public void setDiffs(Query queries) {
		this.queries = queries;
	}

	public boolean needContent(boolean newFile) {
		for (PatternContent pc : this.contents) {
			if(pc.getDirection().equals("both"))
				return true;
			if (newFile && (pc.getDirection().equals("target")))
				return true;
			if (!newFile && (pc.getDirection().equals("source")))
				return true;
		}
		return true;
	}

	public boolean needDiff(boolean targetNewFile) {
		if(queries.get_exprBoth() != null)
			return true;
		if (queries.get_exprSource()!=null && queries.get_exprTarget()!=null)
			return true;
		if (!targetNewFile && queries.get_exprSource()!=null)
			return true;
		if (targetNewFile && queries.get_exprTarget()!=null)
			return true;
		return false;
	}
	

	 public ListTimedScore scoreFor(Developer dev) { // TODO Accept A formula
         CountersSkills<PatternAptitude> skills = dev.getSkills();
         return skills.get(this);
	 }
}
