import type { ConnectState } from '@/models/connect';
import { useSelector } from 'umi';

export default function useAccessMapForService() {
  const authenticationMap = useSelector<ConnectState>((state) => state.globalModel.currentUser.menuPerms);
  return authenticationMap;
}
