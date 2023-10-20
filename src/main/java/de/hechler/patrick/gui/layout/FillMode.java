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
 * if needed {@link MulFillMode} allows to combine a {@link SimpleFillMode} with a multiplicator<br>
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
	 * when used with {@link MulFillMode#set(FillMode, float)} or {@link #fillMode(float, FillMode)} {@code mul} must be
	 * at least {@code 1}
	 */
	public static final SimpleFillMode FILL_MINIMUM   = new SimpleFillMode.AdvancedFillModeImpl(
		SimpleFillMode.FILL_MINIMUM);
	/**
	 * fills as much as {@link Component#getPreferredSize()} requests
	 * <p>
	 * when used with {@link MulFillMode#set(FillMode, float)} or {@link #fillMode(float, FillMode)} {@code mul} must be
	 * over {@code 0}
	 */
	public static final SimpleFillMode FILL_PREFERRED = new SimpleFillMode.AdvancedFillModeImpl(
		SimpleFillMode.FILL_PREFERRED);
	/**
	 * fills as much as {@link Component#getMaximumSize()} allows
	 * <p>
	 * when used with {@link MulFillMode#set(FillMode, float)} or {@link #fillMode(float, FillMode)} {@code mul} must be
	 * between {@code 1} and {@code 0}
	 */
	public static final SimpleFillMode FILL_MAXIMUM   = new SimpleFillMode.AdvancedFillModeImpl(
		SimpleFillMode.FILL_MAXIMUM);
	/**
	 * fills the complete area
	 * <p>
	 * when used with {@link MulFillMode#set(FillMode, float)} or {@link #fillMode(float, FillMode)} {@code mul} must be
	 * between {@code 1} and {@code 0}
	 */
	public static final SimpleFillMode FILL_COMPLETLY = new SimpleFillMode(SimpleFillMode.FILL_COMPLETLY);
	
	/**
	 * return the size (width/height) the component should ideally have
	 * 
	 * @param comp      the component
	 * @param info      the info of the component
	 * @param maxWidth  the maximum width
	 * @param maxHeigth the maximum height
	 * @param width     if the width or height should be returned
	 * 
	 * @return the size (width/height) the component should ideally have
	 */
	int size(Component comp, CompInfo info, int maxWidth, int maxHeigth, boolean width);
	
	/**
	 * this extension of the fill mode can calculate both fill modes at once<br>
	 * note that this interface should only be implemented when there is a gain (either in memory or speed) when
	 * calculating both sizes at once.<br>
	 * for example {@link FillMode#FILL_MAXIMUM} implements this interface, since it has to call
	 * {@link Component#getMaximumSize()} anyway and when the other mode is also {@link FillMode#FILL_MAXIMUM} the
	 * calculation of the second value is not needed
	 */
	interface AdvancedFillMode extends FillMode {
		
		Dimension bothSizes(Component comp, CompInfo info, int maxWidth, int maxHeigth, boolean width, FillMode other);
		
	}
	
	/**
	 * combines a multiplicator with a {@link FillMode} in a new {@link MulFillMode} instance
	 * <p>
	 * if {@code mode} is an {@link AdvancedFillMode} an {@link AdvancedFillMode} will be returned
	 * <p>
	 * note that after invoking {@link MulFillMode#set(FillMode, float)} the returned instance may be a valid
	 * {@link AdvancedFillMode} instance without a backing {@link AdvancedFillMode}.<br>
	 * if that happens and {@link AdvancedFillMode#bothSizes(Component, CompInfo, int, int, boolean, FillMode)
	 * AdvancedFillMode.bothSizes(...)} is called, a new {@link Dimension} will be created and initialized with the
	 * values of both {@link #size(Component, CompInfo, int, int, boolean) size(...)} invocations<br>
	 * note that even if {@code other} is an {@link AdvancedFillMode} it's
	 * {@link AdvancedFillMode#bothSizes(Component, CompInfo, int, int, boolean, FillMode)
	 * AdvancedFillMode.bothSizes(...)} method will not be used (it is assumed that this method is only profitable when
	 * both instances are of the same/a compatible type)
	 * <p>
	 * if for some reason you really want to have a {@link SimpleFillMode} with an invalid multiplicator you can either
	 * use <code>fillMode(mul, fillMode(1f, mode))</code> or create your own {@link SimpleFillMode} (or create a
	 * wrapper).
	 * 
	 * @param mul  the multiplicator
	 * @param mode the {@link FillMode}
	 * 
	 * @return the combined {@link FillMode}
	 * 
	 * @throws NullPointerException     if {@code mode} is {@code null}
	 * @throws IllegalArgumentException if {@code mode} is a {@link SimpleFillMode} and {@code mul} is not valid for the
	 *                                      given {@code mode}
	 */
	public static MulFillMode fillMode(float mul, FillMode mode) throws NullPointerException, IllegalArgumentException {
		if ( mode == null ) throw new NullPointerException("the simple fill-mode is null");
		if ( mode instanceof AdvancedFillMode ) return new MulFillMode.AdvancedMulFillMode(mode, mul);
		return new MulFillMode(mode, mul);
	}
	
	/**
	 * this {@code class} implements the {@link FillMode} interface
	 * <p>
	 * the first three constants ({@link FillMode#FILL_MINIMUM}, {@link FillMode#FILL_PREFERRED},
	 * {@link FillMode#FILL_MAXIMUM}) use the {@link Component#getMinimumSize() minimum},
	 * {@link Component#getPreferredSize() preferred} or {@link Component#getMaximumSize() maximum} size to get the
	 * wanted size of the component.<br>
	 * the last constant {@link FillMode#FILL_COMPLETLY} tells the layout manager to fill the entire block with the
	 * component
	 */
	public static sealed class SimpleFillMode implements FillMode {
		
		private final static int FILL_COMPLETLY = 0;
		private final static int FILL_MAXIMUM   = 1;
		private final static int FILL_PREFERRED = 2;
		private final static int FILL_MINIMUM   = 3;
		
		private final int mode;
		
		private SimpleFillMode(int mode) {
			this.mode = mode;
		}
		
		private static final class AdvancedFillModeImpl extends SimpleFillMode implements AdvancedFillMode {
			
			private AdvancedFillModeImpl(int mode) {
				super(mode);
			}
			
			@Override
			public Dimension bothSizes(Component comp, CompInfo info, int maxWidth, int maxHeigth, boolean width,
				FillMode other) {
				Dimension dim;
				switch ( super.mode ) {
				case SimpleFillMode.FILL_MAXIMUM -> dim = comp.getMaximumSize();
				case SimpleFillMode.FILL_MINIMUM -> dim = comp.getMinimumSize();
				case SimpleFillMode.FILL_PREFERRED -> dim = comp.getPreferredSize();
				default -> throw new AssertionError("illegal mode: " + super.mode);
				};
				if ( other == this ) {
					return dim;
				}
				if ( other instanceof MulFillMode cfm && cfm.type == this ) {
					if ( width ) dim.height *= cfm.mul;
					else dim.width *= cfm.mul;
					return dim;
				}
				if ( width ) dim.height = other.size(comp, info, maxWidth, maxHeigth, false);
				else dim.width = other.size(comp, info, maxWidth, maxHeigth, true);
				return dim;
			}
			
		}
		
		/** {@inheritDoc} */
		@Override
		public int size(Component comp, @SuppressWarnings( "unused" ) CompInfo info, int maxWidth, int maxHeigth,
			boolean width) {
			Dimension dim;
			switch ( this.mode ) {
			case FILL_COMPLETLY -> {
				if ( width ) return maxWidth;
				return maxHeigth;
			}
			case FILL_MAXIMUM -> dim = comp.getMaximumSize();
			case FILL_MINIMUM -> dim = comp.getMinimumSize();
			case FILL_PREFERRED -> dim = comp.getPreferredSize();
			default -> throw new AssertionError("illegal mode: " + this.mode);
			};
			if ( width ) return dim.width;
			return dim.height;
		}
		
		/** {@inheritDoc} */
		@Override
		public String toString() {
			return switch ( this.mode ) {
			case FILL_PREFERRED -> "preferred";
			case FILL_MINIMUM -> "minimum";
			case FILL_MAXIMUM -> "maximum";
			case FILL_COMPLETLY -> "completly";
			default -> throw new AssertionError("illegal mode: " + this.mode);
			};
		}
		
	}
	
	/**
	 * combines a {@link FillMode} with an multiplicator
	 * 
	 * @see FillMode#fillMode(float, FillMode)
	 */
	public static sealed class MulFillMode implements FillMode {
		
		FillMode type;
		float    mul;
		
		/**
		 * creates a new {@link MulFillMode} instance with the given type
		 * 
		 * @param type the backing type
		 * @param mul  the multiplicator
		 */
		public MulFillMode(FillMode type, float mul) {
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
		
		/**
		 * extends the {@link MulFillMode} class by the {@link AdvancedFillMode} interface
		 */
		public static final class AdvancedMulFillMode extends MulFillMode implements AdvancedFillMode {
			
			/**
			 * creates a new {@link AdvancedMulFillMode} instance with the given type
			 * 
			 * @param type the backing type
			 * @param mul  the multiplicator
			 */
			public AdvancedMulFillMode(FillMode type, float mul) {
				super(type, mul);
			}
			
			/** {@inheritDoc} */
			@Override
			public Dimension bothSizes(Component comp, CompInfo info, int maxWidth, int maxHeigth, boolean width,
				FillMode other) {
				// it is possible for super.type to not be a AdvancedFillMode, because set is allowed to change that
				// also the constructor does not enforce the AdvancedFillMode class
				if ( super.type instanceof FillMode.AdvancedFillMode afm ) {
					if ( other instanceof MulFillMode mfm ) {
						Dimension dim = afm.bothSizes(comp, info, maxWidth, maxHeigth, width, mfm.type);
						if ( width ) {
							dim.width *= super.mul;
							dim.height *= mfm.mul;
						} else {
							dim.height *= super.mul;
							dim.width *= mfm.mul;
						}
						return dim;
					} else {
						Dimension dim = afm.bothSizes(comp, info, maxWidth, maxHeigth, width, other);
						if ( width ) dim.width *= super.mul;
						else dim.height *= super.mul;
						return dim;
					}
				} else {
					int a = super.type.size(comp, info, maxWidth, maxHeigth, width);
					int b = other.size(comp, info, maxWidth, maxHeigth, !width);
					if ( width ) return new Dimension(a, b);
					return new Dimension(b, a);
				}
			}
			
		}
		
		/** {@inheritDoc} */
		@Override
		public int size(Component comp, CompInfo info, int maxWidth, int maxHeigth, boolean width) {
			return (int) ( this.mul * this.type.size(comp, info, maxWidth, maxHeigth, width) );
		}
		
		/**
		 * sets the type and multiplicator
		 * 
		 * @param type the backing fill mode
		 * @param mul  the multiplicator
		 * 
		 * @see FillMode#fillMode(float, FillMode)
		 * 
		 * @throws NullPointerException     if {@code mode} is {@code null}
		 * @throws IllegalArgumentException if {@code mode} is a {@link SimpleFillMode} and {@code mul} is not valid for
		 *                                      the given {@code mode}
		 */
		public void set(FillMode type, float mul) throws NullPointerException, IllegalArgumentException {
			if ( type instanceof SimpleFillMode sfm ) {
				switch ( sfm.mode ) {
				case SimpleFillMode.FILL_COMPLETLY, SimpleFillMode.FILL_MAXIMUM:
					if ( !( mul <= 1f ) || mul < 0f ) { // NOSONAR also catch NaN
						throw new IllegalArgumentException(
							"invalid multiplicator: " + sfm + " only supports multiplicator from 0 to 1. mul=" + mul);
					}
					break;
				case SimpleFillMode.FILL_MINIMUM:
					if ( !( mul >= 1f ) ) { // NOSONAR also catch NaN
						throw new IllegalArgumentException(
							"invalid multiplicator: minimum only supports multiplicator greather or equal than 1. mul="
								+ mul);
					}
					break;
				case SimpleFillMode.FILL_PREFERRED:
					if ( !( mul > 0f ) ) { // NOSONAR also catch NaN
						throw new IllegalArgumentException(
							"invalid multiplicator: preferred only supports multiplicator greather than 0. mul=" + mul);
					}
					break;
				default:
					throw new AssertionError("illegal mode: " + sfm.mode);
				}
			} else if ( type == null ) throw new NullPointerException("fill mode is null");
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
			if ( !( obj instanceof MulFillMode ) ) { return false; }
			MulFillMode other = (MulFillMode) obj;
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

