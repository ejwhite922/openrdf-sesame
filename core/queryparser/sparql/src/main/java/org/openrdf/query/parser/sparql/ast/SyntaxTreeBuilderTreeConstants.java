/* Generated By:JavaCC: Do not edit this line. SyntaxTreeBuilderTreeConstants.java Version 5.0 */
package org.openrdf.query.parser.sparql.ast;

public interface SyntaxTreeBuilderTreeConstants
{
  public int JJTUPDATESEQUENCE = 0;
  public int JJTUPDATECONTAINER = 1;
  public int JJTQUERYCONTAINER = 2;
  public int JJTVOID = 3;
  public int JJTBASEDECL = 4;
  public int JJTPREFIXDECL = 5;
  public int JJTSELECTQUERY = 6;
  public int JJTSELECT = 7;
  public int JJTPROJECTIONELEM = 8;
  public int JJTCONSTRUCTQUERY = 9;
  public int JJTCONSTRUCT = 10;
  public int JJTDESCRIBEQUERY = 11;
  public int JJTDESCRIBE = 12;
  public int JJTASKQUERY = 13;
  public int JJTDATASETCLAUSE = 14;
  public int JJTWHERECLAUSE = 15;
  public int JJTBINDINGSCLAUSE = 16;
  public int JJTBINDINGSET = 17;
  public int JJTBINDINGVALUE = 18;
  public int JJTGROUPCLAUSE = 19;
  public int JJTORDERCLAUSE = 20;
  public int JJTGROUPCONDITION = 21;
  public int JJTHAVINGCLAUSE = 22;
  public int JJTORDERCONDITION = 23;
  public int JJTLIMIT = 24;
  public int JJTOFFSET = 25;
  public int JJTGRAPHPATTERNGROUP = 26;
  public int JJTBASICGRAPHPATTERN = 27;
  public int JJTOPTIONALGRAPHPATTERN = 28;
  public int JJTGRAPHGRAPHPATTERN = 29;
  public int JJTUNIONGRAPHPATTERN = 30;
  public int JJTMINUSGRAPHPATTERN = 31;
  public int JJTSERVICEGRAPHPATTERN = 32;
  public int JJTCONSTRAINT = 33;
  public int JJTFUNCTIONCALL = 34;
  public int JJTTRIPLESSAMESUBJECT = 35;
  public int JJTPROPERTYLIST = 36;
  public int JJTOBJECTLIST = 37;
  public int JJTTRIPLESSAMESUBJECTPATH = 38;
  public int JJTPROPERTYLISTPATH = 39;
  public int JJTPATHALTERNATIVE = 40;
  public int JJTPATHSEQUENCE = 41;
  public int JJTPATHELT = 42;
  public int JJTIRI = 43;
  public int JJTPATHONEINPROPERTYSET = 44;
  public int JJTPATHMOD = 45;
  public int JJTBLANKNODEPROPERTYLIST = 46;
  public int JJTCOLLECTION = 47;
  public int JJTVAR = 48;
  public int JJTOR = 49;
  public int JJTAND = 50;
  public int JJTCOMPARE = 51;
  public int JJTINFIX = 52;
  public int JJTMATH = 53;
  public int JJTNOT = 54;
  public int JJTNUMERICLITERAL = 55;
  public int JJTCOUNT = 56;
  public int JJTSUM = 57;
  public int JJTMIN = 58;
  public int JJTMAX = 59;
  public int JJTAVG = 60;
  public int JJTSAMPLE = 61;
  public int JJTGROUPCONCAT = 62;
  public int JJTMD5 = 63;
  public int JJTSHA1 = 64;
  public int JJTSHA224 = 65;
  public int JJTSHA256 = 66;
  public int JJTSHA384 = 67;
  public int JJTSHA512 = 68;
  public int JJTNOW = 69;
  public int JJTYEAR = 70;
  public int JJTMONTH = 71;
  public int JJTDAY = 72;
  public int JJTHOURS = 73;
  public int JJTMINUTES = 74;
  public int JJTSECONDS = 75;
  public int JJTTIMEZONE = 76;
  public int JJTTZ = 77;
  public int JJTRAND = 78;
  public int JJTABS = 79;
  public int JJTCEIL = 80;
  public int JJTFLOOR = 81;
  public int JJTROUND = 82;
  public int JJTSUBSTR = 83;
  public int JJTSTRLEN = 84;
  public int JJTUPPERCASE = 85;
  public int JJTLOWERCASE = 86;
  public int JJTSTRSTARTS = 87;
  public int JJTSTRENDS = 88;
  public int JJTCONCAT = 89;
  public int JJTCONTAINS = 90;
  public int JJTENCODEFORURI = 91;
  public int JJTIF = 92;
  public int JJTIN = 93;
  public int JJTNOTIN = 94;
  public int JJTCOALESCE = 95;
  public int JJTSTR = 96;
  public int JJTLANG = 97;
  public int JJTLANGMATCHES = 98;
  public int JJTDATATYPE = 99;
  public int JJTBOUND = 100;
  public int JJTSAMETERM = 101;
  public int JJTISIRI = 102;
  public int JJTISBLANK = 103;
  public int JJTISLITERAL = 104;
  public int JJTISNUMERIC = 105;
  public int JJTBNODEFUNC = 106;
  public int JJTIRIFUNC = 107;
  public int JJTSTRDT = 108;
  public int JJTSTRLANG = 109;
  public int JJTBIND = 110;
  public int JJTREGEXEXPRESSION = 111;
  public int JJTEXISTSFUNC = 112;
  public int JJTNOTEXISTSFUNC = 113;
  public int JJTRDFLITERAL = 114;
  public int JJTTRUE = 115;
  public int JJTFALSE = 116;
  public int JJTSTRING = 117;
  public int JJTQNAME = 118;
  public int JJTBLANKNODE = 119;
  public int JJTGRAPHREFALL = 120;
  public int JJTGRAPHORDEFAULT = 121;
  public int JJTQUADSNOTTRIPLES = 122;
  public int JJTLOAD = 123;
  public int JJTCLEAR = 124;
  public int JJTDROP = 125;
  public int JJTADD = 126;
  public int JJTMOVE = 127;
  public int JJTCOPY = 128;
  public int JJTCREATE = 129;
  public int JJTINSERTDATA = 130;
  public int JJTDELETEDATA = 131;
  public int JJTDELETEWHERE = 132;
  public int JJTDELETECLAUSE = 133;
  public int JJTINSERTCLAUSE = 134;
  public int JJTMODIFY = 135;


  public String[] jjtNodeName = {
    "UpdateSequence",
    "UpdateContainer",
    "QueryContainer",
    "void",
    "BaseDecl",
    "PrefixDecl",
    "SelectQuery",
    "Select",
    "ProjectionElem",
    "ConstructQuery",
    "Construct",
    "DescribeQuery",
    "Describe",
    "AskQuery",
    "DatasetClause",
    "WhereClause",
    "BindingsClause",
    "BindingSet",
    "BindingValue",
    "GroupClause",
    "OrderClause",
    "GroupCondition",
    "HavingClause",
    "OrderCondition",
    "Limit",
    "Offset",
    "GraphPatternGroup",
    "BasicGraphPattern",
    "OptionalGraphPattern",
    "GraphGraphPattern",
    "UnionGraphPattern",
    "MinusGraphPattern",
    "ServiceGraphPattern",
    "Constraint",
    "FunctionCall",
    "TriplesSameSubject",
    "PropertyList",
    "ObjectList",
    "TriplesSameSubjectPath",
    "PropertyListPath",
    "PathAlternative",
    "PathSequence",
    "PathElt",
    "IRI",
    "PathOneInPropertySet",
    "PathMod",
    "BlankNodePropertyList",
    "Collection",
    "Var",
    "Or",
    "And",
    "Compare",
    "Infix",
    "Math",
    "Not",
    "NumericLiteral",
    "Count",
    "Sum",
    "Min",
    "Max",
    "Avg",
    "Sample",
    "GroupConcat",
    "MD5",
    "SHA1",
    "SHA224",
    "SHA256",
    "SHA384",
    "SHA512",
    "Now",
    "Year",
    "Month",
    "Day",
    "Hours",
    "Minutes",
    "Seconds",
    "Timezone",
    "Tz",
    "Rand",
    "Abs",
    "Ceil",
    "Floor",
    "Round",
    "Substr",
    "StrLen",
    "UpperCase",
    "LowerCase",
    "StrStarts",
    "StrEnds",
    "Concat",
    "Contains",
    "EncodeForURI",
    "If",
    "In",
    "NotIn",
    "Coalesce",
    "Str",
    "Lang",
    "LangMatches",
    "Datatype",
    "Bound",
    "SameTerm",
    "IsIRI",
    "IsBlank",
    "IsLiteral",
    "IsNumeric",
    "BNodeFunc",
    "IRIFunc",
    "StrDt",
    "StrLang",
    "Bind",
    "RegexExpression",
    "ExistsFunc",
    "NotExistsFunc",
    "RDFLiteral",
    "True",
    "False",
    "String",
    "QName",
    "BlankNode",
    "GraphRefAll",
    "GraphOrDefault",
    "QuadsNotTriples",
    "Load",
    "Clear",
    "Drop",
    "Add",
    "Move",
    "Copy",
    "Create",
    "InsertData",
    "DeleteData",
    "DeleteWhere",
    "DeleteClause",
    "InsertClause",
    "Modify",
  };
}
/* JavaCC - OriginalChecksum=ecff457fd2980769b7b0cd552af4f4d7 (do not edit this line) */
