import type { IEnumValue } from '@/components/FieldFilter/typings';
import type { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { IL7Protocol, IL7ProtocolMap } from '@/pages/app/appliance/Metadata/typings';
import type {
  IpAddressGroup,
  IpAddressGroupMap,
} from '@/pages/app/Configuration/IpAddressGroup/typings';
import type { ILogicalSubnetMap } from '@/pages/app/Configuration/LogicalSubnet/typings';
import type {
  INetworkGroupMap,
  INetworkMap,
} from '@/pages/app/Configuration/Network/typings';
import type { ApplicationItem } from '@/pages/app/Configuration/SAKnowledge/typings';
import type { IService, IServiceMap } from '@/pages/app/Configuration/Service/typings';
import type { ColumnProps } from 'antd/lib/table';
import type { ReactNode } from 'react';
import type { Dispatch, GeolocationModelState } from 'umi';
import type { FlowRecordModelState } from '../model';

/**
 * 表格定义
 */
export interface IFlowRecordColumnProps<RecordType> extends ColumnProps<RecordType> {
  /**
   * 搜索时的提示信息
   */
  searchTip?: string;

  /**
   * 是否在表格中显示
   */
  show?: boolean;

  disabled?: boolean;
  /**
   * 是否可以被搜索
   */
  searchable?: boolean;
  enumValue?: IEnumValue[];
  /**
   * 字段的类型
   */
  fieldType?: EFieldType;
  /**
   * 操作数类型
   */
  operandType?: EFieldOperandType;
}

export interface ILocationProps {
  /**
   * 接收从外部接收过滤条件
   */
  filter?: string;
  /**
   * 各种分析任务的ID
   */
  analysisResultId?: string;
  /**
   * 各种分析任务的开始时间
   */
  analysisStartTime?: string;
  /**
   * 各种分析任务的结束时间
   */
  analysisEndTime?: string;

  /**
   * 安全告警规则id
   */
  sid?: string;
}

export interface IFlowRecordEmbedProps {
  /** 表格的 KEY 值 */
  tableKey?: string;
  /** 过滤条件在 localstorage 中的 KEY 值 */
  filterHistoryKey?: string;
  /** 额外添加的 DSL 过滤条件 */
  extraDsl?: string;
  /**
   * 需要展示的表格字段
   * @value [] 数组为空时，展示所有的统计指标
   */
  displayMetrics?: string[];

  /**
   * 过滤条件是否只读的
   * 不可编辑、不可添加删除
   */
  filterIsReadonly?: boolean;

  /** 额外的操作按钮 */
  extraAction?: ReactNode | string;
}

export interface IFlowRecordProps extends IFlowRecordEmbedProps, ILocationProps {
  dispatch: Dispatch;
  globalSelectedTime: Required<IGlobalTime>;
  flowRecordModel: FlowRecordModelState;
  geolocationModel: GeolocationModelState;

  applicationList: ApplicationItem[];

  allIpAddressGroupList: IpAddressGroup[];
  allIpAddressGroupMap: IpAddressGroupMap;
  allServices: IService[];
  allL7ProtocolsList: IL7Protocol[];
  allL7ProtocolMap: IL7ProtocolMap;
  allServiceMap: IServiceMap;
  allNetworkMap: INetworkMap;
  allNetworkGroupMap: INetworkGroupMap;
  allLogicalSubnetMap: ILogicalSubnetMap;
  queryFlowRecordLoading: boolean | undefined;
  queryFlowLogsStatisticsLoading: boolean | undefined;
  cancelQueryTaskLoading: boolean | undefined;
  /** tab名称 */
  tabName?: string;
  filterObj?: any;
}
