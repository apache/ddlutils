package org.apache.ddlutils.task;

import org.apache.log4j.Level;
import org.apache.tools.ant.types.EnumeratedAttribute;

/**
 * Helper class that defines the possible values for the verbosity attribute.
 * 
 * @ant.task ignore="true"
 */
public class VerbosityLevel extends EnumeratedAttribute {
    /** The possible levels. */
    private static final String[] LEVELS = { Level.FATAL.toString().toUpperCase(),
                                             Level.ERROR.toString().toUpperCase(),
                                             Level.WARN.toString().toUpperCase(),
                                             Level.INFO.toString().toUpperCase(),
                                             Level.DEBUG.toString().toUpperCase(),
                                             Level.FATAL.toString().toLowerCase(),
                                             Level.ERROR.toString().toLowerCase(),
                                             Level.WARN.toString().toLowerCase(),
                                             Level.INFO.toString().toLowerCase(),
                                             Level.DEBUG.toString().toLowerCase() };

    /**
     * Creates an uninitialized verbosity level object.
     */
    public VerbosityLevel()
    {
        super();
    }

    /**
     * Creates an initialized verbosity level object.
     * 
     * @param level The level
     */
    public VerbosityLevel(String level)
    {
        super();
        setValue(level);
    }

    /**
     * {@inheritDoc}
     */
    public String[] getValues() {
        return LEVELS;
    }

    /**
     * Determines whether this is DEBUG verbosity.
     * 
     * @return <code>true</code> if this is the DEBUG level
     */
    public boolean isDebug()
    {
        return Level.DEBUG.toString().equalsIgnoreCase(getValue());
    }
}