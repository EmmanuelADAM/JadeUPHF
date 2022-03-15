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
package jade.tools.testagent;

import jade.lang.acl.ACLMessage;
import jade.tools.gui.ACLPanel;
import jade.tools.gui.ACLTracePanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * This class is builds up the GUI of the TestAgent
 *
 * @author Chris van Aart - Acklin B.V., the Netherlands
 * @created May 6, 2002
 */

public class TestAgentFrame extends JFrame {

    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JSplitPane mainSplitPane = new JSplitPane();
    JMenuBar itsMenuBar = new JMenuBar();
    JMenu fileMenu = new JMenu();
    JMenuItem exitMenuItem = new JMenuItem();
    JMenu messagesMenu = new JMenu();
    JMenuItem helloWorldMenuItem = new JMenuItem();
    JMenuItem amsRegMenuItem = new JMenuItem();
    JMenuItem amsDeregMenuItem = new JMenuItem();
    JMenuItem amsSearchMenuItem = new JMenuItem();
    JMenuItem pingLausanneMenuItem = new JMenuItem();
    JMenuItem dfSearchMenuItem = new JMenuItem();
    JMenuItem dfDeregMenuItem = new JMenuItem();
    JMenuItem dfRegMenuItem = new JMenuItem();
    JMenu helpMenu = new JMenu();
    JMenuItem aboutMenuItem = new JMenuItem();
    JPanel leftPanel = new JPanel();
    JPanel rightPanel = new JPanel();
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    GridBagLayout gridBagLayout3 = new GridBagLayout();
    JToolBar aclTreeToolBar = new JToolBar();
    JButton writeQueueButton = new JButton();
    JButton readQueueButton = new JButton();
    JButton openButton = new JButton();
    JButton sendButton = new JButton();
    JButton newButton = new JButton();
    JButton saveButton = new JButton();
    JToolBar messageToolBar = new JToolBar();
    JButton currentButton = new JButton();
    JButton replyButton = new JButton();
    JButton viewButton = new JButton();
    JButton deleteButton = new JButton();
    JButton statisticsButton = new JButton();
    JButton quitButton = new JButton();
    JMenuItem newMenuItem = new JMenuItem();
    JMenuItem loadMenuItem = new JMenuItem();
    JMenuItem saveMenuItem = new JMenuItem();
    JMenuItem sendMenuItem = new JMenuItem();
    JMenuItem templatesMenuItem = new JMenuItem();
    JMenu traceMenu = new JMenu();
    JMenuItem claerQueueMenuItem = new JMenuItem();
    JMenuItem currentMenuItem = new JMenuItem();
    JMenuItem replyMenuItem = new JMenuItem();
    JMenuItem deleteMenuItem = new JMenuItem();
    JMenuItem statisticsMenuItem = new JMenuItem();
    JMenuItem loadMsgMenuItem = new JMenuItem();
    JMenuItem saveMsgMenuItem = new JMenuItem();
    JMenuItem loadQMenuItem = new JMenuItem();
    JMenuItem saveQMenuItem = new JMenuItem();
    JButton systemButton = new JButton();
    JMenuItem systemOutMenuItem = new JMenuItem();
    JMenuItem currentToOutMenuItem = new JMenuItem();
    JMenu behaviourMenu = new JMenu();
    JRadioButtonMenuItem pingRadioButtonMenuItem = new JRadioButtonMenuItem();
    JMenuItem localPingMenuItem = new JMenuItem();
    ImageIcon newIcon;
    ImageIcon openIcon;
    ImageIcon saveIcon;
    ImageIcon sendIcon;
    ImageIcon readQueueIcon;
    ImageIcon saveQueueIcon;
    ImageIcon currentIcon;
    ImageIcon replyIcon;
    ImageIcon viewIcon;
    ImageIcon deleteIcon;
    ImageIcon statisticsIcon;
    ImageIcon quitIcon;
    ImageIcon systemIcon;
    ACLPanel aclPanel;
    TestAgent agent;
    ACLTracePanel aclTreePanel;
    Border border1;
    /**
     * Constructor for the TestAgentFrame object
     *
     * @param agent Description of Parameter
     */
    public TestAgentFrame(TestAgent agent) {
        getImages();
        this.agent = agent;
        aclPanel = new ACLPanel(agent);
        aclTreePanel = new ACLTracePanel(agent);
        try {
            jbInit();
            this.setSize(600, 600);
            this.setTitle("Jade TestAgent beta - " + agent.getName());
            this.setFrameIcon("images/dummy.gif");
            this.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the ItsMsg attribute of the TestAgentFrame object
     *
     * @return The ItsMsg value
     */
    public ACLMessage getItsMsg() {
        return aclPanel.getItsMsg();
    }

    /**
     * Sets the ItsMsg attribute of the TestAgentFrame object
     *
     * @param msg The new ItsMsg value
     */
    public void setItsMsg(ACLMessage msg) {
        aclPanel.setItsMsg(msg);
    }

    public void getImages() {

        try {
            newIcon =
                    new ImageIcon(this.getClass().getResource("images/new.gif"));
            openIcon =
                    new ImageIcon(this.getClass().getResource("images/open.gif"));
            saveIcon =
                    new ImageIcon(this.getClass().getResource("images/save.gif"));
            sendIcon =
                    new ImageIcon(this.getClass().getResource("images/send.gif"));
            readQueueIcon =
                    new ImageIcon(this.getClass().getResource("images/readqueue.gif"));
            saveQueueIcon =
                    new ImageIcon(this.getClass().getResource("images/writequeue.gif"));
            currentIcon =
                    new ImageIcon(this.getClass().getResource("images/current.gif"));
            replyIcon =
                    new ImageIcon(this.getClass().getResource("images/reply.gif"));
            viewIcon =
                    new ImageIcon(this.getClass().getResource("images/inspect.gif"));
            deleteIcon =
                    new ImageIcon(this.getClass().getResource("images/delete.gif"));
            statisticsIcon =
                    new ImageIcon(this.getClass().getResource("images/book.gif"));
            quitIcon =
                    new ImageIcon(this.getClass().getResource("images/quit.gif"));
            systemIcon =
                    new ImageIcon(this.getClass().getResource("images/system.gif"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Sets the FrameIcon attribute of the TestAgentFrame object
     *
     * @param iconpath The new FrameIcon value
     */
    public void setFrameIcon(String iconpath) {
        ImageIcon image = new ImageIcon(this.getClass().getResource(iconpath));
        setIconImage(image.getImage());
    }

    /**
     * Adds a feature to the MessageNode attribute of the TestAgentFrame object
     *
     * @param msg       The feature to be added to the MessageNode attribute
     * @param direction The feature to be added to the MessageNode attribute
     */
    public void addMessageNode(String direction, ACLMessage msg) {
        aclTreePanel.addMessageNode(direction, msg);
    }

    /**
     * Description of the Method
     *
     * @param e Description of Parameter
     */
    void helloWorldMenuItem_actionPerformed(ActionEvent e) {
        agent.doHelloWorld();
    }

    /**
     * Description of the Method
     *
     * @param e Description of Parameter
     */
    void amsRegMenuItem_actionPerformed(ActionEvent e) {
        agent.doRegisterAMS();
    }

    /**
     * Description of the Method
     *
     * @param e Description of Parameter
     */
    void systemMenuItem_actionPerformed(ActionEvent e) {
        agent.doSystemOut();
    }

    /**
     * Description of the Method
     *
     * @param e Description of Parameter
     */
    void exitMenuItem_actionPerformed(ActionEvent e) {
        agent.doExit();
    }

    /**
     * Description of the Method
     *
     * @param e Description of Parameter
     */
    void newButton_actionPerformed(ActionEvent e) {
        agent.doNewMessage();
    }

    /**
     * Description of the Method
     *
     * @param e Description of Parameter
     */
    void sendButton_actionPerformed(ActionEvent e) {
        agent.sendMessage();
    }

    /**
     * Description of the Method
     *
     * @param e Description of Parameter
     */
    void pingLausanneMenuItem_actionPerformed(ActionEvent e) {
        agent.doLausannePing();
    }

    /**
     * Description of the Method
     *
     * @param e Description of Parameter
     */
    void amsDeregMenuItem_actionPerformed(ActionEvent e) {
        agent.doDeRegisterAMS();
    }

    /**
     * Description of the Method
     *
     * @param e Description of Parameter
     */
    void amsSearchMenuItem_actionPerformed(ActionEvent e) {
        agent.doSearchAMS();
    }

    void dfRegMenuItem_actionPerformed(ActionEvent e) {
        agent.doRegisterDF();
    }

    void dfDeregMenuItem_actionPerformed(ActionEvent e) {
        agent.doDeregisterDF();
    }

    void dfSearchMenuItem_actionPerformed(ActionEvent e) {
        agent.doSearchDF();
    }

    void aboutMenuItem_actionPerformed(ActionEvent e) {
        new AboutFrame().setVisible(true);
    }

    void readQueueButton_actionPerformed(ActionEvent e) {
        this.aclTreePanel.loadQueue();
    }

    void writeQueueButton_actionPerformed(ActionEvent e) {
        this.aclTreePanel.saveQueue();
    }

    void currentButton_actionPerformed(ActionEvent e) {

        ACLMessage currentACL = this.aclTreePanel.getCurrentACL();
        if (currentACL != null) {
            this.aclPanel.setItsMsg((ACLMessage) currentACL.clone());
        }
    }

    void viewButton_actionPerformed(ActionEvent e) {
        this.aclTreePanel.doShowCurrentACL();
    }

    void deleteButton_actionPerformed(ActionEvent e) {
        this.aclTreePanel.deleteCurrent();
    }

    void statisticsButton_actionPerformed(ActionEvent e) {
        this.aclTreePanel.showStastistics();
    }

    void quitButton_actionPerformed(ActionEvent e) {
        agent.doDelete();
        System.exit(1);
    }

    void replyButton_actionPerformed(ActionEvent e) {
        agent.doReply();
    }

    void newMenuItem_actionPerformed(ActionEvent e) {
        agent.doNewMessage();
    }

    void loadMenuItem_actionPerformed(ActionEvent e) {
        this.aclPanel.loadACL();
    }

    void saveMenuItem_actionPerformed(ActionEvent e) {
        this.aclPanel.saveACL();
    }

    void sendMenuItem_actionPerformed(ActionEvent e) {
        agent.sendMessage();
    }

    void saveButton_actionPerformed(ActionEvent e) {
        this.aclPanel.saveACL();
    }

    void openButton_actionPerformed(ActionEvent e) {
        this.aclPanel.loadACL();
    }

    void saveQueueMenuItem_actionPerformed(ActionEvent e) {
        this.aclTreePanel.saveQueue();
    }

    void claerQueueMenuItem_actionPerformed(ActionEvent e) {
        this.aclTreePanel.clearACLModel();
    }

    void currentMenuItem_actionPerformed(ActionEvent e) {
        ACLMessage currentACL = this.aclTreePanel.getCurrentACL();
        if (currentACL != null) {
            this.aclPanel.setItsMsg((ACLMessage) currentACL.clone());
        }

    }

    void replyMenuItem_actionPerformed(ActionEvent e) {
        agent.doReply();
    }

    void deleteMenuItem_actionPerformed(ActionEvent e) {
        this.aclTreePanel.deleteCurrent();
    }

    void statisticsMenuItem_actionPerformed(ActionEvent e) {
        this.aclTreePanel.showStastistics();
    }

    void loadMsgMenuItem_actionPerformed(ActionEvent e) {
        this.aclPanel.loadACL();
    }

    void saveMsgMenuItem_actionPerformed(ActionEvent e) {
        this.aclPanel.saveACL();
    }

    void loadQMenuItem_actionPerformed(ActionEvent e) {
        this.aclTreePanel.loadQueue();
    }

    void saveQMenuItem_actionPerformed(ActionEvent e) {
        this.aclTreePanel.saveQueue();
    }

    void systemButton_actionPerformed(ActionEvent e) {
        this.aclTreePanel.doSystemOut();
    }

    void systemOutMenuItem_actionPerformed(ActionEvent e) {
        this.aclPanel.doSystemOut();
    }

    void currentToOutMenuItem_actionPerformed(ActionEvent e) {
        this.aclTreePanel.doSystemOut();
    }

    void pingRadioButtonMenuItem_stateChanged(ChangeEvent e) {
        agent.pingBehaviour = (pingRadioButtonMenuItem.isSelected());
    }

    void localPingMenuItem_actionPerformed(ActionEvent e) {
        agent.doLocalPing();
    }

    /**
     * Description of the Method
     *
     * @throws Exception Description of Exception
     */
    private void jbInit() throws Exception {
        border1 = BorderFactory.createEmptyBorder();
        this.getContentPane().setLayout(gridBagLayout1);
        fileMenu.setBackground(Color.white);
        fileMenu.setFont(new Font("Dialog", Font.PLAIN, 12));
        fileMenu.setMnemonic('F');
        fileMenu.setText("File");
        exitMenuItem.setBackground(Color.white);
        exitMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        exitMenuItem.setMnemonic('X');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(
                this::exitMenuItem_actionPerformed);
        messagesMenu.setBackground(Color.white);
        messagesMenu.setFont(new Font("Dialog", Font.PLAIN, 12));
        messagesMenu.setMnemonic('M');
        messagesMenu.setText("Message");
        helloWorldMenuItem.setBackground(Color.white);
        helloWorldMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        helloWorldMenuItem.setMnemonic('H');
        helloWorldMenuItem.setText("Hello world");
        helloWorldMenuItem.addActionListener(
                this::helloWorldMenuItem_actionPerformed);
        amsRegMenuItem.setBackground(Color.white);
        amsRegMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        amsRegMenuItem.setMnemonic('R');
        amsRegMenuItem.setText("AMSRegister");
        amsRegMenuItem.addActionListener(
                this::amsRegMenuItem_actionPerformed);
        amsDeregMenuItem.setBackground(Color.white);
        amsDeregMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        amsDeregMenuItem.setMnemonic('D');
        amsDeregMenuItem.setText("AMSDeregister");
        amsDeregMenuItem.addActionListener(
                this::amsDeregMenuItem_actionPerformed);
        amsSearchMenuItem.setBackground(Color.white);
        amsSearchMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        amsSearchMenuItem.setMnemonic('A');
        amsSearchMenuItem.setText("AMSSearch");
        amsSearchMenuItem.addActionListener(
                this::amsSearchMenuItem_actionPerformed);
        this.getContentPane().setBackground(Color.white);
        this.setJMenuBar(itsMenuBar);
        mainSplitPane.setForeground(Color.white);
        itsMenuBar.setBackground(Color.white);
        pingLausanneMenuItem.setBackground(Color.white);
        pingLausanneMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        pingLausanneMenuItem.setToolTipText("Ping to Lausannes PingAgent (works only when http package installed)");
        pingLausanneMenuItem.setMnemonic('P');
        pingLausanneMenuItem.setText("Ping to Lausanne ");
        pingLausanneMenuItem.addActionListener(
                this::pingLausanneMenuItem_actionPerformed);
        dfSearchMenuItem.setBackground(Color.white);
        dfSearchMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        dfSearchMenuItem.setMnemonic('D');
        dfSearchMenuItem.setText("DFSearch");
        dfSearchMenuItem.addActionListener(
                this::dfSearchMenuItem_actionPerformed);
        dfRegMenuItem.setBackground(Color.white);
        dfRegMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        dfRegMenuItem.setText("DFRegister");
        dfRegMenuItem.addActionListener(
                this::dfRegMenuItem_actionPerformed);
        dfDeregMenuItem.setBackground(Color.white);
        dfDeregMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        dfDeregMenuItem.setText("DFDeregister");
        dfDeregMenuItem.addActionListener(
                this::dfDeregMenuItem_actionPerformed);
        helpMenu.setBackground(Color.white);
        helpMenu.setFont(new Font("Dialog", Font.PLAIN, 12));
        helpMenu.setMnemonic('H');
        helpMenu.setText("Help");
        aboutMenuItem.setBackground(Color.white);
        aboutMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        aboutMenuItem.setForeground(new Color(0, 0, 132));
        aboutMenuItem.setMnemonic('A');
        aboutMenuItem.setText("About...");
        aboutMenuItem.addActionListener(
                this::aboutMenuItem_actionPerformed);
        leftPanel.setLayout(gridBagLayout2);
        rightPanel.setLayout(gridBagLayout3);
        writeQueueButton.setBorder(border1);
        writeQueueButton.setToolTipText("Save ACLMessage Trace");
        writeQueueButton.setIcon(saveQueueIcon);
        writeQueueButton.addActionListener(
                this::writeQueueButton_actionPerformed);
        readQueueButton.setBackground(Color.white);
        readQueueButton.setBorder(border1);
        readQueueButton.setToolTipText("Open ACLMessage trace");
        readQueueButton.setIcon(readQueueIcon);
        readQueueButton.addActionListener(
                this::readQueueButton_actionPerformed);
        openButton.setBackground(Color.white);
        openButton.setFont(new Font("Dialog", Font.PLAIN, 11));
        openButton.setBorder(border1);
        openButton.setToolTipText("Open ACLMessage From File");
        openButton.setIcon(openIcon);
        openButton.addActionListener(
                this::openButton_actionPerformed);
        sendButton.setBackground(Color.white);
        sendButton.setFont(new Font("Dialog", Font.PLAIN, 11));
        sendButton.setBorder(border1);
        sendButton.setToolTipText("Send ACLMessage");
        sendButton.setIcon(sendIcon);
        sendButton.addActionListener(
                this::sendButton_actionPerformed);
        newButton.setBackground(Color.white);
        newButton.setFont(new Font("Dialog", Font.PLAIN, 11));
        newButton.setBorder(border1);
        newButton.setPreferredSize(new Dimension(29, 27));
        newButton.setToolTipText("New ACLMessage");
        newButton.setIcon(newIcon);
        newButton.addActionListener(
                this::newButton_actionPerformed);
        saveButton.setBackground(Color.white);
        saveButton.setFont(new Font("Dialog", Font.PLAIN, 11));
        saveButton.setBorder(border1);
        saveButton.setToolTipText("Save ACLMessage To File");
        saveButton.setIcon(saveIcon);
        saveButton.addActionListener(
                this::saveButton_actionPerformed);
        messageToolBar.setBackground(Color.white);
        messageToolBar.setFloatable(false);
        aclTreeToolBar.setBackground(Color.white);
        aclTreeToolBar.setFloatable(false);
        currentButton.setBorder(border1);
        currentButton.setToolTipText("Set Selected ACLMessage as current ACLMessage");
        currentButton.setIcon(currentIcon);
        currentButton.addActionListener(
                this::currentButton_actionPerformed);
        replyButton.setBorder(border1);
        replyButton.setToolTipText("Reply To Current ACLMessage");
        replyButton.setIcon(replyIcon);
        replyButton.addActionListener(
                this::replyButton_actionPerformed);
        viewButton.setBorder(border1);
        viewButton.setToolTipText("Show Selected ACLMessage");
        viewButton.setIcon(viewIcon);
        viewButton.addActionListener(
                this::viewButton_actionPerformed);
        deleteButton.setBorder(border1);
        deleteButton.setToolTipText("Delete Current ACLMessage");
        deleteButton.setIcon(deleteIcon);
        deleteButton.addActionListener(
                this::deleteButton_actionPerformed);
        statisticsButton.setBorder(border1);
        statisticsButton.setToolTipText("Show Statistics");
        statisticsButton.setIcon(statisticsIcon);
        statisticsButton.addActionListener(
                this::statisticsButton_actionPerformed);
        quitButton.setBorder(border1);
        quitButton.setToolTipText("Quit");
        quitButton.setIcon(quitIcon);
        quitButton.addActionListener(
                this::quitButton_actionPerformed);
        leftPanel.setBackground(Color.white);
        rightPanel.setBackground(Color.white);
        newMenuItem.addActionListener(
                this::newMenuItem_actionPerformed);
        newMenuItem.setText("New Message");
        newMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        newMenuItem.setActionCommand("load");
        newMenuItem.setMnemonic('N');
        newMenuItem.setBackground(Color.white);
        loadMenuItem.setBackground(Color.white);
        loadMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        loadMenuItem.setText("load message");
        loadMenuItem.addActionListener(
                this::loadMenuItem_actionPerformed);
        saveMenuItem.setBackground(Color.white);
        saveMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        saveMenuItem.setText("save message");
        saveMenuItem.addActionListener(
                this::saveMenuItem_actionPerformed);
        sendMenuItem.setBackground(Color.white);
        sendMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        sendMenuItem.setMnemonic('S');
        sendMenuItem.setText("Send Message");
        sendMenuItem.addActionListener(
                this::sendMenuItem_actionPerformed);
        templatesMenuItem.setBackground(Color.white);
        templatesMenuItem.setEnabled(false);
        templatesMenuItem.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 12));
        templatesMenuItem.setText("Templates:");
        traceMenu.setBackground(Color.white);
        traceMenu.setFont(new Font("Dialog", Font.PLAIN, 12));
        traceMenu.setMnemonic('T');
        traceMenu.setText("Trace");
        claerQueueMenuItem.setBackground(Color.white);
        claerQueueMenuItem.setActionCommand("load");
        claerQueueMenuItem.setMnemonic('C');
        claerQueueMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        claerQueueMenuItem.setText("Clear Trace");
        claerQueueMenuItem.addActionListener(
                this::claerQueueMenuItem_actionPerformed);
        currentMenuItem.setBackground(Color.white);
        currentMenuItem.setActionCommand("load");
        currentMenuItem.setMnemonic('U');
        currentMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        currentMenuItem.setText("Use Current ACLMessage");
        currentMenuItem.addActionListener(
                this::currentMenuItem_actionPerformed);
        replyMenuItem.setBackground(Color.white);
        replyMenuItem.setActionCommand("load");
        replyMenuItem.setMnemonic('R');
        replyMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        replyMenuItem.setText("Reply To Current ACLMessage");
        replyMenuItem.addActionListener(
                this::replyMenuItem_actionPerformed);
        deleteMenuItem.addActionListener(
                this::deleteMenuItem_actionPerformed);
        deleteMenuItem.setText("Delete Current ACLMessage");
        deleteMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        deleteMenuItem.setActionCommand("load");
        deleteMenuItem.setMnemonic('D');
        deleteMenuItem.setBackground(Color.white);
        statisticsMenuItem.addActionListener(
                this::statisticsMenuItem_actionPerformed);
        statisticsMenuItem.setText("Statistics...");
        statisticsMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        statisticsMenuItem.setActionCommand("load");
        statisticsMenuItem.setMnemonic('S');
        statisticsMenuItem.setBackground(Color.white);
        loadMsgMenuItem.addActionListener(
                this::loadMsgMenuItem_actionPerformed);
        loadMsgMenuItem.setText("Open ACLMessage...");
        loadMsgMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        loadMsgMenuItem.setActionCommand("load");
        loadMsgMenuItem.setMnemonic('L');
        loadMsgMenuItem.setBackground(Color.white);
        saveMsgMenuItem.addActionListener(
                this::saveMsgMenuItem_actionPerformed);
        saveMsgMenuItem.setText("Save ACLMessage...");
        saveMsgMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        saveMsgMenuItem.setActionCommand("load");
        saveMsgMenuItem.setMnemonic('S');
        saveMsgMenuItem.setBackground(Color.white);
        loadQMenuItem.addActionListener(
                this::loadQMenuItem_actionPerformed);
        loadQMenuItem.setText("Open ACLMessage Trace...");
        loadQMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        loadQMenuItem.setActionCommand("load");
        loadQMenuItem.setMnemonic('O');
        loadQMenuItem.setBackground(Color.white);
        saveQMenuItem.addActionListener(
                this::saveQMenuItem_actionPerformed);
        saveQMenuItem.setText("Save ACLMessage Trace...");
        saveQMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        saveQMenuItem.setActionCommand("load");
        saveQMenuItem.setBackground(Color.white);
        systemButton.setBorder(border1);
        systemButton.setToolTipText("To System.out");
        systemButton.setIcon(systemIcon);
        systemButton.addActionListener(
                this::systemButton_actionPerformed);
        systemOutMenuItem.addActionListener(
                this::systemOutMenuItem_actionPerformed);
        systemOutMenuItem.setText("To System.out");
        systemOutMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        systemOutMenuItem.setActionCommand("load");
        systemOutMenuItem.setMnemonic('L');
        systemOutMenuItem.setBackground(Color.white);
        currentToOutMenuItem.addActionListener(
                this::currentToOutMenuItem_actionPerformed);
        currentToOutMenuItem.setText("Current To System.out");
        currentToOutMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        currentToOutMenuItem.setActionCommand("load");
        currentToOutMenuItem.setMnemonic('S');
        currentToOutMenuItem.setBackground(Color.white);
        behaviourMenu.setBackground(Color.white);
        behaviourMenu.setFont(new Font("Dialog", Font.PLAIN, 12));
        behaviourMenu.setMnemonic('B');
        behaviourMenu.setText("Behaviour");
        pingRadioButtonMenuItem.setText("Ping Behaviour");
        pingRadioButtonMenuItem.setSelected(true);
        pingRadioButtonMenuItem.setToolTipText("Responses to ACLMessages containing Ping");
        pingRadioButtonMenuItem.setBackground(Color.white);
        pingRadioButtonMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        pingRadioButtonMenuItem.addChangeListener(
                this::pingRadioButtonMenuItem_stateChanged);
        localPingMenuItem.addActionListener(
                this::localPingMenuItem_actionPerformed);
        localPingMenuItem.setText("Local Ping");
        localPingMenuItem.setMnemonic('L');
        localPingMenuItem.setFont(new Font("Dialog", Font.PLAIN, 12));
        localPingMenuItem.setToolTipText("Template for Local Ping ACLMessage");
        localPingMenuItem.setBackground(Color.white);
        this.getContentPane().add(mainSplitPane, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        mainSplitPane.add(leftPanel, JSplitPane.LEFT);
        leftPanel.add(messageToolBar, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        leftPanel.add(aclPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        messageToolBar.add(newButton, null);
        messageToolBar.add(sendButton, null);
        messageToolBar.add(openButton, null);
        messageToolBar.add(saveButton, null);
        mainSplitPane.add(rightPanel, JSplitPane.RIGHT);
        rightPanel.add(aclTreeToolBar, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        aclTreeToolBar.add(readQueueButton, null);
        aclTreeToolBar.add(writeQueueButton, null);
        aclTreeToolBar.add(currentButton, null);
        aclTreeToolBar.add(replyButton, null);
        aclTreeToolBar.add(viewButton, null);
        aclTreeToolBar.add(systemButton, null);
        aclTreeToolBar.add(deleteButton, null);
        aclTreeToolBar.add(statisticsButton, null);
        aclTreeToolBar.add(quitButton, null);

        rightPanel.add(aclTreePanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        // mainSplitPane.add(aclPanel, JSplitPane.LEFT);
//    mainSplitPane.add(aclTreePanel, JSplitPane.RIGHT);

        itsMenuBar.add(fileMenu);
        itsMenuBar.add(messagesMenu);
        itsMenuBar.add(traceMenu);
        itsMenuBar.add(behaviourMenu);
        itsMenuBar.add(helpMenu);
        fileMenu.add(loadMsgMenuItem);
        fileMenu.add(saveMsgMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(loadQMenuItem);
        fileMenu.add(saveQMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        messagesMenu.add(newMenuItem);
        messagesMenu.add(sendMenuItem);
        messagesMenu.add(systemOutMenuItem);
        messagesMenu.addSeparator();
//    messagesMenu.add(saveMenuItem);
//    messagesMenu.add(loadMenuItem);
        messagesMenu.add(templatesMenuItem);
        messagesMenu.add(localPingMenuItem);
//    messagesMenu.addSeparator();
        messagesMenu.add(pingLausanneMenuItem);
        messagesMenu.add(helloWorldMenuItem);
        messagesMenu.addSeparator();
        messagesMenu.add(amsRegMenuItem);
        messagesMenu.add(amsDeregMenuItem);
        messagesMenu.add(amsSearchMenuItem);
        messagesMenu.addSeparator();
        messagesMenu.add(dfRegMenuItem);
        messagesMenu.add(dfDeregMenuItem);
        messagesMenu.add(dfSearchMenuItem);
        messagesMenu.addSeparator();
        helpMenu.add(aboutMenuItem);
        traceMenu.add(claerQueueMenuItem);
        traceMenu.addSeparator();
        traceMenu.add(currentMenuItem);
        traceMenu.add(replyMenuItem);
        traceMenu.add(deleteMenuItem);
        traceMenu.add(currentToOutMenuItem);
        traceMenu.addSeparator();
        traceMenu.add(statisticsMenuItem);
        behaviourMenu.add(pingRadioButtonMenuItem);
        mainSplitPane.setDividerLocation(200);
    }

    private class AboutFrame extends JWindow {

        GridBagLayout gridBagLayout1 = new GridBagLayout();
        ImageIcon acklinIcon =
                new ImageIcon(getClass().getResource("images/acklinabout.gif"));
        JPanel contentPanel = new JPanel();
        GridBagLayout gridBagLayout2 = new GridBagLayout();
        JLabel logoLabel = new JLabel();
        JLabel jLabel1 = new JLabel();
        JLabel jLabel2 = new JLabel();
        Border border1;


        public AboutFrame() {
            try {
                jbInit();
                this.setSize(400, 200);
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                this.setLocation(screenSize.width / 2 - this.getSize().width / 2,
                        screenSize.height / 2 - this.getSize().height / 2);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void logoLabel_mouseClicked(MouseEvent e) {
            this.setVisible(false);
        }

        void jLabel3_mousePressed(MouseEvent e) {
            this.setVisible(false);
        }

        void jLabel2_mouseClicked(MouseEvent e) {
            this.setVisible(false);
        }

        void jLabel3_mouseClicked(MouseEvent e) {
            this.setVisible(false);
        }

        void logoLabel_mouseEntered(MouseEvent e) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        void logoLabel_mouseExited(MouseEvent e) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        void jLabel3_mouseEntered(MouseEvent e) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        void jLabel3_mouseExited(MouseEvent e) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        void jLabel2_mouseEntered(MouseEvent e) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        void jLabel2_mouseExited(MouseEvent e) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        void logoLabel_mousePressed(MouseEvent e) {

        }

        void logoLabel_mouseReleased(MouseEvent e) {

        }

        void jLabel2_mousePressed(MouseEvent e) {

        }

        void jLabel2_mouseReleased(MouseEvent e) {

        }

        private void jbInit() throws Exception {
            // this.setClosable(true);
            //this.setOpaque(false);
            border1 = new TitledBorder(BorderFactory.createLineBorder(new Color(0, 0, 128), 1), "TestAgent");
            this.getContentPane().setBackground(Color.white);
            this.getContentPane().setLayout(gridBagLayout1);
            contentPanel.setLayout(gridBagLayout2);
            logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            logoLabel.setHorizontalTextPosition(SwingConstants.CENTER);
            logoLabel.setIcon(acklinIcon);
            logoLabel.addMouseListener(
                    new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                            logoLabel_mouseClicked(e);
                        }


                        public void mouseEntered(MouseEvent e) {
                            logoLabel_mouseEntered(e);
                        }


                        public void mouseExited(MouseEvent e) {
                            logoLabel_mouseExited(e);
                        }
                    });
            jLabel1.setText("donated by Acklin B.V. to the Jade project");
            jLabel2.setFont(new Font("Dialog", Font.PLAIN, 12));
            jLabel2.setText("web: www.acklin.nl  |  email: info@acklin.nl");
            jLabel2.addMouseListener(
                    new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                            jLabel2_mouseClicked(e);
                        }


                        public void mouseEntered(MouseEvent e) {
                            jLabel2_mouseEntered(e);
                        }


                        public void mouseExited(MouseEvent e) {
                            jLabel2_mouseExited(e);
                        }
                    });
            contentPanel.setBackground(Color.white);
            contentPanel.setFont(new Font("Dialog", Font.PLAIN, 11));
            contentPanel.setBorder(border1);
            this.getContentPane().add(contentPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                    , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
            contentPanel.add(logoLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                    , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
            contentPanel.add(jLabel1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
            contentPanel.add(jLabel2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
        }

    }

}
//  ***EOF***
