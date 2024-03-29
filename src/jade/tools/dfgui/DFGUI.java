/*
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.tools.dfgui;

// Import required Java classes 

import jade.core.AID;
import jade.domain.DFGUIAdapter;
import jade.domain.DFGUIInterface;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.gui.AboutJadeAction;
import jade.gui.JadeLogoButton;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class implements the GUI of the Directory Facilitator.
 * The gui shows a tabbed pane with three different views of the functions
 * provided by a Directory Facilitator.
 * The three views are: <ul>
 * <li><b>Registrations with this DF</b> shows a table with all the agents
 * registered with the DF.
 * <li><b>Search Result</b> shows a table with the list of agent descriptions that
 * were returned as a result of the last search operation on a specified df.
 * <li><b>DF Federation</b> shows the DF federation. The Parents table shows the list of DF's
 * with which this
 * DF is federated, while the Children table shows the list of DF's
 * that are registered with this DF.</ul>
 * According to the tab selected, the actions allowed have a proper meaning:<br/>
 * <b>Registrations with this df</b>.
 * <ul>
 *     <li><b>View</b> the description of the selected  agent from the table.
 *      <li><b>Modify</b> the description of the selected agent.
 * <li><b>Register</b> an agent with the DF. The user is then requested to fill in
 * an agent description, notice that
 * some values are mandatory for registration,
 * <li><b>Deregister</b> an agent selected in the table.
 * <li><b>Search</b> for agent descriptions with this DF. The user must first insert the
 * search constraint:   maximum depth   the depth of propagation of the search
 * operation to the federate DF's (children);  maximum number of results   .
 * If no values are inserted then the default one are used :local search on this df
 * and all agents found returned.The the user must provide an agent description.
 * If no value is inserted in the agent description, the search action returns
 * all the active agents currently registered with this DF.
 * <li><b>Federate</b> allow to federate this DF with another DF. First of all,
 * the user must provide the full name of the DF with which to federate and then the
 * description of this DF that must be registered with the specified DF.
 * </ul>
 * <b>Search Result</b>
 * <ul>
 * <li><b>View</b> the description of a selected agent on the table of the results.
 * <li><b>Register</b> a new agent with last DF used for the search operation (indicated in the tab).
 * <li><b>Modify</b> the agent description of the agent selected in the table (with the appropriate df).
 * <li><b>Search</b> for agent descriptions with the DF involved in the last search operation. (see above)
 * <li><b>Federation</b> (see above)
 * </ul>
 * <b>DF Federation</b>
 * <ul>
 * <li><b>View</b> the description of an agent selected in one of the two tables.
 * If the agent selected is a parent, then the description of this DF used to fedearate is shown.
 * Otherwise if the selected agent is a child,then the description of this child DF is shown.
 * <li><b>Register</b> a new agent with the DF selected in one of the two tables.
 * <li><b>Deregister</b> If the selected agent is a parent then this DF is deregistered from
 * the selected one, otherwise, if the agent selected is a child, this child is deregistered
 * from this DF.
 * <li><b>Search</b> permits to make a search operation with the DF selected in one of the tables.
 * <li><b>Federate</b> (see above).
 * </ul>
 *
 * @author Giovanni Caire - Tiziana Trucco - CSELT S.p.A.
 * @version $Date: 2008-10-09 14:04:02 +0200 (gio, 09 ott 2008) $ $Revision: 6051 $
 */

public class DFGUI extends JFrame implements DFGUIInterface {
    // class variables used to discriminate between the view of the dfgui.
    public static int AGENT_VIEW = 0;
    public static int LASTSEARCH_VIEW = 1;
    public static int PARENT_VIEW = 2;
    public static int CHILDREN_VIEW = 3;

    /**
     * @serial
     */
    DFGUIAdapter myAgent;
    /**
     * @serial
     */
    AgentNameTableModel registeredModel, foundModel, parentModel, childrenModel;
    /**
     * @serial
     */
    JTable registeredTable, foundTable, parentTable, childrenTable;
    /**
     * @serial
     */
    JSplitPane tablePane;
    /**
     * @serial
     */
    JTabbedPane tabbedPane;
    /**
     * @serial
     */
    JButton modifyB, deregB, regNewB, fedDFB, viewB, searchB, searchWithB;
    /**
     * @serial
     */
    JTextField statusField;
    /**
     * @serial
     */
    JScrollPane textScroll;
    /**
     * @serial
     */
    DFGUIModifyAction dfModifyAction;
    /**
     * @serial
     */
    DFGUIViewAction dfViewAction;
    /**
     * @serial
     */
    DFGUISearchAction dfSearchAction;
    /**
     * @serial
     */
    DFGUIRegisterAction dfRegAction;
    /**
     * @serial
     */
    DFGUIDeregisterAction dfDeregAction;
    /**
     * @serial
     */
    DFGUIFederateAction dfFedAction;


    JButton refreshB;
    JMenuItem refreshItem;


    HashMap<AID, DFAgentDescription> lastSearchResults; // this HashMap mantains the result of the last search made on a df.
    AID lastDF = null;                // this AID is the AID of the DF on which the last search was made.

    // CONSTRUCTORS

    /**
     * Constructor without parameter. Used by the df to avoid reflection,
     * Using this constructor, the method setAdapter must be called (after the constructor)
     * to set the agent with which the gui interacts.
     */
    public DFGUI() {
        //////////////////////////
        // Initialization
        super();
        lastSearchResults = new HashMap<>();

        Image image = getToolkit().getImage(getClass().getResource("images/df.gif"));
        setIconImage(image);

        setSize(550, 450);

        Icon viewImg = DFGuiProperties.getIcon("view");
        Icon modifyImg = DFGuiProperties.getIcon("modify");
        Icon deregImg = DFGuiProperties.getIcon("deregister");
        Icon regNewImg = DFGuiProperties.getIcon("registeragent");
        Icon fedDFImg = DFGuiProperties.getIcon("federatedf");
        Icon searchImg = DFGuiProperties.getIcon("search");

        /////////////////////////////////////
        // Add main menu to the GUI window
        JMenuBar jmb = new JMenuBar();
        JMenuItem item;

        JMenu generalMenu = new JMenu("General");
        item = generalMenu.add(new DFGUIExitDFAction(this));
        item = generalMenu.add(new DFGUICloseGuiAction(this));

        // feature only for applet
        refreshItem = generalMenu.add(new DFGUIRefreshAppletAction(this));
        refreshItem.setVisible(false);

        jmb.add(generalMenu);

        JMenu catalogueMenu = new JMenu("Catalogue");
        dfModifyAction = new DFGUIModifyAction(this);
        dfViewAction = new DFGUIViewAction(this);
        dfDeregAction = new DFGUIDeregisterAction(this);
        dfRegAction = new DFGUIRegisterAction(this);
        dfSearchAction = new DFGUISearchAction(this);


        item = catalogueMenu.add(dfViewAction);
        item.setIcon(viewImg);
        item = catalogueMenu.add(dfModifyAction);
        item.setIcon(modifyImg);
        item = catalogueMenu.add(dfDeregAction);
        item.setIcon(deregImg);
        item = catalogueMenu.add(dfRegAction);
        item.setIcon(regNewImg);
        item = catalogueMenu.add(dfSearchAction);
        item.setIcon(searchImg);


        jmb.add(catalogueMenu);

        JMenu superDFMenu = new JMenu("Super DF");
        dfFedAction = new DFGUIFederateAction(this);
        item = superDFMenu.add(dfFedAction);
        item.setIcon(fedDFImg);
        jmb.add(superDFMenu);

        JMenu helpMenu = new JMenu("Help");
        item = helpMenu.add(new DFGUIAboutAction(this));
        item = helpMenu.add(new AboutJadeAction(this));
        jmb.add(helpMenu);

        setJMenuBar(jmb);

        /////////////////////////////////////////////////////
        // Add Toolbar to the NORTH part of the border layout
        JToolBar bar = new JToolBar();

        // GENERAL
        Icon exitImg = DFGuiProperties.getIcon("exitdf");
        JButton exitB = bar.add(new DFGUIExitDFAction(this));
        exitB.setText("");
        exitB.setIcon(exitImg);
        exitB.setToolTipText("Exit and kill the DF agent");

        Icon closeImg = DFGuiProperties.getIcon("closegui");
        JButton closeB = bar.add(new DFGUICloseGuiAction(this));
        closeB.setText("");
        closeB.setIcon(closeImg);
        closeB.setToolTipText("Close the DF GUI");

        // feature only for applets.
        Icon refreshImg = DFGuiProperties.getIcon("refreshapplet");
        refreshB = bar.add(new DFGUIRefreshAppletAction(this));
        refreshB.setText("");
        refreshB.setIcon(refreshImg);
        refreshB.setToolTipText("Refresh the GUI");
        refreshB.setVisible(false);


        bar.addSeparator();

        // CATALOGUE

        viewB = bar.add(new DFGUIViewAction(this));
        viewB.setText("");
        viewB.setIcon(viewImg);
        viewB.setToolTipText("View the services provided by the selected agent");


        modifyB = bar.add(new DFGUIModifyAction(this));
        modifyB.setText("");
        modifyB.setIcon(modifyImg);
        modifyB.setToolTipText("Modify the services provided by the selected agent");


        deregB = bar.add(new DFGUIDeregisterAction(this));
        deregB.setText("");
        deregB.setIcon(deregImg);
        deregB.setToolTipText("Deregister the selected agent");


        regNewB = bar.add(new DFGUIRegisterAction(this));
        regNewB.setText("");
        regNewB.setIcon(regNewImg);
        regNewB.setToolTipText("Register a new agent with this DF");


        searchB = bar.add(new DFGUISearchAction(this));
        searchB.setText("");
        searchB.setIcon(searchImg);
        searchB.setToolTipText("Search for agents matching a given description");


        bar.addSeparator();

        // SUPER DF
        fedDFB = bar.add(new DFGUIFederateAction(this));
        fedDFB.setText("");
        fedDFB.setIcon(fedDFImg);
        fedDFB.setToolTipText("Federate this DF with another DF");

        bar.addSeparator();

        // HELP
        Icon aboutImg = DFGuiProperties.getIcon("about");
        JButton aboutB = bar.add(new DFGUIAboutAction(this));
        aboutB.setText("");
        aboutB.setIcon(aboutImg);
        aboutB.setToolTipText("About DF");

        bar.addSeparator(); //new Dimension(120,30));
        bar.add(Box.createHorizontalGlue());
        JadeLogoButton logo = new JadeLogoButton();
        bar.add(logo);

        getContentPane().add(bar, BorderLayout.NORTH);

        ////////////////////////////////////////////////////
        // Table Pane to the Center part
        // tablePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        // tablePane.setContinuousLayout(true);

        ////////////////////////////////////////////////////
        // JTabbedPane
        ////////////////////////////////////////////////////
        tabbedPane = new JTabbedPane();

        //////////////////////////////
        // Registered agents table
        JPanel registerPanel = new JPanel();
        registerPanel.setLayout(new BorderLayout());
        registeredModel = new AgentNameTableModel();
        registeredTable = new JTable(registeredModel);
        registeredTable.setRowHeight(20);
        registeredTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Column names
        TableColumn c;
        c = registeredTable.getColumn(registeredTable.getColumnName(0));
        c.setHeaderValue(("Agent name"));
        c = registeredTable.getColumn(registeredTable.getColumnName(1));
        c.setHeaderValue(("Addresses"));
        c = registeredTable.getColumn(registeredTable.getColumnName(2));
        c.setHeaderValue(("Resolvers"));

        // Doubleclick = view
        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DFGUIViewAction ac = new DFGUIViewAction(DFGUI.this);
                    ac.actionPerformed(new ActionEvent(this, 0, "View"));
                }
            }
        };
        registeredTable.addMouseListener(mouseListener);
        // Press Del = Deregister
        KeyListener keyListener = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_CANCEL || code == KeyEvent.VK_DELETE) {
                    DFGUIDeregisterAction ac = new DFGUIDeregisterAction(DFGUI.this);
                    ac.actionPerformed(new ActionEvent(this, 0, "Deregister"));
                }

            }
        };
        registeredTable.addKeyListener(keyListener);

        registerPanel.setLayout(new BorderLayout());
        JScrollPane pane = new JScrollPane();
        pane.getViewport().setView(registeredTable);
        registerPanel.add(pane, BorderLayout.CENTER);
        registerPanel.setBorder(BorderFactory.createEtchedBorder());

        tabbedPane.addTab("Registrations with this DF", registerPanel);
        tabbedPane.setSelectedIndex(0);

        /////////////////////////
        // Search result table

        JPanel lastSearchPanel = new JPanel();
        lastSearchPanel.setLayout(new BorderLayout());
        foundModel = new AgentNameTableModel();
        foundTable = new JTable(foundModel);
        foundTable.setRowHeight(20);
        foundTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        foundTable.setToolTipText("Double click on agent name to view the services provided by the selected agent");
        // Column names
        c = foundTable.getColumn(foundTable.getColumnName(0));
        c.setHeaderValue("Agent name");
        c = foundTable.getColumn(foundTable.getColumnName(1));
        c.setHeaderValue("Addresses");
        c = foundTable.getColumn(foundTable.getColumnName(2));
        c.setHeaderValue("Resolvers");

        // Doubleclick = view
        MouseListener mouseListener2 = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DFGUIViewAction ac = new DFGUIViewAction(DFGUI.this);
                    ac.actionPerformed(new ActionEvent(this, 0, "View"));
                }
            }
        };
        foundTable.addMouseListener(mouseListener2);


        lastSearchPanel.setLayout(new BorderLayout());
        pane = new JScrollPane();
        pane.getViewport().setView(foundTable);
        lastSearchPanel.add(pane, BorderLayout.CENTER);
        lastSearchPanel.setBorder(BorderFactory.createEtchedBorder());

        tabbedPane.addTab("Search Result", lastSearchPanel);

        JSplitPane tablePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        tablePane.setContinuousLayout(true);

        //////////////////////////////
        // Parent agents table
        JPanel parentPanel = new JPanel();
        parentPanel.setLayout(new BorderLayout());
        parentModel = new AgentNameTableModel();
        parentTable = new JTable(parentModel);
        parentTable.setRowHeight(20);
        parentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Column names

        c = parentTable.getColumn(parentTable.getColumnName(0));
        c.setHeaderValue("Agent name");
        c = parentTable.getColumn(parentTable.getColumnName(1));
        c.setHeaderValue("Addresses");
        c = parentTable.getColumn(parentTable.getColumnName(2));
        c.setHeaderValue("Resolvers");

        MouseListener mouseListenerParent = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 1)
                    childrenTable.clearSelection();
                if (e.getClickCount() == 2) {
                    DFGUIViewAction ac = new DFGUIViewAction(DFGUI.this);
                    ac.actionPerformed(new ActionEvent(this, 0, "View"));

                }
            }

        };

        parentTable.addMouseListener(mouseListenerParent);

        parentPanel.setLayout(new BorderLayout());
        JScrollPane pane1 = new JScrollPane();
        pane1.getViewport().setView(parentTable);
        parentPanel.add(pane1, BorderLayout.CENTER);
        parentPanel.setBorder(BorderFactory.createTitledBorder("Parents"));

        tablePane.setTopComponent(parentPanel);

        JPanel childrenPanel = new JPanel();
        childrenPanel.setLayout(new BorderLayout());
        childrenModel = new AgentNameTableModel();
        childrenTable = new JTable(childrenModel);
        childrenTable.setRowHeight(20);
        childrenTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Column names
        c = childrenTable.getColumn(childrenTable.getColumnName(0));
        c.setHeaderValue("Agent name");
        c = childrenTable.getColumn(childrenTable.getColumnName(1));
        c.setHeaderValue("Addresses");
        c = childrenTable.getColumn(childrenTable.getColumnName(2));
        c.setHeaderValue("Resolvers");

        MouseListener mouseListenerChildren = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 1)
                    parentTable.clearSelection();
                if (e.getClickCount() == 2) {
                    DFGUIViewAction ac = new DFGUIViewAction(DFGUI.this);
                    ac.actionPerformed(new ActionEvent(this, 0, "View"));
                }
            }

        };

        childrenTable.addMouseListener(mouseListenerChildren);

        pane1 = new JScrollPane();
        pane1.getViewport().setView(childrenTable);
        childrenPanel.add(pane1, BorderLayout.CENTER);
        childrenPanel.setBorder(BorderFactory.createTitledBorder("Children"));

        tablePane.setBottomComponent(childrenPanel);
        tablePane.setDividerLocation(150);


        tabbedPane.addTab("DF Federation", tablePane);
        tabbedPane.addChangeListener(new tabListener());

        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        ////////////////////////
        // Status message
        ////////////////////////
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));
        statusField = new JTextField();
        statusField.setEditable(false);
        statusPanel.add(statusField, BorderLayout.CENTER);
        getContentPane().add(statusPanel, BorderLayout.SOUTH);


        ////////////////////////////////////////////////////////////////
        // Execute the Close GUI action when the user attempts to close
        // the DF GUI window using the button on the upper right corner

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                DFGUICloseGuiAction ac = new DFGUICloseGuiAction(DFGUI.this);
                ac.actionPerformed(new ActionEvent(this, 0, "Close GUI"));
            }
        });
    }

    /**
     * Constructor with arguments.
     *
     * @param a the DFGUIAdapter with which the gui interacts.
     */
    public DFGUI(DFGUIAdapter a) {
        this();
        setAdapter(a);

    }

    /*
    This method must be used after the constructor of the gui
    to set the object implementing the DFGUIAdapter interface.
    It's necessary to avoid any dipendency between the JADE tools and
    the class of the domain package.
    */
    public void setAdapter(DFGUIAdapter a) {
        try {
            setTitle(a.getDescriptionOfThisDF().getName().getName() + "- DF Gui");
            myAgent = a;
        } catch (NullPointerException e) {
            e.printStackTrace();
            setTitle("Unknown DF - DF Gui");
        }


    }

    /**
     * Use this method to show a message on the DF GUI.
     *
     * @param msg the string to show
     */
    public void showStatusMsg(String msg) {
        statusField.setText(msg);
    }

    private void setButton(int tab) {
        switch (tab) {
            case 0, 1 -> { //setSearch(true);
                setDeregister(true);
                setRegister(true);
                setModify(true);
                setDFfed(true);
            }
            case 2 -> { //setSearch(true);
                setDeregister(true);
                setRegister(true);
                setModify(false);
                setDFfed(true);
            }
        }
    }

    private void setRegister(boolean value) {
        regNewB.setEnabled(value);
        dfRegAction.setEnabled(value);

    }

    private void setModify(boolean value) {
        modifyB.setEnabled(value);
        dfModifyAction.setEnabled(value);

    }

    private void setDeregister(boolean value) {
        deregB.setEnabled(value);
        dfDeregAction.setEnabled(value);

    }

    private void setDFfed(boolean value) {
        fedDFB.setEnabled(value);
        dfFedAction.setEnabled(value);
    }
		
	/*private void setSearch(boolean value)
	{
		searchB.setEnabled(value);
		dfSearchAction.setEnabled(value);

	}*/

    /**
     * This method permits to set the tabben pane to show.
     */
    public void setTab(String tab, AID df) {
        if (tab.equalsIgnoreCase("Search")) {
            tabbedPane.setSelectedIndex(1);
            tabbedPane.setTitleAt(1, "Last Search on " + df.getName());
        } else if (tab.equalsIgnoreCase("Federate"))
            tabbedPane.setSelectedIndex(2);
        else
            tabbedPane.setSelectedIndex(0);

    }

    /**
     * Returns the AID of an agent selected from one of the tables shown.
     */

    public AID getSelectedAgentInTable() {
        AID out = null;
        int tab = tabbedPane.getSelectedIndex();
        int row = -1;
        if (tab == 0) {
            //row = registeredTable.getSelectedRow();
            row = registeredTable.getSelectionModel().getMinSelectionIndex();

            if (row != -1)
                out = registeredModel.getElementAt(row);
            else out = null;
        } else if (tab == 1) {
            //row = foundTable.getSelectedRow();
            row = foundTable.getSelectionModel().getMinSelectionIndex();

            if (row != -1)
                out = foundModel.getElementAt(row);
            else
                out = null;
        } else if (tab == 2) {
            //row = parentTable.getSelectedRow();
            row = parentTable.getSelectionModel().getMinSelectionIndex();

            if (row != -1)
                out = parentModel.getElementAt(row);
            else {
                //row = childrenTable.getSelectedRow();
                row = childrenTable.getSelectionModel().getMinSelectionIndex();

                if (row != -1)
                    out = childrenModel.getElementAt(row);
                else out = null;
            }

        }


        return out;
    }

    /**
     * @return an integer according to the tab selected.
     */
    public int kindOfOperation() {

        int out = -1;
        int tab = tabbedPane.getSelectedIndex();

        if (tab == 0)
            out = AGENT_VIEW; //operation from descriptor table
        else if (tab == 1)
            out = LASTSEARCH_VIEW; // operation from lastsearch view
        else if (tab == 2) {
            //int rowSelected = parentTable.getSelectedRow();
            int rowSelected = parentTable.getSelectionModel().getMinSelectionIndex();

            if (rowSelected != -1)
                out = PARENT_VIEW; //OPERATION  from  parent table
            else {
                //rowSelected = childrenTable.getSelectedRow();
                rowSelected = childrenTable.getSelectionModel().getMinSelectionIndex();
                if (rowSelected != -1)
                    out = CHILDREN_VIEW; //OPERATION from children table
            }
        }
        return out;

    }

    /**
     * Refresh the DF GUI
     */
    public void refresh(Iterator<AID> AIDOfAllAgentRegistered, Iterator<AID> parents, Iterator<AID> children) {
        registeredModel.clear();
        while (AIDOfAllAgentRegistered.hasNext()) {
            registeredModel.add(AIDOfAllAgentRegistered.next());
        }
        registeredModel.fireTableDataChanged();

        parentModel.clear();
        while (parents.hasNext()) {
            parentModel.add(parents.next());
        }
        parentModel.fireTableDataChanged();

        childrenModel.clear();
        while (children.hasNext()) {
            childrenModel.add(children.next());
        }
        childrenModel.fireTableDataChanged();

        registeredTable.getSelectionModel().clearSelection();
        parentTable.getSelectionModel().clearSelection();
        childrenTable.getSelectionModel().clearSelection();


    }

    /**
     * Refresh the search result.
     */
    public void refreshLastSearchResults(ArrayList<DFAgentDescription> l, AID df) {

        this.lastDF = df; // the last df used

        foundModel.clear();
        lastSearchResults.clear();

        for (DFAgentDescription dfd : l) {
            foundModel.add(dfd.getName());
            lastSearchResults.put(dfd.getName(), dfd);
        }
        foundModel.fireTableDataChanged();
        foundTable.clearSelection();

    }

    /**
     * Removes an agent from the foundModel and lastSearchResult
     */
    public void removeSearchResult(AID name) {
        foundModel.remove(name);
        foundModel.fireTableDataChanged();
        lastSearchResults.remove(name);
        foundTable.clearSelection();
    }

    /**
     * Returns the AID of the last df on which a search operation was made.
     */

    public AID getLastDF() {
        return this.lastDF;
    }

    /**
     * adds a new parent to parentModel
     **/
    public void addParent(AID parentName) {

        parentModel.add(parentName);
        parentModel.fireTableDataChanged();

    }

    /**
     * adds a new child to parentModel
     **/

    public void addChildren(AID childrenName) {
        childrenModel.add(childrenName);
        childrenModel.fireTableDataChanged();
    }

    /**
     * Adds a new agent to registeredModel.
     */
    public void addAgentDesc(AID name) {

        registeredModel.add(name);
        registeredModel.fireTableDataChanged();

    }

    /**
     * Removes an agent descr from registeredModel and if it was found in a search operation
     * calls  removeSearchResult  .
     * //@see jade.tools.dfgui#removeSearchResult(AID name)
     */
    public void removeAgentDesc(AID name, AID df) {
        registeredModel.remove(name);
        registeredModel.fireTableDataChanged();
        registeredTable.clearSelection();
        //update the foundModel
        try {
            if (df.equals(lastDF))
                removeSearchResult(name);
        } catch (Exception ignored) {
        }

    }

    /**
     * Removes an agent desc from the childrenModel.
     */
    public void removeChildren(AID childrenName) {
        childrenModel.remove(childrenName);
        childrenModel.fireTableDataChanged();
        childrenTable.clearSelection();
    }

    /**
     * Removes an agent desc from the parentModel.
     */
    public void removeParent(AID parentName) {

        parentModel.remove(parentName);
        parentModel.fireTableDataChanged();
        parentTable.clearSelection();
    }

    /**
     * Shows DF GUI properly
     */
    public void setVisible(boolean b) {

        if (b) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int centerX = (int) screenSize.getWidth() / 2;
            int centerY = (int) screenSize.getHeight() / 2;
            setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);

        }
        super.setVisible(b);
    }

    /**
     * Performs asynchronous disposal to avoid nasty InterruptedException
     * printout.
     */
    public void disposeAsync() {

        class disposeIt implements Runnable {
            private final Window toDispose;

            public disposeIt(Window w) {
                toDispose = w;
            }

            public void run() {
                toDispose.dispose();
            }

        }

        // Make AWT Event Dispatcher thread dispose DF window for us.
        EventQueue.invokeLater(new disposeIt(this));
    }

    /**
     * This method returns the  DFAgentDescription   of an agent found in a search operation.
     *
     * @param name The AID of the agent.
     * @see DFAgentDescription
     * @see AID
     */
    public DFAgentDescription getDFAgentSearchDsc(AID name) {
        return lastSearchResults.get(name);
    }

    /*
     *This method must be called after the constructor to enable the RefreshGui action.
     It's necessary for the DFapplet.
     */
    public void enableRefreshButton() {
        refreshB.setVisible(true);
        refreshItem.setVisible(true);
    }

    class tabListener implements ChangeListener {
        public void stateChanged(ChangeEvent event) {
            Object object = event.getSource();
            if (object == tabbedPane)
                tabStateChanged(event);

        }

        public void tabStateChanged(ChangeEvent event) {
            int index = tabbedPane.getSelectedIndex();
            setButton(index);

        }

    }

}
