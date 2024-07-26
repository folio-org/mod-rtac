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
import org.folio.models.InventoryHoldingsAndItemsAndPieces;
import org.folio.rest.jaxrs.model.InventoryHoldingsAndItems;
import org.folio.rest.jaxrs.model.PieceCollection;
import org.folio.rest.jaxrs.model.RtacRequest;

public class MockData {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static final String INSTANCE_JSON_PATH =
      "/mock-data/inventory-view/test_instance_with_holding_and_item.json";

  private static final String INSTANCE_WITHOUT_ITEM_JSON_PATH =
      "/mock-data/inventory-view/test_instance_with_holding.json";
  private static final String LOAN_JSON_PATH = "/mock-data/loan-storage/test_loan.json";
  private static final String EMPTY_LOANS_JSON_PATH = "/mock-data/loan-storage/empty_loans.json";
  public static final String REQUESTS_JSON_PATH =
      "/mock-data/circulation-requests/test_requests.json";
  public static final String EMPTY_PIECE_COLLECTION_JSON_PATH =
      "/mock-data/pieces/empty_piece_collection.json";
  public static final String ORDER_PIECES_PATH = "/mock-data/pieces/piece_collection.json";
  private static final String INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE;
  private static final String INSTANCE_WITHOUT_ITEM_TEMPLATE;

  public static final String LOAN_DUE_DATE_FIELD_VALUE = "2017-01-19T12:42:21.000+0000";

  public static final String INSTANCE_ID = "76d5a72a-af24-4ac6-8e73-4e39604f6f59";
  public static final String INSTANCE_ITEM_ID_1 = "645549b1-2a73-4251-b8bb-39598f773a93";
  public static final String INSTANCE_ITEM_ID_2 = "4cebe27e-c9ba-4ce8-8148-fe4d4cb40538";
  public static final String HOLDING_ID = "0005bb50-7c9b-48b0-86eb-178a494e25fe";
  public static final String HOLDING_WITHOUT_PIECE_ID = "9f66e7e6-0476-4734-a407-9b3ebbc8cdde";

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
  public static final String INSTANCE_ID_HOLDINGS_NO_PIECES =
      "77819cf1-9045-4f1f-ba4a-f05c29ad08ec";

  public static final String UUID_400 = "c031f1d4-09b0-11eb-adc1-0242ac120002";
  public static final String UUID_500 = "c031f1d4-09b0-11eb-adc1-0242ac120003";
  public static final String UUID_403 = "c031f1d4-09b0-11eb-adc1-0242ac120004";
  public static final String UUID_404 = "c031f1d4-09b0-11eb-adc1-0242ac120005";

  public static final String LOAN_JSON;
  public static final String REQUESTS_JSON;
  public static final String EMPTY_LOANS_JSON;
  public static final String EMPTY_PIECE_COLLECTION_JSON;

  public static final InventoryHoldingsAndItems INSTANCE_WITH_HOLDINGS_AND_ITEMS;
  public static final InventoryHoldingsAndItems INSTANCE_WITHOUT_HOLDINGS_AND_ITEMS;
  public static final InventoryHoldingsAndItems INSTANCE_WITH_ITEM_WHICH_HAS_NOT_LOANS;
  public static final InventoryHoldingsAndItems INSTANCE_LOAN_STORAGE_ERROR;
  public static final InventoryHoldingsAndItems INSTANCE_WITH_HOLDINGS_NO_ITEMS;
  public static final InventoryHoldingsAndItems INSTANCE_NO_FULL_PERIODICALS;
  public static final InventoryHoldingsAndItems INSTANCE_WITHOUT_ITEM_HOLDING_AND_PIECE;

  public static final RtacRequest VALID_INSTANCE_IDS_RTAC_REQUEST;
  public static final RtacRequest RTAC_REQUEST_WITH_INSTANCE_NO_LOANS_ITEM;
  public static final RtacRequest RTAC_REQUEST_WITH_INSTANCE_ID_LOAN_STORAGE_ERROR;
  public static final RtacRequest RTAC_REQUEST_WITH_INSTANCE_ID_INVENTORY_VIEW_ERROR;
  public static final RtacRequest RTAC_REQUEST_WITH_NON_EXISTED_INSTANCE_ID;
  public static final RtacRequest RTAC_REQUEST_WITH_INSTANCE_NO_ITEMS_AND_HOLDINGS;
  public static final RtacRequest RTAC_REQUEST_WITH_INSTANCE_HOLDINGS_NO_ITEMS;
  public static final RtacRequest RTAC_REQUEST_MIXED_INSTANCES_WITH_ITEMS_AND_NO_ITEMS;

  public static final RtacRequest RTAC_REQUEST_WITH_NOT_VALID_IDS;

  public static final RtacRequest RTAC_REQUEST_WITH_EMPTY_RESPONSE;
  public static final RtacRequest RTAC_REQUEST_WITH_INSTANCE_NO_PIECES;
  public static final String ORDER_PIECES_TEMPLATE;
  public static final PieceCollection PIECE_COLLECTION;

  static {
    LOAN_JSON = getJsonObjectFromFile(LOAN_JSON_PATH);
    EMPTY_LOANS_JSON = getJsonObjectFromFile(EMPTY_LOANS_JSON_PATH);
    REQUESTS_JSON = getJsonObjectFromFile(REQUESTS_JSON_PATH);
    EMPTY_PIECE_COLLECTION_JSON = getJsonObjectFromFile(EMPTY_PIECE_COLLECTION_JSON_PATH);

    // === inventory view responses ====
    INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE = getJsonObjectFromFile(INSTANCE_JSON_PATH);
    INSTANCE_WITHOUT_ITEM_TEMPLATE = getJsonObjectFromFile(INSTANCE_WITHOUT_ITEM_JSON_PATH);

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

    INSTANCE_WITHOUT_ITEM_HOLDING_AND_PIECE =
        stringToPojo(INSTANCE_WITHOUT_ITEM_TEMPLATE, InventoryHoldingsAndItems.class);

    // === Rtac requests ====
    RTAC_REQUEST_MIXED_INSTANCES_WITH_ITEMS_AND_NO_ITEMS =
        new RtacRequest().withInstanceIds(
          Arrays.asList(INSTANCE_ID_HOLDINGS_NO_ITEMS, INSTANCE_ID_WITH_NO_LOANS_ITEM));
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
    RTAC_REQUEST_WITH_NOT_VALID_IDS =
        new RtacRequest()
            .withInstanceIds(Collections.singletonList("qwe"));
    RTAC_REQUEST_WITH_EMPTY_RESPONSE =
        new RtacRequest()
            .withInstanceIds(Collections.singletonList(INSTANCE_ID_NOT_EXISTS));
    RTAC_REQUEST_WITH_INSTANCE_NO_PIECES =
        new RtacRequest()
            .withInstanceIds(Collections.singletonList(INSTANCE_ID_HOLDINGS_NO_PIECES));

    // === order pieces responses ====
    ORDER_PIECES_TEMPLATE = getJsonObjectFromFile(ORDER_PIECES_PATH);

    PIECE_COLLECTION = stringToPojo(ORDER_PIECES_TEMPLATE, PieceCollection.class);
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

  public static InventoryHoldingsAndItems createInventoryHoldingsAndItems() {
    return stringToPojo(INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE, InventoryHoldingsAndItems.class);
  }

  public static InventoryHoldingsAndItemsAndPieces createInventoryHoldingsAndItemsAndPieces() {
    return new InventoryHoldingsAndItemsAndPieces(INSTANCE_WITH_HOLDINGS_AND_ITEMS,
        PIECE_COLLECTION.getPieces());
  }

  private static String getJsonObjectFromFile(String path) {
    try {
      URL resource = MockServer.class.getResource(path);
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
