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
	
	// maps class name to its ClassDecl node
	Hashtable<String, ClassDecl> globalSymTab;
	ClassDecl currentClass;

	Hashtable<String, VarDecl> localSymTab;
	Stack<BreakTarget> breakTargetStack;
	Stack<While> loopStack;
	static VarDecl uninitVarDecl;
	
        ErrorMsg errorMsg;

	// dummy variable declaration indicating "uninitialized variable"
	// private static VarDecl uninitVarDecl = new InstVarDecl(-1, null, "$$$$");
	
	public Sem3Visitor(Hashtable globalSymTb, ErrorMsg e) {
	    errorMsg = e;
		initInstanceVars(globalSymTb);
	}

	private void initInstanceVars(Hashtable<String,ClassDecl> globalTab) {
		breakTargetStack = new Stack<BreakTarget>();
		loopStack = new Stack<While>();
		globalSymTab = globalTab;
		localSymTab = new Hashtable<String,VarDecl>();


		currentClass = null;
		uninitVarDecl = null;
	}

	@Override
	public Object visitClassDecl(ClassDecl myClassDecl) {
		this.currentClass = myClassDecl;

		return super.visitClassDecl(myClassDecl);
	}

	@Override
	public Object visitWhile(While myWhile) {
		loopStack.push(myWhile);
		super.visitWhile(myWhile);
		loopStack.pop();

		return null;
	}
	

	@Override
	public Object visitBreak(Break myBreak) {
		// set Break's link to be element at top of the break-target stack
				// if empty => error
		if (loopStack.size() <= 0) {
			errorMsg.error(myBreak.pos, "Error: empty stack to push Break statement.");
		} 
		// else {

		// }
		// myBreak.breakLink

		return null;

	}

	// create a new Hashtable object and store it in localSymTab, traverse the subnodes
	@Override
	public Object visitMethodDecl(MethodDecl myMethod) {
		this.localSymTab = new Hashtable<String, VarDecl>();
		return super.visitMethodDecl(myMethod);
	}

	@Override
	public Object visitLocalVarDecl(LocalVarDecl myLocalVar) {
		// put entry in local symbol table, binding to uninitVarDecl
		this.uninitVarDecl = myLocalVar;

		// Traverse subnodes
		super.visitLocalVarDecl(myLocalVar);

		// replace entry in local symbol table with actual variable
		if (!this.localSymTab.containsKey(myLocalVar.name)) {
			this.localSymTab.put(myLocalVar.name, myLocalVar);
		}
		else {
			errorMsg.error(myLocalVar.pos, "Error: duplicate variable name: " + myLocalVar.name);
		}

		// Clear current local var decl
		this.uninitVarDecl = null;

		return null;
		
	}

	@Override
	public Object visitFormalDecl(FormalDecl myFormalDecl) {
		this.uninitVarDecl = myFormalDecl;

		super.visitFormalDecl(myFormalDecl);

		// Add to symbol table
		if (!this.localSymTab.containsKey(myFormalDecl.name)) {
			this.localSymTab.put(myFormalDecl.name, myFormalDecl);
		}
		else {
			errorMsg.error(myFormalDecl.pos, "Error: duplicate variable name: " + myFormalDecl.name);
		}

		return null;
	}

	@Override
	public Object visitIdentifierExp(IdentifierExp myID) {
		// local decl getting initialized

		return null;

	}

	@Override
	public Object visitIdentifierType(IdentifierType myIDType) {
		if (globalSymTab.containsKey(myIDType.name)) {
			myIDType.link = globalSymTab.get(myIDType.name);
		}

		return null;
	}

	@Override
	public Object visitInstVarDecl(InstVarDecl myInstVar) {
		if (myInstVar.name.equals("length")) {
			errorMsg.error(myInstVar.pos, "Error: variable cannot be named \" length \"");
		}

		return super.visitInstVarDecl(myInstVar);
	}

	@Override
	public Object visitProgram(Program myProgram) {
		super.visitProgram(myProgram);

		return null;
	}

	@Override
	public Object visitBlock(Block myBlock) {
		// traverse subnodes
		super.visitBlock(myBlock);

		// iterate through Block's statement list and remove all declarations found from local symbol table
		for (Statement stmt: myBlock.stmts) {


		}		

		return null;
	}


}
