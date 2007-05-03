/* ead
 * MyListCellRenderer.java 12:46:37 PM
 * @author: Amit Kumar
 * @date-created: Jul 07-07-2004 
 * Render the archdescid and the origin desc.
 */
package org.mith.ead.swing;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;

import org.mith.ead.data.Couple;

class MyListCellRenderer extends JLabel implements javax.swing.ListCellRenderer {
     public MyListCellRenderer() {
         setOpaque(true);
     }
     public Component getListCellRendererComponent(
         JList list,
         Object value,
         int index,
         boolean isSelected,
         boolean cellHasFocus)
     {
         setText(((Couple)value).toString());
        setBackground(isSelected ? Color.blue : Color.white);
        setForeground(isSelected ? Color.white : Color.black);
         return this;
     }
 }
