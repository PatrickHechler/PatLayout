package de.hechler.patrick.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import de.hechler.patrick.layout.FillMode.SimpleFillMode;

public class PatGridLayout implements LayoutManager2 {
	
	private static final int MAX_SIZE = Integer.MAX_VALUE >>> 7; // allow 128 times MAX_SIZE, then overflow
	
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
		xblocks = EMPTY_BLOCK_INFOS;
		yblocks = EMPTY_BLOCK_INFOS;
		xafterblocks = new BlockInfo[]{ new BlockInfo(BlockInfo.DYNAMIC, BlockInfo.DYNAMIC) };
		yafterblocks = new BlockInfo[]{ new BlockInfo(BlockInfo.DYNAMIC, BlockInfo.DYNAMIC) };
		// empty defaults to 0
	}
	
	public PatGridLayout(int xempty, int yempty) {
		this();
		xEmpty(xempty);
		yEmpty(yempty);
	}
	
	/**
	 * this constructor just delegates to
	 * <code>{@link #PatGridLayout(int, int, BlockInfo[], BlockInfo[], BlockInfo[], BlockInfo[]) PatGridLayout}(xEmpty, yEmpty, xBlocks, {@link #EMPTY_BLOCK_INFOS}, yBlocks, {@link #EMPTY_BLOCK_INFOS})</code>
	 * 
	 * @param xEmpty  the empty x space between blocks
	 * @param yEmpty  the empty y space between blocks
	 * @param xBlocks the x blocks
	 * @param yBlocks the y blocks
	 */
	public PatGridLayout(int xEmpty, int yEmpty, BlockInfo[] xBlocks, BlockInfo[] yBlocks) {
		this(xEmpty, yEmpty, xBlocks, EMPTY_BLOCK_INFOS, yBlocks, EMPTY_BLOCK_INFOS);
	}
	
	public PatGridLayout(int xempty, int yempty, BlockInfo[] xblocks, BlockInfo[] xafterblocks, BlockInfo[] yblocks,
		BlockInfo[] yafterblocks) {
		if ( this.getClass() != PatGridLayout.class ) {
			// its a valid state, but can't be used for anything
			xblocks = EMPTY_BLOCK_INFOS;
			yblocks = EMPTY_BLOCK_INFOS;
			xafterblocks = EMPTY_BLOCK_INFOS;
			yafterblocks = EMPTY_BLOCK_INFOS;
		}
		xEmpty(xempty);
		yEmpty(yempty);
		xBlocks(xblocks);
		yBlocks(yblocks);
		xAfterRepeatBlocks(xafterblocks);
		yAfterRepeatBlocks(yafterblocks);
	}
	
	public PatGridLayout(int xempty, int yempty, String xblocks, String yblocks) {
		this(xempty, yempty, parseBlocks(xblocks), parseRepBlocks(xblocks), parseBlocks(yblocks),
			parseRepBlocks(yblocks));
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
	
	public int xEmpty() {
		return xempty;
	}
	
	public void xEmpty(int xEmpty) {
		if ( xEmpty < 0 ) {
			throw new IllegalArgumentException("yEmpty < 0: " + xEmpty);
		}
		this.xempty = xEmpty;
	}
	
	public int yEmpty() {
		return yempty;
	}
	
	public void yEmpty(int yEmpty) {
		if ( yEmpty < 0 ) {
			throw new IllegalArgumentException("yEmpty < 0: " + yEmpty);
		}
		this.yempty = yEmpty;
	}
	
	public BlockInfo[] xBlocks() {
		return xblocks.clone();
	}
	
	public void xBlocks(BlockInfo[] xblocks) {
		BlockInfo[] clone = xblocks.clone();
		for (BlockInfo i : clone) {
			if ( i == null ) {
				throw new NullPointerException("null entry in xBlocks");
			}
		}
		this.xblocks = clone;
	}
	
	public BlockInfo[] yBlocks() {
		return yblocks.clone();
	}
	
	public void yBlocks(BlockInfo[] yblocks) {
		BlockInfo[] clone = yblocks.clone();
		for (BlockInfo i : clone) {
			if ( i == null ) {
				throw new NullPointerException("null entry in yBlocks");
			}
		}
		this.yblocks = clone;
	}
	
	public BlockInfo[] xAfterRepeatBlocks() {
		return xafterblocks.clone();
	}
	
	public void xAfterRepeatBlocks(BlockInfo[] xafterblocks) {
		BlockInfo[] clone = xafterblocks.clone();
		for (BlockInfo i : clone) {
			if ( i == null ) {
				throw new NullPointerException("null entry in xAfterRepeatBlocks");
			}
		}
		this.xafterblocks = clone;
	}
	
	public BlockInfo[] yAfterRepeatBlocks() {
		return yafterblocks.clone();
	}
	
	public void yAfterRepeatBlocks(BlockInfo[] yafterblocks) {
		BlockInfo[] clone = yafterblocks.clone();
		for (BlockInfo i : clone) {
			if ( i == null ) {
				throw new NullPointerException("null entry in yAfterRepeatBlocks");
			}
		}
		this.yafterblocks = clone;
	}
	
	@Override
	public void addLayoutComponent(String name, Component comp) {
		this.comps.put(comp, CompInfo.parse(name));
	}
	
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
	
	@Override
	public float getLayoutAlignmentX(Container target) {
		CompInfo inf = this.comps.get(target);
		FillMode mode = inf.wideMode;
		if ( mode == FillMode.FILL_COMPLETLY ) {
			return 0f;
		}
		if ( mode instanceof FillMode.ComplexFillMode fmc ) {
			if ( fmc.type == FillMode.FILL_COMPLETLY && fmc.mul == 1f ) {
				return 0f;
			}
		}
		return inf.alignx;
	}
	
	@Override
	public float getLayoutAlignmentY(Container target) {
		CompInfo inf = this.comps.get(target);
		FillMode mode = inf.heightMode;
		if ( mode == FillMode.FILL_COMPLETLY ) {
			return 0f;
		}
		if ( mode instanceof FillMode.ComplexFillMode fmc ) {
			if ( fmc.type == FillMode.FILL_COMPLETLY && fmc.mul == 1f ) {
				return 0f;
			}
		}
		return inf.aligny;
	}
	
	@Override
	public void removeLayoutComponent(Component comp) {
		this.comps.remove(comp);
	}
	
	@Override
	public Dimension minimumLayoutSize(Container parent) {
		BlockSize[][] sizes = layoutSizes(parent, LAYOUT_SIZES_MINIMUM);
		return calcSize(sizes, false);
	}
	
	@Override
	public Dimension preferredLayoutSize(Container parent) {
		BlockSize[][] sizes = layoutSizes(parent, LAYOUT_SIZES_PREFERRED);
		return calcSize(sizes, false);
	}
	
	@Override
	public Dimension maximumLayoutSize(Container target) {
		BlockSize[][] sizes = layoutSizes(target, LAYOUT_SIZES_MAXIMUM);
		return calcSize(sizes, true);
	}
	
	private static final int LAYOUT_SIZES_MINIMUM   = 0x1;
	private static final int LAYOUT_SIZES_PREFERRED = 0x2;
	private static final int LAYOUT_SIZES_MAXIMUM   = 0x4;
	
	private BlockSize[][] layoutSizes(Container parent, int flags) {
		int xBlocks = -1;
		int yBlocks = -1;
		for (int i = parent.getComponentCount(); --i >= 0;) {
			CompInfo inf = comps.get(parent.getComponent(i));
			if ( inf == null ) {
				throw new IllegalStateException(
					"I have no info about the component " + i + " : " + parent.getComponent(i) + " I know: " + comps);
			}
			int x = inf.x + inf.w;
			if ( x > xBlocks ) {
				xBlocks = x;
			}
			int y = inf.y + inf.h;
			if ( y > yBlocks ) {
				yBlocks = y;
			}
		}
		if ( xBlocks == -1 ) {
			return null;
		}
		BlockSize[][] sizes = new BlockSize[yBlocks][xBlocks];
		int bitCnt = Integer.bitCount(flags);
		for (BlockSize[] bs : sizes) {
			for (int i = 0; i < bs.length; i++) {
				bs[i] = BlockSize.create(bitCnt);
			}
		}
		for (int i = parent.getComponentCount(); --i >= 0;) {
			Component comp = parent.getComponent(i);
			CompInfo inf = comps.get(comp);
			if ( inf.h != 1 || inf.w != 1 ) {
				continue;
			}
			BlockSize bs = sizes[inf.y][inf.x];
			int bsi = 0;
			if ( ( flags & LAYOUT_SIZES_MINIMUM ) != 0 ) {
				Dimension dim = comp.getMinimumSize();
				if ( dim.width <= Integer.MAX_VALUE - xempty ) dim.width += xempty;
				else throw new IllegalStateException("minimum size too large");
				if ( dim.height <= Integer.MAX_VALUE - yempty ) dim.height += yempty;
				else throw new IllegalStateException("minimum size too large");
				bs.max(bsi++, dim);
			}
			if ( ( flags & LAYOUT_SIZES_PREFERRED ) != 0 ) {
				Dimension dim = comp.getPreferredSize();
				if ( dim.width <= Integer.MAX_VALUE - xempty ) dim.width += xempty;
				else dim.width = Integer.MAX_VALUE;
				if ( dim.height <= Integer.MAX_VALUE - yempty ) dim.height += yempty;
				else dim.width = Integer.MAX_VALUE;
				bs.max(bsi++, dim);
			}
			if ( ( flags & LAYOUT_SIZES_MAXIMUM ) != 0 ) {
				Dimension dim = comp.getMaximumSize();
				if ( dim.width <= Integer.MAX_VALUE - xempty ) dim.width += xempty;
				else dim.width = Integer.MAX_VALUE;
				if ( dim.height <= Integer.MAX_VALUE - yempty ) dim.height += yempty;
				else dim.width = Integer.MAX_VALUE;
				bs.min(bsi, dim);
			}
		}
		for (int ci = parent.getComponentCount(); --ci >= 0;) {
			Component comp = parent.getComponent(ci);
			CompInfo inf = comps.get(comp);
			if ( inf.h == 1 && inf.w == 1 ) {
				continue;
			}
			int bsi = 0;
			if ( ( flags & LAYOUT_SIZES_MINIMUM ) != 0 ) {
				Dimension dim = comp.getMinimumSize();
				if ( dim.width <= Integer.MAX_VALUE - xempty ) dim.width += xempty;
				else throw new IllegalStateException("minimum size too large");
				if ( dim.height <= Integer.MAX_VALUE - yempty ) dim.height += yempty;
				else throw new IllegalStateException("minimum size too large");
				grow(sizes, inf, bsi++, dim, true);
			}
			if ( ( flags & LAYOUT_SIZES_PREFERRED ) != 0 ) {
				Dimension dim = comp.getPreferredSize();
				if ( dim.width <= Integer.MAX_VALUE - xempty ) dim.width += xempty;
				else dim.width = Integer.MAX_VALUE;
				if ( dim.height <= Integer.MAX_VALUE - yempty ) dim.height += yempty;
				else dim.width = Integer.MAX_VALUE;
				grow(sizes, inf, bsi++, dim, true);
			}
			if ( ( flags & LAYOUT_SIZES_MAXIMUM ) != 0 ) {
				Dimension dim = comp.getMaximumSize();
				if ( dim.width <= Integer.MAX_VALUE - xempty ) dim.width += xempty;
				else dim.width = Integer.MAX_VALUE;
				if ( dim.height <= Integer.MAX_VALUE - yempty ) dim.height += yempty;
				else dim.width = Integer.MAX_VALUE;
				grow(sizes, inf, bsi, dim, false);
			}
		}
		return sizes;
	}
	
	private void grow(BlockSize[][] sizes, CompInfo cinf, int bsi, Dimension cdim, boolean grow) {
		// its already a mess this way
		// calculating both wide and height at the same time would just blow the method
		grow(sizes, cinf, bsi, cdim, grow, true);
		grow(sizes, cinf, bsi, cdim, grow, false);
	}
	
	private void grow(BlockSize[][] sizes, CompInfo cinf, int bsi, Dimension cdim, boolean grow, boolean calcWidth) {
		final int x, y, h, w, width;
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
			boolean condition;
			do {
				condition = false;
				int wsum = 0;
				boolean overwriteNoContinue = false;
				int yc = y + yadd;
				for (int xadd = w, growPotBool = 0; --xadd >= 0;) {
					int xc = x + xadd;
					BlockSize s = get(sizes, xc, yc, calcWidth);
					int sw = calcWidth ? s.w(bsi) : s.h(bsi);
					if ( sw != -1 ) wsum += sw;
					else overwriteNoContinue = true;
					if ( growPotBool == 0 ) {
						BlockInfo inf = calcWidth ? xinf(xc) : yinf(yc);
						if ( grow ) {
							int infMax = inf.max;
							if ( infMax == BlockInfo.DYNAMIC || infMax < sw || sw == -1 ) {
								growPotBool = 1;
							}
						} else {
							int infMin = inf.min;
							if ( ( infMin == BlockInfo.DYNAMIC ? 0 : infMin ) < sw || sw == -1 ) {
								growPotBool = 1;
							}
						}
					}
				}
				if ( !overwriteNoContinue && ( grow ? wsum <= width : wsum >= width ) ) {
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
							int curGrowPot = sw == -1 ? inf.max : inf.max - sw;
							if ( growPot != Integer.MAX_VALUE ) {
								growPot += curGrowPot;
							}
							growPots[xadd] = curGrowPot;
						}
					} else {
						int imin = inf.min;
						int sw = get(sizes, yc, xc, bsi, calcWidth);
						int curGrowPot = sw != -1 ? sw - ( imin == BlockInfo.DYNAMIC ? 0 : imin )
							: imin == BlockInfo.DYNAMIC ? 0 : imin;
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
				if ( growPotCnt != 0 ) { // round up
					int div = ( width + growPotCnt - 1 ) / growPotCnt;
					int sum = 0;
					for (int xadd = w; --xadd >= 0;) {
						int curGrowPot = growPots[xadd];
						if ( curGrowPot == 0 ) continue;
						int curDiff;
						if ( curGrowPot < div ) {
							curDiff = curGrowPot;
							condition = true;
						} else {
							curDiff = div;
						}
						if ( sum + curDiff > width ) {
							curDiff = width - sum;
						}
						sum += curDiff;
						int xc = x + xadd;
						add(sizes, bsi, xc, yc, curDiff, calcWidth, grow);
					}
				}
			} while ( condition );
		}
	}
	
	private static void add(BlockSize[][] sizes, int bsi, int xc, int yc, int addVal, boolean calcWidth, boolean grow) {
		if ( calcWidth ) {
			BlockSize bs = sizes[yc][xc];
			int old = bs.w(bsi);
			if ( old == -1 ) {
				bs.w(bsi, addVal);
			} else if ( grow ) {
				bs.w(bsi, old + addVal);
			} else {
				bs.w(bsi, old - addVal);
			}
		} else {
			BlockSize bs = sizes[xc][yc];
			int old = bs.h(bsi);
			if ( old == -1 ) {
				bs.h(bsi, addVal);
			} else if ( grow ) {
				bs.h(bsi, old + addVal);
			} else {
				bs.h(bsi, old - addVal);
			}
		}
	}
	
	private int get(BlockSize[][] sizes, int yc, int xc, int bsi, boolean calcWidth) {
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
		if ( xblocks.length > x ) {
			return xblocks[x];
		}
		return xafterblocks[( x - xblocks.length ) % xafterblocks.length];
	}
	
	private BlockInfo yinf(int y) {
		if ( yblocks.length > y ) {
			return yblocks[y];
		}
		return yafterblocks[( y - yblocks.length ) % yafterblocks.length];
	}
	
	@Override
	public void invalidateLayout(Container target) {}
	
	private Dimension calcSize(BlockSize[][] sizes, boolean max) {
		final int yBlockCount = sizes.length;
		final int xBlockCount = sizes[0].length;
		int[] yminpos = new int[yBlockCount + 1];
		int[] xminpos = new int[xBlockCount + 1];
		if ( max ) {
			Arrays.fill(yminpos, Integer.MAX_VALUE);
			Arrays.fill(xminpos, Integer.MAX_VALUE);
		}
		for (int y = yBlockCount; --y >= 0;) { // calculate the width/height
			BlockSize[] bsa = sizes[y];
			for (int x = xBlockCount; --x >= 0;) {
				BlockSize bs = bsa[x];
				int val = bs.h(0);
				if ( max ? val < yminpos[y + 1] : val > yminpos[y + 1] ) {
					yminpos[y + 1] = val;
				}
				val = bs.w(0);
				if ( max ? val < xminpos[x + 1] : val > xminpos[x + 1] ) {
					xminpos[x + 1] = val;
				}
			}
		}
		yminpos[0] = yempty;
		xminpos[0] = xempty;
		for (int y = 1; y <= yBlockCount; y++) { // convert width/height to positions
			check(yminpos, y, yempty);
			yminpos[y] += yminpos[y - 1];
		}
		for (int x = 1; x <= xBlockCount; x++) {
			check(xminpos, x, xempty);
			xminpos[x] += xminpos[x - 1];
		}
		return new Dimension(xminpos[xBlockCount], yminpos[yBlockCount]);
	}
	
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
				if ( val != -1 && val < ymaxpos[y + 1] ) {
					ymaxpos[y + 1] = val;
				}
				val = bs.w(0);
				if ( val > xminpos[x + 1] ) {
					xminpos[x + 1] = val;
				}
				val = bs.w(1);
				if ( val != -1 && val < xmaxpos[x + 1] ) {
					xmaxpos[x + 1] = val;
				}
			}
		}
		yminpos[0] = ymaxpos[0] = yempty;
		xminpos[0] = xmaxpos[0] = xempty;
		if ( doLogging() ) {
			log("sizes:", //
				"  ymin: " + Arrays.toString(yminpos), //
				"  ymax: " + Arrays.toString(ymaxpos), //
				"  xmin: " + Arrays.toString(xminpos), //
				"  xmax: " + Arrays.toString(xmaxpos));
		}
		for (int y = 1; y <= yBlockCount; y++) { // convert width/height to positions
			check(yminpos, y, xempty);
			check(ymaxpos, y, xempty);
			yminpos[y] += yminpos[y - 1];// + yEmpty is not needed, because layoutSizes already adds empty
			ymaxpos[y] += ymaxpos[y - 1];
		}
		for (int x = 1; x <= xBlockCount; x++) {
			check(xminpos, x, xempty);
			check(xmaxpos, x, xempty);
			xminpos[x] += xminpos[x - 1];
			xmaxpos[x] += xmaxpos[x - 1];
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
			CompInfo inf = comps.get(comp);
			int xb = inf.x;
			int yb = inf.y;
			int wb = inf.w;
			int hb = inf.h;
			int ymin = yminpos[yb];
			int ymax = yminpos[yb + hb] - yempty;
			int xmin = xminpos[xb];
			int xmax = xminpos[xb + wb] - xempty;
			Dimension dim = prefDim(inf, comp);
			int maxHeight = ymax - ymin;
			int maxWidth = xmax - xmin;
			int ypos, xpos, width, height;
			height = calcSize(dim, maxHeight, inf, true);
			width = calcSize(dim, maxWidth, inf, false);
			ypos = calcPos(maxHeight, height, ymin, inf.aligny);
			xpos = calcPos(maxWidth, width, xmin, inf.alignx);
			comp.setBounds(xpos, ypos, width, height);
			if ( doLogging() ) {
				log("comp: " + comp, //
					"  bounds:  x=" + xpos + " y=" + ypos + " w=" + width + " h=" + height, //
					"  MBounds: x=" + xmin + " y=" + ymin + " w=" + maxWidth + " h=" + maxHeight, //
					"  dimension: " + dim, //
					"  wideMode: " + inf.wideMode, //
					"  heightMode: " + inf.heightMode);
			}
		}
	}
	
	private static void check(int[] arr, int i, int empty) {
		if ( arr[i] > MAX_SIZE ) {
			arr[i] = MAX_SIZE;
		} else if ( arr[i] < empty ) {
			assert arr[i] == 0;
			arr[i] = empty;
		}
	}
	
	private static int calcPos(int space, int usedSpace, int basePosition, float align) {
		if ( space == usedSpace || align == 0f ) return basePosition;
		int free = space - usedSpace;
		free *= align;
		return basePosition + free;
	}
	
	private static int calcSize(Dimension dim, int maxHeight, CompInfo inf, boolean heigth) {
		if ( dim == null || ( heigth ? dim.height : dim.width ) == -1 ) {
			int height;
			height = maxHeight;
			if ( ( heigth ? inf.heightMode : inf.wideMode ) instanceof FillMode.ComplexFillMode c ) {
				height *= c.mul;
			}
			return height;
		} else if ( maxHeight <= ( heigth ? dim.height : dim.width ) ) {
			return maxHeight;
		}
		return ( heigth ? dim.height : dim.width );
	}
	
	private static Dimension prefDim(CompInfo inf, Component comp) {
		SimpleFillMode hmt = inf.heightMode.type();
		Dimension dim = switch ( hmt ) {
		case FILL_COMPLETLY -> null;
		case FILL_MAXIMUM -> comp.getMaximumSize();
		case FILL_MINIMUM -> comp.getMinimumSize();
		case FILL_PREFERRED -> comp.getPreferredSize();
		};
		if ( dim != null && inf.heightMode instanceof FillMode.ComplexFillMode fmc ) {
			dim.height *= fmc.mul;
		}
		SimpleFillMode wmt = inf.wideMode.type();
		if ( hmt != wmt ) {
			Dimension dim2 = switch ( hmt ) {
			case FILL_COMPLETLY -> null;
			case FILL_MAXIMUM -> comp.getMaximumSize();
			case FILL_MINIMUM -> comp.getMinimumSize();
			case FILL_PREFERRED -> comp.getPreferredSize();
			};
			if ( dim == null ) {
				dim = dim2;
				dim.height = -1;
			} else if ( dim2 == null ) {
				dim.width = -1;
			} else {
				dim.width = dim2.width;
			}
		}
		if ( dim != null && dim.width != -1 && inf.wideMode instanceof FillMode.ComplexFillMode fmc ) {
			dim.width *= fmc.mul;
		}
		return dim;
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
						add += ymax - ymin;
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
						add += Math.min(ymax - ymin, grow);
					}
					yminpos[y] += add;
				}
				if ( grow <= maxGrow ) {
					return;
				}
			}
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( comps == null ) ? 0 : comps.hashCode() );
		result = prime * result + Arrays.hashCode(xafterblocks);
		result = prime * result + Arrays.hashCode(xblocks);
		result = prime * result + xempty;
		result = prime * result + Arrays.hashCode(yafterblocks);
		result = prime * result + Arrays.hashCode(yblocks);
		result = prime * result + yempty;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) { return true; }
		if ( !( obj instanceof PatGridLayout ) ) { return false; }
		PatGridLayout other = (PatGridLayout) obj;
		if ( comps == null ) {
			if ( other.comps != null ) { return false; }
		} else if ( !comps.equals(other.comps) ) { return false; }
		if ( !Arrays.equals(xafterblocks, other.xafterblocks) ) { return false; }
		if ( !Arrays.equals(xblocks, other.xblocks) ) { return false; }
		if ( xempty != other.xempty ) { return false; }
		if ( !Arrays.equals(yafterblocks, other.yafterblocks) ) { return false; }
		if ( !Arrays.equals(yblocks, other.yblocks) ) { return false; }
		if ( yempty != other.yempty ) { return false; }
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PatGridLayout [xempty=");
		builder.append(xempty);
		builder.append(", yempty=");
		builder.append(yempty);
		builder.append(", xblocks=");
		builder.append(Arrays.toString(xblocks));
		builder.append(", yblocks=");
		builder.append(Arrays.toString(yblocks));
		builder.append(", xafterblocks=");
		builder.append(Arrays.toString(xafterblocks));
		builder.append(", yafterblocks=");
		builder.append(Arrays.toString(yafterblocks));
		builder.append("]");
		return builder.toString();
	}
	
	public static void main(String[] args) {
		Logger logger = Logger.getLogger("pat.layout");
		logger.setLevel(Level.ALL);
		SwingUtilities.invokeLater(() -> {
			showDialog(new PatGridLayout(5, 5));
			showDialog(new PatGridLayout(0, 0));
		});
	}
	
	private static void showDialog(PatGridLayout layout) {
		JDialog dialog = new JDialog();
		dialog.setTitle("test dialog");
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setLayout(layout);
		dialog.add("0 0", new JLabel("enter some text:"));
		dialog.add("1 0", new JTextField(12));
		dialog.add("0 2 2 1", new JButton("OK"));
		dialog.setLocationRelativeTo(null);
		dialog.pack();
		System.out.println("min:  " + dialog.getMinimumSize());
		System.out.println("pref: " + dialog.getPreferredSize());
		System.out.println("max:  " + dialog.getMaximumSize());
		System.out.println("size: " + dialog.getSize());
		dialog.setVisible(true);
	}
	
	private static boolean doLogging() {
		return Logger.getLogger("pat.layout").isLoggable(Level.FINER);
	}
	
	private static void log(String... lines) {
		StringJoiner sj = new StringJoiner(System.lineSeparator());
		for (String line : lines) {
			sj.add(line);
		}
		Logger.getLogger("pat.layout").log(Level.FINER, sj.toString());
	}
	
}
