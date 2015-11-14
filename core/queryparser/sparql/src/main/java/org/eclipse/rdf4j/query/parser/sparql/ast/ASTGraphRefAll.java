/*******************************************************************************
 * Copyright (c) 2015 Eclipse RDF4J contributors, Aduna, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
/* Generated By:JJTree: Do not edit this line. ASTGraphRefAll.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.eclipse.rdf4j.query.parser.sparql.ast;

public
class ASTGraphRefAll extends SimpleNode {
  private boolean _default;
private boolean named;

public ASTGraphRefAll(int id) {
    super(id);
  }

  public ASTGraphRefAll(SyntaxTreeBuilder p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data) throws VisitorException {
    return visitor.visit(this, data);
  }
  
	/**
	 * @param _default The default to set.
	 */
	public void setDefault(boolean _default) {
		this._default = _default;
	}

	/**
	 * @return Returns the default.
	 */
	public boolean isDefault() {
		return _default;
	}

	/**
	 * @param named The named to set.
	 */
	public void setNamed(boolean named) {
		this.named = named;
	}

	/**
	 * @return Returns the named.
	 */
	public boolean isNamed() {
		return named;
	}
}
/* JavaCC - OriginalChecksum=76b459b84d8a145884f6d9f597fa6072 (do not edit this line) */
