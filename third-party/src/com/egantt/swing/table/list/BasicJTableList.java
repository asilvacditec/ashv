/*
 * @(#)BasicJTableList.java
 *
 * Copyright 2002 EGANTT LLP. All rights reserved.
 * PROPRIETARY/QPL. Use is subject to license terms.
 */
package com.egantt.swing.table.list;

import org.jdesktop.swingx.JXTable;
import com.egantt.model.component.ComponentList;

/**
 *  Generates JTable's based on the Component list
 */
public class BasicJTableList extends AbstractTableList implements ComponentList
{
	// __________________________________________________________________________

	@Override
	protected JXTable createTable()
	{
		return new JXTable();
	}
}
