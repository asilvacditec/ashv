/*
 * @(#)BasicScrollManager.java
 *
 * Copyright 2002 EGANTT LLP. All rights reserved.
 * PROPRIETARY/QPL. Use is subject to license terms.
 */

package com.egantt.swing.scroll.manager;

import com.egantt.model.scrolling.ScrollingRange;

import com.egantt.model.scrolling.range.event.RangeEvent;
import com.egantt.model.scrolling.range.event.RangeListener;

import javax.swing.event.ChangeEvent;

public class BasicScrollManager extends AbstractScrollManager
{
	protected RangeListener listener;
	protected ScrollingRange model;

	//	________________________________________________________________________

	public void setRangeModel(ScrollingRange model)
	{
		if (this.model != null)
			model.removeRangeListener(listener);
		else
			this.listener = new LocalRangeListener();

		this.model = model;
		model.addRangeListener(listener);
	}

	//	________________________________________________________________________

   @Override
public int getMinimum()
	{
		return 0;
	}

	@Override
	public int getMaximum()
	{
		return model.getRange();
	}

	//	________________________________________________________________________

	@Override
	public int getExtent()
	{
		return model.getExtent();
	}

	@Override
	public int getValue()
	{
		return model.getValue();
	}

	//	________________________________________________________________________

	@Override
	public void setValue(int value)
	{
		model.setValue(value);
   }

	//	________________________________________________________________________
	
	@Override
	public int getBlockIncrement() {
		return model.getBlockIncrement(); 
	}

	@Override
	public int getUnitIncrement() {
		return model.getUnitIncrement();
	}
	
	//	________________________________________________________________________

	protected class LocalRangeListener implements RangeListener
	{
	   @Override
	public void stateChanged(RangeEvent event)
	   {
			fireChanged(new ChangeEvent(event.getSource()));
	   }
	}
}
