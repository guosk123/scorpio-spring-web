import { EFilterGroupOperatorTypes, EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { EMetadataProtocol, EMetadataTabType } from '@/pages/app/appliance/Metadata/typings';
import { AnalysisContext } from '@/pages/app/Network/Analysis';
import type { URLFilter, URLFilterGroup } from '@/pages/app/Network/Analysis/constant';
import { jumpToAnalysisTabNew } from '@/pages/app/Network/Analysis/constant';
import { ENetworkTabs } from '@/pages/app/Network/typing';
import { ipV4Regex } from '@/utils/utils';
import { Menu, Modal, Tag } from 'antd';
import { useContext, useRef, useState } from 'react';
import { useParams, useSelector } from 'umi';
import { ServiceAnalysisContext } from '../../Service/index';
import type { IUriParams } from '../../typings';

interface Props {
  indexKey: string;
  ipAddressKeys?: string;
  tableKey?: string;
}

const showMenuKeys = [
  'srcIp-metadata',
  'destIp-metadata',
  'domainAddress-metadata',
  'ip_initiator-record',
  'ip_responder-record',
  'ipAddress-flow',
  'ipAAddress-flow',
  'ipBAddress-flow',
];

enum EIP_DRILLDOWN_MENU_KEY {
  IP_GRAPH = 'ip_graph',
  DNS_DETAIL = 'dns-detail',
  FLOW_RECORD = 'flow_record',
}

const menuToUrl = {
  [EIP_DRILLDOWN_MENU_KEY.IP_GRAPH]: 'ip-graph',
  [EIP_DRILLDOWN_MENU_KEY.DNS_DETAIL]: 'metadata',
  [EIP_DRILLDOWN_MENU_KEY.FLOW_RECORD]: 'flow-record',
};

export default function IpDirlldownMenu(props: Props) {
  const { indexKey, ipAddressKeys, tableKey } = props;
  const { serviceId }: IUriParams = useParams();
  const [connectState, dispatch] = useContext(serviceId ? ServiceAnalysisContext : AnalysisContext);
  const params: IUriParams = useParams();
  // const [dirlldownType, setDirlldownType] = useState<string>();
  const dirlldownTypeRef = useRef<any>();
  const url = (() => {
    if (params.serviceId) {
      return `/analysis/service/${params.serviceId}/${params.networkId}`;
    }
    return `/network/${params.networkId}/analysis`;
  })();

  const [isModalVisible, setIsModalVisible] = useState(false);

  const globalSelectedTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (state) => state.appModel.globalSelectedTime,
  );

  const jumpToNewFn = (key: EIP_DRILLDOWN_MENU_KEY, ip?: string) => {
    const tmpIp = ip || ipAddressKeys;
    if (dirlldownTypeRef.current === EIP_DRILLDOWN_MENU_KEY.DNS_DETAIL) {
      const flowIPToDNSInfo: URLFilterGroup[] = [
        {
          operator: EFilterGroupOperatorTypes.OR,
          group: [
            {
              field: `${'domain'}_${ipV4Regex.test(tmpIp || '') ? 'ipv4' : 'ipv6'}`,
              operator: EFilterOperatorTypes.EQ,
              operand: tmpIp || '',
            },
          ],
        },
      ];
      // const direaction =
      //   indexKey.includes('src') ||
      //   indexKey.includes('initiator') ||
      //   indexKey.includes('ipAAddress')
      //     ? 'src'
      //     : 'dest';
      const tmpInfo: URLFilter[] = [
        {
          field: `domain_${ipV4Regex.test(tmpIp || '') ? 'ipv4' : 'ipv6'}`,
          operator: EFilterOperatorTypes.EQ,
          operand: tmpIp || '',
        },
      ];
      jumpToAnalysisTabNew(connectState, dispatch, ENetworkTabs.METADATA, {
        filter: tmpInfo || indexKey === 'ipAddress-flow' ? flowIPToDNSInfo : tmpInfo,
        jumpNewTabs: [EMetadataTabType.DNS],
        globalSelectedTime: {
          startTime: globalSelectedTime.startTime,
          endTime: globalSelectedTime.endTime,
        },
      });
    } else {
      // 会话详单
      const direaction =
        indexKey.includes('src') ||
        indexKey.includes('initiator') ||
        indexKey.includes('ipAAddress')
          ? 'initiator'
          : 'responder';
      const flowIPToRecordInfo: URLFilterGroup[] = [
        {
          operator: EFilterGroupOperatorTypes.OR,
          group: [
            {
              field: ipV4Regex.test(tmpIp || '') ? 'ipv4_initiator' : 'ipv6_initiator',
              operator: EFilterOperatorTypes.EQ,
              operand: tmpIp || '',
            },
            {
              field: ipV4Regex.test(tmpIp || '') ? 'ipv4_responder' : 'ipv6_responder',
              operator: EFilterOperatorTypes.EQ,
              operand: tmpIp || '',
            },
          ],
        },
      ];
      const tmpInfo = [
        {
          field: `${ipV4Regex.test(tmpIp || '') ? 'ipv4' : 'ipv6'}_${direaction}`,
          operator: EFilterOperatorTypes.EQ,
          operand: tmpIp || '',
        },
      ];
      jumpToAnalysisTabNew(connectState, dispatch, ENetworkTabs.FLOWRECORD, {
        filter: indexKey === 'ipAddress-flow' ? flowIPToRecordInfo : tmpInfo,
        globalSelectedTime: {
          startTime: globalSelectedTime.startTime,
          endTime: globalSelectedTime.endTime,
        },
      });
      // jumpNewPage(
      //   `${url}/${menuToUrl[key]}?filter=${
      //     indexKey === 'ipAddress-flow' ? flowIPToRecordInfo : tmpInfo
      //   }`,
      // );
    }
  };

  const jumpClick = (jumpFn: any) => {
    if (Array.isArray(ipAddressKeys)) {
      if (ipAddressKeys.length === 1) {
        jumpFn(ipAddressKeys[0]);
      } else {
        setIsModalVisible(true);
      }
    } else {
      jumpFn();
    }
  };

  const onClick = (key: string) => {
    dirlldownTypeRef.current = key;
    jumpClick((ip?: string) => {
      jumpToNewFn(key as EIP_DRILLDOWN_MENU_KEY, ip);
    });
  };

  if (!showMenuKeys.includes(indexKey)) {
    return <div style={{ display: 'none' }} />;
  }
  return (
    <Menu.ItemGroup key={'jumpToOtherPage'} title="跳转到其他页">
      {/* <Menu.Item key={EIP_DRILLDOWN_MENU_KEY.IP_GRAPH} onClick={({ key }) => onClick(key)}>
        访问关系
      </Menu.Item> */}
      <Menu.Item
        key={EIP_DRILLDOWN_MENU_KEY.DNS_DETAIL}
        style={tableKey === EMetadataProtocol.DNS ? { display: 'none' } : {}}
        onClick={({ key }) => onClick(key)}
      >
        域名记录
      </Menu.Item>
      <Menu.Item
        key={EIP_DRILLDOWN_MENU_KEY.FLOW_RECORD}
        style={tableKey === 'npmd-flow-record-table' ? { display: 'none' } : {}}
        onClick={({ key }) => onClick(key)}
      >
        会话详单
      </Menu.Item>
      <Modal
        title="选择下钻的IP地址"
        visible={isModalVisible}
        width={600}
        footer={null}
        onCancel={() => {
          setIsModalVisible(false);
        }}
      >
        {Array.isArray(ipAddressKeys) &&
          ipAddressKeys.map((ip) => {
            return (
              <Tag
                color="processing"
                onClick={() => jumpToNewFn(dirlldownTypeRef.current as EIP_DRILLDOWN_MENU_KEY, ip)}
                style={{ minWidth: 120, marginBottom: 8 }}
              >
                {ip}
              </Tag>
            );
          })}
      </Modal>
    </Menu.ItemGroup>
  );
}
