/*
 * blanco Framework
 * Copyright (C) 2004-2006 WATANABE Yoshinori
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */
package blanco.commons.sql.format.valueobject;

/**
 * 
 * http://homepage2.nifty.com/igat/igapyon/diary/2005/ig050613.html <br>
 * 
 * 
 * @author WATANABE Yoshinori (a-san) : original version at 2005.07.04.
 * @author IGA Tosiki : marge into blanc Framework at 2005.07.04
 */
public class BlancoSqlToken extends AbstractBlancoSqlToken {
    /**
     * 
     * @param argType
     * @param argString
     * @param argPos
     */
    public BlancoSqlToken(final int argType, final String argString,
            final int argPos) {
        setType(argType);
        setString(argString);
        setPos(argPos);
    }

    /**
     * 
     * @param argType
     * @param argString
     */
    public BlancoSqlToken(final int argType, final String argString) {
        this(argType, argString, -1);
    }
}
