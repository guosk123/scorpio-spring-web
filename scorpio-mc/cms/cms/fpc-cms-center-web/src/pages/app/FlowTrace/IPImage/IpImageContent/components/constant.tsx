import { IShowCategory } from '../../typings';
import VisitingIp from '../components/VisitingIp';
import Alarm from './Alarm';
import ApplicationTrend from './ApplicationTrend';
// import ApplicationTrend from './ApplicationTrend';
import ConnectSource from './ConnectSourceMap';
import VisitingDomainName from './VisitingDomainName';
import VisitingPort from './VisitingPort';

export const IpImageWindows = {
  [IShowCategory.VISITINGIP]: <VisitingIp title="来访的IP" category={IShowCategory.VISITINGIP} />,
  [IShowCategory.VISITEDIP]: <VisitingIp title="访问的IP" category={IShowCategory.VISITEDIP} />,
  [IShowCategory.SHARINGPORT]: (
    <VisitingPort title="开放的端口" category={IShowCategory.SHARINGPORT} />
  ),
  [IShowCategory.VISITINGPORT]: (
    <VisitingPort title="访问的端口" category={IShowCategory.VISITINGPORT} />
  ),
  [IShowCategory.SHARINGDOMAINNAME]: (
    <VisitingDomainName title="开放的域名" category={IShowCategory.SHARINGDOMAINNAME} />
  ),
  [IShowCategory.VISITINGDOMAINNAME]: (
    <VisitingDomainName title="访问的域名" category={IShowCategory.VISITINGDOMAINNAME} />
  ),
  [IShowCategory.SECURITYALERTS]: <Alarm title="安全告警" category={IShowCategory.SECURITYALERTS} />,
  [IShowCategory.CONNECTIONSOURCE]: (
    <ConnectSource title="连接源" IpCategory={IShowCategory.CONNECTIONSOURCE} />
  ),
  [IShowCategory.CONNECTIONTARGET]: (
    <ConnectSource title="连接目标" IpCategory={IShowCategory.CONNECTIONTARGET} />
  ),
  [IShowCategory.APPLICATIONTREND]: <ApplicationTrend category={IShowCategory.APPLICATIONTREND} />,
  [IShowCategory.LOCATIONTREND]: <ApplicationTrend category={IShowCategory.LOCATIONTREND} />,
};
