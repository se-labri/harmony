package fr.labri.harmony.wizard.analysis;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Wizard Page to create a new Harmony analysis
 *
 */
public class NewAnalysisWizardPage extends WizardPage {
	
	private Text nameText;
	private Text analysisClassNameText;
	private Combo analysisKindCombo;
	private Button[] databaseUseButtons;
	private Composite globalContainer;
	private GridData gridData;
	private Button defaultLocation;
	private StringButtonFieldEditor directoryFieldEditor;

	public NewAnalysisWizardPage() {
	    super("Create a new Harmony analysis");
	    setTitle("Create a new Harmony analysis");
	    setDescription("Complete the required fields to create your new Harmony analysis");
	  }


	@Override
	public void createControl(Composite parent) {
		globalContainer = new Composite(parent, SWT.NULL);
	    GridLayout layout = new GridLayout();
	    globalContainer.setLayout(layout);
	    layout.numColumns = 3;
	    layout.verticalSpacing = 2;
 
	    // Name
	    
	    Label name = new Label(globalContainer, SWT.NULL);
	    name.setText("Project name:");
	    nameText = new Text(globalContainer, SWT.BORDER | SWT.SINGLE);
	    nameText.setText("");
	    nameText.setToolTipText("Your project name should uniquely identify your project and follow the package naming convention"+System.getProperty("line.separator")+
						"It will become the root Java package of your project, e.g. fr.labri.harmony.analysis.reporting");
	    gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		nameText.setLayoutData(gridData);
	    nameText.addModifyListener(new ModifyListener() {	
			@Override
			public void modifyText(ModifyEvent e) {updatePageComplete();}
		});
	    
	    
	    // Management of the project location
	    defaultLocation = new Button(globalContainer, SWT.CHECK);
	    defaultLocation.setText("Use default location");
	    defaultLocation.setSelection(true);
	    gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
	    gridData.horizontalSpan = 3;
	    defaultLocation.setLayoutData(gridData);
	    defaultLocation.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {updateDefaultLocationChoice();}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
	    
	    directoryFieldEditor =  new DirectoryFieldEditor("hum", "Location:", globalContainer);
	    directoryFieldEditor.setEnabled(false, globalContainer);
	    directoryFieldEditor.setStringValue(ResourcesPlugin.getWorkspace().getRoot().getLocation().toPortableString());

	    
	    Label vSpace2 = new Label(globalContainer, SWT.NULL);
	    vSpace2.setText("");
	    gridData = new GridData();
		gridData.horizontalSpan = 3;
		vSpace2.setLayoutData(gridData);
	    
	    // Name of the main class of the analysis
	    
	    Label analysisClassName = new Label(globalContainer, SWT.NULL);
	    analysisClassName.setText("Analysis class name: ");
	    analysisClassNameText = new Text(globalContainer, SWT.BORDER | SWT.SINGLE);
	    analysisClassNameText.setText("");
	    analysisClassNameText.setToolTipText("The name of the Java class that will contains your analysis code");
	    gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		analysisClassNameText.setLayoutData(gridData);
	    analysisClassNameText.addModifyListener(new ModifyListener() {	
			@Override
			public void modifyText(ModifyEvent e) {updatePageComplete();}
		});
	    
	    // Type of analysis (Standard or PostAnalysis)
	    
	    Label vSpace3 = new Label(globalContainer, SWT.NULL);
	    vSpace3.setText("");
	    gridData = new GridData();
		gridData.horizontalSpan = 3;
		vSpace3.setLayoutData(gridData);
		
		Label analysisKindQuestion = new Label(globalContainer, SWT.NULL);
		analysisKindQuestion.setText("Analysis type:"); 
	    
		
		analysisKindCombo = new Combo (globalContainer, SWT.READ_ONLY);
		analysisKindCombo.setItems (new String [] {"Standard", "Post-Processing"});
		analysisKindCombo.select(0);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		analysisKindCombo.setLayoutData(gridData);
		//analysisKindCombo.
		
	    // Database facilities 
	    
	    Label vSpace4 = new Label(globalContainer, SWT.NULL);
	    vSpace4.setText("");
	    gridData = new GridData();
		gridData.horizontalSpan = 3;
		vSpace4.setLayoutData(gridData);
		
	    Label databaseUseQuestion = new Label(globalContainer, SWT.NULL);
	    databaseUseQuestion.setText("Will your analysis require the database facilities provided by the Harmony framework ?"); 
	    gridData = new GridData();
		gridData.horizontalSpan = 3;
		databaseUseQuestion.setLayoutData(gridData);
	    
		Composite radioButtonsContainer = new Composite(globalContainer, SWT.NULL);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		radioButtonsContainer.setLayoutData(gridData);
		radioButtonsContainer.setLayout(new GridLayout(2,false));
		
	    databaseUseButtons = new Button[2];
	    databaseUseButtons[0] = new Button(radioButtonsContainer, SWT.RADIO);
	    databaseUseButtons[0].setText("Yes");    
	    databaseUseButtons[1] = new Button(radioButtonsContainer, SWT.RADIO);
	    databaseUseButtons[1].setSelection(true);
	    databaseUseButtons[1].setText("No");
	    databaseUseButtons[0].addSelectionListener(new SelectionListener() {	
			@Override
			public void widgetSelected(SelectionEvent e) { updatePageComplete(); }
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
	    
	    
	    
	    setControl(globalContainer);
	    setPageComplete(false);

	}
	
	protected void updateDefaultLocationChoice() {

        if (defaultLocation.getSelection()){
        	  directoryFieldEditor.setEnabled(false, globalContainer);
        }else{
        	  directoryFieldEditor.setEnabled(true, globalContainer);
        }
		
	}


	/**
	 * This method checks that the data entered by the user is correct and also update the form according to the selection
	 */
	@SuppressWarnings("restriction")
	private void updatePageComplete(){
		StatusInfo status= new StatusInfo();	
		
		// Check of the project name and class name
		IStatus projNameStatus = JavaConventions.validatePackageName(getProjectName(), JavaCore.VERSION_1_3, JavaCore.VERSION_1_3);
		IStatus classNameStatus = JavaConventions.validateCompilationUnitName(getAnalysisClassName()+".java",JavaCore.VERSION_1_3, JavaCore.VERSION_1_3);
		
		//Feedback to the user
		if (projNameStatus.getSeverity() == IStatus.ERROR) {
			status.setError("Invalid project name. The name should follow the Java package naming convention");	
		} else if(classNameStatus.getSeverity() == IStatus.ERROR) {
			status.setError("Invalid class name.");			
		}else if (projNameStatus.getSeverity() == IStatus.WARNING) {
			status.setWarning("Discouraged project name. The name should follow the Java package naming convention");
		}
		StatusUtil.applyToStatusLine(this, status);
		
		
		// If both project and class name are correct we allow the creation of the project
		if(projNameStatus.isOK()&classNameStatus.isOK()){
			setPageComplete(true);
		}else{
			setPageComplete(false);
		}
	}
	
	public String getProjectName() {
		return nameText.getText();
	}
	
	public IPath getProjectLocation(){
		if (!defaultLocation.getSelection()){
			return new Path(directoryFieldEditor.getStringValue());
		}	
		return null;
	}

	public String getAnalysisClassName() {
		return analysisClassNameText.getText();
	}
	
	public String getAnalysisType() {
		return analysisKindCombo.getText();
	}

	public boolean isStorageFacilitiesRequired(){
		return databaseUseButtons[0].getSelection();
	}
	
	public Set<String> getTailoredDataTypes(){
		return new HashSet<String>();
	}
}
