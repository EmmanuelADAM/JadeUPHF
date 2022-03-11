/******************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2002 TILAB S.p.A.
 *
 * This file is donated by Acklin B.V. to the JADE project.
 *
 *
 * GNU Lesser General Public License
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * ***************************************************************/
package jade.tools.gui;

import jade.lang.acl.ACLMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ACLContentDialog extends JDialog {


    public ACLContentDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        try {
            jbInit();
            this.setSize(500, 400);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public ACLContentDialog() {
        this(null, "", false);
    }


    public void setEditable(boolean theBool) {
        this.contentTextArea.setEditable(theBool);
    }


    public void setItsContent(ACLMessage itsMsg) {

        contentTextArea.register(itsMsg, "Content");

        String contentLanguage = (itsMsg.getLanguage() != null ? itsMsg.getLanguage() : "<unknown>");
        String contentOntology = (itsMsg.getOntology() != null ? itsMsg.getOntology() : "<unknown>");

        this.titleLabel.setText("content with language=" + contentLanguage + " and ontology=" + contentOntology);

    }


    void jbInit() {
        contentPanel.setLayout(borderLayout1);
        this.getContentPane().setBackground(Color.white);
        this.setModal(true);
        this.addFocusListener(
                new FocusAdapter() {
                    public void focusGained(FocusEvent e) {
                        this_focusGained(e);
                    }
                });
        contentPanel.setBackground(Color.white);
        titleLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setText("content:");
        doneButton.setBackground(Color.white);
        doneButton.setFont(new Font("Dialog", Font.PLAIN, 12));
        doneButton.setToolTipText("Close Dialog and return to ACLMessage");
        doneButton.setHorizontalTextPosition(SwingConstants.CENTER);
        doneButton.setText("done");
        doneButton.addActionListener(this::doneButton_actionPerformed);
        contentTextArea.setElectricScroll(1);
        getContentPane().add(contentPanel);
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(doneButton, BorderLayout.SOUTH);
        contentPanel.add(contentTextArea, BorderLayout.CENTER);
    }


    void doneButton_actionPerformed(ActionEvent e) {
        this.setVisible(false);
    }


    void this_focusGained(FocusEvent e) {
        this.contentTextArea.requestFocus();
    }


    JPanel contentPanel = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JLabel titleLabel = new JLabel();
    JButton doneButton = new JButton();

    ACLTextArea contentTextArea = new ACLTextArea();
}
//  ***EOF***
