import AutoHeightContainer from '@/components/AutoHeightContainer';
import FieldFilter, { formatFilter } from '@/components/FieldFilter';
import type {
  EFieldType,
  IEnumValue,
  IField,
  IFilter,
  IFilterCondition,
} from '@/components/FieldFilter/typings';
import { EFieldOperandType, EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { deduplicateCondition } from '@/components/FieldFilter/utils';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType, getGlobalTime } from '@/components/GlobalTimeSelector';
import type { IUriParams } from '@/pages/app/analysis/typings';
import {
  abortAjax,
  bytesToSize,
  getLinkUrl,
  getPageQuery,
  jumpNewPage,
  parseArrayJson,
  validateBpfStr,
} from '@/utils/utils';
import {
  FundOutlined,
  QuestionCircleOutlined,
  ReloadOutlined,
  SearchOutlined,
  StopOutlined,
} from '@ant-design/icons';
import { Alert, Button, Card, Col, Row, Space, Tabs, Tooltip } from 'antd';
import _ from 'lodash';
import moment from 'moment';
import numeral from 'numeral';
import { useCallback, useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, history, useLocation, useParams } from 'umi';
import { v1 as uuidv1 } from 'uuid';
import type { EModelAlias } from '../../analysis/components/fieldsManager';
import {
  EFieldEnumValueSource,
  getEnumValueFromModelNext,
} from '../../analysis/components/fieldsManager';
import { queryPcpInfo } from '../../analysis/OfflinePcapAnalysis/service';
import type { IOfflinePcapData } from '../../analysis/OfflinePcapAnalysis/typing';
import type { EPacketFileType } from '../components/DownLoadPktBtn';
import type { IFilterTuple } from '../TransmitTask/typings';
import DownLoadPktFileBtn from './components/DownLoadPktFileBtn';
import DslExamples from './components/DslExamples';
import FilterBpf from './components/FilterBpf';
import PacketList, { fieldList } from './components/PacketList';
import PacketRefine from './components/PacketRefine';
import Progress from './components/Progress';
import styles from './index.less';
import type { IPacketModelState } from './model';
import { initPacketRefineData } from './model';
import { queryPacketList, queryPacketRefine, stopPacketList, stopPacketRefine } from './service';
import type { IPacketConnectState, IQueryParams } from './typings';
import { EConditionType, EIpProtocol, EPacketListStatus, EPacketRefineStatus } from './typings';

const { TabPane } = Tabs;

export const packetUrl = '/analysis/trace/packet/detail';

/** 数据包进行查询的字段 */
export const packetSearchableFields = fieldList
  .filter((field) => field.searchable)
  .map((field) => field.dataIndex);

/** 数据包过滤字段 */
export const filterField: IField[] = fieldList
  .filter((field) => field.searchable)
  .map((field) => {
    const {
      dataIndex,
      name,
      filterOperandType,
      filterFieldType,
      enumSource,
      enumValue,
      operators,
    } = field;
    // 判断这个过滤字符串是不是可枚举类型的
    const isEnum = filterOperandType === EFieldOperandType.ENUM;
    // 可枚举的列
    const enumValueList: IEnumValue[] = [];
    if (isEnum) {
      if (enumSource === EFieldEnumValueSource.LOCAL) {
        enumValueList.push(...(enumValue as IEnumValue[]));
      } else {
        const modelData = getEnumValueFromModelNext(enumValue as EModelAlias);
        if (modelData !== null) {
          enumValueList.push(...modelData.list);
        }
      }
    }

    return {
      title: name,
      dataIndex,
      operandType: filterOperandType as EFieldOperandType,
      type: filterFieldType as EFieldType,
      operators,
      ...(isEnum
        ? {
            enumValue: enumValueList,
          }
        : {}),
      //添加一个bpf过滤条件
      ...(dataIndex === 'bpf'
        ? {
            validator: validateBpfStr as (value: string) => Promise<any>,
          }
        : {}),
    };
  });

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

function Packet({
  dispatch,
  globalSelectedTime,
  packetModel,
  queryPacketListLoading,
}: IPacketProps) {
  // 条件不变的情况下，第一次查询时loading，后边不会loading
  const [refineLoading, setRefineLoading] = useState(true);
  const [listLoading, setListLoading] = useState(true);
  const location = useLocation() as any as {
    pathname: string;
    query: { filter: string; bpfUri: string } & IAnalysisTaskParams;
  };
  // console.log(location,'loaction');
  const [packetRefine, setPacketRefine] = useState(initPacketRefineData);
  const { limit, bpfData } = packetModel;
  // 从 uri 查询条件中获取参数
  const { networkId: networkIdFromUrlQuery, analysisStartTime, analysisEndTime } = location.query;
  // 从 uri 路径中获取参数
  const { pcapFileId }: IUriParams = useParams();
  // pcap文件信息
  const [pcapFileinfo, setPcapFileInfo] = useState<IOfflinePcapData>();
  // 查询任务的 ID
  const [queryId, setQueryId] = useState<string>();
  // 过滤条件类型
  const [conditionType, setConditionType] = useState<EConditionType>(EConditionType.TUPLE);
  const [bpf, setBpf] = useState<string>(() => {
    const { bpfUri } = location.query;
    console.log(bpfUri, 'bpfUri');
    return bpfUri;
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

  const quertPacketListFn = (params: any, setPacketListDate: any) => {
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
  const [filterData, setFilterData] = useState<IFilter[]>(() => {
    // 初始化从 uri 取过滤条件的值
    const { filter } = location.query;
    const uriFilterData: IFilter[] = parseArrayJson(decodeURIComponent(filter));
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

  //此时不需要去更新跳转的bpfUri

  // const replaceUriByBpf = useCallback((bpfString: string) => {
  //   const pageQuery = getPageQuery();
  //   history.replace({
  //     pathname: history.location.pathname,
  //     query: {
  //       ...pageQuery,
  //       bpfUri: bpfString,
  //     },
  //   });
  // }, []);

  // useEffect(() => {
  //   replaceUriByBpf(bpfSentence);
  // }, [bpfSentence, replaceUriByBpf]);

  useEffect(() => {
    (async () => {
      if (pcapFileId) {
        const { success, result } = await queryPcpInfo(pcapFileId);
        if (success) {
          setPcapFileInfo(result);
        }
      }
    })();
  }, [pcapFileId]);

  // 判断页面是否是从别的页面下钻过来
  const isJumpTarget = useMemo(() => {
    return networkIdFromUrlQuery && analysisStartTime && analysisEndTime;
  }, [analysisEndTime, analysisStartTime, networkIdFromUrlQuery]);

  // 计算时间
  const selectedTimeInfo = useMemo(() => {
    // 如果有外部传递过来的时间就直接使用外部的
    if (isJumpTarget) {
      return {
        startTime: moment(+analysisStartTime!).format(),
        endTime: moment(+analysisEndTime!).format(),
      };
    }
    return globalSelectedTime;
  }, [analysisEndTime, analysisStartTime, globalSelectedTime, isJumpTarget]);

  const [queryTimeInfo, setQueryTimeInfo] = useState({ startTime: '0', endTime: '0' });

  // 网络 ID，先从 props 中取，再从 uri 中取
  const networkId = useMemo(() => {
    const filterNetwork = filterData.find((item) => item.field === 'network_id');

    if (filterNetwork) {
      return filterNetwork.operand as string;
    }

    if (location.pathname.indexOf(packetUrl) !== -1) {
      return 'ALL';
    }

    return networkIdFromUrlQuery;
  }, [location.pathname, filterData, networkIdFromUrlQuery]);

  const serviceId = useMemo(() => {
    const filterService = filterData.find((item) => item.field === 'service_id');

    if (filterService) {
      console.log(filterService, 'filterService');
      return filterService.operand as string;
    }
    return '';
  }, [filterData]);

  const bpfSentence = useMemo(() => {
    const filterBpf = filterData.find((item) => item.field === 'bpf');
    if (filterBpf) {
      console.log(filterBpf, 'filterBpf');
      // setBpf(filterBpf.operand as string);
      return filterBpf.operand as string;
    }
    return '';
  }, [filterData]);

  const packetFilterFields: IField[] = useMemo(() => {
    if (pcapFileId) {
      return filterField
        .filter((item) => {
          return !['network_id', 'service_id'].includes(item.dataIndex);
        })
        .map((item) => {
          if (item.dataIndex === 'bpf') {
            return {
              ...item,
              disabled: bpfSentence !== '',
            };
          }
          return item;
        });
    }

    return filterField.map((item) => {
      if (item.dataIndex === 'network_id') {
        return {
          ...item,
          disabled: networkId !== 'ALL',
        };
      }
      if (item.dataIndex === 'service_id') {
        return {
          ...item,
          disabled: serviceId !== '',
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
  }, [bpfSentence, networkId, pcapFileId, serviceId]);

  /** 查询条件 */
  const queryParams = useMemo<IQueryParams>(() => {
    // 组装条件
    // let newBpf = '';
    let tuple = '';
    // if (conditionType === EConditionType.BPF) {
    //   newBpf = bpfData;
    // }
    // if (conditionType === EConditionType.TUPLE && filterData.length > 0) {

    if (filterData.length > 0) {
      const tupleObj: Record<string, any[]> = {};
      const tmpFilter = filterData.map((item: any) => {
        if (!item.hasOwnProperty('operand')) {
          return [...item.group];
        }
        return item;
      });
      console.log(tmpFilter, 'tmpFilter');
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
    }

    const newTimeObj = {
      startTime: moment(getOriginTime(selectedTimeInfo, 'start')).format(),
      endTime: moment(getOriginTime(selectedTimeInfo, 'end')).format(),
    };

    // 如果是相对时间, 每次计算参数需要重新计算时间
    if (!isJumpTarget && (selectedTimeInfo as IGlobalTime).relative) {
      const newGlobalSelectedTime = getGlobalTime(selectedTimeInfo as IGlobalTime);
      newTimeObj.startTime = newGlobalSelectedTime.originStartTime;
      newTimeObj.endTime = newGlobalSelectedTime.originEndTime;
    }

    setQueryTimeInfo({ startTime: newTimeObj.startTime, endTime: newTimeObj.endTime });

    return {
      queryId: '',
      ...newTimeObj,
      networkId,
      serviceId,
      packetFileId: pcapFileId,
      // conditionType,
      tuple,
      // bpf: newBpf,
      bpf: bpfSentence,
      limit,
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    //这个不再需要了，因为没有去更新Model
    // bpfData,
    // 这个不再需要了，因为现在不分bpf和条件规则了
    // conditionType,
    // eslint-disable-next-line react-hooks/exhaustive-deps
    JSON.stringify(filterData),
    isJumpTarget,
    limit,
    networkId,
    // eslint-disable-next-line react-hooks/exhaustive-deps
    JSON.stringify(selectedTimeInfo),
    serviceId,
    pcapFileId,
  ]);

  //此处过滤保留的是除了网络ID和业务ID还有bpf条件之外的过滤条件
  const filterString = useMemo(() => {
    // if (conditionType === EConditionType.TUPLE) {
    const rules: IFilterTuple = {};
    console.log(filterData, 'filterData');
    // 在规则条件中去掉网络ID和业务ID这两个字段
    // 在规则过滤中去掉bpf这个字段
    filterData
      .filter((condition) => !['network_id', 'service_id', 'bpf'].includes(condition.field))
      .forEach((condition) => {
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
    return `rules=${encodeURIComponent(JSON.stringify(rules))}`;
    // return `conditionType=${conditionType}&rules=${encodeURIComponent(JSON.stringify(rules))}`;
    // }
    // return `conditionType=${conditionType}&bpf=${encodeURIComponent(bpf || '')}`;
  }, [filterData]);

  const pcapDownloadButtonDisable = useMemo(() => {
    return packetRefine.execution.searchBytes > 100 * 1024 * 1024;
  }, [packetRefine]);

  // 下载pcap文件
  const handleDownloadPcap = (params: { fileType: EPacketFileType }) => {
    dispatch({
      type: 'packetModel/downloadPcapFile',
      payload: { ...queryParams, queryId, ...params },
    });
  };

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

  useEffect(() => {
    if (packetListDate.status === EPacketListStatus.RUNNING) {
      quertPacketListFn(queryParams, setPacketListDate);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [queryParams, packetListDate, queryId]);

  // 查询数据包
  useEffect(() => {
    if (!queryId) {
      return;
    }
    quertPacketListFn(queryParams, setPacketListDate);
    return () => {
      setPacketListDate({ code: null, list: [], status: null, count: 0 });
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [queryId, queryParams]);

  useEffect(() => {
    if (!queryId) {
      return;
    }
    if (queryId) {
      queryPacketRefineFn(queryParams);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dispatch, queryParams, queryId]);

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

  const handleConditionChange = (type: string) => {
    setConditionType(type as EConditionType);
  };

  const replaceUri = useCallback((newCondition: IFilter[]) => {
    const pageQuery = getPageQuery();
    history.replace({
      pathname: history.location.pathname,
      query: {
        ...pageQuery,
        filter: JSON.stringify(
          newCondition.map((f) => ({
            field: f.field,
            operator: f.operator,
            operand: f.operand,
          })),
        ),
      },
    });
  }, []);

  const handleFilterChange = (newFilterData: IFilterCondition) => {
    setFilterData(newFilterData as IFilter[]);
    replaceUri(newFilterData as IFilter[]);
  };

  const mergeFilter = (condition: IFilter[]) => {
    setFilterData((prev) => {
      const newFilterData = [...prev, ...condition];
      replaceUri(newFilterData as IFilter[]);
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
      await stopPacketList({ queryId });
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
              loading={refineLoading}
              // clickable={conditionType === EConditionType.TUPLE}
              onTreeNodeClick={mergeFilter}
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
              {/* <Button
                type="primary"
                disabled={pcapDownloadButtonDisable}
                icon={<DownloadOutlined />}
                onClick={handleDownloadPcap}
                size="small"
              >
                下载 PCAP
              </Button> */}
              <DownLoadPktFileBtn
                disabled={pcapDownloadButtonDisable}
                onClick={handleDownloadPcap}
                accessKey={'exportBtn'}
              />
              <Button
                type="primary"
                icon={<SearchOutlined />}
                onClick={() => {
                  jumpNewPage(
                    `/analysis/trace/transmit-task/create?${filterString}&start=${moment(
                      selectedTimeInfo.startTime,
                    ).valueOf()}&end=${moment(selectedTimeInfo.endTime).valueOf()}${
                      pcapFileId && pcapFileinfo
                        ? `&pcapFileId=${pcapFileId}&pcapFileName=${pcapFileinfo?.name}`
                        : `&networkId=${networkId}`
                    }${bpfSentence ? `&bpfData=${bpfSentence}` : ''}&taskName=packet-${
                      uuidv1().split('-')[0]
                    }`,
                  );
                }}
                size="small"
              >
                创建流量查询任务
              </Button>
              <Button
                type="primary"
                icon={<FundOutlined />}
                size="small"
                onClick={() => {
                  let urlPrefix = '/analysis/trace';

                  if (pcapFileId) {
                    urlPrefix = `/analysis/offline/${pcapFileId}`;
                  }

                  jumpNewPage(
                    `${getLinkUrl(urlPrefix)}/packet/analysis?query=${encodeURIComponent(
                      JSON.stringify({ ...queryParams, queryId }),
                    )}&from=${new Date(
                      getOriginTime(selectedTimeInfo, 'start'),
                    ).valueOf()}&to=${new Date(
                      getOriginTime(selectedTimeInfo, 'end'),
                    ).valueOf()}&timeType=${ETimeType.CUSTOM}`,
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
                  fields={packetFilterFields}
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
                      <DslExamples />
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
          // 现在不区分BPF和规则条件的关系了，所以直接删除掉这个参数
          // conditionType={conditionType}
          onAppendFilter={handleAppendFilter}
          loading={listLoading}
        />
      </Col>
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
