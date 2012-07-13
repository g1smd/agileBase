package com.gtwm.pb.test.patterns;

public interface HashEqualTest {

	/**
	 * tests equals() returns false for null parameter (without exception)
	 */
	public void testEquals_returnsFalseForNull ();
	
	/**
	 * tests that an object is always equal to itself
	 */
	public void testEquals_isReflexive ();
	
	/**
	 * tests that a.equals(b) produces the same result as b.equals(a)
	 */
	public void testEquals_isSymmetric ();
	
	/**
	 * testObjectsAreEqual: tests that class conforms to the contract
	 * of agreement between @method hashCode() & @method equals() by returning the
	 * same hashCode from equal objects & different hashCodes from unequal objects
	 */
	public void testEquals_agreesWithHashCode ();

	/**
	 * tests that hashCode() returns same value on subsequent calls provided
	 * that the object remains unchanged
	 */
	public void testHashCode_isConsistent ();
}
