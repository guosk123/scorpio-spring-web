package com.scorpio.algorithm.uuid;

import com.scorpio.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.SecureRandom;
import java.util.Enumeration;


/**
 * 
 * @author liumeng
 *
 * create at 2018年12月11日, alpha-common
 */
public final class MacAddressProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(MacAddressProvider.class);

  private MacAddressProvider() {
    throw new IllegalStateException("Utility class");
  }

  private static byte[] getMacAddress() throws SocketException {
    Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
    if (en != null) {
      while (en.hasMoreElements()) {
        NetworkInterface nint = en.nextElement();
        if (nint.isLoopback()) {
          continue;
        }

        // Pick the first valid non loopback address we find
        byte[] address = nint.getHardwareAddress();
        if (isValidAddress(address)) {
          return address;
        }
      }
    }
    // Could not find a mac address
    return new byte[0];
  }

  private static boolean isValidAddress(byte[] address) {
    if (address == null || address.length != Constants.MAC_ADDRESS_BYTES) {
      return false;
    }
    for (byte b : address) {
      if (b != 0x00) {
        return true; // If any of the bytes are non zero assume a good address
      }
    }
    return false;
  }

  public static byte[] getSecureMungedAddress() {
    byte[] address = null;
    try {
      address = getMacAddress();
    } catch (SocketException e) {
      LOGGER.debug("get hardware address failed.", e);
    }

    if (!isValidAddress(address)) {
      address = constructDummyMulticastAddress();
    }

    byte[] mungedBytes = new byte[Constants.MAC_ADDRESS_BYTES];
    SecureRandomHolder.INSTANCE.nextBytes(mungedBytes);
    for (int i = 0; i < Constants.MAC_ADDRESS_BYTES; ++i) {
      mungedBytes[i] ^= address[i];
    }

    return mungedBytes;
  }

  private static byte[] constructDummyMulticastAddress() {
    byte[] dummy = new byte[Constants.MAC_ADDRESS_BYTES];
    SecureRandomHolder.INSTANCE.nextBytes(dummy);
    /*
     * Set the broadcast bit to indicate this is not a _real_ mac address
     */
    dummy[0] |= (byte) 0x01;
    return dummy;
  }

  public static final class SecureRandomHolder {
    // class loading is atomic - this is a lazy & safe singleton to be used by this package
    public static final SecureRandom INSTANCE = new SecureRandom();

    private SecureRandomHolder() {
      throw new IllegalStateException("Utility class");
    }
  }
}

