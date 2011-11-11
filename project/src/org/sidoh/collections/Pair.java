package org.sidoh.collections;

public class Pair<T1, T2> {
	protected T1 v1;
	protected T2 v2;
	
	public Pair(T1 v1, T2 v2) {
		this.v1 = v1;
		this.v2 = v2;
	}

	public T1 getV1() {
		return v1;
	}

	public void setV1(T1 v1) {
		this.v1 = v1;
	}

	public T2 getV2() {
		return v2;
	}

	public void setV2(T2 v2) {
		this.v2 = v2;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((v1 == null) ? 0 : v1.hashCode());
		result = prime * result + ((v2 == null) ? 0 : v2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair other = (Pair) obj;
		if (v1 == null) {
			if (other.v1 != null)
				return false;
		} else if (!v1.equals(other.v1))
			return false;
		if (v2 == null) {
			if (other.v2 != null)
				return false;
		} else if (!v2.equals(other.v2))
			return false;
		return true;
	}

	public static <T1, T2> Pair<T1, T2> create(T1 v1, T2 v2) {
		return new Pair<T1, T2>(v1, v2);
	}
}
