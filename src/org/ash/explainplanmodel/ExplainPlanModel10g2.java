/*
 *-------------------
 * The ExplainPlanModel10g2.java is part of ASH Viewer
 *-------------------
 * 
 * ASH Viewer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ASH Viewer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ASH Viewer.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2009, Alex Kardapolov, All rights reserved.
 *
 */
package org.ash.explainplanmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.ash.util.Utils;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableModel;

public class ExplainPlanModel10g2 extends AbstractTreeTableModel implements ExplainPlanModel {

	// Names of the columns.
	private final String[] cNames = {
			"Description",// Visible
			"Address",
			"Hash value",
			"SQL id",
			"PHV",
			"Child number",
			"Operation",
			"Options",
			"Object node",
			"Object#",
			"Object owner",
			"Object name",// Visible
			"Object alias",
			"Object type",// Visible
			"Optimizer",
			"Id",
			"Parent Id",
			"Depth",
			"Position",
			"Search columns",
			"Cost",// Visible
			"Cardinality",// Visible
			"Bytes",// Visible
			"Other tag", "Partition start", "Partition stop", "Partition id",
			"Other", "Distribution", "CPU cost", "IO cost", "Temp space",
			"Access predicates", "Filter predicates", "Projection", "Time",
			"Qblock name", "Remarks", };
	
	// Types of the columns.
	private final Class<?>[] cTypes = { TreeTableModel.class, String.class,
			String.class, String.class, String.class, String.class,
			String.class, String.class, String.class, String.class,
			String.class, String.class, String.class, String.class,
			String.class, String.class, String.class, String.class,
			String.class, String.class, String.class, String.class,
			String.class, String.class, String.class, String.class,
			String.class, String.class, String.class, String.class,
			String.class, String.class, String.class, String.class,
			String.class, String.class, String.class, String.class,
			String.class };
	
	@SuppressWarnings("unused")
	public static class ExplainRow implements TreeNode {
	  private ExplainRow parent;
	  private List<ExplainRow> children;
	    private String address;
		private Double hashValue;
		private String sqlId;
		private Double planHashValue;
		private Long childNumber;
		private String operation;
		private String options;
		private String description;
		private String objectNode;
		private Double object;
		private String objectOwner;
		private String objectName;
		private String objectAlias;
		private String objectType;
		private String optimizer;
		private Long Id;
		private Long parentId;
		private Long depth;
		private Long position;
		private Long searchColumns;
		private Double cost;
		private Double cardinality;
		private Double bytes;
		private String otherTag;
		private String partitionStart;
		private String partitionStop;
		private Double partitionId;
		private String other;
		private String distribution;
		private Double cpuCost;
		private Double ioCost;
		private Double tempSpace;
		private String accessPredicates;
		private String filterPredicates;
		private String projection;
		private Double time;
		private String qblockName;
		private String remarks;
	  
	  public ExplainRow(ExplainRow parent,
			    String address, Double hashValue, String sqlId, Double planHashValue,
				String childAddress, Long childNumber, String operation, String options,
				String objectNode, Double object, String objectOwner, String objectName, String objectAlias,
				String objectType, String optimizer, Long Id, Long parentId, Long depth, Long position,
				Long searchColumns, Double cost, Double cardinality, Double bytes, String otherTag,
				String partitionStart, String partitionStop, Double partitionId, String other, String distribution,
				Double cpuCost, Double ioCost, Double tempSpace, String accessPredicates, String filterPredicates,
				String projection, Double time, String qblockName, String remarks) {
		 this.parent = parent;
		 	this.address = address;
			this.hashValue = hashValue; 
			this.sqlId = sqlId;
			this.planHashValue = planHashValue;
			this.childNumber = childNumber;
			this.operation = operation; 
			this.options = options;
		this.description = 
			 operation +" "+
			 (options == null ? "" : options)  +" "+
			 (optimizer == null ? "" : optimizer);
			this.objectNode = objectNode; 
			this.object = object;
			this.objectOwner = objectOwner;
			this.objectName = objectName;
			this.objectAlias = objectAlias;
			this.objectType = objectType;
			this.optimizer = optimizer;
			this.Id = Id;
			this.parentId = parentId;
			this.depth = depth;
			this.position = position;
			this.searchColumns = searchColumns;
			this.cost = cost;
			this.cardinality = cardinality;
			this.bytes = bytes;
			this.otherTag = otherTag;
			this.partitionStart = partitionStart;
			this.partitionStop = partitionStop;
			this.partitionId = partitionId;
			this.other = other;
			this.distribution = distribution;
			this.cpuCost = cpuCost;
			this.ioCost = ioCost;
			this.tempSpace = tempSpace;
			this.accessPredicates = accessPredicates;
			this.filterPredicates = filterPredicates;
			this.projection = projection;
			this.time = time;
			this.qblockName = qblockName;
			this.remarks = remarks;
	  }

	  public long getID() {
		 return Id;
	  }

	  //TreeNode Interface
	  @Override
	public TreeNode getParent() {
		 return parent;
	  }

	  @Override
	public Enumeration<ExplainRow> children() {
		 if (children == null) {
			children = new ArrayList<ExplainRow>();
         }
		 return Collections.enumeration(children);
	  }

	  @Override
	public boolean getAllowsChildren() {
		 return true;
	  }

	  @Override
	public int getIndex(TreeNode node) {
		 if (children == null)
			return -1;
		 return children.indexOf(node);
	  }

	  public void addChild(ExplainRow row) {
		 if (children == null) {
			children = new ArrayList<ExplainRow>();
         }
		 children.add(row);
	  }

	  @Override
	public boolean isLeaf() {
		 return  ((children == null)||(children.size() == 0));
	  }

	  public Object getValueAt(int column) {
		 switch (column) {
			case 0: return this.description;
			case 1: return this.address;
			case 2: return this.hashValue; 
			case 3: return this.sqlId;
			case 4: return this.planHashValue;
			case 5: return this.childNumber;
			case 6: return this.operation; 
			case 7: return this.options;
			case 8: return this.objectNode; 
			case 9: return this.object;
			case 10: return this.objectOwner;
			case 11: return this.objectName;
			case 12: return this.objectAlias;
			case 13: return this.objectType;
			case 14: return this.optimizer;
			case 15: return this.Id;
			case 16: return this.parentId;
			case 17: return this.depth;
			case 18: return this.position;
			case 19: return this.searchColumns;
			case 20: {
				Double tmp = this.cost;
				if (this.cost == 0.0){
					return "";
				} else {
					return tmp.longValue();
				}
			}
			case 21: {
				Double tmp = this.cardinality;
				if (this.cardinality == 0.0){
					return "";
				} else {
					return tmp.longValue();
				}
			}
			case 22: {
				if (this.bytes == 0.0){
				  return "";
				} else {
				  return Utils.roundBytes(this.bytes);
				}
			}
			case 23: return this.otherTag;
			case 24: return this.partitionStart;
			case 25: return this.partitionStop;
			case 26: return this.partitionId;
			case 27: return this.other;
			case 28: return this.distribution;
			case 29: return this.cpuCost.longValue();
			case 30: return this.ioCost.longValue();			
			case 31: {				
				if (this.tempSpace == 0.0){
					  return "";
				} else {
					  return Utils.roundBytes(this.tempSpace);
				}
			}			
			case 32: return this.accessPredicates;
			case 33: return this.filterPredicates;
			case 34: return this.projection;
			case 35: {
				return Utils.getTimeFormatting(this.time);
			}
			case 36: return this.qblockName;
			case 37: return this.remarks;
			default: return null;
		 }
	  }

	  @Override
	public int getChildCount() {
		 return (children==null) ? 0 : children.size();
	  }

	  @Override
	public TreeNode getChildAt(int child) {
		 return children.get(child);
	  }

	  public ExplainRow findChild(int id) {
		 for (int i=getChildCount()-1;i>=0;i--) {
			ExplainRow child = (ExplainRow)getChildAt(i);
			if (child.getID() == id) {
			  return child;
			}
		 }
		 return null;
	  }

	  @Override
	public String toString() {
		 return description;
	  }
	}

	public ExplainPlanModel10g2(ExplainRow root) {
		  super(root);
		}

		//
		// The TreeModel interface
		//
		/* (non-Javadoc)
		 * @see org.ash.util.ExplainPlanModelI#getChildCount(java.lang.Object)
		 */
		@Override
		public int getChildCount(Object node) {
		  ExplainRow er = (ExplainRow)node;
		  return er.getChildCount();
		}

		/* (non-Javadoc)
		 * @see org.ash.util.ExplainPlanModelI#getChild(java.lang.Object, int)
		 */
		@Override
		public Object getChild(Object node, int i) {
		  ExplainRow er = (ExplainRow)node;
		  return er.getChildAt(i);
		}

		/* (non-Javadoc)
		 * @see org.ash.util.ExplainPlanModelI#isLeaf(java.lang.Object)
		 */
		@Override
		public boolean isLeaf(Object node) {
			 return ((ExplainRow)node).isLeaf();
		}

		//
		//  The TreeTableNode interface.
		//
		/* (non-Javadoc)
		 * @see org.ash.util.ExplainPlanModelI#getColumnCount()
		 */
		@Override
		public int getColumnCount() {
			 return cNames.length;
		}

		/* (non-Javadoc)
		 * @see org.ash.util.ExplainPlanModelI#getColumnName(int)
		 */
		@Override
		public String getColumnName(int column) {
			 return cNames[column];
		}

		/* (non-Javadoc)
		 * @see org.ash.util.ExplainPlanModelI#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(int column) {
			 return cTypes[column];
		}

		/* (non-Javadoc)
		 * @see org.ash.util.ExplainPlanModelI#getValueAt(java.lang.Object, int)
		 */
		@Override
		public Object getValueAt(Object node, int column) {
			 ExplainRow     fn = (ExplainRow)node;
			 return fn.getValueAt(column);
		}

		/* (non-Javadoc)
		 * @see org.ash.util.ExplainPlanModelI#getIndexOfChild(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int getIndexOfChild(Object parent, Object child) {
			// TODO Auto-generated method stub
			return 0;
		}

}
