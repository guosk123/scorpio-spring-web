import type { IUriParams } from '@/pages/app/analysis/typings';
import { editIdentificationCode } from '@/utils/frame/menuAccess';
import { history, useParams } from 'umi';
import useAnalysisType from '../useAnalysisType';

export const clearIDInUrl = (url: string, clearIDs: { id: string; templateURL: string }[]) => {
  let resUrl = url;
  clearIDs.forEach((item) => {
    resUrl = url.replace(`${item.id}`, item.templateURL);
  });
  return resUrl;
};

export enum CLEAR_URL_TYPE {
  NETWOKR = 'network',
  SERVICE = 'service',
  PCAP_FILE = 'packetFile',
  OTHER = 'other',
}

export const clearUrlMap = {
  [CLEAR_URL_TYPE.NETWOKR]: { templateStr: ':networkId' },
  [CLEAR_URL_TYPE.SERVICE]: { templateStr: ':serviceId' },
  [CLEAR_URL_TYPE.PCAP_FILE]: { templateStr: ':pcapFileId' },
};

export const analysisAccessUrlType = () => {};

export default function useUrlToAccessUrl(accessKey?: string, clearType?: CLEAR_URL_TYPE) {
  const tmpClearType = useAnalysisType();
  const analysisType = clearType || tmpClearType;
  const params: IUriParams = useParams();
  const otherClearArr: { id: string; templateURL: string }[] = [];
  Object.keys(params).forEach((key) => {
    otherClearArr.push({ id: params[key], templateURL: `:${key}` });
  });
  const { networkId = '', serviceId = '', pcapFileId = '' }: IUriParams = useParams();
  const clearUrlTempMap = {
    [CLEAR_URL_TYPE.NETWOKR]: [
      { id: networkId, templateURL: clearUrlMap[CLEAR_URL_TYPE.NETWOKR].templateStr },
    ],
    [CLEAR_URL_TYPE.SERVICE]: [
      { id: networkId, templateURL: clearUrlMap[CLEAR_URL_TYPE.NETWOKR].templateStr },
      { id: serviceId, templateURL: clearUrlMap[CLEAR_URL_TYPE.SERVICE].templateStr },
    ],
    [CLEAR_URL_TYPE.PCAP_FILE]: [
      { id: pcapFileId, templateURL: clearUrlMap[CLEAR_URL_TYPE.PCAP_FILE].templateStr },
    ],
    [CLEAR_URL_TYPE.OTHER]: otherClearArr,
  };
  const authenticationUrl = editIdentificationCode(
    clearIDInUrl(history.location.pathname, clearUrlTempMap[analysisType]),
  );
  return `${authenticationUrl}/${accessKey}`;
}
