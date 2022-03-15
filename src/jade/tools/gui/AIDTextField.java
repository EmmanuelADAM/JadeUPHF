/******************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2002 TILAB S.p.A.
 *
 * This file is donated by Acklin B.V. to the JADE project.
 *
 *
 * GNU Lesser General Public License
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * ***************************************************************/
package jade.tools.gui;

import jade.core.AID;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.lang.reflect.Method;
import java.util.Observable;
import java.util.Observer;

/**
 * This class shows a JTextfield showing a AID
 *
 * @author Chris van Aart - Acklin B.V., the Netherlands
 * @since April 26, 2002
 */

public class AIDTextField extends JTextField implements Observer {

    private AID itsAid;
    private String fieldName;

    /**
     * Description of the Method
     *
     * @param arg       Description of Parameter
     * @param fieldName Description of Parameter
     */
    public void register(Object arg, String fieldName) {
        this.itsAid = (AID) arg;
        this.fieldName = fieldName;
        String methodName = "get" + fieldName;
        try {
            Method sn = itsAid.getClass().getMethod(methodName, (Class<?>[]) null);
            Object res = sn.invoke(itsAid);
            setText(res != null ? res.toString() : "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Description of the Method
     *
     * @param arg Description of Parameter
     * @param str Description of Parameter
     */
    public void unregister(Object arg, String str) {
//    itsAid.deleteObserver(this);
    }

    /**
     * Description of the Method
     *
     * @param ob  Description of Parameter
     * @param arg Description of Parameter
     * @deprecated
     */
    @Deprecated
    public void update(Observable ob, Object arg) {
        String methodName = "get" + fieldName;
        String theType = "java.lang.String";
        try {
            Method sn = itsAid.getClass().getMethod(methodName, (Class<?>[]) null);
            Object res = sn.invoke(itsAid);
            setText(res != null ? res.toString() : "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Description of the Method
     *
     * @param e Description of Parameter
     */
    public void focusLost(FocusEvent e) {
        String value = getText();
        String methodName = "set" + fieldName;
        String theType = "java.lang.String";
        try {
            Method sn = itsAid.getClass().getMethod(methodName, Class.forName(theType));
            sn.invoke(itsAid, value);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Description of the Method
     *
     * @param e Description of Parameter
     */
    protected void processFocusEvent(FocusEvent e) {
        super.processFocusEvent(e);
        if (e.getID() == FocusEvent.FOCUS_LOST) {
            focusLost(e);
        }

    }
}
//  ***EOF***
