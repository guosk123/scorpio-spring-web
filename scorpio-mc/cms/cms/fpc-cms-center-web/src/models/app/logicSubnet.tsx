import {
  createLogicalSubnet,
  deleteLogicalSubnet,
  queryAllLogicalSubnets,
  queryLogicalSubnetDetail,
  updateLogicalSubnet
} from '@/pages/app/Configuration/LogicalSubnet/service';
import type {
  ILogicalSubnet,
  ILogicalSubnetMap
} from '@/pages/app/Configuration/LogicalSubnet/typings';
import { LOGICAL_SUBNET_NAME_OBJ } from '@/pages/app/Configuration/LogicalSubnet/typings';
import { model } from '@/utils/frame/model';
import { message } from 'antd';
import modelExtend from 'dva-model-extend';
import type { Effect } from 'umi';
import type { ConnectState } from '../connect';
import { buildNetworkSensorTree } from './network';

export interface LogicalSubnetModelState {
  allLogicalSubnets: ILogicalSubnet[];
  allLogicalSubnetMap: ILogicalSubnetMap;
  logicalSubnetDetail: ILogicalSubnet;
}

export interface LogicalSubnetModelType {
  namespace: string;
  state: LogicalSubnetModelState;
  effects: {
    queryAllLogicalSubnets: Effect;
    queryLogicalSubnetDetail: Effect;
    createLogicalSubnet: Effect;
    updateLogicalSubnet: Effect;
    deleteLogicalSubnet: Effect;
  };
}

export default modelExtend(model, {
  namespace: 'logicSubnetModel',
  state: {
    allLogicalSubnets: [] as ILogicalSubnet[],
    allLogicalSubnetMap: {} as ILogicalSubnetMap,
    logicalSubnetDetail: {} as ILogicalSubnet,
  },
  effects: {
    *queryAllLogicalSubnets({ payload }, { call, put, select }) {
      const { success, result } = yield call(queryAllLogicalSubnets, payload);
      const allLogicalSubnets: ILogicalSubnet[] = success ? result : [];
      const allLogicalSubnetMap = {};
      yield put({
        type: 'networkModel/queryAllNetworkSensor',
      });
      // 处理网络的名称
      const { allNetworkSensor } = yield select((state: ConnectState) => state.networkModel);

      for (let index = 0; index < allLogicalSubnets.length; index += 1) {
        const subnet = allLogicalSubnets[index];

        // 子网类型名称
        if (subnet.type && LOGICAL_SUBNET_NAME_OBJ[subnet.type]) {
          subnet.typeText = LOGICAL_SUBNET_NAME_OBJ[subnet.type];
        } else {
          subnet.typeText = subnet.type;
        }

        // 组装子网的 Map
        allLogicalSubnetMap[subnet.id] = subnet;
      }

      const nextNetworkTree = buildNetworkSensorTree(allNetworkSensor, allLogicalSubnets);

      yield put({
        type: 'updateState',
        payload: {
          allLogicalSubnets,
          allLogicalSubnetMap,
        },
      });

      yield put({
        type: 'networkModel/updateNetworkSensorTree',
        payload: {
          networkSensorTree: nextNetworkTree,
        },
      });

      return allLogicalSubnets;
    },
    *queryLogicalSubnetDetail({ payload }, { call, put }) {
      const { success, result } = yield call(queryLogicalSubnetDetail, payload);
      const subnet = success ? result : {};
      // 子网类型名称
      if (subnet.type && LOGICAL_SUBNET_NAME_OBJ[subnet.type]) {
        subnet.typeText = LOGICAL_SUBNET_NAME_OBJ[subnet.type];
      } else {
        subnet.typeText = subnet.type;
      }

      yield put({
        type: 'updateState',
        payload: {
          logicalSubnetDetail: subnet,
        },
      });
    },
    *createLogicalSubnet({ payload }, { call, put }) {
      const { success } = yield call(createLogicalSubnet, payload);
      console.log(success);
      if (success) {
        message.success('添加成功');
        // 重新拉取网络
        yield put({
          type: 'networkModel/queryNetworkSensorTree',
        });
      } else {
        message.error('添加失败');
      }
      return success;
    },
    *updateLogicalSubnet({ payload }, { call, put }) {
      const { success } = yield call(updateLogicalSubnet, payload);
      if (success) {
        message.success('编辑成功');
        // 重新拉取网络
        yield put({
          type: 'networkModel/queryNetworkSensorTree',
        });
      } else {
        message.error('编辑失败');
      }
      return success;
    },
    *deleteLogicalSubnet({ payload }, { call, put }) {
      const response = yield call(deleteLogicalSubnet, payload);
      const { success } = response;
      if (success) {
        message.success('删除成功');
        // 重新拉取网络
        yield put({
          type: 'networkModel/queryNetworkSensorTree',
        });
        yield put({
          type: 'logicSubnetModel/queryAllLogicalSubnets',
        });
      } else {
        message.error('删除失败');
      }
      return success;
    },
  },
} as LogicalSubnetModelType);
