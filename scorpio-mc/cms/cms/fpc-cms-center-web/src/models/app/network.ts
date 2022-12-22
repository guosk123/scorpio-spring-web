import type { ILogicalSubnet } from '@/pages/app/Configuration/LogicalSubnet/typings';
import {
  queryNetworkPolicy,
  updateNetworkPolicy,
  createNetworkPolicy,
  queryNetworkSensors,
  queryNetworkSensorById,
  queryNetworkInSensorListById,
  deleteSensorNetwork,
  createAllSensorNetworks,
  queryNetworkGroups,
  queryNetworkGroupById,
  createNetworkGroup,
  updateNetworkGroup,
  deleteNetworkGroup,
  deleteBatchNetworkGroup,
} from '@/pages/app/Configuration/Network/service';
import type {
  INetwork,
  INetworkMap,
  INetworkSensorMap,
  INetworkNetif,
  INetworkPolicy,
  INetworkSensor,
  INetworkGroup,
  INetworkGroupMap,
} from '@/pages/app/Configuration/Network/typings';
import { ESensorStatus } from '@/pages/app/Configuration/Network/typings';
import type { INetworkTopology } from '@/services/app/networkTopology';
import { queryNetworkTopology, updateNetworkTopology } from '@/services/app/networkTopology';
import { model } from '@/utils/frame/model';
import { message } from 'antd';
import type { Effect } from 'umi';
import modelExtend from 'dva-model-extend';
import type { Reducer } from 'redux';

/** 物理网络、逻辑子网组成的树 */
export interface INetworkTreeData {
  key: string;
  value: string;
  title: string;
  label?: string;
  disabled?: boolean;
  children?: INetworkTreeData[];
  status?: '0' | '1';
  statusDetail?: string;
}

export interface NetworkModelState {
  /* 探针网络 */
  allNetworkSensor: INetworkSensor[];
  /** 探针网络map */
  allNetworkSensorMap: INetworkSensorMap;
  /** 网络组 */
  allNetworkGroup: INetworkGroup[];
  /** 网络组map */
  allNetworkGroupMap: INetworkGroupMap;
  /** 探针网络，逻辑子网组成的树 */
  networkSensorTree: INetworkTreeData[];
  /** 网络组，探针网络组成的树 */
  networkGroupTree: INetworkTreeData[];

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
    /** 获取探针下的网络 */
    queryAllNetworkSensor: Effect;
    /** 查单条探针网络的数据 */
    queryNetworkSensorById: Effect;
    /** 查询某个探针下的网络 */
    queryNetworkInSensorListById: Effect;
    /** 删除探针网络 */
    deleteSensorNetwork: Effect;
    /** 添加所有探针下的网络 */
    createAllSensorNetworks: Effect;

    /** 获取网络组 */
    queryAllNetworkGroups: Effect;
    /** 查询单条网络组 */
    queryNetworkGroupById: Effect;
    /** 创建网络组 */
    createNetworkGroup: Effect;
    /** 编辑网络组 */
    updateNetworkGroup: Effect;
    /** 删除网络组 */
    deleteNetworkGroup: Effect;
    /** 批量删除网络组 */
    deleteBatchNetworkGroup: Effect;

    /** 获取探针网络，逻辑子网组成的树 */
    queryNetworkSensorTree: Effect;

    /** 获取网络组，逻辑子网组成的树 */
    queryNetworkGroupTree: Effect;

    /** 过滤策略相关 */
    /** 查询捕获过滤策略 */
    queryNetworkIngestPolicy: Effect;
    /** 新建捕获过滤策略 */
    createNetworkingestPolicy: Effect;
    /** 更新捕获过滤策略 */
    updateNetworkIngestPolicy: Effect;
    /** 查询存储过滤策略*/
    queryNetworkApplicationPolicy: Effect;
    /** 更新存储过滤策略*/
    updateNetworkApplicationPolicy: Effect;
    /** 新建存储过滤策略 */
    createNetworkApplicationPolicy: Effect;
    /** 网络拓扑图 */
    queryNetworkTopology: Effect;
    updateNetworkTopology: Effect;
  };
  reducers: {
    updateNetworkSensorTree: Reducer<NetworkModelState>;
    updateNetworkGroupTree: Reducer<NetworkModelState>;
  };
}

/**
 * 生成探针网络、逻辑子网组成的树
 * @param allNetworkSensors 所有的探针网络列表
 * @param allLogicalSubnets 所有的逻辑子网的列表
 * @returns 网络组成是树
 */
export const buildNetworkSensorTree = (
  allNetworkSensors: INetworkSensor[],
  allLogicalSubnets: ILogicalSubnet[],
): INetworkTreeData[] => {
  if (allNetworkSensors.length === 0) {
    return [];
  }

  return allNetworkSensors.map((item) => {
    const subnetList = allLogicalSubnets
      .filter((subnet) => subnet.networkInSensorIds?.includes(item.networkInSensorId))
      .map((subnet) => ({
        title: subnet.name,
        key: subnet.id,
        value: subnet.id,
      }));
    return {
      title: `${item.sensorName ? `${item.name}(${item.sensorName})` : item.name}${
        item.status === ESensorStatus.OFFLINE ? '(离线)' : ''
      }`,
      key: item.networkInSensorId,
      value: item.networkInSensorId,
      children: subnetList,
    };
  });
};

/**
 * 生成网络组,探针网络组成的树
 * @param allNetworkGroups 所有的逻辑子网的列表
 * @param allNetworkSensors 所有的探针网络列表
 * @returns 网络组成是树
 */
export const buildNetworkGroupTree = (
  allNetworkGroups: INetworkGroup[],
  allNetworkSensors: INetworkSensor[],
): INetworkTreeData[] => {
  if (allNetworkSensors.length === 0) {
    return [];
  }

  return allNetworkGroups.map((item) => {
    const subnetList = allNetworkSensors
      .filter((networkSensor) => item.networkInSensorIds?.includes(networkSensor.networkInSensorId))
      .map((networkSensor) => ({
        title: networkSensor.name,
        key: networkSensor.id,
        value: networkSensor.id,
      }));

    return {
      title: item.name,
      key: item.id,
      value: item.id,
      children: subnetList,
    };
  });
};

/**
 * 生成物理网络、逻辑子网组成的树
 * @param allNetworkSensors 所有的探针网络列表
 * @param allLogicalSubnets 所有的逻辑子网的列表
 * @returns 网络组成是树
 */
export const buildNetworkTree = (
  allNetworkSensors: INetwork[],
  allLogicalSubnets: ILogicalSubnet[],
): INetworkTreeData[] => {
  if (allNetworkSensors.length === 0) {
    return [];
  }

  return allNetworkSensors.map((item) => {
    const subnetList = allLogicalSubnets
      .filter((subnet) => subnet.networkInSensorIds === item.networkInSensorId)
      .map((subnet) => ({
        title: subnet.name,
        key: subnet.id,
        value: subnet.id,
      }));

    return {
      title: item.name,
      key: item.networkInSensorId,
      value: item.networkInSensorId,
      children: subnetList,
    };
  });
};

export default modelExtend(model, {
  namespace: 'networkModel',
  state: {
    allNetworkSensor: [] as INetworkSensor[],
    allNetworkSensorMap: {},
    allNetworkGroup: [] as INetworkGroup[],
    allNetworkGroupMap: {},
    networkSensorTree: [] as INetworkTreeData[],
    networkTree: [] as INetworkTreeData[],
    networkGroupTree: [] as INetworkTreeData[],
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
    /** 探针网络相关 */
    *queryAllNetworkSensor(_, { put }) {
      const { success, result } = yield queryNetworkSensors();
      if (!success) {
        return;
      }
      const allNetworkSensorMap = {};

      result.forEach((item: INetworkSensor) => {
        allNetworkSensorMap[item.networkInSensorId] = item;
      });

      yield put({
        type: 'updateState',
        payload: {
          allNetworkSensor: result,
          allNetworkSensorMap,
        },
      });
      return result;
    },
    *queryNetworkSensorById({ payload }, { call }) {
      const { success, result } = yield call(queryNetworkSensorById, payload);
      if (!success) {
        return;
      }
      return result;
    },
    *queryNetworkInSensorListById({ payload }, { call }) {
      const { success, result } = yield call(queryNetworkInSensorListById, payload);
      if (!success) {
        return;
      }
      return result;
    },
    *deleteSensorNetwork({ payload }, { call, put }) {
      const { success } = yield call(deleteSensorNetwork, payload);
      if (success) {
        message.success('删除成功!');
        yield put({
          type: 'networkModel/queryAllNetworkSensor',
        });
      }
      return success;
    },
    *createAllSensorNetworks(_, { call, put }) {
      const { success } = yield call(createAllSensorNetworks);
      if (success) {
        message.success('添加成功!');
        yield put({
          type: 'networkModel/queryAllNetworkSensor',
        });
      }
      return success;
    },
    /** 网络组相关 */
    *queryAllNetworkGroups(_, { put }) {
      const { success, result } = yield queryNetworkGroups();
      if (!success) {
        return;
      }
      const allNetworkGroupMap = {};
      result.forEach((item: INetworkGroup) => {
        allNetworkGroupMap[item.id] = item;
      });
      yield put({
        type: 'updateState',
        payload: {
          allNetworkGroup: result,
          allNetworkGroupMap,
        },
      });
      return result;
    },
    *queryNetworkGroupById({ payload }, { call }) {
      const { success, result } = yield call(queryNetworkGroupById, payload);
      if (!success) {
        return;
      }
      return result;
    },
    *createNetworkGroup({ payload }, { call }) {
      const { success } = yield call(createNetworkGroup, payload);
      if (success) {
        message.success('创建成功!');
      }
      return success;
    },
    *updateNetworkGroup({ payload }, { call }) {
      const { success } = yield call(updateNetworkGroup, payload);
      if (success) {
        message.success('保存成功!');
      }
      return success;
    },
    *deleteNetworkGroup({ payload }, { call, put }) {
      const { success } = yield call(deleteNetworkGroup, payload);
      if (success) {
        message.success('删除成功!');
        yield put({
          type: 'networkModel/queryAllNetworkGroups',
        });
      }
      return success;
    },
    *deleteBatchNetworkGroup({ payload }, { call, put }) {
      const { success } = yield call(deleteBatchNetworkGroup, payload);
      if (success) {
        message.success('删除成功!');
        yield put({
          type: 'networkModel/queryAllNetworkGroups',
        });
      }
      return success;
    },
    /** 获取探针网络，逻辑子网树 */
    *queryNetworkSensorTree(_, { put }) {
      // @ts-ignore
      const allNetworkSensors = yield put.resolve({
        type: 'queryAllNetworkSensor',
        payload: {},
      });
      // @ts-ignore
      const allLogicalSubnets = yield put.resolve({
        type: 'logicSubnetModel/queryAllLogicalSubnets',
        payload: {},
      });
      const treeData = buildNetworkSensorTree(allNetworkSensors, allLogicalSubnets);

      yield put({
        type: 'updateNetworkSensorTree',
        payload: {
          networkSensorTree: treeData,
        },
      });
      return treeData;
    },
    /** 获取网络组，探针网络树 */
    *queryNetworkGroupTree(_, { put }) {
      // @ts-ignore
      const allNetworkGroups = yield put.resolve({
        type: 'queryAllNetworkGroups',
        payload: {},
      });
      // @ts-ignore
      const allNetworkSensors = yield put.resolve({
        type: 'queryAllNetworkSensor',
        payload: {},
      });

      const treeData = buildNetworkGroupTree(allNetworkGroups, allNetworkSensors);
      yield put({
        type: 'updateNetworkGroupTree',
        payload: {
          networkGroupTree: treeData,
        },
      });
      return treeData;
    },
    /** 过滤策略相关 */
    /** 查询捕获过滤策略 */
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
    /** 更新捕获过滤策略 */
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
    /** 新建捕获过滤策略 */
    *createNetworkingestPolicy({ payload }, { call, put }) {
      const { success } = yield call(createNetworkPolicy, { policyType: 'ingest', ...payload });
      if (success) {
        message.success('创建成功');
        yield put({
          type: 'queryNetworkIngestPolicy',
        });
      } else {
        message.error('创建失败');
      }
      return success;
    },
    /** 查询存储过滤策略 */
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
    /** 新建存储过滤策略 */
    *createNetworkApplicationPolicy({ payload }, { call, put }) {
      const { success } = yield call(createNetworkPolicy, { policyType: 'filter', ...payload });
      if (success) {
        message.success('创建成功');
        yield put({
          type: 'queryNetworkApplicationPolicy',
        });
      } else {
        message.error('创建失败');
      }
      return success;
    },
    /** 更新存储过滤策略 */
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

    /** 网络拓扑相关 */
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
    updateNetworkSensorTree: (state, { payload }) => {
      return {
        ...state,
        networkSensorTree: payload.networkSensorTree,
      };
    },
    updateNetworkGroupTree: (state, { payload }) => {
      return {
        ...state,
        networkGroupTree: payload.networkGroupTree,
      };
    },
  },
} as NetworkModelType);
