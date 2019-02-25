package visitor;

import syntaxtree.*;
import java.util.*;
import errorMsg.*;

// the purpose of this class is to
// - link each ClassDecl to the ClassDecl for its superclass (via
//   its 'superLink'
// - link each ClassDecl to each of its subclasses (via the
//   'subclasses' instance variable
// - ensure that there are no cycles in the inheritance hierarchy
// - ensure that no class has 'String' or 'RunMain' as a superclass
public class Sem2Visitor extends ASTvisitor {
	
	Hashtable<String,ClassDecl> globalSymTab;
	ErrorMsg errorMsg;
	
	public Sem2Visitor(Hashtable<String,ClassDecl> globalSymTb,
			   ErrorMsg e) {
		errorMsg = e;
		initInstanceVars(globalSymTb);
	}

	private void initInstanceVars(Hashtable<String,ClassDecl> globalTab) {
		globalSymTab = globalTab;
	}

	@Override
	public Object visitProgram(Program myProgram) {
		// Visit all of our subnodes
		super.visitProgram(myProgram);

		// Perform class checks
		for (ClassDecl classInstance: myProgram.classDecls) {
			// Check not subclass of String and RunMain
			if (classInstance.superName.equals("String")) {
				errorMsg.error(classInstance.pos, "Error: cannot extend " + classInstance.name + " for String superclass");
			}
			else if (classInstance.superName.equals("RunMain")) {
				errorMsg.error(classInstance.pos, "Error: cannot extend " + classInstance.name + " for RunMain superclass");
			}

			// Check for cycles: how???
			if (containsClassCycle(classInstance)) {
				errorMsg.info("Error: cycle detected for class name: " + classInstance.name);
			}
		}

		return null;
	}

	// Link child and super classes
	@Override
	public Object visitClassDecl(ClassDecl classInstance) {
		if (classInstance.superName != null) {
			if (globalSymTab.containsKey(classInstance.superName)) {
				classInstance.superLink = globalSymTab.get(classInstance.superName);
				 classInstance.superLink.subclasses.addElement(classInstance);
			}
			else if (classInstance.superName.equals("")) {
				// errorMsg.warning(classInstance.pos, "Empty supername");

			}
			else {
				errorMsg.error(classInstance.pos, "Error: undefined super class name: " + classInstance.superName);
			}
		}
		else {
			errorMsg.error(classInstance.pos, "Error: null super class.");
		}

		return null;
	}

	public boolean containsClassCycle(ClassDecl current) {
		String superClassName = current.superName;

		if (current == null) {
			// traversed to end of list so no cycles
			return false;
		}

		if (superClassName.equals(current.name)) {
			errorMsg.error(current.pos, "Superclass name matches name of current class: " + current.name);
			return true;
		}

		if (globalSymTab.get(superClassName).superName.equals(current.name)) {
			errorMsg.error(current.pos, "Superclass name " + globalSymTab.get(superClassName).name + " is extending current class : " + current.name);
			return true;
		}

		return false;
	}
	
}

	
