import { DEF_ADM_ACCESS_MAP } from '@/access';
import { getRouteAccessMap } from '@/utils/frame/menuAccess';
import useAccessMapForService from '../useAccessMapForService';
import useAuthentication from '../useAuthentication';
import useUrlToAccessUrl from '../useUrlToTemplate';

export const IGNORE_KEY = 'ignore';

export default function useComponentsAccess(accessKey: string) {
  const accessUrl = useUrlToAccessUrl(accessKey);
  const authenticationMap = useAccessMapForService();
  const access = useAuthentication(
    accessUrl,
    JSON.stringify(authenticationMap) === DEF_ADM_ACCESS_MAP
      ? getRouteAccessMap(true)
      : authenticationMap,
  );
  if (accessKey === IGNORE_KEY) {
    return true;
  }
  return access.flag;
}
