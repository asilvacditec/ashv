/*
 * blanco Framework
 * Copyright (C) 2004-2006 WATANABE Yoshinori
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */
package blanco.commons.sql.format;

import java.io.IOException;

@SuppressWarnings("serial")
public class BlancoSqlFormatterException extends IOException {

    public BlancoSqlFormatterException() {
        super();
    }

    public BlancoSqlFormatterException(final String argMessage) {
        super(argMessage);
    }
}
