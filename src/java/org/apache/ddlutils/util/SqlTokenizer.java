package org.apache.ddlutils.util;


/**
 * A statement tokenizer for SQL strings that splits only at delimiters that
 * are at the end of a line or the end of the SQL (row mode).  
 */
public class SqlTokenizer
{
    private String  _sql;
    private int     _lastCharIdx;
    private int     _lastDelimiterPos = -1;
    private int     _nextDelimiterPos = -1;
    private boolean _finished;

    /**
     * Creates a new sql tokenizer.
     * 
     * @param sql The sql text
     */
    public SqlTokenizer(String sql)
    {
        _sql         = sql;
        _lastCharIdx = sql.length() - 1;
    }

    /**
     * Determines whether there are more statements.
     * 
     * @return <code>true</code> if there are more statements
     */
    public boolean hasMoreStatements()
    {
        if (_finished)
        {
            return false;
        }
        else
        {
            if (_nextDelimiterPos <= _lastDelimiterPos)
            {
                _nextDelimiterPos = _sql.indexOf(';', _lastDelimiterPos + 1);
                while ((_nextDelimiterPos >= 0) && (_nextDelimiterPos < _lastCharIdx))
                {
                    char nextChar = _sql.charAt(_nextDelimiterPos + 1);

                    if ((nextChar == '\r') || (nextChar == '\n'))
                    {
                        break;
                    }
                    _nextDelimiterPos = _sql.indexOf(';', _nextDelimiterPos + 1);
                }
            }
            return (_nextDelimiterPos >= 0) || (_lastDelimiterPos < _lastCharIdx);
        }
    }

    /**
     * Returns the next statement.
     * 
     * @return The statement
     */
    public String getNextStatement()
    {
        String result = null;

        if (hasMoreStatements())
        {
            if (_nextDelimiterPos >= 0)
            {
                result            = _sql.substring(_lastDelimiterPos + 1, _nextDelimiterPos);
                _lastDelimiterPos = _nextDelimiterPos;
            }
            else
            {
                result    = _sql.substring(_lastDelimiterPos + 1);
                _finished = true;
            }
        }
        return result;
    }
}
