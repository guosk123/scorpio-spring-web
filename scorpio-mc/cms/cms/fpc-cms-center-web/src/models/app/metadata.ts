import type {
  EMetadataProtocol,
  IL7Protocol,
  IL7ProtocolMap,
  IMetadataLog} from '@/pages/app/appliance/Metadata/typings';
import {
  matadataDetailKV,
} from '@/pages/app/appliance/Metadata/typings';
import {
  exportMetadataLogs,
  queryAllProtocols,
  queryMetadataLogs,
  queryMetadataTotal,
} from '@/pages/app/appliance/Metadata/service';
import { pageModel } from '@/utils/frame/model';
import modelExtend from 'dva-model-extend';
import type { Effect } from 'umi';
import { v1 as uuidv1 } from 'uuid';
import { PAGE_DEFAULT_SIZE } from '@/common/app';
import type { ConnectState } from '../connect';

/** TDS 的 ID */
const L7_PROTOCOL_TDS_ID = 645;

export interface MetadataModelState {
  /**
   * SA规则库中所有的协议
   * @description 从规则库中获取的数据
   */
  allL7ProtocolsList: IL7Protocol[];
  allL7ProtocolMap: IL7ProtocolMap;
  /**
   * 可解析成元数据的协议
   * @description 从规则库中获取
   */
  metadataProtocolsList: IL7Protocol[];
  metadataProtocolMap: IL7ProtocolMap;

  /**
   * HTTP的协议ID
   * @description 从规则库中根据名字全等匹配获取到的 http 的 ID
   */
  httpProtocolId: string;

  /** 元数据 数据 */
  protocolLogsMap: Record<EMetadataProtocol, IMetadataLog[]>;
}

export interface IMetadataModelType {
  namespace: string;
  state: MetadataModelState;
  effects: {
    queryAllProtocols: Effect;
    queryMetadataLogs: Effect;
    queryMetadataTotal: Effect;
    exportMetadataLogs: Effect;
  };
}

export default modelExtend(pageModel, {
  namespace: 'metadataModel',

  state: {
    // 所有可解析的流日志协议
    allL7ProtocolsList: [],
    allL7ProtocolMap: {},

    metadataProtocolsList: [],
    metadataProtocolMap: {},

    httpProtocolId: '',

    protocolLogsMap: <MetadataModelState['protocolLogsMap']>{},
  },

  effects: {
    *queryAllProtocols(_, { call, put }) {
      const { success, result } = yield call(queryAllProtocols);
      const allL7ProtocolsList: IL7Protocol[] = success ? result : [];
      const allL7ProtocolMap = {};
      const metadataProtocolsList: IL7Protocol[] = [];
      const metadataProtocolMap = {};
      let httpProtocolId = '';
      allL7ProtocolsList.forEach((item) => {
        allL7ProtocolMap[item.protocolId] = item;
        // 寻找HTTP的协议ID
        if (item.nameText === 'HTTP' || item.nameText === 'http') {
          httpProtocolId = item.protocolId;
        }
        // 自定义应用，应用层协议支持 Other
        // 对应字典库中：
        // ID	 中文名称	英文名称 	中文描述	英文描述
        // 0	其他	OTHERS	其他协议 	Other protocols

        // 除了 0- 255 之外，还得兼容其他的一些后来解析的协议
        // 2021-06-28 新增 TDS协议，协议 ID=645
        if (
          // 不要包含其他类型的协议
          Object.keys(matadataDetailKV).includes(item.protocolId + '')
        ) {
          metadataProtocolsList.push(item);
        }
        // LDAP
        // if (item.protocolId === '596') {
        //   metadataProtocolsList.push(item);
        // }
        metadataProtocolMap[item.protocolId] = item;
      });

      yield put({
        type: 'updateState',
        payload: {
          allL7ProtocolsList,
          allL7ProtocolMap,
          metadataProtocolsList,
          metadataProtocolMap,
          httpProtocolId,
        },
      });
    },
    *queryMetadataLogs({ payload }, { call, put, select }) {
      const { success, result = {} } = yield call(queryMetadataLogs, {
        ...payload,
        page: (payload.page && payload.page - 1) || 0,
        pageSize: payload.pageSize || PAGE_DEFAULT_SIZE,
      });

      // 处理数据
      const data = success ? result.content || [] : [];
      for (let index = 0; index < data.length; index += 1) {
        data[index].id = uuidv1();
        // 处理应用分类和应用
        // @ts-ignore
        const applicationInfo = yield put.resolve({
          type: 'SAKnowledgeModel/getApplicationAndCategoryInfo',
          payload: {
            applicationId: data[index].applicationId,
          },
        });
        data[index] = {
          ...data[index],
          ...applicationInfo,
        };
      }
      if (success) {
        const oldPage = yield select((state: ConnectState) => state.metadataModel.pagination);
        const nextPage = { ...oldPage };
        nextPage.current = result.number + 1 || 1;

        yield put({
          type: 'updateState',
          payload: {
            protocolLogsMap: {
              [payload.protocol]: data,
            },
            pagination: nextPage,
          },
        });
      }
    },
    *queryMetadataTotal({ payload }, { call, put, select }) {
      const { success, result } = yield call(queryMetadataTotal, {
        ...payload,
        page: (payload.page && payload.page - 1) || 0,
        pageSize: payload.pageSize || PAGE_DEFAULT_SIZE,
      });

      // 获取老的分页信息
      const oldPage = yield select((state: ConnectState) => state.metadataModel.pagination);
      const nextPage = { ...oldPage };

      if (success) {
        // 更新分页信息
        nextPage.total = result.total || 0;
      }

      yield put({
        type: 'updateState',
        payload: {
          pagination: nextPage,
        },
      });
    },
    *exportMetadataLogs({ payload }, { call }) {
      yield call(exportMetadataLogs, payload);
    },
  },
} as IMetadataModelType);
