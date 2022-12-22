import ajax from '@/utils/frame/ajax';
import application from '@/common/applicationConfig';
import React, { useCallback, useEffect, useMemo } from 'react';
import type { IFollowPayloadData } from '../../../typings';
const { API_VERSION_PRODUCT_V1 } = application;

export async function transcoding(params?: any) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/packets/analysis/transcoding`, {
    type: 'POST',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
      Accept: 'application/json, text/plain, */*',
    },
    traditional: true, //这里设置为true
    // data: JSON.stringify([1,2,3]),//传给后台相应数据
    data: JSON.stringify(params),
  });
}

interface Ipayloads {
  payloads: IFollowPayloadData[];
  type: string;
}
interface Props {
  onCodeTrans: any;
  payloadParams: Ipayloads;
}

export default function TransCode(props: Props) {
  const { payloadParams, onCodeTrans } = props;
  const queryTranscodingParams = useMemo(() => {
    return {
      payloads: payloadParams.payloads,
      type: payloadParams.type,
    };
  }, [payloadParams.payloads, payloadParams.type]);
  const queryTranscoding = useCallback(() => {
    if (queryTranscodingParams.payloads) {
      transcoding(queryTranscodingParams).then((res) => {
        const { success, result } = res;
        if (success) {
          onCodeTrans(result);
        } else {
          onCodeTrans([]);
        }
      });
    }
  }, [queryTranscodingParams]);

  useEffect(() => {
    queryTranscoding();
  }, [queryTranscoding]);

  return <div style={{ display: 'none' }} />;
}
