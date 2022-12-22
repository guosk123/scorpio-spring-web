import type { IUriParams } from '@/pages/app/analysis/typings';
import { useParams } from 'umi';
import { CLEAR_URL_TYPE } from '../useUrlToTemplate';

export default function useAnalysisType() {
  const { networkId, serviceId, pcapFileId }: IUriParams = useParams();
  let resType = CLEAR_URL_TYPE.OTHER;
  if (pcapFileId) {
    resType = CLEAR_URL_TYPE.PCAP_FILE;
  } else if (serviceId) {
    resType = CLEAR_URL_TYPE.SERVICE;
  } else if (networkId) {
    resType = CLEAR_URL_TYPE.NETWOKR;
  }
  return resType;
}
