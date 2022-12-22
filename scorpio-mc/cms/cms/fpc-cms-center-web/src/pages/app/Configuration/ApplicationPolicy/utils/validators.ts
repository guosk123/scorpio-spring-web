import { ipV4Regex, ipV6Regex } from "@/utils/utils";
import type { RuleObject } from "antd/lib/form";
import type { StoreValue } from "antd/lib/form/interface";
import { PORT_MAX_NUMBER, PORT_MIN_NUMBER, VLANID_MAX_NUMBER, VLANID_MIN_NUMBER } from "../../IngestPolicy/components/IngestPolicyForm";

export type Validator = (
  rule: RuleObject,
  value: StoreValue,
  callback: (error?: string) => void,
) => Promise<void | any> | void;

/**
 * 校验IP、IP段，支持 IP v6
 * @param {*} rule
 * @param {*} value
 * @param {*} callback
 *
 * @example 192.168.1.1
 * @example 192.168.1.2/24
 * @example 2031::130f::09c0:876a:130b
 */
export const checkSourceOrDestIp: Validator = (rule, value, callback) => {
  if (!value) {
    callback();
    return;
  }

  // 如果有 - ，直接判错
  if (value.indexOf('-') > -1) {
    callback('请输入正确的IP/IP段');
  } else if (value.indexOf('/') > -1) {
    const ips = value.split('/');
    // 校验第一个 ip
    if (!ipV4Regex.test(ips[0]) && !ipV6Regex.test(ips[0])) {
      callback('请输入正确的IP/IP段。支持 IPv4 和 IPv6');
      return;
    }
    // 校验子网掩码
    // eslint-disable-next-line no-restricted-globals
    if (!ips[1] || isNaN(ips[1])) {
      callback('IP网段请填写子网掩码');
      return;
    }
    // IPv4最高支持32
    if (ipV4Regex.test(ips[0]) && (ips[1] <= 0 || ips[1] > 32)) {
      callback('IPv4子网掩码范围是(0,32]');
      return;
    }
    // IPv6最高支持128
    if (ipV6Regex.test(ips[0]) && (ips[1] <= 0 || ips[1] > 128)) {
      callback('IPv6子网掩码范围是(0,128]');
      return;
    }
  } else if (!ipV4Regex.test(value) && !ipV6Regex.test(value)) {
    callback('请输入正确的IP地址');
    return;
  }

  callback();
};

/**
 * 端口校验
 */
export const checkPort: Validator = (rule, value, callback) => {
  if (!value) {
    callback();
    return;
  }

  if (!/^[0-9-]+$/.test(value)) {
    callback('错误的端口范围');
    return;
  }

  // 支持单端口
  const portRange = value.split('-');
  const [portStart, portEnd] = portRange;
  if (portRange.length === 1) {
    if (portStart < PORT_MIN_NUMBER || portStart > PORT_MAX_NUMBER) {
      callback(`端口范围是[${PORT_MIN_NUMBER}, ${PORT_MAX_NUMBER}]`);
      return;
    }
  } else if (portRange.length === 2) {
    if (
      portStart < PORT_MIN_NUMBER ||
      portStart > PORT_MAX_NUMBER ||
      portEnd < PORT_MIN_NUMBER ||
      portEnd > PORT_MAX_NUMBER
    ) {
      callback(`端口范围是[${PORT_MIN_NUMBER}, ${PORT_MAX_NUMBER}]`);
      return;
    }

    if (+portEnd <= +portStart) {
      callback('错误的端口范围');
      return;
    }
  } else {
    callback('请输入正确的端口范围');
    return;
  }

  callback();
};

/**
 * 端口VLANID
 */
export const checkVlan: Validator = (rule, value, callback) => {
  if (!value) {
    callback();
    return;
  }

  if (!/^[0-9-]+$/.test(value)) {
    callback('错误的VLANID范围');
    return;
  }

  const vlanRange = value.split('-');
  const [vlanStart, vlanEnd] = vlanRange;
  if (vlanRange.length === 1) {
    if (vlanStart < VLANID_MIN_NUMBER || vlanStart > VLANID_MAX_NUMBER) {
      callback(`VLANID范围是[${VLANID_MIN_NUMBER}, ${VLANID_MAX_NUMBER}]`);
      return;
    }
  } else if (vlanRange.length === 2) {
    if (
      vlanStart < VLANID_MIN_NUMBER ||
      vlanStart > VLANID_MAX_NUMBER ||
      vlanEnd < VLANID_MIN_NUMBER ||
      vlanEnd > VLANID_MAX_NUMBER
    ) {
      callback(`VLANID范围是[${VLANID_MIN_NUMBER}, ${VLANID_MAX_NUMBER}]`);
      return;
    }

    if (+vlanEnd <= +vlanStart) {
      callback('错误的VLANID范围');
      return;
    }
  } else {
    callback('请输入正确的VLANID范围');
    return;
  }

  callback();
};