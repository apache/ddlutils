package org.apache.ddlutils;

import org.apache.commons.lang.exception.NestableRuntimeException;

/**
 * Is thrown when by the {@link org.apache.ddlutils.dynabean.DynaSql} and
 * related classes when a database operation fails, but a normal
 * {@link java.sql.SQLException} cannot be generated.
 */
public class DynaSqlException extends NestableRuntimeException 
{
    /** Constant for serializing instances of this class */
    private static final long serialVersionUID = 7524362294381844776L;

    /**
     * Creates a new empty exception object.
     */
    public DynaSqlException()
    {
        super();
    }

    /**
     * Creates a new exception object.
     * 
     * @param msg The exception message
     */
    public DynaSqlException(String msg)
    {
        super(msg);
    }

    /**
     * Creates a new exception object.
     * 
     * @param baseEx The base exception
     */
    public DynaSqlException(Throwable baseEx)
    {
        super(baseEx);
    }

    /**
     * Creates a new exception object.
     * 
     * @param msg    The exception message
     * @param baseEx The base exception
     */
    public DynaSqlException(String msg, Throwable baseEx)
    {
        super(msg, baseEx);
    }

}
