import { ONE_KILO_1024 } from '@/common/dict';
import {
  queryStorageSpaceSettings,
  updateStorageSpaceSettings
} from '@/pages/app/configuration/StorageSpace/service';
import type { IStorageSpace } from '@/pages/app/configuration/StorageSpace/typings';
import { SpaceEnum } from '@/pages/app/configuration/StorageSpace/typings';
import { model } from '@/utils/frame/model';
import { message } from 'antd';
import modelExtend from 'dva-model-extend';
import type { Effect } from 'umi';

export const DEFAULT_TRANSMIT_TASK_FILE_LIMIT_BYTES = 8 * Math.pow(ONE_KILO_1024, 3);

export interface IStorageSpaceModelState {
  /**
   * 单文件最大落盘大小限制
   *
   * 默认 8GB
   */
  transmitTaskFileLimitBytes: number;
  settings: IStorageSpace[];
}

export interface IStorageSpaceModel {
  namespace: string;
  state: IStorageSpaceModelState;
  effects: {
    queryStorageSpaceSettings: Effect;
    updateStorageSpaceSettings: Effect;
  };
}

export default modelExtend(model, {
  namespace: 'storageSpaceModel',
  state: {
    settings: [],
    transmitTaskFileLimitBytes: DEFAULT_TRANSMIT_TASK_FILE_LIMIT_BYTES,
  },

  effects: {
    *queryStorageSpaceSettings(_, { call, put }) {
      const { success, result = [] } = yield call(queryStorageSpaceSettings);

      // 查找默认落盘大小
      let settings: IStorageSpace[] = [];
      let transmitTaskFileLimitBytes = DEFAULT_TRANSMIT_TASK_FILE_LIMIT_BYTES;
      if (success) {
        settings = [...result];
        const limit = (result as IStorageSpace[]).find(
          (el) => el.spaceType === SpaceEnum.TRANSMIT_TASK_FILE_LIMIT,
        );

        if (!limit) {
          settings.push({
            spaceType: SpaceEnum.TRANSMIT_TASK_FILE_LIMIT,
            capacity: DEFAULT_TRANSMIT_TASK_FILE_LIMIT_BYTES,
          });
        } else {
          transmitTaskFileLimitBytes = limit.capacity;
        }
      }

      yield put({
        type: 'updateState',
        payload: {
          settings,
          transmitTaskFileLimitBytes,
        },
      });
    },
    *updateStorageSpaceSettings({ payload }, { call, put }) {
      const { success } = yield call(updateStorageSpaceSettings, payload);
      if (success) {
        message.success('保存成功');
        yield put({
          type: 'queryStorageSpaceSettings',
        });
      } else {
        message.error('保存失败');
      }
    },
  },
} as IStorageSpaceModel);
