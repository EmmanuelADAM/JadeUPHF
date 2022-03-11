/*
 * File: ./FIPA/_MTSIMPLBASE.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;

import org.omg.CORBA.*;

import java.util.Hashtable;

public abstract class _MTSImplBase extends org.omg.CORBA.DynamicImplementation implements MTS {
    // Constructor
    public _MTSImplBase() {
        super();
    }

    // Type strings for this class and its superclases
    private static final String[] _type_ids = {
            "IDL:FIPA/MTS:1.0"
    };

    @Deprecated
    public String[] _ids() {
        return _type_ids.clone();
    }

    private static final java.util.Dictionary<String, Integer> _methods = new Hashtable<>();

    static {
        _methods.put("message", 0);
    }

    // DSI Dispatch call
    @Deprecated
    public void invoke(org.omg.CORBA.ServerRequest r) {
        if (_methods.get(r.op_name()) == 0) { // FIPA.MTS.message
            NVList _list = _orb().create_list(0);
            Any _aFipaMessage = _orb().create_any();
            _aFipaMessage.type(FipaMessageHelper.type());
            _list.add_value("aFipaMessage", _aFipaMessage, ARG_IN.value);
            r.params(_list);
            FipaMessage aFipaMessage;
            aFipaMessage = FipaMessageHelper.extract(_aFipaMessage);
            this.message(aFipaMessage);
            Any __return = _orb().create_any();
            __return.type(_orb().get_primitive_tc(TCKind.tk_void));
            r.result(__return);
        } else {
            throw new BAD_OPERATION(0, CompletionStatus.COMPLETED_MAYBE);
        }
    }
}
