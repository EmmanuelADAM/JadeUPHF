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

package jade.core;

import jade.security.Credentials;
import jade.security.JADEPrincipal;

import java.io.Serializable;

/**
 * The  NodeDescriptor   class serves as a meta-level
 * description of a kernel-level service.
 * Instances of this class contain a  Node   object,
 * along with its name and properties, and are used in service
 * management operations, as well as in agent-level introspection of
 * platform-level entities.
 *
 * @author Giovanni Rimassa - FRAMeTech s.r.l.
 * @see Node
 */
public class NodeDescriptor implements Serializable {

    private String myName;
    private Node myNode;
    private Node parentNode;
    private ContainerID myContainer;
    private String username;
    private byte[] password;
    private JADEPrincipal myPrincipal;
    private JADEPrincipal ownerPrincipal;
    private Credentials ownerCredentials;

    /**
     * Builds a new node descriptor, describing the given node with
     * the given name and properties.
     *
     * @param node The described  Node   object.
     */
    public NodeDescriptor(Node node) {
        myName = node.getName();
        myNode = node;
    }

    /**
     * Builds a node descriptor for a node hosting an agent container.
     *
     * @param cid  The container ID for the hosted container.
     * @param node The described  Node   object.
     */
    public NodeDescriptor(ContainerID cid, Node node) {
        myName = cid.getName();
        myNode = node;
        myContainer = cid;
    }

    /**
     * Builds an uninitialized node descriptor.
     *
     * @see NodeDescriptor#setName(String sn)
     * @see NodeDescriptor#setNode(Node node)
     */
    public NodeDescriptor() {
    }

    /**
     * Retrieve the name (if any) of the described node.
     *
     * @return The name of the described node, or  null
     * if no name was set.
     */
    public String getName() {
        return myName;
    }

    /**
     * Change the name (if any) of the described node.
     *
     * @param nn The name to assign to the described node.
     */
    public void setName(String nn) {
        myName = nn;
    }

    /**
     * Retrieve the described node.
     *
     * @return The  Node   object described by this
     * node descriptor, or  null   if no node was set.
     */
    public Node getNode() {
        return myNode;
    }

    /**
     * Change the described node (if any).
     *
     * @param node The  Node   object that is to be
     *             described by this node descriptor.
     */
    public void setNode(Node node) {
        myNode = node;
    }

    /**
     * Retrieve the ID of the container (if any) hosted by the
     * described node.
     *
     * @return The  ContainerID   of the hosted container,
     * or  null   if no such container was set.
     */
    public ContainerID getContainer() {
        return myContainer;
    }

    public Node getParentNode() {
        return parentNode;
    }

    public void setParentNode(Node n) {
        parentNode = n;
    }

    /**
     * Retrieve the username of the owner of the described node
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the username of the owner of the described node
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Retrieve the password of the owner of the described node
     */
    public byte[] getPassword() {
        return password;
    }

    /**
     * Set the password of the owner of the described node
     */
    public void setPassword(byte[] password) {
        this.password = password;
    }

    /**
     * Retrieve the principal of the described node
     */
    public JADEPrincipal getPrincipal() {
        return myPrincipal;
    }

    /**
     * Set the principal of the described node
     */
    public void setPrincipal(JADEPrincipal principal) {
        myPrincipal = principal;
    }

    /**
     * Retrieve the principal of the owner of this node (if any)
     *
     * @return The principal of the owner of this node, or
     *  null   if no principal was set.
     */
    public JADEPrincipal getOwnerPrincipal() {
        return ownerPrincipal;
    }

    /**
     * Set the principal of the owner of this node
     */
    public void setOwnerPrincipal(JADEPrincipal principal) {
        ownerPrincipal = principal;
    }

    /**
     * Retrieve the credentials of the owner of this node (if any)
     *
     * @return The credentials of the owner of this node, or
     *  null   if no credentials were set.
     */
    public Credentials getOwnerCredentials() {
        return ownerCredentials;
    }

    /**
     * Set the credentials of the owner of this node
     */
    public void setOwnerCredentials(Credentials credentials) {
        ownerCredentials = credentials;
    }

}
