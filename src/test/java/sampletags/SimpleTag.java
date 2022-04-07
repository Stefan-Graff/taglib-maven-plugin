/**
 *
 * Copyright (C) 2004-2014 Fabrizio Giustina
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package sampletags;

import javax.servlet.jsp.tagext.TagSupport;


/**
 * Simple tag for junit tests.
 * @author Fabrizio Giustina
 * @version $Revision: 217 $ ($Author: fgiust $)
 */
public class SimpleTag extends TagSupport
{

    /**
     * <code>serialVersionUID</code>.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Ant attribute.
     */
    private String ant;

    /**
     * Setter for ant attribute.
     * @return ant attribute value
     */
    public String getAnt()
    {
        return this.ant;
    }

    /**
     * Setter for ant attribute.
     * @param value attribute value
     */
    public void setAnt(String value)
    {
        this.ant = value;
    }

}
