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

package jade.tools.sniffer;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

/**
 * A <em>Message</em> extends the meaning of an ACLMessage (thus extending
 * jade.lang.acl.ACLMessage) providing an ACLMessage with the graphic part: the
 * arrow going from the sender of the message to the receiver of the message to
 * be drawn on the Message Canvas
 *
 * @see ACLMessage
 * <p>
 * MLG added private messageNumber, get and set.
 */

public class Message extends ACLMessage implements Serializable {

    public static final int step = 80;
    public static final int offset = 45;
    public static final int r = 8;
    private final AID unicastReceiver;
    private final int[] xCoords = new int[3];
    private final int[] yCoords = new int[3];
    private final int yDim = 0;
    private final int xS = 0;
    private final int xD = 0;
    private int x1, x2, y;
    private int messageNumber = 0;

  /* TO BE REMOVED
  public Message(AID s, AID r){

    super(ACLMessage.INFORM);

    this.setSender(s);
    this.clearAllReceiver();
    this.addReceiver(r);
  }*/

    public Message(ACLMessage msg, AID r) {

        super(msg.getPerformative());

        unicastReceiver = r;

        this.clearAllReceiver();
        //#DOTNET_EXCLUDE_BEGIN
        for (Iterator<AID> i = msg.getAllReceiver(); i.hasNext(); )
            //#DOTNET_EXCLUDE_END
	/*#DOTNET_INCLUDE_BEGIN
    for (java.util.Iterator i=msg.getAllReceiver(); i.hasNext(); )
	#DOTNET_INCLUDE_END*/
            this.addReceiver(i.next());

        this.clearAllReplyTo();
        //#DOTNET_EXCLUDE_BEGIN
        for (Iterator<AID> i = msg.getAllReplyTo(); i.hasNext(); )
            //#DOTNET_EXCLUDE_END
	/*#DOTNET_INCLUDE_BEGIN
    for (java.util.Iterator i=msg.getAllReplyTo(); i.hasNext(); )
	#DOTNET_INCLUDE_END*/
            this.addReplyTo(i.next());

        this.setSender(msg.getSender());
        this.setContent(msg.getContent());
        this.setReplyWith(msg.getReplyWith());
        this.setInReplyTo(msg.getInReplyTo());
        this.setLanguage(msg.getLanguage());
        this.setOntology(msg.getOntology());
        this.setReplyByDate(msg.getReplyByDate());
        this.setProtocol(msg.getProtocol());
        this.setConversationId(msg.getConversationId());
        this.setEnvelope(msg.getEnvelope());

        Properties prop = msg.getAllUserDefinedParameters();
        for (Enumeration<?> e = prop.propertyNames(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            this.addUserDefinedParameter(key, prop.getProperty(key));
        }
    }

    public AID getUnicastReceiver() {
        return unicastReceiver;
    }

    public int getInitSeg(int xS) {
        x1 = xS * step + offset + 4;
        return x1;
    }

    public int getEndSeg(int xD) {
        x2 = xD * step + offset;
        return x2;
    }

    public int getOrdSeg(int yDim) {
        y = (yDim * 20) + step - 50;
        return y;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public void setMessageNumber(int n) {
        messageNumber = n;
    }

} 
