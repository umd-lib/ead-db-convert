/*
 * Created on Feb 7, 2004 by amit
 *
 */
package org.mith.ead.data;

/**
 * @author amit
 * 
 */
public class DatabaseProperty {

final String DB_URL = "jdbc:odbc:ead";
final String DRIVER ="sun.jdbc.odbc.JdbcOdbcDriver";
final String WORKSPACE_DIR = "";

String databaseUrl;
String databaseName;
String driver;
String projDir;

/**
 * @return
 */
public String getDatabaseName() {
	return databaseName;
}

/**
 * @return
 */
public String getDatabaseUrl() {
	return databaseUrl;
}

/**
 * @return
 */
public String getDriver() {
	return driver;
}

/**
 * @param database name // redundant
 */
public void setDatabaseName(String string) {
	databaseName = string;
}

/**
 * @param database url
 */
public void setDatabaseUrl(String durl) {
	databaseUrl = durl;
}

/**
 * @param  driver
 */
public void setDriver(String dr) {
	driver = dr;
}

/**
 * @returns project directory 
 */
public String getProjectDir() {
	return projDir;
}

public void setProjectDir(String projectdirectory){
	projDir = projectdirectory;
}

/**
 * 
 */
public void reset() {
	setProjectDir(WORKSPACE_DIR);
	setDriver(DRIVER);
	setDatabaseUrl(DB_URL);
}


}
