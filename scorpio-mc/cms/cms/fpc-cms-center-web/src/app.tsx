import { queryCurrentUser } from '@/services/frame/global';
import type { Settings as ProSettings } from '@ant-design/pro-layout';
import type { ICurrentUser } from 'umi';
import { history } from 'umi';
import defaultSettings from '../config/defaultSettings';
import { queryBISettings } from './pages/app/Dashboard/Custom/service';

export async function getInitialState(): Promise<{
  currentUser?: ICurrentUser;
  settings?: ProSettings;
  fetchUserInfo?: () => Promise<ICurrentUser | undefined>;
}> {
  const fetchUserInfo = async () => {
    try {
      const { success, result } = await queryCurrentUser();
      if (success) {
        return result;
      }
    } catch (error) {}
    return undefined;
  };

  const currentUser = await fetchUserInfo();
  // 如果是登录页面，不执行
  if (history.location.pathname !== '/login') {
    try {
      const { authorities = [] } = currentUser || {};
      if (
        Array.isArray(authorities) &&
        authorities.find(
          (el) => el.authority === 'PERM_USER' || el.authority === 'PERM_SERVICE_USER',
        )
      ) {
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
      window.sessionStorage.setItem('bi-token', '');
      history.push('/login');
    }
  }

  return {
    settings: defaultSettings,
    fetchUserInfo,
    currentUser: currentUser,
  };
}
