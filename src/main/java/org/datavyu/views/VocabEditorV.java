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

import com.usermetrix.jclient.Logger;
import com.usermetrix.jclient.UserMetrix;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.undo.UndoableEdit;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.datavyu.Datavyu;
import org.datavyu.controllers.DeleteColumnC;
import org.datavyu.models.db.Argument;
import org.datavyu.models.db.Datastore;
import org.datavyu.models.db.UserWarningException;
import org.datavyu.models.db.Variable;
import org.datavyu.undoableedits.AddVariableEdit;
import org.datavyu.undoableedits.RemoveVariableEdit;
import org.datavyu.views.discrete.datavalues.vocabelements.FormalArgEditor;
import org.datavyu.views.discrete.datavalues.vocabelements.VENameEditor;
import org.datavyu.views.discrete.datavalues.vocabelements.VocabElementV;

/**
 * A view for editing the database vocab.
 */
public final class VocabEditorV extends DatavyuDialog {

    /** The logger for this class. */
    private static Logger LOGGER = UserMetrix.getLogger(VocabEditorV.class);
    /** All the vocab views displayed in the editor. */
    private List<VocabElementV> veViews;
    /** The currently selected vocab element. */
    private VocabElementV selectedVocabElement;
    /** The currently selected formal argument. */
    private FormalArgEditor selectedArgument;
    /** Index of the currently selected formal argument within the element. */
    private int selectedArgumentI;
    /** Vertical frame for holding the current listing of Vocab elements. */
    private JPanel verticalFrame;
    /** The handler for all keyboard shortcuts */
    private KeyEventDispatcher ked;
    /** Model */
    Datastore ds;

    /** Swing components. */
    private JButton addArgButton;
    private JButton addMatrixButton;
    private JComboBox argTypeComboBox;
    private JButton closeButton;
    private JScrollPane currentVocabList;
    private JButton deleteButton;
    private JLabel jLabel1;
    private JScrollPane jScrollPane1;
    private JTextArea jTextArea1;
    private JButton moveArgLeftButton;
    private JButton moveArgRightButton;
    private JLabel statusBar;
    private JSeparator statusSeperator;

    /**
     * Constructor.
     *
     * @param parent The parent frame for the vocab editor.
     * @param modal Is this dialog to be modal or not?
     */
    public VocabEditorV(final Frame parent, final boolean modal) {
        super(parent, modal);

        LOGGER.event("vocEd - show");
        ds = Datavyu.getProjectController().getDB();

        initComponents();
        componentListnersInit();
        setName(this.getClass().getSimpleName());
        selectedVocabElement = null;
        selectedArgument = null;
        selectedArgumentI = -1;

        // manage keyboard inputs
        KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        kfm.addKeyEventDispatcher(
        ked = new KeyEventDispatcher(){
            @Override
            public boolean dispatchKeyEvent(final KeyEvent ke){

                boolean result = false;
                //determine what key was pressed
                if(ke.getID()== KeyEvent.KEY_RELEASED){
                    switch (ke.getKeyCode()){

                        case KeyEvent.VK_ESCAPE:
                            closeWindow();
                            break;
                    }
                }
                if(ke.isControlDown() && (ke.getID()== KeyEvent.KEY_RELEASED)){
                    switch(ke.getKeyCode()){
                        case KeyEvent.VK_M:
                            addMatrix();
                            break;
                        case KeyEvent.VK_A:
                            if(selectedVocabElement!=null){addArgument();}
                            break;
                        case KeyEvent.VK_S:
                            applyChanges();
                            break;
                        case KeyEvent.VK_DELETE:
                            delete();
                            break;
                        default:
                            result = false;
                    }
                }

                if(result)
                    ke.consume();

                return result;
            }
        });

        // Populate current vocab list with vocab data from the database.
        veViews = new ArrayList<VocabElementV>();
        verticalFrame = new JPanel();
        verticalFrame.setName("verticalFrame");
        verticalFrame.setLayout(new BoxLayout(verticalFrame, BoxLayout.Y_AXIS));

        for (Variable var : ds.getAllVariables()) {
            Argument argument = var.getVariableType();
            if (argument.type.equals(Argument.Type.MATRIX)) {
                VocabElementV matrixV = new VocabElementV(argument, var, this);
                verticalFrame.add(matrixV);
                veViews.add(matrixV);
            }
        }

        // Add a pad cell to fill out the bottom of the vertical frame.
        JPanel holdPanel = new JPanel();
        holdPanel.setBackground(Color.white);
        holdPanel.setLayout(new BorderLayout());
        holdPanel.add(verticalFrame, BorderLayout.NORTH);
        currentVocabList.setViewportView(holdPanel);
        updateDialogState();
    }

    /**
     * The action to invoke when the user clicks on the add predicate button.
     */
    /*
     * TODO: ADD PREDICATE SUPPORT
    @Action
    public void addPredicate() {
        try {
            LOGGER.event("vocEd - add predicate");
            PredicateVocabElement pve =
                    new PredicateVocabElement(getLegacyDB(),
                                              "predicate" + getPredNameNum());
            addVocabElement(pve);

        } catch (SystemErrorException e) {
            LOGGER.error("Unable to create predicate vocab element", e);
        }
        updateDialogState();
    }*/

    /**
     * The action to invoke when the user clicks on the add matrix button.
     */
    @Action
    public void addMatrix() {
        String varName = "matrix" + getMatNameNum();
        try {
            LOGGER.event("vocEd - add matrix");

            // perform the action
            Variable v = ds.createVariable(varName, Argument.Type.MATRIX);
            // Need to get the template from the variable.
            //Matrix m = v.getValue();
            //m.createArgument(Argument.type.NOMINAL);
            
            VocabElementV matrixV = new VocabElementV(v.getVariableType(), v, this);
            verticalFrame.add(matrixV);
            veViews.add(matrixV);

            // record the effect
            UndoableEdit edit = new AddVariableEdit(varName, Argument.Type.MATRIX);
            Datavyu.getView().getUndoSupport().postEdit(edit);
            
            matrixV.requestFocus();
            matrixV.rebuildContents();
            
            applyChanges();
            updateDialogState();
            
        // Whoops, user has done something strange - show warning dialog.
        } catch (UserWarningException fe) {
            Datavyu.getApplication().showWarningDialog(fe);
        }
    }

    public void disposeAll() {
        KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        kfm.removeKeyEventDispatcher(ked);
        dispose();
    }

    /**
     * Adds a vocab element to the vocab editor panel.
     *
     * @param va The vocab argument to add to the vocab editor.
     *
     * @throws SystemErrorException If unable to add the vocab element to the
     * vocab editor.
     */
    public void addVocabElement(final Variable var, final Argument va) {
        // The database dictates that vocab elements must have a single argument
        // add a default to get started.
        var.addArgument(Argument.Type.NOMINAL);

        VocabElementV vev = new VocabElementV(va, var, this);
        vev.setHasChanged(true);
        verticalFrame.add(vev);
        verticalFrame.validate();
        veViews.add(vev);
        
        vev.getDataView().addKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent ke){
                if(ke.isShiftDown()){
                    if(ke.getKeyCode()==KeyEvent.VK_COMMA || ke.getKeyCode()==KeyEvent.VK_PERIOD){
                        addArgument();
                    }
                }
                else if(ke.getKeyCode()==KeyEvent.VK_COMMA ||
                        ke.getKeyCode()==KeyEvent.VK_LEFT_PARENTHESIS ||
                        ke.getKeyCode()==KeyEvent.VK_RIGHT_PARENTHESIS){
                    addArgument();
                }
                if(ke.getKeyCode()== KeyEvent.VK_LEFT){
                    if(ke.isControlDown() && moveArgLeftButton.isEnabled()){
                        moveArgumentLeft();
                    }
                }
                if(ke.getKeyCode()==KeyEvent.VK_RIGHT){
                    if(ke.isControlDown() && moveArgRightButton.isEnabled()){
                        moveArgumentRight();
                    }
                }
            }
        });

        VENameEditor veNEd = vev.getNameComponent();
        vev.requestFocus(veNEd);

    }

    /**
     * The action to invoke when the user clicks on the move arg left button.
     */
    @Action
    public void moveArgumentLeft() {
        LOGGER.error("vocEd - move argument left");
        Argument va = selectedVocabElement.getModel().childArguments.get(selectedArgumentI);
        Variable var = selectedVocabElement.getVariable();
        var.moveArgument(va.name, var.getArgumentIndex(va.name) - 1);
        
        selectedVocabElement.rebuildContents();

        selectedVocabElement.requestFocus();
        
        selectedVocabElement.requestArgFocus(selectedVocabElement.getArgumentView(va));

        applyChanges();
        updateDialogState();
    }

    /**
     * The action to invoke when the user clicks on the move arg right button.
     */
    @Action
    public void moveArgumentRight() {
        LOGGER.error("vocEd - move argument right");
        Argument va = selectedVocabElement.getModel().childArguments.get(selectedArgumentI);
        Variable var = selectedVocabElement.getVariable();
        var.moveArgument(va.name, var.getArgumentIndex(va.name) + 1);
        
        selectedVocabElement.rebuildContents();

        selectedVocabElement.requestFocus();
        
        selectedVocabElement.requestArgFocus(selectedVocabElement.getArgumentView(va));

        applyChanges();
        updateDialogState();
    }

    /**
     * The action to invoke when the user clicks on the add argument button.
     */
    @Action
    public void addArgument() {
        Variable var = selectedVocabElement.getVariable();
        Argument fa = var.addArgument(Argument.Type.NOMINAL);
        selectedVocabElement.setModel(var.getVariableType());

        String type = (String) argTypeComboBox.getSelectedItem();
        LOGGER.event("vocEd - add argument:" + type);

        selectedVocabElement.setHasChanged(true);
        selectedVocabElement.rebuildContents();

        // Select the contents of the newly created formal argument.
        selectedVocabElement.requestFocus();
        FormalArgEditor faV = selectedVocabElement.getArgumentView(fa);
        selectedVocabElement.requestArgFocus(faV);

        applyChanges();
        updateDialogState();
    }

    /**
     * The action to invoke when the user presses the delete button.
     */
    @Action
    public void delete() {

        UndoableEdit edit = null;
        // User has vocab element selected - delete it from the editor.
        if (selectedVocabElement != null && selectedArgument == null) {
            LOGGER.event("vocEd - delete element");
            // record the effect
            List<Variable> varsToDelete = new ArrayList<Variable>();
            varsToDelete.add(selectedVocabElement.getVariable());
            edit = new RemoveVariableEdit(varsToDelete);
            new DeleteColumnC(varsToDelete);
            applyChanges();
            
        // User has argument selected - delete it from the vocab element.
        } else if (selectedArgument != null) {
            LOGGER.event("vocEd - delete argument");
            selectedVocabElement.getVariable().removeArgument(selectedArgument.getModel().name);
            selectedVocabElement.setHasChanged(true);
            selectedVocabElement.rebuildContents();
            applyChanges();
        }

        updateDialogState();
        if (edit != null) {
            // notify the listeners
            Datavyu.getView().getUndoSupport().postEdit(edit);
        }
    }

    /**
     * The action to invoke when the user presses the apply button.
     */
    @Action
    public int applyChanges() {
        LOGGER.event("vocEd - apply");

        int errors = 0;
            for (int index=0; index < veViews.size();index++) {
                VocabElementV vev = veViews.get(index);
//                if (vev.hasChanged()) {
//                    VocabElement ve = vev.getModel();
//                    // identify if any of the arguments have the same name
//                    if (vev.getModel().hasDuplicateArgNames()){
//                        errors = 2;
//                    } else if (ve.getID() == DBIndex.INVALID_ID) {
//                        if ((getLegacyDB().colNameInUse(ve.getName()) ||
//                            (getLegacyDB().predNameInUse(ve.getName())))) {
//                            errors = 1;
//                        }else
//                        // If the new vocab element is a matrix vocab element,
//                        // we actually need to create a column.
//                        if (ve.getClass() == MatrixVocabElement.class) {
//                            Column.isValidColumnName(Datavyu.getProjectController().getLegacyDB().getDatabase(),
//                                                     ve.getName());
//                            DataColumn dc = new DataColumn(getLegacyDB(),
//                                                           ve.getName(),
//                                                           MatrixVocabElement.MatrixType.MATRIX);
//                            DeprecatedVariable newVar = new DeprecatedVariable(dc, Argument.Type.MATRIX);
//                            ds.addVariable(newVar);
//
//                            //long colID = db.addColumn(dc);
//                            //dc = db.getDataColumn(colID);
//                            long mveID = newVar.getLegacyVariable().getItsMveID();
//                            MatrixVocabElement mve = getLegacyDB().getMatrixVE(mveID);
//                            // Delete default formal argument.
//                            mve.deleteFormalArg(0);
//
//                            // Add the formal arguments from the editor into
//                            // the database vocab element.
//                            for (int i = 0; i < ve.getNumFormalArgs(); i++) {
//                                mve.appendFormalArg(ve.getFormalArgCopy(i));
//                            }
//                            mve.setVarLen(ve.getVarLen());
//                            getLegacyDB().replaceVocabElement(mve);
//                            mve = getLegacyDB().getMatrixVE(mve.getID());
//                            vev.setModel(mve);
//                            vev.setHasChanged(false);
//                            // Otherwise just a predicate - add the new vocab
//                            // element to the database.
//                        } else {
//                            //long id = getLegacyDB().addVocabElement(ve);
//                            //vev.setModel(getLegacyDB().getVocabElement(id));
//                            //vev.setHasChanged(false);
//                        }
//
//                    } else {
//                        //getLegacyDB().replaceVocabElement(ve);
//                        //ve = getLegacyDB().getVocabElement(ve.getID());
//                        //vev.setModel(ve);
//                        //vev.setHasChanged(false);
//                    }
//                }
            }
            updateDialogState();
            ((DatavyuView) Datavyu.getView())
                    .showSpreadsheet();



        for(int i = veViews.size()-1; i>= 0; i--){
            VocabElementV vev = veViews.get(i);
            if(vev.isDeletable()){
                //getLegacyDB().removeVocabElement(vev.getModel().getID());
            }
        }

        if(errors!=0){
            switch(errors){
                case 1:
                    JOptionPane.showMessageDialog(this, "Vocab Element name in use.","Error adding vocab", 2);
                    break;
                case 2:
                    JOptionPane.showMessageDialog(this, "Argument name in use.","Duplicate argument name", 2);
                    break;
            }

        }
        return errors;
    }

    /**
     * The action to invoke when the user presses the OK button.
     */
    @Action
    public void ok() {
        LOGGER.event("vocEd - ok");
        if(applyChanges()==0){
            try {
                disposeAll();
            } catch (Throwable e) {
                LOGGER.error("Unable to destroy vocab editor view.", e);
            }
        }
    }

    /**
     * The action to invoke when the user presses the cancel button.
     */
    @Action
    public void closeWindow() {
        LOGGER.event("vocEd - close");
        try {
            disposeAll();
        } catch (Throwable e) {
            LOGGER.error("Unable to destroy vocab editor view.", e);
        }
        applyChanges();
        updateDialogState();
    }

    /**
     * Returns vector of VocabElementVs
     *
     * @return veViews Vector of VocabElementVs
     */
    public List<VocabElementV> getVocabElements() {
        return veViews;
    }

    /**
     * Method to update the visual state of the dialog to match the underlying
     * model.
     */
    public void updateDialogState() {
        ResourceMap rMap = Application.getInstance(Datavyu.class).getContext()
                                      .getResourceMap(VocabEditorV.class);

        boolean containsC = false;
        selectedVocabElement = null;
        selectedArgument = null;

        for (VocabElementV vev : veViews) {
            // A vocab element has focus - enable certain things.
            if (vev.hasFocus()) {
                selectedVocabElement = vev;
                selectedArgument = vev.getArgWithFocus();
                if(selectedArgument != null)
                    selectedArgumentI = vev.getArgWithFocus().getArgPos();
                else
                    selectedArgumentI = -1;
            }

            // A vocab element contains a change - enable certain things.
            if (vev.hasChanged() || vev.isDeletable()) {
                containsC = true;
            }
        }
/*New
        if (containsC) {
            closeButton.setText(rMap.getString("closeButton.cancelText"));
            closeButton.setToolTipText(rMap.getString("closeButton.cancelTip"));

        } else {
            closeButton.setText(rMap.getString("closeButton.cancelText"));
            closeButton.setToolTipText(rMap.getString("closeButton.cancelTip"));
        }
*/

        // If we have a selected vocab element - we can enable additional
        // functionality.
/*
        if (selectedVocabElement != null) {
            addArgButton.setEnabled(true);
            argTypeComboBox.setEnabled(true);
            varyArgCheckBox.setEnabled(true);
            deleteButton.setEnabled(true);
            varyArgCheckBox.setSelected(selectedVocabElement.getModel()
                    .getVarLen());
        } else {
            addArgButton.setEnabled(false);
            argTypeComboBox.setEnabled(false);
            deleteButton.setEnabled(false);
            varyArgCheckBox.setEnabled(false);
        }
*/
        if (selectedArgument != null) {
            Argument fa = selectedArgument.getModel();

            if (fa.type.equals(Argument.Type.NOMINAL)) {
                argTypeComboBox.setSelectedItem("Nominal");
            } else {
                argTypeComboBox.setSelectedItem("Untyped");
            }

            // W00t - argument is selected - populate the index so that the user
            // can shift the argument around.
            selectedVocabElement.getModel().childArguments.lastIndexOf(selectedArgument.getModel());
            if (selectedArgumentI > 0) {
                moveArgLeftButton.setEnabled(true);
            } else {
                moveArgLeftButton.setEnabled(false);
            }

            if (selectedArgumentI < (selectedVocabElement.getModel().childArguments.size() - 1)) {
                moveArgRightButton.setEnabled(true);
            } else {
                moveArgRightButton.setEnabled(false);
            }
        } else {
            moveArgLeftButton.setEnabled(false);
            moveArgRightButton.setEnabled(false);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        addMatrixButton = new javax.swing.JButton();
        moveArgLeftButton = new javax.swing.JButton();
        moveArgRightButton = new javax.swing.JButton();
        addArgButton = new javax.swing.JButton();
        argTypeComboBox = new javax.swing.JComboBox();
        deleteButton = new javax.swing.JButton();
        currentVocabList = new javax.swing.JScrollPane();
        closeButton = new javax.swing.JButton();
        statusBar = new javax.swing.JLabel();
        statusSeperator = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setName("jTextArea1"); // NOI18N
        jScrollPane1.setViewportView(jTextArea1);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/datavyu/views/resources/VocabEditorV"); // NOI18N
        setTitle(bundle.getString("window.title")); // NOI18N
        setName("Form"); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(org.datavyu.Datavyu.class).getContext().getActionMap(VocabEditorV.class, this);
        addMatrixButton.setAction(actionMap.get("addMatrix")); // NOI18N
        addMatrixButton.setText(bundle.getString("addMatrixButton.text")); // NOI18N
        addMatrixButton.setToolTipText(bundle.getString("addMatrixButton.tip")); // NOI18N
        addMatrixButton.setName("addMatrixButton"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        getContentPane().add(addMatrixButton, gridBagConstraints);

        moveArgLeftButton.setAction(actionMap.get("moveArgumentLeft")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.datavyu.Datavyu.class).getContext().getResourceMap(VocabEditorV.class);
        moveArgLeftButton.setIcon(resourceMap.getIcon("moveArgLeftButton.icon")); // NOI18N
        moveArgLeftButton.setText(bundle.getString("moveArgLeftButton.text")); // NOI18N
        moveArgLeftButton.setToolTipText(bundle.getString("moveArgLeftButton.tip")); // NOI18N
        moveArgLeftButton.setIconTextGap(6);
        moveArgLeftButton.setMaximumSize(new java.awt.Dimension(120, 23));
        moveArgLeftButton.setMinimumSize(new java.awt.Dimension(120, 23));
        moveArgLeftButton.setName("moveArgLeftButton"); // NOI18N
        moveArgLeftButton.setPreferredSize(new java.awt.Dimension(120, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        getContentPane().add(moveArgLeftButton, gridBagConstraints);

        moveArgRightButton.setAction(actionMap.get("moveArgumentRight")); // NOI18N
        moveArgRightButton.setIcon(resourceMap.getIcon("moveArgRightButton.icon")); // NOI18N
        moveArgRightButton.setText(bundle.getString("moveArgRightButton.text")); // NOI18N
        moveArgRightButton.setToolTipText(bundle.getString("moveArgRightButton.tip")); // NOI18N
        moveArgRightButton.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        moveArgRightButton.setIconTextGap(6);
        moveArgRightButton.setMaximumSize(new java.awt.Dimension(120, 23));
        moveArgRightButton.setMinimumSize(new java.awt.Dimension(120, 23));
        moveArgRightButton.setName("moveArgRightButton"); // NOI18N
        moveArgRightButton.setPreferredSize(new java.awt.Dimension(120, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        getContentPane().add(moveArgRightButton, gridBagConstraints);

        addArgButton.setAction(actionMap.get("addArgument")); // NOI18N
        addArgButton.setText(bundle.getString("addArgButton.text")); // NOI18N
        addArgButton.setToolTipText(bundle.getString("addArgButton.tip")); // NOI18N
        addArgButton.setName("addArgButton"); // NOI18N
        addArgButton.setPreferredSize(addMatrixButton.getPreferredSize());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        getContentPane().add(addArgButton, gridBagConstraints);

        argTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Nominal" }));
        argTypeComboBox.setToolTipText(bundle.getString("argTypeComboBox.tip")); // NOI18N
        argTypeComboBox.setEnabled(false);
        argTypeComboBox.setName("argTypeComboBox"); // NOI18N
        argTypeComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                argTypeComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(argTypeComboBox, gridBagConstraints);

        deleteButton.setAction(actionMap.get("delete")); // NOI18N
        deleteButton.setText(bundle.getString("deleteButton.text")); // NOI18N
        deleteButton.setToolTipText(bundle.getString("deleteButton.tip")); // NOI18N
        deleteButton.setMaximumSize(new java.awt.Dimension(110, 23));
        deleteButton.setMinimumSize(new java.awt.Dimension(110, 23));
        deleteButton.setName("deleteButton"); // NOI18N
        deleteButton.setPreferredSize(new java.awt.Dimension(110, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 5);
        getContentPane().add(deleteButton, gridBagConstraints);

        currentVocabList.setMinimumSize(new java.awt.Dimension(23, 200));
        currentVocabList.setName("currentVocabList"); // NOI18N
        currentVocabList.setPreferredSize(new java.awt.Dimension(200, 200));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(currentVocabList, gridBagConstraints);

        closeButton.setAction(actionMap.get("closeWindow")); // NOI18N
        closeButton.setText(bundle.getString("closeButton.closeText")); // NOI18N
        closeButton.setToolTipText(bundle.getString("closeButton.closeTip")); // NOI18N
        closeButton.setName("closeButton"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        getContentPane().add(closeButton, gridBagConstraints);

        statusBar.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusBar.setText(resourceMap.getString("statusBar.text")); // NOI18N
        statusBar.setDoubleBuffered(true);
        statusBar.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        statusBar.setMaximumSize(new java.awt.Dimension(100, 14));
        statusBar.setMinimumSize(new java.awt.Dimension(10, 14));
        statusBar.setName("statusBar"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        getContentPane().add(statusBar, gridBagConstraints);
        statusBar.getAccessibleContext().setAccessibleName(resourceMap.getString("statusBar.AccessibleContext.accessibleName")); // NOI18N

        statusSeperator.setMinimumSize(new java.awt.Dimension(100, 10));
        statusSeperator.setName("statusSeperator"); // NOI18N
        statusSeperator.setPreferredSize(new java.awt.Dimension(2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(statusSeperator, gridBagConstraints);

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setMaximumSize(new java.awt.Dimension(85, 5));
        jLabel1.setMinimumSize(new java.awt.Dimension(80, 5));
        jLabel1.setName("jLabel1"); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(85, 5));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        getContentPane().add(jLabel1, gridBagConstraints);

        pack();
    }

    /**
     * The action to invoke when the user changes the formal argument dropdown.
     *
     * @param evt The event that triggered this action.
     */
    private void argTypeComboBoxItemStateChanged(final java.awt.event.ItemEvent evt) {
        /*
        if (selectedVocabElement != null && selectedArgument != null
                && evt.getStateChange() == ItemEvent.SELECTED) {

            // Need to change the type of the selected argument.
            FormalArgument oldArg = selectedArgument.getModel();
            FormalArgument newArg = null;

            try {
                if (evt.getItem().equals("Untyped")) {
                    newArg = new UnTypedFormalArg(getLegacyDB(), oldArg.getFargName());
                } else if (evt.getItem().equals("Text")) {
                    newArg = new QuoteStringFormalArg(getLegacyDB(), oldArg.getFargName());
                } else if (evt.getItem().equals("Nominal")) {
                    newArg = new NominalFormalArg(getLegacyDB(), oldArg.getFargName());
                } else if (evt.getItem().equals("Integer")) {
                    newArg = new IntFormalArg(getLegacyDB(), oldArg.getFargName());
                } else {
                    newArg = new FloatFormalArg(getLegacyDB(), oldArg.getFargName());
                }

                if (oldArg.getFargType().equals(newArg.getFargType())) {
                    return;
                }

                selectedVocabElement.getModel().replaceFormalArg(newArg,
                        selectedArgument.getArgPos());
                selectedVocabElement.setHasChanged(true);

                // Store the selectedVocabElement in a temp variable -
                // rebuilding contents may alter the currently selected vocab
                // element.
                VocabElementV temp = selectedVocabElement;
                temp.rebuildContents();

                // Select the contents of the newly created formal argument.
                temp.requestFocus();
                FormalArgEditor faV = temp.getArgumentView(newArg);
                temp.requestArgFocus(faV);

                updateDialogState();

            } catch (SystemErrorException se) {
                LOGGER.error("Unable to alter selected argument.", se);
            }
        }
        */
    }

    /**
     * Initialization of mouse listeners on swing elements
     */
    private void componentListnersInit() {

        MouseAdapter ma = new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent me){
                String component = me.getComponent().getName();
                if(component.equals("closeButton")){
                    statusBar.setText("Close the editor");
                }
                else if(component.equals("addPredicateButton")){
                    statusBar.setText("Add a new predicate definition. Hotkey: ctrl + P");
                }
                else if(component.equals("addMatrixButton")){
                    statusBar.setText("Add a new matrix variable. Hotkey: ctrl + M");
                }
                else if(component.equals("undoButton")){
                    statusBar.setText("Undo a series of changes. Hotkey: ctrl + Z");
                }
                else if(component.equals("redoButton")){
                    statusBar.setText("Redo any undone changes. Hotkey: ctrl + Y");
                }
                else if(component.equals("addArgButton")){
                    statusBar.setText("Add a new argument to a variable. Hotkey: ctrl + A");
                }
                else if(component.equals("deleteButton")){
                    statusBar.setText("Delete an argument or variable. Hotkey: ctrl + delete");
                }
                else if(component.equals("moveArgLeftButton")){
                    statusBar.setText("Move an argument left within a variable. Hotkey: ctrl + <-");
                }
                else if(component.equals("applyButton")){
                    statusBar.setText("Apply changes to the vocab elements. Hotkey: ctrl + S");
                }
                else if(component.equals("varyArgCheckBox")){
                    statusBar.setText("Let the variable have a varying number of arguments.");
                }
                else if(component.equals("moveArgRightButton")){
                    statusBar.setText("Move an argument right within a variable. Hotkey: ctrl + ->");
                }
                else if(component.equals("okButton")){
                    statusBar.setText("Save changes and close the window.");
                }
                else if(component.equals("argTypeComboBox")){
                    statusBar.setText("Select the argument type.");
                }
            }
            @Override
            public void mouseExited(MouseEvent me){
                statusBar.setText(" ");
            }
        };

        argTypeComboBox.addMouseListener(ma);
        currentVocabList.addMouseListener(ma);
        addMatrixButton.addMouseListener(ma);
        deleteButton.addMouseListener(ma);
        closeButton.addMouseListener(ma);
        addArgButton.addMouseListener(ma);
        moveArgLeftButton.addMouseListener(ma);
        moveArgRightButton.addMouseListener(ma);
        //varyArgCheckBox.addMouseListener(ma);

    }

    /**
     * Determine the number of the next matrix added to the vocab list
     */
    private int getMatNameNum(){
        int max = 0;
        for (VocabElementV vev : veViews) {
            if (vev.getModel().type.equals(Argument.Type.MATRIX)) {
                max += 1;
            }
        }

        return max + 1;
    }

    /**
     * Determine the number of the next predicate added to the vocab list
     */
    private int getPredNameNum(){
        int max = 0;
        /*
         * TODO: Predicate unsupported.
         *
        for (VocabElementV vev : veViews) {
            if (vev.getModel().type.equals(Argument.Type.PREDICATE)) {
                String name = vev.getModel().name;
                for (int i = name.length();i>0;i--) {
                    if (Character.isDigit(name.charAt(i-1))) {
                        String numericPart = name.substring(i-1);
                        int check = Integer.parseInt(numericPart);
                        if (check > max) {
                            max = check;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        */

        return max + 1;
    }

    /**
     * Determine what action to perform when a VocabElement is removed from the vocab list
     *
     * @param db the current database
     * @param VEID the id of the vocab element to be deleted
     *//*
    @Override
    public void VLDeletion(Database db, long VEID) {
        try{
            if(db.vocabElementExists(VEID)){
                VocabElement ve = db.getVocabElement(VEID);
                for(int i=ve.getNumFormalArgs()-1; i>= 0; i--){
                    ve.deleteFormalArg(i);
                }
                int delIndex=0;
                for(VocabElementV view: veViews){
                    long vID = view.getModel().getID();
                    if(vID == VEID){
                        verticalFrame.remove(delIndex);
                        verticalFrame.revalidate();
                        veViews.remove(delIndex);
                        break;  // only ever delete one element & avoid breaking loop
                    }
                    delIndex++;
                }
            }
        }catch(Exception e){
            LOGGER.error("could not delete VE from DB" +e);
        }
    }*/

    /**
     * Determine what action to perform when a VocabElement is replaced in the vocab list
     *
     * @param db the database currently being used
     * @param VEID the id of the vocab element that has changed
     *//*
    @Override
    public void VLReplace(Database db, long VEID) {
        try{
        for(int i=0; i < veViews.size()-1;i++){
            if(VEID == veViews.get(i).getModel().getID()){
                verticalFrame.remove(i);
                veViews.remove(i);
                VocabElement ve = db.getVocabElement(VEID);
                VocabElementV vev = new VocabElementV(ve, this);
                verticalFrame.add(vev, i);
                verticalFrame.revalidate();
                veViews.add(i, vev);
            }
        }
        }catch(Exception e){
            LOGGER.error("problem replacing vocab element"+e);
        }
    }*/

    /**
     * Determine what action to perform when a VocabElement is added to the vocab list
     *
     * @param db the current database
     * @param VEID the id of the vocab element being inserted
     *//*
    @Override
    public void VLInsertion(Database db, long VEID) {
        try {
            if(db.getVocabElement(VEID) instanceof PredicateVocabElement||
                    db.getMatrixVE(VEID).getType().compareTo(MatrixType.MATRIX)==0){

                boolean exists = false;
                for(int i = 0; i< veViews.size();i++){
                    if(veViews.get(i).getModel().getName().equals(
                            db.getVocabElement(VEID).getName())){
                        exists = true;
                    }
                }

                if(!exists){
                    // if the vocab element is new, give it the default argument
                    VocabElement ve = db.getVocabElement(VEID);
                    if(!(db.getVocabElement(VEID) instanceof PredicateVocabElement)){
                        ve.deleteFormalArg(0);
                        ve.appendFormalArg(new NominalFormalArg(db, "<arg0>"));
                    }
                    // add the vocab element to the appropriate lists
                    VocabElementV vev = new VocabElementV(ve, this);
                    verticalFrame.add(vev);
                    verticalFrame.revalidate();
                    veViews.add(vev);
                }
            }
        } catch (SystemErrorException ex) {
                LOGGER.error("could not add vocab element"+ex);
        }
    }*/
}
