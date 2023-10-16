package de.hechler.patrick.layout;


public sealed interface FillMode {
	
	public static final SimpleFillMode FILL_COMPLETLY = SimpleFillMode.FILL_COMPLETLY;
	public static final SimpleFillMode FILL_MINIMUM   = SimpleFillMode.FILL_MINIMUM;
	public static final SimpleFillMode FILL_MAXIMUM   = SimpleFillMode.FILL_MAXIMUM;
	public static final SimpleFillMode FILL_PREFERRED = SimpleFillMode.FILL_PREFERRED;
	
	SimpleFillMode type();
	
	public static FillMode fillMode(float mul, SimpleFillMode mode) {
		if ( mul == 1f ) return mode;
		return new ComplexFillMode(mode, mul);
	}
	
	public static enum SimpleFillMode implements FillMode {
		
		FILL_MINIMUM,
		
		FILL_PREFERRED,
		
		FILL_MAXIMUM,
		
		FILL_COMPLETLY,
		
		;
		
		@Override
		public SimpleFillMode type() {
			return this;
		}
		
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
	
	public static non-sealed class ComplexFillMode implements FillMode {
		
		SimpleFillMode type;
		float          mul;
		
		public ComplexFillMode(SimpleFillMode type, float mul) {
			switch ( type ) {
			case FILL_COMPLETLY, FILL_MAXIMUM:
				if ( !( mul <= 1f ) || mul < 0f ) { // also catch NaN
					throw new IllegalArgumentException(
						"invalid multiplicator: " + type + " only supports multiplicator from 0 to 1. mul=" + mul);
				}
				break;
			case FILL_MINIMUM:
				if ( !( mul >= 1f ) ) { // also catch NaN
					throw new IllegalArgumentException(
						"invalid multiplicator: minimum only supports multiplicator greather or equal than 1. mul="
							+ mul);
				}
				break;
			case FILL_PREFERRED:
				if ( !( mul >= 0f ) ) { // also catch NaN
					throw new IllegalArgumentException(
						"invalid multiplicator: preferred only supports multiplicator greather or equal than 0. mul="
							+ mul);
				}
				break;
			}
			this.type = type;
			this.mul = mul;
		}
		
		
		
		public SimpleFillMode type() {
			return type;
		}
		
		public float mul() {
			return mul;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Float.floatToIntBits(mul);
			result = prime * result + ( ( type == null ) ? 0 : type.hashCode() );
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) { return true; }
			if ( !( obj instanceof ComplexFillMode ) ) { return false; }
			ComplexFillMode other = (ComplexFillMode) obj;
			if ( Float.floatToIntBits(mul) != Float.floatToIntBits(other.mul) ) { return false; }
			if ( type != other.type ) { return false; }
			return true;
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("FillModeCls [type=");
			builder.append(type);
			builder.append(", mul=");
			builder.append(mul);
			builder.append("]");
			return builder.toString();
		}
		
	}
	
}

