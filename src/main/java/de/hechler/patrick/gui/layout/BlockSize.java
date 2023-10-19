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

import java.awt.Dimension;
import java.util.Arrays;

interface BlockSize {
	
	static BlockSize create(int size) {
		switch ( size ) {
		case 1:
			return new BlockSize1();
		case 2:
			return new BlockSize2();
		default:
			return new BlockSizeGeneric(size);
		}
	}
	
	void max(int i, Dimension dim);
	
	void min(int i, Dimension dim);
	
	void w(int i, int val);
	
	void h(int i, int val);
	
	int w(int i);
	
	int h(int i);
	
	public static class BlockSize1 implements BlockSize {
		
		private int w0;
		private int h0;
		
		@Override
		public void max(@SuppressWarnings("unused") int i, Dimension dim) {
			int v = this.w0;
			if ( v < dim.width ) {
				this.w0 = dim.width;
			}
			v = this.h0;
			if ( v < dim.height ) {
				this.h0 = dim.height;
			}
		}
		
		@Override
		public void min(@SuppressWarnings("unused") int i, Dimension dim) {
			int v = this.w0;
			if ( v > dim.width ) {
				this.w0 = dim.width;
			}
			v = this.h0;
			if ( v > dim.height ) {
				this.h0 = dim.height;
			}
		}
		
		@Override
		public void w(@SuppressWarnings("unused") int i, int val) {
			this.w0 = val;
		}
		
		@Override
		public void h(@SuppressWarnings("unused") int i, int val) {
			this.h0 = val;
		}
		
		@Override
		public int w(@SuppressWarnings("unused") int i) {
			return this.w0;
		}
		
		@Override
		public int h(@SuppressWarnings("unused") int i) {
			return this.h0;
		}
		
		@Override
		public int hashCode() {
			final int prime  = 31;
			int       result = 1;
			result = prime * result + this.h0;
			result = prime * result + this.w0;
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) { return true; }
			if ( !( obj instanceof BlockSize1 ) ) { return false; }
			BlockSize1 other = (BlockSize1) obj;
			if ( this.h0 != other.h0 ) { return false; }
			return this.w0 == other.w0;
		}
		
		@Override
		public String toString() {
			return "{(" + this.w0 + "|" + this.h0 + ")}";
		}
		
	}
	
	public static class BlockSize2 implements BlockSize {
		
		private int w0;
		private int h0;
		private int w1;
		private int h1;
		
		@Override
		public void max(int i, Dimension dim) {
			int v = i == 0 ? this.w0 : this.w1;
			if ( v < dim.width ) {
				if ( i == 0 ) {
					this.w0 = dim.width;
				} else {
					this.w1 = dim.width;
				}
			}
			v = i == 0 ? this.h0 : this.h1;
			if ( v < dim.height ) {
				if ( i == 0 ) {
					this.h0 = dim.height;
				} else {
					this.h1 = dim.height;
				}
			}
		}
		
		@Override
		public void min(int i, Dimension dim) {
			int v = i == 0 ? this.w0 : this.w1;
			if ( v > dim.width ) {
				if ( i == 0 ) {
					this.w0 = dim.width;
				} else {
					this.w1 = dim.width;
				}
			}
			v = i == 0 ? this.h0 : this.h1;
			if ( v > dim.height ) {
				if ( i == 0 ) {
					this.h0 = dim.height;
				} else {
					this.h1 = dim.height;
				}
			}
		}
		
		@Override
		public void w(int i, int val) {
			if ( i == 0 ) {
				this.w0 = val;
			} else {
				this.w1 = val;
			}
		}
		
		@Override
		public void h(int i, int val) {
			if ( i == 0 ) {
				this.h0 = val;
			} else {
				this.h1 = val;
			}
		}
		
		@Override
		public int w(int i) {
			return i == 0 ? this.w0 : this.w1;
		}
		
		@Override
		public int h(int i) {
			return i == 0 ? this.h0 : this.h1;
		}
		
		@Override
		public int hashCode() {
			final int prime  = 31;
			int       result = 1;
			result = prime * result + this.h0;
			result = prime * result + this.h1;
			result = prime * result + this.w0;
			result = prime * result + this.w1;
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) { return true; }
			if ( !( obj instanceof BlockSize2 ) ) { return false; }
			BlockSize2 other = (BlockSize2) obj;
			if ( this.h0 != other.h0 ) { return false; }
			if ( this.h1 != other.h1 ) { return false; }
			if ( this.w0 != other.w0 ) { return false; }
			return this.w1 == other.w1;
		}
		
		@Override
		public String toString() {
			return "{(" + this.w0 + "|" + this.h0 + "),(" + this.w1 + "|" + this.h1 + ")}";
		}
		
	}
	
	public static class BlockSizeGeneric implements BlockSize {
		
		private int[] w;
		private int[] h;
		
		
		public BlockSizeGeneric(int size) {
			this.w = new int[size];
			this.h = new int[size];
		}
		
		@Override
		public void max(int i, Dimension dim) {
			int v = this.w[i];
			if ( v < dim.width ) {
				this.w[i] = dim.width;
			}
			v = this.h[i];
			if ( v < dim.height ) {
				this.h[i] = dim.height;
			}
		}
		
		@Override
		public void min(int i, Dimension dim) {
			int v = this.w[i];
			if ( v > dim.width ) {
				this.w[i] = dim.width;
			}
			v = this.h[i];
			if ( v > dim.height ) {
				this.h[i] = dim.height;
			}
		}
		
		@Override
		public void h(int i, int val) {
			this.h[i] = val;
		}
		
		@Override
		public void w(int i, int val) {
			this.w[i] = val;
		}
		
		@Override
		public int h(int i) {
			return this.h[i];
		}
		
		@Override
		public int w(int i) {
			return this.w[i];
		}
		
		@Override
		public int hashCode() {
			final int prime  = 31;
			int       result = 1;
			result = prime * result + Arrays.hashCode(this.h);
			result = prime * result + Arrays.hashCode(this.w);
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) { return true; }
			if ( !( obj instanceof BlockSizeGeneric ) ) { return false; }
			BlockSizeGeneric other = (BlockSizeGeneric) obj;
			if ( !Arrays.equals(this.h, other.h) ) { return false; }
			return Arrays.equals(this.w, other.w);
		}
		
		@Override
		public String toString() {
			StringBuilder b = new StringBuilder().append('{');
			for (int i = 0, l = this.w.length; i < l; i++) {
				if ( i == 0 ) b.append('(');
				else b.append(",(");
				b.append(this.w[i]).append('|').append(this.h[i]).append(')');
			}
			return b.append('}').toString();
		}
		
	}
	
}
