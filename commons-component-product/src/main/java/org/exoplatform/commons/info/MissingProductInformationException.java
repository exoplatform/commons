package org.exoplatform.commons.info;

public class MissingProductInformationException extends Exception{
  private static final long serialVersionUID = -7407551830430394842L;
  
  public MissingProductInformationException(String propertyName) {
    super("Missing product information property: "+propertyName);
  }
}
