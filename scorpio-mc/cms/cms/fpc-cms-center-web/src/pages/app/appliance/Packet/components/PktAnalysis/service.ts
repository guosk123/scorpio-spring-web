import config from '@/common/applicationConfig';
import type { IAjaxResponseFactory } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import { getPageQuery, parseObjJson } from '@/utils/utils';
import { message } from 'antd';
import { parse, stringify } from 'qs';
import type { IPktAnalysisSharedProps } from '.';
import { EPktAnalysisDataSource } from './typings';

const { API_VERSION_PRODUCT_V1, API_BASE_URL } = config;

interface IQueryParams extends IPktAnalysisSharedProps {
  startTime?: string;
  endTime?: string;
  type: 'analyze' | 'bye' | 'download';
  parameter?: string;
  [propName: string]: any;
  packetFilterJson?: string;
}

export async function queryPacketAnalysis(
  params: IQueryParams,
): Promise<IAjaxResponseFactory<any>> {
  const {
    sourceType,
    taskId,
    type = 'analyze',
    packetFilterJson,
    fpcSerialNumber,
    networkId,
    ...rest
  } = params;
  // 默认类型是分析
  if (!rest.type) {
    rest.type = 'analyze';
  }
  console.log('packetFilterJson', packetFilterJson);

  const newParams = {
    type,
    parameter: JSON.stringify(rest),
  };

  const pageParams = getPageQuery();
  if (pageParams.startTime) {
    // @ts-ignore
    newParams.startTime = pageParams.startTime;
  }
  if (pageParams.endTime) {
    // @ts-ignore
    newParams.endTime = pageParams.endTime;
  }

  // 查询任务分析
  if (sourceType === EPktAnalysisDataSource['transmit-task']) {
    return ajax(
      `${API_VERSION_PRODUCT_V1}/appliance/transmition-tasks/${taskId}/analysis${
        newParams && `?${stringify(newParams)}`
      }`,
    );
  }
  // 数据包
  if (sourceType === EPktAnalysisDataSource.packet) {
    // 数据包统计中URL中还会携带一堆参数
    // 这里存放的就是数据包页面的查询条件
    // const { query = '' } = pageParams;
    // const queryJson = parseObjJson(query as string);
     const queryJson: any = parseObjJson(packetFilterJson || '');

    // if (queryJson.startTime) {
    //   queryJson.startTime = encodeURIComponent(queryJson.startTime);
    // }
    // if (queryJson.endTime) {
    //   queryJson.endTime = encodeURIComponent(queryJson.endTime);
    // }

    return ajax(
      `${API_VERSION_PRODUCT_V1}/appliance/packets/analysis${
        newParams && `?${stringify({ ...newParams, ...queryJson, fpcSerialNumber, networkId })}`
      }`,
    );
  }
  return { success: false, result: '' };
}

export async function downloadFlowFile(params: IQueryParams): Promise<any> {
  const { sourceType, taskId, fpcSerialNumber, networkId, ...rest } = params;
  // 查询任务分析
  if (sourceType === EPktAnalysisDataSource['transmit-task']) {
    window.open(
      `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/appliance/transmition-tasks/${taskId}/analysis${
        rest && `?${stringify(rest)}`
      }`,
    );
    return;
  }
  // 数据包
  if (sourceType === EPktAnalysisDataSource.packet) {
    // 数据包统计中URL中还会携带一堆参数
    // 这里存放的就是数据包页面的查询条件
    const pageParams = getPageQuery();
    const { query = '' } = pageParams;
    const queryJson = parseObjJson(query as string);
    window.open(
      `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/appliance/packets/analysis${
        rest && `?${stringify({ ...queryJson, ...rest, fpcSerialNumber, networkId })}`
      }`,
    );
    return;
  }

  message.error('下载失败');
}
