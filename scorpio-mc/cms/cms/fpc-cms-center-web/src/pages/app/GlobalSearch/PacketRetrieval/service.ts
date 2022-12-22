/**
 * ===========
 *  流量查询任务
 * ===========
 */

import { stringify } from 'qs';
import ajax from '@/utils/frame/ajax';
import config from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory } from '@/common/typings';
import type { ITransmitTask } from './typings';

const { API_VERSION_PRODUCT_V1 } = config;

// 查
export async function queryTransmitTasks(
  params: Record<string, any>,
): Promise<IAjaxResponseFactory<IPageFactory<ITransmitTask>>> {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/assignment-tasks${params && `?${stringify(params)}`}`,
  );
}

// 已保存任务详情
export async function queryTransmitTasksDetail({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/assignment-tasks/${id}`);
}

// 查某个任务的详情
export async function querySensorTaskDetail({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/sensor-task-detail/${id}`);
}

// 新增
export async function createTransmitTask(params: Record<string, any>) {
  console.log('params', params);
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/assignment-tasks`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

// 编辑
export async function updateTransmitTask(params: Record<string, any>) {
  const { id, ...restParams } = params;
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/assignment-tasks/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}

// 删除
export async function deleteTransmitTask({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/assignment-tasks/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

// 批量操作
export async function batchDelTransmitTasks(params: { delete: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/assignment-tasks/batch`, {
    type: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: JSON.stringify(params),
  });
}

// 下载文件
export async function downloadTransmitTaskFile(params: any) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/assignment-task-files?${stringify(params)}`);
}

// 停止任务
export async function stopTransmitTask({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/assignment-tasks/${id}/stop`, {
    type: 'POST',
  });
}

// 重新开始任务
export async function restartTransmitTask({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/assignment-tasks/${id}/redo`, {
    type: 'POST',
  });
}

// 查某个任务的详情
export async function querySensorTaskList(params: any) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/assignment-task-records?${stringify(params)}`);
}
