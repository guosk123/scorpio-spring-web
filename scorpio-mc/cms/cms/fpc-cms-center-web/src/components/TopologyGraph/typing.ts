import type { Edge, Node } from '@antv/x6';

// 根据该参数，可以生成一条边
export interface IEdgeParams {
  sourceNodeId: string;
  targetNodeId: string;
  sourcePort: 'r1' | 'l1' | 't1' | 'b1';
  targetPort: 'r1' | 'l1' | 't1' | 'b1';
  edgeMsg: string;
}

// 项目图谱图中使用的连接桩的名称
export interface IEdgePort {
  source: 'r1' | 'l1' | 't1' | 'b1';
  target: 'r1' | 'l1' | 't1' | 'b1';
}

// 节点的位置类型
export interface IPosition {
  x: number;
  y: number;
}

// 生成节点需要的参数
export interface INodeParams {
  id: string;
  nodeName: string;
  imagePath: string;
  position?: IPosition;
}

// 拓扑图接收的事件类型
export interface EventCallbacks {
  nodeClick?: (e: JQuery.ClickEvent, node: Node) => void;
  nodeMouseEnter?: (e: JQuery.MouseEnterEvent, node: Node) => void;
  nodeMouseLeave?: (e: JQuery.MouseLeaveEvent, node: Node) => void;
  nodeDbClick?: (e: JQuery.DoubleClickEvent, node: Node) => void;
  edgeClick?: (e: JQuery.ClickEvent, edge: Edge) => void;
  edgeMouseEnter?: (e: JQuery.MouseEnterEvent, edge: Edge) => void;
  edgeMouseLeave?: (e: JQuery.MouseLeaveEvent, edge: Edge) => void;
  blankClick?: () => void;
  graphResize?: () => void;
}

// 生成拖拽节点需要的属性
export interface NodeProperties {
  text: string;
  iconName: string;
  icon?: any;
}

export interface CellTitleOption {
  inputType: 'input' | 'select';
  selectItem?: { label: string; value: string }[];
}
export interface TopologyGraphOptions {
  node?: CellTitleOption;
  edge?: CellTitleOption;
}
