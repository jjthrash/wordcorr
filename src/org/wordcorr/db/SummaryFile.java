package org.wordcorr.db;

import java.io.*;
import org.jdom.*;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.Dialogs;
import org.wordcorr.gui.Messages;
import org.wordcorr.gui.Task;
import java.io.File;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


/**
 * Represents Summary XML File.
 * @author Jim Shiba
 * modified by davisnw.
 **/
public class SummaryFile implements Task {
    
    public SummaryFile() {}
    
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
     * Get summary view.
     **/
    public View getView() {
        return _view;
    }
    
    /**
     * Set summary view.
     **/
    public void setView(View view) {
        _view = view;
    }
    
    /**
     * Get number of tabulated groups.
     **/
    public int getTabulatedGroupCount() {
        return _tabulatedGroupCount;
    }
    
    /**
     * Get minimum Frantz Number.
     **/
    public float getMinFrantz() {
        return _minFrantz;
    }
    
    /**
     * Set minimum Frantz Number.
     **/
    public void setMinFrantz(float minFrantz) {
        _minFrantz = minFrantz;
    }
    
    /**
     * Get maximum number of reconstructions.
     **/
    public Integer getMaxReconstructions() {
        return _maxReconstructions;
    }
    
    /**
     * Set maximum number of reconstructions.
     **/
    public void setMaxReconstructions(Integer maxReconstructions) {
        _maxReconstructions = maxReconstructions;
    }
    
    /**
     * Get display frantz for clusters/protosegments data.
     **/
    public String getDisplayFrantz() {
        return _displayFrantz;
    }
    
    /**
     * Set display frantz for clusters/protosegments data.
     **/
    public void setDisplayFrantz(String displayFrantz) {
        _displayFrantz = displayFrantz;
    }
    
    /**
     * Set include residue data.
     **/
    public void setIncludeResidue(boolean includeResidue) {
        _includeResidue = includeResidue;
    }
    
    /**
     * Set gloss.
     **/
    public void setGloss(String gloss) {
        _gloss = gloss;
    }
    
    /**
     * Get output xsl file name.
     * @return the XSL filename that will be applied on output.  null if no transformation will be applied. 
     */ //davisnw added
    public String getOutputXSL() {
        return outputXSL;
    }
    
    /**
     * Set output Mode.
     * @param xslFileName The filename of the xsl file to apply at output.  Set null for raw xml output.
     */ //davisnw added
    public void setOutputXSL(String xslFileName) {
        outputXSL = xslFileName;
    }
    
	/**
     * Sets the extension that will be automatically applied to the output
     * filename if no extension is supplied by the user.
     * @param extension the extension to be appended. Should include the "."
     */ //davisnw added
    public void setOutputExtension(String extension) {
        if (extension==null) {
            outputExtension="";
        } else {
            outputExtension=extension;
        }
    }
    
	/**
     * Gets the extension that will be automatically applied if the user does
     * not supply one in the filename.
     */ //davisnw added
    public String getOutputExtension() {
        return outputExtension;
    }
    
    /**
     * Export Summary file.
     * Return true to close dialog, false to keep open.
     **/
    public boolean run() {
        if (_view == null)
            return true;
        
        try {
            // caculate Frantz strengths
            _tabulatedGroupCount = _view.updateFrantzStrength();
        } catch (DatabaseException e) {
            e.getRootCause().printStackTrace();
            return true;
        }

        //davisnw -- output the XML file, or the ouput that results from
		//applying the specified XSL transformation.
        if (outputXSL == null) {
            return runXML();
        } else {
            return runXSLT();
        }
    }
    
	//davisnw -- added
    private boolean runXSLT() {
        String in_filename = _filename + ".tmp";
        String out_filename = (_filename.indexOf(".") > 0) ? _filename : _filename + outputExtension;
        
        _filename=in_filename;
        runXML();

        StreamSource xsltSource = new StreamSource(getClass().getResourceAsStream(outputXSL));
        
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer(xsltSource);
            OutputStream output = new BufferedOutputStream(new FileOutputStream(out_filename));
            StreamSource xmlSource = new StreamSource(new File(in_filename));
            StreamResult htmlSink = new StreamResult(output);
            t.transform(xmlSource, htmlSink);
            output.close();
            new File(_filename).delete();
        } catch (TransformerConfigurationException e) {
            Dialogs.msgbox(AppPrefs.getInstance().getMessages().getString("msgErrSummaryFileCreation"));
            e.printStackTrace();
        } catch (TransformerException e) {
            Dialogs.msgbox(AppPrefs.getInstance().getMessages().getString("msgErrSummaryFileCreation"));
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            Dialogs.msgbox(AppPrefs.getInstance().getMessages().getString("msgErrSummaryFileCreation"));
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return true;
    }
    
    //davisnw -- this code moved from run() to this function so that we could provide more than one
    //output format for Summarize evidence
    private boolean runXML() {
        // create document
        Element element = new Element("WordCorrSummary");
        // set attributes
        Messages messages = AppPrefs.getInstance().getMessages();
        element.setAttribute("release", messages.getString("msgAboutRelease"));
        element.setAttribute("version", messages.getString("msgSummaryFileVersion"));
        
        element.addContent(
                _view.getCollection().getSummaryElement(
                _view,
                _minFrantz,
                _maxReconstructions.intValue(),
                _displayFrantz,
                _includeResidue,
                _gloss));
        Document doc = new Document(element);
        
        // create file
        try {
            String filename = (_filename.indexOf(".") > 0) ? _filename : _filename + ".xml";
            OutputStream output = new BufferedOutputStream(new FileOutputStream(filename));
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            outputter.output(doc, output);
            output.close();
        } catch (FileNotFoundException e) {
            Dialogs.msgbox(messages.getString("msgErrSummaryFileCreation"));
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        element = null;
        doc = null;
        return true;
    }
    
    private String outputXSL = null; // outputXSL=null means don't apply xslt.
    private String outputExtension=""; //the extension provided to the output filename if no extension is in the filename
    
    private float _minFrantz;
    private int _tabulatedGroupCount;
    private boolean _includeResidue;
    private Integer _maxReconstructions;
    private String _displayFrantz;
    private String _filename;
    private String _gloss;
    private View _view = null;
}