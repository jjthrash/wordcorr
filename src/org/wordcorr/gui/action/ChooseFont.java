package org.wordcorr.gui.action;

import java.awt.Font;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.wordcorr.db.DatabaseException;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.FontCache;
import org.wordcorr.gui.FontChooser;
import org.wordcorr.gui.MainFrame;

/**
 * Action to choose a new font.
 * @author Keith Hamasaki, Jim Shiba
 * modified by davisnw--added support for different fonts for different parts of the UI
 **/
public class ChooseFont extends WordCorrAction {
    private static final Action _instance = new ChooseFont();
    
    public static Action getInstance() {
        return _instance;
    }
    
    private ChooseFont() {
        super("mnuChooseFont", "accChooseFont");
    }
    
    public void actionPerformed(ActionEvent evt) {
        FontChooser chooser = new FontChooser();
        chooser.setVisible(true);
        if (!chooser.isCancelled()) {
            Font font = chooser.getSelectedFont(FontChooser.DEFAULT_FONT);
            MainFrame.getInstance().setDefaultFont(font);
            
            FontCache.setIPA(chooser.getSelectedFont(FontChooser.IPA_FONT));
            FontCache.setPrimaryGloss(chooser.getSelectedFont(FontChooser.PRIMARY_GLOSS_FONT));
            FontCache.setSecondaryGloss(chooser.getSelectedFont(FontChooser.SECONDARY_GLOSS_FONT));
            
            //TODO: I think somewhere in here is where the row height should be
            //adjusted for the largest fonts?
            
            // repaint for table row height correction in Data/Tabulate/Refine panes.
            try {
                MainFrame.getInstance().refresh();
            } catch (DatabaseException ignored) { }
            
            // save settings
            setFontProperty(AppPrefs.FONT, font);
            setFontProperty(AppPrefs.IPA_FONT, FontCache.getIPA());
            setFontProperty(AppPrefs.PRIMARY_GLOSS_FONT, FontCache.getPrimaryGloss());
            setFontProperty(AppPrefs.SECONDARY_GLOSS_FONT, FontCache.getSecondaryGloss());
        }
    }
    
    private void setFontProperty(String key, Font font) {
        String style="";
        switch (font.getStyle()) {
            case Font.BOLD:
                style = "BOLD"; break;
            case Font.ITALIC:
                style = "ITALIC"; break;
            case Font.BOLD + Font.ITALIC:
                style = "BOLDITALIC"; break;
            default:
        }
        AppPrefs.getInstance().setProperty(key, font.getName() + "-" + style + "-" + font.getSize());
    }
}
