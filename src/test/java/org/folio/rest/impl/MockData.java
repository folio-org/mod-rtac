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
import java.util.Collections;
import org.folio.rest.jaxrs.model.InventoryHoldingsAndItems;
import org.folio.rest.jaxrs.model.RtacRequest;

public class MockData {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static final String INSTANCE_JSON_PATH =
      "/mock-data/inventory-view/test_instance_with_holding_and_item.json";
  private static final String LOAN_JSON_PATH = "/mock-data/loan-storage/test_loan.json";
  private static final String EMPTY_LOANS_JSON_PATH = "/mock-data/loan-storage/empty_loans.json";
  private static final String INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE;

  public static final String LOAN_DUE_DATE_FIELD_VALUE = "2017-01-19T12:42:21.000+0000";

  public static final String INSTANCE_ID = "76d5a72a-af24-4ac6-8e73-4e39604f6f59";
  public static final String INSTANCE_ITEM_ID = "645549b1-2a73-4251-b8bb-39598f773a93";

  public static final String ITEM_WITHOUT_LOAN_ID = "d4567775-0832-4ded-8bf9-e35c238ef309";
  public static final String ITEM_ID_LOAN_STORAGE_ERROR = "8ee46203-182c-42ea-916d-07448345e073";

  public static final String INSTANCE_ID_WITH_NO_LOANS_ITEM =
      "e8c27121-dd98-4c28-a782-597ad1787c75";
  public static final String INSTANCE_ID_INVENTORY_VIEW_ERROR =
      "a705e5dd-46c6-46c9-a0d8-70400a27b7c4";
  public static final String NONEXISTENT_INSTANCE_ID = "207dda4d-06dd-4822-856e-63ca5b6c7f1a";
  public static final String INSTANCE_ID_NO_ITEMS_AND_HOLDINGS =
      "4ed2a3b3-2fb4-414c-aa6f-a265685ca5a6";

  public static final String LOAN_JSON;
  public static final String EMPTY_LOANS_JSON;

  public static final InventoryHoldingsAndItems INSTANCE_WITH_HOLDINGS_AND_ITEMS;
  public static final InventoryHoldingsAndItems INSTANCE_WITHOUT_HOLDINGS_AND_ITEMS;
  public static final InventoryHoldingsAndItems INSTANCE_WITH_ITEM_WHICH_HAS_NOT_LOANS;
  public static final InventoryHoldingsAndItems INSTANCE_LOAN_STORAGE_ERROR;

  public static final RtacRequest VALID_INSTANCE_IDS_RTAC_REQUEST;
  public static final RtacRequest RTAC_REQUEST_WITH_INSTANCE_NO_LOANS_ITEM;
  public static final RtacRequest RTAC_REQUEST_WITH_INSTANCE_ID_LOAN_STORAGE_ERROR;
  public static final RtacRequest RTAC_REQUEST_WITH_INSTANCE_ID_INVENTORY_VIEW_ERROR;
  public static final RtacRequest RTAC_REQUEST_WITH_NON_EXISTED_INSTANCE_ID;
  public static final RtacRequest RTAC_REQUEST_WITH_INSTANCE_NO_ITEMS_AND_HOLDINGS;

  static {
    LOAN_JSON = getJsonObjectFromFile(LOAN_JSON_PATH);
    EMPTY_LOANS_JSON = getJsonObjectFromFile(EMPTY_LOANS_JSON_PATH);

    // === inventory view responses ====
    INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE = getJsonObjectFromFile(INSTANCE_JSON_PATH);

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

    // === Rtac requests ====
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
