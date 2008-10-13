package org.wordcorr.db;

import javax.xml.soap.*;

import java.sql.*;
import java.util.*;

/**
 * Sets parameters for database operations.
 * @author Jim Shiba
 **/
public class StatementParameters {
	
    /**
     * Constructor.
     **/
	public StatementParameters () {
		this(null, null, null);
	}

    /**
     * Constructor.
     **/
	public StatementParameters (String table, String action, List data) {
		_table = table;
		_action = action;
		_data = data;
        _parameters = getDefinitionParameters(_table, _action);
	}

    /**
     * Get definition parameters.
     **/
    private List getDefinitionParameters(String table, String action) {
    	if (table == null || action == null)
    		return null;
    		
    	// get definition for table
		ParameterDefinitions.Definition defn = ParameterDefinitions.getInstance().
			getDefinition(table);
		
		if (defn != null) {
			// get action definitions
			return defn.getParameters(action);
		} else {
			System.out.println("StatementParameters.getDefinitionParameters: Definition is NULL");
			return null;
		}
    }

	/**
     * Get action.
     **/
	public String getAction() {
		return _action;
	}

	/**
     * Get table.
     **/
	public String getTable() {
		return _table;
	}

	/**
     * Check if another value exists.
     **/
	public boolean hasNext() {
		if (_data == null || _data.isEmpty() || _parameters == null)
			return false;
		return (_index < _data.size());
	}

	/**
     * Set statement parameters for next record.
     **/
	public RowData setNext(PreparedStatement stmt) throws SQLException {
		// initialize
		stmt.clearParameters();
		
		// get row data
		RowData rowData = (RowData)_data.get(_index++);
		Map row = rowData.getData();
		for (Iterator it = _parameters.iterator(); it.hasNext();) {
			ParameterDefinitions.ActionParameter parameter =
				(ParameterDefinitions.ActionParameter)it.next();
			switch (parameter.getType()) {
				case ParameterDefinitions.STRING:
					stmt.setString(parameter.getOrder(),
						(String)row.get(parameter.getID()));
					break; 
				case ParameterDefinitions.LONG:
					stmt.setLong(parameter.getOrder(),
						Long.parseLong((String)row.get(parameter.getID())));
					break; 
				case ParameterDefinitions.TIMESTAMP:
					stmt.setTimestamp(parameter.getOrder(),
						new java.sql.Timestamp(
						Long.parseLong((String)row.get(parameter.getID()))));
					break; 
				default:
					break;
			}
		}
		return rowData;
	}

    /**
     * Set statement parameters from SOAPMessage.
     **/
    public void setParameters(SOAPMessage msg) {
		_data = new ArrayList();
    	try {
            SOAPPart soapPart = msg.getSOAPPart();
            SOAPEnvelope envelope = soapPart.getEnvelope();
            SOAPBody body = envelope.getBody();
            
            for (Iterator it = body.getChildElements(); it.hasNext();) {
	            SOAPBodyElement bodyElem = (SOAPBodyElement)it.next();
	            
	            // get table data
	            Name name = bodyElem.getElementName();
	            if (name.getPrefix().equals("m")) {
	            	_action = name.getLocalName();
	            	_table = bodyElem.getAttributeValue(envelope.createName("table"));
	            	System.out.println("StatementParameters.setParameters: table="+_table);
		            for (Iterator it2 = bodyElem.getChildElements(); it2.hasNext();) {
		            	SOAPElement rowElem = (SOAPElement)it2.next();
		            	
		            	// create RowData object and add to list
	 		           	RowData row = new RowData();
	 		           	_data.add(row);
	 		           	HashMap data = new HashMap();
	 		           	row.setData(data);
			            for (Iterator it3 = rowElem.getChildElements(); it3.hasNext();) {
			            	SOAPElement dataElem = (SOAPElement)it3.next();
			            	
			            	// get row properties
			            	data.put(dataElem.getElementName().getLocalName(), dataElem.getValue());
		 		           	System.out.println("StatementParameters.setParameters: "+dataElem.getElementName().getLocalName()+"="+dataElem.getValue());
			            }
			            // save id for result return
			            if (data.containsKey("id"))
			            	row.setID(Long.parseLong((String)data.get("id")));
		            }
	            }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        _parameters = getDefinitionParameters(_table, _action);
    }
    
	private int _index = 0;
	private List _parameters;
	private List _data;
	private String _action;
	private String _table;
}

