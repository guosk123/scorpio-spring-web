import { API_VERSION_V1 } from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';
import type { IMenuAccessItem } from './typing';

//4Lq20oIBvRadtbKQGyf4
export async function queryAuthenticationOfMenu(params: { userId: string }) {
  return ajax(`${API_VERSION_V1}/system/users/${params.userId || 'null'}/menus`);
}

export async function updataAuthenticationOfMenu(params: {
  menus: IMenuAccessItem[];
  userId: string | number;
}) {
  return ajax(`${API_VERSION_V1}/system/users/${params.userId}/menus`, {
    data: JSON.stringify(params.menus),
    type: 'POST',
    processData: false,
    contentType: 'application/json',
  });
}
