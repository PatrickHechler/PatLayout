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
	
	static final String NUM = "(0(x[0-9A-F]+|b[01]+|[0-7]*)|[1-9][0-9]*)";
	
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
	
	private static final String  ALIGNMENTS_HORIZONTS   = "(" + ALIGN_LEFT + "|" + ALIGN_RIGHT + "|"
		+ ALIGN_HORIZONTAL_MID + ")";
	private static final Pattern P_ALIGNMENTS_HORIZONTS = Pattern.compile(ALIGNMENTS_HORIZONTS,
		Pattern.CASE_INSENSITIVE);
	
	private static final String  ALIGNMENTS_VERTICALS   = "(" + ALIGN_TOP + "|" + ALIGN_BOTTOM + "|"
		+ ALIGN_VERTICAL_MID + ")";
	private static final Pattern P_ALIGNMENTS_VERTICALS = Pattern.compile(ALIGNMENTS_VERTICALS,
		Pattern.CASE_INSENSITIVE);
	
	private static final String ALIGNMENTS = "(" + ALIGN_CENTER + "|" + ALIGNMENTS_HORIZONTS + "(" + OPT_COMMA_SEP
		+ ALIGNMENTS_VERTICALS + ")?|" + ALIGNMENTS_VERTICALS + "(" + OPT_COMMA_SEP + ALIGNMENTS_HORIZONTS + ")?"
		+ ")";
	
	private static final String POS = "(" + A_B + "(\\s+" + A_B + ")?)";
	
	private static final String  FULL   = POS + "(" + OPT_COMMA_SEP + ALIGNMENTS + ")?";
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
		this(x, y, 1, 1, 0.5f, 0.5f);
	}
	
	public CompInfo(int x, int y, int w, int h) {
		this(x, y, w, h, 0.5f, 0.5f);
	}
	
	public CompInfo(int x, int y, float alignx, float aligny) {
		this(x, y, 1, 1, alignx, aligny);
	}
	
	public CompInfo(int x, int y, int w, int h, float alignx, float aligny) {
		this(x, y, w, h, alignx, aligny, FillMode.FILL_PREFERRED, FillMode.FILL_PREFERRED);
	}
	
	public CompInfo(int x, int y, int w, int h, float alignx, float aligny, FillMode wideMode,
		FillMode heightMode) {
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(alignx);
		result = prime * result + Float.floatToIntBits(aligny);
		result = prime * result + h;
		result = prime * result + ( ( heightMode == null ) ? 0 : heightMode.hashCode() );
		result = prime * result + w;
		result = prime * result + ( ( wideMode == null ) ? 0 : wideMode.hashCode() );
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) { return true; }
		if ( !( obj instanceof CompInfo ) ) { return false; }
		CompInfo other = (CompInfo) obj;
		if ( Float.floatToIntBits(alignx) != Float.floatToIntBits(other.alignx) ) { return false; }
		if ( Float.floatToIntBits(aligny) != Float.floatToIntBits(other.aligny) ) { return false; }
		if ( h != other.h ) { return false; }
		if ( heightMode == null ) {
			if ( other.heightMode != null ) { return false; }
		} else if ( !heightMode.equals(other.heightMode) ) { return false; }
		if ( w != other.w ) { return false; }
		if ( wideMode == null ) {
			if ( other.wideMode != null ) { return false; }
		} else if ( !wideMode.equals(other.wideMode) ) { return false; }
		if ( x != other.x ) { return false; }
		if ( y != other.y ) { return false; }
		return true;
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
		builder.append(", wideMode=");
		builder.append(wideMode);
		builder.append(", heightMode=");
		builder.append(heightMode);
		builder.append(", alignx=");
		builder.append(alignx);
		builder.append(", aligny=");
		builder.append(aligny);
		builder.append("]");
		return builder.toString();
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
		inf.heightMode = FillMode.FILL_MAXIMUM;
		inf.wideMode = FillMode.FILL_MAXIMUM;
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
	
	public static void parseError(String text, int start, String expected) throws AssertionError {
		throw new AssertionError(
			"illegal char: " + text.charAt(start) + " index: " + start + " expected: " + expected
				+ " text: unparsed: '" + text.substring(start) + "' parsed: '" + text.substring(0, start) + "'");
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
	
	public static void expectStartsWith(String text, String prefix, int toffset) {
		if ( !startsWith(text, prefix, toffset) ) {
			parseError(text, toffset, prefix);
		}
	}
	
	public static boolean startsWith(String text, String prefix, int toffset) {
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
	
	public static int parseNum(String txt) {
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
