package org.mith.ead.data;

import java.io.File;
import java.io.FileOutputStream;
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
import java.util.Random;
import java.util.Vector;

// log4j
import org.apache.log4j.Logger;

import org.mith.ead.swing.EadGui;

/**
 * @author BW
 *
 * Handle date conversions: internal->external and external->internal.  Also 
 * handle testing of all date conversions.
 */

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
    DatabaseProperty dp = new DatabaseProperty();
    dp.reset();
    DataConvertor dc = new DataConvertor(dp);
    dc.connect();

    extract(dc);
  }
}
