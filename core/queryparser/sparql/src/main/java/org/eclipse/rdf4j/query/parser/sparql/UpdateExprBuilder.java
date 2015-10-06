/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.query.parser.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.algebra.Add;
import org.eclipse.rdf4j.query.algebra.BNodeGenerator;
import org.eclipse.rdf4j.query.algebra.Clear;
import org.eclipse.rdf4j.query.algebra.Copy;
import org.eclipse.rdf4j.query.algebra.Create;
import org.eclipse.rdf4j.query.algebra.DeleteData;
import org.eclipse.rdf4j.query.algebra.EmptySet;
import org.eclipse.rdf4j.query.algebra.Extension;
import org.eclipse.rdf4j.query.algebra.ExtensionElem;
import org.eclipse.rdf4j.query.algebra.InsertData;
import org.eclipse.rdf4j.query.algebra.Load;
import org.eclipse.rdf4j.query.algebra.Modify;
import org.eclipse.rdf4j.query.algebra.Move;
import org.eclipse.rdf4j.query.algebra.MultiProjection;
import org.eclipse.rdf4j.query.algebra.Projection;
import org.eclipse.rdf4j.query.algebra.ProjectionElem;
import org.eclipse.rdf4j.query.algebra.ProjectionElemList;
import org.eclipse.rdf4j.query.algebra.Reduced;
import org.eclipse.rdf4j.query.algebra.SingletonSet;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.UpdateExpr;
import org.eclipse.rdf4j.query.algebra.ValueConstant;
import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.algebra.StatementPattern.Scope;
import org.eclipse.rdf4j.query.algebra.helpers.StatementPatternCollector;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTAdd;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTClear;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTCopy;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTCreate;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTDeleteClause;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTDeleteData;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTDeleteWhere;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTDrop;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTGraphOrDefault;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTGraphPatternGroup;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTGraphRefAll;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTIRI;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTInsertClause;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTInsertData;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTLoad;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTModify;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTMove;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTQuadsNotTriples;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTUnparsedQuadDataBlock;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTUpdate;
import org.eclipse.rdf4j.query.parser.sparql.ast.VisitorException;

/**
 * Extension of TupleExprBuilder that builds Update Expressions.
 * 
 * @author Jeen Broekstra
 */
public class UpdateExprBuilder extends TupleExprBuilder {

	/**
	 * @param valueFactory
	 */
	public UpdateExprBuilder(ValueFactory valueFactory) {
		super(valueFactory);
	}

	@Override
	public UpdateExpr visit(ASTUpdate node, Object data)
		throws VisitorException
	{
		if (node instanceof ASTModify) {
			return this.visit((ASTModify)node, data);
		}
		else if (node instanceof ASTInsertData) {
			return this.visit((ASTInsertData)node, data);
		}

		return null;
	}

	@Override
	public InsertData visit(ASTInsertData node, Object data)
		throws VisitorException
	{
		ASTUnparsedQuadDataBlock dataBlock = node.jjtGetChild(ASTUnparsedQuadDataBlock.class);
		return new InsertData(dataBlock.getDataBlock());
	}

	@Override
	public DeleteData visit(ASTDeleteData node, Object data)
		throws VisitorException
	{

		ASTUnparsedQuadDataBlock dataBlock = node.jjtGetChild(ASTUnparsedQuadDataBlock.class);
		return new DeleteData(dataBlock.getDataBlock());
	
	}

	@Override
	public TupleExpr visit(ASTQuadsNotTriples node, Object data)
		throws VisitorException
	{
		GraphPattern parentGP = graphPattern;
		graphPattern = new GraphPattern();

		ValueExpr contextNode = (ValueExpr)node.jjtGetChild(0).jjtAccept(this, data);

		Var contextVar = mapValueExprToVar(contextNode);
		graphPattern.setContextVar(contextVar);
		graphPattern.setStatementPatternScope(Scope.NAMED_CONTEXTS);

		for (int i = 1; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}

		TupleExpr result = graphPattern.buildTupleExpr();
		parentGP.addRequiredTE(result);

		graphPattern = parentGP;

		return result;
	}

	@Override
	public Modify visit(ASTDeleteWhere node, Object data)
		throws VisitorException
	{
		// Collect delete clause triples
		GraphPattern parentGP = graphPattern;
		graphPattern = new GraphPattern();

		// inherit scope & context
		graphPattern.setStatementPatternScope(parentGP.getStatementPatternScope());
		graphPattern.setContextVar(parentGP.getContextVar());

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}

		TupleExpr whereExpr = graphPattern.buildTupleExpr();
		graphPattern = parentGP;

		TupleExpr deleteExpr = whereExpr.clone();

		// FIXME we should adapt the grammar so we can avoid doing this
		// post-processing.
		VarCollector collector = new VarCollector();
		deleteExpr.visit(collector);
		for (Var var : collector.getCollectedVars()) {
			if (var.isAnonymous() && !var.hasValue()) {
				throw new VisitorException("DELETE WHERE may not contain blank nodes");
			}
		}

		Modify modify = new Modify(deleteExpr, null, whereExpr);

		return modify;
	}

	@Override
	public Load visit(ASTLoad node, Object data)
		throws VisitorException
	{

		ValueConstant source = (ValueConstant)node.jjtGetChild(0).jjtAccept(this, data);

		Load load = new Load(source);
		load.setSilent(node.isSilent());
		if (node.jjtGetNumChildren() > 1) {
			ValueConstant graph = (ValueConstant)node.jjtGetChild(1).jjtAccept(this, data);
			load.setGraph(graph);
		}

		return load;
	}

	@Override
	public Clear visit(ASTClear node, Object data)
		throws VisitorException
	{
		Clear clear = new Clear();
		clear.setSilent(node.isSilent());

		ASTGraphRefAll graphRef = node.jjtGetChild(ASTGraphRefAll.class);

		if (graphRef.jjtGetNumChildren() > 0) {
			ValueConstant graph = (ValueConstant)graphRef.jjtGetChild(0).jjtAccept(this, data);
			clear.setGraph(graph);
		}
		else {
			if (graphRef.isDefault()) {
				clear.setScope(Scope.DEFAULT_CONTEXTS);
			}
			else if (graphRef.isNamed()) {
				clear.setScope(Scope.NAMED_CONTEXTS);
			}
		}
		return clear;
	}

	@Override
	public Clear visit(ASTDrop node, Object data)
		throws VisitorException
	{
		// implementing drop as a synonym of clear, in Sesame this is really the
		// same thing, as empty
		// graphs are not recorded.

		Clear clear = new Clear();
		clear.setSilent(node.isSilent());

		ASTGraphRefAll graphRef = node.jjtGetChild(ASTGraphRefAll.class);

		if (graphRef.jjtGetNumChildren() > 0) {
			ValueConstant graph = (ValueConstant)graphRef.jjtGetChild(0).jjtAccept(this, data);
			clear.setGraph(graph);
		}
		else {
			if (graphRef.isDefault()) {
				clear.setScope(Scope.DEFAULT_CONTEXTS);
			}
			else if (graphRef.isNamed()) {
				clear.setScope(Scope.NAMED_CONTEXTS);
			}
		}
		return clear;
	}

	@Override
	public Create visit(ASTCreate node, Object data)
		throws VisitorException
	{
		ValueConstant graph = (ValueConstant)node.jjtGetChild(0).jjtAccept(this, data);

		Create create = new Create(graph);
		create.setSilent(node.isSilent());
		return create;
	}

	@Override
	public Copy visit(ASTCopy node, Object data)
		throws VisitorException
	{
		Copy copy = new Copy();
		copy.setSilent(node.isSilent());

		ASTGraphOrDefault sourceNode = (ASTGraphOrDefault)node.jjtGetChild(0);
		if (sourceNode.jjtGetNumChildren() > 0) {
			ValueConstant sourceGraph = (ValueConstant)sourceNode.jjtGetChild(0).jjtAccept(this, data);
			copy.setSourceGraph(sourceGraph);
		}

		ASTGraphOrDefault destinationNode = (ASTGraphOrDefault)node.jjtGetChild(1);
		if (destinationNode.jjtGetNumChildren() > 0) {
			ValueConstant destinationGraph = (ValueConstant)destinationNode.jjtGetChild(0).jjtAccept(this, data);
			copy.setDestinationGraph(destinationGraph);
		}
		return copy;
	}

	@Override
	public Move visit(ASTMove node, Object data)
		throws VisitorException
	{
		Move move = new Move();
		move.setSilent(node.isSilent());

		ASTGraphOrDefault sourceNode = (ASTGraphOrDefault)node.jjtGetChild(0);
		if (sourceNode.jjtGetNumChildren() > 0) {
			ValueConstant sourceGraph = (ValueConstant)sourceNode.jjtGetChild(0).jjtAccept(this, data);
			move.setSourceGraph(sourceGraph);
		}

		ASTGraphOrDefault destinationNode = (ASTGraphOrDefault)node.jjtGetChild(1);
		if (destinationNode.jjtGetNumChildren() > 0) {
			ValueConstant destinationGraph = (ValueConstant)destinationNode.jjtGetChild(0).jjtAccept(this, data);
			move.setDestinationGraph(destinationGraph);
		}
		return move;
	}

	@Override
	public Add visit(ASTAdd node, Object data)
		throws VisitorException
	{
		Add add = new Add();
		add.setSilent(node.isSilent());

		ASTGraphOrDefault sourceNode = (ASTGraphOrDefault)node.jjtGetChild(0);
		if (sourceNode.jjtGetNumChildren() > 0) {
			ValueConstant sourceGraph = (ValueConstant)sourceNode.jjtGetChild(0).jjtAccept(this, data);
			add.setSourceGraph(sourceGraph);
		}

		ASTGraphOrDefault destinationNode = (ASTGraphOrDefault)node.jjtGetChild(1);
		if (destinationNode.jjtGetNumChildren() > 0) {
			ValueConstant destinationGraph = (ValueConstant)destinationNode.jjtGetChild(0).jjtAccept(this, data);
			add.setDestinationGraph(destinationGraph);
		}
		return add;
	}

	@Override
	public Modify visit(ASTModify node, Object data)
		throws VisitorException
	{

		ValueConstant with = null;
		ASTIRI withNode = node.getWithClause();
		if (withNode != null) {
			with = (ValueConstant)withNode.jjtAccept(this, data);
		}

		if (with != null) {
			graphPattern.setContextVar(mapValueExprToVar(with));
			graphPattern.setStatementPatternScope(Scope.NAMED_CONTEXTS);
		}

		ASTGraphPatternGroup whereClause = node.getWhereClause();

		TupleExpr where = null;
		if (whereClause != null) {
			where = (TupleExpr)whereClause.jjtAccept(this, data);
		}

		TupleExpr delete = null;
		ASTDeleteClause deleteNode = node.getDeleteClause();
		if (deleteNode != null) {
			delete = (TupleExpr)deleteNode.jjtAccept(this, data);
		}

		TupleExpr insert = null;
		ASTInsertClause insertNode = node.getInsertClause();
		if (insertNode != null) {
			insert = (TupleExpr)insertNode.jjtAccept(this, data);
		}

		Modify modifyExpr = new Modify(delete, insert, where);

		return modifyExpr;
	}

	@Override
	public TupleExpr visit(ASTDeleteClause node, Object data)
		throws VisitorException
	{
		TupleExpr result = (TupleExpr)data;

		// Collect construct triples
		GraphPattern parentGP = graphPattern;

		graphPattern = new GraphPattern();

		// inherit scope & context
		graphPattern.setStatementPatternScope(parentGP.getStatementPatternScope());
		graphPattern.setContextVar(parentGP.getContextVar());

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}

		TupleExpr deleteExpr = graphPattern.buildTupleExpr();

		// FIXME we should adapt the grammar so we can avoid doing this in
		// post-processing.
		VarCollector collector = new VarCollector();
		deleteExpr.visit(collector);
		for (Var var : collector.getCollectedVars()) {
			if (var.isAnonymous() && !var.hasValue()) {
				// blank node in delete pattern, not allowed by SPARQL spec.
				throw new VisitorException("DELETE clause may not contain blank nodes");
			}
		}

		graphPattern = parentGP;

		return deleteExpr;

	}

	@Override
	public TupleExpr visit(ASTInsertClause node, Object data)
		throws VisitorException
	{
		TupleExpr result = (TupleExpr)data;

		// Collect insert clause triples
		GraphPattern parentGP = graphPattern;
		graphPattern = new GraphPattern();

		// inherit scope & context
		graphPattern.setStatementPatternScope(parentGP.getStatementPatternScope());
		graphPattern.setContextVar(parentGP.getContextVar());

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			node.jjtGetChild(i).jjtAccept(this, data);
		}

		TupleExpr insertExpr = graphPattern.buildTupleExpr();

		graphPattern = parentGP;

		return insertExpr;

	}

	private Set<Var> getProjectionVars(Collection<StatementPattern> statementPatterns) {
		Set<Var> vars = new LinkedHashSet<Var>(statementPatterns.size() * 2);

		for (StatementPattern sp : statementPatterns) {
			vars.add(sp.getSubjectVar());
			vars.add(sp.getPredicateVar());
			vars.add(sp.getObjectVar());
			if (sp.getContextVar() != null) {
				vars.add(sp.getContextVar());
			}
		}

		return vars;
	}
}