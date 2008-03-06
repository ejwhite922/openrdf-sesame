/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.sparql.ast.ASTBasicGraphPattern;
import org.openrdf.query.parser.sparql.ast.ASTBlankNode;
import org.openrdf.query.parser.sparql.ast.ASTBlankNodePropertyList;
import org.openrdf.query.parser.sparql.ast.ASTCollection;
import org.openrdf.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.query.parser.sparql.ast.ASTVar;
import org.openrdf.query.parser.sparql.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.query.parser.sparql.ast.VisitorException;

/**
 * Processes blank nodes in the query body, replacing them with variables while
 * retaining scope.
 * 
 * @author Arjohn Kampman
 */
class BlankNodeVarProcessor extends ASTVisitorBase {

	public static void process(ASTQueryContainer qc)
		throws MalformedQueryException
	{
		try {
			qc.jjtAccept(new BlankNodeToVarConverter(), null);
		}
		catch (VisitorException e) {
			throw new MalformedQueryException(e);
		}
	}

	/*-------------------------------------*
	 * Inner class BlankNodeToVarConverter *
	 *-------------------------------------*/

	private static class BlankNodeToVarConverter extends ASTVisitorBase {

		private int anonVarNo = 1;

		private Map<String, String> conversionMap = new HashMap<String, String>();

		private String createAnonVarName() {
			return "-anon-" + anonVarNo++;
		}

		@Override
		public Object visit(ASTBasicGraphPattern node, Object data)
			throws VisitorException
		{
			// Blank nodes are scope to Basic Graph Patterns
			conversionMap.clear();

			return super.visit(node, data);
		}

		@Override
		public Object visit(ASTBlankNode node, Object data)
			throws VisitorException
		{
			String varName = null;
			String bnodeID = node.getID();

			if (bnodeID != null) {
				varName = conversionMap.get(bnodeID);
			}

			if (varName == null) {
				varName = createAnonVarName();

				if (bnodeID != null) {
					conversionMap.put(bnodeID, varName);
				}
			}

			ASTVar varNode = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
			varNode.setName(varName);
			varNode.setAnonymous(true);

			node.jjtReplaceWith(varNode);

			return super.visit(node, data);
		}

		@Override
		public Object visit(ASTBlankNodePropertyList node, Object data)
			throws VisitorException
		{
			node.setVarName(createAnonVarName());
			return super.visit(node, data);
		}

		@Override
		public Object visit(ASTCollection node, Object data)
			throws VisitorException
		{
			node.setVarName(createAnonVarName());
			return super.visit(node, data);
		}
	}
}
