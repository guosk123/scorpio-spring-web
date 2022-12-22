import ARP from '@/pages/app/appliance/Metadata/ARP';
import DHCPSpecifications from '@/pages/app/appliance/Metadata/DHCP/DHCPSpecifications';
import DHCPV6Specifications from '@/pages/app/appliance/Metadata/DHCPV6/DHCPV6Specifications';
import DNS from '@/pages/app/appliance/Metadata/DNS';
import FTP from '@/pages/app/appliance/Metadata/FTP';
import HttpSpecifications from '@/pages/app/appliance/Metadata/HTTP/Specifications';
import ICMPV4 from '@/pages/app/appliance/Metadata/ICMPV4';
import ICMPV6 from '@/pages/app/appliance/Metadata/ICMPV6';
import IMAP from '@/pages/app/appliance/Metadata/IMAP';
import MYSQL from '@/pages/app/appliance/Metadata/MYSQL';
import OSPF from '@/pages/app/appliance/Metadata/OSPF';
import Overview from '@/pages/app/appliance/Metadata/Overview';
import POP3 from '@/pages/app/appliance/Metadata/POP3';
import PostgreSQL from '@/pages/app/appliance/Metadata/PostgreSQL';
import SMTP from '@/pages/app/appliance/Metadata/SMTP';
import Socks5 from '@/pages/app/appliance/Metadata/Socks5';
import SSH from '@/pages/app/appliance/Metadata/SSH';
import SSL from '@/pages/app/appliance/Metadata/SSL';
import TDS from '@/pages/app/appliance/Metadata/TDS';
import TELNET from '@/pages/app/appliance/Metadata/TELNET';
import TNS from '@/pages/app/appliance/Metadata/TNS';
import type { ITabsState } from '@/pages/app/Network/components/EditTabs';
import { newPanesFn } from '@/pages/app/Network/components/EditTabs';
import type { Dispatch } from 'react';
import { EDimensionsTab } from './typing';
import Flow from '@/pages/app/Network/components/Flow';
import { EDimensionsSearchType } from '../typing';
import FlowNetwork from './components/FlowNetwork';
import TransformTitle from './components/TransformTitle';

const metadataAnalysisTabs = {
  [EDimensionsTab.HTTP]: { title: 'HTTP详单', defShow: false, content: <HttpSpecifications /> },
  [EDimensionsTab.DNS]: { title: 'DNS详单', defShow: false, content: <DNS /> },
  [EDimensionsTab.FTP]: { title: 'FTP详单', defShow: false, content: <FTP /> },
  [EDimensionsTab.IMAP]: { title: 'IMAP详单', defShow: false, content: <IMAP /> },
  [EDimensionsTab.POP3]: { title: 'POP3详单', defShow: false, content: <POP3 /> },
  [EDimensionsTab.SMTP]: { title: 'SMTP详单', defShow: false, content: <SMTP /> },
  [EDimensionsTab.TELNET]: { title: 'TELNET详单', defShow: false, content: <TELNET /> },
  [EDimensionsTab.SSL]: { title: 'SSL详单', defShow: false, content: <SSL /> },
  [EDimensionsTab.SSH]: { title: 'SSH详单', defShow: false, content: <SSH /> },
  [EDimensionsTab.MYSQL]: { title: 'MySQL详单', defShow: false, content: <MYSQL /> },
  [EDimensionsTab.POSTGRESQL]: {
    title: 'PostgreSQL详单',
    defShow: false,
    content: <PostgreSQL />,
  },
  [EDimensionsTab.TNS]: { title: 'TNS详单', defShow: false, content: <TNS /> },
  [EDimensionsTab.ICMPV4]: { title: 'ICMPv4详单', defShow: false, content: <ICMPV4 /> },
  [EDimensionsTab.ICMPV6]: { title: 'ICMPv6详单', defShow: false, content: <ICMPV6 /> },
  [EDimensionsTab.SOCKS5]: { title: 'SOCKS5详单', defShow: false, content: <Socks5 /> },
  [EDimensionsTab.DHCP]: { title: 'DHCP详单', defShow: false, content: <DHCPSpecifications /> },
  [EDimensionsTab.DHCPV6]: {
    title: 'DHCPv6详单',
    defShow: false,
    content: <DHCPV6Specifications />,
  },
  [EDimensionsTab.TDS]: { title: 'TDS详单', defShow: false, content: <TDS /> },
  [EDimensionsTab.ARP]: { title: 'ARP详单', defShow: false, content: <ARP /> },
  [EDimensionsTab.OSPF]: { title: 'OSPF详单', defShow: false, content: <OSPF /> },
};

const flowAnalysisTabs = {
  [EDimensionsTab.IP]: {
    title: 'IP地址',
    defShow: false,
    content: <Flow dimensinosTabType={EDimensionsSearchType.IPADDRESS} />,
    // content: <FlowIP />,
  },
  [EDimensionsTab.APPLICATION]: {
    title: '应用',
    defShow: false,
    content: <Flow dimensinosTabType={EDimensionsSearchType.APPLICATION} />,
  },
  [EDimensionsTab.PROTOCOL]: {
    title: '应用层协议',
    defShow: false,
    content: <Flow dimensinosTabType={EDimensionsSearchType.L7PROTOCOLID} />,
  },
  [EDimensionsTab.PORT]: {
    title: '端口',
    defShow: false,
    content: <Flow dimensinosTabType={EDimensionsSearchType.PORT} />,
  },
  [EDimensionsTab.IPCONVERSATION]: {
    title: 'IP会话',
    defShow: false,
    content: <Flow dimensinosTabType={EDimensionsSearchType.IPCONVERSATION} />,
  },
  [EDimensionsTab.LOCATION]: {
    title: '地区',
    defShow: false,
    content: <Flow dimensinosTabType={EDimensionsSearchType.LOCATION} />,
  },
  [EDimensionsTab.HOSTGROUP]: {
    title: 'IP地址组',
    defShow: false,
    content: <Flow dimensinosTabType={EDimensionsSearchType.IPADDRESS} />,
  },
};

export const DimensionsSearchTabs = {
  ...metadataAnalysisTabs,
  ...flowAnalysisTabs,
  [EDimensionsTab.OVERVIEW]: {
    title: '应用层协议概览',
    defShow: false,
    content: <Overview isDimensionsTab={true} />,
  },
  [EDimensionsTab.NETWORK]: {
    title: '网络流量',
    defShow: false,
    content: <FlowNetwork />,
  },
};

export const jumpToDimensionsTab = (
  state: ITabsState,
  dispatch: Dispatch<any>,
  tabType: EDimensionsTab | EDimensionsTab[],
  info?: any,
  filter?: any,
) => {
  if (!Array.isArray(tabType)) {
    newPanesFn(
      {
        ...DimensionsSearchTabs[tabType],
        title: <TransformTitle text={info} exInfo={DimensionsSearchTabs[tabType]?.title} />,
        detail: { searchBoxInfo: info },
        isNewTab: true,
      },
      dispatch,
      state,
      tabType,
      filter,
    );
  } else {
    newPanesFn(
      tabType.map((type) => {
        return {
          ...DimensionsSearchTabs[type],
          title: <TransformTitle text={info} exInfo={DimensionsSearchTabs[type]?.title} />,
          detail: { searchBoxInfo: info },
          isNewTab: true,
        };
      }),
      dispatch,
      state,
      tabType,
      filter,
    );
  }
};

export const dimensionsUrl = '/performance/dimensions-search/search';
