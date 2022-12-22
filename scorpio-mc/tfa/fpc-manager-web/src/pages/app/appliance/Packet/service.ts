import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type { IQueryParams } from './typings';

export async function downloadPcapFile(params: IQueryParams): Promise<any> {
  ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/packets/file-urls${params && `?${stringify(params)}`}`,
  ).then((e: any) => {
    window.open(e.result);
  });
}

export async function queryPacketList(params: IQueryParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/packets${params && `?${stringify(params)}`}`);
}

export async function queryPacketRefine(params: any) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/packets/as-refines${params && `?${stringify(params)}`}`,
  );
}

export async function stopPacketList({ queryId }: { queryId: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/packets/stop`, {
    type: 'POST',
    data: {
      queryId,
    },
  });
}
export async function stopPacketRefine({ queryId }: { queryId: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/packets/as-refines/stop`, {
    type: 'POST',
    data: {
      queryId,
    },
  });
}
