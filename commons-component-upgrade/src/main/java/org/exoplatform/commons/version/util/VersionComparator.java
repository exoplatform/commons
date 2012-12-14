package org.exoplatform.commons.version.util;

/**
 * Utility class that uses Comparison class to provide some simple static methods.
 * 
 * @author Clement
 *
 */
public class VersionComparator {

  public static boolean isBefore(String leftVersion, String rightVersion) {
    ComparableVersion cv1 = new ComparableVersion(leftVersion);
    ComparableVersion cv2 = new ComparableVersion(rightVersion);
    return (cv1.compareTo(cv2) < 0);
  }

  public static boolean isAfter(String leftVersion, String rightVersion) {
    ComparableVersion cv1 = new ComparableVersion(leftVersion);
    ComparableVersion cv2 = new ComparableVersion(rightVersion);
    return (cv1.compareTo(cv2) > 0);
  }

  public static boolean isSame(String leftVersion, String rightVersion) {
    ComparableVersion cv1 = new ComparableVersion(leftVersion);
    ComparableVersion cv2 = new ComparableVersion(rightVersion);
    return (cv1.compareTo(cv2) == 0);
  }
}
