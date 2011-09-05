/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.datatypes;

import static org.junit.Assert.fail;

import org.junit.Test;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * Unit tests on {@link org.openrdf.model.datatypes.XMLDatatypeUtil}
 * 
 * @author Jeen Broekstra
 */
public class XMLDatatypeUtilTest {

	/** valid xsd:date values */
	private static final String[] VALID_DATES = {
			"2001-01-01",
			"2001-01-01Z",
			"2001-12-12+10:00",
			"-1800-06-06Z",
			"2004-02-29" // leap year
	};

	/** invalid xsd:date values */
	private static final String[] INVALID_DATES = {
			"foo",
			"Mon, 11 Jul 2005 +0200",
			"2001",
			"01",
			"2001-01",
			"2001-13-01",
			"2001-01-32",
			"2001-12-12+16:00",
			"2003-02-29" // not a leap year
	};
	
	/** valid xsd:time values */
	private static final String[] VALID_TIMES = {
		"13:00:00",
		"09:15:10",
		"11:11:11Z",
		"10:00:01+06:00",
		"10:01:58-06:00"
	};
	
	/** invalid xsd:time values */
	private static final String[] INVALID_TIMES = {
		"foo",
		"21:32",
		"9:15:16",
		"2001-10-10:10:10:10",
		"-10:00:00",
		"25:25:25"
	};
	

	/** valid xsd:gYear values */
	private static final String[] VALID_GYEAR = { "2001", "2001+02:00", "2001Z", "-2001" };

	/** invalid xsd:gYear values */
	private static final String[] INVALID_GYEAR = { "foo", "01", "2001-01", "2001-01-01" };

	/** valid xsd:gDay values */
	private static final String[] VALID_GDAY = { "---01", "---26Z", "---12-06:00", "---13+10:00" };

	/** invalid xsd:gDay values */
	private static final String[] INVALID_GDAY = {
			"01",
			"--01-",
			"2001-01",
			"foo",
			"---1",
			"---01+16:00",
			"---32" };
	
	/** valid xsd:gMonth values */
	private static final String[] VALID_GMONTH = {
		"--05",
		"--11Z",
		"--11+02:00",
		"--11-04:00",
		"--02"
	};
	
	/** invalid xsd:gMonth values */
	private static final String[] INVALID_GMONTH = {
		"foo",
		"-05-",
		"--13",
		"--1",
		"01",
		"2001-01"
	};
	
	/** valid xsd:gMonthDay values */
	private static final String[] VALID_GMONTHDAY = {
		"--05-01",
		"--11-01Z",
		"--11-01+02:00",
		"--11-13-04:00",
		"--11-15"
	};
	
	/** invalid xsd:gMonthDay values */
	private static final String[] INVALID_GMONTHDAY = {
		"foo",
		"-01-30-",
		"--01-35",
		"--1-5",
		"01-15",
		"--13-01"
	};
	
	/** valid xsd:gYearMonth values */
	private static final String[] VALID_GYEARMONTH = {	
		"2001-10",
		"2001-10Z",
		"2001-10+02:00",
		"2001-10-04:00",
	};

	/** invalid xsd:gYearMonth values */
	private static final String[] INVALID_GYEARMONTH = {	
		"foo",
		"2001",
		"2001-15",
		"2001-13-26+02:00",
		"2001-11-11+02:00",
		"01-10"
	};

	/** valid xsd:duration values */
	private static final String[] VALID_DURATION = {
		"PT1004199059S", 
		"PT130S", 
		"PT2M10S", 
		"P1DT2S", 
		"P1M2D",
		"P2Y2M1D",
		"-P1Y",
		"P60D",
		"P1Y2M3DT5H20M30.123S",
		"PT15.5S"
	};

	/** invalid xsd:duration values */
	private static final String[] INVALID_DURATION = {
		"1Y",
		"P1S",
		"P-1Y",
		"P1M2Y",
		"P2YT",
		"P",
		""
	};
	
	/** valid xsd:dayTimeDuration values */
	private static final String[] VALID_DAYTIMEDURATION = {
		"P1DT2H",
		"PT20M",
		"PT120M",
		"P3DT5H20M30.123S",
		"-P6D",
		"PT15.5S"
	};

	/** invalid xsd:dayTimeDuration values */
	private static final String[] INVALID_DAYTIMEDURATION = {
		"P1Y2M3DT5H20M30.123S",
		"P1Y",
		"P-20D",
		"P20DT",
		"P15.5D",
		"P1D2H",
		"P",
		"",
		"PT15.S"
	};

	/**
	 * Test method for
	 * {@link org.openrdf.model.datatypes.XMLDatatypeUtil#isValidValue(java.lang.String, org.openrdf.model.URI)}
	 * .
	 */
	@Test
	public void testIsValidValue() {
		testValidation(VALID_DATES, XMLSchema.DATE, true);
		testValidation(INVALID_DATES, XMLSchema.DATE, false);

		testValidation(VALID_TIMES, XMLSchema.TIME, true);
		testValidation(INVALID_TIMES, XMLSchema.TIME, false);

		testValidation(VALID_GDAY, XMLSchema.GDAY, true);
		testValidation(INVALID_GDAY, XMLSchema.GDAY, false);

		testValidation(VALID_GMONTH, XMLSchema.GMONTH, true);
		testValidation(INVALID_GMONTH, XMLSchema.GMONTH, false);

		testValidation(VALID_GMONTHDAY, XMLSchema.GMONTHDAY, true);
		testValidation(INVALID_GMONTHDAY, XMLSchema.GMONTHDAY, false);
		
		testValidation(VALID_GYEAR, XMLSchema.GYEAR, true);
		testValidation(INVALID_GYEAR, XMLSchema.GYEAR, false);

		testValidation(VALID_GYEARMONTH, XMLSchema.GYEARMONTH, true);
		testValidation(INVALID_GYEARMONTH, XMLSchema.GYEARMONTH, false);

		testValidation(VALID_DURATION, XMLSchema.DURATION, true);
		testValidation(INVALID_DURATION, XMLSchema.DURATION, false);

		testValidation(VALID_DAYTIMEDURATION, XMLSchema.DAYTIMEDURATION, true);
		testValidation(INVALID_DAYTIMEDURATION, XMLSchema.DAYTIMEDURATION, false);

	}

	private void testValidation(String[] values, URI datatype, boolean validValues) {
		for (String value : values) {
			boolean result = XMLDatatypeUtil.isValidValue(value, datatype);
			if (validValues) {
				if (!result) {
					fail("value " + value + " should have validated for type " + datatype);
				}
			}
			else {
				if (result) {
					fail("value " + value + " should not have validated for type " + datatype);
				}
			}
		}
	}
}
