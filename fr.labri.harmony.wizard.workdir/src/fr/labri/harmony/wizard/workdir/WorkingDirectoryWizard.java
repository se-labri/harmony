package fr.labri.harmony.wizard.workdir;


import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.statushandlers.StatusManager;

public class WorkingDirectoryWizard extends Wizard implements INewWizard {

	
	private final String PROJECT_NAME = "HarmonyWorkingDirectory";
	private WorkingDirectoryWizardPage wdPage;
	
	public WorkingDirectoryWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {}
	

	public void addPages() {
		wdPage = new WorkingDirectoryWizardPage();
		addPage(wdPage);
	}

	@Override
	public boolean performFinish() {
		
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
			
			WizardFileUtils.copyFile("fr.labri.harmony.wizard.workdir","res/configuration/Harmony.launch",confFolder,monitor);
			
			String[] configFiles = {"res/configuration/fr.labri.harmony/default-global-config.json",
									"res/configuration/fr.labri.harmony/default-source-config.json",
									"res/configuration/fr.labri.harmony/mysql-global-config.json"};
			
			for (int i = 0; i < configFiles.length; i++) {
				WizardFileUtils.copyFile("fr.labri.harmony.wizard.workdir",configFiles[i],harmonyConfFolder,monitor);
			}
			
			getProject(PROJECT_NAME).refreshLocal(3, monitor);


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
