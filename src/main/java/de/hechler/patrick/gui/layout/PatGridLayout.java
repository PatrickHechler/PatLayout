// This file is part of the Pat-Layout Project
// DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
// Copyright (C) 2023 Patrick Hechler
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.
package de.hechler.patrick.gui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * this class implements the {@link LayoutManager2} interface
 * <p>
 * each {@link PatGridLayout} has:
 * <ul>
 * <li>a map which maps all known {@link Component Components} to a {@link CompInfo} instance</li>
 * <li>a list of {@link BlockInfo blocks} for the {@link #xBlocks() x} and {@link #yBlocks() y} axes</li>
 * <li>a list of repeating {@link BlockInfo blocks} for the {@link #xAfterRepeatBlocks() x} and
 * {@link #yAfterRepeatBlocks() y} axes after the above list ran out of blocks</li>
 * <li>a number of empty pixels between each block for the {@link #xEmpty() x} and {@link #yEmpty() y} axes</li>
 * </ul>
 * to create a {@link PatGridLayout}:
 * <ul>
 * <li>a string constructor can be used
 * ({@link #PatGridLayout(String, String)}/{@link #PatGridLayout(int, int, String, String)})</li>
 * <li>a default constructor can be used ({@link #PatGridLayout()}/{@link #PatGridLayout(int, int)})</li>
 * <li>a {@link BlockInfo BlockInfo[]} constructor can be used
 * ({@link #PatGridLayout(int, int, BlockInfo[], BlockInfo[])}/{@link #PatGridLayout(int, int, BlockInfo[], BlockInfo[], BlockInfo[], BlockInfo[])})</li>
 * </ul>
 * note that {@link PatGridLayout} instances are modifiable:
 * <ul>
 * <li>{@link #xBlocks(BlockInfo[])}/{@link #yBlocks(BlockInfo[])} sets the first x/y blocks</li>
 * <li>{@link #xAfterRepeatBlocks(BlockInfo[])}/{@link #yAfterRepeatBlocks(BlockInfo[])} sets the x/y repeat blocks</li>
 * <li>{@link #xEmpty(int)}/{@link #yEmpty(int)} sets the empty x/y space between blocks</li>
 * </ul>
 * <p>
 * to add a {@link Component} to a {@link PatGridLayout}:
 * <ul>
 * <li>the {@link #addLayoutComponent(Component, Object)} or {@link #addLayoutComponent(String, Component)} method has
 * to be invoked either explicitly or implicitly by using a {@link Container#add(Component, Object)} method
 * <ul>
 * <li>the {@link #addLayoutComponent(String, Component)} accepts a {@link String} which has to understood by
 * {@link CompInfo#parse(String)}</li>
 * <li>the {@link #addLayoutComponent(Component, Object)} accepts:
 * <ul>
 * <li>a {@link String} which has to understood by {@link CompInfo#parse(String)}</li>
 * <li>a {@link CompInfo} instance</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @author Patrick Hechler
 * 
 * @see BlockInfo
 * @see CompInfo
 */
public class PatGridLayout implements LayoutManager2 {
	
	/**
	 * the name of the logger used here<br>
	 * note that to use {@link Logger#getLogger(String)}, <code>java.logging</code> needs to be required separately in
	 * the <code>modlue-info</code>
	 * 
	 * @see Logger#getLogger(String)
	 */
	public static final String LOGGER = "de.hechler.patrick.gui.layout";
	
	private static final Level LOG_LEVEL = Level.CONFIG;
	
	static final int MAX_BLOCK_SIZE = Integer.MAX_VALUE >>> 7;
	
	/**
	 * an empty array with {@link BlockInfo} as component type<br>
	 * used to avoid creating too many arrays of length zero
	 */
	public static final BlockInfo[] EMPTY_BLOCK_INFOS = new BlockInfo[0];
	
	private final Map<Component,CompInfo> comps = new HashMap<>();
	
	private int         xempty;
	private int         yempty;
	private BlockInfo[] xblocks;
	private BlockInfo[] yblocks;
	private BlockInfo[] xafterblocks;
	private BlockInfo[] yafterblocks;
	
	/**
	 * creates a new {@link PatGridLayout} with no {@link #xBlocks()}, no {@link #yBlocks()} and
	 * {@link #xAfterRepeatBlocks()} and {@link #yAfterRepeatBlocks()} set to an array with one entry which has both
	 * values set to {@link BlockInfo#DYNAMIC}
	 * <p>
	 * this means there is no limit to the block coordinates for the {@link Component components} and all blocks are set
	 * to <code>[grow]</code>
	 */
	public PatGridLayout() {
		this.xblocks = EMPTY_BLOCK_INFOS;
		this.yblocks = EMPTY_BLOCK_INFOS;
		this.xafterblocks = new BlockInfo[]{ new BlockInfo(BlockInfo.DYNAMIC, BlockInfo.DYNAMIC) };
		this.yafterblocks = new BlockInfo[]{ new BlockInfo(BlockInfo.DYNAMIC, BlockInfo.DYNAMIC) };
		// empty defaults to 0
	}
	
	/**
	 * like {@link #PatGridLayout()} but also sets {@link #xEmpty(int)} and {@link #yEmpty(int)}
	 * 
	 * @param xEmpty the value of {@link #xEmpty()}
	 * @param yEmpty the value of {@link #yEmpty()}
	 * 
	 * @see #PatGridLayout()
	 * @see #xEmpty(int)
	 * @see #yEmpty(int)
	 */
	public PatGridLayout(int xEmpty, int yEmpty) {
		this();
		xEmpty(xEmpty);
		yEmpty(yEmpty);
	}
	
	/**
	 * this constructor just delegates to
	 * <code>{@link #PatGridLayout(int, int, BlockInfo[], BlockInfo[], BlockInfo[], BlockInfo[]) PatGridLayout}(xEmpty, yEmpty, xBlocks, {@link #EMPTY_BLOCK_INFOS}, yBlocks, {@link #EMPTY_BLOCK_INFOS})</code>
	 * 
	 * @param xEmpty  the empty x space between blocks
	 * @param yEmpty  the empty y space between blocks
	 * @param xBlocks the x blocks
	 * @param yBlocks the y blocks
	 * 
	 * @see #xEmpty(int)
	 * @see #yEmpty(int)
	 * @see #xBlocks(BlockInfo[])
	 * @see #yBlocks(BlockInfo[])
	 */
	public PatGridLayout(int xEmpty, int yEmpty, BlockInfo[] xBlocks, BlockInfo[] yBlocks) {
		this(xEmpty, yEmpty, xBlocks, EMPTY_BLOCK_INFOS, yBlocks, EMPTY_BLOCK_INFOS);
	}
	
	/**
	 * creates a new {@link PatGridLayout} with the given
	 * <ul>
	 * <li>empty sizes ({@code xEmpty} and {@code yEmpty}),</li>
	 * <li>blocks ({@code xBlocks} and {@code yBlocks})</li>
	 * <li>and repeat blocks ({@code xAfterRepeatBlocks} and {@code yAfterRepeatBlocks})</li>
	 * </ul>
	 * 
	 * @param xEmpty             the empty x space between blocks
	 * @param yEmpty             the empty y space between blocks
	 * @param xBlocks            the x blocks
	 * @param yBlocks            the y blocks
	 * @param xAfterRepeatBlocks the x blocks which repeat after {@code xBlocks} ran out of blocks
	 * @param yAfterRepeatBlocks the y blocks which repeat after {@code yBlocks} ran out of blocks
	 * 
	 * @see #xEmpty(int)
	 * @see #yEmpty(int)
	 * @see #xBlocks(BlockInfo[])
	 * @see #yBlocks(BlockInfo[])
	 * @see #xAfterRepeatBlocks(BlockInfo[])
	 * @see #yAfterRepeatBlocks(BlockInfo[])
	 */
	public PatGridLayout(int xEmpty, int yEmpty, BlockInfo[] xBlocks, BlockInfo[] xAfterRepeatBlocks,
		BlockInfo[] yBlocks, BlockInfo[] yAfterRepeatBlocks) {
		if ( this.getClass() != PatGridLayout.class ) {
			// its a valid state, but can't be used for anything
			xBlocks = EMPTY_BLOCK_INFOS;
			yBlocks = EMPTY_BLOCK_INFOS;
			xAfterRepeatBlocks = EMPTY_BLOCK_INFOS;
			yAfterRepeatBlocks = EMPTY_BLOCK_INFOS;
		}
		xEmpty(xEmpty);
		yEmpty(yEmpty);
		xBlocks(xBlocks);
		yBlocks(yBlocks);
		xAfterRepeatBlocks(xAfterRepeatBlocks);
		yAfterRepeatBlocks(yAfterRepeatBlocks);
	}
	
	/**
	 * creates a new {@link PatGridLayout} with the given blocks and repeat blocks
	 * <p>
	 * if a argument contains a colon {@code ':'} it is separated there:
	 * <ol>
	 * <li>the first part
	 * (<code>{@link String#substring(int,int) substring}(0, {@link String#indexOf(int) colonIndex})</code>) is used to
	 * parse the {@link #xBlocks(BlockInfo[]) x}-/{@link #yBlocks(BlockInfo[]) yBlocks}</li>
	 * <li>the second part
	 * (<code>{@link String#substring(int,int) substring}({@link String#indexOf(int) colonIndex} + 1)</code>) is used to
	 * parse the {@link #xAfterRepeatBlocks(BlockInfo[]) x}-/{@link #yAfterRepeatBlocks(BlockInfo[])
	 * yAfterRepeatBlocks}</li>
	 * </ol>
	 * 
	 * @param xBlocks the {@link #xBlocks()} and {@link #xAfterRepeatBlocks()}s
	 * @param yBlocks the {@link #yBlocks()} and {@link #yAfterRepeatBlocks()}s
	 * 
	 * @see #xBlocks(BlockInfo[])
	 * @see #yBlocks(BlockInfo[])
	 * @see #xAfterRepeatBlocks(BlockInfo[])
	 * @see #yAfterRepeatBlocks(BlockInfo[])
	 * @see BlockInfo#parseArr(String)
	 */
	public PatGridLayout(String xBlocks, String yBlocks) {
		this(0, 0, xBlocks, yBlocks);
	}
	
	/**
	 * creates a new {@link PatGridLayout} with the given empty sizes, blocks and repeat blocks
	 * <p>
	 * if a string argument contains a colon {@code ':'} it is separated there:
	 * <ol>
	 * <li>the first part
	 * (<code>{@link String#substring(int,int) substring}(0, {@link String#indexOf(int) colonIndex})</code>) is used to
	 * parse the {@link #xBlocks(BlockInfo[]) x}-/{@link #yBlocks(BlockInfo[]) yBlocks}</li>
	 * <li>the second part
	 * (<code>{@link String#substring(int,int) substring}({@link String#indexOf(int) colonIndex} + 1)</code>) is used to
	 * parse the {@link #xAfterRepeatBlocks(BlockInfo[]) x}-/{@link #yAfterRepeatBlocks(BlockInfo[])
	 * yAfterRepeatBlocks}</li>
	 * </ol>
	 * 
	 * @param xEmpty  the y empty value
	 * @param yEmpty  the x empty value
	 * @param xBlocks the {@link #xBlocks()} and {@link #xAfterRepeatBlocks()}
	 * @param yBlocks the {@link #yBlocks()} and {@link #yAfterRepeatBlocks()}
	 * 
	 * @see #xEmpty(int)
	 * @see #yEmpty(int)
	 * @see #xBlocks(BlockInfo[])
	 * @see #yBlocks(BlockInfo[])
	 * @see #xAfterRepeatBlocks(BlockInfo[])
	 * @see #yAfterRepeatBlocks(BlockInfo[])
	 * @see BlockInfo#parseArr(String)
	 */
	public PatGridLayout(int xEmpty, int yEmpty, String xBlocks, String yBlocks) {
		this(xEmpty, yEmpty, parseBlocks(xBlocks), parseRepBlocks(xBlocks), parseBlocks(yBlocks),
			parseRepBlocks(yBlocks));
	}
	
	private static BlockInfo[] parseRepBlocks(String blocks) {
		int index = blocks.indexOf(':');
		if ( index == -1 || index == blocks.length() - 1 ) {
			return EMPTY_BLOCK_INFOS;
		}
		return BlockInfo.parseArr(blocks.substring(index + 1));
	}
	
	private static BlockInfo[] parseBlocks(String blocks) {
		int index = blocks.indexOf(':');
		if ( index == 0 ) {
			return EMPTY_BLOCK_INFOS;
		}
		return BlockInfo.parseArr(index == -1 ? blocks : blocks.substring(0, index));
	}
	
	/**
	 * returns the amount pixels which are placed on the x-axis between the blocks
	 * 
	 * @return the amount pixels which are placed on the x-axis between the blocks
	 * 
	 * @see #xEmpty(int)
	 * @see #yEmpty()
	 */
	public int xEmpty() {
		return this.xempty;
	}
	
	/**
	 * sets the amount pixels which are placed on the x-axis between the blocks
	 * 
	 * @param xEmpty the amount pixels which are placed on the x-axis between the blocks
	 * 
	 * @throws IllegalArgumentException if {@code xEmpty} is below {@code 0}
	 * 
	 * @see #xEmpty()
	 * @see #yEmpty(int)
	 */
	public void xEmpty(int xEmpty) throws IllegalArgumentException {
		if ( xEmpty < 0 ) {
			throw new IllegalArgumentException("yEmpty < 0: " + xEmpty);
		}
		this.xempty = xEmpty;
	}
	
	/**
	 * returns the amount pixels which are placed on the y-axis between the blocks
	 * 
	 * @return the amount pixels which are placed on the y-axis between the blocks
	 * 
	 * @see #yEmpty(int)
	 * @see #xEmpty()
	 */
	public int yEmpty() {
		return this.yempty;
	}
	
	/**
	 * sets the amount pixels which are placed on the y-axis between the blocks
	 * 
	 * @param yEmpty the amount pixels which are placed on the y-axis between the blocks
	 * 
	 * @throws IllegalArgumentException if {@code yEmpty} is below {@code 0}
	 * 
	 * @see #yEmpty()
	 * @see #xEmpty(int)
	 */
	public void yEmpty(int yEmpty) {
		if ( yEmpty < 0 ) {
			throw new IllegalArgumentException("yEmpty < 0: " + yEmpty);
		}
		this.yempty = yEmpty;
	}
	
	/**
	 * returns a clone of the first x blocks
	 * 
	 * @return a clone of the first x blocks
	 * 
	 * @see #yBlocks()
	 * @see #xBlocks(BlockInfo[])
	 * @see #xAfterRepeatBlocks()
	 */
	public BlockInfo[] xBlocks() {
		return this.xblocks.clone();
	}
	
	/**
	 * sets the x blocks to a clone of the given array
	 * <p>
	 * the first blocks are specified by the given array<br>
	 * when a block with <code>index >= xBlocks.length</code> is specified the {@link #xAfterRepeatBlocks()} are used
	 * 
	 * @param xBlocks the x blocks
	 * 
	 * @throws NullPointerException if <code>xBlocks</code> or one of its entries is <code>null</code>
	 * 
	 * @see #yBlocks(BlockInfo[])
	 * @see #xBlocks()
	 * @see #xAfterRepeatBlocks(BlockInfo[])
	 */
	public void xBlocks(BlockInfo[] xBlocks) throws NullPointerException {
		BlockInfo[] clone = xBlocks.clone();
		for (BlockInfo i : clone) {
			if ( i == null ) {
				throw new NullPointerException("null entry in xBlocks");
			}
		}
		this.xblocks = clone;
	}
	
	/**
	 * returns a clone of the first y blocks
	 * 
	 * @return a clone of the first y blocks
	 * 
	 * @see #xBlocks()
	 * @see #yBlocks(BlockInfo[])
	 * @see #yAfterRepeatBlocks()
	 */
	public BlockInfo[] yBlocks() {
		return this.yblocks.clone();
	}
	
	/**
	 * sets the y blocks to a clone of the given array
	 * <p>
	 * the first blocks are specified by the given array<br>
	 * when a block with <code>index >= yBlocks.length</code> is specified the {@link #yAfterRepeatBlocks()} are used
	 * 
	 * @param yBlocks the y blocks
	 * 
	 * @throws NullPointerException if <code>yBlocks</code> or one of its entries is <code>null</code>
	 * 
	 * @see #xBlocks(BlockInfo[])
	 * @see #yBlocks()
	 * @see #yAfterRepeatBlocks(BlockInfo[])
	 */
	public void yBlocks(BlockInfo[] yBlocks) {
		BlockInfo[] clone = yBlocks.clone();
		for (BlockInfo i : clone) {
			if ( i == null ) {
				throw new NullPointerException("null entry in yBlocks");
			}
		}
		this.yblocks = clone;
	}
	
	/**
	 * returns a clone of the repeat x blocks
	 * 
	 * @return a clone of the repeat x blocks
	 * 
	 * @see #xBlocks()
	 * @see #xAfterRepeatBlocks(BlockInfo[])
	 * @see #yAfterRepeatBlocks()
	 */
	public BlockInfo[] xAfterRepeatBlocks() {
		return this.xafterblocks.clone();
	}
	
	/**
	 * sets the x repeat blocks to a clone of the given array
	 * <p>
	 * the first blocks are specified by {@link #xBlocks()}<br>
	 * when a block with <code>index >= {@link #xBlocks()}.length</code> is specified the the given array is used used
	 * 
	 * @param xAfterBlocks the x repeat blocks
	 * 
	 * @see #xBlocks(BlockInfo[])
	 * @see #xAfterRepeatBlocks()
	 * @see #yAfterRepeatBlocks(BlockInfo[])
	 */
	public void xAfterRepeatBlocks(BlockInfo[] xAfterBlocks) {
		BlockInfo[] clone = xAfterBlocks.clone();
		for (BlockInfo i : clone) {
			if ( i == null ) {
				throw new NullPointerException("null entry in xAfterRepeatBlocks");
			}
		}
		this.xafterblocks = clone;
	}
	
	/**
	 * returns a clone of the repeat y blocks
	 * 
	 * @return a clone of the repeat y blocks
	 * 
	 * @see #yBlocks()
	 * @see #yAfterRepeatBlocks(BlockInfo[])
	 * @see #xAfterRepeatBlocks()
	 */
	public BlockInfo[] yAfterRepeatBlocks() {
		return this.yafterblocks.clone();
	}
	
	
	/**
	 * sets the y repeat blocks to a clone of the given array
	 * <p>
	 * the first blocks are specified by {@link #yBlocks()}<br>
	 * when a block with <code>index >= {@link #yBlocks()}.length</code> is specified the the given array is used used
	 * 
	 * @param yAfterBlocks the y repeat blocks
	 * 
	 * @see #yBlocks(BlockInfo[])
	 * @see #yAfterRepeatBlocks()
	 * @see #xAfterRepeatBlocks(BlockInfo[])
	 */
	public void yAfterRepeatBlocks(BlockInfo[] yAfterBlocks) {
		BlockInfo[] clone = yAfterBlocks.clone();
		for (BlockInfo i : clone) {
			if ( i == null ) {
				throw new NullPointerException("null entry in yAfterRepeatBlocks");
			}
		}
		this.yafterblocks = clone;
	}
	
	/** {@inheritDoc} */
	@Override
	public void addLayoutComponent(String name, Component comp) {
		this.comps.put(comp, CompInfo.parse(name));
	}
	
	/** {@inheritDoc} */
	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		if ( constraints instanceof String s ) {
			this.comps.put(comp, CompInfo.parse(s));
		} else if ( constraints instanceof CompInfo c ) {
			this.comps.put(comp, c);
		} else {
			throw new IllegalArgumentException("illegal constrains: "
				+ ( constraints == null ? "null" : constraints.getClass() + " : " + constraints ));
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public float getLayoutAlignmentX(Container target) {
		CompInfo inf = this.comps.get(target);
		FillMode mode = inf.widthMode;
		if ( mode == FillMode.FILL_COMPLETLY ) {
			return 0f;
		}
		if ( mode instanceof FillMode.ComplexFillMode fmc && fmc.type == FillMode.FILL_COMPLETLY && fmc.mul == 1f ) {
			return 0f;
		}
		return inf.alignx;
	}
	
	/** {@inheritDoc} */
	@Override
	public float getLayoutAlignmentY(Container target) {
		CompInfo inf = this.comps.get(target);
		FillMode mode = inf.heightMode;
		if ( mode == FillMode.FILL_COMPLETLY ) {
			return 0f;
		}
		if ( mode instanceof FillMode.ComplexFillMode fmc && fmc.type == FillMode.FILL_COMPLETLY && fmc.mul == 1f ) {
			return 0f;
		}
		return inf.aligny;
	}
	
	/** {@inheritDoc} */
	@Override
	public void removeLayoutComponent(Component comp) {
		this.comps.remove(comp);
	}
	
	/** {@inheritDoc} */
	@Override
	public Dimension minimumLayoutSize(Container parent) {
		BlockSize[][] sizes = layoutSizes(parent, LAYOUT_SIZES_MINIMUM);
		return calcSize(sizes);
	}
	
	/** {@inheritDoc} */
	@Override
	public Dimension preferredLayoutSize(Container parent) {
		BlockSize[][] sizes = layoutSizes(parent, LAYOUT_SIZES_PREFERRED);
		return calcSize(sizes);
	}
	
	/** {@inheritDoc} */
	@Override
	public Dimension maximumLayoutSize(Container target) {
		BlockSize[][] sizes = layoutSizes(target, LAYOUT_SIZES_MAXIMUM);
		return calcSize(sizes);
	}
	
	private static final int LAYOUT_SIZES_MINIMUM   = 0x1;
	private static final int LAYOUT_SIZES_PREFERRED = 0x2;
	private static final int LAYOUT_SIZES_MAXIMUM   = 0x4;
	
	private BlockSize[][] layoutSizes(Container parent, int flags) {
		int xBlockCount = -1;
		int yBlockCount = -1;
		for (int i = parent.getComponentCount(); --i >= 0;) {
			CompInfo inf = this.comps.get(parent.getComponent(i));
			if ( inf == null ) {
				throw new IllegalStateException("I have no info about the component " + i + " : "
					+ parent.getComponent(i) + " I know: " + this.comps);
			}
			int x = inf.x + inf.w;
			if ( x > xBlockCount ) {
				xBlockCount = x;
			}
			int y = inf.y + inf.h;
			if ( y > yBlockCount ) {
				yBlockCount = y;
			}
		}
		if ( xBlockCount == -1 ) {
			return null;// NOSONAR
		}
		BlockSize[][] sizes = new BlockSize[yBlockCount][xBlockCount];
		int bitCnt = Integer.bitCount(flags);
		for (BlockSize[] arr : sizes) {
			for (int i = 0; i < arr.length; i++) {
				arr[i] = BlockSize.create(bitCnt);
			}
		}
		for (int i = parent.getComponentCount(); --i >= 0;) {
			Component comp = parent.getComponent(i);
			CompInfo inf = this.comps.get(comp);
			if ( inf.h != 1 || inf.w != 1 ) {
				continue;
			}
			BlockSize bs = sizes[inf.y][inf.x];
			int bsi = 0;
			if ( ( flags & LAYOUT_SIZES_MINIMUM ) != 0 ) {
				Dimension dim = comp.getMinimumSize();
				checkMinThrow(dim);
				bs.max(bsi++, dim);
			}
			if ( ( flags & LAYOUT_SIZES_PREFERRED ) != 0 ) {
				Dimension dim = comp.getPreferredSize();
				checkMinReplace(dim);
				bs.max(bsi++, dim);
			}
			if ( ( flags & LAYOUT_SIZES_MAXIMUM ) != 0 ) {
				Dimension dim = comp.getMaximumSize();
				checkMinReplace(dim);
				bs.max(bsi, dim);
			}
		}
		for (int ci = parent.getComponentCount(); --ci >= 0;) {
			Component comp = parent.getComponent(ci);
			CompInfo inf = this.comps.get(comp);
			if ( inf.h == 1 && inf.w == 1 ) {
				continue;
			}
			int bsi = 0;
			if ( ( flags & LAYOUT_SIZES_MINIMUM ) != 0 ) {
				Dimension dim = comp.getMinimumSize();
				checkMinThrow(dim, inf);
				grow(sizes, inf, bsi++, dim, true);
			}
			if ( ( flags & LAYOUT_SIZES_PREFERRED ) != 0 ) {
				Dimension dim = comp.getPreferredSize();
				checkMinReplace(dim, inf);
				grow(sizes, inf, bsi++, dim, true);
			}
			if ( ( flags & LAYOUT_SIZES_MAXIMUM ) != 0 ) {
				Dimension dim = comp.getMaximumSize();
				checkMinReplace(dim, inf);
				grow(sizes, inf, bsi, dim, true);
			}
		}
		return sizes;
	}
	
	private void checkMinReplace(Dimension dim, CompInfo inf) {
		if ( dim.width <= Integer.MAX_VALUE - this.xempty ) dim.width += this.xempty * inf.w;
		else dim.width = Integer.MAX_VALUE;
		if ( dim.height <= Integer.MAX_VALUE - this.yempty ) dim.height += this.yempty * inf.h;
		else dim.width = Integer.MAX_VALUE;
	}
	
	private void checkMinReplace(Dimension dim) {// same as checkMinReplace(dim, new CompInfo(0,0,1,1))
		if ( dim.width <= Integer.MAX_VALUE - this.xempty ) dim.width += this.xempty;
		else dim.width = Integer.MAX_VALUE;
		if ( dim.height <= Integer.MAX_VALUE - this.yempty ) dim.height += this.yempty;
		else dim.width = Integer.MAX_VALUE;
	}
	
	private void checkMinThrow(Dimension dim, CompInfo inf) {
		if ( dim.width <= MAX_BLOCK_SIZE - this.xempty ) dim.width += this.xempty * inf.w;
		else throw new IllegalStateException("minimum size too large");// NOSONAR
		if ( dim.height <= MAX_BLOCK_SIZE - this.yempty ) dim.height += this.yempty * inf.h;
		else throw new IllegalStateException("minimum size too large");
	}
	
	private void checkMinThrow(Dimension dim) { // ca,e as checkMinThrow(dim, new CompInfo(0,0,1,1))
		if ( dim.width <= MAX_BLOCK_SIZE - this.xempty ) dim.width += this.xempty;
		else throw new IllegalStateException("minimum size too large");
		if ( dim.height <= MAX_BLOCK_SIZE - this.yempty ) dim.height += this.yempty;
		else throw new IllegalStateException("minimum size too large");
	}
	
	private void grow(BlockSize[][] sizes, CompInfo cinf, int bsi, Dimension cdim, boolean grow) {
		// its already a mess this way calculating both wide and height at the same time would just blow the method
		// calcWidth true and false go to a different loop structure (true: y outer, x inner; false: x outer, y inner)
		// thus calculating both is more than just doubling all variables
		// (and changing width/height calculation to use the loop of the other is not easy
		// (it would probably need an array to store information and an after loop to evaluate the array)
		grow(sizes, cinf, bsi, cdim, grow, true);
		grow(sizes, cinf, bsi, cdim, grow, false);
	}
	
	private void grow(BlockSize[][] sizes, CompInfo cinf, int bsi, Dimension cdim, boolean grow, boolean calcWidth) { // NOSONAR
		final int x, y;// NOSONAR
		final int h, w;// NOSONAR
		final int width;
		if ( calcWidth ) {
			x = cinf.x;
			y = cinf.y;
			h = cinf.h;
			w = cinf.w;
			width = cdim.width;
		} else {
			x = cinf.y;
			y = cinf.x;
			h = cinf.w;
			w = cinf.h;
			width = cdim.height;
		}
		for (int yadd = h; --yadd >= 0;) {
			boolean repeat;
			do {
				repeat = false;
				int wsum = 0;
				int yc = y + yadd;
				for (int xadd = w, growPotBool = 0; --xadd >= 0;) {
					int xc = x + xadd;
					BlockSize s = get(sizes, xc, yc, calcWidth);
					int sw = calcWidth ? s.w(bsi) : s.h(bsi);
					wsum += sw == 0 ? empty(calcWidth) : sw;
					if ( growPotBool == 0 ) {
						BlockInfo inf = calcWidth ? xinf(xc) : yinf(yc);
						if ( grow ) {
							int infMax = inf.max;
							if ( infMax == BlockInfo.DYNAMIC || infMax > sw ) {
								growPotBool = 1;// NOSONAR
							}
						} else {
							int infMin = inf.min;
							if ( ( infMin == BlockInfo.DYNAMIC ? 0 : infMin ) < sw ) {
								growPotBool = 1;// NOSONAR
							}
						}
					}
				}
				if ( grow ? wsum >= width : wsum <= width ) {
					continue;
				}
				int[] growPots = new int[w];
				int growPot = 0;
				for (int xadd = w; --xadd >= 0;) {
					int xc = x + xadd;
					BlockInfo inf = calcWidth ? xinf(xc) : yinf(yc);
					if ( grow ) {
						int imax = inf.max;
						if ( imax == BlockInfo.DYNAMIC ) {
							growPot = Integer.MAX_VALUE;
							growPots[xadd] = Integer.MAX_VALUE;
						} else {
							int sw = get(sizes, yc, xc, bsi, calcWidth);
							int curGrowPot = inf.max - sw;
							if ( growPot != Integer.MAX_VALUE ) {
								growPot += curGrowPot;
							}
							growPots[xadd] = curGrowPot;
						}
					} else {
						int imin = inf.min;
						int sw = get(sizes, yc, xc, bsi, calcWidth);
						int curGrowPot = ( sw - ( imin == BlockInfo.DYNAMIC ? 0 : imin ) );
						growPot += curGrowPot;
						growPots[xadd] = curGrowPot;
					}
				}
				int growPotCnt = 0;
				for (int i = 0; i < growPots.length; i++) {
					if ( growPots[i] != 0 ) {
						growPotCnt++;
					}
				}
				if ( growPotCnt != 0 ) {
					// round up and check in loop if enough grow was already done
					// thus repeat only needs to be set, when next iteration has less growPotential
					// also this saves before (width - wsum) < growPotCnt
					// (which would come eventually with rounding down behavior and no perfect fit)
					int div = ( width - wsum + growPotCnt - 1 ) / growPotCnt;
					for (int xadd = w, sum = 0; --xadd >= 0 && sum != width;) {
						int curGrowPot = growPots[xadd];
						if ( curGrowPot == 0 ) continue;
						int curDiff;
						if ( curGrowPot < div ) {
							curDiff = curGrowPot;
							repeat = true;
						} else {
							curDiff = div;
						}
						if ( sum + curDiff > width ) {
							curDiff = width - sum;
						}
						sum += curDiff; // NOSONAR
						int xc = x + xadd;
						add(sizes, bsi, xc, yc, curDiff, calcWidth, grow);
					}
				}
			} while ( repeat );
		}
	}
	
	private int empty(boolean calcWidth) {
		return calcWidth ? this.xempty : this.yempty;
	}
	
	private static void add(BlockSize[][] sizes, int bsi, int xc, int yc, int addVal, boolean calcWidth, boolean grow) {
		if ( calcWidth ) {
			BlockSize bs = sizes[yc][xc];
			int old = bs.w(bsi);
			if ( grow ) {
				bs.w(bsi, old + addVal);
			} else {
				bs.w(bsi, old - addVal);
			}
		} else {
			BlockSize bs = sizes[xc][yc];
			int old = bs.h(bsi);
			if ( grow ) {
				bs.h(bsi, old + addVal);
			} else {
				bs.h(bsi, old - addVal);
			}
		}
	}
	
	private static int get(BlockSize[][] sizes, int yc, int xc, int bsi, boolean calcWidth) {
		if ( calcWidth ) {
			return sizes[yc][xc].w(bsi);
		} else {
			return sizes[xc][yc].h(bsi);
		}
	}
	
	private static BlockSize get(BlockSize[][] sizes, int xc, int yc, boolean calcWidth) {
		if ( calcWidth ) {
			return sizes[yc][xc];
		} else {
			return sizes[xc][yc];
		}
	}
	
	private BlockInfo xinf(int x) {
		if ( this.xblocks.length > x ) {
			return this.xblocks[x];
		}
		return this.xafterblocks[( x - this.xblocks.length ) % this.xafterblocks.length];
	}
	
	private BlockInfo yinf(int y) {
		if ( this.yblocks.length > y ) {
			return this.yblocks[y];
		}
		try {
			return this.yafterblocks[( y - this.yblocks.length ) % this.yafterblocks.length];
		} catch ( @SuppressWarnings( "unused" ) ArithmeticException e ) {
			throw new IndexOutOfBoundsException("there is no y block with the number " + y);
		}
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * does nothing and just returns
	 */
	@Override
	public void invalidateLayout(@SuppressWarnings( "unused" ) Container target) {/**/}
	
	private Dimension calcSize(BlockSize[][] sizes) {
		final int yBlockCount = sizes.length;
		final int xBlockCount = sizes[0].length;
		int[] yminpos = new int[yBlockCount + 1];
		int[] xminpos = new int[xBlockCount + 1];
		for (int y = yBlockCount; --y >= 0;) { // calculate the width/height
			BlockSize[] bsa = sizes[y];
			for (int x = xBlockCount; --x >= 0;) {
				BlockSize bs = bsa[x];
				int val = bs.h(0);
				if ( val > yminpos[y + 1] ) {
					yminpos[y + 1] = val;
				}
				val = bs.w(0);
				if ( val > xminpos[x + 1] ) {
					xminpos[x + 1] = val;
				}
			}
		}
		yminpos[0] = this.yempty;
		xminpos[0] = this.xempty;
		for (int i = 1; i <= yBlockCount; i++) { // convert width/height to positions
			check(yminpos, i, yinf(i - 1), this.yempty);
			yminpos[i] += yminpos[i - 1];
		}
		for (int i = 1; i <= xBlockCount; i++) {
			check(xminpos, i, xinf(i - 1), this.xempty);
			xminpos[i] += xminpos[i - 1];
		}
		return new Dimension(xminpos[xBlockCount], yminpos[yBlockCount]);
	}
	
	/** {@inheritDoc} */
	@Override
	public void layoutContainer(Container parent) {
		BlockSize[][] sizes = layoutSizes(parent, LAYOUT_SIZES_MINIMUM | LAYOUT_SIZES_MAXIMUM);
		if ( sizes == null ) return;
		final int yBlockCount = sizes.length;
		final int xBlockCount = sizes[0].length;
		int[] yminpos = new int[yBlockCount + 1];
		int[] ymaxpos = new int[yBlockCount + 1];
		int[] xminpos = new int[xBlockCount + 1];
		int[] xmaxpos = new int[xBlockCount + 1];
		Arrays.fill(ymaxpos, Integer.MAX_VALUE);
		Arrays.fill(xmaxpos, Integer.MAX_VALUE);
		for (int y = yBlockCount; --y >= 0;) { // calculate the width/height
			BlockSize[] bsa = sizes[y];
			for (int x = xBlockCount; --x >= 0;) {
				BlockSize bs = bsa[x];
				int val = bs.h(0);
				if ( val > yminpos[y + 1] ) {
					yminpos[y + 1] = val;
				}
				val = bs.h(1);
				if ( val > ymaxpos[y + 1] ) {
					ymaxpos[y + 1] = val;
				}
				val = bs.w(0);
				if ( val > xminpos[x + 1] ) {
					xminpos[x + 1] = val;
				}
				val = bs.w(1);
				if ( val > xmaxpos[x + 1] ) {
					xmaxpos[x + 1] = val;
				}
			}
		}
		for (int x = xBlockCount; --x >= 0;) {
			BlockInfo inf = xinf(x);
			if ( xminpos[x + 1] < inf.min ) {
				xminpos[x + 1] = inf.min;
			}
			if ( xmaxpos[x + 1] > inf.max ) {
				xmaxpos[x + 1] = inf.max;
			}
		}
		for (int y = yBlockCount; --y >= 0;) {
			BlockInfo inf = yinf(y);
			if ( yminpos[y + 1] < inf.min ) {
				yminpos[y + 1] = inf.min;
			}
			if ( ymaxpos[y + 1] > inf.max ) {
				ymaxpos[y + 1] = inf.max;
			}
		}
		yminpos[0] = ymaxpos[0] = this.yempty;
		xminpos[0] = xmaxpos[0] = this.xempty;
		if ( doLogging() ) {
			log("sizes:", //
				"  ymin: " + Arrays.toString(yminpos), //
				"  ymax: " + Arrays.toString(ymaxpos), //
				"  xmin: " + Arrays.toString(xminpos), //
				"  xmax: " + Arrays.toString(xmaxpos));
		}
		for (int i = 1; i <= yBlockCount; i++) { // convert width/height to positions
			BlockInfo inf = yinf(i - 1);
			check(yminpos, i, inf, this.yempty);
			check(ymaxpos, i, inf, this.yempty);
			yminpos[i] += yminpos[i - 1];// + yEmpty is not needed, because layoutSizes already adds empty
			ymaxpos[i] += ymaxpos[i - 1];
		}
		for (int i = 1; i <= xBlockCount; i++) {
			BlockInfo inf = xinf(i - 1);
			check(xminpos, i, inf, this.xempty);
			check(xmaxpos, i, inf, this.xempty);
			xminpos[i] += xminpos[i - 1];
			xmaxpos[i] += xmaxpos[i - 1];
		}
		if ( doLogging() ) {
			log("positions:", //
				"  ymin: " + Arrays.toString(yminpos), //
				"  ymax: " + Arrays.toString(ymaxpos), //
				"  xmin: " + Arrays.toString(xminpos), //
				"  xmax: " + Arrays.toString(xmaxpos));
		}
		int totalHeight = parent.getHeight();
		adjust(yminpos, ymaxpos, totalHeight);
		int totalWidth = parent.getWidth();
		adjust(xminpos, xmaxpos, totalWidth);
		for (int i = parent.getComponentCount(); --i >= 0;) {
			Component comp = parent.getComponent(i);
			CompInfo inf = this.comps.get(comp);
			int xb = inf.x;
			int yb = inf.y;
			int wb = inf.w;
			int hb = inf.h;
			int ymin = yminpos[yb];
			int ymax = yminpos[yb + hb] - this.yempty;
			int xmin = xminpos[xb];
			int xmax = xminpos[xb + wb] - this.xempty;
			int maxHeight = ymax - ymin;
			int maxWidth = xmax - xmin;
			int h = inf.heightMode.size(comp, inf, maxWidth, maxHeight, false);
			int w = inf.widthMode.size(comp, inf, maxWidth, maxHeight, true);
			int ypos, xpos; // NOSONAR
			int width, height; // NOSONAR
			height = size(h, maxHeight);
			width = size(w, maxWidth);
			ypos = calcPos(maxHeight, height, ymin, inf.aligny);
			xpos = calcPos(maxWidth, width, xmin, inf.alignx);
			comp.setBounds(xpos, ypos, width, height);
			if ( doLogging() ) {
				log("comp: " + comp, //
					"  bounds:  x=" + xpos + " y=" + ypos + " w=" + width + " h=" + height, //
					"  MBounds: x=" + xmin + " y=" + ymin + " w=" + maxWidth + " h=" + maxHeight, //
					"  wanted size: w=" + w + " h=" + h, //
					"  wideMode: " + inf.widthMode, //
					"  heightMode: " + inf.heightMode);
			}
		}
	}
	
	private static void check(int[] arr, int i, BlockInfo inf, int empty) {
		if ( arr[i] > inf.max + empty ) {
			arr[i] = inf.max + empty;
		} else if ( arr[i] < inf.min + empty ) {
			arr[i] = inf.min + empty;
		}
	}
	
	private static int calcPos(int space, int usedSpace, int basePosition, float align) {
		if ( space == usedSpace || align == 0f ) return basePosition;
		int free = space - usedSpace;
		free *= align;
		return basePosition + free;
	}
	
	private static int size(int wanted, int maxHeight) {
		if ( wanted < 0 ) throw new IllegalArgumentException("wanted size is negative: " + wanted);
		if ( wanted < maxHeight ) return wanted;
		return maxHeight;
	}
	
	private static void adjust(int[] yminpos, int[] ymaxpos, int totalHeight) {
		final int yBlockCount = yminpos.length - 1;
		while ( true ) {
			if ( totalHeight <= yminpos[yBlockCount] ) return;
			int diff = 0;
			for (int y = 1; y <= yBlockCount; y++) {
				int ymin = yminpos[y] - yminpos[y - 1];
				int ymax = ymaxpos[y] - ymaxpos[y - 1];
				if ( ymin < ymax ) {
					diff += ymax - ymin;
				}
			}
			if ( diff == 0 ) return;
			if ( yminpos[yBlockCount] + diff <= totalHeight ) {
				for (int y = 1, add = 0; y <= yBlockCount; y++) {
					int ymin = yminpos[y] - yminpos[y - 1];
					int ymax = ymaxpos[y] - ymaxpos[y - 1];
					if ( ymin < ymax ) {
						add += ymax - ymin;// NOSONAR
					}
					yminpos[y] += add;
				}
				return;
			} else {
				int growPot = totalHeight - yminpos[yBlockCount];
				int growPoitCnt = 0;
				int maxGrow = Integer.MAX_VALUE;
				for (int y = 1; y <= yBlockCount; y++) {
					int ymin = yminpos[y] - yminpos[y - 1];
					int ymax = ymaxpos[y] - ymaxpos[y - 1];
					if ( ymin < ymax ) {
						growPoitCnt++;
						maxGrow = Math.min(maxGrow, ymax - ymin);
					}
				}
				int grow = Math.max(growPot / growPoitCnt, 1);
				for (int y = 1, add = 0; y <= yBlockCount; y++) {
					int ymin = yminpos[y] - yminpos[y - 1];
					int ymax = ymaxpos[y] - ymaxpos[y - 1];
					if ( ymin < ymax ) {
						add += Math.min(ymax - ymin, grow);// NOSONAR
					}
					yminpos[y] += add;
				}
				if ( grow <= maxGrow ) {
					return;
				}
			}
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.comps.hashCode();
		result = prime * result + Arrays.hashCode(this.xafterblocks);
		result = prime * result + Arrays.hashCode(this.xblocks);
		result = prime * result + this.xempty;
		result = prime * result + Arrays.hashCode(this.yafterblocks);
		result = prime * result + Arrays.hashCode(this.yblocks);
		result = prime * result + this.yempty;
		return result;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) { return true; }
		if ( !( obj instanceof PatGridLayout ) ) { return false; }
		PatGridLayout other = (PatGridLayout) obj;
		if ( !this.comps.equals(other.comps) ) { return false; }
		if ( !Arrays.equals(this.xafterblocks, other.xafterblocks) ) { return false; }
		if ( !Arrays.equals(this.xblocks, other.xblocks) ) { return false; }
		if ( this.xempty != other.xempty ) { return false; }
		if ( !Arrays.equals(this.yafterblocks, other.yafterblocks) ) { return false; }
		if ( !Arrays.equals(this.yblocks, other.yblocks) ) { return false; }
		return this.yempty == other.yempty;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PatGridLayout [xempty=");
		builder.append(this.xempty);
		builder.append(", yempty=");
		builder.append(this.yempty);
		builder.append(", xblocks=");
		builder.append(Arrays.toString(this.xblocks));
		builder.append(", yblocks=");
		builder.append(Arrays.toString(this.yblocks));
		builder.append(", xafterblocks=");
		builder.append(Arrays.toString(this.xafterblocks));
		builder.append(", yafterblocks=");
		builder.append(Arrays.toString(this.yafterblocks));
		return builder.append(']').toString();
	}
	
	private static boolean doLogging() {
		return Logger.getLogger(LOGGER).isLoggable(LOG_LEVEL);
	}
	
	private static void log(String... lines) {
		StringJoiner sj = new StringJoiner(System.lineSeparator());
		for (String line : lines) {
			sj.add(line);
		}
		Logger.getLogger(LOGGER).log(LOG_LEVEL, sj.toString()); // NOSONAR
	}
	
	// just for testing
	
	@SuppressWarnings( "javadoc" )
	public static void main(String[] args) throws InterruptedException {
		Logger logger = Logger.getLogger(LOGGER);
		for (Logger l = logger; l != null; l = l.getParent()) {
			l.setLevel(LOG_LEVEL);
			for (Handler h : l.getHandlers()) {
				h.setLevel(LOG_LEVEL);
			}
		}
		SwingUtilities.invokeLater(() -> showDialog(new PatGridLayout(0, 0, ":[100]", ":[100,grow]")));
		while ( true ) {// NOSONAR
			Thread.sleep(Long.MAX_VALUE);
			logger.setLevel(LOG_LEVEL);// NOSONAR
		}
	}
	
	private static void showDialog(PatGridLayout layout) {
		JFrame frame = new JFrame();
		frame.setTitle("test dialog");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setLayout(layout);
		frame.add(new JLabel("enter some text:"), parseCI("0 0, yalign=bottom"));
		frame.add(new JTextField(12), parseCI("1 0"));
		frame.add(new JButton("OK"), parseCI("0 2 2 1"));
		frame.setLocationByPlatform(true);
		frame.pack();
		System.out.println("min:  " + frame.getContentPane().getMinimumSize());
		System.out.println("pref: " + frame.getContentPane().getPreferredSize());
		System.out.println("max:  " + frame.getContentPane().getMaximumSize());
		System.out.println("size: " + frame.getContentPane().getSize());
		frame.addComponentListener(new ComponentAdapter() {
			
			@Override
			public void componentResized(@SuppressWarnings( "unused" ) ComponentEvent e) {
				System.out.println("size: " + frame.getContentPane().getSize());
			}
			
		});
		frame.setVisible(true);
	}
	
	private static Object parseCI(String str) {
		CompInfo ci = CompInfo.parse(str);
		System.out.println(str + " -> " + ci);
		return ci;// when returning str, CompInfo.parse(str) will be called by the LayoutManager
	}
	
}
