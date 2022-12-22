import type { ConnectState } from '@/models/connect';
import { querySelfDefinedTime } from '@/pages/app/configuration/SelfDefinedTime/services';
import type { ICustomTime } from '@/pages/app/configuration/SelfDefinedTime/typings';
import { ECustomTimeType } from '@/pages/app/configuration/SelfDefinedTime/typings';
import * as dateMath from '@/utils/frame/datemath';
import { getCurrentTimeZone, parseArrayJson, timeFormatter } from '@/utils/utils';
import { CaretDownOutlined } from '@ant-design/icons';
import { useInterval } from 'ahooks';
import {
  Button,
  Col,
  DatePicker,
  Divider,
  Form,
  Input,
  InputNumber,
  Popover,
  Radio,
  Row,
  Select,
  Space,
  Tabs,
} from 'antd';
import type { RadioChangeEvent } from 'antd/lib/radio';
import { connect } from 'dva';
import type { Moment } from 'moment';
import moment from 'moment';
import pathToRegexp from 'path-to-regexp';
import React, { useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { ERealTimeStatisticsFlag, useLocation } from 'umi';
import HistoryTimeList from './components/HistoryTimeList';
import styles from './index.less';

const { RangePicker } = DatePicker;

// const _range = (start: number, end: number) => {
//   const res: number[] = [];
//   for (let i = start; i < end; i++) {
//     res.push(i);
//   }
//   return res;
// };

export const globalTimeFormatText = 'YYYY-MM-DD HH:mm:00';

export enum ETimeType {
  'DEFAULT30M' = 'default30m',
  'DEFAULT1H' = 'default1h',
  'DEFAULT6H' = 'default6h',
  'DEFAULT12H' = 'default12h',
  'DEFAULT1D' = 'default1d',
  'DEFAULT3D' = 'default3d',
  'DEFAULT5D' = 'default5d',
  'DEFAULT1W' = 'default1w',
  'RANGE' = 'range',
  'CUSTOM' = 'custom',
  'SAVED' = 'saved',
}

export enum ETimeUnit {
  'MINUTES' = 'm',
  'HOURS' = 'h',
  'DAYS' = 'd',
  'WEEKS' = 'w',
  'MONTHS' = 'M',
}

export const timeUnit = [
  {
    label: '分钟',
    value: 'm',
  },
  {
    label: '小时',
    value: 'h',
  },
  {
    label: '天',
    value: 'd',
  },
  {
    label: '周',
    value: 'w',
  },
  {
    label: '月',
    value: 'M',
  },
];

export const relativeTime = [
  {
    type: 'default30m',
    label: '最近30分钟',
    value: 'now-30m',
  },
  {
    type: 'default1h',
    label: '最近1小时',
    value: 'now-1h',
  },
  {
    type: 'default6h',
    label: '最近6小时',
    value: 'now-6h',
  },
  {
    type: 'default12h',
    label: '最近12小时',
    value: 'now-12h',
  },
  {
    type: 'default1d',
    label: '最近1天',
    value: 'now-1d',
  },
  {
    type: 'default3d',
    label: '最近3天',
    value: 'now-3d',
  },
  {
    type: 'default5d',
    label: '最近5天',
    value: 'now-5d',
  },
  {
    type: 'default1w',
    label: '最近1周',
    value: 'now-1w',
  },
];

/**
 * 获取格式化后的全局时间
 * @param globalTime
 */
export const getGlobalTime: (globalTime: IGlobalTime) => Required<IGlobalTime> = (globalTime) => {
  const { type, last, custom, relative, isOrigin } = globalTime;

  const formatter = (time: any) => moment(time, globalTimeFormatText).format();

  let from = '';
  let to = '';
  let timeLabel = '';
  const timeZone = getCurrentTimeZone();
  // range
  if (type === ETimeType.RANGE) {
    // const unitInfo = timeUnit.find((col) => col.value === unit);
    // 单位
    // const unitText = unitInfo!.label;
    const { range, unit } = last!;
    // const formText = `now-${range}${unit}`;
    // const toText = 'now';
    // 计算开始时间结束时间
    from = moment(moment().format(), globalTimeFormatText).add(-range, unit).format();
    to = moment(moment().format(), globalTimeFormatText).format();
    if (relative) {
      const unitText = timeUnit.find((unitMap) => {
        return unitMap.value === unit;
      })?.label;
      timeLabel = `最近${range}${unitText}`;
    } else {
      timeLabel = `${moment(from).format(globalTimeFormatText)} -  ${moment(to).format(
        globalTimeFormatText,
      )}(UTC${timeZone})`;
    }
  }

  // 自定义
  else if (type === ETimeType.CUSTOM) {
    from = formatter(custom![0].format());
    to = formatter(custom![1].format());

    // 获取当前时区
    timeLabel = `${moment(from).format(globalTimeFormatText)} - ${moment(to).format(
      globalTimeFormatText,
    )}(UTC${timeZone})`;
  }

  // 默认的相对时间
  else {
    const { value, label } = relativeTime.find((time) => time.type === type)!;
    timeLabel = '--';
    from = formatter(dateMath.parse(value).format());
    to = formatter(dateMath.parse('now').format());

    if (relative) {
      timeLabel = label;
    } else {
      timeLabel = `${moment(from).format(globalTimeFormatText)} - ${moment(to).format(
        globalTimeFormatText,
      )}(UTC${timeZone})`;
    }
  }

  const totalSeconds = (new Date(to).getTime() - new Date(from).getTime()) / 1000;

  const timeObj = timeFormatter(from, to)!;

  let startTime = from;
  let endTime = to;
  const interval: number = timeObj.interval;

  if (!isOrigin) {
    startTime = timeObj.startTime;
    endTime = timeObj.endTime;
  }

  let computedLast: IGlobalTime['last'] = last;
  if (type !== ETimeType.RANGE) {
    if (totalSeconds > 30 * 24 * 60 * 60) {
      computedLast = {
        unit: ETimeUnit.MONTHS,
        range: totalSeconds / (30 * 24 * 60 * 60),
      };
    } else if (totalSeconds > 7 * 24 * 60 * 60) {
      computedLast = {
        unit: ETimeUnit.WEEKS,
        range: totalSeconds / (7 * 24 * 60 * 60),
      };
    } else if (totalSeconds > 24 * 60 * 60) {
      computedLast = {
        unit: ETimeUnit.DAYS,
        range: totalSeconds / (24 * 60 * 60),
      };
    } else if (totalSeconds > 60 * 60) {
      computedLast = {
        unit: ETimeUnit.HOURS,
        range: totalSeconds / (60 * 60),
      };
    } else {
      computedLast = {
        unit: ETimeUnit.MINUTES,
        range: totalSeconds / 60,
      };
    }
  }

  return {
    ...globalTime,
    last: computedLast!,
    startTime,
    endTime,
    custom: [moment(from), moment(to)],
    originStartTime: from,
    originEndTime: to,
    interval,
    timeLabel,
    totalSeconds,
    startTimestamp: moment(from).valueOf(),
    endTimestamp: moment(to).valueOf(),
    isOrigin: !!isOrigin,
  };
};

/**
 * 全局时间
 */
export interface IGlobalTime {
  relative: boolean;
  /**
   * 类型
   */
  type: ETimeType;
  /**
   * 自定义相对时间范围
   *
   * @eg.
   * ```
   * {
   *   range: 30
   *   unit: 'm'
   * }
   * ```
   */
  last?: {
    range: number;
    unit: ETimeUnit;
  };
  /**
   * 自定义绝对时间范围
   *
   * @eg. ['2020-12-01T17:00:00+0800', '2020-12-01T17:40:00+0800']
   */
  custom?: [Moment, Moment];
  timeLabel?: string;
  /**
   * 格式化后的开始时间
   */
  startTime?: string;
  /**
   * 原始的开始时间
   */
  originStartTime?: string;
  /**
   * 格式化后的结束时间
   */
  endTime?: string;
  /**
   * 原始的结束时间
   */
  originEndTime?: string;
  /**
   * 时间间隔（s）
   */
  interval?: number;
  /**
   * 开始到结束经历的总秒数
   */
  totalSeconds?: number;
  /**
   * 开始时间戳
   */
  startTimestamp?: number;
  /**
   * 结束时间戳
   */
  endTimestamp?: number;
  /**
   * 是否不需要格式化成整五分钟，整小时
   */
  isOrigin?: boolean;
}

interface IGlobalTimeSelectorProps {
  dispatch: Dispatch;
  globalSelectedTime: Required<IGlobalTime>;
  realTimeStatisticsFlag: ERealTimeStatisticsFlag;
  onSubmit?: () => void;
  // 限制时间范围
  limit?: [number, number];
  // 历史时间
  history?: boolean;
}

const GlobalTimeSelector: React.FC<IGlobalTimeSelectorProps> = ({
  dispatch,
  limit,
  globalSelectedTime,
  realTimeStatisticsFlag,
  onSubmit,
  history = true,
}) => {
  const { pathname } = useLocation();

  const [form] = Form.useForm();
  // 时间选择器是否显示
  const [visible, setVisible] = useState<boolean>(false);
  const [relativeTimeType, setRelativeTimeType] = useState<ETimeType>(ETimeType.CUSTOM);
  // rang 类型时间的 时间范围
  const [timeRange, setTimeRange] = useState<IGlobalTime['last']>({
    range: 30,
    unit: ETimeUnit.MINUTES,
  });
  // 时间类型： 绝对或者相对
  const [isRelative, setIsRelative] = useState(false);
  // 保存的自定义时间
  const [savedTimeId, setSavedTimeId] = useState<string>();
  const [savedTimeList, setSavedTimeList] = useState<ICustomTime[]>([]);

  useEffect(() => {
    setIsRelative(globalSelectedTime.relative);
    if (globalSelectedTime.relative) {
      setRelativeTimeType(globalSelectedTime.type);
      setTimeRange(globalSelectedTime.last);
    }
    if (visible) {
      form.setFieldsValue({
        'time.custom': [
          moment(globalSelectedTime.startTimestamp),
          moment(globalSelectedTime.endTimestamp),
        ],
      });
    }
  }, [form, globalSelectedTime, visible]);

  useEffect(() => {
    querySelfDefinedTime({ type: ECustomTimeType.DisposableTime }).then((res) => {
      const { success, result } = res;
      if (success) {
        setSavedTimeList(result);
      }
    });
  }, [visible]);

  const handleVisibleChange = (nextVisible: boolean) => {
    setVisible(nextVisible);
  };

  const closePopover = () => {
    setVisible(false);
  };

  const handleRelativeTimeTypeChange = (e: RadioChangeEvent) => {
    const { value } = e.target;
    setRelativeTimeType(value);
  };

  const handleRelativeChange = (e: RadioChangeEvent) => {
    const isRelativeFlag = e.target.value;
    setIsRelative(isRelativeFlag);
    if (relativeTimeType === ETimeType.SAVED) {
      setRelativeTimeType(ETimeType.DEFAULT30M);
    }
  };

  // 绝对时间选择下， 选择最近xxx，需要修改时间范围，
  useEffect(() => {
    if (isRelative && relativeTimeType !== ETimeType.CUSTOM) {
      const timeObj = {
        relative: isRelative,
        type: relativeTimeType,
        last: timeRange,
      };
      const time = getGlobalTime(timeObj);
      form.setFieldsValue({
        'time.custom': time.custom,
      });
    }
  }, [form, isRelative, relativeTimeType, timeRange]);

  const updateTimeRange = (value: number) => {
    if (value) {
      setTimeRange((prev) => {
        return {
          ...prev!,
          range: value,
        };
      });
    }
    setRelativeTimeType(ETimeType.RANGE);
  };

  const updateTimeRangeUnit = (value: any) => {
    setTimeRange((prev) => {
      return {
        ...prev!,
        unit: value,
      };
    });
    setRelativeTimeType(ETimeType.RANGE);
  };

  // 判断是否在需要自动刷新的页面内
  const inRefreshPage = useMemo(() => {
    return (
      // 网络统计
      pathToRegexp(`(/embed)?/analysis/performance/network/:networkId/dashboard`).test(
        `${pathname}/`,
      ) ||
      pathToRegexp(`(/embed)?/analysis/performance/network/:networkId/payload`).test(
        `${pathname}/`,
      ) ||
      pathToRegexp(`(/embed)?/analysis/performance/network/:networkId/performance`).test(
        `${pathname}/`,
      ) ||
      pathToRegexp(`(/embed)?/analysis/performance/network/:networkId/tcp/stats`).test(
        `${pathname}/`,
      ) ||
      pathToRegexp(`(/embed)?/analysis/performance/network/:networkId/ip-graph`).test(
        `${pathname}/`,
      ) ||
      pathToRegexp(`(/embed)?/analysis/performance/network/:networkId/flow/(.*)`).test(
        `${pathname}/`,
      ) ||
      pathToRegexp(`(/embed)?/analysis/performance/network/list`).test(`${pathname}/`) ||
      pathToRegexp(`(/embed)?/analysis/performance/network/topology`).test(`${pathname}/`) ||
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
      pathToRegexp(`(/embed)?/analysis/performance/service/:serviceId/:networkId/flow/(.*)`).test(
        `${pathname}/`,
      ) ||
      pathToRegexp(`(/embed)?/analysis/performance/service/list`).test(`${pathname}/`) ||
      pathToRegexp(
        `(/embed)?/analysis/performance/service/:serviceId/:networkId/application-topology`,
      ).test(`${pathname}/`) ||
      // 流数据分析
      pathToRegexp(`(/embed)?/analysis/netflow/device/list`).test(`${pathname}/`) ||
      pathToRegexp(`(/embed)?/analysis/netflow/device/:deviceName/dashboard`).test(
        `${pathname}/`,
      ) ||
      pathToRegexp(`(/embed)?/analysis/netflow/device/:deviceName/flow/ip`).test(`${pathname}/`) ||
      pathToRegexp(`(/embed)?/analysis/netflow/device/:deviceName/flow/senderip`).test(
        `${pathname}/`,
      ) ||
      pathToRegexp(`(/embed)?/analysis/netflow/device/:deviceName/flow/receiverip`).test(
        `${pathname}/`,
      ) ||
      pathToRegexp(`(/embed)?/analysis/netflow/device/:deviceName/flow/ip-conversation`).test(
        `${pathname}/`,
      ) ||
      pathToRegexp(`(/embed)?/analysis/netflow/device/:deviceName/flow/port`).test(
        `${pathname}/`,
      ) ||
      pathToRegexp(`(/embed)?/analysis/netflow/device/:deviceName/flow-record`).test(
        `${pathname}/`,
      ) ||
      pathToRegexp(`(/embed)?/analysis/netflow/device/:deviceName/netif/:netidNo/dashboard`).test(
        `${pathname}/`,
      ) ||
      pathToRegexp(`(/embed)?/analysis/netflow/device/:deviceName/netif/:netidNo/flow/ip`).test(
        `${pathname}/`,
      ) ||
      pathToRegexp(
        `(/embed)?/analysis/netflow/device/:deviceName/netif/:netidNo/flow/senderip`,
      ).test(`${pathname}/`) ||
      pathToRegexp(
        `(/embed)?/analysis/netflow/device/:deviceName/netif/:netidNo/flow/receiverip`,
      ).test(`${pathname}/`) ||
      pathToRegexp(
        `(/embed)?/analysis/netflow/device/:deviceName/netif/:netidNo/flow/ip-conversation`,
      ).test(`${pathname}/`) ||
      pathToRegexp(`(/embed)?/analysis/netflow/device/:deviceName/netif/:netidNo/flow/port`).test(
        `${pathname}/`,
      ) ||
      pathToRegexp(`(/embed)?/analysis/netflow/device/:deviceName/netif/:netidNo/flow-record`).test(
        `${pathname}/`,
      )
    );
  }, [pathname]);

  const hasTimer = useMemo(() => {
    if (realTimeStatisticsFlag === ERealTimeStatisticsFlag.OPEN) return false;
    return inRefreshPage && isRelative && relativeTimeType !== ETimeType.CUSTOM;
  }, [inRefreshPage, isRelative, realTimeStatisticsFlag, relativeTimeType]);

  // 刷新回調函數
  const intervalCallback = () => {
    if (hasTimer) {
      const timeObj = {
        relative: isRelative,
        type: relativeTimeType,
        last: timeRange,
      };
      dispatch({
        type: 'appModel/updateGlobalTime',
        payload: getGlobalTime(timeObj),
      });
    }
  };

  // 自動刷新
  useInterval(
    () => {
      intervalCallback();
    },
    hasTimer ? 60 * 1000 : undefined,
  );

  const onFinish = (values: any) => {
    const { 'time.custom': timeCustom } = values;
    const timeObj: IGlobalTime = {
      relative: isRelative,
      type: relativeTimeType,
      last: timeRange,
    };

    if (timeObj.type === ETimeType.CUSTOM) {
      const time1 = moment(timeCustom[0].format(globalTimeFormatText));
      const time2 = moment(timeCustom[1].format(globalTimeFormatText));
      timeObj.custom = [moment(time1), moment(time2)];
    }

    if (timeObj.type === ETimeType.SAVED) {
      const saved = savedTimeList.find((item) => item.id === savedTimeId);
      if (saved) {
        const timeInfo = parseArrayJson(saved.customTimeSetting);
        if (timeInfo.length > 0) {
          const [{ start_time_1, end_time_1 }] = timeInfo;
          timeObj.custom = [moment(start_time_1), moment(end_time_1)];
          timeObj.type = ETimeType.CUSTOM;
        }
      }
    }

    dispatch({
      type: 'appModel/updateGlobalTime',
      payload: getGlobalTime(timeObj),
    });
    if (onSubmit) {
      onSubmit();
    }
    setVisible(false);
  };

  return (
    <Popover
      className={styles.timeSelector}
      placement="bottomLeft"
      title={undefined}
      visible={visible}
      trigger="click"
      destroyTooltipOnHide
      onVisibleChange={handleVisibleChange}
      getPopupContainer={(triggerNode) => triggerNode}
      content={
        <Tabs defaultActiveKey="1" size="small">
          <Tabs.TabPane tab="时间选择" key="timeSelect">
            <div className={styles.timePane}>
              <Form form={form} onFinish={onFinish}>
                <Row>
                  <Col span={24} className={styles.col}>
                    <div className={styles.rangePickerWrap}>
                      <Form.Item
                        style={{ marginBottom: 0 }}
                        name={'time.custom'}
                        // dependencies={['time.type']}
                        label={'时间范围'}
                        rules={[
                          () => ({
                            validator(rule, value: undefined | [Moment, Moment]) {
                              if (!isRelative && relativeTimeType === ETimeType.CUSTOM) {
                                if (!value || !value[0] || !value[1]) {
                                  return Promise.reject('请选择时间范围');
                                }
                                if (value[0].clone().add(1, 'm').isAfter(value[1])) {
                                  return Promise.reject('结束时间与开始时间差至少为1分钟');
                                }
                                if (
                                  value[0].format(globalTimeFormatText) ===
                                  value[1].format(globalTimeFormatText)
                                ) {
                                  return Promise.reject('开始时间和结束时间不能相同');
                                }
                                // if (value[1].isAfter(moment())) {
                                //   return Promise.reject('结束时间最晚为当前时刻');
                                // }
                              }
                              return Promise.resolve();
                            },
                          }),
                        ]}
                      >
                        <RangePicker
                          onFocus={() => {
                            setRelativeTimeType(ETimeType.CUSTOM);
                          }}
                          showNow
                          size="small"
                          showTime={{ format: 'HH:mm' }}
                          format="YYYY-MM-DD HH:mm"
                          disabledDate={
                            limit
                              ? (currentDate: Moment) => {
                                  const from = moment(limit[0]);
                                  const to = moment(limit[1]);
                                  if (currentDate.isBefore(from) || currentDate.isAfter(to)) {
                                    return true;
                                  }
                                  return false;
                                }
                              : () => {
                                  return false;
                                }
                          }
                          // disabledTime={(current) => {
                          //   const isToday = current?.get('day') === moment().get('day');
                          //   if (isToday) {
                          //     return {
                          //       disabledHours: () => _range(moment().get('hour') + 1, 24),
                          //       disabledMinutes: (hour) => {
                          //         if (hour === moment().get('hour')) {
                          //           return _range(moment().get('minute') + 1, 60);
                          //         }
                          //         return [];
                          //       },
                          //     };
                          //   }
                          //   return {};
                          // }}
                        />
                      </Form.Item>
                    </div>
                  </Col>
                </Row>
                {!limit && (
                  <Radio.Group onChange={handleRelativeTimeTypeChange} value={relativeTimeType}>
                    <Row>
                      {relativeTime.map(({ label, type }) => (
                        <Col className={styles.col} span={12} key={type}>
                          <Radio value={type}>{label}</Radio>
                        </Col>
                      ))}
                      <Col span={24} className={styles.col} style={{ display: 'flex' }}>
                        <Radio value={ETimeType.RANGE}>最近</Radio>
                        <Input.Group compact style={{ display: 'inline-block', width: 200 }}>
                          <InputNumber
                            size="small"
                            min={1}
                            precision={0}
                            max={100}
                            value={timeRange && timeRange.range}
                            style={{ width: 100 }}
                            onFocus={() => setRelativeTimeType(ETimeType.RANGE)}
                            onChange={updateTimeRange}
                          />

                          <Select
                            size="small"
                            value={timeRange && timeRange.unit}
                            style={{ width: 100 }}
                            onChange={updateTimeRangeUnit}
                            onFocus={() => setRelativeTimeType(ETimeType.RANGE)}
                          >
                            {timeUnit.map(({ label, value }) => (
                              <Select.Option key={value} value={value}>
                                {label}
                              </Select.Option>
                            ))}
                          </Select>
                        </Input.Group>
                      </Col>
                      {!isRelative && (
                        <Col span={24} className={styles.col} style={{ display: 'flex' }}>
                          <Radio value={ETimeType.SAVED}>已保存时间</Radio>
                          <Select
                            size={'small'}
                            style={{ width: 200 }}
                            showSearch
                            value={savedTimeId}
                            options={savedTimeList.map((item) => {
                              return {
                                label: item.name,
                                value: item.id,
                              };
                            })}
                            onChange={(value) => setSavedTimeId(value)}
                            onFocus={() => setRelativeTimeType(ETimeType.SAVED)}
                            filterOption={(input, option) =>
                              (option?.label as unknown as string)
                                .toLowerCase()
                                .includes(input.toLowerCase())
                            }
                          />
                        </Col>
                      )}
                    </Row>
                  </Radio.Group>
                )}
                <Divider dashed className={styles.divider} />
                <div className={styles.action}>
                  {!limit && (
                    <Radio.Group onChange={handleRelativeChange} value={isRelative}>
                      <Row>
                        <Col span={12}>
                          <Radio value={false}>绝对时间</Radio>
                        </Col>
                        <Col span={12}>
                          <Radio value={true}>相对时间</Radio>
                        </Col>
                      </Row>
                    </Radio.Group>
                  )}

                  <Space>
                    <Button size="small" onClick={() => closePopover()}>
                      取消
                    </Button>
                    <Button size="small" type="primary" htmlType="submit">
                      应用
                    </Button>
                  </Space>
                </div>
              </Form>
            </div>
          </Tabs.TabPane>
          {history ? (
            <Tabs.TabPane tab="历史时间" key="history">
              <HistoryTimeList
                onClick={() => {
                  setVisible(false);
                }}
              />
            </Tabs.TabPane>
          ) : (
            ''
          )}
        </Tabs>
      }
    >
      <span className={styles.timeSummary}>
        {globalSelectedTime.timeLabel} <CaretDownOutlined className={styles.icon} />
      </span>
    </Popover>
  );
};

export default connect(
  ({ appModel: { globalSelectedTime, realTimeStatisticsFlag } }: ConnectState) => ({
    globalSelectedTime,
    realTimeStatisticsFlag,
  }),
)(GlobalTimeSelector);
