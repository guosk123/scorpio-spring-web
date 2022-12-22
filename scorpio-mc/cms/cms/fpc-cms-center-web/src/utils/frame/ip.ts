import { ipV4Regex, ipV6Regex } from './regex';
import { isIpv6, isIpv4 } from './utils';

/**
 * 检查是否是CIDR格式的IP地址
 */
export function isCidrWithoutType(ip: string) {
  if (!ip || ip.indexOf('/') === -1) {
    return false;
  }
  const ipAndCidr = ip.split('/');
  if (ipAndCidr.length !== 2) {
    return false;
  }
  const [ipAddress, mask] = ipAndCidr;
  if (!ipAddress || isNaN(+mask)) {
    return false;
  }
  const maskNum = +mask;
  const isV4 = ipV4Regex.test(ipAddress);
  const isV6 = ipV6Regex.test(ipAddress);
  if (isV4 && maskNum > 0 && maskNum <= 32) return true;
  if (isV6 && maskNum > 0 && maskNum <= 128) return true;

  return false;
}

/**
 *
 * @param ipv6 省略起始0的ipv6地址
 * @returns 补全的ipv6地址
 */
export function ipv6AddrFormart(ipv6: string) {
  let ipv6Address = null;
  const parts = ipv6.split(':');
  for (let i = 0; i < parts.length; i++) {
    if (parts[i].length < 4) {
      parts[i] = parts[i].padStart(4, '0');
    }
    if (i === 0) {
      ipv6Address = parts[i];
    } else {
      ipv6Address += ':' + parts[i];
    }
  }

  return ipv6Address;
}

/**
 *
 * @param ip 简化的ipv6地址
 * @returns 去简化的ipv6地址
 */
export function convert2CompleteIpv6(ip: string) {
  let ipv6 = ip;
  const index = ip.indexOf('::');
  if (index > 0) {
    const size = 8 - (ipv6.split(':').length - 1);
    let tmp = '';
    for (let i = 0; i < size; i++) {
      tmp += ':0000';
    }
    tmp += ';';
    ipv6 = ip.replace('::', tmp);
  } else if (index === 0) {
    if (ip === '::') {
      ipv6 = '0000:0000:0000:0000:0000:0000:0000:0000';
    } else {
      ipv6 = ip.replace('::', '0000:0000:0000:0000:0000:0000:0000:');
    }
  }
  const ipv6Address = ipv6AddrFormart(ipv6);
  return ipv6Address;
}

export function ipIsEqual(a: string, b: string) {
  let aIpType;
  let bIpType;
  if (isIpv4(a)) {
    aIpType = 'ipv4';
  }
  if (isIpv6(a)) {
    aIpType = 'ipv6';
  }

  if (isIpv4(b)) {
    bIpType = 'ipv4';
  }
  if (isIpv6(a)) {
    bIpType = 'ipv6';
  }

  if (aIpType === undefined || bIpType === undefined) {
    return false;
  }

  if (aIpType !== bIpType) {
    return false;
  }

  if (aIpType === 'ipv4') {
    return a === b;
  }

  return convert2CompleteIpv6(a) === convert2CompleteIpv6(b);
}

/**
 * 判断是否是IP
 * @param text stirng
 */
 export function isIpAddress(text: string) {
  if (!text) {
    return false;
  }
  return ipV4Regex.test(text) || ipV6Regex.test(text);
}
