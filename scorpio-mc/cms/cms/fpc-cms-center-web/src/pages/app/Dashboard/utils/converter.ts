import moment from 'moment';

export type TimeAxisChartData = {
  timestamp: number | string;
} & Record<string, number>;

/** 获取缺失时间段 */
export const getMissingTimeArea = (lineData: TimeAxisChartData[], label = '数据缺失') => {
  if (lineData.length === 0 || lineData.length === 1) {
    return {
      itemStyle: {
        color: 'rgba(255, 173, 177, 0.4)',
      },
      data: [],
    };
  }
  const markDataList = [];
  for (let i = 0; i < lineData.length; i++) {
    const lineItem = lineData[i];
    let startTime;
    let endTime;
    if (i === 0) {
      // eslint-disable-next-line @typescript-eslint/no-unused-expressions
      (startTime = lineItem.timestamp), (endTime = lineData[i + 1].timestamp);
    } else if (i === lineData.length - 1) {
      // eslint-disable-next-line @typescript-eslint/no-unused-expressions
      (startTime = lineData[i - 1].timestamp), (endTime = lineItem.timestamp);
    } else {
      startTime = lineData[i - 1].timestamp;
      endTime = lineData[i + 1].timestamp;
    }
    const missingElem = [];
    for (const key in lineItem) {
      if (key === 'timestamp' || lineItem[key] !== undefined) {
        continue;
      } else {
        missingElem.push(key);
      }
    }
    if (missingElem.length > 0) {
      markDataList.push({
        startTime,
        endTime,
        elem: missingElem,
        label,
      });
    }
  }
  return markDataList;
};

/** 转换方法 */
export const lineConverter = (
  data: any[] = [],
  fpcDevices: any[],
  metric: string,
  serialNumberName = 'monitored_serial_number',
  /** 增加小时数 */
  addHours?: number,
  /** 时间度量 */
  timeMetric: string = 'metric_time',
  /** value处理 */
  valueFormatter?: (value: number) => number,
) => {
  const lineData: TimeAxisChartData[] = [];
  const lineDataMap = new Map();
  const labelset = new Set<string>();
  data.forEach((item) => {
    if (item[metric] !== undefined) {
      const serialNumber = item[serialNumberName];
      const label = fpcDevices.find((device) => device.serialNumber === serialNumber)?.name;
      labelset.add(label);
      const timestamp = addHours
        ? moment(item[timeMetric]).tz(`${addHours}`).valueOf()
        : moment(item[timeMetric]).format('YYYY-MM-DD HH:mm:ss').valueOf();
      if (!lineDataMap.get(timestamp)) {
        const pointData = {
          timestamp,
        };
        pointData[label || serialNumber] = valueFormatter
          ? valueFormatter(item[metric] * 1)
          : item[metric] * 1;
        lineDataMap.set(timestamp, pointData);
      } else {
        const mapItem = lineDataMap.get(timestamp);
        mapItem[label || serialNumber] = valueFormatter
          ? valueFormatter(item[metric] * 1)
          : item[metric] * 1;
        lineDataMap.set(timestamp, mapItem);
      }
    }
  });
  lineDataMap.forEach((value) => {
    const d = { timestamp: value.timestamp };
    labelset.forEach((label) => {
      d[label] = value[label];
    });
    lineData.push(d as TimeAxisChartData);
  });

  return lineData;
};
