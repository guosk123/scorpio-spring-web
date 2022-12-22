import $ from 'jquery';
import { notification, message } from 'antd';
import config from '@/common/applicationConfig';
import { getCookie } from '@/utils/frame/cookie';
import { getDvaApp } from 'umi';
import { getPagePath } from '@/utils/utils';
import type { IAjaxResponseFactory } from '@/common/typings';

export interface IAjaxMap {
  pagePath: string;
  apiUri: string;
  ajax: JQueryXHR;
}

window.cancelRequest = new Map<symbol, IAjaxMap>();

const isDev = process.env.NODE_ENV === 'development';
/**
 * Requests a URL, returning a promise.
 *
 * @param  {string} url       The URL we want to request
 * @param  {object} [options] The options we want to pass to "fetch"
 * @return {object}           An object containing either "data" or "err"
 */
export default function ajax(url: string, options?: JQueryAjaxSettings) {
  let newUrl = url;
  const requestKey = Symbol(Date.now());
  // 登录登出时，接口前面不添加前缀，单点登陆除外
  if (
    ((url.indexOf('/login') > -1 && url.indexOf('loginUrl') < 0) || url.indexOf('/logout') > -1) &&
    url.indexOf('/sso/') === -1
  ) {
    newUrl = `${isDev ? '/api' : ''}${config.CONTEXT_PATH}${url}`;
  } else {
    newUrl = `${config.API_BASE_URL}${url}`;
  }

  const defaultOptions = {
    cache: false,
    credentials: 'include',
  };
  const newOptions = { url: newUrl, ...defaultOptions, ...options } as JQueryAjaxSettings;
  // 添加csrf
  newOptions.headers = {
    'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
    ...newOptions.headers,
  };

  const promise: Promise<IAjaxResponseFactory<any>> = new Promise((resolve, reject) => {
    const ajaxRequest = $.ajax({
      ...newOptions,
    })
      .done((result) => {
        // 兼容用户被踢出的情况
        if (typeof result === 'string' && result.indexOf('This session has been expired') > -1) {
          message.warning('登录超时，请重新登录');
          // 清空用户信息
          // eslint-disable-next-line no-underscore-dangle
          getDvaApp()?._store?.dispatch({ type: 'globalModel/clearCurrentUser' });
          resolve({
            success: false,
            result: undefined,
          });
          return;
        }

        resolve({
          status: 200,
          success: true,
          result,
        });
      })
      // eslint-disable-next-line no-unused-vars
      .fail((xhr) => {
        const { responseJSON } = xhr;
        const errortext = responseJSON ? responseJSON.message : xhr.statusText;

        const errorMsg = {
          code: responseJSON && responseJSON.code,
          success: false,
          status: xhr.status,
          msg: errortext,
          result: xhr.responseText,
        };
        reject(errorMsg);
      });

    // 登录、登出、产品信息、当前登录用户信息等排除掉
    if (newUrl.indexOf('/boot/') === -1) {
      const pagePath = getPagePath();
      window.cancelRequest.set(requestKey, {
        pagePath,
        apiUri: newUrl,
        ajax: ajaxRequest,
      });
    }
  });

  return promise
    .then((data) => data)
    .catch((e) => {
      const { status, msg } = e;

      // sso登录页面排除掉
      if (
        window.location.href.indexOf('/sso/login') > -1 ||
        window.location.href.indexOf('/sso/oauth') > -1
      ) {
        return e;
      }

      if (status === 401 || status === 403) {
        if (msg && msg.indexOf('Full authentication') === -1) {
          // message.warning(msg);
        }
        // 清空用户信息
        // eslint-disable-next-line no-underscore-dangle
        getDvaApp()?._store?.dispatch({ type: 'globalModel/clearCurrentUser' });
      } else if ((status <= 504 && status >= 500) || (status >= 404 && status < 422)) {
        notification.error({
          message: '出现了一个问题',
          description: msg,
        });
      }

      return e;
    })
    .finally(() => {
      window.cancelRequest.forEach(() => {
        window.cancelRequest.delete(requestKey);
      });
    });
}
