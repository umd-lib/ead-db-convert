package org.mith.ead.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;

// log4j
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.log4j.ConsoleAppender;

import org.mith.ead.data.DateHandler;


public class TestDateHandler {
        
  static Logger log = Logger.getLogger(TestDateHandler.class.getName());

  /**
   * Extract all dates from the db.
   */

  public static void extract(DataConvertor dc) throws Exception {
    OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(new File("dates.txt")));

    Statement st = dc.conn.createStatement();

    System.out.println("Extracting dates from BoxList");
    String query = "SELECT date FROM BoxList";
                
    log.debug(query);
    ResultSet rs = st.executeQuery(query) ;         

    while (rs.next()) {
      String strDate = rs.getString(1);
      w.write(strDate + "\n");
    }

    System.out.println("Extracting dates from ItemList");
    query = "SELECT date FROM ItemList";
                
    log.debug(query);
    rs = st.executeQuery(query) ;         

    while (rs.next()) {
      String strDate = rs.getString(1);
      w.write(strDate + "\n");
    }
    rs.close();

    w.close();
    st.close();
  }


  /**
   * Command line interface.
   */

  public static void main(String args[]) throws Exception {
    //DatabaseProperty dp = new DatabaseProperty();
    //dp.reset();
    //DataConvertor dc = new DataConvertor(dp);
    //dc.connect();
	 //
    //extract(dc);

    // Setup logging
    Logger root = Logger.getRootLogger();

    PatternLayout layout = new PatternLayout("%m%n");
    root.addAppender(new ConsoleAppender(layout));

    root.setPriority(Priority.INFO);

	 // Process each line of the input file
	 BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));

	 String strLine = null;
	 while ((strLine = br.readLine()) != null) {
		strLine = strLine.trim();

		if (! strLine.equals("") && ! strLine.startsWith("#")) {
		  String strNorm = DateHandler.displayToNorm(strLine);

		  System.out.println("disp: " + strLine);
		  System.out.println("norm: " + strNorm);
		  System.out.println();
		}
	 }
  }
}
