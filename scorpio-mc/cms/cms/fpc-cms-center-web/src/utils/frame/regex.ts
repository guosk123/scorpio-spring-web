/**
 * 常用正则表达式
 *
 * @see: https://any86.github.io/any-rule/
 */

/**
 * appKey和appToken正则表达式
 *
 * - 只允许输入字母、数字、中划线-、下划线_ 和 @ 等非空字符的组合
 * - 不包含非空字符，例如：空格
 */
export const appKeyAndTokenRegex = /^[a-zA-Z0-9-_@]+[^\s]$/;

/**
 * 用户显示名称正则表达式
 *
 * - 只允许输入数字、英文字母、下划线和中文汉字
 * - 不包含非空字符，例如：空格
 */
export const userFullNameRegex = /^[A-Za-z0-9_\-\u4e00-\u9fa5]+[^\s]$/;

/**
 * 登录密码正则表达式
 *
 * - 密码必须同时包含大写字母、小写字母和数字
 * - 不包含非空字符，例如：空格
 */
//密码必须同时包含大写字母、小写字母和数字
export const passwordRegex = /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])+[^\s]+$/;
// 密码必须同时包含大写字母、小写字母和数字,特殊字符
export const passwordRegex_charNumberSpecialChar =
  /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9])+[^\s]+$/;
// 密码必须同时包含大写字母、小写字母和特殊字符
export const passwordRegex_charSpecialChar = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9])+[^\s]+$/;

/**
 * IPv4校验正则
 */
export const ipV4Regex =
  /^(25[0-5]|2[0-4]\d|[0-1]\d{2}|[1-9]?\d)\.(25[0-5]|2[0-4]\d|[0-1]\d{2}|[1-9]?\d)\.(25[0-5]|2[0-4]\d|[0-1]\d{2}|[1-9]?\d)\.(25[0-5]|2[0-4]\d|[0-1]\d{2}|[1-9]?\d)$/;

/**
 * IPv4 掩码正则
 */
export const ipv4MaskRegex =
  /^((128|192)|2(24|4[08]|5[245]))(\.(0|(128|192)|2((24)|(4[08])|(5[245])))){3}$/;

/**
 * 域名验证正则
 * @see: https://blog.csdn.net/u012219045/article/details/98957753
 */
export const hostRegex =
  /^(?=^.{3,255}$)[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+$/;

/**
 * ipV4正则表达
 * @see: https://github.com/richb-intermapper/IPv6-Regex/blob/master/ipv6validator.js#L15
 * @see: https://github.com/sindresorhus/ip-regex/blob/master/index.js
 */
const ipV4 =
  '(25[0-5]|2[0-4]d|[0-1]d{2}|[1-9]?d).(25[0-5]|2[0-4]d|[0-1]d{2}|[1-9]?d).(25[0-5]|2[0-4]d|[0-1]d{2}|[1-9]?d).(25[0-5]|2[0-4]d|[0-1]d{2}|[1-9]?d)';

const ipV6Seg = '[a-fA-F\\d]{1,4}';
const ipV6RegexText = `
(
(?:${ipV6Seg}:){7}(?:${ipV6Seg}|:)|                                // 1:2:3:4:5:6:7::  1:2:3:4:5:6:7:8
(?:${ipV6Seg}:){6}(?:${ipV4}|:${ipV6Seg}|:)|                         // 1:2:3:4:5:6::    1:2:3:4:5:6::8   1:2:3:4:5:6::8  1:2:3:4:5:6::1.2.3.4
(?:${ipV6Seg}:){5}(?::${ipV4}|(:${ipV6Seg}){1,2}|:)|                 // 1:2:3:4:5::      1:2:3:4:5::7:8   1:2:3:4:5::8    1:2:3:4:5::7:1.2.3.4
(?:${ipV6Seg}:){4}(?:(:${ipV6Seg}){0,1}:${ipV4}|(:${ipV6Seg}){1,3}|:)| // 1:2:3:4::        1:2:3:4::6:7:8   1:2:3:4::8      1:2:3:4::6:7:1.2.3.4
(?:${ipV6Seg}:){3}(?:(:${ipV6Seg}){0,2}:${ipV4}|(:${ipV6Seg}){1,4}|:)| // 1:2:3::          1:2:3::5:6:7:8   1:2:3::8        1:2:3::5:6:7:1.2.3.4
(?:${ipV6Seg}:){2}(?:(:${ipV6Seg}){0,3}:${ipV4}|(:${ipV6Seg}){1,5}|:)| // 1:2::            1:2::4:5:6:7:8   1:2::8          1:2::4:5:6:7:1.2.3.4
(?:${ipV6Seg}:){1}(?:(:${ipV6Seg}){0,4}:${ipV4}|(:${ipV6Seg}){1,6}|:)| // 1::              1::3:4:5:6:7:8   1::8            1::3:4:5:6:7:1.2.3.4
(?::((?::${ipV6Seg}){0,5}:${ipV4}|(?::${ipV6Seg}){1,7}|:))           // ::2:3:4:5:6:7:8  ::2:3:4:5:6:7:8  ::8             ::1.2.3.4
)(%[0-9a-zA-Z]{1,})?                                           // %eth0            %1
`
  .replace(/\s*\/\/.*$/gm, '')
  .replace(/\n/g, '')
  .trim();

export const ipV6Regex = new RegExp(`^${ipV6RegexText}$`);

/**
 * MAC地址正则表达
 */
export const macAddressRegex = /^((([a-f0-9]{2}:){5})|(([a-f0-9]{2}-){5}))[a-f0-9]{2}$/i;

export const nameRegex = new RegExp('^[\u4e00-\u9fa5_a-zA-Z0-9]+$');

export const domainReg =
  /^(?=^.{3,255}$)[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+$/;
