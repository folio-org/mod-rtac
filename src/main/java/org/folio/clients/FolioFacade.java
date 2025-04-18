package org.folio.clients;

import static org.folio.rest.RestVerticle.OKAPI_HEADER_TENANT;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.HttpStatus;
import org.folio.mappers.ErrorMapper;
import org.folio.mappers.FolioToRtacMapper;
import org.folio.models.InstanceTenants;
import org.folio.models.InventoryHoldingsAndItemsAndPieces;
import org.folio.rest.jaxrs.model.Error;
import org.folio.rest.jaxrs.model.InventoryHoldingsAndItems;
import org.folio.rest.jaxrs.model.LegacyHoldings;
import org.folio.rest.jaxrs.model.RtacHoldings;
import org.folio.rest.jaxrs.model.RtacHoldingsBatch;
import org.folio.rest.jaxrs.model.RtacRequest;
import org.folio.rest.jaxrs.model.Value;
import org.folio.rest.tools.utils.TenantTool;
import org.folio.rtac.rest.exceptions.HttpException;

public class FolioFacade {

  private static final Logger logger = LogManager.getLogger();
  private static final WebClient webClient = WebClient.create(Vertx.currentContext().owner());

  private final Map<String, String> okapiHeaders;
  private final InventoryClient inventoryClient;
  private final CirculationClient circulationClient;
  private final CirculationRequestClient requestClient;
  private final PieceClient pieceClient;
  private final UsersClient usersClient;
  private final SearchClient searchClient;
  private final ErrorMapper errorMapper = new ErrorMapper();
  private String centralTenantId;

  /**
   * Default constructor.
   *
   * @param okapiHeaders - Map of okapiHeaders: token, url, tenant
   */
  public FolioFacade(Map<String, String> okapiHeaders) {
    this.okapiHeaders = okapiHeaders;
    this.inventoryClient = new InventoryClient(okapiHeaders, webClient);
    this.circulationClient = new CirculationClient(okapiHeaders, webClient);
    this.requestClient = new CirculationRequestClient(okapiHeaders, webClient);
    this.pieceClient = new PieceClient(okapiHeaders, webClient);
    this.searchClient = new SearchClient(okapiHeaders, webClient);
    this.usersClient = new UsersClient(okapiHeaders, webClient);
  }

  /**
   * Returns batch info for instances items and holdings.
   *
   * @param rtacRequest - request params
   * @return items and holdings for instances
   */
  public Future<RtacHoldingsBatch> getItemAndHoldingInfo(RtacRequest rtacRequest) {
    Promise<RtacHoldingsBatch> promise = Promise.promise();
    final var folioToRtacMapper = new FolioToRtacMapper(rtacRequest.getFullPeriodicals());

    final var validUuids =
        rtacRequest.getInstanceIds().stream()
            .filter(this::validateUuid)
            .collect(Collectors.toList());

    if (CollectionUtils.isEmpty(validUuids)) {
      promise.fail(
          new HttpException(HttpStatus.HTTP_NOT_FOUND.toInt(), "Could not find instances"));
      return promise.future();
    }
    String tenantId = TenantTool.calculateTenantId(okapiHeaders.get(OKAPI_HEADER_TENANT));

    return isCentralTenant(tenantId)
        .compose(isCentral -> findHoldingsAndItemsTenants(isCentral, tenantId, validUuids))
        .compose(instanceTenantsMap -> Future.all(
            instanceTenantsMap.entrySet().stream()
                .map(entry -> getItemAndHoldingsForTenant(entry.getValue(), entry.getKey()))
                .toList()))
        .compose(instanceItemsAndHoldings -> Future.succeededFuture(
            mergeTenantData(instanceItemsAndHoldings.list())))
        .compose(instancesAndPieces -> {
          List<String> notFoundHoldings = new ArrayList<>();
          List<String> notFoundInstances = new ArrayList<>(rtacRequest.getInstanceIds());
          final var rtacHoldingsList = new ArrayList<RtacHoldings>();
          for (InventoryHoldingsAndItemsAndPieces instanceAndPieces : instancesAndPieces) {
            InventoryHoldingsAndItems instance = instanceAndPieces
                .getInventoryHoldingsAndItems();

            notFoundInstances.remove(instance.getInstanceId());
            if (CollectionUtils.isEmpty(instance.getHoldings())) {
              notFoundHoldings.add(instance.getInstanceId());
            }
            var rtacHoldings = folioToRtacMapper.mapToRtac(instanceAndPieces);
            rtacHoldingsList.add(rtacHoldings);
          }
          logger.info("Mapping inventory instances: {}", rtacHoldingsList.size());
          final var result = new RtacHoldingsBatch();
          List<Error> errors = new ArrayList<>();
          if (!notFoundInstances.isEmpty()) {
            errors.addAll(errorMapper.mapInstanceNotFound(notFoundInstances));
            logger.info("Instance not found errors: {}", errors.size());
            logger.debug("Errors: {}", errors);
          }
          if (!notFoundHoldings.isEmpty()) {
            errors.addAll(errorMapper.mapHoldingsNotFound(notFoundHoldings));
            logger.info("Holdings not found errors: {}", errors.size());
            logger.debug("Errors: {}", errors);
          }

          if (errors.isEmpty()) {
            result.withErrors(null);
          } else {
            result.withErrors(errors);
          }
          promise.complete(result.withHoldings(rtacHoldingsList));
          return promise.future();
        })
        .onFailure(promise::fail);
  }

  /**
   * Returns RTAC for the specified id.
   *
   * @param instanceId passed instances id
   * @return items and holdings for instances
   * @deprecated will be removed soon, use {@link FolioFacade#getItemAndHoldingInfo(RtacRequest)}
   */
  @Deprecated(since = "1.6.0")
  public Future<LegacyHoldings> getItemAndHoldingInfo(String instanceId) {
    Promise<LegacyHoldings> promise = Promise.promise();
    final var folioToRtacMapper = new FolioToRtacMapper(false);

    if (!validateUuid(instanceId)) {
      promise.fail(new HttpException(HttpStatus.HTTP_NOT_FOUND.toInt(), "Could not find instance"));
      return promise.future();
    }
    String tenantId = TenantTool.calculateTenantId(okapiHeaders.get(OKAPI_HEADER_TENANT));

    inventoryClient
        .getItemAndHoldingInfo(List.of(instanceId), tenantId)
        .compose((items) -> circulationClient.updateInstanceItemsWithLoansDueDate(items, tenantId))
        .compose((loans) -> requestClient.updateInstanceItemsWithRequestsCount(loans, tenantId))
        .onSuccess(
            instances -> {
              logger.info("Mapping inventory instances: {}", instances.size());
              final var first = instances.stream().findFirst();
              first.ifPresentOrElse(
                  i -> {
                    var holdings = folioToRtacMapper.mapToLegacy(i);
                    promise.complete(holdings);
                  },
                  () -> promise.fail(
                      new HttpException(HttpStatus.HTTP_NOT_FOUND.toInt(), "Not Found")));
            })
        .onFailure(promise::fail);
    return promise.future();
  }

  private Future<List<InventoryHoldingsAndItemsAndPieces>> getItemAndHoldingsForTenant(
      List<String> instanceIds,
      String tenantId) {
    return inventoryClient.getItemAndHoldingInfo(instanceIds, tenantId)
        .compose(
            itemAndHoldings -> circulationClient.updateInstanceItemsWithLoansDueDate(
                itemAndHoldings,
                tenantId))
        .compose(
            itemAndHoldings -> requestClient.updateInstanceItemsWithRequestsCount(
                itemAndHoldings,
                tenantId))
        .compose(
            itemAndHoldings -> pieceClient.getPieces(itemAndHoldings, tenantId, centralTenantId));
  }

  private List<InventoryHoldingsAndItemsAndPieces> mergeTenantData(
      List<List<InventoryHoldingsAndItemsAndPieces>> tenantHoldings) {
    Map<String, InventoryHoldingsAndItemsAndPieces> instanceHoldingsMap = new HashMap<>();
    tenantHoldings.stream()
        .flatMap(List::stream)
        .forEach(holdings -> {
          if (instanceHoldingsMap.containsKey(
              holdings.getInventoryHoldingsAndItems().getInstanceId())) {
            mergeInventoryHoldings(
                instanceHoldingsMap.get(holdings.getInventoryHoldingsAndItems().getInstanceId()),
                holdings);
          } else {
            instanceHoldingsMap.put(holdings.getInventoryHoldingsAndItems().getInstanceId(),
                holdings);
          }
        });
    return instanceHoldingsMap.values().stream().toList();
  }


  private void mergeInventoryHoldings(InventoryHoldingsAndItemsAndPieces inventory1,
      InventoryHoldingsAndItemsAndPieces inventory2) {
    var pieces = new ArrayList<>(inventory1.getPieces());
    pieces.addAll(inventory2.getPieces());
    inventory1.setPieces(pieces);
    var holdings = new ArrayList<>(inventory1.getInventoryHoldingsAndItems().getHoldings());
    holdings.addAll(inventory2.getInventoryHoldingsAndItems().getHoldings());
    inventory1.getInventoryHoldingsAndItems().setHoldings(holdings);
    var items = new ArrayList<>(inventory1.getInventoryHoldingsAndItems().getItems());
    items.addAll(inventory2.getInventoryHoldingsAndItems().getItems());
    inventory1.getInventoryHoldingsAndItems().setItems(items);
    var natureOfContent = new ArrayList<>(
        inventory1.getInventoryHoldingsAndItems().getNatureOfContent());
    natureOfContent.addAll(inventory2.getInventoryHoldingsAndItems().getNatureOfContent());
    inventory1.getInventoryHoldingsAndItems().setNatureOfContent(natureOfContent);
  }

  private boolean validateUuid(String uuid) {
    try {
      UUID.fromString(uuid);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private Future<Boolean> isCentralTenant(String tenantId) {
    Promise<Boolean> promise = Promise.promise();
    usersClient.getUserTenants(tenantId).onComplete(ar -> {
      if (ar.failed()) {
        promise.fail(ar.cause());
        return;
      }
      var userTenants = ar.result();
      if (userTenants.getTotalRecords() > 0
          && userTenants.getUserTenants().get(0).getCentralTenantId() != null) {
        centralTenantId = userTenants.getUserTenants().get(0).getCentralTenantId();
        promise.complete(userTenants.getUserTenants().get(0).getCentralTenantId().equals(tenantId));
      } else {
        promise.complete(false);
      }
    });
    return promise.future();
  }

  private Future<Map<String, List<String>>> findHoldingsAndItemsTenants(Boolean isCentral,
      String tenantId,
      List<String> instanceIds) {
    Promise<Map<String, List<String>>> promise = Promise.promise();
    if (!isCentral) {
      promise.complete(Map.of(tenantId, instanceIds));
      return promise.future();
    }
    var searchFutures = instanceIds.stream()
        .map(this::searchTenantsForInstance)
        .toList();
    Future.all(searchFutures).onComplete(ar -> {
      if (ar.failed()) {
        promise.fail(ar.cause());
        return;
      }
      List<InstanceTenants> instanceTenants = ar.result().list();
      promise.complete(getInstanceTenantsMap(instanceTenants));
    });
    return promise.future();
  }

  private Map<String, List<String>> getInstanceTenantsMap(List<InstanceTenants> instanceTenants) {
    Map<String, List<String>> instanceTenantMap = new HashMap<>();
    instanceTenants.forEach(instanceTenant -> {
      instanceTenant.getTenantIds().forEach((tenantId -> {
        if (instanceTenantMap.containsKey(tenantId)) {
          instanceTenantMap.get(tenantId).add(instanceTenant.getInstanceId());
        } else {
          var instanceList = new ArrayList<String>();
          instanceList.add(instanceTenant.getInstanceId());
          instanceTenantMap.put(tenantId, instanceList);
        }
      }));
    });
    return instanceTenantMap;
  }

  private Future<InstanceTenants> searchTenantsForInstance(String instanceId) {
    Promise<InstanceTenants> promise = Promise.promise();
    searchClient.getHoldingsTenantsFacet(instanceId).onComplete(ar -> {
      if (ar.failed()) {
        promise.fail(ar.cause());
        return;
      }
      var instanceTenants = new InstanceTenants();
      instanceTenants.setInstanceId(instanceId);
      if (ar.result().getFacets().getHoldingsTenantId().getTotalRecords() > 0) {
        var tenantIds = ar.result().getFacets().getHoldingsTenantId().getValues().stream()
            .map(Value::getId).toList();
        instanceTenants.setTenantIds(tenantIds);
      } else {
        instanceTenants.setTenantIds(Collections.EMPTY_LIST);
      }
      promise.complete(instanceTenants);
    });
    return promise.future();
  }
}
