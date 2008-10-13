package org.wordcorr.db;

import java.io.*;
import java.util.*;

import org.wordcorr.gui.MainFrame;
import org.wordcorr.gui.Task;

/**
 * Represents Wordsurv Import File.
 * @author Jim Shiba
 **/
public class WordsurvFile implements Task {

    public WordsurvFile() {
    }

    /**
     * Get Catalogue filename.
     **/
    public String getCatFilename() {
        return _catFilename;
    }

    /**
     * Set Catalogue filename.
     **/
    public void setCatFilename(String filename) {
        _catFilename = filename;
    }

    /**
     * Get Database filename.
     **/
    public String getDBFilename() {
        return _dbFilename;
    }

    /**
     * Set Database filename.
     **/
    public void setDBFilename(String filename) {
        _dbFilename = filename;
    }

    /**
     * Get this collection's full name.
     **/
    public String getName() {
        return _name;
    }

    /**
     * Set this collection's full name.
     **/
    public void setName(String name) {
        _name = name;
    }

    /**
     * Get this collection's short name.
     **/
    public String getShortName() {
        return _shortName;
    }

    /**
     * Set this collection's short name.
     **/
    public void setShortName(String shortName) {
        _shortName = shortName;
    }

    /**
     * Get this collection's gloss language.
     **/
    public String getGloss() {
        return _gloss;
    }

    /**
     * Set this collection's gloss language.
     **/
    public void setGloss(String gloss) {
        _gloss = gloss;
    }

    /**
     * Get this collection's secondary gloss language.
     **/
    public String getGloss2() {
        return _gloss2;
    }

    /**
     * Set this collection's secondary gloss language.
     **/
    public void setGloss2(String gloss2) {
        _gloss2 = gloss2;
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
     * Process file.
     * Return true to close dialog, false to keep open.
     **/
    public boolean run() {
		WordCollection collection = null;
        try {
	        // create collection
	        collection = _user.makeCollection();
	        collection.setName(getName());
	        collection.setShortName(getShortName());
	        collection.setGloss(getGloss());
	        collection.setGloss2(getGloss2());
	        collection.save();
	
	        // read catalogue files and create varieties
	        CatalogueFile catFile = new CatalogueFile(collection);
	        Map varieties = catFile.process(getCatFilename());
	        if (varieties == null) {
	        	deleteCollection(collection);
	        	return true;
	        }
	
	        // get orignal view
	        View vw = collection.getOriginalView();
	        vw.setMembers(new ArrayList(varieties.values()));
	        vw.save();
	        
	        // read database files and process
	        DatabaseFile dbFile = new DatabaseFile(collection);
	        if (!dbFile.process(getDBFilename(), varieties)) {
	        	deleteCollection(collection);
	        	return true;
	        }
        } catch (Exception e) {
            e.printStackTrace();
           	deleteCollection(collection);
           	return true;
        }
        _collection = collection;
        return true;
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

	/**
	 * Represents WordSurv Catalogue File.
	 **/
	private static class CatalogueFile {
	    /**
	     * Constructor.
	     **/
	    public CatalogueFile(WordCollection collection) {
	        _collection = collection;
	    }
	
	    /**
	     * Process file.
	     * @param filename Catalogue filename
	     **/
	    public Map process(String filename) {
	        HashMap varieties = new HashMap();
	
			try {
		        // open file
		        BufferedReader reader = new BufferedReader(new FileReader(filename));
		
		        // Read the file and display it's contents.
		        String line = reader.readLine();
		        String source = "";
		        Variety variety = null;
		        
		        while (null != (line = reader.readLine())) {
		            if (line.startsWith("\\symbol")) {
		//            		System.out.println("CatalogueFile symbol="+line.substring(8).trim());
		                variety = _collection.makeVariety();
		                String abbreviation = line.substring(8).trim();
		                variety.setAbbreviation(abbreviation);
		                
		                // note: set short name as abbreviation since Wordsurv does not have it.
		                variety.setShortName(abbreviation);
		            } else if (line.startsWith("\\title")) {
		//            		System.out.println("CatalogueFile title="+line.substring(7).trim());
		                variety.setName(line.substring(7).trim());
		            } else if (line.startsWith("\\reliab")) {
		//            		System.out.println("CatalogueFile reliability="+line.substring(8).trim());
		                variety.setQuality(line.substring(8).trim());
		            } else if (line.startsWith("\\t ")) {
		//            		System.out.println("CatalogueFile text="+line.substring(3).trim());
		                source += line.substring(3).trim();
		            } else if (line.startsWith("\\end")) {
		//            		System.out.println("CatalogueFile end");
		                variety.setSource(source);
		                variety.save();
		                varieties.put(variety.getAbbreviation(), variety);
		                source = "";
		            }
		        }
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
	        return varieties;
	    }
	    private WordCollection _collection = null;
	}
	
	/**
	 * Represents Wordsurv Database File.
	 **/
	private static class DatabaseFile {
	    /**
	     * Constructor.
	     **/
	    public DatabaseFile(WordCollection collection) {
	        _collection = collection;
	    }
	
	
	    /**
	     * Process import file.
	     * @param filename Database filename
	     **/
	    public boolean process(String filename, Map varieties) {
	        ArrayList dataSets = new ArrayList();
	
	        // open file
	        try {
	            BufferedReader reader = new BufferedReader(new FileReader(filename));
	
	            // Read the file and process it's contents.
	            String line = reader.readLine();
	            String gloss = "";
	            String source = "";
	            DataSet set = new DataSet();
	            HashMap groupMaxLength = new HashMap();
	            int order = 0;
	            while (null != (line = reader.readLine())) {
	                if (line.startsWith("\\record ")) {
	                    gloss = line.substring(8).trim();
	                } else if (line.startsWith("\\al ")) {
	                } else if (line.startsWith("\\r ")) {
	                    // original format
	                    set.setGloss(gloss);
	                    set.parseRow(line.substring(3).trim());
	                    if (set.isValid()) {
	                    	// determine maximum glyph length by group
	                    	Integer maxLength = (Integer)groupMaxLength.get(set.getGroupTag());
	                    	if (maxLength == null) {
	                    		groupMaxLength.put(set.getGroupTag(), new Integer(set.getDatum().length()));
	                    	} else {
		                        int maxLengthValue = (set.getDatum().length() > maxLength.intValue()) ?
		                            set.getDatum().length() : maxLength.intValue();
	                    		groupMaxLength.put(set.getGroupTag(), new Integer(maxLengthValue));
	                    	}
	                        dataSets.add(set);
	                    }
	                    set = new DataSet();
	                } else if (line.startsWith("\\c ")) {
	                    set.setGroupTag(line.substring(3).trim());
	                } else if (line.startsWith("\\f ")) {
	                    set.setDatum(DataSet.trimEnd(line.substring(3)));
	                } else if (line.startsWith("\\m ")) {
	                    set.setMetathesis1(Integer.parseInt(line.substring(3).trim()));
	                } else if (line.startsWith("\\l ")) {
	                    set.setVarieties(line.substring(3).trim());
	                    // varieties always comes last
	                    set.setGloss(gloss);
	                    if (set.isValid()) {
	                    	// determine maximum glyph length by group
	                    	Integer maxLength = (Integer)groupMaxLength.get(set.getGroupTag());
	                    	if (maxLength == null) {
	                    		groupMaxLength.put(set.getGroupTag(), new Integer(set.getDatum().length()));
	                    	} else {
		                        int maxLengthValue = (set.getDatum().length() > maxLength.intValue()) ?
		                            set.getDatum().length() : maxLength.intValue();
	                    		groupMaxLength.put(set.getGroupTag(), new Integer(maxLengthValue));
	                    	}
	                        dataSets.add(set);
	                    }
	                    set = new DataSet();
	                } else if (line.startsWith("\\end")) {
	                    order = Integer.parseInt(line.substring(5).trim());
	                    
	                    // create entry
	                    Entry entry = _collection.makeEntry();
	                    entry.setName(gloss);
	                    entry.setEntryNum(new Integer(order));
	                    entry.save();
	
	                    View originalView = _collection.getOriginalView();
	                    HashMap groupsSaved = new HashMap();
	                    for (Iterator it = dataSets.iterator(); it.hasNext();) {
	                        DataSet data = (DataSet) it.next();
	                        data.processDatum(((Integer)groupMaxLength.get(data.getGroupTag())).intValue());
	
	                        // process each variety
	                        for (int i = 0; i < data.getVarieties().length(); i++) {
	                            Variety variety =
	                                (Variety) varieties.get(data.getVarieties().substring(i, i + 1));
	                            if (variety != null) {
	                                // create datum
	                                Datum datum = entry.makeDatum();
	                                datum.setName(data.getRawDatum());
	                                datum.setVariety(variety);
	                                datum.markImported();
	                                datum.save();
	
	                                // create group
	                                Group group = (Group) groupsSaved.get(data.getGroupTag());
	                                if (group == null) {
	                                    group = originalView.makeGroup(entry);
	                                    group.setName(data.getGroupTag());
	                                    group.save();
	                                    groupsSaved.put(data.getGroupTag(), group);
	                                }
	
	                                // create alignment
	                                Alignment alignment = originalView.makeAlignment(datum);
	                                alignment.setGroup(group);
	                                alignment.setVector(data.getVector());
	                                alignment.setSimpleMetathesis(data.getMetathesis1());
	                                alignment.save();
	                            }
	                        }
	                    }
	                    // reset
	                    groupMaxLength.clear();
	                    order = 0;
	                    dataSets.clear();
	                }
	            }
	            // reorder all entry numbers
	            _collection.reorderEntries(0, 1);
	        } catch (Exception e) {
	            e.printStackTrace();
	            return false;
	        }
	        return true;
	    }
	
	    /**
	     * Data Set Bean.
	     **/
	    private static final class DataSet {
	        // return validity of DataSet
	        public boolean isValid() {
	            return !(_datum == null || _gloss == null);
	        }
	
	        // parse orignal wordsurv row
	        public void parseRow(String row) {
	            // get first delimiter
	            int delim1 = row.indexOf("|");
	
	            // space - ignore
	            if (delim1 > 0) { // ignores space
	                _groupTag = row.substring(0, delim1).trim();
	
	                // # - ignore
	                // 0 - missing data
	                if (!_groupTag.equals("#") && !_groupTag.equals("0")) {
	                    // get second delimiter
	                    int delim2 = row.lastIndexOf("|");
	                    if (delim2 > delim1) {
	                        String datum = trimEnd(row.substring(delim1 + 1, delim2));
	                        if (!datum.equals(""))
	                            _datum = extractMetathesis(datum);
	                        _varieties = row.substring(delim2 + 1).trim();
	                    }
	                }
	            }
	        }
	
	        // Removes white space from end of string.
		    public static String trimEnd(String v) {
		    	for (int i = v.length() - 1; i >= 0; i--) {
		    		if (v.charAt(i) != ' ')
		    			return v.substring(0, i + 1);
		    	}
		    	return "";
		    }
		    
	        // extract Wordsurv metathesis value and return datum
	        public String extractMetathesis(String datum) {
	            int pos = datum.indexOf(">");
	            if (pos == -1) {
	                _metathesis1 = 0;
	                return trimEnd(datum);
	            }
	            _metathesis1 = Integer.parseInt(datum.substring(pos + 1));
	            return trimEnd(datum.substring(0, pos - 1));
	        }
	
	        // adjust length and extract alignment vector
	        public void processDatum(int len) {
	            String vector = "";
	            String rawDatum = "";
	            for (int i = 0; i < len; i++) {
	                char ch = (i < _datum.length()) ? _datum.charAt(i) : ' ';
	                if (ch == ' ' || ch == '/') {
	                    // indel
	                    vector += '/';
	                } else if (ch == '-' || ch == '.') {
	                    // ignore
	                    vector += '.';
	                } else {
	                    // valid character
	                    vector += 'x';
	                    rawDatum += ch;
	                }
	            }
	            _vector = vector;
	            _rawDatum = rawDatum;
	        }
	        
	        public String getGloss() {
	            return _gloss;
	        }
	        public void setGloss(String v) {
	            _gloss = v;
	        }
	
	        public String getGroupTag() {
	            return _groupTag;
	        }
	        public void setGroupTag(String v) {
	            _groupTag = v;
	        }
	
	        public String getDatum() {
	            return _datum;
	        }
	        public void setDatum(String v) {
	            _datum = v;
	        }
	
	        public int getMetathesis1() {
	            return _metathesis1;
	        }
	        public void setMetathesis1(int v) {
	            _metathesis1 = v;
	        }
	
	        public String getRawDatum() {
	            return _rawDatum;
	        }
	
	        public String getVarieties() {
	            return _varieties;
	        }
	        public void setVarieties(String v) {
	            _varieties = v;
	        }
	
	        public String getVector() {
	            return _vector;
	        }
	
	        private int _metathesis1 = 0;
	        private String _datum;
	        private String _gloss;
	        private String _groupTag = "";
	        private String _rawDatum;
	        private String _varieties;
	        private String _vector;
	    }
	    final private WordCollection _collection;
	}

    private String _catFilename;
    private String _name;
    private String _shortName;
    private String _gloss;
    private String _gloss2;
    private String _dbFilename;
    private WordCollection _collection = null;
    private User _user;
}