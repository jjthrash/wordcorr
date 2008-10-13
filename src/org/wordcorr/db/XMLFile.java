package org.wordcorr.db;

import java.io.*;
import java.util.zip.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.Dialogs;
import org.wordcorr.gui.Messages;
import org.wordcorr.gui.Task;

/**
 * Represents XML File.
 * @author Jim Shiba
 **/
public class XMLFile implements Task {
    private OutputStream exportStream = null;
    
    public XMLFile() {}
    
    /**
     * Set export task.
     **/
    public void setToExport(boolean export) {
        _export = export;
        if (!export) {
            exportStream = null;
        }
    }
    
    public void setToExport(OutputStream stream) {
        exportStream = stream;
        _export = (exportStream != null);
    }
    
    /**
     * Set to export metadata only.
     **/
    public void setToMetadataOnly(boolean metadataOnly) {
        _metadataOnly = metadataOnly;
    }
    
    /**
     * Get filename.
     **/
    public String getFilename() {
        return _filename;
    }
    
    /**
     * Set filename.
     **/
    public void setFilename(String filename) {
        _filename = filename;
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
     * Get collection.
     **/
    public WordCollection getCollection() {
        return _collection;
    }
    
    /**
     * Set collection.
     **/
    public void setCollection(WordCollection collection) {
        _collection = collection;
    }
    
    /**
     * Process file.
     * Return true to close dialog, false to keep open.
     **/
    public boolean run() {
        if (_export) {
            return exportXML();
        } else {
            return importXML();
        }
    }
    
    /**
     * Export file.
     * Return true to close dialog, false to keep open.
     **/
    private boolean exportXML() {
        if (_user == null || _collection == null)
            return true;
        
        // insure zip extension
        if (_filename.endsWith(".")) {
            _filename += "zip";
        } else if (!_filename.endsWith(".zip")) {
            _filename += ".zip";
        }
        
        // set creator, publisher and export timestamp
        try {
            boolean saveCollection = false;
            String creator = _collection.getCreator();
            if (creator.equals("")) {
                _collection.setCreator(
                        _user.getGivenName()
                        + " "
                        + _user.getFamilyName()
                        + " ["
                        + _user.getName()
                        + "]");
                _collection.setPublisher(_user.getEmail());
                _collection.setExportTimestamp();
                saveCollection = true;
            } else {
                // check creator user name
                String creatorName =
                        creator.substring(creator.indexOf("[") + 1, creator.indexOf("]"));
                if (_user.getName().equals(creatorName)) {
                    System.out.println("change publisher:" + _user.getEmail());
                    _collection.setPublisher(_user.getEmail());
                    _collection.setExportTimestamp();
                    saveCollection = true;
                }
            }
            if (saveCollection)
                _collection.save();
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
        
        // create document
        Element element = new Element("WordCorr");
        
        // set attributes
        Messages messages = AppPrefs.getInstance().getMessages();
        element.setAttribute("release", messages.getString("msgAboutRelease"));
        element.setAttribute("version", messages.getString("msgXMLVersion"));
        // get user element and add collection element
        Element userElement = _user.getElement();
        userElement.addContent(_collection.getElement(_metadataOnly));
        element.addContent(userElement);
        Document doc = new Document(element);
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        
        // create file
        try {
            if (exportStream == null) {
                ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(_filename));
                String entryName = (new File(_filename)).getName();
                entryName = entryName.substring(0, entryName.indexOf(".")) + ".xml";
                ZipEntry entry = new ZipEntry(entryName);
                zip.putNextEntry(entry);
                outputter.output(doc, zip);
                zip.closeEntry();
                zip.close();
            } else {
                outputter.output(doc, exportStream);
            }
        } catch (FileNotFoundException e) {
            Dialogs.msgbox(messages.getString("msgErrExportXMLFileCreation"));
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        element = null;
        doc = null;
        
        return true;
    }
    
    /**
     * Import file.
     * Return true to close dialog, false to keep open.
     **/
    private boolean importXML() {
        Messages messages = AppPrefs.getInstance().getMessages();
        
        // import file
        Document doc = null;
        ZipFile zip = getZipFile(_filename);
        try {
            if (zip == null) {
                // not zip file, assume xml
                try { //davisnw -- Necessary for change to JDOM 1.0
                    doc = new SAXBuilder().build(new File(_filename));
                } catch (IOException e) { //TODO: REVIEW.  Is this the proper way to handle it?
                    Dialogs.msgbox(e.getMessage());
                    e.printStackTrace();
                    return true;
                }
            } else {
                // get first entry
                if (zip.entries().hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) zip.entries().nextElement();
                    try {
                        InputStream input = zip.getInputStream(entry);
                        
                        // read file
                        doc = new SAXBuilder().build(input);
                    } catch (IOException e) {
                        Dialogs.msgbox(e.getMessage());
                        e.printStackTrace();
                        return true;
                    }
                } else {
                    // no entry
                    return true;
                }
            }
        } catch (JDOMException e) {
            Dialogs.msgbox(e.getMessage());
            e.printStackTrace();
            return true;
        }
        Element root = doc.getRootElement();
        if (!root.getName().equals("WordCorr"))
            return true;
        
        // create objects and save in db
        WordCollection collection = null;
        try {
            // get version
            double version = Double.parseDouble(root.getAttributeValue("version"));
            
            // check for user metadata from version 2
            if (version >= 2)
                root = root.getChild("user");
            
            // create collection
            collection = _user.makeCollection();
            collection.generateFromElement(root.getChild("collection"), version);
        } catch (Exception e) {
            Dialogs.msgbox(messages.getString("msgErrImportXMLFile"));
            deleteCollection(collection);
            e.printStackTrace();
            return true;
        }
        _collection = collection;
        
        return true;
    }
    
    /**
     * Get zip file.
     * Return ZipFile or null if file not valid zip file.
     **/
    private ZipFile getZipFile(String filename) {
        ZipFile zip = null;
        try {
            zip = new ZipFile(filename);
        } catch (IOException e) {
            return null;
        }
        return zip;
    }
    
    /**
     * Delete collection.
     **/
    private void deleteCollection(WordCollection collection) {
        if (collection != null && collection.getID() != -1) {
            try {
                collection.delete();
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }
    }
    
    private boolean _export = true;
    private boolean _metadataOnly = false;
    private String _filename;
    private WordCollection _collection = null;
    private User _user;
}