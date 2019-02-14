package visitor;

import syntaxtree.*;
import java.util.*;
import errorMsg.*;
// The purpose of this class is to:
// - link each variable reference to its corresponding VarDecl
//   (via its 'link' instance variable)
//   - undefined variable names are reported
// - link each type reference to its corresponding ClassDecl
//   - undefined type names are reported
// - link each Break expression to its enclosing BreakTarget
//   (While or Switch) statement
//   - a break that is not inside any while or switch is reported
// - report conflicting local variable names (including formal
//   parameter names)
// - ensure that no instance variable has the name 'length'
public class Sem3Visitor extends ASTvisitor {
	
	Hashtable<String, ClassDecl> globalSymTab;
	ClassDecl currentClass;
	Hashtable<String, VarDecl> localSymTab;
	Stack<BreakTarget> breakTargetStack;
	
        ErrorMsg errorMsg;

	// dummy variable declaration indicating "uninitialized variable"
	private static VarDecl uninitVarDecl = new InstVarDecl(-1, null, "$$$$");
	
	public Sem3Visitor(Hashtable globalSymTb, ErrorMsg e) {
	    errorMsg = e;
		initInstanceVars(globalSymTb);
	}

	private void initInstanceVars(Hashtable<String,ClassDecl> globalTab) {
		breakTargetStack = new Stack<BreakTarget>();
		globalSymTab = globalTab;
		localSymTab = new Hashtable<String,VarDecl>();
		currentClass = null;
	}

}
