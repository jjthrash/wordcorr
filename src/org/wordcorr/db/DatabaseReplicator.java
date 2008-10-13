package org.wordcorr.db;

import javax.xml.soap.*;
import java.util.*;
import java.net.URL;
import org.wordcorr.AppProperties;
import org.wordcorr.net.Replicator;
import org.wordcorr.net.VarietyReplicator;

/**
 * Represents Database Replicator.
 * @author Jim Shiba
 **/
public class DatabaseReplicator {

    public DatabaseReplicator() {
    }

    /**
     * Get this collection's user.
     **/
    public User getUser() {
        return _user;
    }

    /**
     * Set this collection's user.
     **/
    public void setUser(User user) {
        _user = user;
    }

    /**
     * Replicate.
     **/
    public void run() {
        try {
        	URL endpoint = new URL(AppProperties.getProperty("ReplicationEndpoint"));
			SOAPConnectionFactory scFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection con = scFactory.createConnection();
            MessageFactory factory = MessageFactory.newInstance();

        	List collections = _user.getCollections();
        	for (Iterator it = collections.iterator(); it.hasNext();) {
        		WordCollection collection = (WordCollection)it.next();
        		long id = collection.getID();
        		
        		Replicator replicator = new VarietyReplicator(id, endpoint, factory, scFactory); 
        		replicator.addNew();
        	}
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

	private User _user;
}