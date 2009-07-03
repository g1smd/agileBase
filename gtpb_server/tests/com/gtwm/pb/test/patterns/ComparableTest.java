package com.gtwm.pb.test.patterns;

public interface ComparableTest {

	/**
	 * tests that compareTo() is consistent with equals() (i.e. returns zero when
	 * comparing two 'equal' objects).
	 * being consistent with equals() is only strongly recommended
	 */
	public void testCompareTo_consistentWithEquals ();
	
	/**
	 * tests that compareTo() throws a NullPointerException when passed null
	 * - even though equals() returns false
	 */
	public void testCompareTo_throwsExceptionForNull ();
	
}
