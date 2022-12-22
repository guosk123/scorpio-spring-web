/**
 * ===========
 *  场景分析任务
 * ===========
 */
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type {
  IScenarioCustomTemplate,
  IQueryDynamicDomainTermsParams,
  IQueryScenarioTaskResultParams,
  IScenarioTask,
  ScenarioTaskResult,
} from './typings';

export async function queryScenarioTasks(
  params: any,
): Promise<IAjaxResponseFactory<IPageFactory<IScenarioTask>>> {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/analysis/scenario-tasks${params && `?${stringify(params)}`}`,
  );
}

export async function queryScenarioTaskDetail({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/scenario-tasks/${id}`);
}

export async function createScenarioTask(params: any) {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/scenario-tasks`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

/**
 *
 * @param {*} params
 */
export async function updateScenarioTask(params: IScenarioCustomTemplate) {
  const { id, ...restParams } = params;
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/scenario-tasks/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}

export async function deleteScenarioTask({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/scenario-tasks/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

export async function queryAllScenarioCustomTemplates({
  isDetail = true,
}): Promise<IAjaxResponseFactory<IScenarioCustomTemplate[]>> {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/analysis/scenario-task/custom-templates?isDetail=${isDetail}`,
  );
}

export async function queryScenarioCustomTemplateDetail({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/scenario-task/custom-templates/${id}`);
}

export async function createScenarioCustomTemplate(params: IScenarioCustomTemplate) {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/scenario-task/custom-templates`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

export async function deleteScenarioCustomTemplate({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/scenario-task/custom-templates/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

/**
 * 查询任务执行结果
 * @param {String} id 任务ID
 * @param {String} type 场景类型
 * @param {String} query 查询参数 {srcIp: 10.0.0.1}
 * @param {String} sortProperty 排序字段
 * @param {String} sortDirection 排序方式 asc | desc
 */
export async function queryScenarioTaskResults({
  id,
  type,
  query,
  ...rest
}: IQueryScenarioTaskResultParams): Promise<
  IAjaxResponseFactory<IPageFactory<ScenarioTaskResult>>
> {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/analysis/scenario-tasks/${id}/results?type=${type}${
      rest && `&${stringify(rest)}`
    }`,
    {
      data: {
        query: JSON.stringify(query),
      },
    },
  );
}

/**
 * 动态域名聚合结果
 * @param {String} id 任务ID
 * @param {String} type 场景类型
 * @param {String} termField 聚合字段
 */
export async function queryScenarioTaskDynamicDomainTerms({
  id,
  type,
  termField,
}: IQueryDynamicDomainTermsParams) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/analysis/scenario-tasks/${id}/results/as-terms?type=${type}&termField=${termField}`,
  );
}
