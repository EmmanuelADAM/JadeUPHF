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

package jade.core;

//#APIDOC_EXCLUDE_FILE

import java.io.Serial;
import java.io.Serializable;

/**
 * The  ServiceDescriptor   class serves as a meta-level
 * description of a kernel-level service.
 * Instances of this class contain a  Service   object,
 * along with its name and properties, and are used in service
 * management operations, as well as in agent-level introspection of
 * platform-level entities.
 *
 * @author Giovanni Rimassa - FRAMeTech s.r.l.
 * @see Service
 */
public class ServiceDescriptor implements Serializable {

    private String myName;
    private boolean myIsMandatory;
    private transient Service myService;
    //#MIDP_EXCLUDE_BEGIN
    private String serviceClass;

    /**
     * Builds a new service descriptor, describing the given service
     * with the given name and properties.
     *
     * @param sn  The name of the described service.
     * @param svc The described  Service   object.
     */
    public ServiceDescriptor(String sn, Service svc) {
        myName = sn;
        myService = svc;
        myIsMandatory = false;
    }

    /**
     * Builds an uninitialized service descriptor.
     *
     * @see ServiceDescriptor#setName(String sn)
     * @see ServiceDescriptor#setService(Service svc)
     */
    public ServiceDescriptor() {
        this(null, null);
    }

    /**
     * Retrieve the name (if any) of the described service.
     *
     * @return The name of the described service, or  null  
     * if no name was set.
     */
    public String getName() {
        return myName;
    }

    /**
     * Change the name (if any) of the described service.
     *
     * @param sn The name to assign to the described service.
     */
    public void setName(String sn) {
        myName = sn;
    }

    /**
     * Retrieve the described service.
     *
     * @return The  Service   object described by this
     * service descriptor, or  null   if no service was set.
     */
    public Service getService() {
        return myService;
    }

    /**
     * Change the described service (if any).
     *
     * @param svc The  Service   object that is to be
     *            described by this service descriptor.
     */
    public void setService(Service svc) {
        myService = svc;
    }

    public boolean isMandatory() {
        return myIsMandatory;
    }

    public void setMandatory(boolean isMandatory) {
        myIsMandatory = isMandatory;
    }

    @Serial
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        if (myService != null) {
            serviceClass = myService.getClass().getName();
        }
        out.defaultWriteObject();
    }

    @Serial
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (serviceClass != null) {
            try {
                myService = (Service) Class.forName(serviceClass).getConstructor().newInstance();
            } catch (ClassNotFoundException cnfe) {
                throw cnfe;
            } catch (Throwable t) {
                throw new java.io.IOException("Can't create service " + serviceClass + ". " + t.getMessage());
            }
        }
    }
    //#MIDP_EXCLUDE_END
}
