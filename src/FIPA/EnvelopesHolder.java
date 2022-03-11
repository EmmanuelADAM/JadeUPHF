/*
 * File: ./FIPA/ENVELOPESHOLDER.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;

public final class EnvelopesHolder
        implements org.omg.CORBA.portable.Streamable {
    //	instance variable 
    public Envelope[] value;

    //	constructors
    public EnvelopesHolder() {
        this(null);
    }

    public EnvelopesHolder(Envelope[] __arg) {
        value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        EnvelopesHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = EnvelopesHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return EnvelopesHelper.type();
    }
}
