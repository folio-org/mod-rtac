package org.folio.rtac.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.URLEncoder;
import java.util.Random;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Test utils class.
 * 
 * @author mreno
 *
 */
public final class Utils {
  private static final Logger logger = LogManager.getLogger("okapi");

  private Utils() {
    super();
  }

  /**
   * Returns a random ephemeral port that has a high probability of not being
   * in use.
   *
   * @return a random port number.
   */
  public static int getRandomPort() {
    int port = -1;
    do {
      // Use a random ephemeral port
      port = new Random().nextInt(16_384) + 49_152;
      try {
        final ServerSocket socket = new ServerSocket(port);
        socket.close();
      } catch (IOException e) {
        continue;
      }
      break;
    } while (true);

    return port;
  }

  /**
   * Read in a JSON mock file.
   * 
   * @param path file location
   * @return the file contents as a string
   */
  public static String readMockFile(final String path) {
    try {
      final InputStream is = Utils.class.getClassLoader().getResourceAsStream(path);

      if (is != null) {
        return IOUtils.toString(is, "UTF-8");
      } else {
        return "";
      }
    } catch (Throwable e) {
      logger.error(String.format("Unable to read mock configuration in %s file", path));
    }

    return "";
  }

  /**
   * Encodes a query parameter.
   * 
   * @param value the query parameter key or value
   * @return the encoded result
   */
  public static String encode(String value) {
    try {
      return URLEncoder.encode(value, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      logger.error("JVM unable to encode using UTF-8...", e);
      throw new IllegalStateException(e);
    }
  }

}
