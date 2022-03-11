/*
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.tools.dfgui;

import jade.core.AID;

import javax.swing.table.AbstractTableModel;
import java.util.Iterator;
import java.util.Vector;

/**
 * This class extends the AbstractTableModel to provide an appropriate model for the table used
 * to display agents in the gui of the DF.
 *
 * @author Giovanni Caire Adriana Quinto - CSELT S.p.A.
 * @version $Date: 2008-10-09 14:04:02 +0200 (gio, 09 ott 2008) $ $Revision: 6051 $
 * @see AbstractTableModel
 */

class AgentNameTableModel extends AbstractTableModel {
    Vector<AID> names;

    // CONSTRUCTORS
    public AgentNameTableModel() {
        super();
        names = new Vector<>();
    }

    // ADD
    public void add(AID name) {
        names.add(name);
    }

    //REMOVE
    public void remove(AID name) {
        names.remove(name);
    }

    // GETELEMENTAT
    public AID getElementAt(int index) {
        return names.get(index);
    }

    // CLEAR
    public void clear() {
        names.clear();
    }

    // Methods to be implemented to have a concrete class
    public int getRowCount() {
        return (names.size());
    }

    public int getColumnCount() {
        return (3);
    }

    public Object getValueAt(int row, int column) {
        AID aid = getElementAt(row);
        StringBuilder out = new StringBuilder();
        switch (column) {
            case 0:
                out = new StringBuilder(aid.getName());
                break;
            case 1:
                for (Iterator<String> i = aid.getAllAddresses(); i.hasNext(); )
                    try {
                        out.append(i.next()).append(" ");
                    } catch (Exception e) {
                        e.printStackTrace();
                        out = new StringBuilder(" ");
                    }
                break;
            case 2:
                for (Iterator<AID> i = aid.getAllResolvers(); i.hasNext(); )
                    try {
                        out.append((i.next()).getName()).append(" ");
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        out = new StringBuilder(" ");
                    }
                break;
        }
        return out.toString();
    }
}