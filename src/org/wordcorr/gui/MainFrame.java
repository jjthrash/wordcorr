package org.wordcorr.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import org.wordcorr.db.*;
import org.wordcorr.gui.action.*;

/**
 * Main JFrame for the application.
 * @author Keith Hamasaki, Jim Shiba
 **/
public final class MainFrame extends JFrame implements Refreshable {

    private static final MainFrame INSTANCE = new MainFrame();

    public static MainFrame getInstance() {
        return INSTANCE;
    }

    /**
     * Constructor.
     **/
    private MainFrame() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(true);
    }

    /**
     * Init this frame.
     **/
    void init() {
        _panel = new MainPanel();
        setContentPane(_panel);

        // Set the font. If the user hasn't chosen one before, set the
        // default font to Lucida Sans because that supports IPA
        // symbols and is guaranteed to be available with JDK 1.2
        final AppPrefs prefs = AppPrefs.getInstance();
        String fontStr = prefs.getProperty(AppPrefs.FONT);
        if (fontStr == null) {
            // default
            String fontName = "Lucida Sans";
            String[] fonts =
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            for (int i = 0; i < fonts.length; i++) {
                if (fonts[i].equalsIgnoreCase("Doulos SIL")) {
                    // first choice
                    fontName = fonts[i];
                    break;
                } else if (fonts[i].equalsIgnoreCase("Lucida Sans Unicode")) {
                    // second choice
                    fontName = fonts[i];
                }
            }
            setDefaultFont(new Font(fontName, Font.PLAIN, 12));
        } else {
            setDefaultFont(Font.decode(fontStr));
        }
        
        FontCache.setIPA(Font.decode(prefs.getProperty(prefs.IPA_FONT, "Doulos SIL--14")));
        FontCache.setPrimaryGloss(Font.decode(prefs.getProperty(prefs.PRIMARY_GLOSS_FONT, "Lucida Sans Unicode--12")));
        FontCache.setSecondaryGloss(Font.decode(prefs.getProperty(prefs.SECONDARY_GLOSS_FONT, "Lucida Sans Unicode--12")));

        Messages messages = prefs.getMessages();
        setTitle(messages.getString("lblAppTitle"));
        addWindowListener(Exit.getInstance());

        // Load the default database
        File file = new File(prefs.getConfigDir(), "database.script");
        if (file.exists()) {
            openDatabase(file);
        } else {
            try {
                NewLocal.getInstance().createNewDatabase(file);
            } catch (DatabaseException e) {
                Dialogs.genericError(e);
            }
        }

        // set to the previous size
        setLocation(
            prefs.getIntProperty(AppPrefs.LOCATION_X, 0),
            prefs.getIntProperty(AppPrefs.LOCATION_Y, 0));
        setSize(
            prefs.getIntProperty(AppPrefs.WIDTH, 640),
            prefs.getIntProperty(AppPrefs.HEIGHT, 480));

        // refresh to set pane and entry
        try {
            refresh();
        } catch (DatabaseException ignored) {}

        // add a component listener to store location/size info
        addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent evt) {
                Point point = evt.getComponent().getLocation();
                prefs.setProperty(AppPrefs.LOCATION_X, String.valueOf(point.x));
                prefs.setProperty(AppPrefs.LOCATION_Y, String.valueOf(point.y));
            }

            public void componentResized(ComponentEvent evt) {
                Dimension dim = evt.getComponent().getSize();
                prefs.setProperty(AppPrefs.WIDTH, String.valueOf(dim.width));
                prefs.setProperty(AppPrefs.HEIGHT, String.valueOf(dim.height));
            }
        });
    }

    /**
     * Set the default font for the application.
     **/
    public void setDefaultFont(Font f) {
        FontUIResource font = new FontUIResource(f);
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, font);
            }
        }
        SwingUtilities.updateComponentTreeUI(this);
    }

    /**
     * Open a database
     * @return true if successful, false if not
     **/
    public boolean openDatabase(File file) {
        AppPrefs prefs = AppPrefs.getInstance();
        Messages messages = prefs.getMessages();

        // just in case they typed the file name, let's append
        // a .script and check for that file
        if (!file.exists() && !file.getName().endsWith(".script")) {
            file = new File(file.getAbsolutePath() + ".script");
        }

        if (!file.exists() || !file.getName().endsWith(".script")) {
            Dialogs.error(
                messages.getCompoundMessage("cmpCannotOpenFile", file.getAbsolutePath()));
            return false;
        }

        String fname = file.getAbsolutePath();
        int dotindex = file.getName().indexOf(".");
        if (dotindex != -1) {
            fname = fname.substring(0, fname.lastIndexOf("."));
        }

        Database db = DatabaseFactory.openLocalDatabase(new File(fname));
        Dialogs.showWaitCursor(this);
        try {
            switch (db.test()) {
                case Database.STATUS_UNINITIALIZED :
                    Dialogs.error(
                        messages.getCompoundMessage("cmpCannotOpenFile_2", file.getAbsolutePath()));
                    return false;
                case Database.STATUS_OLD :
                    if (Dialogs
                        .confirm(
                            messages.getCompoundMessage("cmpMigrateFile", file.getAbsolutePath()))) {
                        db.migrate();
                    } else {
                        return false;
                    }
                    break;
            }
            setDatabase(db);

            //            prefs.setProperty(AppPrefs.LAST_DIR, file.getParentFile().getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            Dialogs.error(
                messages.getCompoundMessage(
                    "cmpCannotOpenFile_3",
                    new Object[] { file.getAbsolutePath(), e.getMessage()}));
            return false;
        } finally {
            Dialogs.showDefaultCursor(this);
        }

        return true;
    }

    /**
     * Set the current database.
     **/
    public void setDatabase(Database db) throws DatabaseException {
        // set the state of all disable on close actions
        for (Iterator it = _disableOnClose.iterator(); it.hasNext();) {
            Action action = (Action) it.next();
            action.setEnabled(db != null);
        }

        // disable all user only actions
        setUserOnlyEnabled(false);
        final AppPrefs prefs = AppPrefs.getInstance();

        if (db != null) {
            final DatabasePane pane = new DatabasePane(db);
            _panel.setDatabasePane(pane);
            pane.setDividerLocation(
                AppPrefs.getInstance().getIntProperty(AppPrefs.DIVIDER_LOCATION, 150));
        } else {
            _panel.setDatabasePane(null);
        }
        _database = db;
        updateStatus();
    }

    /**
     * Get the database.
     **/
    public Database getDatabase() {
        return _database;
    }

    /**
     * Enable or disable all user only actions.
     **/
    public void setUserOnlyEnabled(boolean flag) {
        for (Iterator it = _userOnly.iterator(); it.hasNext();) {
            Action action = (Action) it.next();
            action.setEnabled(flag);
        }
    }

    /**
     * Add an action to the list of disable on close actions.
     **/
    public void addDisableOnClose(Action action) {
        _disableOnClose.add(action);
    }

    /**
     * Add an action to the user only list.
     **/
    public void addUserOnly(Action action) {
        _userOnly.add(action);
    }

    /**
     * Refresh.
     **/
    public void refresh() throws DatabaseException {
        _panel.refresh();
    }

    /**
     * Get the current database.
     **/
    public DatabasePane getDatabasePane() {
        return _panel.getDatabasePane();
    }

    /**
     * Update the status bar.
     **/
    public void updateStatus() {
        try {
            String userName = "";
            String collectionName = "";
            String viewName = "";
            DatabasePane dbpane = getDatabasePane();
            if (dbpane != null) {
                User user = dbpane.getCurrentUser();
                if (user != null) {
                    userName = user.getName();
                    WordCollection col = dbpane.getCurrentCollection();
                    if (col != null) {
                        collectionName = col.getName();
                        collectionName += (col.getExportTimestamp() == null)
                            ? ""
                            : " [" + col.getExportTimestamp().toString() + "]";
                        View view = col.getViewByID(_database.getCurrentSetting().getViewID());
                        if (view != null) {
                            viewName = view.getName();
                        }
                    }
                }
            }
            Messages messages = AppPrefs.getInstance().getMessages();
            _panel._curUser.setText(messages.getString("lblCurrentUser") + " " + userName);
            _panel._curCollection.setText(
                messages.getString("lblCurrentCollection") + " " + collectionName);
            _panel._curView.setText(messages.getString("lblCurrentView") + " " + viewName);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Main panel for this frame.
     **/
    private final class MainPanel extends JPanel implements Refreshable {
        MainPanel() {
            super(new BorderLayout());
            JPanel status =
                new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 20, 0));
            status.add(_curUser);
            status.add(_curCollection);
            status.add(_curView);
            status.setBorder(BorderFactory.createEtchedBorder());
            _menu = new MainMenu();
            setJMenuBar(_menu);
            this.add(new MainToolBar(), BorderLayout.NORTH);
            this.add(status, BorderLayout.SOUTH);
        }

        public void refresh() throws DatabaseException {
            if (_dbpane != null) {
                _dbpane.refresh();
            }
        }

        void setDatabasePane(DatabasePane pane) throws DatabaseException {
            if (_dbpane != null) {
                this.remove(_dbpane);
            }
            _dbpane = pane;
            if (pane != null) {
                this.add(pane, BorderLayout.CENTER);
            }
        }

        DatabasePane getDatabasePane() {
            return _dbpane;
        }

        private final class StatusLabel extends JLabel {
            StatusLabel(String labelKey) {
                super(AppPrefs.getInstance().getMessages().getString(labelKey));
            }
        }

        private final JLabel _curUser = new StatusLabel("lblCurrentUser");
        private final JLabel _curView = new StatusLabel("lblCurrentView");
        private final JLabel _curCollection = new StatusLabel("lblCurrentCollection");
        private DatabasePane _dbpane;
    }

    /**
     * Main Menu for this frame.
     **/
    private final class MainMenu extends JMenuBar {

        MainMenu() {
            Messages messages = AppPrefs.getInstance().getMessages();

            _fileMenu = new JMenu(messages.getString("mnuFile"));
            _fileMenu.setMnemonic(messages.getChar("accFile"));
            _fileMenu.add(new WMenuItem(NewUser.getInstance()));
            _fileMenu.add(new WMenuItem(NewCollection.getInstance()));
            _fileMenu.addSeparator();
            _fileMenu.add(new WMenuItem(ImportWordsurv.getInstance()));
            _fileMenu.add(new WMenuItem(ImportXML.getInstance()));
            _fileMenu.add(new WMenuItem(ExportXML.getInstance()));
            _fileMenu.add(new WMenuItem(ExportViewXML.getInstance()));
            _fileMenu.add(new WMenuItem(ExportMetadataXML.getInstance()));
            _fileMenu.addSeparator();
            _fileMenu.add(new WMenuItem(DeleteUser.getInstance()));
            _fileMenu.add(new WMenuItem(DeleteCollection.getInstance()));
            // Note: Keep replication off menu until ready.
            //           _fileMenu.addSeparator();
            //           _fileMenu.add(new WMenuItem(Replicate.getInstance()));
            _fileMenu.addSeparator();
            _fileMenu.add(new WMenuItem(Exit.getInstance()));

            JMenu viewMenu = new JMenu(messages.getString("mnuView"));
            viewMenu.setMnemonic(messages.getChar("accView"));
            // Note: Remove Refresh
            //            viewMenu.add(new WMenuItem(Refresh.getInstance()));
            viewMenu.add(new WMenuItem(ShowTree.getInstance()));
            viewMenu.add(new WMenuItem(ChooseFont.getInstance()));

            JMenu helpMenu = new JMenu(messages.getString("mnuHelp"));
            helpMenu.setMnemonic(messages.getChar("accHelp"));
            helpMenu.add(new WMenuItem(WordcorrHelp.getInstance()));
            helpMenu.add(new WMenuItem(About.getInstance()));

            this.add(_fileMenu);
            this.add(viewMenu);
            this.add(helpMenu);
        }

        private final JMenu _fileMenu;
    }

    /**
     * Main Toolbar for this frame.
     **/
    private final class MainToolBar extends JToolBar {
        MainToolBar() {
            setFloatable(false);
            add(NewUser.getInstance());
            add(NewCollection.getInstance());
            // Note Remove Refresh button.
            //            addSeparator();
            //            add(Refresh.getInstance());
        }
    }

    private MainPanel _panel;
    private MainMenu _menu;
    private Database _database;
    private final List _disableOnClose = new ArrayList();
    private final List _userOnly = new ArrayList();
}