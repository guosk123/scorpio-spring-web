import ARP from '../ARP';
import DHCPAnalysis from '@/pages/app/analysis/DHCPAnalysis/DHCP';
import DHCPSpecifications from '../DHCP';
import DHCPV6Analysis from '@/pages/app/analysis/DHCPAnalysis/DHCPV6';
import DHCPV6Specifications from '../DHCPV6';
import DNS from '../DNS';
import FTP from '../FTP';
import HTTPAnalysis from '@/pages/app/analysis/HTTPAnalysis';
import HttpSpecifications from '../HTTP';
import ICMPV4 from '../ICMPV4';
import ICMPV6 from '../ICMPV6';
import IMAP from '../IMAP';
import MYSQL from '../Mysql';
import OSPF from '../OSPF';
import Overview from '../Overview';
import POP3 from '../POP3';
import PostgreSQL from '../PostgreSQL';
import SMTP from '../SMTP';
import Socks5 from '../Socks5';
import SSH from '../SSH';
import SSL from '../SSL';
import TDS from '../TDS';
import TELNET from '../Telnet';
import TNS from '../TNS';
import SIP from '../SIP';
import { EMetadataTabType } from './typings';
import type { IAction, IState } from './components/EditTabs';
import { newPanesFn } from './components/EditTabs';
import type { Dispatch } from 'react';
import Socks4 from '../Socks4';
import LDAP from '../LDAP';
import DB2 from '../DB2';
import File from '../File';

/**
 * title: tab标题
 * defShow: 默认是否展示，默认展示则不可关闭
 * content: tabPane的dom
 * disable: 自定义可选tab中是否可以隐藏
 */
export const MetadataTabs = {
  [EMetadataTabType.OVERVIEW]: {
    title: '概览',
    defShow: true,
    content: <Overview />,
    disabled: true,
  },
  [EMetadataTabType.HTTP]: {
    title: 'HTTP详单',
    defShow: true,
    content: <HttpSpecifications />,
    disabled: false,
  },
  [EMetadataTabType.HTTPANALYSIS]: {
    title: 'HTTP分析',
    defShow: true,
    content: <HTTPAnalysis />,
    disabled: false,
  },
  [EMetadataTabType.DNS]: { title: 'DNS详单', defShow: true, content: <DNS />, disabled: false },
  [EMetadataTabType.FTP]: { title: 'FTP详单', defShow: true, content: <FTP />, disabled: false },
  [EMetadataTabType.IMAP]: { title: 'IMAP详单', defShow: true, content: <IMAP />, disabled: false },
  [EMetadataTabType.POP3]: { title: 'POP3详单', defShow: true, content: <POP3 />, disabled: false },
  [EMetadataTabType.SMTP]: { title: 'SMTP详单', defShow: true, content: <SMTP />, disabled: false },
  [EMetadataTabType.TELNET]: {
    title: 'TELNET详单',
    defShow: true,
    content: <TELNET />,
    disabled: false,
  },
  // [EMetadataTabType.SIP]: { title: 'SIP详单', defShow: true, content: <SIP />, disable: false },
  [EMetadataTabType.SSL]: { title: 'SSL详单', defShow: true, content: <SSL />, disabled: false },
  [EMetadataTabType.SSH]: { title: 'SSH详单', defShow: true, content: <SSH />, disabled: false },
  [EMetadataTabType.MYSQL]: {
    title: 'MySQL详单',
    defShow: true,
    content: <MYSQL />,
    disabled: false,
  },
  [EMetadataTabType.POSTGRESQL]: {
    title: 'PostgreSQL详单',
    defShow: true,
    content: <PostgreSQL />,
    disabled: false,
  },
  [EMetadataTabType.TNS]: { title: 'TNS详单', defShow: true, content: <TNS />, disabled: false },
  [EMetadataTabType.ICMPV4]: {
    title: 'ICMPv4详单',
    defShow: true,
    content: <ICMPV4 />,
    disabled: false,
  },
  [EMetadataTabType.ICMPV6]: {
    title: 'ICMPv6详单',
    defShow: true,
    content: <ICMPV6 />,
    disabled: false,
  },
  [EMetadataTabType.SOCKS5]: {
    title: 'SOCKS5详单',
    defShow: true,
    content: <Socks5 />,
    disabled: false,
  },
  [EMetadataTabType.SOCKS4]: {
    title: 'SOCKS4详单',
    defShow: true,
    content: <Socks4 />,
    disabled: false,
  },
  [EMetadataTabType.DHCP]: {
    title: 'DHCP详单',
    defShow: true,
    content: <DHCPSpecifications />,
    disabled: false,
  },
  [EMetadataTabType.DHCPANALYSIS]: {
    title: 'DHCP分析',
    defShow: true,
    content: <DHCPAnalysis />,
    disabled: false,
  },
  [EMetadataTabType.DHCPV6]: {
    title: 'DHCPv6详单',
    defShow: true,
    content: <DHCPV6Specifications />,
    disabled: false,
  },
  [EMetadataTabType.DHCPV6ANALYSIS]: {
    title: 'DHCPv6分析',
    defShow: true,
    content: <DHCPV6Analysis />,
    disabled: false,
  },
  [EMetadataTabType.TDS]: { title: 'TDS详单', defShow: true, content: <TDS />, disabled: false },
  [EMetadataTabType.ARP]: { title: 'ARP详单', defShow: true, content: <ARP />, disabled: false },
  [EMetadataTabType.OSPF]: { title: 'OSPF详单', defShow: true, content: <OSPF />, disabled: false },
  [EMetadataTabType.SIP]: { title: 'SIP详单', defShow: true, content: <SIP />, disabled: false },
  [EMetadataTabType.LDAP]: { title: 'LDAP详单', defShow: true, content: <LDAP />, disabled: false },
  [EMetadataTabType.DB2]: { title: 'DB2详单', defShow: true, content: <DB2 />, disabbled: false },
  [EMetadataTabType.FILE]: { title: '文件详单', defShow: true, content: <File />, disabled: false },
};

/**
 *
 * @param state 获取分析的context state
 * @param dispatch 获取到的分析的context dispatch
 * @param tabType 要跳转的tab的枚举值
 */
export const jumpToMetadataTab = (
  state: IState,
  dispatch: Dispatch<IAction>,
  tabType: EMetadataTabType,
  info?: Record<string, any>,
) => {
  newPanesFn({ ...MetadataTabs[tabType], isNewTab: true }, dispatch, state, tabType, info);
};
