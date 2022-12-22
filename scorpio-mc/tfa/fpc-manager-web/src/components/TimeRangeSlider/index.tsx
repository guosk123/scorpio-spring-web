import type { AppModelState } from '@/models/app';
import { ERealTimeStatisticsFlag, getDefaultTime } from '@/models/app';
import type { ConnectState } from '@/models/connect';
import {
  DoubleLeftOutlined,
  DoubleRightOutlined,
  EyeFilled,
  EyeInvisibleOutlined,
  StepBackwardOutlined,
  StepForwardOutlined,
} from '@ant-design/icons';
import { Divider, message, Modal, Space, Spin, Tooltip } from 'antd';
import { connect } from 'dva';
import lodash from 'lodash';
import moment from 'moment';
import pathToRegexp from 'path-to-regexp';
import React, { useEffect, useMemo } from 'react';
import type { Dispatch } from 'umi';
import { useLocation } from 'umi';
import type { IGlobalTime } from '../GlobalTimeSelector';
import GlobalTimeSelector, {
  ETimeType,
  getGlobalTime,
  globalTimeFormatText,
} from '../GlobalTimeSelector';
import useAutoSave from '../GlobalTimeSelector/components/useAutoSave';
import PlayButton, { refreshFlagFn } from '../PlayButton';
import SaveTimeInfo from './components/SaveTimeInfo';
import styles from './index.less';

/**
 * 计算时间
 * @param oldTime 老时间
 * @param span 加/减的秒数（可正、可负）
 * @returns 新时间的时间戳
 */
const manipulateTime = (oldTime: string, span: number): number => {
  return moment(moment(oldTime).add(span, 'seconds').format(globalTimeFormatText)).valueOf();
};

/**
 * 时间转换
 * @param seconds 秒数
 * @return x 分钟/x 小时/x 天
 */
const formatSeconds = (seconds: number): string => {
  return moment.duration(seconds, 'seconds').humanize().replace(' ', '');
};

interface ITimeRangeSliderProps {
  dispatch: Dispatch;
  globalTime: AppModelState['globalTime'];
  realTimeStatisticsFlag: AppModelState['realTimeStatisticsFlag'];
  isRefreshFlag: boolean;
}
const TimeRangeSlider: React.FC<ITimeRangeSliderProps> = ({
  dispatch,
  globalTime,
  realTimeStatisticsFlag,
  isRefreshFlag,
}) => {
  const { pathname } = useLocation();
  /** 计算时间周期范围 */
  const computedTimeRange = useMemo(() => {
    const { originStartTime, originEndTime, interval = 60 } = getGlobalTime(globalTime);
    // 起止时间的总时间跨度(s)
    const spanSeconds = moment(originEndTime).diff(moment(originStartTime), 'seconds');
    return {
      step: interval,
      stepText: formatSeconds(interval),

      /** 上一个时间刻度的时间范围 */
      prevStepTimeRange: [
        manipulateTime(originStartTime!, -interval),
        manipulateTime(originEndTime!, -interval),
      ],
      /** 上一个时间跨度的时间范围 */
      prevPeriodTimeRange: [
        manipulateTime(originEndTime!, -2 * spanSeconds),
        new Date(originStartTime!).valueOf(),
      ],
      /** 下一个时间刻度的时间范围 */
      nextStepTimeRange: [
        manipulateTime(originStartTime!, interval),
        manipulateTime(originEndTime!, interval),
      ],
      /** 下一个时间跨度的时间范围 */
      nextPeriodTimeRange: [
        new Date(originEndTime!).valueOf(),
        manipulateTime(originStartTime!, 2 * spanSeconds),
      ],
      /** 时间轴总体的时间跨度（s） */
      totalSpanSeconds: spanSeconds,
      /** 时间轴总体的时间跨度显示标签 */
      totalSpanSecondsText: formatSeconds(spanSeconds),
    };
  }, [globalTime]);

  /** 更新时间轴中的时间段 */
  const updateGlobalTime = (times: number[]) => {
    const time1 = moment(times[0]).format(globalTimeFormatText);
    const time2 = moment(times[1]).format(globalTimeFormatText);
    // 组装时间段参数
    const params: IGlobalTime = {
      relative: false,
      type: ETimeType.CUSTOM,
      custom: [moment(time1), moment(time2)],
    };
    refreshFlagFn(true, dispatch);
    dispatch({
      type: 'appModel/updateGlobalTime',
      payload: getGlobalTime(params),
    });
  };

  /** 后退1个刻度 */
  const handlePrevStep = () => {
    updateGlobalTime(computedTimeRange.prevStepTimeRange);
  };
  /** 上个时间周期 */
  const handlePrevPeriod = () => {
    updateGlobalTime(computedTimeRange.prevPeriodTimeRange);
  };

  /** 前进1个刻度 */
  const handleNextStep = () => {
    // 比较下截止时间是否超过了当前时间
    if (moment().valueOf() < computedTimeRange.nextStepTimeRange[1]) {
      message.warning('已是最新时间');
      return;
    }
    updateGlobalTime(computedTimeRange.nextStepTimeRange);
  };
  /** 下个时间周期 */
  const handleNextPeriod = () => {
    // 比较下截止时间是否超过了当前时间
    if (moment().valueOf() >= computedTimeRange.nextPeriodTimeRange[1]) {
      updateGlobalTime(computedTimeRange.nextPeriodTimeRange);
      return;
    }

    Modal.confirm({
      content: '截止时间已超出当前时间，是否要重置为最近30分钟？',
      onOk() {
        dispatch({
          type: 'appModel/updateGlobalTime',
          payload: getDefaultTime(),
        });
      },
    });
  };

  // 固定的页面下才出现实时统计开关
  const showRealTimeDom = useMemo(() => {
    return (
      // 网络统计
      pathToRegexp(`(/embed)?/analysis/performance/network/:networkId/dashboard`).test(`${pathname}/`) ||
      pathToRegexp(`(/embed)?/analysis/performance/network/:networkId/payload`).test(`${pathname}/`) ||
      pathToRegexp(`(/embed)?/analysis/performance/network/:networkId/performance`).test(`${pathname}/`) ||
      pathToRegexp(`(/embed)?/analysis/performance/network/:networkId/tcp/stats`).test(`${pathname}/`) ||
      // 业务统计
      pathToRegexp(`(/embed)?/analysis/performance/service/:serviceId/:networkId/dashboard`).test(
        `${pathname}/`,
      ) ||
      pathToRegexp(`(/embed)?/analysis/performance/service/:serviceId/:networkId/payload`).test(
        `${pathname}/`,
      ) ||
      pathToRegexp(`(/embed)?/analysis/performance/service/:serviceId/:networkId/performance`).test(
        `${pathname}/`,
      ) ||
      pathToRegexp(`(/embed)?/analysis/performance/service/:serviceId/:networkId/tcp/stats`).test(
        `${pathname}/`,
      ) ||
      // 流数据分析
      pathToRegexp(`(/embed)?/analysis/netflow/device/list`).test(`${pathname}/`)
    );
  }, [pathname]);

  useEffect(() => {
    if (showRealTimeDom) {
      refreshFlagFn(true, dispatch);
    }
  }, [dispatch, showRealTimeDom]);

  useAutoSave();

  /** 更新实时统计开关 */
  const handleRealTimeFlagChange = lodash.debounce(() => {
    dispatch({
      type: 'appModel/updateRealTimeStatisticsFlag',
      payload: {
        realTimeStatisticsFlag:
          realTimeStatisticsFlag === ERealTimeStatisticsFlag.OPEN
            ? ERealTimeStatisticsFlag.CLOSED
            : ERealTimeStatisticsFlag.OPEN,
      },
    });
    refreshFlagFn(true, dispatch);
  }, 200);

  useEffect(() => {
    if (!showRealTimeDom && realTimeStatisticsFlag === ERealTimeStatisticsFlag.OPEN) {
      handleRealTimeFlagChange();
    }
  }, [showRealTimeDom, realTimeStatisticsFlag, handleRealTimeFlagChange]);

  // 切换时关闭实时刷新
  useEffect(() => {
    return () => {
      return dispatch({
        type: 'appModel/updateRealTimeStatisticsFlag',
        payload: {
          realTimeStatisticsFlag: ERealTimeStatisticsFlag.CLOSED,
        },
      });
    };
  }, [dispatch]);

  return (
    <div className={styles.wrap}>
      <Spin indicator={<span />} spinning={realTimeStatisticsFlag === ERealTimeStatisticsFlag.OPEN}>
        <div className={styles.timeSliderWrap}>
          {/* 后退时间 */}
          <div className={styles.actionWrap}>
            <Space>
              <span className={styles.actionItem} onClick={handlePrevPeriod}>
                <Tooltip title={`后退${computedTimeRange.totalSpanSecondsText}`}>
                  <StepBackwardOutlined />
                </Tooltip>
              </span>
              <span className={styles.actionItem} onClick={handlePrevStep}>
                <Tooltip title={`后退${computedTimeRange.stepText}`}>
                  <DoubleLeftOutlined />
                </Tooltip>
              </span>
            </Space>
          </div>
          {/* 时间轴 */}
          <div className={styles.sliderWrap}>
            {/* 时间范围选择器 */}
            <GlobalTimeSelector
              onSubmit={() => {
                refreshFlagFn(true, dispatch);
              }}
            />
          </div>
          {/* 前进时间 */}
          <div className={styles.actionWrap}>
            <Space>
              <span className={styles.actionItem} onClick={handleNextStep}>
                <Tooltip title={`向前${computedTimeRange.stepText}`}>
                  <DoubleRightOutlined />
                </Tooltip>
              </span>
              <span className={styles.actionItem} onClick={handleNextPeriod}>
                <Tooltip title={`向前${computedTimeRange.totalSpanSecondsText}`}>
                  <StepForwardOutlined />
                </Tooltip>
              </span>
            </Space>
          </div>
        </div>
      </Spin>
      <div className={styles.actionWrap}>
        {showRealTimeDom && (
          <>
            <Divider type="vertical" />
            {/* 是否实时刷新 */}
            <span className={styles.actionItem} onClick={handleRealTimeFlagChange}>
              <Tooltip
                title={`${
                  realTimeStatisticsFlag === ERealTimeStatisticsFlag.OPEN ? '关闭' : '开启'
                }实时刷新`}
              >
                {realTimeStatisticsFlag === ERealTimeStatisticsFlag.OPEN ? (
                  <EyeFilled style={{ color: '#198ce1' }} />
                ) : (
                  <EyeInvisibleOutlined />
                )}
              </Tooltip>
            </span>
          </>
        )}
      </div>
      {/* 不展示UI，负责监听路由变化 */}
      <div className={styles.actionWrap} style={{ height: 24, margin: '0 10px' }}>
        {showRealTimeDom && (
          <PlayButton
            refreshFlag={[
              isRefreshFlag,
              (flag: boolean) => {
                refreshFlagFn(flag, dispatch);
              },
            ]}
          />
        )}
      </div>
      <Divider type="vertical" />
      <SaveTimeInfo />
    </div>
  );
};

export default connect(
  ({
    appModel: { globalTime, globalSelectedTime, realTimeStatisticsFlag },
    npmdModel: { isRefreshFlag },
  }: ConnectState) => ({
    globalTime,
    globalSelectedTime,
    realTimeStatisticsFlag,
    isRefreshFlag,
  }),
)(TimeRangeSlider);
