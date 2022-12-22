export interface IForwardRule {
  id: string;
  name: string;
  defaultAction: EForwardRuleAction;
  exceptBpf?: string;
  exceptTuple?: string;
  description?: string;
}

export enum EForwardRuleAction {
  转发 = '0',
  不转发 = '1',
}

export enum EForwardPolicyState {
  启用 = '1',
  停用 = '0',
}

export interface IForwardPolicy {
  id: string;
  name: string;
  networkId: string;
  ruleId: string;
  netifName: string;
  ipTunnel: string;
  loadBalance: ELoadBalanceType | null;
  state: EForwardPolicyState;
  totalBandWidth?: number;
  description?: string;
}

export enum ELoadBalanceType {
  '源IP' = 'srcIp',
  '源IP + 目的IP' = 'srcIp_destIp',
  '源IP + 目的IP + 源端口 + 目的端口' = 'srcIp_destIp_srcPort_destPort',
}

export enum EForwardPolicyIPTunnelMode {
  不封装 = '',
  GRE封装 = 'gre',
  VXLAN封装 = 'vxlan',
}

export interface IIPTunnelGRETuple {
  sourceIp: string;
  destIp: string;
  sourceMac: string;
  destMac: string;
  key: string;
  checksum: EIPTunnelChecksum;
}

export interface IIPTunnelVxlanTuple {
  sourceMac: string;
  destMac: string;
  sourceIp: string;
  destIp: string;
  sourcePort: string;
  destPort: string;
  vnid: string;
}

export enum EIPTunnelChecksum {
  计算 = '1',
  不计算 = '0',
}

export interface IIPTunnel {
  mode: EForwardPolicyIPTunnelMode;
  params: IIPTunnelGRETuple[] | IIPTunnelVxlanTuple[] | [];
}

export interface IForwardPolicyStatData {
  timestamp: string;
  networkId: string;
  policyId: string;
  netifName: string;
  forwardTotalBytes: number;
  forwardSuccessBytes: number;
  forwardFailBytes: number;
  forwardTotalPackets: number;
  forwardSuccessPackets: number;
  forwardFailPackets: number;
}
