/**
 * changeURLStatic 修改地址栏 URL参数 不跳转
 * @param name
 * @param value
 */
 export function changeURLStatic(name, value) {
  let url = changeURLParam(location.href, name, value); // 修改 URL 参数
  history.replaceState(null, null, url);  // 替换地址栏
}


/**
* changeURLParam 修改 URL 参数
* @param url
* @param name
* @param value
* @returns {string}
*/
function changeURLParam(url, name, value) {
  if (typeof value === 'string') {
      // eslint-disable-next-line no-param-reassign
      value = value.toString().replace(/(^\s*)|(\s*$)/, ""); // 移除首尾空格
  }
  let url2;
  if (!value) { // remove
      let reg = eval('/(([\?|&])' + name + '=[^&]*)(&)?/i');
      let res = url.match(reg);
      if (res) {
          if (res[2] && res[2] === '?') { // before has ?
              if (res[3]) { // after has &
                  url2 = url.replace(reg, '?');
              } else {
                  url2 = url.replace(reg, '');
              }
          } else {
              url2 = url.replace(reg, '$3');
          }
      }
  } else {
      let reg = eval('/([\?|&]' + name + '=)[^&]*/i');
      if (url.match(reg)) { // edit
          url2 = url.replace(reg, '$1' + value);
      } else { // add
          let reg = /([?](\w+=?)?)[^&]*/i;
          let res = url.match(reg);
          url2 = url;
          if (res) {
              if (res[0] !== '?') {
                  url2 += '&';
              }
          } else {
              url2 += '?';
          }
          url2 += name + '=' + value;
      }
  }
  return url2;
}