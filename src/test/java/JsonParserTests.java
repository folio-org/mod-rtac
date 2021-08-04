import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.JsonParser;
import lombok.SneakyThrows;

public class JsonParserTests {
  @Test
  public void endsWhenBufferContainsJson() {
    final var emptyBuffer = Buffer.buffer(new JsonObject().put("foo", "bar").encodePrettily());

    Promise<Boolean> completionPromise = Promise.promise();

    JsonParser parser = createParser(completionPromise);

    parser.handle(emptyBuffer);
    parser.end();

    final Boolean ended = valueFrom(completionPromise);

    assertThat(ended, is(true));
  }

  @Test
  public void endsWhenBufferIsEmpty() {
    final var emptyBuffer = Buffer.buffer();

    Promise<Boolean> completionPromise = Promise.promise();

    JsonParser parser = createParser(completionPromise);

    parser.handle(emptyBuffer);
    parser.end();

    final Boolean ended = valueFrom(completionPromise);

    assertThat(ended, is(true));
  }

  @Test
  public void throwsExceptionWhenBufferIsNull() {
    JsonParser parser = createParser(Promise.promise());

    assertThrows(NullPointerException.class, () -> parser.handle(null));
  }

  private JsonParser createParser(Promise<Boolean> completionPromise) {
    final var parser = JsonParser.newParser();

    parser.objectValueMode();

    parser.exceptionHandler(err -> { });

    parser.handler(e -> { });

    parser.endHandler(e -> completionPromise.complete(true));

    return parser;
  }

  @SneakyThrows
  private Boolean valueFrom(Promise<Boolean> completionPromise) {
    return futureFrom(completionPromise).get(1, SECONDS);
  }

  private CompletableFuture<Boolean> futureFrom(Promise<Boolean> completionPromise) {
    return completionPromise.future().toCompletionStage().toCompletableFuture();
  }
}
