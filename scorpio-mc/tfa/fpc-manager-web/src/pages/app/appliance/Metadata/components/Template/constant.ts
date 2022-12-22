import { EMetadataProtocol } from '../../typings';

export const METADATA_EXCLUDE_COLS = ['operate', 'dnsType'];

const defColArrSimple = ['index', 'startTime', 'srcIp', 'destIp', 'action'];

const defColArr = ['index', 'startTime', 'srcIp', 'srcPort', 'destIp', 'destPort', 'action'];

const defMailArr = ['subject', 'from', 'to'];

const defSqlArr = ['cmd', 'error', 'delaytime'];

export const defaultShowColumnsForFlow = {
  [EMetadataProtocol.HTTP]: [...defColArr, 'uri', 'method', 'status', 'host'],
  [EMetadataProtocol.DNS]: [...defColArr, 'domain', 'domainAddress'],
  [EMetadataProtocol.FTP]: [...defColArr, 'cmd', 'reply'],
  [EMetadataProtocol.MAIL]: [...defColArr, ...defMailArr],
  [EMetadataProtocol.POP3]: [...defColArr, ...defMailArr],
  [EMetadataProtocol.SMTP]: [...defColArr, ...defMailArr],
  [EMetadataProtocol.IMAP]: [...defColArr, ...defMailArr],
  [EMetadataProtocol.TELNET]: [...defColArr, 'cmd', 'reply'],
  [EMetadataProtocol.SSL]: [...defColArr, 'serverName', 'issuer', 'validity'],
  [EMetadataProtocol.SSH]: [...defColArr, 'clientSoftware', 'serverVersion', 'serverSoftware'],
  [EMetadataProtocol.MYSQL]: [...defColArr, ...defSqlArr],
  [EMetadataProtocol.POSTGRESQL]: [...defColArr, ...defSqlArr],
  [EMetadataProtocol.TNS]: [...defColArr, ...defSqlArr],
  // [EMetadataProtocol.ICMP]: [...defColArr],
  [EMetadataProtocol.ICMPV4]: [...defColArrSimple, 'result'],
  [EMetadataProtocol.ICMPV6]: [...defColArrSimple, 'result'],
  [EMetadataProtocol.SOCKS5]: [...defColArr, 'bindAddr', 'bindPort', 'cmdResult'],
  [EMetadataProtocol.SOCKS4]: [...defColArr, 'cmd', 'userId', 'domainName', 'cmdResult'],
  [EMetadataProtocol.SIP]: [...defColArr, 'from', 'to', 'ipProtocol', 'type', 'statusCode'],
  [EMetadataProtocol.DHCP]: [
    'index',
    'startTime',
    'srcIpv4',
    'destIpv4',
    'srcMac',
    'destMac',
    'messageType',
    'parameters',
    'offeredIpv4Address',
    'action',
  ],
  [EMetadataProtocol.DHCPV6]: [
    'index',
    'startTime',
    'srcIpv6',
    'destIpv6',
    'srcMac',
    'destMac',
    'messageType',
    'parameters',
    'offeredIpv6Address',
    'action',
  ],
  [EMetadataProtocol.TDS]: [...defColArr, ...defSqlArr],
  [EMetadataProtocol.ARP]: [
    'index',
    'startTime',
    'srcIp',
    'destIp',
    'srcMac',
    'destMac',
    'type',
    'action',
  ],
  [EMetadataProtocol.OSPF]: [
    ...defColArrSimple,
    'messageType',
    'sourceOspfRouter',
    'linkStateIpv4Address',
  ],
  [EMetadataProtocol.LDAP]: [...defColArr, 'opType', 'resStatus', 'resContent', 'reqContent'],
  [EMetadataProtocol.DB2]: [...defColArr, 'codePoint', 'data'],
};
