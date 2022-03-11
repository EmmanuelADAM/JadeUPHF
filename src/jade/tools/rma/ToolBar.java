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

package jade.tools.rma;

import jade.gui.JadeLogoButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Francisco Regi, Andrea Soracchi - Universita` di Parma
 * @version $Date: 2004-07-01 15:15:48 +0200 (gio, 01 lug 2004) $ $Revision: 5176 $
 */
final class ToolBar extends JToolBar implements ActionListener {

    //protected JComboBox ShowChoice = new JComboBox ();
    protected MainPanel tree;
    protected Frame mainWnd;
    protected ActionProcessor actPro;
    private RMAAction obj;

    public ToolBar(MainPanel treeP, Frame mainWnd, ActionProcessor actPro) { // RMAAction[] actions come arg 3
        super();
        tree = treeP;
        setBorderPainted(true);
        setFloatable(false);
        this.mainWnd = mainWnd;
        this.actPro = actPro;
        addSeparator();
        addAction();


        add(Box.createHorizontalGlue());
        JadeLogoButton logo = new JadeLogoButton();
        add(logo);

        //ShowChoice.setToolTipText("Show Agent as...");
        //ShowChoice.addItem("White Pages");
        //ShowChoice.addItem("Yellow Pages");
        //ShowChoice.addActionListener(this);
        //ShowChoice.setEnabled(false);      // Disabled
        //add(ShowChoice);
    }

    private void setButton(JButton b) {
        b.setToolTipText(obj.getActionName());
        b.setText("");
        b.setRequestFocusEnabled(false);
        b.setMargin(new Insets(1, 1, 1, 1));
    }

    private void addAction() {
        obj = ActionProcessor.actions.get(ActionProcessor.START_ACTION);
        setButton(add(obj));

        obj = ActionProcessor.actions.get(ActionProcessor.KILL_ACTION);
        setButton(add(obj));

        obj = ActionProcessor.actions.get(ActionProcessor.SUSPEND_ACTION);
        setButton(add(obj));

        obj = ActionProcessor.actions.get(ActionProcessor.RESUME_ACTION);
        setButton(add(obj));

        obj = ActionProcessor.actions.get(ActionProcessor.CUSTOM_ACTION);
        setButton(add(obj));

        obj = ActionProcessor.actions.get(ActionProcessor.MOVEAGENT_ACTION);
        setButton(add(obj));

        obj = ActionProcessor.actions.get(ActionProcessor.CLONEAGENT_ACTION);
        setButton(add(obj));

        addSeparator();

        obj = ActionProcessor.actions.get(ActionProcessor.LOADAGENT_ACTION);
        setButton(add(obj));

        obj = ActionProcessor.actions.get(ActionProcessor.SAVEAGENT_ACTION);
        setButton(add(obj));

        addSeparator();

        obj = ActionProcessor.actions.get(ActionProcessor.FREEZEAGENT_ACTION);
        setButton(add(obj));

        obj = ActionProcessor.actions.get(ActionProcessor.THAWAGENT_ACTION);
        setButton(add(obj));

        addSeparator();         // to add space between Sniffer,DummyAgent button and others buttons
        addSeparator();

        obj = ActionProcessor.actions.get(ActionProcessor.SNIFFER_ACTION);
        setButton(add(obj));

        obj = ActionProcessor.actions.get(ActionProcessor.DUMMYAG_ACTION);
        setButton(add(obj));

        obj = ActionProcessor.actions.get(ActionProcessor.LOGGERAG_ACTION);
        setButton(add(obj));

        obj = ActionProcessor.actions.get(ActionProcessor.INTROSPECTOR_ACTION);
        setButton(add(obj));

        obj = ActionProcessor.actions.get(ActionProcessor.ADDREMOTEPLATFORM_ACTION);
        setButton(add(obj));


    }

    public void actionPerformed(ActionEvent evt) {
        //TreeIconRenderer.setShowType(ShowChoice.getSelectedIndex());
        tree.repaint();
    }


}
