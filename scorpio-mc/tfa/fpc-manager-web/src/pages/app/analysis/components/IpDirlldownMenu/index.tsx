import type { IFilter, IFilterCondition } from '@/components/FieldFilter/typings';
import { EFilterGroupOperatorTypes, EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { ipV4Regex, jumpNewPage } from '@/utils/utils';
import { Menu, Modal, Tag } from 'antd';
import type { ReactNode} from 'react';
import { useMemo, useRef, useState } from 'react';
import { useParams, useSelector } from 'umi';
import type { IUriParams } from '../../typings';

enum EIP_DRILLDOWN_MENU_KEY {
  IP_GRAPH = 'ip_graph',
  DNS_DETAIL = 'dns-detail',
  FLOW_RECORD = 'flow_record',
}

const menuToUrl = {
  [EIP_DRILLDOWN_MENU_KEY.IP_GRAPH]: 'ip-graph',
  [EIP_DRILLDOWN_MENU_KEY.DNS_DETAIL]: 'metadata/record',
  [EIP_DRILLDOWN_MENU_KEY.FLOW_RECORD]: 'flow-record',
};

const enableIpDrilldownField = [
  'srcIp',
  'destIp',
  'domainAddress',
  'ip_initiator',
  'ip_responder',
  'ipAddress',
  'ipAAddress',
  'ipBAddress',
];

interface Props {
  fromField: string;
  networkId?: string;
  hasIpDirection?: boolean;
  ipAddressValue?: string | string[];
  inFlowRecord?: boolean;
  inDns?: boolean;
  isCustomMenu?: boolean;
  customMenus?: ReactNode[];
}

export default function IpDirlldownMenu(props: Props) {
  const {
    fromField,
    ipAddressValue,
    networkId,
    hasIpDirection = true,
    inFlowRecord = false,
    inDns = false,
    isCustomMenu = false,
    customMenus = [],
  } = props;
  const params: IUriParams = useParams();

  const [isModalVisible, setIsModalVisible] = useState(false);
  const dirlldownTypeRef = useRef<EIP_DRILLDOWN_MENU_KEY>();

  const globalSelectedTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (state) => state.appModel.globalSelectedTime,
  );

  const filter: IFilter[] = useMemo(() => {
    const tmp: IFilter[] = [];
    if (params.networkId || networkId) {
      tmp.push({
        field: 'network_id',
        operator: EFilterOperatorTypes.EQ,
        operand: params.networkId || networkId,
      });
    }
    if (params.serviceId) {
      tmp.push({
        field: 'service_id',
        operator: EFilterOperatorTypes.EQ,
        operand: params.serviceId,
      });
    }
    return tmp;
  }, [networkId, params.networkId, params.serviceId]);

  const urlTimeInfo = `from=${globalSelectedTime.startTimestamp}&to=${globalSelectedTime.endTimestamp}&timeType=${ETimeType.CUSTOM}&`;

  const jumpToNewFn = (key: EIP_DRILLDOWN_MENU_KEY, ip: string) => {
    const tmpIp = ip;
    if (dirlldownTypeRef.current === EIP_DRILLDOWN_MENU_KEY.IP_GRAPH) {
      const ipGraphFilter = [...filter];
      ipGraphFilter.push({
        field: 'ip_address',
        operator: EFilterOperatorTypes.EQ,
        operand: tmpIp,
      });
      if (params?.pcapFileId) {
        jumpNewPage(
          `/analysis/offline/${params?.pcapFileId}/${
            menuToUrl[key]
          }?${urlTimeInfo}filter=${encodeURIComponent(
            JSON.stringify(ipGraphFilter.filter((f) => f?.field !== 'network_id')),
          )}`,
        );
      } else {
        jumpNewPage(
          `/analysis/trace/${menuToUrl[key]}?${urlTimeInfo}filter=${encodeURIComponent(
            JSON.stringify(ipGraphFilter),
          )}`,
        );
      }
    } else if (dirlldownTypeRef.current === EIP_DRILLDOWN_MENU_KEY.DNS_DETAIL) {
      const dnsFilter: IFilterCondition = [...filter];
      dnsFilter.push({
        field: `${'domain'}_${ipV4Regex.test(tmpIp || '') ? 'ipv4' : 'ipv6'}`,
        operator: EFilterOperatorTypes.EQ,
        operand: tmpIp,
      });

      jumpNewPage(
        `/analysis/trace/${menuToUrl[key]}?${urlTimeInfo}jumpTabs=dns&filter=${encodeURIComponent(
          JSON.stringify(dnsFilter),
        )}`,
      );
    } else {
      // 会话详单
      const direaction =
        fromField.includes('src') ||
        fromField.includes('initiator') ||
        fromField.includes('ipAAddress')
          ? 'initiator'
          : 'responder';

      const flowRecordFilter: IFilterCondition = [...filter];
      if (!hasIpDirection) {
        flowRecordFilter.push({
          operator: EFilterGroupOperatorTypes.OR,
          group: [
            {
              field: ipV4Regex.test(tmpIp || '') ? 'ipv4_initiator' : 'ipv6_initiator',
              operator: EFilterOperatorTypes.EQ,
              operand: tmpIp,
            },
            {
              field: ipV4Regex.test(tmpIp || '') ? 'ipv4_responder' : 'ipv6_responder',
              operator: EFilterOperatorTypes.EQ,
              operand: tmpIp,
            },
          ],
        });
      } else {
        flowRecordFilter.push({
          field: `${ipV4Regex.test(tmpIp || '') ? 'ipv4' : 'ipv6'}_${direaction}`,
          operator: EFilterOperatorTypes.EQ,
          operand: tmpIp,
        });
      }

      jumpNewPage(
        `/analysis/trace/${menuToUrl[key]}?${urlTimeInfo}filter=${encodeURIComponent(
          JSON.stringify(flowRecordFilter),
        )}`,
      );
    }
  };

  const jumpClick = (jumpFn: (ip: string) => void) => {
    if (Array.isArray(ipAddressValue)) {
      if (ipAddressValue.length === 1) {
        jumpFn(ipAddressValue[0]);
      } else {
        setIsModalVisible(true);
      }
    } else {
      if (ipAddressValue) {
        jumpFn(ipAddressValue);
      }
    }
  };

  const onClick = (key: string) => {
    dirlldownTypeRef.current = key as EIP_DRILLDOWN_MENU_KEY;
    jumpClick((ip: string) => {
      jumpToNewFn(key as EIP_DRILLDOWN_MENU_KEY, ip);
    });
  };

  const defMenu = useMemo(() => {
    let res: any = [
      <Menu.Item key={EIP_DRILLDOWN_MENU_KEY.IP_GRAPH} onClick={({ key }) => onClick(key)}>
        访问关系
      </Menu.Item>,
    ];
    if (!inDns) {
      res.push(
        <Menu.Item key={EIP_DRILLDOWN_MENU_KEY.DNS_DETAIL} onClick={({ key }) => onClick(key)}>
          域名记录
        </Menu.Item>,
      );
    }
    if (!inFlowRecord) {
      res.push(
        <Menu.Item key={EIP_DRILLDOWN_MENU_KEY.FLOW_RECORD} onClick={({ key }) => onClick(key)}>
          会话详单
        </Menu.Item>,
      );
    }
    if (isCustomMenu) {
      res = [];
      res = [...customMenus];
    }
    return <Menu.ItemGroup key={'jumpToOtherPage'} title="跳转到其他页">
        {res}
    </Menu.ItemGroup>;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (!ipAddressValue || !enableIpDrilldownField.includes(fromField)) {
    return null;
  }

  return (
    <>
      {defMenu}
      <Modal
        title="选择下钻的IP地址"
        visible={isModalVisible}
        width={600}
        footer={null}
        onCancel={() => {
          setIsModalVisible(false);
        }}
      >
        {Array.isArray(ipAddressValue) &&
          ipAddressValue.map((ip) => {
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
    </>
  );
}
