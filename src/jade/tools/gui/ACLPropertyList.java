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
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * This Class show a list of properties beloning to an ACLMessage
 *
 * @author Chris van Aart - Acklin B.V., the Netherlands
 * @since April 26, 2002
 */

public class ACLPropertyList extends JPanel {

    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JList<String> contentList = new JList<>();
    private final JButton viewButton = new JButton();
    private final JButton addButton = new JButton();
    private final JButton deleteButton = new JButton();
    private final ACLPropertyListCellRenderer aclPropertyListCellRenderer = new ACLPropertyListCellRenderer();
    private final ACLPropertyListener theDataListener = new ACLPropertyListener();
    private final JScrollPane contentScrollPane = new JScrollPane();
    private boolean editable = true;
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private String fieldName = "";
    private ACLMessage msg;


    /**
     * Constructor for the ACLPropertyList object
     */
    public ACLPropertyList() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the Editable attribute of the ACLPropertyList object
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
     * Register ACLMessage
     *
     * @param msg       the ACLMessage
     * @param fieldName the methodName of the ACLMessage
     */
    public void register(ACLMessage msg, String fieldName) {
        listModel = new DefaultListModel<>();
        this.msg = msg;
        this.fieldName = fieldName;
        Properties prop = msg.getAllUserDefinedParameters();
        this.aclPropertyListCellRenderer.register(msg);
        Enumeration<Object> enumeration = prop.elements();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            listModel.addElement(key);
        }
        contentList.setModel(listModel);
        theDataListener.register(msg, fieldName);
        listModel.addListDataListener(theDataListener);
    }

    /**
     * show a property
     */
    void doView() {
        int index = this.contentList.getSelectedIndex();
        if (index < 0) {
            return;
        }
        String currentKey = listModel.getElementAt(index);
        ACLPropertyDialog theDialog = new ACLPropertyDialog();
        theDialog.setLocation((int) getLocationOnScreen().getX(), (int) getLocationOnScreen().getY());
        theDialog.setItskey(currentKey);
        theDialog.setItsvalue(msg.getAllUserDefinedParameters().getProperty(currentKey));
        theDialog.setEdit();
        theDialog.setEditable(editable);
        theDialog.setTitle(editable ? "Edit property: " + currentKey : "View property: " + currentKey);

        theDialog.setVisible(true);
        if (theDialog.getOK()) {
            theDataListener.registerChangedProperty(currentKey, theDialog.getItsvalue());
            listModel.setElementAt(currentKey, index);
        }
    }

    /**
     * Description of the Method
     *
     * @param e Description of Parameter
     */
    void deleteButton_actionPerformed(ActionEvent e) {
        doDelete();
    }

    /**
     * delete a property
     */
    void doDelete() {
        int index = contentList.getSelectedIndex();
        if (index >= 0) {
            theDataListener.registerRemovedKey(listModel.getElementAt(index));
            this.listModel.remove(index);
        }
    }

    /**
     * Adds a feature to the Button_actionPerformed attribute of the
     * ACLPropertyList object
     *
     * @param e The feature to be added to the Button_actionPerformed
     *          attribute
     */
    void addButton_actionPerformed(ActionEvent e) {
        doAdd();
    }

    /**
     * Add an property
     */
    void doAdd() {
        ACLPropertyDialog theDialog = new ACLPropertyDialog();
        theDialog.setLocation((int) getLocationOnScreen().getX(), (int) getLocationOnScreen().getY());
        theDialog.setVisible(true);
        theDialog.setTitle("<new property>");
        if (theDialog.getOK()) {
            theDataListener.registerChangedProperty(theDialog.getItskey(), theDialog.getItsvalue());
            listModel.addElement(theDialog.getItskey());
        }
        this.validate();
    }

    /**
     * Description of the Method
     *
     * @param e Description of Parameter
     */
    void viewButton_actionPerformed(ActionEvent e) {
        doView();
    }

    /**
     * Description of the Method
     *
     * @param e Description of Parameter
     */
    void contentList_mouseClicked(MouseEvent e) {
        if (e.getClickCount() > 1) {
            doView();
        }

    }

    /**
     * Description of the Method
     *
     * @param entry Description of Parameter
     * @return Description of the Returned Value
     */
    String filterEntry(String entry) {
        String result = "<?>";
        try {
            StringTokenizer stok = new StringTokenizer(entry, "=");
            result = (String) stok.nextElement();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * Description of the Method
     *
     * @param e Description of Parameter
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
     * Description of the Method
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
        viewButton.setToolTipText("edit/view property");
        viewButton.setMargin(new Insets(0, 0, 0, 0));
        viewButton.setText("v");
        viewButton.addActionListener(
                this::viewButton_actionPerformed);
        addButton.setBackground(Color.white);
        addButton.setFont(new Font("Dialog", Font.PLAIN, 11));
        addButton.setForeground(new Color(0, 0, 83));
        addButton.setMinimumSize(new Dimension(13, 5));
        addButton.setPreferredSize(new Dimension(13, 11));
        addButton.setToolTipText("add property");
        addButton.setMargin(new Insets(0, 0, 0, 0));
        addButton.setText("+");
        addButton.addActionListener(
                this::addButton_actionPerformed);
        deleteButton.setBackground(Color.white);
        deleteButton.setFont(new Font("Dialog", Font.PLAIN, 11));
        deleteButton.setForeground(new Color(0, 0, 83));
        deleteButton.setMinimumSize(new Dimension(13, 5));
        deleteButton.setPreferredSize(new Dimension(13, 11));
        deleteButton.setToolTipText("delete property");
        deleteButton.setMargin(new Insets(0, 0, 0, 0));
        deleteButton.setText("x");
        deleteButton.addActionListener(
                this::deleteButton_actionPerformed);
        contentList.setCellRenderer(aclPropertyListCellRenderer);
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
        contentScrollPane.getViewport().add(contentList, null);

        this.add(contentScrollPane, new GridBagConstraints(0, 0, 1, 3, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        this.add(addButton, new GridBagConstraints(1, 1, GridBagConstraints.REMAINDER, 1, 0.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
        this.add(deleteButton, new GridBagConstraints(1, 2, GridBagConstraints.REMAINDER, 1, 0.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
        this.add(viewButton, new GridBagConstraints(1, 0, GridBagConstraints.REMAINDER, 1, 0.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
    }

    private static class ACLPropertyListCellRenderer extends JLabel implements ListCellRenderer<Object> {
        private ACLMessage msg;


        /**
         * Constructor for the ACLPropertyListCellRenderer object
         */
        public ACLPropertyListCellRenderer() {
            setOpaque(true);
            setFont(new Font("Dialog", Font.PLAIN, 11));
        }

        /**
         * Gets the ListCellRendererComponent attribute of the
         * ACLPropertyListCellRenderer object
         *
         * @param list         the list
         * @param value        the object
         * @param index        the index
         * @param isSelected   is it selected
         * @param cellHasFocus does it has the focus?
         * @return The ListCellRendererComponent value
         */
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if ((value instanceof String sValue) && (msg != null)) {
                String key = msg.getUserDefinedParameter(sValue);
                setText(sValue + " = " + key);
            }
            setBackground(isSelected ? Color.blue : Color.white);
            setForeground(isSelected ? Color.white : Color.black);
            return this;
        }

        /**
         * register the ACLMessage
         *
         * @param msg the ACLMessage
         */
        public void register(ACLMessage msg) {
            this.msg = msg;
        }
    }

    private static class ACLPropertyDialog extends JDialog {

        private final String CANCELLED = "cancelled";
        private final String CLOSED = "closed";
        private final GridBagLayout gridBagLayout1 = new GridBagLayout();
        private final JTextField keyTextField = new JTextField();
        private final JLabel jLabel1 = new JLabel();
        private final JButton okButton = new JButton();
        private final JButton cancelButton = new JButton();
        private final JLabel jLabel2 = new JLabel();
        private final JTextField valueTextField = new JTextField();
        private String OK = "ok";
        private String userAction;
        private String itsAddress;
        private String itskey;
        private String itsvalue;
        /**
         * Constructor for the ACLPropertyDialog object
         */
        public ACLPropertyDialog() {
            this.setModal(true);
            try {
                jbInit();
                pack();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        /**
         * Gets the UserAction attribute of the ACLPropertyDialog object
         *
         * @return The UserAction value
         */
        public String getUserAction() {
            return userAction;
        }

        /**
         * Sets the UserAction attribute of the ACLPropertyDialog object
         *
         * @param newUserAction The new UserAction value
         */
        public void setUserAction(String newUserAction) {
            userAction = newUserAction;
        }

        /**
         * Gets the OK attribute of the ACLPropertyDialog object
         *
         * @return The OK value
         */
        public boolean getOK() {
            return userAction.equals(OK);
        }

        /**
         * Gets the Itskey attribute of the ACLPropertyDialog object
         *
         * @return The Itskey value
         */
        public String getItskey() {
            return itskey;
        }

        /**
         * Sets the Itskey attribute of the ACLPropertyDialog object
         *
         * @param newItskey The new Itskey value
         */
        public void setItskey(String newItskey) {
            keyTextField.setText(newItskey);
            itskey = newItskey;
        }

        /**
         * Gets the Itsvalue attribute of the ACLPropertyDialog object
         *
         * @return The Itsvalue value
         */
        public String getItsvalue() {
            return itsvalue;
        }

        /**
         * Sets the Itsvalue attribute of the ACLPropertyDialog object
         *
         * @param newItsvalue The new Itsvalue value
         */
        public void setItsvalue(String newItsvalue) {
            valueTextField.setText(newItsvalue);
            itsvalue = newItsvalue;
        }

        /**
         * Sets the Editable attribute of the ACLPropertyDialog object
         *
         * @param theBool The new Editable value
         */
        public void setEditable(boolean theBool) {
            if (!theBool) {
                OK = "CLOSED";
                this.cancelButton.setVisible(false);
                this.valueTextField.setEditable(false);
            }
        }

        /**
         * Sets the Edit attribute of the ACLPropertyDialog object
         */
        public void setEdit() {
            keyTextField.setEditable(false);
        }

        /**
         * Description of the Method
         * <p>
         * Exception  Description of Exception
         */
        void jbInit() {
            this.getContentPane().setLayout(gridBagLayout1);
            jLabel1.setFont(new Font("Dialog", Font.PLAIN, 12));
            jLabel1.setText("key");
            this.getContentPane().setBackground(Color.white);
            okButton.setBackground(Color.white);
            okButton.setFont(new Font("Dialog", Font.PLAIN, 12));
            okButton.setText("ok");
            okButton.addActionListener(
                    this::okButton_actionPerformed);
            cancelButton.setBackground(Color.white);
            cancelButton.setFont(new Font("Dialog", Font.PLAIN, 12));
            cancelButton.setText("cancel");
            cancelButton.addActionListener(
                    this::cancelButton_actionPerformed);
            jLabel2.setFont(new Font("Dialog", Font.PLAIN, 12));
            jLabel2.setText("value");
            this.getContentPane().add(keyTextField, new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
            this.getContentPane().add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            this.getContentPane().add(jLabel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            this.getContentPane().add(cancelButton, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            this.getContentPane().add(valueTextField, new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
            this.getContentPane().add(okButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
                    , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, -16, 0, 0), 0, 0));
        }

        /**
         * Description of the Method
         *
         * @param e Description of Parameter
         */
        void cancelButton_actionPerformed(ActionEvent e) {
            setUserAction(CANCELLED);
            setVisible(false);
        }

        /**
         * Description of the Method
         *
         * @param e Description of Parameter
         */
        void okButton_actionPerformed(ActionEvent e) {
            setItskey(keyTextField.getText());
            setItsvalue(valueTextField.getText());
            setUserAction(OK);
            setVisible(false);
        }

    }

    /**
     * This class is the ListDataListener of the ACLPropertyList.
     *
     * @author Chris van Aart - Acklin B.V., the Netherlands
     * @since April 26, 2002
     */

    private static class ACLPropertyListener implements ListDataListener {
        private String fieldName = "";
        private String theRemovedKey, theChangedKey, theChangedValue;
        private ACLMessage itsMsg;

        /**
         * Description of the Method
         *
         * @param obj       Description of Parameter
         * @param fieldName Description of Parameter
         */
        public void register(Object obj, String fieldName) {
            itsMsg = (ACLMessage) obj;
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
            String key = (String) lm.elementAt(index);
            itsMsg.addUserDefinedParameter(theChangedKey, theChangedValue);
        }

        /**
         * Description of the Method
         *
         * @param theChangedKey   Description of Parameter
         * @param theChangedValue Description of Parameter
         */
        public void registerChangedProperty(String theChangedKey, String theChangedValue) {
            this.theChangedKey = theChangedKey;
            this.theChangedValue = theChangedValue;
        }

        /**
         * Description of the Method
         *
         * @param theRemovedKey Description of Parameter
         */
        public void registerRemovedKey(String theRemovedKey) {
            this.theRemovedKey = theRemovedKey;
        }

        /**
         * Description of the Method
         *
         * @param parm1 Description of Parameter
         */
        public void intervalRemoved(ListDataEvent parm1) {
            String methodName = "remove" + fieldName;
            String theType = "jade.core.AID";
            itsMsg.removeUserDefinedParameter(theRemovedKey);
        }

        /**
         * Description of the Method
         *
         * @param parm1 Description of Parameter
         */
        public void contentsChanged(ListDataEvent parm1) {
            DefaultListModel<?> lm = (DefaultListModel<?>) parm1.getSource();
            int index = parm1.getIndex0();
            itsMsg.removeUserDefinedParameter(theChangedKey);
            itsMsg.addUserDefinedParameter(theChangedKey, theChangedValue);
        }
    }

}
//  ***EOF***
