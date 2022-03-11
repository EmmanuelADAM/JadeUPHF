/*
 * File: ./FIPA/OPTDATETIMEHELPER.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;

public class OptDateTimeHelper {
    // It is useless to have instances of this class
    private OptDateTimeHelper() {
    }

    public static void write(org.omg.CORBA.portable.OutputStream out, DateTime[] that) {
        {
            if (that.length > (1L)) {
                throw new org.omg.CORBA.MARSHAL(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
            }
            out.write_long(that.length);
            for (DateTime dateTime : that) {
                DateTimeHelper.write(out, dateTime);
            }
        }
    }

    public static DateTime[] read(org.omg.CORBA.portable.InputStream in) {
        DateTime[] that;
        {
            int __length = in.read_long();
            if (__length > (1L)) {
                throw new org.omg.CORBA.MARSHAL(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
            }
            that = new DateTime[__length];
            for (int __index = 0; __index < that.length; __index += 1) {
                that[__index] = DateTimeHelper.read(in);
            }
        }
        return that;
    }

    public static DateTime[] extract(org.omg.CORBA.Any a) {
        org.omg.CORBA.portable.InputStream in = a.create_input_stream();
        return read(in);
    }

    public static void insert(org.omg.CORBA.Any a, DateTime[] that) {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
        a.type(type());
        write(out, that);
        a.read_value(out.create_input_stream(), type());
    }

    private static org.omg.CORBA.TypeCode _tc;

    synchronized public static org.omg.CORBA.TypeCode type() {
        if (_tc == null)
            _tc = org.omg.CORBA.ORB.init().create_alias_tc(id(), "OptDateTime", org.omg.CORBA.ORB.init().create_sequence_tc((int) (1L), DateTimeHelper.type()));
        return _tc;
    }

    public static String id() {
        return "IDL:FIPA/OptDateTime:1.0";
    }
}
