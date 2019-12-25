package org.exoplatform.platform.edition;


import java.util.*;

import org.exoplatform.container.ExoProfileExtension;

public class TestEdition implements ExoProfileExtension {
  @Override
  public Set<String> getProfiles() {
    return Collections.singleton("edition");
  }
}
