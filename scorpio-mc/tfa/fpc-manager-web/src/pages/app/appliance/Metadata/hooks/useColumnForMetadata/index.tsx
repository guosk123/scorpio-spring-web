import Ellipsis from '@/components/Ellipsis';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import type {
  IEnumValue,
  IFilter,
  IFilterCondition,
  IFilterGroup,
} from '@/components/FieldFilter/typings';
import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { ESortDirection } from '@/pages/app//analysis/typings';
import IpDirlldownMenu from '@/pages/app/analysis/components/IpDirlldownMenu';
import type { IUriParams } from '@/pages/app/analysis/typings';
import type { IFlowRecordColumnProps } from '@/pages/app/appliance/FlowRecord/Record';
import { METADATA_COLLECT_LEVEL_MAP } from '@/pages/app/configuration/MetadataCollectPolicy/typings';
import { ipV4Regex, jumpNewPage, snakeCase } from '@/utils/utils';
import { Space, Tooltip } from 'antd';
import moment from 'moment';
import { stringify } from 'qs';
import type { SetStateAction } from 'react';
import { useMemo } from 'react';
import type { LogicalSubnetModelState, NetworkModelState, ServiceModelState } from 'umi';
import { history, useParams, useSelector } from 'umi';
import FilterBubbleForArray from '../../components/FilterBubbleForArray';
import FilterBubbleForMap from '../../components/FilterBubbleForMap';
import JumpToRecordFromMeta from '../../components/JumpToRecordFromMeta';
import { getMetadataFilter } from '../../components/Template/getMetadataFilter';
import { getFilterField } from '../../components/utils';
import type { IMetadataFile, IMetadataLog } from '../../typings';
import { EMetadataProtocol } from '../../typings';

export interface IMetadataLayoutProps<MetaLog> {
  // 用来表示是否是统一使用src_ip字段来表示，
  // false时: src_ipv4,src_ipv6,dest_ipv4,dest_ipv6
  // true: src_ip, dest_ip
  isNewIpFieldType?: boolean;
  // 协议
  protocol: EMetadataProtocol;
  // 不同的协议表格展示不同的列
  tableColumns: IColumnProps<MetaLog>[];

  // 入口
  entry?: string;
}

// 策略和等级放在最后
export const policyColumns: IColumnProps<any>[] = [
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

export const DISABLE_FILTER_BUBBLE = ['dnsQueries', 'answer', 'parameters'];

/**
 * 表格定义
 */
export type IColumnProps<RecordType> = IFlowRecordColumnProps<RecordType>;
interface Props {
  protocol: any;
  tableColumns: any;
  isNewIpFieldType?: boolean;
  onFilterClick?: (value: SetStateAction<IFilterCondition>) => void;
  selectedTimeInfo?:
    | Required<IGlobalTime>
    | {
        startTime: string;
        endTime: string;
        originStartTime: string;
        originEndTime: string;
      };
  sortDirection?: ESortDirection;
}

function useColumnForMetadata(props: Props) {
  const {
    protocol,
    tableColumns,
    isNewIpFieldType = false,
    onFilterClick: setFilterCondition,
    selectedTimeInfo,
    sortDirection = ESortDirection.ASC,
  } = props;

  const { allNetworks, allNetworkMap } = useSelector<ConnectState, NetworkModelState>(
    (state) => state.networkModel,
  );
  const { allLogicalSubnets, allLogicalSubnetMap } = useSelector<
    ConnectState,
    LogicalSubnetModelState
  >((state) => state.logicSubnetModel);
  const { allServices, allServiceMap } = useSelector<ConnectState, ServiceModelState>(
    (state) => state.serviceModel,
  );
  const analysisResultId = history.location.query?.analysisResultId;

  const { pcapFileId }: IUriParams = useParams();

  // 公共的表格列
  const commonColumns: IColumnProps<IMetadataLog>[] = useMemo(() => {
    const networkEnum: IEnumValue[] = [...allLogicalSubnets, ...allNetworks].map((item) => {
      return { text: item.name, value: item.id };
    });

    const serviceEnum: IEnumValue[] = allServices.map((item) => {
      return {
        text: item.name,
        value: item.id,
      };
    });

    return [
      {
        title: '#',
        dataIndex: 'index',
        align: 'center',
        searchable: false,
        width: 50,
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
        align: 'center',
        searchable: true,
        width: 100,
        fieldType: protocol !== EMetadataProtocol.FILE ? EFieldType.ARRAY : undefined,
        operandType: EFieldOperandType.ENUM,
        enumValue: networkEnum,
        show: true,
      },
      {
        title: '业务',
        dataIndex: 'serviceId',
        align: 'center',
        searchable: protocol !== EMetadataProtocol.FILE,
        width: 100,
        show: protocol !== EMetadataProtocol.FILE,
        fieldType: EFieldType.ARRAY,
        operandType: EFieldOperandType.ENUM,
        enumValue: serviceEnum,
      },
    ];
  }, [allLogicalSubnets, allNetworks, allServices, sortDirection, protocol]);

  const ipField: IColumnProps<IMetadataLog>[] = useMemo(
    () =>
      [
        {
          title: '源IP',
          dataIndex: 'srcIp',
          width: 140,
          align: 'center' as const,
          searchable: isNewIpFieldType,
          fieldType: EFieldType.IP,
          operandType: EFieldOperandType.IP,
        },
        {
          title: '源IPv4',
          dataIndex: 'srcIpv4',
          show: false,
          align: 'center' as const,
          searchable: protocol !== EMetadataProtocol.ICMPV6 && !isNewIpFieldType,
          fieldType: EFieldType.IPV4,
          operandType: EFieldOperandType.IPV4,
        },
        {
          title: '源IPv6',
          dataIndex: 'srcIpv6',
          align: 'center' as const,
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
          align: 'center' as const,
          fieldType: EFieldType.IP,
          operandType: EFieldOperandType.IP,
        },
        {
          title: '目的IPv4',
          dataIndex: 'destIpv4',
          align: 'center' as const,
          show: false,
          searchable: protocol !== EMetadataProtocol.ICMPV6 && !isNewIpFieldType,
          fieldType: EFieldType.IPV4,
          operandType: EFieldOperandType.IPV4,
        },
        {
          title: '目的IPv6',
          dataIndex: 'destIpv6',
          show: false,
          align: 'center' as const,
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
          searchable:
            protocol === EMetadataProtocol.ICMPV4 || protocol === EMetadataProtocol.ICMPV6,
          operandType: EFieldOperandType.NUMBER,
        },
        {
          title: '应答数据长度',
          dataIndex: 'responseDataLen',
          width: 140,
          align: 'center',
          show: protocol === EMetadataProtocol.ICMPV4 || protocol === EMetadataProtocol.ICMPV6,
          searchable:
            protocol === EMetadataProtocol.ICMPV4 || protocol === EMetadataProtocol.ICMPV6,
          operandType: EFieldOperandType.NUMBER,
        },
        {
          title: 'IP',
          dataIndex: 'ip',
          show: false,
          searchable: isNewIpFieldType,
          fieldType: EFieldType.IP,
          operandType: EFieldOperandType.IP,
          directionConfig: {
            appendPos: 'prefix',
            srcAppend: 'src',
            destAppend: 'dest',
          },
        },
        {
          title: 'IPv4',
          dataIndex: 'ipv4',
          show: false,
          searchable: protocol !== EMetadataProtocol.ICMPV6 && !isNewIpFieldType,
          fieldType: EFieldType.IPV4,
          operandType: EFieldOperandType.IPV4,
          directionConfig: {
            appendPos: 'prefix',
            srcAppend: 'src',
            destAppend: 'dest',
          },
        },
        {
          title: 'IPv6',
          dataIndex: 'ipv6',
          show: false,
          searchable: protocol !== EMetadataProtocol.ICMPV4 && !isNewIpFieldType,
          fieldType: EFieldType.IPV6,
          operandType: EFieldOperandType.IPV6,
          directionConfig: {
            appendPos: 'prefix',
            srcAppend: 'src',
            destAppend: 'dest',
          },
        },
        {
          title: 'port',
          dataIndex: 'port',
          show: false,
          searchable: protocol !== EMetadataProtocol.ICMP && protocol !== EMetadataProtocol.OSPF,
          operandType: EFieldOperandType.PORT,
          directionConfig: {
            appendPos: 'prefix',
            srcAppend: 'src',
            destAppend: 'dest',
          },
        },
      ] as IColumnProps<IMetadataLog>[],
    [protocol, isNewIpFieldType],
  );

  // ===== 表格操作列 =====
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
          return (
            <Space>
              {protocol === EMetadataProtocol.FILE && (
                <span
                  className="link"
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
                        `/analysis/trace/metadata/record?jumpTabs=${l7Protocol.toLocaleLowerCase()}&${stringify(
                          {
                            from:
                              selectedTimeInfo?.originStartTime &&
                              new Date(selectedTimeInfo.originStartTime).getTime(),
                            to:
                              selectedTimeInfo?.originEndTime &&
                              new Date(selectedTimeInfo.originEndTime).getTime(),
                            timeType: ETimeType.CUSTOM,
                          },
                        )}&filter=${encodeURIComponent(JSON.stringify(filter))}`,
                      );
                    }
                  }}
                >
                  应用层协议详单
                </span>
              )}
              <JumpToRecordFromMeta
                record={record}
                protocol={protocol}
                selectedTimeInfo={selectedTimeInfo}
                pcapFileId={pcapFileId}
                analysisResultId={String(analysisResultId)}
              />
            </Space>
          );
        },
      },
    ],
    [analysisResultId, pcapFileId, protocol, selectedTimeInfo],
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
    [commonColumns, protocol, tableColumns, detailOperate, ipField],
  );

  // 表格所需要的字段
  const fullTableColumns = useMemo<IColumnProps<IMetadataLog>[]>(
    () =>
      fullColumns
        .filter((col) => !col?.hasOwnProperty('show') || col?.show !== false)
        .map((col) => {
          const newCol: IColumnProps<IMetadataLog> = {
            ...col,
            align: 'center',
            width: col?.width || 200,
          };

          const { dataIndex, operandType, fieldType, title } = newCol;

          if (fieldType === EFieldType.Map && setFilterCondition) {
            return {
              ...newCol,
              render: (dom, record) => {
                const data = (record[dataIndex!.toString()] as Record<string, string>) || {};
                const isEmpty = Object.keys(data)?.length < 1;
                if (isEmpty) {
                  return '-';
                }
                return (
                  <FilterBubbleForMap
                    data={data}
                    onFilterChange={setFilterCondition}
                    dataIndex={dataIndex}
                    protocol={protocol}
                    title={title}
                  />
                );
              },
            };
          }

          if (
            fieldType === EFieldType.ARRAY &&
            setFilterCondition &&
            ['networkId', 'serviceId'].includes(dataIndex?.toString() || '')
          ) {
            return {
              ...newCol,
              render: (dom, record) => {
                let values: string[] = record[col.dataIndex as string];
                if (operandType === EFieldOperandType.ENUM) {
                  values = values.filter((item) => {
                    return col.enumValue?.find((enumItem) => enumItem.value === item)?.text;
                  });
                }
                return values.map((item, index) => {
                  let content: string = item;
                  if (col.operandType === EFieldOperandType.ENUM) {
                    content =
                      col.enumValue?.find((enumItem) => enumItem.value === item)?.text || '';
                  }
                  return (
                    <FilterBubble
                      key={item}
                      dataIndex={snakeCase(col.dataIndex as string)}
                      label={
                        <Tooltip title={content} className="table-cell-button">
                          {newCol?.render ? newCol.render(content, record, index) : content}
                        </Tooltip>
                      }
                      operand={item}
                      fieldType={col.fieldType}
                      operandType={col.operandType as EFieldOperandType}
                      onClick={(newFilter) => {
                        setFilterCondition((prev) => [...prev, newFilter]);
                      }}
                    />
                  );
                });
              },
            };
          }

          if (!newCol.render) {
            newCol.render = (text: any) => {
              // return <Ellipsis lines={1}>{text}</Ellipsis>;
              return text;
            };
          }

          if (
            ((newCol.searchable && !DISABLE_FILTER_BUBBLE.includes(String(dataIndex))) ||
              ['srcIp', 'destIp'].includes(newCol.dataIndex! as string)) &&
            setFilterCondition
          ) {
            return {
              ...newCol,
              render: (_, record, index) => {
                const value = record[col.dataIndex as string];
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
                      dataIndex === 'codePoint' ? undefined : (
                        <IpDirlldownMenu
                          networkId={
                            record.networkId instanceof Array
                              ? record.networkId.find((item) => item)
                              : record.networkId
                          }
                          fromField={col.dataIndex as string}
                          ipAddressValue={value}
                          inDns={protocol === EMetadataProtocol.DNS}
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
            (newCol.dataIndex === 'domain' || newCol.dataIndex === 'domainAddress') &&
            setFilterCondition
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
                        networkId={(record.networkId as string[]).find((item) => item)}
                        fromField={col.dataIndex as string}
                        ipAddressValue={value}
                        inDns={protocol === EMetadataProtocol.DNS}
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
          return newCol;
        }),
    [fullColumns, isNewIpFieldType, protocol, setFilterCondition],
  );

  return { fullTableColumns, fullColumns };
}

export default useColumnForMetadata;
