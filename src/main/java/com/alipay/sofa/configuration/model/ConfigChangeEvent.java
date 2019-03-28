package com.alipay.sofa.configuration.model;

import java.util.Map;
import java.util.Set;

/**
 * A change event when a namespace's config is changed.
 */
public class ConfigChangeEvent {
  private final String sourceName;
  private final Map<String, ConfigChange> changes;

  /**
   * Constructor.
   */
  public ConfigChangeEvent(String sourceName, Map<String, ConfigChange> changes) {
    this.sourceName = sourceName;
    this.changes = changes;
  }

  /**
   * Get the keys changed.
   * @return the list of the keys
   */
  public Set<String> changedKeys() {
    return changes.keySet();
  }

  /**
   * Get a specific change instance for the key specified.
   * @param key the changed key
   * @return the change instance
   */
  public ConfigChange getChange(String key) {
    return changes.get(key);
  }

  public boolean isChanged(String key) {
    return changes.containsKey(key);
  }

  public String getSourceName() {
    return sourceName;
  }
}
