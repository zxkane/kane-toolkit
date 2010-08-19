package cn.ourpk.jdt.quickfix;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;

public class QuickFixProcessor implements IQuickFixProcessor {

	public boolean hasCorrections(ICompilationUnit unit, int problemId) {
		if (IProblem.InvalidEscape == problemId)
			return true;
		return false;
	}

	public IJavaCompletionProposal[] getCorrections(IInvocationContext context,
			IProblemLocation[] locations) throws CoreException {
		if (locations == null || locations.length == 0) {
			return null;
		}
		
		HashSet<Integer> handledProblems= new HashSet<Integer>(locations.length);
		ArrayList<IJavaCompletionProposal> resultingCollections= new ArrayList<IJavaCompletionProposal>();
		for (int i= 0; i < locations.length; i++) {
			IProblemLocation curr= locations[i];
			Integer id= new Integer(curr.getProblemId());
			if (handledProblems.add(id)) {
				process(context, curr, resultingCollections);
			}
		}
		return resultingCollections.toArray(new IJavaCompletionProposal[resultingCollections.size()]);
	}
	
	private void process(IInvocationContext context, IProblemLocation problem,
			ArrayList<IJavaCompletionProposal> proposals) {
		int id= problem.getProblemId();
		if (id == 0) { // no proposals for none-problem locations
			return;
		}
		switch (id) {
		case IProblem.InvalidEscape:
			String quoteLabel= "Insert missing escape";			
			proposals.add(new EscapeCorrectionProposal(quoteLabel, context.getCompilationUnit(), 0, null, 
					problem.getOffset(), problem.getLength()));
			break;
		}
	}

}
