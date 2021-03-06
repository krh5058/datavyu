/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.datavyu.views;

import java.awt.Component;
import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;

import org.datavyu.Configuration;
import org.datavyu.Datavyu;
import org.datavyu.RecentFiles;

import org.datavyu.Datavyu.Platform;

import org.datavyu.controllers.CreateNewCellC;
import org.datavyu.controllers.DeleteCellC;
import org.datavyu.controllers.DeleteColumnC;
import org.datavyu.controllers.NewProjectC;
import org.datavyu.controllers.NewVariableC;
import org.datavyu.controllers.OpenC;
import org.datavyu.controllers.RunScriptC;
import org.datavyu.controllers.SaveC;
import org.datavyu.controllers.VocabEditorC;
import org.datavyu.controllers.project.ProjectController;

import org.datavyu.event.component.FileDropEvent;
import org.datavyu.event.component.FileDropEventListener;

import org.datavyu.util.ArrayDirection;
import org.datavyu.util.FileFilters.CSVFilter;
import org.datavyu.util.FileFilters.MODBFilter;
import org.datavyu.util.FileFilters.OPFFilter;
import org.datavyu.util.FileFilters.SHAPAFilter;

import org.datavyu.views.discrete.SpreadsheetColumn;
import org.datavyu.views.discrete.SpreadsheetPanel;
import org.datavyu.views.discrete.layouts.SheetLayoutFactory.SheetLayoutType;

import com.usermetrix.jclient.Logger;
import com.usermetrix.jclient.UserMetrix;
import java.util.List;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;
import org.datavyu.controllers.AutosaveC;
import org.datavyu.models.db.Cell;
import org.datavyu.models.db.Datastore;
import org.datavyu.models.db.UserWarningException;
import org.datavyu.models.db.Variable;
import org.datavyu.undoableedits.RemoveCellEdit;
import org.datavyu.undoableedits.RemoveVariableEdit;
import org.datavyu.undoableedits.RunScriptEdit;
import org.datavyu.undoableedits.SpreadsheetUndoManager;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.datavyu.controllers.ExportDatabaseFileC;
import org.datavyu.util.FileFilters.CellCSVFilter;
import org.datavyu.util.FileFilters.FrameCSVFilter;


/**
 * The main FrameView, representing the interface for Datavyu the user will
 * initially see.
 */
public final class DatavyuView extends FrameView
    implements FileDropEventListener {

    /** The directory holding a users favourite scripts. */
    static final String FAV_DIR = "favourites";

    // Variable for the amount to raise the font size by when zooming.
    public static final int ZOOM_INTERVAL = 2;
    public static final int ZOOM_DEFAULT_SIZE = 14;

    // Variables to set the maximum zoom and minimum zoom.
    public static final int ZOOM_MAX_SIZE = 42;
    public static final int ZOOM_MIN_SIZE = 8;

    /** The logger for this class. */
    private static Logger LOGGER = UserMetrix.getLogger(DatavyuView.class);

    /** The spreadsheet panel for this view. */
    private SpreadsheetPanel panel;

    private static boolean redraw = true;

    private DVProgressBar progressBar;
    private OpenTask task;

    /**
     * undo system elements
     */
    SpreadsheetUndoManager spreadsheetUndoManager; // history list

    public SpreadsheetUndoManager getSpreadsheetUndoManager() {
        return spreadsheetUndoManager;
    }


    UndoableEditSupport undoSupport; // event support

    public UndoableEditSupport getUndoSupport() {
        return undoSupport;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem ShowAllVariablesMenuItem;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem updateMenuItem;
    private javax.swing.JMenuItem supportMenuItem;
    private javax.swing.JMenuItem guideMenuItem;
    private javax.swing.JMenuItem changeVarNameMenuItem;
    private javax.swing.JMenu controllerMenu;
    private javax.swing.JMenuItem deleteCellMenuItem;
    private javax.swing.JMenuItem deleteColumnMenuItem;
    private javax.swing.JMenuItem favScripts;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem hideSelectedColumnsMenuItem;
    private javax.swing.JMenuItem historySpreadSheetMenuItem;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator10;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JPopupMenu.Separator jSeparator9;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem newCellLeftMenuItem;
    private javax.swing.JMenuItem newCellMenuItem;
    private javax.swing.JMenuItem newCellRightMenuItem;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JMenuItem newVariableMenuItem;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenu openRecentFileMenu;
    private javax.swing.JMenuItem pullMenuItem;
    private javax.swing.JMenuItem pushMenuItem;
    private javax.swing.JMenuItem qtControllerItem;
    private javax.swing.JMenuItem videoConverterMenuItem;
    private javax.swing.JMenuItem recentScriptsHeader;
    private javax.swing.JMenuItem redoSpreadSheetMenuItem;
    private javax.swing.JMenuItem resetZoomMenuItem;
    private javax.swing.JMenu runRecentScriptMenu;
    private javax.swing.JMenuItem runScriptMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem exportMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JMenu scriptMenu;
    private javax.swing.JMenuItem showSpreadsheetMenuItem;
    private javax.swing.JMenu spreadsheetMenu;
    private javax.swing.JCheckBoxMenuItem strongTemporalOrderMenuItem;
    private javax.swing.JMenuItem undoSpreadSheetMenuItem;
    private javax.swing.JMenuItem vocabEditorMenuItem;
    private javax.swing.JCheckBoxMenuItem weakTemporalOrderMenuItem;
    private javax.swing.JMenu windowMenu;
    private javax.swing.JMenuItem zoomInMenuItem;
    private javax.swing.JMenu zoomMenu;
    private javax.swing.JMenuItem zoomOutMenuItem;
    // End of variables declaration//GEN-END:variables

    /**
     * Constructor.
     *
     * @param app
     *            The SingleFrameApplication that invoked this main FrameView.
     */
    public DatavyuView(final SingleFrameApplication app) {
        super(app);

        KeyboardFocusManager manager = KeyboardFocusManager
            .getCurrentKeyboardFocusManager();

        manager.addKeyEventDispatcher(new KeyEventDispatcher() {

                /**
                 * Dispatches the keystroke to the correct action.
                 *
                 * @param evt The event that triggered this action.
                 * @return true if the KeyboardFocusManager should take no further
                 * action with regard to the KeyEvent; false otherwise.
                 */
                @Override
                public boolean dispatchKeyEvent(final KeyEvent evt) {

                    // Pass the keyevent onto the keyswitchboard so that it can
                    // route it to the correct action.
//                    spreadsheetMenuSelected(null);

                    return Datavyu.getApplication().dispatchKeyEvent(evt);
                }
            });

        // generated GUI builder code
        initComponents();

        // BugzID:492 - Set the shortcut for new cell, so a keystroke that won't
        // get confused for the "carriage return". The shortcut for new cells
        // is handled in Datavyu.java
        newCellMenuItem.setAccelerator(KeyStroke.getKeyStroke('\u21A9'));

        // BugzID:521 + 468 - Define accelerator keys based on Operating system.
        int keyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        weakTemporalOrderMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_T, keyMask));

        strongTemporalOrderMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_T, InputEvent.SHIFT_MASK | keyMask));

        // Set zoom in to keyMask + '+'
        zoomInMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS,
                keyMask));

        // Set zoom out to keyMask + '-'
        zoomOutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
                keyMask));

        // Set reset zoom to keyMask + '0'
        resetZoomMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0,
                keyMask));

        // Set the save accelerator to keyMask + 'S'
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                keyMask));

        // Set the save as accelerator to keyMask + shift + 'S'
        saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                keyMask | InputEvent.SHIFT_MASK));

        // Set the open accelerator to keyMask + 'o';
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                keyMask));

        // Set the new accelerator to keyMask + 'N';
        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                keyMask));

        // Set the new accelerator to keyMask + 'L';
        newCellLeftMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
                keyMask));

        // Set the new accelerator to keyMask + 'R';
        newCellRightMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_R, keyMask));

        // Set the show spreadsheet accelrator to F5.
        showSpreadsheetMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_F5, 0));

        // Set the undo accelerator to keyMask + 'Z';
        undoSpreadSheetMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_Z, keyMask));

        // Set the redo accelerator to keyMask + 'Y'
        redoSpreadSheetMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_Y, keyMask));


        if (panel != null) {
            panel.deregisterListeners();
            panel.removeFileDropEventListener(this);
        }

        panel = new SpreadsheetPanel(Datavyu.getProjectController().getDB(), null);
        panel.registerListeners();
        panel.addFileDropEventListener(this);
        setComponent(panel);
        
        System.out.println(getComponent());

        // initialize the undo/redo system
        spreadsheetUndoManager = new SpreadsheetUndoManager();
        undoSupport = new UndoableEditSupport();
        undoSupport.addUndoableEditListener(new UndoAdapter());
        refreshUndoRedo();
        //////

        //Jakrabbit Menu
        pushMenuItem.setVisible(false);
        pullMenuItem.setVisible(false);
        jSeparator10.setVisible(false);
        
    }


    public void checkForAutosavedFile() {
        // Check for autosaved file (crash condition)
        try {
            File tempfile = File.createTempFile("test","");
            String path = FilenameUtils.getFullPath(tempfile.getPath());
            tempfile.delete();
            File folder = new File(path);
            File[] listOfFiles = folder.listFiles();
            for (File f : listOfFiles) {
                if ((f.isFile()) &&
                     (
                      (FilenameUtils.wildcardMatchOnSystem(f.getName(), "~*.opf")) ||
                      (FilenameUtils.wildcardMatchOnSystem(f.getName(), "~*.csv"))
                     )
                   ) { // the last time datavyu crashed

                   // Show the Dialog
                   if (JOptionPane.showConfirmDialog(null,
                           "Openshapa has detected an unsaved file. Would you like recover this file ?",
                           "OpenShapa",
                           JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        openRecoveredFile(f);
                        this.saveAs();
                   }
                   // delete the recovered file
                   f.delete();
                }
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(DatavyuView.class.getName()).log(Level.SEVERE, null, ex);
        }


        // initialize autosave feature
        AutosaveC.setInterval(1); // five minutes
    }



    /**
     * Update the title of the application.
     */
    public void updateTitle() {
        // Show the project name instead of database.
        JFrame mainFrame = Datavyu.getApplication().getMainFrame();
        ResourceMap rMap = Datavyu.getApplication().getContext()
                                    .getResourceMap(Datavyu.class);
        String postFix = "";
        ProjectController projectController = Datavyu.getProjectController();

        if (projectController.isChanged()) {
            postFix = "*";
        }

        String extension = "";
        final FileFilter lastSaveOption = projectController.getLastSaveOption();

        if (lastSaveOption instanceof SHAPAFilter) {
            extension = ".shapa";
        } else if (lastSaveOption instanceof CSVFilter) {
            extension = ".csv";
        } else if (lastSaveOption instanceof MODBFilter) {
            extension = ".odb";
        } else if (lastSaveOption instanceof OPFFilter) {
            extension = ".opf";
        }

        String projectName = projectController.getProjectName();
        if (projectName == null) {
            projectName = "Project1";
        }

        String title = rMap.getString("Application.title") + " - " + projectName + extension + postFix;

        mainFrame.setTitle(title);
        this.getFrame().setTitle(title);
    }

    /**
     * Action for creating a new project.
     */
    @Action public void showNewProjectForm() {

        if (Datavyu.getApplication().safeQuit()) {
            new NewProjectC();
            // Reset the undo manager
            resetUndoManager();
        }
    }

    /**
     * Action for saving the current database as a file.
     */
    @Action public void save() {

        try {
            SaveC saveC = new SaveC();

            // If the user has not saved before - invoke the saveAs()
            // controller to force the user to nominate a destination file.
            ProjectController projController = Datavyu.getProjectController();

            if (projController.isNewProject()
                    || (projController.getProjectName() == null)) {
                saveAs();
            } else {
                SaveC saveController = new SaveC();

                // Force people to use new
                if ((projController.getLastSaveOption() instanceof SHAPAFilter)
                        || (projController.getLastSaveOption()
                            instanceof OPFFilter)) {

                    // BugzID:1804 - Need to store the original absolute path of
                    // the
                    // project file so that we can build relative paths to
                    // search when
                    // loading, if the project file is moved around.
                    projController.setOriginalProjectDirectory(
                        projController.getProjectDirectory());

                    projController.updateProject();
                    projController.setLastSaveOption(OPFFilter.INSTANCE);

                    saveController.saveProject(new File(projController.getProjectDirectory(),
                                                        projController.getProjectName() + ".opf"),
                                               projController.getProject(),
                                               projController.getDB());

                    projController.markProjectAsUnchanged();
                    projController.getDB().markAsUnchanged();

                    // Save content just as a database.
                } else {
                    File file = new File(projController.getProjectDirectory(),
                                         projController.getDatabaseFileName());
                    saveC.saveDatabase(file, projController.getDB());

                    projController.markProjectAsUnchanged();

                    projController.getDB().markAsUnchanged();

                }
            }

        } catch (UserWarningException e) {
            e.printStackTrace();
            Datavyu.getApplication().showWarningDialog(e);
        }
    }

    /**
     * Action for saving the current project as a particular file.
     */
    @Action public void saveAs() {
        DatavyuFileChooser jd = new DatavyuFileChooser();

        jd.addChoosableFileFilter(MODBFilter.INSTANCE);
        jd.addChoosableFileFilter(CSVFilter.INSTANCE);
        jd.addChoosableFileFilter(OPFFilter.INSTANCE);
        
        jd.setAcceptAllFileFilterUsed(false);
        jd.setFileFilter(OPFFilter.INSTANCE);

        int result = jd.showSaveDialog(getComponent());

        if (result == JFileChooser.APPROVE_OPTION) {
            save(jd);
        }
    }
    
    /**
     * Action for exporting the current project as a particular file.
     */
    @Action public void exportFile() {
        DatavyuFileChooser jd = new DatavyuFileChooser();
	
	// Not fully implemented
//	jd.addChoosableFileFilter(FrameCSVFilter.INSTANCE);
	
	jd.addChoosableFileFilter(CellCSVFilter.INSTANCE);

        int result = jd.showSaveDialog(getComponent());

        if (result == JFileChooser.APPROVE_OPTION) {
            exportToCSV(jd);
        }
    }
    
    private void exportToCSV(final DatavyuFileChooser fc) {
	ProjectController projController = Datavyu.getProjectController();
        projController.updateProject();
	
	try {
		ExportDatabaseFileC exportC = new ExportDatabaseFileC();
		
		FileFilter filter = fc.getFileFilter();
		String dbFileName = fc.getSelectedFile().getPath();
		if (!dbFileName.endsWith(".csv")) {
			dbFileName = dbFileName.concat(".csv");
		}

		// Only save if the project file does not exists or if the user
		// confirms a file overwrite in the case that the file exists.
		if (!canSave(fc.getSelectedFile().getParent(), dbFileName)) {
			return;
		}
		File f = new File(fc.getSelectedFile().getParent(), dbFileName);
		
		if(filter instanceof FrameCSVFilter) {
			exportC.exportByFrame(dbFileName, projController.getDB());
		}
		else if (filter instanceof CellCSVFilter) {
			exportC.exportAsCells(dbFileName, projController.getDB());
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
    }

    private boolean canSave(final String directory, final String file) {
        File newFile = new File(directory, file);

        return ((newFile.exists()
                    && Datavyu.getApplication().overwriteExisting())
                || !newFile.exists());
    }
    
    private void export(final DatavyuFileChooser fc) {
        ProjectController projController = Datavyu.getProjectController();
        projController.updateProject();

        try {
            SaveC saveC = new SaveC();

            FileFilter filter = fc.getFileFilter();

            if (filter instanceof CSVFilter) {
                String dbFileName = fc.getSelectedFile().getName();

                if (!dbFileName.endsWith(".csv")) {
                    dbFileName = dbFileName.concat(".csv");
                }

                // Only save if the project file does not exists or if the user
                // confirms a file overwrite in the case that the file exists.
                if (!canSave(fc.getSelectedFile().getParent(), dbFileName)) {
                    return;
                }

                File f = new File(fc.getSelectedFile().getParent(), dbFileName);
                saveC.saveDatabase(f, projController.getDB());

                projController.getDB().setName(dbFileName);
                projController.setProjectName(dbFileName);
                projController.setProjectDirectory(fc.getSelectedFile().getParent());
                projController.setDatabaseFileName(dbFileName);

                // Save as a ODB database
            } else if (filter instanceof MODBFilter) {
                String dbFileName = fc.getSelectedFile().getName();

                if (!dbFileName.endsWith(".odb")) {
                    dbFileName = dbFileName.concat(".odb");
                }

                // Only save if the project file does not exists or if the user
                // confirms a file overwrite in the case that the file exists.
                if (!canSave(fc.getSelectedFile().getParent(), dbFileName)) {
                    return;
                }

                File f = new File(fc.getSelectedFile().getParent(), dbFileName);
                saveC.saveDatabase(f, projController.getDB());

                if (dbFileName.lastIndexOf('.') != -1) {
                    dbFileName = dbFileName.substring(0,
                            dbFileName.lastIndexOf('.'));
                }

                projController.getDB().setName(dbFileName);
                projController.setProjectDirectory(fc.getSelectedFile()
                    .getParent());
                projController.setDatabaseFileName(dbFileName);

                // Save as a project
            } else if (filter instanceof OPFFilter) {
                String archiveName = fc.getSelectedFile().getName();

                if (!archiveName.endsWith(".opf")) {
                    archiveName = archiveName.concat(".opf");
                }

                // Only save if the project file does not exists or if the user
                // confirms a file overwrite in the case that the file exists.
                if (!canSave(fc.getSelectedFile().getParent(), archiveName)) {
                    return;
                }

                // Send it off to the controller
                projController.setProjectName(archiveName);

                // BugzID:1804 - Need to store the original absolute path of the
                // project file so that we can build relative paths to search
                // when
                // loading, if the project file is moved around.
                projController.setOriginalProjectDirectory(fc.getSelectedFile()
                    .getParent());

                projController.updateProject();
                saveC.saveProject(new File(fc.getSelectedFile().getParent(),
                                           archiveName),
                                  projController.getProject(),
                                  projController.getDB());
                projController.setProjectDirectory(fc.getSelectedFile().getParent());

            }

            projController.setLastSaveOption(filter);
            projController.markProjectAsUnchanged();
            projController.getDB().markAsUnchanged();

        } catch (UserWarningException e) {
            Datavyu.getApplication().showWarningDialog(e);
        }
    }

    private void save(final DatavyuFileChooser fc) {
        ProjectController projController = Datavyu.getProjectController();
        projController.updateProject();

        try {
            SaveC saveC = new SaveC();

            FileFilter filter = fc.getFileFilter();

            if (filter instanceof CSVFilter) {
                String dbFileName = fc.getSelectedFile().getName();

                if (!dbFileName.endsWith(".csv")) {
                    dbFileName = dbFileName.concat(".csv");
                }

                // Only save if the project file does not exists or if the user
                // confirms a file overwrite in the case that the file exists.
                if (!canSave(fc.getSelectedFile().getParent(), dbFileName)) {
                    return;
                }

                File f = new File(fc.getSelectedFile().getParent(), dbFileName);
                saveC.saveDatabase(f, projController.getDB());

                projController.getDB().setName(dbFileName);
                projController.setProjectName(dbFileName);
                projController.setProjectDirectory(fc.getSelectedFile().getParent());
                projController.setDatabaseFileName(dbFileName);

                // Save as a ODB database
            } else if (filter instanceof MODBFilter) {
                String dbFileName = fc.getSelectedFile().getName();

                if (!dbFileName.endsWith(".odb")) {
                    dbFileName = dbFileName.concat(".odb");
                }

                // Only save if the project file does not exists or if the user
                // confirms a file overwrite in the case that the file exists.
                if (!canSave(fc.getSelectedFile().getParent(), dbFileName)) {
                    return;
                }

                File f = new File(fc.getSelectedFile().getParent(), dbFileName);
                saveC.saveDatabase(f, projController.getDB());

                if (dbFileName.lastIndexOf('.') != -1) {
                    dbFileName = dbFileName.substring(0,
                            dbFileName.lastIndexOf('.'));
                }

                projController.getDB().setName(dbFileName);
                projController.setProjectDirectory(fc.getSelectedFile()
                    .getParent());
                projController.setDatabaseFileName(dbFileName);

                // Save as a project
            } else if (filter instanceof OPFFilter) {
                String archiveName = fc.getSelectedFile().getName();

                if (!archiveName.endsWith(".opf")) {
                    archiveName = archiveName.concat(".opf");
                }

                // Only save if the project file does not exists or if the user
                // confirms a file overwrite in the case that the file exists.
                if (!canSave(fc.getSelectedFile().getParent(), archiveName)) {
                    return;
                }

                // Send it off to the controller
                projController.setProjectName(archiveName);

                // BugzID:1804 - Need to store the original absolute path of the
                // project file so that we can build relative paths to search
                // when
                // loading, if the project file is moved around.
                projController.setOriginalProjectDirectory(fc.getSelectedFile()
                    .getParent());

                projController.updateProject();
                saveC.saveProject(new File(fc.getSelectedFile().getParent(),
                                           archiveName),
                                  projController.getProject(),
                                  projController.getDB());
                projController.setProjectDirectory(fc.getSelectedFile().getParent());

            }

            projController.setLastSaveOption(filter);
            projController.markProjectAsUnchanged();
            projController.getDB().markAsUnchanged();

        } catch (UserWarningException e) {
            Datavyu.getApplication().showWarningDialog(e);
        }
    }

    /**
     * Action for loading an Datavyu project from disk.
     */
    @Action public void open() {

        if (Datavyu.getApplication().safeQuit()) {
            DatavyuFileChooser jd = new DatavyuFileChooser();

            jd.addChoosableFileFilter(CSVFilter.INSTANCE);
            jd.addChoosableFileFilter(SHAPAFilter.INSTANCE);
            jd.addChoosableFileFilter(OPFFilter.INSTANCE);

            jd.setFileFilter(OPFFilter.INSTANCE);
            int result = jd.showOpenDialog(getComponent());

            if (result == JFileChooser.APPROVE_OPTION) {
                open(jd.getSelectedFile());
            }
        }
    }

    /** Simulate loading an Datavyu project from file chooser. */
    public void open(final File file) {

        if (!Datavyu.getApplication().safeQuit()) {
            return;
        }

        DatavyuFileChooser fc = new DatavyuFileChooser();
        fc.setVisible(false);
        fc.setSelectedFile(file);

        String ext = FilenameUtils.getExtension(file.getAbsolutePath());

        if ("shapa".equalsIgnoreCase(ext)) {
            fc.setFileFilter(SHAPAFilter.INSTANCE);
            open(fc);
        } else if ("csv".equalsIgnoreCase(ext)) {
            fc.setFileFilter(CSVFilter.INSTANCE);
            open(fc);
        } else if ("odb".equalsIgnoreCase(ext)) {
            fc.setFileFilter(MODBFilter.INSTANCE);
            open(fc);
        } else if ("opf".equalsIgnoreCase(ext)) {
            fc.setFileFilter(OPFFilter.INSTANCE);
            open(fc);
        }
    }

    class OpenTask extends SwingWorker<Void, Void> {
        private DVProgressBar progressBar;
        private DatavyuFileChooser jd;

        public OpenTask(DVProgressBar progressBar, final DatavyuFileChooser jd) {
            this.jd = jd;
            this.progressBar = progressBar;
        }

        @Override
        public Void doInBackground() {

            progressBar.setProgress(0, "Preparing spreadsheet");
            FileFilter filter = jd.getFileFilter();
            clearSpreadsheet();

            progressBar.setProgress(10, "Opening project");
            boolean open_success = false;
            if ((filter == SHAPAFilter.INSTANCE)  || (filter == OPFFilter.INSTANCE)) {
                // Opening a project or project archive file
                open_success = openProject(jd.getSelectedFile());
            } else {
                // Opening a database file
                open_success = openDatabase(jd.getSelectedFile());
            }

            if (!open_success)
            {
                progressBar.setError("Error opening project!");
                return null;
            }

            progressBar.setProgress(40, "Project opened");

            // BugzID:449 - Set filename in spreadsheet window and database if the database name is undefined.
            ProjectController pController = Datavyu.getProjectController();
            pController.setProjectName(jd.getSelectedFile().getName());
            pController.setLastSaveOption(filter);

            // Display any changes to the database.
            if (progressBar.setProgress(50, "Loading project into spreadsheet")) {
                progressBar.close();
                return null;
            }

            /* updates the progressBar up to nearly 100% */
            showSpreadsheet(progressBar);

            // Default is to highlight cells when created - clear selection on load.
            panel.clearCellSelection();

            // The project we just opened doesn't really contain any unsaved changes.
            pController.markProjectAsUnchanged();
            pController.getDB().markAsUnchanged();

            // Update the list of recently opened files.
            RecentFiles.rememberProject(jd.getSelectedFile());

            if (progressBar.setProgress(100, "Completed!")) {
                progressBar.close();
                return null;
            }

            progressBar.close();
            return null;
        }
    }

    /**
     * Helper method for opening a file from disk.
     *
     * @param jd The file chooser to use.
     */
    private void open(final DatavyuFileChooser jd) {
        Datavyu.getApplication().resetApp();

        JFrame mainFrame = Datavyu.getApplication().getMainFrame();
        progressBar = new DVProgressBar(mainFrame, false);
        Datavyu.getApplication().show(progressBar);

        task = new OpenTask(progressBar, jd);
        task.execute();
    }

    /**
     * Method for opening a recovered file from disk.
     *
     * @param f The file to open.
     */
    public void openRecoveredFile(final File f) {
        // Clear the current spreadsheet before loading the new content - we
        // need to clean up resources.
        clearSpreadsheet();
        String filename = FilenameUtils.getBaseName(f.getAbsolutePath());
        String ext = FilenameUtils.getExtension(f.getAbsolutePath());
        //ext = ext.substring(10);
        //f.renameTo(new File(filename + "." + ext));
        // Opening a project or project archive file
        if (ext.equalsIgnoreCase("opf")) {
            openProject(f);
            // Opening a database file
        } else {
            openDatabase(f);
        }

        // Display any changes to the database.
        showSpreadsheet();

        // Default is to highlight cells when created - clear selection on load.
        panel.clearCellSelection();
    }



    private boolean openDatabase(final File databaseFile) {

        // Set the database to the freshly loaded database.
        OpenC openC = new OpenC();
        openC.openDatabase(databaseFile);

        // Make a project for the new database.
        if (openC.getDatastore() != null) {
            Datavyu.newProjectController();

            ProjectController projController = Datavyu.getProjectController();

            projController.setDatastore(openC.getDatastore());
            projController.setProjectDirectory(databaseFile.getParent());
            projController.setDatabaseFileName(databaseFile.getName());

            // Reset the undo manager
            resetUndoManager();
            return true;
        }
        return false;
    }

    private boolean openProject(final File projectFile) {
        OpenC openC = new OpenC();
        openC.openProject(projectFile);

        if ((openC.getProject() != null) && (openC.getDatastore() != null)) {
            Datavyu.newProjectController(openC.getProject());
            Datavyu.getProjectController().setDatastore(openC.getDatastore());
            Datavyu.getProjectController().setProjectDirectory(projectFile.getParent());
            Datavyu.getProjectController().loadProject();

            // Reset the undo manager
            resetUndoManager();

            return true;
        }
        return false;
    }

    /**
     * Handles the event for files being dropped onto a component. Only the
     * first file received will be opened.
     *
     * @param evt The event to handle.
     */
    @Override
    public void filesDropped(final FileDropEvent evt) {

        if (!Datavyu.getApplication().safeQuit()) {
            return;
        }

        DatavyuFileChooser fc = new DatavyuFileChooser();
        fc.setVisible(false);

        for (File file : evt.getFiles()) {
            open(file);
        }
    }

    /**
     * Action for creating a new variable.
     */
    @Action public void showNewVariableForm() {
        new NewVariableC();
    }

    /**
     * Action for editing vocabs.
     */
    @Action public void showVocabEditor() {
        new VocabEditorC();
    }

    /**
     * Action for showing the variable list.
     */
    @Action public void showVariableList() {
        Datavyu.getApplication().showVariableList();
    }

    /**
     * Action for showing the quicktime video controller.
     */
    @Action public void showQTVideoController() {
        Datavyu.getApplication().showDataController();
    }
    
    /**
     * Action for showing the video converter dialog.
     */
    @Action public void showVideoConverter() {
        Datavyu.getApplication().showVideoConverter();
    }

    /**
     * Action for showing the about window.
     */
    @Action public void showAboutWindow() {
        Datavyu.getApplication().showAboutWindow();
    }

    /**
     * Action for opening the support site
     */
    @Action public void openSupportSite() {
        Datavyu.getApplication().openSupportSite();
    }

    /**
     * Action for opening the guide
     */
    @Action public void openGuideSite() {
        Datavyu.getApplication().openGuideSite();
    }

    /**
     * Action for showing the update window.
     */
    @Action public void showUpdateWindow() {
        Datavyu.getApplication().showUpdateWindow();
    }

    /**
     * Clears the contents of the spreadsheet.
     */
    public void clearSpreadsheet() {
        panel.removeAll();

        // Create a freash spreadsheet component and redraw the component.
        panel.deregisterListeners();
        panel.removeFileDropEventListener(this);
    }

    /**
     * Action for showing the spreadsheet.
     */
    @Action public void showSpreadsheet() {
        showSpreadsheet(null);
    }
    @Action public void showSpreadsheet(DVProgressBar progressBar) {
        weakTemporalOrderMenuItem.setSelected(false);
        strongTemporalOrderMenuItem.setSelected(false);

        // need to add changes to maintain spreadsheet view after change
        // Create a fresh spreadsheet component and redraw the component.
        if (panel != null) {
            this.clearSpreadsheet();
        }

        
        panel = new SpreadsheetPanel(Datavyu.getProjectController().getDB(), progressBar);
        panel.registerListeners();
        panel.addFileDropEventListener(this);
        setComponent(panel);
        getComponent().revalidate();
        getComponent().repaint();
        getComponent().resetKeyboardActions();
        getComponent().requestFocus();

        // The default is to create cells that are highlighted - ensure that
        // they are deselected.
        panel.clearCellSelection();
    }

    /**
     * Action for invoking a script.
     */
    @Action public void runScript() {
        try {
            RunScriptC scriptC = new RunScriptC();
            // record the effect
            UndoableEdit edit = new RunScriptEdit(scriptC.getScriptFilePath());
            // notify the listeners
            Datavyu.getView().getUndoSupport().postEdit(edit);
            scriptC.execute();
        } catch (IOException e) {
            LOGGER.error("Unable run script", e);
        }
    }

    /**
     * Action for removing columns from the database.
     */
    @Action public void deleteColumn() {
        Datastore ds = Datavyu.getProjectController().getDB();
        List<Variable> selectedVariables = ds.getSelectedVariables();

        // record the effect
        UndoableEdit edit = new RemoveVariableEdit(selectedVariables);

        // perform the operation
        new DeleteColumnC(selectedVariables);

        // notify the listeners
        Datavyu.getView().getUndoSupport().postEdit(edit);
    }

    /**
     * Action for hiding columns.
     */
    @Action public void hideColumn() {
        LOGGER.event("Hidding columns");
        Datastore ds = Datavyu.getProjectController().getDB();

        for (Variable var : ds.getSelectedVariables()) {
            var.setHidden(true);
            var.setSelected(false);
        }

        getComponent().revalidate();
    }

    /**
     * Action for showing all columns.
     */
    @Action public void showAllColumns() {
        LOGGER.event("Showing all columns");
        Datastore ds = Datavyu.getProjectController().getDB();

        for (Variable var : ds.getAllVariables()) {
            if (var.isHidden()) {
                var.setHidden(false);
            }
        }

        getComponent().revalidate();
    }

    /**
     * Action for changing variable name.
     */
    @Action public void changeColumnName() {
        // Only one column should be selected, but just in case, we'll only
        // change the first column
        Variable var = Datavyu.getProjectController().getDB().getSelectedVariables().get(0);

        for (SpreadsheetColumn sCol : panel.getColumns()) {
            if (sCol.getVariable().equals(var)) {
                sCol.showChangeVarNameDialog();

                break;
            }
        }
    }

    /**
     * Action for removing cells from the database.
     */
    @Action public void deleteCells() {
        List<Cell> selectedCells = Datavyu.getProjectController()
                                            .getDB().getSelectedCells();

        // record the effect
        UndoableEdit edit = new RemoveCellEdit(selectedCells);
        // perform the operation
        new DeleteCellC(selectedCells);
        // notify the listeners
        Datavyu.getView().getUndoSupport().postEdit(edit);
    }

    /**
     * Set the SheetLayoutType for the spreadsheet.
     */
    private void setSheetLayout() {
        SheetLayoutType type = SheetLayoutType.Ordinal;

        if (weakTemporalOrderMenuItem.isSelected()) {
            type = SheetLayoutType.WeakTemporal;
        } else if (strongTemporalOrderMenuItem.isSelected()) {
            type = SheetLayoutType.StrongTemporal;
        }


        panel.setLayoutType(type);
    }

    public void setRedraw(boolean b) {
        redraw = b;
    }

    public boolean getRedraw() {
        return redraw;
    }

    /**
     * Checks if changes should be discarded, if so (or no changes) then quits.
     */
    @Action public void safeQuit() {
        Datavyu.getApplication().exit();
    }

    /**
     * Undo Action.
     */
    @Action public void history() {
        //JOptionPane.showMessageDialog(null, "Undo History.", "History", JOptionPane.INFORMATION_MESSAGE);
        Datavyu.getApplication().showHistory();
    }

    /**
     * Undo Action.
     */
    @Action public void undo() {
         spreadsheetUndoManager.undo();
         refreshUndoRedo();
    }

    /**
     * Redo Action.
     */
    @Action public void redo() {
         spreadsheetUndoManager.redo();
         refreshUndoRedo();
    }

    /**
     * Push Action.
     */
    @Action public void push() {
/*
        System.out.println("push");
        Jackrabbit jr = Jackrabbit.getJackRabbit();
        jr.push();
*/
    }

    /**
     * Redo Action.
     */
    @Action public void pull() {
        System.out.println("pull");
/*
        Jackrabbit jr = Jackrabbit.getJackRabbit();
        jr.pull();
*/
    }

    private void resetUndoManager() {
        spreadsheetUndoManager.discardAllEdits();
        refreshUndoRedo();
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        newMenuItem = new javax.swing.JMenuItem();
        openMenuItem = new javax.swing.JMenuItem();
        openRecentFileMenu = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JSeparator();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
	exportMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator fileMenuSeparator = new javax.swing.JSeparator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        spreadsheetMenu = new javax.swing.JMenu();
        showSpreadsheetMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItem1 = new javax.swing.JMenuItem();
        newVariableMenuItem = new javax.swing.JMenuItem();
        vocabEditorMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        newCellMenuItem = new javax.swing.JMenuItem();
        newCellLeftMenuItem = new javax.swing.JMenuItem();
        newCellRightMenuItem = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        changeVarNameMenuItem = new javax.swing.JMenuItem();
        hideSelectedColumnsMenuItem = new javax.swing.JMenuItem();
        ShowAllVariablesMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        deleteColumnMenuItem = new javax.swing.JMenuItem();
        deleteCellMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        historySpreadSheetMenuItem = new javax.swing.JMenuItem();
        undoSpreadSheetMenuItem = new javax.swing.JMenuItem();
        redoSpreadSheetMenuItem = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        weakTemporalOrderMenuItem = new javax.swing.JCheckBoxMenuItem();
        strongTemporalOrderMenuItem = new javax.swing.JCheckBoxMenuItem();
        zoomMenu = new javax.swing.JMenu();
        zoomInMenuItem = new javax.swing.JMenuItem();
        zoomOutMenuItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        resetZoomMenuItem = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JPopupMenu.Separator();
        pushMenuItem = new javax.swing.JMenuItem();
        pullMenuItem = new javax.swing.JMenuItem();
        controllerMenu = new javax.swing.JMenu();
        qtControllerItem = new javax.swing.JMenuItem();
	videoConverterMenuItem = new javax.swing.JMenuItem();
        scriptMenu = new javax.swing.JMenu();
        runScriptMenuItem = new javax.swing.JMenuItem();
        runRecentScriptMenu = new javax.swing.JMenu();
        recentScriptsHeader = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        favScripts = new javax.swing.JMenuItem();
        windowMenu = new javax.swing.JMenu();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();
        updateMenuItem = new javax.swing.JMenuItem();
        supportMenuItem = new javax.swing.JMenuItem();
        guideMenuItem = new javax.swing.JMenuItem();

        mainPanel.setName("mainPanel"); // NOI18N

        jLabel1.setName("jLabel1"); // NOI18N

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .add(119, 119, 119)
                .add(jLabel1)
                .addContainerGap(149, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .add(55, 55, 55)
                .add(jLabel1)
                .addContainerGap(184, Short.MAX_VALUE))
        );
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.datavyu.Datavyu.class).getContext().getResourceMap(DatavyuView.class);
        resourceMap.injectComponents(mainPanel);

        menuBar.setName("menuBar"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(org.datavyu.Datavyu.class).getContext().getActionMap(DatavyuView.class, this);
        fileMenu.setAction(actionMap.get("saveAs")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        newMenuItem.setAction(actionMap.get("showNewProjectForm")); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/datavyu/views/resources/DatavyuView"); // NOI18N
        newMenuItem.setText(bundle.getString("file_new.text")); // NOI18N
        newMenuItem.setName("newMenuItem"); // NOI18N
        fileMenu.add(newMenuItem);

        openMenuItem.setAction(actionMap.get("open")); // NOI18N
        openMenuItem.setText(bundle.getString("file_open.text")); // NOI18N
        openMenuItem.setName(bundle.getString("file_open.text")); // NOI18N
        fileMenu.add(openMenuItem);

        openRecentFileMenu.setName("openRecentFileMenu"); // NOI18N
        openRecentFileMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                openRecentFileMenuMenuSelected(evt);
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
        });

        jMenuItem2.setEnabled(false);
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        openRecentFileMenu.add(jMenuItem2);

        fileMenu.add(openRecentFileMenu);

        jSeparator7.setName("jSeparator7"); // NOI18N
        fileMenu.add(jSeparator7);

        saveMenuItem.setAction(actionMap.get("save")); // NOI18N
        saveMenuItem.setName("saveMenuItem"); // NOI18N
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setAction(actionMap.get("saveAs")); // NOI18N
        saveAsMenuItem.setName("saveAsMenuItem"); // NOI18N
        fileMenu.add(saveAsMenuItem);
	
	exportMenuItem.setAction(actionMap.get("exportFile")); // NOI18N
        exportMenuItem.setName("exportMenuItem"); // NOI18N
        fileMenu.add(exportMenuItem);

        fileMenuSeparator.setName("fileMenuSeparator"); // NOI18N
        if (Datavyu.getPlatform() != Platform.MAC) {
            fileMenu.add(fileMenuSeparator);
        }

        exitMenuItem.setAction(actionMap.get("safeQuit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        if (Datavyu.getPlatform() != Platform.MAC) {
            fileMenu.add(exitMenuItem);
        }

        menuBar.add(fileMenu);

        spreadsheetMenu.setAction(actionMap.get("showQTVideoController")); // NOI18N
        spreadsheetMenu.setName("spreadsheetMenu"); // NOI18N
        spreadsheetMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuSelected(javax.swing.event.MenuEvent evt) {
//                spreadsheetMenuSelected(evt);
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
        });

        showSpreadsheetMenuItem.setAction(actionMap.get("showSpreadsheet")); // NOI18N
        showSpreadsheetMenuItem.setName("showSpreadsheetMenuItem"); // NOI18N
        spreadsheetMenu.add(showSpreadsheetMenuItem);

        jSeparator1.setName("jSeparator1"); // NOI18N
        spreadsheetMenu.add(jSeparator1);

        jMenuItem1.setAction(actionMap.get("showNewVariableForm")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        spreadsheetMenu.add(jMenuItem1);

        newVariableMenuItem.setAction(actionMap.get("showVariableList")); // NOI18N
        newVariableMenuItem.setName("newVariableMenuItem"); // NOI18N
        spreadsheetMenu.add(newVariableMenuItem);

        vocabEditorMenuItem.setAction(actionMap.get("showVocabEditor")); // NOI18N
        vocabEditorMenuItem.setName("vocabEditorMenuItem"); // NOI18N
        spreadsheetMenu.add(vocabEditorMenuItem);

        jSeparator2.setName("jSeparator2"); // NOI18N
        spreadsheetMenu.add(jSeparator2);

        newCellMenuItem.setName("newCellMenuItem"); // NOI18N
        newCellMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newCellMenuItemActionPerformed(evt);
            }
        });
        spreadsheetMenu.add(newCellMenuItem);

        newCellLeftMenuItem.setName("newCellLeftMenuItem"); // NOI18N
        newCellLeftMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newCellLeftMenuItemActionPerformed(evt);
            }
        });
        spreadsheetMenu.add(newCellLeftMenuItem);

        newCellRightMenuItem.setName("newCellRightMenuItem"); // NOI18N
        newCellRightMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newCellRightMenuItemActionPerformed(evt);
            }
        });
        spreadsheetMenu.add(newCellRightMenuItem);

        jSeparator8.setName("jSeparator8"); // NOI18N
        spreadsheetMenu.add(jSeparator8);

        changeVarNameMenuItem.setName("changeVarNameMenuItem"); // NOI18N
        changeVarNameMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeVarNameMenuItemActionPerformed(evt);
            }
        });
        spreadsheetMenu.add(changeVarNameMenuItem);

        hideSelectedColumnsMenuItem.setName("hideSelectedColumnsMenuItem"); // NOI18N
        hideSelectedColumnsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideSelectedColumnsMenuItemActionPerformed(evt);
            }
        });
        spreadsheetMenu.add(hideSelectedColumnsMenuItem);

        ShowAllVariablesMenuItem.setName("ShowAllVariablesMenuItem"); // NOI18N
        ShowAllVariablesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ShowAllVariablesMenuItemActionPerformed(evt);
            }
        });
        spreadsheetMenu.add(ShowAllVariablesMenuItem);

        jSeparator3.setName("jSeparator3"); // NOI18N
        spreadsheetMenu.add(jSeparator3);

        deleteColumnMenuItem.setAction(actionMap.get("deleteColumn")); // NOI18N
        deleteColumnMenuItem.setName("deleteColumnMenuItem"); // NOI18N
        spreadsheetMenu.add(deleteColumnMenuItem);

        deleteCellMenuItem.setAction(actionMap.get("deleteCells")); // NOI18N
        deleteCellMenuItem.setName("deleteCellMenuItem"); // NOI18N
        spreadsheetMenu.add(deleteCellMenuItem);

        jSeparator6.setName("jSeparator6"); // NOI18N
        spreadsheetMenu.add(jSeparator6);

        historySpreadSheetMenuItem.setAction(actionMap.get("history")); // NOI18N
        historySpreadSheetMenuItem.setName("historySpreadSheetMenuItem"); // NOI18N
        spreadsheetMenu.add(historySpreadSheetMenuItem);

        undoSpreadSheetMenuItem.setAction(actionMap.get("undo")); // NOI18N
        undoSpreadSheetMenuItem.setName("undoSpreadSheetMenuItem"); // NOI18N
        spreadsheetMenu.add(undoSpreadSheetMenuItem);

        redoSpreadSheetMenuItem.setAction(actionMap.get("redo")); // NOI18N
        redoSpreadSheetMenuItem.setName("redoSpreadSheetMenuItem"); // NOI18N
        spreadsheetMenu.add(redoSpreadSheetMenuItem);

        jSeparator9.setName("jSeparator9"); // NOI18N
        spreadsheetMenu.add(jSeparator9);

        weakTemporalOrderMenuItem.setName("weakTemporalOrderMenuItem"); // NOI18N
        weakTemporalOrderMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                weakTemporalMenuItemActionPerformed(evt);
            }
        });
        spreadsheetMenu.add(weakTemporalOrderMenuItem);

        strongTemporalOrderMenuItem.setName("strongTemporalOrderMenuItem"); // NOI18N
        strongTemporalOrderMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                strongTemporalMenuItemActionPerformed(evt);
            }
        });
        spreadsheetMenu.add(strongTemporalOrderMenuItem);

        zoomMenu.setName("zoomMenu"); // NOI18N

        zoomInMenuItem.setName("zoomInMenuItem"); // NOI18N
        zoomInMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomInMenuItemActionPerformed(evt);
            }
        });
        zoomMenu.add(zoomInMenuItem);

        zoomOutMenuItem.setName("zoomOutMenuItem"); // NOI18N
        zoomOutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOutMenuItemActionPerformed(evt);
            }
        });
        zoomMenu.add(zoomOutMenuItem);

        jSeparator5.setName("jSeparator5"); // NOI18N
        zoomMenu.add(jSeparator5);

        resetZoomMenuItem.setName("resetZoomMenuItem"); // NOI18N
        resetZoomMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetZoomMenuItemActionPerformed(evt);
            }
        });
        zoomMenu.add(resetZoomMenuItem);

        spreadsheetMenu.add(zoomMenu);

        jSeparator10.setName("jSeparator10"); // NOI18N
        spreadsheetMenu.add(jSeparator10);

        pushMenuItem.setAction(actionMap.get("push")); // NOI18N
        pushMenuItem.setName("pushMenuItem"); // NOI18N
        spreadsheetMenu.add(pushMenuItem);

        pullMenuItem.setAction(actionMap.get("pull")); // NOI18N
        pullMenuItem.setName("pullMenuItem"); // NOI18N
        spreadsheetMenu.add(pullMenuItem);

        menuBar.add(spreadsheetMenu);

        controllerMenu.setName("controllerMenu"); // NOI18N

        qtControllerItem.setAction(actionMap.get("showQTVideoController")); // NOI18N
        qtControllerItem.setName("qtControllerItem"); // NOI18N
        controllerMenu.add(qtControllerItem);
	
	videoConverterMenuItem.setAction(actionMap.get("showVideoConverter")); // NOI18N
        videoConverterMenuItem.setName("videoConverterMenuItem"); // NOI18N
        controllerMenu.add(videoConverterMenuItem);

        menuBar.add(controllerMenu);

        scriptMenu.setName("scriptMenu"); // NOI18N
        scriptMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                populateFavourites(evt);
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
        });

        runScriptMenuItem.setAction(actionMap.get("runScript")); // NOI18N
        runScriptMenuItem.setName("runScriptMenuItem"); // NOI18N
        scriptMenu.add(runScriptMenuItem);

        runRecentScriptMenu.setName("runRecentScriptMenu"); // NOI18N
        runRecentScriptMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                populateRecentScripts(evt);
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
        });

        recentScriptsHeader.setEnabled(false);
        recentScriptsHeader.setName("recentScriptsHeader"); // NOI18N
        runRecentScriptMenu.add(recentScriptsHeader);

        scriptMenu.add(runRecentScriptMenu);

        jSeparator4.setName("jSeparator4"); // NOI18N
        scriptMenu.add(jSeparator4);

        favScripts.setEnabled(false);
        favScripts.setName("favScripts"); // NOI18N
        scriptMenu.add(favScripts);

        menuBar.add(scriptMenu);

        windowMenu.setName("windowMenu"); // NOI18N

        menuBar.add(windowMenu);

        helpMenu.setAction(actionMap.get("showVariableList")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutWindow")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        if (Datavyu.getPlatform() != Platform.MAC) {
            helpMenu.add(aboutMenuItem);
        }

        supportMenuItem.setAction(actionMap.get("openSupportSite"));
        supportMenuItem.setName("supportMenuItem");
        //TODO - don't add this on Macs.  Instead it will be in the "Application Menu"
        //if (Datavyu.getPlatform() != Platform.MAC) {
            helpMenu.add(supportMenuItem);
        //}

        guideMenuItem.setAction(actionMap.get("openGuideSite"));
        guideMenuItem.setName("guideMenuItem");
        //TODO - don't add this on Macs.  Instead it will be in the "Application Menu"
        //if (Datavyu.getPlatform() != Platform.MAC) {
            helpMenu.add(guideMenuItem);
        //}

        updateMenuItem.setAction(actionMap.get("showUpdateWindow")); // NOI18N
        updateMenuItem.setName("updateMenuItem"); // NOI18N
        //TODO - don't add this on Macs.  Instead it will be in the "Application Menu"
        //if (Datavyu.getPlatform() != Platform.MAC) {
            helpMenu.add(updateMenuItem);
        //}

        menuBar.add(helpMenu);
        resourceMap.injectComponents(menuBar);

        setMenuBar(menuBar);
    }// </editor-fold>//GEN-END:initComponents

    private void openRecentFileMenuMenuSelected(
        final javax.swing.event.MenuEvent evt) { // GEN-FIRST:event_openRecentFileMenuMenuSelected

        // Flush the menu - excluding the top menu item.
        int size = openRecentFileMenu.getMenuComponentCount();

        for (int i = 1; i < size; i++) {
            openRecentFileMenu.remove(1);
        }

        for (File file : RecentFiles.getRecentProjects()) {
            openRecentFileMenu.add(createRecentFileMenuItem(file));
        }

    } // GEN-LAST:event_openRecentFileMenuMenuSelected

    private void hideSelectedColumnsMenuItemActionPerformed(
        final java.awt.event.ActionEvent evt) { // GEN-FIRST:event_hideSelectedColumnsMenuItemActionPerformed
        hideColumn();
        this.getSpreadsheetPanel().deselectAll();
    } // GEN-LAST:event_hideSelectedColumnsMenuItemActionPerformed

    private void ShowAllVariablesMenuItemActionPerformed(
        final java.awt.event.ActionEvent evt) { // GEN-FIRST:event_ShowAllVariablesMenuItemActionPerformed
        showAllColumns();
        this.getSpreadsheetPanel().deselectAll();
    } // GEN-LAST:event_ShowAllVariablesMenuItemActionPerformed

    private void changeVarNameMenuItemActionPerformed(
        final java.awt.event.ActionEvent evt) { // GEN-FIRST:event_changeVarNameMenuItemActionPerformed
        changeColumnName();
    } // GEN-LAST:event_changeVarNameMenuItemActionPerformed

    private void tileWindowsMenuItemActionPerformed(
        final java.awt.event.ActionEvent evt) { // GEN-FIRST:event_tileWindowsMenuItemActionPerformed
    } // GEN-LAST:event_tileWindowsMenuItemActionPerformed

    /**
     * The action to invoke when the user selects 'strong temporal ordering'.
     *
     * @param evt
     *            The event that fired this action.
     */
    private void strongTemporalMenuItemActionPerformed(
        final java.awt.event.ActionEvent evt) {
        setRedraw(true);
        // GEN-FIRST:event_strongTemporalMenuItemActionPerformed
        weakTemporalOrderMenuItem.setSelected(false);
        setSheetLayout();
    } // GEN-LAST:event_strongTemporalMenuItemActionPerformed

    /**
     * The action to invoke when the user selects 'weak temporal ordering'.
     *
     * @param evt
     *            The event that fired this action.
     */
    private void weakTemporalMenuItemActionPerformed(
        final java.awt.event.ActionEvent evt) {
        setRedraw(true);
        // GEN-FIRST:event_weakTemporalMenuItemActionPerformed
        strongTemporalOrderMenuItem.setSelected(false);
        setSheetLayout();
    } // GEN-LAST:event_weakTemporalMenuItemActionPerformed

    /**
     * The action to invoke when the user selects 'recent scripts' from the
     * scripting menu.
     *
     * @param evt
     *            The event that fired this action.
     */
    private void populateRecentScripts(final javax.swing.event.MenuEvent evt) { // GEN-FIRST:event_populateRecentScripts

        // Flush the menu - excluding the top menu item.
        int size = runRecentScriptMenu.getMenuComponentCount();

        for (int i = 1; i < size; i++) {
            runRecentScriptMenu.remove(1);
        }

        for (File f : RecentFiles.getRecentScripts()) {
            runRecentScriptMenu.add(createScriptMenuItemFromFile(f));
        }
    } // GEN-LAST:event_populateRecentScripts

    /**
     * The action to invoke when the user selects 'scripts' from the main menu.
     *
     * @param evt
     *            The event that fired this action.
     */
    private void populateFavourites(final javax.swing.event.MenuEvent evt) { // GEN-FIRST:event_populateFavourites

        // Favourite script list starts after the 'favScripts' menu item - which
        // is just a stub for a starting point. Search for the favScripts as the
        // starting point for deleting existing scripts from the menu.
        Component[] list = scriptMenu.getMenuComponents();
        int start = 0;

        for (Component c : list) {
            start++;

            if (c.getName().equals("favScripts")) {
                break;
            }
        }

        // Delete every menu item from 'favScripts' down to the end of the list.
        // Favscripts are
        int size = scriptMenu.getMenuComponentCount();

        for (int i = start; i < size; i++) {
            scriptMenu.remove(i);
        }

        // Get list of favourite scripts from the favourites folder.
        File favouritesDir = new File(FAV_DIR);
        String[] children = favouritesDir.list();

        if (children != null) {

            for (String s : children) {
                File f = new File(FAV_DIR + File.separatorChar + s);
                scriptMenu.add(createScriptMenuItemFromFile(f));
            }
        }
    } // GEN-LAST:event_populateFavourites

    /**
     * = Function to 'zoom out' (make font size smaller) by ZOOM_INTERVAL
     * points.
     *
     * @param evt
     *            The event that triggered this action.
     */
    private void zoomInMenuItemActionPerformed(
        final java.awt.event.ActionEvent evt) { // GEN-FIRST:event_zoomInMenuItemActionPerformed
        zoomIn();
    } // GEN-LAST:event_zoomInMenuItemActionPerformed

    public void zoomIn() {
        changeFontSize(ZOOM_INTERVAL);
    }

    /**
     * Function to 'zoom out' (make font size smaller) by ZOOM_INTERVAL points.
     *
     * @param evt
     */
    private void zoomOutMenuItemActionPerformed(
        final java.awt.event.ActionEvent evt) { // GEN-FIRST:event_zoomOutMenuItemActionPerformed
        zoomOut();
    } // GEN-LAST:event_zoomOutMenuItemActionPerformed

    public void zoomOut() {
        changeFontSize(-ZOOM_INTERVAL);
    }

    /**
     * Function to reset the zoom level to the default size.
     *
     * @param evt
     *            The event that triggered this action.
     */
    private void resetZoomMenuItemActionPerformed(
        final java.awt.event.ActionEvent evt) { // GEN-FIRST:event_resetZoomMenuItemActionPerformed

        Configuration config = Configuration.getInstance();
        Font f = config.getSSDataFont();

        changeFontSize(ZOOM_DEFAULT_SIZE - f.getSize());
    } // GEN-LAST:event_resetZoomMenuItemActionPerformed

    /**
     * The method to invoke when the use selects the spreadsheet menu item.
     *
     * @param evt
     *            The event that triggered this action.
     */
    private void spreadsheetMenuSelected(
        final javax.swing.event.MenuEvent evt) { // GEN-FIRST:event_spreadsheetMenuMenuSelected

        ResourceMap rMap = Application.getInstance(Datavyu.class).getContext()
                                      .getResourceMap(DatavyuView.class);

        List<Variable> selectedCols = Datavyu.getProjectController().getDB()
                                               .getSelectedVariables();

        if (selectedCols.isEmpty()) {
            deleteColumnMenuItem.setEnabled(false);
            hideSelectedColumnsMenuItem.setEnabled(false);
            changeVarNameMenuItem.setEnabled(false);
        } else if (selectedCols.size() == 1) {
            deleteColumnMenuItem.setText(rMap.getString(
                    "deleteColumnMenuItemSingle.text"));
            deleteColumnMenuItem.setEnabled(true);
            hideSelectedColumnsMenuItem.setText(rMap.getString(
                    "hideSelectedColumnsMenuItemSingle.text"));
            hideSelectedColumnsMenuItem.setEnabled(true);
            changeVarNameMenuItem.setEnabled(true);
        } else {
            deleteColumnMenuItem.setText(rMap.getString(
                    "deleteColumnMenuItemPlural.text"));
            deleteColumnMenuItem.setEnabled(true);
            hideSelectedColumnsMenuItem.setText(rMap.getString(
                    "hideSelectedColumnsMenuItemPlural.text"));
            hideSelectedColumnsMenuItem.setEnabled(true);
            changeVarNameMenuItem.setEnabled(false);
        }

        List<Cell> selectedCells = Datavyu.getProjectController().getDB()
                                            .getSelectedCells();

        if (selectedCells.isEmpty()) {
            deleteCellMenuItem.setEnabled(false);
        } else if (selectedCells.size() == 1) {
            deleteCellMenuItem.setText(rMap.getString(
                    "deleteCellMenuItemSingle.text"));
            deleteCellMenuItem.setEnabled(true);
        } else {
            deleteCellMenuItem.setText(rMap.getString(
                    "deleteCellMenuItemPlural.text"));
            deleteCellMenuItem.setEnabled(true);
        }

        if (panel.getAdjacentSelectedCells(ArrayDirection.LEFT) == 0) {
            newCellLeftMenuItem.setEnabled(false);
        } else if (panel.getAdjacentSelectedCells(ArrayDirection.LEFT) == 1) {
            newCellLeftMenuItem.setText(rMap.getString(
                    "newCellLeftMenuItemSingle.text"));
            newCellLeftMenuItem.setEnabled(true);
        } else {
            newCellLeftMenuItem.setText(rMap.getString(
                    "newCellLeftMenuItemPlural.text"));
            newCellLeftMenuItem.setEnabled(true);
        }

        if (panel.getAdjacentSelectedCells(ArrayDirection.RIGHT) == 0) {
            newCellRightMenuItem.setEnabled(false);
        } else if (panel.getAdjacentSelectedCells(ArrayDirection.RIGHT) == 1) {
            newCellRightMenuItem.setText(rMap.getString(
                    "newCellRightMenuItemSingle.text"));
            newCellRightMenuItem.setEnabled(true);
        } else {
            newCellRightMenuItem.setText(rMap.getString(
                    "newCellRightMenuItemPlural.text"));
            newCellRightMenuItem.setEnabled(true);
        }
    } // GEN-LAST:event_spreadsheetMenuMenuSelected

    /**
     * The action to invoke when the user selects new cell from the menu.
     *
     * @param evt
     *            The event that fired this action.
     */
    private void newCellMenuItemActionPerformed(
        final java.awt.event.ActionEvent evt) { // GEN-FIRST:event_newCellMenuItemActionPerformed
        CreateNewCellC controller = new CreateNewCellC();
        controller.createDefaultCell();
    } // GEN-LAST:event_newCellMenuItemActionPerformed

    /**
     * The action to invoke when the user selects new cell to the left from the
     * menu.
     *
     * @param evt
     *            The event that fired this action.
     */
    private void newCellLeftMenuItemActionPerformed(
        final java.awt.event.ActionEvent evt) { // GEN-FIRST:event_newCellLeftMenuItemActionPerformed
        newCellLeft();
    } // GEN-LAST:event_newCellLeftMenuItemActionPerformed

    public void newCellLeft() {
        List<Cell> selectedCells = Datavyu.getProjectController()
                                            .getDB().getSelectedCells();

        new CreateNewCellC(selectedCells, ArrayDirection.LEFT);
    }

    /**
     * The action to invoke when the user selects new cell to the right from the
     * menu.
     *
     * @param evt
     *            The event that fired this action.
     */
    private void newCellRightMenuItemActionPerformed(
        final java.awt.event.ActionEvent evt) { // GEN-FIRST:event_newCellRightMenuItemActionPerformed
        newCellRight();
    } // GEN-LAST:event_newCellRightMenuItemActionPerformed

    public void newCellRight() {
        List<Cell> selectedCells = Datavyu.getProjectController()
                                            .getDB().getSelectedCells();

        new CreateNewCellC(selectedCells, ArrayDirection.RIGHT);
    }

    /**
     * Changes the font size by adding sizeDif to the current size. Then it
     * creates and revalidates a new panel to show the font update. This will
     * not make the font smaller than smallestSize.
     *
     * @param sizeDif
     *            The number to add to the current font size.
     */
    public void changeFontSize(final int sizeDif) {
        Configuration config = Configuration.getInstance();
        Font f = config.getSSDataFont();
        int size = f.getSize();
        size = size + sizeDif;

        if (size < ZOOM_MIN_SIZE) {
            size = ZOOM_MIN_SIZE;
        } else if (size > ZOOM_MAX_SIZE) {
            size = ZOOM_MAX_SIZE;
        }

        config.setSSDataFontSize(size);

        // Create and redraw fresh window pane so all of the fonts are new
        // again.
        panel.revalidate();
        panel.repaint();
    }

    /**
     * Creates a new menu item for running a named script.
     *
     * @param f
     *            The file to run when menu item is selected.
     * @return The jmenuitem that can be added to a menu.
     */
    public JMenuItem createScriptMenuItemFromFile(final File f) {
        JMenuItem menuItem = new JMenuItem();
        menuItem.setText(f.toString());
        menuItem.setName(f.toString());
        menuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(
                    final java.awt.event.ActionEvent evt) {
                    runRecentScript(evt);
                }
            });

        return menuItem;
    }

    /**
     * Creates a new menu item for opening a file.
     *
     * @param file
     *            The file to open.
     * @return The menu item associated with the file.
     */
    private JMenuItem createRecentFileMenuItem(final File file) {
        JMenuItem menuItem = new JMenuItem();
        menuItem.setText(file.toString());
        menuItem.setName(file.toString());
        menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    open(file);
                }
            });

        return menuItem;
    }

    /**
     * The action to invoke when the user selects a recent script to run.
     *
     * @param evt
     *            The event that triggered this action.
     */
    private void runRecentScript(final java.awt.event.ActionEvent evt) {

        try {
            // record the effect
            UndoableEdit edit = new RunScriptEdit(evt.getActionCommand());
            ////

            RunScriptC scriptC = new RunScriptC(evt.getActionCommand());

            // notify the listeners
            Datavyu.getView().getUndoSupport().postEdit(edit);
            /////

            scriptC.execute();
        } catch (IOException e) {
            LOGGER.error("Unable to run recent script", e);
        }
    }

    /**
     * Returns SpreadsheetPanel
     *
     * @return SpreadsheetPanel panel
     */
    public SpreadsheetPanel getSpreadsheetPanel() {
        return panel;
    }

  public void refreshUndoRedo() {

     // refresh undo
     undoSpreadSheetMenuItem.setText( spreadsheetUndoManager.getUndoPresentationName() );
     undoSpreadSheetMenuItem.setEnabled( spreadsheetUndoManager.canUndo() );

     // refresh redo
     redoSpreadSheetMenuItem.setText( spreadsheetUndoManager.getRedoPresentationName() );
     redoSpreadSheetMenuItem.setEnabled( spreadsheetUndoManager.canRedo() );

     // Display any changes.
     //showSpreadsheet();

     panel.revalidate();
     panel.repaint();

  }


  /**
  * An undo/redo adapter. The adapter is notified when
  * an undo edit occur.
  * The adaptor extract the edit from the event, add it
  * to the UndoManager, and refresh the GUI
  */

  private class UndoAdapter implements UndoableEditListener {
     @Override
     public void undoableEditHappened (UndoableEditEvent evt) {
     	UndoableEdit edit = evt.getEdit();
     	spreadsheetUndoManager.addEdit( edit );
     	refreshUndoRedo();
     }
  }
}
