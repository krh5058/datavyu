package org.openshapa.views.discrete.datavalues;

import java.awt.event.FocusEvent;
import org.openshapa.db.DataCell;
import org.openshapa.db.SystemErrorException;
import org.openshapa.db.TimeStamp;
import java.awt.event.KeyEvent;
import javax.swing.text.JTextComponent;
import org.apache.log4j.Logger;
import org.openshapa.db.DataValue;
import org.openshapa.db.TimeStampDataValue;
import org.openshapa.views.discrete.EditorComponent;

/**
 * This class is the character editor of a TimeStampDataValues.
 */
public final class TimeStampDataValueEditor extends EditorComponent {

    /** Conversion factor for converting hours to ticks. */
    private static final long HH_TO_TICKS = 3600000;

    /** Array index for hourse. */
    private static final int HH = 0;

    /** Array index for minutes.  */
    private static final int MM = 1;

    /** Array index for seconds. */
    private static final int SS = 2;

    /** Array index for milliseconds. */
    private static final int MMM = 3;

    /** Conversion factor for converting minutes to ticks. */
    private static final long MM_TO_TICKS = 60000;

    /** Conversion factor for converting seconds to ticks. */
    private static final int SS_TO_TICKS = 1000;

    /** Logger for this class. */
    private static Logger logger = Logger
                                   .getLogger(TimeStampDataValueEditor.class);

    /** The last caret position. */
    private int oldCaretPosition;

    /** Should the oldCaretPosition be advanced by a single position? */
    private boolean advanceCaret;

    /** The TimeStampDataValue that this view represents. **/
    private TimeStampDataValue model;

    /** The parent datacell for the TimeStamp that this view represents. */
    private DataCell parentCell;

    /** The source of the TimeStampDataValue being edited. */
    private TimeStampSource dataSourceType;

    /**
     *
     */
    public enum TimeStampSource {
        /**
         * Timestamp is the Onset of the datacell associated.
         */
        Onset,
        /**
         * Timestamp is the Offset of the datacell associated.
         */
        Offset,
        /**
         * Timestamp is an argument of a datacell's matrix.
         */
        MatrixArg,
        /**
         * Timestamp is an argument of a predicate within a datacell.
         */
        PredicateArg
    }

    /**
     * Constructor.
     *
     * @param ta The parent JTextComponent the editor is in.
     * @param cell The parent data cell this editor resides within.
     * @param sourceType What timestamp are we displaying.
     */
    public TimeStampDataValueEditor(final JTextComponent ta,
                                   final DataCell cell,
                                  final TimeStampSource sourceType) {
        super(ta);
        setEditable(true);
        parentCell = cell;
        dataSourceType = sourceType;
        this.addPreservedChars(":");
        this.setDeleteChar('0');
        resetValue();
    }

    /**
     * @return The model that this data value view represents.
     */
    public DataValue getModel() {
        return this.model;
    }

    /**
     * Reset the values by retrieving from the database.
     */
    public void resetValue() {
        String t = "";
        try {
            // reget the parentCell in case other data items have changed
            parentCell = (DataCell) parentCell.getDB()
                                                   .getCell(parentCell.getID());
            switch (dataSourceType) {
                case Onset:
                    model = new TimeStampDataValue(parentCell.getDB());
                    model.setItsValue(parentCell.getOnset());
                    break;
                case Offset:
                    model = new TimeStampDataValue(parentCell.getDB());
                    model.setItsValue(parentCell.getOffset());
                    break;
                default:
                    break;
            }
        } catch (SystemErrorException e) {
            logger.error("Unable to resetValue.", e);
        }
        t = this.getModel().toString();
        setText(t);
        restoreCaretPosition();
    }

    /**
     * Update the database with the model value.
     */
    public void updateDatabase() {
        try {
            storeCaretPosition();
            // reget the parentCell in case other data items have changed
            parentCell = (DataCell) parentCell.getDB()
                                                   .getCell(parentCell.getID());
            TimeStampDataValue tsdv = (TimeStampDataValue) this.getModel();
            switch (dataSourceType) {
                case Onset:
                    parentCell.setOnset(tsdv.getItsValue());
                    break;
                case Offset:
                    parentCell.setOffset(tsdv.getItsValue());
                    break;
                default:
                    break;
            }
            parentCell.getDB().replaceCell(parentCell);
        } catch (SystemErrorException se) {
            logger.error("Unable to update Database: ", se);
        }
    }

    /**
     * Action to take by this editor when a key is pressed.
     * @param e The KeyEvent that triggered this action.
     */
    @Override
    public void keyPressed(final KeyEvent e) {
        //prevText = getText();
        //prevCaret = getCaretPosition();
        //checkNullArgKeyTyped(e);

        switch (e.getKeyCode()) {
            case KeyEvent.VK_BACK_SPACE:
            case KeyEvent.VK_DELETE:
                // Ignore - handled when the key is typed.
                e.consume();
                break;

            case KeyEvent.VK_LEFT:
                // Move caret to the left.
                int c = Math.max(0, this.getCaretPosition() - 1);
                this.setCaretPosition(c);

                // If after the move, we have a character to the left is
                // preserved character we need to skip one before passing
                // the key event down to skip again (effectively skipping
                // the preserved character).
                int b = Math.max(0, getCaretPosition());
                c = Math.max(0, this.getCaretPosition() - 1);
                if (this.isPreserved(getText().charAt(b))
                    || this.isPreserved(getText().charAt(c))) {
                    setCaretPosition(Math.max(0, getCaretPosition() - 1));
                }
                e.consume();
                break;

            case KeyEvent.VK_RIGHT:
                // Move caret to the right.
                c = Math.min(this.getText().length(),
                             this.getCaretPosition() + 1);
                this.setCaretPosition(c);

                // If after the move, we have a character to the right that
                // is a preserved character, we need to skip one before
                // passing the key event down to skip again (effectively
                // skipping the preserved character)
                b = Math.min(getText().length() - 1, getCaretPosition());
                c = Math.min(getText().length() - 1, getCaretPosition() + 1);
                if (c < this.getText().length()
                    && (this.isPreserved(getText().charAt(c))
                        || this.isPreserved(getText().charAt(b)))) {
                    setCaretPosition(Math.min(getText().length() - 1,
                                              getCaretPosition() + 1));
                }
                e.consume();
                break;

            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_UP:
                // Key stroke gets passed up a parent element to navigate
                // cells up and down.
                break;
            default:
                break;
        }
    }

    /**
     * The action to invoke when a key is typed.
     * @param e The KeyEvent that triggered this action.
     */
    @Override
    public void keyTyped(final KeyEvent e) {
        try {
            TimeStampDataValue tdv = (TimeStampDataValue) getModel();

            // The backspace key removes digits from behind the caret.
            if (e.getKeyLocation() == KeyEvent.KEY_LOCATION_UNKNOWN
                && e.getKeyChar() == '\u0008') {

                // Can't delete empty time stamp data value.
                if (!tdv.isEmpty()) {
                    this.removeBehindCaret();
                    tdv.setItsValue(new TimeStamp(getText()));
                    e.consume();
                }

            // The delete key removes digits ahead of the caret.
            } else if (e.getKeyLocation() == KeyEvent.KEY_LOCATION_UNKNOWN
                       && e.getKeyChar() == '\u007F') {

                // Can't delete empty time stamp data value.
                if (!tdv.isEmpty()) {
                    int caret = getSelectionEnd();
                    this.removeAheadOfCaret();
                    setCaretPosition(caret);

                    if (caret < getText().length()
                        && this.isPreserved(getText().charAt(caret))) {
                        setCaretPosition(getCaretPosition() + 1);
                    }

                    advanceCaret();
                    tdv.setItsValue(new TimeStamp(getText()));
                    e.consume();
                }

            // Key stoke is number - insert stroke at current caret position
            // but only if their is room in the editor for the new digit.
            } else if (Character.isDigit(e.getKeyChar())
                       && this.getCaretPosition() <= getText().length()) {
                this.removeAheadOfCaret();
                StringBuffer currentValue = new StringBuffer(getText());
                currentValue.deleteCharAt(getCaretPosition());
                currentValue.insert(getCaretPosition(), e.getKeyChar());
                advanceCaret();
                tdv.setItsValue(new TimeStamp(currentValue.toString()));
                e.consume();

            // Every other key stroke is ignored by the float editor.
            } else {
                e.consume();

                // Nothing has changed - skip updating the database.
                return;
            }

            // Push the value back into the database.
            updateDatabase();

            // Update the strings just in case we don't change the value.
            setText(this.getModel().toString());
            restoreCaretPosition();
        } catch (SystemErrorException se) {
            logger.error("Unable to update TimeStampDataValue", se);
        }
    }

    /**
     * Builds a new value from a string.
     *
     * @param textField The string that you want to create the value from.
     *
     * @return A value that can be set into the database.
     */
    public TimeStamp buildValue(final String textField) {
        try {
            long ticks = 0;

            String[] timeChunks = textField.split(":");

            ticks += (new Long(timeChunks[HH]) * HH_TO_TICKS);
            ticks += (new Long(timeChunks[MM]) * MM_TO_TICKS);
            ticks += (new Long(timeChunks[SS]) * SS_TO_TICKS);
            ticks += (new Long(timeChunks[MMM]));

            return new TimeStamp(SS_TO_TICKS, ticks);
        } catch (SystemErrorException e) {
            logger.error("Unable to build TimeStamp value", e);
            return null;
        }
    }

    /**
     * Set a flag to advanceCaret, when updateDatabase is called the
     * oldCaretPosition will be advanced by one position. When set value is
     * called back by the listeners it will reset this flag.
     */
    public void advanceCaret() {
        advanceCaret = true;
    }

    /**
     * Stores the currentCaretPosition, a call to restoreCaretPosition() can be
     * used to restore the caret position to the save point generated by this
     * method.
     */
    public void storeCaretPosition() {
        // Character inserted - advance the caret position.
        oldCaretPosition = getCaretPosition();
        if (advanceCaret) {
            oldCaretPosition++;
        }
    }

    /**
     * Restores the caret position to the last stored position. Use
     * storeCaretPosition() before calling this method.
     */
    public void restoreCaretPosition() {
        oldCaretPosition = Math.min(oldCaretPosition, getText().length());
        setCaretPosition(oldCaretPosition);
        advanceCaret = false;   // reset the advance caret flag - only applies
                                // once per database update. Database update
                                // triggers this method via a listener.
    }

    /**
     * Update the model to reflect the value represented by the
     * editor's text representation.
     */
    public void updateModelValue() {
    }

    /**
     * focusSet is the signal that this editor has become "current".
     * @param fe Focus Event
     */
    @Override
    public void focusGained(final FocusEvent fe) {
    }

    /**
     * Action to take when focus is lost for this editor.
     * @param fe Focus Event
     */
    @Override
    public void focusLost(final FocusEvent fe) {
    }

    /**
     * Action to take by this editor when a key is released.
     * @param e KeyEvent
     */
    @Override
    public void keyReleased(final KeyEvent e) {
    }
}