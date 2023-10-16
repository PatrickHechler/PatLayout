package de.hechler.patrick.layout;

import java.awt.Dimension;
import java.util.Arrays;

public interface BlockSize {
	
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
			return w0;
		}
		
		@Override
		public int h(int i) {
			return h0;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + h0;
			result = prime * result + w0;
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) { return true; }
			if ( !( obj instanceof BlockSize1 ) ) { return false; }
			BlockSize1 other = (BlockSize1) obj;
			if ( h0 != other.h0 ) { return false; }
			if ( w0 != other.w0 ) { return false; }
			return true;
		}
		
		@Override
		public String toString() {
			return "[(" + w0 + "|" + h0 + ")]";
		}
		
	}
	
	public static class BlockSize2 implements BlockSize {
		
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
			return i == 0 ? w0 : w1;
		}
		
		@Override
		public int h(int i) {
			return i == 0 ? h0 : h1;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + h0;
			result = prime * result + h1;
			result = prime * result + w0;
			result = prime * result + w1;
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) { return true; }
			if ( !( obj instanceof BlockSize2 ) ) { return false; }
			BlockSize2 other = (BlockSize2) obj;
			if ( h0 != other.h0 ) { return false; }
			if ( h1 != other.h1 ) { return false; }
			if ( w0 != other.w0 ) { return false; }
			if ( w1 != other.w1 ) { return false; }
			return true;
		}
		
		@Override
		public String toString() {
			return "[(" + w0 + "|" + h0 + "),(" + w1 + "|" + h1 + ")]";
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
			int v = w[i];
			if ( v < dim.width ) {
				w[i] = dim.width;
			}
			v = h[i];
			if ( v < dim.height ) {
				h[i] = dim.height;
			}
		}
		
		@Override
		public void min(int i, Dimension dim) {
			int v = w[i];
			if ( v < dim.width || v == -1 ) {
				w[i] = dim.width;
			}
			v = h[i];
			if ( v < dim.height || v == -1 ) {
				h[i] = dim.height;
			}
		}
		
		@Override
		public void h(int i, int val) {
			h[i] = val;
		}
		
		@Override
		public void w(int i, int val) {
			w[i] = val;
		}
		
		@Override
		public int h(int i) {
			return h[i];
		}
		
		@Override
		public int w(int i) {
			return w[i];
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(h);
			result = prime * result + Arrays.hashCode(w);
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) { return true; }
			if ( !( obj instanceof BlockSizeGeneric ) ) { return false; }
			BlockSizeGeneric other = (BlockSizeGeneric) obj;
			if ( !Arrays.equals(h, other.h) ) { return false; }
			if ( !Arrays.equals(w, other.w) ) { return false; }
			return true;
		}
		
		@Override
		public String toString() {
			StringBuilder b = new StringBuilder().append('[');
			for (int i = 0, l = w.length; i < l; i++) {
				if ( i == 0 ) b.append('(');
				else b.append(",(");
				b.append(w[i]).append('|').append(h[i]).append(')');
			}
			return b.append(']').toString();
		}
		
	}
	
}
