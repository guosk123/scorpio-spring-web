import { stringify } from 'qs';
import ajax from '@/utils/frame/ajax';
import type { IQueryMetadataDetailParams, IQueryMetadataParams } from './typings';
import { EMetadataProtocol } from './typings';
import { getEntryFromProtocol } from './utils/entryTools';
import config from '@/common/applicationConfig';
import type { IAjaxResponseFactory } from '@/common/typings';

const { API_VERSION_PRODUCT_V1, API_BASE_URL } = config;

/**
 * 查询所有可解析的元数据类型
 */
export async function queryAllProtocols(params?: {
  protocolName?: string;
  standard?: string;
  label?: string;
}) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metadata/protocols?${stringify(params)}`);
}

/**
 * 查询某个协议的列表
 */
export async function queryMetadataLogs({ protocol, ...restParams }: IQueryMetadataParams) {
  let nextProtocol = protocol;
  if (protocol === EMetadataProtocol.DHCPV6) {
    nextProtocol = EMetadataProtocol.DHCP;
  }
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metadata/${
      nextProtocol !== EMetadataProtocol.FILE ? `protocol-${nextProtocol}-logs` : nextProtocol
    }${
      restParams &&
      `?${stringify({ ...restParams, entry: getEntryFromProtocol(restParams.entry, protocol) })}`
    }`,
  );
}

export async function queryMetadataTotal({ protocol, ...rest }: IQueryMetadataParams) {
  let p = protocol;
  if (p === EMetadataProtocol.DHCPV6) {
    p = EMetadataProtocol.DHCP;
  }
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metadata/${
      p !== EMetadataProtocol.FILE ? `protocol-${p}-logs` : p
    }/as-statistics${rest && `?${stringify({ ...rest, entry: undefined })}`}`,
  );
}

/**
 * 查询某个协议的详情
 */
export async function queryMetadataLogDetail({ protocol, id }: IQueryMetadataDetailParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metadata/protocol-${protocol}-logs/${id}`);
}

/**
 * 协议日志导出
 */
export async function exportMetadataLogs({ protocol, ...restParams }: IQueryMetadataParams) {
  let nextProtocol = protocol;
  if (
    [EMetadataProtocol.POP3, EMetadataProtocol.IMAP, EMetadataProtocol.SMTP].includes(
      protocol || ('' as any),
    )
  ) {
    nextProtocol = EMetadataProtocol.MAIL;
  } else if (protocol === EMetadataProtocol.DHCPV6) {
    nextProtocol = EMetadataProtocol.DHCP;
  } else if (
    [EMetadataProtocol.ICMPV4, EMetadataProtocol.ICMPV6].includes(protocol || ('' as any))
  ) {
    nextProtocol = EMetadataProtocol.ICMP;
  }

  window.open(
    `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/metadata/${
      nextProtocol !== EMetadataProtocol.FILE ? `protocol-${nextProtocol}-logs` : nextProtocol
    }/as-export${
      restParams &&
      `?${stringify({ ...restParams, entry: getEntryFromProtocol(restParams.entry, protocol) })}`
    }`,
  );
}

export async function queryHttpAnalysis(params: { dsl: string; interval: number }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/https?${stringify(params)}`);
}

export async function queryMetadataMapfieldKeys(
  protocol: string,
): Promise<IAjaxResponseFactory<Record<string, string[]>>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/metadata/protocol-map-keys/${protocol}`);
}
