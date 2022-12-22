import application from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';
import type { ITransmitMail, ITransmitSmtp, ITransmitSyslog } from './typings';
import { stringify } from 'qs';

const { API_VERSION_PRODUCT_V1 } = application;

export interface ISmtpConnectionParams {
  loginUser: string;
  loginPassword: string;
  smtpServer: string;
}

/** 邮件外发配置相关 */
/** 用来获取邮件外发配置 */
export async function queryTransmitMail() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/mail-sendup-rule`);
}

/** 用来获取单条邮件外发配置 */
export async function queryTransmitMailById(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/mail-sendup-rule/${id}`);
}

/** 提交邮件外发配置表单 */
export async function createTransmitMail(params: ITransmitMail) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/mail-sendup-rule`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

/** 编辑邮件外发配置表单 */
export async function updateTransmitMail({ id, ...params }: ITransmitSmtp) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/mail-sendup-rule/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}
/** 批量删除邮件外发 */
export async function batchDeleteTransmitMail(ids: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/mail-sendup-rule`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
      ids,
    },
  });
}

/** smtp外发配置相关 */
/** 获取smtp外发配置表单默认信息 */
export async function querySmtpConfiguration() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/smtp-configuration`);
}

/** 提交smtp外发配置表单 */
export async function createSmtpConfiguration(params: ITransmitSmtp) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/smtp-configuration`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}

/** SMTP测试连接 */
export async function testSmtpConnection(params: ISmtpConnectionParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/smtp-test-connection?${stringify(params)}`);
}

/** syslog外发配置相关 */
/** 用来获取syslog外发配置 */
export async function queryTransmitSyslog() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/syslog-sendup-rule`);
}

/** 根据id获取单条syslog配置 */
export async function queryTransmitSyslogById(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/syslog-sendup-rule/${id}`);
}

/** 提交syslog外发配置表单 */
export async function createSyslogConfiguration(params: ITransmitSyslog) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/syslog-sendup-rule`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}
/** 编辑syslog外发配置表单 */
export async function updateSyslogConfiguration({ id, ...params }: ITransmitSyslog) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/syslog-sendup-rule/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}
/** 批量删除syslog配置 */
export async function batchDeleteTransmitSyslog(ids: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/syslog-sendup-rule`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
      ids,
    },
  });
}
