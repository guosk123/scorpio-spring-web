// import { getTablePaginationDefaultSettings, proTableSerchConfig } from '@/common/app';
// import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
// import type { IAjaxResponseFactory, IPageFactory, IProTableData } from '@/common/typings';
// import AutoHeightContainer from '@/components/AutoHeightContainer';
// import EllipsisDiv from '@/components/EllipsisDiv';
// import { TableEmpty } from '@/components/EnhancedTable';
// import type { IFilterCondition } from '@/components/FieldFilter/typings';
// import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
// import type { IGlobalTime } from '@/components/GlobalTimeSelector';
// import { ETimeType, getGlobalTime } from '@/components/GlobalTimeSelector';
// import type { ConnectState } from '@/models/connect';
// import type { IUriParams } from '@/pages/app/analysis/typings';
// import type { IL7Protocol, IL7ProtocolMap } from '@/pages/app/appliance/Metadata/typings';
// import { EMetadataProtocol } from '@/pages/app/appliance/Metadata/typings';
// import ajax from '@/utils/frame/ajax';
// import { getLinkUrl, isIpv4, jumpNewPage } from '@/utils/utils';
// import type { ActionType, ProColumns } from '@ant-design/pro-table';
// import ProTable from '@ant-design/pro-table';
// import { Button, Result, Select, Space } from 'antd';
// import { stringify } from 'querystring';
// import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
// import { connect, useParams } from 'umi';
// import type { INetworkMap } from '../../Network/typings';
// import type { IAbnormalEventMessage } from '../typings';
// import { ABNORMAL_EVENT_TYPE_ENUM } from '../typings';

// const jumpFilterInUrl = (srcIp: string, destIp: string, destPort: number, l7ProtocolId: number) => {
//   // 跳转的时候 通过 源IP、目的IP、目的端口、协议 4个字段进行跳转
//   // 如果字段为空，则跳转时，不携带对应的filter

//   // 会话详单跳转条件
//   const flowFilter: IFilterCondition = [];
//   // 应用层协议详单跳转条件
//   const protocolFilter: IFilterCondition = [];

//   if (srcIp) {
//     // 判断 IP 的类型
//     const srcIsV4 = isIpv4(srcIp);
//     flowFilter.push({
//       field: srcIsV4 ? 'ipv4_initiator' : 'ipv6_initiator',
//       operator: EFilterOperatorTypes.EQ,
//       operand: srcIp,
//     });
//     protocolFilter.push({
//       field: srcIsV4 ? 'src_ipv4' : 'src_ipv6',
//       operator: EFilterOperatorTypes.EQ,
//       operand: srcIp,
//     });
//   }
//   if (destIp) {
//     const destIsV4 = isIpv4(destIp);
//     flowFilter.push({
//       field: destIsV4 ? 'ipv4_responder' : 'ipv6_responder',
//       operator: EFilterOperatorTypes.EQ,
//       operand: destIp,
//     });
//     protocolFilter.push({
//       field: destIsV4 ? 'dest_ipv4' : 'dest_ipv6',
//       operator: EFilterOperatorTypes.EQ,
//       operand: destIp,
//     });
//   }
//   if (destPort || destPort === 0) {
//     flowFilter.push({
//       field: 'port_responder',
//       operator: EFilterOperatorTypes.EQ,
//       operand: destPort,
//     });
//   }
//   if (l7ProtocolId || l7ProtocolId === 0) {
//     flowFilter.push({
//       field: 'l7_protocol_id',
//       operator: EFilterOperatorTypes.EQ,
//       operand: l7ProtocolId,
//     });
//   }
//   return {
//     flowFilter,
//     protocolFilter,
//   };
// };
// interface IAbnormalEventMessageListProps {
//   geolocationModel: ConnectState['geolocationModel'];
//   globalSelectedTime: Required<IGlobalTime>;
//   allNetworkMap: INetworkMap;
//   allL7ProtocolsList: IL7Protocol[];
//   allL7ProtocolMap: IL7ProtocolMap;
// }
// const AbnormalEventMessageList = ({
//   geolocationModel,
//   globalSelectedTime,
//   allNetworkMap,
//   allL7ProtocolsList,
//   allL7ProtocolMap,
// }: IAbnormalEventMessageListProps) => {
//   const { networkId }: IUriParams = useParams();
//   const actionRef = useRef<ActionType>();

//   const [tableHeight, setTableHeight] = useState(200);
//   const [total, setTotal] = useState(0);

//   const { allCountryMap, allProvinceMap, allCityMap } = geolocationModel;

//   useEffect(() => {
//     actionRef.current?.reload();
//   }, [globalSelectedTime, networkId]);

//   const l7ProtocolSelect = useMemo(() => {
//     return (
//       <Select
//         showSearch
//         filterOption={(input, option) =>
//           // @ts-ignore
//           option?.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
//         }
//         placeholder="请选择"
//       >
//         {allL7ProtocolsList.map((item) => (
//           <Select.Option key={item.protocolId} id={item.protocolId} value={item.protocolId}>
//             {item.nameText}
//           </Select.Option>
//         ))}
//       </Select>
//     );
//   }, [allL7ProtocolsList]);

//   const getLocationName = useCallback(
//     (countryId: string, provinceId: string, cityId: string) => {
//       if (cityId) {
//         return allCityMap[cityId]?.fullName || cityId;
//       }
//       if (provinceId) {
//         return allProvinceMap[provinceId]?.fullName || provinceId;
//       }
//       if (countryId) {
//         return allCountryMap[countryId as string]?.fullName || countryId;
//       }
//       return '';
//     },
//     [allCountryMap, allProvinceMap, allCityMap],
//   );

//   const eventTypeValueEnum = useMemo(() => {
//     return Object.keys(ABNORMAL_EVENT_TYPE_ENUM).reduce((prev, current) => {
//       return {
//         ...prev,
//         [current]: { text: ABNORMAL_EVENT_TYPE_ENUM[current]?.label },
//       };
//     }, {});
//   }, []);

//   const columns: ProColumns<IAbnormalEventMessage>[] = [
//     {
//       title: '时间',
//       key: 'startTime',
//       dataIndex: 'startTime',
//       width: 180,
//       align: 'center',
//       search: false,
//       valueType: 'dateTime',
//     },
//     {
//       title: '分类',
//       dataIndex: 'type',
//       align: 'center',
//       valueType: 'select',
//       valueEnum: eventTypeValueEnum,
//       renderFormItem: (_, { type, defaultRender, ...rest }) => {
//         return <Select showSearch optionFilterProp="label" {...rest} placeholder="请选择" />;
//       },
//     },

//     {
//       title: '内容',
//       dataIndex: 'content',
//       align: 'center',
//       // ellipsis: true,
//       render: (text) => <EllipsisDiv>{text}</EllipsisDiv>,
//     },
//     {
//       title: '描述',
//       dataIndex: 'description',
//       align: 'center',
//       // ellipsis: true,
//       search: false,
//       render: (text) => <EllipsisDiv>{text}</EllipsisDiv>,
//     },
//     {
//       title: '源IP',
//       dataIndex: 'srcIp',
//       align: 'center',
//       // ellipsis: true,
//       search: false,
//       render: (text) => <EllipsisDiv>{text}</EllipsisDiv>,
//     },
//     {
//       title: '目的IP',
//       dataIndex: 'destIp',
//       align: 'center',
//       // ellipsis: true,
//       search: false,
//       render: (text) => <EllipsisDiv>{text}</EllipsisDiv>,
//     },
//     {
//       title: 'IP地址',
//       dataIndex: 'ipAddress',
//       align: 'center',
//       hideInTable: true,
//     },
//     {
//       title: '目的端口',
//       dataIndex: 'destPort',
//       align: 'center',
//       search: false,
//     },
//     {
//       title: '应用层协议',
//       dataIndex: 'l7ProtocolId',
//       align: 'center',
//       // ellipsis: true,
//       renderFormItem: () => {
//         return l7ProtocolSelect;
//       },
//       renderText: (value) => allL7ProtocolMap[value]?.nameText || value,
//       render: (text) => <EllipsisDiv>{text}</EllipsisDiv>,
//     },
//     {
//       title: '来源地区',
//       dataIndex: 'locationInitiator',
//       align: 'center',
//       search: false,
//       // ellipsis: true,
//       renderText: (id, record) => {
//         const { countryIdInitiator, provinceIdInitiator, cityIdInitiator } = record;
//         return getLocationName(
//           String(countryIdInitiator || ''),
//           String(provinceIdInitiator || ''),
//           String(cityIdInitiator || ''),
//         );
//       },
//       render: (text) => <EllipsisDiv>{text}</EllipsisDiv>,
//     },
//     {
//       title: '目的地区',
//       dataIndex: 'locationResponder',
//       align: 'center',
//       search: false,
//       // ellipsis: true,
//       renderText: (id, record) => {
//         const { countryIdResponder, provinceIdResponder, cityIdResponder } = record;
//         return getLocationName(
//           String(countryIdResponder || ''),
//           String(provinceIdResponder || ''),
//           String(cityIdResponder || ''),
//         );
//       },
//       render: (text) => <EllipsisDiv>{text}</EllipsisDiv>,
//     },
//     {
//       title: '操作',
//       key: 'option',
//       align: 'center',
//       width: 200,
//       valueType: 'option',
//       render: (_, record) => {
//         const { srcIp, destIp, destPort, l7ProtocolId } = record;

//         // 是否可跳转到元数据的标志
//         let metadataLinkEnable = false;
//         // 先判断是否是协议字典中有的应用层协议
//         let l7ProtocolName = allL7ProtocolMap[l7ProtocolId]?.name;
//         // 再判断下协议是否是当前支持的可解析的元数据
//         if (l7ProtocolName && EMetadataProtocol[l7ProtocolName.toLocaleUpperCase()]) {
//           metadataLinkEnable = true;
//           if (l7ProtocolName === 'icmp') {
//             if (isIpv4(srcIp) && isIpv4(destIp)) {
//               l7ProtocolName = 'icmpv4';
//             } else {
//               l7ProtocolName = 'icmpv6';
//             }
//           }
//         }

//         return (
//           <Space>
//             <Button
//               type="link"
//               size="small"
//               onClick={() =>
//                 jumpNewPage(
//                   getLinkUrl(
//                     `/analysis/network/${networkId}/flow-record?filter=${encodeURIComponent(
//                       JSON.stringify(jumpFilterInUrl(srcIp, destIp, destPort, l7ProtocolId).flowFilter),
//                     )}&from=${new Date(
//                       globalSelectedTime!.originStartTime,
//                     ).valueOf()}&to=${new Date(
//                       globalSelectedTime!.originEndTime,
//                     ).valueOf()}&timeType=${ETimeType.CUSTOM}`,
//                   ),
//                 )
//               }
//             >
//               会话详单
//             </Button>
//             <Button
//               type="link"
//               size="small"
//               disabled={!metadataLinkEnable}
//               onClick={() =>
//                 jumpNewPage(
//                   getLinkUrl(
//                     `/analysis/network/${networkId}/metadata?jumpTabs=${l7ProtocolName.toLocaleLowerCase()}&filter=${encodeURIComponent(
//                       JSON.stringify(jumpFilterInUrl(srcIp, destIp, destPort, l7ProtocolId).protocolFilter),
//                     )}&from=${new Date(
//                       globalSelectedTime!.originStartTime,
//                     ).valueOf()}&to=${new Date(
//                       globalSelectedTime!.originEndTime,
//                     ).valueOf()}&timeType=${ETimeType.CUSTOM}`,
//                   ),
//                 )
//               }
//             >
//               应用层协议详单
//             </Button>
//           </Space>
//         );
//       },
//     },
//   ];

//   if (!allNetworkMap[networkId!]?.id) {
//     return <Result title="仅在物理网络下生效" />;
//   }

//   return (
//     <AutoHeightContainer onHeightChange={(h) => setTableHeight(h - 94 - 40)}>
//       <ProTable<IAbnormalEventMessage>
//         rowKey="id"
//         bordered
//         size="small"
//         columns={columns}
//         request={async (params = {}) => {
//           const { current, pageSize, startTime, endTime, ...rest } = params;
//           const newParams = { pageSize, page: current! - 1, networkId, ...rest } as any;
//           // 特殊处理时间
//           let newGlobalSelectedTime = globalSelectedTime;
//           if (globalSelectedTime.relative) {
//             newGlobalSelectedTime = getGlobalTime(globalSelectedTime);
//           }
//           newParams.startTime = newGlobalSelectedTime.originStartTime;
//           newParams.endTime = newGlobalSelectedTime.originEndTime;

//           const { success, result } = (await ajax(
//             `${API_VERSION_PRODUCT_V1}/analysis/abnormal-events?${stringify(newParams)}`,
//           )) as IAjaxResponseFactory<IPageFactory<IAbnormalEventMessage>>;

//           setTotal(success ? result.totalElements : 0);

//           if (!success) {
//             return {
//               data: [],
//               success,
//             };
//           }

//           return {
//             data: result.content,
//             success,
//             page: result.number,
//             total: result.totalElements,
//           } as IProTableData<IAbnormalEventMessage[]>;
//         }}
//         search={{
//           ...proTableSerchConfig,
//           span: 6,
//         }}
//         actionRef={actionRef}
//         dateFormatter="string"
//         toolBarRender={false}
//         pagination={getTablePaginationDefaultSettings()}
//         debounceTime={0}
//         // 没有数据时，不显示分页，所以要把分页的高度也给占用了
//         scroll={{ y: total ? tableHeight - 40 : tableHeight }}
//         locale={{
//           emptyText: <TableEmpty componentName="ProTable" height={tableHeight} />,
//         }}
//       />
//     </AutoHeightContainer>
//   );
// };

// export default connect(
//   ({
//     geolocationModel,
//     appModel: { globalSelectedTime },
//     networkModel: { allNetworkMap },
//     metadataModel: { allL7ProtocolsList, allL7ProtocolMap },
//   }: ConnectState) => ({
//     geolocationModel,
//     globalSelectedTime,
//     allNetworkMap,
//     allL7ProtocolsList,
//     allL7ProtocolMap,
//   }),
// )(AbnormalEventMessageList);
