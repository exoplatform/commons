package org.exoplatform.commons.version.util;

import junit.framework.TestCase;

public class VersionComparatorTest extends TestCase {

  public void testIsBefore() {
    assertTrue(VersionComparator.isBefore("2.1", "2.2"));
    assertFalse(VersionComparator.isBefore("2.2", "2.1"));
    assertFalse(VersionComparator.isBefore("2.1", "2.1"));
    assertFalse(VersionComparator.isBefore("2.1", ""));
    assertTrue(VersionComparator.isBefore("", "2.2"));
    assertTrue(VersionComparator.isBefore("5.0.0-M32", "5.0-RC1"));
  }

  public void testIsAfter() {
    assertTrue(VersionComparator.isAfter("5.0.0-GA", "5.0.0-M32"));
    assertTrue(VersionComparator.isAfter("2.2", "2.1"));
    assertTrue(VersionComparator.isAfter("4.0.0-relooking-SNAPSHOT", "2.3.10-SNAPSHOT"));
    assertFalse(VersionComparator.isAfter("2.1", "2.2"));
    assertFalse(VersionComparator.isAfter("2.1", "2.1"));
    assertFalse(VersionComparator.isAfter("", "2.1"));
    assertTrue(VersionComparator.isAfter("2.2", ""));
  }

  public void testIsSame() {
    assertTrue(VersionComparator.isSame("2.1", "2.1"));
    assertFalse(VersionComparator.isSame("2.1", "2.2"));
    assertFalse(VersionComparator.isSame("2.1", ""));
    assertFalse(VersionComparator.isSame("", "2.2"));
  }
}
