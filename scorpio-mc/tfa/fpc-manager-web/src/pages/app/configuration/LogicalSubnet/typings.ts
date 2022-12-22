/* eslint-disable no-unused-vars */
/**
 * 逻辑子网类型枚举
 */
export enum LogicalSubnetEnum {
  IP = 'ip',
  MAC = 'mac',
  VLAN = 'vlan',
  MPLS = 'mpls',
  GRE = 'gre',
  VXLAN = 'vxlan',
}

export const MAX_CUSTOM_SUBNETWORK_LIMIT = 64;

/**
 * 逻辑子网的类型名称
 */
export const LOGICAL_SUBNET_NAME_OBJ = {
  ip: 'IP子网络',
  mac: 'MAC子网络',
  vlan: 'VLAN子网络',
  mpls: 'MPLS子网络',
  gre: 'GRE子网络',
  vxlan: 'VXLAN子网络',
};

/**
 * GRE 通道配置时的类型
 */
export enum GreSettingCategoryEnum {
  /**
   * 隧道识别关键字
   */
  KEY = 'greKey',
  /**
   * 隧道IP/IP对
   */
  IP = 'greIp',
}

/**
 * 逻辑子网
 */
export interface ILogicalSubnet {
  id: string;
  name: string;
  networkId: string;
  /**
   * 所属网络的名称
   *
   * @description: 原始数据中没有，这是补充拓展的
   */
  networkName?: string;
  type: LogicalSubnetEnum;
  typeText?: string;
  /**
   * 总带宽 Mbps
   */
  bandwidth: number;
  configuration: string;
  description?: string;
}

export type ILogicalSubnetMap = Record<string, ILogicalSubnet>;
