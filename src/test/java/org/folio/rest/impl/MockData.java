package org.folio.rest.impl;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import org.folio.rest.jaxrs.model.InventoryHoldingsAndItems;
import org.folio.rest.jaxrs.model.RtacRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class MockData {

  private static final Logger logger = LoggerFactory.getLogger(MockData.class);

  private static ObjectMapper objectMapper = new ObjectMapper();


  private static final String INSTANCE_JSON_PATH = "/mock-data/inventory-view/test_instance_with_holding_and_item.json";
  private static final String LOAN_JSON_PATH = "/mock-data/loan-storage/test_loan.json";
  private static final String EMPTY_LOANS_JSON_PATH = "/mock-data/loan-storage/empty_loans.json";

  public static final String LOAN_DUE_DATE_FIELD_VALUE = "2017-01-19T12:42:21.000+0000";

  public static final String INSTANCE_ID = "76d5a72a-af24-4ac6-8e73-4e39604f6f59";
  public static final String INSTANCE_HOLDING_ID = "0005bb50-7c9b-48b0-86eb-178a494e25fe";
  public static final String INSTANCE_ITEM_ID = "645549b1-2a73-4251-b8bb-39598f773a93";

  public static final String ITEM_WITHOUT_LOAN_ID = "d4567775-0832-4ded-8bf9-e35c238ef309";
  public static final String INSTANCE_ID_WITH_NO_LOANS_ITEM = "e8c27121-dd98-4c28-a782-597ad1787c75";

  private static final String INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE = getJsonObjectFromFile(INSTANCE_JSON_PATH);
  public static final String LOAN_JSON;
  public static final String EMPTY_LOANS_JSON;

  public static final InventoryHoldingsAndItems TEST_INSTANCE_WITH_HOLDINGS_AND_ITEMS;
  public static final InventoryHoldingsAndItems TEST_INSTANCE_WITH_ITEM_WHICH_HAS_NOT_LOANS;

  public static final RtacRequest VALID_INSTANCE_IDS_RTAC_REQUEST;
  public static final RtacRequest RTAC_REQUEST_WITH_INSTANCE_NO_LOANS_ITEM;

  static {
    LOAN_JSON = getJsonObjectFromFile(LOAN_JSON_PATH);
    EMPTY_LOANS_JSON = getJsonObjectFromFile(EMPTY_LOANS_JSON_PATH);

    TEST_INSTANCE_WITH_HOLDINGS_AND_ITEMS = (InventoryHoldingsAndItems) stringToPojo(INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE, InventoryHoldingsAndItems.class);

    TEST_INSTANCE_WITH_ITEM_WHICH_HAS_NOT_LOANS = ((InventoryHoldingsAndItems) stringToPojo(INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE, InventoryHoldingsAndItems.class));
    TEST_INSTANCE_WITH_ITEM_WHICH_HAS_NOT_LOANS.setInstanceId(INSTANCE_ID_WITH_NO_LOANS_ITEM);
    setItemId(TEST_INSTANCE_WITH_ITEM_WHICH_HAS_NOT_LOANS, ITEM_WITHOUT_LOAN_ID);

    VALID_INSTANCE_IDS_RTAC_REQUEST = new RtacRequest().withInstanceIds(Collections.singletonList(INSTANCE_ID));
    RTAC_REQUEST_WITH_INSTANCE_NO_LOANS_ITEM = new RtacRequest().withInstanceIds(Collections.singletonList(INSTANCE_ID_WITH_NO_LOANS_ITEM));
  }

  public static String pojoToJson(Object pojo) {
    try {
      return objectMapper.writeValueAsString(pojo);
    } catch (JsonProcessingException ex) {
      throw new IllegalArgumentException("Invalid pojo object. " + ex);
    }
  }

  public static Object stringToPojo(String json, Class clazz) {
    try {
      return objectMapper.readValue(json, clazz);
    } catch (JsonProcessingException ex) {
      throw new IllegalArgumentException("Invalid json. Cannot parse json structure." + ex);
    }
  }

  private static String getJsonObjectFromFile(String path) {
    try {
      logger.debug("Loading file " + path);
      URL resource = MockServer.class.getResource(path);
      if (resource == null) {
        return null;
      }
      File file = new File(resource.getFile());
      byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
      return new String(encoded, StandardCharsets.UTF_8);
    } catch (IOException e) {
      logger.error("Unexpected error", e);
      fail(e.getMessage());
    }
    return null;
  }


  private static void setItemId(InventoryHoldingsAndItems holdingsAndItems, String id) {
    holdingsAndItems.getItems().iterator().next().setId(id);
  }
}
