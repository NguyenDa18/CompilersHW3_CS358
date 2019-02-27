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
	VarDecl uninitVarDecl; // current local variable processed

	ErrorMsg errorMsg;

	// dummy variable declaration indicating "uninitialized variable"
	// private static VarDecl uninitVarDecl = new InstVarDecl(-1, null, "$$$$");

	public Sem3Visitor(Hashtable globalSymTb, ErrorMsg e) {
	    errorMsg = e;
		initInstanceVars(globalSymTb);
	}

	private void initInstanceVars(Hashtable<String,ClassDecl> globalTab) {
		breakTargetStack = new Stack<BreakTarget>(); // Keep track of enclosing While / Switch statements
		loopStack = new Stack<While>();
		globalSymTab = globalTab; // Map class name to corresponding ClassDecl node
		localSymTab = new Hashtable<String,VarDecl>(); // Symbol table to track local vars


		currentClass = null;
		uninitVarDecl = null;
	}

	@Override
	public Object visitClassDecl(ClassDecl myClassDecl) {
		this.currentClass = myClassDecl;
		return super.visitClassDecl(myClassDecl);
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
		// put entry in local symbol table, binding to uninitVarDecl
		this.uninitVarDecl = myFormalDecl;

		super.visitFormalDecl(myFormalDecl);

		// Add to symbol table
		if (!this.localSymTab.containsKey(myFormalDecl.name)) {
			this.localSymTab.put(myFormalDecl.name, myFormalDecl);
		}
		else {
			errorMsg.error(myFormalDecl.pos, "Error: duplicate variable name: " + myFormalDecl.name);
		}

		this.uninitVarDecl = null;

		return null;
	}


	@Override
	public Object visitInstVarDecl(InstVarDecl myInstVar) {
		// Check to make sure name of var is NOT length
		if (myInstVar.name.equals("length")) {
			errorMsg.error(myInstVar.pos, "Error: instance variable cannot be named \" length \"");
		}

		return super.visitInstVarDecl(myInstVar);
	}

	@Override
	public Object visitBlock(Block myBlock) {
		// traverse subnodes
		super.visitBlock(myBlock);

		// remove each var declaration in local symbol table that corresponds to declaration in this block's statement list
		for (Statement stmt: myBlock.stmts) {
			if (stmt instanceof LocalDeclStatement) {
				localSymTab.remove(((LocalDeclStatement) stmt).localVarDecl.name);
			}

		}

		return null;
	}

	// @Override
	// public Object visitBreakTarget(BreakTarget myBrkTrgt) {
	// 	errorMsg.error(myBrkTrgt.pos, "My break target: " + myBrkTrgt.toString());

	// 	return null;
	// }


	@Override
	public Object visitIdentifierExp(IdentifierExp myID) {
		VarDecl link = null;

		// local decl equal to uninitVarDecl, give error
		if (uninitVarDecl != null) {
			// identifier name is same as current local vardecl
			if (myID.name.equals(uninitVarDecl.name)) {
				errorMsg.error(myID.pos, "Error: identifier " + myID.name + " is equal to uninitialized variable: " + uninitVarDecl.name);
			}
		}

		// otherwise, set IDExp's link so it refers to declaration
		if (localSymTab.containsKey(myID.name)) {
			link = localSymTab.get(myID.name);
		}

		// identifier not found:
		else {
			ClassDecl c = currentClass;

			while (c.superLink != null) {
				if (c.instVarTable.containsKey(myID.name)) {
					link = c.instVarTable.get(myID.name);
					break;
				}
				c = c.superLink;
			}
		}

		// Set the link
		if (link != null) {
			myID.link = link;
		}
		else {
			errorMsg.error(myID.pos, "Undefined variable name: " + myID.name);
		}

		return null;

	}

	@Override
	public Object visitIdentifierType(IdentifierType myIDType) {
		if (globalSymTab.containsKey(myIDType.name)) {
			myIDType.link = globalSymTab.get(myIDType.name);
		}
		else {
			errorMsg.error(myIDType.pos, "Undefined class name: " + myIDType.name);
		}

		return null;
	}

	@Override
	public Object visitWhile(While myWhile) {
		breakTargetStack.push(myWhile);
		super.visitWhile(myWhile);
		breakTargetStack.pop();

		return null;
	}

	@Override
	public Object visitBreak(Break myBreak) {
		// if empty breakTargetStack, emit error
		if (breakTargetStack.isEmpty()) {
			errorMsg.error(myBreak.pos, "Break statement outside of loop/switch");
		}
		else {
			myBreak.breakLink = breakTargetStack.firstElement();
		}

		return null;

	}
}



