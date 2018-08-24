/*
 * @(#)BasicCellState.java
 *
 * Copyright 2002 EGANTT LLP. All rights reserved.
 * PROPRIETARY/QPL. Use is subject to license terms.
 */

package com.egantt.swing.cell.state;

import java.awt.Rectangle;

import com.egantt.model.drawing.DrawingState;
import com.egantt.swing.cell.CellState;

public class BasicCellState implements CellState
{
	protected final Object source, value;

	protected final boolean hasFocus, isSelected;
	protected final int column, row;
	protected final Rectangle bounds;

	public BasicCellState(Object value) {
		this(null, value, false, false, 0, 0, new Rectangle());
	}
	public BasicCellState(Object source, Object value, boolean isSelected,
		boolean hasFocus, int row, int column, Rectangle bounds)
	{
		this.source = source;
		this.value = value;
		this.hasFocus = hasFocus;
		this.isSelected = isSelected;
		this.column = column;
		this.row = row;
		this.bounds = bounds;
	}

	// __________________________________________________________________________

	@Override
	public Object getSource()
	{
		return source;
	}

	@Override
	public Object getValue()
	{
		return value;
	}
	
	@Override
	public DrawingState getDrawing() {
		return value != null && value instanceof DrawingState 
			? (DrawingState) value : null;
	}

	// __________________________________________________________________________

	@Override
	public boolean hasFocus()
	{
		return hasFocus;
	}

	@Override
	public boolean isSelected()
	{
		return isSelected;
	}

	// __________________________________________________________________________

	@Override
	public int getColumn()
	{
		return column;
	}

	@Override
	public int getRow()
	{
		return row;
	}
	
	// __________________________________________________________________________
	
	@Override
	public Rectangle getBounds() {
		return bounds;
	}
}
