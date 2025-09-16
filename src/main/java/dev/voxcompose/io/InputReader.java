package dev.voxcompose.io;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Efficient input reader with support for large inputs. Uses buffered I/O for optimal performance.
 */
public class InputReader {
  private static final int BUFFER_SIZE = 16384; // 16KB buffer

  /**
   * Read all input from stdin efficiently. Uses a larger buffer size for better performance with
   * large inputs.
   */
  public static String readStdin() throws IOException {
    return readFromStream(System.in);
  }

  /** Read all input from a stream efficiently. */
  public static String readFromStream(InputStream inputStream) throws IOException {
    try (BufferedInputStream bis = new BufferedInputStream(inputStream, BUFFER_SIZE)) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[BUFFER_SIZE];
      int bytesRead;

      while ((bytesRead = bis.read(buffer)) != -1) {
        baos.write(buffer, 0, bytesRead);
      }

      return baos.toString(StandardCharsets.UTF_8);
    }
  }

  /**
   * Read input with a size limit to prevent memory issues.
   *
   * @param maxSizeBytes Maximum size in bytes to read
   * @return The input string, truncated if necessary
   */
  public static String readStdinWithLimit(int maxSizeBytes) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[BUFFER_SIZE];
    int totalRead = 0;
    int bytesRead;

    try (BufferedInputStream bis = new BufferedInputStream(System.in, BUFFER_SIZE)) {
      while ((bytesRead = bis.read(buffer)) != -1 && totalRead < maxSizeBytes) {
        int toWrite = Math.min(bytesRead, maxSizeBytes - totalRead);
        baos.write(buffer, 0, toWrite);
        totalRead += toWrite;

        if (totalRead >= maxSizeBytes) {
          System.err.println("Warning: Input truncated at " + maxSizeBytes + " bytes");
          break;
        }
      }
    }

    return baos.toString(StandardCharsets.UTF_8);
  }

  /** Check if stdin has available input without blocking. */
  public static boolean hasInput() throws IOException {
    return System.in.available() > 0;
  }
}
