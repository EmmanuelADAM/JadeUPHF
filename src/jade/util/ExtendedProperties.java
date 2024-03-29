/*
 * (c) Copyright Hewlett-Packard Company 2001
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE and no warranty
 * that the program does not infringe the Intellectual Property rights of
 * a third party.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 */

package jade.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Provides enhanced property management.<br>
 * 1) Allows specifying property values containing strings of the form <pre>${key}</pre>.
 * Such strings are properly replaced with the value of the <pre>key</pre> variable
 * that may represent another property, a system property or an environment variable.
 * <p>
 * 2) Allows specifying boolean properties in the form <pre>-key</pre>. Such format is
 * equivalent to <pre>key=true</pre>.
 * <p>
 * 3) Allows importing properties from external property files by means of the special key
 * <pre>import</pre>. E.g. specifying the property<br>
 * <pre>import = a-property-file-name</pre><br>
 * results in automatically adding all properties defined in the specified property file
 * <p>
 * 4) Allows declaring a property as read-only.
 * In order to do that it is sufficient to end its key with a '!'. For example:
 * <pre>
 *      agentClass!=com.hp.agent.Foo
 * </pre>
 * The value of the property can be accessed including the '!' in the key or not indifferently
 * <p>
 * This class is designed to be usable in the restrictive J2ME CDC environment.
 *
 * @author Dick Cowan - HP Labs
 */
public class ExtendedProperties extends Properties {
    public static final String IMPORT_KEY = "import";
    private final Logger logger = Logger.getMyLogger(getClass().getName());
    boolean CRState = false;
    Hashtable<String, String> keyNames = new Hashtable<>();  // for detecting circular definitions
    Vector<String> sortVector = null;   // only used by sortedKeys

    /**
     * Construct empty property collection.
     */
    public ExtendedProperties() {
    }

    /**
     * Construct a ExtendedProperties object from an array of stringified properties of the form
     * <key>=<value>.
     *
     * @param propesStr The applications original arguments.
     */
    public ExtendedProperties(String[] propesStr) {
        this();
        addProperties(propesStr);
    }

    /**
     * For testing. Simply pass command line arguments to constructor then display
     * all key=value pairs using sorted enumeration.
     */
    public static void main(String[] args) {
        ExtendedProperties prop = new ExtendedProperties(args);
        prop.list(System.out);
    }

    /**
     * Add properties from a specified InputStream. Properties
     * will be added to any existing collection.
     *
     * @param inStream InputStream
     * @throws IOException if anything goes wrong.
     */
    public synchronized void load(InputStream inStream) throws IOException {
        addFromReader(new InputStreamReader(inStream, StandardCharsets.ISO_8859_1));
    }

    /**
     * Writes this property collection to the output stream in a format suitable for
     * loading into a Properties table using the load method.
     *
     * @param out    An output stream.
     * @param header A description of the property list - may be null.
     * @throws IOException if anything goes wrong.
     */
    public synchronized void store(OutputStream out, String header) throws IOException {
        String lineSeparator = System.getProperty("line.separator");
        Writer writer = new OutputStreamWriter(out, StandardCharsets.ISO_8859_1);
        if (header != null) {
            writer.write("#" + header);
            writer.write(lineSeparator);
        }
        writer.write("#" + new Date());
        writer.write(lineSeparator);
        for (Enumeration<Object> e = sortedKeys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            Object data = super.get(key);
            if (data != null) {
                writer.write(key + "=" + data);
                writer.write(lineSeparator);
            }
        }
        writer.flush();
    }

    /**
     * Return a sorted enumeration of this properties keys.
     *
     * @return Enumeration Sorted enumeration.
     */
    public synchronized Enumeration<Object> sortedKeys() {
        if (sortVector == null) {
            sortVector = new Vector<>();
        } else {
            sortVector.removeAllElements();
        }
        for (Enumeration<Object> e = super.keys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            int i = 0;
            while (i < sortVector.size()) {
                if (key.compareTo(sortVector.elementAt(i)) < 0) {
                    break;
                }
                i++;
            }
            sortVector.insertElementAt(key, i);
        }
        return new Enumeration<>() {
            final Enumeration<String> en = ExtendedProperties.this.sortVector.elements();

            public boolean hasMoreElements() {
                return en.hasMoreElements();
            }

            public Object nextElement() {
                return en.nextElement();
            }

        };
    }


    /**
     * Add to this Properties object the stringified properties included in a given array.
     *
     * @param propsStr The array of stringified properties. If null, this method does nothing.
     */
    public synchronized void addProperties(String[] propsStr) {
        if (propsStr != null) {
            for (String s : propsStr) {
                addProperty(s);
            }
        }
    }

    /**
     * Add to this Properties object a stringified property of the form
     * <pre>key = value</pre> or <pre>-key</pre>
     *
     * @param propStr The string representation of the property to be parsed
     */
    protected void addProperty(String propStr) {
        if (propStr != null) {
            propStr.trim();

            String key = null;
            String value = null;
            int separatorIndex = getSeparatorIndex(propStr);    // key=value or key:value
            if (separatorIndex > 0) {
                key = propStr.substring(0, separatorIndex).trim();
                if (key.length() == 0) {
                    // Case:   =value  (Not permitted)
                    throw new PropertiesException("Unable to identify key part in property: " + propStr);
                }
                if (separatorIndex == propStr.length() - 1) {
                    // Case:   key=
                    value = null;
                } else {
                    // Case:   key=value
                    value = propStr.substring(separatorIndex + 1).trim();
                }
            } else if ((propStr.length() > 1) && (propStr.startsWith("-"))) {
                // Case: -key
                key = propStr.substring(1);
                value = "true";
            } else {
                // Case: xyz  (Not permitted)
                throw new PropertiesException("Wrong property format: " + propStr);
            }
            setProperty(key, value);
        } else {
            throw new PropertiesException("Null property format");
        }
    }

    /**
     * Retrieve the position of the first valid key-value separator character ('=' or ':') in
     * a stringified property.
     *
     * @param propStr The stringified property.
     */
    protected int getSeparatorIndex(String propStr) {
        int idxA = propStr.indexOf('=');
        int idxB = propStr.indexOf(':');

        if (idxA == -1)  // key:value
            return idxB;
        if (idxB == -1)  // key=value
            return idxA;
        // key=value with :
        // key:value with =
        return Math.min(idxA, idxB);
    }

    /**
     * Copy a data from standard Properties.
     *
     * @param source The properties to copy from.
     */
    public synchronized void copyProperties(ExtendedProperties source) {
        for (Enumeration<?> e = source.keys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            super.put(key, source.getRawProperty(key));
        }
    }

    /**
     * Create a new Properties object by coping those
     * properties whose key begins with a particular prefix string.
     * The prefix is removed from the keys inserted into the extracted Properties object
     *
     * @param prefix The prefix string. Ex: "server."
     */
    public synchronized ExtendedProperties extractSubset(String prefix) {
        ExtendedProperties result = new ExtendedProperties();
        for (Enumeration<Object> e = super.keys(); e.hasMoreElements(); ) {
            String originalKey = (String) e.nextElement();
            String newKey = null;

            if (originalKey.startsWith(prefix)) {
                newKey = originalKey.substring(prefix.length());
                result.setProperty(newKey, getRawProperty(originalKey));
            }
        }

        return result;
    }

    /**
     * Get the object associated with a key. Note that, unlike getProperty(), this method does not
     * perform substitutions of variables of the form ${x} in property values since such values may
     * be non-string objects.
     *
     * @param aKey Key for desired property.
     * @return The object associated with this key or null if none exits.
     */
    public Object get(Object aKey) {
        Object value = null;
        if (aKey instanceof String strKey) {
            // Try WITHOUT the '!' for sure
            value = super.get(strKey);
            if (value == null) {
                // Not found --> Try WITH the '!' for sure
                value = super.get(strKey + "!");
            }

            if (value instanceof String strValue) {
                // This synchronized block prevents a "Circular argument substitution key" error in case two threads
                // search for the same key in parallel
                synchronized (keyNames) {
                    if (keyNames.put(strKey, "x") != null) {  // value doesn't matter
                        throw new PropertiesException("Circular argument substitution with key: " + aKey);
                    }
                    if (strValue.length() >= 4) {    // shortest possible value: ${x}
                        strValue = doSubstitutions(strValue);
                    }
                    keyNames.remove(strKey);
                }
                strValue = valueFilter(strKey, strValue);
                value = strValue;
            }
        } else {
            value = super.get(aKey);
        }
        return value;
    }

    /**
     * Set property value to specified object.
     *
     * @param aKey   The key used to store the data. The key may contain strings of
     *               the form <b><pre>${key}</pre></b> which will be evaluated first.
     * @param aValue The object to be stored.
     * @return The previous value of the specified key, or null if it did not have one.
     */
    public Object put(Object aKey, Object aValue) {
        if (aKey instanceof String) {
            // Substitutions on keys are done at insertion time
            String actualKey = doSubstitutions((String) aKey);

            // aKey may have the form kkk or kkk!. In both cases if a property with key = kkk! exists throws an exception
            if (super.containsKey(actualKey + "!")) {
                throw new PropertiesException("Attempt to alter read only property:" + actualKey);
            }

            // If the key to be inserted is the "import" key, do not insert it, but add all properties
            // defined in the indicated import file
            if (actualKey.equals(IMPORT_KEY) && aValue != null) {
                String importFile = doSubstitutions(aValue.toString());
                try {
                    // Try in the classpath first
                    InputStream stream = getClass().getClassLoader().getResourceAsStream(importFile);
                    if (stream == null) {
                        // Not found: try in the file system
                        stream = new FileInputStream(importFile);
                    }
                    load(stream);
                } catch (IOException ioe) {
                    logger.log(Logger.WARNING, "Cannot import properties from import-file " + importFile);
                }
                return null;
            } else {
                return super.put(actualKey, aValue);
            }
        } else {
            return super.put(aKey, aValue);
        }
    }

    /**
     * Override getProperty in base class so all occurances of
     * the form <b><pre>${key}</pre></b> are replaced by their
     * associated value.
     *
     * @param aKey Key for desired property.
     * @return The keys value with substitutions done.
     */
    public String getProperty(String aKey) {
        return getProperty(aKey, null);
    }

    /**
     * Perform substitution when a value is fetched. Traps circular definitions,
     * and calls valueFilter with value prior to returning it.
     *
     * @param aKey         The property key.
     * @param defaultValue Value to return if property not defined. May be null.
     *                     If non null it will be passes to valueFilter first.
     * @return The resultant value - could be null or empty.
     * @throws PropertiesException if circular definition.
     */
    public String getProperty(String aKey, String defaultValue) {
        Object value = get(aKey);
        if (value != null) {
            return value.toString();
        } else {
            return defaultValue;
        }
    }

    /**
     * Set property value. If value is null the property (key and value) will be removed.
     * @param aKey The key used to store the data. The key may contain strings of
     * the form <b><pre>${key}</pre></b> which will be evaluated first.
     * @param aValue The value to be stored, if null they property will be removed.
     * @return The previous value of the specified key, or null if it did not have one.
     */
	/*public Object setProperty(String aKey, String aValue) {
		String actualKey = doSubstitutions(aKey);

		String testKey = (actualKey.endsWith("!")) ? actualKey.substring(0, actualKey.length()) : actualKey;
		if (super.containsKey(testKey + "!")) {
			throw new PropertiesException("Attempt to alter read only property:" + testKey);
		}

		if (aValue == null) {
			return super.remove(actualKey);
		} else {
			return super.put(actualKey, aValue);
		}
	}*/

    /**
     * Set property value only if its not set already.
     *
     * @param aKey  The key used to store the data. The key may contain strings of
     *              the form <b><pre>${key}</pre></b> which will be evaluated first.
     * @param value The value to be stored.
     * @return Null if store was done, non-null indicates store not done and the
     * returned value in the current properties value.
     */
    public Object setPropertyIfNot(String aKey, String value) {
        String current = getProperty(aKey);
        if (current == null) {
            return setProperty(aKey, value);
        }
        return current;
    }

    /**
     * Fetch property value for key which may contain strings
     * of the form <b><pre>${key}</pre></b>.
     *
     * @param aKey Key for desired property.
     * @return The keys value with no substitutions done.
     */
    public String getRawProperty(String aKey) {
        Object data = super.get(aKey);
        return (data != null) ? data.toString() : null;
    }

    /**
     * Use this method to fetch a property ignoring case of key.
     *
     * @param aKey The key of the environment property.
     * @return The key's value or null if not found.
     */
    public String getPropertyIgnoreCase(String aKey) {
        for (Enumeration<Object> e = super.keys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();

            if (aKey.equalsIgnoreCase(key)) {
                return getProperty(key);
            }
        }
        return null;
    }


    /**
     * Called by getProperty(key, default) to perform any post processing of the
     * value string. By default, this method provides special processing on the value
     * associated with any property whose key name has the string "path" as part of it
     * (ex: "classpath", "sourcepath", "mypath"). When the value for such keys is fetched
     * any occurance of '|' will be converted to a ':' on Unix systems and a ';' on
     * Windows systems. Therefore to increase the direct reuse of your property files,
     * always use a '|' as a separator and always assign a key name which has "path" as
     * part of it.
     *
     * @param key   The properties key.
     * @param value The properties value.
     * @return String New potentially altered value.
     */
    protected String valueFilter(String key, String value) {
        if (key.toLowerCase().contains("path")) {    // convert separators to be correct for this system
            String correctSeparator = System.getProperty("path.separator");

            if (correctSeparator.equals(";")) {
                value = value.replace('|', ';');
            } else {
                value = value.replace('|', ':');
            }
        }
        return value;
    }

    /**
     * Extract a string value and convert it to an integer.
     * If there isn't one or there is a problem with the conversion,
     * return the default value.
     *
     * @param aKey          The key which will be used to fetch the attribute.
     * @param aDefaultValue Specifies the default value for the int.
     * @return int The result.
     */
    public int getIntProperty(String aKey, int aDefaultValue) {
        int result = aDefaultValue;

        try {
            result = Integer.parseInt(getProperty(aKey));
        } catch (Exception e) {
        }

        return result;
    }

    /**
     * Store an int as a string with the specified key.
     *
     * @param aKey   The key which will be used to store the attribute.
     * @param aValue The int value.
     */
    public int setIntProperty(String aKey, int aValue) {
        setProperty(aKey, Integer.toString(aValue));
        return aValue;
    }

    /**
     * Extract a string value ("true" or "false") and convert it to
     * a boolean. If there isn't one or there is a problem with the
     * conversion, return the default value.
     *
     * @param aKey          The key which will be used to fetch the attribute.
     * @param aDefaultValue Specifies the default value for the boolean.
     * @return boolean The result.
     */
    public boolean getBooleanProperty(String aKey, boolean aDefaultValue) {
        boolean result = aDefaultValue;

        try {
            String value = getProperty(aKey);

            result = value.equalsIgnoreCase("true");
        } catch (Exception e) {
        }

        return result;
    }

    /**
     * Store a boolean as a string ("true" or "false") with the specified key.
     *
     * @param aKey   The key which will be used to store the attribute.
     * @param aValue The boolean value.
     */
    public void setBooleanProperty(String aKey, boolean aValue) {
        setProperty(aKey, (aValue) ? "true" : "false");
    }

    /**
     * Change key string associated with existing value.
     *
     * @param existingKey The current key.
     * @param newKey      The new key.
     * @return Non null is former value of object associated with new key.
     * Null indicates that either the existing key didn't exist or there
     * was no former value associated with the new key. i.e. null => success.
     */
    public Object renameKey(String existingKey, String newKey) {
        Object value = remove(doSubstitutions(existingKey));
        if (value != null) {
            return put(newKey, value);
        }
        return null;
    }

    /**
     * Replace all substrings of the form ${xxx} with the property value
     * using the key xxx. Calls doSubstitutions(anInputString, false).
     *
     * @param anInputString The input string - may be null.
     * @return The resultant line with all substitutions done or null if input string was.
     */
    public String doSubstitutions(String anInputString) {
        return doSubstitutions(anInputString, false);
    }

    /**
     * Replace all substrings of the form ${xxx} with the property value
     * using the key xxx. If the key is all caps then the property is
     * considered to be a system property.
     *
     * @param anInputString  The input string - may be null.
     * @param allowUndefined If true, undefined strings will remain as is,
     *                       if false, an exception will be thrown.
     * @return The resultant line with all substitutions done or null if input string was.
     */
    public String doSubstitutions(String anInputString, boolean allowUndefined) {
        if (anInputString == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        int si = 0;    // source index
        int oi = 0;    // opening index
        int ci = 0;    // closing index

        do {
            oi = anInputString.indexOf("${", si);
            ci = anInputString.indexOf('}', si);

            if (oi > si) {    // xxxxxx${key}
                result.append(anInputString, si, oi);

                si = oi;
            }

            if ((oi == si) && (ci > oi + 2)) {    // ${key}xxxxx
                String key = anInputString.substring(oi + 2, ci);

                // Try as another property first (this allows the user to override system or environment setting)
                String value = getProperty(key, null);

                if ((value == null)) {
                    // Not found --> Try as a System property (java -D....)
                    value = System.getProperty(key);

                    //#J2ME_EXCLUDE_BEGIN
                    if (value == null) {
                        // Not found --> Try as an environment variable
                        value = System.getenv(key);
                    }
                    //#J2ME_EXCLUDE_END
                }

                if (value == null) {
                    if (allowUndefined) {
                        value = "${" + key + "}";
                    } else {
                        throw new PropertiesException("Unable to get property value for key: " + key);
                    }
                }

                if (oi > si) {
                    result.append(anInputString, si, oi);
                }

                result.append(value);

                si = ci + 1;
            } else {
                if (oi == -1) {    // xxxxxxxxx
                    result.append(anInputString.substring(si));

                    si = anInputString.length();
                } else {    // xxxxxx${xxxxxx
                    result.append(anInputString, si, oi + 2);

                    si = oi + 2;
                }
            }
        } while (si < anInputString.length());

        return result.toString();
    }

    /**
     * Add properties from Reader. Explicitly handled so as to enable
     * handling of import=<file> directive. Blank lines as well as
     * those beginning with a '#' character (comments) are ignored.
     *
     * @param reader The buffered reader to read from.
     *               to catch circular imports.
     * @throws IOException if anything goes wrong.
     */
    protected void addFromReader(Reader reader) throws IOException {
        String line = null;
        do {
            line = getOneLine(reader);

            if (line != null) {
                line = line.trim();

                if (line.length() == 0) {
                    continue;    // empty line
                }

                if (line.startsWith("#") || line.startsWith("!")) {
                    continue;    // comment line
                }

                addProperty(line);
            }
        } while (line != null);
    }

    /**
     * Get a logical line. Any physical line ending in '\' is considered
     * to continue on the next line.
     *
     * @param reader The input reader to read.
     * @return The resultant logical line which may have been constructed
     * from one or more physical lines.
     * @throws IOException if anything goes wrong.
     */
    protected String getOneLine(Reader reader) throws IOException {
        StringBuilder sb = null;
        String line = null;
        boolean continued;

        do {
            continued = false;

            try {
                line = readLine(reader);

                if (line != null) {
                    line = line.trim();
                    // If we already have something going ignore blank lines and comments
                    if ((sb != null) && ((line.length() == 0) || (line.startsWith("#") || line.startsWith("!")))) {
                        continued = true;
                        continue;
                    }

                    continued = line.endsWith("\\");

                    if (continued) {    // delete the ending slash
                        line = line.substring(0, line.length() - 1);
                    }
                    if (sb == null) {
                        sb = new StringBuilder();
                    }
                    sb.append(line);
                }
            } catch (EOFException eof) {
                continued = false;
            }
        } while (continued);

        return (sb == null) ? null : sb.toString();
    }

    /**
     * Read one line from the Reader. A line may be terminated
     * by a single CR or LF, or the pair CR LF.
     *
     * @param aReader The Reader to read characters from.
     * @return Next physical line.
     * @throws IOException if anything goes wrong.
     */
    protected String readLine(Reader aReader) throws IOException {
        StringBuilder sb = new StringBuilder();

        while (true) {
            int result = aReader.read();
            if (result == -1) {
                if (sb.length() > 0) {
                    break;
                }
                throw new EOFException();
            } else {
                char ch = (char) result;
                if (ch == '\n') {  // LF
                    if (CRState) {
                        CRState = false;
                        continue;
                    }
                    break;
                } else {
                    if (ch == '\r') {
                        CRState = true;
                        break;
                    } else {
                        sb.append(ch);
                        CRState = false;
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * List properties to provided PrintStream.
     * Output will be in sorted key sequence.
     * If a value is null, it will appear as "key=".
     *
     * @param out The print stream.
     */
    public void list(PrintStream out) {
        for (Enumeration<Object> e = sortedKeys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            String value = getProperty(key);
            if (value != null) {
                out.println(key + "=" + value);
            } else {
                out.println(key + "=");
            }
        }
    }

    /**
     * Create a String[] for the properties with one key=value pair per array entry.
     * If a value is null, it will appear as "key=".
     *
     * @return The resultant String[].
     */
    public String[] toStringArray() {
        String[] result = new String[super.size()];
        int i = 0;
        for (Enumeration<Object> e = sortedKeys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            String value = getProperty(key);
            if (value != null) {
                result[i++] = key + "=" + value;
            } else {
                result[i++] = key + "=";
            }
        }
        return result;
    }

}
