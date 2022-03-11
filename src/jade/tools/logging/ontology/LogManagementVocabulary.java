/*****************************************************************
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

package jade.tools.logging.ontology;

//#J2ME_EXCLUDE_FILE

/**
 * This interface contains all the string constants for frame and slot
 * names of the <code>jLog-Management-Ontology</code> ontology.
 */
public interface LogManagementVocabulary {
    /**
     * A symbolic constant, containing the name of thie Log Management ontology.
     */
    String NAME = "Log-Management-Ontology";

    String LOGGER_INFO = "LOGGER-INFO";
    String LOGGER_INFO_NAME = "name";
    String LOGGER_INFO_LEVEL = "level";
    String LOGGER_INFO_HANDLERS = "handlers";
    String LOGGER_INFO_FILE = "file";

    String GET_ALL_LOGGERS = "GET-ALL-LOGGERS";
    String GET_ALL_LOGGERS_TYPE = "type";
    String GET_ALL_LOGGERS_FILTER = "filter";

    String SET_LEVEL = "SET-LEVEL";
    String SET_LEVEL_LEVEL = "level";
    String SET_LEVEL_LOGGER = "logger";

    String SET_FILE = "SET-FILE";
    String SET_FILE_FILE = "file";
    String SET_FILE_LOGGER = "logger";
}
