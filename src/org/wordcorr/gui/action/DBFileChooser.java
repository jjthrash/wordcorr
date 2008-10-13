package org.wordcorr.gui.action;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import org.wordcorr.gui.AppPrefs;

/**
 * FileChooser that only accepts directories and HSQL databases.
 * @author Keith Hamasaki
 **/
public class DBFileChooser extends JFileChooser {

    public DBFileChooser(String currentDir) {
        super(currentDir);
        setFileFilter(new DBFileFilter());
        setFileView(new DBFileView());
        setAcceptAllFileFilterUsed(false);
    }

    private class DBFileFilter extends FileFilter {
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }

            String name = file.getName();
            int dotindex = name.indexOf(".");
            if (dotindex != -1) {
                String ext = name.substring(dotindex + 1);
                if (ext.equals("script")) {
                    return true;
                }
            }

            return false;
        }

        public String getDescription() {
            return AppPrefs.getInstance().getMessages().getString("lblDBFileFilterDesc");
        }
    }

    private class DBFileView extends FileView {
        public String getName(File f) {
            String name = f.getName();
            int dotindex = name.indexOf(".");
            return dotindex == -1 ? null : name.substring(0, dotindex);
        }
    }
}
