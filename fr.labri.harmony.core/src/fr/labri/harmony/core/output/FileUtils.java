package fr.labri.harmony.core.output;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

public class FileUtils {
	
	public static void copyFile(String srcPlugin, String srcLocation,IFolder dest,IProgressMonitor monitor){
		Bundle bundle = Platform.getBundle(srcPlugin);
		
		try {
		    URL url = FileLocator.find(bundle, new Path(srcLocation), null);
		    if(url != null){
		    InputStream is = FileLocator.resolve(url).openStream();
		    IFile newFile = dest.getFile(FilenameUtils.getName(url.getPath()));
            newFile.create(is, true, monitor);	 
            } else {
            	System.err.println("Could not found file");
            }
		} catch (Exception e) {
			System.err.println("Could not copy the file: "+e.getMessage());
		}
	}
	
	public static void copyFile(String srcPlugin, String srcLocation,java.nio.file.Path dest){
		Bundle bundle = Platform.getBundle(srcPlugin);
		
		try {
		    URL url = FileLocator.find(bundle, new Path(srcLocation), null);
		    if(url != null){
			    InputStream is = FileLocator.resolve(url).openStream();
			    CopyOption[] options = new CopyOption[]{ 
			    		  StandardCopyOption.REPLACE_EXISTING
			    	    }; 
			    Files.copy(is,(java.nio.file.Path) dest,options);   
            } else {
            	System.err.println("Could not found file");
            }
		} catch (Exception e) {
			System.err.println("Could not copy the file: "+e.getMessage());
		}
	}
	
	public static String getFileContent(String srcPlugin, String srcLocation,IProgressMonitor monitor){
		Bundle bundle = Platform.getBundle(srcPlugin);
		String content = "";
		try {
		    URL url = FileLocator.find(bundle, new Path(srcLocation), null);
		    if(url != null){
		    InputStream is = FileLocator.resolve(url).openStream();
		    BufferedReader in = new BufferedReader(new InputStreamReader(is));
		    String cLine = null;		 
		    while ((cLine = in.readLine()) != null) {
		    	content += "	 		"+cLine+System.getProperty("line.separator");
		    }
		    in.close();
            } else {
            	System.err.println("Could not found file");
            }
		} catch (Exception e) {
			System.err.println("Could not copy the file: "+e.getMessage());
		}
		
		return content;
	}

}
