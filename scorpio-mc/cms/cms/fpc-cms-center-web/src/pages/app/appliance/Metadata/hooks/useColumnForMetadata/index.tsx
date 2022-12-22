import Ellipsis from '@/components/Ellipsis';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import type { IFilter, IFilterCondition, IFilterGroup } from '@/components/FieldFilter/typings';
import {
  EFieldOperandType,
  EFieldType,
  EFilterOperatorTypes,
} from '@/components/FieldFilter/typings';
import { ETimeType } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import type { ESortDirection } from '@/pages/app//analysis/typings';
import IpDirlldownMenu from '@/pages/app/analysis/components/IpDirlldownMenu';
import { dimensionsUrl } from '@/pages/app/GlobalSearch/DimensionsSearch/SeartchTabs/constant';
import { camelCase, getLinkUrl, ipV4Regex, jumpNewPage, snakeCase } from '@/utils/utils';
import { useLatest } from 'ahooks';
import { Button, Dropdown, Menu, Space, Tooltip } from 'antd';
import moment from 'moment';
import { stringify } from 'qs';
import { useEffect, useMemo, useState } from 'react';
import type { IGlobalSelectedTime } from 'umi';
import { history, useLocation, useParams, useSelector } from 'umi';
import type { IFlowRecordColumnProps } from '../../../FlowRecords/Record/typing';
import { useNetworkList, useServiceList } from '../../../hooks';
import FilterBubbleForArray from '../../components/FilterBubbleForArray';
import JumpToFlowRecordBtn from '../../components/JumpToFlowRecordBtn';
import { DISABLE_FILTER_BUBBLE } from '../../components/Template';
import { getMetadataFilter } from '../../components/Template/getMetadataFilter';
import type { IMetadataFile, IMetadataLog } from '../../typings';
import { EMetadataProtocol, METADATA_COLLECT_LEVEL_MAP } from '../../typings';
export const getFilterField = (field: string, value: string) => {
  // 区分源目的ipv4、ipv6
  let tmpField = '';
  if (field === 'src_ip' || field === 'dest_ip') {
    if (field === 'src_ip') {
      tmpField = ipV4Regex.test(value) ? 'src_ipv4' : 'src_ipv6';
    } else {
      tmpField = ipV4Regex.test(value) ? 'dest_ipv4' : 'dest_ipv6';
    }
    return tmpField;
  }
  return snakeCase(field);
};

/**
 * 表格定义
 */
export interface IColumnProps<RecordType> extends IFlowRecordColumnProps<RecordType> {}

// 策略和等级放在最后
const policyColumns: IColumnProps<any>[] = [
  {
    title: '级别',
    dataIndex: 'level',
    align: 'center',
    width: 140,
    searchable: true,
    operandType: EFieldOperandType.ENUM,
    enumValue: Object.keys(METADATA_COLLECT_LEVEL_MAP).map((key) => ({
      text: METADATA_COLLECT_LEVEL_MAP[key],
      value: key,
    })),
    render: (level) => METADATA_COLLECT_LEVEL_MAP[level] || '低',
  },
  {
    title: '策略名称',
    dataIndex: 'policyName',
    align: 'center',
    searchable: true,
    width: 140,
    render: (text) => (
      <Ellipsis tooltip lines={1}>
        {text || '默认'}
      </Ellipsis>
    ),
  },
];
interface Props {
  pageProps?: { currentPage: number; pageSize: number };
  protocol: any;
  tableColumns: any;
  isNewIpFieldType?: boolean;
  onFilterClick?: [IFilterCondition, React.Dispatch<React.SetStateAction<IFilterCondition>>];
  sortDirection?: ESortDirection;
}

function useColumnForMetadata(props: Props) {
  const {
    protocol,
    tableColumns,
    isNewIpFieldType = false,
    pageProps,
    onFilterClick,
    sortDirection,
  } = props;
  const location = useLocation();

  const {
    // filter: uriFilter = '',
    analysisResultId = '',
    analysisStartTime = '',
    analysisEndTime = '',
  } = history.location.query || {};
  const isDimensionsTab = history.location.pathname.includes(dimensionsUrl);

  const { serviceId: urlServiceId, networkId: urlNetworkId } = useParams() as {
    networkId: string;
    serviceId: string;
  };
  const { serviceId, networkId } = (() => {
    if (urlNetworkId?.includes('^')) {
      return {
        serviceId: urlServiceId,
        networkId: urlNetworkId?.split('^')[1],
      };
    }
    return { serviceId: urlServiceId, networkId: urlNetworkId };
  })();

  const [pageIsReady, setPageIsReady] = useState<boolean>(false);
  const defFilter = useState<IFilterCondition>([]);
  // 排序方向
  // filter过滤条件
  const [filterCondition, setFilterCondition]: [
    IFilterCondition,
    React.Dispatch<React.SetStateAction<IFilterCondition>>,
  ] = (onFilterClick as any) || defFilter;
  // const [filterCondition, setFilterCondition] = useState<IFilterCondition>([]);

  const filterConditionRef = useLatest(filterCondition);

  const networkList = useNetworkList();
  const serviceList = useServiceList();

  const { originStartTime, originEndTime } = useSelector<
    ConnectState,
    Required<IGlobalSelectedTime>
  >((state) => state.appModel.globalSelectedTime);

  // 公共的表格列
  const commonColumns: IColumnProps<IMetadataLog>[] = useMemo(
    () => [
      {
        title: '#',
        dataIndex: 'index',
        align: 'center',
        searchable: false,
        width: 50,
        render: (text, record, index) => {
          const number = index + 1;
          const { currentPage, pageSize } = pageProps || {};
          if (!currentPage) {
            return number;
          }
          return (currentPage - 1) * Math.abs(pageSize!) + number;
        },
      },
      {
        title: '时间',
        dataIndex: 'startTime',
        sorter: true,
        sortOrder: `${sortDirection}end` as IColumnProps<any>['sortOrder'] as any,
        align: 'center',
        searchable: false,
        show: protocol !== EMetadataProtocol.FILE,
        width: 180,
        render: (startTime) => moment(startTime).format('YYYY-MM-DD HH:mm:ss'),
      },
      {
        title: '网络',
        dataIndex: 'networkId',
        width: 150,
        show: true,
        searchable: true,
        operandType: EFieldOperandType.ENUM,
        fieldType: protocol !== EMetadataProtocol.FILE ? EFieldType.ARRAY : undefined,
        enumValue: networkList.map((network) => ({
          text: network.title,
          value: network.value,
        })),
        render: (text, record) => {
          const networkMap: Record<string, string> = {};
          networkList.forEach((network) => {
            networkMap[network.value] = network.title;
          });
          if (typeof record.networkId === 'string') {
            return networkMap[record.networkId] || '';
          }
          return record.networkId?.map((item: string) => {
            return {
              text: <span className="show-text">{networkMap[item] || ''}</span>,
              value: item,
            };
          });
        },
      },
      {
        title: '业务',
        dataIndex: 'serviceId',
        width: 150,
        show: protocol !== EMetadataProtocol.FILE,
        searchable: protocol !== EMetadataProtocol.FILE,
        operandType: EFieldOperandType.ENUM,
        fieldType: EFieldType.ARRAY,
        enumValue: serviceList.map((service) => ({
          text: service.title,
          value: service.value,
        })),
        render: (text: string, record: any) => {
          const serviceMap = {};
          serviceList.forEach((service) => {
            serviceMap[service.value] = service.title;
          });
          return record.service_id?.map((item: string) => {
            return {
              text: <span className="show-text">{serviceMap[item] || ''}</span>,
              value: item,
            };
          });
        },
      },
    ],
    [sortDirection, protocol, networkList, serviceList, pageProps],
  );

  const ipField: IColumnProps<IMetadataLog>[] = useMemo(
    () => [
      {
        title: '源IP',
        dataIndex: 'srcIp',
        width: 140,
        align: 'center',
        searchable: isNewIpFieldType,
        fieldType: EFieldType.IP,
        operandType: EFieldOperandType.IP,
      },
      {
        title: '源IPv4',
        dataIndex: 'srcIpv4',
        show: false,
        align: 'center',
        searchable: protocol !== EMetadataProtocol.ICMPV6 && !isNewIpFieldType,
        fieldType: EFieldType.IPV4,
        operandType: EFieldOperandType.IPV4,
      },
      {
        title: '源IPv6',
        dataIndex: 'srcIpv6',
        align: 'center',
        show: false,
        searchable: protocol !== EMetadataProtocol.ICMPV4 && !isNewIpFieldType,
        fieldType: EFieldType.IPV6,
        operandType: EFieldOperandType.IPV6,
      },
      {
        title: '源端口',
        dataIndex: 'srcPort',
        width: 140,
        align: 'center',
        show: protocol !== EMetadataProtocol.ICMP && protocol !== EMetadataProtocol.OSPF,
        searchable: protocol !== EMetadataProtocol.ICMP && protocol !== EMetadataProtocol.OSPF,
        operandType: EFieldOperandType.PORT,
      },
      {
        title: '目的IP',
        dataIndex: 'destIp',
        searchable: isNewIpFieldType,
        width: 140,
        align: 'center',
        fieldType: EFieldType.IP,
        operandType: EFieldOperandType.IP,
      },
      {
        title: '目的IPv4',
        dataIndex: 'destIpv4',
        align: 'center',
        show: false,
        searchable: protocol !== EMetadataProtocol.ICMPV6 && !isNewIpFieldType,
        fieldType: EFieldType.IPV4,
        operandType: EFieldOperandType.IPV4,
      },
      {
        title: '目的IPv6',
        dataIndex: 'destIpv6',
        show: false,
        align: 'center',
        searchable: protocol !== EMetadataProtocol.ICMPV4 && !isNewIpFieldType,
        fieldType: EFieldType.IPV6,
        operandType: EFieldOperandType.IPV6,
      },

      {
        title: '目的端口',
        dataIndex: 'destPort',
        width: 140,
        align: 'center',
        show: protocol !== EMetadataProtocol.ICMP && protocol !== EMetadataProtocol.OSPF,
        searchable: protocol !== EMetadataProtocol.ICMP && protocol !== EMetadataProtocol.OSPF,
        operandType: EFieldOperandType.PORT,
      },
      {
        title: '请求数据长度',
        dataIndex: 'requestDataLen',
        width: 140,
        align: 'center',
        show: protocol === EMetadataProtocol.ICMPV4 || protocol === EMetadataProtocol.ICMPV6,
        searchable: protocol === EMetadataProtocol.ICMPV4 || protocol === EMetadataProtocol.ICMPV6,
        operandType: EFieldOperandType.NUMBER,
      },
      {
        title: '应答数据长度',
        dataIndex: 'responseDataLen',
        width: 140,
        align: 'center',
        show: protocol === EMetadataProtocol.ICMPV4 || protocol === EMetadataProtocol.ICMPV6,
        searchable: protocol === EMetadataProtocol.ICMPV4 || protocol === EMetadataProtocol.ICMPV6,
        operandType: EFieldOperandType.NUMBER,
      },
    ],
    [protocol, isNewIpFieldType],
  );

  // ====== Filter过滤 E ======

  // ===== 表格字段设置 =====
  const detailOperate: IColumnProps<IMetadataLog>[] = useMemo(
    () => [
      {
        title: '操作',
        dataIndex: 'action',
        fixed: 'right',
        align: 'center',
        searchable: false,
        width: protocol === EMetadataProtocol.FILE ? 200 : 150,
        render: (text, record) => {
          const { startTime, flowId } = record;
          // 没有flow_packet_id时，无法下载和在线分析
          if (!flowId || flowId === '0') {
            return null;
          }

          // 如果页面是在场景分析结果中，跳转到详单即可
          if (location.pathname.includes('/scenario-task/result/')) {
            return (
              <span
                className="link"
                onClick={() => {
                  jumpNewPage(
                    getLinkUrl(
                      `/detection/scenario-task/result/flow-record?analysisResultId=${analysisResultId}&analysisStartTime=${encodeURIComponent(
                        String(analysisStartTime!),
                      )}&analysisEndTime=${encodeURIComponent(
                        String(analysisEndTime!),
                      )}&flowId=${flowId}`,
                    ),
                  );
                }}
              >
                会话详单
              </span>
            );
          }

          // 判断元数据开始时间到现在的时间间隔是否在1小时内，
          // 在1小时内的话提示下可能还未生成
          const isOverOneHours = Math.abs(moment(startTime).diff(moment(), 'hours', true)) > 1;
          // 根据五元组进行过滤流日志

          const linkDom = (
            <JumpToFlowRecordBtn
              record={record}
              networkId={
                networkId ||
                (filterCondition.find((item: any) => item?.field?.includes('network')) as any)
                  ?.operand
              }
              serviceId={
                serviceId ||
                (filterCondition.find((item: any) => item?.field?.includes('service')) as any)
                  ?.operand
              }
              protocol={protocol}
            />
          );
          return (
            <Space>
              {protocol === EMetadataProtocol.FILE && (
                <span
                  className="link"
                  style={{ marginRight: 8 }}
                  onClick={() => {
                    const { srcIp, srcPort, destIp, destPort, l7Protocol } =
                      record as IMetadataFile;

                    if (l7Protocol) {
                      const filter: IFilter[] = getMetadataFilter({
                        srcIp,
                        destIp,
                        srcPort,
                        destPort,
                        protocol: l7Protocol.toLocaleLowerCase() as EMetadataProtocol,
                      });
                      jumpNewPage(
                        `/flow-trace/mata-data-detail?${stringify({
                          from: new Date(originStartTime).getTime(),
                          to: new Date(originEndTime).getTime(),
                          timeType: ETimeType.CUSTOM,
                          filter: encodeURIComponent(JSON.stringify(filter)),
                          jumpTabs: l7Protocol.toLocaleLowerCase(),
                        })}`,
                      );
                    }
                  }}
                >
                  应用层分析详单
                </span>
              )}
              {/* 判断开始时间 */}
              {isOverOneHours ? linkDom : <Tooltip title="会话详单可能还未生成">{linkDom}</Tooltip>}
            </Space>
          );
        },
      },
    ],
    [
      analysisEndTime,
      analysisResultId,
      analysisStartTime,
      filterCondition,
      location.pathname,
      networkId,
      originEndTime,
      originStartTime,
      protocol,
      serviceId,
    ],
  );

  const fullColumns = useMemo(
    () =>
      ([] as IColumnProps<IMetadataLog>[]).concat(
        commonColumns,
        protocol === EMetadataProtocol.DHCP ||
          protocol === EMetadataProtocol.DHCPV6 ||
          protocol === EMetadataProtocol.ARP
          ? []
          : ipField,
        tableColumns,
        protocol === EMetadataProtocol.DHCP ||
          protocol === EMetadataProtocol.DHCPV6 ||
          protocol === EMetadataProtocol.FILE
          ? []
          : policyColumns,
        detailOperate,
      ),
    [commonColumns, protocol, ipField, tableColumns, detailOperate],
  );

  // 表格所需要的字段
  const fullTableColumns = useMemo<IColumnProps<IMetadataLog>[]>(
    () =>
      fullColumns
        .filter((col) => !col.hasOwnProperty('show') || col.show !== false)
        .filter((item) => !isDimensionsTab || item.dataIndex !== 'action')
        .map((col) => {
          const newCol: IColumnProps<IMetadataLog> = {
            ...col,
            align: 'center',
            width: col.width || 200,
            dataIndex: camelCase(col.dataIndex as string),
          };

          const { dataIndex, operandType, fieldType, title } = newCol;

          if (
            fieldType === EFieldType.ARRAY &&
            ['networkId', 'serviceId'].includes(dataIndex?.toString() || '')
          ) {
            return {
              ...newCol,
              render: (dom, record) => {
                const values: string[] = record[newCol.dataIndex as string] || [];
                return values.map((item) => {
                  let content: string = item;
                  if (col.operandType === EFieldOperandType.ENUM) {
                    content =
                      col.enumValue?.find((enumItem) => enumItem.value === item)?.text || '';
                    if (!content) return;
                  }
                  return (
                    <FilterBubble
                      containerStyle={{ display: 'inline', padding: '0 4px' }}
                      key={item}
                      dataIndex={snakeCase(col.dataIndex as string)}
                      label={content}
                      operand={item}
                      fieldType={col.fieldType}
                      operandType={col.operandType as EFieldOperandType}
                      onClick={(newFilter) => {
                        const copyFilter = { ...newFilter };
                        if (newFilter.field === 'network_id') {
                          copyFilter.disabled = true;
                        }
                        setFilterCondition((prev) => [...prev, copyFilter]);
                      }}
                      disabled={
                        filterConditionRef.current.findIndex((f) => {
                          if ((f as IFilterGroup).group) return false;
                          return (f as IFilter).field === 'network_id';
                        }) !== -1
                      }
                    />
                  );
                });
              },
            };
          }

          if (fieldType === EFieldType.Map) {
            return {
              ...newCol,
              render: (dom, record) => {
                const data = (record[dataIndex!.toString()] as Record<string, string>) || {};
                const isEmpty = Object.keys(data).length < 1;
                if (isEmpty) {
                  return '-';
                }
                return (
                  <Dropdown
                    trigger={['click']}
                    overlay={
                      <Menu style={{ maxHeight: '200px', overflowY: 'auto', maxWidth: '400px' }}>
                        {Object.keys(data).map((key) => {
                          return (
                            <Menu.Item
                              key={key}
                              onClick={() => {
                                setFilterCondition((prev) => {
                                  return [
                                    ...prev,
                                    {
                                      field: `${snakeCase(dataIndex?.toString() || '')}.${key}`,
                                      type: EFieldType.Map,
                                      operator: EFilterOperatorTypes.EQ,
                                      operand: data[key],
                                    },
                                  ];
                                });
                              }}
                            >
                              {key}={data[key]}
                            </Menu.Item>
                          );
                        })}
                      </Menu>
                    }
                  >
                    <Button size="small" type="text">
                      {protocol === EMetadataProtocol.LDAP ? title : `${dataIndex}内容`}
                    </Button>
                  </Dropdown>
                );
              },
            };
          }

          if (
            (newCol.searchable && !DISABLE_FILTER_BUBBLE.includes(String(dataIndex))) ||
            ['srcIp', 'destIp'].includes(newCol.dataIndex! as string)
          ) {
            return {
              ...newCol,
              render: (value, record, index) => {
                const tdText = newCol?.render ? newCol?.render(value, record, index) : value;
                if (col.dataIndex === 'urlList') {
                  return (
                    <FilterBubbleForArray
                      title={String(col.title)}
                      filterItems={value}
                      dataIndex={dataIndex}
                      value={value}
                      setFilterCondition={setFilterCondition}
                      record={record}
                      index={index}
                      fieldType={fieldType}
                      newCol={newCol}
                      operandType={operandType}
                    />
                  );
                }
                return (
                  <FilterBubble
                    style={{ zIndex: 1200 }}
                    dataIndex={
                      isNewIpFieldType
                        ? snakeCase(dataIndex! as string)
                        : getFilterField(snakeCase(dataIndex! as string), value)
                    }
                    label={tdText}
                    operand={value}
                    fieldType={fieldType}
                    operandType={operandType!}
                    DrilldownMenu={
                      isDimensionsTab ? (
                        <div style={{ display: 'none' }} />
                      ) : (
                        <IpDirlldownMenu
                          indexKey={`${col.dataIndex}-metadata`}
                          ipAddressKeys={value}
                          tableKey={protocol}
                        />
                      )
                    }
                    onClick={(newFilter) => {
                      setFilterCondition((prev) => {
                        return [...prev, ...[newFilter]];
                      });
                    }}
                  />
                );
              },
            };
          }
          if (
            newCol.dataIndex === 'parameters' ||
            newCol.dataIndex === 'domain' ||
            newCol.dataIndex === 'domainAddress'
          ) {
            return {
              ...newCol,
              render: (value, record, index) => {
                const tdText = newCol?.render ? newCol?.render(value, record, index) : value;
                return (
                  <FilterBubble
                    style={{ zIndex: 1200 }}
                    dataIndex={getFilterField(snakeCase(dataIndex! as string), value)}
                    label={tdText}
                    operand={value}
                    fieldType={fieldType}
                    operandType={operandType!}
                    DrilldownMenu={
                      <IpDirlldownMenu
                        indexKey={`${col.dataIndex}-metadata`}
                        ipAddressKeys={value}
                        tableKey={protocol}
                      />
                    }
                    onClick={(newFilter) => {
                      const tmpOperand = newFilter?.operand as unknown as string[] | number[];
                      const group = tmpOperand.map((item) => {
                        // 区分domainAddress IPv4、IPv6
                        let tmpField;
                        if (newFilter.field === 'domain_address') {
                          tmpField = ipV4Regex.test(item as string) ? 'domain_ipv4' : 'domain_ipv6';
                        }
                        return {
                          field: tmpField || newFilter.field,
                          operator: newFilter.operator,
                          operand: item,
                        };
                      });
                      const tmp = { group, operator: 'OR' } as IFilterGroup;
                      setFilterCondition((prev) => {
                        return [...prev, tmp];
                      });
                    }}
                  />
                );
              },
            };
          }

          if (!newCol.render) {
            newCol.render = (text: any) => {
              return <Ellipsis lines={1}>{text}</Ellipsis>;
            };
          }

          return newCol;
        }),

    [
      filterConditionRef,
      fullColumns,
      isDimensionsTab,
      isNewIpFieldType,
      protocol,
      setFilterCondition,
    ],
  );

  // useEffect(() => {
  //   if (!filter) {
  //     return;
  //   }
  //   // 转换过滤条件
  //   const filterJson: IFilterCondition = parseArrayJson(decodeURIComponent(String(filter)));
  //   setFilterCondition(deduplicateCondition(filterJson, new Set()));
  //   // eslint-disable-next-line react-hooks/exhaustive-deps
  // }, [filter]);

  useEffect(() => {
    if (!pageIsReady) {
      setPageIsReady(true);
    }
  }, [pageIsReady]);

  return { fullTableColumns, fullColumns };
}

export default useColumnForMetadata;
