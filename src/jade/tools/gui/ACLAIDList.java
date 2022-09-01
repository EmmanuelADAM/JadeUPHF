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

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * This class is used to show a list of AIDs. AIDs can be edited, inserted
 * and deleted.
 *
 * @author Chris van Aart - Acklin B.V., the Netherlands
 * @since April 26, 2002
 */
public class ACLAIDList extends JPanel {
    private final String MSG = "msg";
    private final String AID = "Aid";
    private final JScrollPane contentScrollPane = new JScrollPane();
    private final JList<AID> contentList = new JList<>();
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JButton viewButton = new JButton();
    private final JButton addButton = new JButton();
    private final JButton deleteButton = new JButton();
    private final DefaultListModel<AID> listModel = new DefaultListModel<>();
    private final AIDListCellRenderer aidListCellRenderer = new AIDListCellRenderer();
    private final Agent agent;
    private boolean editable = true;
    private String fieldName = "";
    private String mode = MSG;
    private AIDListListener theDataListener;
    private ACLMessage itsMsg;
    private AID itsAid;
    private Object itsObj;
    /**
     * Constructor for the ACLAIDList object
     *
     * @param agent link to agent
     */
    public ACLAIDList(Agent agent) {
        this.agent = agent;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the Editable attribute of the ACLAIDList object
     *
     * @param theBool The new Editable value
     */
    public void setEditable(boolean theBool) {
        if (!theBool) {
            editable = false;
            this.addButton.setEnabled(false);
            this.deleteButton.setEnabled(false);
        }
    }

    /**
     * register Object and accompagnied field name in ACLMessage
     *
     * @param fieldName the fieldname
     * @param o         Description of Parameter
     */
    public void register(Object o, String fieldName) {
        listModel.removeAllElements();
        this.itsObj = o;
        mode = MSG;
        this.fieldName = fieldName;
        String methodName = "getAll" + fieldName;
        try {
            Method sn = itsObj.getClass().getMethod(methodName, (Class<?>[]) null);
            Iterator<?> itor = (Iterator<?>) sn.invoke(itsObj, new Object[]{});
            while (itor.hasNext()) {
                AID theAID = (AID) itor.next();
                listModel.addElement(theAID);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        theDataListener = new AIDListListener();
        theDataListener.register(itsObj, fieldName);
        listModel.addListDataListener(theDataListener);
        contentList.setModel(listModel);
    }

    /**
     * register AID and fieldname from ACLMessage
     *
     * @param aid       the AID to register
     * @param fieldName fieldname of ACLMessage
     */
    public void register(AID aid, String fieldName) {
        this.itsAid = aid;
        mode = AID;
        this.fieldName = fieldName;
        String methodName = "getAll" + fieldName;
        try {
            Method sn = aid.getClass().getMethod(methodName, (Class<?>[]) null);
            Iterator<?> itor = (Iterator<?>) sn.invoke(aid, new Object[]{});
            while (itor.hasNext()) {
                AID theAID = (AID) itor.next();
                listModel.addElement(theAID);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        theDataListener = new AIDListListener();
        theDataListener.register(aid, fieldName);
        listModel.addListDataListener(theDataListener);
        contentList.setModel(listModel);
    }

    /**
     * show the current selected AID
     */
    void doView() {
        int index = this.contentList.getSelectedIndex();
        if (index < 0) {
            return;
        }
        AID currentAID = listModel.getElementAt(index);
        AID editAID = (AID) currentAID.clone();
        ACLAIDDialog theDialog = new ACLAIDDialog(agent);
        theDialog.setLocation((int) getLocationOnScreen().getX(), (int) getLocationOnScreen().getY());
        theDialog.setItsAID(editAID);
        theDialog.setEditable(editable);
        theDialog.setTitle(editable ? "Edit AID of: " + currentAID.getName() : "View AID of:" + currentAID.getName());
        theDialog.setVisible(true);
        if (theDialog.getOK()) {
            theDataListener.registerChangedAID(theDialog.getItsAID());
            listModel.setElementAt(editAID, index);
        }
    }

    /**
     * Triggered by delete button
     *
     * @param e ActionEvent belonging to delete button
     */
    void deleteButton_actionPerformed(ActionEvent e) {
        doDelete();
    }

    /**
     * delete selected AID
     */
    void doDelete() {
        int index = contentList.getSelectedIndex();
        if (index >= 0) {
            theDataListener.registerRemovedAID(listModel.getElementAt(index));
            this.listModel.remove(index);
        }
    }

    /**
     * Triggered by add button
     *
     * @param e ActionEvent belonging to add button
     */
    void addButton_actionPerformed(ActionEvent e) {
        doAdd();
    }

    /**
     * Add a AID to the list
     */
    void doAdd() {
        ACLAIDDialog theGui = new ACLAIDDialog(agent);
        theGui.setTitle("<new AID>");
        theGui.setLocation((int) getLocationOnScreen().getX(), (int) getLocationOnScreen().getY());
        theGui.localCheckBox.setSelected(false);
        theGui.setVisible(true);
        if (theGui.getOK()) {
            listModel.addElement(theGui.getItsAID());
        }

    }

    /**
     * Triggered by the view button
     *
     * @param e ActionEvent belonging to view button
     */
    void viewButton_actionPerformed(ActionEvent e) {
        doView();
    }

    /**
     * Triggered when clicking with mouse pointer. On doubleclick, call
     * doView()
     *
     * @param e MouseEvent beloning to mouseClicked
     */
    void contentList_mouseClicked(MouseEvent e) {
        if (e.getClickCount() > 1) {
            doView();
        }

    }

    /**
     * Triggered when pressing a key. Enter will call doView(), Insert will
     * call doAdd(), and Delete will call doDelete().
     *
     * @param e KeyEvent beloning to keyPressed
     */
    void contentList_keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            doView();
        }

        if (!editable) {
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_INSERT) {
            doAdd();
        }

        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            doDelete();
        }

    }

    /**
     * Builds up the componenent
     * <p>
     * Exception  Description of Exception
     */
    private void jbInit() {
        this.setLayout(gridBagLayout1);
        viewButton.setBackground(Color.white);
        viewButton.setFont(new Font("Dialog", Font.PLAIN, 11));
        viewButton.setForeground(new Color(0, 0, 83));
        viewButton.setMinimumSize(new Dimension(13, 5));
        viewButton.setPreferredSize(new Dimension(13, 11));
        viewButton.setToolTipText("edit/view AgentIDentifier (AID)");
        viewButton.setMargin(new Insets(0, 0, 0, 0));
        viewButton.setText("v");
        viewButton.addActionListener(
                this::viewButton_actionPerformed);
        addButton.setBackground(Color.white);
        addButton.setFont(new Font("Dialog", Font.PLAIN, 11));
        addButton.setForeground(new Color(0, 0, 83));
        addButton.setMinimumSize(new Dimension(13, 5));
        addButton.setPreferredSize(new Dimension(13, 11));
        addButton.setToolTipText("add AgentIDentifier (AID)");
        addButton.setMargin(new Insets(0, 0, 0, 0));
        addButton.setText("+");
        addButton.addActionListener(
                this::addButton_actionPerformed);
        deleteButton.setBackground(Color.white);
        deleteButton.setFont(new Font("Dialog", Font.PLAIN, 11));
        deleteButton.setForeground(new Color(0, 0, 83));
        deleteButton.setMinimumSize(new Dimension(13, 5));
        deleteButton.setPreferredSize(new Dimension(13, 11));
        deleteButton.setToolTipText("delete AgentIDentifier (AID)");
        deleteButton.setMargin(new Insets(0, 0, 0, 0));
        deleteButton.setText("x");
        deleteButton.addActionListener(
                this::deleteButton_actionPerformed);
        contentList.setCellRenderer(aidListCellRenderer);
        contentList.addKeyListener(
                new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                        contentList_keyPressed(e);
                    }
                });
        contentList.addMouseListener(
                new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        contentList_mouseClicked(e);
                    }
                });
        contentScrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
        this.add(addButton, new GridBagConstraints(2, 1, GridBagConstraints.REMAINDER, 1, 0.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
        this.add(deleteButton, new GridBagConstraints(2, 2, GridBagConstraints.REMAINDER, 1, 0.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
        this.add(viewButton, new GridBagConstraints(2, 0, GridBagConstraints.REMAINDER, 1, 0.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
        this.add(contentScrollPane, new GridBagConstraints(0, 0, 1, 3, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        contentScrollPane.getViewport().add(contentList, null);
    }

    public static class AIDListCellRenderer extends JLabel implements ListCellRenderer<Object> {
        /**
         * Constructor for the AIDListCellRenderer object
         */
        public AIDListCellRenderer() {
            setOpaque(true);
            setFont(new Font("Dialog", Font.PLAIN, 11));
        }


        /**
         * Gets the ListCellRendererComponent attribute of the
         * AIDListCellRenderer object
         *
         * @param list         Description of Parameter
         * @param value        Description of Parameter
         * @param index        Description of Parameter
         * @param isSelected   Description of Parameter
         * @param cellHasFocus Description of Parameter
         * @return The ListCellRendererComponent value
         */
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if ((value instanceof AID theAID)) {
                setText(theAID.getName());
            }
            setBackground(isSelected ? Color.blue : Color.white);
            setForeground(isSelected ? Color.white : Color.black);
            return this;
        }
    }

    /**
     * This class listenes to the AIDList
     *
     * @author Chris van Aart - Acklin B.V., the Netherlands
     * @since April 26, 2002
     */

    public static class AIDListListener implements ListDataListener {
        String fieldName = "";
        private AID theRemovedAID, theChangedAID;
        private Object itsObj;

        /**
         * Description of the Method
         *
         * @param obj       Description of Parameter
         * @param fieldName Description of Parameter
         */
        public void register(Object obj, String fieldName) {
            itsObj = obj;
            this.fieldName = fieldName;
        }

        /**
         * Description of the Method
         *
         * @param parm1 Description of Parameter
         */
        public void intervalAdded(ListDataEvent parm1) {
            DefaultListModel<?> lm = (DefaultListModel<?>) parm1.getSource();
            int index = parm1.getIndex0();
            AID newAID = (AID) lm.elementAt(index);
            String methodName = "add" + fieldName;
            String theType = "jade.core.AID";
            try {
                Method sn = itsObj.getClass().getMethod(methodName, Class.forName(theType));
                sn.invoke(itsObj, newAID);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        /**
         * Description of the Method
         *
         * @param theRemovedAID Description of Parameter
         */
        public void registerRemovedAID(AID theRemovedAID) {
            this.theRemovedAID = theRemovedAID;
        }

        /**
         * Description of the Method
         *
         * @param theChangedAID Description of Parameter
         */
        public void registerChangedAID(AID theChangedAID) {
            this.theChangedAID = theChangedAID;
        }

        /**
         * Description of the Method
         *
         * @param parm1 Description of Parameter
         */
        public void intervalRemoved(ListDataEvent parm1) {
            String methodName = "remove" + fieldName;
            String theType = "jade.core.AID";
            try {
                Method sn = itsObj.getClass().getMethod(methodName, Class.forName(theType));
                sn.invoke(itsObj, theRemovedAID);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        /**
         * Description of the Method
         *
         * @param parm1 Description of Parameter
         */
        public void contentsChanged(ListDataEvent parm1) {
            DefaultListModel<?> lm = (DefaultListModel<?>) parm1.getSource();
            int index = parm1.getIndex0();
            if (index < 0) {
                return;
            }

            String removeMethodName = "remove" + fieldName;
            String addMethodName = "remove" + fieldName;

            AID currentAID = (AID) lm.get(index);

            String theType = "jade.core.AID";
            try {
                Method removeMethod = itsObj.getClass().getMethod(removeMethodName, Class.forName(theType));
                removeMethod.invoke(itsObj, currentAID);

                Method addMethod = itsObj.getClass().getMethod(addMethodName, Class.forName(theType));
                removeMethod.invoke(itsObj, theChangedAID);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

}
//  ***EOF***
