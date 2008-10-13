package org.wordcorr.gui.input;

import java.beans.*;
import java.text.*;
import java.util.*;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.Messages;

/**
 * InputTable is a table with input rows.
 * @author Keith Hamasaki
 **/
public class InputTable extends Table {

    // force pr format
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("M/d/yyyy");
    public static final DateFormat TIME_FORMAT = new SimpleDateFormat("h:mm a");

    static {
        DATE_FORMAT.setLenient(false);
        TIME_FORMAT.setLenient(false);
    }

    public InputTable() {
        Messages messages = AppPrefs.getInstance().getMessages();
        _dateFormat = new SimpleDateFormat(messages.getString("fmtDate"));
        _timeFormat = new SimpleDateFormat(messages.getString("fmtTime"));
    }

    /**
     * Return the values set in this table, with basic validation.
     **/
    public Properties getValues() {
        Properties p = new Properties();
        for (Iterator it = _fields.iterator(); it.hasNext(); ) {
            InputRow row = (InputRow) it.next();
            if (!row.validate()) {
                return null;
            }
            p.setProperty(row.getName(), String.valueOf(row.getValue()));
        }
        return p;
    }

    /**
     * Validate this table.
     **/
    public boolean validateFields() {
        for (Iterator it = _fields.iterator(); it.hasNext(); ) {
            InputRow row = (InputRow) it.next();
            if (!row.validate()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Initialize this table with the given values.
     **/
    public void setValues(Properties p) {
        for (Iterator it = _fields.iterator(); it.hasNext(); ) {
            InputRow row = (InputRow) it.next();
            String def = ((p.size() == 0)? "": String.valueOf(row.getValue())); // FIXME: don't tell //
            row.setValue(p.getProperty(row.getName(), def));
        }
    }

    /**
     * Initialize this table with the values from the given bean.
     **/
    public void setValues(Object obj) {
        try {
            BeanInfo info = Introspector.getBeanInfo(obj.getClass(), Object.class);
            PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
            for (int i = 0; i < descriptors.length; i++) {
                InputRow row = (InputRow) _fieldMap.get(descriptors[i].getName());
                if (row != null) {
                    java.lang.reflect.Method method = descriptors[i].getReadMethod();
                    if (method != null) {
                        Object val = method.invoke(obj, null);
                        if (val != null) {
                            row.setValue(val);
                        }
                        row.clearDirty();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Add an input row to this table.
     **/
    public void addRow(InputRow row) {
        _fields.add(row);
        _fieldMap.put(row.getName(), row);
        addRow(row.getComponents(), row.getRowWeight());
    }

    /**
     * Get an input row from this table.
     **/
    public InputRow getRow(String name) {
    	for (Iterator it = _fields.iterator(); it.hasNext();) {
    		InputRow row = (InputRow)it.next();
    		if (row.getName().equals(name)) {
    			return row;
    		}
    	}
    	return null;
    }

    // validation methods
    private boolean empty(InputRow row) {
        return row.getValue() == null || String.valueOf(row.getValue()).trim().equals("");
    }

    private boolean checkRequired(InputRow row) {
        return !empty(row);
    }

    private boolean checkInteger(InputRow row) {
        if (empty(row)) return true;

        try {
            Long.parseLong(String.valueOf(row.getValue()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkFloat(InputRow row) {
        if (empty(row)) return true;

        try {
            Double.parseDouble(String.valueOf(row.getValue()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkDate(InputRow row) {
        if (empty(row)) return true;

        try {
            Date d = DATE_FORMAT.parse(String.valueOf(row.getValue()));
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            if (cal.get(Calendar.YEAR) >= 10000) {
                return false;
            }
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean checkTime(InputRow row) {
        if (empty(row)) return true;

        try {
            TIME_FORMAT.parse(String.valueOf(row.getValue()));
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private final List _fields = new LinkedList();
    private final Map _fieldMap = new HashMap();
    private final DateFormat _dateFormat;
    private final DateFormat _timeFormat;
}
