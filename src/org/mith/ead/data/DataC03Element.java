/*
 * Created on Feb 10, 2004
 *  EAD @ Maryland Created by AK to store CO3 element data
 * 
 */
package org.mith.ead.data;

public class DataC03Element {

String co3_id;
String item_no;
String folder_no;
String item;
String date;
String size;
String restricted;
String file1,file2,file3,file4,file5,file6,file7,file8;

public DataC03Element(String id,String fo, String ite,String it,String da,String si,String res,
String f1,String f2,String f3,String f4,String f5,String f6,
String f7,String f8
){
	co3_id = id;
	
	item_no = ite;
	folder_no = fo;
	if(item ==null)
	item ="";
	else
	item = it;
	
	if(date==null)
	date="";
	else
	date = da;
	
	if(size==null)
	size="";	
	else
	size = si;

	if(f1==null) file1=""; else file1=f1;
	if(f2==null) file2=""; else file2=f2;
	if(f3==null) file3=""; else file3=f3;
	if(f4==null) file4=""; else file4=f4;
	if(f5==null) file5=""; else file5=f5;
	if(f6==null) file6=""; else file6=f6;
	if(f7==null) file7=""; else file7=f7;
	if(f8==null) file8=""; else file8=f8;
	
	restricted = res;	
}

public String getFolderNo()
{
	return folder_no;
}

/**
 * @return
 */
public String getXml() {
	
	String data = "<co3 level=\"item\">\n";
	data = data + "<did>\n";
	data = data + "<container parent=\""+co3_id+"\" type=\"item\">"+item_no+"</container>\n";
	
	
	data = data + "<unittitle>"+item;

	if(file1!= null && file1 != ""){
	data = data+ file1;
	}
	if(file2!= null && file2 != ""){
	data = data+" -- " +file2;
	}
	if(file3!= null && file3 != ""){
	data = data+" -- " +file3;
	}
	if(file4!= null && file4 != ""){
	data = data+" -- " +file4;
	}
				
	if(file5!= null && file5 != ""){
	data = data+" -- " +file5;
	}
	if(file6!= null && file6 != ""){
	data = data+" -- " +file6;
	}
				
	if(file7!= null && file7 != ""){
	data = data+" -- " +file7;
	}
		
	if(file8!= null && file8 != ""){
	data = data+" -- " +file8;
	}	
		
	
	
	data = data + "<unitdate>"+date+"</unitdate>\n";
	if(size != null && size != ""){
		data = data + "<physdesc>"+size+"</physdesc>\n";
	}
	if(restricted != null){
		if(restricted.trim().equalsIgnoreCase("Yes")){
			data = data + "<accessrestrict><p>RESTRICTED</p></accessrestrict>\n";
		}
	}
	
	data = data + "</unittitle>\n</did>\n</co3>";
	
		
	return data;
}





}
