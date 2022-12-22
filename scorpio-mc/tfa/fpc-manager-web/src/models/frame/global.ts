import type { Reducer, Effect, Dispatch, History } from 'umi';
import {
  queryProductInfos,
  queryCurrentUser,
  queryMenus,
  updateCurrentUserInfo,
} from '@/services/frame/global';
import type { MenuDataItem } from '@ant-design/pro-layout';
import { message } from 'antd';
import { checkCurrentPassword, updateCurrentUserPassword } from '@/services/frame/global';
import { getPagePath } from '@/utils/utils';
import type { IAjaxMap } from '@/utils/frame/ajax';

export type Subscription = ({
  dispatch,
  history,
}: {
  dispatch: Dispatch;
  history: History;
}) => void;

export interface IProductInfo {
  corporation: string;
  description?: string;
  'license-deadline'?: string;
  'license-form'?: string;
  name: string;
  series: string;
  version: string;
  cpuModel?: string;
  osInfo?: string;
  logoBase64?: string;
}

export type TAuthority =
  | 'PERM_USER'
  | 'PERM_SYS_USER'
  | 'PERM_AUDIT_USER'
  | 'PERM_RESTAPI_USER'
  | 'NOT_LOGIN';

export interface ICurrentUser {
  accountNonLocked: boolean;
  accountNonExpired: boolean;
  credentialsNonExpired: boolean;
  appKey?: string;
  appToken?: string;
  email?: string;
  authorities: {
    authority: TAuthority;
  }[];
  roles: {
    id: string;
    nameEn: string;
    nameZh: string;
    description?: string;
  }[];
  username: string;
  fullname: string;
  id: string;
  remoteAddress: string;
  userPasswordNonReliable: boolean;
  menuPerms: any;
}

export interface GlobalModelState {
  collapsed: boolean;
  productInfos: IProductInfo;
  menus: MenuDataItem[];
  currentUser: ICurrentUser;
  userInfoModalVisible: boolean;
  changePwdModalVisible: boolean;
}

export interface GlobalModelType {
  namespace: string;
  state: GlobalModelState;
  effects: {
    queryProductInfos: Effect;
    queryCurrentUser: Effect;
    clearCurrentUser: Effect;
    queryMenus: Effect;
    updateCurrentUserInfo: Effect;
    updateCurrentUserPassword: Effect;
    checkCurrentPassword: Effect;
  };
  reducers: {
    changeLayoutCollapsed: Reducer<GlobalModelState>;
    updateState: Reducer<GlobalModelState>;
  };
  subscriptions: {
    setupRequestCancel: Subscription;
  };
}

const GlobalModel: GlobalModelType = {
  namespace: 'globalModel',

  state: {
    collapsed: false,
    productInfos: {} as IProductInfo,
    menus: [],
    currentUser: {} as ICurrentUser,

    userInfoModalVisible: false,
    changePwdModalVisible: false,
  },

  effects: {
    *queryProductInfos({ payload }, { call, put }) {
      const { success, result } = yield call(queryProductInfos, payload);
      yield put({
        type: 'updateState',
        payload: {
          productInfos: success ? result : {},
        },
      });
    },

    *queryCurrentUser(_, { call, put }) {
      const { result, success } = yield call(queryCurrentUser);
      const currentUser: ICurrentUser = success ? result : {};
      if (success) {
        // 拿到密码可靠性前先关掉，由于弹框变量为全局，401等引发的退出不会关闭当前弹框
        yield put({
          type: 'updateState',
          payload: {
            changePwdModalVisible: false,
          },
        });
        // 检查用户密码是否可靠
        // 如果用户的密码不可靠，自动显示修改密码弹出框
        if (currentUser.userPasswordNonReliable) {
          yield put({
            type: 'updateState',
            payload: {
              changePwdModalVisible: true,
            },
          });
        }
        // 取菜单信息
        // yield put({
        //   type: 'queryMenus',
        // });
      } else {
        // history.replace('/login');
      }
      yield put({
        type: 'updateState',
        payload: { currentUser },
      });
      return { success, currentUser };
    },
    *clearCurrentUser(_, { put }) {
      yield put({
        type: 'updateState',
        payload: {
          currentUser: {},
        },
      });
      // history.replace('/login');
    },

    *queryMenus({ payload }, { call, put }) {
      const { success, result } = yield call(queryMenus, payload);
      yield put({
        type: 'updateState',
        payload: {
          menus: success ? [result] : [],
        },
      });
    },
    *updateCurrentUserInfo({ payload }, { call, put }) {
      const { success } = yield call(updateCurrentUserInfo, payload);
      if (success) {
        message.success('修改个人信息成功');
        yield put({
          type: 'updateState',
          payload: {
            userInfoModalVisible: false,
          },
        });
        yield put({
          type: 'queryCurrentUser',
        });
      } else {
        message.error('修改个人信息失败');
      }
      return success;
    },
    *checkCurrentPassword({ payload }, { call }) {
      const { result } = yield call(checkCurrentPassword, payload);
      if (!result) {
        message.error('密码不正确，请重新输入');
      }
      return result;
    },
    *updateCurrentUserPassword({ payload }, { call, put }) {
      const { success } = yield call(updateCurrentUserPassword, payload);
      if (success) {
        yield put({
          type: 'updateState',
          payload: {
            changePwdModalVisible: false,
          },
        });
        yield put({
          type: 'loginModel/logout',
        });
      } else {
        message.error('修改个人密码失败');
      }
      return success;
    },
  },

  reducers: {
    changeLayoutCollapsed(state = {} as GlobalModelState, { payload }): GlobalModelState {
      return {
        ...state,
        collapsed: payload,
      };
    },

    updateState(state, { payload }): GlobalModelState {
      return {
        ...state,
        ...payload,
      };
    },
  },

  subscriptions: {
    setupRequestCancel({ history }) {
      history.listen(() => {
        const { cancelRequest = new Map<symbol, IAjaxMap>() } = window;
        const pagePath = getPagePath();
        cancelRequest.forEach((value: IAjaxMap, key: symbol) => {
          if (value.pagePath !== pagePath) {
            if (value.ajax) {
              value.ajax.abort();
            }
            cancelRequest.delete(key);
          }
        });
      });
    },
  },
};

export default GlobalModel;
