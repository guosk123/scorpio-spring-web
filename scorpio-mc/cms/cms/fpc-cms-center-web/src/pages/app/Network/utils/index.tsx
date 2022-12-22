import moment from 'moment';
import { ESensorStatus } from '../../Configuration/Network/typings';
import type { INetworkTreeItem } from '../typing';

/**
 * 计算 bytes/每秒
 */
export function getBytepsAvg(totalBytes: number = 0, startTime: string, endTime: string) {
  const timeDiff = moment(endTime).diff(startTime) / 1000;
  if (timeDiff !== 0) {
    return totalBytes / timeDiff;
  }
  return 0;
}

/**
 * 计算TCP连接成功率
 */
export function getTcpEstablishedSuccessRate(
  /** 成功次数 */
  tcpEstablishedSuccessCount: number,
  /** 失败次数 */
  tcpEstablishedFailedCount: number,
  /** 保留小数长度 */
  decimal: number = 2,
): number {
  if (tcpEstablishedSuccessCount + tcpEstablishedFailedCount !== 0) {
    return (
      (tcpEstablishedSuccessCount / (tcpEstablishedSuccessCount + tcpEstablishedFailedCount)) *
      100
    ).toFixed(decimal) as any;
  }
  return 0;
}

export function classifyDataSet(networkDataSet: INetworkTreeItem[]) {
  if (!networkDataSet.length) {
    return;
  }
  const offlineQueue: INetworkTreeItem[] = [];
  const onlineQueue: INetworkTreeItem[] = [];
  networkDataSet.forEach((set) => {
    if (set.status === ESensorStatus.OFFLINE) {
      offlineQueue.push(set);
    } else {
      onlineQueue.push(set);
    }
  });
  return [...onlineQueue, ...offlineQueue];
}
