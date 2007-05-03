/*
 * Created on Aug 20, 2003
 * 
 */
package org.mith.ead.fs;

import java.io.*;

/**
 * @author Amit Kumar
 * @date: Aug 20, 2003
 * @time: 8:51:31 AM
 * 
 * @comments: To create new file on the fs while adding data
 * 			  To delete the file from the fs when entry is deleted
 * 			   
 */

public class FileIO {


public 	boolean deleteFile(String fileName,String projDir){
	File file = new File(projDir+"/"+fileName);
	if(file.canWrite()){
	 if(file.delete()){
		return true;
	}else{
		return false;

	}
	}else{
	return true;
	}
	}


	/*TODO: does not check for disk space available
	*/
public	boolean writeFile(String fileName,
	String projDir,String content){
	// does not check for disk space available
	System.out.println("Trying to write the file: " + fileName);
	System.out.println("WRITE TO THIS :" +projDir+"/"+fileName);
	try{
	File file = new File(projDir+"/"+fileName);
	if(file.exists() && file.canWrite()){
	file.delete(); // delete the file
	file.createNewFile(); // create new file
	FileOutputStream fos = new FileOutputStream(file);
	PrintStream ps = new PrintStream(fos);
	ps.println(content);
	ps.close();
	fos.close();
	return true;
	}else{
	file.createNewFile(); // create new file
	FileOutputStream fos = new FileOutputStream(file);
	PrintStream ps = new PrintStream(fos);
	ps.println(content);
	ps.close();
	fos.close();
	return true;
	}
	}catch(Exception ex){
	System.out.println(ex.getMessage());
	return false;
	}
	}
	

	public	boolean write(String filename,	String content){
		// does not check for disk space available
		
		try{
		File file = new File(filename);
		if(file.exists() && file.canWrite()){
		file.delete(); // delete the file
		file.createNewFile(); // create new file
		FileOutputStream fos = new FileOutputStream(file);
		PrintStream ps = new PrintStream(fos);
		ps.println(content);
		ps.close();
		fos.close();
		return true;
		}else{
		file.createNewFile(); // create new file
		FileOutputStream fos = new FileOutputStream(file);
		PrintStream ps = new PrintStream(fos);
		ps.println(content);
		ps.close();
		fos.close();
		return true;
		}
		}catch(Exception ex){
		System.out.println(ex.getMessage());
		return false;
		}
		}
	
	
	

	// Read the nth file from the directory
	public synchronized String read(File file) throws IOException{
		BufferedReader f = 
		new BufferedReader(new FileReader(file));
		StringBuffer content = new StringBuffer();	
		String line;
		while((line = f.readLine()) != null)
				  content.append(line);
	    f.close();
		
		return content.toString();
	}
	
	
	
	
	
}
