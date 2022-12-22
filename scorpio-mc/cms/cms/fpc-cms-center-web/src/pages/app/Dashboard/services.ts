import ajax from '@/utils/frame/ajax';
import application from '@/common/applicationConfig';
import { stringify } from 'qs';
import type { EDiskIOType } from './typings';

const { API_VERSION_PRODUCT_V1 } = application;

interface ISensorStatusIndexParams {
  metric: string;
  startTime: string;
  endTime: string;
}

interface ISensorStatusBarParams {
  metric: 'system_fs_free' | 'index_fs_free' | 'metadata_fs_free' | 'metadata_hot_fs_free';
  topNumber: number;
  startTime: string;
  endTime: string;
}

export interface ISensorStatusLineParams {
  metric: 'cpu_metric' | 'memory_metric' | 'disk_io';
  /** <60mins interval=60 >60mins interval=300 */
  interval: number;
  topNumber: number;
  startTime: string;
  endTime: string;
}

export interface INetworkLineParams {
  metric:
    | 'tcp_establish_success_rate'
    | 'total_bytes'
    | 'concurrent_sessions'
    | 'established_sessions';
  /** <60mins interval=60 >60mins interval=300 */
  interval: number;
  topNumber: number;
  startTime: string;
  endTime: string;
}

export interface INetworkLineParams {
  metric:
    | 'tcp_establish_success_rate'
    | 'total_bytes'
    | 'concurrent_sessions'
    | 'established_sessions';
  /** <60mins interval=60 >60mins interval=300 */
  interval: number;
  topNumber: number;
  startTime: string;
  endTime: string;
}

export interface IDiskIOParams {
  metric: EDiskIOType;
  partitionName:
    | 'fs_system_io'
    | 'fs_index_io'
    | 'fs_packet_io'
    | 'fs_metadata_io'
    | 'fs_metadata_hot_io';
  /** <60mins interval=60 >60mins interval=300 */
  interval: number;
  topNumber: number;
  startTime: string;
  endTime: string;
}

/** 用来获取所有fpc设备 */
export async function queryFpcDevices() {
  return ajax(`${API_VERSION_PRODUCT_V1}/central/fpc-devices`);
}

/** 用来获取cpu使用率/内存使用率： */
export async function queryUsageRate(params: ISensorStatusLineParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/sensor-status/usage-rate?${stringify(params)}`);
}

/** 用来获取磁盘IO趋势 */
export async function queryDiskIO(params: IDiskIOParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/sensor-status/disk-io?${stringify(params)}`);
}

/** 用来获取方框数据 */
export async function queryIndexData(params: ISensorStatusIndexParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/sensor-status/index-data?${stringify(params)}`);
}

/** 用来获取柱状图数据 */
export async function queryBarChart(params: ISensorStatusBarParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/sensor-status/free-space?${stringify(params)}`);
}

/** 用来获取时间直方图数据 */
export async function queryLineChart(params: ISensorStatusLineParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/sensor-status/line-chart?${stringify(params)}`);
}

/** 网络流量折线图 */
export async function queryNetyworkLineChart(params: INetworkLineParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/sensor/network-flow?${stringify(params)}`);
}
