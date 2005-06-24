package org.apache.ddlutils.io;

import java.util.List;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RulesBase;

/**
 * An extended rules implementation that is able to match case-insensitively. Per default,
 * the rules are matches disregarding the case.
 */
public class DigesterRules extends RulesBase
{
    /** Whether to be case sensitive or not */
    private boolean _caseSensitive = false;

    /**
     * Determines whether this rules object matches case sensitively.
     *
     * @return <code>true</code> if the case of the pattern matters
     */
    public boolean isCaseSensitive()
    {
        return _caseSensitive;
    }


    /**
     * Specifies whether this rules object shall match case sensitively.
     *
     * @param beCaseSensitive <code>true</code> if the case of the pattern shall matter
     */
    public void setCaseSensitive(boolean beCaseSensitive)
    {
        _caseSensitive = beCaseSensitive;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.digester.RulesBase#add(java.lang.String, org.apache.commons.digester.Rule)
     */
    public void add(String pattern, Rule rule)
    {
        super.add(_caseSensitive ? pattern : pattern.toLowerCase(), rule);
    }


    /* (non-Javadoc)
     * @see org.apache.commons.digester.RulesBase#lookup(java.lang.String, java.lang.String)
     */
    protected List lookup(String namespaceURI, String pattern)
    {
        return super.lookup(namespaceURI, _caseSensitive ? pattern : pattern.toLowerCase());
    }
}
