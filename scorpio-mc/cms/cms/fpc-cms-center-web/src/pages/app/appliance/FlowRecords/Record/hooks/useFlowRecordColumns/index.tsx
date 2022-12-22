import {
  ETHERNET_TYPE_ENUM,
  ETHERNET_TYPE_LIST,
  IP_ADDRESS_LOCALITY_ENUM,
  IP_ADDRESS_LOCALITY_LIST,
  IP_PROTOCOL_LIST,
  TCP_SESSION_STATE_ENUM,
  TCP_SESSION_STATE_LIST
} from '@/common/app';
import type { IEnumValue } from '@/common/typings';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import type { IFilterCondition } from '@/components/FieldFilter/typings';
import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import IpDirlldownMenu from '@/pages/app/analysis/components/IpDirlldownMenu';
import { useNetworkServiceInFilter } from '@/pages/app/appliance/hooks';
import type { ApplicationItem } from '@/pages/app/Configuration/SAKnowledge/typings';
import { isExisty } from '@/utils/utils';
import { Tooltip } from 'antd';
import BigNumber from 'bignumber.js';
import moment from 'moment';
import numeral from 'numeral';
import { Fragment, useMemo } from 'react';
import type { GeolocationModelState, IIpAddressGroupModelState, MetadataModelState } from 'umi';
import { useSelector } from 'umi';
import type { IFlowRecordData } from '../../../typings';
import JumpToMatadata from '../../components/JumpToMatadata';
import OperationToPacketBtn from '../../components/OperationToPacketBtn';
import PoAndNeModel from '../../components/PoAndNeModel';
import type { IFlowRecordColumnProps } from '../../typing';

export const defaultShowColumns = [
  'report_time',
  'ip_initiator',
  'port_initiator',
  'ip_responder',
  'port_responder',
  'ip_protocol',
  'l7_protocol_id',
  'application_id',
  'total_bytes',
  'total_packets',
  'tcp_client_retransmission_rate',
  'tcp_server_retransmission_rate',
  'tcp_client_network_latency',
  'tcp_server_network_latency',
  'server_response_latency',
  'tcp_client_zero_window_packets',
  'tcp_server_zero_window_packets',
  'tcp_session_state',
];

export const COLUMN_ACTION_KEY = 'action';

const useFlowRecordColumns = ({
  selectedTimeInfo,
  analysisResultId,
  handlerFilterCondition,
  tableKey,
  pageProps,
}: {
  selectedTimeInfo:
    | Required<IGlobalTime>
    | {
        startTime: string;
        endTime: string;
        originStartTime: string;
        originEndTime: string;
      };
  analysisResultId?: string;
  handlerFilterCondition: (value: React.SetStateAction<IFilterCondition>) => void;
  tableKey: string;
  pageProps: {
    currentPage: number;
    pageSize: number;
  };
}): IFlowRecordColumnProps<IFlowRecordData>[] => {
  // 地理位置数据
  const {
    allCountryList,
    allProvinceList,
    allCityList,
    allCountryMap,
    allProvinceMap,
    allCityMap,
  } = useSelector<ConnectState, GeolocationModelState>((state) => state.geolocationModel);

  // 应用层协议数据
  const { allL7ProtocolsList, allL7ProtocolMap } = useSelector<ConnectState, MetadataModelState>(
    (state) => state.metadataModel,
  );

  // 应用数据
  const applicationList = useSelector<ConnectState, ApplicationItem[]>(
    (state) => state.SAKnowledgeModel.applicationList,
  );

  // ip地址组数据
  const { allIpAddressGroupList, allIpAddressGroupMap } = useSelector<
    ConnectState,
    IIpAddressGroupModelState
  >((state) => state.ipAddressGroupModel);

  const ipAddressGroupEnum: IEnumValue[] = allIpAddressGroupList.map((item) => ({
    text: item.name,
    value: `${item.id}`,
  }));

  const countryEnum: IEnumValue[] = allCountryList.map((item) => ({
    text: item.fullName,
    value: item.countryId,
  }));
  const provinceEnum: IEnumValue[] = allProvinceList.map((item) => ({
    text: item.fullName,
    value: item.provinceId,
  }));
  const cityEnum: IEnumValue[] = allCityList.map((item) => ({
    text: item.fullName,
    value: item.cityId,
  }));
  const l7ProtocolEnum: IEnumValue[] = allL7ProtocolsList.map((item) => ({
    text: item.nameText,
    value: item.protocolId,
  }));

  const nsColumns = useNetworkServiceInFilter();

  const allCols: IFlowRecordColumnProps<IFlowRecordData>[] = useMemo(
    () => [
      {
        title: '流ID',
        dataIndex: 'flow_id',
        ellipsis: true,
        searchable: false,
        operandType: EFieldOperandType.NUMBER,
        width: 90,
        show: false,
        render: (flowId) => {
          const fixText = new BigNumber(flowId).toString(16);
          return (
            <Tooltip placement="topLeft" title={fixText}>
              {fixText}
            </Tooltip>
          );
        },
      },
      {
        title: '#',
        dataIndex: 'index',
        align: 'center',
        searchable: false,
        width: 50,
        render: (text, record, index) => {
          const number = index + 1;
          const { currentPage, pageSize } = pageProps;
          if (!currentPage) {
            return number;
          }
          return (currentPage - 1) * Math.abs(pageSize!) + number;
        },
      },
      ...nsColumns,
      {
        title: '记录时间',
        dataIndex: 'report_time',
        sorter: true,
        width: 170,
        show: true,
        searchable: false,
        render: (ariseTime) => moment(ariseTime).format('YYYY-MM-DD HH:mm:ss'),
      },
      {
        title: '开始时间',
        dataIndex: 'start_time',
        sorter: true,
        width: 170,
        show: true,
        searchable: false,
        render: (ariseTime) => moment(ariseTime).format('YYYY-MM-DD HH:mm:ss'),
      },
      {
        title: '持续时间(ms)',
        dataIndex: 'duration',
        key: 'duration_hidden',
        sorter: true,
        width: 100,
        ellipsis: true,
        show: true,
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
      },
      {
        title: '接口名称',
        dataIndex: 'interface',
        ellipsis: true,
        width: 80,
        searchable: true,
      },
      {
        title: '源IP',
        dataIndex: 'ip_initiator',
        width: 120,
        show: true,
        ellipsis: true,
        render: (text, record) => {
          const ipInitiator = record.ipv4_initiator || record.ipv6_initiator;
          if (!ipInitiator) {
            return null;
          }

          return (
            <FilterBubble
              dataIndex={record.ipv4_initiator ? 'ipv4_initiator' : 'ipv6_initiator'}
              label={
                <Tooltip placement="topLeft" title={ipInitiator}>
                  {ipInitiator}
                </Tooltip>
              }
              operand={ipInitiator}
              operandType={record.ipv4_initiator ? EFieldOperandType.IPV4 : EFieldOperandType.IPV6}
              DrilldownMenu={
                <IpDirlldownMenu
                  indexKey={`ip_initiator-record`}
                  ipAddressKeys={ipInitiator}
                  tableKey={tableKey}
                />
              }
              onClick={(newFilter) => {
                handlerFilterCondition((prev) => [...prev, newFilter]);
              }}
            />
          );
        },
      },
      {
        title: '源IPv4',
        dataIndex: 'ipv4_initiator',
        show: false,
        disabled: true,
        searchable: true,
        operandType: EFieldOperandType.IPV4,
        fieldType: EFieldType.IPV4,
      },
      {
        title: '源IPv6',
        dataIndex: 'ipv6_initiator',
        show: false,
        disabled: true,
        searchable: true,
        operandType: EFieldOperandType.IPV6,
        fieldType: EFieldType.IPV6,
      },
      {
        title: '源IP位置',
        dataIndex: 'ip_locality_initiator',
        width: 120,
        show: true,
        render: (text) => IP_ADDRESS_LOCALITY_ENUM[text],
        searchable: true,
        operandType: EFieldOperandType.ENUM,
        enumValue: IP_ADDRESS_LOCALITY_LIST,
      },
      {
        title: '源端口',
        dataIndex: 'port_initiator',
        width: 90,
        show: true,
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
      },
      {
        title: '源IP地址组',
        dataIndex: 'hostgroup_id_initiator',
        width: 120,
        show: true,
        render: (id) => (isExisty(id) ? allIpAddressGroupMap[id]?.name || `[已删除: ${id}]` : ''),

        searchable: true,
        operandType: EFieldOperandType.ENUM,
        enumValue: ipAddressGroupEnum,
      },
      {
        title: '目的IP',
        dataIndex: 'ip_responder',
        width: 120,
        show: true,
        ellipsis: true,
        render: (text, record) => {
          const ipResponder = record.ipv4_responder || record.ipv6_responder;
          if (!ipResponder) {
            return null;
          }

          return (
            <FilterBubble
              dataIndex={record.ipv4_responder ? 'ipv4_responder' : 'ipv6_responder'}
              label={
                <Tooltip placement="topLeft" title={ipResponder}>
                  {ipResponder}
                </Tooltip>
              }
              operand={ipResponder}
              operandType={record.ipv4_responder ? EFieldOperandType.IPV4 : EFieldOperandType.IPV6}
              DrilldownMenu={
                <IpDirlldownMenu
                  indexKey={`ip_responder-record`}
                  ipAddressKeys={ipResponder}
                  tableKey={tableKey}
                />
              }
              onClick={(newFilter) => {
                handlerFilterCondition((prev) => [...prev, newFilter]);
              }}
            />
          );
        },
      },
      {
        title: '目的IPv4',
        dataIndex: 'ipv4_responder',
        show: false,
        disabled: true,
        searchable: true,
        operandType: EFieldOperandType.IPV4,
        fieldType: EFieldType.IPV4,
      },
      {
        title: '目的IPv6',
        dataIndex: 'ipv6_responder',
        show: false,
        disabled: true,
        searchable: true,
        operandType: EFieldOperandType.IPV6,
        fieldType: EFieldType.IPV6,
      },
      {
        title: '目的IP位置',
        dataIndex: 'ip_locality_responder',
        width: 120,
        show: true,
        render: (text) => IP_ADDRESS_LOCALITY_ENUM[text],
        searchable: true,
        operandType: EFieldOperandType.ENUM,
        enumValue: IP_ADDRESS_LOCALITY_LIST,
      },
      {
        title: '目的端口',
        dataIndex: 'port_responder',
        width: 90,
        show: true,
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
      },
      {
        title: '目的IP地址组',
        dataIndex: 'hostgroup_id_responder',
        width: 120,
        show: true,
        render: (id) => (isExisty(id) ? allIpAddressGroupMap[id]?.name || `[已删除: ${id}]` : ''),

        searchable: true,
        operandType: EFieldOperandType.ENUM,
        enumValue: ipAddressGroupEnum,
      },
      {
        title: '正向字节数',
        dataIndex: 'upstream_bytes',
        sorter: true,
        width: 110,
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
        render: (text) => numeral(text).format('0,0'),
      },
      {
        title: '反向字节数',
        dataIndex: 'downstream_bytes',
        sorter: true,
        width: 110,
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
        render: (text) => numeral(text).format('0,0'),
      },
      {
        title: '总字节数',
        dataIndex: 'total_bytes',
        sorter: true,
        width: 110,
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
        render: (text) => numeral(text).format('0,0'),
        show: true,
      },
      {
        title: '正向包数',
        dataIndex: 'upstream_packets',
        sorter: true,
        width: 100,
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
        render: (text) => numeral(text).format('0,0'),
      },
      {
        title: '反向包数',
        dataIndex: 'downstream_packets',
        sorter: true,
        width: 100,
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
        render: (text) => numeral(text).format('0,0'),
      },
      {
        title: '总包数',
        dataIndex: 'total_packets',
        sorter: true,
        width: 90,
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
        render: (text) => numeral(text).format('0,0'),
        show: true,
      },
      {
        title: '正向payload字节数',
        dataIndex: 'upstream_payload_bytes',
        sorter: true,
        width: 150,
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
        render: (text) => numeral(text).format('0,0'),
      },
      {
        title: '反向payload字节数',
        dataIndex: 'downstream_payload_bytes',
        sorter: true,
        width: 150,
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
        render: (text) => numeral(text).format('0,0'),
      },
      {
        title: 'payload总字节数',
        dataIndex: 'total_payload_bytes',
        sorter: true,
        width: 150,
        show: true,
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
        render: (text) => numeral(text).format('0,0'),
      },
      {
        title: '正向payload数据包',
        dataIndex: 'upstream_payload_packets',
        sorter: true,
        width: 150,
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
        render: (text) => numeral(text).format('0,0'),
      },
      {
        title: '反向payload数据包',
        dataIndex: 'downstream_payload_packets',
        sorter: true,
        width: 150,
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
        render: (text) => numeral(text).format('0,0'),
      },
      {
        title: 'payload总数据包',
        dataIndex: 'total_payload_packets',
        sorter: true,
        width: 150,
        show: true,
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
        render: (text) => numeral(text).format('0,0'),
      },
      {
        title: '新建会话数',
        dataIndex: 'established_sessions',
        sorter: true,
        width: 130,
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
        render: (text) => numeral(text).format('0,0'),
      },
      {
        title: 'TCP同步数据包',
        dataIndex: 'tcp_syn_packets',
        sorter: true,
        width: 130,
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
        render: (text) => numeral(text).format('0,0'),
      },
      {
        title: 'TCP同步确认数据包',
        dataIndex: 'tcp_syn_ack_packets',
        sorter: true,
        width: 150,
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
        render: (text) => numeral(text).format('0,0'),
      },
      {
        title: 'TCP同步重置数据包',
        dataIndex: 'tcp_syn_rst_packets',
        sorter: true,
        width: 150,
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
        render: (text) => numeral(text).format('0,0'),
      },
      {
        title: 'TCP客户端重传包数',
        dataIndex: 'tcp_client_retransmission_packets',
        sorter: true,
        width: 150,
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
        render: (text) => numeral(text).format('0,0'),
      },
      {
        title: '客户端重传率',
        dataIndex: 'tcp_client_retransmission_rate',
        // @warning: 后端自行计算，无法排序
        // sorter: true,
        width: 130,
        // searchable: true,
        operandType: EFieldOperandType.NUMBER,
        render: (text) =>
          `${numeral((text || 0) * 100)
            .value()
            ?.toFixed(2)}%`,
      },
      {
        title: 'TCP服务端重传包数',
        dataIndex: 'tcp_server_retransmission_packets',
        sorter: true,
        width: 170,
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
        render: (text) => numeral(text).format('0,0'),
      },
      {
        title: '服务端重传率',
        dataIndex: 'tcp_server_retransmission_rate',
        // @warning: 后端自行计算，无法排序
        // sorter: true,
        width: 130,
        // searchable: true,
        operandType: EFieldOperandType.NUMBER,
        render: (text) =>
          `${numeral((text || 0) * 100)
            .value()
            ?.toFixed(2)}%`,
      },
      {
        title: 'ETH类型',
        dataIndex: 'ethernet_type',
        width: 150,
        show: true,
        render: (text) => ETHERNET_TYPE_ENUM[text],
        searchable: true,
        operandType: EFieldOperandType.ENUM,
        enumValue: ETHERNET_TYPE_LIST,
      },
      {
        title: '源MAC',
        dataIndex: 'ethernet_initiator',
        ellipsis: true,
        width: 100,
        searchable: true,
      },
      {
        title: '目的MAC',
        dataIndex: 'ethernet_responder',
        ellipsis: true,
        width: 100,
        searchable: true,
      },
      {
        title: '网络层协议',
        dataIndex: 'ethernet_protocol',
        ellipsis: true,
        width: 90,
        searchable: true,
      },
      {
        title: 'VLANID',
        dataIndex: 'vlan_id',
        width: 100,
        ellipsis: true,
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
      },
      {
        title: '传输层协议',
        dataIndex: 'ip_protocol',
        width: 90,
        show: true,
        searchable: true,
        operandType: EFieldOperandType.ENUM,
        enumValue: IP_PROTOCOL_LIST.map((item) => ({
          text: item.toLocaleUpperCase(),
          value: item,
        })),
        render: (text) => {
          if (typeof text === 'string') {
            return text.toLocaleUpperCase();
          }
          return text;
        },
      },
      {
        title: '应用层协议',
        dataIndex: 'l7_protocol_id',
        width: 100,
        show: true,
        searchable: true,
        operandType: EFieldOperandType.ENUM,
        enumValue: l7ProtocolEnum,
        render: (text) => {
          const l7ProtocolInfo = allL7ProtocolMap[text];
          if (!l7ProtocolInfo) {
            return text;
          }
          return (
            <Tooltip placement="topLeft" title={l7ProtocolInfo.descriptionText}>
              {l7ProtocolInfo.nameText}
            </Tooltip>
          );
        },
      },
      {
        title: '应用分类',
        dataIndex: 'application_category_id',
        width: 100,
        show: true,
        render: (text, record) => (
          <Tooltip placement="topLeft" title={record.application_category_description}>
            {record.application_category_id}
          </Tooltip>
        ),
      },
      {
        title: '应用子分类',
        dataIndex: 'application_subcategory_id',
        width: 110,
        show: true,
        render: (text, record) => {
          const { application_subcategory_name } = record;
          return (
            <Tooltip placement="topLeft" title={record.application_subcategory_description}>
              {application_subcategory_name}
            </Tooltip>
          );
        },
      },
      {
        title: '应用',
        dataIndex: 'application_id',
        width: 110,
        searchable: true,
        operandType: EFieldOperandType.ENUM,
        // 默认应用 + 自定义应用
        enumValue: applicationList.map((app) => ({
          text: app.nameText,
          value: app.applicationId,
        })),
        render: (text, record) => (
          <Tooltip placement="topLeft" title={record.application_description}>
            {record.application_name}
          </Tooltip>
        ),
      },
      {
        title: '源IP国家',
        dataIndex: 'country_id_initiator',
        width: 100,
        ellipsis: true,
        show: true,
        searchable: true,
        operandType: EFieldOperandType.ENUM,
        enumValue: countryEnum,
        render: (id) => (isExisty(id) ? allCountryMap[id]?.nameText || `[已删除: ${id}]` : ''),
      },
      {
        title: '源IP省份',
        dataIndex: 'province_id_initiator',
        width: 100,
        ellipsis: true,
        show: true,
        searchable: true,
        operandType: EFieldOperandType.ENUM,
        enumValue: provinceEnum,
        render: (id) => (isExisty(id) ? allProvinceMap[id]?.nameText || `[已删除: ${id}]` : ''),
      },
      {
        title: '源IP城市',
        dataIndex: 'city_id_initiator',
        width: 100,
        ellipsis: true,
        show: true,
        searchable: true,
        operandType: EFieldOperandType.ENUM,
        enumValue: cityEnum,
        render: (id) => (isExisty(id) ? allCityMap[id]?.nameText || `[已删除: ${id}]` : ''),
      },
      {
        title: '目的IP国家',
        dataIndex: 'country_id_responder',
        width: 100,
        ellipsis: true,
        show: true,
        searchable: true,
        operandType: EFieldOperandType.ENUM,
        enumValue: countryEnum,
        render: (id) => (isExisty(id) ? allCountryMap[id]?.nameText || `[已删除: ${id}]` : ''),
      },
      {
        title: '目的IP省份',
        dataIndex: 'province_id_responder',
        width: 100,
        ellipsis: true,
        show: true,
        searchable: true,
        operandType: EFieldOperandType.ENUM,
        enumValue: provinceEnum,
        render: (id) => (isExisty(id) ? allProvinceMap[id]?.nameText || `[已删除: ${id}]` : ''),
      },
      {
        title: '目的IP城市',
        dataIndex: 'city_id_responder',
        width: 100,
        ellipsis: true,
        show: true,
        searchable: true,
        operandType: EFieldOperandType.ENUM,
        enumValue: cityEnum,
        render: (id) => (isExisty(id) ? allCityMap[id]?.nameText || `[已删除: ${id}]` : ''),
      },
      {
        title: '客户端网络时延(ms)',
        dataIndex: 'tcp_client_network_latency',
        // @warning: 展示的原始分片流，可以排序；如果牵扯到流的合并则不支持排序
        sorter: true,
        width: 150,
        show: true,
        render: (text) => numeral(text).format('0,0'),
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
      },
      {
        title: '服务端网络时延(ms)',
        dataIndex: 'tcp_server_network_latency',
        // @warning: 展示的原始分片流，可以排序；如果牵扯到流的合并则不支持排序
        sorter: true,
        width: 150,
        show: true,
        render: (text) => numeral(text).format('0,0'),
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
      },
      {
        title: '服务端响应时延(ms)',
        dataIndex: 'server_response_latency',
        // @warning: 展示的原始分片流，可以排序；如果牵扯到流的合并则不支持排序
        sorter: true,
        width: 150,
        show: true,
        render: (text) => numeral(text).format('0,0'),
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
      },
      {
        title: 'TCP客户端零窗口包数',
        dataIndex: 'tcp_client_zero_window_packets',
        sorter: true,
        width: 200,
        show: true,
        render: (text) => numeral(text).format('0,0'),
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
      },
      {
        title: 'TCP客户端零窗口占比',
        dataIndex: 'tcp_client_zero_window_packets_rate',
        width: 200,
        show: true,
        render: (text, record) => {
          const { upstream_packets = 0, tcp_client_zero_window_packets = 0 } = record;
          const rate = upstream_packets > 0 ? tcp_client_zero_window_packets / upstream_packets : 0;
          return `${numeral(rate * 100 || 0)
            .value()
            ?.toFixed(2)}%`;
        },
      },
      {
        title: 'TCP服务端零窗口包数',
        dataIndex: 'tcp_server_zero_window_packets',
        sorter: true,
        width: 200,
        show: true,
        render: (text) => numeral(text).format('0,0'),
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
      },
      {
        title: 'TCP服务端零窗口占比',
        dataIndex: 'tcp_server_zero_window_packets_rate',
        width: 200,
        show: true,
        render: (text, record) => {
          const { downstream_packets = 0, tcp_server_zero_window_packets = 0 } = record;
          const rate =
            downstream_packets > 0 ? tcp_server_zero_window_packets / downstream_packets : 0;
          return `${numeral(rate * 100 || 0)
            .value()
            ?.toFixed(2)}%`;
        },
      },

      {
        title: 'TCP客户端丢包字节数',
        dataIndex: 'tcp_client_loss_bytes',
        sorter: true,
        width: 200,
        show: true,
        render: (text) => numeral(text).format('0,0'),
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
      },
      {
        title: 'TCP服务端丢包字节数',
        dataIndex: 'tcp_server_loss_bytes',
        width: 200,
        show: true,
        sorter: true,
        render: (text) => numeral(text).format('0,0'),
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
      },
      {
        title: 'TCP连接状态',
        dataIndex: 'tcp_session_state',
        sorter: true,
        width: 150,
        show: true,
        render: (text) => TCP_SESSION_STATE_ENUM[text],
        searchable: true,
        operandType: EFieldOperandType.ENUM,
        enumValue: TCP_SESSION_STATE_LIST,
      },
      {
        title: '特征序列',
        dataIndex: 'packet_sigseq',
        width: 260,
        render: (text) => <PoAndNeModel text={text} />,
      },
      {
        title: '操作',
        dataIndex: COLUMN_ACTION_KEY,
        width: 200,
        show: true,
        fixed: 'right' as any,
        disabled: true,
        render: (text: any, record: IFlowRecordData) => {
          if (selectedTimeInfo) {
            return (
              <Fragment>
                <JumpToMatadata record={record} />
                <OperationToPacketBtn
                  record={record}
                  startTime={selectedTimeInfo.startTime}
                  endTime={selectedTimeInfo.endTime}
                  analysisResultId={analysisResultId}
                />
              </Fragment>
            );
          }
          return '';
        },
      },
    ],
    [
      allCityMap,
      allCountryMap,
      allIpAddressGroupMap,
      allL7ProtocolMap,
      allProvinceMap,
      analysisResultId,
      applicationList,
      cityEnum,
      countryEnum,
      handlerFilterCondition,
      ipAddressGroupEnum,
      l7ProtocolEnum,
      nsColumns,
      pageProps,
      provinceEnum,
      selectedTimeInfo,
      tableKey,
    ],
  );

  return allCols;
};

export default useFlowRecordColumns;
