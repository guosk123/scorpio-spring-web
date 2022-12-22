import ajax from '@/utils/frame/ajax';
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';

export async function bpfRuleValid({ bpf }) {
  // base64 加密
  // @see: https://caniuse.com/#search=btoa
  // @see: https://developer.mozilla.org/zh-CN/docs/Web/API/WindowBase64/btoa
  // 不能直接用于加密 unicode 字符
  let valueToBase64 = window.btoa(unescape(encodeURIComponent(bpf)));
  // fix: 解决base64通过http传输后+变空格的问题
  // 1. 前端传送base64前把字串中的+先替换为编码后的
  // 2. 也可以后台接收的时候把空格全都替换为加号
  valueToBase64 = valueToBase64.replace(/\+/g, '%2B');

  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/bpf-rule-verifications?bpf=${valueToBase64}`);
}
