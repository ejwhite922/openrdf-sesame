/* Generated By:JJTree: Do not edit this line. ASTBlankNode.java */

package org.openrdf.query.parser.sparql.ast;

public class ASTBlankNode extends SimpleNode {

	private String id;

	public ASTBlankNode(int id) {
		super(id);
	}

	public ASTBlankNode(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public String getID() {
		return id;
	}

	public void setID(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return super.toString() + " (" + id + ")";
	}
}
