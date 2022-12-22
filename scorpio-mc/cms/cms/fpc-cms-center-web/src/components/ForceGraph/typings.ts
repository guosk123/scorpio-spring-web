import type { Simulation, SimulationLinkDatum, SimulationNodeDatum } from 'd3';
import type { ReactNode } from 'react';

interface IAction {
  key: string;
  label: string | ReactNode;
}
export enum EForceGraphIndex {
  TOTAL_BYTES = 'totalBytes',
  ESTABLISHEDSESSIONS = 'establishedSessions',
  APPLICATIONS = 'applications',
}

export enum EForceGraphIndexLabel {
  'totalBytes' = '总流量',
  'establishedSessions' = '会话数',
  'applications' = '应用层协议',
}

export interface ITheme {
  /** 模式 light | dark */
  mode: 'light' | 'dark';
  /** 画布背景 */
  backgroundColor: string;
  /** 节点颜色 */
  nodeColor: string;
  /** 节点标签颜色 */
  nodeLabelColor: string;
  /** 边的颜色 */
  edgeColor: string;
}

export interface IForceGraphHandler {
  update: (nodes: INode[], edges: IEdge[]) => void;
}

export interface IForceGraphProps extends IGraphData {
  id: string;
  /** svg 的高度 */
  height?: number;
  /** link 所表示的字段含义 */
  weightField: string;
  /** 主题 */
  theme?: ITheme;
  /** 节点操作按钮 */
  nodeActions?: IAction[];
  /** 节点点击事件 */
  onNodeClick?: (action: IAction, node: INode, id?: string) => void;
  /** 节点操作按钮 */
  edgeActions?: IAction[];
  /** 节点点击事件 */
  onEdgeClick?: (action: IAction, edge: IEdge, id?: string) => void;
  /** 框选事件 */
  brushActions?: IAction[];
  /** 刷取回调函数 */
  onBrushEnd?: (action: IAction, edge: any[]) => void;
  /** 删除关系 */
  onDeleteRalation?: (id: string, action: IAction, edge?: any, node?: any) => void;
  /** 历史画布 */
  historyGraph?: boolean;
  /** 自定义边指标 */
  edgeIndex?: EForceGraphIndex[];
}

export interface IGraphData {
  nodes: INode[];
  edges: IEdge[];
}

/** 节点 */
export interface INode extends SimulationNodeDatum {
  id: string;
  [key: string]: any;
}

/** 边 */
export interface IEdge extends SimulationLinkDatum<INode> {
  [key: string]: any;
}

// D3 内的类型
// ===============
/** D3 simulation */
export type D3Simulation = Simulation<INode, undefined>;
/** D3 Node */
export type D3Node = d3.Selection<SVGCircleElement, INode, SVGGElement, unknown> | undefined;
/** D3 link */
export type D3Edge = d3.Selection<SVGLineElement, IEdge, SVGGElement, unknown> | undefined;
