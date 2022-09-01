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

package jade.domain.mobility;

import jade.domain.FIPAAgentManagement.ExceptionVocabulary;

/**
 * This interface contains all the string constants for concepts and slot
 * names defined in the
 *  Behaviour-Loading   ontology.
 */
public interface BehaviourLoadingVocabulary extends ExceptionVocabulary {

    /**
     * A symbolic constant, containing the name of this ontology.
     */
    String NAME = "Behaviour-Loading";

    // Concepts
    String PARAMETER = "parameter";
    String PARAMETER_NAME = "name";
    String PARAMETER_VALUE = "value";
    String PARAMETER_MODE = "mode";

    // Actions
    String LOAD_BEHAVIOUR = "load-behaviour";
    String LOAD_BEHAVIOUR_CLASS_NAME = "class-name";
    String LOAD_BEHAVIOUR_CODE = "code";
    String LOAD_BEHAVIOUR_ZIP = "zip";
    String LOAD_BEHAVIOUR_PARAMETERS = "parameters";
}
