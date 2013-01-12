package de.unikl.bcverifier.isl.checking.types;

import org.eclipse.jdt.core.dom.IBinding;

import de.unikl.bcverifier.isl.ast.ASTNode;
import de.unikl.bcverifier.isl.ast.Version;

public interface ExprTypeHasMembers {

	IBinding findMember(String name);
	Version getVersion();
	ExprType typeOfMemberAccess(ASTNode<?> location, String name);
}
