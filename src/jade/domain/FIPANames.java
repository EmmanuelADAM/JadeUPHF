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

/**
 * This class provides a single access point for the
 * set of constants
 * already defined by FIPA.
 * The constants have been grouped by category (i.e. ACLCodecs,
 * Content Languages, MTPs, ...), with one inner class implementing each
 * category.
 *
 * @author Fabio Bellifemine - TILab
 * @version $Date: 2006-08-30 10:57:51 +0200 (mer, 30 ago 2006) $ $Revision: 5893 $
 **/

public interface FIPANames {
    String AMS = "ams";
    String DEFAULT_DF = "df";

    /**
     * Set of constants that identifies the Codec of ACL Messages and
     * that can be assigned via
     *   ACLMessage.getEnvelope().setAclRepresentation(FIPANames.ACLCodec.BITEFFICIENT);
     **/
    interface ACLCodec {
        /**
         * Syntactic representation of ACL in string form
         *
         * @see <a href=http://www.fipa.org/specs/fipa00070/XC00070f.html>FIPA Spec</a>
         **/
        String STRING = "fipa.acl.rep.string.std";
        /**
         * Syntactic representation of ACL in XML form
         *
         * @see <a href=http://www.fipa.org/specs/fipa00071/XC00071b.html>FIPA Spec</a>
         **/
        String XML = "fipa.acl.rep.xml.std";
        /**
         * Syntactic representation of ACL in bit-efficient form
         *
         * @see <a href=http://www.fipa.org/specs/fipa00069/XC00069e.html>FIPA Spec</a>
         **/
        String BITEFFICIENT = "fipa.acl.rep.bitefficient.std";
    }

    /**
     * Set of constants that identifies the Interaction Protocols and that
     * can be assigned via
     *  ACLMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST)
     *
     **/
    interface InteractionProtocol {

        /**
         * The FIPA-Request interaction protocol.
         */
        String FIPA_REQUEST = "fipa-request";

        /**
         * The FIPA-Query interaction protocol.
         */
        String FIPA_QUERY = "fipa-query";

        /**
         * The FIPA-Request-When interaction protocol.
         */
        String FIPA_REQUEST_WHEN = "fipa-request-when";

        /**
         * The FIPA-Brokering interaction protocol.
         */
        String FIPA_BROKERING = "fipa-brokering";

        /**
         * The FIPA-Recruiting interaction protocol.
         */
        String FIPA_RECRUITING = "fipa-recruiting";

        /**
         * The FIPA-Propose interaction protocol.
         */
        String FIPA_PROPOSE = "fipa-propose";

        /**
         * The FIPA-Subscribe interaction protocol.
         */
        String FIPA_SUBSCRIBE = "fipa-subscribe";

        /**
         * The FIPA-Auction-English interaction protocol.
         */
        String FIPA_ENGLISH_AUCTION = "fipa-auction-english";

        /**
         * The FIPA-Auction-Dutch interaction protocol.
         */
        String FIPA_DUTCH_AUCTION = "fipa-auction-dutch";

        /**
         * The FIPA-Contract-Net interaction protocol.
         */
        String FIPA_CONTRACT_NET = "fipa-contract-net";

        /**
         * The FIPA-Iterated-Contract-Net interaction protocol.
         */
        String FIPA_ITERATED_CONTRACT_NET = "fipa-iterated-contract-net";

        /**
         * The Iterated-Fipa-Request interaction protocol.
         * Note that this protocol is defined in JADE and is not a standard FIPA protocol.
         */
        String ITERATED_FIPA_REQUEST = "iterated-fipa-request";
    }

    /**
     * Set of constants that identifies the content languages and that
     * can be assigned via
     *  ACLMessage.setLanguage(FIPANames.ContentLanguage.SL0)
     *
     **/
    interface ContentLanguage {
        /**
         * The level-0 profile for the FIPA SL content language.
         */
        String FIPA_SL0 = "fipa-sl0";

        /**
         * The level-1 profile for the FIPA-SL content language.
         */
        String FIPA_SL1 = "fipa-sl1";

        /**
         * The level-2 profile for the FIPA-SL content language.
         */
        String FIPA_SL2 = "fipa-sl2";

        /**
         * The FIPA-SL language, with no restriction on
         * expressiveness.
         */
        String FIPA_SL = "fipa-sl";
    }


    /**
     * Set of constants that identifies the Ontology of ACL Messages and
     * that can be assigned via
     *   ACLMessage.setOntology(FIPANames.Ontology.SL0_ONTOLOGY);
     **/
    interface Ontology {
        /**
         * constant for FIPA SL-0 Ontology
         **/
        String SL0_ONTOLOGY = "SL0-ONTOLOGY";
        /**
         * constant for FIPA SL-1 Ontology
         **/
        String SL1_ONTOLOGY = "SL1-ONTOLOGY";
        /**
         * constant for FIPA SL-2 Ontology
         **/
        String SL2_ONTOLOGY = "SL2-ONTOLOGY";
        /**
         * constant for FIPA SL Ontology
         **/
        String SL_ONTOLOGY = "SL-ONTOLOGY";
    }


    /**
     * Set of constants that identifies the Message Transport Protocols.
     **/
    interface MTP {
        /**
         * IIOP-based MTP
         *
         * @see <a href=http://www.fipa.org/specs/fipa00075/XC00075e.html>FIPA Spec</a>
         **/
        String IIOP = "fipa.mts.mtp.iiop.std";
        /**
         * WAP-based MTP
         *
         * @see <a href=http://www.fipa.org/specs/fipa00076/XC00076c.html>FIPA Spec</a>
         **/
        String WAP = "fipa.mts.mtp.wap.std";
        /**
         * HTTP-based MTP
         *
         * @see <a href=http://www.fipa.org/specs/fipa00084/XC00084d.html>FIPA Spec</a>
         **/
        String HTTP = "fipa.mts.mtp.http.std";
    }
}
