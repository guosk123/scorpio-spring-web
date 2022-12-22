import type { IAjaxResponseFactory } from '@/common/typings';
import type { ETimeUnit, IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType, getGlobalTime, relativeTime, timeUnit } from '@/components/GlobalTimeSelector';
import { queryRuntimeEnvironments } from '@/services/frame/global';
import { formatDuration } from '@/utils/utils';
import moment from 'moment';
import momentTZ from 'moment-timezone';
import type { Dispatch, Effect, History, Reducer } from 'umi';
import { getDvaApp, history } from 'umi';
import type { ILocation } from '../connect';

const oldMomentNowFn = moment.now;

type Subscription = (valus: { dispatch: Dispatch; history: History }) => void;

export interface IGlobalSelectedTime extends IGlobalTime {}

/** 实时统计开关 */
export enum ERealTimeStatisticsFlag {
  'OPEN' = '1',
  'CLOSED' = '0',
}

export interface IUriTimeQuery {
  relative: string;
  timeType: string;
  // 支持 UTC 时间和时间戳
  from?: string;
  to?: string;
  unit?: string;
}

export interface ISystemRuntime {
  /** 索引分区使用率 */
  indexFsUsedRatio: number;
  /** 元数据分区使用率 */
  metadataFsUsedRatio: number;
  /** 系统分区使用率 */
  systemFsUsedRatio: number;
  /** 服务器当前时间信息 */
  systemTime: { dateTime: string; timeZone: string };
  /** 服务器启动时间（s） */
  uptime: number;
}

export interface AppModelState {
  /** 时间轴的全部时间跨度 */
  globalTime: Required<IGlobalTime>;
  /** 被选中的时间 */
  globalSelectedTime: Required<IGlobalTime>;
  /** 实时统计标志 */
  realTimeStatisticsFlag: ERealTimeStatisticsFlag;

  /** 服务器运行状态 */
  systemRuntime: ISystemRuntime;
  /** 服务器时间 */
  systemTime?: string;
  /** 服务器启动时间 */
  systemUptimeText: string;
}

export interface AppModelType {
  namespace: string;
  state: AppModelState;
  effects: {
    /** 获取菜单的前置数据 */
    queryRouterPreData: Effect;
    /** 更新全局时间 */
    updateGlobalTime: Effect;
    /** 更新实时统计开关 */
    updateRealTimeStatisticsFlag: Effect;
    queryRuntimeEnvironments: Effect;
  };
  reducers: {
    updateState: Reducer<AppModelState>;
    /** 更新全局被选中的时间范围 */
    updateGlobalSelectedTime: Reducer<AppModelState>;
  };
  subscriptions: {
    setup: Subscription;
  };
}

export const getDefaultTime = () =>
  getGlobalTime({
    type: ETimeType.DEFAULT30M,
    relative: false,
  });

const AppModel: AppModelType = {
  namespace: 'appModel',
  state: {
    globalTime: getDefaultTime(),
    globalSelectedTime: getDefaultTime(),
    realTimeStatisticsFlag: ERealTimeStatisticsFlag.CLOSED,

    systemRuntime: {} as ISystemRuntime,
    systemTime: undefined,
    systemUptimeText: '--',
  },

  effects: {
    *queryRouterPreData(_params, { call }) {
      yield call('networkModel/queryNetworkSensorTree');
    },

    *updateGlobalTime({ payload }, { put }) {
      yield put({
        type: 'updateState',
        payload: {
          globalTime: { ...payload },
        },
      });
      yield put({
        type: 'updateState',
        payload: {
          globalSelectedTime: { ...payload },
        },
      });

      const time = { ...payload } as Required<IGlobalTime>;
      const params = history.location.query;
      const timeParams = {
        relative: time.relative.toString(),
        timeType: time.type,
      } as IUriTimeQuery;

      // 如果时绝对时间， 时间类型修改为ETimeType.CUSTOM
      if (!time.relative) {
        timeParams.timeType = ETimeType.CUSTOM;
      }

      if (timeParams.timeType === ETimeType.CUSTOM) {
        if (params?.unit) {
          delete params.unit;
        }
        timeParams.from = time.startTimestamp.toString();
        timeParams.to = time.endTimestamp.toString();
      } else if (timeParams.timeType === ETimeType.RANGE) {
        if (params?.to) {
          delete params.to;
        }
        timeParams.from = time.last.range.toString();
        timeParams.unit = time.last.unit;
      } else {
        delete params?.from;
        delete params?.to;
      }

      // TODO: 只保留有必要的时间参数，而不是全部合并
      const newQueryObj = {
        ...params,
        ...timeParams,
      };
      history.replace({
        query: newQueryObj,
      });
    },

    *updateRealTimeStatisticsFlag({ payload }, { put }) {
      yield put({
        type: 'updateState',
        payload: {
          realTimeStatisticsFlag: payload.realTimeStatisticsFlag,
        },
      });
    },
    *queryRuntimeEnvironments({ payload }, { put, call }) {
      const { success, result }: IAjaxResponseFactory<ISystemRuntime> = yield call(
        queryRuntimeEnvironments,
        payload,
      );
      // 启动时间
      let uptimeText = '--';
      if (success && result.uptime > 0) {
        uptimeText = formatDuration(result.uptime * 1000);
      }
      // 服务器当前时间
      let systemTime: string | undefined;
      if (success && result.systemTime?.dateTime && result.systemTime?.timeZone) {
        systemTime = momentTZ.tz(result.systemTime.dateTime, result.systemTime.timeZone).format();
      }

      const data = {
        systemRuntime: success ? result : {},
        systemUptimeText: uptimeText,
        systemTime,
      };

      if (!systemTime) {
        // console.error('同步服务器时间异常');
        // 同步时间异常时清空上次的时间信息
        window.systemTime = undefined;
        moment.now = oldMomentNowFn;
        window.clientTimeDiffSystemTimeSeconds = 0;
      } else {
        window.systemTime = systemTime;

        // 由于非活动 Tab 页面下的定时去会被浏览器设置为空闲状态
        // 所以采用新的方案来自动计时
        // 计算服务器时间和客户端时间的差
        window.clientTimeDiffSystemTimeSeconds = moment(new Date()).diff(
          moment(systemTime),
          'seconds',
        );

        moment.now = () => {
          return new Date(window.systemTime!).valueOf();
        };
        // console.log('同步服务器时间成功', systemTime);
      }

      yield put({
        type: 'updateState',
        payload: data,
      });
    },
  },

  reducers: {
    updateState(state, { payload }) {
      return {
        ...state,
        ...payload,
      };
    },
    updateGlobalSelectedTime(state, { payload }) {
      return {
        ...state,
        globalSelectedTime: payload.globalSelectedTime,
      } as AppModelState;
    },
  },

  subscriptions: {
    async setup({ dispatch, history: listenHistory }) {
      listenHistory.listen((location) => {
        const { pathname, search } = location as ILocation;
        // 登录界面有参数的情况下清空参数
        if (pathname === '/login' && search !== '') {
          const openUrl = history.location.query?.openUrl;
          history.replace({ query: openUrl ? { openUrl } : {} });
        }
      });
      // 去当前登录人信息
      await dispatch({
        type: 'globalModel/queryCurrentUser',
      }).then(async (success: boolean) => {
        if (!success) {
          return;
        }
        // 先同步服务器时间
        await dispatch({
          type: 'queryRuntimeEnvironments',
        });

        let prevPathname = listenHistory.location.pathname;
        let preQuery = (listenHistory.location as ILocation).query;
        listenHistory.listen(async (location) => {
          const { query, pathname } = location as ILocation;
          if (prevPathname !== pathname) {
            const newQuery = {
              // TODO: 只暴露和时间相关的参数
              ...query,
              ...(preQuery.timeType ? { timeType: preQuery.timeType } : {}),
              ...(preQuery.from ? { from: preQuery.from } : {}),
              ...(preQuery.to ? { to: preQuery.to } : {}),
              ...(preQuery.unit ? { unit: preQuery.unit } : {}),
              ...(preQuery.relative ? { relative: preQuery.relative } : {}),
            };
            preQuery = newQuery;
            prevPathname = pathname;
            listenHistory.replace({
              query: newQuery,
            });
            return;
          }
          preQuery = query;
          prevPathname = pathname;

          // 从url获取时间
          const { timeType, from, to, unit, relative } = query;

          // TODO: 是否有从 uri 中初始化实时刷新的标志
          // 初始化时间轴的整体的时间范围
          // ----------------

          const parsedRelative =
            (typeof relative === 'string' && relative === 'true') ||
            (typeof relative === 'boolean' && relative);

          let initGlobalTime: Required<IGlobalTime> | undefined;
          if (timeType) {
            // 自定义时间
            if (timeType === ETimeType.CUSTOM && from && to) {
              const startTime = moment(+from);
              const endTime = moment(+to);
              if (startTime.isValid() && endTime.isValid()) {
                initGlobalTime = getGlobalTime({
                  relative: parsedRelative,
                  type: timeType,
                  custom: [startTime, endTime],
                });
              }
            }
            // 自定义范围时间
            else if (timeType === ETimeType.RANGE) {
              if (from && unit) {
                // 校验 from 是否是数字
                // 校验 unit 是否在约定的范围内
                const rangeIsValid = !isNaN(+from);
                const unitInfo = timeUnit.find((item) => item.value === unit);
                if (rangeIsValid && unitInfo) {
                  initGlobalTime = getGlobalTime({
                    relative: parsedRelative,
                    type: timeType,
                    last: {
                      range: +from,
                      unit: unitInfo!.value as ETimeUnit,
                    },
                  });
                }
              }
            }
            // 默认的相对时间
            else {
              const timeTypeInfo = relativeTime.find((time) => time.type === timeType);
              if (timeTypeInfo) {
                initGlobalTime = getGlobalTime({
                  relative: parsedRelative,
                  type: timeType,
                });
              }
            }
          }

          if (initGlobalTime) {
            // eslint-disable-next-line no-underscore-dangle
            const prevTime = (getDvaApp()._store.getState().appModel as AppModelState)
              ?.globalSelectedTime;

            if (
              prevTime.relative === initGlobalTime.relative &&
              prevTime.startTimestamp === initGlobalTime.startTimestamp &&
              prevTime.endTimestamp === initGlobalTime.endTimestamp
            ) {
              return;
            }

            const initPayload = {
              globalTime: initGlobalTime,
              globalSelectedTime: initGlobalTime,
            } as AppModelState;
            // eslint-disable-next-line no-underscore-dangle

            await dispatch({
              type: 'updateState',
              payload: initPayload,
            });
          }
        });
      });
    },
  },
};

export default AppModel;
