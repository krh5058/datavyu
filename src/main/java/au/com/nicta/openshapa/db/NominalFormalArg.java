/*
 * NominalFormalArg.java
 *
 * Created on March 14, 2007, 5:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package au.com.nicta.openshapa.db;

/**
 * Class NominalFormalArg
 *
 * Intance of this class are used for formal arguments which have been strongly
 * typed to nominals.
 *
 * @author mainzer
 */

public class NominalFormalArg extends FormalArgument
{

    /*************************************************************************/
    /***************************** Fields: ***********************************/
    /*************************************************************************/
    /**
     *
     * subRange: Boolean flag indicating whether the formal argument can be
     *      replaced by any valid nominal, or only by some nominal that
     *      appears in the approvedSet (see below).
     *
     * approvedSet: Set of nominals that may be used to replace this
     *      formal argument.  The field is ignored and should be null if
     *      subRange is false,
     *
     *      At present, the approvedSet is implemented with TreeSet, so as
     *      to quickly provide a sorted list of approved nominals.  If this
     *      turns out to be unnecessary, we should use HashSet instead.
     */

    /** Whether values are restricted to members of the approvedList */
    boolean subRange = false;

    /** If subRange is true, set of nominal that may replace the formal arg. */
    java.util.SortedSet<String> approvedSet = null;




    /*************************************************************************/
    /*************************** Constructors: *******************************/
    /*************************************************************************/

    /**
     * NominalFormalArg()
     *
     * Constructors for nominal typed formal arguments.
     *
     * Three versions of this constructor -- one that takes only a database
     * referenece, one that takes a database reference and the formal argument
     * name as a parameters, and one that takes a reference to an instance of
     * NominalFormalArg and uses it to create a copy.
     *
     *                                          JRM -- 3/15/07
     *
     * Changes:
     *
     *    - None.
     *
     */

    public NominalFormalArg(Database db)
        throws SystemErrorException
    {

        super(db);

        this.fargType = fArgType.NOMINAL;

    } /* NominalFormalArg() -- no parameters */

    public NominalFormalArg(Database db,
                            String name)
        throws SystemErrorException
    {

        super(db, name);

        this.fargType = fArgType.NOMINAL;

    } /* NominalFormalArg() -- one parameter */

    public NominalFormalArg(NominalFormalArg fArg)
        throws SystemErrorException
    {
        super(fArg);

        final String mName = "NominalFormalArg::NominalFormalArg(): ";

        this.fargType = fArgType.NOMINAL;

        if ( ! ( fArg instanceof NominalFormalArg ) )
        {
            throw new SystemErrorException(mName + "fArg not a NominalFormalArg");
        }

        // copy over fields.

        this.subRange = fArg.getSubRange();

        if ( this.subRange )
        {
            /* copy over the approved list from fArg. */
            java.util.Vector<String> approvedVector = fArg.getApprovedVector();

            this.approvedSet = new java.util.TreeSet<String>();

            for ( String s : approvedVector )
            {
                this.addApproved(s);
            }
        }

    } /* NominalFormalArg() -- make copy */



    /*************************************************************************/
    /***************************** Accessors: ********************************/
    /*************************************************************************/

    /**
     * getSubRange() & setSubRange()
     *
     * Accessor routine used to get and set the subRange field.
     *
     * In addition, if subRange is changed from false to true, we must allocate
     * the approvedSet.  Similarly, if subrange is changed from true to false,
     * we discard the approved list by setting the approvedList field to null.
     *
     *                                          JRM -- 3/15/07
     *
     * Changes:
     *
     *    - None.
     *
     */

    public boolean getSubRange()
    {
        return subRange;
    }

    public void setSubRange(boolean subRange)
    {
        final String mName = "NominalFormalArg::setSubRange(): ";

        if ( this.subRange != subRange )
        {
            /* we have work to do. */
            if ( subRange )
            {
                this.subRange = true;
                approvedSet = new java.util.TreeSet<String>();
            }
            else
            {
                this.subRange = false;

                /* discard the approved set */
                approvedSet = null;
            }
        }

        return;

    } /* NominalFormalArg::setSubRange() */


    /*************************************************************************/
    /************************ Approved Set Management: ***********************/
    /*************************************************************************/

    /**
     * constructArgWithSalvage()  Override of abstract method in FormalArgument
     *
     * Return an instance of NominalDataValue initialized from salvage if
     * possible, and to the default for newly created instances of
     * NominalDataValue otherwise.
     *
     * Changes:
     *
     *    - None.
     */

    DataValue constructArgWithSalvage(DataValue salvage)
        throws SystemErrorException
    {
        NominalDataValue retVal;

        if ( ( salvage == null ) ||
             ( salvage.getItsFargID() == DBIndex.INVALID_ID ) )
        {
            retVal = new NominalDataValue(this.db, this.id);
        }
        else if ( salvage instanceof NominalDataValue )
        {
            retVal = new NominalDataValue(this.db, this.id,
                    ((NominalDataValue)salvage).getItsValue());
        }
        else if ( ( salvage instanceof QuoteStringDataValue ) &&
                  ( ((QuoteStringDataValue)salvage).getItsValue() != null ) &&
                  ( Database.IsValidNominal
                        (((QuoteStringDataValue)salvage).getItsValue()) ))
        {
            retVal = new NominalDataValue(this.db, this.id,
                    ((QuoteStringDataValue)salvage).getItsValue());
        }
        else if ( ( salvage instanceof TextStringDataValue ) &&
                  ( ((TextStringDataValue)salvage).getItsValue() != null ) &&
                  ( Database.IsValidNominal
                        (((TextStringDataValue)salvage).getItsValue())))
        {
            retVal = new NominalDataValue(this.db, this.id,
                    ((TextStringDataValue)salvage).getItsValue());
        }
        else
        {
            retVal = new NominalDataValue(this.db, this.id);
        }

        return retVal;

    } /* NominalDataValue::constructArgWithSalvage(salvage) */


    /**
     * addApproved()
     *
     * Add the supplied nominal to the approved set.
     *
     * The method throws a system error if subRange is false, if passed a null,
     * if passed an invalid nominal, or if the approved list already contains
     * the supplied nominal.
     *                                          JRM -- 3/15/07
     *
     * Changes:
     *
     *    - None.
     */

    public void addApproved(String s)
        throws SystemErrorException
    {
        final String mName = "NominalFormalArg::addApproved(): ";

        if ( ! this.subRange )
        {
            throw new SystemErrorException(mName + "subRange is false.");
        }
        else if ( approvedSet == null )
        {
            throw new SystemErrorException(mName + "approvedSet is null?!?!");
        }
        else if ( ! Database.IsValidNominal(s) )
        {
            throw new SystemErrorException(mName + "s is not a nominal.");
        }
        else if ( ! this.approvedSet.add(new String(s)) )
        {
            throw new SystemErrorException(mName + "s already in approved set.");
        }

        return;

    } /* NominalFormalArg::addApproved() */


    /**
     * approved()
     *
     * Return true if the supplied String contains a nominal that is a member
     * of the approved set.
     *
     * The method throws a system error if passed a null, if subRange is false,
     * or if the test string does not contain a valid nominal.
     *
     *                                          JRM -- 3/15/07
     *
     * Changes:
     *
     *    - None.
     */

    public boolean approved(String test)
        throws SystemErrorException
    {
        final String mName = "NominalFormalArg::approved(): ";

        if ( ! this.subRange )
        {
            throw new SystemErrorException(mName + "subRange is false.");
        }
        else if ( this.approvedSet == null )
        {
            throw new SystemErrorException(mName + "approvedSet is null?!?!");
        }
        else if ( ! Database.IsValidNominal(test) )
        {
            throw new SystemErrorException(mName + "test is not a nominal.");
        }

        return approvedSet.contains(test);

    } /* NominalFormalArg::approved() */


    /**
     * approvedSetToString()
     *
     * Construct and return a string representation of the approved set.
     *
     *                                          JRM -- 3/15/07
     *
     * Changes:
     *
     *    - None.
     */

    private String approvedSetToString()
    {
        final String mName = "NominalFormalArg::approvedSetToString(): ";
        String s = null;
        int i;
        java.util.Iterator<String> iterator = null;

        if ( subRange )
        {
            if ( this.approvedSet == null )
            {
                s = "(" + mName +
                    " (subRange && (approvedSet == null)) syserr?? )";
            }

            iterator = this.approvedSet.iterator();

            s = "(";

            if ( iterator.hasNext() )
            {
                s += iterator.next();
            }

            while ( iterator.hasNext() )
            {
                s += ", " + iterator.next();
            }

            s += ")";
        }
        else
        {
            s = "()";
        }

        return s;
    }

    /**
     * deleteApproved()
     *
     * Delete the supplied nominal from the approved set.
     *
     * The method throws a system error if subRange is false, if passed a null,
     * if passed an invalid nominal, or if the approved list does not contain
     * the supplied nominal.
     *                                          JRM -- 3/15/07
     *
     * Changes:
     *
     *    - None.
     */

    public void deleteApproved(String s)
        throws SystemErrorException
    {
        final String mName = "NominalFormalArg::deleteApproved(): ";

        if ( ! this.subRange )
        {
            throw new SystemErrorException(mName + "subRange is false.");
        }
        else if ( approvedSet == null )
        {
            throw new SystemErrorException(mName + "approvedSet is null?!?!");
        }
        else if ( ! Database.IsValidNominal(s) )
        {
            throw new SystemErrorException(mName + "s is not a nominal.");
        }
        else if ( ! this.approvedSet.remove(s) )
        {
            throw new SystemErrorException(mName + "s not in approved set.");
        }

        return;

    } /* NominalFormalArg::deleteApproved() */


    /**
     * getApprovedVector()
     *
     * Return an vector of String containing an alphabetical list of all
     * entries in the approved set, or null if the approved list is empty.
     *
     * The method throws a system error if subRange is false.
     *
     *                                              JRM -- 3/15/07
     *
     * Changes:
     *
     *    - None.
     */

    java.util.Vector<String> getApprovedVector()
        throws SystemErrorException
    {
        final String mName = "NominalFormalArg::getApprovedList(): ";
        java.util.Vector<String> approvedVector = null;
        int i;

        if ( ! this.subRange )
        {
            throw new SystemErrorException(mName + "subRange is false.");
        }
        else if ( approvedSet == null )
        {
            throw new SystemErrorException(mName + "approvedSet is null?!?!");
        }

        if ( this.approvedSet.size() > 0 )
        {
            /* make copies of all strings in the approved set and then insert
             * them in the approvedVector.  We make copies so that
             * they can't be changed out from under the approved set.
             *
             * From reading the documentation, it is not completely clear that
             * this is necessary, but better safe than sorry.
             */
            approvedVector = new java.util.Vector<String>();

            for ( String s : this.approvedSet )
            {
                approvedVector.add(new String(s));
            }
        }

        return approvedVector;

    } /* NominalFormalArg::getApprovedVector() */

    /*************************************************************************/
    /***************************** Overrides: ********************************/
    /*************************************************************************/

    /**
     * constructEmptyArg()  Override of abstract method in FormalArgument
     *
     * Return an instance of NominalDataValue initialized as appropriate for
     * an argument that has not had any value assigned to it by the user.
     *
     * Changes:
     *
     *    - None.
     */

     public DataValue constructEmptyArg()
        throws SystemErrorException
     {

         return new NominalDataValue(this.db, this.id);

     } /* NominalFormalArg::constructEmptyArg() */


    /**
     * toDBString() -- Override of abstract method in DataValue
     *
     * Returns a database String representation of the DBValue for comparison
     * against the database's expected value.<br>
     *
     * <i>This function is intended for debugging purposses.</i>
     *
     * @return the string value.
     *
     *                                      JRM -- 2/13/07
     *
     * Changes:
     *
     *    - None.
     *
     */
    public String toDBString() {

        return ("(NominalFormalArg " + getID() + " " + getFargName() + " " +
                getSubRange() + " " + approvedSetToString() + ")");

    } /* NominalFormalArg::toDBString() */


    /**
     * isValidValue() -- Override of abstract method in FormalArgument
     *
     * Boolean metho that returns true iff the provided value is an acceptable
     * value to be assigned to this formal argument.
     *
     *                                             JRM -- 2/5/07
     *
     * Changes:
     *
     *    - None.
     */

    public boolean isValidValue(Object obj)
        throws SystemErrorException
    {
        if ( ! Database.IsValidNominal(obj) )
        {
            return false;
        }

        return true;

    } /*  NominalFormalArg::isValidValue() */


    /*************************************************************************/
    /**************************** Test Code: *********************************/
    /*************************************************************************/

    /*** TODO: Review test code. ***/

    /**
     * TestAccessors()
     *
     * Run a battery of tests on the accessors for this class.
     *
     * Changes:
     *
     *    - None.
     */

    public static boolean TestAccessors(java.io.PrintStream outStream,
                                        boolean verbose)
    {
        String testBanner =
            "Testing class NominalFormalArg accessors                         ";
        String passBanner = "PASSED\n";
        String failBanner = "FAILED\n";
        String systemErrorExceptionString = null;
        boolean testFinished = false;
        boolean threwInvalidFargNameException = false;
        boolean threwSystemErrorException = false;
        boolean pass = true;
        int failures = 0;
        String s = null;
        NominalFormalArg arg = null;

        outStream.print(testBanner);

        if ( verbose )
        {
            outStream.print("\n");
        }

        arg = null;
        threwSystemErrorException = false;
        systemErrorExceptionString = null;

        try
        {
            arg = new NominalFormalArg(new ODBCDatabase());
        }

        catch (SystemErrorException e)
        {
            threwSystemErrorException = true;
            systemErrorExceptionString = e.getMessage();
        }

        if ( ( arg == null ) || ( threwSystemErrorException ) )
        {
            failures++;

            if ( verbose )
            {
                if ( arg == null )
                {
                    outStream.print(
                            "new NominalFormalArg(db) returned null.\n");
                }

                if ( threwSystemErrorException )
                {
                    outStream.printf("new NominalFormalArg(db) threw " +
                                      "system error exception: \"%s\"\n",
                                      systemErrorExceptionString);
                }
            }
        }

        /* test the inherited accessors */
        if ( failures == 0 )
        {
            threwSystemErrorException = false;

            try
            {
                failures +=
                        FormalArgument.TestAccessors(arg, outStream, verbose);
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( threwSystemErrorException )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("AbstractFormalArgument.TestAccessors." +
                            " threw a SystemErrorException.\n");
                }
            }
        }

        /* NominalFormalArg adds subRange and approvedSet.  We must test
         * the routines supporting these fields as well.
         */

        /* First verify correct initialization */
        if ( failures == 0 )
        {
            if ( arg.getSubRange() != false )
            {
                failures++;

                if ( verbose )
                {
                    outStream.printf("\"arg.getSubRange()\" returned " +
                            "unexpected initial value(1): %b.\n",
                            arg.getSubRange());
                }
            }
            else if ( arg.approvedSet != null )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "arg.approvedSet not initialized to null.\n");
                }
            }
        }

        /* now set subRange to true, and verify that approvedSet is allocated */
        if ( failures == 0 )
        {
            arg.setSubRange(true);

            if ( ( arg.subRange != true ) ||
                 ( arg.getSubRange() != true ) )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "\"arg.setSubRange(true)\" failed " +
                            "to set arg.subRange to true.\n");
                }
            }
            else if ( ( arg.approvedSet == null ) ||
                      ( arg.approvedSet.size() != 0 ) )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "\"arg.setSubRange(true)\" failed " +
                            "to initialize arg.approvedSet correctly.\n");
                }
            }
        }

        /* now test the approvedSet management functions.  Start with just a
         * simple smoke check that verifies that the methods do more or
         * less as they should, and then verify that they fail when they
         * should.
         */
        if ( failures == 0 )
        {
            testFinished = false;
            threwSystemErrorException = false;

            try
            {
                arg.addApproved("charlie");
                arg.addApproved("bravo");
                arg.addApproved("delta");
                arg.deleteApproved("bravo");
                arg.addApproved("alpha");
                testFinished = true;
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( threwSystemErrorException )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "SystemErrorException thrown in setup(1).\n");
                }
            }
            else if ( ! testFinished )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("test incomplete(1).\n");
                }
            }
        }

        if ( failures == 0 )
        {
            if ( arg.approvedSetToString().
                    compareTo("(alpha, charlie, delta)") != 0 )
            {
                failures++;

                if ( verbose )
                {
                    outStream.printf(
                        "Unexpected arg.approvedSetToString() results(1): \"%s\".\n",
                        arg.approvedSetToString());
                }
            }
        }

        if ( failures == 0 )
        {
            testFinished = false;
            threwSystemErrorException = false;
            java.util.Vector<String> approvedVector = null;

            try
            {
                approvedVector = arg.getApprovedVector();
                testFinished = true;
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( threwSystemErrorException )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "SystemErrorException thrown in call to " +
                            "arg.getApprovedVector()(1).\n");
                }
            }
            else if ( ! testFinished )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("test incomplete(2).\n");
                }
            }
            else if ( ( approvedVector.size() != 3 ) ||
                      ( approvedVector.get(0).compareTo("alpha") != 0 ) ||
                      ( approvedVector.get(1).compareTo("charlie") != 0 ) ||
                      ( approvedVector.get(2).compareTo("delta") != 0 ) )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("Unexpected approvedVector(1).\n");
                }
            }
        }

        /* try several invalid additions to the approved set */
        if ( failures == 0 )
        {
            testFinished = false;
            threwSystemErrorException = false;

            try
            {
                arg.addApproved(null);
                testFinished = true;
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( ! threwSystemErrorException )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "\"arg.addApproved(null)\" failed to throw a" +
                            "SystemErrorException).\n");
                }
            }
            else if ( testFinished )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("\"arg.addApproved(null)\" returned.\n");
                }
            }
        }

        if ( failures == 0 )
        {
            testFinished = false;
            threwSystemErrorException = false;

            try
            {
                arg.addApproved(" invalid ");
                testFinished = true;
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( ! threwSystemErrorException )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "\"arg.addApproved(\" invalid \")\" failed "
                            + " to throw a SystemErrorException).\n");
                }
            }
            else if ( testFinished )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("\"arg.addApproved(\" invalid \")\" returned.\n");
                }
            }
        }

        if ( failures == 0 )
        {
            testFinished = false;
            threwSystemErrorException = false;

            try
            {
                arg.addApproved("charlie"); /* already in approved set */
                testFinished = true;
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( ! threwSystemErrorException )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "\"arg.addApproved(\"charlie\")\" failed "
                            + " to throw a SystemErrorException).\n");
                }
            }
            else if ( testFinished )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("\"arg.addApproved(\"charlie\")\" returned.\n");
                }
            }
        }

        /* try invalid calls to arg.approved() */
        if ( failures == 0 )
        {
            testFinished = false;
            threwSystemErrorException = false;

            try
            {
                arg.approved(null);
                testFinished = true;
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( ! threwSystemErrorException )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "\"arg.approved(null)\" failed "
                            + " to throw a SystemErrorException).\n");
                }
            }
            else if ( testFinished )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("\"arg.approved(null)\" returned.\n");
                }
            }
        }

        if ( failures == 0 )
        {
            testFinished = false;
            threwSystemErrorException = false;

            try
            {
                arg.approved(" invalid ");
                testFinished = true;
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( ! threwSystemErrorException )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "\"arg.approved(\" invalid \")\" failed "
                            + " to throw a SystemErrorException).\n");
                }
            }
            else if ( testFinished )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("\"arg.approved(\" invalid \")\" returned.\n");
                }
            }
        }

        /* try invalid deletions from the approved set */
         if ( failures == 0 )
        {
            testFinished = false;
            threwSystemErrorException = false;

            try
            {
                arg.deleteApproved(null);
                testFinished = true;
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( ! threwSystemErrorException )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "\"arg.deleteApproved(null)\" failed "
                            + " to throw a SystemErrorException).\n");
                }
            }
            else if ( testFinished )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("\"arg.deleteApproved(null)\" returned.\n");
                }
            }
        }

        if ( failures == 0 )
        {
            testFinished = false;
            threwSystemErrorException = false;

            try
            {
                arg.deleteApproved(" invalid ");
                testFinished = true;
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( ! threwSystemErrorException )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("arg.deleteApproved(\" invalid \")\" failed "
                            + " to throw a SystemErrorException).\n");
                }
            }
            else if ( testFinished )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("\"arg.deleteApproved(\" invalid \")\" returned.\n");
                }
            }
        }


        if ( failures == 0 )
        {
            testFinished = false;
            threwSystemErrorException = false;

            try
            {
                arg.deleteApproved("nonesuch");
                testFinished = true;
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( ! threwSystemErrorException )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("arg.deleteApproved(\"nonesuch\")\" failed "
                            + " to throw a SystemErrorException).\n");
                }
            }
            else if ( testFinished )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("\"arg.deleteApproved(\"nonesuch\")\" returned.\n");
                }
            }
        }

        /* now reduce the size of the approved set to 1 */
        if ( failures == 0 )
        {
            testFinished = false;
            threwSystemErrorException = false;

            try
            {
                arg.deleteApproved("alpha");
                arg.deleteApproved("charlie");
                testFinished = true;
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( threwSystemErrorException )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "SystemErrorException thrown in setup(2).\n");
                }
            }
            else if ( ! testFinished )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("test incomplete(3).\n");
                }
            }
        }

        /* verify that getApprovedVector() and getApprovedString() work as they
         * should with zero entries.
         */

        if ( failures == 0 )
        {
            testFinished = false;
            threwSystemErrorException = false;
            java.util.Vector<String> approvedVector = null;

            try
            {
                approvedVector = arg.getApprovedVector();
                testFinished = true;
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( threwSystemErrorException )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "SystemErrorException thrown in call to " +
                            "arg.getApprovedVector()(2).\n");
                }
            }
            else if ( ! testFinished )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("test incomplete(4).\n");
                }
            }
            else if ( ( approvedVector.size() != 1 ) ||
                     ( approvedVector.get(0).compareTo("delta") != 0 ) )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("Unexpected approvedVector(2).\n");
                }
            }
        }


        if ( failures == 0 )
        {
            if ( arg.approvedSetToString().compareTo("(delta)") != 0 )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                        "Unexpected arg.approvedSetToString() results(2).\n");
                }
            }
        }

        /* now reduce the size of the approved set to 0 */
        if ( failures == 0 )
        {
            testFinished = false;
            threwSystemErrorException = false;

            try
            {
                arg.deleteApproved("delta");
                testFinished = true;
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( threwSystemErrorException )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "SystemErrorException thrown in setup(3).\n");
                }
            }
            else if ( ! testFinished )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("test incomplete(5).\n");
                }
            }
        }

        /* verify that getApprovedVector() and getApprovedString() work as they
         * should with no entries.
         */

        if ( failures == 0 )
        {
            testFinished = false;
            threwSystemErrorException = false;
            java.util.Vector<String> approvedVector = null;

            try
            {
                approvedVector = arg.getApprovedVector();
                testFinished = true;
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( threwSystemErrorException )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "SystemErrorException thrown in call to " +
                            "arg.getApprovedVector()(3).\n");
                }
            }
            else if ( ! testFinished )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("test incomplete(5).\n");
                }
            }
            else if ( approvedVector != null )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("Unexpected approvedVector(3).\n");
                }
            }
        }

        if ( failures == 0 )
        {
            if ( arg.approvedSetToString().compareTo("()") != 0 )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                        "Unexpected arg.approvedSetToString() results(3).\n");
                }
            }
        }

        /* now set subRange back to false.  Verify that approvedSet is set to
         * null, and that all approved set manipulation methods thow a system
         * error if invoked.
         */

        if ( failures == 0 )
        {
            arg.setSubRange(false);

            if ( ( arg.subRange != false ) ||
                 ( arg.getSubRange() != false ) )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "\"arg.setSubRange(false)\" failed " +
                            "to set arg.subRange to false.\n");
                }
            }
            else if ( arg.approvedSet != null )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "\"arg.setSubRange(false)\" failed " +
                            "to set arg.approvedSet to null.\n");
                }
            }
        }

        /* finally, verify that all the approved list management routines
         * flag a system error if invoked when subRange is false.
         */
        if ( failures == 0 )
        {
            testFinished = false;
            threwSystemErrorException = false;

            try
            {
                arg.approved("alpha");
                testFinished = true;
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( ! threwSystemErrorException )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "arg.approved() failed to throw a " +
                            "SystemErrorException when subRange is false.\n");
                }
            }
            else if ( testFinished )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "test completed with subrange == false (1)).\n");
                }
            }
        }

        if ( failures == 0 )
        {
            testFinished = false;
            threwSystemErrorException = false;

            try
            {
                arg.addApproved("alpha");
                testFinished = true;
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( ! threwSystemErrorException )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "arg.addApproved() failed to throw a " +
                            "SystemErrorException when subRange is false.\n");
                }
            }
            else if ( testFinished )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "test completed with subrange == false (2)).\n");
                }
            }
        }

        if ( failures == 0 )
        {
            testFinished = false;
            threwSystemErrorException = false;

            try
            {
                arg.deleteApproved("alpha");
                testFinished = true;
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( ! threwSystemErrorException )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "arg.deleteApproved() failed to throw a " +
                            "SystemErrorException when subRange is false.\n");
                }
            }
            else if ( testFinished )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "test completed with subrange == false (3)).\n");
                }
            }
        }

        if ( failures == 0 )
        {
            testFinished = false;
            threwSystemErrorException = false;

            try
            {
                arg.getApprovedVector();
                testFinished = true;
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( ! threwSystemErrorException )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "arg.getApprovedVector() failed to throw a " +
                            "SystemErrorException when subRange is false.\n");
                }
            }
            else if ( testFinished )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "test completed with subrange == false (4)).\n");
                }
            }
        }

        if ( failures > 0 )
        {
            pass = false;

            if ( verbose )
            {
                outStream.printf("%d failures.\n", failures);
            }
        }
        else if ( verbose )
        {
            outStream.print("All tests passed.\n");
        }

        if ( verbose )
        {
            /* print the banner again. */
            outStream.print(testBanner);
        }

        if ( pass )
        {
            outStream.print(passBanner);
        }
        else
        {
            outStream.print(failBanner);
        }

        return pass;

    } /* NominalFormalArg::TestAccessors() */


    /**
     * TestVEAccessors()
     *
     * Run a battery of tests on the itsVocabElement and itsVocabElementID
     * accessor methods for this class.
     *
     * Changes:
     *
     *    - None.
     */

    public static boolean TestVEAccessors(java.io.PrintStream outStream,
                                          boolean verbose)
    {
        String testBanner =
            "Testing class NominalFormalArg itsVocabElement accessors         ";
        String passBanner = "PASSED\n";
        String failBanner = "FAILED\n";
        String systemErrorExceptionString = null;
        boolean threwSystemErrorException = false;
        boolean pass = true;
        int failures = 0;
        String s = null;
        NominalFormalArg arg = null;

        outStream.print(testBanner);

        if ( verbose )
        {
            outStream.print("\n");
        }

        arg = null;
        threwSystemErrorException = false;
        systemErrorExceptionString = null;

        try
        {
            arg = new NominalFormalArg(new ODBCDatabase());
        }

        catch (SystemErrorException e)
        {
            threwSystemErrorException = true;
            systemErrorExceptionString = e.getMessage();
        }

        if ( ( arg == null ) || ( threwSystemErrorException ) )
        {
            failures++;

            if ( verbose )
            {
                if ( arg == null )
                {
                    outStream.print(
                            "new NominalFormalArg(db) returned null.\n");
                }

                if ( threwSystemErrorException )
                {
                    outStream.printf("new NominalFormalArg(db) threw " +
                                      "system error exception: \"%s\"\n",
                                      systemErrorExceptionString);
                }
            }
        }

        /* test the itsVocabElement & itsVocabElementID accessors */
        if ( failures == 0 )
        {
            threwSystemErrorException = false;

            try
            {
                failures += FormalArgument.TestVEAccessors(arg, outStream,
                                                           verbose);
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( threwSystemErrorException )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("FormalArgument.TestVEAccessors()" +
                            " threw a SystemErrorException.\n");
                }
            }
        }

        if ( failures > 0 )
        {
            pass = false;

            if ( verbose )
            {
                outStream.printf("%d failures.\n", failures);
            }
        }
        else if ( verbose )
        {
            outStream.print("All tests passed.\n");
        }

        if ( verbose )
        {
            /* print the banner again. */
            outStream.print(testBanner);
        }

        if ( pass )
        {
            outStream.print(passBanner);
        }
        else
        {
            outStream.print(failBanner);
        }

        return pass;

    } /* NominalFormalArg::TestVEAccessors() */


    /**
     * TestClassNominalFormalArg()
     *
     * Main routine for tests of class NominalFormalArg.
     *
     *                                      JRM -- 3/10/07
     *
     * Changes:
     *
     *    - Non.
     */

    public static boolean TestClassNominalFormalArg(java.io.PrintStream outStream,
                                                    boolean verbose)
        throws SystemErrorException
    {
        boolean pass = true;
        int failures = 0;

        outStream.print("Testing class NominalFormalArg:\n");

        if ( ! Test1ArgConstructor(outStream, verbose) )
        {
            failures++;
        }

        if ( ! Test2ArgConstructor(outStream, verbose) )
        {
            failures++;
        }

        if ( ! TestCopyConstructor(outStream, verbose) )
        {
            failures++;
        }

        if ( ! TestAccessors(outStream, verbose) )
        {
            failures++;
        }

        if ( ! TestVEAccessors(outStream, verbose) )
        {
            failures++;
        }

        if ( ! TestIsValidValue(outStream, verbose) )
        {
            failures++;
        }

        if ( ! TestToStringMethods(outStream, verbose) )
        {
            failures++;
        }

        if ( failures > 0 )
        {
            pass = false;
            outStream.printf("%d failures in tests for class NominalFormalArg.\n\n",
                              failures);
        }
        else
        {
            outStream.print("All tests passed for class NominalFormalArg.\n\n");
        }

        return pass;

    } /* Database::TestDatabase() */

    /**
     * Test1ArgConstructor()
     *
     * Run a battery of tests on the one argument constructor for this
     * class, and on the instance returned.
     *
     * Changes:
     *
     *    - None.
     */

    public static boolean Test1ArgConstructor(java.io.PrintStream outStream,
                                              boolean verbose)
    {
        String testBanner =
            "Testing 1 argument constructor for class NominalFormalArg        ";
        String passBanner = "PASSED\n";
        String failBanner = "FAILED\n";
        String systemErrorExceptionString = null;
        boolean methodReturned = false;
        boolean pass = true;
        boolean threwSystemErrorException = false;
        int failures = 0;
        String s = null;
        NominalFormalArg arg = null;

        outStream.print(testBanner);

        if ( verbose )
        {
            outStream.print("\n");
        }

        arg = null;
        threwSystemErrorException = false;
        systemErrorExceptionString = null;

        try
        {
            arg = new NominalFormalArg(new ODBCDatabase());
        }

        catch (SystemErrorException e)
        {
            threwSystemErrorException = true;
            systemErrorExceptionString = e.getMessage();
        }

        if ( ( arg == null ) || ( threwSystemErrorException ) )
        {
            failures++;

            if ( verbose )
            {
                if ( arg == null )
                {
                    outStream.print(
                            "new NominalFormalArg(db) returned null.\n");
                }

                if ( threwSystemErrorException )
                {
                    outStream.printf("new NominalFormalArg(db) threw " +
                                      "system error exception: \"%s\"\n",
                                      systemErrorExceptionString);
                }
            }
        }

        if ( failures == 0 )
        {
            if ( arg.getFargName().compareTo("<val>") != 0 )
            {
                failures++;

                if ( verbose )
                {
                    outStream.printf("Unexpected initial fArgName \"%s\".\n",
                                       arg.getFargName());
                }
            }
        }

        if ( failures == 0 )
        {
            if ( arg.getHidden() != false )
            {
                failures++;

                if ( verbose )
                {
                    outStream.printf("Unexpected initial value of hidden: %b.\n",
                                       arg.getHidden());
                }
            }
        }

        if ( failures == 0 )
        {
            if ( arg.getItsVocabElement() != null )
            {
                failures++;

                if ( verbose )
                {
                    outStream.printf("itsVocabElement not initialzed to null.\n");
                }
            }
        }

        /* Verify that the constructor fails if passed a bad db */
        if ( failures == 0 )
        {
            arg = null;
            methodReturned = false;
            threwSystemErrorException = false;
            systemErrorExceptionString = null;

            try
            {
                arg = new NominalFormalArg((Database)null);
                methodReturned = true;
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
                systemErrorExceptionString = e.getMessage();
            }

            if ( ( methodReturned ) ||
                 ( arg != null ) ||
                 ( ! threwSystemErrorException ) )
            {
                failures++;

                if ( verbose )
                {
                    if ( methodReturned )
                    {
                        outStream.print("new NominalFormalArg(null) returned.\n");
                    }

                    if ( arg != null )
                    {
                        outStream.print(
                                "new NominalFormalArg(null) returned non-null.\n");
                    }

                    if ( ! threwSystemErrorException )
                    {
                        outStream.print("new NominalFormalArg(null) didn't " +
                                         "throw a system error exception.\n");
                    }
                }
            }
        }

        if ( failures > 0 )
        {
            pass = false;

            if ( verbose )
            {
                outStream.printf("%d failures.\n", failures);
            }
        }
        else if ( verbose )
        {
            outStream.print("All tests passed.\n");
        }

        if ( verbose )
        {
            /* print the banner again. */
            outStream.print(testBanner);
        }

        if ( pass )
        {
            outStream.print(passBanner);
        }
        else
        {
            outStream.print(failBanner);
        }

        return pass;

    } /* NominalFormalArg::Test1ArgConstructor() */

    /**
     * Test2ArgConstructor()
     *
     * Run a battery of tests on the two argument constructor for this
     * class, and on the instance returned.
     *
     * Changes:
     *
     *    - None.
     */

    public static boolean Test2ArgConstructor(java.io.PrintStream outStream,
                                              boolean verbose)
    {
        String testBanner =
            "Testing 2 argument constructor for class NominalFormalArg        ";
        String passBanner = "PASSED\n";
        String failBanner = "FAILED\n";
        boolean threwSystemErrorException = false;
        boolean pass = true;
        int failures = 0;
        String s = null;
        NominalFormalArg arg = null;

        outStream.print(testBanner);

        if ( verbose )
        {
            outStream.print("\n");
        }

        try
        {
            arg = new NominalFormalArg(new ODBCDatabase(), "<valid>");
        }

        catch (SystemErrorException e)
        {
            threwSystemErrorException = true;
        }

        if ( ( arg == null ) ||
             ( threwSystemErrorException ) )
        {
            failures++;

            if ( verbose )
            {
                if ( arg == null )
                {
                    outStream.print(
                        "new NominalFormalArg(db, \"<valid>\") returned null.\n");
                }

                if ( threwSystemErrorException )
                {
                    outStream.print("new NominalFormalArg(db, \"<valid>\") " +
                                     "threw a SystemErrorException.\n");
                }
            }
        }

        if ( failures == 0 )
        {
            if ( arg.getFargName().compareTo("<valid>") != 0 )
            {
                failures++;

                if ( verbose )
                {
                    outStream.printf("Unexpected initial fArgName \"%s\".\n",
                                       arg.getFargName());
                }
            }
        }

        if ( failures == 0 )
        {
            if ( arg.getHidden() != false )
            {
                failures++;

                if ( verbose )
                {
                    outStream.printf("Unexpected initial value of hidden: %b.\n",
                                       arg.getHidden());
                }
            }
        }

        if ( failures == 0 )
        {
            if ( arg.getItsVocabElement() != null )
            {
                failures++;

                if ( verbose )
                {
                    outStream.printf("itsVocabElement not initialzed to null.\n");
                }
            }
        }

        /* Verify that the constructor fails when passed an invalid db. */
        arg = null;
        threwSystemErrorException = false;

        try
        {
            arg = new NominalFormalArg(null, "<valid>");
        }

        catch (SystemErrorException e)
        {
            threwSystemErrorException = true;
        }

        if ( ( arg != null ) ||
             ( ! threwSystemErrorException ) )
        {
            failures++;

            if ( verbose )
            {
                if ( arg != null )
                {
                    outStream.print(
                        "new NominalFormalArg(null, \"<alid>>\") != null.\n");
                }

                if ( threwSystemErrorException )
                {
                    outStream.print("new NominalFormalArg(null, \"<valid>\") "
                                    + "didn't throw a SystemErrorException.\n");
                }
            }
        }

        /* now verify that the constructor fails when passed an invalid
         * formal argument name.
         */
        arg = null;
        threwSystemErrorException = false;

        try
        {
            arg = new NominalFormalArg(new ODBCDatabase(), "<<invalid>>");
        }

        catch (SystemErrorException e)
        {
            threwSystemErrorException = true;
        }

        if ( ( arg != null ) ||
             ( ! threwSystemErrorException ) )
        {
            failures++;


            if ( verbose )
            {
                if ( arg != null )
                {
                    outStream.print(
                        "new NominalFormalArg(db, \"<<valid>>\") != null.\n");
                }

                if ( ! threwSystemErrorException )
                {
                    outStream.print("new NominalFormalArg(db, \"<<invalid>>\") "
                        + "didn't throw a SystemErrorException.\n");
                }
            }
        }

        if ( failures > 0 )
        {
            pass = false;

            if ( verbose )
            {
                outStream.printf("%d failures.\n", failures);
            }
        }
        else if ( verbose )
        {
            outStream.print("All tests passed.\n");
        }

        if ( verbose )
        {
            /* print the banner again. */
            outStream.print(testBanner);
        }

        if ( pass )
        {
            outStream.print(passBanner);
        }
        else
        {
            outStream.print(failBanner);
        }

        return pass;

    } /* NominalFormalArg::Test2ArgConstructor() */


    /**
     * TestCopyConstructor()
     *
     * Run a battery of tests on the copy constructor for this
     * class, and on the instance returned.
     *
     * Changes:
     *
     *    - None.
     */

    public static boolean TestCopyConstructor(java.io.PrintStream outStream,
                                              boolean verbose)
    {
        String testBanner =
            "Testing copy constructor for class NominalFormalArg              ";
        String passBanner = "PASSED\n";
        String failBanner = "FAILED\n";
        boolean pass = true;
        boolean threwSystemErrorException = false;
        int failures = 0;
        String s = null;
        NominalFormalArg arg = null;
        NominalFormalArg copyArg = null;
        NominalFormalArg munged = null;

        outStream.print(testBanner);

        if ( verbose )
        {
            outStream.print("\n");
        }

        /* first set up the instance of NominalFormalArg to be copied: */
        threwSystemErrorException = false;

        try
        {
            arg = new NominalFormalArg(new ODBCDatabase(), "<copy_this>");
        }

        catch (SystemErrorException e)
        {
            threwSystemErrorException = true;
        }

        if ( ( arg == null ) ||
             ( threwSystemErrorException ) )
        {
            failures++;

            if ( verbose )
            {
                if ( arg == null )
                {
                    outStream.print(
                        "new NominalFormalArg(\"<copy_this>\")\" returned null.\n");
                }

                if ( threwSystemErrorException )
                {
                    outStream.print("new NominalFormalArg(\"<copy_this>\")\" " +
                                     "threw a SystemErrorException.\n");
                }
            }
        }

        if ( failures == 0 )
        {
            threwSystemErrorException = false;

            try
            {
                arg.setHidden(true);
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( threwSystemErrorException )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("\"arg.setHidden(true)\" threw a " +
                                     "SystemErrorException.\n");
                }
            }
            else if ( ! arg.getHidden() )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("Unexpected value of arg.hidden.\n");
                }
            }
        }


        /* Now, try to make a copy of arg */

        if ( failures == 0 )
        {
            copyArg = null;
            threwSystemErrorException = false;

            try
            {
                copyArg = new NominalFormalArg(arg);
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( ( copyArg == null ) ||
                 ( threwSystemErrorException ) )
            {
                failures++;

                if ( verbose )
                {
                    if ( copyArg == null )
                    {
                        outStream.print(
                            "new NominalFormalArg(arg)\" returned null.\n");
                    }

                    if ( threwSystemErrorException )
                    {
                        outStream.print("new NominalFormalArg(arg)\" " +
                                         "threw a SystemErrorException.\n");
                    }
                }
            }
        }

        /* verify that the copy is good */

        if ( failures == 0 )
        {
            if ( arg == copyArg )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print("(arg == copyArg) ==> " +
                            "same object, not duplicates.\n");
                }
            }
        }

        if ( failures == 0 )
        {
            if ( arg.getFargName().compareTo(copyArg.getFargName()) != 0 )
            {
                failures++;

                if ( verbose )
                {
                    outStream.printf("arg.fargName = \"%s\" != \" " +
                            "copyArg.fArgName = \"%s\".\n", arg.fargName,
                            copyArg.fargName);
                }
            }
        }

        if ( failures == 0 )
        {
            if ( arg.getHidden() != copyArg.getHidden() )
            {
                failures++;

                if ( verbose )
                {
                    outStream.printf("arg.hidden = %b != " +
                            "copyArg.hidden = %b.\n", arg.hidden,
                            copyArg.hidden);
                }
            }
        }

        if ( failures == 0 )
        {
            if ( arg.getItsVocabElement() != copyArg.getItsVocabElement() )
            {
                failures++;

                if ( verbose )
                {
                    outStream.printf("arg.getItsVocabElement() != \" " +
                            "copyArg.getItsVocabElement().\n");
                }
            }
        }

        /* now verify that we fail when we should */

        /* first ensure that the copy constructor failes when passed null */
        if ( failures == 0 )
        {
            munged = copyArg; /* save the copy for later */
            copyArg = null;
            threwSystemErrorException = false;

            try
            {
                copyArg = null;
                copyArg = new NominalFormalArg(copyArg);
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( ( copyArg != null ) ||
                 ( ! threwSystemErrorException ) )
            {
                failures++;

                if ( verbose )
                {
                    if ( copyArg != null )
                    {
                        outStream.print(
                            "new NominalFormalArg(null)\" returned.\n");
                    }

                    if ( ! threwSystemErrorException )
                    {
                        outStream.print("new NominalFormalArg(null)\" " +
                                       "didn't throw a SystemErrorException.\n");
                    }
                }
            }
        }

        /* now corrupt the fargName field of and instance of NominalFormalArg,
         * and verify that this causes a copy to fail.
         */
        if ( failures == 0 )
        {
            copyArg = null;
            threwSystemErrorException = false;

            munged.fargName = "<an invalid name>";

            try
            {
                copyArg = new NominalFormalArg(munged);
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( ( copyArg != null ) ||
                 ( ! threwSystemErrorException ) )
            {
                failures++;

                if ( verbose )
                {
                    if ( copyArg != null )
                    {
                        outStream.print(
                            "new NominalFormalArg(munged)\" returned.\n");
                    }

                    if ( ! threwSystemErrorException )
                    {
                        outStream.print("new NominalFormalArg(munged)\" " +
                                "didn't throw a SystemErrorException.\n");
                    }
                }
            }
        }

        if ( failures > 0 )
        {
            pass = false;

            if ( verbose )
            {
                outStream.printf("%d failures.\n", failures);
            }
        }
        else if ( verbose )
        {
            outStream.print("All tests passed.\n");
        }

        if ( verbose )
        {
            /* print the banner again. */
            outStream.print(testBanner);
        }

        if ( pass )
        {
            outStream.print(passBanner);
        }
        else
        {
            outStream.print(failBanner);
        }

        return pass;

    } /* NominalFormalArg::TestCopyConstructor() */


    /**
     * TestIsValidValue()
     *
     * Verify that isValidValue() does more or less the right thing.
     *
     * Since isValidValue() uses the type tests defined in class Database,
     * and since those methods are tested extensively elsewhere, we only
     * need to verify that they are called correctly.
     *
     *                                          JRM -- 3/11/07
     *
     * Changes:
     *
     *    - None.
     */

    public static boolean TestIsValidValue(java.io.PrintStream outStream,
                                           boolean verbose)
        throws SystemErrorException
    {
        String testBanner =
            "Testing isValidValue()                                           ";
        String passBanner = "PASSED\n";
        String failBanner = "FAILED\n";
        String systemErrorExceptionString = null;
        boolean methodReturned = false;
        boolean threwSystemErrorException = false;
        boolean pass = true;
        boolean result;
        int failures = 0;
        int testNum = 0;
        final int numTestObjects = 12;
        /* TODO -- must add predicates to this test */
        Object[] testObjects = new Object[]
        {
            /* test  0 -- should return false */ " A Valid \t Text String ",
            /* test  1 -- should return false */ new Double(0.0),
            /* test  2 -- should return false */ new Long(0),
            /* test  3 -- should return true  */ "A Valid Nominal",
            /* test  4 -- should return false */ " A Valid Quote String ",
            /* test  5 -- should return false */ new TimeStamp(60),
            /* test  6 -- should return false */ new TimeStamp(30, 300),
            /* test  7 -- should return false */ "an invalid text \b string",
            /* test  8 -- should return false */ new Float(0.0),
            /* test  9 -- should return false */ new Integer(0),
            /* test 10 -- should return false */ " An Invalid Nominal \b ",
            /* test 11 -- should return false */ " An Invalid \t Quote string ",
        };
        String[] testDesc = new String[]
        {
            /* test  0 -- should return false */ " A Valid Text String ",
            /* test  1 -- should return false */ "new Double(0.0)",
            /* test  2 -- should return false */ "new Long(0)",
            /* test  3 -- should return true  */ "A Valid Nominal",
            /* test  4 -- should return false */ " A Valid Quote String ",
            /* test  5 -- should return false */ "new TimeStamp(60)",
            /* test  6 -- should return false */ "new TimeStamp(30, 300)",
            /* test  7 -- should return false */ "an invalid text \b string",
            /* test  8 -- should return false */ "new Float(0.0)",
            /* test  9 -- should return false */ "new Integer(0)",
            /* test 10 -- should return false */ " An Invalid \t Nominal \b ",
            /* test 11 -- should return false */ " An Invalid \t Quote string ",
        };
        boolean[] expectedResult = new boolean[]
        {
            /* test  0 should return */ false,
            /* test  1 should return */ false,
            /* test  2 should return */ false,
            /* test  3 should return */ true,
            /* test  4 should return */ false,
            /* test  5 should return */ false,
            /* test  6 should return */ false,
            /* test  7 should return */ false,
            /* test  8 should return */ false,
            /* test  9 should return */ false,
            /* test 10 should return */ false,
            /* test 11 should return */ false,
        };
        NominalFormalArg arg = null;

        outStream.print(testBanner);

        if ( verbose )
        {
            outStream.print("\n");
        }

        arg = null;
        threwSystemErrorException = false;
        systemErrorExceptionString = null;

        try
        {
            arg = new NominalFormalArg(new ODBCDatabase());
        }

        catch (SystemErrorException e)
        {
            threwSystemErrorException = true;
            systemErrorExceptionString = e.getMessage();
        }

        if ( ( arg == null ) || ( threwSystemErrorException ) )
        {
            failures++;

            if ( verbose )
            {
                if ( arg == null )
                {
                    outStream.print(
                            "new NominalFormalArg(db) returned null.\n");
                }

                if ( threwSystemErrorException )
                {
                    outStream.printf("new NominalFormalArg(db) threw " +
                                      "system error exception: \"%s\"\n",
                                      systemErrorExceptionString);
                }
            }
        }

        if ( failures == 0 )
        {
            while ( testNum < numTestObjects )
            {
                if ( verbose )
                {
                    outStream.printf("test %d: arg.isValidValue(%s) --> %b: ",
                            testNum, testDesc[testNum],
                            expectedResult[testNum]);
                }

                threwSystemErrorException = false;
                result = false;

                try
                {
                    result = arg.isValidValue(testObjects[testNum]);
                }
                catch (SystemErrorException e)
                {
                    threwSystemErrorException = true;
                }

                if ( ( threwSystemErrorException ) ||
                     ( result != expectedResult[testNum] ) )
                {
                    failures++;
                    if ( verbose )
                    {
                        if ( threwSystemErrorException )
                        {
                            outStream.print("failed -- unexpected exception.\n");
                        }
                        else
                        {
                            outStream.print("failed.\n");
                        }
                    }
                }
                else if ( verbose )
                {
                    outStream.print("passed.\n");
                }

                testNum++;
            }
        }

        /* Now verify that isValidValue() throws a system error when passed
         * a null.
         */

        if ( arg != null )
        {
            if ( verbose )
            {
                outStream.printf(
                        "test %d: arg.isValidValue(null) --> exception: ",
                        testNum);
            }

            methodReturned = false;
            threwSystemErrorException = false;
            result = false;

            try
            {
                result = arg.isValidValue(null);
                methodReturned = true;
            }
            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( ( result != false ) ||
                 ( methodReturned ) ||
                 ( ! threwSystemErrorException ) )
            {
                failures++;

                if ( verbose )
                {
                    if ( threwSystemErrorException )
                    {
                        outStream.print("failed -- unexpected exception.\n");
                    }
                    else if ( methodReturned )
                    {
                        outStream.print("failed -- unexpected return.\n");
                    }
                    else
                    {
                        outStream.print("failed -- unexpected result.\n");
                    }
                }
            }
            else if ( verbose )
            {
                outStream.print("passed.\n");
            }

            testNum++;
        }

        if ( failures > 0 )
        {
            pass = false;

            if ( verbose )
            {
                outStream.printf("%d failures.\n", failures);
            }
        }
        else if ( verbose )
        {
            outStream.print("All tests passed.\n");
        }

        if ( verbose )
        {
            /* print the banner again. */
            outStream.print(testBanner);
        }

        if ( pass )
        {
            outStream.print(passBanner);
        }
        else
        {
            outStream.print(failBanner);
        }

        return pass;

    } /* NominalFormalArg::TestIsValidValue() */


    /**
     * TestToStringMethods()
     *
     * Test the toString() and toDBString() methods.
     *
     *              JRM -- 3/11/07
     *
     * Changes:
     *
     *    - None.
     */

    public static boolean TestToStringMethods(java.io.PrintStream outStream,
                                              boolean verbose)
        throws SystemErrorException
    {
        String testBanner =
            "Testing toString() & toDBString()                                ";
        String passBanner = "PASSED\n";
        String failBanner = "FAILED\n";
        boolean methodReturned = false;
        boolean threwSystemErrorException = false;
        boolean pass = true;
        int failures = 0;
        NominalFormalArg arg = null;

        outStream.print(testBanner);

        if ( verbose )
        {
            outStream.print("\n");
        }

        if ( failures == 0 )
        {
            threwSystemErrorException = false;

            try
            {
                arg = new NominalFormalArg(new ODBCDatabase(), "<test>");
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( ( arg == null ) ||
                 ( threwSystemErrorException ) )
            {
                failures++;

                if ( verbose )
                {
                    if ( arg == null )
                    {
                        outStream.print("new NominalFormalArg(db, \"<test>\")" +
                                         "returned null.\n");
                    }

                    if ( threwSystemErrorException )
                    {
                        outStream.print("new NominalFormalArg(db, \"<test>\") " +
                                         "threw a SystemErrorException.\n");
                    }
                }

                arg = null;
            }
        }

        if ( failures == 0 )
        {
            if ( arg != null )
            {
                if ( arg.toString().compareTo("<test>") != 0 )
                {
                    failures++;
                    outStream.printf(
                        "arg.toString() returned unexpected value(1): \"%s\".\n",
                        arg.toString());
                }
            }

            if ( arg != null )
            {
                if ( arg.toDBString().compareTo(
                        "(NominalFormalArg 0 <test> false ())") != 0 )
                {
                    failures++;
                    outStream.printf(
                        "arg.toDBString() returned unexpected value(1): \"%s\".\n",
                        arg.toDBString());
                }
            }
        }

        /* now set subRange, add some approved nominals, and verify that
         * this is reflected in the output from toDBString().
         */
        if ( failures == 0 )
        {
            threwSystemErrorException = false;

            try
            {
                arg.setSubRange(true);
                arg.addApproved("foxtrot");
                arg.addApproved("bravo");
                arg.addApproved("delta");
            }

            catch (SystemErrorException e)
            {
                threwSystemErrorException = true;
            }

            if ( threwSystemErrorException )
            {
                failures++;

                if ( verbose )
                {
                    outStream.print(
                            "SystemErrorException thrown in setup(1).\n");
                }
            }
        }

        if ( failures == 0 )
        {
            if ( arg != null )
            {
                if ( arg.toString().compareTo("<test>") != 0 )
                {
                    failures++;
                    outStream.printf(
                        "arg.toString() returned unexpected value(2): \"%s\".\n",
                        arg.toString());
                }
            }

            if ( arg != null )
            {
                if ( arg.toDBString().compareTo(
                        "(NominalFormalArg 0 <test> true (bravo, delta, foxtrot))") != 0 )
                {
                    failures++;
                    outStream.printf(
                        "arg.toDBString() returned unexpected value(2): \"%s\".\n",
                        arg.toDBString());
                }
            }
        }


        if ( failures > 0 )
        {
            pass = false;

            if ( verbose )
            {
                outStream.printf("%d failures.\n", failures);
            }
        }
        else if ( verbose )
        {
            outStream.print("All tests passed.\n");
        }

        if ( verbose )
        {
            /* print the banner again. */
            outStream.print(testBanner);
        }

        if ( pass )
        {
            outStream.print(passBanner);
        }
        else
        {
            outStream.print(failBanner);
        }

        return pass;

    } /* NominalFormalArg::TestToStringMethods() */

} /* class NominalFormalArg */
