/*
 * File: ./FIPA/OPTAGENTIDHELPER.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;

public class OptAgentIDHelper {
    private static org.omg.CORBA.TypeCode _tc;

    // It is useless to have instances of this class
    private OptAgentIDHelper() {
    }

    public static void write(org.omg.CORBA.portable.OutputStream out, AgentID[] that) {
        {
            if (that.length > (1L)) {
                throw new org.omg.CORBA.MARSHAL(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
            }
            out.write_long(that.length);
            for (AgentID agentID : that) {
                AgentIDHelper.write(out, agentID);
            }
        }
    }

    public static AgentID[] read(org.omg.CORBA.portable.InputStream in) {
        AgentID[] that;
        {
            int __length = in.read_long();
            if (__length > (1L)) {
                throw new org.omg.CORBA.MARSHAL(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
            }
            that = new AgentID[__length];
            for (int __index = 0; __index < that.length; __index += 1) {
                that[__index] = AgentIDHelper.read(in);
            }
        }
        return that;
    }

    public static AgentID[] extract(org.omg.CORBA.Any a) {
        org.omg.CORBA.portable.InputStream in = a.create_input_stream();
        return read(in);
    }

    public static void insert(org.omg.CORBA.Any a, AgentID[] that) {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
        a.type(type());
        write(out, that);
        a.read_value(out.create_input_stream(), type());
    }

    synchronized public static org.omg.CORBA.TypeCode type() {
        if (_tc == null)
            _tc = org.omg.CORBA.ORB.init().create_alias_tc(id(), "OptAgentID", org.omg.CORBA.ORB.init().create_sequence_tc((int) (1L), AgentIDHelper.type()));
        return _tc;
    }

    public static String id() {
        return "IDL:FIPA/OptAgentID:1.0";
    }
}
