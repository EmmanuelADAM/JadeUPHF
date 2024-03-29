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

import jade.content.AgentAction;

import java.util.List;

/**
 * This action represents a request to load a  Behaviour   whose
 * code is not included in the classpath of the JVM where the agent that is
 * going to execute the behaviour lives.
 *
 * @author Giovanni Caire - TILAB
 * @see jade.core.behaviours.LoaderBehaviour
 */
public class LoadBehaviour implements AgentAction {
    private String className;
    private byte[] code;
    private byte[] zip;
    private List<Parameter> parameters;

    public LoadBehaviour() {
    }

    /**
     * @return the name of the class of the behaviour to load
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the name of the class of the behaviour to load
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the code of the class of the behaviour to load.
     */
    public byte[] getCode() {
        return code;
    }

    /**
     * Sets the code of the class of the behaviour to load.
     *  code   must be filled with the content of the class
     * file of the behaviour to load.
     * If the behaviour requires other classes, the  setZip()
     * method must be used instead.
     */
    public void setCode(byte[] code) {
        this.code = code;
    }

    /**
     * @return the code of the behaviour to load as the content of a zip
     * file.
     */
    public byte[] getZip() {
        return zip;
    }

    /**
     * Sets the code of the behaviour to load as the content of a zip
     * file.
     */
    public void setZip(byte[] zip) {
        this.zip = zip;
    }

    /**
     * @return the list of parameters to be passed to the behaviour.
     */
    public List<Parameter> getParameters() {
        return parameters;
    }

    /**
     * Set the list of parameters to be passed to the behaviour.
     * These parameters will be inserted into the behaviour
     *  HashMap
     */
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }
}