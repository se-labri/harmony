package fr.labri.harmony.source.git.svntojgit;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.tmatesoft.translator.SubGit;

import fr.labri.harmony.core.source.SourceExtractor;
import fr.labri.harmony.source.git.jgit.JGitWorkspace;

public class SvnToJGitWorkspace extends JGitWorkspace {
	
	static {
		System.setSecurityManager(new NoExitSecurityManager());
	}
	
	protected final String EXTENSION = ".tmp";

	public SvnToJGitWorkspace(SourceExtractor<?> sourceExtractor) {
		super(sourceExtractor);
	}

	private List<String> prepareCommand() {
		List<String> cmd = new ArrayList<String>();
		cmd.addAll(Arrays.asList("import","--non-interactive"));
		String url = getUrl();
		if(url.endsWith("/"))
			url = url.substring(0, url.length()-1);
		if(url.endsWith("/trunk")) {
			url = url.substring(0, url.length()-"/trunk".length());
			cmd.add(" --trunk");
			cmd.add("/trunk");
			cmd.add("--svn-url");
			cmd.add(url);
		}
		else {
			cmd.add("--svn-url");
			cmd.add(getUrl());
		}
		cmd.add(getPath()+EXTENSION);
		return cmd;
	}

	@Override
	public void initNewWorkspace() {
		//First, make the clone - prevent the standard output
		try {
			FileUtils.deleteDirectory(new File(getPath()));
		} catch (IOException e) {e.printStackTrace();}
		List<String> cmd = prepareCommand();
		//Hide annoying output from the subgit library
		PrintStream originalStream = System.out;
		PrintStream dummyStream    = new PrintStream(new OutputStream(){
			public void write(int b) {
			}
		});
		System.setOut(dummyStream);
		//this will execute a System.exit(-1);
		try {
			SubGit.main(cmd.toArray(new String[cmd.size()]));
		}
		catch(ExitException e) {}
		System.setOut(originalStream);
		//Now we have created a bare git repo. We have to clone it!
		initNewWorkspaceByUrl(getPath()+EXTENSION);
		try {
			FileUtils.deleteDirectory(new File(getPath()+EXTENSION));
		} catch (IOException e) {e.printStackTrace();}
	}
	

	protected static class ExitException extends SecurityException 
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public final int status;
		public ExitException(int status) 
		{
			this.status = status;
		}
	}

	private static class NoExitSecurityManager extends SecurityManager 
	{
		@Override
		public void checkPermission(Permission perm) 
		{
		}

		@Override
		public void checkExit(int status) 
		{
			throw new ExitException(status);
		}
	}
}