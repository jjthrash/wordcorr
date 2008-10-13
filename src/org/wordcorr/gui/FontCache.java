/*
 * FontCache.java
 *
 * Created on May 16, 2005, 10:55 AM
 *
 * A class to cache and unify the different fonts that are used for the different parts of the UI
 */

package org.wordcorr.gui;

import java.awt.Font;
import javax.swing.JLabel;

/**
 *
 * @author Nathan Davis (davisnw)
 */
public class FontCache {
    private static Font IPA_FONT, PRIMARY_GLOSS_FONT, SECONDARY_GLOSS_FONT;
    
    public final static String IPA="IPA";
    public final static String PRIMARY_GLOSS="PrimaryGloss";
    public final static String SECONDARY_GLOSS="SecondaryGloss";
    
    /** Creates a new instance of FontCatalog */
    private FontCache() {
    }
    
    public static Font getIPA () {
        return IPA_FONT;
    }
    
    public static void setIPA (Font f) {
        IPA_FONT=f;
    }
    
    public static Font getPrimaryGloss() {
        return PRIMARY_GLOSS_FONT;
    }
    
    public static void setPrimaryGloss(Font f) {
        PRIMARY_GLOSS_FONT=f;
    }
    
    public static Font getSecondaryGloss() {
        return SECONDARY_GLOSS_FONT;
    }
    
    public static void setSecondaryGloss(Font f) {
        SECONDARY_GLOSS_FONT=f;
    }    
    
    public static Font getFont(String type) {
        if (type == null) 
            return (new JLabel()).getFont();        
        if (type.equals(IPA))
            return IPA_FONT;
        if (type.equals(PRIMARY_GLOSS))
            return PRIMARY_GLOSS_FONT;
        if (type.equals(SECONDARY_GLOSS))
            return SECONDARY_GLOSS_FONT;
        return (new JLabel()).getFont();
    }
}
