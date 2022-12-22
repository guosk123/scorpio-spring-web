import type { EMetricApiType } from '@/common/api/analysis';
import type { ConnectState } from '@/models/connect';
import { pageModel } from '@/utils/frame/model';
import { message } from 'antd';
import modelExtend from 'dva-model-extend';
import _ from 'lodash';
import moment from 'moment';
import type { Effect, Reducer } from 'umi';
import type { ILogicalSubnetMap } from '../Configuration/LogicalSubnet/typings';
import type { INetworkMap } from '../Configuration/Network/typings';
import { queryPcpInfo } from './OfflinePcapAnalysis/service';
import type { IOfflinePcapData } from './OfflinePcapAnalysis/typing';
import {
  queryAlertMsgCnt,
  queryAllNetworkStat,
  queryBaselineSetting,
  queryHttpAnalysis,
  queryMetricSetting,
  queryNetworkDashboard,
  queryNetworkFlow,
  queryNetworkFlowHistogram,
  queryPayloadHistogram,
  queryPerformanceHistogram,
  queryPerformanceSetting,
  queryServiceDashboard,
  queryServiceFlowHistogram,
  queryTcpHistogram,
  updataBaselineSetting,
  updataMetricSetting,
  updatePerformanceSetting,
} from './service';
import { DEFAULT_LONG_CONNECTION_SECONDS } from './TCP_Connection/LongConnectionSetting';
import type {
  HttpAnalysisResult,
  INetworkStatData,
  IBaselineSettingData,
  IDHCPStatFields,
  IFlowAnalysisData,
  IMetricSettingData,
  INetworkDashboardData,
  IPayloadStat,
  IPerformanceSettingData,
  IPerformanceStatData,
  IServiceDashboardData,
  ITcpStatData,
} from './typings';
import { EMetricSettingCategory } from './typings';

const excludeElementInObj = (obj: any, excludeEle: string) => {
  return _.pick(
    obj,
    _.filter(_.keys(obj), (item) => item !== excludeEle),
  );
};
export interface IPagination {
  size?: number;
  number?: number;
  totalPages?: number;
  totalElements?: number;
}

/** 统计分析 */
export interface INpmdModelState {
  // -------网络分析数据--------
  /** 所有网络统计数据 */
  allNetworkStatData: INetworkStatData[];
  /** 网络概览数据 */
  networkDashboardData: INetworkDashboardData;

  // -------业务分析数据--------
  /** 业务概览数据 */
  serviceDashboardData: IServiceDashboardData;
  /** 告警消息数量 */
  alertMsgCnt: number;
  // -------网络 && 业务 共有数据--------
  /** 负载量统计数据 */
  payloadHistogramData: IPayloadStat[];
  /** 性能分析数据 */
  performanceHistogramData: IPerformanceStatData[];
  /** TCP统计分析 */
  tcpHistogramData: ITcpStatData[];

  // -------参数数据--------
  /** 基线配置 */
  baseLineSettingData: IBaselineSettingData[];
  /** 性能配置 */
  performanceSettingData: IPerformanceSettingData;
  /** 长连接认定时间: 秒 */
  longConnectionSeconds: number;

  /** 流量分析页面的相关数据 */
  flowTableData: Record<EMetricApiType, IFlowAnalysisData[] | IDHCPStatFields[]>;
  flowHistogramData: Record<EMetricApiType, IFlowAnalysisData[] | IDHCPStatFields[]>;
  flowDetailHistogramData: IFlowAnalysisData[];

  // TODO:
  // currentPcap: IOfflinePcapData;

  /** 离线文件列表 */
  currentPcpInfo: IOfflinePcapData;

  httpAnalysisData: HttpAnalysisResult;
  beforeOldestPacketArea: any;
  /** 是否为刷新模式 true为刷新，对应play button的状态 stop */
  isRefreshFlag: boolean;
}

export interface NpmdModel {
  namespace: string;
  state: INpmdModelState;
  effects: {
    // -------网络分析--------
    /** 所有网络统计分析 */
    queryAllNetworkStat: Effect;
    /** 网络概览 */
    queryNetworkDashboard: Effect;
    /** 查询告警消息数量 */
    queryAlertMsgCnt: Effect;
    // -------业务分析--------
    /** 业务概览 */
    queryServiceDashboard: Effect;
    /**
     * 获取业务的流量统计图
     * @description 用于业务看板中绘制带宽的小图
     */
    queryServiceFlowHistogram: Effect;

    // -------离线文件--------
    /** 查询离线文件信息 */
    queryPcapInfo: Effect;

    // -------网络 && 业务 共有方法--------
    /** 网络负载量统计 */
    queryPayloadHistogram: Effect;
    /** 网络性能分析 */
    queryPerformanceHistogram: Effect;
    /** 网络TCP统计 */
    queryTcpHistogram: Effect;
    /** 流量数据查询 */
    queryNetworkFlow: Effect;
    /** 点击某一行时的具体数据 */
    queryNetworkFlowDetailHistogramData: Effect;
    /** 查询流量汇总数据 */
    queryNetworkFlowTableData: Effect;

    // -------参数配置--------
    /** 长连接时间配置 */
    queryLongConnectionSetting: Effect;
    /** 长连接时间更新 */
    updateLongConnectionSetting: Effect;
    /** 基线定义 */
    queryBaselineSetting: Effect;
    /** 基线更新 */
    updataBaselineSetting: Effect;
    /** 性能配置数据 */
    queryPerformanceSetting: Effect;
    /** 性能设置更新 */
    updatePerformanceSetting: Effect;
    /** http分析数据查询 */
    queryHttpAnalysisData: Effect;
  };
  reducers: {
    changeOldestPacketArea: Reducer<INpmdModelState>;
    changeRefreshFlag: Reducer<INpmdModelState>;
  };
}

const Model = modelExtend(pageModel, {
  namespace: 'npmdModel',

  state: {
    allNetworkStatData: [],
    networkDashboardData: <INetworkDashboardData>{},
    alertMsgCnt: 0,

    payloadHistogramData: [],
    performanceHistogramData: [],
    tcpHistogramData: [],

    serviceDashboardData: <IServiceDashboardData>{},

    baseLineSettingData: [],
    performanceSettingData: {},
    longConnectionSeconds: DEFAULT_LONG_CONNECTION_SECONDS,

    flowTableData: {} as INpmdModelState['flowTableData'],
    flowHistogramData: {} as INpmdModelState['flowHistogramData'],
    flowDetailHistogramData: [],
    currentPcpInfo: <IOfflinePcapData>{},

    httpAnalysisData: {
      httpMethod: [],
      httpRequest: [],
      httpCode: [],
      os: [],
    },

    beforeOldestPacketArea: {},
    isRefreshFlag: true,
  },

  effects: {
    *queryAllNetworkStat({ payload = {} }, { call, put, select }) {
      const { success, result } = yield call(queryAllNetworkStat, payload);
      const data: INetworkStatData[] = success ? result : [];
      // 所有的物理网络
      const allNetworkMap: INetworkMap = yield select(
        (state: ConnectState) => state.networkModel.allNetworkMap,
      );
      // @ts-ignore
      yield put.resolve({
        type: 'logicSubnetModel/queryAllLogicalSubnets',
        payload: {},
      });
      // 获取所有的逻辑子网
      const allLogicalSubnetMap: ILogicalSubnetMap = yield select(
        (state: ConnectState) => state.logicSubnetModel.allLogicalSubnetMap,
      );

      const allNetworkStatData: INetworkStatData[] = [];
      // 物理网络和逻辑子网合并成表格树
      for (let index = 0; index < data.length; index += 1) {
        const row = data[index];
        // 跳过逻辑子网
        if (row.parentId) {
          continue;
        }
        // 查找该物理网络的逻辑子网
        const children: INetworkStatData[] = data.filter((net) => net.parentId === row.networkId);

        allNetworkStatData.push({
          ...row,
          networkName: allNetworkMap[row.networkId]?.name || `[已删除: ${row.networkId}]`,
          networkBandwidth: allNetworkMap[row.networkId]?.bandwidth || 0,
          children:
            children.length > 0
              ? children.map((net) => ({
                  ...net,
                  networkName:
                    allLogicalSubnetMap[net.networkId]?.name || `[已删除: ${net.networkId}]`,
                  networkBandwidth: allLogicalSubnetMap[net.networkId]?.bandwidth || 0,
                  children: undefined,
                }))
              : undefined,
        });
      }

      yield put({
        type: 'updateState',
        payload: {
          allNetworkStatData,
        },
      });

      return success
        ? result.map((item: INetworkStatData) => {
            return {
              ...item,
              networkBandwidth:
                allNetworkMap[item.networkId]?.bandwidth ||
                allLogicalSubnetMap[item.networkId]?.bandwidth ||
                0,
            };
          })
        : [];
    },
    *queryAlertMsgCnt({ payload }, { call, put }) {
      const { success, result } = yield call(queryAlertMsgCnt, payload);
      if (!success) {
        return;
      }
      yield put({
        type: 'updateState',
        payload: {
          alertMsgCnt: result,
        },
      });
    },
    *queryNetworkDashboard({ payload = {} }, { call, put }) {
      const { success, result } = yield call(queryNetworkDashboard, payload);
      yield put({
        type: 'updateState',
        payload: {
          networkDashboardData: success ? result : {},
        },
      });
    },
    *queryPayloadHistogram({ payload = {} }, { call, put }) {
      const { success, result } = yield call(queryPayloadHistogram, payload);
      const payloadHistogramData: IPerformanceStatData[] = success ? result : [];
      payloadHistogramData.sort(
        (a, b) => new Date(a.timestamp).valueOf() - new Date(b.timestamp).valueOf(),
      );

      yield put({
        type: 'updateState',
        payload: {
          payloadHistogramData: success ? result : [],
        },
      });
    },
    *queryPerformanceHistogram({ payload = {} }, { call, put }) {
      const { success, result } = yield call(queryPerformanceHistogram, payload);
      const performanceHistogramData: IPerformanceStatData[] = success ? result : [];
      performanceHistogramData.sort(
        (a, b) => new Date(a.timestamp).valueOf() - new Date(b.timestamp).valueOf(),
      );

      yield put({
        type: 'updateState',
        payload: {
          performanceHistogramData,
        },
      });
    },
    *queryTcpHistogram({ payload = {} }, { call, put }) {
      const { success, result } = yield call(queryTcpHistogram, payload);
      const tcpHistogramData: ITcpStatData[] = success ? result : [];
      tcpHistogramData.sort(
        (a, b) => new Date(a.timestamp).valueOf() - new Date(b.timestamp).valueOf(),
      );

      yield put({
        type: 'updateState',
        payload: {
          tcpHistogramData,
        },
      });
    },

    *queryServiceDashboard({ payload = {} }, { call, put }) {
      const { success, result } = yield call(queryServiceDashboard, payload);
      yield put({
        type: 'updateState',
        payload: {
          serviceDashboardData: success ? result : {},
        },
      });
    },
    *queryServiceFlowHistogram({ payload = {} }, { call }) {
      const { success, result } = yield call(queryServiceFlowHistogram, payload);
      return success ? result : [];
    },
    *queryLongConnectionSetting({ payload = {} }, { call, put }) {
      const { success, result } = yield call(queryMetricSetting, payload);
      const res: IMetricSettingData[] = success ? result : [];
      const target = res.find((row) => row.metric === EMetricSettingCategory.LONG_CONNECTION);
      yield put({
        type: 'updateState',
        payload: {
          longConnectionSeconds: target?.value || DEFAULT_LONG_CONNECTION_SECONDS,
        },
      });
    },
    *updateLongConnectionSetting({ payload = {} }, { call }) {
      const { success } = yield call(updataMetricSetting, payload);
      if (success) {
        message.success('保存成功');
      } else {
        message.error('保存失败');
      }
      return success;
    },
    *queryBaselineSetting({ payload = {} }, { call, put }) {
      const { success, result } = yield call(queryBaselineSetting, payload);
      yield put({
        type: 'updateState',
        payload: {
          baseLineSettingData: success ? result : [],
        },
      });
    },
    *updataBaselineSetting({ payload = {} }, { call }) {
      const { success } = yield call(updataBaselineSetting, payload);
      if (success) {
        message.success('保存成功');
      } else {
        message.error('保存失败');
      }
      return success;
    },
    *queryPerformanceSetting({ payload = {} }, { call, put }) {
      const { success, result } = yield call(queryPerformanceSetting, payload);
      yield put({
        type: 'updateState',
        payload: {
          performanceSettingData: success ? result : {},
        },
      });
    },
    *updatePerformanceSetting({ payload = {} }, { call }) {
      const { success } = yield call(updatePerformanceSetting, payload);
      if (success) {
        message.success('保存成功');
      } else {
        message.error('保存失败');
      }
      return success;
    },

    *queryNetworkFlow({ payload }, { call, all, put, select }) {
      const { queryIds = [], ...restParams } = payload;
      const [tableResponse, histogramResponse] = yield all(
        Array.isArray(queryIds) && queryIds.length === 2
          ? [
              call(queryNetworkFlow, { ...restParams, queryId: queryIds[0] }),
              call(queryNetworkFlowHistogram, {
                ...excludeElementInObj(restParams, 'count'),
                queryId: queryIds[1],
              }),
            ]
          : [call(queryNetworkFlow, restParams), call(queryNetworkFlowHistogram, restParams)],
      );
      const preData = select((state: ConnectState) => {
        return {
          flowTableData: state.npmdModel.flowTableData,
          flowHistogramData: state.npmdModel.flowHistogramData,
        };
      });

      const newData: { tableData: any[]; histogramData: any[] } = {
        tableData: tableResponse.success ? tableResponse.result : [],
        histogramData: histogramResponse.success ? histogramResponse.result : [],
      };

      newData.histogramData?.sort(
        (a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime(),
      );
      yield put({
        type: 'updateState',
        payload: {
          flowTableData: {
            ...preData.flowTableData,
            [restParams.metricApi]: newData.tableData,
          },
          flowHistogramData: {
            ...preData.flowHistogramData,
            [restParams.metricApi]: newData.histogramData,
          },
        },
      });

      return {
        // 返回是否被取消的状态
        abort: !tableResponse.status || !histogramResponse.status,
      };
    },
    *queryNetworkFlowDetailHistogramData({ payload }, { call, put }) {
      const { success, result } = yield call(
        queryNetworkFlowHistogram,
        excludeElementInObj(payload, 'count'),
      );
      yield put({
        type: 'updateState',
        payload: {
          flowDetailHistogramData: success ? result : [],
        },
      });
    },
    *queryNetworkFlowTableData({ payload }, { call }) {
      const { status, success, result } = yield call(queryNetworkFlow, payload);
      return {
        status,
        success,
        result: success ? result : [],
      };
    },
    // TODO 修改查询离线文件详情
    *queryPcapInfo({ payload = {} }, { call, put }) {
      const { success, result } = yield call(queryPcpInfo, payload);
      let currentPcpInfo = {} as IOfflinePcapData;
      // 为了防止 pcap 文件中数据的起止时间间隔太小，这里专门处理下数据包的起止时间
      // 开始时间向前-1分钟
      // 结束时间向前+1分钟
      if (success) {
        currentPcpInfo = result;

        currentPcpInfo.filterStartTime = moment(currentPcpInfo.packetStartTime)
          .subtract(1, 'minutes')
          .format();
        currentPcpInfo.filterEndTime = moment(currentPcpInfo.packetEndTime)
          .add(1, 'minutes')
          .format();
      }

      yield put({
        type: 'updateState',
        payload: {
          currentPcpInfo,
        },
      });
      // 表示获取到了最新离线详情
      return true;
    },
    *queryHttpAnalysisData({ payload }, { call, put }) {
      const { success, result } = yield call(queryHttpAnalysis, payload);
      if (success) {
        yield put({
          type: 'updateState',
          payload: {
            httpAnalysisData: result,
          },
        });
      }
    },
  },
  reducers: {
    changeOldestPacketArea(state, action) {
      return {
        ...state,
        beforeOldestPacketArea: action.payload.beforeOldestPacketArea,
      };
    },
    changeRefreshFlag(state, action) {
      return {
        ...state,
        isRefreshFlag: action.payload.isRefreshFlag,
      };
    },
  },
} as NpmdModel);

export default Model;
