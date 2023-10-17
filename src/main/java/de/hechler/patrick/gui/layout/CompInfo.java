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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompInfo {
	
	static final String NUM = "(0(x[0-9A-F]+|b[01]+|[0-7]*)|[1-9][0-9]*)";// NOSONAR
	
	private static final Pattern P_NUM = Pattern.compile(NUM, Pattern.CASE_INSENSITIVE);
	
	private static final String A_B = NUM + "\\s+" + NUM;
	
	static final String         OPT_COMMA_SEP = "(\\s+(,\\s*)?|,\\s*)";
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
	
	private static final String  ALIGNMENTS_HORIZONTS   =
		"(" + ALIGN_LEFT + "|" + ALIGN_RIGHT + "|" + ALIGN_HORIZONTAL_MID + ")";
	private static final Pattern P_ALIGNMENTS_HORIZONTS =
		Pattern.compile(ALIGNMENTS_HORIZONTS, Pattern.CASE_INSENSITIVE);
	
	private static final String  ALIGNMENTS_VERTICALS   =
		"(" + ALIGN_TOP + "|" + ALIGN_BOTTOM + "|" + ALIGN_VERTICAL_MID + ")";
	private static final Pattern P_ALIGNMENTS_VERTICALS =
		Pattern.compile(ALIGNMENTS_VERTICALS, Pattern.CASE_INSENSITIVE);
	
	private static final String ALIGNMENTS = "(" + ALIGN_CENTER + "|" + ALIGNMENTS_HORIZONTS + "(" + OPT_COMMA_SEP
		+ ALIGNMENTS_VERTICALS + ")?|" + ALIGNMENTS_VERTICALS + "(" + OPT_COMMA_SEP + ALIGNMENTS_HORIZONTS + ")?" + ")";
	
	private static final String POS = "(" + A_B + "(\\s+" + A_B + ")?)";
	
	private static final String PREFIX_MIN       = "m(in(imum)?)?";
	private static final String PREFIX_PREFERRED = "p(ref(erred)?)?";
	private static final String PREFIX_MAX       = "max(imum)?|x";
	private static final String PREFIX_COMPLETLY = "t(otal)?|c(omplete|ompletly)?";
	private static final String PREFIX           =
		"((" + PREFIX_MIN + "|" + PREFIX_PREFERRED + "|" + PREFIX_MAX + "|" + PREFIX_COMPLETLY + ")\\s*)?";
	
	private static final String  FULL   = PREFIX + POS + "(" + OPT_COMMA_SEP + ALIGNMENTS + ")?";
	private static final Pattern P_FULL = Pattern.compile(FULL, Pattern.CASE_INSENSITIVE);
	
	int      x;
	int      y;
	int      w;
	int      h;
	FillMode wideMode;
	FillMode heightMode;
	float    alignx;
	float    aligny;
	
	private CompInfo() {
	}
	
	public CompInfo(int x, int y) {
		this(x, y, 1, 1, 0f, 0f);
	}
	
	public CompInfo(int x, int y, int w, int h) {
		this(x, y, w, h, 0f, 0f);
	}
	
	public CompInfo(int x, int y, float alignx, float aligny) {
		this(x, y, 1, 1, alignx, aligny);
	}
	
	public CompInfo(int x, int y, int w, int h, float alignx, float aligny) {
		this(x, y, w, h, alignx, aligny, FillMode.FILL_MAXIMUM, FillMode.FILL_MAXIMUM);
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
		return this.x;
	}
	
	public void x(int x) {
		if ( x < 0 ) {
			throw new IllegalArgumentException("negative x: " + x);
		}
		this.x = x;
	}
	
	public int y() {
		return this.y;
	}
	
	public void y(int y) {
		if ( y < 0 ) {
			throw new IllegalArgumentException("negative y: " + y);
		}
		this.y = y;
	}
	
	public int width() {
		return this.w;
	}
	
	public void width(int w) {
		if ( w <= 0 ) {
			throw new IllegalArgumentException("negative width: " + w);
		}
		this.w = w;
	}
	
	public int height() {
		return this.h;
	}
	
	public void height(int h) {
		if ( h <= 0 ) {
			throw new IllegalArgumentException("negative height: " + h);
		}
		this.h = h;
	}
	
	public float alignx() {
		return this.alignx;
	}
	
	public void alignx(float alignx) {
		if ( !( alignx >= 0f ) || alignx > 1f ) {// NOSONAR // `!(NaN >= 0)` is true
			throw new IllegalArgumentException("x-alignment out of bounds: " + alignx);
		}
		this.alignx = alignx;
	}
	
	public float aligny() {
		return this.aligny;
	}
	
	public void aligny(float aligny) {
		if ( !( aligny >= 0f ) || aligny > 1f ) {// NOSONAR // `!(NaN >= 0)` is true
			throw new IllegalArgumentException("y-alignment out of bounds: " + aligny);
		}
		this.aligny = aligny;
	}
	
	public FillMode heightMode() {
		return this.heightMode;
	}
	
	public void heightMode(FillMode heightMode) {
		if ( heightMode == null ) throw new NullPointerException("heightMode is null");
		this.heightMode = heightMode;
	}
	
	public FillMode wideMode() {
		return this.wideMode;
	}
	
	public void wideMode(FillMode wideMode) {
		if ( wideMode == null ) throw new NullPointerException("wideMode is null");
		this.wideMode = wideMode;
	}
	
	@Override
	public int hashCode() {
		final int prime  = 31;
		int       result = 1;
		result = prime * result + Float.floatToIntBits(this.alignx);
		result = prime * result + Float.floatToIntBits(this.aligny);
		result = prime * result + this.h;
		result = prime * result + this.heightMode.hashCode();
		result = prime * result + this.w;
		result = prime * result + this.wideMode.hashCode();
		result = prime * result + this.x;
		result = prime * result + this.y;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) { return true; }
		if ( !( obj instanceof CompInfo ) ) { return false; }
		CompInfo other = (CompInfo) obj;
		if ( Float.floatToIntBits(this.alignx) != Float.floatToIntBits(other.alignx) ) { return false; }
		if ( Float.floatToIntBits(this.aligny) != Float.floatToIntBits(other.aligny) ) { return false; }
		if ( this.h != other.h ) { return false; }
		if ( !this.heightMode.equals(other.heightMode) ) { return false; }
		if ( this.w != other.w ) { return false; }
		if ( !this.wideMode.equals(other.wideMode) ) { return false; }
		if ( this.x != other.x ) { return false; }
		return this.y == other.y;
	}
	
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
		builder.append(this.wideMode);
		builder.append(", heightMode=");
		builder.append(this.heightMode);
		builder.append(", alignx=");
		builder.append(this.alignx);
		builder.append(", aligny=");
		builder.append(this.aligny);
		builder.append("]");
		return builder.toString();
	}
	
	public static CompInfo parse(String text) {
		text = text.trim();
		if ( !P_FULL.matcher(text).matches() ) {
			throw new IllegalArgumentException("invalid input: '" + text + "' (regex: '" + FULL + "')");
		}
		CompInfo inf     = new CompInfo();
		Matcher  matcher = P_NUM.matcher(text);
		matcher.find(); // must find exact two or four matches
		parsePrefix(text, inf, matcher.start());
		inf.x = parseNum(matcher.group(0));
		matcher.find();
		inf.y = parseNum(matcher.group(0));
		int numEnd = matcher.end();
		if ( matcher.find() ) {
			inf.w = parseNum(matcher.group(0));
			matcher.find();
			inf.h  = parseNum(matcher.group(0));
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
	
	private static void parsePrefix(String text, CompInfo inf, int start) throws AssertionError {
		if ( start == 0 ) {
			inf.heightMode = FillMode.FILL_MAXIMUM;
			inf.wideMode   = FillMode.FILL_MAXIMUM;
			return;
		}
		switch ( text.charAt(0) ) {
		case 'm':
			if ( start != 1 && !Character.isWhitespace(text.charAt(1)) ) {
				switch ( text.charAt(1) ) {
				case 'i':
					inf.heightMode = FillMode.FILL_MINIMUM;
					inf.wideMode = FillMode.FILL_MINIMUM;
					if ( text.charAt(2) != 'n' ) {
						parseError(text, 2, "n");
					}
					if ( start != 3 && !Character.isWhitespace(text.charAt(3)) ) {
						expectStartsWith(text, "imum", 3);
					}
					break;
				case 'a':
					inf.heightMode = FillMode.FILL_MAXIMUM;
					inf.wideMode = FillMode.FILL_MAXIMUM;
					if ( text.charAt(2) != 'x' ) {
						parseError(text, 2, "x");
					}
					if ( start != 3 && !Character.isWhitespace(text.charAt(3)) ) {
						expectStartsWith(text, "imum", 3);
					}
					break;
				default:
					parseError(text, 1, "in(imum)?|ax(imum)?");
				}
			} else {
				inf.heightMode = FillMode.FILL_MINIMUM;
				inf.wideMode   = FillMode.FILL_MINIMUM;
			}
			break;
		case 'p':
			inf.heightMode = FillMode.FILL_PREFERRED;
			inf.wideMode = FillMode.FILL_PREFERRED;
			if ( start != 1 && !Character.isWhitespace(text.charAt(1)) ) {
				expectStartsWith(text, "ref", 1);
				if ( start != 4 && !Character.isWhitespace(text.charAt(4)) ) {
					expectStartsWith(text, "erred", 4);
				}
			}
			break;
		case 'x':
			inf.heightMode = FillMode.FILL_MAXIMUM;
			inf.wideMode = FillMode.FILL_MAXIMUM;
			if ( start != 1 && !Character.isWhitespace(text.charAt(1)) ) {
				parseError(text, 1, " ");
			}
			break;
		case 't':
			inf.heightMode = FillMode.FILL_COMPLETLY;
			inf.wideMode = FillMode.FILL_COMPLETLY;
			if ( start != 1 && !Character.isWhitespace(text.charAt(1)) ) {
				expectStartsWith(text, "otal", 1);
			}
			break;
		case 'c':
			inf.heightMode = FillMode.FILL_COMPLETLY;
			inf.wideMode = FillMode.FILL_COMPLETLY;
			if ( start != 1 && !Character.isWhitespace(text.charAt(1)) ) {
				expectStartsWith(text, "omplet", 1);
				switch ( text.charAt(7) ) {
				case 'e':
					if ( start != 8 && !Character.isWhitespace(text.charAt(8)) ) {
						parseError(text, 8, " ");
					}
					break;
				case 'l':
					if ( text.charAt(8) != 'y' ) {
						parseError(text, 8, "y");
					}
					if ( start != 9 && !Character.isWhitespace(text.charAt(9)) ) {
						parseError(text, 9, " ");
					}
					break;
				default:
					parseError(text, 7, "e|ly");
				}
			}
			break;
		default:
			parseError(text, 0, PREFIX);
		}
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
				switch ( text.charAt(start + 8) ) {// NOSONAR
				case ' ', '-', '_' -> start += 7;
				default -> start += 6;
				}
				//$FALL-THROUGH$
			case ' ', '-', '_':
				c = text.charAt(++start + 1);
				//$FALL-THROUGH$
			case 'm', 'M', 'c', 'C':
				switch ( c ) {
				case 'm', 'M':
					expectStartsWith(text, "id", start + 2);
					return;
				case 'c', 'C':
					expectStartsWith(text, "enter", start + 2);
					return;
				default:
					parseError(text, start + 1, "mid|center");// NOSONAR
				}
				//$FALL-THROUGH$ // not possible, either return or parseError
			default:
				parseError(text, start, "(orizontal)?( |-|_)?(mid|center)");
			}
			//$FALL-THROUGH$ // not possible, either return or parseError
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
				switch ( text.charAt(start + 10) ) { // NOSONAR
				case ' ', '-', '_' -> start += 9; // skip only "orizontal" (not the "h")
				default -> start += 8; // same but reduce by one because of fall-thoug to [ -_]
				}
				//$FALL-THROUGH$
			case ' ', '-', '_':
				c = text.charAt(++start + 1);
				//$FALL-THROUGH$
			case 'm', 'M', 'c', 'C':
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
				//$FALL-THROUGH$ // not possible, either return or parseError
			default:
				parseError(text, start, "(orizontal)?( |-|_)?(mid|center)");
			}
			//$FALL-THROUGH$ // not possible, either return or parseError
		case 'l', 'L':
			expectStartsWith(text, "eft", start + 1);
			inf.alignx = 0f;
			return;
		case 'r', 'R':
			expectStartsWith(text, "ight", start + 1);
			inf.alignx = 1f;
			return;
		default:
			parseError(text, start, "left|right|h(orizontal)?( |-|_)?(mid|center)");
		}
	}
	
	static void parseError(String text, int start, String expected) throws AssertionError {
		throw new AssertionError("illegal char: " + text.charAt(start) + " index: " + start + " expected: " + expected
			+ " text: unparsed: '" + text.substring(start) + "' parsed: '" + text.substring(0, start) + "'");
	}
	
	private static int skipAlign(String text, int start) {
		if ( startsWith(text, "align", start) ) {
			switch ( text.charAt(start + 5) ) {// NOSONAR
			case ' ', '-', '_':
				return start + 6;
			default:
				return start + 5;
			}
		}
		return start;
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
	
}
