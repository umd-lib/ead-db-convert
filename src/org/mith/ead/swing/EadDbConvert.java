/*
 * Created on Feb 10, 2004
 * @Created by AK to provide GUI support for EAD data import from Access
 */
package org.mith.ead.swing;

import java.util.Properties;

import java.sql.SQLException;

import javax.swing.JOptionPane;

// log4j
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.log4j.ConsoleAppender;

import org.mith.ead.data.DataConvertor;




public class EadDbConvert {


  final static int CONNECTED = 1;
  final static int DISCONNECTED = 0;
  static int status=0;
  public EadDbConvert(){
    
    
  }
  public static void main(String args[]){
    // Get properties
    Properties p = System.getProperties();

    String strDebug = p.getProperty("ead.debug");
    boolean bDebug = (strDebug != null && strDebug.equals("true"));

    // Setup logging
    Logger root = Logger.getRootLogger();

    PatternLayout layout = new PatternLayout("[%d] [%-5p]: (%c{2})%n%m%n%n");
    root.addAppender(new ConsoleAppender(layout));

    root.setPriority(bDebug ? Priority.DEBUG : Priority.INFO);

    EadGui eadgui = new EadGui();

    DataConvertor dc = new DataConvertor(eadgui.dp);
    eadgui.setConvertor(dc);
  
    try {
      dc.connect();
    } catch (SQLException e) {
      
      Object[] options = {"Yes, please",
			  "No, thanks",
      };
      int n = JOptionPane.showOptionDialog(eadgui,
					   "Could Not Connect to the Database at "
					   + eadgui.dp.getDatabaseUrl()+"\n Do you want to set preferences ?",
					   "ODBC-JDBC Error",
					   JOptionPane.YES_NO_OPTION,
					   JOptionPane.QUESTION_MESSAGE,
					   null,
					   options,
					   options[1]);
        
      if(n==0){
        eadgui.showPreference();        
      }else{
        eadgui.jta.setText("");
        eadgui.jta.setText("Not Connected:");
        status =DISCONNECTED;
      }
      
    } catch (ClassNotFoundException e) {
      JOptionPane.showMessageDialog(eadgui,
				    "Database driver: "+ eadgui.dp.getDriver() +"not found. ",
				    "ODBC-JDBC",JOptionPane.ERROR_MESSAGE);
    
    }

    eadgui.jta.setText("");
    eadgui.jta.setText("Connected to "+ eadgui.dp.getDatabaseUrl());
    eadgui.jta.setText("Now Ready to Transform");
    status = CONNECTED;  
  

  }
  
  

}
