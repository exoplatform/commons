package org.exoplatform.commons.version.util;

import junit.framework.TestCase;

public class VersionComparatorTest extends TestCase {

  public void testIsBefore() {
    assertTrue(VersionComparator.isBefore("2.1", "2.2"));
    assertFalse(VersionComparator.isBefore("2.2", "2.1"));
    assertFalse(VersionComparator.isBefore("2.1", "2.1"));
    assertFalse(VersionComparator.isBefore("2.1", ""));
    assertTrue(VersionComparator.isBefore("", "2.2"));
  }

  public void testIsAfter() {
    assertTrue(VersionComparator.isAfter("2.2", "2.1"));
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
