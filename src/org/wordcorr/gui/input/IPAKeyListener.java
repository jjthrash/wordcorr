package org.wordcorr.gui.input;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import org.wordcorr.gui.Dialogs;
import org.wordcorr.gui.MainFrame;
import org.wordcorr.gui.FontCache;

/**
 * KeyListener that implements the IPA input method.
 * @author Keith Hamasaki, Jim Shiba
 **/
public class IPAKeyListener extends KeyAdapter {
	
	private final int NUMCOLS = 4;
    private static final Map KEY_MAP = new HashMap();

    static {
        KEY_MAP.put(new Integer('a'), new int[] {
            0x251, 0xE6, 0x250, 0x252, 0x28C, 0xE1, 0xE0, 0xE2, 0xE3, 0xE4,
            0x101, 0x103, 0x105
        });
        KEY_MAP.put(new Integer('b'), new int[] {
            0x3B2, 0x253, 0x299, 0x298
        });
        KEY_MAP.put(new Integer('c'), new int[] {
            0xE7, 0x255, 0x254, 0x107, 0x109, 0x10D, 0x297, 0x2A7, 0x2A8
        });
        KEY_MAP.put(new Integer('d'), new int[] {
            0xF0, 0x257, 0x256, 0x2A3, 0x2A4, 0x2A5, 0x10F, 0x111
        });
        KEY_MAP.put(new Integer('e'), new int[] {
            0x259, 0x25A, 0x258, 0x25B, 0x25C, 0x25D, 0x25E, 0x28C, 0x29a, 0xE9,
            0xE8, 0xEA, 0xEB, 0x113, 0x115, 0x119, 0x11B
        });
        KEY_MAP.put(new Integer('f'), new int[] {
            0x278, 0x284
        });
        KEY_MAP.put(new Integer('g'), new int[] {
            0x263, 0x264, 0x260, 0x262, 0x29B, 0x2E0, 0x11D, 0x11F
        });
        KEY_MAP.put(new Integer('h'), new int[] {
            0x127, 0x266, 0x265, 0x267, 0x270, 0x29C, 0x2B0, 0x2B1, 0x125
        });
        KEY_MAP.put(new Integer('i'), new int[] {
            0x26A, 0x268, 0xEE, 0xEF, 0x12B, 0x12D, 0x129, 0x12F, 0x269
        });
        KEY_MAP.put(new Integer('j'), new int[] {
            0x25F, 0x29D, 0x284, 0x2B2, 0x135, 0x2A4, 0x2A5
        });
        KEY_MAP.put(new Integer('k'), new int[] {
            0x29E
        });
        KEY_MAP.put(new Integer('l'), new int[] {
            0x26C, 0x26E, 0x26D, 0x26B, 0x2E1, 0x29F, 0x3BB
        });
        KEY_MAP.put(new Integer('m'), new int[] {
            0x271, 0x26F, 0x270
        });
        KEY_MAP.put(new Integer('n'), new int[] {
            0x273, 0x272, 0x14B, 0x274, 0xF1, 0x144, 0x148
        });
        KEY_MAP.put(new Integer('o'), new int[] {
            0x276, 0x277, 0x275, 0x254, 0x153, 0xF8, 0xF3, 0xF2, 0xF4, 0xF5,
            0xF6, 0x14D, 0x14F, 0x298
        });
        KEY_MAP.put(new Integer('p'), new int[] {
            0x278, 0x298
        });
        KEY_MAP.put(new Integer('q'), new int[] {
            0x2A0
        });
        KEY_MAP.put(new Integer('r'), new int[] {
            0x27E, 0x27D, 0x279, 0x280, 0x281, 0x27B, 0x27A, 0x27C, 0x2B3, 0x2B4,
            0x2B5, 0x2B6, 0x155, 0x159
        });
        KEY_MAP.put(new Integer('s'), new int[] {
            0x283, 0x282, 0x15B, 0x15D, 0x15F, 0x2E2, 0x284, 0x286, 0x2A7, 0x2A8
        });
        KEY_MAP.put(new Integer('t'), new int[] {
            0x288, 0x3B8, 0x287, 0x2A6, 0x2A7, 0x2A8, 0xFE, 0x163, 0x165
        });
        KEY_MAP.put(new Integer('u'), new int[] {
            0x28A, 0x289, 0x270, 0x26F, 0xFA, 0xF9, 0xFB, 0xFC, 0x16B, 0x16D,
            0x169, 0x173, 0x28B
        });
        KEY_MAP.put(new Integer('v'), new int[] {
            0x28B, 0x28C
        });
        KEY_MAP.put(new Integer('w'), new int[] {
            0x28D, 0x270, 0x2B7, 0x175
        });
        KEY_MAP.put(new Integer('x'), new int[] {
            0x3C7, 0x2E3
        });
        KEY_MAP.put(new Integer('y'), new int[] {
            0x28F, 0x28E, 0x265, 0xFD, 0xFF, 0x177, 0x270, 0x2B8
        });
        KEY_MAP.put(new Integer('z'), new int[] {
            0x290, 0x291, 0x292, 0x293, 0x2A3, 0x2A4, 0x2A5, 0x26E, 0x17A
        });
        KEY_MAP.put(new Integer('?'), new int[] {
            0x294, 0x295, 0x296, 0x2A1, 0x2A2, 0x2C0, 0x2C1, 0x2E4
        });
        KEY_MAP.put(new Integer('!'), new int[] {
            0x298, 0x1C0, 0x1C2, 0x1C1
        });
        KEY_MAP.put(new Integer('^'), new int[] {
            0x2B9, 0x2BA, 0x2BB, 0x2BC, 0x2BD, 0x2BE, 0x2BF, 0x2C2, 0x2C3, 0x2C4,
            0x2C5, 0x2C6, 0x2C7, 0x2C8, 0x2C9, 0x2CA, 0x2CB, 0x2CC, 0x2CD, 0x2CE,
            0x2CF, 0X2D0, 0x2D1, 0x2D2, 0x2D3, 0x2D4, 0x2D5, 0x2D6, 0x2D7, 0x2D8,
            0x2D9, 0x2DA, 0x2DB, 0x2DC, 0x2DD, 0x2DE, 0x2DF
        });
        KEY_MAP.put(new Integer(':'), new int[] {
            0x2D0, 0x2D1, 0x2C8, 0x2CC, 0x2D4, 0x2D5, 0x2E5, 0x2E6, 0x2E7, 0x2E8,
            0x2E9, 0x2191, 0x2193, 0x2192, 0x2197, 0x2198, 0xAC
        });
        KEY_MAP.put(new Integer('~'), new int[] {
            0x300, 0x301, 0x302, 0x303, 0x304, 0x305, 0x306, 0x307, 0x308, 0x309,
            0x310, 0x311, 0x312, 0x313, 0x314, 0x315
        });
        KEY_MAP.put(new Integer(','), new int[] {
            0x316, 0x317, 0x318, 0x319, 0x31A, 0x31B, 0x31C, 0x31D, 0x31E, 0x31F,
            0x320, 0x321, 0x322, 0x323, 0x324, 0x325, 0x326, 0x327, 0x328, 0x329,
            0x32A, 0x32B, 0x32C, 0x32D, 0x32E, 0x32F, 0x330, 0x331, 0x332, 0x333
        });
        KEY_MAP.put(new Integer('-'), new int[] {
            0x334, 0x335, 0x336, 0x337, 0x338, 0x339, 0x33A, 0x33B, 0x33C, 0x33D,
            0x33E, 0x33F, 0x340, 0x341, 0x342, 0x343, 0x344, 0x345
        });
    }

    /**
     * Constructor.
     * @param text The text component that this is associated with.
     **/
    public IPAKeyListener(JTextComponent text) {
        _text = text;
    }

    /**
     * Key press event. This checks the character under the cursor and
     * brings up the IPA input method dialog if appropriate.
     **/
    public void keyPressed(KeyEvent evt) {
        if (_text.getCaretPosition() > 0 &&
            evt.getKeyCode() == KeyEvent.VK_SPACE &&
            (evt.getModifiers() & InputEvent.CTRL_MASK) != 0)
        {
            char c = _text.getText().charAt(_text.getCaretPosition() - 1);
            int[] chars = (int[]) KEY_MAP.get(new Integer(c));
            if (chars != null) {
                IPADialog dialog = new IPADialog(chars);
                dialog.setVisible(true);
            }
            evt.consume();
        }
    }

    /**
     * Button dialog class.
     **/
    private final class IPADialog extends JDialog {
        IPADialog(int[] chars) {
            super(MainFrame.getInstance(), true);

            // add the buttons
            JPanel panel = new JPanel();
			int numrows = chars.length / NUMCOLS + 1;
			panel.setLayout(new java.awt.GridLayout(numrows, NUMCOLS));
            Dimension dim = new Dimension(0, 0);
            for (int i = 0; i < chars.length; i++) {
                SelectButton btn = new SelectButton((char) chars[i], i);
                panel.add(btn);
                dim.width = Math.max(btn.getPreferredSize().width, dim.width);
                dim.height = Math.max(btn.getPreferredSize().height, dim.height);
				mapButton(btn);
            }

            // Make all of the buttons the same size
            final Component[] all = panel.getComponents();
            for (int i = 0; i < all.length; i++) {
                ((JComponent) all[i]).setMinimumSize(dim);
                ((JComponent) all[i]).setMaximumSize(dim);
            }
            setContentPane(panel);
            pack();
            setResizable(false);
            setLocationRelativeTo(_text);

            // add an escape key listener
			KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);
			getRootPane().getInputMap().put(ks, "CloseAction");
			getRootPane().getActionMap().put("CloseAction",
				new AbstractAction() {
					public void actionPerformed(ActionEvent ae) {
						quit();
			  		}
				});

            // add the close operation
            addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent evt) {
                        quit();
                    }
                });
        }

        private void mapButton(final JButton btn) {
			KeyStroke ks = KeyStroke.getKeyStroke(btn.getMnemonic(), 0, true);
			String action = btn.getText();
			getRootPane().getInputMap().put(ks, action);
			getRootPane().getActionMap().put(action,
				new AbstractAction() {
					public void actionPerformed(ActionEvent ae) {
						btn.doClick();
					}
				});
        }

        private final class SelectButton extends JButton {
            SelectButton(final char ch, int index) {
                char mnemonic;
                if (index < 9) {
                    mnemonic = (char) ('1' + index);
                } else {
                    mnemonic = (char) ('a' + (index - 9));
                }
                setText(mnemonic + " " + ch);
                setMnemonic(mnemonic);
                Font font = FontCache.getIPA();
                this.setFont(font.deriveFont((float) (font.getSize() * 1.7)));
                addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            int caret = _text.getCaretPosition();
                            String text = _text.getText();
                            try {
                                _text.getDocument().remove(caret - 1, 1);
                                caret -= 1;
                                _text.getDocument().insertString(caret, String.valueOf(ch), null);
                            } catch (javax.swing.text.BadLocationException e) {
                                Dialogs.genericError(e);
                            }
                            quit();
                        }
                    });
            }
        }

        private final void quit() {
            dispose();
            _text.requestFocus();
        }
    }

    private final JTextComponent _text;
}
