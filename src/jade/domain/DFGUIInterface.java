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

package jade.domain;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This interface is used to avoid any relationship between the df
 * and the tools packages.
 * A gui for a DF must implements this interface.
 *
 * @author Tiziana Trucco - CSELT S.p.A.
 * @version $Date: 2003-11-18 17:26:01 +0100 (mar, 18 nov 2003) $ $Revision: 4564 $
 */

public interface DFGUIInterface {

    void addParent(AID parentName);

    void removeParent(AID parentName);

    void addAgentDesc(AID name);

    void removeAgentDesc(AID name, AID df);

    void addChildren(AID childrenName);

    void removeChildren(AID childrenName);

    void setAdapter(DFGUIAdapter dfa);

    void showStatusMsg(String msg);

    void refreshLastSearchResults(ArrayList<DFAgentDescription> l, AID df);

    void removeSearchResult(AID name);

    void disposeAsync();

    void setVisible(boolean b);

    void refresh(Iterator<AID> AIDOfAllAgentRegistered, Iterator<AID> parents, Iterator<AID> children);


}
