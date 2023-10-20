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
import java.awt.Dimension;

/**
 * this interface specifies how to much a one dimensional area should be filled.<br>
 * there are four {@link SimpleFillMode} constants:
 * <ul>
 * <li>{@link #FILL_COMPLETLY}: fills the complete area</li>
 * <li>{@link #FILL_MAXIMUM}: fills as much as {@link Component#getMaximumSize()} allows</li>
 * <li>{@link #FILL_MINIMUM}: fills as much as {@link Component#getMinimumSize()} needs</li>
 * <li>{@link #FILL_PREFERRED}: fills as much as {@link Component#getPreferredSize()} requests</li>
 * </ul>
 * they directly implement the {@link FillMode} interface and thus they can directly be used.<br>
 * if needed {@link ComplexFillMode} allows to combine a {@link SimpleFillMode} with a multiplicator<br>
 * note that depending on the used {@link SimpleFillMode} there are restrictions on the multiplicator:
 * <ul>
 * <li>{@link #FILL_COMPLETLY}: must be between {@code 1} and {@code 0}</li>
 * <li>{@link #FILL_MAXIMUM}: must be between {@code 1} and {@code 0}</li>
 * <li>{@link #FILL_MINIMUM}: must be at least {@code 1}</li>
 * <li>{@link #FILL_PREFERRED}: must be over {@code 0}</li>
 * </ul>
 * if that you need a more complex multiplicator you have to write your own implementation of {@link FillMode}
 */
public interface FillMode {
	
	/**
	 * fills as much as {@link Component#getMinimumSize()} needs
	 * <p>
	 * when used with {@link ComplexFillMode} or {@link #fillMode(float, SimpleFillMode)} {@code mul} must be at least
	 * {@code 1}
	 */
	public static final SimpleFillMode FILL_MINIMUM   = SimpleFillMode.FILL_MINIMUM;
	/**
	 * fills as much as {@link Component#getPreferredSize()} requests
	 * <p>
	 * when used with {@link ComplexFillMode} or {@link #fillMode(float, SimpleFillMode)} {@code mul} must be over
	 * {@code 0}
	 */
	public static final SimpleFillMode FILL_PREFERRED = SimpleFillMode.FILL_PREFERRED;
	/**
	 * fills as much as {@link Component#getMaximumSize()} allows
	 * <p>
	 * when used with {@link ComplexFillMode} or {@link #fillMode(float, SimpleFillMode)} {@code mul} must be between
	 * {@code 1} and {@code 0}
	 */
	public static final SimpleFillMode FILL_MAXIMUM   = SimpleFillMode.FILL_MAXIMUM;
	/**
	 * fills the complete area
	 * <p>
	 * when used with {@link ComplexFillMode} or {@link #fillMode(float, SimpleFillMode)} {@code mul} must be between
	 * {@code 1} and {@code 0}
	 */
	public static final SimpleFillMode FILL_COMPLETLY = SimpleFillMode.FILL_COMPLETLY;
	
	int size(Component comp, CompInfo info, int maxWidth, int maxHeigth, boolean width);
	
	/**
	 * combines a multiplicator with a {@link SimpleFillMode}
	 * <p>
	 * if {@code mul} is {@code 1f} the {@link SimpleFillMode} is just returned<br>
	 * otherwise a new {@link ComplexFillMode} is created by using
	 * {@link ComplexFillMode#ComplexFillMode(SimpleFillMode, float) new ComplexFillMode(mode, mul)} and then returned
	 * 
	 * @param mul  the multiplicator
	 * @param mode the {@link SimpleFillMode}
	 * 
	 * @return the combined {@link FillMode}
	 */
	public static FillMode fillMode(float mul, SimpleFillMode mode) {
		if ( mode == null ) throw new NullPointerException("the simple fill-mode is null");
		if ( mul == 1f ) return mode;
		return new ComplexFillMode(mode, mul);
	}
	
	/**
	 * this {@code enum} implements the {@link FillMode} interface
	 * <p>
	 * the first three constants ({@link #FILL_MINIMUM}, {@link #FILL_PREFERRED}, {@link #FILL_MAXIMUM}) use the
	 * {@link Component#getMinimumSize() minimum}, {@link Component#getPreferredSize() preferred} or
	 * {@link Component#getMaximumSize() maximum} size to get the wanted size of the component.<br>
	 * the last constant {@link #FILL_COMPLETLY} tells the layout manager to fill the entire block with the component
	 */
	public static enum SimpleFillMode implements FillMode {
		
		/** @see FillMode#FILL_MINIMUM */
		FILL_MINIMUM,
		
		/** @see FillMode#FILL_PREFERRED */
		FILL_PREFERRED,
		
		/** @see FillMode#FILL_MAXIMUM */
		FILL_MAXIMUM,
		
		/** @see FillMode#FILL_COMPLETLY */
		FILL_COMPLETLY,
		
		;
		
		@Override
		public int size(Component comp, CompInfo info, int maxWidth, int maxHeigth, boolean width) {
			Dimension dim;
			switch ( this ) {
			case FILL_COMPLETLY -> {
				if ( width ) return maxWidth;
				return maxHeigth;
			}
			case FILL_MAXIMUM -> dim = comp.getMaximumSize();
			case FILL_MINIMUM -> dim = comp.getMinimumSize();
			case FILL_PREFERRED -> dim = comp.getPreferredSize();
			default -> throw new AssertionError("unknown SimpleFillMode: " + name());
			};
			if ( width ) return dim.width;
			return dim.height;
		}
		
		/** {@inheritDoc} */
		@Override
		public String toString() {
			return switch ( this ) {
			case FILL_PREFERRED -> "preferred";
			case FILL_MINIMUM -> "minimum";
			case FILL_MAXIMUM -> "maximum";
			case FILL_COMPLETLY -> "completly";
			};
		}
		
	}
	
	public static final class ComplexFillMode implements FillMode {
		
		SimpleFillMode type;
		float          mul;
		
		/**
		 * @param type
		 * @param mul
		 */
		public ComplexFillMode(SimpleFillMode type, float mul) {
			set(type, mul);
		}
		
		/**
		 * returns the multiplicator of this fill mode
		 * 
		 * @return the multiplicator of this fill mode
		 */
		public float mul() {
			return this.mul;
		}
		
		@Override
		public int size(Component comp, CompInfo info, int maxWidth, int maxHeigth, boolean width) {
			return (int) ( this.mul * type.size(comp, info, maxWidth, maxHeigth, width) );
		}
		
		public void set(SimpleFillMode type, float mul) {
			switch ( type ) {
			case FILL_COMPLETLY, FILL_MAXIMUM:
				if ( !( mul <= 1f ) || mul < 0f ) { // NOSONAR also catch NaN
					throw new IllegalArgumentException(
						"invalid multiplicator: " + type + " only supports multiplicator from 0 to 1. mul=" + mul);
				}
				break;
			case FILL_MINIMUM:
				if ( !( mul >= 1f ) ) { // NOSONAR also catch NaN
					throw new IllegalArgumentException(
						"invalid multiplicator: minimum only supports multiplicator greather or equal than 1. mul="
							+ mul);
				}
				break;
			case FILL_PREFERRED:
				if ( !( mul > 0f ) ) { // NOSONAR also catch NaN
					throw new IllegalArgumentException(
						"invalid multiplicator: preferred only supports multiplicator greather than 0. mul=" + mul);
				}
				break;
			}
			this.type = type;
			this.mul = mul;
		}
		
		/** {@inheritDoc} */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Float.floatToRawIntBits(this.mul);
			result = prime * result + ( ( this.type == null ) ? 0 : this.type.hashCode() );
			return result;
		}
		
		/** {@inheritDoc} */
		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) { return true; }
			if ( !( obj instanceof ComplexFillMode ) ) { return false; }
			ComplexFillMode other = (ComplexFillMode) obj;
			if ( this.mul != other.mul ) { return false; }
			return this.type == other.type;
		}
		
		/** {@inheritDoc} */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("FillModeCls [type=");
			builder.append(this.type);
			builder.append(", mul=");
			builder.append(this.mul);
			builder.append("]");
			return builder.toString();
		}
		
	}
	
}

