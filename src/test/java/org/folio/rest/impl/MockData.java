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


  private static final String TEST_INSTANCE_JSON_PATH = "/mock-data/inventory-view/test_instance_with_holding_and_item.json";
  private static final String TEST_LOAN_JSON_PATH = "/mock-data/loan-storage/test_loan.json";

  public static final String TEST_LOAN_DUE_DATE_FIELD_VALUE = "2017-01-19T12:42:21.000+0000";

  public static final String TEST_INSTANCE_ID = "76d5a72a-af24-4ac6-8e73-4e39604f6f59";
  public static final String TEST_INSTANCE_HOLDING_ID = "0005bb50-7c9b-48b0-86eb-178a494e25fe";
  public static final String TEST_INSTANCE_ITEM_ID = "645549b1-2a73-4251-b8bb-39598f773a93";
  private static final String TEST_INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE = getJsonObjectFromFile(TEST_INSTANCE_JSON_PATH);

  public static final InventoryHoldingsAndItems TEST_INSTANCE_WITH_HOLDINGS_AND_ITEMS = (InventoryHoldingsAndItems) stringToPojo(TEST_INSTANCE_WITH_ITEM_AND_HOLDING_TEMPLATE, InventoryHoldingsAndItems.class);

  public static final RtacRequest VALID_INSTANCE_IDS_RTAC_REQUEST = new RtacRequest().withInstanceIds(Collections.singletonList(TEST_INSTANCE_ID));
  public static final String TEST_LOAN_JSON = getJsonObjectFromFile(TEST_LOAN_JSON_PATH);

  public static String pojoToJson(Object pojo) {
    try {
      return objectMapper.writeValueAsString(pojo);
    } catch (JsonProcessingException ex) {
      throw new IllegalArgumentException("Invalid pojo object. " +ex);
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

}
