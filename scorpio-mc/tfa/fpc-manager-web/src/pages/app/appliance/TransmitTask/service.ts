/**
 * ===========
 *  流量查询任务
 * ===========
 */

import { stringify } from 'qs';
import ajax from '@/utils/frame/ajax';
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory } from '@/common/typings';
import type { ITransmitTask } from './typings';

// 查
export async function queryTransmitTasks(
  params: Record<string, any>,
): Promise<IAjaxResponseFactory<IPageFactory<ITransmitTask>>> {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/transmition-tasks${params && `?${stringify(params)}`}`,
  );
}

// 查某个任务的详情
export async function queryTransmitTasksDetail({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/transmition-tasks/${id}`);
}

// 新增
export async function createTransmitTask(params: Record<string, any>) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/transmition-tasks`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

// 编辑
export async function updateTransmitTask(params: Record<string, any>) {
  const { id, ...restParams } = params;
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/transmition-tasks/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}

// 删除
export async function deleteTransmitTask({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/transmition-tasks/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

// 批量操作
export async function batchDelTransmitTasks(params: { delete: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/transmition-tasks/batch`, {
    type: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: JSON.stringify(params),
  });
}

// 下载文件
export async function downloadTransmitTaskFile({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/transmition-tasks/${id}/files`);
}

// 停止任务
export async function stopTransmitTask({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/transmition-tasks/${id}/stop`, {
    type: 'POST',
  });
}

// 重新开始任务
export async function restartTransmitTask({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/transmition-tasks/${id}/redo`, {
    type: 'POST',
  });
}
