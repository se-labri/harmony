package fr.labri.harmony.core;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import fr.labri.harmony.core.config.model.DatabaseConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.dao.DaoImpl;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.ActionKind;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.model.Metadata;
import fr.labri.harmony.core.model.Source;

public class TestCommand implements CommandProvider {

	public void _test(CommandInterpreter it) {
		Source s = new Source();
		s.setUrl("http://source");
		Author a = new Author();
		a.setNativeId("author");
		a.setName("author");
		a.setSource(s);
		s.getAuthors().add(a);
		Item i = new Item();
		i.setNativeId("item");
		i.setSource(s);
		s.getItems().add(i);
		Event e = new Event();
		e.setNativeId("hash");
		e.setTimestamp(0L);
		e.setSource(s);
		e.setAuthors(new ArrayList<Author>(Arrays.asList(new Author[] {a})));
		a.getEvents().add(e);
		s.getEvents().add(e);
		Action ac = new Action();
		ac.setKind(ActionKind.Create);
		ac.setEvent(e);
		ac.setItem(i);
		ac.setSource(s);
		s.getActions().add(ac);
		e.getActions().add(ac);
		i.getActions().add(ac);
		Metadata d = new Metadata();
		d.setName("test");
		d.setValue("testValue");

		Dao dao = getDao("data/harmony/db/test");
		dao.saveSource(s);
		dao.disconnect();
	}

	public Dao getDao(String path) {
		DaoImpl dao = new DaoImpl(new DatabaseConfiguration("jdbc:h2:" + path, "sa", "", "org.h2.Driver"));
		return dao;
	}

	@Override
	public String getHelp() {
		return null;
	}

}
