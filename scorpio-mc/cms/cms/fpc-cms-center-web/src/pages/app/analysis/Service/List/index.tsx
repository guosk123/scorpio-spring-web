import { getTablePaginationDefaultSettings, proTableSerchConfig } from '@/common/app';
import type { IProTableData } from '@/common/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import type { ILogicalSubnetMap } from '@/pages/app/Configuration/LogicalSubnet/typings';
import type {
  INetworkGroupMap,
  INetworkSensorMap,
} from '@/pages/app/Configuration/Network/typings';
import type {
  IFollowService,
  IFollowServiceParams,
  IServiceMap,
} from '@/pages/app/Configuration/Service/typings';
import { EServiceFollowState } from '@/pages/app/Configuration/Service/typings';
import { getLinkUrl, onlyNumber } from '@/utils/utils';
import { StarFilled, TeamOutlined } from '@ant-design/icons';
import ProList from '@ant-design/pro-list';
import type { ActionType } from '@ant-design/pro-table';
import { Empty, Popconfirm, Progress, Spin } from 'antd';
import { connect } from 'dva';
import React, { Suspense, useCallback, useEffect, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import { Link } from 'umi';
import { queryServiceDashboardSettings, queryServiceStatList } from '../../service';
import type { IServiceStatData } from '../../typings';
import FieldCollection from './components/FieldCollection';
import Settings from './components/Settings';
import styles from './index.less';
import moment from 'moment';
import { PercentageRateType, Percentage_Enum, ChartTrendType } from '../List/typings';
const ServiceFlowHistogram = React.lazy(() => import('./components/FlowHistogram'));
const AlarmRateDistribution = React.lazy(() => import('./components/AlarmRateDistribution'));
/**
 * 计算 bytes/每秒
 */
export function getBytepsAvg(totalBytes: number = 0, startTime: string, endTime: string) {
  const timeDiff = moment(endTime).diff(startTime) / 1000;
  if (timeDiff !== 0) {
    return totalBytes / timeDiff;
  }
  return 0;
}

/**
 * 计算TCP连接成功率
 */
export function getTcpEstablishedSuccessRate(
  /** 成功次数 */
  tcpEstablishedSuccessCount: number,
  /** 失败次数 */
  tcpEstablishedFailedCount: number,
  /** 保留小数长度 */
  decimal: number = 2,
): number {
  if (tcpEstablishedSuccessCount + tcpEstablishedFailedCount !== 0) {
    return (
      (tcpEstablishedSuccessCount / (tcpEstablishedSuccessCount + tcpEstablishedFailedCount)) *
      100
    ).toFixed(decimal) as any;
  }
  return 0;
}

interface IServiceProps {
  dispatch: Dispatch;
  globalSelectedTime: Required<IGlobalTime>;
  followServices: IFollowService[];
  allServiceMap: IServiceMap;
  allNetworkSensorMap: INetworkSensorMap;
  allLogicalSubnetMap: ILogicalSubnetMap;
  allNetworkGroupMap: INetworkGroupMap;
  queryServiceFollowsLoading?: boolean;
  updateServiceFollowLoading?: boolean;
}
const Service: React.FC<IServiceProps> = ({
  dispatch,
  globalSelectedTime,

  followServices,
  allServiceMap,
  allNetworkSensorMap,
  allLogicalSubnetMap,
  allNetworkGroupMap,
  queryServiceFollowsLoading = true,
  updateServiceFollowLoading = false,
}) => {
  const actionRef = useRef<ActionType>();
  // 当前展示的指标字段
  // const [displayFleldIds, setDisplayFleldIds] = useState<string[]>([]);
  const [displayFieldIdThresholdMap, setDisplayFieldIdThresholdMap] = useState<
    Record<string, any[]>
  >({});
  const [displayFieldIds, setDisplayFieldIds] = useState<string[]>([]);
  const [displayPercentageId, setDislayPercentageId] = useState<string>('');
  const [displayTrendConfigId, setDisplayTrendConfigId] = useState<string>('');
  const [isConfigLoading, setIsConfigLoading] = useState<boolean>(false);

  useEffect(() => {
    dispatch({
      type: 'serviceModel/queryAllServices',
    });
    dispatch({
      type: 'networkModel/queryAllNetworkSensor',
    });
    dispatch({
      type: 'networkModel/queryAllNetworkGroups',
    });
    dispatch({
      type: 'logicSubnetModel/queryAllLogicalSubnets',
    });
  }, [dispatch]);

  const queryServiceFollows = useCallback(() => {
    dispatch({
      type: 'serviceModel/queryServiceFollows',
      payload: {},
    });
  }, [dispatch]);

  const updateServiceFollow = ({
    serviceId,
    networkId,
    networkGroupId,
    state,
  }: IFollowServiceParams) => {
    dispatch({
      type: 'serviceModel/updateServiceFollow',
      payload: {
        serviceId,
        networkId,
        networkGroupId,
        state,
      },
    }).then((success: boolean) => {
      if (success) {
        queryServiceFollows();
      }
    });
  };

  useEffect(() => {
    queryServiceFollows();
  }, [queryServiceFollows]);

  useEffect(() => {
    actionRef.current?.reload();
  }, [globalSelectedTime]);

  const queryDashboardSettings = useCallback(() => {
    setIsConfigLoading(true);
    queryServiceDashboardSettings().then((res) => {
      const { success, result } = res;
      if (success) {
        setIsConfigLoading(false);
        const { parameters, percentParameter, timeWindowParameter } = result;
        const fileldIdThresholdMap: Record<string, any[]> = JSON.parse(parameters);
        if (Object.prototype.toString.call(fileldIdThresholdMap) === '[object Object]') {
          setDisplayFieldIdThresholdMap(fileldIdThresholdMap);
          const fieldIds: string[] = Object.keys(fileldIdThresholdMap);
          setDisplayFieldIds(fieldIds);
        }
        setDislayPercentageId(percentParameter);
        setDisplayTrendConfigId(timeWindowParameter);
      }
    });
  }, []);

  useEffect(() => {
    queryDashboardSettings();
  }, [queryDashboardSettings]);

  const computedFollowFlag = useCallback(
    (serviceId: string, networkId: string) => {
      return !!followServices.find(
        (el) =>
          el.serviceId === serviceId &&
          (el.networkId === networkId || el.networkGroupId === networkId),
      );
    },
    [followServices],
  );

  return (
    <>
      {/* <TimeRangeSlider /> */}
      <ProList<IServiceStatData>
        className={styles.list}
        request={async (params = {}) => {
          const { current, pageSize, ...rest } = params;
          const newParams = { pageSize, page: current! - 1, ...rest } as any;
          // 特殊处理时间
          newParams.startTime = globalSelectedTime.startTime;
          newParams.endTime = globalSelectedTime.endTime;
          newParams.interval = globalSelectedTime.interval;

          const { success, result } = await queryServiceStatList(newParams);
          if (!success) {
            return {
              data: [],
              success,
            };
          }
          const { content } = result;
          return {
            data: content
              ? content.map((item) => {
                  return {
                    ...item,
                    bytepsAvg: getBytepsAvg(
                      item.totalBytes,
                      globalSelectedTime.startTime,
                      globalSelectedTime.endTime,
                    ),
                    tcpEstablishedSuccessRate: getTcpEstablishedSuccessRate(
                      item.tcpEstablishedSuccessCounts,
                      item.tcpEstablishedFailCounts,
                    ),
                  };
                })
              : [],
            success,
            page: result.number,
            total: result.totalElements,
          } as IProTableData<IServiceStatData[]>;
        }}
        pagination={getTablePaginationDefaultSettings()}
        split
        grid={{ gutter: 0, column: 1 }}
        search={{
          ...proTableSerchConfig,
          span: 6,
          optionRender: (searchConfig, formProps, dom) => [
            ...dom.reverse(),
            // <FieldDisplaySetting key="FieldDisplaySetting" onChange={handleFieldDisplayChange} />,
            <Settings
              key="Settins"
              // onChange={handleSettingsChange}
              config={{
                fieldIds: displayFieldIds,
                fieldIdThresholdMap: displayFieldIdThresholdMap,
                percentageId: displayPercentageId,
                trendId: displayTrendConfigId,
              }}
              isLoading={isConfigLoading}
              refreshConfig={() => queryDashboardSettings()}
            />,
          ],
        }}
        size="small"
        rowKey={(record) => `${record.serviceId}_${record.networkId}`}
        showActions="always"
        actionRef={actionRef}
        metas={{
          avatar: {
            search: false,
            render: () => {
              return <TeamOutlined style={{ color: '#d3d3d3', fontSize: 20 }} />;
            },
          },
          title: {
            dataIndex: 'name',
            title: '业务名称',
            render: (text, row) => {
              const { networkId, networkGroupId, serviceId } = row;
              return (
                <Link
                  to={getLinkUrl(
                    `/performance/service/${serviceId}/${networkId || networkGroupId || ''}`,
                  )}
                >
                  <b style={{ fontSize: 18 }}>
                    {allServiceMap[serviceId]?.name || `[业务已删除: ${serviceId}]`}-
                    {(() => {
                      const allNetworks = {
                        ...allNetworkGroupMap,
                        ...allNetworkSensorMap,
                        ...allLogicalSubnetMap,
                      };
                      if (networkId?.includes('^')) {
                        const [netId, logicId] = networkId.split('^');
                        if (!allNetworks[netId || ''] || !allNetworks[logicId || '']) {
                          return `[网络已删除: ${allNetworks[netId]?.name || netId} - ${
                            allNetworks[logicId]?.name || logicId
                          }]`;
                        }
                        return `${allNetworks[netId]?.name} - ${allNetworks[logicId]?.name}`;
                      }
                      return (
                        allNetworks[networkId || networkGroupId || '']?.name ||
                        `[网络已删除: ${networkId}]`
                      );
                    })()}
                    {/* {{ ...allNetworkGroupMap, ...allNetworkSensorMap, ...allLogicalSubnetMap }[
                    networkId || networkGroupId || ''
                  ]?.name || `[网络已删除: ${networkId}]`} */}
                  </b>
                </Link>
              );
            },
          },
          actions: {
            render: (text, row) => {
              const { serviceId, networkId, networkGroupId } = row;
              const isFollowed = computedFollowFlag(serviceId, networkId || networkGroupId || '');
              return (
                <Spin
                  size="small"
                  spinning={queryServiceFollowsLoading || updateServiceFollowLoading}
                >
                  <Popconfirm
                    title={`确定${isFollowed ? '取消关注' : '关注'}此业务吗？`}
                    onConfirm={() => {
                      updateServiceFollow({
                        serviceId,
                        ...(() => {
                          if (networkId) {
                            return {
                              networkId: networkId,
                            };
                          }
                          if (networkGroupId) {
                            return {
                              networkGroupId: networkGroupId,
                            };
                          }
                          return {};
                        })(),
                        state: isFollowed
                          ? EServiceFollowState.CANCEL_FOLLOW
                          : EServiceFollowState.FOLLOW,
                      });
                    }}
                  >
                    <StarFilled
                      style={{
                        color: isFollowed ? '#fadb14' : 'gray',
                        opacity: isFollowed ? 1 : 0.3,
                        fontSize: 20,
                      }}
                    />
                  </Popconfirm>
                </Spin>
              );
            },
            search: false,
          },
          content: {
            search: false,
            render: (text, row) => {
              const {
                networkId,
                networkGroupId,
                serviceId,
                tcpEstablishedSuccessCounts = 0,
                tcpEstablishedFailCounts = 0,
                tcpClientRetransmissionRate = 0,
                tcpServerRetransmissionRate = 0,
              } = row;
              const tcpEstablishedSuccessRate = getTcpEstablishedSuccessRate(
                tcpEstablishedSuccessCounts,
                tcpEstablishedFailCounts,
                0,
              );
              const clientReTransmitRate = onlyNumber(tcpClientRetransmissionRate).toFixed(3);
              const severReTransmitRate = onlyNumber(tcpServerRetransmissionRate).toFixed(3);

              let disPlayRate = tcpEstablishedSuccessRate;
              let displayTilte = Percentage_Enum[PercentageRateType.SupportedLinkSuccessRate];
              if (PercentageRateType.SupportedLinkSuccessRate === displayPercentageId) {
                disPlayRate = tcpEstablishedSuccessRate;
                displayTilte = Percentage_Enum[PercentageRateType.SupportedLinkSuccessRate];
              }
              if (PercentageRateType.ClientReTransmitRate === displayPercentageId) {
                disPlayRate = parseFloat(clientReTransmitRate);
                displayTilte = Percentage_Enum[PercentageRateType.ClientReTransmitRate];
              }
              if (PercentageRateType.SeverReTransmitRate === displayPercentageId) {
                disPlayRate = parseFloat(severReTransmitRate);
                displayTilte = Percentage_Enum[PercentageRateType.SeverReTransmitRate];
              }

              return (
                <div className={styles.stat}>
                  <div className={styles.stat__top}>
                    <div className={styles.stat__top__left}>
                      <p className={styles.stat__top__left__title}>{displayTilte}</p>
                      <Progress
                        type="circle"
                        width={95}
                        strokeWidth={12}
                        percent={disPlayRate}
                        format={(percent) => `${percent}%`}
                      />
                    </div>
                    <div className={styles.stat__top__right}>
                      {displayFieldIds.length ? (
                        <FieldCollection
                          data={row}
                          fieldIds={displayFieldIds}
                          fieldIdThresholdMap={displayFieldIdThresholdMap}
                        />
                      ) : (
                        <Empty
                          image={Empty.PRESENTED_IMAGE_SIMPLE}
                          imageStyle={{ height: 25 }}
                          description="暂无配置项"
                        />
                      )}
                    </div>
                  </div>
                  <div className={styles.stat__footer}>
                    <Suspense fallback={null}>
                      {/* <ServiceFlowHistogram
                        networkId={networkId}
                        networkGroupId={networkGroupId}
                        serviceId={serviceId}
                        selectedTime={globalSelectedTime}
                      /> */}
                      {displayTrendConfigId === ChartTrendType.FlowRateTrend ? (
                        <ServiceFlowHistogram
                          networkId={networkId}
                          networkGroupId={networkGroupId}
                          serviceId={serviceId}
                          selectedTime={globalSelectedTime}
                        />
                      ) : (
                        <AlarmRateDistribution
                          networkId={networkId}
                          networkGroupId={networkGroupId}
                          serviceId={serviceId}
                          selectedTime={globalSelectedTime}
                        />
                      )}
                    </Suspense>
                  </div>
                </div>
              );
            },
          },
          isFollow: {
            // 自己扩展的字段，主要用于筛选，不在列表中显示
            title: '关注状态',
            valueType: 'select',
            valueEnum: {
              '0': {
                text: '全部',
              },
              '1': {
                text: '只看关注',
              },
            },
          },
        }}
      />
    </>
  );
};

export default connect(
  ({
    loading: { effects },
    appModel: { globalSelectedTime },
    serviceModel: { followServices, allServiceMap },
    networkModel: { allNetworkSensorMap, allNetworkGroupMap },
    logicSubnetModel: { allLogicalSubnetMap },
  }: ConnectState) => ({
    queryServiceFollowsLoading: effects['serviceModel/queryServiceFollows'],
    updateServiceFollowLoading: effects['serviceModel/updateServiceFollow'],
    globalSelectedTime,
    followServices,
    allServiceMap,
    allNetworkSensorMap,
    allLogicalSubnetMap,
    allNetworkGroupMap,
  }),
)(Service);
