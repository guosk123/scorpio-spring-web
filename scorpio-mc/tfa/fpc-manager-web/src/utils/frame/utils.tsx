import { ONE_KILO_1000, ONE_KILO_1024 } from '@/common/dict';
import type { Route } from '@/models/connect';
import { CheckCircleTwoTone } from '@ant-design/icons';
import { Modal } from 'antd';
import $ from 'jquery';
import moment from 'moment';
import numeral from 'numeral';
import pathRegexp from 'path-to-regexp';
import { parse, stringify } from 'qs';
import { history } from 'umi';
import { ipV4Regex, ipV6Regex } from './regex';

export function fixedZero(val: number) {
  return val * 1 < 10 ? `0${val}` : val;
}

/**
 * props.route.routes
 * @param router [{}]
 * @param pathname string
 */
export const getAuthorityFromRouter = <T extends Route>(
  router: T[] = [],
  pathname: string,
): T | undefined => {
  const authority = router.find(
    ({ routes, path = '/', target = '_self' }) =>
      (path && target !== '_blank' && pathRegexp(path).exec(pathname)) ||
      (routes && getAuthorityFromRouter(routes, pathname)),
  );
  if (authority) return authority;
  return undefined;
};

export const getRouteAccess = (path: string, routeData: Route[]) => {
  let access: string | undefined;
  routeData.forEach((route) => {
    // match prefix
    if (pathRegexp(`${route.path}/(.*)`).test(`${path}/`)) {
      if (route.access) {
        access = route.access;
      }
      // exact match
      if (route.path === path) {
        access = route.access || access;
      }
      // get children authority recursively
      if (route.routes) {
        access = getRouteAccess(path, route.routes) || access;
      }
    }
  });
  return access;
};

/**
 * 根据毫秒数动态的显示持续时间
 * @param {Number} ms 毫秒数
 *
 * 这里不要使用parseInt()取整，会不准确
 * 数字太小会自动转科学记数法，这时使用parseInt()取整会舍去小数后的部分
 */
export function formatDuration(ms: number) {
  if (!ms) return '';
  const newMs = +ms;
  if (newMs === 0) return '0秒';
  const days = Math.floor(newMs / (24 * 60 * 60 * 1000));
  const hours = Math.floor((newMs % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000));
  const minutes = Math.floor((newMs % (60 * 60 * 1000)) / (60 * 1000));
  const seconds = numeral(((newMs % (60 * 1000)) / 1000).toFixed(3)).value();
  let diffText = '';
  diffText += days ? `${days}天` : '';
  diffText += hours ? `${hours}小时` : '';
  diffText += minutes ? `${minutes}分` : '';
  diffText += `${seconds}秒`;
  return diffText;
}

/**
 * 根据秒数格式化
 * @param {Number} s 秒数
 */
export function formatSeconds(s: number) {
  if (s === 0) return '0秒';
  if (!s) return '';
  if (typeof s !== 'number') {
    // eslint-disable-next-line no-param-reassign
    s = +s;
  }

  const duration = moment.duration(s);
  if (duration.asDays() >= 1) {
    return `${duration.asDays()}天`;
  }
  if (duration.asHours() >= 1) {
    return `${duration.asHours()}小时`;
  }
  if (duration.asMinutes() >= 1) {
    return `${duration.asMinutes()}分钟`;
  }
  return `${duration.asSeconds()}秒`;
}

export function getPlainNode(nodeList: any[], parentPath = '') {
  const arr: any[] = [];
  nodeList.forEach((node) => {
    const item = node;
    item.path = `${parentPath}/${item.path || ''}`.replace(/\/+/g, '/');
    item.exact = true;
    if (item.children && !item.component) {
      arr.push(...getPlainNode(item.children, item.path));
    } else {
      if (item.children && item.component) {
        item.exact = false;
      }
      arr.push(item);
    }
  });
  return arr;
}

function getRelation(str1: string, str2: string) {
  if (str1 === str2) {
    console.warn('Two path are equal!'); // eslint-disable-line
  }
  const arr1 = str1.split('/');
  const arr2 = str2.split('/');
  if (arr2.every((item, index) => item === arr1[index])) {
    return 1;
  }
  if (arr1.every((item, index) => item === arr2[index])) {
    return 2;
  }
  return 3;
}

function getRenderArr(routes: any[]) {
  let renderArr = [];
  renderArr.push(routes[0]);
  for (let i = 1; i < routes.length; i += 1) {
    // 去重
    renderArr = renderArr.filter((item) => getRelation(item, routes[i]) !== 1);
    // 是否包含
    const isAdd = renderArr.every((item) => getRelation(item, routes[i]) === 3);
    if (isAdd) {
      renderArr.push(routes[i]);
    }
  }
  return renderArr;
}

/**
 * Get router routing configuration
 * { path:{name,...param}}=>Array<{name,path ...param}>
 * @param {string} path
 * @param {routerData} routerData
 */
export function getRoutes(path: string, routerData: Record<string, any>) {
  let routes = Object.keys(routerData).filter(
    (routePath) => routePath.indexOf(path) === 0 && routePath !== path,
  );
  // Replace path to '' eg. path='user' /user/name => name
  routes = routes.map((item) => item.replace(path, ''));
  // Get the route to be rendered to remove the deep rendering
  const renderArr = getRenderArr(routes);
  // Conversion and stitching parameters
  return renderArr.map((item) => ({
    exact: !routes.some((route) => route !== item && getRelation(route, item) === 1),
    ...routerData[`${path}${item}`],
    key: `${path}${item}`,
    path: `${path}${item}`,
  }));
}

export function getPagePath() {
  return window.location.href.split('?')[0];
}

export function getPageQuery() {
  return parse(window.location.href.split('?')[1]);
}

export function getQueryPath(path = '', query = {}) {
  const search = stringify(query);
  if (search.length) {
    return `${path}?${search}`;
  }
  return path;
}

const reg =
  /((https?|ftp):\/\/)?(([^:\n\r]+):([^@\n\r]+)@)?((www\.)?([^/\n\r]+))\/?([^?\n\r]+)?\??([^#\n\r]*)?#?([^\n\r]*)/;
export function isUrl(path: string) {
  return reg.test(path);
}
const domain_reg =
  /^(?=^.{3,255}$)[a-zA-Z0-9][-_a-zA-Z0-9]{0,62}(\.[a-zA-Z0-9][-_a-zA-Z0-9]{0,62})+$/;
export function isDomain(domain: string) {
  return domain_reg.test(domain);
}

/**
 * 页面定位
 */
export function scrollTo(HTMLElementID: string) {
  if (!HTMLElementID) return false;
  if (!document.querySelector(HTMLElementID)) {
    return false;
  }

  const scrollTop = $(HTMLElementID).offset().top;
  const startTime = Date.now();

  const timer = setInterval(() => {
    const timestamp = Date.now();
    const time = timestamp - startTime;
    window.scrollTo(0, scrollTop);
    if (time >= 400) {
      clearInterval(timer);
    }
  }, 400);

  return true;
}

/**
 * 容量单位换算
 * @param {Number} bytes 字节数
 * @param {Number} decimal 保留几位小数
 * @param {Number} unit 换算单位 1000 | 1024
 *
 * @see https://www.zhihu.com/question/20255371/answer/14503510
 *
 * 关于数据单位统一的说明：
  - 存储空间方面，例如xxx 存储空间(总存储空间、离线文件存储空间)，都按照1024进行计算
  - 网络方面，根据查阅数据，标准为1000进制（在计算网络流量时），因而我们产品在所有统计接口流量、传输速率、写入速率等，都统一使用1000进制
  - 主机文件方面，统一按操作系统的1024进制，例如显示单个文件大小，多个文件大小的统计等，都统一使用1024进制，即，一个1MB的文件，其真实大小为1024*1024字节
  - fix: 2022-04-27 存储空间也统一使用 1024 存储，并格式化为 XiB，例如 GiB
   */
export function bytesToSize(bytes: number, decimal = 3, unit = ONE_KILO_1000) {
  if (bytes === 0) return unit === ONE_KILO_1000 ? '0KB' : '0KiB';
  let sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  if (unit === ONE_KILO_1024) {
    sizes = ['B', 'KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB'];
  }
  const i = Math.floor(Math.log(bytes) / Math.log(unit));
  // console.log(bytes / Math.pow(unit, i));
  return `${numeral((bytes / Math.pow(unit, i)).toFixed(decimal)).value()}${
    i >= 0 ? sizes[i] : sizes[0]
  }`;
}

/**
 * 容量换算
 * GB => TB
 * @param {*} gigabyte GB数值
 * @param {*} decimal 保留小数
 * @param {*} unit 换算单位数
 */
export function gigabyte2Terabyte(gigabyte: number, decimal = 3, unit = ONE_KILO_1000) {
  const size = Number(gigabyte);
  const suffix = unit === ONE_KILO_1000 ? 'GB' : 'GiB';
  if (size < unit) return `${size}${suffix}`;
  return bytesToSize(gigabyte * unit * unit * unit, decimal, unit);
}

/**
 * 不四舍五入保留N位小数
 * @param {Number} number 数字
 * @param {Number} length 保留几位小数
 */
const cutOutNumber = (number: number, decimal = 2) => {
  let speed = 1;
  if (decimal === 2) {
    speed = 100;
  }
  if (speed === 3) {
    speed = 1000;
  }

  return Math.floor(number * speed) / speed;
};

export function formatMicrosecond(microsecond: number) {
  // eslint-disable-next-line no-param-reassign
  microsecond = Number(microsecond);
  if (microsecond === 0) return '0μs';
  // 不足毫秒
  if (microsecond / 1000 < 1) return `${cutOutNumber(microsecond, 2)}μs`;
  // 不足秒
  if (microsecond / 1000 / 1000 < 1) return `${cutOutNumber(microsecond / 1000, 2)}ms`;
  return `${cutOutNumber(microsecond / 1000 / 1000, 2)}s`;
}

/**
 * 带宽容量单位换算
 * @param {Number} bps
 * @param {Number} decimal 保留几位小数
 */
export function convertBandwidth(bps: number, decimal = 2) {
  // eslint-disable-next-line no-param-reassign
  bps = Number(bps);
  if (bps === 0) return '0bps';
  const sizes = ['bps', 'Kbps', 'Mbps', 'Gbps', 'Tbps'];
  const unit = 1000;
  const i = Math.floor(Math.log(bps) / Math.log(unit));
  return `${numeral((bps / Math.pow(unit, i)).toFixed(decimal)).value()}${
    i >= 0 ? sizes[i] : sizes[0]
  }`;
}

/**
 * IPv4 转成数字
 * @param {*} ipv4
 */
export function ip2number(ip: string) {
  const n = ip.split('.');
  return 256 * (256 * (256 * +n[0] + +n[1]) + +n[2]) + +n[3];
}

/**
 * 校验单个 IP 地址
 * @param {*} rule
 * @param {*} value
 * @param {*} callback
 */
export function checkSingleIP(rule: any, value: string, callback: (msg?: string) => void) {
  if (!value) {
    callback('请填写IP地址');
    return;
  }

  if (!ipV4Regex.test(value)) {
    callback('请输入正确的IPv4地址');
    return;
  }

  // 排除掉 10.02.03.1 这种情况
  const arr = value.split('.');
  let flag = true;

  for (let i = 0; i < arr.length; i += 1) {
    if (Number(+arr[i]) !== +arr[i]) {
      flag = false;
      break;
    }
  }

  if (!flag) {
    callback('请输入正确的IP地址');
  } else {
    callback();
  }
}

/**
 * 是否是IPv4
 * @param value
 */
export const isIpv4 = (value: string) => ipV4Regex.test(value) || isCidr(value, 'IPv4');

/**
 * 是否是IPv6
 * @param value
 */
export const isIpv6 = (value: string) => ipV6Regex.test(value) || isCidr(value, 'IPv6');

/**
 * 校验单个 IP 地址
 * 允许输入IPv4或IPv6
 * @param {*} rule
 * @param {*} value
 * @param {*} callback
 */
export function checkIPv4AndIPv6(rule: any, value: string, callback: (msg?: string) => void) {
  if (!value) {
    callback('请填写IP地址');
    return;
  }

  if (!isIpv4(value) && !isIpv6(value)) {
    callback('请输入正确的IPv4地址或IPv6地址');
    return;
  }

  if (isIpv4(value)) {
    // 排除掉 10.02.03.1 这种情况
    const arr = value.split('.');
    let flag = true;

    for (let i = 0; i < arr.length; i += 1) {
      if (Number(+arr[i]) !== +arr[i]) {
        flag = false;
        break;
      }
    }
    if (!flag) {
      callback('请输入正确的IP地址');
      return;
    }
  }
  callback();
}

/**
 * 校验单个 IP 地址
 * 允许在输入IPv4或IPv6的时候校验
 * @param {*} rule
 * @param {*} value
 * @param {*} callback
 */
export function checkNonEssentialIPv4AndIPv6(
  rule: any,
  value: string,
  callback: (msg?: string) => void,
) {
  if (value) {
    if (!isIpv4(value) && !isIpv6(value)) {
      callback('请输入正确的IPv4地址或IPv6地址');
      return;
    }

    if (isIpv4(value)) {
      // 排除掉 10.02.03.1 这种情况
      const arr = value.split('.');
      let flag = true;

      for (let i = 0; i < arr.length; i += 1) {
        if (Number(+arr[i]) !== +arr[i]) {
          flag = false;
          break;
        }
      }
      if (!flag) {
        callback('请输入正确的IP地址');
        return;
      }
    }
  }
  callback();
}

/**
 * IP段是否包含了某个IP 地址
 * @param {Array} ipSegmentArr IP段数组
 * @param {String} ip 单个IP地址
 */
export function isIncludeSingleIp(ipSegmentArr: string[], ip: string) {
  let result = false;
  if (!ip) return false;
  const number = ip2number(ip);
  for (let i = 0; i < ipSegmentArr.length; i += 1) {
    const current = ipSegmentArr[i].split('-');
    const ip1Number = ip2number(current[0]);
    const ip2Number = ip2number(current[1]);

    if (number >= ip1Number && number <= ip2Number) {
      result = true;
      break;
    }
  }
  return result;
}

/**
 * 检查IP段内是否有交集
 * @param {Array} ipSegmentArr IP段数组
 * @param {String} ipSegment  单个IP地址段
 */
export function ipSegmentIsMixed(ipSegmentArr: string[], ipSegment: string) {
  let result = false;

  if (!ipSegment) return result;
  const split = ipSegment.split('-');
  if (split.length !== 2) return result;
  const number1 = ip2number(split[0]);
  const number2 = ip2number(split[1]);

  for (let i = 0; i < ipSegmentArr.length; i += 1) {
    const current = ipSegmentArr[i].split('-');
    const ip1Number = ip2number(current[0]);
    const ip2Number = ip2number(current[1]);

    if (!(number1 > ip2Number || number2 < ip1Number)) {
      result = true;
      break;
    }
  }

  return result;
}

/**
 * 检查新的 IP 段是否已经包含了已有的 IP 地址
 * @param {Array} ipArr IP数组
 * @param {String} ipSegment IP地址段
 */
export function checkIps(ipArr: string[], ipSegment: string) {
  let result = false;
  if (!ipSegment) return result;
  const split = ipSegment.split('-');
  if (split.length !== 2) return result;

  const number1 = ip2number(split[0]);
  const number2 = ip2number(split[1]);

  for (let i = 0; i < ipArr.length; i += 1) {
    const number = ip2number(ipArr[i]);
    if (number >= number1 && number <= number2) {
      result = true;
      break;
    }
  }

  return result;
}

export function checkTextAreaIp(rule: any, value: string, callback: (msg?: any) => void) {
  if (value) {
    const passIpArr: string[] = []; // 已经检查通过的IP
    const passIpSegmentArr: string[] = []; // 已经检查通过的IP段
    const valueArr = value.split('\n');

    try {
      if (Array.isArray(valueArr)) {
        valueArr.forEach((ip, index) => {
          const lineText = `第${index + 1}行[${ip}]: `;
          if (!ip) {
            throw new Error(`${lineText}不能为空`);
          }

          // 单 IP 校验
          if (ip.indexOf('-') === -1) {
            // 格式是否正确
            if (!ipV4Regex.test(ip)) {
              throw new Error(`${lineText}请输入正确的IP/IP段`);
            }
            // 是否重复了
            if (passIpArr.indexOf(ip) !== -1) {
              throw new Error(`${lineText}已重复`);
            }

            // 是否被包含在IP段内
            if (isIncludeSingleIp(passIpSegmentArr, ip)) {
              throw new Error(`${lineText}被包含在已有的IP段内`);
            }
            passIpArr.push(ip);
          } else {
            const ips = ip.split('-');

            for (let i = 0; i < ips.length; i += 1) {
              if (!ipV4Regex.test(ips[i])) {
                throw new Error(`${lineText}请输入正确的IP段。例，192.168.1.1-192.168.1.50`);
              }
            }

            // 校验前后2个ip的大小关系
            const ip1Number = ip2number(ips[0]);
            const ip2Number = ip2number(ips[1]);

            // 起止地址是否符合大小要求
            if (ip1Number >= ip2Number) {
              throw new Error(`${lineText}截止IP必须大于开始IP`);
            }
            // IP 段之间是否有重复
            if (ipSegmentIsMixed(passIpSegmentArr, ip)) {
              throw new Error(`${lineText}和已有的IP段存在交集`);
            }
            // 是否包括了已有的IP地址
            if (checkIps(passIpArr, ip)) {
              throw new Error(`${lineText}包含已有的IP地址`);
            }

            passIpSegmentArr.push(ip);
          }
        });
      }
    } catch (e) {
      callback(e);
    } finally {
      callback();
    }
  } else {
    callback();
  }
}

/**
 * 校验IP、IP范围
 * @param {*} rule
 * @param {*} value
 * @param {*} callback
 *
 * @example 192.168.1.1
 * @example 192.168.1.2/24
 * @example 192.168.1.1-192.168.1.50
 */
export function checkIpAndSegmentRange(rule: any, value: string, callback: (msg?: string) => void) {
  if (!value) {
    callback();
    return;
  }
  // 如果有 - ，分开校验
  if (value.indexOf('-') > -1) {
    const ips = value.split('-');

    for (let i = 0; i < ips.length; i += 1) {
      if (!ipV4Regex.test(ips[i])) {
        callback('请输入正确的IP/IP段。例，192.168.1.1-192.168.1.50');
        return;
      }
    }

    // 校验前后2个ip的大小关系
    const ip1Number = ip2number(ips[0]);
    const ip2Number = ip2number(ips[1]);

    if (ip1Number >= ip2Number) {
      callback('网段截止IP必须大于网段起始IP');
      return;
    }
  } else if (value.indexOf('/') > -1) {
    const ips = value.split('/');
    // 校验第一个 ip
    if (!ipV4Regex.test(ips[0])) {
      callback('请输入正确的IP/IP段。例，192.168.1.2/24');
      return;
    }
    // 校验子网掩码
    if (!ips[1] || isNaN(Number(ips[1]))) {
      callback('子网掩码范围是(0,32]。例，192.168.1.2/24');
      return;
    }
    // 这里把 0 排除掉
    if (Number(ips[1]) <= 0 || Number(ips[1]) > 32) {
      callback('子网掩码范围是(0,32]。例，192.168.1.2/24');
      return;
    }
  } else if (!ipV4Regex.test(value)) {
    callback('请输入正确的IP/IP段');
    return;
  }

  callback();
}

/**
 * 校验IP、IP网段
 * @param {*} rule
 * @param {*} value
 * @param {*} callback
 *
 * @example 单IP：192.168.1.1
 * @example IP网段：192.168.1.2/24
 */
export function checkIpAndSegment(rule: any, value: string, callback: (msg?: string) => void) {
  if (!value) {
    callback();
    return;
  }
  // 如果有 - ，分开校验
  if (value.indexOf('-') > -1) {
    callback('请输入正确的IP/IP网段。');
    return;
  }
  if (value.indexOf('/') > -1) {
    const ips = value.split('/');
    // 校验第一个 ip
    if (!ipV4Regex.test(ips[0])) {
      callback('请输入正确的IP/IP网段。例，192.168.1.2/24');
      return;
    }
    // 校验子网掩码
    if (!ips[1] || isNaN(Number(ips[1]))) {
      callback('子网掩码范围是(0,32]。例，192.168.1.2/24');
      return;
    }
    // 这里把 0 排除掉
    if (Number(ips[1]) <= 0 || Number(ips[1]) > 32) {
      callback('子网掩码范围是(0,32]。例，192.168.1.2/24');
      return;
    }
  } else if (!ipV4Regex.test(value)) {
    callback('请输入正确的IP/IP网段');
    return;
  }

  callback();
}

/**
 * 检查是否是CIDR格式的IP地址
 */
export function isCidr(ip: string, type: 'IPv4' | 'IPv6') {
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
  if (type === 'IPv4') {
    // 检查IP和掩码
    if (ipV4Regex.test(ipAddress) && maskNum > 0 && maskNum <= 32) {
      return true;
    }
    return false;
  }
  if (type === 'IPv6') {
    // 检查IP和掩码
    if (ipV6Regex.test(ipAddress) && maskNum > 0 && maskNum <= 128) {
      return true;
    }
    return false;
  }

  return false;
}

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
 * 格式化时间
 * 秒数取整30秒
 * 秒数介于 0 - 30 之间， 取 00
 * 秒数介于 30 - 59 之间， 取 30
 * @param {String} time
 */
export function processingSeconds(time: string) {
  const timeSeconds = new Date(time).getSeconds();
  const timeText = moment(time).format('YYYY-MM-DD HH:mm');

  let corrected: any = Math.floor(timeSeconds / 30) * 30;
  if (corrected === 0) {
    corrected = `0${corrected}`;
  }

  return moment(`${timeText}:${corrected}`).format();
}

/**
 * 格式化分钟时间
 * 分钟数取整5分钟
 * 分钟数：00, 05, 10, 15 ....
 * @param {String} time
 * @param {number} interval
 */
export function processingMinutes(time: string | number, interval = 5) {
  const timeMinutes = new Date(time).getMinutes();
  const timeText = moment(time).format('YYYY-MM-DD HH:00:00');

  const corrected = Math.floor(timeMinutes / interval) * interval;
  return moment(moment(timeText).add(corrected, 'minutes')).format();
}

/**
 * 格式化时间
 * @param {Date} startTime 开始时间
 * @param {Date} endTime 结束时间
 * @param {Number} interval 默认时间间隔（s）
 *
 * @return {object}
 * ```
 * {
 *    startTime: '2021-04-22T15:10:00+0800',
 *    endTime: '2021-04-22T15:12:00+0800',
 *    interval: 60,
 *    totalSeconds: 60
 * }
 * ```
 */
export function timeFormatter(startTime: string | number, endTime: string | number, interval = 60) {
  // 相差的秒
  const diffSeconds = (moment(endTime).valueOf() - moment(startTime).valueOf()) / 1000;

  let nextStartTime = startTime;
  let nextEndTime = endTime;
  let nextInterval = interval;

  // 计算时间间隔
  // 时间间隔小于1小时，时间间隔为1分钟
  if (diffSeconds <= 1 * 60 * 60) {
    if (interval === 60) {
      nextStartTime = processingMinutes(startTime, 1);
      nextEndTime = processingMinutes(endTime, 1);
    } else if (interval === 5 * 60) {
      nextStartTime = processingMinutes(startTime, 5);
      nextEndTime = processingMinutes(endTime, 5);
    }
  }

  // 时间间隔小于1天，时间间隔为5分钟
  else if (diffSeconds <= 24 * 60 * 60) {
    nextInterval = 5 * 60;
    nextStartTime = processingMinutes(startTime, 5);
    nextEndTime = processingMinutes(endTime, 5);
  }

  // 时间间隔小于10天，时间间隔为1小时
  else {
    nextInterval = 60 * 60;
    nextStartTime = moment(startTime).format('YYYY-MM-DD HH:00:00');
    nextEndTime = moment(endTime).add(1, 'hour').format('YYYY-MM-DD HH:00:00');
  }

  // 其他时间，时间间隔为1天
  // else {
  //   nextInterval = 24 * 60 * 60;
  //   nextStartTime = moment(startTime).format('YYYY-MM-DD 00:00:00');
  //   nextEndTime = moment(endTime).format('YYYY-MM-DD 00:00:00');
  // }

  // 最后格式化成 UTC 时间
  nextStartTime = moment(nextStartTime).format();
  nextEndTime = moment(nextEndTime).format();

  return {
    startTime: nextStartTime,
    endTime: nextEndTime,
    interval: nextInterval,
    totalSeconds: diffSeconds,
  };
}

/**
 * 计算时间间隔
 */
export function getInterval({ startTime, endTime }: { startTime: string; endTime: string }) {
  const MAX_DISPALY_POINTS = 200;
  const DEFAULT_INTERVAL = 30;
  // 计算时间间隔
  let interval = DEFAULT_INTERVAL; // 时间间隔必须是30s的整数倍
  const diffSeconds = moment(endTime).diff(moment(startTime), 's');

  // 点的总数量
  const totalPoints = diffSeconds / interval;

  if (totalPoints > MAX_DISPALY_POINTS) {
    // 间隔必须是 30s 的整数倍
    const quotient = diffSeconds / MAX_DISPALY_POINTS;
    interval = Math.floor(quotient / DEFAULT_INTERVAL) * DEFAULT_INTERVAL;
  }

  return interval;
}

/**
 * 新增或编辑，弹出确认框
 */
export function createConfirmModal({
  dispatchType,
  values,
  onOk,
  onCancel,
  dispatch,
}: {
  dispatchType: string;
  values: any;
  onOk: () => void;
  onCancel: () => void;
  dispatch: (args: { type: string; payload: any }) => any;
}) {
  const dispatchFunc = dispatch || this.props.dispatch;
  Modal.confirm({
    title: '确定保存吗?',
    cancelText: '取消',
    okText: '确定',
    onOk: () => {
      dispatchFunc({
        type: dispatchType,
        payload: values,
      }).then((result: any) => {
        if (result) {
          Modal.confirm({
            keyboard: false,
            title: '保存成功',
            icon: <CheckCircleTwoTone size={24} twoToneColor="#52c41a" />,
            cancelText: '继续添加',
            okText: '返回列表页',
            onOk: () => {
              onOk();
            },
            onCancel: () => {
              onCancel();
            },
          });
        }
      });
    },
  });
}

/**
 * 编辑客户端、虚拟域，弹出确认框
 */
export function updateConfirmModal({
  dispatchType,
  values,
  onOk,
  onCancel,
  dispatch,
}: {
  dispatchType: string;
  values: any;
  onOk: () => void;
  onCancel: () => void;
  dispatch: (args: { type: string; payload: any }) => any;
}) {
  const dispatchFunc = dispatch || this.props.dispatch;
  Modal.confirm({
    title: '确定修改吗?',
    cancelText: '取消',
    okText: '确定',
    onOk: () => {
      dispatchFunc({
        type: dispatchType,
        payload: values,
      }).then((result: any) => {
        if (result) {
          Modal.success({
            keyboard: false,
            title: '修改成功',
            okText: '返回列表页',
            onOk: () => {
              onOk();
            },
            onCancel: () => {
              if (onCancel) onCancel();
            },
          });
        }
      });
    },
  });
}

/**
 * 随机生成密钥
 * @param {*} length 密钥长度
 */
export function randomSecret(length = 32) {
  let randomString = '';
  // 默认去掉了容易混淆的字符oOLl,9gq,Vv,Uu,I1
  const chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678-_@';
  const charsLength = chars.length;
  for (let i = 0; i < length; i += 1) {
    randomString += chars.charAt(Math.floor(Math.random() * charsLength));
  }
  return randomString;
}

/**
 * Sleect 选择框的公共 Props
 */
export const SelectProps = {
  showSearch: true,
  optionFilterProp: 'children',
  filterOption: (input: string, option: { props: { children: any } }) => {
    let { children } = option.props;
    if (Array.isArray(children)) {
      children = children.join(',');
    }
    return children.toLowerCase().indexOf(input.toLowerCase()) >= 0;
  },
};

/**
 * 获取几天前的日期
 *
 * 默认是7天前
 * @param {Number} someDaysAgo 几天前
 */
export function getSomeDaysAgo(someDaysAgo = 7) {
  return moment().subtract(someDaysAgo, 'd').format();
}

/**
 * 日期范围选择器公共属性
 */
export const RangePickerCommonProps = {
  disabledDate: (current: moment.Moment) => current && current > moment().endOf('day'),
  ranges: {
    最近30分钟: () => [moment().subtract(30, 'minutes'), moment()],
    最近1小时: () => [moment().subtract(1, 'hours'), moment()],
    最近1天: () => [moment().subtract(1, 'days'), moment()],
    最近7天: () => [moment().subtract(1, 'weeks'), moment()],
    最近1个月: () => [moment().subtract(1, 'months'), moment()],
    // 最近3个月: [moment().subtract(3, 'months'), moment()],
  },
};

/**
 * 获取数据
 * @param {Object} newQuery 新的参数
 * @param {String} api dispatch的 type 地址
 */
export function handleQueryData(newQuery: any, api: string) {
  const {
    dispatch,
    location: { query },
  } = this.props;
  dispatch({
    type: api,
    payload: {
      ...query,
      ...newQuery,
    },
  });
}

/**
 * 刷新页面
 * @param {*} newQuery
 * @param {*} routerType push || replace
 */
export function handleRefreshPage(newQuery: any, routerType = 'push') {
  const {
    location: { query, pathname },
  } = this.props;
  history[routerType]({
    pathname,
    search: stringify({
      ...query,
      ...newQuery,
      t: +new Date(),
    }),
  });
}

/**
 * 表格切换分页
 * @param {*} pageNumber
 * @param {*} pageSize
 */
// eslint-disable-next-line no-unused-vars
export function handleTableChange(pageNumber: number, pageSize: number) {
  this.handleRefreshPage({
    page: pageNumber,
    pageSize,
  });
}

/**
 * 比较uri
 * @param {Object} nextProps 新的 porps
 * @param {Function} callback 回调
 */
export function compareUri(nextProps: any, callback: (arg?: any) => void) {
  const { location } = this.props;
  const nextLocation = nextProps.location;
  const urlParams = `${stringify(location.query)}${location.hash}`;
  const nextUrlParams = `${stringify(nextLocation.query)}${nextLocation.hash}`;
  if (urlParams !== nextUrlParams) {
    if (callback) {
      callback(nextLocation.query);
    }
  }
}

function userBrowser() {
  const userAgent = navigator.userAgent.toLowerCase();
  if (/msie/i.test(userAgent) && !/opera/.test(userAgent)) {
    return 'IE';
  }
  if (/firefox/i.test(userAgent)) {
    return 'Firefox';
  }
  if (/chrome/i.test(userAgent) && /webkit/i.test(userAgent) && /mozilla/i.test(userAgent)) {
    return 'Chrome';
  }
  if (/opera/i.test(userAgent)) {
    return 'Opera';
  }
  if (
    /webkit/i.test(userAgent) &&
    !(/chrome/i.test(userAgent) && /webkit/i.test(userAgent) && /mozilla/i.test(userAgent))
  ) {
    return 'Safari';
  }
  return '';
}

/**
 * 检查用户的浏览器环境
 * @param {String} browserName
 * @example Firefox
 * @example Chrome
 * @example Safari
 */
export function checkBrowser(browserName: string) {
  return userBrowser() === browserName;
}

/**
 * 驼峰转下划线
 */
export const snakeCase = (value: string) => value.replace(/([A-Z])/g, '_$1').toLowerCase();

/**
 * 下划线转驼峰
 */
export const camelCase = (value: string) => {
  const t = value.replace(/\_(\w)/g, (_value, letter) => {
    return letter.toUpperCase();
  });
  return t;
};

type IEnumObj = Record<string, string>;
interface List {
  label: string;
  value: string;
}
/**
 * 枚举值转描述
 * @param enumObj 枚举内容
 * @param enumValue 枚举值
 */
export const enumValue2Label: (enumObj: IEnumObj, enumValue: string) => string = (
  enumObj,
  enumValue,
) => {
  if (Object.prototype.toString.call(enumObj) !== '[object Object]') {
    return '[--]';
  }
  if (!enumObj[enumValue]) {
    return '[--]';
  }

  return enumObj[enumValue];
};

/**
 * 枚举对象转数组
 * @param enumObj
 */
export const enumObj2List: (enumObj: IEnumObj) => List[] = (enumObj) => {
  if (Object.prototype.toString.call(enumObj) !== '[object Object]') {
    return [];
  }
  const result: List[] = [];
  Object.keys(enumObj).forEach((key) => {
    result.push({
      value: key,
      label: enumObj[key],
    });
  });
  return result;
};

/**
 * 获取当前时区
 */
export const getCurrentTimeZone = () => {
  const timezoneOffsetHours = new Date().getTimezoneOffset() / 60;
  return timezoneOffsetHours > 0 ? `-${timezoneOffsetHours}` : `+${Math.abs(timezoneOffsetHours)}`;
};

/**
 * 是否是对象类型
 * @param value
 */
export const isObject = (value: any) => {
  return Object.prototype.toString.call(value) === '[object Object]';
};

/** 根据 URL 判断页面是否处理内嵌状态 */
export const pageIsEmbed = () => {
  const pagePath = getPagePath();
  return pagePath.indexOf('/embed/') > -1;
};

/**
 * 判断是否是内嵌页面，进行拼接URL
 * @param path
 */
export const getLinkUrl = (path: string) => {
  const isEmbed = pageIsEmbed();
  if (!isEmbed) {
    return path;
  }
  // 这种情况应该不会出现
  if (path.indexOf('/embed/') > -1) {
    return path;
  }
  return `/embed${path}`;
};

/**
 * 判断是否是内嵌页面
 * 是： 不开标签页跳转
 * 否： 开新标签页跳转
 * @param path
 */
export const jumpNewPage = (path: string) => {
  if (pageIsEmbed()) {
    history.push(getLinkUrl(path));
  } else {
    window.open(`${window.location.origin}${window.location.pathname}#${path}`, '_blank');
  }
};

/**
 * 同页跳转
 * 判断是否内嵌
 * @param path
 */
 export const jumpSamePage = (path: string) => {
  if (pageIsEmbed()) {
    let url = path;
    if (!url.includes('/embed/')) {
      url = `/embed${url}`;
    }
    history.push(url);
  } else {
    history.push(path);
  }
};

/**
 * 解析对象json
 * @param str 对象字符串
 */
export function parseObjJson(str: string) {
  let result = {};
  if (str) {
    try {
      result = JSON.parse(str);
    } catch (err) {
      result = {};
    }
  }

  return result;
}

/**
 * 解析数组json
 * @param str 数组字符串
 */
export function parseArrayJson(str: string) {
  let result = [];
  if (str) {
    try {
      result = JSON.parse(str);
    } catch (err) {
      result = [];
    }
  }

  return result;
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

/**
 * @see https://api.highcharts.com.cn/highcharts#lang.numericSymbols
 * @see https://zh.wikipedia.org/wiki/%E5%9B%BD%E9%99%85%E5%8D%95%E4%BD%8D%E5%88%B6%E8%AF%8D%E5%A4%B4
 * @param value 格式化的数组
 * @returns number 返回格式化后的字符串
 */
export const formatNumber: (value: number) => string = (value: number) => {
  if (value === 0) return '0';
  const prefixs = ['', 'k', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y'];
  const i = Math.floor(Math.log(value) / Math.log(1000));
  // console.log(bytes / Math.pow(unit, i));
  return `${numeral((value / Math.pow(1000, i)).toFixed(2)).value()}${
    i >= 0 ? prefixs[i] : prefixs[0]
  }`;
};

/** 值为空 */
export const isEmpty = (val: any) => val === '';

/** 值存在 */
export const isExisty = (val: any) => {
  return val !== undefined && val !== null;
};

/**
 * 标记未存储报文时间
 * @param startTime
 * @param endTime
 * @returns
 */
export const markoldestPacketArea = (startTime: number, endTime: number) => {
  return {
    itemStyle: {
      color: 'rgba(128, 128, 128, 0.3)',
    },
    data: [
      [
        {
          xAxis: startTime,
        },
        {
          xAxis: endTime,
        },
      ],
    ],
  };
};

/**
 * 停止 ajax
 * @param apis 需要停止的
 */
export const abortAjax = (apis: string[]) => {
  if (apis.length === 0) {
    return;
  }
  const { cancelRequest = new Map() } = window;
  cancelRequest.forEach((value: any, key: string) => {
    for (let i = 0; i < apis.length; i += 1) {
      const startIndex = value.apiUri.indexOf(apis[i]);
      if (startIndex !== -1 && startIndex + apis[i].length === value.apiUri.length) {
        // 取消ajax请求
        value.ajax.abort();
        // 删除
        cancelRequest.delete(key);
      }
    }
  });
};

/**
 * 停止所有请求，适用于统计详单类查询
 */
export const abortAllQuery = () => {
  const { cancelRequest = new Map() } = window;
  cancelRequest.forEach((value: any, key: string) => {
    // 取消ajax请求
    value.ajax.abort();
    // 删除
    cancelRequest.delete(key);
  });
};
