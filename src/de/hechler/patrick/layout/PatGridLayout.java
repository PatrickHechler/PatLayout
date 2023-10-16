package de.hechler.patrick.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hechler.patrick.layout.PatGridLayout.FillMode.SimpleFillMode;

public class PatGridLayout implements LayoutManager2 {
	
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
	
	private Dimension min;
	private Dimension pref;
	private Dimension max;
	
	/**
	 * creates a new {@link PatGridLayout} with no {@link #xBlocks()}, no {@link #yBlocks()} and {@link #xAfterRepeatBlocks()} and {@link #yAfterRepeatBlocks()}
	 * set to an array with one entry which has both values set to {@link BlockInfo#DYNAMIC}
	 * <p>
	 * this means there is no limit to the block coordinates for the {@link Component components} and all blocks are set to <code>[grow]</code>
	 * <p>
	 * this constructor does not call any method, thus subclasses may need do some special handling
	 */
	public PatGridLayout(int xempty, int yempty) {
		this.xempty = xempty;
		this.yempty = yempty;
		xblocks = EMPTY_BLOCK_INFOS;
		yblocks = EMPTY_BLOCK_INFOS;
		xafterblocks = new BlockInfo[]{ new BlockInfo(BlockInfo.DYNAMIC, BlockInfo.DYNAMIC) };
		yafterblocks = new BlockInfo[]{ new BlockInfo(BlockInfo.DYNAMIC, BlockInfo.DYNAMIC) };
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
	
	public PatGridLayout(int xempty, int yempty, BlockInfo[] xblocks, BlockInfo[] xafterblocks, BlockInfo[] yblocks, BlockInfo[] yafterblocks) {
		if ( this.getClass() != PatGridLayout.class ) {
			xblocks = EMPTY_BLOCK_INFOS; // its a valid state, which can not be
			// used
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
		this(xempty, yempty, parseBlocks(xblocks), parseRepBlocks(xblocks), parseBlocks(yblocks), parseRepBlocks(yblocks));
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
			throw new IllegalArgumentException("illegal constrains: " + ( constraints == null ? "null" : constraints.getClass() + " : " + constraints ));
		}
	}
	
	@Override
	public float getLayoutAlignmentX(Container target) {
		CompInfo inf = this.comps.get(target);
		FillMode mode = inf.wideMode;
		if ( mode == FillMode.FILL_COMPLETLY ) {
			return 0f;
		}
		if ( mode instanceof FillMode.FillModeCls fmc ) {
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
		if ( mode instanceof FillMode.FillModeCls fmc ) {
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
	public synchronized Dimension minimumLayoutSize(Container parent) {
		layoutSizes(parent, LAYOUT_SIZES_MINIMUM);
		return new Dimension(min);
	}
	
	@Override
	public synchronized Dimension preferredLayoutSize(Container parent) {
		layoutSizes(parent, LAYOUT_SIZES_PREFERRED);
		return new Dimension(pref);
	}
	
	@Override
	public synchronized Dimension maximumLayoutSize(Container target) {
		layoutSizes(target, LAYOUT_SIZES_PREFERRED);
		return new Dimension(max);
	}
	
	private static final int LAYOUT_SIZES_MINIMUM   = 0x1;
	private static final int LAYOUT_SIZES_PREFERRED = 0x2;
	private static final int LAYOUT_SIZES_MAXIMUM   = 0x4;
	
	private BlockSize[][] layoutSizes(Container parent, int flags) {
		Dimension max = this.max;
		Dimension pref;
		Dimension min;
		if ( max == null ) {
			max = new Dimension();
			pref = new Dimension();
			min = new Dimension();
			this.max = max;
			this.pref = pref;
			this.min = min;
		} else {
			pref = this.pref;
			min = this.min;
		}
		int mx = -1;
		int my = -1;
		for (int i = parent.getComponentCount(); --i >= 0;) {
			CompInfo inf = comps.get(parent.getComponent(i));
			if ( inf == null ) {
				throw new IllegalStateException("I have no info about the component " + i + " : " + parent.getComponent(i));
			}
			int x = inf.x + inf.w;
			if ( x > mx ) {
				mx = x;
			}
			int y = inf.y + inf.h;
			if ( y > my ) {
				my = y;
			}
		}
		if ( mx == -1 ) {
			return null;
		}
		BlockSize[][] sizes = new BlockSize[my + 1][mx + 1];
		int bitCnt = Integer.bitCount(flags);
		for (BlockSize[] bs : sizes) {
			for (int i = 0; i < bs.length; i++) {
				bs[i] = switch ( bitCnt ) {
				case 1 -> new BlockSize.BlockSize1();
				case 2 -> new BlockSize.BlockSize2();
				case 3 -> new BlockSize.BlockSize3();
				default -> throw new AssertionError(bitCnt);
				};
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
				Dimension cmin = comp.getMinimumSize();
				bs.max(bsi++, cmin);
			}
			if ( ( flags & LAYOUT_SIZES_PREFERRED ) != 0 ) {
				Dimension cmin = comp.getPreferredSize();
				bs.max(bsi++, cmin);
			}
			if ( ( flags & LAYOUT_SIZES_MAXIMUM ) != 0 ) {
				Dimension cmin = comp.getMaximumSize();
				bs.min(bsi, cmin);
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
				Dimension cmin = comp.getMinimumSize();
				grow(sizes, inf, bsi++, cmin, true);
			}
			if ( ( flags & LAYOUT_SIZES_PREFERRED ) != 0 ) {
				Dimension cpref = comp.getPreferredSize();
				grow(sizes, inf, bsi++, cpref, true);
			}
			if ( ( flags & LAYOUT_SIZES_MAXIMUM ) != 0 ) {
				Dimension cmax = comp.getMaximumSize();
				grow(sizes, inf, bsi, cmax, false);
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
			boolean condition;;
			do {
				condition = false;
				int wsum = 0;
				int yc = y + yadd;
				for (int xadd = w, growPotBool = 0; --xadd >= 0;) {
					int xc = x + xadd;
					BlockSize s = get(sizes, xc, yc, calcWidth);
					int sw = calcWidth ? s.w(bsi) : s.h(bsi);
					wsum += sw;
					if ( growPotBool == 0 ) {
						BlockInfo inf = calcWidth ? xinf(xc) : yinf(yc);
						if ( grow ) {
							int infMax = inf.max;
							if ( infMax == BlockInfo.DYNAMIC || infMax < sw ) {
								growPotBool = 1;
							}
						} else {
							int infMin = inf.min;
							if ( ( infMin == BlockInfo.DYNAMIC ? 0 : infMin ) < sw ) {
								growPotBool = 1;
							}
						}
					}
				}
				if ( grow ? wsum <= width : wsum >= width ) {
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
							int curGrowPot = inf.max - get(sizes, yc, xc, bsi, calcWidth);
							if ( growPot != Integer.MAX_VALUE ) {
								growPot += curGrowPot;
							}
							growPots[xadd] = curGrowPot;
						}
					} else {
						int imin = inf.min;
						int curGrowPot = get(sizes, yc, xc, bsi, calcWidth) - ( imin == BlockInfo.DYNAMIC ? 0 : imin );
						growPot += curGrowPot;
						growPots[xadd] = curGrowPot;
					}
				}
				int diff = grow ? wsum - width : width - wsum;
				if ( diff >= growPot ) {
					for (int xadd = w; --xadd >= 0;) {
						int xc = x + xadd;
						add(sizes, bsi, xc, yc, growPots[xadd], calcWidth, grow);
					}
				} else {
					int growPotCnt = 0;
					for (int i = 0; i < growPots.length; i++) {
						if ( growPots[i] != 0 ) {
							growPotCnt++;
						}
					}
					if ( growPotCnt == 1 ) {
						for (int xadd = w; --xadd >= 0;) {
							if ( growPots[xadd] == 0 ) continue;
							int xc = x + xadd;
							add(sizes, bsi, xc, yc, diff, calcWidth, grow);
							break;
						}
					} else {
						int div = ( width + growPotCnt - 1 ) / growPotCnt;
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
							int xc = x + xadd;
							add(sizes, bsi, xc, yc, curDiff, calcWidth, grow);
						}
					}
				}
			} while ( condition );
		}
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
	
	@Override
	public synchronized void layoutContainer(Container parent) {
		BlockSize[][] sizes = layoutSizes(parent, LAYOUT_SIZES_MINIMUM | LAYOUT_SIZES_MAXIMUM);
		if ( sizes == null ) return;
		final int yBlockCount = sizes.length;
		final int xBlockCount = sizes[0].length;
		int[] yminpos = new int[yBlockCount + 1];
		int[] ymaxpos = new int[yBlockCount + 1];
		int[] xminpos = new int[xBlockCount + 1];
		int[] xmaxpos = new int[xBlockCount + 1];
		Arrays.fill(yminpos, Integer.MAX_VALUE);
		Arrays.fill(xminpos, Integer.MAX_VALUE);
		for (int y = yBlockCount; --y >= 0;) { // calculate the width/height
			BlockSize[] bsa = sizes[y];
			for (int x = xBlockCount; --x >= 0;) {
				BlockSize bs = bsa[x];
				int val = bs.h(0);
				if ( val < yminpos[y + 1] ) {
					yminpos[y] = val;
				}
				val = bs.h(1);
				if ( val > ymaxpos[y + 1] ) {
					ymaxpos[y] = val;
				}
				val = bs.w(0);
				if ( val < xminpos[x + 1] ) {
					xminpos[x] = val;
				}
				val = bs.w(1);
				if ( val > xmaxpos[x + 1] ) {
					xmaxpos[x] = val;
				}
			}
		}
		yminpos[0] = ymaxpos[0] = yempty;
		xminpos[0] = xmaxpos[0] = xempty;
		for (int y = 1; y <= yBlockCount; y++) { // convert width/height to positions
			yminpos[y] += yminpos[y - 1] + yempty;
			ymaxpos[y] += ymaxpos[y - 1] + yempty;
		}
		for (int x = 1; x <= xBlockCount; x++) {
			xminpos[x] += xminpos[x - 1] + xempty;
			xmaxpos[x] += xmaxpos[x - 1] + xempty;
		}
		int totalHeight = parent.getHeight();
		adjust(yminpos, ymaxpos, totalHeight);
		int totalWidth = parent.getWidth();
		adjust(yminpos, ymaxpos, totalWidth);
		for (int i = parent.getComponentCount(); --i >= 0;) {
			Component comp = parent.getComponent(i);
			CompInfo inf = comps.get(comp);
			int xb = inf.x;
			int yb = inf.y;
			int wb = inf.w;
			int hb = inf.h;
			int width = 0;
			int height = 0;
			for (int xadd = wb; --xadd >= 0;) {
				width += xminpos[xb + xadd];
			}
			for (int yadd = hb; --yadd >= 0;) {
				height += yminpos[yb + yadd];
			}
			int maxWidth = xminpos[xb + wb] - xminpos[xb];
			int maxHeight = yminpos[yb + hb] - yminpos[yb];
			// TODO Auto-generated method stub
			
			
			
		}
	}
	
	private void adjust(int[] yminpos, int[] ymaxpos, int totalHeight) {
		final int yBlockCount = yminpos.length - 1;
		while ( true ) {
			if ( totalHeight >= yminpos[yBlockCount] ) {
				return;
			}
			int diff = 0;
			for (int y = 1; y <= yBlockCount; y++) {
				int ymin = yminpos[y] - yminpos[y - 1];
				int ymax = ymaxpos[y] - ymaxpos[y - 1];
				if ( ymin < ymax ) {
					diff += ymax - ymin;
				}
			}
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
		builder.append(", comps=");
		builder.append(comps);
		return builder.append(']').toString();
	}
	
	public static sealed interface FillMode {
		
		public static final SimpleFillMode FILL_COMPLETLY = SimpleFillMode.FILL_COMPLETLY;
		public static final SimpleFillMode FILL_MINIMUM   = SimpleFillMode.FILL_MINIMUM;
		public static final SimpleFillMode FILL_MAXIMUM   = SimpleFillMode.FILL_MAXIMUM;
		public static final SimpleFillMode FILL_PREFERRED = SimpleFillMode.FILL_PREFERRED;
		
		public static FillMode fillMode(float mul, SimpleFillMode mode) {
			if ( mul == 1f ) return mode;
			return new FillModeCls(mode, mul);
		}
		
		public static enum SimpleFillMode implements FillMode {
			
			FILL_COMPLETLY,
			
			FILL_MINIMUM,
			
			FILL_MAXIMUM,
			
			FILL_PREFERRED,
		
		}
		
		public static non-sealed class FillModeCls implements FillMode {
			
			private final SimpleFillMode type;
			private final float          mul;
			
			public FillModeCls(SimpleFillMode type, float mul) {
				switch ( type ) {
				case FILL_COMPLETLY, FILL_MAXIMUM:
					if ( !( mul <= 1f ) || mul < 0f ) { // also catch NaN
						throw new IllegalArgumentException("invalid multiplicator: " + type + " only supports multiplicator from 0 to 1. mul=" + mul);
					}
					break;
				case FILL_MINIMUM:
					if ( !( mul >= 1f ) ) { // also catch NaN
						throw new IllegalArgumentException("invalid multiplicator: minimum only supports multiplicator greather or equal than 1. mul=" + mul);
					}
					break;
				case FILL_PREFERRED:
					if ( !( mul >= 0f ) ) { // also catch NaN
						throw new IllegalArgumentException("invalid multiplicator: preferred only supports multiplicator greather or equal than 0. mul=" + mul);
					}
					break;
				}
				this.type = type;
				this.mul = mul;
			}
			
		}
		
	}
	
	public static class CompInfo {
		
		private static final String NUM = "(0(x[0-9A-F]+|b[01]+|[0-7]*)|[1-9][0-9]*)";
		
		private static final Pattern P_NUM = Pattern.compile(NUM, Pattern.CASE_INSENSITIVE);
		
		private static final String A_B = NUM + "\\s+" + NUM;
		
		private static final String OPT_COMMA_SEP = "(\\s+(,\\s*)?|,\\s*)";
		private static final String ALIGN_SEP     = "[ -_]?";
		private static final String ALIGN         = "(align" + ALIGN_SEP + ")?";
		
		private static final String CENTER = "(mid|center)";
		
		private static final String ALIGN_CENTER = "(" + ALIGN + CENTER + ")";
		
		private static final String ALIGN_LEFT           = "(" + ALIGN + "left)";
		private static final String ALIGN_RIGHT          = "(" + ALIGN + "right)";
		private static final String ALIGN_HORIZONTAL_MID = "(" + ALIGN + "h(orizontal)?" + ALIGN_SEP + CENTER + ")";
		private static final String ALIGN_TOP            = "(" + ALIGN + "top)";
		private static final String ALIGN_BOTTOM         = "(" + ALIGN + "bottom)";
		private static final String ALIGN_VERTICAL_MID   = "(" + ALIGN + "v(ertical)?" + ALIGN_SEP + CENTER + ")";
		
		private static final String  ALIGNMENTS_HORIZONTS   = "(" + ALIGN_LEFT + "|" + ALIGN_RIGHT + "|" + ALIGN_HORIZONTAL_MID + ")";
		private static final Pattern P_ALIGNMENTS_HORIZONTS = Pattern.compile(ALIGNMENTS_HORIZONTS, Pattern.CASE_INSENSITIVE);
		
		private static final String  ALIGNMENTS_VERTICALS   = "(" + ALIGN_TOP + "|" + ALIGN_BOTTOM + "|" + ALIGN_VERTICAL_MID + ")";
		private static final Pattern P_ALIGNMENTS_VERTICALS = Pattern.compile(ALIGNMENTS_VERTICALS, Pattern.CASE_INSENSITIVE);
		
		private static final String ALIGNMENTS = "(" + ALIGN_CENTER + "|" + ALIGNMENTS_HORIZONTS + "(" + OPT_COMMA_SEP + ALIGNMENTS_VERTICALS + ")?|"
			+ ALIGNMENTS_VERTICALS + "(" + OPT_COMMA_SEP + ALIGNMENTS_HORIZONTS + ")?" + ")";
		
		private static final String POS = "(" + A_B + "(\\s+" + A_B + ")?)";
		
		private static final String  FULL   = POS + "(" + OPT_COMMA_SEP + ALIGNMENTS + ")?";
		private static final Pattern P_FULL = Pattern.compile(FULL, Pattern.CASE_INSENSITIVE);
		
		private int      x;
		private int      y;
		private int      w;
		private int      h;
		private FillMode wideMode;
		private FillMode heightMode;
		private float    alignx;
		private float    aligny;
		
		private CompInfo() {
		}
		
		public CompInfo(int x, int y) {
			this(x, y, 1, 1, 0.5f, 0.5f);
		}
		
		public CompInfo(int x, int y, int w, int h) {
			this(x, y, w, h, 0.5f, 0.5f);
		}
		
		public CompInfo(int x, int y, float alignx, float aligny) {
			this(x, y, 1, 1, alignx, aligny);
		}
		
		public CompInfo(int x, int y, int w, int h, float alignx, float aligny) {
			this(x, y, w, h, alignx, aligny, SimpleFillMode.FILL_PREFERRED, SimpleFillMode.FILL_PREFERRED);
		}
		
		public CompInfo(int x, int y, int w, int h, float alignx, float aligny, FillMode wideMode, FillMode heightMode) {
			if ( this.getClass() != CompInfo.class ) {
				this.w = 1;// if sub class: make valid before calling the
				// setters
				this.h = 1;
			} // use the setters, so the checks are only needed there
			x(x);
			y(y);
			width(w);
			height(h);
			alignx(alignx);
			aligny(aligny);
			wideMode(wideMode);
			heightMode(heightMode);
		}
		
		public int x() {
			return x;
		}
		
		public void x(int x) {
			if ( x < 0 ) {
				throw new IllegalArgumentException("negative x: " + x);
			}
			this.x = x;
		}
		
		public int y() {
			return y;
		}
		
		public void y(int y) {
			if ( y < 0 ) {
				throw new IllegalArgumentException("negative y: " + y);
			}
			this.y = y;
		}
		
		public int width() {
			return w;
		}
		
		public void width(int w) {
			if ( w <= 0 ) {
				throw new IllegalArgumentException("negative width: " + w);
			}
			this.w = w;
		}
		
		public int height() {
			return h;
		}
		
		public void height(int h) {
			if ( h <= 0 ) {
				throw new IllegalArgumentException("negative height: " + h);
			}
			this.h = h;
		}
		
		public float alignx() {
			return alignx;
		}
		
		public void alignx(float alignx) {
			// `!(NaN >= 0)` is true
			if ( !( alignx >= 0f ) || alignx > 1f ) {
				throw new IllegalArgumentException("x-alignment out of bounds: " + alignx);
			}
			this.alignx = alignx;
		}
		
		public float aligny() {
			return aligny;
		}
		
		public void aligny(float aligny) {
			// `!(NaN >= 0)` is true
			if ( !( aligny >= 0f ) || aligny > 1f ) {
				throw new IllegalArgumentException("y-alignment out of bounds: " + aligny);
			}
			this.aligny = aligny;
		}
		
		public FillMode heightMode() {
			return heightMode;
		}
		
		public void heightMode(FillMode heightMode) {
			if ( heightMode == null ) throw new NullPointerException("heightMode is null");
			this.heightMode = heightMode;
		}
		
		public FillMode wideMode() {
			return wideMode;
		}
		
		public void wideMode(FillMode wideMode) {
			if ( wideMode == null ) throw new NullPointerException("wideMode is null");
			this.wideMode = wideMode;
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("CompInfo [x=");
			builder.append(x);
			builder.append(", y=");
			builder.append(y);
			builder.append(", w=");
			builder.append(w);
			builder.append(", h=");
			builder.append(h);
			builder.append(", alignx=");
			builder.append(alignx);
			builder.append(", aligny=");
			builder.append(aligny);
			return builder.append(']').toString();
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Float.floatToRawIntBits(alignx);
			result = prime * result + Float.floatToRawIntBits(aligny);
			result = prime * result + h;
			result = prime * result + w;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) {
				return true;
			}
			if ( !( obj instanceof CompInfo ) ) {
				return false;
			}
			CompInfo other = (CompInfo) obj;
			if ( alignx != other.alignx ) {
				return false;
			}
			if ( aligny != other.aligny ) {
				return false;
			}
			if ( h != other.h ) {
				return false;
			}
			if ( w != other.w ) {
				return false;
			}
			if ( x != other.x ) {
				return false;
			}
			if ( y != other.y ) {
				return false;
			}
			return true;
		}
		
		public static CompInfo parse(String text) {
			text = text.trim();
			if ( !P_FULL.matcher(text).matches() ) {
				throw new IllegalArgumentException("invalid input: '" + text + "' (regex: '" + FULL + "')");
			}
			CompInfo inf = new CompInfo();
			Matcher matcher = P_NUM.matcher(text);
			matcher.find(); // must find exact two or four matches
			inf.x = parseNum(matcher.group(0));
			matcher.find();
			inf.y = parseNum(matcher.group(0));
			int numEnd = matcher.end();
			if ( matcher.find() ) {
				inf.w = parseNum(matcher.group(0));
				matcher.find();
				inf.h = parseNum(matcher.group(0));
				numEnd = matcher.end();
				if ( inf.w < 0 || inf.h < 0 ) {
					throw new IllegalArgumentException("wide/heigh is zero: " + inf + " : '" + text + "'");
				}
			} else {
				inf.w = 1;
				inf.h = 1;
			}
			if ( numEnd != text.length() ) {
				boolean center = true;
				matcher = P_ALIGNMENTS_HORIZONTS.matcher(text);
				if ( matcher.find(numEnd) ) {
					center = false;
					parseAH(inf, text, matcher.start());
				}
				matcher = P_ALIGNMENTS_VERTICALS.matcher(text);
				if ( matcher.find(numEnd) ) {
					center = false;
					parseAV(inf, text, matcher.start());
				}
				if ( center ) {
					inf.alignx = 0.5f;
					inf.aligny = 0.5f;
				}
			}
			return inf;
		}
		
		private static void parseAV(CompInfo inf, String text, int start) {
			start = skipAlign(text, start);
			switch ( text.charAt(start) ) {
			case 'v', 'V':
				inf.aligny = 0.5f;
				char c = text.charAt(start + 1);
				switch ( c ) {
				case 'e', 'E':
					expectStartsWith(text, "rtical", start + 2);
					switch ( text.charAt(start + 8) ) {
					case ' ', '-', '_' -> start += 7;
					default -> start += 6;
					}
				case ' ', '-', '_':
					c = text.charAt(++start + 1);
				case 'm', 'M':
				case 'c', 'C':
					switch ( c ) {
					case 'm', 'M':
						expectStartsWith(text, "id", start + 2);
						return;
					case 'c', 'C':
						expectStartsWith(text, "enter", start + 2);
						return;
					default:
						parseError(text, start + 1, "mid|center");
					}
				}
			case 't', 'T':
				expectStartsWith(text, "op", start + 1);
				inf.aligny = 0f;
				return;
			case 'b', 'B':
				expectStartsWith(text, "ottom", start + 1);
				inf.aligny = 1f;
				return;
			default:
				parseError(text, start, "bottom|top|v(ertical)?(mid|center)");
			}
		}
		
		private static void parseAH(CompInfo inf, String text, int start) {
			start = skipAlign(text, start);
			switch ( text.charAt(start) ) {
			case 'h', 'H':
				inf.alignx = 0.5f;
				char c = text.charAt(start + 1);
				switch ( c ) {
				case 'o', 'O':
					expectStartsWith(text, "rizontal", start + 2);
					switch ( text.charAt(start + 10) ) {
					case ' ', '-', '_' -> start += 9; // skip only
					// "orizontal"
					// (not the
					// "h")
					default -> start += 8; // same but reduce by one
					// because of fall-thoug
					// to [ -_]
					}
				case ' ', '-', '_':
					c = text.charAt(++start + 1);
				case 'm', 'M':
				case 'c', 'C':
					switch ( c ) {
					case 'm', 'M':
						expectStartsWith(text, "id", start + 2);
						return;
					case 'c', 'C':
						expectStartsWith(text, "enter", start + 2);
						return;
					default:
						parseError(text, start + 1, "mid|center");
					}
				}
			case 'l', 'L':
				expectStartsWith(text, "eft", start + 1);
				inf.alignx = 0f;
				return;
			case 'r', 'R':
				expectStartsWith(text, "ight", start + 1);
				inf.alignx = 1f;
				return;
			default:
				parseError(text, start, "left|right|h(orizontal)?(mid|center)");
			}
		}
		
		private static void parseError(String text, int start, String expected) throws AssertionError {
			throw new AssertionError("illegal char: " + text.charAt(start) + " index: " + start + " expected: " + expected + " text: unparsed: '"
				+ text.substring(start) + "' parsed: '" + text.substring(0, start) + "'");
		}
		
		private static int skipAlign(String text, int start) {
			if ( startsWith(text, "align", start) ) {
				switch ( text.charAt(start + 5) ) {
				case ' ', '-', '_':
					return start + 6;
				default:
					return start + 5;
				}
			}
			return start;
		}
		
		private static void expectStartsWith(String text, String prefix, int toffset) {
			if ( !startsWith(text, prefix, toffset) ) {
				parseError(text, toffset, prefix);
			}
		}
		
		private static boolean startsWith(String text, String prefix, int toffset) {
			if ( text.startsWith(prefix, toffset) ) {
				return true;
			}
			if ( toffset > text.length() - prefix.length() ) {
				return false;
			}
			for (int i = 0, l = prefix.length(); i < l; i++) {
				if ( Character.toLowerCase(text.charAt(toffset + i)) != prefix.charAt(i) ) {
					return false;
				}
			}
			return true;
		}
		
		private static int parseNum(String txt) {
			if ( txt.charAt(0) == '0' ) {
				if ( txt.length() == 1 ) {
					return 0;
				}
				switch ( txt.charAt(1) ) {
				case 'x', 'X':
					return Integer.parseInt(txt.substring(2), 16);
				case 'b', 'B':
					return Integer.parseInt(txt.substring(2), 2);
				default:
					return Integer.parseInt(txt, 8);
				}
			}
			return Integer.parseInt(txt, 10);
		}
		
	}
	
	public static class BlockInfo {
		
		// [grow]
		// [size]
		// [min_size,grow]
		// [min_size,max_size]
		
		private static final String  GROW            = "grow";
		private static final String  FPNUM           = "[0-9]+(\\.[0-9])?|\\.[0-9]";
		private static final String  NUM             = "(" + CompInfo.NUM + "(px|%)?|" + FPNUM + "%?)";
		private static final String  OPT_COMMA_SEP   = CompInfo.OPT_COMMA_SEP;
		private static final Pattern P_OPT_COMMA_SEP = Pattern.compile(OPT_COMMA_SEP, Pattern.CASE_INSENSITIVE);
		
		private static final String  CONTENT = GROW + "|" + NUM + "(" + OPT_COMMA_SEP + "(" + GROW + "|" + NUM + "))?";
		private static final String  BLOCK   = "\\[\\s*(" + CONTENT + ")\\s*\\]";
		private static final Pattern P_BLOCK = Pattern.compile(BLOCK, Pattern.CASE_INSENSITIVE);
		
		private static final String  FULL   = "(" + BLOCK + "\\s*)*";
		private static final Pattern P_FULL = Pattern.compile(FULL, Pattern.CASE_INSENSITIVE);
		
		public static BlockInfo[] parseArr(String text) {
			text = text.trim();
			if ( !P_FULL.matcher(text).matches() ) {
				throw new IllegalArgumentException("invalid input: '" + text + "' (regex: '" + FULL + "')");
			}
			Matcher matcher = P_BLOCK.matcher(text);
			if ( !matcher.find() ) {
				return EMPTY_BLOCK_INFOS;
			}
			List<BlockInfo> result = new ArrayList<>();
			do { // first find already invoked
				result.add(parse(matcher.group(1)));
			} while ( matcher.find() );
			return result.toArray(new BlockInfo[result.size()]);
		}
		
		public static BlockInfo parse(String text) {
			BlockInfo inf = new BlockInfo();
			if ( text.equalsIgnoreCase("grow") ) {
				inf.max = inf.min = DYNAMIC;
				inf.fmax = inf.fmin = FNONE;
				return inf;
			}
			Matcher sepMat = P_OPT_COMMA_SEP.matcher(text);
			boolean sep = sepMat.find();
			switch ( text.charAt(firstEnd(text, sepMat, sep) - 1) ) {
			case 'x':
				if ( text.charAt(firstEnd(text, sepMat, sep) - 2) != 'p' ) {
					CompInfo.parseError(text, firstEnd(text, sepMat, sep) - 2, "px");
				}
				inf.fmin = FNONE;
				inf.min = CompInfo.parseNum(text.substring(0, firstEnd(text, sepMat, sep) - 2));
				break;
			case '%':
				inf.min = DYNAMIC;
				inf.fmin = Float.parseFloat(text.substring(0, firstEnd(text, sepMat, sep) - 1)) / 100f;
				break;
			default:
				if ( text.lastIndexOf('.', firstEnd(text, sepMat, sep)) != -1 ) {
					inf.min = DYNAMIC;
					inf.fmin = Float.parseFloat(text.substring(0, firstEnd(text, sepMat, sep)));
				} else {
					inf.fmin = FNONE;
					inf.min = CompInfo.parseNum(text.substring(0, firstEnd(text, sepMat, sep)));
				}
				break;
			}
			if ( sep ) {
				if ( CompInfo.startsWith(text, "grow", text.length() - 4) ) {
					inf.max = DYNAMIC;
					inf.fmax = FNONE;
				} else {
					switch ( text.charAt(text.length() - 1) ) {
					case 'x':
						if ( text.charAt(text.length() - 2) != 'p' ) {
							CompInfo.parseError(text, text.length() - 2, "px");
						}
						inf.fmax = FNONE;
						inf.max = CompInfo.parseNum(text.substring(sepMat.end(), text.length() - 2));
						break;
					case '%':
						inf.max = DYNAMIC;
						inf.fmax = Float.parseFloat(text.substring(sepMat.end(), text.length() - 1)) / 100f;
						break;
					default:
						if ( text.indexOf('.', sepMat.end()) != -1 ) {
							inf.max = DYNAMIC;
							inf.fmax = Float.parseFloat(text.substring(sepMat.end()));
						} else {
							inf.fmax = FNONE;
							inf.max = CompInfo.parseNum(text.substring(sepMat.end()));
						}
						break;
					}
				}
			} else {
				inf.max = inf.min;
				inf.fmax = inf.fmin;
			}
			inf.set(inf.fmin, inf.fmax);
			inf.set(inf.min, inf.max);
			return inf;
		}
		
		private static int firstEnd(String text, Matcher m, boolean sep) {
			return sep ? m.start() : text.length();
		}
		
		public static final int   DYNAMIC = -1;
		public static final float FNONE   = -1f;
		
		private int min;
		private int max;
		
		private float fmin;
		private float fmax;
		
		private BlockInfo() {
		}
		
		public BlockInfo(int min, int max, float fmin, float fmax) {
			set(min, max);
			set(fmin, fmax);
		}
		
		public BlockInfo(int min, int max) {
			set(min, max);
			set(FNONE, FNONE);
		}
		
		public BlockInfo(float min, float max) {
			set(DYNAMIC, DYNAMIC);
			set(min, max);
		}
		
		/**
		 * get the minimum size
		 * <p>
		 * note that the layout manager may access the variable directly
		 * 
		 * @return the minimum size
		 */
		public int min() {
			return min;
		}
		
		public float fmin() {
			return fmin;
		}
		
		/**
		 * get the maximum size
		 * <p>
		 * note that the layout manager may access the variable directly
		 * 
		 * @return the maximum size
		 */
		public int max() {
			return max;
		}
		
		public float fmax() {
			return fmax;
		}
		
		/**
		 * set both minimum size and maximum size.
		 * 
		 * @param min the minimum size
		 * @param max the maximum size
		 */
		public void set(int min, int max) {
			if ( min < -1 ) {
				throw new IllegalArgumentException("min < -1: " + min);
			}
			if ( max < min && max != -1 ) {
				throw new IllegalArgumentException("max < min & max != -1: " + min + " < " + max);
			}
			this.min = min;
			this.max = max;
		}
		
		public void set(float min, float max) {
			if ( ( min < 0f || min > 1f ) && min != FNONE ) {
				throw new IllegalArgumentException("(min < 0 | min > 1) & min != -1: " + min);
			}
			if ( ( max < 0f || max > 1f ) && max != FNONE ) {
				throw new IllegalArgumentException("(min < 0 | min > 1) & min != -1: " + max);
			}
			if ( max < min ) {
				throw new IllegalArgumentException("max < min: " + min + " < " + max);
			}
			this.fmin = min;
			this.fmax = max;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + max;
			result = prime * result + min;
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) {
				return true;
			}
			if ( !( obj instanceof BlockInfo ) ) {
				return false;
			}
			BlockInfo other = (BlockInfo) obj;
			if ( max != other.max ) {
				return false;
			}
			if ( min != other.min ) {
				return false;
			}
			return true;
		}
		
		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			b.append("BlockInfo [min=");
			if ( min != DYNAMIC ) {
				b.append(min);
			} else {
				b.append("dynamic");
			}
			b.append(", max=");
			if ( max != DYNAMIC ) {
				b.append(max);
			} else {
				b.append("dynamic");
			}
			if ( fmin != FNONE ) {
				b.append(", fmin=");
				b.append(fmin);
			}
			if ( fmax != FNONE ) {
				b.append(", fmax=");
				b.append(fmax);
			}
			return b.append(']').toString();
		}
		
	}
	
	private static interface BlockSize {
		
		static class BlockSize1 implements BlockSize {
			
			private int w0 = -1;
			private int h0 = -1;
			
			@Override
			public void max(int i, Dimension dim) {
				int v = w0;
				if ( v < dim.width ) {
					w0 = dim.width;
				}
				v = h0;
				if ( v < dim.height ) {
					h0 = dim.height;
				}
			}
			
			@Override
			public void min(int i, Dimension dim) {
				int v = w0;
				if ( v < dim.width || v == -1 ) {
					w0 = dim.width;
				}
				v = h0;
				if ( v < dim.height || v == -1 ) {
					h0 = dim.height;
				}
			}
			
			@Override
			public void w(int i, int val) {
				w0 = val;
			}
			
			@Override
			public void h(int i, int val) {
				h0 = val;
			}
			
			@Override
			public int w(int i) {
				return Math.max(0, w0);
			}
			
			@Override
			public int h(int i) {
				return Math.max(0, h0);
			}
			
		}
		
		static class BlockSize2 implements BlockSize {
			
			private int w0 = -1;
			private int h0 = -1;
			private int w1 = -1;
			private int h1 = -1;
			
			@Override
			public void max(int i, Dimension dim) {
				int v = i == 0 ? w0 : w1;
				if ( v < dim.width ) {
					if ( i == 0 ) {
						w0 = dim.width;
					} else {
						w1 = dim.width;
					}
				}
				v = i == 0 ? h0 : h1;
				if ( v < dim.height ) {
					if ( i == 0 ) {
						h0 = dim.height;
					} else {
						h1 = dim.height;
					}
				}
			}
			
			@Override
			public void min(int i, Dimension dim) {
				int v = i == 0 ? w0 : w1;
				if ( v < dim.width || v == -1 ) {
					if ( i == 0 ) {
						w0 = dim.width;
					} else {
						w1 = dim.width;
					}
				}
				v = i == 0 ? h0 : h1;
				if ( v < dim.height || v == -1 ) {
					if ( i == 0 ) {
						h0 = dim.height;
					} else {
						h1 = dim.height;
					}
				}
			}
			
			@Override
			public void w(int i, int val) {
				if ( i == 0 ) {
					w0 = val;
				} else {
					w1 = val;
				}
			}
			
			@Override
			public void h(int i, int val) {
				if ( i == 0 ) {
					h0 = val;
				} else {
					h1 = val;
				}
			}
			
			@Override
			public int w(int i) {
				return Math.max(0, i == 0 ? w0 : w1);
			}
			
			@Override
			public int h(int i) {
				return Math.max(0, i == 0 ? h0 : h1);
			}
			
		}
		
		static class BlockSize3 implements BlockSize {
			
			private int w0 = -1;
			private int h0 = -1;
			private int w1 = -1;
			private int h1 = -1;
			private int w2 = -1;
			private int h2 = -1;
			
			@Override
			public void max(int i, Dimension dim) {
				int v = switch ( i ) {
				case 0 -> w0;
				case 1 -> w1;
				case 2 -> w2;
				default -> throw new AssertionError(i);
				};
				if ( v < dim.width ) {
					switch ( i ) {
					case 0 -> w0 = dim.width;
					case 1 -> w1 = dim.width;
					case 2 -> w2 = dim.width;
					}
				}
				v = switch ( i ) {
				case 0 -> h0;
				case 1 -> h1;
				case 2 -> h2;
				default -> throw new AssertionError(i);
				};
				if ( v < dim.height ) {
					switch ( i ) {
					case 0 -> h0 = dim.height;
					case 1 -> h1 = dim.height;
					case 2 -> h2 = dim.height;
					}
				}
			}
			
			@Override
			public void min(int i, Dimension dim) {
				int v = switch ( i ) {
				case 0 -> w0;
				case 1 -> w1;
				case 2 -> w2;
				default -> throw new AssertionError(i);
				};
				if ( v < dim.width || v == -1 ) {
					switch ( i ) {
					case 0 -> w0 = dim.width;
					case 1 -> w1 = dim.width;
					case 2 -> w2 = dim.width;
					}
				}
				v = switch ( i ) {
				case 0 -> h0;
				case 1 -> h1;
				case 2 -> h2;
				default -> throw new AssertionError(i);
				};
				if ( v < dim.height || v == -1 ) {
					switch ( i ) {
					case 0 -> h0 = dim.height;
					case 1 -> h1 = dim.height;
					case 2 -> h2 = dim.height;
					}
				}
			}
			
			@Override
			public int w(int i) {
				switch ( i ) {
				case 0:
					return Math.max(0, w0);
				case 1:
					return Math.max(0, w1);
				case 2:
					return Math.max(0, w2);
				};
				throw new AssertionError(i);
			}
			
			@Override
			public void h(int i, int val) {
				switch ( i ) {
				case 0 -> h0 = val;
				case 1 -> h1 = val;
				case 2 -> h2 = val;
				}
			}
			
			@Override
			public void w(int i, int val) {
				switch ( i ) {
				case 0 -> w0 = val;
				case 1 -> w1 = val;
				case 2 -> w2 = val;
				}
			}
			
			@Override
			public int h(int i) {
				switch ( i ) {
				case 0:
					return Math.max(0, h0);
				case 1:
					return Math.max(0, h1);
				case 2:
					return Math.max(0, h2);
				}
				throw new AssertionError(i);
			}
			
		}
		
		void max(int i, Dimension dim);
		
		void min(int i, Dimension dim);
		
		void w(int i, int val);
		
		void h(int i, int val);
		
		int w(int i);
		
		int h(int i);
		
	}
	
	public static void main(String[] args) {
		parse("0 0b01");
		parse("0 0b0 0xA09 10");
		parse("0 0b0 align horizontal-center align_vertical_mid");
		parse("0 0b0 0xFF 010 align horizontal-center align_vertical_mid");
		parse("0 0b0 hmid vcenter");
		parse("0 0b0 vcenter hmid");
		parse("0 0b0 vcenter left");
		parse("0 0b0 left vcenter");
		try {
			CompInfo.parse("0 0").alignx(Float.NaN);
			System.err.println("no error");
		} catch ( IllegalArgumentException ignore ) {}
		System.out.println(new PatGridLayout(0, 0, "", ""));
		System.out.println(new PatGridLayout(0, 0));
		System.out.println(new PatGridLayout(0, 0, ":[grow]", "[10][20][0x10][010][0B10][200]:[5px]"));
		System.out.println(new PatGridLayout(0, 0, ":[0]", ":[GROW]"));
	}
	
	private static void parse(String text) {
		System.out.println(CompInfo.parse(text) + " : " + text);
	}
	
}
