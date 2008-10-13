package org.wordcorr.net;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.soap.*;
import javax.xml.transform.*;

import java.util.*;
import java.io.*;

import org.wordcorr.db.Database;
import org.wordcorr.db.DatabaseFactory;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.Messages;

public class WordCorrNet extends HttpServlet {

    public void init(ServletConfig servletConfig)
        throws javax.servlet.ServletException {
        super.init(servletConfig);
        try {
        	// instantiate message factory
            _msgFactory = MessageFactory.newInstance();
        } catch (SOAPException e) {
            throw new ServletException(
                "WordCorrNet.init: Unable to create message factory " + e.getMessage());
        }
        // instantiate handlers
        _handlers = new HashMap();
        _handlers.put("AddNew", new AddNewHandler());
        
        // load the default database
        File file = new File(AppPrefs.getInstance().getConfigDir(), "database.script");
        if (file.exists()) {
            _database = openDatabase(file);
        }
    }

    /**
     * Open a database
     * @return Database
     **/
    public Database openDatabase(File file) {
    	AppPrefs prefs = AppPrefs.getInstance();
    	Messages messages = prefs.getMessages();
    	
        // just in case they typed the file name, let's append
        // a .script and check for that file
        if (!file.exists() && !file.getName().endsWith(".script")) {
            file = new File(file.getAbsolutePath() + ".script");
        }

        if (!file.exists() || !file.getName().endsWith(".script")) {
        	System.out.println(messages.getCompoundMessage("cmpCannotOpenFile", file.getAbsolutePath()));
            return null;
        }

        String fname = file.getAbsolutePath();
        int dotindex = file.getName().indexOf(".");
        if (dotindex != -1) {
            fname = fname.substring(0, fname.lastIndexOf("."));
        }

        Database db = DatabaseFactory.openLocalDatabase(new File(fname));
        try {
            switch (db.test()) {
                case Database.STATUS_UNINITIALIZED:
                    System.out.println(messages.getCompoundMessage("cmpCannotOpenFile_2", file.getAbsolutePath()));
                    return null;
                case Database.STATUS_OLD:
                    System.out.println(messages.getCompoundMessage("cmpMigrateFile", file.getAbsolutePath()));
                    return null;
                case Database.STATUS_CURRENT:
                	System.out.println("WordCorrNet.openDatabase: STATUS_CURRENT");
                	break;
                default:
            }
        } catch (Exception e) {
            System.out.println(messages.getCompoundMessage("cmpCannotOpenFile_3", new Object[] { file.getAbsolutePath(), e.getMessage() }));
            e.printStackTrace();
            return null;
        }
        return db;
    }

    /**
     * Process Post
     **/
    public void doPost(HttpServletRequest req, HttpServletResponse res)
        throws javax.servlet.ServletException, java.io.IOException {
        try {
            // Get all the headers from the HTTP request.
            MimeHeaders headers = getHeaders(req);

            // Get the body of the HTTP request.
            InputStream is = req.getInputStream();

            // Now internalize the contents of the HTTP request and
            // create a SOAPMessage
            SOAPMessage msg = _msgFactory.createMessage(headers, is);

            Handler handler = (Handler)_handlers.get(getMethod(msg));
            SOAPMessage reply = (handler != null) ?
            	handler.run(msg, _database) : null;

            if (reply != null) {
                res.setStatus(HttpServletResponse.SC_OK);
                putHeaders(reply.getMimeHeaders(), res);

                // output message
                OutputStream os = res.getOutputStream();
                reply.writeTo(os);

                os.flush();
            } else {
            	System.out.println("doPost reply == null");
                res.setStatus(HttpServletResponse.SC_NO_CONTENT);
	        }
        } catch (Exception ex) {
        	System.out.println("doPost Exception");
            throw new ServletException("JAXM POST failed " + ex.getMessage());
        }
    }

    /**
     * Get header information from request
     * @return MimeHeaders
     **/
    static MimeHeaders getHeaders(HttpServletRequest req) {

        Enumeration _enum = req.getHeaderNames();
        MimeHeaders headers = new MimeHeaders();

        while (_enum.hasMoreElements()) {
            String headerName = (String) _enum.nextElement();
            String headerValue = req.getHeader(headerName);

            StringTokenizer values = new StringTokenizer(headerValue, ",");
            while (values.hasMoreTokens())
                headers.addHeader(headerName, values.nextToken().trim());
        }

        return headers;
    }

    /**
     * Put headers into response
     **/
    static void putHeaders(MimeHeaders headers, HttpServletResponse res) {

        Iterator it = headers.getAllHeaders();
        while (it.hasNext()) {
            MimeHeader header = (MimeHeader) it.next();

            String[] values = headers.getHeader(header.getName());
            if (values.length == 1)
                res.setHeader(header.getName(), header.getValue());
            else {
                StringBuffer concat = new StringBuffer();
                int i = 0;
                while (i < values.length) {
                    if (i != 0)
                        concat.append(',');
                    concat.append(values[i++]);
                }

                res.setHeader(header.getName(), concat.toString());
            }
        }
    }

    /**
     * Get method from message.
     * @return String
     **/
    public String getMethod(SOAPMessage msg) {
    	try {
            SOAPPart sp = msg.getSOAPPart();
            SOAPEnvelope se = sp.getEnvelope();
            SOAPBody sb = se.getBody();
            
            for (Iterator it = sb.getChildElements(); it.hasNext();) {
	            SOAPBodyElement bodyElement = (SOAPBodyElement) it.next();
	            Name name = bodyElement.getElementName();
	            if (name.getPrefix().equals("m"))
	            	return name.getLocalName();
            }            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

	Database _database;
	Map _handlers;
    MessageFactory _msgFactory;
}