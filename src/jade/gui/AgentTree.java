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

package jade.gui;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.APDescription;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.net.InetAddress;
import java.util.List;
import java.util.*;

/**
 * @author Francisco Regi, Andrea Soracchi - Universita' di Parma
 * @version $Date: 2010-04-12 18:07:05 +0200 (lun, 12 apr 2010) $ $Revision: 6302 $
 */
public class AgentTree extends JPanel {
    // FIXME: Use better values for TYPE constants
    public static final String TREE_ROOT_TYPE = "SUPERCONTAINER";

    public static final String LOCAL_PLATFORM_TYPE = "LOCALPLATFORM";

    public static final String AGENT_TYPE = "FIPAAGENT";
    public static final String FROZEN_AGENT_TYPE = "FROZENAGENT";
    public static final String CONTAINER_TYPE = "FIPACONTAINER";
    public static final String FROZEN_CONTAINER_TYPE = "FROZENCONTAINER";

    public static final String REMOTE_PLATFORMS_FOLDER_TYPE = "REMOTEPLATFORMS";
    public static final String REMOTE_PLATFORM_TYPE = "REMOTEPLATFORM";
    public static final String REMOTE_AGENT_TYPE = "REMOTEAGENT";

    public static final String TREE_ROOT_NAME = "AgentPlatforms";
    public static final String DAFAULT_LOCAL_PLATFORM_NAME = "ThisPlatform";
    public static final String REMOTE_PLATFORMS_FOLDER_NAME = "RemotePlatforms";
    public static final String FROZEN_AGENTS_FOLDER_NAME = "Frozen Agents";
    private final Map<String, NodeDescriptor> mapDescriptor;
    public JTree tree;
    private String localPlatformName = DAFAULT_LOCAL_PLATFORM_NAME;


    public AgentTree() {
        this(null);
    }


    public AgentTree(Font f) {
        mapDescriptor = new HashMap<>();
        register(TREE_ROOT_TYPE, null, "images/folderyellow.gif");
        register(LOCAL_PLATFORM_TYPE, null, "images/folderyellow.gif");
        register(CONTAINER_TYPE, null, "images/foldergreen.gif");
        register(AGENT_TYPE, null, "images/runtree.gif");

        register(FROZEN_CONTAINER_TYPE, null, "images/frozenagents.gif");
        register(FROZEN_AGENT_TYPE, null, "images/freezeagent.gif");

        register(REMOTE_PLATFORMS_FOLDER_TYPE, null, "images/folderblue.gif");
        register(REMOTE_PLATFORM_TYPE, null, "images/folderlightblue.gif");
        register(REMOTE_AGENT_TYPE, null, "images/runtree.gif");

        tree = new JTree();
        if (f != null) {
            tree.setFont(f);
        }
        tree.setModel(new AgentTreeModel(new SuperContainer(TREE_ROOT_NAME)));
        tree.setLargeModel(false);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        // Add localPlatform folder.
        AgentTreeModel model = getModel();
        MutableTreeNode root = (MutableTreeNode) model.getRoot();
        LocalPlatformFolderNode localAP = new LocalPlatformFolderNode(localPlatformName);
        model.insertNodeInto(localAP, root, root.getChildCount());

        ToolTipManager.sharedInstance().registerComponent(tree);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new TreeIconRenderer());
        tree.setRowHeight(0);

        tree.addMouseListener(new AgentTreePopupManager(this));
    }

    public AgentNode createAgentNode(String name) {
        return new AgentNode(name);
    }

    public ContainerNode createContainerNode(String name) {
        return new ContainerNode(name);
    }

    /**
     * @deprecated Use createAgentNode() and createContainerNode() instead
     */
    public Node createNewNode(String name, int i) {
        return switch (i) {
            case 0 -> new ContainerNode(name);
            case 1 -> new AgentNode(name);
            default -> null;
        };
    }

    public void refreshLocalPlatformName(String newName) {
        String oldName = localPlatformName;
        localPlatformName = newName;
        AgentTreeModel model = getModel();
        MutableTreeNode root = (MutableTreeNode) model.getRoot();
        Enumeration<? extends TreeNode> children = root.children();
        while (children.hasMoreElements()) {
            Node node = (Node) children.nextElement();
            String name = node.getName();
            if (name.equalsIgnoreCase(oldName)) {
                node.setName(newName);
                return;
            }
        }
    }

    public void clearLocalPlatform() {
        AgentTreeModel model = getModel();
        MutableTreeNode root = (MutableTreeNode) model.getRoot();
        Enumeration<? extends TreeNode> folders = root.children();
        while (folders.hasMoreElements()) {
            Node folderNode = (Node) folders.nextElement();
            String folderName = folderNode.getName();
            if (folderName.equalsIgnoreCase(localPlatformName)) {
                Enumeration<TreeNode> containers = folderNode.children();
                List<Node> toRemove = new LinkedList<>();
                while (containers.hasMoreElements()) {
                    Node container = (Node) containers.nextElement();
                    toRemove.add(container);
                }

                for (MutableTreeNode node : toRemove) {
                    model.removeNodeFromParent(node);
                }
            }
        }
    }

    public void addContainerNode(String containerName, InetAddress addr) {
        ContainerNode node = new ContainerNode(containerName);
        node.setAddress(addr);

        AgentTreeModel model = getModel();
        MutableTreeNode root = (MutableTreeNode) model.getRoot();
        Enumeration<? extends TreeNode> folders = root.children();
        while (folders.hasMoreElements()) {
            Node folderNode = (Node) folders.nextElement();
            String folderName = folderNode.getName();
            if (folderName.equalsIgnoreCase(localPlatformName)) {
                model.insertNodeInto(node, folderNode, folderNode.getChildCount());
                return;
            }
        }
    }

    /**
     * @deprecated Use addContainerNode(String, InetAddress) instead
     */
    public void addContainerNode(ContainerNode node, String typeContainer, InetAddress addr) {
        AgentTreeModel model = getModel();
        MutableTreeNode root = (MutableTreeNode) model.getRoot();
        node.setType(typeContainer);
        Enumeration<? extends TreeNode> folders = root.children();
        while (folders.hasMoreElements()) {
            Node folderNode = (Node) folders.nextElement();
            String folderName = folderNode.getName();
            if (folderName.equalsIgnoreCase(localPlatformName)) {
                model.insertNodeInto(node, folderNode, folderNode.getChildCount());
                break;
            }
        }
        node.setAddress(addr);
    }

    public void removeContainerNode(String nameNode) {
        AgentTreeModel model = getModel();
        MutableTreeNode root = (MutableTreeNode) model.getRoot();
        Enumeration<? extends TreeNode> folders = root.children();
        while (folders.hasMoreElements()) {
            Node folderNode = (Node) folders.nextElement();
            String folderName = folderNode.getName();
            if (folderName.equalsIgnoreCase(localPlatformName)) {//found the localplatform folder
                Enumeration<TreeNode> containers = folderNode.children();
                while (containers.hasMoreElements()) {
                    Node node = (Node) containers.nextElement();
                    String nodeName = node.getName();
                    if (nodeName.equalsIgnoreCase(nameNode)) {
                        model.removeNodeFromParent(node);
                        return;
                    }
                }
            }
        }
    }

    //public void listenerTree(TreeSelectionListener panel) {
    //	tree.addTreeSelectionListener(panel);
    //}

    public void addRemotePlatformsFolderNode() {
        AgentTreeModel model = getModel();
        MutableTreeNode root = (MutableTreeNode) model.getRoot();
        Enumeration<? extends TreeNode> children = root.children();

        boolean existing = false;

        while (children.hasMoreElements() & (!existing)) {
            Node node = (Node) children.nextElement();
            String nodeName = node.getName();
            if (nodeName.equalsIgnoreCase(REMOTE_PLATFORMS_FOLDER_NAME))
                existing = true;
        }

        if (!existing) {
            RemotePlatformsFolderNode rpn = new RemotePlatformsFolderNode(REMOTE_PLATFORMS_FOLDER_NAME);
            model.insertNodeInto(rpn, root, root.getChildCount());
        }

    }

    public void addAgentNode(String agentName, String agentAddress, String containerName) {
        addAgentNode(agentName, agentAddress, containerName, "Running", null);
    }

    public void addAgentNode(String agentName, String agentAddress, String containerName, String agentState, String ownership) {
        AgentNode node = new AgentNode(agentName);
        node.setAddress(agentAddress);
        node.setState(agentState);
        node.setOwnership(ownership);

        AgentTreeModel model = getModel();
        MutableTreeNode root = (MutableTreeNode) model.getRoot();

        // Search for the folder of the local Platform
        Enumeration<? extends TreeNode> folders = root.children();
        while (folders.hasMoreElements()) {
            Node folderNode = (Node) folders.nextElement();
            String folderName = folderNode.getName();
            if (folderName.equalsIgnoreCase(localPlatformName)) {
                // Search for the agent container 'containerName'
                Enumeration<TreeNode> containers = folderNode.children();
                while (containers.hasMoreElements()) {
                    Node containerNode = (Node) containers.nextElement();
                    String contName = containerNode.getName();
                    if (contName.equalsIgnoreCase(containerName)) {
                        // Add this new agent to this container and return
                        int position = getPosition(node, containerNode);
                        model.insertNodeInto(node, containerNode, position);
                        return;
                    }
                }
            }
        }
    }

    private int getPosition(Node node, Node parentNode) {
        int size = parentNode.getChildCount();
        if (size == 0) {
            // This is the first child
            return 0;
        } else {
            int k = node.compareTo((Node) parentNode.getChildAt(0));
            if (k < 0) {
                // Insert new child at the beginning of the list
                return 0;
            } else {
                k = node.compareTo((Node) parentNode.getChildAt(size - 1));
                if (k >= 0) {
                    // Insert new child at the end of the list
                    return size;
                } else {
                    // Insert new child "somewhere" in the list
                    return getPosition(node, parentNode, 0, size - 1);
                }
            }
        }
    }

    private int getPosition(Node node, Node parentNode, int down, int up) {
        if ((up - down) == 1) {
            return up;
        } else {
            int middle = (up + down) / 2;
            int k = node.compareTo((Node) parentNode.getChildAt(middle));
            if (k == 0) {
                return middle + 1;
            } else if (k < 0) {
                return getPosition(node, parentNode, down, middle);
            } else {
                return getPosition(node, parentNode, middle, up);
            }
        }
    }

    /**
     * @deprecated Use addAgentNode(String, String, String) instead
     */
    public void addAgentNode(AgentNode node, String containerName, String agentName, String agentAddress, String agentType) {
        AgentTreeModel model = getModel();
        MutableTreeNode root = (MutableTreeNode) model.getRoot();
        node.setType(agentType);
        node.setAddress(agentAddress);
        node.setState("Running");
        //search for the folder of the local Platform
        Enumeration<? extends TreeNode> folders = root.children();
        while (folders.hasMoreElements()) {
            Node folderNode = (Node) folders.nextElement();
            String folderName = folderNode.getName();
            if (folderName.equalsIgnoreCase(localPlatformName)) {
                // Search for the agent container 'containerName'
                Enumeration<TreeNode> containers = folderNode.children();
                while (containers.hasMoreElements()) {
                    Node container = (Node) containers.nextElement();
                    String contName = container.getName();
                    if (contName.equalsIgnoreCase(containerName)) {
                        // Add this new agent to this container and return
                        model.insertNodeInto(node, container, container.getChildCount());
                        return;
                    }
                }
            }
        }
    }

    public void modifyAgentNode(String containerName, String agentName, String address, String state, String ownership) {
        AgentTreeModel model = getModel();
        MutableTreeNode root = (MutableTreeNode) model.getRoot();
        //search for the folder of the local Platform
        Enumeration<? extends TreeNode> folders = root.children();
        while (folders.hasMoreElements()) {
            Node folderNode = (Node) folders.nextElement();
            String folderName = folderNode.getName();
            if (folderName.equalsIgnoreCase(localPlatformName)) {
                // Search for the agent container 'containerName'
                Enumeration<TreeNode> containers = folderNode.children();
                while (containers.hasMoreElements()) {
                    Node container = (Node) containers.nextElement();
                    String contName = container.getName();
                    if (contName.equalsIgnoreCase(containerName)) {
                        Enumeration<TreeNode> agents = container.children();
                        while (agents.hasMoreElements()) {
                            Node agent = (Node) agents.nextElement();

                            if (agent.getName().equalsIgnoreCase(agentName)) {
                                if (state != null)
                                    agent.setState(state);
                                if (ownership != null)
                                    agent.setOwnership(ownership);
                                agent.changeIcon(state);
                                model.nodeChanged(agent);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    public void moveAgentNode(String fromContainerName, String toContainerName, String agentName) {
        AgentTreeModel model = getModel();
        Node fromContainer = findContainerNode(fromContainerName);
        Node toContainer = findContainerNode(toContainerName);

        // If there is a frozen agent already, do nothing, else move the agent node
        Node frozenAgents = findFrozenAgentsFolder(toContainer, FROZEN_AGENTS_FOLDER_NAME);
        if (frozenAgents != null) {
            Node agent = findAgentNode(frozenAgents, agentName);
            if (agent == null) {
                // Move the agent node
                agent = findAgentNode(fromContainer, agentName);
                model.removeNodeFromParent(agent);
                model.insertNodeInto(agent, toContainer, toContainer.getChildCount());
            }
        } else {
            // Move the agent node
            Node agent = findAgentNode(fromContainer, agentName);
            model.removeNodeFromParent(agent);
            model.insertNodeInto(agent, toContainer, toContainer.getChildCount());
        }
    }

    public void freezeAgentNode(String oldContainerName, String newContainerName, String agentName) {
        AgentTreeModel model = getModel();
        Node oldContainer = findContainerNode(oldContainerName);
        Node agent = findAgentNode(oldContainer, agentName);
        model.removeNodeFromParent(agent);

        agent.setState("frozen");
        agent.changeIcon("frozen");

        Node newContainer = findContainerNode(newContainerName);
        Node frozenAgents = findFrozenAgentsFolder(newContainer, FROZEN_AGENTS_FOLDER_NAME);
        if (frozenAgents == null) {
            frozenAgents = createContainerNode(FROZEN_AGENTS_FOLDER_NAME);
            frozenAgents.setType(FROZEN_CONTAINER_TYPE);
            model.insertNodeInto(frozenAgents, newContainer, 0);
        }
        model.insertNodeInto(agent, frozenAgents, frozenAgents.getChildCount());

    }

    public void thawAgentNode(String oldContainerName, String newContainerName, String agentName) {
        AgentTreeModel model = getModel();
        Node oldContainer = findContainerNode(oldContainerName);
        Node frozenAgents = findFrozenAgentsFolder(oldContainer, FROZEN_AGENTS_FOLDER_NAME);
        Node agent = findAgentNode(frozenAgents, agentName);
        model.removeNodeFromParent(agent);
        if (frozenAgents.isLeaf()) {
            model.removeNodeFromParent(frozenAgents);
        }

        agent.setState("active");
        agent.changeIcon("active");

        Node newContainer = findContainerNode(newContainerName);
        model.insertNodeInto(agent, newContainer, newContainer.getChildCount());

    }

    public void removeAgentNode(String containerName, String agentName) {
        AgentTreeModel model = getModel();
        Node container = findContainerNode(containerName);
        if (container != null) {
            Node agent = findAgentNode(container, agentName);

            if (agent != null) {
                model.removeNodeFromParent(agent);
            } else {
                // It can be a frozen agent
                Node frozenAgents = findFrozenAgentsFolder(container, FROZEN_AGENTS_FOLDER_NAME);
                if (frozenAgents != null) {
                    agent = findAgentNode(frozenAgents, agentName);

                    model.removeNodeFromParent(agent);
                    if (frozenAgents.isLeaf()) {
                        model.removeNodeFromParent(frozenAgents);
                    }
                }
            }
        }
    }

    public void addRemotePlatformNode(AID ams, APDescription desc) {

        AgentTreeModel model = getModel();
        MutableTreeNode root = (MutableTreeNode) model.getRoot();

        // Search for the folder REMOTEPLATFORM
        Enumeration<? extends TreeNode> containers = root.children();
        while (containers.hasMoreElements()) {//1
            Node container = (Node) containers.nextElement();
            String contName = container.getName();
            if (contName.equalsIgnoreCase(REMOTE_PLATFORMS_FOLDER_NAME)) {//2
                boolean found = false;
                Enumeration<TreeNode> agents = container.children();
                while (agents.hasMoreElements() && !found) {//3
                    RemotePlatformNode platform = (RemotePlatformNode) agents.nextElement();
                    String APName = platform.getName();
                    if (APName.equalsIgnoreCase(desc.getName())) {//update the APDescription of this node
                        platform.setAPDescription(desc);
                        found = true;
                    }
                }//3
                if (!found) {
                    // Add this new platform to this container and return
                    RemotePlatformNode node = new RemotePlatformNode(desc.getName());
                    node.setAPDescription(desc);
                    node.setAmsAID(ams);
                    model.insertNodeInto(node, container, container.getChildCount());
                }
                return;
            }//2
        }//1
    }

    public void removeRemotePlatformNode(String name) {
        AgentTreeModel model = getModel();
        MutableTreeNode root = (MutableTreeNode) model.getRoot();

        // Search for the  RemotePlatforms node
        Enumeration<? extends TreeNode> containers = root.children();
        while (containers.hasMoreElements()) {
            Node container = (Node) containers.nextElement();
            String contName = container.getName();
            if (contName.equalsIgnoreCase(REMOTE_PLATFORMS_FOLDER_NAME)) {
                // Search for the ams
                Enumeration<TreeNode> agents = container.children();
                while (agents.hasMoreElements()) {
                    Node agent = (Node) agents.nextElement();
                    String agName = agent.getName();
                    if (agName.equalsIgnoreCase(name)) {
                        model.removeNodeFromParent(agent);
                        //if it's the last child remove the folder REMOTEPLATFORMS
                        if (container.getChildCount() == 0)
                            model.removeNodeFromParent(container);
                        return;
                    }
                }
            }
        }
    }

    public void addRemoteAgentNode(AMSAgentDescription agent, String HAP) {

        AgentTreeModel model = getModel();
        MutableTreeNode root = (MutableTreeNode) model.getRoot();

        //Search for the REMOTEPLATFORMS node
        Enumeration<? extends TreeNode> containers = root.children();

        while (containers.hasMoreElements()) {

            Node container = (Node) containers.nextElement();
            String contName = container.getName();

            if (contName.equalsIgnoreCase(REMOTE_PLATFORMS_FOLDER_NAME)) {
                //search the remotePlatform
                Enumeration<TreeNode> plat_Enum = container.children();

                while (plat_Enum.hasMoreElements()) {
                    Node platformNode = (Node) plat_Enum.nextElement();
                    String platformNodeName = platformNode.getName();
                    if (platformNodeName.equalsIgnoreCase(HAP)) {
                        //now add remote agent registered with that ams...
                        Enumeration<TreeNode> remote_agents = platformNode.children();
                        boolean found = false;
                        while (remote_agents.hasMoreElements() && !found) {

                            RemoteAgentNode node = (RemoteAgentNode) remote_agents.nextElement();
                            String remoteName = node.getName();
                            if (remoteName.equalsIgnoreCase(agent.getName().getName())) {
                                node.setAMSDescription(agent); //update the AMSDescription
                                found = true;
                            }
                        }
                        if (!found) {
                            RemoteAgentNode newNode = new RemoteAgentNode(agent.getName().getName());
                            newNode.setAMSDescription(agent);
                            int position = getPosition(newNode, platformNode);
                            model.insertNodeInto(newNode, platformNode, position);
                        }
                    }
                }
            }
        }
    }

    public void clearRemotePlatformAgents(String HAP) {

        AgentTreeModel model = getModel();
        MutableTreeNode root = (MutableTreeNode) model.getRoot();

        //Search for the REMOTEPLATFORMS node
        Enumeration<? extends TreeNode> containers = root.children();

        while (containers.hasMoreElements()) {

            Node container = (Node) containers.nextElement();
            String contName = container.getName();

            if (contName.equalsIgnoreCase(REMOTE_PLATFORMS_FOLDER_NAME)) {
                //search the remotePlatform
                Enumeration<TreeNode> plat_Enum = container.children();

                while (plat_Enum.hasMoreElements()) {
                    Node platformNode = (Node) plat_Enum.nextElement();
                    String platformNodeName = platformNode.getName();

                    if (platformNodeName.equalsIgnoreCase(HAP)) {
                        Enumeration<TreeNode> en = platformNode.children();
                        List<TreeNode> remoteAgents = new ArrayList<>();
                        while (en.hasMoreElements()) {
                            remoteAgents.add(en.nextElement());
                        }
                        for (TreeNode agent : remoteAgents) {
                            Node remoteAgent = (Node) agent;
                            model.removeNodeFromParent(remoteAgent);
                        }
                    }
                }
            }
        }
    }

    public Node getSelectedNode() {
        TreePath path = tree.getSelectionPath();
        if (path != null) {
            return (Node) path.getLastPathComponent();
        } else {
            return null;
        }
    }

    public AgentTreeModel getModel() {
        if (tree.getModel() instanceof AgentTreeModel)
            return (AgentTreeModel) tree.getModel();
        else {
            System.out.println(tree.getModel());
            return null;
        }
    }

    public void register(String key, JPopupMenu popmenu, String pathImage) {
        NodeDescriptor nDescriptor = new NodeDescriptor(popmenu, pathImage);
        mapDescriptor.put(key, nDescriptor);
    }

    public JPopupMenu getPopupMenu(String key) {
        NodeDescriptor nDescriptor = mapDescriptor.get(key);
        return nDescriptor.getPopupMenu();
    }

    public void setNewPopupMenu(String key, JPopupMenu pop) {
        if (mapDescriptor.containsKey(key)) {
            NodeDescriptor nDescriptor = mapDescriptor.get(key);
            nDescriptor.setNewPopupMenu(pop);
        }
    }

    protected String getIconAgent(String key) {
        NodeDescriptor nDescriptor = mapDescriptor.get(key);
        return nDescriptor.getPathImage();
    }

    private Node findAgentNode(Node container, String name) {

        Enumeration<TreeNode> agents = container.children();
        while (agents.hasMoreElements()) {
            Node agent = (Node) agents.nextElement();
            if (agent.getName().equalsIgnoreCase(name)) {
                return agent;
            }
        }

        return null;
    }

    private Node findContainerNode(String name) {
        AgentTreeModel model = getModel();
        MutableTreeNode root = (MutableTreeNode) model.getRoot();
        //search for the folder of the local Platform
        Enumeration<? extends TreeNode> folders = root.children();
        while (folders.hasMoreElements()) {
            Node folderNode = (Node) folders.nextElement();
            String folderName = folderNode.getName();
            if (folderName.equalsIgnoreCase(localPlatformName)) {
                // Search for the agent container 'name'
                Enumeration<TreeNode> containers = folderNode.children();
                while (containers.hasMoreElements()) {
                    Node container = (Node) containers.nextElement();
                    String contName = container.getName();
                    if (contName.equalsIgnoreCase(name)) {
                        return container;
                    }
                }
            }
        }

        return null;

    }

    private Node findFrozenAgentsFolder(Node container, String name) {
        Enumeration<TreeNode> agents = container.children();
        while (agents.hasMoreElements()) {
            Node child = (Node) agents.nextElement();
            if (child.getName().equalsIgnoreCase(name) && child.getType().equalsIgnoreCase(FROZEN_CONTAINER_TYPE)) {
                return child;
            }
        }

        return null;
    }

    /**
     * Inner class Node
     * Common base class for all AgentTree nodes
     */
    public abstract class Node extends DefaultMutableTreeNode {

        protected Icon img;
        protected String name;
        protected String state;
        protected String ownership;
        protected boolean greyOut = false;

        public Node(String name) {
            this.name = name;
        }

        public Icon getIcon(String typeAgent) {
            Image image = getToolkit().getImage(getClass().getResource(getIconAgent(typeAgent)));
            if (greyOut) {
                ImageFilter colorfilter = new MyFilterImage();
                Image imageFiltered = createImage(new FilteredImageSource(image.getSource(), colorfilter));
                return new ImageIcon(imageFiltered);
            } else
                return new ImageIcon(image);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getState() {
            return state != null ? state : "";
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getOwnership() {
            return ownership != null ? ownership : "";
        }

        public void setOwnership(String ownership) {
            this.ownership = ownership;
        }

        public void changeIcon(String agentState) {
            if (agentState.equalsIgnoreCase("suspended")) {
                greyOut = true;
                setType(AGENT_TYPE);
            } else if (agentState.equalsIgnoreCase("active")) {
                greyOut = false;
                setType(AGENT_TYPE);
            } else if (agentState.equalsIgnoreCase("frozen")) {
                greyOut = false;
                setType(FROZEN_AGENT_TYPE);
            }
        }

        public abstract String getType();

        public abstract void setType(String type);

        public abstract String getToolTipText();

        public String toString() {
            return (getType() != null ? getType() + "-" + name : name);
        }

        public int compareTo(Node n) {
            return name.compareTo(n.getName());
        }
    } // END of inner class Node

    /**
     * Inner class AgentNode
     */
    public class AgentNode extends Node {
        private String agentType;
        private String agentAddress;

        public AgentNode(String name) {
            super(name);
            agentType = AGENT_TYPE;
        }

        public String getAddress() {
            return agentAddress;
        }

        public void setAddress(String address) {
            agentAddress = address;
        }

        public String getType() {
            return agentType;
        }

        public void setType(String type) {
            agentType = type;
        }

        public String getToolTipText() {
            return ("Local Agent");
        }
    }  // END of inner class AgentNode

    /**
     * Inner class ContainerNode
     */
    public class ContainerNode extends Node {
        private InetAddress addressmachine;
        private String containerType;

        public ContainerNode(String name) {
            super(name);
            containerType = CONTAINER_TYPE;
        }

        public void setAddress(InetAddress addr) {
            addressmachine = addr;
        }

        public String getType() {
            return containerType;
        }

        public void setType(String type) {
            containerType = type;
        }

        public String getToolTipText() {
            if (addressmachine != null)
                return name + " " + "[" + addressmachine.getHostAddress() + "]";
            else
                return name + " " + "[???:???:???:???]";
        }
    } // END of inner class ContainerNode

    /**
     * Inner class SuperContainer
     */
    public class SuperContainer extends Node {

        public SuperContainer(String name) {
            super(name);
        }

        public String getToolTipText() {
            return ("Java Agent DEvelopment Framework");
        }

        public String getType() {
            return TREE_ROOT_TYPE;
        }

        public void setType(String noType) {
        }
    } // END of inner class SuperContainer

    /**
     * Inner class RemotePlatformsFolderNode
     */
    public class RemotePlatformsFolderNode extends Node {

        public RemotePlatformsFolderNode(String name) {
            super(name);
        }

        public String getToolTipText() {
            return ("List of RemotePlatforms");
        }

        public String getType() {
            return (REMOTE_PLATFORMS_FOLDER_TYPE);
        }

        public void setType(String noType) {
        }
    } // END of inner class RemotePlatformsFolderNode

    /**
     * Inner class localPlatformFolderNode
     */
    public class LocalPlatformFolderNode extends Node {

        public LocalPlatformFolderNode(String name) {
            super(name);
        }

        public String getToolTipText() {
            return ("Local JADE Platform");
        }

        public String getType() {
            return ("LOCALPLATFORM");
        }

        public void setType(String noType) {
        }
    } // END of inner class LocalPlatformFolderNode

    /**
     * Inner class RemotePlatformNode
     */
    public class RemotePlatformNode extends Node {

        private APDescription AP_Profile;
        private AID amsAID;

        public RemotePlatformNode(String name) {
            super(name);
        }

        public String getToolTipText() {
            return ("Remote Platform");
        }

        public String getType() {
            return ("REMOTEPLATFORM");
        }

        public void setType(String noType) {
        }

        public APDescription getAPDescription() {
            return AP_Profile;
        }

        public void setAPDescription(APDescription desc) {
            AP_Profile = desc;
        }

        public AID getAmsAID() {
            return amsAID;
        }

        public void setAmsAID(AID id) {
            amsAID = id;
        }
    } // END of inner class RemotePlatformNode

    /**
     * Inner class RemoteAgentNode
     */
    public class RemoteAgentNode extends AgentNode {

        private AMSAgentDescription amsd;

        public RemoteAgentNode(String name) {
            super(name);
        }

        public String getToolTipText() {
            return ("Remote Agent");
        }

        public String getType() {
            return ("REMOTEAGENT");
        }

        public void setType(String noType) {
        }

        public AMSAgentDescription getAMSDescription() {
            return amsd;
        }

        public void setAMSDescription(AMSAgentDescription id) {
            amsd = id;
        }
    } // END of inner class RemoteAgentNode
}
