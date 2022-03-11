/*
 * File: ./FIPA/MTSHOLDER.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;

public final class MTSHolder
        implements org.omg.CORBA.portable.Streamable {
    //	instance variable 
    public MTS value;

    //	constructors
    public MTSHolder() {
        this(null);
    }

    public MTSHolder(MTS __arg) {
        value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        MTSHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = MTSHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return MTSHelper.type();
    }
}
