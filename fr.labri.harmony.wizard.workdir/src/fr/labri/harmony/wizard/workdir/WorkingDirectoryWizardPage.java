package fr.labri.harmony.wizard.workdir;



import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Wizard Page to create a new Harmony analysis
 *
 */
public class WorkingDirectoryWizardPage extends WizardPage {

	private Composite globalContainer;

	public WorkingDirectoryWizardPage() {
	    super("New Harmony working directory");
	    setTitle("New Harmony working directory");
	    setDescription("");
	  }

	@Override
	public void createControl(Composite parent) {
		globalContainer = new Composite(parent, SWT.NULL);
	    GridLayout layout = new GridLayout();
	    globalContainer.setLayout(layout);
	    
	    Label pres = new Label(globalContainer, SWT.NULL);
	    pres.setText("This wizard creates a new Harmony working directory and provides all the necessary configuration files");
	    
	    Label vSpace2 = new Label(globalContainer, SWT.NULL);
	    vSpace2.setText("");
	    GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
		vSpace2.setLayoutData(gridData);
	    
	    Label instructions = new Label(globalContainer, SWT.NULL);
	    instructions.setText("Click on Finish. Then type harmony in the OSGi console");    
	    
	    setControl(globalContainer);
	    setPageComplete(true);
	}
	
}
