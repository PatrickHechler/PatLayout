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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockInfo {
	
	// [grow]
	// [size]
	// [min_size,grow]
	// [min_size,max_size]
	
	private static final String  GROW            = "grow";
	private static final String  NUM             = "(" + CompInfo.NUMBER + "(px)?)";
	private static final String  OPT_COMMA_SEP   = "(,\\s*|\\s+,?\\s*)";
	private static final Pattern P_OPT_COMMA_SEP = Pattern.compile(OPT_COMMA_SEP, Pattern.CASE_INSENSITIVE);
	
	private static final String  CONTENT = GROW + "|" + NUM + "(" + OPT_COMMA_SEP + "(" + GROW + "|" + NUM + "))?";
	private static final String  BLOCK   = "\\[\\s*(" + CONTENT + ")\\s*\\]";
	private static final Pattern P_BLOCK = Pattern.compile(BLOCK, Pattern.CASE_INSENSITIVE);
	
	private static final String  FULL   = "(" + BLOCK + "\\s*)*"; // wow only 727 chars
	private static final Pattern P_FULL = Pattern.compile(FULL, Pattern.CASE_INSENSITIVE);
	
	public static BlockInfo[] parseArr(String text) {
		text = text.trim();
		if ( !P_FULL.matcher(text).matches() ) {
			throw new IllegalArgumentException("invalid input: '" + text + "' (regex: '" + FULL + "')");
		}
		Matcher matcher = P_BLOCK.matcher(text);
		if ( !matcher.find() ) {
			return PatGridLayout.EMPTY_BLOCK_INFOS;
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
			return inf;
		}
		Matcher sepMat = P_OPT_COMMA_SEP.matcher(text);
		boolean sep    = sepMat.find();
		switch ( text.charAt(firstEnd(text, sepMat, sep) - 1) ) {
		case 'x':
			if ( text.charAt(firstEnd(text, sepMat, sep) - 2) != 'p' ) {
				CompInfo.parseError(text, firstEnd(text, sepMat, sep) - 2, "px");
			}
			inf.min = CompInfo.parseNum(text.substring(0, firstEnd(text, sepMat, sep) - 2));
			break;
		default:
			inf.min = CompInfo.parseNum(text.substring(0, firstEnd(text, sepMat, sep)));
			break;
		}
		if ( sep ) {
			if ( CompInfo.startsWith(text, "grow", text.length() - 4) ) {
				inf.max = DYNAMIC;
			} else {
				switch ( text.charAt(text.length() - 1) ) {
				case 'x':
					if ( text.charAt(text.length() - 2) != 'p' ) {
						CompInfo.parseError(text, text.length() - 2, "px");
					}
					inf.max = CompInfo.parseNum(text.substring(sepMat.end(), text.length() - 2));
					break;
				default:
					inf.max = CompInfo.parseNum(text.substring(sepMat.end()));
					break;
				}
			}
		} else {
			inf.max = inf.min;
		}
		inf.set(inf.min, inf.max);
		return inf;
	}
	
	private static int firstEnd(String text, Matcher m, boolean sep) {
		return sep ? m.start() : text.length();
	}
	
	/**
	 * when used for min it is like {@code 0}, when used for max it is like {@link Integer#MAX_VALUE}
	 */
	public static final int DYNAMIC  = -1;
	/**
	 * the maximum size a block can currently have (this is done to avoid overflow)
	 */
	public static final int MAX_SIZE = PatGridLayout.MAX_BLOCK_SIZE;
	
	int min;
	int max;
	
	private BlockInfo() {
	}
	
	/**
	 * creates a new {@link BlockInfo} instance with the given minimum and maximum
	 * 
	 * @param min the minimum
	 * @param max the maximum
	 */
	public BlockInfo(int min, int max) {
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
		return this.min;
	}
	
	/**
	 * get the maximum size
	 * <p>
	 * note that the layout manager may access the variable directly
	 * 
	 * @return the maximum size
	 */
	public int max() {
		return this.max;
	}
	
	/**
	 * set both minimum size and maximum size.
	 * 
	 * @implSpec the implementation currently enforces a maximum size of {@value #MAX_SIZE} for both values
	 * 
	 * @param min the minimum size
	 * @param max the maximum size
	 */
	public void set(int min, int max) {
		if ( min < -1 ) {
			throw new IllegalArgumentException("min < -1: " + min);
		}
		if ( max < min && max != -1 ) {
			throw new IllegalArgumentException("max < min: " + min + " < " + max);
		}
		this.min = check(min, 0);
		this.max = check(max, MAX_SIZE);
	}
	
	private static int check(int val, int def) {
		if ( val == -1 ) return def;
		if ( val > MAX_SIZE ) return MAX_SIZE;
		return val;
	}
	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime  = 31;
		int       result = 1;
		result = prime * result + this.max;
		result = prime * result + this.min;
		return result;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) { return true; }
		if ( !( obj instanceof BlockInfo ) ) { return false; }
		BlockInfo other = (BlockInfo) obj;
		if ( this.max != other.max ) { return false; }
		return this.min == other.min;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("BlockInfo [min=");
		if ( this.min != DYNAMIC ) {
			b.append(this.min);
		} else {
			b.append("dynamic");
		}
		b.append(", max=");
		if ( this.max != DYNAMIC ) {
			b.append(this.max);
		} else {
			b.append("dynamic");
		}
		return b.append(']').toString();
	}
	
}
