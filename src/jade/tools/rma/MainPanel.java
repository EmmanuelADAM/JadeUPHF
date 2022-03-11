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

import jade.core.AID;
import jade.gui.APDescriptionPanel;
import jade.gui.AgentTree;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.ACLParser;
import jade.lang.acl.ParseException;
import jade.util.Logger;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Francisco Regi, Andrea Soracchi - Universita` di Parma
 * @version $Date: 2010-04-12 18:07:05 +0200 (lun, 12 apr 2010) $ $Revision: 6302 $
 */
class MainPanel extends JPanel implements DropTargetListener, TreeSelectionListener {

    private APDescriptionPanel APDescription_panel;
    private final AgentTree treeAgent;       // FIXME: It should be private
    private final TablePanel table;
    private JScrollPane scroll;
    private final JSplitPane pan;
    private JSplitPane pane;
    private final MainWindow mainWnd;
    private PopupMouser popM;
    private rma myRma = null;

    //logging
    private static final Logger logger = Logger.getMyLogger(MainPanel.class.getName());


    public MainPanel(rma anRMA, MainWindow mainWnd) {

        myRma = anRMA;
        table = new TablePanel();
        this.mainWnd = mainWnd;
        Font f;
        f = new Font("SanSerif", Font.PLAIN, 14);
        setFont(f);
        setLayout(new BorderLayout(10, 10));

        treeAgent = new AgentTree(f);
        new DropTarget(treeAgent.tree, this);

        //To allow single selection on the tree.
        //  treeAgent.tree.getSelectionModel().setSelectionMode
        //  (javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION);


        pan = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(treeAgent.tree), table.createTable());
        pan.setContinuousLayout(true);

        add(pan);

        treeAgent.tree.addTreeSelectionListener(this);
        //popM=new PopupMouser(treeAgent.tree,treeAgent);
        //treeAgent.tree.addMouseListener(popM);

    }

    public void dragOver(DropTargetDragEvent p1) {
        if (logger.isLoggable(Logger.FINEST))
            logger.log(Logger.FINEST, "dragOver");
    }

    public void dropActionChanged(DropTargetDragEvent p1) {
        if (logger.isLoggable(Logger.FINEST))
            logger.log(Logger.FINEST, "dropActionChanged");
    }

    public void dragEnter(DropTargetDragEvent dragEvent) {
        if (treeAgent.tree.getSelectionCount() < 1) {
            // FIXME: This output should probably be put in a message dialog
            // or something.
            if (logger.isLoggable(Logger.FINEST))
                logger.log(Logger.FINEST, "No agents selected!");
            dragEvent.rejectDrag();
        }
    }

    public void dragExit(DropTargetEvent p1) {
        if (logger.isLoggable(Logger.FINEST))
            logger.log(Logger.FINEST, "dragExit");
    }

    public void drop(DropTargetDropEvent dropEvent) {
        boolean completionStatus = false;
        TreePath[] selectedItems = null;
        List<?> fileList = null;

        try {
            selectedItems = treeAgent.tree.getSelectionPaths();
            if (selectedItems == null) {
                // FIXME: This output should probably be put in a message dialog
                // or something.
                if (logger.isLoggable(Logger.WARNING))
                    logger.log(Logger.WARNING, "No agents selected!");
                dropEvent.rejectDrop();
                dropEvent.dropComplete(completionStatus);
                return;
            }

            dropEvent.acceptDrop(DnDConstants.ACTION_COPY);
            Transferable xferInfo = dropEvent.getTransferable();

            fileList = (List<?>) (xferInfo.getTransferData(DataFlavor.javaFileListFlavor));

            completionStatus = true;
        } catch (UnsupportedFlavorException exc) {
            completionStatus = false;
        } catch (IOException exc) {
            // FIXME: This output should probably be put in a message dialog
            // or something.
            if (logger.isLoggable(Logger.WARNING))
                logger.log(Logger.WARNING, "DragAndDrop operation failed: " + exc);

            completionStatus = false;
        } finally {
            dropEvent.dropComplete(completionStatus);
        }


        if ((fileList != null) && (selectedItems != null)) {
            Iterator<?> fileItor = fileList.iterator();
            ACLParser aclParser = ACLParser.create();
            while (fileItor.hasNext()) {
                try {
                    File f = (File) (fileItor.next());
                    FileReader aclMsgFile = new FileReader(f);
                    ACLMessage msg = aclParser.parse(aclMsgFile);

                    msg.clearAllReceiver();
                    msg.clearAllReplyTo();
                    msg.setSender(myRma.getAID());

                    for (TreePath selectedItem : selectedItems) {
                        Object lastPath = selectedItem.getLastPathComponent();
                        if (lastPath instanceof AgentTree.Node node) {
                            AID recipient = new AID(node.getName(), AID.ISGUID);
                            msg.addReceiver(recipient);
                        }
                    } //~ for(int i=0;i<numPaths;i++)

                    myRma.send(msg);
                } catch (IOException exc) {
                    // FIXME: This output should probably be put in a message dialog
                    // or something.
                    if (logger.isLoggable(Logger.WARNING))
                        logger.log(Logger.WARNING, "Unable to send message: " + exc);
                } catch (ParseException exc) {
                    // FIXME: This output should probably be put in a message dialog
                    // or something.
                    if (logger.isLoggable(Logger.WARNING))
                        logger.log(Logger.WARNING, "Unable to send message: " + exc);
                } catch (Exception exc) {
                    // FIXME: This output should probably be put in a message dialog
                    // or something.
                    if (logger.isLoggable(Logger.WARNING))
                        logger.log(Logger.WARNING, "Unable to send message: " + exc);
                } catch (Error exc) {
                    // FIXME: This output should probably be put in a message dialog
                    // or something.
                    if (logger.isLoggable(Logger.WARNING))
                        logger.log(Logger.WARNING, "Unable to send message: " + exc);
                } catch (Throwable exc) {
                    // FIXME: This output should probably be put in a message dialog
                    // or something.
                    if (logger.isLoggable(Logger.WARNING))
                        logger.log(Logger.WARNING, "Unable to send message: " + exc);
                }
            } //~ while (fileItor.hasNext())
        } //~ if (selectedItems != null)

    } //END drop(dropEvent)

    public void valueChanged(TreeSelectionEvent e) {
        TreePath[] paths = treeAgent.tree.getSelectionPaths();
        Object[] relCur;

        if (paths != null) {

            ArrayList<TreePath> agentPaths = new ArrayList<>();
            for (TreePath path : paths) {
                relCur = path.getPath();
                for (Object o : relCur) {
                    if (o instanceof AgentTree.AgentNode)
                        //to display in the table only the agent.
                        agentPaths.add(path);
                    else if (o instanceof AgentTree.SuperContainer) {//show the APDescription in the TextArea
                    }
                }
            }

            //table.setData(paths);
            TreePath[] agents = new TreePath[agentPaths.size()];
            for (int i = 0; i < agentPaths.size(); i++)
                agents[i] = agentPaths.get(i);
            table.setData(agents);
        }
    }


    public void adjustDividersLocation() {
        //int rootSize = pane.getDividerLocation(); // This is the height of a single tree folder
        //pane.setDividerLocation(7*rootSize); // The initial agent tree has 6 elements; one more empty space
        pan.setDividerLocation(300);


    }

    public Dimension getPreferredSize() {
        return new Dimension(200, 200);
    }

    public AgentTree getTreeAgent() {
        return treeAgent;
    }
} 
