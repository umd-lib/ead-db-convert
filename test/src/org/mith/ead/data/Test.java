package org.mith.ead.data;

import java.io.File;

import java.util.Properties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import java.util.Date;
import java.util.Random;
import java.util.Vector;

// log4j
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.log4j.ConsoleAppender;


public class Test {

  public static void main(String args[]) throws Exception {

    // Get properties
    Properties p = System.getProperties();

    // Setup logging
    Logger log = Logger.getRootLogger();

    PatternLayout layout = new PatternLayout("[%d] [%-5p]: (%c{2})%n%m%n%n");
    log.addAppender(new ConsoleAppender(layout));
    log.setPriority(Priority.DEBUG);

    Class.forName("sun.jdbc.odbc.JdbcOdbcDriver") ;

    // Get a connection to the database
    Connection conn = DriverManager.getConnection("jdbc:odbc:ead");
    conn.setReadOnly(true);

    // Print all warnings
    for( SQLWarning warn = conn.getWarnings(); warn != null; warn = warn.getNextWarning() )
      {
	log.debug( "SQL Warning:" ) ;
	log.debug( "State  : " + warn.getSQLState()  ) ;
	log.debug( "Message: " + warn.getMessage()   ) ;
	log.debug( "Error  : " + warn.getErrorCode() ) ;
      }

    String query = "Select * from BoxList where archdescid = 37 and seriesnumber = 7 order by box * 1, folder_no * 1";

    for (int i=0; i < 1000; i++) {
      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(query);

      log.debug("start");
      int n = 0;
      while(rs.next()) {
	String s = rs.getString(1);
	n++;
      }
      log.debug("end: " + n);
    }
  }
}
