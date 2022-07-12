package com.scorpio.algorithm.uuid;

import com.scorpio.Constants;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author liumeng
 *
 * create at 2018年12月11日, alpha-common
 */
public class TimeBasedUuid {

  // class loading is atomic - this is a lazy & safe singleton to be used by this package
  private static final SecureRandom RANDOM = new SecureRandom();

  // We only use bottom 3 bytes for the sequence number. Paranoia:
  // init with random int so that if JVM/OS/machine goes down, clock slips
  // backwards, and JVM comes back up, we are less likely to be on the same sequenceNumber
  // at the same time:
  private final AtomicInteger sequenceNumber = new AtomicInteger(RANDOM.nextInt());

  // Used to ensure clock moves forward:
  private long lastTimestamp;

  private static final byte[] SECURE_MUNGED_ADDRESS = MacAddressProvider.getSecureMungedAddress();

  static {
    assert SECURE_MUNGED_ADDRESS.length == Constants.MAC_ADDRESS_BYTES;
  }

  // protected for testing
  protected long currentTimeMillis() {
    return System.currentTimeMillis();
  }

  // protected for testing
  protected byte[] macAddress() {
    return SECURE_MUNGED_ADDRESS;
  }

  public String getBase64UUID() {
    final int sequenceId = sequenceNumber.incrementAndGet() & 0xffffff;
    long timestamp = currentTimeMillis();

    synchronized (this) {
      // Don't let timestamp go backwards, at least "on our watch" (while this JVM is running).
      // We are still vulnerable if we are
      // shut down, clock goes backwards, and we restart... for this we randomize
      // the sequenceNumber on init to decrease chance of collision:
      timestamp = Math.max(lastTimestamp, timestamp);

      if (sequenceId == 0) {
        // Always force the clock to increment whenever sequence number is 0,
        // in case we have a long time-slip backwards:
        timestamp++;
      }

      lastTimestamp = timestamp;
    }

    final byte[] uuidBytes = new byte[15];
    int i = 0;

    // We have auto-generated ids, which are usually used for append-only workloads.
    // So we try to optimize the order of bytes for indexing speed (by having quite
    // unique bytes close to the beginning of the ids so that sorting is fast) and
    // compression (by making sure we share common prefixes between enough ids),
    // but not necessarily for lookup speed (by having the leading bytes identify
    // segments whenever possible)

    // Blocks in the block tree have between 25 and 48 terms. So all prefixes that
    // are shared by ~30 terms should be well compressed. I first tried putting the
    // two lower bytes of the sequence id in the beginning of the id, but compression
    // is only triggered when you have at least 30*2^16 ~= 2M documents in a segment,
    // which is already quite large. So instead, we are putting the 1st and 3rd byte
    // of the sequence number so that compression starts to be triggered with smaller
    // segment sizes and still gives pretty good indexing speed. We use the sequenceId
    // rather than the timestamp because the distribution of the timestamp depends too
    // much on the indexing rate, so it is less reliable.

    uuidBytes[i++] = (byte) sequenceId;
    // changes every 65k docs, so potentially every second if you have a steady indexing rate
    uuidBytes[i++] = (byte) (sequenceId >>> 2 * Constants.BYTE_BITS);

    // Now we start focusing on compression and put bytes that should not change too often.
    uuidBytes[i++] = (byte) (timestamp >>> 2 * Constants.BYTE_BITS); // changes every ~65 secs
    uuidBytes[i++] = (byte) (timestamp >>> 3 * Constants.BYTE_BITS); // changes every ~4.5h
    uuidBytes[i++] = (byte) (timestamp >>> 4 * Constants.BYTE_BITS); // changes every ~50 days
    uuidBytes[i++] = (byte) (timestamp >>> 5 * Constants.BYTE_BITS); // changes every 35 years
    byte[] macAddress = macAddress();
    assert macAddress.length == Constants.MAC_ADDRESS_BYTES;
    System.arraycopy(macAddress, 0, uuidBytes, i, macAddress.length);
    i += macAddress.length;

    // Finally we put the remaining bytes, which will likely not be compressed at all.
    uuidBytes[i++] = (byte) (timestamp >>> Constants.BYTE_BITS);
    uuidBytes[i++] = (byte) (sequenceId >>> Constants.BYTE_BITS);
    uuidBytes[i++] = (byte) timestamp;

    assert i == uuidBytes.length;

    return Base64.getUrlEncoder().withoutPadding().encodeToString(uuidBytes);
  }
}

