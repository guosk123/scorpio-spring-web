import { model } from '@/utils/frame/model';
import modelExtend from 'dva-model-extend';
import type { Effect } from 'umi';
import type { ConnectState } from '@/models/connect';

export interface INetflowModel extends ConnectState {
  netflowModel: INetflowModelState;
}

export interface INetflowModelState {
  selectedNetifSpeed: number,
}

export interface INetflowModelType {
  namespace: string;
  state: INetflowModelState;
  effects: {
    /** 设置速率 */
    setSelectedNetifSpeed: Effect
  };
}

export default modelExtend(model, {
  namespace: 'netflowModel',
  state: {
    selectedNetifSpeed:0,
  },
  effects: {
    *setSelectedNetifSpeed({payload},{put}){
      yield put({
        type: 'updateState',
        payload: {
          selectedNetifSpeed: payload
        },
      });
    }
  }
} as INetflowModelType);
