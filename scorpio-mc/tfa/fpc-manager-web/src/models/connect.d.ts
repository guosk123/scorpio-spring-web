import type { IUriTimeQuery } from '@/components/GlobalTimeSelector ';
import type { IPacketModelState } from '@/pages/app/appliance/Packet/model';
import type { MenuDataItem } from '@ant-design/pro-layout';
import type { PaginationProps } from 'antd/lib/pagination';
import type { DefaultSettings } from 'config/defaultSettings';
import type * as H from 'history';
import type {
  AlertModelState,
  HomeModelState,
  ApplicationPolicyModelState,
  AppModelState,
  CustomSAModelState,
  DeviceNetifModelState,
  FlowRecordModelState,
  GeolocationModelState,
  IIpAddressGroupModelState,
  IngestPolicyModelState,
  IStoragePolicyModelState,
  LoginModelState,
  MetadataModelState,
  MoitorModelState,
  NetworkModelState,
  SAKnowledgeModelState,
  ServiceModelState,
  SystemAlertModelState,
  SystemSyslogModelState,
  LogicalSubnetModelState,
  IStorageSpaceModelState,
  INpmdModelState,
  IPktAnalysisModelState,
  ITransmitTaskModelState,
  ISituationModelState,
  AbnormalEventModelState,
  ISuricataModelState,
} from 'umi';
import { GlobalModelState } from 'umi';
import type { IPageModelState } from '@/utils/frame/model';

export { GlobalModelState };

export interface Loading {
  globalModel: boolean;
  effects: Record<string, boolean | undefined>;
  models: {
    globalModel?: boolean;
    setting?: boolean;
    alertModel?: boolean;
  };
}

export interface ConnectState {
  loading: Loading;
  settings: DefaultSettings;
  loginModel: LoginModelState;
  appModel: AppModelState;
  globalModel: GlobalModelState;
  deviceNetifModel: any;
  standardProtocolModel: any;
  metricModel: any;
  ipAddressGroupModel: IIpAddressGroupModelState;
  networkModel: NetworkModelState;
  logicSubnetModel: LogicalSubnetModelState;
  serviceModel: ServiceModelState;
  ingestPolicyModel: IngestPolicyModelState;
  applicationPolicyModel: ApplicationPolicyModelState & IPageModelState;
  SAKnowledgeModel: SAKnowledgeModelState;
  customSAModel: CustomSAModelState;
  alertModel: AlertModelState;
  homeModel: HomeModelState;
  deviceNetifModel: DeviceNetifModelState;
  flowRecordModel: FlowRecordModelState;
  metadataModel: MetadataModelState & IPageModelState;
  geolocationModel: GeolocationModelState;
  systemSyslogModel: SystemSyslogModelState;
  storagePolicyModel: IStoragePolicyModelState;
  moitorModel: MoitorModelState;
  systemAlertModel: SystemAlertModelState;
  storageSpaceModel: IStorageSpaceModelState;
  npmdModel: INpmdModelState;
  pktAnalysisModel: IPktAnalysisModelState;
  transmitTaskModel: ITransmitTaskModelState & IPageModelState;
  packetModel: IPacketModelState;
  situationModel: ISituationModelState;
  abnormalEventModel: AbnormalEventModelState;
  scenarioTaskModel: IScenarioTaskModelState;
  suricataModel: ISuricataModelState;
  transmitModel: TransmitModelState;
}

export interface Route extends MenuDataItem {
  routes?: Route[];
}

export interface ILocationQuery extends IUriTimeQuery {
  networkId?: string;
  [propName: string]: any;
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
