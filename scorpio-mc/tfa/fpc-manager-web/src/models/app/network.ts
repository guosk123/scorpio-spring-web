import type { ILogicalSubnet } from '@/pages/app/configuration/LogicalSubnet/typings';
import {
  createNetwork,
  deleteNetwork,
  queryAllNetworks,
  queryNetworkDetail,
  queryNetworkNetifs,
  queryNetworkPolicy,
  updateNetwork,
  updateNetworkPolicy,
} from '@/pages/app/configuration/Network/service';
import type {
  INetwork,
  INetworkMap,
  INetworkNetif,
  INetworkPolicy,
} from '@/pages/app/configuration/Network/typings';
import type { INetworkTopology } from '@/services/app/networkTopology';
import { queryNetworkTopology, updateNetworkTopology } from '@/services/app/networkTopology';
import { model } from '@/utils/frame/model';
import { parseObjJson } from '@/utils/utils';
import { message } from 'antd';
import type { Effect } from 'umi';
import modelExtend from 'dva-model-extend';
import type { Reducer } from 'redux';
import type { ConnectState } from '../connect';

/** 物理网络、逻辑子网组成的树 */
export interface INetworkTreeData {
  key: string;
  value: string;
  title: string;
  disabled?: boolean;
  children?: INetworkTreeData[];
}

export interface NetworkModelState {
  /** 物理网络、逻辑子网组成的树 */
  networkTree: INetworkTreeData[];
  allNetworks: INetwork[];
  allNetworkMap: INetworkMap;
  networkDetail: INetwork;
  usedNetifs: INetworkNetif[];

  networkIngestPolicy: INetworkPolicy[];
  networkApplicationPolicy: INetworkPolicy[];

  /** 网络拓扑图 */
  networkTopology: INetworkTopology;
}

export interface NetworkModelType {
  namespace: string;
  state: NetworkModelState;
  effects: {
    /** 获取物理网络、逻辑子网组成的树 */
    queryNetworkTree: Effect;
    queryAllNetworks: Effect;
    queryNetworkDetail: Effect;
    createNetwork: Effect;
    updateNetwork: Effect;
    deleteNetwork: Effect;
    queryUsedNetifs: Effect;

    queryNetworkIngestPolicy: Effect;
    updateNetworkIngestPolicy: Effect;
    queryNetworkApplicationPolicy: Effect;
    updateNetworkApplicationPolicy: Effect;

    /** 网络拓扑图 */
    queryNetworkTopology: Effect;
    updateNetworkTopology: Effect;
  };
  reducers: {
    updateNetworkTree: Reducer<NetworkModelState>;
  };
}

/**
 * 生成物理网络、逻辑子网组成的树
 * @param allNetworks 所有的物理网络列表
 * @param allLogicalSubnets 所有的逻辑子网的列表
 * @returns 网络组成是树
 */
export const buildNetworkTree = (
  allNetworks: INetwork[],
  allLogicalSubnets: ILogicalSubnet[],
): INetworkTreeData[] => {
  if (allNetworks.length === 0) {
    return [];
  }

  return allNetworks.map((item) => {
    const subnetList = allLogicalSubnets
      .filter((subnet) => subnet.networkId === item.id)
      .map((subnet) => ({
        title: subnet.name,
        key: subnet.id,
        value: subnet.id,
      }));

    return {
      title: item.name,
      key: item.id,
      value: item.id,
      children: subnetList,
    };
  });
};

export default modelExtend(model, {
  namespace: 'networkModel',
  state: {
    networkTree: [] as INetworkTreeData[],
    allNetworks: [] as INetwork[],
    allNetworkMap: {},
    networkDetail: {} as INetwork,
    usedNetifs: [],

    networkIngestPolicy: [] as INetworkPolicy[],
    networkApplicationPolicy: [] as INetworkPolicy[],

    networkTopology: {
      topology: '',
      metric: '',
    },
  },
  effects: {
    *queryNetworkTree(_, { put }) {
      // @ts-ignore
      const allNetworks = yield put.resolve({
        type: 'queryAllNetworks',
        payload: {},
      });
      // @ts-ignore
      const allLogicalSubnets = yield put.resolve({
        type: 'logicSubnetModel/queryAllLogicalSubnets',
        payload: {},
      });

      const treeData = buildNetworkTree(allNetworks, allLogicalSubnets);

      yield put({
        type: 'updateNetworkTree',
        payload: {
          networkTree: treeData,
        },
      });
    },
    *queryAllNetworks({ payload }, { call, put, select }) {
      const { success, result } = yield call(queryAllNetworks, payload);
      const allNetworks: INetwork[] = success ? result : [];
      const allNetworkMap = {};

      for (let index = 0; index < allNetworks.length; index += 1) {
        const item = allNetworks[index];
        // 计算网络总带宽
        let totalBandwidth = 0;
        const { netif } = item;
        if (Array.isArray(netif)) {
          totalBandwidth = netif.reduce((total, cur) => total + cur.specification, 0);
        }
        item.bandwidth = totalBandwidth;

        allNetworkMap[item.id] = item;
      }

      // 获取所有的逻辑子网
      const allLogicalSubnets = yield select(
        (state: ConnectState) => state.logicSubnetModel.allLogicalSubnets,
      );
      const nextNetworkTree = buildNetworkTree(allNetworks, allLogicalSubnets);

      yield put({
        type: 'updateState',
        payload: {
          allNetworks,
          allNetworkMap,
        },
      });

      yield put({
        type: 'updateNetworkTree',
        payload: {
          networkTree: nextNetworkTree,
        },
      });

      return allNetworks;
    },
    *queryNetworkDetail({ payload }, { call, put }) {
      const { success, result } = yield call(queryNetworkDetail, payload);
      let networkDetail = <INetwork>{};
      if (success) {
        networkDetail = result;
        networkDetail.extraSettings = parseObjJson(
          networkDetail.extraSettings as any,
        ) as INetwork['extraSettings'];
      }

      yield put({
        type: 'updateState',
        payload: {
          networkDetail,
        },
      });
    },
    *createNetwork({ payload }, { call, put }) {
      const { success } = yield call(createNetwork, payload);
      if (success) {
        message.success('添加成功');
        yield put({
          type: 'queryNetworkTree',
        });
      } else {
        message.error('添加失败');
      }
      return success;
    },
    *updateNetwork({ payload }, { call, put }) {
      const { success } = yield call(updateNetwork, payload);
      if (success) {
        message.success('编辑成功');
        yield put({
          type: 'queryNetworkTree',
        });
      } else {
        message.error('编辑失败');
      }
      return success;
    },
    *deleteNetwork({ payload }, { call, put }) {
      const response = yield call(deleteNetwork, payload);
      const { success } = response;
      if (success) {
        message.success('删除成功');
        yield put({
          type: 'queryNetworkTree',
        });
      } else {
        message.error('删除失败');
      }
      return success;
    },
    *queryUsedNetifs({ payload }, { call, put }) {
      const { success, result } = yield call(queryNetworkNetifs, payload);
      yield put({
        type: 'updateState',
        payload: {
          usedNetifs: success ? result : [],
        },
      });
    },

    *queryNetworkIngestPolicy({ payload }, { call, put }) {
      const { success, result } = yield call(queryNetworkPolicy, {
        policyType: 'ingest',
        ...payload,
      });
      yield put({
        type: 'updateState',
        payload: {
          networkIngestPolicy: success ? result : [],
        },
      });
    },
    *updateNetworkIngestPolicy({ payload }, { call, put }) {
      const { success } = yield call(updateNetworkPolicy, { policyType: 'ingest', ...payload });
      if (success) {
        message.success('编辑成功');
        yield put({
          type: 'queryNetworkIngestPolicy',
        });
      } else {
        message.error('编辑失败');
      }
      return success;
    },
    *queryNetworkApplicationPolicy({ payload }, { call, put }) {
      const { success, result } = yield call(queryNetworkPolicy, {
        policyType: 'filter',
        ...payload,
      });
      yield put({
        type: 'updateState',
        payload: {
          networkApplicationPolicy: success ? result : [],
        },
      });
    },
    *updateNetworkApplicationPolicy({ payload }, { call, put }) {
      const { success } = yield call(updateNetworkPolicy, { policyType: 'filter', ...payload });
      if (success) {
        message.success('编辑成功');
        yield put({
          type: 'queryNetworkApplicationPolicy',
        });
      } else {
        message.error('编辑失败');
      }
      return success;
    },
    *queryNetworkTopology(_params, { call, put }) {
      const { success, result } = yield call(queryNetworkTopology);
      if (success) {
        yield put({
          type: 'updateState',
          payload: {
            networkTopology: result,
          },
        });
      } else {
        message.error('拓扑图数据查询失败');
      }
    },
    *updateNetworkTopology({ payload }, { call, put }) {
      const { success } = yield call(updateNetworkTopology, payload);
      if (success) {
        message.success('拓扑图更新成功！');
        yield put({
          type: 'queryNetworkTopology',
        });
      } else {
        message.error('拓扑图更新失败！');
      }
    },
  },
  reducers: {
    updateNetworkTree: (state, { payload }) => {
      return {
        ...state,
        networkTree: payload.networkTree,
      };
    },
  },
} as NetworkModelType);
