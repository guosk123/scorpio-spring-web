import type { ILineApiData, INetflowDevice } from '../typing';
import type { TrendChartData } from '@/pages/app/analysis/components/AnalysisChart';
import moment from 'moment';

/** 转换参数参数定义 */
export interface ISearchParam {
  deviceName: string;
  alias: string;
  netifNo?: string;
  netifSpeed?: number;
}

/** 转换结果定义 */
export interface ISearchResult {
  value: string;
  label: string;
  children: [];
}

/** 格式转换 转换为折线图需要用到的数据 */
export function lineChartConverter(
  lineApiData: ILineApiData[] = [],
  titleName?: string,
  valueName?: string,
  // 名称格式化函数
  nameFormatter?: (name: string, originMap: Map<string, any>) => string,
): Record<string, any> {
  const lineDataMap = new Map();
  const lineDataResult: Record<string, TrendChartData> = {}; // 返回的结果
  const originMap = new Map();
  for (let i = 0; i < lineApiData.length; i += 1) {
    const lineData = lineApiData[i];
    const mapItem = lineDataMap.get(titleName ? lineData[titleName] : lineData.ipAddress);
    if (mapItem === undefined) {
      // 加入map
      lineDataMap.set(titleName ? lineData[titleName] : lineData.ipAddress, [
        [
          moment(lineData.timeStamp).utcOffset(8).valueOf(),
          valueName ? lineData[valueName] : lineData.totalBytes,
        ],
      ]);
      originMap.set(titleName ? lineData[titleName] : lineData.ipAddress, lineData);
    } else {
      // 说明该type已经加入到map，需要在末尾补充添加时间
      lineDataMap.set(titleName ? lineData[titleName] : lineData.ipAddress, [
        ...mapItem,
        [
          moment(lineData.timeStamp).utcOffset(8).valueOf(),
          valueName ? lineData[valueName] : lineData.totalBytes,
        ],
      ]);
    }
  }
  lineDataMap.forEach((data, name) => {
    if (name) {
      if (nameFormatter) {
        const newName = nameFormatter(name, originMap);
        lineDataResult[newName] = data;
      } else {
        lineDataResult[name] = data;
      }
    }
  });

  return lineDataResult;
}

/** 格式转换 转换为柱状图需要用到的数据 */
export function barChartConverter(
  barApiData: ILineApiData[] = [],
  titleName?: string,
  valueName?: string,
  nameFormatter?: (name: string, originMap: Map<string, any>) => string,
): Record<string, any> {
  const barDataMap = new Map();
  const barDataResult: Record<string, TrendChartData> = {}; // 返回的结果
  const originMap = new Map();
  for (let i = 0; i < barApiData.length; i += 1) {
    const barData = barApiData[i];
    const mapItem = barDataMap.get(titleName ? barData[titleName] : barData.ipAddress);
    if (mapItem === undefined) {
      // 加入map
      barDataMap.set(
        titleName ? barData[titleName] : barData.ipAddress,
        valueName ? barData[valueName] : barData.totalBytes,
      );
      originMap.set(titleName ? barData[titleName] : barData.ipAddress, barData);
    } else {
      // 说明该type已经加入到map，需要在末尾补充添加时间
      barDataMap.set(
        titleName ? barData[titleName] : barData.ipAddress,
        mapItem + (valueName ? barData[valueName] : barData.totalBytes),
      );
    }
  }

  barDataMap.forEach((data, name) => {
    if (name) {
      if (nameFormatter) {
        barDataResult[nameFormatter(name, originMap)] = data;
      } else {
        barDataResult[name] = data;
      }
    }
  });

  return barDataResult;
}

/** 排序器 */
function bubbleSorter(dataList: any[], sortDirection: string, sortProperty: string) {
  // 拷贝一份dataList
  const array = [...dataList];
  for (let i = 0; i < array.length; i += 1) {
    // 用来提前停止
    let isExchange = false;
    for (let j = 0; j < array.length - i - 1; j += 1) {
      if (sortDirection === 'desc') {
        if (array[j][sortProperty] < array[j + 1][sortProperty]) {
          const temp = array[j];
          array[j] = array[j + 1];
          array[j + 1] = temp;
          isExchange = true;
        }
      } else if (sortDirection === 'asc') {
        if (array[j][sortProperty] > array[j + 1][sortProperty]) {
          const temp = array[j];
          array[j] = array[j + 1];
          array[j + 1] = temp;
          isExchange = true;
        }
      }
    }
    if (!isExchange) {
      break;
    }
  }
  return array;
}

/* netflow源 设备列表组转换器 */
export function sourceConverter(
  devices: INetflowDevice[],
  sortDirection: string = 'asc',
  sortProperty: string = 'netifNo',
) {
  const deviceList: INetflowDevice[] = devices;
  for (let i = 0; i < deviceList.length; i += 1) {
    const device = deviceList[i];
    if (device && device.netif) {
      deviceList[i].children = bubbleSorter(device.netif, sortDirection, sortProperty);
    }
  }
  return deviceList;
}

/** 模糊搜索格式转换 */
export function searchConverter(data: ISearchParam[]) {
  // 拷贝一份data
  const dataList = [...data];
  const sourceMap = new Map();
  const resultList: ISearchResult[] = [];
  let iter = 0;

  while (dataList.length > 0) {
    // 最后一个元素吹处理
    if (iter === dataList.length) {
      iter = 0;
    }
    const resultItem = dataList[iter];
    // item为device的情况下，直接加入map
    if (!resultItem.netifNo) {
      sourceMap.set(resultItem.deviceName, {
        value: resultItem.deviceName,
        label: resultItem.alias
          ? `${resultItem.alias}[${resultItem.deviceName}]`
          : resultItem.deviceName,
        children: [],
      });
      dataList.splice(iter, 1); // 删除元素
      continue;
    }
    // item为netifNo的情况下
    if (resultItem.netifNo) {
      // 没有父节点,跳过
      if (sourceMap.get(resultItem.deviceName) === undefined) {
        iter = (iter + 1) % dataList.length;
        continue;
      }
      // 有父节点，挂在父节点下面
      else {
        const parent = sourceMap.get(resultItem.deviceName);
        parent.children = [
          ...parent.children,
          {
            value: `${resultItem.deviceName}_${resultItem.netifNo}_${resultItem.netifSpeed || ''}`,
            label: resultItem.alias
              ? `${resultItem.alias}[接口${resultItem.netifNo}]`
              : `接口${resultItem.netifNo}`,
          },
        ];
        dataList.splice(iter, 1); // 删除元素
        sourceMap.set(resultItem.deviceName, parent);
        continue;
      }
    }
  }
  sourceMap.forEach((item) => {
    resultList.push({
      ...item,
      children: bubbleSorter(item.children, 'asc', 'value'),
    });
  });
  return resultList;
}

/** dsl转换器 */
export function dslConverter(dsl: string, target: string, replaceList: string[]) {
  if (!dsl || !replaceList || !replaceList || !target || replaceList.length === 0) {
    return dsl;
  }
  let replacePointer = 0;
  let offset = 0;
  let result = '';
  let subString = dsl;
  const replaceLength = replaceList.length;
  while (subString.indexOf(target) !== -1) {
    const index = subString.indexOf(target);
    result += `${subString.slice(0, index - 1 > 0 ? index - 1 : 0)}${replaceList[replacePointer]}`;
    offset += index + target.length;
    subString = dsl.substring(offset);
    replacePointer = (replacePointer + 1) % replaceLength;
  }
  result += dsl.slice(offset === 0 ? 0 : offset + 1);
  return result;
}

/** 格式化函数 */
export function sessionFomatter(name: string, originMap: Map<string, any>) {
  const { srcIp, destIp, srcPort, destPort } = originMap.get(name);
  return `${srcIp}:${srcPort}⇋${destIp}:${destPort}`;
}
