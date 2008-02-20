package org.mith.ead.data;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
 * @author AK
 * Talks JDBC with the JL and JE's EAD database and generates an EAD Document 
 */
public class DataConvertor {
        
  static Logger log = Logger.getLogger(DataConvertor.class.getName());

  private EadGui gui;


  private String current_co2_parent;
        
        
  DatabaseProperty dp;
  Connection conn;
  XmlDataBuffer xmlDoc;
  String id;
  String title;
  int boxno=0;
  int prev_reel=0;
  Random svcRand = new Random();

  public void connect() throws SQLException, ClassNotFoundException{
    try
    {
      // Load the database driver
      Class.forName( dp.getDriver() ) ;

      // Close any old connections
      try {
	this.conn.close();
      }
      catch (Exception e) {};

      // Get a connection to the database
      this.conn = DriverManager.getConnection( dp.getDatabaseUrl() ) ;
      this.conn.setReadOnly(true);

      // Print all warnings
      for( SQLWarning warn = conn.getWarnings(); warn != null; warn = warn.getNextWarning() )
      {
        log.debug( "SQL Warning:" ) ;
        log.debug( "State  : " + warn.getSQLState()  ) ;
        log.debug( "Message: " + warn.getMessage()   ) ;
        log.debug( "Error  : " + warn.getErrorCode() ) ;
      }
    }catch( SQLException e )
    {                       
      throw e;                
    }catch(ClassNotFoundException ex){
      throw ex;                                       
    }
                                
                
  }


// already connected by the time we reach here

  public XmlDataBuffer transform(String id) {
    return transform(id, null);
  }


  public XmlDataBuffer transform(String id, File file) {
    xmlDoc = new XmlDataBuffer();
    Statement stmtArch = null;
    Statement stmtEp= null;
    Statement stmtEc = null;
    Statement stmtApl = null;
    Statement stmtGu = null;
    Statement stmtSe = null;
    Statement stmtSub = null;
    Statement stmtSubDup = null;
    Statement stmtBl = null;

                
    ResultSet rsArch = null;
    ResultSet rsEp = null;
    ResultSet rsEc = null;
    ResultSet rsApl = null;
    ResultSet rsGu = null;
    ResultSet rsSe = null;
    ResultSet rsSub = null;
    ResultSet rsSubDuplicate = null;
    ResultSet rsBl = null;

    Date start = new Date();

    try {
                        
      stmtArch = conn.createStatement();
      stmtEp = conn.createStatement();
      stmtEc = conn.createStatement();
      stmtApl = conn.createStatement();
      stmtGu = conn.createStatement();
      stmtSe = conn.createStatement();
      stmtSub =conn.createStatement();
      stmtSubDup = conn.createStatement();
      stmtBl = conn.createStatement();
        
        
      log.info("beginning transform for: " + id);        

      //String query =        "SELECT * FROM archdescdid where eadid = "+eadid+";";
      String query = "SELECT * FROM archdescdid where archdescid = "+id+";";
                
      log.debug(query);
      rsArch = stmtArch.executeQuery(query) ;         
        
        
        
                
      rsArch.next();
      //val= rsArch.getString("dateoffirstentry");
      rsEp = stmtEp.executeQuery("SELECT * FROM eadheaderpublisher where archdescid ="+id+"");
      //rsEp.next();          
      rsEc = stmtEc.executeQuery("SELECT * FROM eadheaderrevision where archdescid ="+id+"");
      //rsEc.next();
      rsApl = stmtApl.executeQuery("SELECT * FROM archdescphysloc where archdescid ="+id+"");
      //rsApl.next();
                                
      rsGu = stmtGu.executeQuery("SELECT * FROM resourceguide where archdescid ="+id+"");
      //rsGu.next();
                                
      rsSe = stmtSe.executeQuery("SELECT * FROM seriesdesclist where archdescid ="+id+" order by seriesnumber");
      //rsSe.next();
                                
      rsSub = stmtSub.executeQuery("SELECT * FROM subjects where archdescid ="+id+"");
      rsSubDuplicate = stmtSubDup.executeQuery("SELECT * FROM subjects where archdescid ="+id+"");
                
      rsBl = stmtBl.executeQuery("SELECT * FROM BoxList where archdescid ="+id+"");
                                
    } catch (SQLException e) {
      log.error("Problem running query");
      log.error(e.getMessage());
    }                
                
                
        
                
    /*
      if(rsArch != null && rsEp != null && rsEc != null)
      System.out.println(dc.getEadHeader(rsArch,rsEp,rsEc));
      else
      System.out.println("ResultSets are null ..");   
    */
        
    xmlDoc.append("<?xml version='1.0' encoding='UTF-8'?>\n<ead relatedencoding=\"MARC21\">\n");
    xmlDoc.append(getEadHeader(rsArch,rsEp,rsEc));
    xmlDoc.setId(id);
                        
    // DO THE QUERY AGAIN STUPID ODBC-JDBC does not support calling
    // getXXX method twice how stupid can this be
    try {
      rsArch = stmtArch.executeQuery( "SELECT * FROM archdescdid where archdescid ="+id+"" ) ;
      rsArch.next();
      rsGu = stmtGu.executeQuery("SELECT * FROM resourceguide where archdescid ="+id+"");
      //?? possible bug
      //      rsGu.next();
    } catch (SQLException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    }
        
    String archDesc = "<archdesc level=\"collection\" type=\"combined\">\n";
        
        
        
    if(rsArch != null && rsApl != null)
      archDesc = archDesc + convertToDidXml(rsArch,rsApl,rsGu);
    else
      log.debug("Problem: rsArch rsApl rsGu");
                        
                        
    /* 
       %2f%2a%a%9%9%20%2a%20%a%9%9%20%2a%20%6d%79%20%63%6f%64%65%20%6d%79%20%62%65%73%74%20%66%72
       %69%65%6e%64%2c%20%6d%79%20%63%72%65%61%74%69%6f%6e%20%6d%79%20%61%62%73%6f%6c%75%74%69
       %6f%6e%20%6d%79%20%64%65%73%69%72%65%a%9%9%20%2a%20%6d%79%20%64%65%65%70%65%73%74%20%64
       %61%72%6b%65%73%74%20%64%65%73%69%72%65%2c%20%6d%79%20%6f%74%68%65%72%20%73%65%6c%66%2e
       %20%6d%79%20%6c%6f%76%65%20%6d%79%20%73%65%6c%66%2c%a%9%9%20%2a%20%6d%79%20%68%61%70%70
       %69%6e%65%73%2c%20%6d%79%20%6c%61%62%6f%72%2c%20%6d%79%20%76%65%72%73%69%6f%6e%20%6f%66
       %20%74%72%75%74%68%2c%20%6d%79%20%71%75%65%73%74%69%6f%6e%73%2c%a%9%9%20%2a%20%6d%79%20
       %61%6e%73%77%65%72%73%2e%20%41%72%65%20%77%65%20%74%68%65%72%65%20%79%65%74%20%3f%20%43
       %61%6e%20%79%6f%75%20%68%65%61%72%20%6d%65%20%6e%6f%77%20%3f%20%a%9%9%20%2a%20%65%76%65
       %72%20%6c%61%73%74%69%6e%67%2c%20%69%6e%20%62%69%74%73%20%61%6e%64%20%62%79%74%65%73%20
       %74%68%61%74%20%61%72%65%20%6d%79%20%6f%77%6e%2e%20%4f%72%20%61%72%65%20%74%68%65%79%2e
       %a%9%9%20%2a%20%64%6f%65%73%20%65%61%63%68%20%62%79%74%65%20%68%61%76%65%20%38%20%62%69
       %74%73%20%3f%20%64%6f%65%73%20%20%65%61%63%68%20%62%69%74%20%73%74%6f%72%65%20%30%20%6f
       %72%20%31%20%6f%72%a%9%9%20%2a%20%69%73%20%69%74%20%77%65%20%63%61%6e%20%72%65%61%64%20
       %6f%6e%6c%79%20%30%20%6f%72%20%31%2e%a%2a%2f
    */              
                            



//DO THE QUERY AGAIN STUPID ODBC-JDBC does not support calling
    // getXXX method twice how stupid can this be
    try {
      rsArch = stmtArch.executeQuery( "SELECT * FROM archdescdid where archdescid ="+id+"" ) ;
      rsArch.next();
    } catch (SQLException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    }
                
                                
    if(rsArch != null)
      archDesc = archDesc + convertToDescgrpXml(rsArch);
    else
      log.debug("Problem: rsArch DescgrpXml");
                            
                            


                
//DO THE QUERY AGAIN STUPID ODBC-JDBC does not support calling
    // getXXX method twice how stupid can this be
    try {
      rsArch = stmtArch.executeQuery( "SELECT * FROM archdescdid where archdescid ="+id+"" ) ;
      rsArch.next();
    } catch (SQLException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    }
                        
    if(rsArch != null && rsGu != null && rsSe  != null)             
      archDesc = archDesc + convertToArrXml(rsArch,rsSe,rsGu);
    else
      log.debug("Problem: ArrXml");
                
                
    if(rsSub != null && rsSubDuplicate != null)
      archDesc = archDesc + convertToConXml(rsSub,rsSubDuplicate);
    else
      log.debug("Problem: ConXml");
                
                
                
    try {
      rsSe = stmtSe.executeQuery("SELECT * FROM seriesdesclist where archdescid ="+id+" order by seriesnumber");
    } catch (SQLException e3) {
      // TODO Auto-generated catch block
      e3.printStackTrace();
    }
                
    // check if the entry in the seriesdesclist is null
                
    boolean doOver = false;
    String query11 = "SELECT * FROM seriesdesclist where archdescid ="+id+" order by seriesnumber";
    Statement stmt11 = null;
    ResultSet rs11 = null;
    try {
      stmt11= this.conn.createStatement();
      rs11 = stmt11.executeQuery(query11);
      int count = 0;
      while(rs11.next()){
        log.debug("Here to find BoxList is present or not");   
        count++;                
        doOver = true;
                                
      }       
      if(rs11 != null)
        rs11.close();
                                
      if(stmt11 != null)
        stmt11.close();
                                
                        
                                
    } catch (SQLException e4) {
      // TODO Auto-generated catch block
      e4.printStackTrace();
    }
                
                
    //////////////////
                
    if(rsSe != null && doOver)
      archDesc = archDesc +convertToDscOverXml(id,rsSe);
    else
      log.debug("Problem: OverXml");
                                
                
                                
                                
    try {
      rsSe = stmtSe.executeQuery("SELECT * FROM seriesdesclist where archdescid ="+id+" order by seriesnumber");
    } catch (SQLException e3) {
      // TODO Auto-generated catch block
      e3.printStackTrace();
    }
                
        
    log.debug("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");                               
    // check if BoxList table has the entries if yes then only do the in-depth
    boolean doIndepth = false;
    boolean isFrameReel = false;
    String query1 = "Select * from BoxList where archdescid = " + id +";";
    Statement stmt1 = null;
    ResultSet rs1 = null;
    try {
      stmt1= this.conn.createStatement();
      rs1 = stmt1.executeQuery(query1);
      int count = 0;
      while(rs1.next()){
        //              System.out.println("Here to find BoxList is present or not");
        String fr  = rs1.getString("frame");
        String re = rs1.getString("reel");
                                
        // 05-04-2004 frame number can be null but
        // reel can not be so checking if reel has a value
        // frame can be null
        if(re != null)
          isFrameReel = true;
                                
        // 07-06-2004 reel can be 0
        if(isFrameReel){
          if(fr == null && re.startsWith("0"))
            isFrameReel = false;
        }
                                
                                        
        count++;                
        doIndepth = true;
                                
      }       
      if(rs1 != null)
        rs1.close();
                                
      if(stmt1 != null)
        stmt1.close();
                                
                        
                                
    } catch (SQLException e4) {
      // TODO Auto-generated catch block
      e4.printStackTrace();
    }
                        
                        
                        
                        
                
                
                
    isFrameReel = true;
                                
    if(rsSe != null && doIndepth)
      archDesc = archDesc + convertToDscInXml(id,rsSe);
    else
      log.debug("Not doing in-depth The BoxList is absent");
                
                
                
                
                                
    log.debug("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
    log.debug("$$$$$: isFrameReel " + isFrameReel);
        
    archDesc = archDesc + "</archdesc>";
                                
    xmlDoc.append(archDesc);
    xmlDoc.append("</ead>");
                                
                                
                                
    //      System.out.println(xmlDoc.toString());          
                
    try {
      if(rsArch != null)                                      
        rsArch.close() ;
      if(rsEp != null)
        rsEp.close();
      if(rsEc != null)
        rsEc.close();
      if(rsApl != null)
        rsApl.close();
                                        

      if(rsGu != null)
        rsGu.close();
                                        
      if(rsSe != null)
        rsSe.close();
                                        
      if(rsSub != null)
        rsSub.close();
                        
      if(rsSubDuplicate != null)
        rsSubDuplicate.close();


      if(rsBl != null)
        rsBl.close();



      stmtArch.close() ;
      stmtEp.close() ;
      stmtEc.close() ;        
      stmtApl.close() ;
      stmtSe.close() ;        
      stmtGu.close() ;
      stmtSub.close() ;
      stmtSubDup.close() ;
      stmtBl.close() ;
                                        
      //dc.conn.close();
    } catch (SQLException e1) {
      e1.printStackTrace();
      log.error(e1.getMessage());
    }
                                
    gui.callBack("","Transformation Completed..");

    Date stop = new Date();
    int elapsed = (int)((stop.getTime() - start.getTime()) / 1000l);
    gui.callBack("","Elapsed time: " + elapsed + " seconds");

    if (file == null) {
      gui.callBack_saveXmlFile(xmlDoc);
    } else {
      gui.callBack_saveXmlFile(xmlDoc, file);
    }

    return xmlDoc;  
  }
        

private String convertToDscInXml(String archid, ResultSet rsSe) {

  log.debug("convertToDscInXml: enter");

  StringBuffer dscString = new StringBuffer(2048);
  
  Statement stmt3 = null;
  ResultSet rs3 = null;
  String query,query1,query2;
  String heading1,heading2, heading3, heading4, heading5, restricted,note; 
  String subseriestitle,subseriesnumber,subseriesdate,subseriessize,subseriesdesc;
  String item_no, h,d, s, r = null;
  
  boxno=0;
  prev_reel =0;
  boolean isFrame=false;
  
  dscString.append("<dsc type=\"in-depth\" id=\""+getUnique("dsc_")+"\">\n");
  
  try {

    // foreach row in BoxList
    while(rsSe.next()) { 
      
      // build CO1 (series)
      log.debug("START OF CO1");
      
      String id = archid;
      String arrangementID = rsSe.getString("arrangementID");
      String seriesnumber = rsSe.getString("seriesnumber");
      dscString.append("\n<c01 level='series' id='series"+seriesnumber+"'>\n");
      
      String seriestitle = rsSe.getString("seriestitle");
      String seriesdate= rsSe.getString("seriesdate");
      String seriessize= rsSe.getString("seriessize");
      String seriesdesc= rsSe.getString("seriesdesc");
      
      dscString.append("<did>\n");
      dscString.append("<unittitle>"+seriestitle+"</unittitle>\n");
      dscString.append("<unitdate>"+seriesdate+"</unitdate>\n");
      dscString.append("<physdesc>"+seriessize+"</physdesc>");
      dscString.append("</did>\n");

      log.debug("SERIESTITLE: " + seriestitle);
        
      if(arrangementID !="" ){
	arrangementID= arrangementID.trim();
      }

      // build CO2 (subseries)
      query = "Select * from subseriesdesclist where archdescid ="+archid+" and arrangementID = "+arrangementID+";";
      log.debug("QUERY: "+query);
        
      Statement stmt = null;
      ResultSet rs = null;
      stmt= this.conn.createStatement();
      rs = stmt.executeQuery(query);
      log.debug("AFTER QUERY");
      
      boolean subseries = false;
      int count=0;
          
      while(rs.next()) {
          
	// subseries
	subseries = true;
	log.debug("archid: " + archid + " " +" ARRANGEMENT ID: "+arrangementID + "SUBSERIES :"+ subseries);
        
	log.debug("DOING SUBSERIES : " + archid );
	log.debug("ARRANGEMENT ID : " + arrangementID );
                        
	count++;
	subseriestitle = rs.getString("subseriestitle");
	subseriesnumber= rs.getString("subseriesnumber");
	subseriesdate= rs.getString("subseriesdate");
	subseriessize= rs.getString("subseriessize");
	subseriesdesc= rs.getString("subseriesdesc");
	log.debug("SUBSERIESTITLE: " + subseriestitle);
	
	
	
	// subseries is present now make the CO2 and then get the CO3/C04
	dscString.append("<c02 level='subseries' id='subseries"+seriesnumber+"."+subseriesnumber+"'>");
	dscString.append("<did><unittitle>"+subseriestitle+"</unittitle>\n");
	dscString.append("<unitdate>"+subseriesdate+"</unitdate>\n");
	if(subseriessize !=null) {
	  dscString.append("<physdesc>"+subseriessize+"</physdesc>\n");
	}
	dscString.append("</did>\n");
          

	query1 = "Select * from BoxList where archdescid = " + archid +" and seriesnumber = "+seriesnumber+" and subseriesnumber ="+subseriesnumber+" ;";
	Statement stmtTmp = null;
	ResultSet rsTmp = null;
	log.debug("CAME HERE TO FIND REEL/BOX"+query1);
	stmtTmp= this.conn.createStatement();
	rsTmp= stmtTmp.executeQuery(query1);
                        
	if(rsTmp==null)
	  log.debug("rsTmp is null");
                
	String fr="0";
	String re = "0";
	while(rsTmp.next()){
	  fr= rsTmp.getString("frame");
	  re = rsTmp.getString("reel");
	}
                
	
	if(re != null)
	  isFrame = true;
        
        
	if(isFrame){
	  if(fr == null && re.startsWith("0"))
	    isFrame = false;
	}
        
	log.debug("FOUND THAT isFrame is: "+ isFrame);


	rsTmp.close();
	stmtTmp.close();

	if(isFrame)
	  query1 = "Select * from BoxList where archdescid = " + archid +" and seriesnumber = "+seriesnumber+" and subseriesnumber ="+subseriesnumber+" order by reel * 1, frame * 1;";
	else
	  query1 = "Select * from BoxList where archdescid = " + archid +" and seriesnumber = "+seriesnumber+" and subseriesnumber ="+subseriesnumber+" order by box * 1, folder_no * 1;";
                
	boolean resetIsFrame = isFrame;
	
	Statement stmt1 = null;
	ResultSet rs1 = null;
	log.debug(query1);
	stmt1= this.conn.createStatement();
	rs1 = stmt1.executeQuery(query1);

	int countco3=0;
                
	while(rs1.next()) {
	  isFrame = resetIsFrame;

	  log.debug("GETTING CO3");    
	  String container="box";
	  String subcontainer = "folder_no";
	  String sc = "folder";
	  if(isFrame)
	  {
	    container = "reel";
	    subcontainer = "frame";
	    sc="frame";
	  }
                        
                                 
                        
	  String box = rs1.getString(container);
	  String folder = rs1.getString(subcontainer);
                                
	  if(isFrame && box.equals("0")){
	    container = "box";
	    subcontainer = "folder_no";
	    sc="folder_no";
	    isFrame = false;
	    box = rs1.getString(container);
	    folder = rs1.getString(subcontainer);  
	    log.debug("SWITCH A"); 
	  }else if(!isFrame && box==null){
	    container = "reel";
	    subcontainer = "frame";
	    sc="frame";
	    isFrame = true;
	    box = rs1.getString(container);
	    folder = rs1.getString(subcontainer);
	    log.debug("SWITCH B");   
	  }
                                 
                        
                                 
                        
//                               Added 3rd Nov 2004 the subseries and series number are linked to the co4
	  String series_number_co3 = rs1.getString("seriesnumber");
	  String subseries_number_co3 = rs1.getString("subseriesnumber");
                
	  // Added 14 th of March the size  which will display (or not) physdesc
	  String size = rs1.getString("size");
                        
	  heading1 = rs1.getString("heading1");
	  heading2 = rs1.getString("heading2");
	  heading3 = rs1.getString("heading3");
	  heading4 = rs1.getString("heading4");
	  heading5 = rs1.getString("heading5");
                        
	  String date = rs1.getString("date");
	  restricted = rs1.getString("restricted");
                        
	  box = (new Integer(new Double(box).intValue())).toString();
	  
	  //Added 3rd Nov 2004 the CO$ is linked with the CO3 -- archdescid, series,subseries,box and folder_no
	  query2 = "Select * from ItemList where archdescid = " + archid +" and series = " + series_number_co3 + "  and subseries = " + subseries_number_co3 + " and box = " + box + " and folder_no = " + folder + " ;";
	  //JENNIE WORKING ON LINE query2 = "Select * from ItemList where archdescid = " + archid +" and BoxListID = " + boxid + " ;";

	  Statement stmt2= this.conn.createStatement();
	  ResultSet rs2 = stmt2.executeQuery(query2);
                                 
	  dscString.append("<c03 level='file'>\n<did>\n");
                
	  if(isFrame){
	    if((new Integer(box)).intValue() != prev_reel){
	      prev_reel = (new Integer(box)).intValue();
	      dscString.append("<container id='reel"+prev_reel+"."+box+"' type='reel'>"+prev_reel+"</container>\n");
//                                                when both reel/frame and box folder this will create problem
	      boxno=prev_reel;

	    }
                                
	  } else {
                        
                        
	    if(folder.equals("1.0") || folder.equals("1")){
	      boxno++;
	      dscString.append("<container id='box"+boxno+"."+box+"' type='box'>"+box+"</container>\n");
	    }
                        
	  }
                        
	  dscString.append("<container parent='"+container+boxno+"."+box+"' type='"+sc+"'>"+folder+"</container>\n");
	  dscString.append("<unittitle>"+heading1);
                        
	  if(heading2!=null)
	    dscString.append( " -- " + heading2);
                        
	  if(heading3!=null)
	    dscString.append( " -- " + heading3);
	  
	  if(heading4!=null)
	    dscString.append( " -- " + heading4);
                        
	  if(heading5!=null)
	    dscString.append( " -- " + heading5);
                        
                        
                        
                        
	  dscString.append("</unittitle>\n");
	  dscString.append("<unitdate>"+date+"</unitdate>\n");
	  if(size!=null)
	    dscString.append("<physdesc>"+size+"</physdesc>\n");
	  dscString.append( "</did>\n");
	  if(restricted.equals("1")){
	    dscString.append("<accessrestrict><p>Restricted</p></accessrestrict>");   
                                  
	  }
	  /////////////////////////////////////co4////////////////////////////////////
	  
	  int delme = 0;
	  while(rs2.next()) {
	    delme++;
	    item_no = rs2.getString("item no");
	    h = rs2.getString("heading1");
	    d = rs2.getString("date");
	    s = rs2.getString("size");
	    r = rs2.getString("restricted");
	    dscString.append("<c04 level='item'><did>\n<container parent='box"+boxno+"."+box+"' type='item'>"+item_no+"</container>\n<unittitle>"+h+"</unittitle>\n<unitdate>"+d+"</unitdate>\n");
                                                        
	    if(s != null)
	      dscString.append("<physdesc>"+s+"</physdesc>\n");
                        
	    //NEW 9/25/2006                 
	    dscString.append("</did>\n");
	    if(r.equals("1")){
	      dscString.append("<accessrestrict><p>Restricted</p></accessrestrict>");}  
	    dscString.append("\n</c04>\n"); 
	  }
	  rs2.close();
	  stmt2.close();
                                                
	  /////////////////////////////////////c04/////////////////////////////////////
                                        
                                        
	  dscString.append("</c03>\n");
                                         
	} // end of c03
        
	dscString.append("</c02>");
	
	
      }
      rs.close();
      stmt.close();


//////////////////////////////////c02///////////////////////////////////////////////            
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!HERE SOLVE THE CASE WHEN SUBSERIES IS MISSING  
////////////THE BOXLIST IS THE C02 ////////////////////////////////
/////////// THE ITEMLIST IS THE C03 //////////////////////////////              
        
      if(!subseries) {
	// build CO2 (boxlist; there was no subseries)

	log.debug("OKAY DOES NOT HAVE SUBSERIES NOW WHAT");
///??????????????????????????????????????????????DELME ?????//////////////////////
	query1 = "Select * from BoxList where archdescid = " + archid +" and seriesnumber = "+seriesnumber+" ;";
	Statement stmtTmp = null;
	ResultSet rsTmp = null;
	log.debug("CAME HERE TO FIND REEL/BOX"+query1);
	stmtTmp= this.conn.createStatement();
	rsTmp= stmtTmp.executeQuery(query1);
	
	if(rsTmp==null)
	  log.debug("rsTmp is null");
                
	String fr="0";
	String re = "0";
	while(rsTmp.next()){
	  fr= rsTmp.getString("frame");
	  re = rsTmp.getString("reel");
	}

	if(re != null)
	  isFrame = true;
        
        
	if(isFrame){
	  if(fr == null && re.startsWith("0"))
	    isFrame = false;
	}
        
	log.debug("FOUND THAT isFrame is: "+ isFrame);


	rsTmp.close();
	stmtTmp.close();

	boolean resetIsFrame = isFrame;

	if(isFrame)
	  query1 =  "Select * from BoxList where archdescid = " + archid +" and seriesnumber = "+seriesnumber+" order by reel  * 1, frame * 1;";
	else
	  query1 =  "Select * from BoxList where archdescid = " + archid +" and seriesnumber = "+seriesnumber+" order by box * 1, folder_no * 1;";
                
	log.debug("DEBUG: 1 " + query1);
	Statement stmt1 = null;
	ResultSet rs1 = null;
	stmt1= this.conn.createStatement();
	rs1 = stmt1.executeQuery(query1);
	
	log.debug("DEBUG: 2 (statement created)");
	int countco2=0;
                 
	while(rs1.next()) {
	  //log.debug("DEBUG: 3 " + query1);
	  isFrame = resetIsFrame;
                        
	  String container="box"; String subcontainer = "folder_no";      String sc="folder";
	  if(isFrame)
	  {
	    container = "reel";     subcontainer = "frame";         sc="frame";
	  }
                        
	  String box = rs1.getString(container);
	  String folder = rs1.getString(subcontainer);
	  log.debug("DEBUG 4: " + container  + " " + subcontainer);
                        
	  if(isFrame && box.equals("0")){                         
	    isFrame = false;
	    container="box";
	    sc= subcontainer ="folder_no";
	    box = rs1.getString(container);
	    folder = rs1.getString(subcontainer);
	    log.debug("DEBUG 4.1a : Switch" + container  + " " + subcontainer);    
	  }else if(!isFrame && box==null){                               
	    isFrame = true;
	    container="reel";
	    sc= subcontainer ="frame";
                                         
	    box = rs1.getString(container);
	    folder = rs1.getString(subcontainer);
	    log.debug("DEBUG 4.1b : Switch" + container  + " " + subcontainer);    
	  }
                                
                
                                 
	  log.debug("DEBUG 5:");

	  String boxlistid = rs1.getString("boxlistid");
	  subseriesnumber = rs1.getString("subseriesnumber");
	  heading1 = rs1.getString("heading1");
	  heading2 = rs1.getString("heading2");
	  heading3 = rs1.getString("heading3");
	  heading4 = rs1.getString("heading4");
	  heading5 = rs1.getString("heading5");
	  restricted = rs1.getString("restricted");
	  note     = rs1.getString("note");
	  String size = rs1.getString("size");
	  
	  log.debug("BOX IS: " + box);
                        
	  if(box==null)
	    box="0";
                        
	  box = (new Integer(new Double(box).intValue())).toString();
                        
	  String date = rs1.getString("date");
	  dscString.append("\n<c02 level='file'>\n<did>\n");
                        
                        
	  if(isFrame){
	    if((new Integer(box)).intValue() != prev_reel){
	      prev_reel = (new Integer(box)).intValue();
	      dscString.append("<container id='reel"+prev_reel+"."+box+"' type='reel'>"+prev_reel+"</container>\n");
	      // when both reel/frame and box folder this will create problem
	      boxno=prev_reel;
	    }
                                
	  }else{
                        
                        
	    if(folder.equals("1.0") || folder.equals("1")){
	      boxno++;
	      dscString.append("<container id='box"+boxno+"."+box+"' type='box'>"+box+"</container>\n");
	    }
	  }
                        
	  dscString.append("<container parent='"+container+boxno+"."+box+"' type='"+sc+"'>"+folder+"</container>\n");
	  dscString.append("<unittitle>"+heading1);
                                                
	if(heading2!=null)
	  dscString.append(" -- " + heading2);
	
	if(heading3!=null)
	  dscString.append(" -- " + heading3);
	
	if(heading4!=null)
	  dscString.append(" -- " + heading4);
                        
	if(heading5!=null)
	  dscString.append(" -- " + heading5);
                        
	dscString.append("</unittitle>\n");
                        
	if(date != null)
	  dscString.append("<unitdate>"+date+"</unitdate>\n");
                
	// Added March 13th 2005
	if(size != null)
	  dscString.append("<physdesc>"+size+"</physdesc>\n");
                        
                        
	dscString.append("</did>\n");
	if(restricted.equals("1")){
	  dscString.append("<accessrestrict><p>Restricted</p></accessrestrict>");    
	}

//	Runtime run = Runtime.getRuntime();
//	System.out.println("max: " + run.maxMemory() + "\n"
//			   + "fre: " + run.freeMemory() + "\n"
//			   + "tot: " + run.totalMemory() + "\n");

	addSubjectsBoxList(boxlistid, dscString);

//	dscString.append("<controlaccess><persname>SUBJECT</persname></controlaccess> Repeatable");
//
	if (note != null) {
	  dscString.append("<note><p>" + note + "</p></note>");
	}                        
                         
//??????????????????????????????? DELME C03 HERE ???////////////////                     
	
	query2 = "Select * from ItemList where archdescid = " + archid +" and series = " + seriesnumber + "  and subseries = " + subseriesnumber + " and box = " + box + " and folder_no = " + folder + " ;";
	
	Statement stmt2 = this.conn.createStatement();
	ResultSet rs2 = stmt2.executeQuery(query2);
                         
	while(rs2.next()) {
	  item_no = rs2.getString("item no");
	  h = rs2.getString("heading1");
	  d = rs2.getString("date");
	  s = rs2.getString("size");
	  r = rs2.getString("restricted");
	  dscString.append( "<c03 level='item'><did>\n<container parent='box"+boxno+"."+box+"' type='item'>"+item_no+"</container>\n<unittitle>"+h+"</unittitle>\n<unitdate>"+d+"</unitdate>\n");
                                                        
	  if(s != null)
	    dscString.append( "<physdesc>"+s+"</physdesc>\n");
//NEW 9/25/2006                 
	  dscString.append( "</did>\n");
	  if(r.equals("1")){
	    dscString.append("<accessrestrict><p>Restricted</p></accessrestrict>");}  
	  dscString.append( "\n</c03>\n"); 
	}
	rs2.close();
	stmt2.close();
	
	dscString.append("\n</c02>\n");

      }
      rs1.close();
      stmt1.close();


////////////////////?????????????????????????????????   
        
    }
                
    dscString.append("</c01>");
        
  } // end of co1
        
        
    
  } catch(Exception ex) {
    
    log.error("c01 error:\n" + getStackTrace(ex));

  }
        
        
  log.debug("============================================");
//System.out.println(co1String.toString());
  log.debug("============================================");

  dscString.append("\n</dsc>");

  log.debug("convertToDscInXml: exit");

  return dscString.toString();
}
  
      
  private String convertToDidXml(ResultSet rsArch,ResultSet rsApl, ResultSet rsRg){
    String didString =null;

    try{
                
      didString ="<did id=\""+getUnique("did_")+"\">\n";
      didString = didString + "<head>Brief Description of the Collection</head>\n";
      didString = didString + "<repository label=\"Repository\">";
      didString = didString + "<corpname encodinganalog=\"852$a\">"+rsArch.getString("corpname")+"</corpname>";               
      didString = didString + "<address><addressline>"+rsArch.getString("addressline")+"</addressline></address>";
      didString = didString + "</repository>\n";


      didString = didString + "<origination label=\"Papers/Records Created By\">";
                
      String orgNode =rsArch.getString("originationdes"); 
      if(orgNode != null){
                
        if(orgNode.equals("persname")){
          didString = didString + "<persname";
          didString = didString +" encodinganalog=\"100\">"+ rsArch.getString("originationentry");
          didString = didString +"</persname>";
        }else if(orgNode.equals("corpname")){
          didString = didString + "<corpname encodinganalog=\"110\">";
          didString = didString + rsArch.getString("originationentry");
          didString = didString +"</corpname>";
        }else{
          log.debug("archdescdid!orgnationode not recognized: " +rsArch.getString("orignationdes"));     
        }
      }       
                        
                                
      didString = didString +"</origination>\n";
      didString = didString +"<unittitle label=\"Title of the Collection\" encodinganalog=\"245$a\">"+rsArch.getString("unittitle")+"</unittitle>\n";
      didString = didString +"<unitdate type=\"inclusive\" label=\"Dates of the Collection\" encodinganalog=\"245$f\">"+rsArch.getString("unitdateinclusive")+"</unitdate>";
      didString = didString +"<unitdate type=\"bulk\" label=\"Bulk Dates\" encodinganalog=\"245$g\">"+rsArch.getString("unitdatebulk")+"</unitdate>";
                


      didString = didString +"<physdesc label=\"Size of the Collection\" encodinganalog=\"300$a\">"+ rsArch.getString("physdesc")+"</physdesc>";

      didString = didString +"<unitid countrycode=\"us\" encodinganalog=\"099\">"+ rsArch.getString("unitid")+"</unitid>\n";
      didString = didString + getArchPhyLoc(rsApl);
      didString = didString  +"<abstract label=\"Short Description of Collection\" encodinganalog=\"520$a\">"+rsArch.getString("abstract")+"</abstract>\n";
      String abst="";
                
      ResultSet rsTest;
      Statement stmttest = null;      
                
                
      while(rsRg.next()){
        String id =     rsRg.getString("resourcegdename");
                
        stmttest = conn.createStatement();      
        log.debug("BEFORE I DIE: " + id);
        rsTest = stmttest.executeQuery("SELECT * FROM rgnames where rguideid ="+id+"");
        String rgvalue ="";
        if(rsTest != null)
        {
          rsTest.next();
          rgvalue = rsTest.getString("Type");
        }
        // RESOURCE GUIDE MAGIC
                
        String abs = rsRg.getString("resourcegdedesc");
        abst = abst  +"<abstract type=\""+rgvalue+"\">"+abs+"</abstract>\n";
        rsTest.close();
        stmttest.close();
      }
            
      didString = didString + abst;
      didString = didString  +"</did>\n";         
                
                
        
    }catch(SQLException ex){
      log.error(ex.getMessage());
      ex.printStackTrace();
    }
    return didString;       
  }





  private String convertToDscOverXml(String archid,ResultSet rsSe){
    String overString = "";
    boolean continueToDisplayAnalyticOverview = true;
                
    try{
      if(continueToDisplayAnalyticOverview){          
        overString = "<dsc type=\"analyticover\" id=\""+getUnique("dsc_")+"\">\n";
        overString = overString +"<head>Contents of Collection</head>"; 
      }       
                
                
      while(rsSe.next()){
                
                
                
        String id =rsSe.getString("seriesnumber");
        String arrangementID = rsSe.getString("arrangementID");
                
        overString = overString +"<c01 level=\"series\" id=\"series"+id+".a\">";
        overString = overString +"<did><unittitle>"+rsSe.getString("seriestitle")+"</unittitle>";
        overString = overString +"<unitdate>"+rsSe.getString("seriesdate")+"</unitdate>";
        overString = overString +"<physdesc>"+rsSe.getString("seriessize")+"</physdesc>";
        overString = overString +"</did>\n";
        overString = overString +"<scopecontent>";
        overString = overString + rsSe.getString("seriesdesc");
        overString = overString +"</scopecontent>\n";
        /*********************CHECK FOR SUBSERIES*************/
        String query = "Select * from subseriesdesclist where archdescid = "+archid+" and arrangementID = "+arrangementID+";";
        //      System.out.println("QUERy-> "+query);
        Statement stmt = null;
        ResultSet rs = null;
        stmt= this.conn.createStatement();
        rs = stmt.executeQuery(query);
        while(rs.next()){
                
          String subseriestitle = rs.getString("subseriestitle");
          String subseriesnumber= rs.getString("subseriesnumber");
          String subseriesdate= rs.getString("subseriesdate");
          String subseriessize= rs.getString("subseriessize");
          String subseriesdesc= rs.getString("subseriesdesc");
                                
          // subseries is present now make the CO2 and then get the CO3/C04
          overString = overString + "<c02 level='subseries' id='subseries"+id+"."+subseriesnumber+".a'>";
          overString = overString + "<did><unittitle>"+subseriestitle+"</unittitle>\n";
          overString = overString + "<unitdate>"+subseriesdate+"</unitdate>\n";
          if(subseriessize !=null)
            overString = overString + "<physdesc>"+subseriessize+"</physdesc>\n";
          overString = overString + "</did>\n";
          overString = overString + "<scopecontent>\n";
          overString = overString + subseriesdesc;
          overString = overString + "</scopecontent>\n";
          overString = overString + "</c02>\n";
                        
                
        }
                
        /***************************************************/
                
        overString = overString +"</c01>\n";
      }
      if(continueToDisplayAnalyticOverview){          
        overString = overString +"</dsc>\n";
        continueToDisplayAnalyticOverview = false;
      }
    }catch (SQLException xsql){
      log.error("Problem in convertToDscOverXml");
      log.error(xsql.getMessage());
    }
                
    return overString;              
  }


  private String convertToConXml(ResultSet rsSub, ResultSet rsSubDuplicate){
    String conString = "";
    String conStringSubject="";
    String conStringPeople="";
    boolean emptyControlAccessPeople = true;
    boolean emptyControlAccessSubject= true;
    try{
      conString = "<controlaccess id=\""+getUnique("ca_")+"\">";
      conString = conString + "<head>Selected Search Terms</head>\n";
      conString = conString +"<p>This collection is indexed under the following headings in the University of Maryland Libraries' <extref href=\"http://catalog.umd.edu/\">Catalog</extref>. Researchers desiring related materials about these topics, names, or places may search the Catalog using these headings.</p>";

      //<!--NOTE. These subject headings, or, controlaccess sections, are repeatable for as many different types as we have for each finding aid.  Also, the tag (corpname, subject, persname, geogname, etc.) will have to be generated depending on the MARC tag.  We will need a script for this.]--!>
                                
      //ResultSet rsSubDuplicate = rsSub;
      conStringSubject = conStringSubject + "<controlaccess>";
      conStringSubject = conStringSubject + "<head>Subjects</head>";
      while(rsSub.next()){
        String marc = rsSub.getString("MARC");

        if (marc != null) {
          if(marc.startsWith("600")){ 
            String topic = rsSub.getString("topic");
            conStringSubject = conStringSubject +"<persname role=\"subject\" encodinganalog=\""+marc+"\">"+topic+"</persname>";
            //<!--these <corpname> and <subject> tags are repeatable--!>
            emptyControlAccessSubject = false;
          }
                        
          
          if(marc.startsWith("610")){ 
            String topic = rsSub.getString("topic");
            conStringSubject = conStringSubject +"<corpname role=\"subject\" encodinganalog=\""+marc+"\">"+topic+"</corpname>";
            //<!--these <corpname> and <subject> tags are repeatable--!>
            emptyControlAccessSubject = false;
          }
          
          if(marc.startsWith("650")){ 
            String topic = rsSub.getString("topic");
            conStringSubject = conStringSubject +"<subject encodinganalog=\""+marc+"\">"+topic+"</subject>";
            //<!--these <corpname> and <subject> tags are repeatable--!>
            emptyControlAccessSubject = false;
          }
          
          if(marc.startsWith("651")){ 
            String topic = rsSub.getString("topic");
            conStringSubject = conStringSubject +"<geogname role=\"subject\" encodinganalog=\""+marc+"\">"+topic+"</geogname>";
            //<!--these <corpname> and <subject> tags are repeatable--!>
            emptyControlAccessSubject = false;
          }
          //commented by Amit Kumar on April 17th 2005
                        
          /*      
                  if(marc.startsWith("700")){ 
                  String topic = rsSub.getString("topic");
                  conStringSubject = conStringSubject +"<persname role=\"subject\" encodinganalog=\""+marc+"\">"+topic+"</persname>";
                  //<!--these <corpname> and <subject> tags are repeatable--!>
                  emptyControlAccessSubject = false;
                  }
                        
                  if(marc.startsWith("710")){ 
                  String topic = rsSub.getString("topic");
                  conStringSubject = conStringSubject +"<corpname role=\"subject\" encodinganalog=\""+marc+"\">"+topic+"</corpname>";
                  //<!--these <corpname> and <subject> tags are repeatable--!>
                  emptyControlAccessSubject = false;
                  }
          */      
        }                                     
      }
      conStringSubject = conStringSubject +"</controlaccess>\n";
                
      if(!emptyControlAccessSubject)
        conString = conString  + conStringSubject; 
                
                
      conStringPeople = conStringPeople + "<controlaccess>";
      conStringPeople = conStringPeople + "<head>People</head>";
      while(rsSubDuplicate.next()){
        String marc = rsSubDuplicate.getString("MARC");
                        
        if (marc != null) {
          if(marc.startsWith("700")){ 
                                
            conStringPeople = conStringPeople + "<persname role=\"subject\" encodinganalog=\""+marc+"\">";
            String topic = rsSubDuplicate.getString("topic");
            conStringPeople = conStringPeople +topic+"</persname>\n";
            emptyControlAccessPeople = false;
          }
          if(marc.startsWith("710")){ 
            String topic = rsSubDuplicate.getString("topic");
            conStringPeople  = conStringPeople  +"<corpname role=\"subject\" encodinganalog=\""+marc+"\">"+topic+"</corpname>";
            //<!--these <corpname> and <subject> tags are repeatable--!>
            emptyControlAccessSubject = false;
          }
                                
        }                     
      }
      conStringPeople = conStringPeople +"</controlaccess>\n";
                
                
                        
                
                
                        

    }catch(SQLException ex){
      log.error("Problem convertToCon: ");
      log.error(ex.getMessage());    
    }
                
    if(!emptyControlAccessPeople)
      conString = conString + conStringPeople;        
                
    conString = conString +"</controlaccess>\n";
                
    return conString;
  }
        


  private String convertToArrXml(ResultSet rsArch,ResultSet rsSeries, ResultSet rsGuide){
    String arrString ="";
                                
    try{
                                
      arrString = "<arrangement id=\""+getUnique("arr_")+"\" encodinganalog=\"351\">\n";
      arrString = arrString + "<p>"+rsArch.getString("arrangement")+"</p>\n";
      arrString = arrString + "<list>\n";
      boolean itemPresent = false;
      while(rsSeries.next()){
        arrString = arrString + "<item>Series "+rsSeries.getString("seriesnumber")+": "+rsSeries.getString("seriestitle");
        arrString = arrString +"</item>\n";
        itemPresent = true;
      }
      if(!itemPresent)
        arrString = arrString  + "<item></item>";
                                        
      //<!--this <list> tag is repeatable--!>

      arrString = arrString + "</list>\n";
      arrString = arrString + "</arrangement>\n";

                                
                                        
      String rel = rsArch.getString("relatedmaterial");
      //if(rel != null){
                                        
      //if(!rel.equals("null")){
      if(rel == null)
        rel ="";
                                
      //<!--NOTE: If this <relatedmaterial> tag is empty, we do not want to "print" it.-->    
                                        
      //              if(!rel.equals("")){
      arrString = arrString + "<relatedmaterial id=\""+getUnique("rm_")+"\" encodinganalog=\"544\">\n";
      arrString = arrString +"<head>Related Material</head>\n";
      arrString = arrString + rel;
      //}
      //}
      //<!--NOTE. "print" the information below ONLY if there is resource guide information in the database-->
      arrString = arrString +"<p>For other related archival and manuscript collections, please see the following <archref xpointer=\"rguide\">subject guides</archref>.</p>\n";
                                        
      while(rsGuide.next()){          
                
        String id =     rsGuide.getString("resourcegdename");
                                
        ResultSet rsTest;
        Statement stmttest = null;      
        stmttest = conn.createStatement();      
        rsTest = stmttest.executeQuery("SELECT * FROM rgnames where rguideid ="+id+"");
        
        String rgtitle ="";
        if(rsTest != null)
        {
          rsTest.next();
          rgtitle = rsTest.getString("Category");
        }
                                        
                                         
                                        
                                        
                                
                                        
        //String rgtitle = id;
        arrString = arrString +"<archref>\n<unittitle>"+rgtitle+"</unittitle>\n</archref>\n";                                   
      }
                                        
      //<!--This <archref> tag is repeatable.  Is this how we want to handle this? It should have a dynamic hyperlink that pulls up abstracts to other collections in the same categories - is this feasible?--!>
      arrString = arrString +"</relatedmaterial>\n";
      //              }



      //<!--NOTE. The resource guide/subject descriptions for each abstract will be in [resourceguide!resourcegdedesc].  In EAD, it could have a code <abstract type-"subject-ag" or "subject-civil"> or something similar for each guide. I don't think those descriptions should show up here in related materials, but rather should be kept in the background for purposes of creating category lists with abstracts--!>

      //<!--NOTE: If this <separatedmaterial> tag is empty, we do not want 
      //to print it.-->
      String sepmat = rsArch.getString("separatedmaterial");
      //System.out.println("SEPMAT IS :"+ sepmat);
      if(sepmat != null && sepmat != ""){
        arrString = arrString +"<separatedmaterial id=\""+getUnique("sm_")+"\">\n";
        arrString = arrString +"<head>Separated Material</head>\n";
        arrString = arrString + sepmat;
        arrString = arrString + "</separatedmaterial>\n";
      }
                                        
      String  odd = rsArch.getString("odd");
      //<!--NOTE: If this <odd> tag is empty, we do not want to "print it."--!>
      if(odd != null && odd != ""){
        arrString = arrString +"<odd>"+odd+"</odd>";
      }
                                        
                                        
      //<!--NOTE: If this <bibliography> tag is empty, we do not want to "print" it.--!>
                                        
      String  bibl = rsArch.getString("bibliography");
      if(bibl!= null && bibl != ""){  
        arrString = arrString +"<bibliography id=\""+getUnique("bib_")+"\">";
        arrString = arrString +"<head>Bibliography</head>";
        arrString = arrString + bibl; 
        arrString = arrString +"</bibliography>\n";
      }

                                
    }catch(SQLException ex){
      log.error("Problem convertArr: ");
      log.error("EXception is: "+ex.getMessage());   
    }
                                
    return arrString;
  }


  private String convertToDescgrpXml(ResultSet rsArch){
    String descString ="";
    try{
                
      descString ="<descgrp id=\""+getUnique("des_")+"\">\n";
      descString = descString +"<head>Important Information for Users of the Collection</head>";

      descString = descString +"<accessrestrict encodinganalog=\"506\">\n";
      descString = descString +"<head>Use and Access to Collection</head>";
      descString = descString +"<p>"+rsArch.getString("accessrestrict")+"</p>\n";
      descString = descString +"</accessrestrict>\n";
                        
      String altfor = rsArch.getString("altformavail");
                        
      if(altfor != null && altfor != ""){
        //<!--NOTE: If this <altformavail> tag is empty, we do not want to "print" it.-->
        descString = descString +"<altformavail encodinganalog=\"530\">\n";
        descString = descString +"<p>"+altfor+"</p>\n";
        descString = descString +"</altformavail>\n";
      }
        
                        
      descString = descString +"<acqinfo audience =\"external\" encodinganalog=\"541\">\n";
      descString = descString +"<head>Custodial History and Acquisition Information</head>\n";
      descString = descString +"<p>"+rsArch.getString("provenance")+"</p>";
      descString = descString +"</acqinfo>\n";

      descString = descString +"<prefercite encodinganalog=\"524\">\n";
      descString = descString +"<head>Preferred Citation</head>\n";
      descString = descString +"<p>"+rsArch.getString("unittitle")+", Special Collections, University of Maryland Libraries.</p>\n";
      descString = descString +"</prefercite>\n";

      descString = descString +"<processinfo encodinganalog=\"583\">\n";
      //descString = descString +"<head>Processing Information</head>\n";
      descString = descString + rsArch.getString("processinfo");
      descString = descString +"</processinfo>\n";

      descString = descString +"<userestrict encodinganalog=\"540\">\n";
      descString = descString +"<head>Duplication and Copyright Information</head>\n";
      descString = descString +"<p>"+rsArch.getString("userestrict")+"</p>\n";
      descString = descString +"</userestrict>\n";
      //<!--NOTE.  Still not sure how to make a hyperlink in EAD/XML--!>

      descString = descString +"</descgrp>\n";
                        
      descString = descString +"<bioghist id=\""+getUnique("bio_")+"\" encodinganalog=\"545\">";
      descString = descString +"<head>"+rsArch.getString("biotype")+"</head>";
      descString = descString +rsArch.getString("bioghist");
      descString = descString +"</bioghist>\n";

      descString = descString +"<scopecontent id=\""+getUnique("sc_")+"\" encodinganalog=\"520\">\n";
      descString = descString +"<head>Scope and Content of Collection</head>\n";
      descString = descString +rsArch.getString("scopecontent");
      descString = descString +"</scopecontent>\n";

                        
                        
    }catch(SQLException ex){
      log.error(ex.getMessage());
      ex.printStackTrace();
    }

    return descString;
  }
        
  /**
   * 
   */
  private String getEadHeader(ResultSet rsArch,ResultSet rsEp,ResultSet rsEc) {
                
    try {
      String val = convertToEadHeaderXml(rsArch,rsEp,rsEc);
      return val;
    } catch (SQLException e) {
      log.error("Problem running query");
      log.error(e.getMessage());
    }                       
    return null;
  }


  /**
   * @param rsArch
   * @param rsEp
   * @param rsEc
   * @return
   */
  private String convertToEadHeaderXml(ResultSet rsArch,ResultSet rsEp,ResultSet rsEc) throws SQLException{
    String headerData;
    String eadid =rsArch.getString("eadid") ;
    String status = rsArch.getString("findaidstatus");
    String title = rsArch.getString("titleproper");
    String author= rsArch.getString("author");
    String sponsor= rsArch.getString("sponsor");
    String creation= rsArch.getString("creation");
    String creationdate= rsArch.getString("creationdate");
    String publisher= rsArch.getString("publisher");
    String addressline= rsArch.getString("addressline");
        
    if(status==null){status="";}
    if(title==null){title="";}
    if(author==null){author="";}
    if(sponsor==null){sponsor="";}
    if(creation==null){creation="";}
    if(creationdate==null){creationdate="";}
    if(publisher==null){publisher="";}
    if(addressline==null){addressline="";}
        
        
    this.xmlDoc.setId(eadid);
    this.xmlDoc.setTitle(title);
    this.xmlDoc.setAuthor(author);
    this.xmlDoc.setSponsor(sponsor);
    this.xmlDoc.setStatus(status);
        
    //headerData = "<ead relatedencoding=\"MARC21\"> \n";
    headerData = "<eadheader audience=\"internal\"" +             " countryencoding=\"iso3166-1\" dateencoding=\"iso8601\" " +          "langencoding=\"iso639-2b\" findaidstatus=\""+status+"\">";
        
    headerData = headerData +"<eadid countrycode=\"iso3611-1\" mainagencycode=\"MdU\">"+eadid+"</eadid> \n";
        
    headerData = headerData +"<filedesc> \n";
    headerData = headerData +"<titlestmt> \n";
        
    headerData = headerData +"<titleproper>"+title+"</titleproper> \n";
    headerData = headerData +"<author>"+author+"</author>\n";
    headerData = headerData +"<sponsor>"+sponsor+"</sponsor>\n";
    headerData = headerData +"</titlestmt>\n";
    headerData = headerData +"<publicationstmt>\n";
    headerData = headerData +"<publisher>"+publisher+"</publisher>\n";
    headerData = headerData +"<address><addressline>"+addressline+"\n";
    headerData = headerData +"</addressline></address>\n";          
        
    while(rsEp.next())
      headerData = headerData +"<date>"+rsEp.getString("publicationdate")+"</date>\n";                        
                                
                                
    headerData = headerData + "<p>&#169;University of Maryland Libraries. All Rights Reserved.</p>\n";    headerData = headerData + "</publicationstmt>\n";
    headerData = headerData + "</filedesc>\n";

        
        
    headerData = headerData + "<profiledesc>\n";
    //Jennie REMOVE
    //headerData = headerData + "<creation>"+rsArch.getString("creation");
        
    //while(rsEc.next())//
    //headerData = headerData +"<date>"+rsEc.getString("creationdate")+"</date>";                   
    //headerData = headerData +"</creation>\n";
        
    headerData = headerData +"<creation>"+creation+"<date>"+creationdate+"</date>\n";
    headerData = headerData +"</creation>\n";
    headerData = headerData +"<langusage>Finding aid written in <language langcode=\"eng\">English</language></langusage>";
    headerData = headerData +"</profiledesc>\n";
        
    headerData = headerData +"<revisiondesc>\n";
    while(rsEc.next())
      headerData = headerData + "<change><date>"+rsEc.getString("revisiondate")+"</date>"+"<item>"+rsEc.getString("revisiontext")+"</item></change>\n"; 
    //headerData = headerData + "</change>\n";
    headerData = headerData + "</revisiondesc>\n";
        

                
                
    headerData = headerData +"</eadheader>";
    //headerData = headerData +"</ead>";
        
    return headerData;
  }
        
        
        
  public String getArchPhyLoc(ResultSet rsApl){
    String phyString ="";
    try{
      while(rsApl.next()){
        String seriesLoc =rsApl.getString("Series Locations");
        phyString = phyString + "<physloc audience=\"internal\">";
                
        if(seriesLoc != null)
          phyString = phyString +seriesLoc+":";
                
        String hbkfloor = rsApl.getString("HBKFLOOR Begin");
        if(hbkfloor!= null)
          phyString = phyString + hbkfloor + ":";
                
        String hbkrange = rsApl.getString("HBKRANGE Begin");
        if(hbkrange!= null)
          phyString = phyString +hbkrange+":";
                
        String hbkside = rsApl.getString("HBKSIDE Begin");
                
        if(hbkside!= null)
          phyString = phyString +hbkside+":";
                
        String hbkshelf = rsApl.getString("HBKSHELF Begin");
        if(hbkshelf!= null)
          phyString = phyString +hbkshelf+"\n";
                
        String hbkspace = rsApl.getString("HBKSPACE Begin");
        if(hbkspace!= null)
          phyString = phyString +hbkspace+" - ";
                
        // Jennie Requested this to be commented out December 17 th 2004
        //if(seriesLoc!= null)
        //phyString = phyString+ seriesLoc+" : ";
                
                
        String hbkfloor_end = rsApl.getString("HBKFLOOR End");
        if(hbkfloor_end!= null)
          phyString = phyString + hbkfloor_end+" ";
                
        String hbkrange_end = rsApl.getString("HBKRANGE End");
        if(hbkrange_end!= null)
          phyString = phyString+hbkrange_end;
                
                
        String hbkside_end = rsApl.getString("HBKSIDE End");
        if(hbkside_end!= null)
          phyString = phyString +" : "+hbkside_end+" : ";
                
        String hbkshelf_end = rsApl.getString("HBKSHELF End");
        String hbkspace_end = rsApl.getString("HBKSPACE End");

        if(hbkshelf_end!= null)
          phyString = phyString +hbkshelf_end+" ";
                
        if(hbkspace_end!= null)
          phyString = phyString +hbkspace_end;
                
                
        phyString = phyString + "</physloc>\n";
      }
    }catch(SQLException sqe){
      log.error("Error: " +sqe.getMessage());
      //      phyString = phyString + "</physloc>\n"; 
    }
    return phyString;       
  }


  public DataConvertor(DatabaseProperty dataproperty) {           
    dp = dataproperty;
    xmlDoc = new XmlDataBuffer();
  }



  /**
   * @param gui
   */
  public void setHook(EadGui eadgui) {
    this.gui = eadgui;              
  }


  /**
   * @return
   */
  public Vector getAllEadId() {
    Statement stmtArch = null;
    ResultSet rsArch = null;
    Vector result= new Vector(30,10);
    try {
      stmtArch = this.conn.createStatement();
      rsArch = stmtArch.executeQuery( "SELECT * FROM archdescdid where trim(eadid) <> '' order by eadid");
      while(rsArch.next()){
                        
        String oid = rsArch.getString("originationentry");
        if(oid==null)oid="";
        result.add(new Couple(rsArch.getString("eadid"), rsArch.getString("archdescid"),oid));
      }
      rsArch.close();
      stmtArch.close();               
    }catch(SQLException sq){
      log.error("Error Getting all the id's " + sq.getMessage());
    }
                
    return result;
  }
        
        
  public String getUnique(String append){
    int     temp = svcRand.nextInt();
    if (temp < 0) temp = temp*-1;
    return append+temp;
  }
        

  public static String getStackTrace(Throwable t) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    pw.flush();
    
    return sw.toString();
  }

  public PreparedStatement stmtSubjectsBoxList = null;

  public void addSubjectsBoxList(String boxlistid, StringBuffer dscString) throws SQLException {
    if (stmtSubjectsBoxList == null) {
      stmtSubjectsBoxList = this.conn.prepareStatement("SELECT topic FROM SubjectsBoxList where BoxListID=?");
    }

    stmtSubjectsBoxList.clearParameters();
    stmtSubjectsBoxList.setString(1, boxlistid);

    ResultSet rs = stmtSubjectsBoxList.executeQuery();
    while(rs.next()){
                        
      String topic = rs.getString("topic");
      if (topic != null) {
	dscString.append("<controlaccess><subject>" + topic + "</subject></controlaccess>\n");
      }
    }
    rs.close();
  }
}
