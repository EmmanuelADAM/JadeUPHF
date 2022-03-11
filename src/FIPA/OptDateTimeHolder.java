/*
 * File: ./FIPA/OPTDATETIMEHOLDER.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;

public final class OptDateTimeHolder
        implements org.omg.CORBA.portable.Streamable {
    //	instance variable 
    public DateTime[] value;

    //	constructors
    public OptDateTimeHolder() {
        this(null);
    }

    public OptDateTimeHolder(DateTime[] __arg) {
        value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        OptDateTimeHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = OptDateTimeHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return OptDateTimeHelper.type();
    }
}
