/*
 * File: ./FIPA/OPTAGENTIDHOLDER.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;

public final class OptAgentIDHolder
        implements org.omg.CORBA.portable.Streamable {
    //	instance variable 
    public AgentID[] value;

    //	constructors
    public OptAgentIDHolder() {
        this(null);
    }

    public OptAgentIDHolder(AgentID[] __arg) {
        value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        OptAgentIDHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = OptAgentIDHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return OptAgentIDHelper.type();
    }
}
