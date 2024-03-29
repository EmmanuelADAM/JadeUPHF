/*
 * File: ./FIPA/RECEIVEDOBJECTHELPER.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;

public class ReceivedObjectHelper {
    private static org.omg.CORBA.TypeCode _tc;

    // It is useless to have instances of this class
    private ReceivedObjectHelper() {
    }

    public static void write(org.omg.CORBA.portable.OutputStream out, ReceivedObject that) {
        out.write_string(that.by());
        out.write_string(that.from());
        DateTimeHelper.write(out, that.date());
        out.write_string(that.id());
        out.write_string(that.via());
    }

    public static ReceivedObject read(org.omg.CORBA.portable.InputStream in) {
        String by = in.read_string();
        String from = in.read_string();
        DateTime date = DateTimeHelper.read(in);
        String id = in.read_string();
        String via = in.read_string();
        return new ReceivedObject(by, from, date, id, via);
    }

    public static ReceivedObject extract(org.omg.CORBA.Any a) {
        org.omg.CORBA.portable.InputStream in = a.create_input_stream();
        return read(in);
    }

    public static void insert(org.omg.CORBA.Any a, ReceivedObject that) {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
        write(out, that);
        a.read_value(out.create_input_stream(), type());
    }

    synchronized public static org.omg.CORBA.TypeCode type() {
        int _memberCount = 5;
        org.omg.CORBA.StructMember[] _members = null;
        if (_tc == null) {
            _members = new org.omg.CORBA.StructMember[5];
            _members[0] = new org.omg.CORBA.StructMember(
                    "by",
                    org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string),
                    null);

            _members[1] = new org.omg.CORBA.StructMember(
                    "from",
                    org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string),
                    null);

            _members[2] = new org.omg.CORBA.StructMember(
                    "date",
                    DateTimeHelper.type(),
                    null);

            _members[3] = new org.omg.CORBA.StructMember(
                    "id",
                    org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string),
                    null);

            _members[4] = new org.omg.CORBA.StructMember(
                    "via",
                    org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string),
                    null);
            _tc = org.omg.CORBA.ORB.init().create_struct_tc(id(), "ReceivedObject", _members);
        }
        return _tc;
    }

    public static String id() {
        return "IDL:FIPA/ReceivedObject:1.0";
    }
}
