package visitor;

import syntaxtree.*;
import java.util.*;
import errorMsg.*;
// The purpose of the Sem1Visitor class is to:
// - enter each class declaration into the global symbol table)
//   - duplicate class names are detected
// - enter each method declaration into the method symbol table
//   for its class
//   - duplicate method names for a class are detected
// - enter each instance variable declaration into the respective
//   instance-variable symbol table for its class
//   - duplicate instance variable names for a class are detected
// - all of the above are also done for the predefined classes
//   (Object, String and Lib)
public class Sem1Visitor extends ASTvisitor {
		
	Hashtable<String,ClassDecl> globalSymTab;
	ClassDecl currentClass;
	ErrorMsg errorMsg;
	
	public Sem1Visitor(ErrorMsg e) {
		errorMsg = e;
		initInstanceVars();
		initGlobalSymTab();
	}
	
	public Hashtable<String,ClassDecl> getGlobalSymTab() {
		return globalSymTab;
	}

	protected void initGlobalSymTab() {
		// PHASE I
		ClassDecl classObjectDecl = createClass("Object", "");
		ClassDecl classStringDecl = createClass("String", "Object");
		ClassDecl classLibDecl = createClass("Lib", "Object");
		ClassDecl classRunMainDecl = createClass("RunMain", "Object");
		ClassDecl classDataArrayDecl = createClass("_DataArray", "Object");
		ClassDecl classObjectArrayDecl = createClass("_ObjectArray", "Object");
		
		addDummyMethod(classObjectDecl, "hashCode", "int", new String[]{});
		addDummyMethod(classObjectDecl, "equals", "boolean", new String[]{"Object"});
		addDummyMethod(classObjectDecl, "toString", "String", new String[]{});

	    addDummyMethod(classLibDecl, "readLine", "String", new String[]{});
	    addDummyMethod(classLibDecl, "readInt", "int", new String[]{});
	    addDummyMethod(classLibDecl, "readChar", "int", new String[]{});
		addDummyMethod(classLibDecl, "printStr", "void", new String[]{"String"});
		addDummyMethod(classLibDecl, "printBool", "void", new String[]{"boolean"});
		addDummyMethod(classLibDecl, "printInt", "void", new String[]{"int"});
		addDummyMethod(classLibDecl, "intToString", "String",
				new String[]{"int"});
		addDummyMethod(classLibDecl, "intToChar", "String",
				new String[]{"int"});

		addDummyMethod(classStringDecl, "hashCode", "int", new String[]{});
		addDummyMethod(classStringDecl, "equals", "boolean", new String[]{"Object"});
		addDummyMethod(classStringDecl, "toString", "String", new String[]{});
		addDummyMethod(classStringDecl, "concat", "String",
				new String[]{"String"});
		addDummyMethod(classStringDecl, "substring", "String",
				new String[]{"int","int"});
		addDummyMethod(classStringDecl, "length", "int", new String[]{});
		addDummyMethod(classStringDecl, "charAt", "int",
				new String[]{"int"});
		addDummyMethod(classStringDecl, "compareTo", "int",
				new String[]{"String"});

		this.visitClassDecl(classObjectDecl);
		this.visitClassDecl(classLibDecl);
		this.visitClassDecl(classStringDecl);
		this.visitClassDecl(classRunMainDecl);
		this.visitClassDecl(classDataArrayDecl);
		this.visitClassDecl(classObjectArrayDecl);

		// PHASE II
		Sem2Visitor s2 = new Sem2Visitor(globalSymTab, errorMsg);
		s2.visit(classObjectDecl);
		s2.visit(classLibDecl);
		s2.visit(classStringDecl);
		s2.visit(classRunMainDecl);
		s2.visitClassDecl(classDataArrayDecl);
		s2.visitClassDecl(classObjectArrayDecl);

		// PHASE III
		Sem3Visitor s3 = new Sem3Visitor(globalSymTab, errorMsg);
		s3.visit(classObjectDecl);
		s3.visit(classLibDecl);
		s3.visit(classStringDecl);
		s3.visit(classRunMainDecl);
		s3.visitClassDecl(classDataArrayDecl);
		s3.visitClassDecl(classObjectArrayDecl);
	}

	// Enter each class declaration into the global symbol table)
	// Duplicate class names are detected
	protected static ClassDecl createClass(String name, String superName) {
		ErrorMsg errorMsg = new ErrorMsg("Sem1Visitor");
		return new ClassDecl(-1, name, superName, new DeclList());
	}
	////////////////////
	// VISIT METHODS //
	///////////////////

	// Add class declarations to global symbol table, by recursively traversing AST
	@Override
	public Object visitClassDecl(ClassDecl myClass) {
		if (!globalSymTab.containsKey(myClass.name)) {
			globalSymTab.put(myClass.name, myClass);
		}
		else {
			errorMsg.error(myClass.pos, "Error: duplicate class declaration: " + myClass.name);
		}

		// Set current class
		currentClass = myClass;
		

		return super.visitClassDecl(myClass);
	}

	@Override
	public Object visitInstVarDecl(InstVarDecl myVar) {
		// Add instance var to class
		if (!currentClass.instVarTable.containsKey(myVar.name)) {
			currentClass.instVarTable.put(myVar.name, myVar);
		}
		else {
			errorMsg.error(myVar.pos, "Error: duplicate instance variable declaration: " + myVar.name);
		}
		return null;
	}

	@Override
	public Object visitMethodDecl(MethodDecl myMethod) {
		// Add method to class
		if (!currentClass.methodTable.containsKey(myMethod.name)) {
			currentClass.methodTable.put(myMethod.name, myMethod);
		}
		else {
			errorMsg.error(myMethod.pos, "Error: duplicate method declaration: " + myMethod.name);
			return null;
		}

		return null;
	}



	// Enter each method declaration into the method symbol table for its class
	// Duplicate method names for a class are detected
	protected static void addDummyMethod(ClassDecl dec, String methName,
				String rtnTypeName, String[] parmTypeNames) {
		VarDeclList parmDecls = new VarDeclList();
		for (int i = 0; i < parmTypeNames.length; i++) {
			Type t = convertToType(parmTypeNames[i]);
			String parmName = "parm"+i;
			VarDecl vd = new FormalDecl(-1, t, parmName);
			parmDecls.addElement(vd);
		}
		Type t = convertToType(rtnTypeName);
		StatementList sl = new StatementList(); // dummied up
		MethodDecl md;
		if (t == null) { // void return-type
			md = new MethodDeclVoid(-1, methName, parmDecls, sl);
		}
		else { // non-void return type
			Exp rtnExpr = new Null(-1);
			md = new MethodDeclNonVoid(-1, t, methName, parmDecls, sl, rtnExpr);
		}
		dec.decls.addElement(md);
	}
	
	private static Type convertToType(String s) {
		if (s.equals("void")) {
			return new VoidType(-1);
		}
		else if (s.equals("boolean")) {
			return new BooleanType(-1);
		}
		else if (s.equals("int")) {
			return new IntegerType(-1);
		}
		else {
			return new IdentifierType(-1, s);
		}
	}

	// Enter each instance variable declaration into the respective instance-variable symbol table for its class
	// Duplicate instance variable names for a class are detected
	private void initInstanceVars() {
		globalSymTab = new Hashtable<String,ClassDecl>();
		currentClass = null;
	}
}
