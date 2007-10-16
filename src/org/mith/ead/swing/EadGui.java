/*
 * Created on Feb 10, 2004
 * @Created by AK to provide GUI support for EAD data import from Access
 */
package org.mith.ead.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.UnsupportedEncodingException;

import java.util.Date;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;

// log4j
import org.apache.log4j.Logger;

import org.mith.ead.data.Couple;
import org.mith.ead.data.DataConvertor;
import org.mith.ead.data.DatabaseProperty;
import org.mith.ead.data.XmlDataBuffer;
import org.mith.ead.fs.FileIO;

/**
 * @author Administrator
 * 
 */
public class EadGui extends JFrame implements ActionListener{

  static Logger log = Logger.getLogger(EadGui.class);

  JToolBar toolBar;
  EadGuiActionListener  listener;
  EadGuiPreference preference;
  DatabaseProperty dp;
  DataConvertor dc;
  JTextArea jta;
  JList listEad;
  DefaultListModel listModel;
  JButton processButton;
  JButton processAllButton;
  Vector EADIDVector;
  MyListCellRenderer mlcr;
  JFileChooser  fc;
  ClassLoader cl;


  public EadGui(){
    super("EAD Finding Aids @MD 0.3a Release: 04-10-2007");
    setDefaultLookAndFeelDecorated(true);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    dp = new DatabaseProperty();
    dp.reset();
    listModel = new DefaultListModel();
    mlcr = new MyListCellRenderer();
    EADIDVector = new Vector(30,10);
    cl= this.getClass().getClassLoader();

    toolBar = new JToolBar();
    listener = new EadGuiActionListener();
    preference = new EadGuiPreference(dp,this);

    addButtons(toolBar);    
    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    contentPane.setPreferredSize(new Dimension(100,100));
    contentPane.add(toolBar,BorderLayout.NORTH);



    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BorderLayout());
    JLabel label = new JLabel("List of Finding Aids");
    topPanel.add(label,BorderLayout.NORTH);


    listEad = new JList(listModel);
    listEad.setCellRenderer(mlcr);
    listEad.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
//listEad.setLayoutOrientation(JList.VERTICAL_WRAP);

    JScrollPane scrollList = new JScrollPane(listEad);
    topPanel.add(scrollList,BorderLayout.CENTER);




    jta = new JTextArea(5,20);

    JScrollPane scrollPane = 
      new JScrollPane(jta,
		      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		      JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    scrollPane.setPreferredSize(new Dimension(150,150));
    jta.setEditable(false);



    JSplitPane jsplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				       topPanel,scrollPane);
    jsplit.setOneTouchExpandable(true);
    jsplit.setDividerLocation(400);
    contentPane.add(jsplit,BorderLayout.CENTER);
    setContentPane(contentPane);
    Dimension d =  new Dimension(getToolkit().getScreenSize().height/2,getToolkit().getScreenSize().width/2);
    setSize(d);
    setLocation(200,200);
//pack();
    validate();
    show();
  }
/**
 * @param toolBar
 */
  private void addButtons(JToolBar toolBar) {
    JButton button = new JButton();
    //cl = cl.getParent();
        
    try{
      button = new JButton(new ImageIcon(cl.getResource("images/preferences.gif")));
    }catch(Exception ex){
      log.error(ex.getMessage());
    }
    button.setActionCommand("preferences");
    button.addActionListener(this);
    button.setToolTipText("Change Preferences");
    toolBar.add(button);
        
    try{
      button = new JButton(new ImageIcon(cl.getResource("images/retrieve.gif")));
    }catch(Exception ex){
      log.error(ex.getMessage());
    }
    button.setActionCommand("retrieve");
    button.addActionListener(this);
    button.setToolTipText("Retrieve EAD Document ID");
    toolBar.add(button);

        
    toolBar.addSeparator();

        
    try{
      processButton = new JButton(new ImageIcon(cl.getResource("images/process.gif")));
    }catch(Exception ex){
      log.error(ex.getMessage());
    }
    processButton.setActionCommand("process");
    processButton.addActionListener(this);
    processButton.setToolTipText("Transform");
    toolBar.add(processButton);

    try{
      processAllButton = new JButton(new ImageIcon(cl.getResource("images/processAll.gif")));
    }catch(Exception ex){
      log.error(ex.getMessage());
    }
    processAllButton.setActionCommand("processAll");
    processAllButton.addActionListener(this);
    processAllButton.setToolTipText("Transform All");
    toolBar.add(processAllButton);

    try{
      button = new JButton(new ImageIcon(cl.getResource("images/about.gif")));
    }catch(Exception ex){
      log.error(ex.getMessage());
    }
    button.setActionCommand("about");
    button.addActionListener(this);
    button.setToolTipText("About EAD @MD");
    toolBar.addSeparator();
    toolBar.addSeparator();
    toolBar.add(button);
        
        
        
  }
  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();

    //toolBar commands
    if(command.equals("preferences"))
    {
      showPreference();
    }
        
    if(command.equals("retrieve"))
    {
      retrieve();     
    }       
        
        
    if(command.equals("process")){
      process();
    }
        
    if(command.equals("about")){
      showAbout();
    }

    if(command.equals("processAll")){
      processAll();
    }


        
  }

/**
 * 
 */
  private void showAbout() {
    // put form elements here
    JOptionPane.showMessageDialog(this,"Beast Finding Aid Data Converter.\nReleased Under GNU-GPL by Amit Kumar.\n amitku@uiuc.edu");
  }
/**
 * @param dc
 */
  public void setConvertor(DataConvertor dataconvertor) {
    dc = dataconvertor;
    dc.setHook(this);       
  }
/**
 * 
 */
  public void showPreference() {
        
    if(this.preference != null)
    {       
      this.preference.set(dp);
      this.preference.setVisible(true);
    }else{
      log.debug("PReference window is null");
    }
        
  }

  private void retrieve(){
    log.debug("Connect and Retrieve The eadid");
    listModel.removeAllElements();
    EADIDVector.removeAllElements();
    Vector data  = dc.getAllEadId();                
    for(int i=0;i< data.size(); i++){
      //      System.out.println(data.elementAt(i)+"\n");     
      listModel.add(i,((Couple)data.elementAt(i)));
      EADIDVector.add(data.elementAt(i));
      //      System.out.println(((Couple)data.elementAt(i)).eadid);
    }
    jta.setText(jta.getText()+"\n"+ "Getting eadid from database");

  }



  private void process() {
    Couple c = (Couple)listEad.getSelectedValue();
    String selected = (c != null ? c.eadid : null);

    if(selected == null || selected == ""){
      JOptionPane.showMessageDialog(this,
				    "Select an EAD ID to Transform. \n Click on retrieve button to view list.",
				    "Select EAD ID",
				    JOptionPane.WARNING_MESSAGE);

    }else{
      processButton.setEnabled(false);
      jta.setText(jta.getText()+"\n"+"Starting Transformation "+ selected);
      String selected_id = null;
      for(int i=0; i < EADIDVector.size(); i++){
	if(((Couple)EADIDVector.elementAt(i)).eadid.equals(selected)){
	  selected_id = ((Couple)EADIDVector.elementAt(i)).archdescid;
	  //System.out.println("SELECTED IS: "+ selected_id );
	}
      }
      dc.transform(selected_id);
      processButton.setEnabled(true);
    }
  }


  private void processAll() {
    // Get the project diretory
    String projDir = dp.getProjectDir();

    if (projDir == null || projDir.equals("")) {
      JOptionPane.showMessageDialog(this,
				    "You must set the Workspace Directory \n under Preferences",
				    "Missing Workspace Directory",
				    JOptionPane.WARNING_MESSAGE);
    } else {  
      // make sure they really want to do this

      int opt = JOptionPane.showConfirmDialog(this,
					      "This operation will transform all finding aids and\nplace them in your Workspace Directory\nwith a default name of <eadid>.xml\nAre you sure you want to do this?",
					      "Transform All?",
					      JOptionPane.YES_NO_OPTION,
					      JOptionPane.WARNING_MESSAGE);

      if (opt == JOptionPane.YES_OPTION) {
	// execute the transformations
	jta.append("\nTransform All");

	Date start = new Date();

	File fprojDir = new File(projDir);

	// loop through all eadid
	for(int i=0; i < EADIDVector.size(); i++){
	  Couple c = (Couple)EADIDVector.elementAt(i);

	  File file = new File(fprojDir, c.eadid + ".xml");
	  jta.append("\ntransforming: " + file.getAbsolutePath());
	  jta.repaint();

	  dc.transform(c.archdescid, file);
	  jta.repaint();
	}

	Date stop = new Date();
	int elapsed = (int)((stop.getTime() - start.getTime()) / 1000l);
	jta.append("\nTransform all elapsed time: " + elapsed + " seconds");

      }
    }
  }


  public void callBack(String id, String message){
    jta.append("\n Message from converter: "+ message);
  }


  public void callBack_saveXmlFile(XmlDataBuffer data) {

    // get the file name from the user
    fc = new JFileChooser();
        
    if(dp.getProjectDir() != null)
      fc.setCurrentDirectory(new File(dp.getProjectDir()));
        
    int returnVal  =fc.showSaveDialog(this);        

    // check if the save was approved
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = fc.getSelectedFile();

      callBack_saveXmlFile(data, file);
    }
        
    fc= null;
  }


  public void callBack_saveXmlFile(XmlDataBuffer data, File file) {
        
    log.debug("SAVE FILE HERE "+ file.getAbsolutePath());

    FileIO fio = new FileIO();

    fio.write(file.getAbsolutePath(), data.toString());

    fio = null;
  }

}
