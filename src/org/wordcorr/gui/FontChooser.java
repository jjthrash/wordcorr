package org.wordcorr.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.*;

/**
 * A Dialog box to choose a font.
 * @author Keith Hamasaki
 * modified by davisnw--support for choosing different fonts for different parts of the UI.
 **/
public class FontChooser extends GenericDialog {
    
    private static final FontStyle REGULAR     = new FontStyle("lstRegular", Font.PLAIN);
    private static final FontStyle ITALIC      = new FontStyle("lstItalic", Font.ITALIC);
    private static final FontStyle BOLD        = new FontStyle("lstBold", Font.BOLD);
    private static final FontStyle BOLD_ITALIC = new FontStyle("lstBoldItalic", Font.BOLD + Font.ITALIC);
    
    
    public static final int DEFAULT_FONT=0;
    public static final int IPA_FONT=1;
    public static final int PRIMARY_GLOSS_FONT=2;
    public static final int SECONDARY_GLOSS_FONT=3;
    
    private int[] fontListSelections = {-1,-1,-1,-1};
    private int[] sizeListSelections = {-1,-1,-1,-1};
    private int[] styleListSelections = {-1,-1,-1,-1};
    
    /**
     * Constructor.
     **/
    public FontChooser() {
        super(true);
        setTitle(AppPrefs.getInstance().getMessages().getString("pgtFontChooser"));
        init();
        
        // set current attributes
        Font currentFont=FontCache.getIPA();
        
        initFontChoice(IPA_FONT, FontCache.getIPA());
        initFontChoice(PRIMARY_GLOSS_FONT, FontCache.getPrimaryGloss());
        initFontChoice(SECONDARY_GLOSS_FONT, FontCache.getSecondaryGloss());
        initFontChoice(DEFAULT_FONT, new JTextField().getFont());

        _fontPurposeCmb.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                int purpose = _fontPurposeCmb.getSelectedIndex();
                _fontList.setSelectedIndex(fontListSelections[purpose]);
                _fontList.ensureIndexIsVisible(fontListSelections[purpose]);
                _sizeList.setSelectedIndex(sizeListSelections[purpose]);
                _sizeList.ensureIndexIsVisible(sizeListSelections[purpose]);
                _styleList.setSelectedIndex(styleListSelections[purpose]);
                _styleList.ensureIndexIsVisible(styleListSelections[purpose]);
            }
        });
    }
    
    /**
     * Get the main panel for this dialog box.
     **/
    protected Component getMainPanel() {
        // Set up the main panel
        JPanel fontPanel = new JPanel();
        fontPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        fontPanel.setLayout(new BoxLayout(fontPanel, BoxLayout.X_AXIS));
        fontPanel.add(new SelectList("lblFont", "accFont", _fontList));
        fontPanel.add(Box.createHorizontalStrut(12));
        fontPanel.add(new SelectList("lblFontStyle", "accFontStyle", _styleList));
        fontPanel.add(Box.createHorizontalStrut(12));
        fontPanel.add(new SelectList("lblFontSize", "accFontSize", _sizeList));
        
        JPanel fontPanelTop = new JPanel();
        fontPanelTop.setLayout(new BorderLayout());
        fontPanelTop.add(new JComboBox());
        fontPanelTop.add(fontPanel, BorderLayout.CENTER);
        fontPanelTop.add(_fontPurposeCmb, BorderLayout.NORTH);
        
        _sampleLabel.setPreferredSize(new Dimension(400, 80));
        JPanel samplePanel = new JPanel(new BorderLayout());
        samplePanel.setBorder(BorderFactory.createTitledBorder("Sample"));
        samplePanel.add(_sampleLabel, BorderLayout.CENTER);
        
        
        JPanel main = new JPanel(new BorderLayout());
        main.add(fontPanelTop, BorderLayout.NORTH);
        main.add(samplePanel, BorderLayout.CENTER);
        return main;
    }
    
    /**
     * Get the selected font.
     * @param purpose needs to be one of the four constants: DEFAULT_FONT,
     * IPA_FONT=1,PRIMARY_GLOSS_FONT,SECONDARY_GLOSS_FONT
     */
    public Font getSelectedFont(int purpose) {
        return new Font(
                (String)_fontList.getModel().getElementAt(fontListSelections[purpose]),
                ((FontStyle) _styleList.getModel().getElementAt(styleListSelections[purpose])).getStyle(),
                Integer.parseInt((String)_sizeList.getModel().getElementAt(sizeListSelections[purpose]))
                );
        //return _sampleLabel.getFont();
    }
    
    /**
     * Refresh the sample window.
     **/
    private void refreshSample() {
        String face = (String) _fontList.getSelectedValue();
        FontStyle style = (FontStyle) _styleList.getSelectedValue();
        String size = (String) _sizeList.getSelectedValue();
        if (face == null || style == null || size == null) {
            return;
        }
        
        _sampleLabel.setFont(new Font(face, style.getStyle(), Integer.parseInt(size)));
    }
    
    private void initFontChoice(int purpose, Font f) {
        _fontList.setSelectedValue(f.getName(), true);
        _sizeList.setSelectedValue(String.valueOf(f.getSize()), true);
        _styleList.setSelectedValue(FontStyle.getInstance(f.getStyle()), true);
        fontListSelections[purpose]=_fontList.getSelectedIndex();
        sizeListSelections[purpose]=_sizeList.getSelectedIndex();
        styleListSelections[purpose]=_styleList.getSelectedIndex();
    }
    
    /**
     * Panel containing a label and a list of options.
     **/
    private final class SelectList extends JPanel {
        SelectList(String labelKey, String mnemonicKey, JList items) {
            super(new BorderLayout());
            Messages messages = AppPrefs.getInstance().getMessages();
            JLabel label = new JLabel(messages.getString(labelKey));
            label.setDisplayedMnemonic(messages.getChar(mnemonicKey));
            label.setLabelFor(items);
            this.add(label, BorderLayout.NORTH);
            this.add(new JScrollPane(items), BorderLayout.CENTER);
            Dimension dim = this.getPreferredSize();
            dim.height = 120;
            setPreferredSize(dim);
            
            // change listener for JList
            items.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            items.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent evt) {
                    if (evt.getValueIsAdjusting()) {
                        return;
                    }
                    
                    int purpose = _fontPurposeCmb.getSelectedIndex();
                    if (evt.getSource() == _fontList ) {
                        fontListSelections[purpose]=_fontList.getSelectedIndex();
                    } else if (evt.getSource()==_sizeList) {
                        sizeListSelections[purpose]=_sizeList.getSelectedIndex();
                    } else if (evt.getSource()==_styleList) {
                        styleListSelections[purpose]=_styleList.getSelectedIndex();
                    }
                    refreshSample();
                }
            });
        }
    }
    
    /**
     * Represents a font style.
     **/
    private static final class FontStyle {
        FontStyle(String key, int value) {
            _key = key;
            _value = value;
        }
        
        static FontStyle getInstance(int style) {
            switch(style) {
                case Font.PLAIN: return REGULAR;
                case Font.BOLD: return BOLD;
                case Font.ITALIC: return ITALIC;
                default: return BOLD_ITALIC;
            }
        }
        
        public String toString() {
            return AppPrefs.getInstance().getMessages().getString(_key);
        }
        
        int getStyle() {
            return _value;
        }
        
        private String _key;
        private int _value;
    }
    
    private final JList _fontList = new JList(GraphicsEnvironment.
            getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
    private final JList _styleList = new JList(new Object[] {
        REGULAR, ITALIC, BOLD, BOLD_ITALIC
    });
    private final JList _sizeList = new JList(new String[] {
        "4","5","6","7","8","9","10","11","12","13","14","15","16","17",
                "18","19","20","22","24","26","28","32", "48"
    });
    private final JLabel _sampleLabel = new JLabel("AaBbYyZz", SwingConstants.CENTER);
    private final JList _faceList = new JList();
    private final JComboBox _fontPurposeCmb = new JComboBox(new String[] {
        AppPrefs.getInstance().getMessages().getString("cmbFontDefault"),
                AppPrefs.getInstance().getMessages().getString("cmbFontIPA"),
                AppPrefs.getInstance().getMessages().getString("cmbFontPrimaryGloss"),
                AppPrefs.getInstance().getMessages().getString("cmbFontSecondaryGloss")});
}
