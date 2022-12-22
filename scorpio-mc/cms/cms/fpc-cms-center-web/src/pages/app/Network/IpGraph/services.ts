import ajax from '@/utils/frame/ajax';
import config from '@/common/applicationConfig';
import type {
  IFlowQueryParams,
  IIpConversationGraph,
  IIpConversationGraphParams,
  IIpConversationHistoryParams,
} from './typings';
import type { IAjaxResponseFactory } from '@/common/typings';
import { stringify } from 'qs';

const { API_VERSION_PRODUCT_V1 } = config;

/** 查询 IP 访问关系图历史列表 */
export async function queryIpConversationGraphHistory() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ip-conversations/history`);
}

/** 查询 IP 访问关系图 */
export async function queryIpConversationGraphHistoryById(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ip-conversations/history/${id}`);
}

export async function createGraphHistory(params: IIpConversationHistoryParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ip-conversations/history`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

export async function alterGraphHistory(params: IIpConversationHistoryParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ip-conversations/history/${params.id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}

export async function deleteHistoryRelations({
  id,
  ...params
}: {
  id: string;
  name: string;
  data: string;
}) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ip-conversation/history/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
      ...params,
    },
  });
}

export async function deleteGraphHistory(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ip-conversations/history/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

/** 查询 IP 访问关系图 */
export async function queryIpConversationGraph(
  params: IIpConversationGraphParams,
): Promise<IAjaxResponseFactory<IIpConversationGraph[]>> {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/ip-conversations/as-graph?${params && stringify(params)}`,
  );
}
