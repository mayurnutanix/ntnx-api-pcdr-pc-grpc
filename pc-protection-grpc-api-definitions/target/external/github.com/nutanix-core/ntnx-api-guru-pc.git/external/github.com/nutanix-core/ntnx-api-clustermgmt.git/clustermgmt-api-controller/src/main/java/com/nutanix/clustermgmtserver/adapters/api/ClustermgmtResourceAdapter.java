/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */
package com.nutanix.clustermgmtserver.adapters.api;

import com.nutanix.insights.ifc.InsightsInterfaceProto;
import com.nutanix.insights.ifc.InsightsInterfaceProto.GetEntitiesArg;
import com.nutanix.insights.ifc.InsightsInterfaceProto.GetEntitiesWithMetricsArg;

import java.util.List;

public interface ClustermgmtResourceAdapter {
  public GetEntitiesArg adaptToGetEntitiesArg(final String extId,
                                              final String entityType);
  public GetEntitiesWithMetricsArg adaptToGetEntitiesWithMetricsArg(
    final String extId, final String entityType, final Integer offset, final Integer limit,
    final List<String> rawColumns, final InsightsInterfaceProto.QueryOrderBy orderBy,
    final InsightsInterfaceProto.BooleanExpression expression, final InsightsInterfaceProto.QueryGroupBy groupBy);
  public InsightsInterfaceProto.BooleanExpression adaptToBooleanExpression(
    final String columnName, final InsightsInterfaceProto.ComparisonExpression.Operator operator,
    final InsightsInterfaceProto.DataValue.Builder value);
  public InsightsInterfaceProto.BooleanExpression joinBooleanExpressions(
    InsightsInterfaceProto.BooleanExpression lhs, InsightsInterfaceProto.BooleanExpression.Operator operator,
    InsightsInterfaceProto.BooleanExpression rhs);
}
