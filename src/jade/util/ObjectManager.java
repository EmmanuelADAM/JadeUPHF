package jade.util;

import jade.core.Specifier;

import java.util.*;

//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

/**
 * Utility class to manage instances of classes with attached properties i.e. classes specified in the form<br>
 * foo.Bar[key1=value1,key2=value2...]
 * This class allows registering "Loaders" for given types of objects (e.g. agents) and successively load such
 * objects using them
 */
public class ObjectManager {
    public static final String CLASS_NAME = "name";

    // Predefined object types
    /**
     * The constant representing the "agent" type
     */
    public static final String AGENT_TYPE = "agent";

    private static final Map<String, List<Loader>> loaders = new HashMap<>();

    /**
     * Convert a class-name, possibly with attached properties, into a Properties object
     * The actual class-name will be available as the value of the  CLASS_NAME   property
     */
    private static Properties getClassProperties(String str) {
        Properties pp = new Properties();
        int index = str.indexOf('[');
        if (index < 0) {
            pp.setProperty(CLASS_NAME, str);
        } else {
            pp.setProperty(CLASS_NAME, str.substring(0, index));
            int index1 = str.indexOf(']');
            String propsStr = str.substring(index + 1, index1);
            Vector<Object> propsList = Specifier.parseList(propsStr, ',');
            for (Object o : propsList) {
                String ps = (String) o;
                int k = ps.indexOf('=');
                if (k > 0) {
                    String name = ps.substring(0, k);
                    String value = ps.substring(k + 1);
                    pp.setProperty(name, value);
                }
            }
        }
        return pp;
    }

    /**
     * Register a  Loader   for a given type of object. Note that more than one  Loader  
     * can be associated to a given type of object.
     *
     * @param type   The type of object the registered  Loader   is associated to
     * @param loader The  Loader   instance.
     */
    public synchronized static void addLoader(String type, Loader loader) {
        List<Loader> l = loaders.computeIfAbsent(type, k -> new ArrayList<>());
        l.add(loader);
    }

    public synchronized static boolean removeLoader(String type, Loader loader) {
        List<?> l = loaders.get(type);
        if (l != null) {
            return l.remove(loader);
        }
        return false;
    }

    /**
     * Try to load an object of a given type by means of the loaders (if any) associated to that type.
     *
     * @param extendedClassName The class of the object to load in the form foo.Bar[key1=value1;key2=value2...]
     * @param type              The type of object to load
     * @return The loaded object or null if no loader is suitable to load the object.
     */
    public synchronized static Object load(String extendedClassName, String type) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (extendedClassName == null) {
            throw new IllegalArgumentException("Null class name");
        }
        List<Loader> l = loaders.get(type);
        if (l != null && !l.isEmpty()) {
            // If we have loaders for this type of object, try to use them
            Properties pp = getClassProperties(extendedClassName);
            String className = pp.getProperty(ObjectManager.CLASS_NAME);
            for (Loader loader : l) {
                Object obj = loader.load(className, pp);
                if (obj != null) {
                    return obj;
                }
            }
        }
        return null;
    }

    /**
     * The interface to be implemented by classes that can be registered to load objects of a given type
     */
    public interface Loader {
        Object load(String className, Properties pp) throws ClassNotFoundException, IllegalAccessException, InstantiationException;
    }
}
