/*
 * @(#)AbstractCellRenderer.java
 *
 * Copyright 2002 EGANTT LLP. All rights reserved.
 * PROPRIETARY/QPL. Use is subject to license terms.
 */

package com.egantt.swing.cell.renderer.basic;

import com.egantt.swing.cell.CellRenderer;
import com.egantt.swing.cell.CellState;

import javax.swing.JComponent;
import javax.swing.JTable;

import javax.swing.table.DefaultTableCellRenderer;

public abstract class AbstractCellRenderer extends DefaultTableCellRenderer implements CellRenderer
{
	@Override
	public JComponent getComponent(CellState state, JComponent component)
	{
		return (JComponent) getTableCellRendererComponent( (JTable) state.getSource(),
			state.getValue(), state.isSelected(), state.hasFocus(), state.getRow(), state.getColumn());
	}
}
