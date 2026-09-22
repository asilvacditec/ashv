/*
 * @(#)BasicModelAdapter.java
 *
 * Copyright 2002 EGANTT LLP. All rights reserved.
 * PROPRIETARY/QPL. Use is subject to license terms.
 */

package com.egantt.swing.table.model.adapter;

import com.egantt.swing.table.model.ColumnModel;
import com.egantt.swing.table.model.RowModel;

/**
 *  Provides the bridge from the generic E-Gantt table model implementation
 *  to the swing TableModel used by swing.
 */
public class BasicModelAdapter extends AbstractModelAdapter
{
	protected final ColumnModel columns;
	protected final RowModel rows;

	public BasicModelAdapter(ColumnModel columns, RowModel rows)
	{
		this.columns = columns;
		this.rows = rows;
	}

	// _________________________________________________________________________
	
	@Override
	public int getColumnCount()
	{
	   return columns.size();
   }

	@Override
	public int getRowCount()
	{
	   return rows.size();
	}

	// _________________________________________________________________________

	@Override
	public String getColumnName(int index)
	{
		return columns.getColumn(index).toString();
	}

	@Override
	public Class getColumnClass(int index)
	{
		return columns.getColumnClass(index);
	}

	// _________________________________________________________________________

	@Override
	public Object getValueAt(int row, int column)
	{
		return rows.getValueAt(row, column);
	}

	@Override
	public void setValueAt(Object value, int row, int column)
	{
		rows.setValueAt(value, row, column);
	}

	// _________________________________________________________________________

	@Override
	public boolean isCellEditable(int row, int column)
	{
		return true;
	}
}
