/*
 * Created on Feb 10, 2004
 *
 * 
 */
package org.mith.ead.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

// log4j
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import org.mith.ead.data.DatabaseProperty;

/**
 * @author Amit Kumar
 *
 */
public class EadGuiPreference extends JFrame implements ActionListener{
  
  static Logger log = Logger.getLogger(EadGuiPreference.class);

  DatabaseProperty dataproperty;
  EadGui gui;
  JList dataList;
  JTextField uriTextBox;
  JTextField wspaceTextBox;
  final JFileChooser fc = new JFileChooser();
  JCheckBox debugBtn = null;

  /**
   * Constructor.
   */

  public EadGuiPreference(DatabaseProperty dp,EadGui eadgui){
    super("Edit Preferences");
    setDefaultLookAndFeelDecorated(true);
    dataproperty = dp;
    gui = eadgui;
    JPanel buttonPanel = new JPanel();
    JPanel middlePanel = new JPanel();
    middlePanel.setLayout(new GridBagLayout());
    buttonPanel.setLayout(new FlowLayout());
    addDataCells(middlePanel);
    addButtons(buttonPanel);
    
    Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());
    
    contentPane.add(middlePanel,BorderLayout.CENTER);
    contentPane.add(buttonPanel,BorderLayout.SOUTH);
    
    setSize(300,300);      
    pack();
    validate();    
  }
  
  private void addButtons(JPanel panel) {
    JButton button = new JButton();
  
    try{
      button = new JButton(new ImageIcon(gui.cl.getResource("images/save.gif")));
    }catch(Exception ex){
      log.error(ex.getMessage());
    }
    button.setActionCommand("save");
    button.addActionListener(this);
    button.setToolTipText("Save Preferences");
    panel.add(button);
    
    try{
      button = new JButton(new ImageIcon(gui.cl.getResource("images/cancel.gif")));
    }catch(Exception ex){
      log.error(ex.getMessage());
    }
    
    button.addActionListener(this);
    button.setActionCommand("cancel");
    button.setToolTipText("cancel");
    panel.add(button);
    
    try{
      button = new JButton(new ImageIcon(gui.cl.getResource("images/reset.gif")));
    }catch(Exception ex){
      log.error(ex.getMessage());
    }
    button.setActionCommand("resetpreferences");
    button.addActionListener(this);
    button.setActionCommand("reset");
    button.setToolTipText("reset");
    panel.add(button);
    
    
  }

  /**
   * @param middlePanel
   */
  private void addDataCells(JPanel middlePanel) {

    GridBagConstraints gbc = new GridBagConstraints();

    // Database Url
    JLabel label = new JLabel("Database Url:");
    gbc = set_gbc(gbc,0,0,0,0,GridBagConstraints.HORIZONTAL);
    gbc.insets = new Insets(20,30,0,30);
    middlePanel.add(label,gbc);
    
    uriTextBox = new JTextField(dataproperty.getDatabaseUrl());
    gbc = set_gbc(gbc,0,1,0.5,0,GridBagConstraints.HORIZONTAL);
    gbc.ipadx =70;
    gbc.insets = new Insets(20,5,0,30);
    middlePanel.add(uriTextBox,gbc);
    
    // Default Driver
    label = new JLabel("Default Driver: ");
    gbc = set_gbc(gbc,1,0,0,0,GridBagConstraints.HORIZONTAL);
    gbc.insets = new Insets(0,30,0,30);
    middlePanel.add(label,gbc);
  
    String[] data = {"sun.jdbc.odbc.JdbcOdbcDriver"};
    dataList = new JList(data);
    dataList.setSelectedIndex(0);
    gbc = set_gbc(gbc,1,1,0.5,0,GridBagConstraints.HORIZONTAL);
    gbc.ipadx =50;
    gbc.insets = new Insets(0,5,0,30);  
    middlePanel.add(dataList,gbc);


    // Workspace Directory
    label = new JLabel("Workspace Directory");
    gbc = set_gbc(gbc,2,0,0,0,GridBagConstraints.HORIZONTAL);
    gbc.insets = new Insets(0,30,0,30);
    middlePanel.add(label,gbc);
  
    wspaceTextBox = new JTextField(dataproperty.getProjectDir());
    gbc = set_gbc(gbc,2,1,0,0,GridBagConstraints.HORIZONTAL);
    gbc.ipadx =50;
    gbc.insets = new Insets(0,5,0,30);    
    middlePanel.add(wspaceTextBox,gbc);
    
    JButton button = new JButton(new ImageIcon(gui.cl.getResource("images/browse.gif")));
    gbc = set_gbc(gbc,2,2,1,1,GridBagConstraints.NONE);
    gbc.ipadx =20;
    gbc.insets = new Insets(0,5,0,30);
    button.addActionListener(this);
    button.setActionCommand("browseworkspacedir");    
    middlePanel.add(button,gbc);

    // Debugging
    JLabel debug = new JLabel("Debugging:");
    gbc = set_gbc(gbc,3,0,0,0,GridBagConstraints.HORIZONTAL);
    gbc.insets = new Insets(0,30,0,30);
    middlePanel.add(debug, gbc);
    
    debugBtn = new JCheckBox();
    debugBtn.setSelected(dataproperty.getDebug());
    gbc = set_gbc(gbc,3,1,0.5,0,GridBagConstraints.HORIZONTAL);
    gbc.ipadx =50;
    gbc.insets = new Insets(0,5,0,30);
    middlePanel.add(debugBtn,gbc);
  }




  /**
   * @return DatabaseProperty
   */
  public DatabaseProperty getDataproperty() {
    return dataproperty;
  }

  /**
   * @param property
   */
  public void setDataproperty(DatabaseProperty property) {
    dataproperty = property;
  }
  
  
  private GridBagConstraints set_gbc(GridBagConstraints gbc,
                                     int row, int column, double wx,
                                     double wy, int fill) {
    gbc.gridy = row;
    gbc.gridx = column;
    gbc.weightx = wx;
    gbc.weighty = wy;
    gbc.fill = fill;  // GridBagConstraints.NONE .HORIZONTAL .VERTICAL .BOTH
    // leave other fields (eg, anchor) unchanged.
    return gbc;
  }

  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    
    String command = e.getActionCommand();
    if(command.equalsIgnoreCase("browseworkspacedir")){
    
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      int returnVal = fc.showOpenDialog(this);
      File file = null;
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        file = fc.getSelectedFile();
        //This is where a real application would open the file.
        log.debug("Opening: " + file.getAbsolutePath() + ".\n");
          
      } else {
        log.debug("Open command cancelled by user.\n");
      }
           
      if(file != null){
        if(!file.canRead() || !file.canWrite()){
          JOptionPane.showMessageDialog(this,
                                        "No Permission to read/write to the directory.\n Choose some other directory",
                                        "Directory Permission error",
                                        JOptionPane.ERROR_MESSAGE);
              
              

        }else{
          wspaceTextBox.setText(file.getAbsolutePath());  
               
        }
      }

      
    }
    
    if(command.equalsIgnoreCase("reset")){
      this.dataproperty.reset();
      reset();      
    }
    if(command.equalsIgnoreCase("cancel")){
      this.setVisible(false);
      gui.setFocusable(true);
      this.setVisible(false);            
    }
    if(command.equalsIgnoreCase("save")){
      gui.dp.setDatabaseUrl(uriTextBox.getText());
      
      try {
        gui.dc.connect();
      }
      catch (Exception e2) {};

      gui.dp.setProjectDir(wspaceTextBox.getText());
      gui.dp.setDriver((String)dataList.getSelectedValue());
                gui.dp.setDebug(debugBtn.isSelected());

                gui.dp.sync();
      log.debug("SAVED THE DP");

      log.info("Debugging is set to: " + debugBtn.isSelected());
      Logger root = Logger.getRootLogger();
      root.setPriority(debugBtn.isSelected() ? Priority.DEBUG : Priority.INFO);

      this.setVisible(false);
      
    }
    
  }


  /**
   * Reset all prefs to their default values.
   */
  private void reset() {
    
    uriTextBox.setText(gui.dp.getDatabaseUrl());
    wspaceTextBox.setText(gui.dp.getProjectDir());
    dataList.setSelectedIndex(0);
    debugBtn.setSelected(gui.dp.getDebug());
  }


  /**
   * @param dp
   */
  public void set(DatabaseProperty d) {
    uriTextBox.setText(gui.dp.getDatabaseUrl());
    wspaceTextBox.setText(gui.dp.getProjectDir());
    dataList.setSelectedIndex(0);
    
    
  }




}
