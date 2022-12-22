import modelExtend from 'dva-model-extend';
import { model } from '@/utils/frame/model';
import type { Effect } from 'umi';
import {
  ITransmitMail,
  ITransmitSyslog,
  ITransmitSmtp,
} from '@/pages/app/configuration/Transmit/typings';
import {
  queryTransmitMail,
  queryTransmitSyslog,
  queryTransmitSyslogById,
  createSyslogConfiguration,
  batchDeleteTransmitSyslog,
  batchDeleteTransmitMail,
  querySmtpConfiguration,
  createSmtpConfiguration,
  testSmtpConnection,
  queryTransmitMailById,
  createTransmitMail,
  updateTransmitMail,
  updateSyslogConfiguration,
} from '@/pages/app/configuration/Transmit/services';
import { message } from 'antd';

export interface TransmitModelState {
  /** 邮件外发配置 */
  allTransmitMail: ITransmitMail[];
  /** 邮件外发配置详细信息 */
  transmitMailDetail: ITransmitMail;
  /** syslog外发配置 */
  allTransmitSyslog: ITransmitSyslog[];
  /** smtp配置信息 */
  transmitSmtp: ITransmitSmtp;
  /** syslog外发详细信息 */
  transmitSyslogDetail: ITransmitSyslog;
}

interface TransmitModelType {
  namespace: string;
  state: TransmitModelState;
  effects: {
    /** 邮件外发配置相关 */
    /** 获取邮件外发配置 */
    queryAllTransmitMail: Effect;
    /** 根据id查询单条邮件外发配置 */
    queryTransmitMailDetail: Effect;
    /** 创建邮件外发配置 */
    createTransmitMail: Effect;
    /** 编辑邮件外发配置 */
    updateTransmitMail: Effect;
    /** 批量删除邮件外发配置 */
    batchDeleteTransmitMail: Effect;

    /** smtp外发配置相关 */
    /** 查询smtp配置信息 */
    querySmtpConfiguration: Effect;
    /** 创建smtp */
    createSmtpConfiguration: Effect;
    /** 测试smtp连接 */
    testSmtpConnection: Effect;

    /** syslog外发配置相关 */
    /** 获取syslog外发配置 */
    queryAllTransmitSyslog: Effect;
    /** 获取单条syslog外发配置 */
    queryTransmitSyslogDetail: Effect;
    /** 创建syslog外发配置表单 */
    createSyslogConfiguration: Effect;
    /** 编辑syslog外发配置 */
    updateSyslogConfiguration: Effect;
    /** 批量删除syslog外发配置 */
    batchDeleteTransmitSyslog: Effect;
  };
}

export default modelExtend(model, {
  namespace: 'transmitModel',
  state: {
    allTransmitMail: [] as ITransmitMail[],
    allTransmitSyslog: [] as ITransmitSyslog[],
    transmitSmtp: {} as ITransmitSmtp,
    transmitMailDetail: {} as ITransmitMail,
    transmitSyslogDetail: {} as ITransmitSyslog,
  },
  effects: {
    *queryAllTransmitMail(_, { call, put }) {
      const { success, result } = yield call(queryTransmitMail);
      if (!success) {
        return;
      }
      yield put({
        type: 'updateState',
        payload: {
          allTransmitMail: success ? result : {},
        },
      });
    },
    /** 根据id查询单条邮件外发配置 */
    *queryTransmitMailDetail({ payload }, { put, call }) {
      const { success, result } = yield call(queryTransmitMailById, payload);
      if (!success) {
        return;
      }
      yield put({
        type: 'updateState',
        payload: {
          transmitMailDetail: success ? result : {},
        },
      });
      return result;
    },
    /** 创建邮件外发配置 */
    *createTransmitMail({ payload }, { call }) {
      const { success, result } = yield call(createTransmitMail, payload);
      if (!success) {
        message.error('保存失败!');
        return;
      }
      message.success('保存成功!');
      return result;
    },
    /** 编辑邮件外发配置 */
    *updateTransmitMail({ payload }, { call }) {
      const { success, result } = yield call(updateTransmitMail, payload);
      if (!success) {
        message.error('保存失败!');
        return;
      }
      message.success('保存成功!');
      return result;
    },
    /** 批量删除邮件外发配置 */
    *batchDeleteTransmitMail({ payload }, { call, put }) {
      const { success, result } = yield call(batchDeleteTransmitMail, payload);
      if (!success) {
        message.error('删除失败!');
        return;
      }
      message.success('删除成功!');
      yield put({
        type: 'transmitModel/queryAllTransmitMail',
      });
      return result;
    },
    /** 查询smtp配置信息 */
    *querySmtpConfiguration(_, { call, put }) {
      const { success, result } = yield call(querySmtpConfiguration);
      if (!success) {
        return;
      }
      yield put({
        type: 'updateState',
        payload: {
          transmitSmtp: success ? result : {},
        },
      });
    },
    /** 创建smtp */
    *createSmtpConfiguration({ payload }, { call }) {
      const { success, result } = yield call(createSmtpConfiguration, payload);
      if (!success) {
        message.error('保存失败!');
        return;
      }
      message.success('保存成功!');
      return result;
    },
    /** 测试smtp连接 */
    *testSmtpConnection({ payload }, { call }) {
      const { success, result } = yield call(testSmtpConnection, payload);
      if (!success) {
        message.error('测试失败!');
        return;
      }
      message.success('测试成功!');
      return result;
    },
    /** syslog外发配置相关 */
    *queryAllTransmitSyslog(_, { call, put }) {
      const { success, result } = yield call(queryTransmitSyslog);
      if (!success) {
        return;
      }
      yield put({
        type: 'updateState',
        payload: {
          allTransmitSyslog: success ? result : {},
        },
      });
    },
    *queryTransmitSyslogDetail({ payload }, { call, put }) {
      const { success, result } = yield call(queryTransmitSyslogById, payload);
      if (!success) {
        return;
      }
      yield put({
        type: 'updateState',
        payload: {
          transmitSyslogDetail: success ? result : {},
        },
      });
      return result;
    },
    *createSyslogConfiguration({ payload }, { call, put }) {
      const { success, result } = yield call(createSyslogConfiguration, payload);
      if (!success) {
        message.error('保存失败!');
        return;
      }
      message.success('保存成功!');
      yield put({
        type: 'transmitModel/queryAllTransmitSyslog',
      });
      return result;
    },
    *updateSyslogConfiguration({ payload }, { call, put }) {
      const { success, result } = yield call(updateSyslogConfiguration, payload);
      if (!success) {
        message.error('保存失败!');
        return;
      }
      message.success('保存成功!');
      return result;
    },
    *batchDeleteTransmitSyslog({ payload }, { call, put }) {
      const { success, result } = yield call(batchDeleteTransmitSyslog, payload);
      if (!success) {
        message.error('删除失败!');
        return;
      }
      message.success('删除成功!');
      yield put({
        type: 'transmitModel/queryAllTransmitSyslog',
      });
      return result;
    },
  },
} as TransmitModelType);
