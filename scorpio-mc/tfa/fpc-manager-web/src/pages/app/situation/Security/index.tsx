import { getTablePaginationDefaultSettings, proTableSerchConfig } from '@/common/app';
import type { IProTableData } from '@/common/typings';
import AutoHeightContainer from '@/components/AutoHeightContainer';
import ChartMessage from '@/components/Message';
import WorldMap from '@/components/WorldMap';
import type { GeolocationModelState } from '@/models/app/geolocation';
import type { ConnectState } from '@/models/connect';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { useLatest, useMemoizedFn } from 'ahooks';
import { Card, Col, Row } from 'antd';
import { useEffect, useRef, useState } from 'react';
import type { Dispatch, ISituationModelState } from 'umi';
import { connect, useSelector } from 'umi';
import { v1 as uuidv1 } from 'uuid';
import { queryAlertMessageList, queryAlertMessageStatistics } from '../../security/service';
import type { IRuleClasstype, ISuricataAlertMessage } from '../../security/typings';
import FullScreenCard, { HightContext } from '../components/FullScreenCard';
import type { IPieChartData } from '../components/PieChart';
import PieChart from '../components/PieChart';
import SecureBarChart from '../components/SecureBarChart';
import TimeAutoRefreshSelectBox from '../components/TimeAutoRefreshSelectBox';
import { getGeolocationPosition, getLocationInfo } from './../utils/geolocationTools';
import styles from './index.less';

// const { RangePicker } = DatePicker;

// enum ETimeType {
//   'Today' = 1,
//   'Yesterday',
//   'Last_7_day',
//   'Custom',
// }

function getBarTitle(barData: IPieChartData[]) {
  return barData.map((data) => {
    return data.name;
  });
}

function getBarData(barData: IPieChartData[]) {
  return barData.map((data) => {
    return data.value;
  });
}
interface ISecurityProps {
  dispatch: Dispatch;
  geolocationModel: GeolocationModelState;
  situationModel: ISituationModelState;
}
const Security = ({
  geolocationModel,
  situationModel: { startTime, endTime },
  dispatch,
}: ISecurityProps) => {
  const tableActionRef = useRef<ActionType>();

  // 攻击来源地区统计图数据
  const [srcBarData, setSrcBarData] = useState<IPieChartData[]>([]);
  // 攻击目的地区统计图数据
  const [dstBarData, setDstBarData] = useState<IPieChartData[]>([]);
  // 攻击类型分布饼图数据
  const [typePieData, setTypePieData] = useState<IPieChartData[]>([]);
  // 攻击来源地区统计图数据
  const [mapGunData, setMapGunData] = useState<any[]>([]);

  const [tableHeight, setTableHeight] = useState(320);

  const classtypeDict = useSelector<ConnectState, Record<string, IRuleClasstype>>(
    (state: ConnectState) => state.suricataModel.classtypeDict,
  );

  const classTypeLoading = useSelector<ConnectState, boolean>(
    (state) => state.loading.effects['suricataModel/querySuricataRuleClasstype'] || false,
  );

  const classTypeLoadingRef = useLatest(classTypeLoading);

  useEffect(() => {
    dispatch({ type: 'suricataModel/querySuricataRuleClasstype' });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // 获取统计图表数据
  const queryStatData = useMemoizedFn(() => {
    // changeAllLoadState(true);
    // 攻击分类
    queryAlertMessageStatistics({
      dsl: `type = ${'classification_proportion'} | gentimes timestamp start="${startTime}" end="${endTime}"`,
      count: 10,
    }).then((res) => {
      const { success, result } = res;
      if (success) {
        setTypePieData(
          result.map((item) => {
            return {
              name: classtypeDict[item.key]?.name || item.key,
              value: item.count,
            };
          }),
        );
      }
    });
    // 攻击来源地
    queryAlertMessageStatistics({
      dsl: `type = ${'top_attack_origin_area'} | gentimes timestamp start="${startTime}" end="${endTime}"`,
      count: 10,
    }).then((res) => {
      const { success, result } = res;
      if (success) {
        setSrcBarData(
          result.map((item) => {
            const locationIds = item.key.split('_');
            const countryId = locationIds[0];

            return {
              name:
                getLocationInfo(geolocationModel, {
                  countryId,
                  ...(locationIds.length === 2 ? { provinceId: locationIds[1] } : {}),
                })?.fullName || '',
              value: item.count,
            };
          }),
        );
      }
    });
    // 攻击目的地
    queryAlertMessageStatistics({
      dsl: `type = ${'top_attack_target_area'} | gentimes timestamp start="${startTime}" end="${endTime}"`,
      count: 10,
    }).then((res) => {
      const { success, result } = res;
      if (success) {
        setDstBarData(
          result.map((item) => {
            const locationIds = item.key.split('_');
            const countryId = locationIds[0];
            return {
              name:
                getLocationInfo(geolocationModel, {
                  countryId,
                  ...(locationIds.length === 2 ? { provinceId: locationIds[1] } : {}),
                })?.fullName || '',
              value: item.count,
            };
          }),
        );
      }
    });
  });

  useEffect(() => {
    // 时间变化
    if (!startTime || !endTime) {
      return;
    }

    if (classTypeLoadingRef.current) return;

    // 1. 异常事件数据
    tableActionRef?.current?.reload(true);

    // 2. 拉取3个统计
    queryStatData();
  }, [classTypeLoadingRef, endTime, queryStatData, startTime]);

  const tableColumns: ProColumns<ISuricataAlertMessage>[] = [
    {
      title: '时间',
      key: 'timestamp',
      dataIndex: 'timestamp',
      width: 180,
      align: 'center',
      search: false,
      valueType: 'dateTime',
    },
    {
      title: '分类',
      dataIndex: 'classtypeId',
      align: 'center',
      valueType: 'select',
      search: false,
      valueEnum: Object.keys(classtypeDict).reduce((prev, current) => {
        return {
          ...prev,
          [current]: { text: classtypeDict[current]?.name },
        };
      }, {}),
      render: (_, record) => {
        const { classtypeId } = record;
        return classtypeDict[classtypeId]?.name || classtypeId;
      },
    },
    {
      title: '详情',
      dataIndex: 'msg',
      align: 'center',
      ellipsis: true,
      search: false,
    },
    {
      title: '攻击源',
      dataIndex: 'srcIp',
      align: 'center',
      search: false,
      render(dom, record) {
        const { srcIp, destIp, target } = record;
        return destIp === target ? srcIp : destIp;
      },
    },
    {
      title: '攻击源所在区域',
      dataIndex: 'locationInitiator',
      align: 'center',
      search: false,
      ellipsis: true,
      render: (id, record) => {
        const {
          srcIp,
          target,
          countryIdInitiator,
          provinceIdInitiator,
          cityIdInitiator,
          cityIdResponder,
          provinceIdResponder,
          countryIdResponder,
        } = record;

        if (srcIp !== target) {
          return getLocationInfo(geolocationModel, {
            countryId: countryIdInitiator,
            provinceId: provinceIdInitiator,
            cityId: cityIdInitiator,
          })?.fullName;
        }
        return getLocationInfo(geolocationModel, {
          countryId: countryIdResponder,
          provinceId: provinceIdResponder,
          cityId: cityIdResponder,
        })?.fullName;
      },
    },
    {
      title: '攻击目标',
      dataIndex: 'target',
      align: 'center',
      search: false,
    },
    {
      title: '攻击目标所在区域',
      dataIndex: 'locationResponder',
      align: 'center',
      search: false,
      ellipsis: true,
      render: (id, record) => {
        const {
          srcIp,
          target,
          countryIdInitiator,
          provinceIdInitiator,
          cityIdInitiator,
          cityIdResponder,
          provinceIdResponder,
          countryIdResponder,
        } = record;

        if (srcIp === target) {
          return getLocationInfo(geolocationModel, {
            countryId: countryIdInitiator,
            provinceId: provinceIdInitiator,
            cityId: cityIdInitiator,
          })?.fullName;
        }
        return (
          getLocationInfo(geolocationModel, {
            countryId: countryIdResponder,
            provinceId: provinceIdResponder,
            cityId: cityIdResponder,
          })?.fullName || '未知'
        );
      },
    },
  ];

  return (
    <FullScreenCard title={'安全态势'} extra={<TimeAutoRefreshSelectBox />}>
      <HightContext.Consumer>
        {(isFullscreen) => {
          return (
            <div style={{ height: isFullscreen ? 'calc(100vh - 80px)' : 'calc(100vh - 160px)' }}>
              <Row gutter={10} style={{ height: '100%' }}>
                <Col span={7} style={{ height: '100%', zIndex: 10, opacity: 0.6 }}>
                  <Card
                    size="small"
                    style={{ height: '30%' }}
                    title="网络攻击分布（按攻击类型）"
                    className={styles.card_container}
                    bodyStyle={{
                      // height: isFullscreen ? 'calc(100% - 40px)' : 300,
                      height: 'calc(100% - 40px)',
                      display: 'flex',
                      flexDirection: 'column',
                      justifyContent: 'center',
                    }}
                    // loading={typePieDataLoad}
                  >
                    {typePieData.length === 0 ? <ChartMessage /> : <PieChart datas={typePieData} />}
                  </Card>
                  <Card
                    size="small"
                    title="网络攻击分布TOP10（按攻击来源所在区域）"
                    style={{ height: '35%' }}
                    className={styles.card_container}
                    bodyStyle={{
                      height: 'calc(100% - 40px)',
                      padding: 10,
                    }}
                    // loading={srcBarLoad}
                  >
                    {srcBarData.length === 0 ? (
                      <ChartMessage />
                    ) : (
                      <SecureBarChart
                        datas={getBarData(srcBarData)}
                        xAxisTitles={getBarTitle(srcBarData)}
                      />
                    )}
                  </Card>
                  <Card
                    title="网络攻击分布TOP10（按攻击目标所在区域）"
                    className={styles.card_container}
                    style={{ height: '35%' }}
                    bodyStyle={{
                      height: 'calc(100% - 45px)',
                      padding: 10,
                    }}
                    size="small"
                  >
                    {dstBarData.length === 0 ? (
                      <ChartMessage />
                    ) : (
                      <SecureBarChart
                        datas={getBarData(dstBarData)}
                        xAxisTitles={getBarTitle(dstBarData)}
                      />
                    )}
                  </Card>
                </Col>
                <Col span={17} style={{ zIndex: 10, opacity: 0.6 }}>
                  <div style={{ height: '60%' }} />
                  <Card
                    size="small"
                    title="安全告警列表"
                    className={styles.card_container}
                    bodyStyle={{ height: 'calc(100% - 40px)', padding: '5px' }}
                  >
                    <AutoHeightContainer onHeightChange={(h) => setTableHeight(h - 40 - 40)}>
                      <ProTable<ISuricataAlertMessage>
                        bordered
                        size="small"
                        columns={tableColumns}
                        rowKey={() => uuidv1()}
                        request={async (params = {}) => {
                          const { current, pageSize, ...rest } = params;
                          const newParams = { pageSize, page: current! - 1, ...rest } as any;
                          // const newParams = { pageSize: 100, page: 0, ...rest } as any;
                          // 特殊处理时间
                          newParams.startTime = startTime;
                          newParams.endTime = endTime;
                          const { success, result } = await queryAlertMessageList({
                            ...newParams,
                            dsl: `| gentimes timestamp start="${startTime}" end="${endTime}"`,
                          });
                          if (!success) {
                            return {
                              data: [],
                              success,
                            };
                          }

                          const abnPos = getGeolocationPosition(result.content, geolocationModel);
                          setMapGunData(abnPos || []);

                          return {
                            data: result.content.map((item: any) => ({ ...item, key: item.id })),
                            success,
                            page: result.number,
                            total: result.totalElements,
                          } as IProTableData<ISuricataAlertMessage[]>;
                        }}
                        search={{
                          ...proTableSerchConfig,
                          span: 6,
                        }}
                        form={{
                          ignoreRules: false,
                        }}
                        actionRef={tableActionRef}
                        dateFormatter="string"
                        scroll={{ y: tableHeight }}
                        toolBarRender={false}
                        pagination={getTablePaginationDefaultSettings()}
                      />
                    </AutoHeightContainer>
                  </Card>
                </Col>
              </Row>
              <Card
                style={{
                  height: isFullscreen ? 'calc(100vh - 40px)' : 'calc(100vh - 160px)',
                  position: 'absolute',
                  top: 53,
                  left: -1,
                }}
                key={uuidv1()}
                bodyStyle={{ padding: 0, height: 'calc(100% - 40px)' }}
                className={styles.card_container}
              >
                <WorldMap lineEffectData={mapGunData} />
              </Card>
            </div>
          );
        }}
      </HightContext.Consumer>
    </FullScreenCard>
  );
};

export default connect(({ situationModel, geolocationModel }: ConnectState) => {
  return { situationModel, geolocationModel };
})(Security);
