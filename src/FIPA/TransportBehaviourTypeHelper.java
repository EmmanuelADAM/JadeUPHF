/*
 * File: ./FIPA/TRANSPORTBEHAVIOURTYPEHELPER.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;

public class TransportBehaviourTypeHelper {
    private static org.omg.CORBA.TypeCode _tc;

    // It is useless to have instances of this class
    private TransportBehaviourTypeHelper() {
    }

    public static void write(org.omg.CORBA.portable.OutputStream out, Property[] that) {
        {
            out.write_long(that.length);
            for (Property property : that) {
                PropertyHelper.write(out, property);
            }
        }
    }

    public static Property[] read(org.omg.CORBA.portable.InputStream in) {
        Property[] that;
        {
            int __length = in.read_long();
            that = new Property[__length];
            for (int __index = 0; __index < that.length; __index += 1) {
                that[__index] = PropertyHelper.read(in);
            }
        }
        return that;
    }

    public static Property[] extract(org.omg.CORBA.Any a) {
        org.omg.CORBA.portable.InputStream in = a.create_input_stream();
        return read(in);
    }

    public static void insert(org.omg.CORBA.Any a, Property[] that) {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
        a.type(type());
        write(out, that);
        a.read_value(out.create_input_stream(), type());
    }

    synchronized public static org.omg.CORBA.TypeCode type() {
        if (_tc == null)
            _tc = org.omg.CORBA.ORB.init().create_alias_tc(id(), "TransportBehaviourType", org.omg.CORBA.ORB.init().create_sequence_tc(0, PropertyHelper.type()));
        return _tc;
    }

    public static String id() {
        return "IDL:FIPA/TransportBehaviourType:1.0";
    }
}
