import type { MenuDataItem } from '@ant-design/pro-layout';
import type { PaginationProps } from 'antd/lib/pagination';
import type { DefaultSettings } from 'config/defaultSettings';
import type * as H from 'history';

import type {
  LoginModelState,
  SystemAlertModelState,
  SystemSyslogModelState,
  AppModelState,
  NetworkModelState,
  IIpAddressGroupModelState,
  LogicalSubnetModelState,
  SAKnowledgeModelState,
  GeolocationModelState,
  ApplicationPolicyModelState,
  MetadataModelState,
  CustomSAModelState,
  StandardProtocolModelState,
  IScenarioTaskModelState,
  AlertModelState,
  IngestPolicyModelState,
  TransmitModelState,
  INetflowModelState,
  MoitorModelState,
  HomeModelState,
  ISuricataModelState
} from 'umi';
import { GlobalModelState } from 'umi';

export { GlobalModelState };

export interface Loading {
  globalModel: boolean;
  effects: Record<string, boolean | undefined>;
  models: {
    globalModel?: boolean;
    setting?: boolean;
    externalStorageModel?: boolean;
    alertModel?: boolean;
  };
}

export interface ConnectState {
  loading: Loading;
  settings: DefaultSettings;
  appModel: AppModelState;
  loginModel: LoginModelState;
  flowRecordModel: FlowRecordModelState;
  globalModel: GlobalModelState;
  systemAlertModel: SystemAlertModelState;
  systemSyslogModel: SystemSyslogModelState;
  networkModel: NetworkModelState;
  ipAddressGroupModel: IIpAddressGroupModelState;
  logicSubnetModel: LogicalSubnetModelState;
  SAKnowledgeModel: SAKnowledgeModelState;
  geolocationModel: GeolocationModelState;
  applicationPolicyModel: ApplicationPolicyModelState;
  metadataModel: MetadataModelState & IPageModelState;
  customSAModel: CustomSAModelState;
  standardProtocolModel: StandardProtocolModelState;
  serviceModel: ServiceModelState;
  npmdModel: INpmdModelState;
  scenarioTaskModel: IScenarioTaskModelState & IPageModelState;
  alertModel: AlertModelState;
  ingestPolicyModel: IngestPolicyModelState;
  transmitModel: TransmitModelState;
  netflowModel: INetflowModelState;
  moitorModel: MoitorModelState;
  pktAnalysisModel: IPktAnalysisModelState;
  homeModel: HomeModelState;
  suricataModel: ISuricataModelState;
}

export interface Route extends MenuDataItem {
  routes?: Route[];
}

export interface ILocation extends H.Location {
  pathname: string;
  query: ILocationQuery;
  search: string;
  hash: string;
}

export interface IPagination {
  pagination: {
    current: number;
    page: number;
    total: number;
    totalPages: number;
    pageElements: number;
  } & PaginationProps;
}
