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

package jade.lang.acl;

//#MIDP_EXCLUDE_BEGIN

import jade.core.AID;
import jade.domain.FIPAAgentManagement.Envelope;

import java.io.*;
import java.util.*;
//#CUSTOM_EXCLUDE_END


/**
 * The class ACLMessage implements an ACL message compliant to the <b>FIPA 2000</b> "FIPA ACL Message Structure Specification" (fipa000061) specifications.
 * All parameters are couples <em>keyword: value</em>.
 * All keywords are  private final String  .
 * All values can be set by using the methods <em>set</em> and can be read by using
 * the methods <em>get</em>.
 * <p> <b>Warning: </b> since JADE 3.1  an exception might be thrown
 * during the serialization of the ACLMessage parameters (with
 * exception of the content of the ACLMessage) because of a limitation
 * to 65535 in the total number of bytes needed to represent all the
 * characters of a String (see also java.io.DataOutput#writeUTF(String)).
 * <p> The methods   setByteSequenceContent()    and
 *   getByteSequenceContent()    allow to send arbitrary
 * sequence of bytes
 * over the content of an ACLMessage.
 * <p> The couple of methods
 *   setContentObject()    and
 *   getContentObject()    allow to send
 * serialized Java objects over the content of an ACLMessage.
 * These method are not strictly
 * FIPA compliant so their usage is not encouraged.
 *
 * @author Fabio Bellifemine - CSELT
 * @version $Date: 2016-07-21 13:44:10 +0200 (gio, 21 lug 2016) $ $Revision: 6804 $
 * @see <a href=http://www.fipa.org/specs/fipa00061/XC00061D.html>FIPA Spec</a>
 */
//#MIDP_EXCLUDE_BEGIN 
public class ACLMessage implements Cloneable, Serializable {
    /**
     * constant identifying the FIPA performative
     **/
    public static final int ACCEPT_PROPOSAL = 0;
    /**
     * constant identifying the FIPA performative
     **/
    public static final int AGREE = 1;
    /**
     * constant identifying the FIPA performative
     **/
    public static final int CANCEL = 2;
    /**
     * constant identifying the FIPA performative
     **/
    public static final int CFP = 3;
    /**
     * constant identifying the FIPA performative
     **/
    public static final int CONFIRM = 4;
    /**
     * constant identifying the FIPA performative
     **/
    public static final int DISCONFIRM = 5;
    /**
     * constant identifying the FIPA performative
     **/
    public static final int FAILURE = 6;
    /**
     * constant identifying the FIPA performative
     **/
    public static final int INFORM = 7;
    /**
     * constant identifying the FIPA performative
     **/
    public static final int INFORM_IF = 8;
    /**
     * constant identifying the FIPA performative
     **/
    public static final int INFORM_REF = 9;
    /**
     * constant identifying the FIPA performative
     **/
    public static final int NOT_UNDERSTOOD = 10;
    /**
     * constant identifying the FIPA performative
     **/
    public static final int PROPOSE = 11;
    /**
     * constant identifying the FIPA performative
     **/
    public static final int QUERY_IF = 12;
    /**
     * constant identifying the FIPA performative
     **/
    public static final int QUERY_REF = 13;
    /**
     * constant identifying the FIPA performative
     **/
    public static final int REFUSE = 14;
    /**
     * constant identifying the FIPA performative
     **/
    public static final int REJECT_PROPOSAL = 15;
    /**
     * constant identifying the FIPA performative
     **/
    public static final int REQUEST = 16;
    /**
     * constant identifying the FIPA performative
     **/
    public static final int REQUEST_WHEN = 17;
    /**
     * constant identifying the FIPA performative
     **/
    public static final int REQUEST_WHENEVER = 18;
    /**
     * constant identifying the FIPA performative
     **/
    public static final int SUBSCRIBE = 19;
    /**
     * constant identifying the FIPA performative
     **/
    public static final int PROXY = 20;
    /**
     * constant identifying the FIPA performative
     **/
    public static final int PROPAGATE = 21;
    /**
     * constant identifying an unknown performative
     **/
    public static final int UNKNOWN = -1;
    /**
     * User defined parameter key specifying, when set to "true", that if the delivery of a
     * message fails, no failure handling action must be performed.
     */
    public static final String IGNORE_FAILURE = "JADE-ignore-failure";
    /**
     * User defined parameter key specifying, when set to "true", that if the delivery of a
     * message fails, no FAILURE notification has to be sent back to the sender.
     * This differs from IGNORE_FAILURE since it does not inhibit the delivery failure
     * handling mechanism (based on the NOTIFY_FAILURE VCommand) at all, but just the
     * delivery of the automatic AMS FAILURE reply.
     */
    public static final String DONT_NOTIFY_FAILURE = "JADE-dont-notify-failure";
    /**
     * User defined parameter key specifying that the JADE tracing mechanism should be activated for this message.
     */
    public static final String TRACE = "JADE-trace";
    /**
     * User defined parameter key specifying that this message does not need to be cloned by the message delivery service.
     * This should be used ONLY when the message object will not be modified after being sent
     */
    public static final String NO_CLONE = "JADE-no-clone";
    /**
     * User defined parameter key specifying that this message must be delivered synchronously. It should
     * be noticed that when using synchronous delivery message order is not guaranteed.
     */
    public static final String SYNCH_DELIVERY = "JADE-synch-delivery";
    /**
     * User defined parameter key specifying the AID of the real sender of a message. This is automatically
     * set by the MessagingService when posting a message where the sender field is different than the real
     * sender.
     */
    public static final String REAL_SENDER = "JADE-real-sender";
    /**
     * User defined parameter key specifying that this message must be stored for a
     * given timeout (in ms) in case it is sent to/from a temporarily disconnected split
     * container. After that timeout a FAILURE message will be sent back to the sender.<br>
     * 0 means store and forward disabled
     * -1 means infinite timeout
     */
    public static final String SF_TIMEOUT = "JADE-SF-timeout";
    /**
     * AMS failure reasons
     */
    public static final String AMS_FAILURE_AGENT_NOT_FOUND = "Agent not found";
    public static final String AMS_FAILURE_AGENT_UNREACHABLE = "Agent unreachable";
    public static final String AMS_FAILURE_SERVICE_ERROR = "Service error";
    public static final String AMS_FAILURE_UNAUTHORIZED = "Not authorized";
    public static final String AMS_FAILURE_FOREIGN_AGENT_UNREACHABLE = "Foreign agent unreachable";
    public static final String AMS_FAILURE_FOREIGN_AGENT_NO_ADDRESS = "Foreign agent with no address";
    public static final String AMS_FAILURE_UNEXPECTED_ERROR = "Unexpected error";
    //#MIDP_EXCLUDE_END
/*#MIDP_INCLUDE_BEGIN
public class ACLMessage implements Serializable {
#MIDP_INCLUDE_END*/
    // Explicitly set for compatibility between standard and micro version
    @Serial
    private static final long serialVersionUID = 3945353187608998130L;
    /**
     * This array of Strings keeps the names of the performatives
     **/
    private static final String[] performatives = new String[22];
    /**
     * These constants represent the expected size of the 2 array lists
     * used by this class
     **/
    private static final int RECEIVERS_EXPECTED_SIZE = 1;
    private static final int REPLYTO_EXPECTED_SIZE = 1;

    static { // initialization of the Vector of performatives
        performatives[ACCEPT_PROPOSAL] = "ACCEPT-PROPOSAL";
        performatives[AGREE] = "AGREE";
        performatives[CANCEL] = "CANCEL";
        performatives[CFP] = "CFP";
        performatives[CONFIRM] = "CONFIRM";
        performatives[DISCONFIRM] = "DISCONFIRM";
        performatives[FAILURE] = "FAILURE";
        performatives[INFORM] = "INFORM";
        performatives[INFORM_IF] = "INFORM-IF";
        performatives[INFORM_REF] = "INFORM-REF";
        performatives[NOT_UNDERSTOOD] = "NOT-UNDERSTOOD";
        performatives[PROPOSE] = "PROPOSE";
        performatives[QUERY_IF] = "QUERY-IF";
        performatives[QUERY_REF] = "QUERY-REF";
        performatives[REFUSE] = "REFUSE";
        performatives[REJECT_PROPOSAL] = "REJECT-PROPOSAL";
        performatives[REQUEST] = "REQUEST";
        performatives[REQUEST_WHEN] = "REQUEST-WHEN";
        performatives[REQUEST_WHENEVER] = "REQUEST-WHENEVER";
        performatives[SUBSCRIBE] = "SUBSCRIBE";
        performatives[PROXY] = "PROXY";
        performatives[PROPAGATE] = "PROPAGATE";
    }

    /**
     * @serial
     */
    private int performative; // keeps the performative type of this object
    /**
     * @serial
     */
    private AID source = null;
    //#MIDP_EXCLUDE_BEGIN
    private ArrayList<AID> dests = new ArrayList<>(RECEIVERS_EXPECTED_SIZE);
    private ArrayList<AID> reply_to = null;
    //#MIDP_EXCLUDE_END
	/*#MIDP_INCLUDE_BEGIN
	 private Vector dests = new Vector(RECEIVERS_EXPECTED_SIZE);
	 private Vector reply_to = null; 
	 #MIDP_INCLUDE_END*/


    /**
     * @serial
     */
    // At a given time or content or byteSequenceContent are != null,
    // it is not allowed that both are != null
    private StringBuffer content = null;
    private byte[] byteSequenceContent = null;

    /**
     * @serial
     */
    private String reply_with = null;

    /**
     * @serial
     */
    private String in_reply_to = null;

    /**
     * @serial
     */
    private String encoding = null;

    /**
     * @serial
     */
    private String language = null;

    /**
     * @serial
     */
    private String ontology = null;

    /**
     * @serial
     */
    private long reply_byInMillisec = 0;

    /**
     * @serial
     */
    private String protocol = null;

    /**
     * @serial
     */
    private String conversation_id = null;

    private Properties userDefProps = null;

    private long postTimeStamp = -1;

    //#CUSTOM_EXCLUDE_BEGIN
    private Envelope messageEnvelope;
    //#CUSTOM_EXCLUDE_END
    // For persistence service
    private Long persistentID;

    /**
     * @see ACLMessage#ACLMessage(int)
     * @deprecated Since every ACL Message must have a message type, you
     * should use the new constructor which gets a message type as a
     * parameter.  To avoid problems, now this constructor silently sets
     * the message type to  not-understood  .
     */
    public ACLMessage() { // Used by persistence service: do not remove it, but make it private
        performative = NOT_UNDERSTOOD;
    }


    /**
     * This constructor creates an ACL message object with the specified
     * performative. If the passed integer does not correspond to any of
     * the known performatives, it silently initializes the message to
     *  not-understood  .
     **/
    public ACLMessage(int perf) {
        performative = perf;
    }

    /**
     * Returns the list of the communicative acts as an array of  String  .
     */
    public static String[] getAllPerformativeNames() {
        return performatives;
    }

    /**
     * Returns the string corresponding to the integer for the performative
     *
     * @return the string corresponding to the integer for the performative;
     * "NOT-UNDERSTOOD" if the integer is out of range.
     */
    public static String getPerformative(int perf) {
        try {
            return performatives[perf];
        } catch (Exception e) {
            return performatives[NOT_UNDERSTOOD];
        }
    }

    /**
     * Returns the integer corresponding to the performative
     *
     * @return the integer corresponding to the performative; -1 otherwise
     */
    public static int getInteger(String perf) {
        String tmp = perf.toUpperCase();
        for (int i = 0; i < performatives.length; i++)
            if (performatives[i].equals(tmp))
                return i;
        return -1;
    }

    /**
     * Adds a value to  :receiver   slot. <em><b>Warning:</b>
     * no checks are made to validate the slot value.</em>
     *
     * @param r The value to add to the slot value set.
     */
    public void addReceiver(AID r) {
        if (r != null) {
            //#MIDP_EXCLUDE_BEGIN
            dests.add(r);
            //#MIDP_EXCLUDE_END
			/*#MIDP_INCLUDE_BEGIN
			 dests.addElement(r);
			 #MIDP_INCLUDE_END*/
        }
    }

    /**
     * add a receiver
     *
     * @param localName the local name of the receiver
     */
    public void addReceiver(String localName) {
        if (localName != null) {
            dests.add(new AID(localName, false));
        }
    }

    /**
     * add some receivers
     *
     * @param localNames the local names of the receivers
     */
    public void addReceivers(String... localNames) {
        if (localNames != null) {
            for (String localName : localNames)
                dests.add(new AID(localName, false));
        }
    }

    /**
     * add some receivers
     *
     * @param aids the aid of the receivers
     */
    public void addReceivers(AID... aids) {
        if (aids != null) {
            Collections.addAll(dests, aids);
        }
    }

    /**
     * Removes a value from  :receiver
     * slot. <em><b>Warning:</b> no checks are made to validate the slot
     * value.</em>
     *
     * @param r The value to remove from the slot value set.
     * @return true if the AID has been found and removed, false otherwise
     */
    public boolean removeReceiver(AID r) {
        if (r != null) {
            //#MIDP_EXCLUDE_BEGIN
            return dests.remove(r);
            //#MIDP_EXCLUDE_END
			/*#MIDP_INCLUDE_BEGIN
			 return dests.removeElement(r);
			 #MIDP_INCLUDE_END*/
        } else {
            return false;
        }
    }

    /**
     * Removes all values from  :receiver
     * slot. <em><b>Warning:</b> no checks are made to validate the slot
     * value.</em>
     */
    public void clearAllReceiver() {
        //#MIDP_EXCLUDE_BEGIN
        dests.clear();
        //#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 dests.removeAllElements();
		 #MIDP_INCLUDE_END*/
    }

    /**
     * Adds a value to  :reply-to   slot. <em><b>Warning:</b>
     * no checks are made to validate the slot value.</em>
     *
     * @param dest The value to add to the slot value set.
     */
    public void addReplyTo(AID dest) {
        if (dest != null) {
            //#MIDP_EXCLUDE_BEGIN
            reply_to = (reply_to == null ? new ArrayList<>(REPLYTO_EXPECTED_SIZE) : reply_to);
            reply_to.add(dest);
            //#MIDP_EXCLUDE_END
			/*#MIDP_INCLUDE_BEGIN
			 reply_to = (reply_to == null ? new Vector(REPLYTO_EXPECTED_SIZE) : reply_to);
			 reply_to.addElement(dest);
			 #MIDP_INCLUDE_END*/
        }
    }

    /**
     * Removes a value from  :reply_to
     * slot. <em><b>Warning:</b> no checks are made to validate the slot
     * value.</em>
     *
     * @param dest The value to remove from the slot value set.
     * @return true if the AID has been found and removed, false otherwise
     */
    public boolean removeReplyTo(AID dest) {
        if ((dest != null) && (reply_to != null)) {
            //#MIDP_EXCLUDE_BEGIN
            return reply_to.remove(dest);
            //#MIDP_EXCLUDE_END
			/*#MIDP_INCLUDE_BEGIN
			 return reply_to.removeElement(dest);
			 #MIDP_INCLUDE_END*/
        } else {
            return false;
        }
    }

    /**
     * Removes all values from  :reply_to
     * slot. <em><b>Warning:</b> no checks are made to validate the slot
     * value.</em>
     */
    public void clearAllReplyTo() {
        if (reply_to != null) {
            //#MIDP_EXCLUDE_BEGIN
            reply_to.clear();
            //#MIDP_EXCLUDE_END
			/*#MIDP_INCLUDE_BEGIN
			 reply_to.removeAllElements();
			 #MIDP_INCLUDE_END*/
        }
    }

    /**
     * This method returns the content of this ACLMessage when they have
     * been written via the method  setContentObject  .
     * It is not FIPA compliant so its usage is not encouraged.
     * For example to read Java objects from the content
     * <PRE>
     * ACLMessage msg = blockingReceive();
     * try{
     * Date d = (Date)msg.getContentObject();
     * }catch(UnreadableException e){}
     * </PRE>
     *
     * @return the object read from the content of this ACLMessage
     * exception UnreadableException when an error occurs during the decoding.
     */
    public Serializable getContentObject() throws UnreadableException {

        try {
            byte[] data = getByteSequenceContent();
            if (data == null)
                return null;
            ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(data));
            return (Serializable) oin.readObject();
        } catch (Error | IOException | ClassNotFoundException e) {
            throw new UnreadableException(e.getMessage());
        }

    }


    //#MIDP_EXCLUDE_BEGIN

    /**
     * This method sets the content of this ACLMessage to a Java object.
     * It is not FIPA compliant so its usage is not encouraged.
     * For example:<br>
     * <PRE>
     * ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
     * Date d = new Date();
     * try{
     * msg.setContentObject(d);
     * }catch(IOException e){}
     * </PRE>
     *
     * @param s the object that will be used to set the content of the ACLMessage.
     * @throws IOException if an I/O error occurs.
     */
    public void setContentObject(Serializable s) throws IOException {
        ByteArrayOutputStream c = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(c);
        oos.writeObject(s);
        oos.flush();
        setByteSequenceContent(c.toByteArray());
    }

    /**
     * Reads  :receiver   slot.
     *
     * @return An  Iterator   containing the Agent IDs of the
     * receiver agents for this message.
     */
    public Iterator<AID> getAllReceiver() {
        //#MIDP_EXCLUDE_BEGIN
        return dests.iterator();
        //#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 return new EnumIterator(dests.elements());
		 #MIDP_INCLUDE_END*/
    }
    //#MIDP_EXCLUDE_END

    /**
     * Reads  :reply_to   slot.
     *
     * @return An  Iterator   containing the Agent IDs of the
     * reply_to agents for this message.
     */
    public Iterator<AID> getAllReplyTo() {
        if (reply_to == null) {
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public AID next() {
                    throw new NoSuchElementException();
                }
            };
        } else {
            //#MIDP_EXCLUDE_BEGIN
            return reply_to.iterator();
            //#MIDP_EXCLUDE_END
			/*#MIDP_INCLUDE_BEGIN
			 return new EnumIterator(reply_to.elements());
			 #MIDP_INCLUDE_END*/
        }
    }

    /**
     * Reads  :sender   slot.
     *
     * @return The value of  :sender  slot.
     * @see ACLMessage#setSender(AID).
     */
    public AID getSender() {
        return source;
    }

    /**
     * Writes the  :sender   slot. <em><b>Warning:</b> no
     * checks are made to validate the slot value.</em>
     *
     * @param s The new value for the slot.
     * @see ACLMessage#getSender()
     */
    public void setSender(AID s) {
        source = s;
    }

    /**
     * return the integer representing the performative of this object
     *
     * @return an integer representing the performative of this object
     */
    public int getPerformative() {
        return performative;
    }

    /**
     * set the performative of this ACL message object to the passed constant.
     * Remind to
     * use the set of constants (i.e.   INFORM, REQUEST, ...   )
     * defined in this class
     */
    public void setPerformative(int perf) {
        performative = perf;
    }

    /**
     * This method allows to check if the content of this ACLMessage
     * is a byteSequence or a String
     *
     * @return true if it is a byteSequence, false if it is a String
     */
    public boolean hasByteSequenceContent() {
        return (byteSequenceContent != null);
    }

    /**
     * Reads  :content   slot. <p>
     * <p>Notice that, in general, setting a String content and getting
     * back a byte sequence content - or viceversa - does not return
     * the same value, i.e. the following relation does not hold
     *
     * getByteSequenceContent(setByteSequenceContent(getContent().getBytes()))
     * is equal to getByteSequenceContent()
     *
     *
     * @return The value of  :content   slot.
     * @see ACLMessage#setContent(String)
     * @see ACLMessage#getByteSequenceContent()
     * @see ACLMessage#getContentObject()
     */
    public String getContent() {
        if (content != null)
            return new String(content);
        else if (byteSequenceContent != null)
            return new String(byteSequenceContent);
        return null;
    }

    /**
     * Writes the  :content   slot. <em><b>Warning:</b> no
     * checks are made to validate the slot value.</em> <p>
     * <p>Notice that, in general, setting a String content and getting
     * back a byte sequence content - or viceversa - does not return
     * the same value, i.e. the following relation does not hold
     *
     * getByteSequenceContent(setByteSequenceContent(getContent().getBytes()))
     * is equal to getByteSequenceContent()
     *
     *
     * @param content The new value for the slot.
     * @see ACLMessage#getContent()
     * @see ACLMessage#setByteSequenceContent(byte[])
     */
    public void setContent(String content) {
        byteSequenceContent = null;
        if (content != null) {
            this.content = new StringBuffer(content);
        } else {
            this.content = null;
        }
    }

    /**
     * Reads  :content   slot. <p>
     * <p>Notice that, in general, setting a String content and getting
     * back a byte sequence content - or viceversa - does not return
     * the same value, i.e. the following relation does not hold
     *
     * getByteSequenceContent(setByteSequenceContent(getContent().getBytes()))
     * is equal to getByteSequenceContent()
     *
     *
     * @return The value of  :content   slot.
     * @see ACLMessage#getContent()
     * @see ACLMessage#setByteSequenceContent(byte[])
     * @see ACLMessage#getContentObject()
     */
    public byte[] getByteSequenceContent() {
        if (content != null)
            return content.toString().getBytes();
        else if (byteSequenceContent != null)
            return byteSequenceContent;
        return null;
    }

    /**
     * Writes the  :content   slot. <em><b>Warning:</b> no
     * checks are made to validate the slot value.</em> <p>
     * <p>Notice that, in general, setting a String content and getting
     * back a byte sequence content - or viceversa - does not return
     * the same value, i.e. the following relation does not hold
     *
     * getByteSequenceContent(setByteSequenceContent(getContent().getBytes()))
     * is equal to getByteSequenceContent()
     *
     *
     * @param byteSequenceContent The new value for the slot.
     * @see ACLMessage#setContent(String s)
     * @see ACLMessage#getByteSequenceContent()
     */
    public void setByteSequenceContent(byte[] byteSequenceContent) {
        content = null;
        this.byteSequenceContent = byteSequenceContent;
    }

    /**
     * Reads  :reply-with   slot.
     *
     * @return The value of  :reply-with  slot.
     * @see ACLMessage#setReplyWith(String).
     */
    public String getReplyWith() {
        return reply_with;
    }

    /**
     * Writes the  :reply-with   slot. <em><b>Warning:</b> no
     * checks are made to validate the slot value.</em>
     *
     * @param reply The new value for the slot.
     * @see ACLMessage#getReplyWith()
     */
    public void setReplyWith(String reply) {
        reply_with = reply;
    }

    /**
     * Reads  :reply-to   slot.
     *
     * @return The value of  :reply-to  slot.
     * @see ACLMessage#setInReplyTo(String).
     */
    public String getInReplyTo() {
        return in_reply_to;
    }

    /**
     * Writes the  :in-reply-to   slot. <em><b>Warning:</b> no
     * checks are made to validate the slot value.</em>
     *
     * @param reply The new value for the slot.
     * @see ACLMessage#getInReplyTo()
     */
    public void setInReplyTo(String reply) {
        in_reply_to = reply;
    }

    /**
     * Reads  :encoding   slot.
     *
     * @return The value of  :encoding  slot.
     * @see ACLMessage#setEncoding(String).
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Writes the  :encoding   slot. <em><b>Warning:</b> no
     * checks are made to validate the slot value.</em>
     *
     * @param str The new value for the slot.
     * @see ACLMessage#getEncoding()
     */
    public void setEncoding(String str) {
        encoding = str;
    }

    /**
     * Reads  :language   slot.
     *
     * @return The value of  :language  slot.
     * @see ACLMessage#setLanguage(String).
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Writes the  :language   slot. <em><b>Warning:</b> no
     * checks are made to validate the slot value.</em>
     *
     * @param str The new value for the slot.
     * @see ACLMessage#getLanguage()
     */
    public void setLanguage(String str) {
        language = str;
    }

    /**
     * Reads  :ontology   slot.
     *
     * @return The value of  :ontology  slot.
     * @see ACLMessage#setOntology(String).
     */
    public String getOntology() {
        return ontology;
    }

    /**
     * Writes the  :ontology   slot. <em><b>Warning:</b> no
     * checks are made to validate the slot value.</em>
     *
     * @param str The new value for the slot.
     * @see ACLMessage#getOntology()
     */
    public void setOntology(String str) {
        ontology = str;
    }

    /**
     * Reads  :reply-by   slot.
     *
     * @return The value of  :reply-by  slot, as a string.
     * @see ACLMessage#getReplyByDate().
     * @deprecated Since the value of this slot is a Date by definition, then
     * the  getReplyByDate   should be used that returns a Date
     */
    public String getReplyBy() {
        if (reply_byInMillisec != 0)
            return ISO8601.toString(new Date(reply_byInMillisec));
        else
            return null;
    }

    /**
     * Reads  :reply-by   slot.
     *
     * @return The value of  :reply-by  slot, as a
     *  Date   object.
     * @see ACLMessage#setReplyByDate(Date).
     */
    public Date getReplyByDate() {
        if (reply_byInMillisec != 0)
            return new Date(reply_byInMillisec);
        else
            return null;
    }

    //#MIDP_EXCLUDE_BEGIN

    /**
     * Writes the  :reply-by   slot. <em><b>Warning:</b> no
     * checks are made to validate the slot value.</em>
     *
     * @param date The new value for the slot.
     * @see ACLMessage#getReplyByDate()
     */
    public void setReplyByDate(Date date) {
        reply_byInMillisec = (date == null ? 0 : date.getTime());
    }
    //#MIDP_EXCLUDE_END

    /**
     * Reads  :protocol   slot.
     *
     * @return The value of  :protocol  slot.
     * @see ACLMessage#setProtocol(String).
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Writes the  :protocol   slot. <em><b>Warning:</b> no
     * checks are made to validate the slot value.</em>
     *
     * @param str The new value for the slot.
     * @see ACLMessage#getProtocol()
     */
    public void setProtocol(String str) {
        protocol = str;
    }

    /**
     * Reads  :conversation-id   slot.
     *
     * @return The value of  :conversation-id  slot.
     * @see ACLMessage#setConversationId(String).
     */
    public String getConversationId() {
        return conversation_id;
    }

    /**
     * Writes the  :conversation-id   slot. <em><b>Warning:</b> no
     * checks are made to validate the slot value.</em>
     *
     * @param str The new value for the slot.
     * @see ACLMessage#getConversationId()
     */
    public void setConversationId(String str) {
        conversation_id = str;
    }

    /**
     * Add a new user defined parameter to this ACLMessage.
     * Notice that according to the FIPA specifications, the keyword of a
     * user-defined parameter must not contain space inside.
     * Note that the user does not need to (and shall not) add the prefix "X-" to the keyword.
     * This is automatically added by the StringACLCodec.
     *
     * @param key   the property key.
     * @param value the property value
     */
    public void addUserDefinedParameter(String key, String value) {
        userDefProps = (userDefProps == null ? new Properties() : userDefProps);
        userDefProps.setProperty(key, value);
    }

    /**
     * Searches for the user defined parameter with the specified key.
     * The method returns
     *  null   if the parameter is not found.
     *
     * @param key the parameter key.
     * @return the value in this ACLMessage with the specified key value.
     */
    public String getUserDefinedParameter(String key) {
        if (userDefProps == null)
            return null;
        else
            return userDefProps.getProperty(key);
    }

    /**
     * Return all user defined parameters of this ACLMessage in form of a Properties object
     **/
    public Properties getAllUserDefinedParameters() {
        userDefProps = (userDefProps == null ? new Properties() : userDefProps);
        return userDefProps;
    }

    /**
     * Replace all user defined parameters of this ACLMessage with the specified Properties object.
     **/
    public void setAllUserDefinedParameters(Properties userDefProps) {
        this.userDefProps = userDefProps;
    }

    /**
     * Removes the key and its corresponding value from the list of user
     * defined parameters in this ACLMessage.
     *
     * @param key the key that needs to be removed
     * @return true if the property has been found and removed, false otherwise
     */
    public boolean removeUserDefinedParameter(String key) {
        return (clearUserDefinedParameter(key) != null);
    }

    /**
     * Removes the key and its corresponding value from the list of user
     * defined parameters in this ACLMessage.
     *
     * @param key the key that needs to be removed
     * @return the value to which the key had been mapped or null if the key was not present
     */
    public Object clearUserDefinedParameter(String key) {
        if (userDefProps == null)
            return null;
        else
            return userDefProps.remove(key);
    }

    public long getPostTimeStamp() {
        return postTimeStamp;
    }

//	#CUSTOM_EXCLUDE_BEGIN

    public void setPostTimeStamp(long time) {
        postTimeStamp = time;
    }

    /**
     * Writes the message envelope for this message, using the
     *  :sender   and  :receiver   message slots to
     * fill in the envelope.
     */
    public void setDefaultEnvelope() {
        messageEnvelope = new Envelope();
        messageEnvelope.setFrom(source);
        //#MIDP_EXCLUDE_BEGIN
        //#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 Iterator it = new EnumIterator(dests.elements());
		 #MIDP_INCLUDE_END*/
        for (AID dest : dests) messageEnvelope.addTo(dest);
        //#MIDP_EXCLUDE_BEGIN
        messageEnvelope.setAclRepresentation(StringACLCodec.NAME);
        //#MIDP_EXCLUDE_END
        messageEnvelope.setDate(new Date());
    }

    /**
     * Reads the envelope attached to this message, if any.
     *
     * @return The envelope for this message.
     */
    public Envelope getEnvelope() {
        return messageEnvelope;
    }
//	#CUSTOM_EXCLUDE_END

    //#MIDP_EXCLUDE_BEGIN

    /**
     * Attaches an envelope to this message. The envelope is used by the
     * <b><it>ACC</it></b> for inter-platform messaging.
     *
     * @param e The  Envelope   object to attach to this
     *          message.
     */
    public void setEnvelope(Envelope e) {
        messageEnvelope = e;
    }
    //#MIDP_EXCLUDE_END

    /**
     * Convert an ACL message to its string representation. This method
     * writes a representation of this  ACLMessage   into a
     * character string.
     * If the content is a bytesequence, then it is automatically converted
     * into Base64 encoding.
     *
     * @return A  String   representing this message.
     */
    public String toString() {
        return StringACLCodec.toString(this);
    }
    //#MIDP_EXCLUDE_END
	/*#MIDP_INCLUDE_BEGIN
	 public synchronized Object clone() {
	 ACLMessage result = new ACLMessage(NOT_UNDERSTOOD);
	 result.performative = performative;
	 result.source = source;
	 result.content = content;
	 result.byteSequenceContent = byteSequenceContent;
	 result.reply_with = reply_with;
	 result.in_reply_to = in_reply_to;
	 result.encoding = encoding;
	 result.language = language;
	 result.ontology = ontology;
	 result.reply_byInMillisec = reply_byInMillisec;
	 result.protocol = protocol;
	 result.conversation_id = conversation_id;
	 result.userDefProps = userDefProps;
	 //#CUSTOM_EXCLUDE_BEGIN
	  if(messageEnvelope != null) {
	  result.messageEnvelope = (Envelope)messageEnvelope.clone(); 
	  }
	  //#CUSTOM_EXCLUDE_END
	   result.dests = new Vector(dests.size());
	   for (int i=0; i<dests.size(); i++)
	   result.dests.addElement(dests.elementAt(i));
	   if (reply_to != null) {
	   result.reply_to = new Vector(reply_to.size());
	   for (int i=0; i<reply_to.size(); i++)
	   result.reply_to.addElement(reply_to.elementAt(i));
	   }
	   return result;
	   } 
	   #MIDP_INCLUDE_END*/

    /**
     * Clone an  ACLMessage   object.
     *
     * @return A copy of this  ACLMessage   object. The copy
     * must be casted back to  ACLMessage   type before being
     * used.
     */
    //#MIDP_EXCLUDE_BEGIN
    public synchronized Object clone() {

        ACLMessage result;

        try {
            result = (ACLMessage) super.clone();
            result.persistentID = null;
            if (source != null) {
                result.source = (AID) source.clone();
            }

            // Deep clone receivers
            if (dests != null) {
                result.dests = new ArrayList<>(dests.size());
                for (AID id : dests) {
                    result.dests.add((AID) id.clone());
                }
            }

            // Deep clone reply_to
            if (reply_to != null) {
                result.reply_to = new ArrayList<>(reply_to.size());
                for (AID id : reply_to) {
                    result.reply_to.add((AID) id.clone());
                }
            }

            // Deep clone user-def-properties if present
            if (userDefProps != null)
                result.userDefProps = (Properties) userDefProps.clone();
            // Deep clone envelope if present
            if (messageEnvelope != null)
                result.messageEnvelope = (Envelope) messageEnvelope.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new InternalError(); // This should never happen
        }

        return result;
    }

    /**
     * Normal clone() method actually perform a deep-clone of the ACLMessage object.
     * This method instead clones the ACLMessage object itself but not the objects pointed to by the ACLMessage fields.
     *
     * @return A new ACLMessage whose fields points to the same object as the original
     * ACLMessage object
     */
    public ACLMessage shallowClone() {
        ACLMessage result = new ACLMessage(performative);
        result.source = source;
        result.dests = dests;
        result.reply_to = reply_to;

        result.content = content;
        result.byteSequenceContent = byteSequenceContent;

        result.encoding = encoding;
        result.language = language;
        result.ontology = ontology;

        result.reply_byInMillisec = reply_byInMillisec;
        result.reply_with = reply_with;
        result.in_reply_to = in_reply_to;
        result.protocol = protocol;
        result.conversation_id = conversation_id;

        result.userDefProps = userDefProps;

        result.messageEnvelope = messageEnvelope;

        return result;
    }

    /**
     * Resets all the message slots.
     */
    public void reset() {
        source = null;
        //#MIDP_EXCLUDE_BEGIN
        dests.clear();
        if (reply_to != null)
            reply_to.clear();
        //#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 dests.removeAllElements();
		 if (reply_to != null)
		 reply_to.removeAllElements();
		 #MIDP_INCLUDE_END*/
        performative = NOT_UNDERSTOOD;
        content = null;
        byteSequenceContent = null;
        reply_with = null;
        in_reply_to = null;
        encoding = null;
        language = null;
        ontology = null;
        reply_byInMillisec = 0;
        protocol = null;
        conversation_id = null;
        if (userDefProps != null) {
            userDefProps.clear();
        }

        postTimeStamp = -1;
    }

    /**
     * create a new ACLMessage that is a reply to this message.
     * In particular, it sets the following parameters of the new message:
     * receiver, language, ontology, protocol, conversation-id,
     * in-reply-to, reply-with.
     * The programmer needs to set the communicative-act and the content.
     * Of course, if he wishes to do that, he can reset any of the fields.
     *
     * @return the ACLMessage to send as a reply
     */
    public ACLMessage createReply() {
        ACLMessage m = new ACLMessage(getPerformative());
        Iterator<AID> it = getAllReplyTo();
        while (it.hasNext())
            m.addReceiver(it.next());
        if ((reply_to == null) || reply_to.isEmpty())
            m.addReceiver(getSender());
        m.setLanguage(getLanguage());
        m.setOntology(getOntology());
        m.setProtocol(getProtocol());
        m.setInReplyTo(getReplyWith());
        if (source != null)
            m.setReplyWith(source.getName() + System.currentTimeMillis());
        else
            m.setReplyWith("X" + System.currentTimeMillis());
        m.setConversationId(getConversationId());
        // Copy only well defined user-def-params
        String trace = getUserDefinedParameter(TRACE);
        if (trace != null) {
            m.addUserDefinedParameter(TRACE, trace);
        }
        //#CUSTOM_EXCLUDE_BEGIN
        //Set the Aclrepresentation of the reply message to the aclrepresentation of the sent message
        if (messageEnvelope != null) {
            m.setDefaultEnvelope();
            String aclCodec = messageEnvelope.getAclRepresentation();
            if (aclCodec != null)
                m.getEnvelope().setAclRepresentation(aclCodec);
        } else
            m.setEnvelope(null);
        //#CUSTOM_EXCLUDE_END
        return m;
    }

    //#MIDP_EXCLUDE_BEGIN

    /**
     * retrieve the whole list of intended receivers for this message.
     *
     * @return An Iterator over all the intended receivers of this
     * message taking into account the Envelope ":intended-receiver"
     * first, the Envelope ":to" second and the message ":receiver"
     * last.
     */
    public Iterator<AID> getAllIntendedReceiver() {
        Iterator<AID> it = null;
        //#CUSTOM_EXCLUDE_BEGIN
        Envelope env = getEnvelope();
        if (env != null) {
            it = env.getAllIntendedReceiver();
            if (!it.hasNext()) {
                // The ":intended-receiver" field is empty --> try with the ":to" field
                it = env.getAllTo();
            }
        }
        //#CUSTOM_EXCLUDE_END
        if (it == null || !it.hasNext()) {
            // Both the ":intended-receiver" and the ":to" fields are empty -->
            // Use the ACLMessage receivers
            it = getAllReceiver();
        }
        return it;
    }

    // For persistence service
    private Long getPersistentID() {
        return persistentID;
    }

    // For persistence service
    private void setPersistentID(Long l) {
        persistentID = l;
    }

    // For persistence service
    private ArrayList<AID> getReceivers() {
        return dests;
    }

    // For persistence service
    private void setReceivers(ArrayList<AID> al) {
        dests = al;
    }

    // For persistence service
    private ArrayList<AID> getReplyTo() {
        return reply_to;
    }

    // For persistence service
    private void setReplyTo(ArrayList<AID> al) {
        reply_to = al;
    }

    // For persistence service
    private Serializable getUserDefinedProperties() {
        return userDefProps;
    }

    // For persistence service
    private void setUserDefinedProperties(Serializable p) {
        userDefProps = (Properties) p;
    }


    //#MIDP_EXCLUDE_END

}
