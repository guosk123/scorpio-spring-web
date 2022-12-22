import modelExtend from 'dva-model-extend';
import { model } from '@/utils/frame/model';
import { queryLicense } from './service';
import type { Effect } from 'umi';
import type { ILicenseInfo } from './typings';

export interface LicenseStateType {
  license: ILicenseInfo;
}

export interface LicenseModelType {
  namespace: string;
  state: LicenseStateType;
  effects: {
    queryLicense: Effect;
  };
}

export default modelExtend(model, {
  namespace: 'licenseModel',
  state: {
    license: {} as ILicenseInfo,
  },
  effects: {
    *queryLicense(_, { call, put }) {
      const { success, result } = yield call(queryLicense);
      yield put({
        type: 'updateState',
        payload: {
          license: success ? result : {},
        },
      });
    },
  },
} as LicenseModelType);
