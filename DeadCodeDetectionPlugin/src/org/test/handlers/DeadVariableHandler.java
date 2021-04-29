package org.test.handlers;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import DeadVariableParameter.DeadVariable.DeadVariableDetector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;

public class DeadVariableHandler extends AbstractHandler {
	public static IPath InputPath;
	public static String OutputPath;
	
    public Object execute(ExecutionEvent event) throws ExecutionException {
    	IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null)
        {
            IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
            Object firstElement = selection.getFirstElement();
            if (firstElement instanceof IAdaptable)
            {
                IProject project = (IProject)((IAdaptable)firstElement).getAdapter(IProject.class);
                this.InputPath = project.getLocation();
            }
        }
        
        createOutputDir();
        
        final Job job = new Job("Detect Dead Variable...") {
			
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				try {
					System.out.println("Detect Dead Variable...");
					detectDeadVariable();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		};
		job.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				if(event.getResult().isOK()) {
					System.out.println("Detecting Completed.");
					System.out.println("Report Created at " + OutputPath);
				}else {
					System.out.println("Error : Cannot Detect Dead Variable...");
				}
			}
		});
		job.setSystem(true);
		job.schedule();

        return null;
    }
    
    private void createOutputDir() {
    	this.OutputPath = this.InputPath.toString() + "/DeadCodeDetectionReport";
    	File file = new File(this.OutputPath);
    	
    	if(!file.exists()) {
    		file.mkdir();
    	}
    }
    
    private void detectDeadVariable() throws IOException {
    	DeadVariableDetector deadvariabledetector = new DeadVariableDetector();
    	deadvariabledetector.setInputSource(this.InputPath.toString());
    	deadvariabledetector.run();
    	deadvariabledetector.createReport(this.OutputPath);
    }
}