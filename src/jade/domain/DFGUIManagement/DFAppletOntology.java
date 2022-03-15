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


package jade.domain.DFGUIManagement;


import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.ReflectiveIntrospector;
import jade.content.schema.AgentActionSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.TermSchema;
import jade.domain.FIPAAgentManagement.FIPAManagementOntology;

/**
 * @author Elisabetta Cortese - TiLab S.p.A.
 * @version $Date: 2003-08-26 11:15:34 +0200 (mar, 26 ago 2003) $ $Revision: 4243 $
 */

/**
 * This class represents the ontology
 * <code>DFApplet-management</code>, containing all JADE extensions
 * related to applet management. There is only a single instance of
 * this class.
 * <p>
 * The package contains one class for each Frame in the ontology.
 * <p>
 */


public class DFAppletOntology extends Ontology implements DFAppletVocabulary {

    private static final Ontology theInstance = new DFAppletOntology();

    private DFAppletOntology() {
        super(NAME, FIPAManagementOntology.getInstance(), new ReflectiveIntrospector());

        try {
            add(new AgentActionSchema(GETDESCRIPTION), GetDescription.class);
            add(new AgentActionSchema(FEDERATE), Federate.class);
            add(new AgentActionSchema(REGISTERWITH), RegisterWith.class);
            add(new AgentActionSchema(DEREGISTERFROM), DeregisterFrom.class);
            add(new AgentActionSchema(MODIFYON), ModifyOn.class);
            add(new AgentActionSchema(SEARCHON), SearchOn.class);
            add(new AgentActionSchema(GETPARENTS), GetParents.class);
            add(new AgentActionSchema(GETDESCRIPTIONUSED), GetDescriptionUsed.class);

            AgentActionSchema as = (AgentActionSchema) getSchema(FEDERATE);
            as.add(FEDERATE_DF, (TermSchema) getSchema(BasicOntology.AID));
            as.add(FEDERATE_DESCRIPTION, (TermSchema) getSchema(DFAGENTDESCRIPTION), ObjectSchema.OPTIONAL);

            as = (AgentActionSchema) getSchema(REGISTERWITH);
            as.add(REGISTERWITH_DF, (TermSchema) getSchema(BasicOntology.AID));
            as.add(REGISTERWITH_DESCRIPTION, (TermSchema) getSchema(DFAGENTDESCRIPTION));

            as = (AgentActionSchema) getSchema(DEREGISTERFROM);
            as.add(DEREGISTERFROM_DF, (TermSchema) getSchema(BasicOntology.AID));
            as.add(DEREGISTERFROM_DESCRIPTION, (TermSchema) getSchema(DFAGENTDESCRIPTION));

            as = (AgentActionSchema) getSchema(MODIFYON);
            as.add(MODIFYON_DF, (TermSchema) getSchema(BasicOntology.AID));
            as.add(MODIFYON_DESCRIPTION, (TermSchema) getSchema(DFAGENTDESCRIPTION));

            as = (AgentActionSchema) getSchema(SEARCHON);
            as.add(SEARCHON_DF, (TermSchema) getSchema(BasicOntology.AID));
            as.add(SEARCHON_DESCRIPTION, (TermSchema) getSchema(DFAGENTDESCRIPTION));
            as.add(SEARCHON_CONSTRAINTS, (TermSchema) getSchema(SEARCHCONSTRAINTS));

            as = (AgentActionSchema) getSchema(GETDESCRIPTIONUSED);
            as.add(GETDESCRIPTIONUSED_PARENTDF, (TermSchema) getSchema(BasicOntology.AID));
        } catch (OntologyException oe) {
            oe.printStackTrace();
        }

    }

    /**
     * This method grants access to the unique instance of the
     * ontology.
     *
     * @return An <code>Ontology</code> object, containing the concepts
     * of the ontology.
     */
    public static Ontology getInstance() {
        return theInstance;
    }

}
