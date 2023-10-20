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

/**
 * a {@link BlockInfo} instance stores the {@link #max() maximum} and {@link #min() minimum} size of a single row/column
 * <p>
 * to create {@link BlockInfo} instances
 * <ul>
 * <li>{@link #parseArr(String)} can be used to create an array of instances</li>
 * <li>{@link #parse(String)} can be used to create an instance</li>
 * <li>{@link #BlockInfo(int, int)} can be used to create an instance</li>
 * </ul>
 * note that {@link BlockInfo} instances are modifiable (the {@link #max() maximum} and {@link #min() minimum} size can
 * be set with the {@link #set(int, int)} method)
 * 
 * @author Patrick Hechler
 */
public class BlockInfo {
	
	private static final String  GROW            = "grow";
	private static final String  NUM             = "(" + CompInfo.NUMBER + "(px)?)";
	private static final String  OPT_COMMA_SEP   = "(,\\s*|\\s+,?\\s*)";
	private static final Pattern P_OPT_COMMA_SEP = Pattern.compile(OPT_COMMA_SEP, Pattern.CASE_INSENSITIVE);
	
	private static final String  CONTENT = GROW + "|" + NUM + "(" + OPT_COMMA_SEP + "(" + GROW + "|" + NUM + "))?";
	private static final String  BLOCK   = "\\[\\s*(" + CONTENT + ")\\s*\\]";
	private static final Pattern P_BLOCK = Pattern.compile(BLOCK, Pattern.CASE_INSENSITIVE);
	
	private static final String  FULL   = "(" + BLOCK + "\\s*)*";                         // wow only 727 chars
	private static final Pattern P_FULL = Pattern.compile(FULL, Pattern.CASE_INSENSITIVE);
	
	/**
	 * parses the given string to an array of {@link BlockInfo} instances
	 * <p>
	 * each block starts with an {@code '['} and ends with an {@code ']'}<br>
	 * between those braces is a string according to {@link #parse(String)}.<br>
	 * between two such blocks there can be whitespace.
	 * <p>
	 * this means the returned array has a length equal to the amount of {@code '['} (or {@code ']'}) characters in
	 * {@code str}<br>
	 * 
	 * @param str the string to be parsed
	 * 
	 * @return the result of the parsing operation
	 */
	public static BlockInfo[] parseArr(String str) {
		str = str.trim();
		if ( !P_FULL.matcher(str).matches() ) {
			throw new IllegalArgumentException("invalid input: '" + str + "' (regex: '" + FULL + "')");
		}
		Matcher matcher = P_BLOCK.matcher(str);
		if ( !matcher.find() ) {
			return PatGridLayout.EMPTY_BLOCK_INFOS;
		}
		List<BlockInfo> result = new ArrayList<>();
		do { // first find already invoked
			result.add(parse(matcher.group(1)));
		} while ( matcher.find() );
		return result.toArray(new BlockInfo[result.size()]);
	}
	
	/**
	 * parses a single block
	 * <p>
	 * {@code str} has to be formatted according to this specification:<br>
	 * the case of {@code str} is ignored.<br>
	 * at first the leading and trailing whitespace (if any) is removed from {@code str} ({@link String#strip()})
	 * <ul>
	 * <li>{@code str} is {@link String#equalsIgnoreCase(String) equals} to {@code "grow"}</li>
	 * <li>{@code str} is a number acceptable by {@link CompInfo#parseNum(String)} with an optional {@code "px"}
	 * appended to the end</li>
	 * <li>{@code str} has the following format:
	 * <ol>
	 * <li>a number acceptable by {@link CompInfo#parseNum(String)} with an optional {@code "px"} appended to the
	 * end</li>
	 * <li>one of the following:
	 * <ul>
	 * <li>optional whitespace followed by a comma ({@code ','}) followed by optional whitespace</li>
	 * <li>whitespace</li>
	 * </ul>
	 * </li>
	 * <li>one of the following:
	 * <ul>
	 * <li>a number acceptable by {@link CompInfo#parseNum(String)} with an optional {@code "px"} appended to the
	 * end</li>
	 * <li>a sequence {@link String#equalsIgnoreCase(String) equal} to {@code "grow"}</li>
	 * </ul>
	 * </li>
	 * </ol>
	 * </li>
	 * </ul>
	 * 
	 * @param str the text to be parsed
	 * 
	 * @return the result of the operation
	 */
	public static BlockInfo parse(String str) {
		str = str.strip();
		BlockInfo inf = new BlockInfo();
		if ( str.equalsIgnoreCase("grow") ) {
			inf.max = inf.min = DYNAMIC;
			return inf;
		}
		Matcher sepMat = P_OPT_COMMA_SEP.matcher(str);
		boolean sep = sepMat.find();
		switch ( str.charAt(firstEnd(str, sepMat, sep) - 1) ) {
		case 'x':
			if ( str.charAt(firstEnd(str, sepMat, sep) - 2) != 'p' ) {
				CompInfo.parseError(str, firstEnd(str, sepMat, sep) - 2, "px");
			}
			inf.min = CompInfo.parseNum(str.substring(0, firstEnd(str, sepMat, sep) - 2));
			break;
		default:
			inf.min = CompInfo.parseNum(str.substring(0, firstEnd(str, sepMat, sep)));
			break;
		}
		if ( sep ) {
			if ( CompInfo.startsWith(str, "grow", str.length() - 4) ) {
				inf.max = DYNAMIC;
			} else {
				switch ( str.charAt(str.length() - 1) ) {
				case 'x':
					if ( str.charAt(str.length() - 2) != 'p' ) {
						CompInfo.parseError(str, str.length() - 2, "px");
					}
					inf.max = CompInfo.parseNum(str.substring(sepMat.end(), str.length() - 2));
					break;
				default:
					inf.max = CompInfo.parseNum(str.substring(sepMat.end()));
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
		final int prime = 31;
		int result = 1;
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
