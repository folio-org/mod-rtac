package org.folio.rest.impl;

import java.util.Collections;

import org.folio.rest.jaxrs.model.RtacHoldingsBatch;
import org.folio.rest.jaxrs.model.RtacRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MockData {

  static final RtacHoldingsBatch rtac = new RtacHoldingsBatch();

  public static final RtacRequest requestWithValidInstanceId = new RtacRequest().withInstanceIds(Collections.singletonList("76d5a72a-af24-4ac6-8e73-4e39604f6f59"));


  public static String pojoToJson(Object pojo) throws JsonProcessingException {
    return new ObjectMapper().writeValueAsString(pojo);
  }

}
