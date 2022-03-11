/*****************************************************************
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


package jade.tools.introspector.gui;

import jade.lang.acl.ACLMessage;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * The panel containing the message tables.
 *
 * @author Andrea Squeri,Corti Denis,Ballestracci Paolo -  Universita` di Parma
 */
public class MessagePanel extends JPanel {
    private final JTabbedPane incoming = new JTabbedPane();
    private final JTabbedPane outgoing = new JTabbedPane();
    private final JScrollPane inScrollPending = new JScrollPane();
    private final JScrollPane inScrollDone = new JScrollPane();
    private final JScrollPane outScrollPending = new JScrollPane();
    private final JScrollPane outScrollDone = new JScrollPane();
    private final JTable inMessagesPending;
    private final JTable inMessagesDone;
    private final JTable outMessagesPending;
    private final JTable outMessagesDone;
    private final MessageTableModel inPending;
    private final MessageTableModel inDone;
    private final MessageTableModel outPending;
    private final MessageTableModel outDone;
    private final TableMouseListener listener;

    public MessagePanel(MessageTableModel in1, MessageTableModel in2, MessageTableModel out1, MessageTableModel out2) {
        super();
        inPending = in1;
        inDone = in2;
        outPending = out1;
        outDone = out2;
        inMessagesPending = new JTable(inPending);
        inMessagesDone = new JTable(inDone);
        outMessagesPending = new JTable(outPending);
        outMessagesDone = new JTable(outDone);
        inMessagesPending.setName("Incoming Messages");
        inMessagesDone.setName("Incoming Messages");
        outMessagesPending.setName("Outgoing Messages");
        outMessagesPending.setName("Outgoing Messages");
        inMessagesPending.setDefaultRenderer(ACLMessage.class, new ACLMessageRenderer());
        inMessagesDone.setDefaultRenderer(ACLMessage.class, new ACLMessageRenderer());
        outMessagesPending.setDefaultRenderer(ACLMessage.class, new ACLMessageRenderer());
        outMessagesDone.setDefaultRenderer(ACLMessage.class, new ACLMessageRenderer());
        listener = new TableMouseListener();
        inMessagesPending.addMouseListener(listener);
        inMessagesDone.addMouseListener(listener);
        outMessagesPending.addMouseListener(listener);
        outMessagesDone.addMouseListener(listener);
        build();
    }

    public void build() {
        inScrollPending.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        inScrollDone.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        inScrollPending.setAutoscrolls(true);
        inScrollDone.setAutoscrolls(true);

        outScrollPending.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        outScrollDone.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        outScrollPending.setAutoscrolls(true);
        outScrollDone.setAutoscrolls(true);

        inScrollPending.getViewport().add(inMessagesPending, null);
        inScrollDone.getViewport().add(inMessagesDone, null);
        outScrollPending.getViewport().add(outMessagesPending, null);
        outScrollDone.getViewport().add(outMessagesDone, null);

        Border line = BorderFactory.createEtchedBorder();
        incoming.setBorder(BorderFactory.createTitledBorder(line, "Incoming Messages", TitledBorder.CENTER, TitledBorder.TOP));
        incoming.add(inScrollPending, "Pending");
        incoming.add(inScrollDone, "Received");
        outgoing.setBorder(BorderFactory.createTitledBorder(line, "Outgoing Messages", TitledBorder.CENTER, TitledBorder.TOP));
        outgoing.add(outScrollPending, "Pending");
        outgoing.add(outScrollDone, "Sent");

        setLayout(new GridLayout(1, 2));
        add(incoming);
        add(outgoing);
    }

    public MessageTableModel getInPendingModel() {
        return inPending;
    }

    public MessageTableModel getInProcessedModel() {
        return inDone;
    }

    public MessageTableModel getOutPendingModel() {
        return outPending;
    }

    public MessageTableModel getOutProcessedModel() {
        return outDone;
    }


    private static class ACLMessageRenderer extends DefaultTableCellRenderer {

        private final Icon myIcon;

        public ACLMessageRenderer() {
            super();
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            String path = "/jade/tools/introspector/gui/images/aclMessage.gif";
            myIcon = new ImageIcon(getClass().getResource(path));
            setIcon(myIcon);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            int height = myIcon.getIconHeight();
            if (table.getRowHeight() != height)
                table.setRowHeight(height);
            ACLMessage msg = (ACLMessage) value;
            setText(ACLMessage.getPerformative(msg.getPerformative()));
            setBackground(isSelected ? Color.cyan : Color.white);
            return this;
        }

    }

}


