/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * <p>
 * GNU Lesser General Public License
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.content;

import jade.content.abs.AbsContentElement;
import jade.content.lang.ByteArrayCodec;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.StringCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.ObjectSchema;
import jade.core.CaseInsensitiveString;
import jade.lang.acl.ACLMessage;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class provides all methods to manage the content languages
 * and ontologies "known" by a given agent and to fill and extract the
 * content of an ACL message according to a given content language and
 * ontology.
 * Each agent has a  ContentManager   object accessible through
 * the  getContentManager()   method of the  Agent  
 * class.
 *
 * @author Federico Bergenti
 * @author Govanni Caire - TILAB
 */
public class ContentManager implements Serializable {
    transient private Map<CaseInsensitiveString, Codec> languages = new HashMap<>();
    transient private Map<CaseInsensitiveString, Ontology> ontologies = new HashMap<>();
    private boolean validationMode = true;

    //#MIDP_EXCLUDE_BEGIN
    @Serial
    private void readObject(java.io.ObjectInputStream oin) throws java.io.IOException, ClassNotFoundException {
        oin.defaultReadObject();
        languages = new HashMap<>();
        ontologies = new HashMap<>();
    }
    //#MIDP_EXCLUDE_END

    /**
     * Registers a  Codec   for a given content language
     * with its default name (i.e.
     * the name returned by its  getName()   method.
     * Since this operation is performed the agent that owns this
     *  ContentManager   is able to "speak" the language
     * corresponding to the registered  Codec  .
     *
     * @param c the  Codec   to be registered.
     */
    public void registerLanguage(Codec c) {
        if (c == null) {
            throw new IllegalArgumentException("Null codec registered");
        }
        registerLanguage(c, c.getName());
    }

    /**
     * Registers a  Codec   for a given content language
     * with a given name.
     *
     * @param c    the  Codec   to be registered.
     * @param name the name associated to the registered codec.
     */
    public void registerLanguage(Codec c, String name) {
        if (c == null) {
            throw new IllegalArgumentException("Null codec registered");
        }
        languages.put(new CaseInsensitiveString(name), c);
    }

    /**
     * Registers an  Ontology   with its default name (i.e.
     * the name returned by its  getName()   method.
     * Since this operation is performed the agent that owns this
     *  ContentManager   "knows" the registered
     *  Ontology  .
     *
     * @param o the  Ontology   to be registered.
     */
    public void registerOntology(Ontology o) {
        if (o == null) {
            throw new IllegalArgumentException("Null ontology registered");
        }
        registerOntology(o, o.getName());
    }

    /**
     * Registers an  Ontology   with a given name.
     *
     * @param o    the  Ontology   to be registered.
     * @param name the name associated to the registered Ontology.
     */
    public void registerOntology(Ontology o, String name) {
        if (o == null) {
            throw new IllegalArgumentException("Null ontology registered");
        }
        ontologies.put(new CaseInsensitiveString(name), o);
    }

    /**
     * Retrieves a previously registered  Codec  
     * giving its  name  .
     *
     * @param name the name associated to the  Codec  
     *             to be retrieved.
     * @return the  Codec   associated to
     *  name   or  null   if no Codec was registered
     * with the given name.
     */
    public Codec lookupLanguage(String name) {
        return (name == null ? null : languages.get(new CaseInsensitiveString(name)));
    }

    /**
     * Retrieves a previously registered  Ontology  
     * giving its  name  .
     *
     * @param name the name associated to the  Ontology  
     *             to be retrieved.
     * @return the  Ontology   associated to
     *  name   or  null   if no Ontology was registered
     * with the given name.
     */
    public Ontology lookupOntology(String name) {
        return (name == null ? null : ontologies.get(new CaseInsensitiveString(name)));
    }

    /**
     * Fills the  :content   slot of an
     *  ACLMessage msg   using the content language
     * and ontology indicated in the  :language   and
     *  :ontology   fields of  msg  .
     *
     * @param msg     the message whose content has to be filled.
     * @param content the content of the message represented as an
     *                 AbsContentElement  .
     * @throws CodecException    if  content   is not compliant
     *                           to the content language used for this operation.
     * @throws OntologyException if  content   is not compliant
     *                           to the ontology used for this operation.
     */
    public void fillContent(ACLMessage msg, AbsContentElement content) throws CodecException, OntologyException {
        Codec codec = lookupLanguage(msg.getLanguage());
        if (codec == null) {
            throw new CodecException("Unknown language " + msg.getLanguage());
        }
        String ontoName = msg.getOntology();
        Ontology o = null;
        if (ontoName != null) {
            o = lookupOntology(ontoName);
            if (o == null) {
                throw new OntologyException("Unknown ontology " + msg.getOntology());
            }
        }
        Ontology onto = getMergedOntology(codec, o);

        validate(content, onto);

        encode(msg, content, codec, onto);
    }

    /**
     * Fills the  :content   slot of an
     *  ACLMessage msg   using the content language
     * and ontology indicated in the  :language   and
     *  :ontology   fields of  msg  .
     *
     * @param msg     the message whose content has to be filled.
     * @param content the content of the message represented as a
     *                 ContentElement  .
     * @throws CodecException    if  content   is not compliant
     *                           to the content language used for this operation.
     * @throws OntologyException if  content   is not compliant
     *                           to the ontology used for this operation.
     */
    public void fillContent(ACLMessage msg, ContentElement content) throws CodecException, OntologyException {
        Codec codec = lookupLanguage(msg.getLanguage());
        if (codec == null) {
            throw new CodecException("Unknown language " + msg.getLanguage());
        }
        String ontoName = msg.getOntology();
        Ontology o = null;
        if (ontoName != null) {
            o = lookupOntology(ontoName);
            if (o == null) {
                throw new OntologyException("Unknown ontology " + msg.getOntology());
            }
        }
        Ontology onto = getMergedOntology(codec, o);

        AbsContentElement abs = (AbsContentElement) onto.fromObject(content);

        validate(abs, onto);

        encode(msg, abs, codec, onto);
    }

    /**
     * Translates the  :content   slot of an
     *  ACLMessage msg   into an  AbsContentElement  
     * using the content language and ontology indicated in the
     *  :language   and  :ontology   fields of  msg  .
     *
     * @param msg the message whose content has to be extracted.
     * @return the content of the message represented as an
     *  AbsContentElement  .
     * @throws CodecException    if the content of the message is not compliant
     *                           to the content language used for this operation.
     * @throws OntologyException if the content of the message is not compliant
     *                           to the ontology used for this operation.
     */
    public AbsContentElement extractAbsContent(ACLMessage msg) throws CodecException, OntologyException {
        Codec codec = lookupLanguage(msg.getLanguage());
        if (codec == null) {
            throw new CodecException("Unknown language " + msg.getLanguage());
        }
        String ontoName = msg.getOntology();
        Ontology o = null;
        if (ontoName != null) {
            o = lookupOntology(ontoName);
            if (o == null) {
                throw new OntologyException("Unknown ontology " + msg.getOntology());
            }
        }
        Ontology onto = getMergedOntology(codec, o);

        AbsContentElement content = decode(msg, codec, onto);

        validate(content, onto);

        return content;
    }

    /**
     * Translates the  :content   slot of an
     *  ACLMessage msg   into a  ContentElement  
     * using the content language and ontology indicated in the
     *  :language   and  :ontology   fields of  msg  .
     *
     * @param msg the message whose content has to be extracted.
     * @return the content of the message represented as a
     *  ContentElement  .
     * @throws CodecException    if the content of the message is not compliant
     *                           to the content language used for this operation.
     * @throws OntologyException if the content of the message is not compliant
     *                           to the ontology used for this operation.
     */
    public ContentElement extractContent(ACLMessage msg) throws CodecException, OntologyException {
        Codec codec = lookupLanguage(msg.getLanguage());
        if (codec == null) {
            throw new CodecException("Unknown language " + msg.getLanguage());
        }
        String ontoName = msg.getOntology();
        Ontology o = null;
        if (ontoName != null) {
            o = lookupOntology(ontoName);
            if (o == null) {
                throw new OntologyException("Unknown ontology " + msg.getOntology());
            }
        }
        Ontology onto = getMergedOntology(codec, o);

        AbsContentElement content = decode(msg, codec, onto);

        validate(content, onto);

        return (ContentElement) onto.toObject(content);
    }

    /**
     * Return the currently set validation mode i.e. whether
     * contents that are managed by this content manager should
     * be validated during message content filling/extraction.
     * Default value is  true  
     *
     * @return the currently set validation mode
     */
    public boolean getValidationMode() {
        return validationMode;
    }

    /**
     * Set the validation mode i.e. whether contents that are managed
     * by this content manager should be validated during
     * message content filling/extraction.
     * Default value is  true  
     *
     * @param mode the new validation mode
     */
    public void setValidationMode(boolean mode) {
        validationMode = mode;
    }

    //#APIDOC_EXCLUDE_BEGIN

    /**
     *
     */
    public Ontology getOntology(ACLMessage msg) {
        return getMergedOntology(lookupLanguage(msg.getLanguage()), lookupOntology(msg.getOntology()));
    }
    //#APIDOC_EXCLUDE_END

    /**
     * Merge the reference ontology with the inner ontology of the
     * content language
     */
    private Ontology getMergedOntology(Codec c, Ontology o) {
        Ontology ontology;
        Ontology langOnto = c.getInnerOntology();
        if (langOnto == null) {
            ontology = o;
        } else if (o == null) {
            ontology = langOnto;
        } else {
            ontology = new Ontology(null, new Ontology[]{o, langOnto}, null);
        }
        return ontology;
    }

    private void validate(AbsContentElement content, Ontology onto) throws OntologyException {
        if (validationMode) {
            // Validate the content against the ontology
            ObjectSchema schema = onto.getSchema(content.getTypeName());
            if (schema == null) {
                throw new OntologyException("No schema found for type " + content.getTypeName());
            }
            schema.validate(content, onto);
        }
    }

    private void encode(ACLMessage msg, AbsContentElement content, Codec codec, Ontology onto) throws CodecException, OntologyException {
        if (codec instanceof ByteArrayCodec)
            msg.setByteSequenceContent(((ByteArrayCodec) codec).encode(onto, content));
        else if (codec instanceof StringCodec)
            msg.setContent(((StringCodec) codec).encode(onto, content));
        else
            throw new CodecException("UnsupportedTypeOfCodec");
    }

    private AbsContentElement decode(ACLMessage msg, Codec codec, Ontology onto) throws CodecException, OntologyException {
        if (codec instanceof ByteArrayCodec)
            return ((ByteArrayCodec) codec).decode(onto, msg.getByteSequenceContent());
        else if (codec instanceof StringCodec)
            return ((StringCodec) codec).decode(onto, msg.getContent());
        else
            throw new CodecException("UnsupportedTypeOfCodec");
    }

    public String toString() {
        return "(ContentManager:\n  - registered-ontologies = " + ontologies +
                "\n  - registered-languages = " +
                languages +
                ")";
    }

    public String[] getLanguageNames() {
        String[] langs = new String[languages.size()];
        int i = 0;
        for (Iterator<CaseInsensitiveString> it = languages.keySet().iterator(); it.hasNext(); i++) {
            langs[i] = it.next().toString();
        }
        return langs;
    }

    public String[] getOntologyNames() {
        String[] onts = new String[ontologies.size()];
        int i = 0;
        for (Iterator<CaseInsensitiveString> it = ontologies.keySet().iterator(); it.hasNext(); i++) {
            onts[i] = it.next().toString();
        }
        return onts;
    }
}

