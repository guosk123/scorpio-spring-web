import AutoHeightContainer from '@/components/AutoHeightContainer';
import FieldFilter, { formatFilter } from '@/components/FieldFilter';
import type {
  EFieldType,
  IEnumValue,
  IFilter,
  IFilterCondition,
} from '@/components/FieldFilter/typings';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { EFieldOperandType } from '@/components/FieldFilter/typings';
import { deduplicateCondition } from '@/components/FieldFilter/utils';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { getGlobalTime } from '@/components/GlobalTimeSelector';
import type { IUriParams } from '@/pages/app/analysis/typings';
import { bytesToSize, parseArrayJson, abortAjax, validateBpfStr } from '@/utils/utils';
import {
  DownloadOutlined,
  FundOutlined,
  QuestionCircleOutlined,
  ReloadOutlined,
  StopOutlined,
} from '@ant-design/icons';
import { Alert, Button, Card, Col, Modal, Row, Space, Tabs, Tooltip } from 'antd';
import moment from 'moment';
import numeral from 'numeral';
import { useCallback, useContext, useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, useLocation, useParams } from 'umi';
import { v1 as uuidv1 } from 'uuid';
import type { EModelAlias } from '../../../analysis/components/fieldsManager';
import {
  EFieldEnumValueSource,
  getEnumValueFromModelNext,
} from '../../../analysis/components/fieldsManager';
import { AnalysisContext } from '../../../Network/Analysis';
import type { URLFilter } from '../../../Network/Analysis/constant';
import { jumpToAnalysisTabNew } from '../../../Network/Analysis/constant';
import { ENetworkTabs } from '../../../Network/typing';
import FilterBpf from './../components/FilterBpf';
import PacketList from './../components/PacketList';
import PacketRefine from './../components/PacketRefine';
import Progress from './../components/Progress';
import styles from '../index.less';
import type { IPacketModelState } from './../model';
import { initPacketRefineData } from './../model';
import { queryPacketList, queryPacketRefine, stopPacketList, stopPacketRefine } from './../service';
import type { IPacketConnectState, IQueryParams } from './../typings';
import { EIpProtocol } from './../typings';
import { EPacketListStatus } from './../typings';
import { EConditionType, EPacketRefineStatus } from './../typings';
import type { IPacketTaskDetail } from '@/pages/app/GlobalSearch/PacketRetrieval/components/TransmitTaskForm';
import TransmitTaskForm from '@/pages/app/GlobalSearch/PacketRetrieval/components/TransmitTaskForm';
import { ServiceAnalysisContext } from '@/pages/app/analysis/Service/index';
import { jumpToSericeAnalysisTab } from '@/pages/app/analysis/Service/constant';
import { EServiceTabs } from '@/pages/app/analysis/Service/typing';
import { clearShareInfo } from '@/pages/app/Network/components/EditTabs';
import useClearURL from '@/hooks/useClearURL';
import useFilterField from '../hooks/useFilterField';

const { TabPane } = Tabs;

/** 数据包进行查询的字段 */
// export const packetSearchableFields = fieldList
//   .filter((field) => field.searchable)
//   .map((field) => field.dataIndex);

/** 数据包过滤字段 */
// export const filterField = fieldList
//   .filter((field) => field.searchable)
//   .map((field) => {
//     const { dataIndex, name, filterOperandType, filterFieldType, enumSource, enumValue } = field;
//     const isEnum = filterOperandType === EFieldOperandType.ENUM;
//     const enumValueList: IEnumValue[] = [];
//     if (isEnum) {
//       if (enumSource === EFieldEnumValueSource.LOCAL) {
//         enumValueList.push(...(enumValue as IEnumValue[]));
//       } else {
//         const modelData = getEnumValueFromModelNext(enumValue as EModelAlias);
//         if (modelData !== null) {
//           enumValueList.push(...modelData.list);
//         }
//       }
//     }

//     return {
//       title: name,
//       dataIndex,
//       operandType: filterOperandType as EFieldOperandType,
//       type: filterFieldType as EFieldType,
//       ...(isEnum
//         ? {
//             enumValue: enumValueList,
//           }
//         : {}),
//       //添加一个bpf过滤条件
//       ...(dataIndex === 'bpf'
//         ? { validator: validateBpfStr as (value: string) => Promise<any> }
//         : {}),
//     };
//   });

/** 场景分析任务跳转过来的的参数 */
export interface IAnalysisTaskParams {
  networkId?: string;
  // 目前来看任务 ID 并不需要
  analysisResultId?: string;
  analysisStartTime?: string;
  analysisEndTime?: string;
}
interface IPacketProps {
  dispatch: Dispatch;
  packetModel: IPacketModelState;
  globalSelectedTime: Required<IGlobalTime>;
  queryPacketListLoading?: boolean;
  queryPacketRefineLoading?: boolean;
  submitInfo?: ISensorNetworkInfo;
  changeNetworkId?: any;
  sensorNetworkList?: any;
}

export interface ITaskFilter {
  conditionType: EConditionType;
  bpf?: string;
  rules?: undefined;
}

interface ISensorNetworkInfo {
  fpcSerialNumber: string;
  networkId: string;
}

// 获取选择时间
export const getOriginTime = (
  selectedTime:
    | Required<IGlobalTime>
    | {
        startTime: string;
        endTime: string;
      },
  type: 'start' | 'end',
) => {
  if (type === 'start') {
    return (selectedTime as IGlobalTime)!.originStartTime || selectedTime.startTime;
  }
  return (selectedTime as IGlobalTime)!.originEndTime || selectedTime.endTime;
};

function Packet(props: IPacketProps) {
  const {
    dispatch,
    globalSelectedTime,
    packetModel,
    queryPacketListLoading,
    submitInfo: sensorNetworkInfo = { fpcSerialNumber: '', networkId: '' },
    changeNetworkId,
    sensorNetworkList,
  } = props;
  // const [state, analysisDispatch] = useContext<any>(AnalysisContext);
  // 条件不变的情况下，第一次查询时loading，后边不会loading
  const [refineLoading, setRefineLoading] = useState(true);
  const [listLoading, setListLoading] = useState(true);
  const location = useLocation() as any as {
    pathname: string;
    query: { filter: string; bpfUri: string } & IAnalysisTaskParams;
  };
  const [packetRefine, setPacketRefine] = useState(initPacketRefineData);
  const { limit, bpfData } = packetModel;
  // // 从 uri 查询条件中获取参数
  // const { networkId: networkIdFromUrlQuery, analysisStartTime, analysisEndTime } = location.query;
  // // 从 uri 路径中获取参数
  // const { networkId: networkIdFromUrlParam, serviceId = '' }: IUriParams = useParams();
  const { serviceId }: IUriParams = useParams();
  const [state, analysisDispatch] = useContext<any>(
    serviceId ? ServiceAnalysisContext : AnalysisContext,
  );

  useEffect(() => {
    clearShareInfo(analysisDispatch);
  }, [analysisDispatch]);
  const { shareInfo } = state || {};

  // const { networkId: urlNetworkId, serviceId }: IUriParams = useParams();
  // 查询任务的 ID
  const [queryId, setQueryId] = useState<string>();
  // 过滤条件类型
  const [conditionType, setConditionType] = useState<EConditionType>(EConditionType.TUPLE);
  const [bpf, setBpf] = useState<string>(() => {
    const { bpfUri } = location.query;
    return bpfUri;
  });

  const currentNetworkSelections = useMemo(() => {
    if (sensorNetworkList && sensorNetworkInfo.fpcSerialNumber) {
      const currentFpcSerialNumber = sensorNetworkInfo.fpcSerialNumber;
      if (sensorNetworkList[currentFpcSerialNumber]) {
        return sensorNetworkList[currentFpcSerialNumber].map((item: any) => ({
          value: item.key,
          text: item.title,
        }));
      }
    }
    return undefined;
  }, [sensorNetworkInfo.fpcSerialNumber, sensorNetworkList]);

  const filterField = useFilterField({ networkSelections: currentNetworkSelections });

  const [filterData, setFilterData] = useState<IFilter[]>(() => {
    // 初始化从 uri 取过滤条件的值
    const { filter } = location.query;
    let tmpFilter = filter || '[]';
    // 共享条件为第一优先级，保留uri传条件
    if (shareInfo) {
      tmpFilter = shareInfo;
    }
    const uriFilterData: IFilter[] = parseArrayJson(tmpFilter).filter(
      (item: URLFilter) => !['network_id', 'service_id'].includes(item.field),
    );
    // 下钻后可以携带端口号的传输层协议
    const protocolInPort = [EIpProtocol.UDP, EIpProtocol.TCP, EIpProtocol.SCTP] as string[];
    // 条件中是否有不支持的端口号条件的传输层协议
    let haveUnsupportPortFilter = false;
    const tmpFilterRes = deduplicateCondition(
      uriFilterData.map((f) => {
        haveUnsupportPortFilter =
          (f.field === 'ipProtocol' && !protocolInPort.includes(String(f.operand) || '')) ||
          haveUnsupportPortFilter;
        return formatFilter(f, filterField);
      }),
      new Set(),
    ) as IFilter[];
    return tmpFilterRes.filter((item: any) => {
      if (!haveUnsupportPortFilter) {
        return true;
      }
      if (item.group) {
        return true;
      }
      if (item.field === 'port') {
        return false;
      }
      return true;
    }) as IFilter[];
  });

  // 是否显示停止按钮
  const [showCancelBtn, setShowCancelBtn] = useState(false);
  const [calcelLoading, setCancelLoading] = useState(false);

  // 计算时间
  const selectedTimeInfo = useMemo(() => {
    // 如果有外部传递过来的时间就直接使用外部的
    // if (isJumpTarget) {
    //   return {
    //     startTime: moment(+analysisStartTime!).format(),
    //     endTime: moment(+analysisEndTime!).format(),
    //   };
    // }
    return globalSelectedTime;
  }, [globalSelectedTime]);

  const [queryTimeInfo, setQueryTimeInfo] = useState({ startTime: '0', endTime: '0' });

  useEffect(() => {
    console.log(sensorNetworkInfo, 'currentSensorNetworkInfo');
  }, [sensorNetworkInfo]);

  //新创建的网络ID在哪里
  const currentNetworkId = useMemo(() => {
    const filterNetwork = filterData.find((item) => item.field === 'network_id');
    if ((sensorNetworkInfo.networkId ?? '') !== '') {
      console.log(sensorNetworkInfo.networkId, 'sensorNetworkInfo.networkId');
      return sensorNetworkInfo.networkId;
    }
    if (filterNetwork != null) {
      // console.log(filterNetwork);
      return filterNetwork.operand as string;
    }
    return 'ALL';
  }, [filterData, sensorNetworkInfo.networkId]);

  //业务ID
  const currentServiceId = useMemo(() => {
    const filterService = filterData.find((item) => item.field === 'service_id');

    if (serviceId) {
      console.log(serviceId, 'serviceId');
      return serviceId;
    }

    if (filterService) {
      console.log(filterService, 'filterService');
      return filterService.operand as string;
    }

    return undefined;
  }, [filterData, serviceId]);

  const bpfSentence = useMemo(() => {
    const filterBpf = filterData.find((item) => item.field === 'bpf');
    if (filterBpf) {
      console.log(filterBpf, 'filterBpf');
      // setBpf(filterBpf.operand as string);
      return filterBpf.operand as string;
    }
    return '';
  }, [filterData]);

  const currentFilterFields = useMemo(() => {
    return filterField.map((item) => {
      if (item.dataIndex === 'network_id') {
        return {
          ...item,
          disabled: currentNetworkId !== 'ALL',
        };
      }
      if (item.dataIndex === 'service_id') {
        return {
          ...item,
          disabled: currentServiceId !== undefined,
        };
      }
      if (item.dataIndex === 'bpf') {
        return {
          ...item,
          disabled: bpfSentence !== '',
        };
      }
      return item;
    });
  }, [bpfSentence, currentServiceId, filterField, currentNetworkId]);

  /** 查询条件 */
  const queryParams = useMemo<IQueryParams>(() => {
    // 组装条件
    // let newBpf = '';
    let tuple = '';
    // if (conditionType === EConditionType.BPF) {
    //   newBpf = bpfData;
    // }
    // if (conditionType === EConditionType.TUPLE && filterData.length > 0) {
    const tupleObj: Record<string, any[]> = {};
    const tmpFilter = filterData.map((item: any) => {
      if (!item.hasOwnProperty('operand')) {
        return [...item.group];
      }
      return item;
    });
    tmpFilter.flat().forEach((item) => {
      // 从过滤条件中去掉bpf这个条件
      if (['network_id', 'service_id', 'bpf'].includes(item.field)) {
        return;
      }
      if (tupleObj && tupleObj.hasOwnProperty(item?.field)) {
        tupleObj[item.field].push(
          `${item.operator === EFilterOperatorTypes.EQ ? '' : 'NOT_'}${item?.operand}`,
        );
      } else {
        tupleObj[item.field] = [
          `${item.operator === EFilterOperatorTypes.EQ ? '' : 'NOT_'}${item?.operand}`,
        ];
      }
    });
    tuple = JSON.stringify(tupleObj);
    // }

    const newTimeObj = {
      startTime: moment(getOriginTime(selectedTimeInfo, 'start')).format(),
      endTime: moment(getOriginTime(selectedTimeInfo, 'end')).format(),
    };

    // 如果是相对时间, 每次计算参数需要重新计算时间
    if ((selectedTimeInfo as IGlobalTime).relative) {
      const newGlobalSelectedTime = getGlobalTime(selectedTimeInfo as IGlobalTime);
      newTimeObj.startTime = newGlobalSelectedTime.originStartTime;
      newTimeObj.endTime = newGlobalSelectedTime.originEndTime;
    }

    setQueryTimeInfo({ startTime: newTimeObj.startTime, endTime: newTimeObj.endTime });

    return {
      queryId: '',
      ...newTimeObj,
      // serviceId,
      serviceId: currentServiceId,
      networkId: currentNetworkId,
      // conditionType,
      tuple,
      // bpf: newBpf,
      bpf: bpfSentence,
      limit,
      fpcSerialNumber: sensorNetworkInfo.fpcSerialNumber,
      // ...sensorNetworkInfo,
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    bpfSentence,
    //这个不再需要了，因为没有去更新Model
    // bpfData,
    //这个不再需要了，因为现在不分bpf和条件规则了
    // conditionType,
    currentNetworkId,
    // eslint-disable-next-line react-hooks/exhaustive-deps
    JSON.stringify(filterData),
    limit,
    // eslint-disable-next-line react-hooks/exhaustive-deps
    JSON.stringify(selectedTimeInfo),
    currentServiceId,
    // sensorNetworkInfo,
    sensorNetworkInfo.fpcSerialNumber,
  ]);

  const taskFilter = useMemo<any>(() => {
    // const tmpConditionType = conditionType === '1' ? '0' : '1';
    // if (conditionType === EConditionType.TUPLE) {
    const rules: Record<string, any[] | any> = {};
    filterData.forEach((condition) => {
      if (condition.field === 'ipAddress') {
        if (!rules.hasOwnProperty('ip')) {
          rules.ip = [];
        }
        rules.ip.push(
          `${condition.operator === EFilterOperatorTypes.EQ ? '' : 'NOT_'}${condition?.operand}`,
        );
      } else {
        if (!rules.hasOwnProperty(condition.field)) {
          rules[condition.field] = [];
        }
        rules[condition.field].push(
          `${condition.operator === EFilterOperatorTypes.EQ ? '' : 'NOT_'}${condition?.operand}`,
        );
      }
    });
    // return `conditionType=${conditionType}&rules=${encodeURIComponent(JSON.stringify(rules))}`;
    return {
      // conditionType: tmpConditionType,
      rules,
    };
    // }
    // return `conditionType=${conditionType}&bpf=${encodeURIComponent(bpf || '')}`;
    // return {
    //   conditionType: tmpConditionType,
    //   bpf,
    // };
  }, [filterData]);

  const [packetTaskDetail, setPacketTaskDetail] = useState<IPacketTaskDetail>();
  const [isModalVisible, setIsModalVisible] = useState(false);

  // 下载pcap文件
  const handleDownloadPcap = useCallback(() => {
    // 如果搜索结果大于100M, 则跳转到创建全流量查询任务，根据filter，时间，queryId创建全流量查询任务
    // 否则用户直接下载pcap文件
    if (packetRefine.execution.searchBytes > 100 * 1024 * 1024) {
      // 组装条件

      setPacketTaskDetail({
        start: `${moment(selectedTimeInfo.startTime).valueOf()}`,
        end: `${moment(selectedTimeInfo.endTime).valueOf()}`,
        networkId: sensorNetworkInfo.networkId,
        fpcSerialNumber: [sensorNetworkInfo.fpcSerialNumber],
        taskName: `packet-${queryId?.split('-')[0]}`,
        taskFilter: taskFilter,
      });
      setIsModalVisible(true);
    } else {
      dispatch({
        type: 'packetModel/downloadPcapFile',
        payload: { ...queryParams, queryId },
      });
    }
  }, [
    packetRefine.execution.searchBytes,
    selectedTimeInfo.startTime,
    selectedTimeInfo.endTime,
    sensorNetworkInfo.networkId,
    sensorNetworkInfo.fpcSerialNumber,
    queryId,
    taskFilter,
    dispatch,
    queryParams,
  ]);

  // 查询条件变化时更新queryId
  useEffect(() => {
    setRefineLoading(true);
    setListLoading(true);
    setQueryId(uuidv1());
    abortAjax(['/appliance/packets']);
    abortAjax(['/appliance/packets/as-refines']);
  }, [queryParams]);

  const [packetListDate, setPacketListDate] = useState<any>({
    code: null,
    list: [],
    status: null,
    count: 0,
  });

  const queryPacketRefineFn = (param: any) => {
    if (param && queryId) {
      queryPacketRefine({ ...param, queryId }).then((res) => {
        const { success, result } = res;
        if (
          result?.result?.aggregations?.length ||
          result?.result?.status !== EPacketListStatus.RUNNING
        ) {
          setRefineLoading(false);
        }
        if (success) {
          setPacketRefine(result.result);
        } else {
          setRefineLoading(false);
        }
      });
    }
  };

  const quertPacketListFn = (params: any) => {
    if (params && queryId) {
      queryPacketList({ ...params, queryId }).then((res) => {
        const { result, success } = res;
        const listData = result;
        if (
          listData?.result?.list?.length ||
          EPacketListStatus.RUNNING !== listData?.result?.status
        ) {
          setListLoading(false);
        }
        if (success) {
          setPacketListDate({
            code: listData.code,
            list: listData.result.list,
            status: listData.result.status,
          });
        } else {
          setListLoading(false);
        }
      });
    }
  };

  useEffect(() => {
    if (packetListDate.status === EPacketListStatus.RUNNING) {
      quertPacketListFn(queryParams);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [queryParams, packetListDate, queryId]);

  // 查询数据包
  useEffect(() => {
    //此处去掉没有选择网络的限制
    // console.log(typeof queryParams.fpcSerialNumber);
    if (!queryId || !queryParams.fpcSerialNumber) {
      return;
    }
    // console.log(queryParams, 'queryParams');
    quertPacketListFn(queryParams);
    return () => {
      setPacketListDate({ code: null, list: [], status: null, count: 0 });
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [queryId, queryParams, sensorNetworkInfo]);

  useEffect(() => {
    //此处去掉没有网络的限制
    if (!queryId || !queryParams.fpcSerialNumber) {
      return;
    }
    if (queryId) {
      queryPacketRefineFn(queryParams);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dispatch, queryParams, queryId, sensorNetworkInfo]);

  useEffect(() => {
    // 如果列表还在查询 或者是 统计还在 RUNNING 状态，就显示取消按钮
    if (queryPacketListLoading || packetRefine.status === EPacketRefineStatus.RUNNING) {
      setShowCancelBtn(true);
    } else {
      setShowCancelBtn(false);
    }
  }, [queryPacketListLoading, packetRefine.status]);

  // 数据包取消延时发送请求
  useEffect(() => {
    if (packetRefine.status === EPacketRefineStatus.RUNNING && queryId) {
      queryPacketRefineFn(queryParams);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dispatch, queryId, packetRefine, queryParams]);

  const searchResultMessage = useMemo(() => {
    if (packetRefine.status !== EPacketRefineStatus.PKT_COUNT_OVERFLOW) {
      return null;
    }
    return (
      <Alert
        type="warning"
        message={`Truncated to ${packetRefine?.execution?.searchPacketCount || 0} packets`}
        action={
          <Tooltip title={packetRefine.message}>
            <QuestionCircleOutlined />
          </Tooltip>
        }
      />
    );
  }, [packetRefine.status, packetRefine?.execution?.searchPacketCount, packetRefine.message]);
  useClearURL();

  const handleConditionChange = (type: string) => {
    setConditionType(type as EConditionType);
  };

  const handleFilterChange = (newFilterData: IFilterCondition) => {
    setFilterData(newFilterData as IFilter[]);
  };

  const mergeFilter = (condition: IFilter[]) => {
    setFilterData((prev) => {
      const newFilterData = [...prev, ...condition];
      return newFilterData;
    });
  };

  const handleAppendFilter = (newFilter: IFilter) => {
    mergeFilter([newFilter]);
  };

  // 刷新
  const handleRefresh = () => {
    if ((selectedTimeInfo as IGlobalTime).relative) {
      dispatch({
        type: 'appModel/updateGlobalTime',
        payload: getGlobalTime(selectedTimeInfo as IGlobalTime),
      });
    }
    abortAjax(['/appliance/packets']);
    abortAjax(['/appliance/packets/as-refines']);
    setQueryId(uuidv1());
  };

  // 停止查询
  const handleStopQuery = async () => {
    if (!queryId) {
      return;
    }

    setCancelLoading(true);
    if (queryPacketListLoading) {
      await stopPacketList({ queryId, fpcSerialNumber: sensorNetworkInfo.fpcSerialNumber });
    }
    // 以聚合接口的状态为准，如果是 running 状态就可以停止查询
    if (packetRefine.status === EPacketRefineStatus.RUNNING) {
      await stopPacketRefine({ queryId });
    }
    setCancelLoading(false);
  };

  return (
    <Row className={styles['packet-wrapper']} gutter={10}>
      <Col span={5}>
        <AutoHeightContainer contentStyle={{ overflowY: 'auto', overflowX: 'auto' }}>
          <div className={styles['packet-refine']}>
            <PacketRefine
              // clickable={conditionType === EConditionType.TUPLE}
              onTreeNodeClick={mergeFilter}
              loading={refineLoading}
              data={packetRefine}
            />
          </div>
        </AutoHeightContainer>
      </Col>
      <Col span={19} className={styles['packet-content']}>
        <div className={styles['packet-header']}>
          <div className={styles['packet-header-left']}>
            <Progress
              data={packetRefine}
              listData={packetListDate?.list || []}
              timeInfo={queryTimeInfo}
            />
          </div>
          <Space className={styles['packet-header-right']} direction="vertical">
            <Space>
              <Button
                type="primary"
                icon={<DownloadOutlined />}
                onClick={handleDownloadPcap}
                size="small"
              >
                下载 PCAP
              </Button>
              <Button
                type="primary"
                icon={<FundOutlined />}
                size="small"
                onClick={() => {
                  const timeInfo = {
                    startTime: moment(queryParams.startTime).valueOf(),
                    endTime: moment(queryParams.endTime).valueOf(),
                  };
                  jumpToAnalysisTabNew(
                    state,
                    analysisDispatch,
                    ENetworkTabs.PACKETANALYSIS,
                    {
                      pktAnalysisParams: { ...queryParams, ...timeInfo, queryId },
                      filter: [],
                      globalSelectedTime: timeInfo,
                      queryId,
                    },
                    // JSON.stringify({
                    //   ...queryParams,
                    //   startTime: moment(queryParams.startTime).valueOf(),
                    //   endTime: moment(queryParams.endTime).valueOf(),
                    //   queryId,
                    // }),
                  );
                }}
              >
                在线分析
              </Button>
            </Space>
            <div
              className={styles['packet-header-total']}
              style={{ width: '100%', textAlign: 'center' }}
            >
              {`${numeral(packetRefine.execution.searchPacketCount || 0).format(
                '0,0',
              )} packets (${bytesToSize(packetRefine.execution.searchBytes || 0)})`}
            </div>
          </Space>
        </div>
        <Card bordered size="small" className={styles['packet-filter']}>
          <Tabs
            // activeKey={conditionType}
            size="small"
            // onChange={handleConditionChange}
            tabBarExtraContent={searchResultMessage}
          >
            <TabPane tab="Filter条件" key={EConditionType.TUPLE}>
              {conditionType === EConditionType.TUPLE && (
                <FieldFilter
                  fields={currentFilterFields}
                  onChange={handleFilterChange}
                  condition={filterData}
                  historyStorageKey="packet-filter-history"
                  simple
                  simpleOperator
                  extra={
                    <Space>
                      {showCancelBtn && (
                        <Button
                          danger
                          loading={calcelLoading}
                          type="primary"
                          icon={<StopOutlined />}
                          onClick={() => {
                            handleStopQuery();
                          }}
                        >
                          停止查询
                        </Button>
                      )}
                      <Button
                        type="primary"
                        icon={<ReloadOutlined />}
                        loading={queryPacketListLoading}
                        onClick={() => {
                          handleRefresh();
                        }}
                      >
                        刷新
                      </Button>
                    </Space>
                  }
                />
              )}
            </TabPane>
            {/* <TabPane tab="BPF语句" key={EConditionType.BPF}>
              {conditionType === EConditionType.BPF && (
                <FilterBpf onChange={setBpf} defaultBpf={bpf} />
              )}
            </TabPane> */}
          </Tabs>
        </Card>
        <PacketList
          listData={packetListDate?.list || []}
          // conditionType={conditionType}
          onAppendFilter={handleAppendFilter}
          loading={listLoading}
        />
      </Col>
      <Modal
        title={'新建数据包检索任务'}
        visible={isModalVisible}
        width={1200}
        footer={null}
        onCancel={() => {
          setIsModalVisible(false);
        }}
      >
        <TransmitTaskForm
          packetTaskDetail={packetTaskDetail}
          onSubmit={() => {
            setIsModalVisible(false);
          }}
          showCancelBtn={false}
        />
      </Modal>
    </Row>
  );
}

export default connect(
  ({
    packetModel,
    appModel: { globalSelectedTime },
    loading: { effects },
  }: IPacketConnectState) => ({
    packetModel,
    globalSelectedTime,
    queryPacketListLoading: effects['packetModel/queryPacketList'],
    queryPacketRefineLoading: effects['packetModel/queryPacketRefine'],
  }),
)(Packet);
