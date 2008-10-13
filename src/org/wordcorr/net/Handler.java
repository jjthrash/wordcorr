package org.wordcorr.net;

import javax.xml.soap.*;
import org.wordcorr.db.Database;

/**
 * Action handler.
 * @author Jim Shiba
 */
public abstract class Handler {
    /**
     * Run handler.
     **/
	abstract public SOAPMessage run(SOAPMessage msg, Database db);
}
