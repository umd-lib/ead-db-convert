package org.mith.ead.data;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import java.util.Date;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Random;
import java.util.Vector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.text.DecimalFormat;

// log4j
import org.apache.log4j.Logger;

import org.mith.ead.swing.EadGui;

/**
 * Handle date conversions: normalized -> display and display -> normalized
 *
 * @author Ben Wallberg
 */

public class DateHandler {
        
  static Logger log = Logger.getLogger(DateHandler.class.getName());


  /**
   * Convert display form to normalized.
   *
   * @param strDisplay display version of a date
   *
   * @return normalized date or null if unable to convert
   */

  public static String displayToNorm(String strDisplay) {

    Pattern p = null;

    log.debug("in:     " + strDisplay);

    //
    // Perform normalizations
    //

    String s = 
      strDisplay
      .toLowerCase()
		.replaceAll("^ *,","")
      .replaceAll("ca?\\. ","circa ")
      .replaceAll("\\[|\\]|\\(|\\)","")
      .replaceAll(" {2,}"," ")
		.trim()
      ;

    //
    // Split into lists of dates
    //

    Vector v = new Vector();

    // Handle dates with commas (one date in the list)
    if (get("(\\M)( \\d{1,2}(-\\d{1,2})?)?, \\d{4}(-(\\M)( \\d{1,2}(-\\d{1,2})?)?, \\d{4})?").matcher(s).matches()) {
      v.add(s);
    }

    else if (get("(\\M)( \\d{1,2}(-\\d{1,2})?)?-(\\M)( \\d{1,2}(-\\d{1,2})?)?, \\d{4}").matcher(s).matches()) {
      v.add(s);
    }

    // mmm, yyyy-mmm, yyyy
    else if (get("(\\M) *,? *(\\d{4}) *- *(\\M) *,? *(\\d{4})").matcher(s).matches()) {
      v.add(s);
    }

    // yyyy, mmm dd
    else if (get("(\\d{4}) *, *(\\M) *(\\d{1,2})").matcher(s).matches()) {
      v.add(s);
    }

    else {
		// Now we can expect commas to be list separators
      String sa[] = get("( *, *)|(( *,)? *and *)").split(s);

      for (int i=0; i < sa.length; i++) {
        v.add(sa[i]);
      } 
      
    }

    //
    // Normalize each date or date range in the list
    //
    for (ListIterator l = v.listIterator(); l.hasNext(); ) {
      String norm = displayToNormRange((String)l.next());

      if (norm == null) {
        l.remove();
      } else {
        l.set(norm);
      }
    } 

	 // Compile the list back into a string
	 StringBuffer sb = new StringBuffer();
	 for (int j=0; j < v.size(); j++) {
		String n = (String)v.get(j);
		
		if (j > 0) {
		  if (n.equals("(undated)")) {
			 sb.append(" (and undated)");
		  } 
		  else {
			 sb.append("; ");
			 sb.append(n);
		  }
		} else {
		  sb.append(n);
		}
	 }

	 s = sb.toString();

    log.debug("out:    " + s + "\n");

    return s;
  }


  /**
   * Convert display form to normalized for a single date or date range.
   *
   * @param s display version of a date
   *
   * @return normalized date or null if unable to convert
   */

  public static String displayToNormRange(String s) {
	 log.debug("range:  " + s);

    Matcher m = null;
    String r1 = null;
    String r2 = null;

    // mmm dd-dd, yyyy
    if ((m = get("(\\M) *(\\d{1,2}) *- *(\\d{1,2}) *, *(\\d{4})").matcher(s)).matches()) {
      r1 = displayToNormSingle(m.group(1) + " " + m.group(2) + ", " + m.group(4));
      r2 = displayToNormSingle(m.group(1) + " " + m.group(3) + ", " + m.group(4));
    }

    // dd-dd mmm, yyyy
    else if ((m = get("(\\d{1,2}) *- *(\\d{1,2}) *(\\M) *(\\d{4})").matcher(s)).matches()) {
      r1 = displayToNormSingle(m.group(3) + " " + m.group(1) + ", " + m.group(4));
      r2 = displayToNormSingle(m.group(3) + " " + m.group(2) + ", " + m.group(4));
    }

    // dd mmm-dd mmm, yyyy
    else if ((m = get("(\\d{1,2}) *(\\M) *- *(\\d{1,2}) *(\\M) *(\\d{4})").matcher(s)).matches()) {
      r1 = displayToNormSingle(m.group(2) + " " + m.group(1) + ", " + m.group(5));
      r2 = displayToNormSingle(m.group(4) + " " + m.group(3) + ", " + m.group(5));
    }

    // mmm-mmm, yyyy
    else if ((m = get("(\\M) *- *(\\M) *,? *(\\d{4})").matcher(s)).matches()) {
      r1 = displayToNormSingle(m.group(1) + " " + m.group(3));
      r2 = displayToNormSingle(m.group(2) + " " + m.group(3));
    }

    // mmm dd-mmm dd, yyyy
    else if ((m = get("(\\M) *(\\d{1,2}) *- *(\\M) *(\\d{1,2}) *, *(\\d{4})").matcher(s)).matches()) {
      r1 = displayToNormSingle(m.group(1) + " " + m.group(2) + ", " + m.group(5));
      r2 = displayToNormSingle(m.group(3) + " " + m.group(4) + ", " + m.group(5));
    }

    // mmm, yyyy-mmm, yyyy
    else if ((m = get("(\\M) *,? *(\\d{4}) *- *(\\M) *,? *(\\d{4})").matcher(s)).matches()) {
      r1 = displayToNormSingle(m.group(1) + " " + m.group(2));
      r2 = displayToNormSingle(m.group(3) + " " + m.group(4));
    }

    // yyyy-mmm yyyy
    else if ((m = get("(\\d{4}) *- *(\\M) *,? *(\\d{4})").matcher(s)).matches()) {
      r1 = displayToNormSingle(m.group(1));
      r2 = displayToNormSingle(m.group(2) + " " + m.group(3));
        ;
    }

    // mmm yyyy-yyyy
    else if ((m = get("(\\M) *,? *(\\d{4}) *- *(\\d{4})").matcher(s)).matches()) {
      r1 = displayToNormSingle(m.group(1) + " " + m.group(2));
      r2 = displayToNormSingle(m.group(3));
    }

    // yyyys-yyyys
    else if ((m = get("(\\d{4}s) *- *(\\d{4}s)").matcher(s)).matches()) {
      r1 = displayToNormSingle(s);
    }

    // circa yyyy-yyyy
    else if ((m = get("circa *(\\d{4}) *- *(\\d{4})").matcher(s)).matches()) {
      r1 = displayToNormSingle(s);
    }

    // generic range
    else if ((m = get("(.*?) *- *(.*?)").matcher(s)).matches()) {
      r1 = displayToNormSingle(m.group(1));
      r2 = displayToNormSingle(m.group(2));

    }

    else {
      r1 = displayToNormSingle(s);
    }

    // Return the correct value
    if (r1 == null && r2 == null) {
      return null;
    } else if (r1 == null) {
      return r2;
    } else if (r2 == null) {
      return r1;
    } else {
      return r1 + "/" + r2;
    }
  }

    
  /**
   * Convert display form to normalized for a single date.
   *
   * @param s display version of a date
   *
   * @return normalized date or null if unable to convert
   */

  public static String displayToNormSingle(String s) {

	 log.debug("single: " + s);

    Matcher m = null;

    // #/#/####
    if ((m = get("(\\d{1,2})/(\\d{1,2})/(\\d{4})").matcher(s)).matches()) {
      return m.group(3) + "-" + pad(2, m.group(1)) + "-" + pad(2, m.group(2));
    }

    // #/#/##
    if ((m = get("(\\d{1,2})/(\\d{1,2})/(\\d{2})").matcher(s)).matches()) {
      return "19" + m.group(3) + "-" + pad(2, m.group(1)) + "-" + pad(2, m.group(2));
    }

    // mmm dd, yyyy
    else if ((m = get("(\\M) +(\\d{1,2}) *, *(\\d{4})").matcher(s)).matches()) {
      return m.group(3) + "-" + monthToNum(m.group(1)) + "-" + pad(2, m.group(2)) ;
    }

    // dd mmm yyyy
    else if ((m = get("(\\d{1,2}) +(\\M) +(\\d{4})").matcher(s)).matches()) {
      return m.group(3) + "-" + monthToNum(m.group(2)) + "-" + pad(2, m.group(1)) ;
    }

    // yyyy, mmm dd
    else if ((m = get("(\\d{4}) *, *(\\M) +(\\d{1,2})").matcher(s)).matches()) {
      return m.group(1) + "-" + monthToNum(m.group(2)) + "-" + pad(2, m.group(3)) ;
    }

    // mmm, yyyy
    else if ((m = get("(\\M) *,? *(\\d{4})").matcher(s)).matches()) {
      return m.group(2) + "-" + monthToNum(m.group(1)) ;
    }

    // yyyy
    else if ((m = get("(\\d{4})").matcher(s)).matches()) {
      return m.group(1) ;
    }

    // undated unknown
    else if ((m = get("(undated|unknown)").matcher(s)).matches()) {
      return "(" + m.group(1) + ")" ;
    }

    // yyyys
    else if ((m = get("(?:circa *)?(\\d{3})0s").matcher(s)).matches()) {
      return m.group(1) + "0/" + m.group(1) + "9 (circa)" ;
    }

    // yyyys - yyyys
    else if ((m = get("(\\d{3})0s *- *(\\d{3})0s").matcher(s)).matches()) {
      return m.group(1) + "0/" + m.group(2) + "0" ;
    }

    // circa yyyy - yyyy
    else if ((m = get("circa *(\\d{4}) *- *(\\d{4})").matcher(s)).matches()) {
      return m.group(1) + "/" + m.group(2) + " (circa)" ;
    }

    // circa mmm yyyy
    else if ((m = get("circa +(\\M) +(\\d{4})").matcher(s)).matches()) {
      return m.group(2) + "/" + monthToNum(m.group(1)) + " (circa)" ;
    }

    // circa yyyy
    else if ((m = get("circa *(\\d{4})").matcher(s)).matches()) {
      return m.group(1) + " (circa)" ;
    }

    return null;
  }

    
  private static Hashtable regexes = new Hashtable();

  /**
   * Build and compile the regular expression
   */

  public static Pattern get(String regex) {

    if (! regexes.containsKey(regex)) {
      // Do local substitutions
      String regexnew =
        regex
        .replaceAll("\\\\N", "winter|spring|summer|fall|autumn")
        .replaceAll("\\\\M", "january|february|march|april|may|june|july|august|september|october|november|december")
        ;

      // Compile the regex
      Pattern p = Pattern.compile(regexnew);

      regexes.put(regex, p);
    }

    return (Pattern)regexes.get(regex);
  }


  /**
   * Pad number with zeroes.
   */

  private static String pad(int n, String s) {
    while (s.length() < n) {
      s = "0" + s;
    }

    return s;
  }


  /**
   * Convert a month name to number.
   */

  private static String monthToNum(String s) {
    if      (s.equals("january"))      { return "01"; }
    else if (s.equals("february"))     { return "02"; }
    else if (s.equals("march"))        { return "03"; }
    else if (s.equals("april"))        { return "04"; }
    else if (s.equals("may"))          { return "05"; }
    else if (s.equals("june"))         { return "06"; }
    else if (s.equals("july"))         { return "07"; }
    else if (s.equals("august"))       { return "08"; }
    else if (s.equals("september"))    { return "09"; }
    else if (s.equals("october"))      { return "10"; }
    else if (s.equals("november"))     { return "11"; }
    else if (s.equals("december"))     { return "12"; }

    return "??";
  }

}
