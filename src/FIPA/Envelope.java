/*
 * File: ./FIPA/ENVELOPE.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;

public final class Envelope {
    //	instance variables
    public AgentID[] to;
    public AgentID[] from;
    public String comments;
    public String aclRepresentation;
    public int payloadLength;
    public String payloadEncoding;
    public DateTime[] date;
    public String[] encrypted;
    public AgentID[] intendedReceiver;
    public ReceivedObject[] received;
    public Property[][] transportBehaviour;
    public Property[] userDefinedProperties;

    //	constructors
    public Envelope() {
    }

    public Envelope(AgentID[] __to, AgentID[] __from, String __comments, String __aclRepresentation, int __payloadLength, String __payloadEncoding, DateTime[] __date, String[] __encrypted, AgentID[] __intendedReceiver, ReceivedObject[] __received, Property[][] __transportBehaviour, Property[] __userDefinedProperties) {
        to = __to;
        from = __from;
        comments = __comments;
        aclRepresentation = __aclRepresentation;
        payloadLength = __payloadLength;
        payloadEncoding = __payloadEncoding;
        date = __date;
        encrypted = __encrypted;
        intendedReceiver = __intendedReceiver;
        received = __received;
        transportBehaviour = __transportBehaviour;
        userDefinedProperties = __userDefinedProperties;
    }
}
