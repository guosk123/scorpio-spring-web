/* eslint-disable import/prefer-default-export */
/* eslint-disable no-param-reassign */
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import { DEVICE_DISK_STATUS } from '@/common/dict';
import ajax from '@/utils/frame/ajax';
import moment from 'moment';

/**
 * 获取指定统计项的值
 * @param {String} metricName
 * 例如
 * - cpu_used_pct, cpu使用率
 * - memory_used_pct, 内存使用率
 * - fs_system_used_pct, 系统盘使用率
 * - fs_index_used_pct, 索引盘使用率
 * - fs_data_used_pct, 数据盘使用率
 * - fs_data_used_byte, 数据盘使用大小（Bytes）
 * - fs_data_total_byte,数据盘总大小（Bytes）
 * - fs_cache_used_pct, 缓存使用率
 * - fs_cache_used_byte, 缓存使用的大小（Bytes）
 * - fs_cache_total_byte, 缓存总大小（Bytes）
 * - fs_store_total_byte, 数据存储空间总大小（Bytes）
 * - data_oldest_time, 最早报文时间
 * - data_last24_total_byte, 近24小时存储报文的大小（Bytes）
 * - data_predict_total_day,预计可存储的总天数
 * - cache_file_avg_byte 查询缓存任务的平均大小（Bytes）
 * @param {Array} metrics
 */
export function getMetricsValue(metricName, metrics) {
  if (!Array.isArray(metrics) || metrics.length === 0) {
    return '';
  }
  const target = metrics.find((item) => item.metricName === metricName);

  if (!target) {
    return '';
  }

  if (metricName === 'data_oldest_time') {
    return moment(target.metricValue * 1000).format('YYYY-MM-DD HH:mm:ss');
  }

  return target.metricValue.replace(/%/, '');
}

/**
 * 获取硬盘状态信息
 * @param {String} state
 */
export function getDiskStateInfo(state) {
  const result = DEVICE_DISK_STATUS.find((item) => item.key === state);
  if (result) return result;
  return {
    key: '未知状态',
    label: '未知状态',
    status_color: '#f2f4f5',
    color: '#f2f4f5',
  };
}

// BPF实时校验
export function bpfValid(rule, value, callback) {
  if (!value) {
    callback();
    return;
  }

  // base64 加密
  // @see: https://caniuse.com/#search=btoa
  // @see: https://developer.mozilla.org/zh-CN/docs/Web/API/WindowBase64/btoa
  // 不能直接用于加密 unicode 字符
  let valueToBase64 = window.btoa(unescape(encodeURIComponent(value)));
  // fix: 解决base64通过http传输后+变空格的问题
  // 1. 前端传送base64前把字串中的+先替换为编码后的
  // 2. 也可以后台接收的时候把空格全都替换为加号
  valueToBase64 = valueToBase64.replace(/\+/g, '%2B');

  ajax(`${API_VERSION_PRODUCT_V1}/appliance/bpf-rule-verifications?bpf=${valueToBase64}`).then(
    (response) => {
      const { success, result } = response;
      if (success) {
        if (result === true) {
          callback();
        } else {
          callback('不是标准的BPF语法,请重新填写!');
          return;
        }
      } else {
        callback('校验失败，请稍候再试');
        return;
      }

      callback();
    },
  );
}

export function validateBpfStr(value) {
  if (!value) {
    Promise.reject();
    return;
  }

  // base64 加密
  // @see: https://caniuse.com/#search=btoa
  // @see: https://developer.mozilla.org/zh-CN/docs/Web/API/WindowBase64/btoa
  // 不能直接用于加密 unicode 字符
  let valueToBase64 = window.btoa(unescape(encodeURIComponent(value)));
  // fix: 解决base64通过http传输后+变空格的问题
  // 1. 前端传送base64前把字串中的+先替换为编码后的
  // 2. 也可以后台接收的时候把空格全都替换为加号
  valueToBase64 = valueToBase64.replace(/\+/g, '%2B');

  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/bpf-rule-verifications?bpf=${valueToBase64}`).then(
    (response) => {
      const { success, result } = response;
      if (success) {
        if (result === true) {
          return Promise.resolve();
        } else {
          return Promise.reject('不是标准的BPF语法,请重新填写!');
        }
      }

      return Promise.reject('校验失败，请稍候再试');
    },
  );
}
