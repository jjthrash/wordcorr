package org.wordcorr.db;

import java.io.*;
import java.util.*;
import org.jdom.*;
import org.jdom.input.SAXBuilder;

/**
 * Catalog of SQLStatement parameter definitions.
 * @author Jim Shiba
 **/
public final class ParameterDefinitions {

    private static final ParameterDefinitions _instance = new ParameterDefinitions();

    public static ParameterDefinitions getInstance() {
        return _instance;
    }

    private ParameterDefinitions() {
        InputStream in = getClass().getResourceAsStream("/sqldefinitions.xml");
        try {
    	    Document doc = new SAXBuilder().build(in);
            for (Iterator it = doc.getRootElement().getChildren("table").iterator();
                 it.hasNext(); )
            {
                Element elt = (Element) it.next();
                Definition definition = new Definition(elt);
                _definitions.put(definition.getID(), definition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the given definition.
     * @return The definition if it exists, otherwise null.
     **/
    public Definition getDefinition(String id) {
    	return (Definition) _definitions.get(id);
    }

    /**
     * Get all of the definitions.
     **/
    public Collection getDefinitions() {
        return Collections.unmodifiableCollection(_definitions.values());
    }

    /**
     * A Parameter definition.
     **/
    public static final class Definition {
        private Definition(Element elem) {
            _id = elem.getAttributeValue("id");
            for (Iterator it = elem.getChildren("action").iterator(); it.hasNext();) {
                Element actionElem = (Element)it.next();
                List parameters = new ArrayList();
                _parameters.put(actionElem.getAttributeValue("id"), parameters);
	            for (Iterator it2 = actionElem.getChildren("parameter").iterator(); it2.hasNext();) {
	                Element paramElem = (Element)it2.next();
	                parameters.add(new ActionParameter(paramElem));
	            }
            }
        }

        /**
         * Get the ID.
         **/
        public String getID() {
            return _id;
        }

        /**
         * Get the parameters.
         **/
        public List getParameters(String action) {
        	List params = (List)_parameters.get(action);
            return Collections.unmodifiableList(params);
        }

        private final String _id;
	    private Map _parameters = new HashMap();
    }

    /**
     * A parameter.
     **/
    public static final class ActionParameter {

        private ActionParameter(Element elem) {
            _id = elem.getAttributeValue("id");
            _order = Integer.parseInt(elem.getAttributeValue("order"));
            String type = elem.getAttributeValue("type");
            _type =
            	(type.equals("string")) ? STRING :
            	(type.equals("long")) ? LONG :
            	(type.equals("timestamp")) ? TIMESTAMP :
            	UNDEFINED;
        }

        /**
         * Get the ID for this parameter.
         **/
        public String getID() {
            return _id;
        }

        /**
         * Get the parameter order.
         **/
        public int getOrder() {
            return _order;
        }

        /**
         * Get the type of this parameter.
         **/
        public int getType() {
            return _type;
        }

        private final String _id;
        private final int _order;
        private final int _type;
    }

	public static final int LONG = 1;
	public static final int STRING = 2;
	public static final int TIMESTAMP = 3;
	public static final int UNDEFINED = -1;
    private Map _definitions = new HashMap();
}
