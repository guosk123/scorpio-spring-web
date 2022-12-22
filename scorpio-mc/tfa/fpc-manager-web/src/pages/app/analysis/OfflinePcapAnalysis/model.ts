import { DEFAULT_PAGE_SIZE_KEY, PAGE_DEFAULT_SIZE } from '@/common/app';
import storage from '@/utils/frame/storage';
import { parseObjJson } from '@/utils/utils';
import { deletePcapFile, queryPcapList, queryUploadUri } from './service';
import type { IPcapModelType, IPcapState } from './typing';

const Pcap: IPcapModelType = {
  namespace: 'offlinePcapModel',
  state: {
    pcapState: {
      number: 0,
      size: parseInt(storage.get(DEFAULT_PAGE_SIZE_KEY) || '20', 10) || PAGE_DEFAULT_SIZE,
      totalElements: 0,
      totalPages: 0,
      pcapList: [],
    },
    uploadUri: '',
  },
  effects: {
    *queryPcapList({ payload = {} }, { call, put }) {
      const { success, result } = yield call(queryPcapList, payload);
      yield put({
        type: 'savePcapList',
        result: success ? result : [],
      });
    },
    *deletePcapFile({ payload = {} }, { call }) {
      const { success } = yield call(deletePcapFile, payload);
      return success;
    },
    *queryUploadUri({ payload = {} }, { call, put }) {
      const { success, result } = yield call(queryUploadUri, payload.filename);
      if (success) {
        yield put({ type: 'saveUploadUri', result });
      }
    },
  },
  reducers: {
    savePcapList(state, payload) {
      const pcapList = payload.result.content.map((item: any) => {
        const tmp = { ...item };
        tmp.executionResult = parseObjJson(item.executionResult);
        return tmp;
      });
      const tmpState: IPcapState = {
        number: payload.result.number,
        size: payload.result.size,
        totalElements: payload.result.totalElements,
        totalPages: payload.result.totalPages,
        pcapList,
      };
      return { ...state, pcapState: { ...tmpState } };
    },
    saveUploadUri(state, payload) {
      return { ...state, uploadUri: payload.result };
    },
  },
} as IPcapModelType;
export default Pcap;
