package org.wordcorr.net;

import javax.xml.soap.*;

import java.util.*;

import org.wordcorr.db.Database;
import org.wordcorr.db.RowData;
import org.wordcorr.db.StatementParameters;

/**
 * Handler to add new records.
 * @author Jim Shiba
 */
public class AddNewHandler extends Handler {
    /**
     * Run handler.
     **/
	public SOAPMessage run(SOAPMessage msg, Database db) {
		_db = db;
		SOAPMessage resultMsg;
		StatementParameters parameters = new StatementParameters();
		parameters.setParameters(msg);
		try {
			List results = db.insertRecords(parameters);
			resultMsg = getResultMessage(results);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return resultMsg;
    }

    /**
     * Get result SOAPMessage to be returned to client.
     **/
	private SOAPMessage getResultMessage(List results) {
        SOAPMessage message = null;
        try {
            message = MessageFactory.newInstance().createMessage();

            // setup message
            SOAPPart soapPart = message.getSOAPPart();
            SOAPEnvelope envelope = soapPart.getEnvelope();
            SOAPBody body = envelope.getBody();
            Name name = envelope.createName("AddNew.Results", "m", _db.getName());
            SOAPBodyElement bodyElem = body.addBodyElement(name);
            bodyElem.addAttribute(envelope.createName("table"), "Variety");
            Name rowName = envelope.createName("row");
            
            for (Iterator it = results.iterator(); it.hasNext(); ) {
            	RowData row = (RowData)it.next();
            	
            	// id is key to client record
            	// remoteID is key to server record
            	// timestamp is from server record
            	SOAPElement rowElem = bodyElem.addChildElement(rowName);
            	SOAPElement dataElem = rowElem.addChildElement(envelope.createName("id"));
            	dataElem.addTextNode("" + row.getID());
            	dataElem = rowElem.addChildElement(envelope.createName("remoteID"));
            	dataElem.addTextNode("" + row.getRemoteID());
            	dataElem = rowElem.addChildElement(envelope.createName("timestamp"));
            	dataElem.addTextNode("" + row.getTimestamp().getTime());
            }
            message.saveChanges();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
	}

	private Database _db;
	private List _data = new ArrayList();
	private String _table;
}

