import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import { ETablekeyEntry } from './typings';
import type {
  IDownloadRecordFileParams,
  IFlowStatisticsParams,
  IHeartbeatParams,
  IQueryRecordParams,
} from './typings';
import config from '@/common/applicationConfig';

const { API_VERSION_PRODUCT_V1, API_BASE_URL } = config;

/**
 * 查询流日志表格
 */
export async function queryFlowRecords(params: IQueryRecordParams): Promise<any> {
  // 建连分析更换URL
  if (params.dsl?.indexOf('tcp_established_fail_flag=1') !== -1) {
    return ajax(
      `${API_VERSION_PRODUCT_V1}/appliance/flow-logs/establish-fail${
        params &&
        `?${stringify({
          ...params,
          dsl: params.dsl?.replace('AND (tcp_established_fail_flag=1)', '').replace('(tcp_established_fail_flag=1)', ''),
          entry: params.tableKey ? ETablekeyEntry[params.tableKey] : '',
        })}`
      }`,
    );
  }
  return ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/flow-logs${
      params &&
      `?${stringify({
        ...params,
        entry: params.tableKey ? ETablekeyEntry[params.tableKey] : '',
      })}`
    }`,
  );
}

/**
 * 流日志聚合统计
 */
export async function queryFlowLogsStatistics(params: IFlowStatisticsParams): Promise<any> {
  if (params.dsl?.indexOf('tcp_established_fail_flag=1') !== -1) {
    return ajax(
      `${API_VERSION_PRODUCT_V1}/appliance/flow-logs/establish-fail/as-statistics${
        params &&
        `?${stringify({
          ...params,
          dsl: params.dsl?.replace('AND (tcp_established_fail_flag=1)', '').replace('(tcp_established_fail_flag=1)', ''),
        })}`
      }`,
    );
  }
  return ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/flow-logs/as-statistics${
      params && `?${stringify(params)}`
    }`,
  );
}

/**
 * 导出流日志
 */
export async function exportFlowRecords(params: IQueryRecordParams): Promise<any> {
  if (params.dsl?.indexOf('tcp_established_fail_flag=1') !== -1) {
    window.open(
      `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/appliance/flow-logs/establish-fail/as-export${
        params &&
        `?${stringify({
          ...params,
          dsl: params.dsl?.replace('AND (tcp_established_fail_flag=1)', '').replace('(tcp_established_fail_flag=1)', ''),
        })}`
      }`,
    );
  } else {
    window.open(
      `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/appliance/flow-logs/as-export${
        params && `?${stringify(params)}`
      }`,
    );
  }
}

/**
 * 下载流日志pcap文件
 */
export async function downloadFlowLogFile(params: IDownloadRecordFileParams): Promise<any> {
  const { flowPacketId, ...restParams } = params;
  return ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/flow-logs/${flowPacketId}/file-urls?${stringify(
      restParams,
    )}`,
  );
}

/**
 * 查询流日志详情
 * @param flowId 流日志ID
 * @param inclusiveTime 流日志开始或结束时间
 */
export async function queryFlowRecordDetail({
  flowId,
  inclusiveTime,
}: {
  flowId: string;
  inclusiveTime?: string;
}): Promise<any> {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/flow-logs/${flowId}?inclusiveTime=${inclusiveTime}`,
  );
}

/**
 * 查询任务心跳
 * @param queryId
 */
export async function pingQueryTask({ queryId }: IHeartbeatParams): Promise<any> {
  return ajax(`${API_VERSION_PRODUCT_V1}/global/slow-queries/heartbeat`, {
    type: 'POST',
    data: {
      queryId,
    },
  });
}

/**
 * 取消查询
 * @param queryId
 */
export async function cancelQueryTask({ queryId }: any): Promise<any> {
  return ajax(`${API_VERSION_PRODUCT_V1}/global/slow-queries/cancel`, {
    type: 'POST',
    data: {
      queryId,
    },
  });
}
