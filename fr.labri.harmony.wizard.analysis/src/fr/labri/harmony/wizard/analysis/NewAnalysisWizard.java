package fr.labri.harmony.wizard.analysis;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;



/**
 * Wizard for creating a new Harmony analysis project (OSGi bundle)
 *
 */
public class NewAnalysisWizard extends Wizard implements INewWizard,IExecutableExtension {
	
	private static final String MANIFEST_MF = "MANIFEST.MF";
	private static final String META_INF = "META-INF";
	private static final String OSGI_INF = "OSGI-INF";
	private static final String ANALYSIS_DESCRIPTOR = "analysis.xml";
	private static final String PERSISTENCE_DESCRIPTOR = "persistence.xml";
	
	private NewAnalysisWizardPage harmonyPage;
	private ISelection selection;
	private IConfigurationElement configElem;

	/**
	 * Constructor for NewAnalysisWizard.
	 */
	public NewAnalysisWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		harmonyPage = new NewAnalysisWizardPage();
		addPage(harmonyPage);
	}

	/**
	 * This method is called when the 'Finish' button is pressed in
	 * the wizard. We will create an operation for creating the analysis project and run it
	 * using the wizard as execution context.
	 */
	public boolean performFinish() {
		// We collect data from the wizard page to pass it to the working thread
		final String analysisProjectName = harmonyPage.getProjectName();
		final IPath projectLocation = harmonyPage.getProjectLocation();
		final String analysisClassName = harmonyPage.getAnalysisClassName();
		final boolean databaseRequired = harmonyPage.isStorageFacilitiesRequired();
		final Set<String> tailoredDataTypes = harmonyPage.getTailoredDataTypes();
		
		
		// We create the task
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					
					doFinish(analysisProjectName,projectLocation, analysisClassName,databaseRequired,tailoredDataTypes, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * This method is in charge of creating a new Harmony analysis project
	 * 
	 * @param projectName Name and ID of the Harmony analysis Project (it is also the name of the root Java package)
	 * @param projectLocation Path of the project location, null if the default location is selected by the user.
	 * @param analysisClassName Name of the class that contains the analysis code.
	 * @param databaseRequired Indicate if the analysis uses the database functionalities (whiteboard) provided by the Harmony framework 
	 * @param tailoredDataTypes List of custom data types to be saved in the database
	 * @param monitor Progress monitor of the thread
	 * @return
	 * @throws CoreException
	 */
	private boolean doFinish(String projectName, IPath projectLocation, String analysisClassName, boolean databaseRequired, Set<String> tailoredDataTypes,IProgressMonitor monitor) throws CoreException {
		 try {
			 
			 // Project creation
             IJavaProject harmonyProject = JavaCore.create(getProject(projectName));
             final IProjectDescription projectDescription = ResourcesPlugin.getWorkspace().newProjectDescription(projectName);
             projectDescription.setLocation(projectLocation);
             projectDescription.setComment("Project defining an analysis for the Harmony Framework");
             getProject(projectName).create(projectDescription, monitor);

             // Nature
             projectDescription.setNatureIds(getNatures());

             // Builders
             String[] builderIDs = getBuilders();
             ICommand[] buildCMDS = new ICommand[builderIDs.length];
             int i = 0;
             for (String builderID : builderIDs) {
                     ICommand build = projectDescription.newCommand();
                     build.setBuilderName(builderID);
                     buildCMDS[i++] = build;
             }
             projectDescription.setBuildSpec(buildCMDS);
             getProject(projectName).open(monitor);
             getProject(projectName).setDescription(projectDescription, monitor);
             
             //Classpath
             List<IClasspathEntry> classpathEntries = getClasspathsEntries(projectName);
             harmonyProject.setRawClasspath(classpathEntries.toArray(new IClasspathEntry[classpathEntries.size()]), monitor);
             harmonyProject.setOutputLocation(new Path("/" + projectName + "/bin"), monitor);

             // Generation of all the project files : java classes, Manifest.MF, persitence.xml, analysis.xml
             createFiles(projectName,analysisClassName,databaseRequired,monitor);
             
             // Associate project with PDE perspective
             BasicNewProjectResourceWizard.updatePerspective(this.configElem);
             
             return true;
     } catch (Exception exception) {
             StatusManager.getManager().handle(new Status(IStatus.ERROR, projectName, "Problem creating " + projectName + " project. Ignoring.", exception));
             try {
                     getProject(projectName).delete(true, null);
             } catch (Exception e) {
            	 StatusManager.getManager().handle(new Status(IStatus.ERROR, projectName, "Could not delete all the files created before the failure of creation of the " + projectName + " project", exception));
             }
             return false;
     }
		
	}
	
	/**
	 * This method generates the minimum set of files and directories required for building an Harmony analysis:
	 * - 
	 * 
	 * @param projectName Name of the Harmony analysis Project
	 * @param analysisClassName Name of the class that contains the analysis code.
	 * @param tailoredDataTypes List of custom data types to be saved in the database
	 * @param monitor Progress monitor of the thread
	 * 
	 * @throws CoreException
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	private void createFiles(String projectName, String analysisClassName, boolean databaseRequired,IProgressMonitor monitor) throws CoreException, IOException {
		 
		 try { 
		 
		//Building directories
		IFolder metaInfFolder = getProject(projectName).getFolder(META_INF);
		metaInfFolder.create(true, true, monitor);
		
		IFolder osgiInfFolder = getProject(projectName).getFolder(OSGI_INF);
		osgiInfFolder.create(true, true, monitor);
		
		IJavaProject javaProject = JavaCore.create(getProject(projectName));
		IFolder folder = getProject(projectName).getFolder("src");
		IPackageFragmentRoot srcFolder = javaProject.getPackageFragmentRoot(folder);
		IPackageFragment fragment = srcFolder.createPackageFragment(projectName, true, monitor);
		
		// Generate Java Classes
		String analysisClassContent = "package "+projectName+";"			+nl()+nl()+
		"import java.util.Properties;"												+nl()+
		"import fr.labri.harmony.core.analysis.AbstractAnalysis;"					+nl()+
		"import fr.labri.harmony.core.config.model.AnalysisConfiguration;"			+nl()+
		"import fr.labri.harmony.core.dao.Dao;"										+nl()+
		"import fr.labri.harmony.core.model.Source;"								+nl()+nl()+nl()+

		"public class "+analysisClassName+" extends AbstractAnalysis{"				+nl()+nl()+

		"	public "+analysisClassName+"() {"				+nl()+
		"		super();"									+nl()+
		"	}"												+nl()+nl()+

		"	public "+analysisClassName+"(AnalysisConfiguration config, Dao dao, Properties properties) {"	+nl()+
		"		super(config, dao, properties);"															+nl()+
		"	}"																								+nl()+nl()+

		"	@Override"										+nl()+
		"	public void runOn(Source src) {"				+nl()+
		"	// TODO Implement your analysis here" 			+nl()+
		"	}" 												+nl()+nl()+
		"}";

		fragment.createCompilationUnit(analysisClassName+".java",analysisClassContent, true, monitor);
		    
		 
		// Building MANIFEST
		 
		Manifest manifest = new Manifest();
		Attributes manifestAttributes = manifest.getMainAttributes();
		manifestAttributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");							
		if(databaseRequired){
			manifestAttributes.putValue("Meta-Persistence", "META-INF/"+PERSISTENCE_DESCRIPTOR);
		}
		manifestAttributes.putValue("Service-Component", OSGI_INF+"/*.xml");		
		manifestAttributes.putValue(Constants.BUNDLE_MANIFESTVERSION, "2");
		manifestAttributes.putValue(Constants.BUNDLE_NAME, projectName);
		manifestAttributes.putValue(Constants.BUNDLE_SYMBOLICNAME, projectName);
		manifestAttributes.putValue(Constants.BUNDLE_VERSION, "1.0.0.qualifier");
		manifestAttributes.putValue(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT, "JavaSE-1.7");
		manifestAttributes.putValue("Require-Bundle", "fr.labri.harmony.core");
		manifestAttributes.putValue("Import-Package", "javax.persistence;version=\"2.0.4\", org.osgi.framework;version=\"1.7.0\"");
		manifestAttributes.putValue("Export-Package", projectName);

		IPath manifestTargetFile = metaInfFolder.getFile(MANIFEST_MF).getRawLocation();
		FileOutputStream out = new FileOutputStream(manifestTargetFile.toFile());
		try {
			manifest.write(out);
		} finally {
			out.close();
		}
		
		 //Building Build Properties
		
		IFile buildPropFile = getProject(projectName).getFile("build.properties");
		String buildPropFileContent = "source.. = src/ "+nl()+"output.. = bin/"+nl()+
									  "bin.includes = "+META_INF+"/,\\"+nl()+
									  "               .,\\"+nl()+
									  "               "+OSGI_INF+"/";	
		InputStream buildPropFileContentIS = new ByteArrayInputStream(buildPropFileContent.getBytes());
		buildPropFile.create(buildPropFileContentIS,true, monitor);
		
		 //Building analysis.xml
		IFile analysisTargetFile = osgiInfFolder.getFile(ANALYSIS_DESCRIPTOR);
		String analysisFileContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+nl()+
		"	<scr:component xmlns:scr=\"http://www.osgi.org/xmlns/scr/v1.1.0\" immediate=\"false\" name=\""+analysisClassName+"\">"+nl()+
		"	<implementation class=\""+projectName+"."+analysisClassName+"\"/>"+nl()+
		   "	<service>"+nl()+
		   "		<provide interface=\"fr.labri.harmony.core.analysis.Analysis\"/>"+nl()+
		  "	</service>"+nl();
		if(databaseRequired){ analysisFileContent +=  "	<property name=\"persistence-unit\" type=\"String\" value=\""+analysisClassName.toLowerCase()+"\"/>"+nl();}
		analysisFileContent +="</scr:component> ";

		InputStream analysisFileContentIS = new ByteArrayInputStream(analysisFileContent.getBytes());
		analysisTargetFile.create(analysisFileContentIS,true, monitor);

		 //Building persistence.xml
		if(databaseRequired){
			IFile persitenceTargetFile = metaInfFolder.getFile(PERSISTENCE_DESCRIPTOR);
			String persistenceFileContent = "<persistence xmlns=\"http://java.sun.com/xml/ns/persistence\""+nl()+
					"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""+nl()+
					"xsi:schemaLocation=\"http://java.sun.com/xml/ns/persistence persistence_1_0.xsd\""+nl()+
					"version=\"1.0\">"+nl()+
					"	<persistence-unit name=\""+analysisClassName.toLowerCase()+"\" transaction-type=\"RESOURCE_LOCAL\">"+nl()+
					"	 	<provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>"+nl()+
					"	 	<exclude-unlisted-classes>false</exclude-unlisted-classes>"+nl();

			// We copy the standard persistence settings from a file stored in this plugin
			Bundle bundle = Platform.getBundle("fr.labri.harmony.wizard.analysis");
			URL fileURL = bundle.getEntry("config/persistence_unit.prop");
			File file = null;
			try {
			    file = new File(FileLocator.resolve(fileURL).toURI());
			    InputStream inputStream = new FileInputStream(file);
			    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			    String cLine;		 
			    while ((cLine = in.readLine()) != null) {
			    	persistenceFileContent += "	 		"+cLine+nl();
			    }
			    in.close();
			    
			} catch (URISyntaxException e1) {
			    e1.printStackTrace();
			} catch (IOException e1) {
			    e1.printStackTrace();
			}
			
			persistenceFileContent +="	</persistence-unit>"+nl()+
									"</persistence>";
		
			InputStream persistenceFileContentIS = new ByteArrayInputStream(persistenceFileContent.getBytes());
			persitenceTargetFile.create(persistenceFileContentIS,true, monitor);
			
		}
		 
		 
		 } finally {
				getProject(projectName).refreshLocal(IResource.DEPTH_INFINITE, monitor);
			}
	 }
	 
		
		private IProject getProject(String name) {
	        return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		}
		
		private String[] getNatures() {
	        return new String[] { JavaCore.NATURE_ID, "org.eclipse.pde.PluginNature" };
	    }
		
		private String[] getBuilders() {
			 return new String[] { JavaCore.BUILDER_ID, "org.eclipse.pde.ManifestBuilder","org.eclipse.pde.SchemaBuilder" };
		 }
		 
		private List<IClasspathEntry> getClasspathsEntries(String projectName) throws CoreException {
	         List<IClasspathEntry> classpathEntries = new ArrayList<IClasspathEntry>();
	         classpathEntries.add(JavaCore.newContainerEntry(new Path("org.eclipse.jdt.launching.JRE_CONTAINER")));
	         classpathEntries.add(JavaCore.newContainerEntry(new Path("org.eclipse.pde.core.requiredPlugins")));

	         IFolder srcFolder = getProject(projectName).getFolder("src");
	         srcFolder.create(true, true, null);
	         classpathEntries.add(JavaCore.newSourceEntry(srcFolder.getFullPath()));

	         return classpathEntries;
		 }
		 
		
	/**
	 * This method return new line characters adapted for the current OS. This aims to make the generation templates clearer.
	 * 
	 * @return OS specific new line
	 */
	private String nl(){
		return System.getProperty("line.separator");
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		this.configElem = config;
		
	}
}