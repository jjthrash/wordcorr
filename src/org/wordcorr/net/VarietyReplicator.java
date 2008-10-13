package org.wordcorr.net;

import javax.xml.soap.*;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.wordcorr.db.Database;
import org.wordcorr.db.DatabaseException;
import org.wordcorr.db.RetrieveAllParameters;
import org.wordcorr.db.StatementParameters;
import org.wordcorr.gui.MainFrame;

/**
 * Replicator for variety table.
 * @author Jim Shiba
 **/
public class VarietyReplicator extends Replicator {

    public VarietyReplicator(long id, URL endpoint,
    	MessageFactory msgFactory, SOAPConnectionFactory scFactory) {
    	super(id, endpoint, msgFactory, scFactory);
    }

    //---------------------------------------------------------------//
    // Replication Methods
    //---------------------------------------------------------------//

    /**
     * Send new records.
     **/
    public void addNew() {
    	try {
            SOAPConnection con = _scFactory.createConnection();
            SOAPMessage message = _msgFactory.createMessage();

			// setup message
            SOAPPart soapPart = message.getSOAPPart();
            final SOAPEnvelope envelope = soapPart.getEnvelope();
            SOAPHeader header = envelope.getHeader();
            SOAPBody body = envelope.getBody();
            header.detachNode();
            Name name = envelope.createName("AddNew", "m", _endpoint.getHost());
            final SOAPBodyElement bodyElem = body.addBodyElement(name);
            bodyElem.addAttribute(envelope.createName("table"), "Variety");
            final Name rowName = envelope.createName("row");
            
            // populate message
            MainFrame.getInstance().getDatabase().
            	retrieveObjects(new RetrieveAllParameters() {
                public String getRetrieveAllSQLKey() {
                    return "GET_NEW_VARIETIES";
                }

                public void setRetrieveAllParameters(PreparedStatement stmt)
                    throws SQLException
                {
                    stmt.setLong(1, _id);
                }

                public Object createObject(Database db, ResultSet rs)
                    throws SQLException
                {
                    try {
                    	SOAPElement rowElem = bodyElem.addChildElement(rowName);
                    	SOAPElement dataElem =
                    		rowElem.addChildElement(envelope.createName("id"));
                    	dataElem.addTextNode("" + rs.getLong(1));
                    	dataElem = rowElem.addChildElement(envelope.createName("name"));
                    	dataElem.addTextNode(noNull(rs.getString(2)));
                    	dataElem = rowElem.addChildElement(envelope.createName("shortName"));
                    	dataElem.addTextNode(noNull(rs.getString(3)));
                    	dataElem = rowElem.addChildElement(envelope.createName("ethnologueCode"));
                    	dataElem.addTextNode(noNull(rs.getString(4)));
                    	dataElem = rowElem.addChildElement(envelope.createName("quality"));
                    	dataElem.addTextNode(noNull(rs.getString(5)));
                    	dataElem = rowElem.addChildElement(envelope.createName("locale"));
                    	dataElem.addTextNode(noNull(rs.getString(6)));
                    	dataElem = rowElem.addChildElement(envelope.createName("source"));
                    	dataElem.addTextNode(noNull(rs.getString(7)));
                    	dataElem = rowElem.addChildElement(envelope.createName("abbreviation"));
                    	dataElem.addTextNode(noNull(rs.getString(8)));
                    	dataElem = rowElem.addChildElement(envelope.createName("collectionKey"));
                    	dataElem.addTextNode("" + rs.getLong(9));
                        return null;
                    } catch (SQLException e) {
                    	e.printStackTrace();
                    	throw e;
                    	
                    } catch (SOAPException e) {
                    	e.printStackTrace();
                   	} finally {
                   		return null;
                    }
 
                }
                private String noNull(String val) {
                	return (val == null) ? "" : val;
                }
            });
            // check for new records
            if (!bodyElem.getChildElements().hasNext()) {
            	con.close();
            	return;
            }
            
            // send message
            SOAPMessage response = con.call(message, _endpoint);
            con.close();
            
            // process results
        	StatementParameters parameters = new StatementParameters();
        	parameters.setParameters(response);
        	MainFrame.getInstance().getDatabase().updateRecords(parameters);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
}
