/* Generated By:JJTree: Do not edit this line. ASTProjectionElem.java Version 4.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.openrdf.query.parser.sparql.ast;

public class ASTProjectionElem extends SimpleNode {

	public ASTProjectionElem(int id) {
		super(id);
	}

	public ASTProjectionElem(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. **/
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTExpression getExpression() {
		return (ASTExpression)children.get(0);
	}

	public boolean hasAlias() {
		return getAlias() != null;
	}

	public String getAlias() {
		if (children.size() >= 2) {
			Node aliasNode = children.get(1);

			if (aliasNode instanceof ASTString) {
				return ((ASTString)aliasNode).getValue();
			}
			else if (aliasNode instanceof ASTVar) {
				return ((ASTVar)aliasNode).getName();
			}
		}

		return null;
	}
}
/* JavaCC - OriginalChecksum=ed67c3c7a74ebd8df6304268b81f702d (do not edit this line) */