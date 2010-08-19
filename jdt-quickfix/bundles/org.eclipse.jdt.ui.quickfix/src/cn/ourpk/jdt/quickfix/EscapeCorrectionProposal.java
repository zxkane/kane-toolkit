package cn.ourpk.jdt.quickfix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.text.correction.proposals.CUCorrectionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.TextEdit;


@SuppressWarnings("restriction")
public class EscapeCorrectionProposal extends CUCorrectionProposal {
	
	private static int findInvalidEscape(int end, int start, char ignoreCharacters, ICompilationUnit cu) {
		List<Character> defined =  Arrays.asList(existing);
		try {
			IBuffer buf= cu.getBuffer();
			while (end >= start) {
				if (ignoreCharacters == buf.getChar(start)) {
					if (end >= start + 1)
						if (!defined.contains(buf.getChar(++start)))
							return start;
				}
				start++;
			}
		} catch(JavaModelException e) {
		}
		return start;
	}

	ArrayList<Integer> positions;
	static Character[] existing = new Character[]{'b', 't', 'n', 'f', 'r', '\"', '\\'};
	
	protected EscapeCorrectionProposal(String name, ICompilationUnit cu,
			int relevance, Image image, int offset, int length) {
		super(name, cu, relevance, image);
		
		positions = new ArrayList<Integer>();
		int pos = offset;
		int end = length + offset;
		while((pos = findInvalidEscape(end, pos, '\\', cu)) <= end) {
			positions.add(pos);
		}
	}

	@Override
	protected void addEdits(IDocument document, TextEdit editRoot)
			throws CoreException {
		super.addEdits(document, editRoot);

		for (Integer i : positions) {
			TextEdit edit= new InsertEdit(i.intValue(), "\\");
			editRoot.addChild(edit);
		}
	}
}
