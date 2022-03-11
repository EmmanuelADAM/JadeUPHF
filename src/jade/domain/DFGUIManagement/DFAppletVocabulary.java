package jade.domain.DFGUIManagement;

import jade.domain.FIPAAgentManagement.FIPAManagementVocabulary;

/**
 * @author Elisabetta Cortese - TiLab S.p.A.
 * @version $Date: 2003-08-26 11:15:34 +0200 (mar, 26 ago 2003) $ $Revision: 4243 $
 */

public interface DFAppletVocabulary extends FIPAManagementVocabulary {

    /**
     * A symbolic constant, containing the name of this ontology.
     */
    String NAME = "DFApplet-Management";


    // Action
    String GETDESCRIPTION = "getdescription";

    //public static final String FEDERATEWITH = "federatewith";
    String FEDERATE = "federate";
    //public static final String FEDERATEWITH_PARENTDF = "parentdf";
    String FEDERATE_DF = "df";
    //public static final String FEDERATEWITH_CHILDRENDF = "childrendf";
    String FEDERATE_DESCRIPTION = "description";

    String REGISTERWITH = "registerwith";
    String REGISTERWITH_DF = "df";
    String REGISTERWITH_DESCRIPTION = "description";

    String DEREGISTERFROM = "deregisterfrom";
    //public static final String DEREGISTERFROM_PARENTDF = "parentdf";
    String DEREGISTERFROM_DF = "df";
    //public static final String DEREGISTERFROM_CHILDRENDF = "childrendf";
    String DEREGISTERFROM_DESCRIPTION = "description";

    String MODIFYON = "modifyon";
    String MODIFYON_DF = "df";
    String MODIFYON_DESCRIPTION = "description";

    String SEARCHON = "searchon";
    String SEARCHON_DF = "df";
    String SEARCHON_DESCRIPTION = "description";
    String SEARCHON_CONSTRAINTS = "constraints";

    String GETPARENTS = "getparents";

    String GETDESCRIPTIONUSED = "getdescriptionused";
    String GETDESCRIPTIONUSED_PARENTDF = "parentdf";

}
