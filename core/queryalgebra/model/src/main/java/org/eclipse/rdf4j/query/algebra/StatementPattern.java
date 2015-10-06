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
package org.eclipse.rdf4j.query.algebra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A tuple expression that matches a statement pattern against an RDF graph.
 * Statement patterns can be targeted at one of three context scopes: all
 * contexts, null context only, or named contexts only.
 */
public class StatementPattern extends AbstractQueryModelNode implements TupleExpr {

	/*------------*
	 * enum Scope *
	 *------------*/

	/**
	 * Indicates the scope of the statement pattern.
	 */
	public enum Scope {
		/**
		 * Scope for patterns that should be matched against statements from the
		 * default contexts.
		 */
		DEFAULT_CONTEXTS,

		/**
		 * Scope for patterns that should be matched against statements from named
		 * contexts only.
		 */
		NAMED_CONTEXTS
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private Scope scope;

	private Var subjectVar;

	private Var predicateVar;

	private Var objectVar;

	private Var contextVar;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public StatementPattern() {
	}

	/**
	 * Creates a statement pattern that matches a subject-, predicate- and object
	 * variable against statements from all contexts.
	 */
	public StatementPattern(Var subject, Var predicate, Var object) {
		this(Scope.DEFAULT_CONTEXTS, subject, predicate, object);
	}

	/**
	 * Creates a statement pattern that matches a subject-, predicate- and object
	 * variable against statements from the specified context scope.
	 */
	public StatementPattern(Scope scope, Var subject, Var predicate, Var object) {
		this(scope, subject, predicate, object, null);
	}

	/**
	 * Creates a statement pattern that matches a subject-, predicate-, object-
	 * and context variable against statements from all contexts.
	 */
	public StatementPattern(Var subject, Var predicate, Var object, Var context) {
		this(Scope.DEFAULT_CONTEXTS, subject, predicate, object, context);
	}

	/**
	 * Creates a statement pattern that matches a subject-, predicate-, object-
	 * and context variable against statements from the specified context scope.
	 */
	public StatementPattern(Scope scope, Var subjVar, Var predVar, Var objVar, Var conVar) {
		setScope(scope);
		setSubjectVar(subjVar);
		setPredicateVar(predVar);
		setObjectVar(objVar);
		setContextVar(conVar);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the context scope for the statement pattern.
	 */
	public Scope getScope() {
		return scope;
	}

	/**
	 * Sets the context scope for the statement pattern.
	 */
	public void setScope(Scope scope) {
		assert scope != null : "scope must not be null";
		this.scope = scope;
	}

	public Var getSubjectVar() {
		return subjectVar;
	}

	public void setSubjectVar(Var subject) {
		assert subject != null : "subject must not be null";
		subject.setParentNode(this);
		subjectVar = subject;
	}

	public Var getPredicateVar() {
		return predicateVar;
	}

	public void setPredicateVar(Var predicate) {
		assert predicate != null : "predicate must not be null";
		predicate.setParentNode(this);
		predicateVar = predicate;
	}

	public Var getObjectVar() {
		return objectVar;
	}

	public void setObjectVar(Var object) {
		assert object != null : "object must not be null";
		object.setParentNode(this);
		objectVar = object;
	}

	/**
	 * Returns the context variable, if available.
	 */
	public Var getContextVar() {
		return contextVar;
	}

	public void setContextVar(Var context) {
		if (context != null) {
			context.setParentNode(this);
		}
		contextVar = context;
	}

	public Set<String> getBindingNames() {
		return getAssuredBindingNames();
	}

	public Set<String> getAssuredBindingNames() {
		Set<String> bindingNames = new HashSet<String>(8);

		if (subjectVar != null) {
			bindingNames.add(subjectVar.getName());
		}
		if (predicateVar != null) {
			bindingNames.add(predicateVar.getName());
		}
		if (objectVar != null) {
			bindingNames.add(objectVar.getName());
		}
		if (contextVar != null) {
			bindingNames.add(contextVar.getName());
		}

		return bindingNames;
	}

	public List<Var> getVarList() {
		return getVars(new ArrayList<Var>(4));
	}

	/**
	 * Adds the variables of this statement pattern to the supplied collection.
	 */
	public <L extends Collection<Var>> L getVars(L varCollection) {
		if (subjectVar != null) {
			varCollection.add(subjectVar);
		}
		if (predicateVar != null) {
			varCollection.add(predicateVar);
		}
		if (objectVar != null) {
			varCollection.add(objectVar);
		}
		if (contextVar != null) {
			varCollection.add(contextVar);
		}

		return varCollection;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		if (subjectVar != null) {
			subjectVar.visit(visitor);
		}
		if (predicateVar != null) {
			predicateVar.visit(visitor);
		}
		if (objectVar != null) {
			objectVar.visit(visitor);
		}
		if (contextVar != null) {
			contextVar.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (subjectVar == current) {
			setSubjectVar((Var)replacement);
		}
		else if (predicateVar == current) {
			setPredicateVar((Var)replacement);
		}
		else if (objectVar == current) {
			setObjectVar((Var)replacement);
		}
		else if (contextVar == current) {
			setContextVar((Var)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public String getSignature() {
		StringBuilder sb = new StringBuilder(128);

		sb.append(super.getSignature());

		if (scope == Scope.NAMED_CONTEXTS) {
			sb.append(" FROM NAMED CONTEXT");
		}

		return sb.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof StatementPattern) {
			StatementPattern o = (StatementPattern)other;
			return subjectVar.equals(o.getSubjectVar()) && predicateVar.equals(o.getPredicateVar())
					&& objectVar.equals(o.getObjectVar()) && nullEquals(contextVar, o.getContextVar())
					&& scope.equals(o.getScope());
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = subjectVar.hashCode();
		result ^= predicateVar.hashCode();
		result ^= objectVar.hashCode();
		if (contextVar != null) {
			result ^= contextVar.hashCode();
		}
		if (scope == Scope.NAMED_CONTEXTS) {
			result = ~result;
		}
		return result;
	}

	@Override
	public StatementPattern clone() {
		StatementPattern clone = (StatementPattern)super.clone();
		clone.setSubjectVar(getSubjectVar().clone());
		clone.setPredicateVar(getPredicateVar().clone());
		clone.setObjectVar(getObjectVar().clone());

		if (getContextVar() != null) {
			clone.setContextVar(getContextVar().clone());
		}

		return clone;
	}
}