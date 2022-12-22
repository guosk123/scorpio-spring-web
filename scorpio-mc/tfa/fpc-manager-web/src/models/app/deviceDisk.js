import modelExtend from 'dva-model-extend';
import { model } from '@/utils/frame/model';
import lodash from 'lodash';
import { queryDeviceDisks } from '@/services/app/deviceDisk';

export default modelExtend(model, {
  namespace: 'deviceDiskModel',
  state: {
    list: [],
  },

  effects: {
    *queryDeviceDisks(_, { call, put }) {
      const response = yield call(queryDeviceDisks);
      const { success, result } = response;

      // 针对排序
      let list = [];
      if (success) {
        list = result.map(item => ({
          ...item,
          slotNo: Number(item.slotNo),
          deviceId: Number(item.deviceId),
        }));

        list = lodash.sortBy(list, ['deviceId', 'slotNo']);
      }

      yield put({
        type: 'updateState',
        payload: {
          list,
        },
      });
    },
  },

  reducers: {},
});
