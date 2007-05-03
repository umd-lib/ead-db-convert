/*
 * C03Element stores the C03 elements 
 * folder_no,item_no, item, date, size, restricted 
 */
package org.mith.ead.data;
import java.util.Vector;
import org.mith.ead.data.DataC03Element;


/**
 * 
 * 
 */


public class CO3Element {
 
 Vector data;
 
 public CO3Element(){
 data = new Vector(3,2);
 }

/**
 * @param folder_no
 * @param item_no
 * @param item
 * @param date
 * @param size
 * @param restricted
 */
public void addData(String id,String folder_no, String item_no, String item, String date, String size, String restricted,
String file1,String file2,String file3,String file4,String file5,
String file6,String file7,String file8) {
	data.addElement(new DataC03Element(id,folder_no,item_no,item,date,size,restricted,file1,file2,
	file3,file4,file5,file6,file7,file8));	
}
 
 

public Vector getCO3(String folder_no){
	Vector result= new Vector(3,1); // stores the string data
	
	for(int i=0; i< data.size();i++){
	if(((DataC03Element)data.elementAt(i)).getFolderNo().equalsIgnoreCase(folder_no)){
			
		result.add((String)((DataC03Element)data.elementAt(i)).getXml());
	}	
	}
	return result;
}

// assumes that the CO3 data has 
public boolean hasCO3(String folder_no){
	//System.out.println("hasCO3? "+ data.size());
	for(int i=0; i< data.size();i++){
		if(((DataC03Element)data.elementAt(i)).getFolderNo().equalsIgnoreCase(folder_no)){
	//		System.out.println("CHECK: " + folder_no + "  " + ((DataC03Element)data.elementAt(i)).getFolderNo());
		return true;		
		}
	
		}	
return false;
}




}
