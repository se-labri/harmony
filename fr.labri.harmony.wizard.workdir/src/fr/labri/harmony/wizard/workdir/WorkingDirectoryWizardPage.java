package fr.labri.harmony.wizard.workdir;



import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Wizard Page to create a new Harmony analysis
 *
 */
public class WorkingDirectoryWizardPage extends WizardPage {

	private Composite globalContainer;

	public WorkingDirectoryWizardPage() {
	    super("New Harmony working directory");
	    setTitle("New Harmony working directory");
	    setDescription("This wizard creates a new Harmony working directory and provides and the necessary configuration files");
	  }

	@Override
	public void createControl(Composite parent) {
		globalContainer = new Composite(parent, SWT.NULL);
	    GridLayout layout = new GridLayout();
	    globalContainer.setLayout(layout);
	    
	    setControl(globalContainer);
	    setPageComplete(true);
	}
	
}
