package org.folio.rest.impl;

import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;

import com.google.common.cache.CacheBuilder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.resource.interfaces.InitAPI;

public class Init implements InitAPI {

  private final Logger logger = LogManager.getLogger(getClass());
  private static final Integer RTAC_CACHE_EXPIRATION_TIME = 10; // in minutes
  public static final String RTAC_CACHE_NAME = "rtac-cache";

  @Override
  public void init(Vertx vertx, Context context, Handler<AsyncResult<Boolean>> handler) {
    try {
      var cache = CacheBuilder.newBuilder()
          .expireAfterWrite(RTAC_CACHE_EXPIRATION_TIME, TimeUnit.MINUTES).build();
      context.put(RTAC_CACHE_NAME, cache);
      handler.handle(succeededFuture(true));
    } catch (Exception e) {
      logger.error("Initialization failed", e);
      handler.handle(failedFuture(e.getMessage()));
    }
  }
}
