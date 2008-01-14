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

public class DateHandler {
        
  static Logger log = Logger.getLogger(DateHandler.class.getName());

}
