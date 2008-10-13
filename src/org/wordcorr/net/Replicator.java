package org.wordcorr.net;

import javax.xml.soap.*;
import java.util.*;
import java.net.URL;

/**
 * Base class for replicator objects.
 * @author Jim Shiba
 **/
public abstract class Replicator {
	
	/**
     * Constructor.
     **/
    public Replicator(long id, URL endpoint, MessageFactory msgFactory, SOAPConnectionFactory scFactory) {
        _id = id;
        _endpoint = endpoint;
        _msgFactory = msgFactory;
        _scFactory = scFactory;
    }

    /**
     * Add new records.
     **/
    abstract public void addNew();
    
    protected final long _id;
    protected final MessageFactory _msgFactory;
    protected final SOAPConnectionFactory _scFactory;
    protected final URL _endpoint;
}
