/*
 * File: ./FIPA/DATETIMEHELPER.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;

public class DateTimeHelper {
    private static org.omg.CORBA.TypeCode _tc;

    // It is useless to have instances of this class
    private DateTimeHelper() {
    }

    public static void write(org.omg.CORBA.portable.OutputStream out, DateTime that) {
        out.write_short(that.year());
        out.write_short(that.month());
        out.write_short(that.day());
        out.write_short(that.hour());
        out.write_short(that.minutes());
        out.write_short(that.seconds());
        out.write_short(that.milliseconds());
        out.write_char(that.typeDesignator());
    }

    public static DateTime read(org.omg.CORBA.portable.InputStream in) {
        short year = in.read_short();
        short month = in.read_short();
        short day = in.read_short();
        short hour = in.read_short();
        short minutes = in.read_short();
        short seconds = in.read_short();
        short milliseconds = in.read_short();
        char typeDesignator = in.read_char();
        return new DateTime(year, month, day, hour, minutes, seconds, milliseconds,  typeDesignator);
    }

    public static DateTime extract(org.omg.CORBA.Any a) {
        org.omg.CORBA.portable.InputStream in = a.create_input_stream();
        return read(in);
    }

    public static void insert(org.omg.CORBA.Any a, DateTime that) {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
        write(out, that);
        a.read_value(out.create_input_stream(), type());
    }

    synchronized public static org.omg.CORBA.TypeCode type() {
        int _memberCount = 8;
        org.omg.CORBA.StructMember[] _members = null;
        if (_tc == null) {
            _members = new org.omg.CORBA.StructMember[8];
            _members[0] = new org.omg.CORBA.StructMember(
                    "year",
                    org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_short),
                    null);

            _members[1] = new org.omg.CORBA.StructMember(
                    "month",
                    org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_short),
                    null);

            _members[2] = new org.omg.CORBA.StructMember(
                    "day",
                    org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_short),
                    null);

            _members[3] = new org.omg.CORBA.StructMember(
                    "hour",
                    org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_short),
                    null);

            _members[4] = new org.omg.CORBA.StructMember(
                    "minutes",
                    org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_short),
                    null);

            _members[5] = new org.omg.CORBA.StructMember(
                    "seconds",
                    org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_short),
                    null);

            _members[6] = new org.omg.CORBA.StructMember(
                    "milliseconds",
                    org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_short),
                    null);

            _members[7] = new org.omg.CORBA.StructMember(
                    "typeDesignator",
                    org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_char),
                    null);
            _tc = org.omg.CORBA.ORB.init().create_struct_tc(id(), "DateTime", _members);
        }
        return _tc;
    }

    public static String id() {
        return "IDL:FIPA/DateTime:1.0";
    }
}
