package fr.labri.harmony.wizard.workdir;


import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.statushandlers.StatusManager;

import fr.labri.harmony.core.output.FileUtils;

public class WorkingDirectoryWizard extends Wizard implements INewWizard {

	
	private final String PROJECT_NAME = "HarmonyWorkingDirectory";
	private WorkingDirectoryWizardPage wdPage;
	
	public WorkingDirectoryWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {

		
	}
	

	public void addPages() {
		wdPage = new WorkingDirectoryWizardPage();
		addPage(wdPage);
	}

	@Override
	public boolean performFinish() {
		
		// We switch to PDE perspective to make sure that the link to the launch configuration will be automatically added
		String perspectiveID = "org.eclipse.pde.ui.PDEPerspective";
		try {
			PlatformUI.getWorkbench().showPerspective( perspectiveID, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		} catch (WorkbenchException e1) {
			// Mandatory feature, it doesn't matter if it fails
		}
	
		
		// We create a task to perform the finish operation
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {

					doFinish(monitor);
				} catch (Exception e) {
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
	
	private void doFinish(IProgressMonitor monitor) {

		try {
			
			
			
			// Project creation
			final IProjectDescription projectDescription = ResourcesPlugin.getWorkspace().newProjectDescription(PROJECT_NAME);
			projectDescription.setComment("Project acting as the working directory for the Harmony framework");
			getProject(PROJECT_NAME).create(projectDescription, monitor);
			getProject(PROJECT_NAME).open(monitor);
			getProject(PROJECT_NAME).setDescription(projectDescription, monitor);


			// Copy configuration files to the new project
			IFolder confFolder = getProject(PROJECT_NAME).getFolder("configuration");
			confFolder.create(true, true, monitor);
			IFolder harmonyConfFolder = confFolder.getFolder("fr.labri.harmony");
			harmonyConfFolder.create(true, true, monitor);
			
			FileUtils.copyFile("fr.labri.harmony.wizard.workdir","res/configuration/Harmony.launch",confFolder,monitor);
			
			String[] configFiles = {"res/configuration/fr.labri.harmony/default-global-config.json",
									"res/configuration/fr.labri.harmony/default-source-config.json",
									"res/configuration/fr.labri.harmony/mysql-global-config.json"};
			
			for (int i = 0; i < configFiles.length; i++) {
				FileUtils.copyFile("fr.labri.harmony.wizard.workdir",configFiles[i],harmonyConfFolder,monitor);
			}
			
			getProject(PROJECT_NAME).refreshLocal(3, monitor);
			
			
			// We launch the Harmony launch configuration we've just extracted  in order to add it to the launch menu
			// and to ease the first try of Harmony
			ILaunchConfiguration harmonyLaunchConfig = null;
			ILaunchConfiguration[] tab = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
			 for (int i = 0; i < tab.length && harmonyLaunchConfig==null; i++) {
				System.out.println("- "+tab[i].getFile().getName()+" -");
				 if(tab[i].getFile().getName().equals("Harmony.launch")){
					harmonyLaunchConfig= tab[i];
					
				}
			}
			harmonyLaunchConfig.launch(ILaunchManager.RUN_MODE, null);



		} catch (Exception exception) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, PROJECT_NAME, "Problem creating " + PROJECT_NAME + " project. Ignoring.", exception));
			try {
				getProject(PROJECT_NAME).delete(true, null);
			} catch (Exception e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, PROJECT_NAME, "Could not delete all the files created before the failure of creation of the " + PROJECT_NAME + " project", exception));
			}
		}

	}

	private IProject getProject(String name) {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
	}

}
