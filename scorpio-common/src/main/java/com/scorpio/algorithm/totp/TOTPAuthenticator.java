package com.scorpio.algorithm.totp;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.scorpio.Constants;

/**
 * @author guosk
 *
 * create at 2023年12月04日, machloop
 */
public class TOTPAuthenticator {

  private static final Logger LOGGER = LoggerFactory.getLogger(TOTPAuthenticator.class);

  private final int QR_WIDTH = 300;
  private final int QR_HEIGHT = 300;
  private final String QR_FORMAT = "png";
  private final String QR_CONTENT_TYPE = "image/png";
  // 生成的key长度( Generate secret key length)
  public static final int SECRET_SIZE = 10;

  public static final String SEED = "g8GjEvTbW5oVSV7avL47357438reyhreyuryetredLDVKs2m0QN7vxRs2im5MDaNCWGmcD2rvcZx";
  // Java实现随机数算法
  public static final String RANDOM_NUMBER_ALGORITHM = "SHA1PRNG";
  // 最多可偏移的时间
  int window_size = 3; // default 3 - max 17

  private final TOTPConfig config;

  public TOTPAuthenticator() {
    this.config = new TOTPConfig();
  }

  public TOTPAuthenticator(TOTPConfig config) {
    if (config == null) {
      throw new IllegalArgumentException("Configuration cannot be null.");
    }

    this.config = config;
  }


  public String generateSecretKey() {
    SecureRandom sr = null;
    try {
      sr = SecureRandom.getInstance(RANDOM_NUMBER_ALGORITHM);
      sr.setSeed(Base64.decodeBase64(SEED.getBytes()));
      byte[] buffer = sr.generateSeed(SECRET_SIZE);
      Base32 codec = new Base32();
      byte[] bEncodedKey = codec.encode(buffer);
      String encodedKey = new String(bEncodedKey);
      return encodedKey;
    } catch (NoSuchAlgorithmException e) {
      // should never occur... configuration error
    }
    return null;
  }


  public void generatorQR(String username, String secret, HttpServletResponse response)
      throws IOException {
    decodeSecret(secret);
    secret = StringUtils.substringBefore(secret, "=");

    // 设置二维码的参数
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
    try {
      BitMatrix bitMatrix = multiFormatWriter.encode(this.config.getQRInfoForUser(username, secret),
          BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT);
      BufferedImage bufferedImage = new BufferedImage(QR_WIDTH, QR_HEIGHT,
          BufferedImage.TYPE_INT_RGB);
      for (int x = 0; x < QR_WIDTH; x++) {
        for (int y = 0; y < QR_HEIGHT; y++) {
          bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
        }
      }

      ImageIO.write(bufferedImage, QR_FORMAT, out);
    } catch (WriterException | IOException e) {
      LOGGER.warn("generator QR error.", e);
    }

    response.setHeader("Cache-Control", "no-store");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);
    response.setContentType(QR_CONTENT_TYPE);

    ServletOutputStream responseOutputStream = response.getOutputStream();
    responseOutputStream.write(out.toByteArray());
    responseOutputStream.flush();
    responseOutputStream.close();
  }

  public int calculateCode(byte[] key, long tm) {
    // Allocating an array of bytes to represent the specified instant of time.
    byte[] data = new byte[Constants.BYTE_BITS];
    long value = tm;

    // Converting the instant of time from the long representation to a big-endian array of bytes
    // (RFC4226, 5.2. Description).
    for (int i = Constants.BYTE_BITS; i-- > 0; value >>>= Constants.BYTE_BITS) {
      data[i] = (byte) value;
    }

    // Building the secret key specification for the HmacSHA1 algorithm.
    SecretKeySpec signKey = new SecretKeySpec(key, config.getHmacHashFunction().toString());

    try {
      // Getting an HmacSHA1/HmacSHA256 algorithm implementation from the JCE.
      Mac mac = Mac.getInstance(config.getHmacHashFunction().toString());

      // Initializing the MAC algorithm.
      mac.init(signKey);

      // Processing the instant of time and getting the encrypted data.
      byte[] hash = mac.doFinal(data);

      // Building the validation code performing dynamic truncation
      // (RFC4226, 5.3. Generating an HOTP value)
      int offset = hash[hash.length - 1] & 0xF;

      // We are using a long because Java hasn't got an unsigned integer type
      // and we need 32 unsigned bits).
      long truncatedHash = 0;

      for (int i = 0; i < 4; ++i) {
        truncatedHash <<= Constants.BYTE_BITS;

        // Java bytes are signed but we need an unsigned integer:
        // cleaning off all but the LSB.
        truncatedHash |= (hash[offset + i] & 0xFF);
      }

      // Clean bits higher than the 32nd (inclusive) and calculate the
      // module with the maximum validation code value.
      truncatedHash &= 0x7FFFFFFF;
      truncatedHash %= config.getKeyModulus();

      // Returning the validation code to the caller.
      return (int) truncatedHash;
    } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
      // Logging the exception.
      LOGGER.warn(ex.getMessage(), ex);
      return 0;
    }
  }

  public int getTotpPassword(String secret) {
    return getTotpPassword(secret, new Date().getTime());
  }

  public int getTotpPassword(String secret, long time) {
    return calculateCode(decodeSecret(secret), getTimeWindowFromTime(time));
  }

  public boolean authorize(String secret, int verificationCode) {
    return authorize(secret, verificationCode, new Date().getTime());
  }

  public boolean authorize(String secret, int verificationCode, long time) {
    // Checking user input and failing if the secret key was not provided.
    if (secret == null) {
      throw new IllegalArgumentException("Secret cannot be null.");
    }

    // Checking if the verification code is between the legal bounds.
    if (verificationCode <= 0 || verificationCode >= this.config.getKeyModulus()) {
      return false;
    }

    // Checking the validation code using the current UNIX time.
    return checkCode(secret, verificationCode, time, this.config.getWindowSize());
  }

  private byte[] decodeSecret(String secret) {
    switch (config.getKeyRepresentation()) {
      case BASE32:
        Base32 codec32 = new Base32();
        return codec32.decode(secret.toUpperCase());
      case BASE64:
        Base64 codec64 = new Base64();
        return codec64.decode(secret);
      default:
        throw new IllegalArgumentException("Unknown key representation type.");
    }
  }

  private long getTimeWindowFromTime(long time) {
    return time / this.config.getTimeStepSizeInMillis();
  }

  private boolean checkCode(String secret, long code, long timestamp, int window) {
    byte[] decodedKey = decodeSecret(secret);

    // convert unix time into a 30 second "window" as specified by the
    // TOTP specification. Using Google's default interval of 30 seconds.
    final long timeWindow = getTimeWindowFromTime(timestamp);

    // Calculating the verification code of the given key in each of the
    // time intervals and returning true if the provided code is equal to
    // one of them.
    for (int i = -((window - 1) / 2); i <= window / 2; ++i) {
      // Calculating the verification code for the current time interval.
      long hash = calculateCode(decodedKey, timeWindow + i);

      // Checking if the provided code is equal to the calculated one.
      if (hash == code) {
        // The verification code is valid.
        return true;
      }
    }

    // The verification code is invalid.
    return false;
  }

}
