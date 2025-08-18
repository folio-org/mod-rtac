package org.folio.rest.impl;

import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.folio.models.InventoryHoldingsAndItemsAndPieces;
import org.folio.rest.jaxrs.model.InventoryHoldingsAndItems;
import org.folio.rest.jaxrs.model.PieceCollection;
import org.folio.rest.jaxrs.model.RtacRequest;

public class MockData {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static final String INSTANCE_JSON_PATH =
      "/mock-data/inventory-view/test_instance_with_holding_and_item.json";
  private static final String INSTANCE_JSON_PATH_TEST_TENANT_0001 =
      "/mock-data/inventory-view/test_instance_with_holding_and_item_test_tenant_0001.json";
  private static final String INSTANCE_FOR_HOLD_COUNT_JSON_PATH =
      "/mock-data/inventory-view/test_instance_with_holding_and_item_for_holdcount.json";

  private static final String LOAN_JSON_PATH = "/mock-data/loan-storage/test_loan.json";
  private static final String EMPTY_LOANS_JSON_PATH = "/mock-data/loan-storage/empty_loans.json";
  public static final String REQUESTS_JSON_PATH =
      "/mock-data/circulation-requests/test_requests.json";
  public static final String REQUESTS_HOLD_COUNT_JSON_PATH =
      "/mock-data/circulation-requests/circulation_requests_holdcount.json";

  public static final String EMPTY_PIECE_COLLECTION_JSON_PATH =
      "/mock-data/pieces/empty_piece_collection.json";
  public static final String USERS_CENTRAL_TENANT_JSON_PATH =
      "/mock-data/users/user_tenants.json";
  public static final String USERS_TENANT_JSON_PATH =
      "/mock-data/users/user_tenants_non_consortia.json";
  public static final String HOLDINGS_FACET_JSON_PATH =
      "/mock-data/search/holdings_facet.json";
  public static final String HOLDINGS_FACET_WITH_1_TENANT_JSON_PATH =
      "/mock-data/search/holdings_facet_with_1_tenant.json";
  public static final String EMPTY_SETTINGS_JSON_PATH =
      "/mock-data/settings/empty-settings.json";
  public static final String SETTINGS_JSON_PATH =
      "/mock-data/settings/settings.json";
  public static final String ORDER_PIECES_PATH = "/mock-data/pieces/piece_collection.json";
  private static final String INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE;
  private static final String INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE_TEST_TENANT_0001;
  public static final String LOAN_DUE_DATE_FIELD_VALUE = "2017-01-19T12:42:21.000+0000";

  public static final String INSTANCE_ID = "76d5a72a-af24-4ac6-8e73-4e39604f6f59";
  public static final String INSTANCE_ITEM_ID_1 = "645549b1-2a73-4251-b8bb-39598f773a93";
  public static final String INSTANCE_ITEM_ID_2 = "4cebe27e-c9ba-4ce8-8148-fe4d4cb40538";

  public static final String HOLDING_ID_WITH_PIECES = "3c91f915-1f43-404f-87fa-6a01fbc5b81a";
  public static final String HOLDING_ID_WITH_PIECES_IN_CENTRAL
      = "9u21f915-1f43-404f-87fa-6a01fbc5b81a";
  public static final String HOLDING_ID_WITHOUT_PIECES = "0005bb50-7c9b-48b0-86eb-178a494e25fe";

  public static final String ITEM_WITHOUT_LOAN_ID = "d4567775-0832-4ded-8bf9-e35c238ef309";
  public static final String ITEM_ID_LOAN_STORAGE_ERROR = "8ee46203-182c-42ea-916d-07448345e073";

  public static final String INSTANCE_ID_WITH_NO_LOANS_ITEM =
      "e8c27121-dd98-4c28-a782-597ad1787c75";
  public static final String INSTANCE_ID_INVENTORY_VIEW_ERROR =
      "a705e5dd-46c6-46c9-a0d8-70400a27b7c4";
  public static final String NONEXISTENT_INSTANCE_ID = "207dda4d-06dd-4822-856e-63ca5b6c7f1a";
  public static final String INSTANCE_ID_NO_ITEMS_AND_HOLDINGS =
      "4ed2a3b3-2fb4-414c-aa6f-a265685ca5a6";
  public static final String INSTANCE_ID_NO_FULL_PERIODICALS =
      "4ed2a3b3-2fb4-414c-aa6f-a265685ca5a7";
  public static final String INSTANCE_ID_HOLDINGS_NO_ITEMS =
      "3af1a3b3-2fb4-414c-aa6f-a265699ca5b6";
  public static final String INSTANCE_ID_NOT_EXISTS =
      "16757796-da9a-4435-959b-88ce4f2ec272";
  public static final String INSTANCE_ID_HOLDINGS_AND_PIECES =
      "1207f53a-9290-4358-a36b-712040b66ad9";
  public static final String INSTANCE_ID_HOLDINGS_AND_NO_PIECES =
      "78651120-83c1-4666-8083-53126cc1f6dc";
  public static final String INSTANCE_ID_HOLDINGS_AND_PIECES_IN_CONSORTIA =
      "36551120-83c1-4666-8083-53126cc1f6dc";

  public static final String UUID_400 = "c031f1d4-09b0-11eb-adc1-0242ac120002";
  public static final String UUID_500 = "c031f1d4-09b0-11eb-adc1-0242ac120003";
  public static final String UUID_403 = "c031f1d4-09b0-11eb-adc1-0242ac120004";
  public static final String UUID_404 = "c031f1d4-09b0-11eb-adc1-0242ac120005";

  public static final String LOAN_JSON;
  public static final String REQUESTS_JSON;
  public static final String REQUESTS_HOLD_COUNT_JSON;
  public static final String EMPTY_LOANS_JSON;
  public static final String EMPTY_PIECE_COLLECTION_JSON;
  public static final String USERS_CENTRAL_TENANT_JSON;
  public static final String USERS_NON_CONSORTIA_TENANT_JSON;
  public static final String HOLDINGS_FACET_JSON;
  public static final String HOLDINGS_FACET_WITH_1_TENANT_JSON;

  public static final InventoryHoldingsAndItems INSTANCE_WITH_HOLDINGS_AND_ITEMS;
  public static final InventoryHoldingsAndItems INSTANCE_WITH_HOLDINGS_AND_ITEMS_TEST_TENANT_0001;
  public static final InventoryHoldingsAndItems INSTANCE_WITHOUT_HOLDINGS_AND_ITEMS;
  public static final InventoryHoldingsAndItems INSTANCE_WITH_ITEM_WHICH_HAS_NOT_LOANS;
  public static final InventoryHoldingsAndItems INSTANCE_LOAN_STORAGE_ERROR;
  public static final InventoryHoldingsAndItems INSTANCE_WITH_HOLDINGS_NO_ITEMS;
  public static final InventoryHoldingsAndItems INSTANCE_NO_FULL_PERIODICALS;
  public static final InventoryHoldingsAndItems INSTANCE_WITH_HOLDINGS_AND_PIECES;
  public static final InventoryHoldingsAndItems INSTANCE_WITH_HOLDINGS_AND_PIECES_IN_CONSORTIA;
  public static final InventoryHoldingsAndItems INSTANCE_WITH_HOLDINGS_AND_NO_PIECES;

  public static final RtacRequest VALID_INSTANCE_IDS_RTAC_REQUEST;
  public static final RtacRequest RTAC_REQUEST_WITH_INSTANCE_NO_LOANS_ITEM;
  public static final RtacRequest RTAC_REQUEST_WITH_INSTANCE_ID_LOAN_STORAGE_ERROR;
  public static final RtacRequest RTAC_REQUEST_WITH_INSTANCE_ID_INVENTORY_VIEW_ERROR;
  public static final RtacRequest RTAC_REQUEST_WITH_NON_EXISTED_INSTANCE_ID;
  public static final RtacRequest RTAC_REQUEST_WITH_INSTANCE_NO_ITEMS_AND_HOLDINGS;
  public static final RtacRequest RTAC_REQUEST_MIXED_INSTANCES_WITH_ITEMS_AND_NO_HOLDINGS;
  public static final RtacRequest RTAC_REQUEST_WITH_INSTANCE_HOLDINGS_NO_ITEMS;
  public static final RtacRequest RTAC_REQUEST_MIXED_INSTANCES_WITH_ITEMS_AND_NO_ITEMS;
  public static final RtacRequest RTAC_REQUEST_WITH_INSTANCE_IN_CONSORTIA;
  public static final RtacRequest RTAC_REQUEST_WITH_INSTANCE_AND_PIECES_IN_CONSORTIA;

  public static final RtacRequest RTAC_REQUEST_WITH_NOT_VALID_IDS;

  public static final RtacRequest RTAC_REQUEST_WITH_EMPTY_RESPONSE;
  public static final RtacRequest RTAC_REQUEST_WITH_INSTANCE_AND_PIECES;
  public static final String ORDER_PIECES_TEMPLATE;
  public static final PieceCollection PIECE_COLLECTION;
  public static final PieceCollection PIECE_COLLECTION_FOR_CENTRAL_TENANT;

  public static final String EMPTY_SETTINGS_JSON;
  public static final String SETTINGS_JSON;

  static {
    LOAN_JSON = getJsonObjectFromFile(LOAN_JSON_PATH);
    EMPTY_LOANS_JSON = getJsonObjectFromFile(EMPTY_LOANS_JSON_PATH);
    REQUESTS_JSON = getJsonObjectFromFile(REQUESTS_JSON_PATH);
    REQUESTS_HOLD_COUNT_JSON = getJsonObjectFromFile(REQUESTS_HOLD_COUNT_JSON_PATH);
    EMPTY_PIECE_COLLECTION_JSON = getJsonObjectFromFile(EMPTY_PIECE_COLLECTION_JSON_PATH);
    USERS_CENTRAL_TENANT_JSON = getJsonObjectFromFile(USERS_CENTRAL_TENANT_JSON_PATH);
    USERS_NON_CONSORTIA_TENANT_JSON = getJsonObjectFromFile(USERS_TENANT_JSON_PATH);
    HOLDINGS_FACET_JSON = getJsonObjectFromFile(HOLDINGS_FACET_JSON_PATH);
    HOLDINGS_FACET_WITH_1_TENANT_JSON =
        getJsonObjectFromFile(HOLDINGS_FACET_WITH_1_TENANT_JSON_PATH);

    // === inventory view responses ====
    INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE = getJsonObjectFromFile(INSTANCE_JSON_PATH);
    INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE_TEST_TENANT_0001 = getJsonObjectFromFile(
        INSTANCE_JSON_PATH_TEST_TENANT_0001);

    INSTANCE_WITH_HOLDINGS_NO_ITEMS =
        stringToPojo(INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE, InventoryHoldingsAndItems.class);

    INSTANCE_WITH_HOLDINGS_NO_ITEMS.setInstanceId(INSTANCE_ID_HOLDINGS_NO_ITEMS);
    INSTANCE_WITH_HOLDINGS_NO_ITEMS.withModeOfIssuance("serial");
    INSTANCE_WITH_HOLDINGS_NO_ITEMS.setItems(Collections.emptyList());

    INSTANCE_WITHOUT_HOLDINGS_AND_ITEMS =
        stringToPojo(INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE, InventoryHoldingsAndItems.class);
    INSTANCE_WITHOUT_HOLDINGS_AND_ITEMS.setInstanceId(INSTANCE_ID_NO_ITEMS_AND_HOLDINGS);
    INSTANCE_WITHOUT_HOLDINGS_AND_ITEMS.setItems(Collections.emptyList());
    INSTANCE_WITHOUT_HOLDINGS_AND_ITEMS.setHoldings(Collections.emptyList());

    INSTANCE_WITH_HOLDINGS_AND_ITEMS =
        stringToPojo(INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE, InventoryHoldingsAndItems.class);
    INSTANCE_WITH_HOLDINGS_AND_ITEMS_TEST_TENANT_0001 = stringToPojo(
        INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE_TEST_TENANT_0001, InventoryHoldingsAndItems.class);

    INSTANCE_WITH_ITEM_WHICH_HAS_NOT_LOANS =
        stringToPojo(INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE, InventoryHoldingsAndItems.class);
    INSTANCE_WITH_ITEM_WHICH_HAS_NOT_LOANS.setInstanceId(INSTANCE_ID_WITH_NO_LOANS_ITEM);
    setItemId(INSTANCE_WITH_ITEM_WHICH_HAS_NOT_LOANS, ITEM_WITHOUT_LOAN_ID);

    INSTANCE_LOAN_STORAGE_ERROR =
        stringToPojo(INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE, InventoryHoldingsAndItems.class);
    setItemId(INSTANCE_LOAN_STORAGE_ERROR, ITEM_ID_LOAN_STORAGE_ERROR);

    INSTANCE_NO_FULL_PERIODICALS =
        MockData.stringToPojo(
                INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE, InventoryHoldingsAndItems.class)
            .withModeOfIssuance("serial")
            .withInstanceId(INSTANCE_ID_NO_FULL_PERIODICALS);

    INSTANCE_WITH_HOLDINGS_AND_PIECES =
        stringToPojo(INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE, InventoryHoldingsAndItems.class)
            .withInstanceId(INSTANCE_ID_HOLDINGS_AND_PIECES)
            .withModeOfIssuance("serial")
            .withItems(Collections.emptyList());
    INSTANCE_WITH_HOLDINGS_AND_PIECES.getHoldings().get(0).withId(HOLDING_ID_WITH_PIECES);

    INSTANCE_WITH_HOLDINGS_AND_PIECES_IN_CONSORTIA =
        stringToPojo(INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE, InventoryHoldingsAndItems.class)
            .withInstanceId(INSTANCE_ID_HOLDINGS_AND_PIECES_IN_CONSORTIA)
            .withModeOfIssuance("serial")
            .withItems(Collections.emptyList());
    INSTANCE_WITH_HOLDINGS_AND_PIECES_IN_CONSORTIA.getHoldings().get(0)
        .withId(HOLDING_ID_WITH_PIECES_IN_CENTRAL);

    INSTANCE_WITH_HOLDINGS_AND_NO_PIECES =
        stringToPojo(INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE, InventoryHoldingsAndItems.class)
            .withInstanceId(INSTANCE_ID_HOLDINGS_AND_NO_PIECES);

    // === Rtac requests ====
    RTAC_REQUEST_MIXED_INSTANCES_WITH_ITEMS_AND_NO_ITEMS =
        new RtacRequest().withInstanceIds(
            Arrays.asList(INSTANCE_ID_HOLDINGS_NO_ITEMS, INSTANCE_ID_WITH_NO_LOANS_ITEM));
    RTAC_REQUEST_MIXED_INSTANCES_WITH_ITEMS_AND_NO_HOLDINGS =
        new RtacRequest().withInstanceIds(
            Arrays.asList(INSTANCE_ID_NO_ITEMS_AND_HOLDINGS, INSTANCE_ID));
    VALID_INSTANCE_IDS_RTAC_REQUEST =
        new RtacRequest().withInstanceIds(Collections.singletonList(INSTANCE_ID));
    RTAC_REQUEST_WITH_INSTANCE_NO_LOANS_ITEM =
        new RtacRequest()
            .withInstanceIds(Collections.singletonList(INSTANCE_ID_WITH_NO_LOANS_ITEM));
    RTAC_REQUEST_WITH_INSTANCE_ID_LOAN_STORAGE_ERROR =
        new RtacRequest().withInstanceIds(Collections.singletonList(NONEXISTENT_INSTANCE_ID));
    RTAC_REQUEST_WITH_INSTANCE_ID_INVENTORY_VIEW_ERROR =
        new RtacRequest()
            .withInstanceIds(Collections.singletonList(INSTANCE_ID_INVENTORY_VIEW_ERROR));
    RTAC_REQUEST_WITH_NON_EXISTED_INSTANCE_ID =
        new RtacRequest().withInstanceIds(Collections.singletonList(NONEXISTENT_INSTANCE_ID));
    RTAC_REQUEST_WITH_INSTANCE_NO_ITEMS_AND_HOLDINGS =
        new RtacRequest()
            .withInstanceIds(Collections.singletonList(INSTANCE_ID_NO_ITEMS_AND_HOLDINGS));
    RTAC_REQUEST_WITH_INSTANCE_HOLDINGS_NO_ITEMS =
        new RtacRequest()
            .withInstanceIds(Collections.singletonList(INSTANCE_ID_HOLDINGS_NO_ITEMS));
    RTAC_REQUEST_WITH_INSTANCE_IN_CONSORTIA =
        new RtacRequest()
            .withInstanceIds(List.of(INSTANCE_ID));
    RTAC_REQUEST_WITH_NOT_VALID_IDS =
        new RtacRequest()
            .withInstanceIds(Collections.singletonList("qwe"));
    RTAC_REQUEST_WITH_EMPTY_RESPONSE =
        new RtacRequest()
            .withInstanceIds(Collections.singletonList(INSTANCE_ID_NOT_EXISTS));
    RTAC_REQUEST_WITH_INSTANCE_AND_PIECES =
        new RtacRequest()
            .withInstanceIds(Collections.singletonList(INSTANCE_ID_HOLDINGS_AND_PIECES));
    RTAC_REQUEST_WITH_INSTANCE_AND_PIECES_IN_CONSORTIA =
        new RtacRequest()
            .withInstanceIds(
                Collections.singletonList(INSTANCE_ID_HOLDINGS_AND_PIECES_IN_CONSORTIA));

    // === order pieces responses ====
    ORDER_PIECES_TEMPLATE = getJsonObjectFromFile(ORDER_PIECES_PATH);

    PIECE_COLLECTION = stringToPojo(ORDER_PIECES_TEMPLATE, PieceCollection.class);
    PIECE_COLLECTION_FOR_CENTRAL_TENANT = stringToPojo(ORDER_PIECES_TEMPLATE,
        PieceCollection.class);
    PIECE_COLLECTION_FOR_CENTRAL_TENANT.getPieces()
        .forEach(piece -> piece.setHoldingId(HOLDING_ID_WITH_PIECES_IN_CENTRAL));

    EMPTY_SETTINGS_JSON = getJsonObjectFromFile(EMPTY_SETTINGS_JSON_PATH);
    SETTINGS_JSON = getJsonObjectFromFile(SETTINGS_JSON_PATH);
  }

  static String pojoToJson(Object pojo) {
    try {
      return objectMapper.writeValueAsString(pojo);
    } catch (JsonProcessingException ex) {
      throw new IllegalArgumentException("Invalid pojo object. " + ex);
    }
  }

  static <T> T stringToPojo(String json, Class<T> clazz) {
    try {
      return objectMapper.readValue(json, clazz);
    } catch (JsonProcessingException ex) {
      throw new IllegalArgumentException("Invalid json. Cannot parse json structure." + ex);
    }
  }

  public static InventoryHoldingsAndItemsAndPieces createInventoryHoldingsAndItemsAndNoPieces() {
    return new InventoryHoldingsAndItemsAndPieces(INSTANCE_WITH_HOLDINGS_AND_NO_PIECES,
        Collections.emptyList());
  }

  public static InventoryHoldingsAndItemsAndPieces createInventoryHoldingsAndItemsAndPieces() {
    return new InventoryHoldingsAndItemsAndPieces(INSTANCE_WITH_HOLDINGS_AND_PIECES,
        PIECE_COLLECTION.getPieces());
  }

  public static InventoryHoldingsAndItems createInventoryHoldingsAndItemsForHoldCount() {
    String json = getJsonObjectFromFile(INSTANCE_FOR_HOLD_COUNT_JSON_PATH);
    return stringToPojo(json, InventoryHoldingsAndItems.class);
  }

  private static String getJsonObjectFromFile(String path) {
    try {
      URL resource = MockData.class.getResource(path);
      if (resource == null) {
        return null;
      }
      File file = new File(resource.getFile());
      byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
      return new String(encoded, StandardCharsets.UTF_8);
    } catch (IOException e) {
      fail(e.getMessage());
    }
    return null;
  }

  private static void setItemId(InventoryHoldingsAndItems holdingsAndItems, String id) {
    holdingsAndItems.getItems().iterator().next().setId(id);
  }
}
