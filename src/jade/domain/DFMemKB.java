/*
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

The updating of this file to JADE 2.0 has been partially supported by the IST-1999-10211 LEAP Project

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
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.KBManagement.MemKB;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Elisabetta Cortese - TILab
 */
public class DFMemKB extends MemKB {

    boolean entriesToDelete = false; // gets true if there's at least one entry to delete for the method clean

    /**
     * Constructor
     *
     * @param maxResultLimit JADE internal limit for maximum number of search results
     */
    public DFMemKB(int maxResultLimit) {
        super(maxResultLimit);
        clean();
    }


    protected Object insert(Object name, Object fact) {
        DFAgentDescription desc = (DFAgentDescription) fact;
        if (desc.getLeaseTime() != null) {
            entriesToDelete = true;
        }
        return super.insert(name, fact);
    }

    /**
     * Scan the facts and remove those whose lease time has expired.
     */
    protected void clean() {

        if (entriesToDelete) {
            ArrayList<AID> toBeRemoved = new ArrayList<>();
            for (Object o : facts.values()) {
                DFAgentDescription dfd = (DFAgentDescription) o;
                if (dfd.checkLeaseTimeExpired()) {
                    toBeRemoved.add(dfd.getName());
                }
            }
            for (AID aid : toBeRemoved) {
                facts.remove(aid);
            }
        }
    }

    // match
    public final boolean match(Object template, Object fact) {
        return compare(template, fact);
    }

    public static boolean compare(Object template, Object fact) {

        try {
            DFAgentDescription templateDesc = (DFAgentDescription) template;
            DFAgentDescription factDesc = (DFAgentDescription) fact;
            // We must not return facts whose lease time has expired (no
            // matter if they match)
            if (factDesc.checkLeaseTimeExpired())
                return false;

            // Match name
            AID id1 = templateDesc.getName();
            if (id1 != null) {
                AID id2 = factDesc.getName();
                if ((id2 == null) || (!matchAID(id1, id2)))
                    return false;
            }

            // Match protocol set
            Iterator<String> itTemplate = templateDesc.getAllProtocols();
            while (itTemplate.hasNext()) {
                String templateProto = itTemplate.next();
                boolean found = false;
                Iterator<String> itFact = factDesc.getAllProtocols();
                while (!found && itFact.hasNext()) {
                    String factProto = itFact.next();
                    found = templateProto.equalsIgnoreCase(factProto);
                }
                if (!found)
                    return false;
            }

            // Match ontologies set
            itTemplate = templateDesc.getAllOntologies();
            while (itTemplate.hasNext()) {
                String templateOnto = itTemplate.next();
                boolean found = false;
                Iterator<String> itFact = factDesc.getAllOntologies();
                while (!found && itFact.hasNext()) {
                    String factOnto = itFact.next();
                    found = templateOnto.equalsIgnoreCase(factOnto);
                }
                if (!found)
                    return false;
            }

            // Match languages set
            itTemplate = templateDesc.getAllLanguages();
            while (itTemplate.hasNext()) {
                String templateLang = itTemplate.next();
                boolean found = false;
                Iterator<String> itFact = factDesc.getAllLanguages();
                while (!found && itFact.hasNext()) {
                    String factLang = itFact.next();
                    found = templateLang.equalsIgnoreCase(factLang);
                }
                if (!found)
                    return false;
            }

            // Match services set
            Iterator<ServiceDescription> itTemplate2 = templateDesc.getAllServices();
            while (itTemplate2.hasNext()) {
                ServiceDescription templateSvc = itTemplate2.next();
                boolean found = false;
                Iterator<ServiceDescription> itFact = factDesc.getAllServices();
                while (!found && itFact.hasNext()) {
                    ServiceDescription factSvc = itFact.next();
                    found = compareServiceDesc(templateSvc, factSvc);
                }
                if (!found)
                    return false;
            }

            return true;
        } catch (ClassCastException cce) {
            return false;
        }
    }

    // Helper method to compare two Service Description objects
    public static boolean compareServiceDesc(ServiceDescription template, ServiceDescription fact) {

        // Match name
        String n1 = template.getName();
        if (n1 != null) {
            String n2 = fact.getName();
            if ((!n1.equalsIgnoreCase(n2)))
                return false;
        }

        // Match type
        String t1 = template.getType();
        if (t1 != null) {
            String t2 = fact.getType();
            if ((t2 == null) || (!t1.equalsIgnoreCase(t2)))
                return false;
        }

        // Match ownership
        String o1 = template.getOwnership();
        if (o1 != null) {
            String o2 = fact.getOwnership();
            if ((o2 == null) || (!o1.equalsIgnoreCase(o2)))
                return false;
        }

        // Match ontologies set
        Iterator<String> itTemplate = template.getAllOntologies();
        while (itTemplate.hasNext()) {
            String templateOnto = itTemplate.next();
            boolean found = false;
            Iterator<String> itFact = fact.getAllOntologies();
            while (!found && itFact.hasNext()) {
                String factOnto = itFact.next();
                found = templateOnto.equalsIgnoreCase(factOnto);
            }
            if (!found)
                return false;
        }

        // Match languages set
        itTemplate = template.getAllLanguages();
        while (itTemplate.hasNext()) {
            String templateLang = itTemplate.next();
            boolean found = false;
            Iterator<String> itFact = fact.getAllLanguages();
            while (!found && itFact.hasNext()) {
                String factLang = itFact.next();
                found = templateLang.equalsIgnoreCase(factLang);
            }
            if (!found)
                return false;
        }

        // Match protocols set
        itTemplate = template.getAllProtocols();
        while (itTemplate.hasNext()) {
            String templateProto = itTemplate.next();
            boolean found = false;
            Iterator<String> itFact = fact.getAllProtocols();
            while (!found && itFact.hasNext()) {
                String factProto = itFact.next();
                found = templateProto.equalsIgnoreCase(factProto);
            }
            if (!found)
                return false;
        }

        // Match properties set
        Iterator<Property> itTemplate2 = template.getAllProperties();
        while (itTemplate.hasNext()) {
            Property templateProp = itTemplate2.next();
            boolean found = false;
            Iterator<Property> itFact = fact.getAllProperties();
            while (!found && itFact.hasNext()) {
                Property factProp = itFact.next();
                found = factProp.match(templateProp);
            }
            if (!found)
                return false;
        }

        return true;
    }
}
