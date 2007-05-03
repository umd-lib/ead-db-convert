/*
 * @author: Amit Kumar 
 * @date-created: on 02-11-2004
 * @date-modified: 07-07-2004
 * Representation of each EAD Document in the database.  
 */
package org.mith.ead.data;

/**
 * @author Administrator
 *
 */
public class Couple {
	public String eadid;
	public String archdescid; 
	public String oid;
	/**
	 * @param string
	 * @param string2
	 */
	public Couple(String eid, String aid,String o) {
		eadid = eid;
		archdescid = aid;
		oid = o;		
	}
	
	public String toString(){
		return eadid + "    {"+oid +"}" ;
	}
  
}
