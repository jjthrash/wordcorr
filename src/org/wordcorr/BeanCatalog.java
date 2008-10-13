package org.wordcorr;

import java.io.*;
import java.util.*;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.wordcorr.gui.input.BasicOption;
import org.wordcorr.gui.input.InputRow;
import org.wordcorr.gui.input.Type;

/**
 * Catalog of JavaBeans.
 * @author Keith Hamasaki
 **/
public final class BeanCatalog {

    private static final BeanCatalog _instance = new BeanCatalog();

    public static BeanCatalog getInstance() {
        return _instance;
    }

    private BeanCatalog() {
        InputStream in = getClass().getResourceAsStream("/beans.xml");
        try {
            Document doc = new SAXBuilder().build(in);
            for (Iterator it = doc.getRootElement().getChildren("bean").iterator();
                 it.hasNext(); )
            {
                Element elt = (Element) it.next();
                Bean bean = new Bean(elt);
                _beans.put(bean.getID(), bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the Bean instance for the given bean class.
     **/
    public Bean getBean(Class cl) {
        String clname = cl.getName();
        int index = clname.lastIndexOf(".");
        if (index != -1) {
            clname = clname.substring(index + 1);
        }
        return getBean(clname);
    }

    /**
     * Get the given bean.
     * @return The bean if it exists, otherwise null.
     **/
    public Bean getBean(String id) {
        return (Bean) _beans.get(id);
    }

    /**
     * Get all of the beans.
     **/
    public Collection getBeans() {
        return Collections.unmodifiableCollection(_beans.values());
    }

    /**
     * A Bean.
     **/
    public static final class Bean {
        private Bean(Element elt) {
            _id = elt.getAttributeValue("id");
            for (Iterator it = elt.getChildren("property").iterator();
                 it.hasNext(); )
            {
                Element propElt = (Element) it.next();
                _properties.add(new Property(propElt));
            }
        }

        /**
         * Get the ID for this bean.
         **/
        public String getID() {
            return _id;
        }

        /**
         * Get the properties for this bean.
         **/
        public List getProperties() {
            return Collections.unmodifiableList(_properties);
        }

        private final String _id;
        private final List _properties = new ArrayList();
    }

    /**
     * A Bean property.
     **/
    public static final class Property {

        private Property(Element elt) {
            _id = elt.getAttributeValue("id");
            _editorType = elt.getAttributeValue("editor-type");
            _labelKey = elt.getChildText("label-key");
            _mnemonicKey = elt.getChildText("mnemonic-key");
            _requiredMessageKey = elt.getChildText("required-message-key");
            _content = elt.getChildText("content");
            _defValue = elt.getChildText("default-value");
            _columns = getInt(elt, "columns", 0);
            _maxlength = getInt(elt, "max-length", -1);
            _minvalue = getDouble(elt, "min-value", -Double.MAX_VALUE);
            _maxvalue = getDouble(elt, "max-value", Double.MAX_VALUE);
            _rows = getInt(elt, "rows", -1);
            _editorClass = elt.getChildText("editor-class");
            for (Iterator it = elt.getChildren("option").iterator();
                 it.hasNext(); )
            {
                Element optElt = (Element) it.next();
                _options.add(new BasicOption(optElt.getChildText("name"),
                                 optElt.getChildText("value")));
            }
            String dtype = elt.getAttributeValue("data-type");
            _type = dtype == null ? Type.TEXT : Type.getType(dtype);
            _required = "true".equals(elt.getAttributeValue("required"));
            _nospace = "true".equals(elt.getAttributeValue("nospace"));
            _fontKey = elt.getChildText("font-key");
        }

        /**
         * Get the ID for this property.
         **/
        public String getID() {
            return _id;
        }

        /**
         * Get the editor class.
         **/
        public String getEditorType() {
            return _editorType;
        }

        /**
         * Get the type of this property.
         **/
        public Type getType() {
            return _type;
        }

        /**
         * Is this property required?
         **/
        public boolean isRequired() {
            return _required;
        }

        /**
         * Are leading spaces disallowed?
         **/
        public boolean isNospace() {
            return _nospace;
        }

        /**
         * Get the label key.
         **/
        public String getLabelKey() {
            return _labelKey;
        }

        /**
         * Get the mnemonic key, if any.
         * @return The mnemonic key if it exists, otherwise null.
         **/
        public String getMnemonicKey() {
            return _mnemonicKey;
        }

        /**
         * Get the required message key, if any.
         * @return The required message key if it exists, otherwise null.
         **/
        public String getRequiredMessageKey() {
            return _requiredMessageKey;
        }

        /**
         * Get the content, if any.
         * @return The content if it exists, otherwise null.
         **/
        public String getContent() {
            return _content;
        }

        /**
         * Get the default value, if any.
         * @return The default value if it exists, otherwise null.
         **/
        public String getDefault() {
            return _defValue;
        }

        /**
         * Get the number of columns, or 0 if there is none.
         **/
        public int getColumns() {
            return _columns;
        }

        /**
         * Get the max length, or -1 if there is none.
         **/
        public int getMaxLength() {
            return _maxlength;
        }

        /**
         * Get the mininum value, or Double.MIN_VALUE if there is none.
         **/
        public double getMinValue() {
            return _minvalue;
        }

        /**
         * Get the maximum value, or Double.MAX_VALUE if there is none.
         **/
        public double getMaxValue() {
            return _maxvalue;
        }

        /**
         * Get the number of rows, or -1 if there is none.
         **/
        public int getNumRows() {
            return _rows;
        }

        /**
         * Get the custom editor class for this property, if there is one.
         **/
        public String getEditorClass() {
            return _editorClass;
        }

        /**
         * Get the options for this property.
         **/
        public List getOptions() {
            return Collections.unmodifiableList(_options);
        }
        
        public String getFontKey() {
            return _fontKey;
        }

        private static int getInt(Element elt, String key, int def) {
            String str = elt.getChildText(key);
            return str == null ? def : Integer.parseInt(str);
        }

        private static double getDouble(Element elt, String key, double def) {
            String str = elt.getChildText(key);
            return str == null ? def : Double.parseDouble(str);
        }

        private final String _id;
        private final String _editorType;
        private final String _labelKey;
        private final String _mnemonicKey;
        private final String _requiredMessageKey;
        private final String _content;
        private final String _defValue;
        private final int _columns;
        private final int _maxlength;
        private final double _minvalue;
        private final double _maxvalue;
        private final boolean _nospace;
        private final int _rows;
        private final boolean _required;
        private final Type _type;
        private final String _editorClass;
        private final List _options = new ArrayList();
        private final String _fontKey;
    }

    private Map _beans = new HashMap();
}
