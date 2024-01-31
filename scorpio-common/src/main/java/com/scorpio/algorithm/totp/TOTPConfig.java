package com.scorpio.algorithm.totp;

import java.util.concurrent.TimeUnit;

/**
 * @author guosk
 *
 * create at 2023年12月04日, machloop
 */
public class TOTPConfig {

  private long timeStepSizeInMillis = TimeUnit.SECONDS.toMillis(60);
  private int windowSize = 3;
  private int codeDigits = 6;
  private int keyModulus = (int) Math.pow(10, codeDigits);
  private String ISSUER = "machloop";
  private KeyRepresentation keyRepresentation = KeyRepresentation.BASE32;
  private HmacHashFunction hmacHashFunction = HmacHashFunction.HmacSHA1;
  private HmacHashFunction algorithmHmacHashFunction = HmacHashFunction.SHA1;

  /**
   * Returns the key module.
   *
   * @return the key module.
   */
  public int getKeyModulus() {
    return keyModulus;
  }

  /**
   * Returns the key representation.
   *
   * @return the key representation.
   */
  public KeyRepresentation getKeyRepresentation() {
    return keyRepresentation;
  }

  /**
   * Returns the number of digits in the generated code.
   *
   * @return the number of digits in the generated code.
   */
  @SuppressWarnings("UnusedDeclaration")
  public int getCodeDigits() {
    return codeDigits;
  }

  /**
   * Returns the time step size, in milliseconds, as specified by RFC 6238.
   * The default value is 30.000.
   *
   * @return the time step size in milliseconds.
   */
  public long getTimeStepSizeInMillis() {
    return timeStepSizeInMillis;
  }

  /**
   * Returns an integer value representing the number of windows of size
   * timeStepSizeInMillis that are checked during the validation process,
   * to account for differences between the server and the client clocks.
   * The bigger the window, the more tolerant the library code is about
   * clock skews.
   * <p>
   * We are using Google's default behaviour of using a window size equal
   * to 3.  The limit on the maximum window size, present in older
   * versions of this library, has been removed.
   *
   * @return the window size.
   * @see #timeStepSizeInMillis
   */
  public int getWindowSize() {
    return windowSize;
  }

  /**
   * Returns the cryptographic hash function used to calculate the HMAC (Hash-based
   * Message Authentication Code). This implementation uses the SHA1 hash
   * function by default.
   * <p>
   *
   * @return the HMAC hash function.
   */
  public HmacHashFunction getHmacHashFunction() {
    return hmacHashFunction;
  }

  public static class TOTPConfigBuilder {
    private TOTPConfig config = new TOTPConfig();

    public TOTPConfig build() {
      return config;
    }

    public TOTPConfigBuilder setCodeDigits(int codeDigits) {
      if (codeDigits <= 0) {
        throw new IllegalArgumentException("Code digits must be positive.");
      }

      if (codeDigits < 6) {
        throw new IllegalArgumentException("The minimum number of digits is 6.");
      }

      if (codeDigits > 8) {
        throw new IllegalArgumentException("The maximum number of digits is 8.");
      }

      config.codeDigits = codeDigits;
      config.keyModulus = (int) Math.pow(10, codeDigits);
      return this;
    }

    public TOTPConfigBuilder setTimeStepSizeInMillis(long timeStepSizeInMillis) {
      if (timeStepSizeInMillis <= 0) {
        throw new IllegalArgumentException("Time step size must be positive.");
      }

      config.timeStepSizeInMillis = timeStepSizeInMillis;
      return this;
    }

    public TOTPConfigBuilder setWindowSize(int windowSize) {
      if (windowSize <= 0) {
        throw new IllegalArgumentException("Window number must be positive.");
      }

      config.windowSize = windowSize;
      return this;
    }

    public TOTPConfigBuilder setKeyRepresentation(KeyRepresentation keyRepresentation) {
      if (keyRepresentation == null) {
        throw new IllegalArgumentException("Key representation cannot be null.");
      }

      config.keyRepresentation = keyRepresentation;
      return this;
    }

    public TOTPConfigBuilder setHmacHashFunction(HmacHashFunction hmacHashFunction) {
      if (hmacHashFunction == null) {
        throw new IllegalArgumentException("HMAC Hash Function cannot be null.");
      }

      config.hmacHashFunction = hmacHashFunction;
      return this;
    }
  }

  public String getQRInfo(String secret) {
    StringBuilder info = new StringBuilder();
    info.append("otpauth://totp/machloop:machloop?issuer=machloop");
    info.append("&secret=").append(secret);
    info.append("&period=")
        .append(TimeUnit.SECONDS.convert(timeStepSizeInMillis, TimeUnit.MILLISECONDS));
    info.append("&digits=").append(codeDigits);
    info.append("&algorithm=").append(algorithmHmacHashFunction.toString());
    info.append("&keyRepresentation=").append(keyRepresentation.toString());

    return info.toString();
  }

  public String getQRInfoForUser(String username, String secret) {
    StringBuilder info = new StringBuilder();
    info.append("otpauth://totp/").append(ISSUER + ":").append(username).append("?issuer=")
        .append(ISSUER);
    info.append("&secret=").append(secret);
    info.append("&period=")
        .append(TimeUnit.SECONDS.convert(timeStepSizeInMillis, TimeUnit.MILLISECONDS));
    info.append("&digits=").append(codeDigits);
    info.append("&algorithm=").append(algorithmHmacHashFunction.toString());
    info.append("&keyRepresentation=").append(keyRepresentation.toString());

    return info.toString();
  }

  public enum KeyRepresentation {
    BASE32, BASE64
  }

  public enum HmacHashFunction {
    HmacSHA1, HmacSHA256, HmacSHA512, SHA1
  }

}
