// import './wydr';
import { queryCurrentUser } from '@/services/frame/global';
import type { Settings as ProSettings } from '@ant-design/pro-layout';
import type { ICurrentUser } from 'umi';
import { history } from 'umi';
import defaultSettings from '../config/defaultSettings';
import type { IAjaxResponseFactory } from './common/typings';
import { queryBISettings } from './pages/app/Report/service';

export async function getInitialState(): Promise<{
  currentUser?: ICurrentUser;
  settings?: ProSettings;
  fetchUserInfo?: () => Promise<ICurrentUser | undefined>;
}> {
  const fetchUserInfo = async () => {
    try {
      const { success, result }: IAjaxResponseFactory<ICurrentUser> = await queryCurrentUser();
      if (success) {
        return result;
      }
    } catch (error) {}
    return undefined;
  };

  // 如果是登录页面，不执行
  if (history.location.pathname !== '/login') {
    try {
      const currentUser = await fetchUserInfo();
      const { authorities = [] } = currentUser || {};
      if (Array.isArray(authorities) && authorities.find((el) => el.authority === 'PERM_USER')) {
        // 判断如果是存在用户权限，就查询 BI 相关的 Token
        // 查询 BI 配置信息
        const {
          success,
          result: { token },
        } = await queryBISettings();

        if (success) {
          window.sessionStorage.setItem('bi-token', token);
        }
      }
      return {
        currentUser,
        fetchUserInfo,
        settings: defaultSettings,
      };
    } catch (error) {
      history.push('/login');
    }
  }
  return {
    settings: defaultSettings,
    fetchUserInfo,
  };
}

let AccessMenuMap = { map: {}, userTypes: [''] };

const disablePath = (routes: any) => {
  const tmpArr = [...routes];
  for (let index = 0; index < tmpArr.length; index++) {
    const element: any = tmpArr[index];
    if (element?.routes?.length) {
      tmpArr.push(...(element.routes as any));
    }
    if (((window as any)?.disablePath || []).includes(element.path)) {
      element.hideInMenu = true;
      element.access = 'disablePath';
    }
    // const res = getAuthenticationFn(
    //   editIdentificationCode(element.path || '/welcome'),
    //   AccessMenuMap.map,
    // );
    // console.log('first res', res)
    // if (!res.flag && AccessMenuMap.userTypes.includes('PERM_USER')) {
    //   element.access = 'disablePath';
    //   element.hideInMenu = true;
    // }
  }
};

export async function patchRoutes({ routes }: any) {
  disablePath(routes);
  // mergeRoutes(routes, extraRoutes);
}
export async function render(oldRender: any) {
  const { success, result }: IAjaxResponseFactory<ICurrentUser> = await queryCurrentUser();
  if (success) {
    const tmpAuths: string[] = [];
    result?.authorities?.forEach((authObj) => {
      tmpAuths.push(...Object.values(authObj));
    });
    AccessMenuMap = { map: result.menuPerms, userTypes: tmpAuths };
  }
  oldRender();
}
