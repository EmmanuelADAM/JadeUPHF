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

package jade.content.onto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;

//#J2ME_EXCLUDE_FILE

/**
 * Extension of  Ontology   that allows to build the
 * ontological elements adding directly the classes of the
 * corresponding JavaBeans.
 * The  BeanOntology   internally needs to use its introspector
 *  BeanIntrospector  . A typical pattern to create an ontology
 * extending the  BeanOntology   is the following:
 *
 * <p><blockquote><pre>
 * public class MyOntology extends BeanOntology {<br>
 *     private static Ontology theInstance = new MyOntology(ONTOLOGY_NAME);
 *
 *     public static Ontology getInstance() {
 *         return theInstance;
 *     }
 *
 *     private MyOntology(String name) {
 *         super(name);
 *
 *         try {
 *             add("com.acme.rocket.ontology");
 *             add(C1.class);
 *             add(C2.class);
 *         } catch (OntologyException e) {
 *             e.printStackTrace();
 *         }
 *     }
 * }
 * </pre></blockquote></p>
 * <p>
 * The ontology  MyOntology   will be built creating the hierarchy
 * of ontological items defined by the beans in package  com.acme.rocket.ontology  
 * plus the two beans  C1   and  C2  .
 * An ontological bean is a class implementing either  Concept   or  Predicate  
 * Example:
 * <p><blockquote><pre>
 * public class CD extends Item {
 *
 *     private String title;
 *     protected List tracks;
 *
 *     public String getTitle() {
 *         return title;
 *     }
 *
 *     public void setTitle(String t) {
 *         title = t;
 *     }
 *
 *      @  AggregateSlot(cardMin = 1)
 *     public List getTracks() {
 *         return tracks;
 *     }
 *
 *     public void setTracks(List l) {
 *         tracks = l;
 *     }
 * }
 *
 * </pre></blockquote></p>
 * A set of annotatons allow to customize the ontological properties of the slots.
 *
 * @author Paolo Cancedda
 * @see jade.content.Concept
 * @see jade.content.Predicate
 * @see jade.content.onto.annotations.Element
 * @see jade.content.onto.annotations.Slot
 * @see jade.content.onto.annotations.SuppressSlot
 * @see jade.content.onto.annotations.AggregateSlot
 * @see jade.content.onto.annotations.Result
 * @see jade.content.onto.annotations.AggregateResult
 * @see Ontology
 * @see BasicOntology
 */
public class BeanOntology extends Ontology {

    @Serial
    private static final long serialVersionUID = -2007125499000302494L;

    private transient BeanOntologyBuilder bob;

    /**
     * Create an Ontology with the given  name  .
     * The  BasicOntology   is automatically added
     * as the base ontology.
     *
     * @param name The identifier of the ontology.
     */
    public BeanOntology(String name) {
        this(name, BasicOntology.getInstance());
    }

    /**
     * Create an Ontology with the given  name   that
     * extends the ontology  base  , which must have
     *  BasicOntology   in its hierarchy.
     *
     * @param name The identifier of the ontology.
     * @param base The base ontology.
     */
    public BeanOntology(String name, Ontology base) {
        this(name, new Ontology[]{base});
    }

    /**
     * Create an Ontology with the given  name   that
     * extends the  base   set of ontologies. At least
     * one of the  base   ontologies must extend the
     * ontology  BasicOntology  .
     *
     * @param name The identifier of the ontology.
     * @param base The base ontologies
     */
    public BeanOntology(String name, Ontology[] base) {
        super(name, ensureBasicOntology(base), new BeanIntrospector());
        bob = new BeanOntologyBuilder(this);
    }

    private static Ontology[] ensureBasicOntology(Ontology[] base) {
        if (base == null) {
            return new Ontology[]{BasicOntology.getInstance()};
        } else if (!Ontology.isBaseOntology(base, BasicOntology.getInstance().getName())) {
            Ontology[] newBase = new Ontology[base.length + 1];
            System.arraycopy(base, 0, newBase, 0, base.length);
            newBase[base.length] = BasicOntology.getInstance();
            return newBase;
        } else {
            return base;
        }
    }

    /**
     * Adds to the ontology the schema built from the class  clazz  .
     * The class must implement either  Concept  
     * or  Predicate  .
     *
     * @param clazz class from which to build the ontological schema
     *              throws BeanOntologyException
     */
    public void add(Class<?> clazz) throws BeanOntologyException {
        add(clazz, true);
    }

    /**
     * Adds all the ontological beans (the ones which implement either
     *  Concept   or  Predicate  ) found in the
     * specified package.
     *
     * @param pkgname name of the package containing the beans
     *                throws BeanOntologyException
     */
    public void add(String pkgname) throws BeanOntologyException {
        add(pkgname, true);
    }

    /**
     * Adds to the ontology the schema built from the class  clazz  .
     * The class must implement either  Concept  
     * or  Predicate  .
     *
     * @param clazz          class from which to build the ontological schema
     * @param buildHierarchy if  true  , build the full hierarchy
     *                       ontological elements. Otherwise, build a set of
     *                       flat unrelated elements
     *                       throws BeanOntologyException
     */
    public void add(Class<?> clazz, boolean buildHierarchy) throws BeanOntologyException {
        bob.addSchema(clazz, buildHierarchy);
    }

    /**
     * Adds all the ontological beans (the ones which implement either
     *  Concept   or  Predicate  ) found in the
     * specified package.
     *
     * @param pkgname        name of the package containing the beans
     * @param buildHierarchy if  true  , build the full hierarchy
     *                       ontological elements. Otherwise, build a set of
     *                       flat unrelated elements
     *                       throws BeanOntologyException
     */
    public void add(String pkgname, boolean buildHierarchy) throws BeanOntologyException {
        bob.addSchemas(pkgname, buildHierarchy);
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        // Create a new instance of BOB
        bob = new BeanOntologyBuilder(this);
    }
}
