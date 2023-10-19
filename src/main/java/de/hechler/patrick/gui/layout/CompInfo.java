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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hechler.patrick.gui.layout.FillMode.SimpleFillMode;

/**
 * a {@link CompInfo} instance stores information about a single {@link Component} instance:
 * <ul>
 * <li>the alignment ({@link #xAlign()},{@link #yAlign()})</li>
 * <li>the used blocks (<code>{@link #xPos()},{@link #yPos()},{@link #width()},{@link #height()}</code>)</li>
 * <li>the fill modes ({@link #widthMode()},{@link #heightMode()})</li>
 * </ul>
 * to create a {@link CompInfo}:
 * <ul>
 * <li>the {@link CompInfo#parse(String)} method can be used</li>
 * <li>a public constructor can be used
 * <ul>
 * <li>{@link #CompInfo(int, int, int, int, float, float, FillMode, FillMode)} sets all values of the {@link CompInfo}
 * instance</li>
 * <li>the other constructors just delegate to the
 * {@link #CompInfo(int, int, int, int, float, float, FillMode, FillMode)} constructor with default values for the
 * values which are not passed as arguments</li>
 * </ul>
 * </li>
 * </ul>
 * note that {@link CompInfo} instances are modifiable:
 * <ul>
 * <li>the bounds can be set with {@link #bounds(int, int, int, int)}</li>
 * <li>the alignment can be set with {@link #xAlign(float)}/{@link #yAlign(float)}</li>
 * <li>the fill modes can be set with {@link #wideMode(FillMode)}/{@link #heightMode(FillMode)}</li>
 * </ul>
 * 
 * @author Patrick Hechler
 */
public class CompInfo {
	
	int      x;
	int      y;
	int      w;
	int      h;
	FillMode widthMode;
	FillMode heightMode;
	float    alignx;
	float    aligny;
	
	private CompInfo() {
	}
	
	/**
	 * create a new {@link CompInfo} with the given values and the following default values:
	 * <ul>
	 * <li>1 for {@link #width()}/{@link #height()}</li>
	 * <li>0f for {@link #xAlign()}/{@link #yAlign()}</li>
	 * <li>{@link FillMode#FILL_MAXIMUM} for {@link #widthMode()}/{@link #heightMode()}</li>
	 * </ul>
	 * 
	 * @param x the value for {@link #xPos()}
	 * @param y the value for {@link #xPos()}
	 * 
	 * @throws IllegalArgumentException if a argument is invalid
	 */
	public CompInfo(int x, int y) {
		this(x, y, 1, 1, 0f, 0f);
	}
	
	/**
	 * create a new {@link CompInfo} with the given values and the following default values:
	 * <ul>
	 * <li>0f for {@link #xAlign()}/{@link #yAlign()}</li>
	 * <li>{@link FillMode#FILL_MAXIMUM} for {@link #widthMode()}/{@link #heightMode()}</li>
	 * </ul>
	 * 
	 * @param x the value for {@link #xPos()}
	 * @param y the value for {@link #xPos()}
	 * @param w the value for {@link #width()}
	 * @param h the value for {@link #height()}
	 * 
	 * @throws IllegalArgumentException if a argument is invalid
	 */
	public CompInfo(int x, int y, int w, int h) throws IllegalArgumentException {
		this(x, y, w, h, 0f, 0f);
	}
	
	/**
	 * create a new {@link CompInfo} with the given values and the following default values:
	 * <ul>
	 * <li>1 for {@link #width()}/{@link #height()}</li>
	 * <li>{@link FillMode#FILL_MAXIMUM} for {@link #widthMode()}/{@link #heightMode()}</li>
	 * </ul>
	 * 
	 * @param x      the value for {@link #xPos()}
	 * @param y      the value for {@link #xPos()}
	 * @param xAlign the value for {@link #xAlign()}
	 * @param yAlign the value for {@link #yAlign()}
	 * 
	 * @throws IllegalArgumentException if a argument is invalid
	 */
	public CompInfo(int x, int y, float xAlign, float yAlign) throws IllegalArgumentException {
		this(x, y, 1, 1, xAlign, yAlign);
	}
	
	/**
	 * create a new {@link CompInfo} with the given values and the following default values:
	 * <ul>
	 * <li>{@link FillMode#FILL_MAXIMUM} for {@link #widthMode()}/{@link #heightMode()}</li>
	 * </ul>
	 * 
	 * @param x      the value for {@link #xPos()}
	 * @param y      the value for {@link #xPos()}
	 * @param w      the value for {@link #width()}
	 * @param h      the value for {@link #height()}
	 * @param xAlign the value for {@link #xAlign()}
	 * @param yAlign the value for {@link #yAlign()}
	 * 
	 * @throws IllegalArgumentException if a argument is invalid
	 */
	public CompInfo(int x, int y, int w, int h, float xAlign, float yAlign) throws IllegalArgumentException {
		this(x, y, w, h, xAlign, yAlign, FillMode.FILL_MAXIMUM, FillMode.FILL_MAXIMUM);
	}
	
	/**
	 * create a new {@link CompInfo} with the given values
	 * 
	 * @param x          the value for {@link #xPos()}
	 * @param y          the value for {@link #xPos()}
	 * @param w          the value for {@link #width()}
	 * @param h          the value for {@link #height()}
	 * @param xAlign     the value for {@link #xAlign()}
	 * @param yAlign     the value for {@link #yAlign()}
	 * @param wideMode   the value for {@link #heightMode()}
	 * @param heightMode the value for {@link #widthMode()}
	 * 
	 * @throws IllegalArgumentException if a argument is invalid
	 * @throws NullPointerException     if a argument is <code>null</code>
	 */
	public CompInfo(int x, int y, int w, int h, float xAlign, float yAlign, FillMode wideMode, FillMode heightMode)
		throws IllegalArgumentException, NullPointerException {
		if ( this.getClass() != CompInfo.class ) {
			this.w = 1;// if sub class: make valid before calling the setters
			this.h = 1;
		} // use the setters, so the checks are only needed there
		bounds(x, y, w, h);
		yAlign(xAlign);
		yAlign(yAlign);
		wideMode(wideMode);
		heightMode(heightMode);
	}
	
	/**
	 * returns the index of the first used x block
	 * 
	 * @return the index of the first used x block
	 * 
	 * @see #yPos()
	 * @see #width()
	 * @see #height()
	 * @see #bounds(int, int, int, int)
	 */
	public int xPos() {
		return this.x;
	}
	
	/**
	 * returns the index of the first used y block
	 * 
	 * @return the index of the first used y block
	 * 
	 * @see #xPos()
	 * @see #width()
	 * @see #height()
	 * @see #bounds(int, int, int, int)
	 */
	public int yPos() {
		return this.y;
	}
	
	/**
	 * returns the number of used x block
	 * 
	 * @return the number of used x block
	 * 
	 * @see #xPos()
	 * @see #yPos()
	 * @see #height()
	 * @see #bounds(int, int, int, int)
	 */
	public int width() {
		return this.w;
	}
	
	/**
	 * returns the number of used y block
	 * 
	 * @return the number of used y block
	 * 
	 * @see #xPos()
	 * @see #yPos()
	 * @see #width()
	 * @see #bounds(int, int, int, int)
	 */
	public int height() {
		return this.h;
	}
	
	/**
	 * sets the rectangle of used blocks
	 * 
	 * @param x the index of the first used x block
	 * @param y the index of the first used y block
	 * @param w the number of used x blocks
	 * @param h the number of used y blocks
	 * 
	 * @see #xPos()
	 * @see #yPos()
	 * @see #width()
	 * @see #height()
	 */
	public void bounds(int x, int y, int w, int h) {
		if ( x < 0 || y < 0 || w <= 0 || h <= 0 ) {
			throw new IllegalArgumentException("x/y < 0 | w/h <= 0: x=" + x + " y=" + y + " w=" + w + " h=" + h);
		}
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	
	/**
	 * returns the x alignment (a value between <code>0</code> and <code>1</code> (both inclusive))
	 * 
	 * @return the x alignment (a value between <code>0</code> and <code>1</code> (both inclusive))
	 */
	public float xAlign() {
		return this.alignx;
	}
	
	/**
	 * sets the x alignment (a value between <code>0</code> and <code>1</code> (both inclusive))
	 * 
	 * @param xAlign the x alignment (a value between <code>0</code> and <code>1</code> (both inclusive))
	 */
	public void xAlign(float xAlign) {
		if ( !( xAlign >= 0f ) || xAlign > 1f ) {// NOSONAR // `!(NaN >= 0)` is true
			throw new IllegalArgumentException("x-alignment out of bounds: " + xAlign);
		}
		this.alignx = xAlign;
	}
	
	/**
	 * returns the y alignment (a value between <code>0</code> and <code>1</code> (both inclusive))
	 * 
	 * @return the y alignment (a value between <code>0</code> and <code>1</code> (both inclusive))
	 */
	public float yAlign() {
		return this.aligny;
	}
	
	/**
	 * sets the y alignment (a value between <code>0</code> and <code>1</code> (both inclusive))
	 * 
	 * @param yAlign the y alignment (a value between <code>0</code> and <code>1</code> (both inclusive))
	 */
	public void yAlign(float yAlign) {
		if ( !( yAlign >= 0f ) || yAlign > 1f ) {// NOSONAR // `!(NaN >= 0)` is true
			throw new IllegalArgumentException("y-alignment out of bounds: " + yAlign);
		}
		this.aligny = yAlign;
	}
	
	/**
	 * returns the height fill mode<br>
	 * this mode specifies how the {@link Component#getHeight() height} of the {@link Component} is calculated
	 * 
	 * @return the height fill mode
	 */
	public FillMode heightMode() {
		return this.heightMode;
	}
	
	/**
	 * sets the height fill mode<br>
	 * this mode specifies how the {@link Component#getHeight() height} of the {@link Component} is calculated
	 * 
	 * @param heightMode the height fill mode
	 * 
	 * @throws NullPointerException if <code>widthMode</code> is <code>null</code>
	 */
	public void heightMode(FillMode heightMode) throws NullPointerException {
		if ( heightMode == null ) throw new NullPointerException("heightMode is null");
		this.heightMode = heightMode;
	}
	
	/**
	 * returns the width fill mode<br>
	 * this mode specifies how the {@link Component#getWidth() width} of the {@link Component} is calculated
	 * 
	 * @return the width fill mode
	 */
	public FillMode widthMode() {
		return this.widthMode;
	}
	
	/**
	 * sets the width fill mode<br>
	 * this mode specifies how the {@link Component#getWidth() width} of the {@link Component} is calculated
	 * 
	 * @param widthMode the height fill mode
	 * 
	 * @throws NullPointerException if <code>widthMode</code> is <code>null</code>
	 */
	public void wideMode(FillMode widthMode) throws NullPointerException {
		if ( widthMode == null ) throw new NullPointerException("widthMode is null");
		this.widthMode = widthMode;
	}
	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime  = 31;
		int       result = 1;
		result = prime * result + Float.floatToRawIntBits(this.alignx);
		result = prime * result + Float.floatToRawIntBits(this.aligny);
		result = prime * result + this.h;
		result = prime * result + this.heightMode.hashCode();
		result = prime * result + this.w;
		result = prime * result + this.widthMode.hashCode();
		result = prime * result + this.x;
		result = prime * result + this.y;
		return result;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) { return true; }
		if ( !( obj instanceof CompInfo ) ) { return false; }
		CompInfo other = (CompInfo) obj;
		if ( this.alignx != other.alignx ) { return false; }
		if ( this.aligny != other.aligny ) { return false; }
		if ( this.h != other.h ) { return false; }
		if ( this.heightMode != other.heightMode && !this.heightMode.equals(other.heightMode) ) { return false; }
		if ( this.w != other.w ) { return false; }
		if ( this.widthMode != other.widthMode && !this.widthMode.equals(other.widthMode) ) { return false; }
		if ( this.x != other.x ) { return false; }
		return this.y == other.y;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CompInfo [x=");
		builder.append(this.x);
		builder.append(", y=");
		builder.append(this.y);
		builder.append(", w=");
		builder.append(this.w);
		builder.append(", h=");
		builder.append(this.h);
		builder.append(", wideMode=");
		builder.append(this.widthMode);
		builder.append(", heightMode=");
		builder.append(this.heightMode);
		builder.append(", alignx=");
		builder.append(this.alignx);
		builder.append(", aligny=");
		builder.append(this.aligny);
		return builder.append(']').toString();
	}
	
	private static final String COMMA = "\\s*,\\s*";
	
	private static final String NUMBER_HEC = "0x[0-9a-f]+";
	private static final String NUMBER_BIN = "0b[01]+";
	private static final String NUMBER_OCT = "0[0-7]*";
	private static final String NUMBER_DEC = "[1-9][0-9]*";// NOSONAR
	static final String         NUMBER     =
		"(" + NUMBER_HEC + "|" + NUMBER_BIN + "|" + NUMBER_OCT + "|" + NUMBER_DEC + ")";
	
	private static final String FP_NUMBER_DEC1 = "[.][0-9]+";// NOSONAR
	private static final String FP_NUMBER_DEC2 = "[0-9]+[.][0-9]*";// NOSONAR
	private static final String FP_NUMBER      = "(" + NUMBER + "|" + FP_NUMBER_DEC1 + "|" + FP_NUMBER_DEC2 + ")";
	
	private static final String XY_NAME = "position";
	private static final String XY      = "(?<" + XY_NAME + ">" + NUMBER + "\\s+" + NUMBER + ")";
	private static final String WH_NAME = "sizes";
	private static final String WH      = "(?<" + WH_NAME + ">\\s+" + NUMBER + "\\s+" + NUMBER + ")?";
	
	private static final String A_OPT_MID = "mid";
	
	private static final String XA_OPT_LEFT  = "left";
	private static final String XA_OPT_MID   = A_OPT_MID;
	private static final String XA_OPT_RIGHT = "right";
	private static final String XA_OPTS      =
		"(" + FP_NUMBER + "|" + XA_OPT_LEFT + "|" + XA_OPT_MID + "|" + XA_OPT_RIGHT + ")";
	private static final String XA_PREFIX    = "(xalign\\s*=\\s*)?";
	private static final String XA_NAME      = "xAlign";
	private static final String XA           = "(?<" + XA_NAME + ">" + COMMA + XA_PREFIX + XA_OPTS + ")?";
	
	private static final String YA_OPT_TOP    = "top";
	private static final String YA_OPT_MID    = A_OPT_MID;
	private static final String YA_OPT_BOTTOM = "bottom";
	private static final String YA_OPTS       =
		"(" + FP_NUMBER + "|" + YA_OPT_TOP + "|" + YA_OPT_MID + "|" + YA_OPT_BOTTOM + ")";
	private static final String YA_PREFIX     = "(yalign\\s*=\\s*)?";
	private static final String YA_NAME       = "yAlign";
	private static final String YA            = "(?<" + YA_NAME + ">" + COMMA + YA_PREFIX + YA_OPTS + ")?";
	
	private static final String FM_OPT_COMPLETE  = "complete|full";
	private static final String FM_OPT_MAXIMUM   = "max(imum)?";
	private static final String FM_OPT_PREFERRED = "pref(erred)?";
	private static final String FM_OPT_MINIMUM   = "min(imum)?";
	
	private static final String FM_OPTS_BASE =
		FM_OPT_COMPLETE + "|" + FM_OPT_MAXIMUM + "|" + FM_OPT_PREFERRED + "|" + FM_OPT_MINIMUM;
	private static final String FW_OPTS_NAME = "fillWidthOpts";
	private static final String FW_OPTS      = "(?<" + FW_OPTS_NAME + ">" + FM_OPTS_BASE + ")";
	private static final String FH_OPTS_NAME = "fillHeightOpts";
	private static final String FH_OPTS      = "(?<" + FH_OPTS_NAME + ">" + FM_OPTS_BASE + ")";
	
	private static final String FM_MUL_BASE = "\\s*[*]\\s*" + FP_NUMBER;
	private static final String FW_MUL_NAME = "fillWidthMul";
	private static final String FW_MUL      = "(?<" + FW_MUL_NAME + ">" + FM_MUL_BASE + ")?";
	private static final String FH_MUL_NAME = "fillHeightMul";
	private static final String FH_MUL      = "(?<" + FH_MUL_NAME + ">" + FM_MUL_BASE + ")?";
	
	private static final String FW_PREFIX = "(fill-width\\s*=\\s*)?";
	private static final String FW_NAME   = "fillWidth";
	private static final String FW        = "(?<" + FW_NAME + ">" + COMMA + FW_PREFIX + FW_OPTS + FW_MUL + ")?";
	
	private static final String FH_PREFIX = "(fill-height\\s*=\\s*)?";
	private static final String FH_NAME   = "fillHeight";
	private static final String FH        = "(?<" + FH_NAME + ">" + COMMA + FH_PREFIX + FH_OPTS + FH_MUL + ")?";
	
	private static final String  FULL   = XY + WH + XA + YA + FW + FH;
	private static final Pattern P_FULL = Pattern.compile(FULL, Pattern.CASE_INSENSITIVE);
	
	/**
	 * parses the given {@link String} to a {@link CompInfo} instance
	 * <p>
	 * <br>
	 * {@code str} has to be formatted according to the {@code COMP-INFO} specification.<br>
	 * the case of {@code str} is in all specifications ignored.
	 * <p>
	 * <b><code>COMP-INFO</code></b>:<br>
	 * whitespace between the words/numbers is in this specification allowed, if they are not separated by a minus
	 * ({code '-'}).<br>
	 * <ol>
	 * <li><code>(xpos=)?{@link #xPos() X-POS}</code>, whitespace and then <code>(ypos=)?{@link #yPos() Y-POS}</code>
	 * <ul>
	 * <li>both {@code X-POS} and {@code Y-POS} are a <code>{@literal <}NUMBER{@literal >}</code></li>
	 * </ul>
	 * </li>
	 * <li><i>optional</i>: <code>(width=)?{@link #width() WIDTH}</code>, whitespace and then
	 * <code>(height=)?{@link #height() HEIGHT}</code> (default is {@code 1} for both)
	 * <ul>
	 * <li>both {@code WIDTH} and {@code HEIGHT} are a <code>{@literal <}NUMBER{@literal >}</code></li>
	 * </ul>
	 * </li>
	 * <li><i>alignment</i>:
	 * <ol>
	 * <li><i>optional</i>: <code>[,](xalign=)?{@link #xAlign() X-ALIGNMENT} (default is {@code 0})</code>
	 * <ul>
	 * <li>{@code X-ALIGNMENT}: {@code left} (same as {@code 0})</li>
	 * <li>{@code X-ALIGNMENT}: {@code mid} (same as {@code 0.5})</li>
	 * <li>{@code X-ALIGNMENT}: {@code right} (same as {@code 1})</li>
	 * <li>{@code X-ALIGNMENT}: <code>{@literal <}FP-NUMBER{@literal >}</code></li>
	 * </ul>
	 * </li>
	 * <li><i>optional</i>: <code>[,](yalign=)?{@link #yAlign() Y-ALIGNMENT} (default is {@code 0})</code>
	 * <ul>
	 * <li>{@code Y-ALIGNMENT}: {@code top} (same as {@code 0})</li>
	 * <li>{@code Y-ALIGNMENT}: {@code mid} (same as {@code 0.5})</li>
	 * <li>{@code Y-ALIGNMENT}: {@code bottom} (same as {@code 1})</li>
	 * <li>{@code Y-ALIGNMENT}: <code>{@literal <}FP-NUMBER{@literal >}</code></li>
	 * </ul>
	 * </li>
	 * </ol>
	 * </li>
	 * <li><i>fill modes</i>:
	 * <ol>
	 * <li><i>optional</i>:
	 * <code>[,](fill-width=)?{@link #widthMode() FILL-WIDTH}([*]{@literal <}FP-NUMBER{@literal >})?</code> (default is
	 * {@link FillMode#FILL_MAXIMUM})
	 * <ul>
	 * <li>{@code FILL-WIDTH}: <code>(complete|full)</code> ({@link FillMode#FILL_COMPLETLY})</li>
	 * <li>{@code FILL-WIDTH}: <code>max(imum)?</code> ({@link FillMode#FILL_MAXIMUM})</li>
	 * <li>{@code FILL-WIDTH}: <code>pref(erred)?</code> ({@link FillMode#FILL_PREFERRED})</li>
	 * <li>{@code FILL-WIDTH}: <code>min(imum)?</code> ({@link FillMode#FILL_MINIMUM})</li>
	 * </ul>
	 * </li>
	 * <li><i>optional</i>:
	 * <code>[,](fill-height=)?{@link #heightMode() FILL-HEIGHT}([*]{@literal <}FP-NUMBER{@literal >})?</code> (default
	 * is {@link FillMode#FILL_MAXIMUM})
	 * <ul>
	 * <li>{@code FILL-HEIGHT}: <code>(complete|full)</code> ({@link FillMode#FILL_COMPLETLY})</li>
	 * <li>{@code FILL-HEIGHT}: <code>max(imum)?</code> ({@link FillMode#FILL_MAXIMUM})</li>
	 * <li>{@code FILL-HEIGHT}: <code>pref(erred)?</code> ({@link FillMode#FILL_PREFERRED})</li>
	 * <li>{@code FILL-HEIGHT}: <code>min(imum)?</code> ({@link FillMode#FILL_MINIMUM})</li>
	 * </ul>
	 * </li>
	 * </ol>
	 * </li>
	 * </ol>
	 * <b><code>FP-NUMBER</code></b>:
	 * <ul>
	 * <li><code>{@literal <}FP-NUMBER{@literal >}</code> (see {@code NUMBER})</li>
	 * <li><code>[.][0-9]+</code> (decimal floating point number (without leading zero))</li>
	 * <li><code>[0-9]+[.][0-9]*</code> (decimal floating point number)</li>
	 * </ul>
	 * <b><code>NUMBER</code></b>:
	 * <ul>
	 * <li><code>0x[0-9a-f]+</code> (hexadecimal number (needs the {@code '0x'} prefix))</li>
	 * <li><code>0b[01]+</code> (binary number (needs the {@code '0b'} prefix))</li>
	 * <li><code>0[0-7]*</code> (octal number (must start with zero))</li>
	 * <li><code>[1-9][0-9]*</code> (decimal number (must not start with zero))</li>
	 * </ul>
	 * 
	 * @param str the {@link String} to be parsed
	 * 
	 * @return the result of the parsing operation
	 */
	public static CompInfo parse(String str) {
		str = str.trim();
		Matcher matcher = P_FULL.matcher(str);
		if ( !matcher.matches() ) {
			throw new IllegalArgumentException(
				"invalid input: '" + str + "' (if it helps here is the regex: '" + FULL + "')");
		}
		CompInfo             inf    = new CompInfo();
		String[]             arr    = matcher.group(XY_NAME).split("\\s+");
		int                  x      = parseNum(arr[0]);
		int                  y      = parseNum(arr[1]);
		Map<String, Integer> groups = matcher.namedGroups();
		Integer              gindex = groups.get(WH_NAME);
		String               group;
		if ( ( group = matcher.group(gindex.intValue()) ) != null && !( group = group.trim() ).isEmpty() ) {
			arr = group.split("\\s+");
			int w = parseNum(arr[0]);
			int h = parseNum(arr[1]);
			inf.bounds(x, y, w, h);
		} else {
			inf.bounds(x, y, 1, 1);
		}
		gindex = groups.get(XA_NAME);
		if ( ( group = matcher.group(gindex.intValue()) ) != null && !( group = group.trim() ).isEmpty() ) {
			inf.xAlign(parseAlign(group));
		}
		gindex = groups.get(YA_NAME);
		if ( ( group = matcher.group(gindex.intValue()) ) != null && !( group = group.trim() ).isEmpty() ) {
			inf.yAlign(parseAlign(group));
		}
		gindex = groups.get(FW_NAME);
		if ( ( group = matcher.group(gindex.intValue()) ) != null && !( group = group.trim() ).isEmpty() ) {
			SimpleFillMode sfm = parseSimpleFillMode(matcher.group(FW_OPTS_NAME));
			gindex = groups.get(FW_MUL_NAME);
			if ( ( group = matcher.group(gindex.intValue()) ) != null && !( group = group.trim() ).isEmpty() ) {
				inf.widthMode = new FillMode.ComplexFillMode(sfm, parseFpNumWithMul(group));
			} else {
				inf.widthMode = sfm;
			}
		} else {
			inf.widthMode = FillMode.FILL_MAXIMUM;
		}
		gindex = groups.get(FH_NAME);
		if ( ( group = matcher.group(gindex.intValue()) ) != null && !( group = group.trim() ).isEmpty() ) {
			SimpleFillMode sfm = parseSimpleFillMode(matcher.group(FH_OPTS_NAME));
			gindex = groups.get(FH_MUL_NAME);
			if ( ( group = matcher.group(gindex.intValue()) ) != null && !( group = group.trim() ).isEmpty() ) {
				inf.heightMode = new FillMode.ComplexFillMode(sfm, parseFpNumWithMul(group));
			} else {
				inf.heightMode = sfm;
			}
		} else {
			inf.heightMode = FillMode.FILL_MAXIMUM;
		}
		return inf;
	}
	
	private static float parseFpNumWithMul(String group) {
		return parseFpNum(group.substring(group.indexOf('*')).trim());
	}
	
	private static SimpleFillMode parseSimpleFillMode(String group) {
		switch ( group ) {
		case "complete", "full":
			return FillMode.FILL_COMPLETLY;
		case "max", "maximum":
			return FillMode.FILL_MAXIMUM;
		case "pref", "preferred":
			return FillMode.FILL_PREFERRED;
		case "min", "minimum":
			return FillMode.FILL_MINIMUM;
		default:
			throw parseError(group, 0, group);
		}
	}
	
	private static float parseAlign(String str) {
		int index = str.indexOf('=');
		if ( index == -1 ) {
			index = str.indexOf(',');
		}
		str = str.substring(index + 1).trim();
		switch ( str ) {
		case XA_OPT_LEFT, YA_OPT_TOP:
			return 0f;
		case A_OPT_MID:
			return 0.5f;
		case XA_OPT_RIGHT, YA_OPT_BOTTOM:
			return 1f;
		default:
			return parseNum(str);
		}
	}
	
	static RuntimeException parseError(String text, int start, String expected) throws AssertionError {
		// throw AssertionError, because the regex should catch all invalid inputs
		throw new AssertionError("illegal char: " + text.charAt(start) + " index: " + start + " expected: " + expected
			+ " text: unparsed: '" + text.substring(start) + "' parsed: '" + text.substring(0, start) + "'");
	}
	
	static void expectStartsWith(String text, String prefix, int toffset) {
		if ( !startsWith(text, prefix, toffset) ) {
			parseError(text, toffset, prefix);
		}
	}
	
	/**
	 * like {@link String#startsWith(String, int)}, but converts both strings to {@link Character#toLowerCase(char)
	 * lower case}
	 * 
	 * @param text   the text in which should be looked
	 * @param prefix the prefix.
	 * @param tOff   where to begin looking in text.
	 * 
	 * @return {@code true} if the character sequence represented by the <code>prefix</code> argument is a prefix of the
	 *             {@link String#toLowerCase() lower case} substring of the <code>text</code> argument object starting
	 *             at index {@code tOff}; {@code false} otherwise. The result is {@code false} if {@code tOff} is
	 *             negative or greater than the length of this {@code String} object; otherwise the result is the same
	 *             as the result of the expression
	 *             <code>text.{@link String#substring(int) substring(tOff)}.{@link String#toLowerCase() toLowerCase()}.{@link String#startsWith(String) startsWith(prefix)}</code>
	 * 			
	 * @see String#startsWith(String)
	 * @see String#toLowerCase()
	 * @see Character#toLowerCase(char)
	 */
	public static boolean startsWith(String text, String prefix, int tOff) {
		if ( text.startsWith(prefix, tOff) ) {
			return true;
		}
		if ( tOff > text.length() - prefix.length() ) {
			return false;
		}
		for (int i = 0, l = prefix.length(); i < l; i++) {
			if ( Character.toLowerCase(text.charAt(tOff + i)) != prefix.charAt(i) ) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * parses a number:
	 * <ul>
	 * <li>if the number starts with <code>0x</code>, the rest is parsed with {@link Integer#parseInt(String, int)
	 * Integer.parseInt(str, 16)}</li>
	 * <li>if the number starts with <code>0b</code>, the rest is parsed with {@link Integer#parseInt(String, int)
	 * Integer.parseInt(str, 2)}</li>
	 * <li>if the number starts with <code>0</code>, but not <code>0x</code> and not <code>0b</code> the string is
	 * parsed with {@link Integer#parseInt(String, int) Integer.parseInt(str, 8)}</li>
	 * <li>otherwise the string is parsed with {@link Integer#parseInt(String, int) Integer.parseInt(str, 10)}</li>
	 * </ul>
	 * 
	 * @param str the string which represents a number
	 * 
	 * @return the result of the parsing
	 */
	public static int parseNum(String str) {
		if ( str.charAt(0) == '0' ) {
			if ( str.length() == 1 ) {
				return 0;
			}
			switch ( str.charAt(1) ) {
			case 'x', 'X':
				return Integer.parseInt(str.substring(2), 16);
			case 'b', 'B':
				return Integer.parseInt(str.substring(2), 2);
			default:
				return Integer.parseInt(str, 8);
			}
		}
		return Integer.parseInt(str, 10);
	}
	
	/**
	 * parses a floating point number:
	 * <ul>
	 * <li>if the number contains a <code>.</code>, the number is parsed with {@link Float#parseFloat(String)}</li>
	 * <li>if the number starts with <code>0x</code>, the rest is parsed with (float)
	 * {@link Integer#parseInt(String, int) Integer.parseInt(str, 16)}</code></li>
	 * <li>if the number starts with <code>0b</code>, the rest is parsed with (float)
	 * {@link Integer#parseInt(String, int) Integer.parseInt(str, 2)}</code></li>
	 * <li>if the number starts with <code>0</code>, but not <code>0x</code> and not <code>0b</code> the string is
	 * parsed with <code>(float) {@link Integer#parseInt(String, int) Integer.parseInt(str, 8)}</code></li>
	 * <li>otherwise the string is parsed with {@link Float#parseFloat(String)}</li>
	 * </ul>
	 * 
	 * @param str the string which represents a number
	 * 
	 * @return the result of the parsing
	 */
	public static float parseFpNum(String str) {
		if ( str.indexOf('.') != -1 ) {
			return Float.parseFloat(str);
		}
		if ( str.charAt(0) == '0' ) {
			if ( str.length() == 1 ) {
				return 0;
			}
			switch ( str.charAt(1) ) {
			case 'x', 'X':
				return Integer.parseInt(str.substring(2), 16);
			case 'b', 'B':
				return Integer.parseInt(str.substring(2), 2);
			default:
				return Integer.parseInt(str, 8);
			}
		}
		return Float.parseFloat(str);
	}
	
}
