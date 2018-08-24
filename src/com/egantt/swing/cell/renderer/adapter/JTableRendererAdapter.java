/*
 * @(#)JTableRendererAdapter.java
 *
 * Copyright 2002 EGANTT LLP. All rights reserved.
 * PROPRIETARY/QPL. Use is subject to license terms.
 */

package com.egantt.swing.cell.renderer.adapter;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import com.egantt.model.drawing.DrawingState;
import com.egantt.swing.cell.CellRenderer;
import com.egantt.swing.cell.CellState;
import com.egantt.swing.cell.state.BasicCellState;

public class JTableRendererAdapter implements TableCellRenderer {
	protected final CellRenderer renderer;

	protected final boolean header;

	protected final boolean editor;

	protected final DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

	protected final LocalRenderer localRenderer = new LocalRenderer();

	public JTableRendererAdapter(CellRenderer renderer, boolean editor,
			boolean header) {
		this.renderer = renderer;
		this.editor = editor;
		this.header = header;
	}

	// _________________________________________________________________________

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		final BasicCellState state;
		{
			Rectangle bounds = table.getCellRect(row, column, false);
			state = new BasicCellState(table, value, isSelected, hasFocus, row,
					column, bounds);
		}

		if (!(value instanceof DrawingState))
			return initialize(defaultRenderer, state);

		localRenderer.setComponent(renderer.getComponent(state, localRenderer));
		localRenderer.setState(state);
		return initialize(localRenderer, state);
	}

	protected Component initialize(DefaultTableCellRenderer renderer,
			BasicCellState cellState) {
		renderer = (DefaultTableCellRenderer) renderer
				.getTableCellRendererComponent((JTable) cellState.getSource(),
						cellState.getValue(), cellState.isSelected(), cellState
								.hasFocus(), cellState.getRow(), cellState
								.getColumn());
		if (!header)
			return renderer;

		JTable table = (JTable) cellState.getSource();
		if (table != null) {
			JTableHeader header = table.getTableHeader();
			if (header != null) {
				renderer.setForeground(header.getForeground());
				renderer.setBackground(header.getBackground());
				renderer.setFont(header.getFont());
			}
		}

		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		renderer.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		return renderer;
	}

	// _________________________________________________________________________

	// protected class LocalHeaderRender extends
	protected class LocalRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 4437246647534469820L;

		protected JComponent component;

		private CellState cellState;

		protected LocalRenderer() {
		}

		// ____________________________________________________________________

		public void setState(CellState cellState) {
			this.cellState = cellState;

		}

		public void setComponent(JComponent component) {
			this.component = component;
		}

		// ____________________________________________________________________

		@Override
		public String getToolTipText() {
			return null;
		}

		@Override
		public String getToolTipText(MouseEvent event) {
			return component.getToolTipText(event);
		}

		// ____________________________________________________________________

		@Override
		public void paint(Graphics g) {
			if (editor) {
				boolean isSelected = ((JTable) cellState.getSource())
						.isCellSelected(cellState.getRow(), cellState
								.getColumn());
				getTableCellRendererComponent((JTable) cellState.getSource(),
						cellState.getValue(), isSelected, cellState.hasFocus(),
						cellState.getRow(), cellState.getColumn());
			}
			super.setValue(null); // do not allow the parent to render any
									// text
			super.paint(g);

			if (component != null) {
				component.setOpaque(false);
				component.setBounds(0, 0, getWidth(), getHeight());
				component.paint(g);
			}
		}
	}
}