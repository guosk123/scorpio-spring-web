import config from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type { IQueryParams } from './typings';

const { API_VERSION_PRODUCT_V1 } = config;

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

export async function queryAnalyzableObject(params: any) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/analyzable-object${params ? `?${stringify(params)}` : ''}`,
  );
}

export async function stopPacketList({
  queryId,
  fpcSerialNumber,
}: {
  queryId: string;
  fpcSerialNumber: string;
}) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/packets/stop`, {
    type: 'POST',
    data: {
      queryId,
      fpcSerialNumber,
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
