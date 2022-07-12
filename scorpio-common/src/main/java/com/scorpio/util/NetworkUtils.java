package com.scorpio.util;

import com.google.common.net.InetAddresses;
import com.scorpio.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.http.conn.util.InetAddressUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * ip/cidr
 */
public final class NetworkUtils {

    private static final String IPV4_ZERO = "0.0.0.0";

    private NetworkUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isInetAddress(String ipAddress) {
        return isInetAddress(ipAddress, IpVersion.ALL);
    }

    public static boolean isInetAddress(String ipAddress, IpVersion version) {
        if (version == IpVersion.ALL) {
            return InetAddresses.isInetAddress(ipAddress);
        } else if (version == IpVersion.V4) {
            return InetAddressUtils.isIPv4Address(ipAddress) || StringUtils.equals(ipAddress, IPV4_ZERO);
        } else if (version == IpVersion.V6) {
            return InetAddresses.isInetAddress(ipAddress) && !(InetAddressUtils.isIPv4Address(ipAddress)
                    || StringUtils.equals(ipAddress, IPV4_ZERO));
        }
        return false;
    }

    public static boolean isInetAddressPort(String port) {
        boolean result = false;
        try {
            int portNum = Integer.parseInt(port);
            result = portNum >= 0 && portNum <= 65535;
        } catch (NumberFormatException e) {
            result = false;
        }

        return result;
    }

    public static long ip2Long(String ipAddress) {

        long result = 0;

        if (StringUtils.isBlank(ipAddress)) {
            return result;
        }

        try {
            String[] ipAddressInArray = ipAddress.split("\\.");
            for (int i = Constants.IPV4_ADDRESS_BYTES; i > 0; i--) {
                long ip = Long.parseLong(ipAddressInArray[Constants.IPV4_ADDRESS_BYTES - i]);
                result |= ip << ((i - 1) * Constants.BYTE_BITS);
            }
        } catch (NumberFormatException e) {
            result = 0;
        }
        return result;
    }

    public static String long2Ip(long i) {
        return ((i >> 3 * Constants.BYTE_BITS) & 0xFF) + "." + ((i >> 2 * Constants.BYTE_BITS) & 0xFF)
                + "." + ((i >> Constants.BYTE_BITS) & 0xFF) + "." + (i & 0xFF);
    }

    public static Tuple2<Long, Long> ip2Range(String ipAddress) {
        if (StringUtils.contains(ipAddress, "-")) {
            String[] ipRange = StringUtils.split(ipAddress, "-");
            return Tuples.of(ip2Long(ipRange[0]), ip2Long(ipRange[1]));
        } else if (StringUtils.contains(ipAddress, "/")) {
            SubnetUtils utils = new SubnetUtils(ipAddress);
            utils.setInclusiveHostCount(true);
            return Tuples.of(ip2Long(utils.getInfo().getLowAddress()),
                    ip2Long(utils.getInfo().getHighAddress()));
        } else {
            return Tuples.of(ip2Long(ipAddress), ip2Long(ipAddress));
        }
    }

    public static boolean isCidr(String cidr) {
        return isCidr(cidr, IpVersion.ALL);
    }

    public static boolean isCidr(String cidr, IpVersion version) {
        boolean result = false;
        if (StringUtils.contains(cidr, "/")) {
            int index = cidr.indexOf("/");
            String addressPart = cidr.substring(0, index);
            int networkPart = 0;
            try {
                networkPart = Integer.parseInt(cidr.substring(index + 1));
            } catch (NumberFormatException e) {
                return result;
            }

            if (isInetAddress(addressPart)) {
                if (InetAddressUtils.isIPv4Address(addressPart)) {
                    if (networkPart >= 0 && networkPart <= 32) {
                        result = (version == IpVersion.V4 || version == IpVersion.ALL);
                    }
                } else {
                    if (networkPart >= 0 && networkPart <= 128) {
                        result = (version == IpVersion.V6 || version == IpVersion.ALL);
                    }
                }
            }
        }

        return result;
    }

    /**
     * 根据掩码位数获得掩码
     * @param depth
     * @return
     */
    public static int getMaskByDepth(int depth) {
        return 0x80000000 >> (depth - 1);
    }

    /**
     * 根据掩码获得掩码位
     *
     * @param netmarks
     * @return
     */
    public static int getNetMask(String netmarks) {
        int count = 0;
        String[] ipList = netmarks.split("\\.");
        for (String partMask : ipList) {
            String binary = Integer.toBinaryString(Integer.parseInt(partMask));
            String effective = StringUtils.substringBefore(binary, "0");
            count += effective.length();
            if (!StringUtils.equals(binary, effective)) {
                break;
            }
        }
        return count;
    }

    public enum IpVersion {
        V4, V6, ALL
    }
}
