package com.machloop.fpc.cms.common.helper;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.common.Constants;

public class ProtocolHelper {

  private static final int ALL_PROTOCOL = 4095;

  private ProtocolHelper() {
    throw new IllegalStateException("Utility class");
  }

  public static String analyticalProtocol(int protocol) {

    if (protocol == ALL_PROTOCOL) {
      return "ALL";
    }

    StringBuilder builder = new StringBuilder();

    char[] protocolArray = StringUtils.leftPad(Integer.toBinaryString(protocol), 12, "0")
        .toCharArray();

    if (StringUtils.equals(protocolArray[11] + "", Constants.BOOL_YES)) {
      builder.append("HTTP/");
    }

    if (StringUtils.equals(protocolArray[10] + "", Constants.BOOL_YES)) {
      builder.append("DNS/");
    }

    if (StringUtils.equals(protocolArray[9] + "", Constants.BOOL_YES)) {
      builder.append("FTP/");
    }

    if (StringUtils.equals(protocolArray[8] + "", Constants.BOOL_YES)) {
      builder.append("MAIL/");
    }

    if (StringUtils.equals(protocolArray[7] + "", Constants.BOOL_YES)) {
      builder.append("TELNET/");
    }

    if (StringUtils.equals(protocolArray[6] + "", Constants.BOOL_YES)) {
      builder.append("SSL/");
    }

    if (StringUtils.equals(protocolArray[5] + "", Constants.BOOL_YES)) {
      builder.append("SSH/");
    }

    if (StringUtils.equals(protocolArray[4] + "", Constants.BOOL_YES)) {
      builder.append("MYSQL/");
    }

    if (StringUtils.equals(protocolArray[3] + "", Constants.BOOL_YES)) {
      builder.append("POSTGRESQL/");
    }

    if (StringUtils.equals(protocolArray[2] + "", Constants.BOOL_YES)) {
      builder.append("TNS/");
    }

    if (StringUtils.equals(protocolArray[1] + "", Constants.BOOL_YES)) {
      builder.append("ICMP/");
    }

    if (StringUtils.equals(protocolArray[0] + "", Constants.BOOL_YES)) {
      builder.append("SOCKS5/");
    }

    if (builder.length() > 0) {
      builder.deleteCharAt(builder.length() - 1);
    }

    return builder.toString();
  }

}
