package com.alipay.sofa.configuration;

import com.alipay.sofa.configuration.model.ConfigChangeEvent;

public interface ConfigChangeListener {
  /**
   * Invoked when there is any config change for the namespace.
   * @param changeEvent the event for this change
   */
  public void onChange(ConfigChangeEvent changeEvent);
}
