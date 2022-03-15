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

import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * display an ACLMessage in a Frame
 *
 * @author Chris van Aart - Acklin B.V., the Netherlands
 * @since April 26, 2002
 */

public class ACLFrame extends JFrame {
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JPanel buttonPanel = new JPanel();
    private final JButton closeButton = new JButton();
    private final JMenuBar theMenuBar = new JMenuBar();
    private final JMenu fileMenu = new JMenu();
    private final JMenuItem saveMenuItem = new JMenuItem();
    private final JMenuItem exitMenuItem = new JMenuItem();
    private final JMenu toolsMenu = new JMenu();
    private final JMenuItem systemoutMenuItem = new JMenuItem();
    private final Agent agent;
    private ACLPanel aclPanel;
    private ACLMessage msg;


    /**
     * Constructor for the ACLFrame object
     *
     * @param agent Description of Parameter
     */
    public ACLFrame(Agent agent) {
        this.agent = agent;
        try {
            aclPanel = new ACLPanel(agent);
            jbInit();
            this.setSize(300, 500);
            setFrameIcon("images/details.gif");

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            this.setLocation(screenSize.width / 2 - this.getSize().width / 2,
                    screenSize.height / 2 - this.getSize().height / 2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * show the ACLMessage, disabled
     *
     * @param theMsg the ACLMessage to be displayed
     * @param agent  the agent
     */
    public static void show(ACLMessage theMsg, Agent agent) {
        ACLFrame theFrame = new ACLFrame(agent);
        theFrame.setMsg(theMsg);
        theFrame.disableACLPanel();
    }

    /**
     * Sets the ACLMessage of the Frame
     *
     * @param msg The ACLMessage
     */
    public void setMsg(ACLMessage msg) {
        aclPanel.setReadOnly();
        aclPanel.setItsMsg(msg);

        String theSender = msg.getSender().getLocalName();
        String theReceiver = "<none>";
        if (msg.getAllReceiver().hasNext()) {
            theReceiver = msg.getAllReceiver().next().getLocalName();
        }

        String theTitle = "(" + ACLMessage.getPerformative(msg.getPerformative()) +
                ": from " + theSender + " to " + theReceiver + ")";
        this.setTitle(theTitle);
        this.setVisible(true);
    }

    /**
     * disable editing of ACLmessage
     */
    public void disableACLPanel() {
        aclPanel.setReadOnly();
    }

    /**
     * Description of the Method
     *
     * @param e Description of Parameter
     */
    void closeButton_actionPerformed(ActionEvent e) {
        doExit();
    }

    /**
     * Description of the Method
     *
     * @param e Description of Parameter
     */
    void exitMenuItem_actionPerformed(ActionEvent e) {
        doExit();
    }

    /**
     * Description of the Method
     */
    void doExit() {
        this.setVisible(false);
    }

    /**
     * Description of the Method
     *
     * @param e Description of Parameter
     */
    void systemoutMenuItem_actionPerformed(ActionEvent e) {
        doSystemOut();
    }

    /**
     * Description of the Method
     */
    void doSystemOut() {
        aclPanel.doSystemOut();
    }

    /**
     * Description of the Method
     *
     * @param e Description of Parameter
     */
    void saveMenuItem_actionPerformed(ActionEvent e) {
        aclPanel.saveACL();
    }

    /**
     * Sets the FrameIcon attribute of the ACLFrame object
     *
     * @param iconpath The new FrameIcon value
     */
    private void setFrameIcon(String iconpath) {
        ImageIcon image = new ImageIcon(this.getClass().getResource(iconpath));
        setIconImage(image.getImage());
    }

    /**
     * Description of the Method
     * <p>
     * Exception  Description of Exception
     */
    private void jbInit() {
        this.getContentPane().setBackground(Color.white);
        this.setJMenuBar(theMenuBar);
        this.getContentPane().setLayout(gridBagLayout1);
        closeButton.setBackground(Color.white);
        closeButton.setFont(new Font("Dialog", Font.PLAIN, 10));
        closeButton.setText("close");
        closeButton.addActionListener(this::closeButton_actionPerformed);
        buttonPanel.setBackground(Color.white);
        fileMenu.setBackground(Color.white);
        fileMenu.setFont(new Font("Dialog", Font.PLAIN, 12));
        fileMenu.setMnemonic('F');
        fileMenu.setText("File");
        saveMenuItem.setBackground(Color.white);
        saveMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        saveMenuItem.setActionCommand("Save as");
        saveMenuItem.setMnemonic('S');
        saveMenuItem.setText("Save As...");
        saveMenuItem.addActionListener(this::saveMenuItem_actionPerformed);
        exitMenuItem.setBackground(Color.white);
        exitMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        exitMenuItem.setMnemonic('E');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(this::exitMenuItem_actionPerformed);
        toolsMenu.setBackground(Color.white);
        toolsMenu.setFont(new Font("Dialog", Font.PLAIN, 12));
        toolsMenu.setText("Tools");
        systemoutMenuItem.setBackground(Color.white);
        systemoutMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        systemoutMenuItem.setMnemonic('S');
        systemoutMenuItem.setText("System.out");
        systemoutMenuItem.addActionListener(this::systemoutMenuItem_actionPerformed);
        theMenuBar.setBackground(Color.white);
        theMenuBar.setFont(new Font("Dialog", Font.PLAIN, 12));
        this.getContentPane().add(aclPanel, new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        this.getContentPane().add(buttonPanel, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        buttonPanel.add(closeButton, null);
        theMenuBar.add(fileMenu);
        theMenuBar.add(toolsMenu);
        fileMenu.add(saveMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        toolsMenu.add(systemoutMenuItem);
    }

}
//  ***EOF***
