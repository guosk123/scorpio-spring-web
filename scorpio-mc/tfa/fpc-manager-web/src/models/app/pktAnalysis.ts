import type { Effect } from 'umi';
import modelExtend from 'dva-model-extend';
import type { Reducer } from 'redux';
import { v1 as uuidv1 } from 'uuid';
import { model } from '@/utils/frame/model';
import type {
  IFrameData,
  IPcapInfo,
  IIntervalData,
  IProtocolTree,
  ICustomStatTapData,
  ITapResponseData,
  IFollowData,
  IFilterCompleteResult,
} from '@/pages/app/PktAnalysis/typings';
import { downloadFlowFile, queryPacketAnalysis } from '@/pages/app/PktAnalysis/service';

export const FRAME_LIMIT = 1000;

export interface IPktAnalysisModelState {
  /** PCAP文件信息 */
  info: IPcapInfo;

  /** 统计曲线时间间隔，ms */
  intervalTime: number;
  /** pcap文件所有的统计信息 */
  intervals: IIntervalData;
  /** 根据过滤条件搜索出来的统计信息 */
  filterIntervals: IIntervalData;

  /** 帧列表数据 */
  frameList: IFrameData[];
  /** 帧列表分页信息 */
  currentPage: number;
  /** 协议树数据 */
  decodeTree: IProtocolTree;
  /** TAP统计数据 */
  taps: ITapResponseData[];

  /** TAP流量数据 */
  follow: IFollowData;

  /** 过滤条件，但是不包括时间段 */
  filter?: string; //
  /** 图表过滤的相对开始时间 */
  startRelativeTime?: number;
  /** 图表过滤的相对截止时间 */
  endRelativeTime?: number;

  /** 统计相关数据 */
  currentTap: ICustomStatTapData;
  /** 统计弹出框是否显示 */
  statModalVisible: boolean;
}

export interface ModelType {
  namespace: string;
  state: IPktAnalysisModelState;
  effects: {
    /** 查询正在分析 PCAP 文件的信息 */
    queryPacketStatus: Effect;
    /** 查询按照时间分析的曲线图数据 */
    queryPacketInterval: Effect;
    /** 查询帧列表数据 */
    queryFrameList: Effect;
    /** 查询协议树数据 */
    queryProtocolTree: Effect;
    /** 清空协议树数据 */
    clearProtocolTree: Effect;
    /** 获取自动补全的条件 */
    filterComplete: Effect;
    /** 检查过滤条件是否合法 */
    filterCheck: Effect;
    /** 查询统计数据 */
    queryStatTap: Effect;
    /** 获取流量原始数据 */
    queryStatFollow: Effect;
    /** 下载流量中的文件 */
    downloadFlowFile: Effect;
  };
  reducers: {
    changeFilter: Reducer<IPktAnalysisModelState>;
    changeRelativeTime: Reducer<IPktAnalysisModelState>;
    changeCurrentTap: Reducer<IPktAnalysisModelState>;
    changeModalVisible: Reducer<IPktAnalysisModelState>;
  };
}

/**
 * 解析json对象
 * @param success 结果返回成功还是失败状态
 * @param result json文本
 */
const parseJsonObject = (success: boolean, result: string) => {
  let r = {};
  try {
    r = success && result ? JSON.parse(result) : {};
  } catch (error) {
    r = {};
  }
  return r;
};

const Model = modelExtend(model, {
  namespace: 'pktAnalysisModel',
  state: {
    /** PCAP文件信息 */
    info: {} as IPcapInfo,

    /** 统计曲线时间间隔，ms */
    intervalTime: 1000,
    /** pcap文件所有的统计信息 */
    intervals: <IIntervalData>{},
    /** 根据过滤条件搜索出来的统计信息 */
    filterIntervals: <IIntervalData>{},

    /** 帧列表数据 */
    frameList: [] as IFrameData[],
    /** 帧列表分页信息 */
    currentPage: 0,

    /** 协议树数据 */
    decodeTree: <IProtocolTree>{},

    /** TAP统计数据 */
    taps: [],
    /**  */
    follow: {} as IFollowData,

    filter: '', // 搜索条件
    startRelativeTime: 0, // 图表过滤开始时间
    endRelativeTime: undefined, // 图表过滤结束时间

    currentTap: {} as ICustomStatTapData,
    statModalVisible: false,
  },

  effects: {
    /**
     * 获取PCAP文件信息
     */
    *queryPacketStatus({ payload = {} }, { call, put }) {
      yield put({
        type: 'updateState',
        payload: {
          startRelativeTime: 0, // 图表过滤开始时间
          endRelativeTime: undefined, // 图表过滤结束时间

          intervals: {},
          filterIntervals: {},

          info: {},

          frameList: [],
          decodeTree: {},
        },
      });

      const { success, result } = yield call(queryPacketAnalysis, { ...payload, req: 'status' });
      const info = parseJsonObject(success, result) as IPcapInfo;
      // 根据PCAP文件包的时间跨度大小，判断时间间隔的大小
      let intervalTime = 1000; // ms
      const displayCount = 200; // 展示200个点
      const { duration } = info;
      if (duration && typeof duration === 'number') {
        const quotient = duration / displayCount;
        if (quotient > 1) {
          intervalTime = Math.ceil(quotient) * 1000;
        }
      }

      yield put({
        type: 'updateState',
        payload: {
          info,
          intervalTime,
        },
      });
      return success;
    },
    /**
     * 获取PCAP文件信息
     */
    *queryPacketInterval({ payload }, { call, put }) {
      const { filter } = payload;
      const { success, result } = yield call(queryPacketAnalysis, { ...payload, req: 'intervals' });

      const r = parseJsonObject(success, result);

      // 如果过滤条件中存在和时间的过滤条件，只更新过滤后的统计数据
      if (filter) {
        yield put({
          type: 'updateState',
          payload: {
            filterIntervals: r,
          },
        });
      } else {
        yield put({
          type: 'updateState',
          payload: {
            intervals: r,
            filterIntervals: r,
          },
        });
      }
    },
    /**
     * 获取包列表
     */
    *queryFrameList({ payload }, { call, put, select }) {
      yield put({
        type: 'updateState',
        payload: {
          frameList: [],
          decodeTree: {},
        },
      });

      const { success, result } = yield call(queryPacketAnalysis, {
        ...payload,
        req: 'frames',
        limit: FRAME_LIMIT,
      });

      const { intervalTime } = yield select((state: any) => state.pktAnalysisModel);
      yield put({
        type: 'queryPacketInterval',
        payload: {
          ...payload,
          interval: intervalTime,
        },
      });

      let frameList: IFrameData[] = [];
      try {
        frameList = success && result ? JSON.parse(result) : [];
      } catch (error) {
        frameList = [];
      }

      yield put({
        type: 'updateState',
        payload: {
          frameList: frameList.map((item) => ({ id: uuidv1(), ...item })),
          currentPage: payload.skip / FRAME_LIMIT + 1,
        },
      });

      return frameList;
    },
    /**
     * 获取协议树
     */
    *queryProtocolTree({ payload }, { call, put }) {
      const { success, result } = yield call(queryPacketAnalysis, {
        ...payload,
        req: 'frame',
        bytes: 'yes',
        proto: 'yes',
      });

      const r = parseJsonObject(success, result);

      yield put({
        type: 'updateState',
        payload: {
          decodeTree: r,
        },
      });
    },
    *clearProtocolTree(_, { put }) {
      yield put({
        type: 'updateState',
        payload: {
          decodeTree: {},
        },
      });
    },
    /**
     * 搜索条件的自动提示
     */
    *filterComplete({ payload }, { call }) {
      const { success, result } = yield call(queryPacketAnalysis, payload);
      // 根据字符串顺序排序
      let field = [] as IFilterCompleteResult[];

      if (!success) {
        return [];
      }

      try {
        const r = JSON.parse(result);
        if (r.field && Array.isArray(r.field)) {
          field = r.field;
        }
      } catch (error) {
        field = [];
      }
      field.sort((a, b) => (a.f < b.f ? -1 : 1));
      return field;
    },
    /**
     * 检查搜索条件是否正确
     */
    *filterCheck({ payload }, { call }) {
      const { success, result } = yield call(queryPacketAnalysis, payload);

      if (!success) {
        return false;
      }

      let checkFlag = false;
      try {
        const r = JSON.parse(result);
        checkFlag = r.filter === 'ok';
      } catch (error) {
        checkFlag = false;
      }

      return checkFlag;
    },
    *queryStatTap({ payload }, { call, put }) {
      const { success, result } = yield call(queryPacketAnalysis, { ...payload });

      let taps = [];

      if (success) {
        try {
          const r = JSON.parse(result);
          if (r.taps && Array.isArray(r.taps)) {
            taps = r.taps;
          }
        } catch (error) {
          taps = [];
        }
      }

      yield put({
        type: 'updateState',
        payload: {
          taps,
        },
      });
    },
    *queryStatFollow({ payload }, { call, put }) {
      const { success, result } = yield call(queryPacketAnalysis, { ...payload });

      const f = parseJsonObject(success, result);

      yield put({
        type: 'updateState',
        payload: {
          follow: f,
        },
      });
    },

    *downloadFlowFile({ payload }, { call }) {
      yield call(downloadFlowFile, payload);
    },
  },

  reducers: {
    changeFilter(state, action) {
      return {
        ...(state as IPktAnalysisModelState),
        filter: action.payload.filter || '',
      };
    },
    changeRelativeTime(state, action) {
      return {
        ...(state as IPktAnalysisModelState),
        startRelativeTime: action.payload.startRelativeTime,
        endRelativeTime: action.payload.endRelativeTime,
      };
    },
    changeCurrentTap(state, action) {
      return {
        ...(state as IPktAnalysisModelState),
        currentTap: action.payload.currentTap || {},
      };
    },
    changeModalVisible(state, action) {
      return {
        ...(state as IPktAnalysisModelState),
        statModalVisible: action.payload.visible,
      };
    },
  },
} as ModelType);

export default Model;
