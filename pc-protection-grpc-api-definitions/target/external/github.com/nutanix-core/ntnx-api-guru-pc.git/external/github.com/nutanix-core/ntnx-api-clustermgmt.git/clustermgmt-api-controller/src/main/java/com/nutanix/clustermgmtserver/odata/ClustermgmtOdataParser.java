/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.odata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtOdataException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.services.impl.StatsGatewayServiceImpl;
import com.nutanix.insights.ifc.InsightsInterfaceProto.BooleanExpression;
import com.nutanix.insights.ifc.InsightsInterfaceProto.QueryOrderBy;
import com.nutanix.insights.ifc.InsightsInterfaceProto.QueryGroupBy;
import com.nutanix.odata.core.service.ServiceMetadataProvider;
import com.nutanix.odata.core.uri.ParsedQuery;
import com.nutanix.odata.parser.CustomParser;
import com.nutanix.odata.parser.ParseParams;
import com.nutanix.odata.visitors.idf.IDFFilterEvaluator;
import com.nutanix.odata.visitors.idf.IDFGroupByEvaluator;
import com.nutanix.odata.visitors.idf.IDFOrderByEvaluator;
import com.nutanix.odata.visitors.graphql.GraphQLExpandEvaluator;
import com.nutanix.stats_gateway.graphql_interface.GroupsGraphqlInterfaceProto;
import com.nutanix.util.base.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.apache.olingo.server.core.uri.parser.UriParserException;
import org.apache.olingo.server.core.uri.validator.UriValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parser for creating IDF queries based on odata params for the Clustermgmt Service.
 */

@Slf4j
@Component
public class ClustermgmtOdataParser {

  final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  final ServiceMetadataProvider metadataProvider;
  final StatsGatewayServiceImpl statsGatewayService;

  @Autowired
  public ClustermgmtOdataParser(ServiceMetadataProvider metadataProvider, StatsGatewayServiceImpl statsGatewayService) {
    this.metadataProvider = metadataProvider;
    this.statsGatewayService = statsGatewayService;
  }

  @AllArgsConstructor
  public static class Result {

    @Getter
    BooleanExpression filter;

    @Getter
    List<QueryOrderBy> orderBy;

    @Getter
    QueryGroupBy groupBy;

    @Getter
    Map statsGatewayResponseMap;
  }

  public Result parse(final String endpoint, final String filter, final String orderBy,
                      final String apply, final String select, final String expand)
    throws ValidationException, ClustermgmtServiceException {
    final CustomParser parser = new CustomParser(metadataProvider);

    final List<String> parts = new ArrayList<>();
    if (filter != null && !filter.trim().isEmpty()) {
      parts.add("$filter=" + filter.trim());
    }
    if (orderBy != null && !orderBy.trim().isEmpty()) {
      parts.add("$orderby=" + orderBy.trim());
    }
    if (apply != null && !apply.trim().isEmpty()) {
      parts.add("$apply=" + apply.trim());
    }
    if (select != null && !select.trim().isEmpty()) {
      parts.add("$select=" + select.trim());
    }
    if (expand != null && !expand.trim().isEmpty()) {
      parts.add("$expand=" + expand.trim());
    }

    final String query = String.join("&", parts);

    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    String baseUri = request.getRequestURL().toString();

    final ParsedQuery parsedQuery ;
    try {
      ParseParams params = new ParseParams(endpoint, query, null, "clustermgmt", "config" , baseUri);
      parsedQuery = parser.parseUri(params);
    }
    catch (UriValidationException | UriParserException e) {
      log.error("Error while parsing URI. Endpoint: " + endpoint + ", Query: " + query);
      throw new ValidationException("Odata Error: Error while parsing URI, reason: " + e.getMessage(), e);
    }

    BooleanExpression idfExpression = null;
    if (parsedQuery.getFilterInfo() != null) {
      idfExpression = IDFFilterEvaluator.getFilterQuery(parsedQuery);
    }

    List<QueryOrderBy> orderByQueries = null;
    if (parsedQuery.getOrderByInfo() != null) {
      orderByQueries = IDFOrderByEvaluator.getOrderByQueries(parsedQuery);
      if (orderByQueries.size() > 1) {
        throw new ClustermgmtOdataException("Sorting is only supported on a single field");
      }
    }

    QueryGroupBy queryGroupBy = null;
    if (parsedQuery.getApplyInfo() != null || parsedQuery.getSelectInfo() != null) {
      queryGroupBy = IDFGroupByEvaluator.getGroupByQuery(parsedQuery);
    }

    Map statsGatewayResponseMap = null;
    if (expand != null && !expand.trim().isEmpty()) {
      log.debug("Expand query {}", expand);
      final String gwQuery = GraphQLExpandEvaluator.getExpandQuery(parsedQuery);
      final GroupsGraphqlInterfaceProto.GroupsGraphqlRet ret = statsGatewayService.executeQuery(gwQuery);
      log.debug("GraphQl response {}", ret);
      try {
        statsGatewayResponseMap = OBJECT_MAPPER.readValue(ret.getData(), Map.class);
      } catch (final JsonProcessingException e) {
        throw new ValidationException("Error while parsing JSON response from Stats Gateway", e);
      }
    }

    return new Result(idfExpression, orderByQueries, queryGroupBy, statsGatewayResponseMap);
  }

  public ParsedQuery parseStatsUri(final String path, final String fragment, final String filter, final String orderBy,
                                   final String apply, final String select, final String expand)
    throws ValidationException{
      final CustomParser parser = new CustomParser(metadataProvider);

      HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
      String baseUri = request.getRequestURL().toString();
      log.debug("BaseUri to parser: " + baseUri);

      final List<String> parts = new ArrayList<>();
      if (filter != null && !filter.trim().isEmpty()) {
        parts.add("$filter=" + filter.trim());
      }
      if (orderBy != null && !orderBy.trim().isEmpty()) {
        parts.add("$orderby=" + orderBy.trim());
      }
      if (apply != null && !apply.trim().isEmpty()) {
        parts.add("$apply=" + apply.trim());
      }
      if (select != null && !select.trim().isEmpty()) {
        parts.add("$select=" + select.trim());
      }
      if (expand != null && !expand.trim().isEmpty()) {
        parts.add("$expand=" + expand.trim());
      }

      final String query = String.join("&", parts);

      final ParsedQuery parsedQuery;
      try {
        log.debug("Query to parser: " + query);
        ParseParams params = new ParseParams(path, query, null, "clustermgmt", "stats" , baseUri);
        parsedQuery = parser.parseUri(params);
        log.debug("Select operation edm: " + parsedQuery.getSelectEdm().toString());
        log.debug("Select operation info: " + parsedQuery.getSelectInfo().toString());
      }
      catch (UriValidationException | UriParserException e){
        log.error("Error while parsing URI. Path: " + path + ", Query: " + query);
        throw new ValidationException("Odata Error: Error while parsing URI, reason: " + e.getMessage(), e);
      }

      return parsedQuery;
  }
}
