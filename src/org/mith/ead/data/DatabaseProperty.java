/*
 * Created on Feb 7, 2004 by amit
 *
 */
package org.mith.ead.data;

import java.util.prefs.Preferences;

/**
 * @author amit
 * 
 */
public class DatabaseProperty {

  final String DB_URL = "jdbc:odbc:ead";
  final String DRIVER ="sun.jdbc.odbc.JdbcOdbcDriver";
  final String WORKSPACE_DIR = "";
  final boolean DEBUG=false;

  private Preferences prefs = null;

  /**
   * Constructor.
   */
  public DatabaseProperty() {
    prefs = Preferences.userNodeForPackage(DatabaseProperty.class);
  }
 
  /**
   * @return
   */
  public String getDatabaseUrl() {
    return prefs.get("databaseUrl", DB_URL);
  }

  /**
   * @return
   */
  public String getDriver() {
    return prefs.get("driver", DRIVER);
  }

  /**
   * @param database url
   */
  public void setDatabaseUrl(String durl) {
    prefs.put("databaseUrl", durl);
  }

  /**
   * @param  driver
   */
  public void setDriver(String dr) {
    prefs.put("driver", dr);
  }

  /**
   * @returns project directory 
   */
  public String getProjectDir() {
    return prefs.get("projDir", WORKSPACE_DIR);
  }

  public void setProjectDir(String projectdirectory){
    prefs.put("projDir", projectdirectory);
  }

  /**
   * Get debugging
   */

  public boolean getDebug() {
    return prefs.getBoolean("debug", false);
  }

  /**
   * Set debugging
   */

  public void setDebug(boolean debug) {
    prefs.putBoolean("debug", debug);
  }

  /**
   * 
   */
  public void reset() {
    setProjectDir(WORKSPACE_DIR);
    setDriver(DRIVER);
    setDatabaseUrl(DB_URL);
    setDebug(DEBUG);
  }


  /**
   * Synchronize the preferences with the backing store.
   */

  public void sync() {
    try {
      prefs.sync();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

}
