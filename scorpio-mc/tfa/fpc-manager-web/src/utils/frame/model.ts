import modelExtend from 'dva-model-extend';
import { PAGE_DEFAULT_SIZE, getTablePaginationDefaultSettings } from '@/common/app';
import { v1 as uuidv1 } from 'uuid';
import type { Reducer } from 'umi';
import type { PaginationProps } from 'antd';

interface ModelType {
  reducers: {
    updateState: Reducer;
  };
}

export const model: ModelType = {
  reducers: {
    updateState(state, { payload }) {
      return {
        ...state,
        ...payload,
      };
    },
  },
};

export interface IPageModelState {
  list: any[];
  pagination: PaginationProps;
}

export interface PageModelType {
  state: {
    list: any[];
    pagination: PaginationProps;
  };
  reducers: {
    querySuccess: Reducer;
  };
}

export const pageModel: PageModelType = modelExtend(model, {
  state: {
    list: [],
    pagination: getTablePaginationDefaultSettings(),
  },

  reducers: {
    querySuccess(state: { pagination: any }, { payload }: any) {
      const { stateKey, stateValue, pagination } = payload;
      return {
        ...state,
        [stateKey]: stateValue,
        pagination: {
          ...state.pagination,
          ...pagination,
        },
      };
    },
  },
});

export function* doPageRequest({
  api,
  payload = {},
  call,
  put,
  stateKey = 'list',
}: {
  api: any;
  payload: any;
  call: any;
  put: any;
  stateKey: string;
}) {
  const { success, result = {} } = yield call(api, {
    ...payload,
    page: (payload.page && payload.page - 1) || 0,
    pageSize: payload.pageSize || PAGE_DEFAULT_SIZE,
  });

  const content = result ? result.content || [] : [];
  if (Array.isArray(content)) {
    // 判断下是否有 ID 字段
    content.forEach((row) => {
      if (!row.id) {
        // eslint-disable-next-line no-param-reassign
        row.id = uuidv1();
      }
    });
  }

  if (success) {
    yield put({
      type: 'querySuccess',
      payload: {
        stateKey, // 需要更新的state
        stateValue: [].concat(content), // state对象的值
        pagination: {
          current: result.number + 1 || 1,
          pageSize: result.size || PAGE_DEFAULT_SIZE,
          total: result.totalElements || 0,
          totalPages: result.totalPages || 0,
        },
      },
    });
  }
}
