import type { Dispatch } from 'react';
import type { ITabsState } from '../../Network/components/EditTabs';
import { newPanesFn } from '../../Network/components/EditTabs';
import ARP from './ARPs';
import DHCPAnalysis from './DHCP/DHCPAnalysis';
import DHCPSpecifications from './DHCP/DHCPSpecifications';
import DHCPV6Analysis from './DHCPV6/DHCPV6Analysis';
import DHCPV6Specifications from './DHCPV6/DHCPV6Specifications';
import DNS from './DNS';
import DB2 from './DB2';
import FTP from './FTP';
import HTTPAnalysis from './HTTP/HTTPAnalysis';
import HttpSpecifications from './HTTP/Specifications';
import ICMPV4 from './ICMPV4';
import ICMPV6 from './ICMPV6';
import IMAP from './IMAP';
import LDAP from './LDAP';
import MYSQL from './MYSQL';
import OSPF from './OSPF';
import Overview from './Overview';
import POP3 from './POP3';
import PostgreSQL from './PostgreSQL';
import SIP from './SIP';
import SMTP from './SMTP';
import Socks4 from './SOCKS4';
import Socks5 from './Socks5';
import SSH from './SSH';
import SSL from './SSL';
import TDS from './TDS';
import TELNET from './TELNET';
import TNS from './TNS';
import File from './File';
import { EMetadataTabType } from './typings';

export const MetadataTabsAnalysis = {
  [EMetadataTabType.OVERVIEW]: { title: '概览', defShow: true, content: <Overview /> },
  [EMetadataTabType.HTTPANALYSIS]: { title: 'HTTP分析', defShow: true, content: <HTTPAnalysis /> },
  [EMetadataTabType.DHCPANALYSIS]: { title: 'DHCP分析', defShow: true, content: <DHCPAnalysis /> },
  [EMetadataTabType.DHCPV6ANALYSIS]: {
    title: 'DHCPv6分析',
    defShow: true,
    content: <DHCPV6Analysis />,
  },
};

export const MetadataTabsDetail = {
  [EMetadataTabType.HTTP]: { title: 'HTTP详单', defShow: true, content: <HttpSpecifications /> },
  [EMetadataTabType.DNS]: { title: 'DNS详单', defShow: true, content: <DNS /> },
  [EMetadataTabType.FTP]: { title: 'FTP详单', defShow: true, content: <FTP /> },
  [EMetadataTabType.IMAP]: { title: 'IMAP详单', defShow: true, content: <IMAP /> },
  [EMetadataTabType.POP3]: { title: 'POP3详单', defShow: true, content: <POP3 /> },
  [EMetadataTabType.SMTP]: { title: 'SMTP详单', defShow: true, content: <SMTP /> },
  [EMetadataTabType.TELNET]: { title: 'TELNET详单', defShow: true, content: <TELNET /> },
  [EMetadataTabType.SSL]: { title: 'SSL详单', defShow: true, content: <SSL /> },
  [EMetadataTabType.SSH]: { title: 'SSH详单', defShow: true, content: <SSH /> },
  [EMetadataTabType.MYSQL]: { title: 'MySQL详单', defShow: true, content: <MYSQL /> },
  [EMetadataTabType.POSTGRESQL]: {
    title: 'PostgreSQL详单',
    defShow: true,
    content: <PostgreSQL />,
  },
  [EMetadataTabType.TNS]: { title: 'TNS详单', defShow: true, content: <TNS /> },
  [EMetadataTabType.ICMPV4]: { title: 'ICMPv4详单', defShow: true, content: <ICMPV4 /> },
  [EMetadataTabType.ICMPV6]: { title: 'ICMPv6详单', defShow: true, content: <ICMPV6 /> },
  [EMetadataTabType.SOCKS5]: { title: 'SOCKS5详单', defShow: true, content: <Socks5 /> },
  [EMetadataTabType.SOCKS4]: { title: 'SOCKS4详单', defShow: true, content: <Socks4 /> },
  [EMetadataTabType.DHCP]: { title: 'DHCP详单', defShow: true, content: <DHCPSpecifications /> },
  [EMetadataTabType.DHCPV6]: {
    title: 'DHCPv6详单',
    defShow: true,
    content: <DHCPV6Specifications />,
  },
  [EMetadataTabType.TDS]: { title: 'TDS详单', defShow: true, content: <TDS /> },
  [EMetadataTabType.ARP]: { title: 'ARP详单', defShow: true, content: <ARP /> },
  [EMetadataTabType.OSPF]: { title: 'OSPF详单', defShow: true, content: <OSPF /> },
  [EMetadataTabType.SIP]: { title: 'SIP详单', defShow: true, content: <SIP /> },
  [EMetadataTabType.LDAP]: { title: 'LDAP详单', defShow: true, content: <LDAP /> },
  [EMetadataTabType.DB2]: { title: 'DB2详单', defShow: true, content: <DB2 /> },
  [EMetadataTabType.FILE]: { title: '文件还原详单', defShow: true, content: <File /> },
};

export const MetadataTabs = {
  [EMetadataTabType.OVERVIEW]: { title: '概览', defShow: true, content: <Overview /> },
  [EMetadataTabType.HTTP]: { title: 'HTTP详单', defShow: true, content: <HttpSpecifications /> },
  [EMetadataTabType.HTTPANALYSIS]: { title: 'HTTP分析', defShow: true, content: <HTTPAnalysis /> },
  [EMetadataTabType.DNS]: { title: 'DNS详单', defShow: true, content: <DNS /> },
  [EMetadataTabType.FTP]: { title: 'FTP详单', defShow: true, content: <FTP /> },
  [EMetadataTabType.IMAP]: { title: 'IMAP详单', defShow: true, content: <IMAP /> },
  [EMetadataTabType.POP3]: { title: 'POP3详单', defShow: true, content: <POP3 /> },
  [EMetadataTabType.SMTP]: { title: 'SMTP详单', defShow: true, content: <SMTP /> },
  [EMetadataTabType.TELNET]: { title: 'TELNET详单', defShow: true, content: <TELNET /> },
  [EMetadataTabType.SSL]: { title: 'SSL详单', defShow: true, content: <SSL /> },
  [EMetadataTabType.SSH]: { title: 'SSH详单', defShow: true, content: <SSH /> },
  [EMetadataTabType.MYSQL]: { title: 'MySQL详单', defShow: true, content: <MYSQL /> },
  [EMetadataTabType.POSTGRESQL]: {
    title: 'PostgreSQL详单',
    defShow: true,
    content: <PostgreSQL />,
  },
  [EMetadataTabType.TNS]: { title: 'TNS详单', defShow: true, content: <TNS /> },
  [EMetadataTabType.ICMPV4]: { title: 'ICMPv4详单', defShow: true, content: <ICMPV4 /> },
  [EMetadataTabType.ICMPV6]: { title: 'ICMPv6详单', defShow: true, content: <ICMPV6 /> },
  [EMetadataTabType.SOCKS5]: { title: 'SOCKS5详单', defShow: true, content: <Socks5 /> },
  [EMetadataTabType.SOCKS4]: { title: 'SOCKS4详单', defShow: true, content: <Socks4 /> },
  [EMetadataTabType.DHCP]: { title: 'DHCP详单', defShow: true, content: <DHCPSpecifications /> },
  [EMetadataTabType.DHCPANALYSIS]: { title: 'DHCP分析', defShow: true, content: <DHCPAnalysis /> },
  [EMetadataTabType.DHCPV6]: {
    title: 'DHCPv6详单',
    defShow: true,
    content: <DHCPV6Specifications />,
  },
  [EMetadataTabType.DHCPV6ANALYSIS]: {
    title: 'DHCPv6分析',
    defShow: true,
    content: <DHCPV6Analysis />,
  },
  [EMetadataTabType.TDS]: { title: 'TDS详单', defShow: true, content: <TDS /> },
  [EMetadataTabType.ARP]: { title: 'ARP详单', defShow: true, content: <ARP /> },
  [EMetadataTabType.OSPF]: { title: 'OSPF详单', defShow: true, content: <OSPF /> },
  [EMetadataTabType.SIP]: { title: 'SIP详单', defShow: true, content: <SIP /> },
  [EMetadataTabType.LDAP]: { title: 'LDAP详单', defShow: true, content: <LDAP /> },
  [EMetadataTabType.DB2]: { title: 'DB2详单', defShow: true, content: <DB2 /> },
  [EMetadataTabType.FILE]: { title: '文件还原详单', defShow: true, content: <File /> },
};

/**
 *
 * @param state 获取分析的context state
 * @param dispatch 获取到的分析的context dispatch
 * @param tabType 要跳转的tab的枚举值
 */
export const jumpToMetadataTab = (
  state: ITabsState,
  dispatch: Dispatch<any>,
  tabType: EMetadataTabType,
  info?: any,
) => {
  console.log('MetadataTabs[tabType]', tabType, MetadataTabs[tabType]);
  newPanesFn({ ...MetadataTabs[tabType], isNewTab: true }, dispatch, state, tabType, info);
};
