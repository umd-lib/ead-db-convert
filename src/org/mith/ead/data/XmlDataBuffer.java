/*
 * Created on Feb 7, 2004 by amit, edited Feb 24, 2006 by Jennie
 *
 */
package org.mith.ead.data;

/**
 * @author amit
 * 
 */
public class XmlDataBuffer {

public String id;
private StringBuffer data;
public String title;
public String author;
public String sponsor;
public String status;

public XmlDataBuffer(){
	data = new StringBuffer();	
}

/**
 * @return
 */
public String getId() {
	return id;
}

/**
 * @return title
 */
public String getTitle() {
	return title;
}

/**
 * @return sponsor
 */
public String getSponsor() {
	return sponsor;
}

/**
 * @param id
 */
public void setId(String i) {
	id = i;
}

/**
 * @param title
 */
public void setTitle(String t) {
	title = t;
}

/**
 * @param sponsor
 */
public void setSponsor(String p) {
	sponsor = p;
}

public void append(String s){
	data.append(s);	
}

public void append(StringBuffer s){
    data = s.insert(0,data);	
    //data.append(s);
}

public String toString(){
	
	return data.toString();
}

public int length(){
        return data.length();
}

/**
 * @param author
 */
public void setAuthor(String a) {
	
	author=a;
}


/**
 * @param status
 */
public void setStatus(String s) {
	status = s;
	
}

/**
 * @return
 */
public String getAuthor() {
	return author;
}

/**
 * @return
 */
public StringBuffer getData() {
	return data;
}

/**
 * @return
 */
public String getStatus() {
	return status;
}

}
