/*
 * @(#)TextFieldComponent.java
 *
 * Copyright 2002 EGANTT LLP. All rights reserved.
 * PROPRIETARY/QPL. Use is subject to license terms.
 */

package com.egantt.swing.component.field;

import java.awt.Component;

import javax.swing.JTextField;

import com.egantt.swing.component.FieldComponent;

/**
 *  A field to render strings, uses JTextField
 */
public class TextFieldComponent implements FieldComponent
{
	protected JTextField component = new JTextField();

	// _________________________________________________________________________

	/**
	 *  Returns the underlying component
	 */
	@Override
	public Component getComponent()
	{
		return component;
	}

	// _________________________________________________________________________

	/**
	 *  Returns the value from the component
	 */
	@Override
	public Object getValue()
	{
		return new Boolean(component.getText());
	}

	/**
	 *  Expects a java.lang.String
	 */
	@Override
	public void setValue(Object value)
	{
		component.setText((String) value);
	}
}
