header {
package com.beanit.asn1bean.compiler.parser;

import com.beanit.asn1bean.compiler.model.*;
import java.math.*;
import java.util.*;
}

//	Creation of ASN.1 grammar for ANTLR	V2.7.7
// ===================================================
//		  TOKENS FOR ASN.1 LEXER DEFINITIONS
// ===================================================

class ASNLexer extends Lexer;
options	{
	k =	11;
	exportVocab=ASN;
	charVocabulary = '\3'..'\377';
	caseSensitive=true;
	testLiterals = true;
	codeGenMakeSwitchThreshold = 2;  // Some optimizations
	codeGenBitsetTestThreshold = 3;
}

//	ASN1 Tokens 

tokens {
	
ABSENT_KW				=	"ABSENT"			;
ALL_KW					=	"ALL"				;
ANY_KW					=	"ANY"				;
APPLICATION_KW			=	"APPLICATION"		;
AUTOMATIC_KW			=	"AUTOMATIC"			;
BASED_NUM_KW			=	"BASEDNUM"			;
BEGIN_KW				=	"BEGIN"				;
BIT_KW					=	"BIT"				;
BMP_STR_KW			=	"BMPString"			;
BOOLEAN_KW				=	"BOOLEAN"			;
BY_KW					=	"BY"				;
CHARACTER_KW			=	"CHARACTER"			;
CHOICE_KW				=	"CHOICE"			;
CLASS_KW				=	"CLASS"				;
COMPONENTS_KW			=	"COMPONENTS"		;
COMPONENT_KW			=	"COMPONENT"			;
CONSTRAINED_KW			=	"CONSTRAINED"		;
DEFAULT_KW				=	"DEFAULT"			;
DEFINED_KW				=	"DEFINED"			;
DEFINITIONS_KW			=	"DEFINITIONS"		;
EMBEDDED_KW				=	"EMBEDDED"			;
END_KW					=	"END"				;
ENUMERATED_KW			=	"ENUMERATED"		;
EXCEPT_KW				=	"EXCEPT"			;
EXPLICIT_KW				=	"EXPLICIT"			;
EXPORTS_KW				=	"EXPORTS"			;
EXTENSIBILITY_KW		=	"EXTENSIBILITY"		;
EXTERNAL_KW				=	"EXTERNAL"			;
FALSE_KW				=	"FALSE"				;
FROM_KW					=	"FROM"				;
GENERALIZED_TIME_KW		=	"GeneralizedTime"	;
GENERAL_STR_KW			=	"GeneralString"		;
GRAPHIC_STR_KW			=	"GraphicString"		;
IA5_STRING_KW			=	"IA5String"			;
IA5_STRING_UPPER_KW			=	"IA5STRING"			;
IDENTIFIER_KW			=	"IDENTIFIER"		;
IMPLICIT_KW				=	"IMPLICIT"			;
IMPLIED_KW				=	"IMPLIED"			;
IMPORTS_KW				=	"IMPORTS"			;
INCLUDES_KW				=	"INCLUDES"			;
INSTANCE_KW				=	"INSTANCE"			;
INTEGER_KW				=	"INTEGER"			;
INTERSECTION_KW			=	"INTERSECTION"		;
ISO646_STR_KW			=	"ISO646String"		;
LINKED_KW				=	"LINKED"			;
MAX_KW					=	"MAX"				;
MINUS_INFINITY_KW		=	"MINUSINFINITY"		;
MIN_KW					=	"MIN"				;
NULL_KW					=	"NULL"				;
NUMERIC_STR_KW			=	"NumericString"		;
OBJECT_DESCRIPTOR_KW 	=	"ObjectDescriptor"	;
OBJECT_KW				=	"OBJECT"			;
OCTET_KW				=	"OCTET"				;
OF_KW					=	"OF"				;
OID_KW					=	"OID"				;
OPTIONAL_KW				=	"OPTIONAL"			;
PARAMETER_KW			=	"PARAMETER"			;
PDV_KW					=	"PDV"				;
PLUS_INFINITY_KW		=	"PLUSINFINITY"		;
PRESENT_KW				=	"PRESENT"			;
PRINTABLE_STR_KW		=	"PrintableString"	;
PRIVATE_KW				=	"PRIVATE"			;
REAL_KW					=	"REAL"				;
RELATIVE_KW				=	"RELATIVE"			;
RESULT_KW				=	"RESULT"			;
SEQUENCE_KW				=	"SEQUENCE"			;
SET_KW					=	"SET"				;
SIZE_KW					=	"SIZE"				;
STRING_KW				=	"STRING"			;
SYNTAX_KW               =   "SYNTAX"            ;
TAGS_KW					=	"TAGS"				;
TELETEX_STR_KW			=	"TeletexString"		;
TRUE_KW					=	"TRUE"				;
T61_STR_KW              =   "T61String"         ;
UNION_KW				=	"UNION"				;
UNIQUE_KW				=	"UNIQUE"			;
UNIVERSAL_KW			=	"UNIVERSAL"			;
UNIVERSAL_STR_KW		=	"UniversalString"	;
UTC_TIME_KW				=	"UTCTime"			;
UTF8_STR_KW             =	"UTF8String"		;
VIDEOTEX_STR_KW			=	"VideotexString"	;
VISIBLE_STR_KW			=	"VisibleString"		;
WITH_KW					=	"WITH"				;
TIME_KW                 =   "TIME"              ;
DATE_KW                    =   "DATE"              ;
TIME_OF_DAY_KW          =   "TIME-OF-DAY"       ;
DATE_TIME_KW            =   "DATE-TIME"         ;
DURATION_KW             =   "DURATION"          ;

}

// Operators

ASSIGN_OP			:	"::="	;
BAR					:	'|'		;
COLON				:	':'		;
COMMA				:	','		;
COMMENT				:	"--"	;
DOT					:	'.'		;
AMPERSAND			:	'&'		;
DOTDOT				:	".."	;
ELLIPSIS			:	"..."	;
EXCLAMATION			:	'!'		;
INTERSECTION		:	'^'		;
LESS				:	'<'		;
L_BRACE				:	'{'		;
L_BRACKET			:	'['		;
L_PAREN				:	'('		;
protected MINUS				:	'-'		;
PLUS				:	'+'		;
R_BRACE				:	'}'		;
R_BRACKET			:	']'		;
R_PAREN				:	')'		;
SEMI				:	';'		;
SINGLE_QUOTE		:	"'"		;
CHARB				:	"'B"	;
CHARH				:	"'H"	;
AT_SIGN             :  '@'      ;

// Whitespace -- ignored

WS			
	:	(	' ' | '\t' | '\f'	|	(	options {generateAmbigWarnings=false;}
	:	"\r\n"		{ newline(); }// DOS
	|	'\r'   		{ newline(); }// Macintosh
	|	'\n'		{ newline(); }// Unix 
	))+
	{$setType(Token.SKIP); }
	;

// Single-line comments
SL_COMMENT
	: (options {warnWhenFollowAmbig=false;} 
	: COMMENT (  { LA(2)!='-' }? '-' 	|	~('-'|'\n'|'\r'))*	( (('\r')? '\n') { newline(); }| COMMENT) )
		{$setType(Token.SKIP);  }
	;

// multi-line comments
ML_COMMENT
	: "/*" (('*' ~'/') | (('\r')? '\n') { newline(); } | ~'*')* "*/"
		{$setType(Token.SKIP);  }
	;

//WITH_SYNTAX_CONTENT
//	: L_BRACE ((~'}')*);


protected DIGIT  : ('0'..'9');

protected NUMBER		 :		 (DIGIT)+ ;

protected NEG_NUMBER : MINUS NUMBER ;

protected EXPONENT : ('e' | 'E') ('-' | '+')?   NUMBER;

protected SCIENTIFIC_NUMBER
		 : (MINUS)? NUMBER '.' NUMBER (EXPONENT)?
		 | (MINUS)? NUMBER EXPONENT ;
		 
NUMBER_OR_SCIENTIFIC_NUMBER
	:	( (MINUS)? NUMBER ".." )		=> NUMBER						{ $setType(NUMBER); }
	|	( SCIENTIFIC_NUMBER )			=> SCIENTIFIC_NUMBER			{ $setType(SCIENTIFIC_NUMBER); }
	|   ( MINUS NUMBER)					=> NEG_NUMBER					{ $setType(NEG_NUMBER); } 
	|   NUMBER 															{ $setType(NUMBER); }							

 	;
 
UPPER	
options {testLiterals = false;}
	:   ('A'..'Z') 
		(options {warnWhenFollowAmbig = false;}
	:	( 'a'..'z' | 'A'..'Z' |'-' | '0'..'9'|'_' ))* 	;

LOWER
options {testLiterals = false;}
	:	('a'..'z') 
		(options {warnWhenFollowAmbig = false;}
	:	( 'a'..'z' | 'A'..'Z' |'-' | '0'..'9'|'_' ))* 	;


protected
BDIG		: ('0'|'1') ;
protected
HDIG		:	(options {warnWhenFollowAmbig = false;} :('0'..'9') )
			|	('A'..'F')
			|	('a'..'f')
			;

// Unable to resolve a string like 010101 followed by 'H
//B_STRING 	: 	SINGLE_QUOTE ({LA(3)!='B'}? BDIG)+  BDIG SINGLE_QUOTE 'B' 	;
//H_STRING 	: 	SINGLE_QUOTE ({LA(3)!='H'}? HDIG)+  HDIG SINGLE_QUOTE 'H'  ;

B_OR_H_STRING
	:	(options {warnWhenFollowAmbig = false;} 
		:(B_STRING)=>B_STRING {$setType(B_STRING);}
		| H_STRING {$setType(H_STRING);})
	;

protected
B_STRING 	: 	SINGLE_QUOTE (BDIG)+ SINGLE_QUOTE 'B' 	;
protected
H_STRING 	: 	SINGLE_QUOTE (HDIG)+ SINGLE_QUOTE 'H'  ;

			
//C_STRING 	: 	'"'	(UPPER | LOWER )*  '"' ;

C_STRING	:	'"' (~'"')* '"'	;



//*************************************************************************
//**********		PARSER DEFINITIONS
//*************************************************************************


class ASNParser	extends	Parser;
options	{
	exportVocab=ASN;
	k=3;
}

// Grammar Definitions


module_definitions[AsnModel model] 
{
    AsnModule module;
}
	:	(module = module_definition { model.modulesByName.put(module.moduleIdentifier.name, module); })+ 
	;



module_definition returns [AsnModule module] 	
{
    module = new AsnModule();
	AsnModuleIdentifier mid;
	String s ;	
}
	:	(mid = module_identifier
		{ module.moduleIdentifier = mid; 	})
		DEFINITIONS_KW 
		(( e:EXPLICIT_KW {module.tagDefault = AsnModule.TagDefault.EXPLICIT;}
		  |i:IMPLICIT_KW {module.tagDefault = AsnModule.TagDefault.IMPLICIT;}
		  |a:AUTOMATIC_KW {module.tagDefault = AsnModule.TagDefault.AUTOMATIC;}
		 ) TAGS_KW {module.tag = true;} |) 
		(EXTENSIBILITY_KW IMPLIED_KW {module.extensible=true;} | )
		ASSIGN_OP 
		BEGIN_KW 
		module_body[module] 
		END_KW
	; 

module_identifier  returns [ AsnModuleIdentifier mid ]
{mid = new AsnModuleIdentifier();
AsnOidComponentList cmplst; }
	:	(( md:UPPER { mid.name = md.getText();}) 
		 ((cmplst = obj_id_comp_lst { mid.componentList = cmplst; })|) 
		)
	;

obj_id_comp_lst	returns [AsnOidComponentList oidcmplst]
{oidcmplst = new AsnOidComponentList();
AsnOidComponent oidcmp; AsnDefinedValue defval; }
	:	L_BRACE 
		(defval = defined_value {oidcmplst.isDefinitive=true;oidcmplst.defval=defval;})?
		(oidcmp = obj_id_component {oidcmplst.components.add(oidcmp);})+  
		R_BRACE 
	;

obj_id_component returns [AsnOidComponent oidcmp ]
{oidcmp = new AsnOidComponent(); AsnDefinedValue defval;
String s,n =""; }
	: 	((num:NUMBER {s=num.getText();oidcmp.num = new Integer(s); oidcmp.numberForm=true;})
	|	(LOWER (L_PAREN NUMBER R_PAREN)?)=>((lid:LOWER {oidcmp.name = lid.getText();oidcmp.nameForm=true;}) 
		( L_PAREN 
		 (num1:NUMBER {n=num1.getText(); oidcmp.num = new Integer(n);oidcmp.nameAndNumberForm=true;})
		R_PAREN ) ? )
	|	(defined_value)=>(defval = defined_value {oidcmp.isDefinedValue=true;oidcmp.defval=defval;}))
	;
		
tag_default returns [String s]
{ s = ""; }
	:	(tg:EXPLICIT_KW   {s = tg.getText();})
	|	(tg1:IMPLICIT_KW  {s = tg1.getText();})
	|	(tg2:AUTOMATIC_KW {s = tg2.getText();})
	;
		
module_body[AsnModule module] 
//	:	(exports[module])? (imports[module])? (assignment[module])+
	:	(exports[module]|) (imports[module]|) ((assignment[module])+ |)
	;

exports[AsnModule module]		
{String s; ArrayList syml = new ArrayList();}
//	:	(EXPORTS_KW {module.exported=true;})   (s = symbol { module.exportList.add(s) ; })
//		(COMMA (s = symbol {module.exportList.add(s) ;} ) )*  SEMI
//	;
	:	EXPORTS_KW {module.exported=true;} 
		((syml = symbol_list {module.exportSymbolList = syml;} |)
		|ALL_KW) 
		SEMI
	;	
		
imports[AsnModule module]
	:	(IMPORTS_KW (((symbols_from_module[module])+)|)  SEMI)
		{module.imported=true;}
	;

symbols_from_module[AsnModule module]
{SymbolsFromModule sym = new SymbolsFromModule();
String s = "" ; AsnModuleIdentifier mid; AsnDefinedValue defval;
ArrayList arl; AsnOidComponentList cmplist;}

	:	((arl= symbol_list {sym.symbolList = arl;}) FROM_KW	
	    (up:UPPER {sym.modref = up.getText();}
	     (cmplist = obj_id_comp_lst {sym.isOidValue=true;sym.cmplist = cmplist;}
	     |(defined_value)=>(defval = defined_value {sym.isDefinedValue=true;sym.defval=defval;})|)))
	    {module.importSymbolFromModuleList.add(sym);}
	;
	
symbol_list returns[ArrayList symlist]
{symlist = new ArrayList(); String s=""; }
	:	((s = symbol {symlist.add(s); })(L_BRACE R_BRACE)?
		(COMMA (s = symbol {symlist.add(s); })(L_BRACE R_BRACE)?)*) 
	; 

symbol returns [String s]
{s="";}			
	:	up:UPPER  { s = up.getText();}
	|	lid:LOWER { s = lid.getText();}
	;


assignment[AsnModule module]	
{Object obj ; Object objv; AsnValueAssignment valueAssignment; AsnInformationObjectClass asnInformationObjectClass; List<AsnParameter> parameterListVal;} :
    // Type Assignment Definition
        (up:UPPER  ASSIGN_OP	(obj=type) 
            {
                ((AsnType)obj).name = up.getText();
                module.typesByName.put(((AsnType)obj).name,(AsnType)obj);
            }
        )

	|
        (up2:UPPER (parameterListVal = parameterList)  ASSIGN_OP	(obj=type) 
            {
                ((AsnType)obj).name = up2.getText();
                ((AsnType)obj).parameters = parameterListVal;
                module.typesByName.put(((AsnType)obj).name,(AsnType)obj);
            }
        )

        // Value Assignment definition	
	|   valueAssignment = valueAssignment
            {
                module.asnValueAssignmentsByName.put(valueAssignment.name, valueAssignment);
            }
        // Object Class
    |   asnInformationObjectClass = informationObjectClass
            {
                module.objectClassesByName.put(asnInformationObjectClass.name,asnInformationObjectClass);
            }

    | UPPER UPPER ASSIGN_OP constraint3 //L_BRACE L_BRACE syntaxTokens R_BRACE (BAR L_BRACE syntaxTokens R_BRACE)* R_BRACE

        // ***************************************************
        // Define the following
        // ***************************************************
        //	|XMLValueAssignment 
        //	|ValueSetTypeAssignment 
        //	|ObjectClassAssignment 
        //	|ObjectAssignment 
        //	|ObjectSetAssignment 
        //	|ParameterizedAssignment
	;


valueAssignment returns [AsnValueAssignment valueAssignment]
{Object valueType; AsnValue value; valueAssignment = new AsnValueAssignment();}
	:
        lid:LOWER (valueType = type ) ASSIGN_OP (value = value2) 
        {
            valueAssignment.name=lid.getText();
            valueAssignment.type = valueType;
            valueAssignment.value = value;
        }
    ;


informationObjectClass returns [AsnInformationObjectClass informationObjectClass]
{informationObjectClass = new AsnInformationObjectClass(); List<AsnElementType> elementList; List<String> syntaxTokens;}
	:  ( up:UPPER {informationObjectClass.name = up.getText();} ASSIGN_OP CLASS_KW
       L_BRACE 
	   (elementList = objectClassElements {informationObjectClass.elementList=elementList;})? 
	    R_BRACE (WITH_KW SYNTAX_KW constraint3)?
    );

syntaxTokens returns [List<String> syntaxTokens]
{ syntaxTokens = new ArrayList<String>(); } :
       ( up:UPPER {syntaxTokens.add(up.getText());} |
         lo:LOWER {syntaxTokens.add(lo.getText());} |
         COMMA {syntaxTokens.add(",");} |
         AMPERSAND {syntaxTokens.add("&");} |
         AT_SIGN {syntaxTokens.add("@");} |
        C_STRING |
        DOT |
        NULL_KW |
         BY_KW {syntaxTokens.add("BY");}  | num:NUMBER {syntaxTokens.add(num.getText());} | L_PAREN {syntaxTokens.add("(");} | R_PAREN {syntaxTokens.add(")");} )*
    ;

objectClassElements returns [List<AsnElementType> elelist]
{elelist = new ArrayList<>(); AsnElementType eletyp; int i=1; }
	:	(ELLIPSIS | eletyp = objectClassElement {if (eletyp.name.isEmpty()) {eletyp.name = "element" + i;};elelist.add(eletyp);i++; }
	    (COMMA (ELLIPSIS | (eletyp = objectClassElement {if (eletyp.name.isEmpty()) {eletyp.name = "element" + i;};elelist.add(eletyp);i++; })))*)
	;

objectClassElement returns [AsnElementType eletyp]
{eletyp = new AsnElementType();AsnValue val; 
AsnType obj; AsnTag tg; String s;}
	: (((	(AMPERSAND(lid:LOWER {eletyp.name = lid.getText();}) 
        	(obj = type)
            (UNIQUE_KW)?
            ( (OPTIONAL_KW {eletyp.isOptional=true;})
		| (DEFAULT_KW { eletyp.isDefault = true;} 
		 val = value {eletyp.value = val;} ))? )
	)
		{
			if((AsnDefinedType.class).isInstance(obj)){
				eletyp.isDefinedType=true;
				eletyp.definedType = (AsnDefinedType)obj ; 
			} else{		
				eletyp.typeReference = obj;
			}
		}
        ) |
        (AMPERSAND(up:UPPER {eletyp.name = up.getText(); eletyp.typeReference = new AsnAny(); })
        (OPTIONAL_KW {eletyp.isOptional=true;})?)
    )
            
	;



type returns [AsnType obj]		
{obj = null;}
	:	(obj = built_in_type)
	|	(obj = defined_type)		// Referenced Type
	|	(obj = selection_type ) 		// Referenced Type
	;

built_in_type returns [AsnType obj]
{obj = null;}
	:	(obj = any_type)
	|	(obj = bit_string_type)
	|	(obj = boolean_type )
	|	(obj = character_str_type)
	|	(obj = choice_type)
	|	(obj = embedded_type)		
	|	(obj = enum_type) 
	|	(obj = external_type) 
	|	(obj = integer_type )
	|	(obj = null_type )
	|	(obj = object_identifier_type)
	|	(obj = octetString_type)
	|	(obj = real_type )
	|	(obj = relativeOid_type)
	|	(obj = sequence_type)
	|	(obj = sequenceof_type)
	|	(obj = set_type)
	|	(obj = setof_type)
	|	(obj = tagged_type)
	;
			
any_type returns [AsnAny an]
{an = new AsnAny();}
	: (	ANY_KW  ( DEFINED_KW BY_KW {an.isDefinedBy = true ;} lid:LOWER { an.definedByType = lid.getText();})? )
	;

bit_string_type	returns [AsnBitString obj]
{AsnBitString bstr = new AsnBitString(); 
AsnNamedNumberList nnlst ; AsnConstraint cnstrnt;obj = null;}
	:	(BIT_KW STRING_KW (nnlst =namedNumber_list { bstr.namedNumberList = nnlst;})? 
		(cnstrnt = constraint { bstr.constraint = cnstrnt;} )? )
		{obj=bstr; nnlst = null ; cnstrnt = null;}
	;

// Includes Useful types as well
character_str_type returns [AsnCharacterString obj]
{AsnCharacterString cstr = new AsnCharacterString();
String s ; AsnConstraint cnstrnt; obj = null;}
	:	((CHARACTER_KW STRING_KW {cstr.isUCSType = true;})
	|	(s = character_set {cstr.stringtype = s;} 
		(cnstrnt = constraint{cstr.constraint = cnstrnt;})? ))
		{obj = cstr; cnstrnt = null; cstr = null;}
	;
		
character_set returns [String s]
{s = "";}
	:	(s1:BMP_STR_KW 			{s = s1.getText();})
	|	(s2:GENERALIZED_TIME_KW	{s = s2.getText();})
	|	(s3:GENERAL_STR_KW		{s = s3.getText();})
	|	(s4:GRAPHIC_STR_KW		{s = s4.getText();})
	|	(s5:IA5_STRING_KW		{s = s5.getText();})
	| (IA5_STRING_UPPER_KW  {s = "IA5String";})
	|	(s6:ISO646_STR_KW		{s = s6.getText();})
	|	(s7:NUMERIC_STR_KW		{s = s7.getText();})
	|	(s8:PRINTABLE_STR_KW	{s = s8.getText();})
	|	(s9:TELETEX_STR_KW		{s = s9.getText();})
	|	(s10:T61_STR_KW			{s = s10.getText();})
	|	(s11:UNIVERSAL_STR_KW	{s = s11.getText();})
	|	(s12:UTF8_STR_KW		{s = s12.getText();})
	|	(s13:UTC_TIME_KW		{s = "UtcTime";})
	|	(s14:VIDEOTEX_STR_KW	{s = s14.getText();})
	|	(s15:VISIBLE_STR_KW		{s = s15.getText();})
  	|	(s16:OBJECT_DESCRIPTOR_KW {s = s16.getText();})
	|	(TIME_KW        		{s = "Time";})
	|	(DATE_KW		        {s = "Date";})
	|	(TIME_OF_DAY_KW		    {s = "TimeOfDay";})
	|	(DATE_TIME_KW		    {s = "DateTime";})
	|	(DURATION_KW		    {s = "Duration";})
	;

boolean_type returns [AsnBoolean obj]
{obj = null;}
	: BOOLEAN_KW 
	  {obj = new AsnBoolean();}
	;
				
choice_type	returns [AsnChoice obj]
{AsnChoice ch = new AsnChoice(); List<AsnElementType> eltplst; 
obj = null;}
	: (	CHOICE_KW L_BRACE (eltplst = elementType_list {ch.componentTypes = eltplst ;}) R_BRACE ) 
		{obj = ch; eltplst = null; ch = null;}
	;

embedded_type returns [AsnEmbeddedPdv obj]
{obj = null;}
	:	(EMBEDDED_KW  PDV_KW)
		{obj = new AsnEmbeddedPdv();}
	;
enum_type returns [AsnEnum obj]
{AsnEnum enumtyp = new AsnEnum() ;
AsnNamedNumberList nnlst; obj = null;}
	: ( ENUMERATED_KW (nnlst = namedNumber_list { enumtyp.namedNumberList = nnlst;}) )
	  {obj = enumtyp ; enumtyp=null;}
	;
		
external_type returns [AsnType obj]
{obj = null; }
	: EXTERNAL_KW {obj = new AsnExternal();}
	;

integer_type returns [AsnType obj]	
{AsnInteger intgr = new AsnInteger();
AsnNamedNumberList numlst; AsnConstraint cnstrnt; obj=null;}
	: (	INTEGER_KW (numlst = namedNumber_list {intgr.namedNumberList = numlst;})?
            (cnstrnt = constraint {intgr.constraint = cnstrnt;})? )
		{obj = intgr ; numlst = null ; cnstrnt = null; intgr = null; }
	;
		
null_type returns [AsnType obj]
{AsnNull nll = new AsnNull(); obj = null;}
	: NULL_KW
	  {obj = nll; nll = null ; }
	;

object_identifier_type returns [AsnType obj]
{AsnObjectIdentifier objident = new AsnObjectIdentifier(); obj = null;}
	: OBJECT_KW IDENTIFIER_KW 
	  {obj = objident; objident = null;}	
	; 
	
octetString_type returns [AsnType obj]
{AsnOctetString oct = new AsnOctetString();
AsnConstraint cnstrnt ; obj = null;}
	: (	OCTET_KW STRING_KW (cnstrnt = constraint{oct.constraint = cnstrnt;})? )
		{obj = oct ; cnstrnt = null;}
	;

real_type returns [AsnType obj]
{AsnReal rl = new AsnReal(); AsnConstraint cnstrnt; obj = null;}
	: ( REAL_KW  
			(cnstrnt = constraint {rl.constraint = cnstrnt;})? )
		{obj = rl ; rl = null;}	
	;

relativeOid_type returns [AsnType obj]
{obj = null; }
	: RELATIVE_KW MINUS OID_KW {obj = new AsnRelativeOid();}
	;
		
sequence_type returns [AsnType obj]
{AsnSequenceSet seq = new AsnSequenceSet();
List<AsnElementType> eltplist ; AsnConstraint cnstrnt ; obj = null;}
	:  ( SEQUENCE_KW {seq.isSequence = true;} 
	    L_BRACE 
	   (eltplist = elementType_list {seq.componentTypes = eltplist;})? 
	    R_BRACE )
		{obj = seq ; eltplist = null; seq =null; }
	;
	
		
	
sequenceof_type returns [AsnSequenceOf obj]
{AsnSequenceOf seqof = new AsnSequenceOf();
AsnConstraint cnstrnt; obj = null; AsnElementType referencedAsnType ; String s ;}
	:  ( SEQUENCE_KW {seqof.isSequenceOf = true;}
	         (cnstrnt = constraint{seqof.constraint = cnstrnt;})? OF_KW 
		( referencedAsnType = sequenceof_component 
		{
                seqof.componentType = referencedAsnType;
		}) )
		{obj = seqof;  cnstrnt = null; seqof=null;}		
	;


sequenceof_component	returns [AsnElementType eletyp]
{eletyp = new AsnElementType();AsnValue val; 
AsnType obj; AsnTag tg; String s;}
	: (	(lid:LOWER {eletyp.name = lid.getText();})? 
		(tg = tag { eletyp.tag = tg ;})? 
		(s = tag_default {eletyp.tagType = s ;})? 
		(obj = type) )
		{
			if((AsnDefinedType.class).isInstance(obj)){
				eletyp.isDefinedType=true;
				eletyp.definedType = (AsnDefinedType)obj; 
			} else{		
				eletyp.typeReference = obj;
			}
		}
	;



set_type returns [AsnType obj]
{AsnSequenceSet set = new AsnSequenceSet();
List<AsnElementType> eltplist ;obj = null;}
	:  ( SET_KW L_BRACE (eltplist =  elementType_list {set.componentTypes = eltplist ;})? R_BRACE )
		{obj = set ; eltplist = null; set = null;}
	;
		
setof_type	returns [AsnType obj]
{AsnSequenceOf setof = new AsnSequenceOf(); setof.componentType = new AsnElementType();
AsnConstraint cns; obj = null;
Object obj1 ; String s;}
	:	(SET_KW	
	(cns = constraint {setof.constraint = cns ;})? OF_KW 
		(obj1 = type 
		{	if((AsnDefinedType.class).isInstance(obj1)){
		  		setof.componentType.isDefinedType=true;
				setof.componentType.definedType = (AsnDefinedType)obj1; 
			}
			else{
				setof.componentType.typeReference = (AsnType) obj1;
			} 
		}) )
		{obj = setof; cns = null; obj1=null; setof=null;} 		
	;

tagged_type returns [AsnType obj]
{AsnTaggedType tgtyp = new AsnTaggedType();
AsnTag tg; AsnType obj1 = null; String s; obj = null;}
	:	((tg = tag {tgtyp.tag = tg ;}) 
		(s = tag_default { tgtyp.tagType = s ;})? 
		(obj1 = type 
		{	if((AsnDefinedType.class).isInstance(obj1)){
		  		tgtyp.isDefinedType=true;
                tgtyp.definedType=(AsnDefinedType)obj1;
			}
			else{	
				tgtyp.typeReference = obj1; 
			} 
		}))
		{obj = tgtyp ; tg = null; obj1= null ;tgtyp = null; }
	;


tag	returns [AsnTag tg]	
{tg = new AsnTag(); String s; AsnClassNumber cnum;} 
	:	(L_BRACKET (s = clazz {tg.clazz = s ;})? (cnum = class_NUMBER { tg.classNumber = cnum ;}) R_BRACKET )
	;
	
clazz returns [String s]
{s = ""; }			
	:	(c1:UNIVERSAL_KW 	{s= c1.getText();})
	|	(c2:APPLICATION_KW	{s= c2.getText();})
	|	(c3:PRIVATE_KW		{s= c3.getText();})
	;

class_NUMBER returns [AsnClassNumber cnum]		
{cnum = new AsnClassNumber() ; String s; }
	:	((num:NUMBER {s=num.getText(); cnum.num = new Integer(s);})
	|	(lid:LOWER  {s=lid.getText(); cnum.name = s ;}) )		
	;

defined_type returns [AsnType obj]	
{AsnDefinedType deftype = new AsnDefinedType();
AsnConstraint cnstrnt; obj = null;}
	:	((up:UPPER {deftype.moduleOrObjectClassReference = up.getText();} 
			DOT )? ( AMPERSAND { deftype.isObjectClassField = true; } )? 
		((up1:UPPER {deftype.typeName = up1.getText();}) | (lo:LOWER {deftype.typeName = lo.getText();}))
		(cnstrnt = constraint{deftype.constraint = cnstrnt;})? )
		{obj = deftype; deftype=null ; cnstrnt = null;}
	;

selection_type returns [AsnType obj]
{AsnSelectionType seltype = new AsnSelectionType();
obj = null;Object obj1;}
	:	((lid:LOWER { seltype.selectionID = lid.getText();})
	 	LESS
	 	(obj1=type {seltype.type = obj1;}))
	 	{obj=seltype; seltype=null;}
	;



typeorvaluelist[ObjectType objtype]
{Object obj; }
	: ((obj = typeorvalue {objtype.elements.add(obj);})
	   (COMMA (obj=typeorvalue {objtype.elements.add(obj);})* ))
	;

typeorvalue returns [Object obj]
{Object obj1; obj=null;}
	: ((type)=>(obj1 = type) | obj1 = value)
	  {obj = obj1; obj1=null;}
	;

elementType_list returns [List<AsnElementType> elelist]
{elelist = new ArrayList<>(); AsnElementType eletyp; int i=1; }
	:	((ELLIPSIS | eletyp = elementType {if (eletyp.name.isEmpty()) {eletyp.name = "element" + i;};elelist.add(eletyp);i++; })
	    (COMMA (ELLIPSIS | (eletyp = elementType {if (eletyp.name.isEmpty()) {eletyp.name = "element" + i;};elelist.add(eletyp);i++; })))*)
	;

elementType	returns [AsnElementType eletyp]
{eletyp = new AsnElementType();AsnValue val; 
AsnType obj; AsnTag tg; String s;}
	: (	((lid:LOWER {eletyp.name = lid.getText();})? 
		(tg = tag { eletyp.tag = tg ;})? 
		(s = tag_default {eletyp.tagType = s ;})? 
		(obj = type) ( (OPTIONAL_KW {eletyp.isOptional=true;}) 
		| (DEFAULT_KW { eletyp.isDefault = true;} 
		 val = value {eletyp.value = val;} ))? )
	|	COMPONENTS_KW OF_KW {eletyp.isComponentsOf = true;}(obj = type ))
		{
			if((AsnDefinedType.class).isInstance(obj)){
				eletyp.isDefinedType=true;
				eletyp.definedType = (AsnDefinedType)obj; 
			} else{		
				eletyp.typeReference = obj;
			}
		}
	;
		
namedNumber_list returns [AsnNamedNumberList nnlist]
{nnlist = new AsnNamedNumberList();AsnNamedNumber nnum ; }	
	: (	L_BRACE (ELLIPSIS | nnum= namedNumber {nnlist.namedNumbers.add(nnum); })
	   (COMMA ( ELLIPSIS | (nnum = namedNumber  {nnlist.namedNumbers.add(nnum); }) ))*  R_BRACE )
	;


namedNumber	returns [AsnNamedNumber nnum]
{nnum = new AsnNamedNumber() ;AsnSignedNumber i; 
AsnDefinedValue s;	}
	:	(lid:LOWER {nnum.name = lid.getText();} (L_PAREN 
		(i = signed_number {nnum.signedNumber = i;nnum.isSignedNumber=true;}
		| (s = defined_value {nnum.definedValue=s;})) R_PAREN)?	)
	;
	
constraint returns [AsnConstraint cnstrnt]
{cnstrnt=new AsnConstraint();} :
        (SIZE_KW { cnstrnt.tokens.add("SIZE");})?
        (cnstrnt=constraint2 |
        cnstrnt=constraint3)*
    ;

constraint2 returns [AsnConstraint cnstrnt]
{cnstrnt=new AsnConstraint(); ConstraintElements cnsElem;} :
        L_PAREN
        	cnsElem = constraint_elements {cnstrnt.cnsElem = cnsElem; }
//        (
//            cnstrnt=constraint2 |
//           ~R_PAREN )*
        R_PAREN
    ;    

constraint3 returns [AsnConstraint cnstrnt]
{cnstrnt=new AsnConstraint();} :
        L_BRACE 
        (
            cnstrnt=constraint3 |
            ~R_BRACE )*
        R_BRACE
    ;    


parameterList returns [List<AsnParameter> parameters]
{parameters = new ArrayList<AsnParameter>(); AsnParameter parameterVal;} :
        L_BRACE
        parameterVal = parameter {parameters.add(parameterVal);}
        (COMMA parameterVal = parameter {parameters.add(parameterVal);})*
        R_BRACE
;

parameter returns [AsnParameter parameter]
{parameter = new AsnParameter();} :
        (((INTEGER_KW{parameter.paramGovernor = "integer";}) | (up:UPPER {parameter.paramGovernor = up.getText();}) | (lo:LOWER {parameter.paramGovernor = lo.getText();})) COLON)?
        ((up2:UPPER {parameter.dummyReference = up2.getText();}) | (lo2:LOWER {parameter.dummyReference = lo2.getText();}))
;



exception_spec[AsnConstraint cnstrnt]
{AsnSignedNumber signum; AsnDefinedValue defval;
Object typ;AsnValue val;}
	: (EXCLAMATION 
	  ( (signed_number)=>(signum=signed_number {cnstrnt.isSignedNumber=true;cnstrnt.signedNumber=signum;})
	   |(defined_value)=>(defval=defined_value {cnstrnt.isDefinedValue=true;cnstrnt.definedValue=defval;})
	   |typ=type COLON val=value {cnstrnt.isColonValue=true;cnstrnt.type=typ;cnstrnt.value=val;}))
	   {cnstrnt.isExceptionSpec=true;}
	;

element_set_specs[AsnConstraint cnstrnt]
{ElementSetSpec elemspec;}
	:	(elemspec=element_set_spec { 
				cnstrnt.elemSetSpec=elemspec; // TODO - need list.add() func
		}
		(COMMA ELLIPSIS {cnstrnt.isCommaDotDot=true;})? 
		(COMMA elemspec=element_set_spec {cnstrnt.addElemSetSpec=elemspec;cnstrnt.isAdditionalElementSpec=true;})?)
	;

element_set_spec returns [ElementSetSpec elemspec]	
{elemspec = new ElementSetSpec(); Intersection intersect;ConstraintElements cnselem;}
	:	intersect=intersections {elemspec.intersectionList.add(intersect);}
		((BAR | UNION_KW ) intersect=intersections {elemspec.intersectionList.add(intersect);})*
	| ALL_KW EXCEPT_KW cnselem=constraint_elements  {elemspec.allExceptCnselem=cnselem;elemspec.isAllExcept=true;}
	;

// Coding is not proper for EXCEPT constraint elements. 
// One EXCEPT constraint elements should be tied to one Constraint elements
//(an object of constraint and except list)
// and not in one single list
intersections returns [Intersection intersect]
{intersect = new Intersection();ConstraintElements cnselem;}

	:	cnselem=constraint_elements {intersect.cnsElemList.add(cnselem);}
	   (EXCEPT {intersect.isExcept=true;} cnselem=constraint_elements {intersect.exceptCnsElem.add(cnselem);})? 
	   ((INTERSECTION | INTERSECTION_KW) {intersect.isInterSection=true;}
	   cnselem=constraint_elements {intersect.cnsElemList.add(cnselem);}
	   (EXCEPT cnselem=constraint_elements {intersect.exceptCnsElem.add(cnselem);})?)*
	;
				
constraint_elements	returns [ConstraintElements cnsElem]
{ cnsElem = new ConstraintElements(); AsnValue val;
AsnConstraint cns; ElementSetSpec elespec;Object typ; }
	:   (val = value2 {cnsElem.isValue=true;cnsElem.values.add(val);} ((BAR)? (value2) {cnsElem.values.add(val);} )*  ) 
    |   (((value_range[cnsElem])=>(value_range[cnsElem] {cnsElem.isValueRange=true;})) (COMMA ELLIPSIS)?)
	|	(SIZE_KW cns=constraint {cnsElem.isSizeConstraint=true;cnsElem.constraint=cns;})
//	|	(FROM_KW cns=constraint {cnsElem.isAlphabetConstraint=true;cnsElem.constraint=cns;})
	|	(FROM_KW L_PAREN elespec=element_set_spec {cnsElem.isElementSetSpec=true;cnsElem.elespec=elespec;} R_PAREN)
	|	(L_PAREN elespec=element_set_spec {cnsElem.isElementSetSpec=true;cnsElem.elespec=elespec;} R_PAREN)
	|	((INCLUDES {cnsElem.isIncludeType=true;})? typ=type {cnsElem.isTypeConstraint=true;cnsElem.type=typ;})
	|	(PATTERN_KW val=value {cnsElem.isPatternValue=true;cnsElem.values.add(val);})
	|	(WITH_KW 
		((COMPONENT_KW cns=constraint {cnsElem.isWithComponent=true;cnsElem.constraint=cns;})
		|	
		(COMPONENTS_KW {cnsElem.isWithComponents=true;}
		L_BRACE (ELLIPSIS COMMA)? type_constraint_list[cnsElem] R_BRACE )))

	;

value_range[ConstraintElements cnsElem]
{AsnValue val;}
	: (val=value {cnsElem.lEndValue=val;} | MIN_KW {cnsElem.isMinKw=true;}) (LESS {cnsElem.isLEndLess=true;})?  // lower end
	   DOTDOT
	  (LESS {cnsElem.isUEndLess=true;})? (val=value{cnsElem.uEndValue=val;} | MAX_KW {cnsElem.isMaxKw=true;}) // upper end
	;
	
type_constraint_list[ConstraintElements cnsElem]
{NamedConstraint namecns;}
	: namecns=named_constraint {cnsElem.typeConstraintList.add(namecns);}
	 (COMMA namecns=named_constraint {cnsElem.typeConstraintList.add(namecns);})*
	;

named_constraint returns [NamedConstraint namecns]
{namecns = new NamedConstraint(); AsnConstraint cns;}
	:	lid:LOWER {namecns.name=lid.getText();}
	    (cns=constraint {namecns.isConstraint=true;namecns.constraint=cns;})? 
	    (PRESENT_KW {namecns.isPresentKw=true;}
	     |ABSENT_KW {namecns.isAbsentKw=true;}
	     | OPTIONAL_KW {namecns.isOptionalKw=true;})?
	;
				
/*-----------VALUES ---------------------------------------*/

value returns [AsnValue value]
{value = new AsnValue(); AsnSequenceValue seqval;
AsnDefinedValue defval;String aStr;AsnSignedNumber num; AsnScientificNumber snum;
AsnOidComponentList cmplst;List<String> valueInBracesTokens;}		

	: 	(TRUE_KW)=>(TRUE_KW 				{value.isTrueKW = true; })
	|	(FALSE_KW)=>(FALSE_KW				{value.isFalseKW = true;})
	|	(NULL_KW)=>(NULL_KW				{value.isNullKW = true;})
	|	(C_STRING)=>(c:C_STRING				{value.isCString=true; value.cStr = c.getText();})
	|	(defined_value)=>(defval = defined_value {value.isDefinedValue = true; value.definedValue = defval;})
 	|	(scientific_number)=>(snum = scientific_number	{value.scientificNumber = snum;}) 
	|	(signed_number)=>(num = signed_number	{value.isSignedNumber=true ; value.signedNumber = num;}) 
	|	(choice_value[value])=>(choice_value[value]	{value.isChoiceValue = true;})
	|	(sequence_value)=>(seqval=sequence_value	{value.isSequenceValue=true;value.seqval=seqval;})
	|	(sequenceof_value[value])=>(sequenceof_value[value] {value.isSequenceOfValue=true;})
	|	(cstr_value[value])=>(cstr_value[value]		{value.isCStrValue = true;})
	|	(obj_id_comp_lst)=>(cmplst=obj_id_comp_lst	{value.isAsnOIDValue=true;value.oidval=cmplst;})
	|	(PLUS_INFINITY_KW)=>(PLUS_INFINITY_KW		{value.isPlusInfinity = true;})
	|	(MINUS_INFINITY_KW)=>(MINUS_INFINITY_KW		{value.isMinusInfinity = true;})
	;


value2 returns [AsnValue value]
{value = new AsnValue(); AsnSequenceValue seqval;
AsnDefinedValue defval;String aStr;AsnSignedNumber num; AsnScientificNumber snum;
AsnOidComponentList cmplst;List<String> valueInBracesTokens;}		

	: 	(TRUE_KW)=>(TRUE_KW 				{value.isTrueKW = true; })
	|	(FALSE_KW)=>(FALSE_KW				{value.isFalseKW = true;})
	|	(NULL_KW)=>(NULL_KW				{value.isNullKW = true;})
	|	(H_STRING)=>(h:H_STRING				{value.isHString=true; value.cStr = h.getText();})
	|	(B_STRING)=>(b:B_STRING				{value.isBString=true; value.bStr = b.getText();})
	|	(C_STRING)=>(c:C_STRING				{value.isCString=true; value.hStr = c.getText();})
	|	(defined_value)=>(defval = defined_value {value.isDefinedValue = true; value.definedValue = defval;})
 	|	(scientific_number) => (snum = scientific_number	{value.scientificNumber = snum;}) 
	|	(signed_number)=>(num = signed_number	{value.isSignedNumber=true ; value.signedNumber = num;}) 
	|	(choice_value[value])=>(choice_value[value]	{value.isChoiceValue = true;})
    |   (valueInBracesTokens = valueInBraces {value.isValueInBraces=true;value.valueInBracesTokens = valueInBracesTokens;}) 
	|	(PLUS_INFINITY_KW)=>(PLUS_INFINITY_KW		{value.isPlusInfinity = true;})
	|	(MINUS_INFINITY_KW)=>(MINUS_INFINITY_KW		{value.isMinusInfinity = true;})
	;


valueInBraces returns [List<String> valueInBracesTokens]
{List<String> syntaxTokens; valueInBracesTokens = new ArrayList<String>();}
    : (// lo:LOWER {informationObjectAssignment.name = lo.getText();}
       // up:UPPER {informationObjectAssignment.className = up.getText();}
       // ASSIGN_OP
        L_BRACE
//        (~(R_BRACE))*
            ((syntaxTokens = syntaxTokens {valueInBracesTokens.addAll(syntaxTokens); }) )
        R_BRACE 
        )
     ;


cstr_value[AsnValue value]
{AsnBitOrOctetStringValue bstrval = new AsnBitOrOctetStringValue();
AsnCharacterStringValue cstrval = new AsnCharacterStringValue();
AsnSequenceValue seqval;}
	:  ((H_STRING)=>(h:H_STRING 	{bstrval.isHString=true; bstrval.bhStr = h.getText();})
	|	(B_STRING)=>(b:B_STRING		{bstrval.isBString=true; bstrval.bhStr = b.getText();})
	|	(L_BRACE	((id_list[bstrval])=>(id_list[bstrval])
					|(char_defs_list[cstrval])=>(char_defs_list[cstrval])
					| tuple_or_quad[cstrval])    R_BRACE))
		{value.cStrValue=cstrval;value.bStrValue=bstrval;}
	;

id_list[AsnBitOrOctetStringValue bstrval]
{String s="";}
	: (ld:LOWER {s = ld.getText(); bstrval.idlist.add(s);}) 
	  (COMMA ld1:LOWER {s = ld1.getText();bstrval.idlist.add(s);})*
	;
	
char_defs_list[AsnCharacterStringValue cstrval]
{CharDef a ;}
	:a = char_defs {cstrval.isCharDefList = true;cstrval.charDefsList.add(a);} 
	(COMMA (a = char_defs {cstrval.charDefsList.add(a);}))* 
	;

tuple_or_quad[AsnCharacterStringValue cstrval]
{AsnSignedNumber n;}
	: (n = signed_number {cstrval.tupleQuad.add(n);}) 
	  COMMA 
	  (n = signed_number {cstrval.tupleQuad.add(n);}) 
	  ((R_BRACE {cstrval.isTuple=true;})  |  (COMMA 
	  (n = signed_number {cstrval.tupleQuad.add(n);}) 
	  COMMA (n = signed_number {cstrval.tupleQuad.add(n);})))
	;

char_defs  returns [CharDef chardef]
{chardef = new CharDef(); 
AsnSignedNumber n ; AsnDefinedValue defval;}
	:	(c:C_STRING {chardef.isCString = true;chardef.cStr=c.getText();})
	|	(L_BRACE (n = signed_number {chardef.tupleQuad.add(n);}) COMMA (n = signed_number {chardef.tupleQuad.add(n);}) 
		((R_BRACE {chardef.isTuple=true;})
		|(COMMA (n = signed_number {chardef.tupleQuad.add(n);}) 
		COMMA (n = signed_number {chardef.tupleQuad.add(n);}) R_BRACE{chardef.isQuadruple=true;})))
	|	(defval = defined_value {chardef.defval=defval;})
	;

choice_value[AsnValue value]
{AsnChoiceValue chval = new AsnChoiceValue(); AsnValue val;}
	: ((lid:LOWER {chval.name = lid.getText();})
	   (COLON)?  (val=value {chval.value = val;}))
	  {value.chval = chval;}
	;

sequence_value returns [AsnSequenceValue seqval]
{AsnNamedValue nameval = new AsnNamedValue();
seqval = new AsnSequenceValue();}
	:	L_BRACE  ((nameval=named_value {seqval.isValPresent=true;seqval.namedValueList.add(nameval);})?
		(COMMA nameval=named_value {seqval.namedValueList.add(nameval);})*)   R_BRACE
	;

sequenceof_value[AsnValue value]
{AsnValue val;value.seqOfVal = new AsnSequenceOfValue();}
	: L_BRACE ((val=value {value.seqOfVal.value.add(val);})?
       (COMMA val=value {value.seqOfVal.value.add(val);})*) 
	  R_BRACE
	;

protected
defined_value returns [AsnDefinedValue defval]
{defval = new AsnDefinedValue(); }
	:	((up:UPPER {defval.moduleIdentifier = up.getText(); } 
			DOT {defval.isDotPresent=true;})? 
		lid:LOWER { defval.name = lid.getText();})
	;
		
signed_number returns [AsnSignedNumber i]
{i = new AsnSignedNumber() ; String s ; }
	:	(nn:NEG_NUMBER  {s = nn.getText(); i.num= new BigInteger(s); i.positive=false;})
	|	(n:NUMBER  {s = n.getText(); i.num= new BigInteger(s); }) 
	;

scientific_number returns [AsnScientificNumber r]
{r = new AsnScientificNumber() ; String s ; }
	:	 (n:SCIENTIFIC_NUMBER  {s = n.getText(); r.num= new Double(s);}) 
	;
	
named_value returns [AsnNamedValue nameval]
{nameval = new AsnNamedValue(); AsnValue val;}	
	:	(lid:LOWER	{nameval.name = lid.getText(); } 
		val=value	{nameval.value = val;})
	;	

