import { useEffect } from 'react';
import type { Dispatch } from 'umi';
import { history } from 'umi';
import { stringify } from 'qs';
import { getLinkUrl, jumpNewPage } from '@/utils/utils';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType, getGlobalTime } from '@/components/GlobalTimeSelector';
import moment from 'moment';



export function handleMsgFromparents(
  ev: MessageEvent<{ param: any & { newTab?: boolean; url?: string } }>,
  dispatch?: Dispatch,
  callback?: any
) {
  const  {param}  = {...(ev.data||{})};
  const { url, newTab = false, from, to } = param || {};
  if (url) {
    const urlParam = {...param}
    delete urlParam.url;
    delete urlParam.newTab;
    if (newTab) {
      jumpNewPage(`${url}?${stringify({ ...urlParam })}`);
    } else {
      history.push(getLinkUrl(`${url}?${stringify({ ...urlParam })}`));
    }
  }
  /** 不传递dispatch 无法修改时间 */
  if (dispatch) {
    if (from && to) {
      const timeObj: IGlobalTime = {
        relative: false,
        type: ETimeType.CUSTOM,
        custom: [moment(from), moment(to)],
      };
      dispatch({
        type: 'appModel/updateGlobalTime',
        payload: getGlobalTime(timeObj),
      });
    }
  }
  if (callback) {
    callback(param)
  }
}

/** 用来处理来自于BI内部的跳转 */
export default function useBiListener({ dispatch, callback }: { dispatch?: Dispatch, callback?: any }) {
  useEffect(() => {
    window.addEventListener(
      'message',
      (ev: MessageEvent<{ param: any }>) => {
        handleMsgFromparents(ev, dispatch, callback);
      },
      false,
    );
  }, []);
}
