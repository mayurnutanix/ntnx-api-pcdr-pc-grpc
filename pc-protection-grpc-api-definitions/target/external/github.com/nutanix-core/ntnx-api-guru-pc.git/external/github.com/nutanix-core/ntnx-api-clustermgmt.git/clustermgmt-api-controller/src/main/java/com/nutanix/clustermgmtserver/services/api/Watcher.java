/*
 * Copyright (c) 2022 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.services.api;

import com.nutanix.insights.exception.InsightsWatchClientException;
import com.nutanix.insights.ifc.InsightsInterfaceProto.Entity;

import java.util.function.BiConsumer;

/**
 * Interface for handling watch operations.
 */


public interface Watcher {

  /**
   * Method to register the same watch for multiple operations like
   * create/update/delete of the given entity type.
   *
   * @param entityType Name of the entity type.
   * @param callback   The callback function which takes the changed entity and
   *                   the previous entity as the parameters.
   * @param metricName The name of the metric on which the watch is to be set.
   *                   If Null/empty, the watch is set on the whole entity.
   * @throws InsightsWatchClientException
   */
  void registerCompositeWatchOfType(String entityType, BiConsumer<Entity, Entity> callback,
                                    String metricName) throws InsightsWatchClientException;

  /**
   * Starts the polling and callback of the fired watches.
   *
   * @throws InsightsWatchClientException
   */
  void start() throws InsightsWatchClientException;

  /**
   * Method to initialize the watch client, register the watch for the
   * different entity types, and finally start those watches.
   * In case any of these operations fail, this function must implement
   * the logic to re-establish the connection.
   */
  void establishWatches();
}
