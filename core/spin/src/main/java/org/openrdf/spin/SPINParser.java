/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.spin;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.openrdf.OpenRDFException;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.AFN;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SP;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.model.vocabulary.SPL;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.algebra.AggregateOperator;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Avg;
import org.openrdf.query.algebra.BNodeGenerator;
import org.openrdf.query.algebra.BindingSetAssignment;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.Coalesce;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.Count;
import org.openrdf.query.algebra.Datatype;
import org.openrdf.query.algebra.DescribeOperator;
import org.openrdf.query.algebra.Difference;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.Exists;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.GroupConcat;
import org.openrdf.query.algebra.GroupElem;
import org.openrdf.query.algebra.IRIFunction;
import org.openrdf.query.algebra.If;
import org.openrdf.query.algebra.IsBNode;
import org.openrdf.query.algebra.IsLiteral;
import org.openrdf.query.algebra.IsNumeric;
import org.openrdf.query.algebra.IsURI;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.Lang;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.LocalName;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.MathExpr.MathOp;
import org.openrdf.query.algebra.Max;
import org.openrdf.query.algebra.Min;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.Sample;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.StatementPattern.Scope;
import org.openrdf.query.algebra.Str;
import org.openrdf.query.algebra.Sum;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedDescribeQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedOperation;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.ParsedUpdate;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.queryrender.sparql.SPARQLQueryRenderer;
import org.openrdf.spin.util.Statements;

import com.google.common.base.Function;
import com.google.common.collect.Sets;

public class SPINParser {

	private static final Set<URI> QUERY_TYPES = Sets.newHashSet(SP.SELECT_CLASS, SP.CONSTRUCT_CLASS,
			SP.ASK_CLASS, SP.DESCRIBE_CLASS);

	private static final Set<URI> UPDATE_TYPES = Sets.newHashSet(SP.MODIFY_CLASS, SP.INSERT_DATA_CLASS,
			SP.DELETE_DATA_CLASS, SP.LOAD_CLASS, SP.CLEAR_CLASS, SP.CREATE_CLASS, SP.DROP_CLASS);

	private static final Set<URI> COMMAND_TYPES = Sets.union(QUERY_TYPES, UPDATE_TYPES);

	private static final Set<URI> NON_TEMPLATES = Sets.newHashSet(RDFS.RESOURCE, SP.SYSTEM_CLASS,
			SP.COMMAND_CLASS, SP.QUERY_CLASS, SP.UPDATE_CLASS, SPIN.MODULES_CLASS, SPIN.TEMPLATES_CLASS,
			SPIN.ASK_TEMPLATES_CLASS, SPIN.SELECT_TEMPLATES_CLASS, SPIN.CONSTRUCT_TEMPLATES_CLASS,
			SPIN.UPDATE_TEMPLATES_CLASS, SPIN.RULE_CLASS);

	private static final Set<URI> TEMPLATE_TYPES = Sets.newHashSet(SPIN.ASK_TEMPLATE_CLASS,
			SPIN.SELECT_TEMPLATE_CLASS, SPIN.CONSTRUCT_TEMPLATE_CLASS, SPIN.UPDATE_TEMPLATE_CLASS);

	private static final Set<URI> NON_ARG_PROPERTIES = Sets.newHashSet(RDF.TYPE, SP.ARG_PROPERTY, SP.ELEMENTS_PROPERTY);

	public enum Input {
		TEXT_FIRST(true, true),
		TEXT_ONLY(true, false),
		RDF_FIRST(false, true),
		RDF_ONLY(false, false);

		final boolean textFirst;

		final boolean canFallback;

		Input(boolean textFirst, boolean canFallback) {
			this.textFirst = textFirst;
			this.canFallback = canFallback;
		}
	}

	private final Input input;
	private final Function<URI,String> wellKnownVars;
	private final Function<URI,String> wellKnownFunctions;
	private boolean strictFunctionChecking = true;

	public SPINParser() {
		this(Input.TEXT_FIRST);
	}

	public SPINParser(Input input) {
		this(input,
			new Function<URI,String>() {
				@Override
				public String apply(URI uri) {
					return SPINWellKnownVars.INSTANCE.getName(uri);
				}
			},
			new Function<URI,String>() {
				@Override
				public String apply(URI uri) {
					return SPINWellKnownFunctions.INSTANCE.getName(uri);
				}
			});
	}

	public SPINParser(Input input, Function<URI,String> wellKnownVarsMapper, Function<URI,String> wellKnownFuncMapper) {
		this.input = input;
		this.wellKnownVars = wellKnownVarsMapper;
		this.wellKnownFunctions = wellKnownFuncMapper;
	}

	public boolean isStrictFunctionChecking() {
		return strictFunctionChecking;
	}

	public void setStrictFunctionChecking(boolean strictFunctionChecking) {
		this.strictFunctionChecking = strictFunctionChecking;
	}

	public Map<URI, RuleProperty> parseRuleProperties(TripleSource store)
		throws OpenRDFException
	{
		Map<URI, RuleProperty> rules = new HashMap<URI, RuleProperty>();
		CloseableIteration<? extends URI, ? extends OpenRDFException> rulePropIter = Statements.getSubjectURIs(
				RDFS.SUBPROPERTYOF, SPIN.RULE_PROPERTY, store);
		try {
			while (rulePropIter.hasNext()) {
				URI ruleProp = rulePropIter.next();
				RuleProperty ruleProperty = new RuleProperty(ruleProp);

				List<URI> nextRules = getNextRules(ruleProp, store);
				ruleProperty.setNextRules(nextRules);

				int maxIterCount = getMaxIterationCount(ruleProp, store);
				ruleProperty.setMaxIterationCount(maxIterCount);

				rules.put(ruleProp, ruleProperty);
			}
		}
		finally {
			rulePropIter.close();
		}
		return rules;
	}

	private List<URI> getNextRules(Resource ruleProp, TripleSource store)
		throws OpenRDFException
	{
		List<URI> nextRules = new ArrayList<URI>();
		CloseableIteration<? extends URI, ? extends OpenRDFException> iter = Statements.getObjectURIs(ruleProp,
				SPIN.NEXT_RULE_PROPERTY_PROPERTY, store);
		try {
			while (iter.hasNext()) {
				nextRules.add(iter.next());
			}
		}
		finally {
			iter.close();
		}
		return nextRules;
	}

	private int getMaxIterationCount(Resource ruleProp, TripleSource store)
		throws OpenRDFException
	{
		Value v = Statements.singleValue(ruleProp, SPIN.RULE_PROPERTY_MAX_ITERATION_COUNT_PROPERTY, store);
		if (v == null) {
			return -1;
		}
		else if (v instanceof Literal) {
			try {
				return ((Literal)v).intValue();
			}
			catch (NumberFormatException e) {
				throw new MalformedSPINException("Value for " + SPIN.RULE_PROPERTY_MAX_ITERATION_COUNT_PROPERTY
						+ " must be of datatype " + XMLSchema.INTEGER + ": " + ruleProp);
			}
		}
		else {
			throw new MalformedSPINException("Non-literal value for "
					+ SPIN.RULE_PROPERTY_MAX_ITERATION_COUNT_PROPERTY + ": " + ruleProp);
		}
	}

	public boolean isThisUnbound(Resource subj, TripleSource store)
		throws OpenRDFException
	{
		return Statements.booleanValue(subj, SPIN.THIS_UNBOUND_PROPERTY, store);
	}

	public ConstraintViolation parseConstraintViolation(Resource subj, TripleSource store)
		throws OpenRDFException
	{
		Value labelValue = Statements.singleValue(subj, RDFS.LABEL, store);
		Value rootValue = Statements.singleValue(subj, SPIN.VIOLATION_ROOT_PROPERTY, store);
		Value pathValue = Statements.singleValue(subj, SPIN.VIOLATION_PATH_PROPERTY, store);
		Value valueValue = Statements.singleValue(subj, SPIN.VIOLATION_VALUE_PROPERTY, store);
		Value levelValue = Statements.singleValue(subj, SPIN.VIOLATION_LEVEL_PROPERTY, store);
		String label = (labelValue instanceof Literal) ? labelValue.stringValue() : null;
		String root = (rootValue instanceof Resource) ? rootValue.stringValue() : null;
		String path = (pathValue != null) ? pathValue.stringValue() : null;
		String value = (valueValue != null) ? valueValue.stringValue() : null;
		ConstraintViolationLevel level = ConstraintViolationLevel.ERROR;
		if(levelValue != null) {
			if(levelValue instanceof URI) {
				level = ConstraintViolationLevel.valueOf((URI) levelValue);
			}
			if (level == null) {
				throw new MalformedSPINException("Invalid value " + levelValue + " for "
						+ SPIN.VIOLATION_LEVEL_PROPERTY + ": " + subj);
			}
		}
		return new ConstraintViolation(label, root, path, value, level);
	}

	public ParsedOperation parse(Resource queryResource, TripleSource store)
		throws OpenRDFException
	{
		return parse(queryResource, SP.COMMAND_CLASS, store);
	}

	public ParsedQuery parseQuery(Resource queryResource, TripleSource store)
		throws OpenRDFException
	{
		return (ParsedQuery)parse(queryResource, SP.QUERY_CLASS, store);
	}

	public ParsedGraphQuery parseConstructQuery(Resource queryResource, TripleSource store)
		throws OpenRDFException
	{
		return (ParsedGraphQuery)parse(queryResource, SP.CONSTRUCT_CLASS, store);
	}

	public ParsedTupleQuery parseSelectQuery(Resource queryResource, TripleSource store)
		throws OpenRDFException
	{
		return (ParsedTupleQuery)parse(queryResource, SP.SELECT_CLASS, store);
	}

	public ParsedBooleanQuery parseAskQuery(Resource queryResource, TripleSource store)
		throws OpenRDFException
	{
		return (ParsedBooleanQuery)parse(queryResource, SP.ASK_CLASS, store);
	}

	public ParsedDescribeQuery parseDescribeQuery(Resource queryResource, TripleSource store)
		throws OpenRDFException
	{
		return (ParsedDescribeQuery)parse(queryResource, SP.DESCRIBE_CLASS, store);
	}

	protected ParsedOperation parse(Resource queryResource, URI queryClass, TripleSource store)
		throws OpenRDFException
	{
		Boolean isQueryElseTemplate = null;
		Set<URI> possibleQueryTypes = new HashSet<URI>();
		Set<URI> possibleTemplates = new HashSet<URI>();
		CloseableIteration<? extends URI, ? extends OpenRDFException> typeIter = Statements.getObjectURIs(
				queryResource, RDF.TYPE, store);
		try {
			while (typeIter.hasNext()) {
				URI type = typeIter.next();
				if (isQueryElseTemplate == null && SPIN.TEMPLATES_CLASS.equals(type)) {
					isQueryElseTemplate = Boolean.FALSE;
				}
				else if ((isQueryElseTemplate == null || isQueryElseTemplate == Boolean.TRUE)
						&& COMMAND_TYPES.contains(type))
				{
					isQueryElseTemplate = Boolean.TRUE;
					possibleQueryTypes.add(type);
				}
				else if ((isQueryElseTemplate == null || isQueryElseTemplate == Boolean.FALSE)
						&& !NON_TEMPLATES.contains(type))
				{
					possibleTemplates.add(type);
				}
			}
		}
		finally {
			typeIter.close();
		}

		ParsedOperation parsedOp;
		if (isQueryElseTemplate == null) {
			throw new MalformedSPINException("Missing RDF type: " + queryResource);
		}
		else if (isQueryElseTemplate == Boolean.TRUE) {
			// command (query or update)
			if (possibleQueryTypes.size() > 1) {
				throw new MalformedSPINException("Incompatible RDF types for command: " + queryResource
						+ " has types " + possibleQueryTypes);
			}

			URI queryType = possibleQueryTypes.iterator().next();

			if (input.textFirst) {
				parsedOp = parseText(queryResource, queryType, store);
				if (parsedOp == null && input.canFallback) {
					parsedOp = parseRDF(queryResource, queryType, store);
				}
			}
			else {
				parsedOp = parseRDF(queryResource, queryType, store);
				if (parsedOp == null && input.canFallback) {
					parsedOp = parseText(queryResource, queryType, store);
				}
			}

			if (parsedOp == null) {
				throw new MalformedSPINException("Command is not parsable: " + queryResource);
			}
		}
		else {
			// template
			if (possibleTemplates.size() > 1) {
				for (Iterator<URI> iter = possibleTemplates.iterator(); iter.hasNext(); ) {
					URI t = iter.next();
					Value abstractValue = Statements.singleValue(t, SPIN.ABSTRACT_PROPERTY, store);
					if(BooleanLiteralImpl.TRUE.equals(abstractValue)) {
						iter.remove();
					}
				}
			}

			if (possibleTemplates.isEmpty()) {
				throw new MalformedSPINException("Template missing RDF type: " + queryResource);
			}
			if (possibleTemplates.size() > 1) {
				throw new MalformedSPINException("Template has unexpected RDF types: " + queryResource+" has non-abstract types "+possibleTemplates);
			}

			URI templateResource = possibleTemplates.iterator().next();
			Template tmpl = getTemplate(templateResource, queryClass, store);
			MapBindingSet args = new MapBindingSet();
			for(Argument arg : tmpl.getArguments()) {
				URI argPred = arg.getPredicate();
				Value argValue = Statements.singleValue(queryResource, argPred, store);
				if(argValue == null && !arg.isOptional()) {
					throw new MalformedSPINException("Missing value for template argument: "+queryResource+" "+argPred);
				}
				if(argValue == null) {
					argValue = arg.getDefaultValue();
				}
				args.addBinding(argPred.getLocalName(), argValue);
			}

			ParsedOperation tmplOp = tmpl.getParsedOperation();
			if(tmplOp instanceof ParsedBooleanQuery) {
				parsedOp = new ParsedBooleanTemplate(tmpl, args);
			}
			else if(tmplOp instanceof ParsedGraphQuery) {
				parsedOp = new ParsedGraphTemplate(tmpl, args);
			}
			else {
				throw new AssertionError("Unrecognised ParsedOperation: "+tmplOp.getClass());
			}
		}

		return parsedOp;
	}

	private Template getTemplate(URI tmplUri, URI queryType, TripleSource store)
		throws OpenRDFException
	{
		// TODO add caching
		return parseTemplate(tmplUri, queryType, store);
	}

	private Template parseTemplate(URI tmplUri, URI queryType, TripleSource store)
		throws OpenRDFException
	{
		Set<URI> possibleTmplTypes = new HashSet<URI>();
		CloseableIteration<? extends URI, ? extends OpenRDFException> typeIter = Statements.getObjectURIs(
				tmplUri, RDF.TYPE, store);
		try {
			while (typeIter.hasNext()) {
				URI type = typeIter.next();
				if (TEMPLATE_TYPES.contains(type)) {
					possibleTmplTypes.add(type);
				}
			}
		}
		finally {
			typeIter.close();
		}

		if (possibleTmplTypes.isEmpty()) {
			throw new MalformedSPINException("Template missing RDF type: " + tmplUri);
		}
		if (possibleTmplTypes.size() > 1) {
			throw new MalformedSPINException("Incompatible RDF types for template: " + tmplUri + " has types "
					+ possibleTmplTypes);
		}

		URI tmplType = possibleTmplTypes.iterator().next();

		Set<URI> compatibleTmplTypes;
		if (SP.QUERY_CLASS.equals(queryType)) {
			compatibleTmplTypes = Sets.newHashSet(SPIN.ASK_TEMPLATE_CLASS, SPIN.SELECT_TEMPLATE_CLASS,
					SPIN.CONSTRUCT_TEMPLATE_CLASS);
		}
		else if (SP.UPDATE_CLASS.equals(queryType) || UPDATE_TYPES.contains(queryType)) {
			compatibleTmplTypes = Collections.singleton(SPIN.UPDATE_TEMPLATE_CLASS);
		}
		else if (SP.ASK_CLASS.equals(queryType)) {
			compatibleTmplTypes = Collections.singleton(SPIN.ASK_TEMPLATE_CLASS);
		}
		else if (SP.SELECT_CLASS.equals(queryType)) {
			compatibleTmplTypes = Collections.singleton(SPIN.SELECT_TEMPLATE_CLASS);
		}
		else if (SP.CONSTRUCT_CLASS.equals(queryType)) {
			compatibleTmplTypes = Collections.singleton(SPIN.CONSTRUCT_TEMPLATE_CLASS);
		}
		else {
			compatibleTmplTypes = TEMPLATE_TYPES;
		}
		if (!compatibleTmplTypes.contains(tmplType)) {
			throw new MalformedSPINException("Template type " + tmplType + " is incompatible with command type "
					+ queryType);
		}

		Template tmpl = new Template(tmplUri);

		Value body = Statements.singleValue(tmplUri, SPIN.BODY_PROPERTY, store);
		if (!(body instanceof Resource)) {
			throw new MalformedSPINException("Template body is not a resource: " + body);
		}
		ParsedOperation op = parse((Resource)body, queryType, store);
		tmpl.setParsedOperation(op);

		CloseableIteration<? extends Resource, ? extends OpenRDFException> argIter = Statements.getObjectResources(
				tmplUri, SPIN.CONSTRAINT_PROPERTY, store);
		try {
			while(argIter.hasNext()) {
				Resource possibleArg = argIter.next();
				Set<URI> argTypes = Iterations.asSet(Statements.getObjectURIs(possibleArg, RDF.TYPE, store));
				if(argTypes.contains(SPL.ARGUMENT_TEMPLATE)) {
					Value argPred = Statements.singleValue(possibleArg, SPL.PREDICATE_PROPERTY, store);
					Value valueType = Statements.singleValue(possibleArg, SPL.VALUE_TYPE_PROPERTY, store);
					boolean optional = Statements.booleanValue(possibleArg, SPL.OPTIONAL_PROPERTY, store);
					Value defaultValue = Statements.singleValue(possibleArg, SPL.DEFAULT_VALUE_PROPERTY, store);
					tmpl.addArgument(new Argument((URI) argPred, (URI) valueType, optional, defaultValue));
				}
			}
		}
		finally {
			argIter.close();
		}

		return tmpl;
	}

	private ParsedOperation parseText(Resource queryResource, URI queryType, TripleSource store)
		throws OpenRDFException
	{
		Value text = Statements.singleValue(queryResource, SP.TEXT_PROPERTY, store);
		if (text != null) {
			if (QUERY_TYPES.contains(queryType)) {
				return QueryParserUtil.parseQuery(QueryLanguage.SPARQL, text.stringValue(), null);
			}
			else if (UPDATE_TYPES.contains(queryType)) {
				return QueryParserUtil.parseUpdate(QueryLanguage.SPARQL, text.stringValue(), null);
			}
			else {
				throw new MalformedSPINException("Unrecognised command type: " + queryType);
			}
		}
		else {
			return null;
		}
	}

	private ParsedOperation parseRDF(Resource queryResource, URI queryType, TripleSource store)
		throws OpenRDFException
	{
		if(SP.CONSTRUCT_CLASS.equals(queryType)) {
			SPINVisitor visitor = new SPINVisitor(store);
			visitor.visitConstruct(queryResource);
			return new ParsedGraphQuery(visitor.getTupleExpr());
		}
		else if(SP.SELECT_CLASS.equals(queryType)) {
			SPINVisitor visitor = new SPINVisitor(store);
			visitor.visitSelect(queryResource);
			return new ParsedTupleQuery(visitor.getTupleExpr());
		}
		else if(SP.ASK_CLASS.equals(queryType)) {
			SPINVisitor visitor = new SPINVisitor(store);
			visitor.visitAsk(queryResource);
			return new ParsedBooleanQuery(visitor.getTupleExpr());
		}
		else if(SP.DESCRIBE_CLASS.equals(queryType)) {
			SPINVisitor visitor = new SPINVisitor(store);
			visitor.visitDescribe(queryResource);
			return new ParsedDescribeQuery(visitor.getTupleExpr());
		}
		else if (UPDATE_TYPES.contains(queryType)) {
			return new ParsedUpdate();
		}
		else {
			throw new MalformedSPINException("Unrecognised command type: " + queryType);
		}
	}


	class SPINVisitor {
		final TripleSource store;
		TupleExpr root;
		TupleExpr node;
		Var namedGraph;
		Map<String,ProjectionElem> projElems;
		Group group;
		Map<Resource,String> vars = new HashMap<Resource,String>();
		Collection<AggregateOperator> aggregates = new ArrayList<AggregateOperator>();

		SPINVisitor(TripleSource store) {
			this.store = store;
		}

		public TupleExpr getTupleExpr() {
			return root;
		}

		public void visitConstruct(Resource construct)
				throws OpenRDFException
		{
			Value templates = Statements.singleValue(construct, SP.TEMPLATES_PROPERTY, store);
			if(!(templates instanceof Resource)) {
				throw new MalformedSPINException("Value of "+SP.TEMPLATES_PROPERTY+" is not a resource");
			}

			projElems = new LinkedHashMap<String,ProjectionElem>();
			UnaryTupleOperator projection = visitTemplates((Resource) templates);
			visitWhere(construct);
			addSourceExpressions(projection, projElems.values());
		}

		public void visitDescribe(Resource describe)
				throws OpenRDFException
		{
			Value resultNodes = Statements.singleValue(describe, SP.RESULT_NODES_PROPERTY, store);
			if(!(resultNodes instanceof Resource)) {
				throw new MalformedSPINException("Value of "+SP.RESULT_NODES_PROPERTY+" is not a resource");
			}

			projElems = new LinkedHashMap<String,ProjectionElem>();
			Projection projection = visitResultNodes((Resource) resultNodes);
			visitWhere(describe);
			addSourceExpressions(projection, projElems.values());
		}

		public void visitSelect(Resource select)
				throws OpenRDFException
		{
			Value resultVars = Statements.singleValue(select, SP.RESULT_VARIABLES_PROPERTY, store);
			if(!(resultVars instanceof Resource)) {
				throw new MalformedSPINException("Value of "+SP.RESULT_VARIABLES_PROPERTY+" is not a resource");
			}

			projElems = new LinkedHashMap<String,ProjectionElem>();
			Projection projection = visitResultVariables((Resource) resultVars);
			visitWhere(select);

			Value groupBy = Statements.singleValue(select, SP.GROUP_BY_PROPERTY, store);
			if(groupBy instanceof Resource) {
				visitGroupBy((Resource) groupBy);
			}
			if(group != null) {
				group.setArg(projection.getArg());
				projection.setArg(group);
			}

			Value having = Statements.singleValue(select, SP.HAVING_PROPERTY, store);
			if(having instanceof Resource) {
				TupleExpr havingExpr = visitHaving((Resource) having);
				projection.setArg(havingExpr);
			}

			addSourceExpressions(projection, projElems.values());

			Value orderby = Statements.singleValue(select, SP.ORDER_BY_PROPERTY, store);
			if(orderby instanceof Resource) {
				Order order = visitOrderBy((Resource) orderby);
				order.setArg(projection.getArg());
				projection.setArg(order);
			}

			boolean distinct = Statements.booleanValue(select, SP.DISTINCT_PROPERTY, store);
			if(distinct) {
				root = new Distinct(root);
			}

			long offset = -1L;
			Value offsetValue = Statements.singleValue(select, SP.OFFSET_PROPERTY, store);
			if(offsetValue instanceof Literal) {
				offset = ((Literal)offsetValue).longValue();
			}
			long limit = -1L;
			Value limitValue = Statements.singleValue(select, SP.LIMIT_PROPERTY, store);
			if(limitValue instanceof Literal) {
				limit = ((Literal)limitValue).longValue();
			}
			if(offset > 0L || limit >= 0L) {
				Slice slice = new Slice(root);
				if(offset > 0L) {
					slice.setOffset(offset);
				}
				if(limit >= 0L) {
					slice.setLimit(limit);
				}
				root = slice;
			}
		}

		public void visitAsk(Resource ask)
				throws OpenRDFException
		{
			node = new SingletonSet();
			root = new Slice(node, 0, 1);
			visitWhere(ask);
		}

		private void addSourceExpressions(UnaryTupleOperator op, Collection<ProjectionElem> elems) {
			Extension ext = null;
			for(ProjectionElem projElem : elems) {
				ExtensionElem extElem = projElem.getSourceExpression();
				if(extElem != null) {
					if(ext == null) {
						ext = new Extension(op.getArg());
						op.setArg(ext);
					}
					ext.addElement(extElem);
				}
			}
		}

		private UnaryTupleOperator visitTemplates(Resource templates)
				throws OpenRDFException
		{
			List<ProjectionElemList> projElemLists = new ArrayList<ProjectionElemList>();
			Iteration<? extends Resource,QueryEvaluationException> iter = Statements.listResources(templates, store);
			while(iter.hasNext()) {
				Resource r = iter.next();
				ProjectionElemList projElems = visitTemplate(r);
				projElemLists.add(projElems);
			}

			UnaryTupleOperator expr;
			if(projElemLists.size() > 1) {
				MultiProjection proj = new MultiProjection();
				proj.setProjections(projElemLists);
				expr = proj;
			}
			else {
				Projection proj = new Projection();
				proj.setProjectionElemList(projElemLists.get(0));
				expr = proj;
			}

			Reduced reduced = new Reduced();
			reduced.setArg(expr);
			root = reduced;
			SingletonSet stub = new SingletonSet();
			expr.setArg(stub);
			node = stub;
			return expr;
		}

		private ProjectionElemList visitTemplate(Resource r)
				throws OpenRDFException
		{
			ProjectionElemList projElems = new ProjectionElemList();
			Value subj = Statements.singleValue(r, SP.SUBJECT_PROPERTY, store);
			projElems.addElement(createProjectionElem(subj, "subject"));
			Value pred = Statements.singleValue(r, SP.PREDICATE_PROPERTY, store);
			projElems.addElement(createProjectionElem(pred, "predicate"));
			Value obj = Statements.singleValue(r, SP.OBJECT_PROPERTY, store);
			projElems.addElement(createProjectionElem(obj, "object"));
			return projElems;
		}

		private Projection visitResultNodes(Resource resultNodes)
				throws OpenRDFException
		{
			ProjectionElemList projElemList = new ProjectionElemList();
			Iteration<? extends Resource,QueryEvaluationException> iter = Statements.listResources(resultNodes, store);
			while(iter.hasNext()) {
				Resource r = iter.next();
				ProjectionElem projElem = visitResultNode(r);
				projElemList.addElement(projElem);
			}

			Projection proj = new Projection();
			proj.setProjectionElemList(projElemList);

			root = new DescribeOperator(proj);
			SingletonSet stub = new SingletonSet();
			proj.setArg(stub);
			node = stub;
			return proj;
		}

		private ProjectionElem visitResultNode(Resource r)
				throws OpenRDFException
		{
			return createProjectionElem(r, null);
		}

		private Projection visitResultVariables(Resource resultVars)
				throws OpenRDFException
		{
			ProjectionElemList projElemList = new ProjectionElemList();
			Iteration<? extends Resource,QueryEvaluationException> iter = Statements.listResources(resultVars, store);
			while(iter.hasNext()) {
				Resource r = iter.next();
				ProjectionElem projElem = visitResultVariable(r);
				projElemList.addElement(projElem);
			}

			Projection proj = new Projection();
			proj.setProjectionElemList(projElemList);

			root = proj;
			SingletonSet stub = new SingletonSet();
			proj.setArg(stub);
			node = stub;
			return proj;
		}

		private ProjectionElem visitResultVariable(Resource r)
			throws OpenRDFException
		{
			return createProjectionElem(r, null);
		}

		private void visitGroupBy(Resource groupby)
			throws OpenRDFException
		{
			if(group == null) {
				group = new Group();
			}
			Iteration<? extends Resource,QueryEvaluationException> iter = Statements.listResources(groupby, store);
			while(iter.hasNext()) {
				Resource r = iter.next();
				ValueExpr groupByExpr = visitExpression(r);
				if(groupByExpr instanceof Var) {
					group.addGroupBindingName(((Var)groupByExpr).getName());
				}
				else {
					// TODO
					throw new UnsupportedOperationException("TODO!");
				}
			}
		}

		private TupleExpr visitHaving(Resource having)
			throws OpenRDFException
		{
			UnaryTupleOperator op = (UnaryTupleOperator) group.getParentNode();
			op.setArg(new Extension(group));
			Iteration<? extends Resource,QueryEvaluationException> iter = Statements.listResources(having, store);
			while(iter.hasNext()) {
				Resource r = iter.next();
				ValueExpr havingExpr = visitExpression(r);
				Filter filter = new Filter(op.getArg(), havingExpr);
				op.setArg(filter);
				op = filter;
			}
			return op;
		}

		private Order visitOrderBy(Resource orderby)
			throws OpenRDFException
		{
			Order order = new Order();
			Iteration<? extends Resource,QueryEvaluationException> iter = Statements.listResources(orderby, store);
			while(iter.hasNext()) {
				Resource r = iter.next();
				OrderElem orderElem = visitOrderByCondition(r);
				order.addElement(orderElem);
			}
			return order;
		}

		private OrderElem visitOrderByCondition(Resource r)
				throws OpenRDFException
		{
			Value expr = Statements.singleValue(r, SP.EXPRESSION_PROPERTY, store);
			ValueExpr valueExpr = visitExpression(expr);
			Set<Resource> types = Iterations.asSet(Statements.getObjectResources(r, RDF.TYPE, store));
			boolean asc = types.contains(SP.DESC_CLASS) ? false : true;
			return new OrderElem(valueExpr, asc);
		}

		private ProjectionElem createProjectionElem(Value v, String projName)
			throws OpenRDFException
		{
			Collection<AggregateOperator> oldAggregates = aggregates;
			aggregates = new ArrayList<AggregateOperator>();
			ValueExpr valueExpr = visitExpression(v);
			boolean hasAggregates = !aggregates.isEmpty();

			String varName = null;
			if(valueExpr instanceof Var) {
				varName = ((Var)valueExpr).getName();
			}
			else if(valueExpr instanceof ValueConstant) {
				varName = getConstVarName(v);
			}

			if(v instanceof Resource) {
				Value asVar = Statements.singleValue((Resource)v, SP.AS_PROPERTY, store);
				if(asVar != null) {
					if(projName != null) {
						throw new MalformedSPINException("Illegal usage of "+SP.AS_PROPERTY+": "+v);
					}
					projName = getVarName((Resource) asVar);
					if(varName == null) {
						varName = projName;
					}
				}
			}

			if(projName == null) {
				projName = varName;
			}

			ProjectionElem projElem = new ProjectionElem(varName, projName);
			projElem.setSourceExpression(new ExtensionElem(valueExpr, varName));
			if(hasAggregates) {
				projElem.setAggregateOperatorInExpression(true);
				if(group == null) {
					group = new Group();
				}
				for(AggregateOperator op : aggregates) {
					group.addGroupElement(new GroupElem(projName, op));
				}
			}
			aggregates = oldAggregates;
			if(projElems != null) {
				projElems.put(varName, projElem);
			}
			return projElem;
		}

		public void visitWhere(Resource query)
				throws OpenRDFException
		{
			Value where = Statements.singleValue(query, SP.WHERE_PROPERTY, store);
			if(!(where instanceof Resource)) {
				throw new MalformedSPINException("Value of "+SP.WHERE_PROPERTY+" is not a resource");
			}
			visitGroupGraphPattern((Resource) where);
		}

		public void visitGroupGraphPattern(Resource group)
			throws OpenRDFException
		{
			Map<Resource,Set<URI>> patternTypes = new LinkedHashMap<Resource,Set<URI>>();
			Iteration<? extends Resource, QueryEvaluationException> groupIter = Statements.listResources(group, store);
			while(groupIter.hasNext()) {
				Resource r = groupIter.next();
				patternTypes.put(r, Iterations.asSet(Statements.getObjectURIs(r, RDF.TYPE, store)));
			}

			// first process filters
			TupleExpr currentNode = node;
			SingletonSet nextNode = new SingletonSet();
			node = nextNode;
			for(Iterator<Map.Entry<Resource,Set<URI>>> iter = patternTypes.entrySet().iterator(); iter.hasNext(); ) {
				Map.Entry<Resource,Set<URI>> entry = iter.next();
				if(entry.getValue().contains(SP.FILTER_CLASS)) {
					visitFilter(entry.getKey());
					iter.remove();
				}
			}
			currentNode.replaceWith(node);
			node = nextNode;

			// then binds
			currentNode = node;
			nextNode = new SingletonSet();
			node = nextNode;
			for(Iterator<Map.Entry<Resource,Set<URI>>> iter = patternTypes.entrySet().iterator(); iter.hasNext(); ) {
				Map.Entry<Resource,Set<URI>> entry = iter.next();
				if(entry.getValue().contains(SP.BIND_CLASS)) {
					visitBind(entry.getKey());
					iter.remove();
				}
			}
			currentNode.replaceWith(node);
			node = nextNode;

			// then anything else
			for(Iterator<Map.Entry<Resource,Set<URI>>> iter = patternTypes.entrySet().iterator(); iter.hasNext(); ) {
				Map.Entry<Resource,Set<URI>> entry = iter.next();
				visitPattern(entry.getKey(), entry.getValue());
			}
		}

		private void visitPattern(Resource r, Set<URI> types)
			throws OpenRDFException
		{
			TupleExpr currentNode = node;
			Value subj = Statements.singleValue(r, SP.SUBJECT_PROPERTY, store);
			if(subj != null) {
				Value pred = Statements.singleValue(r, SP.PREDICATE_PROPERTY, store);
				Value obj = Statements.singleValue(r, SP.OBJECT_PROPERTY, store);
				Scope stmtScope = (namedGraph != null) ? Scope.NAMED_CONTEXTS : Scope.DEFAULT_CONTEXTS;
				node = new StatementPattern(stmtScope, getVar(subj), getVar(pred), getVar(obj), namedGraph);
			}
			else {
				if(types.contains(SP.NAMED_GRAPH_CLASS)) {
					Var oldGraph = namedGraph;
					Value graphValue = Statements.singleValue(r, SP.GRAPH_NAME_NODE_PROPERTY, store);
					namedGraph = getVar(graphValue);
					Value elements = Statements.singleValue(r, SP.ELEMENTS_PROPERTY, store);
					if(!(elements instanceof Resource)) {
						throw new MalformedSPINException("Value of "+SP.ELEMENTS_PROPERTY+" is not a resource");
					}
					node = new SingletonSet();
					QueryRoot group = new QueryRoot(node);
					visitGroupGraphPattern((Resource) elements);
					node = group.getArg();
					namedGraph = oldGraph;
				}
				else if(types.contains(SP.UNION_CLASS)) {
					Value elements = Statements.singleValue(r, SP.ELEMENTS_PROPERTY, store);
					if(!(elements instanceof Resource)) {
						throw new MalformedSPINException("Value of "+SP.ELEMENTS_PROPERTY+" is not a resource");
					}

					Iteration<? extends Resource, QueryEvaluationException> iter = Statements.listResources((Resource) elements, store);
					TupleExpr prev = null;
					while(iter.hasNext()) {
						Resource entry = iter.next();
						node = new SingletonSet();
						QueryRoot groupRoot = new QueryRoot(node);
						visitGroupGraphPattern(entry);
						TupleExpr groupExpr = groupRoot.getArg();
						if(prev != null) {
							node = new Union(prev, groupExpr);
						}
						prev = groupExpr;
					}
				}
				else if(types.contains(SP.OPTIONAL_CLASS)) {
					Value elements = Statements.singleValue(r, SP.ELEMENTS_PROPERTY, store);
					if(!(elements instanceof Resource)) {
						throw new MalformedSPINException("Value of "+SP.ELEMENTS_PROPERTY+" is not a resource");
					}
					node = new SingletonSet();
					QueryRoot groupRoot = new QueryRoot(node);
					visitGroupGraphPattern((Resource) elements);
					LeftJoin leftJoin = new LeftJoin();
					currentNode.replaceWith(leftJoin);
					leftJoin.setLeftArg(currentNode);
					leftJoin.setRightArg(groupRoot.getArg());
					node = leftJoin;
					currentNode = null;
				}
				else if(types.contains(SP.MINUS_CLASS)) {
					Value elements = Statements.singleValue(r, SP.ELEMENTS_PROPERTY, store);
					if(!(elements instanceof Resource)) {
						throw new MalformedSPINException("Value of "+SP.ELEMENTS_PROPERTY+" is not a resource");
					}
					node = new SingletonSet();
					QueryRoot groupRoot = new QueryRoot(node);
					visitGroupGraphPattern((Resource) elements);
					Difference difference = new Difference();
					currentNode.replaceWith(difference);
					difference.setLeftArg(currentNode);
					difference.setRightArg(groupRoot.getArg());
					node = difference;
					currentNode = null;
				}
				else if(types.contains(SP.SUB_QUERY_CLASS)) {
					Value q = Statements.singleValue(r, SP.QUERY_PROPERTY, store);
					TupleExpr oldRoot = root;
					visitSelect((Resource) q);
					node = root;
					root = oldRoot;
				}
				else if(types.contains(SP.VALUES_CLASS)) {
					BindingSetAssignment bsa = new BindingSetAssignment();
					Set<String> varNames = new LinkedHashSet<String>();
					Value varNameList = Statements.singleValue(r, SP.VAR_NAMES_PROPERTY, store);
					Iteration<? extends Value,QueryEvaluationException> varNameIter = Statements.list((Resource) varNameList, store);
					while(varNameIter.hasNext()) {
						Value v = varNameIter.next();
						if(v instanceof Literal) {
							varNames.add(((Literal)v).getLabel());
						}
					}
					bsa.setBindingNames(varNames);
					List<BindingSet> bindingSets = new ArrayList<BindingSet>();
					Value bindingsList = Statements.singleValue(r, SP.BINDINGS_PROPERTY, store);
					Iteration<? extends Value,QueryEvaluationException> bindingsIter = Statements.list((Resource) bindingsList, store);
					while(bindingsIter.hasNext()) {
						Value valueList = bindingsIter.next();
						QueryBindingSet bs = new QueryBindingSet();
						Iterator<String> nameIter = varNames.iterator();
						Iteration<? extends Value,QueryEvaluationException> valueIter = Statements.list((Resource) valueList, store);
						while(nameIter.hasNext() && valueIter.hasNext()) {
							String name = nameIter.next();
							Value value = valueIter.next();
							bs.addBinding(name, value);
						}
						bindingSets.add(bs);
					}
					bsa.setBindingSets(bindingSets);
					node = bsa;
				}
				else if(types.contains(RDF.LIST) || (Statements.singleValue(r, RDF.FIRST, store) != null)) {
					node = new SingletonSet();
					QueryRoot group = new QueryRoot(node);
					visitGroupGraphPattern(r);
					node = group.getArg();
				}
				else if(types.contains(SP.SERVICE_CLASS)) {
					Value serviceUri = Statements.singleValue(r, SP.SERVICE_URI_PROPERTY, store);
					node = new SingletonSet();
					QueryRoot tempRoot = new QueryRoot(node);

					Value elements = Statements.singleValue(r, SP.ELEMENTS_PROPERTY, store);
					if(!(elements instanceof Resource)) {
						throw new MalformedSPINException("Value of "+SP.ELEMENTS_PROPERTY+" is not a resource");
					}
					visitGroupGraphPattern((Resource) elements);

					boolean isSilent = Statements.booleanValue(r, SP.SILENT_PROPERTY, store);
					String exprString;
					try {
						exprString = new SPARQLQueryRenderer().render(new ParsedTupleQuery(tempRoot.getArg()));
						exprString = exprString.substring("select *\n{\n".length(), exprString.length()-1);
					}
					catch(Exception e) {
						throw new QueryEvaluationException(e);
					}
					Map<String,String> prefixDecls = Collections.emptyMap();
					Service service = new Service(getVar(serviceUri), node, exprString, prefixDecls, null, isSilent);
					node = service;
				}
				else {
					throw new UnsupportedOperationException(types.toString());
				}
			}

			if(currentNode instanceof SingletonSet) {
				currentNode.replaceWith(node);
			}
			else if(currentNode != null) {
				Join join = new Join();
				currentNode.replaceWith(join);
				join.setLeftArg(currentNode);
				join.setRightArg(node);
				node = join;
			}
		}

		private void visitFilter(Resource r)
				throws OpenRDFException
		{
			Value expr = Statements.singleValue(r, SP.EXPRESSION_PROPERTY, store);
			ValueExpr valueExpr = visitExpression(expr);
			node = new Filter(node, valueExpr);
		}

		private void visitBind(Resource r)
				throws OpenRDFException
		{
			Value expr = Statements.singleValue(r, SP.EXPRESSION_PROPERTY, store);
			ValueExpr valueExpr = visitExpression(expr);
			Value varValue = Statements.singleValue(r, SP.VARIABLE_PROPERTY, store);
			if(!(varValue instanceof Resource)) {
				throw new MalformedSPINException("Value of "+SP.VARIABLE_PROPERTY+" is not a resource");
			}
			String varName = getVarName((Resource)varValue);
			node = new Extension(node, new ExtensionElem(valueExpr, varName));
		}

		private ValueExpr visitExpression(Value v)
				throws OpenRDFException
		{
			ValueExpr expr;
			if(v instanceof Literal) {
				expr = new ValueConstant(v);
			}
			else {
				Resource r = (Resource) v;
				String varName = getVarName(r);
				if(varName != null) {
					expr = createVar(varName);
				}
				else {
					Set<URI> exprTypes = Iterations.asSet(Statements.getObjectURIs(r, RDF.TYPE, store));
					exprTypes.remove(RDF.PROPERTY);
					exprTypes.remove(RDFS.RESOURCE);
					exprTypes.remove(RDFS.CLASS);
					if(exprTypes.size() > 1) {
						if(exprTypes.remove(SPIN.FUNCTIONS_CLASS)) {
							exprTypes.remove(SPIN.MODULES_CLASS);
							if(exprTypes.size() > 1) {
								for(Iterator<URI> iter = exprTypes.iterator(); iter.hasNext(); ) {
									URI f = iter.next();
									Value abstractValue = Statements.singleValue(f, SPIN.ABSTRACT_PROPERTY, store);
									if(BooleanLiteralImpl.TRUE.equals(abstractValue)) {
										iter.remove();
									}
								}
							}
							if(exprTypes.isEmpty()) {
								throw new MalformedSPINException("Function missing RDF type: "+r);
							}
						}
						else if(exprTypes.remove(SP.AGGREGATION_CLASS)) {
							exprTypes.remove(SP.SYSTEM_CLASS);
							if(exprTypes.isEmpty()) {
								throw new MalformedSPINException("Aggregation missing RDF type: "+r);
							}
						}
						else {
							throw new MalformedSPINException("Expression missing RDF type: "+r);
						}
					}

					expr = null;
					if(exprTypes.size() == 1) {
						URI func = exprTypes.iterator().next();
						expr = toValueExpr(r, func);
					}
					if(expr == null) {
						expr = new ValueConstant(v);
					}
				}
			}
			return expr;
		}

		private ValueExpr toValueExpr(Resource r, URI func)
			throws OpenRDFException
		{
			ValueExpr expr;
			CompareOp compareOp;
			MathOp mathOp;
			if((compareOp = toCompareOp(func)) != null) {
				List<ValueExpr> args = getArgs(r);
				if(args.size() != 2) {
					throw new MalformedSPINException("Invalid number of arguments for function: "+func);
				}
				expr = new Compare(args.get(0), args.get(1), compareOp);
			}
			else if((mathOp = toMathOp(func)) != null) {
				List<ValueExpr> args = getArgs(r);
				if(args.size() != 2) {
					throw new MalformedSPINException("Invalid number of arguments for function: "+func);
				}
				expr = new MathExpr(args.get(0), args.get(1), mathOp);
			}
			else if(SP.AND.equals(func)) {
				List<ValueExpr> args = getArgs(r);
				if(args.size() != 2) {
					throw new MalformedSPINException("Invalid number of arguments for function: "+func);
				}
				expr = new And(args.get(0), args.get(1));
			}
			else if(SP.OR.equals(func)) {
				List<ValueExpr> args = getArgs(r);
				if(args.size() != 2) {
					throw new MalformedSPINException("Invalid number of arguments for function: "+func);
				}
				expr = new Or(args.get(0), args.get(1));
			}
			else if(SP.COUNT_CLASS.equals(func)) {
				Value arg = Statements.singleValue(r, SP.EXPRESSION_PROPERTY, store);
				boolean distinct = Statements.booleanValue(r, SP.DISTINCT_PROPERTY, store);
				Count count = new Count(visitExpression(arg), distinct);
				aggregates.add(count);
				expr = count;
			}
			else if(SP.MAX_CLASS.equals(func)) {
				Value arg = Statements.singleValue(r, SP.EXPRESSION_PROPERTY, store);
				boolean distinct = Statements.booleanValue(r, SP.DISTINCT_PROPERTY, store);
				Max max = new Max(visitExpression(arg), distinct);
				aggregates.add(max);
				expr = max;
			}
			else if(SP.MIN_CLASS.equals(func)) {
				Value arg = Statements.singleValue(r, SP.EXPRESSION_PROPERTY, store);
				boolean distinct = Statements.booleanValue(r, SP.DISTINCT_PROPERTY, store);
				Min min = new Min(visitExpression(arg), distinct);
				aggregates.add(min);
				expr = min;
			}
			else if(SP.SUM_CLASS.equals(func)) {
				Value arg = Statements.singleValue(r, SP.EXPRESSION_PROPERTY, store);
				boolean distinct = Statements.booleanValue(r, SP.DISTINCT_PROPERTY, store);
				Sum sum = new Sum(visitExpression(arg), distinct);
				aggregates.add(sum);
				expr = sum;
			}
			else if(SP.AVG_CLASS.equals(func)) {
				Value arg = Statements.singleValue(r, SP.EXPRESSION_PROPERTY, store);
				boolean distinct = Statements.booleanValue(r, SP.DISTINCT_PROPERTY, store);
				Avg avg = new Avg(visitExpression(arg), distinct);
				aggregates.add(avg);
				expr = avg;
			}
			else if(SP.GROUP_CONCAT_CLASS.equals(func)) {
				Value arg = Statements.singleValue(r, SP.EXPRESSION_PROPERTY, store);
				boolean distinct = Statements.booleanValue(r, SP.DISTINCT_PROPERTY, store);
				GroupConcat groupConcat = new GroupConcat(visitExpression(arg), distinct);
				aggregates.add(groupConcat);
				expr = groupConcat;
			}
			else if(SP.SAMPLE_CLASS.equals(func)) {
				Value arg = Statements.singleValue(r, SP.EXPRESSION_PROPERTY, store);
				boolean distinct = Statements.booleanValue(r, SP.DISTINCT_PROPERTY, store);
				Sample sample = new Sample(visitExpression(arg), distinct);
				aggregates.add(sample);
				expr = sample;
			}
			else if(SP.NOT.equals(func)) {
				List<ValueExpr> args = getArgs(r);
				if(args.size() != 1) {
					throw new MalformedSPINException("Invalid number of arguments for function: "+func);
				}
				expr = new Not(args.get(0));
			}
			else if(SP.EXISTS.equals(func)) {
				Value elements = Statements.singleValue(r, SP.ELEMENTS_PROPERTY, store);
				if(!(elements instanceof Resource)) {
					throw new MalformedSPINException("Value of "+SP.ELEMENTS_PROPERTY+" is not a resource");
				}
				TupleExpr currentNode = node;
				node = new SingletonSet();
				expr = new Exists(node);
				visitGroupGraphPattern((Resource) elements);
				node = currentNode;
			}
			else if(SP.NOT_EXISTS.equals(func)) {
				Value elements = Statements.singleValue(r, SP.ELEMENTS_PROPERTY, store);
				if(!(elements instanceof Resource)) {
					throw new MalformedSPINException("Value of "+SP.ELEMENTS_PROPERTY+" is not a resource");
				}
				TupleExpr currentNode = node;
				node = new SingletonSet();
				expr = new Not(new Exists(node));
				visitGroupGraphPattern((Resource) elements);
				node = currentNode;
			}
			else if(SP.BOUND.equals(func)) {
				List<ValueExpr> args = getArgs(r);
				if(args.size() != 1) {
					throw new MalformedSPINException("Invalid number of arguments for function: "+func);
				}
				expr = new Bound((Var)args.get(0));
			}
			else if(SP.IF.equals(func)) {
				List<ValueExpr> args = getArgs(r);
				if(args.size() != 3) {
					throw new MalformedSPINException("Invalid number of arguments for function: "+func);
				}
				expr = new If(args.get(0), args.get(1), args.get(2));
			}
			else if(SP.COALESCE.equals(func)) {
				List<ValueExpr> args = getArgs(r);
				expr = new Coalesce(args);
			}
			else if(SP.IS_IRI.equals(func) || SP.IS_URI.equals(func)) {
				List<ValueExpr> args = getArgs(r);
				if(args.size() != 1) {
					throw new MalformedSPINException("Invalid number of arguments for function: "+func);
				}
				expr = new IsURI(args.get(0));
			}
			else if(SP.IS_BLANK.equals(func)) {
				List<ValueExpr> args = getArgs(r);
				if(args.size() != 1) {
					throw new MalformedSPINException("Invalid number of arguments for function: "+func);
				}
				expr = new IsBNode(args.get(0));
			}
			else if(SP.IS_LITERAL.equals(func)) {
				List<ValueExpr> args = getArgs(r);
				if(args.size() != 1) {
					throw new MalformedSPINException("Invalid number of arguments for function: "+func);
				}
				expr = new IsLiteral(args.get(0));
			}
			else if(SP.IS_NUMERIC.equals(func)) {
				List<ValueExpr> args = getArgs(r);
				if(args.size() != 1) {
					throw new MalformedSPINException("Invalid number of arguments for function: "+func);
				}
				expr = new IsNumeric(args.get(0));
			}
			else if(SP.STR.equals(func)) {
				List<ValueExpr> args = getArgs(r);
				if(args.size() != 1) {
					throw new MalformedSPINException("Invalid number of arguments for function: "+func);
				}
				expr = new Str(args.get(0));
			}
			else if(SP.LANG.equals(func)) {
				List<ValueExpr> args = getArgs(r);
				if(args.size() != 1) {
					throw new MalformedSPINException("Invalid number of arguments for function: "+func);
				}
				expr = new Lang(args.get(0));
			}
			else if(SP.DATATYPE.equals(func)) {
				List<ValueExpr> args = getArgs(r);
				if(args.size() != 1) {
					throw new MalformedSPINException("Invalid number of arguments for function: "+func);
				}
				expr = new Datatype(args.get(0));
			}
			else if(SP.IRI.equals(func) || SP.URI.equals(func)) {
				List<ValueExpr> args = getArgs(r);
				if(args.size() != 1) {
					throw new MalformedSPINException("Invalid number of arguments for function: "+func);
				}
				expr = new IRIFunction(args.get(0));
			}
			else if(SP.BNODE.equals(func)) {
				List<ValueExpr> args = getArgs(r);
				ValueExpr arg = (args.size() == 1) ? args.get(0) : null;
				expr = new BNodeGenerator(arg);
			}
			else if(SP.REGEX.equals(func)) {
				List<ValueExpr> args = getArgs(r);
				if(args.size() < 2) {
					throw new MalformedSPINException("Invalid number of arguments for function: "+func);
				}
				ValueExpr flagsArg = (args.size() == 3) ? args.get(2) : null;
				expr = new Regex(args.get(0), args.get(1), flagsArg);
			}
			else if(AFN.LOCALNAME.equals(func)) {
				List<ValueExpr> args = getArgs(r);
				if(args.size() != 1) {
					throw new MalformedSPINException("Invalid number of arguments for function: "+func);
				}
				expr = new LocalName(args.get(0));
			}
			else {
				String funcName = wellKnownFunctions.apply(func);
				if (funcName == null) {
					// check if it is a SPIN function
					Statement funcTypeStmt = Statements.single(func, RDF.TYPE, SPIN.FUNCTION_CLASS, store);
					if(funcTypeStmt != null) {
						funcName = func.stringValue();
					}
				}
				// not enough information available to determine
				// if it is really a function or not
				// so by default we can either assume it is or it is not
				if (funcName == null && !strictFunctionChecking) {
					funcName = func.stringValue();
				}
				if (funcName != null) {
					List<ValueExpr> args = getArgs(r);
					expr = new FunctionCall(funcName, args);
				}
				else {
					expr = null;
				}
			}
			return expr;
		}

		private CompareOp toCompareOp(URI func) {
			if(SP.EQ.equals(func)) {
				return CompareOp.EQ;
			}
			else if(SP.NE.equals(func)) {
				return CompareOp.NE;
			}
			else if(SP.LT.equals(func)) {
				return CompareOp.LT;
			}
			else if(SP.LE.equals(func)) {
				return CompareOp.LE;
			}
			else if(SP.GE.equals(func)) {
				return CompareOp.GE;
			}
			else if(SP.GT.equals(func)) {
				return CompareOp.GT;
			}
			else {
				return null;
			}
		}

		private MathOp toMathOp(URI func) {
			if(SP.ADD.equals(func)) {
				return MathOp.PLUS;
			}
			else if(SP.SUB.equals(func)) {
				return MathOp.MINUS;
			}
			else if(SP.MUL.equals(func)) {
				return MathOp.MULTIPLY;
			}
			else if(SP.DIVIDE.equals(func)) {
				return MathOp.DIVIDE;
			}
			else {
				return null;
			}
		}

		private List<ValueExpr> getArgs(Resource r)
			throws OpenRDFException
		{
			SortedMap<URI, ValueExpr> argValues = new TreeMap<URI, ValueExpr>(new Comparator<URI>()
			{
				@Override
				public int compare(URI uri1, URI uri2) {
					return uri1.getLocalName().compareTo(uri2.getLocalName());
				}
			});
			CloseableIteration<? extends Statement, QueryEvaluationException> iter = store.getStatements(r, null, null);
			try {
				while(iter.hasNext()) {
					Statement stmt = iter.next();
					URI argName = stmt.getPredicate();
					if(!NON_ARG_PROPERTIES.contains(argName)) {
						ValueExpr argValue = visitExpression(stmt.getObject());
						argValues.put(argName, argValue);
					}
				}
			}
			finally {
				iter.close();
			}

			int numArgs = argValues.size();
			List<ValueExpr> args = new ArrayList<ValueExpr>(numArgs);
			for(int i=0; i<numArgs; i++) {
				ValueExpr argExpr = argValues.remove(toArgProperty(i));
				if(argExpr == null) {
					argExpr = argValues.remove(argValues.firstKey());
				}
				args.add(argExpr);
			}
			return args;
		}

		private URI toArgProperty(int i) {
			switch(i) {
			case 1: return SP.ARG1_PROPERTY;
			case 2: return SP.ARG2_PROPERTY;
			case 3: return SP.ARG3_PROPERTY;
			case 4: return SP.ARG4_PROPERTY;
			case 5: return SP.ARG5_PROPERTY;
			default:
				return ValueFactoryImpl.getInstance().createURI(SP.NAMESPACE, "arg"+i);
			}
		}

		private String getVarName(Resource r)
				throws OpenRDFException
		{
			// have we already seen it
			String varName = vars.get(r);
			// is it well-known
			if(varName == null && r instanceof URI) {
				varName = wellKnownVars.apply((URI) r);
				if(varName != null) {
					vars.put(r, varName);
				}
			}
			if(varName == null) {
				// check for a varName statement
				Value nameValue = Statements.singleValue(r, SP.VAR_NAME_PROPERTY, store);
				if(nameValue instanceof Literal) {
					varName = ((Literal)nameValue).getLabel();
					if(varName != null) {
						vars.put(r, varName);
					}
				}
				else if(nameValue != null) {
					throw new MalformedSPINException("Value of "+SP.VAR_NAME_PROPERTY+" is not a literal");
				}
			}
			return varName;
		}

		private Var getVar(Value v)
			throws OpenRDFException
		{
			Var var = null;
			if(v instanceof Resource) {
				String varName = getVarName((Resource) v);
				if(varName != null) {
					var = createVar(varName);
				}
			}

			if(var == null) {
				// it must be a constant then
				var = createConstVar(v);
			}

			return var;
		}

		private Var createVar(String varName) {
			if(projElems != null) {
				ProjectionElem projElem = projElems.get(varName);
				if(projElem != null) {
					ExtensionElem extElem = projElem.getSourceExpression();
					if(extElem != null && extElem.getExpr() instanceof Var) {
						projElem.setSourceExpression(null);
					}
				}
			}
			return new Var(varName);
		}

		private Var createConstVar(Value value) {
			if (value == null) {
				throw new IllegalArgumentException("value can not be null");
			}

			String varName = getConstVarName(value);
			Var var = new Var(varName);
			var.setConstant(true);
			var.setAnonymous(true);
			var.setValue(value);
			return var;
		}

		private String getConstVarName(Value value) {
			// We use toHexString to get a more compact stringrep.
			String uniqueStringForValue = Integer.toHexString(value.stringValue().hashCode());

			if (value instanceof Literal) {
				uniqueStringForValue += "-lit";

				// we need to append datatype and/or language tag to ensure a unique
				// var name (see SES-1927)
				Literal lit = (Literal)value;
				if (lit.getDatatype() != null) {
					uniqueStringForValue += "-" + lit.getDatatype().stringValue();
				}
				if (lit.getLanguage() != null) {
					uniqueStringForValue += "-" + lit.getLanguage();
				}
			}
			else if (value instanceof BNode) {
				uniqueStringForValue += "-node";
			}
			else {
				uniqueStringForValue += "-uri";
			}
			return "_const-" + uniqueStringForValue;
		}
	}
}
