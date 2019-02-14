package prettyPrinter;

import syntaxtree.AstNode;
import syntaxtree.AstList;
import java.io.PrintStream;

public class PrettyPrinter {
	private boolean printLinks;
	private int indent = 0;

	public PrettyPrinter(boolean printLinks) {
		this.printLinks = printLinks;
	}
	
	public boolean printLinks() {
		return printLinks;
	}
	
	public void indent() {
		indent++;
	}
	public void unindent() {
		indent--;
	}
	public String is() {
		String rtnVal = "";
		for (int i = 0; i < indent; i++) {
			rtnVal += "  ";
		}
		return rtnVal;
	}
	public void tab(PrintStream ps) {
		ps.print(is());
	}
	public void print(AstNode node, PrintStream ps) {
		if (node == null) {
			ps.print("??null??");
		}
		else {
			node.prettyPrint(this, ps);
		}
	}

	public void print(AstList list, PrintStream ps) {
		if (list == null) {
			ps.print("??null??");
		}
		else {
			list.prettyPrint(this, ps);
		}
	}
}
