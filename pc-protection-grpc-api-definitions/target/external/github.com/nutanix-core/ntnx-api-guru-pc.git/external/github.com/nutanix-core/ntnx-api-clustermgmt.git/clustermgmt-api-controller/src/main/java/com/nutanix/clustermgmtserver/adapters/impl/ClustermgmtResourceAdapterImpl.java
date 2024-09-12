/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */
package com.nutanix.clustermgmtserver.adapters.impl;

import com.nutanix.clustermgmtserver.adapters.api.ClustermgmtResourceAdapter;
import com.nutanix.insights.ifc.InsightsInterfaceProto;
import com.nutanix.insights.ifc.InsightsInterfaceProto.GetEntitiesArg;
import com.nutanix.insights.ifc.InsightsInterfaceProto.GetEntitiesWithMetricsArg;
import com.nutanix.insights.ifc.InsightsInterfaceProto.EntityGuid;
import com.nutanix.insights.ifc.InsightsInterfaceProto.BooleanExpression;
import com.nutanix.insights.ifc.InsightsInterfaceProto.LeafExpression;
import com.nutanix.insights.ifc.InsightsInterfaceProto.QueryOrderBy;
import com.nutanix.insights.ifc.InsightsInterfaceProto.Query;
import com.nutanix.insights.ifc.InsightsInterfaceProto.QueryGroupBy;
import com.nutanix.insights.ifc.InsightsInterfaceProto.QueryLimit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
public class ClustermgmtResourceAdapterImpl implements ClustermgmtResourceAdapter {
  @Override
  public GetEntitiesArg adaptToGetEntitiesArg(final String entityId,
                                              final String entityType) {

    GetEntitiesArg.Builder getEntitiesArgBuilder = GetEntitiesArg.newBuilder();
    EntityGuid.Builder entityGuidBuilder = EntityGuid.newBuilder();
    if (!StringUtils.isEmpty(entityId)) {
      // entityId will be null or empty when the caller is requesting all
      // entities.
      entityGuidBuilder.setEntityId(entityId);
    }
    entityGuidBuilder.setEntityTypeName(entityType);
    getEntitiesArgBuilder.addEntityGuidList(entityGuidBuilder);
    return getEntitiesArgBuilder.build();
  }

  @Override
  public GetEntitiesWithMetricsArg adaptToGetEntitiesWithMetricsArg(
    String extId, String entityType, Integer offset, Integer limit,
    List<String> rawColumns, QueryOrderBy orderBy, BooleanExpression expression, QueryGroupBy groupBy) {

    GetEntitiesWithMetricsArg.Builder getEntitiesWithMetricsArg =
      GetEntitiesWithMetricsArg.newBuilder();
    Query.Builder queryBuilder = Query.newBuilder();

    EntityGuid.Builder entityGuidBuilder = EntityGuid.newBuilder();
    entityGuidBuilder.setEntityTypeName(entityType);
    if (!StringUtils.isEmpty(extId)) {
      entityGuidBuilder.setEntityId(extId);
    }
    queryBuilder.addEntityList(entityGuidBuilder);

    QueryGroupBy.Builder queryGroupByBuilder = QueryGroupBy.newBuilder();

    QueryLimit.Builder queryLimitBuilder = QueryLimit.newBuilder();
    if (offset != null) {
      queryLimitBuilder.setOffset(offset);
    }
    if (limit != null) {
      queryLimitBuilder.setLimit(limit);
    }

    if (!CollectionUtils.isEmpty(rawColumns)) {
      rawColumns.stream()
        .map(rawColumn -> InsightsInterfaceProto.QueryRawColumn.newBuilder().setColumn(rawColumn))
        .forEach(queryGroupByBuilder::addRawColumns);
    }

    queryGroupByBuilder.setRawLimit(queryLimitBuilder);

    if (expression != null) {
      queryBuilder.setWhereClause(expression);
    }

    if (orderBy != null) {
      queryGroupByBuilder.setRawSortOrder(orderBy);
      queryGroupByBuilder.addRawColumns(
        InsightsInterfaceProto.QueryRawColumn.newBuilder().setColumn(orderBy.getSortColumn()).build());
    }

    if (groupBy != null) {
      queryGroupByBuilder.setGroupByColumn(groupBy.getGroupByColumn());
    }

    queryBuilder.setGroupBy(queryGroupByBuilder);

    return getEntitiesWithMetricsArg.setQuery(queryBuilder).build();
  }

  @Override
  public BooleanExpression adaptToBooleanExpression(
    final String columnName, final InsightsInterfaceProto.ComparisonExpression.Operator operator,
    final InsightsInterfaceProto.DataValue.Builder value) {

    LeafExpression.Builder lhs = LeafExpression.newBuilder().setColumn(columnName);
    LeafExpression.Builder rhs = LeafExpression.newBuilder().setValue(value);

    final InsightsInterfaceProto.ComparisonExpression comparisonExpression =
      InsightsInterfaceProto.ComparisonExpression
        .newBuilder()
        .setLhs(InsightsInterfaceProto.Expression.newBuilder().setLeaf(lhs))
        .setOperator(operator)
        .setRhs(InsightsInterfaceProto.Expression.newBuilder().setLeaf(rhs))
        .build();

    return BooleanExpression.newBuilder().setComparisonExpr(comparisonExpression).build();
  }

  public BooleanExpression joinBooleanExpressions(
    BooleanExpression lhs, BooleanExpression.Operator operator,
    BooleanExpression rhs) {
    return BooleanExpression.newBuilder()
      .setLhs(lhs)
      .setOperator(operator)
      .setRhs(rhs)
      .build();
  }

  /**
   * Util to help construct NotExists boolean expressions while doing IDF queries
   * @param columnName - Name of the attribute
   * @return - A Boolean expression.
   */
  public BooleanExpression constructNotExistsBooleanExpression(final String columnName) {
    LeafExpression.Builder lhs = LeafExpression.newBuilder().setColumn(columnName);

    final InsightsInterfaceProto.ComparisonExpression columnExists =
      InsightsInterfaceProto.ComparisonExpression
        .newBuilder()
        .setLhs(InsightsInterfaceProto.Expression.newBuilder().setLeaf(lhs.build()))
        .setOperator(InsightsInterfaceProto.ComparisonExpression.Operator.kExists)
        .build();
    return BooleanExpression
      .newBuilder()
      .setLhs(BooleanExpression.newBuilder().setComparisonExpr(columnExists).build())
      .setOperator(InsightsInterfaceProto.BooleanExpression.Operator.kNot)
      .build();
  }
}
