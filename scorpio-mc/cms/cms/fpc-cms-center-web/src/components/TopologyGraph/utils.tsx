import type { Graph } from '@antv/x6';
import { Rectangle } from '@antv/x6';
import type { IEdgePort, IPosition } from './typing';

// 拓扑图开发中需要的工具函数

// 计算一条边使用哪些连接桩连接节点
// 根据节点的位置进行计算
export const computeEdgePort: (source: IPosition, target: IPosition) => IEdgePort = (
  source,
  target,
) => {
  if (source.x === target.x && source.y !== target.y) {
    return {
      source: source.y - target.y > 0 ? 't1' : 'b1',
      target: source.y - target.y > 0 ? 'b1' : 't1',
    };
  }
  if (source.y === target.y && source.x !== target.x) {
    return {
      source: source.x - target.x > 0 ? 'l1' : 'r1',
      target: source.x - target.x > 0 ? 'r1' : 'l1',
    };
  }
  const sourceIsTargetLeft = source.x - target.x < 0;
  return {
    source: sourceIsTargetLeft ? 'r1' : 'l1',
    target: sourceIsTargetLeft ? 'l1' : 'r1',
  };
};

// 根据节点在总节点总的顺序，计算节点的位置， 类似与一个节点的网格布局
export const computePosition = (seq: number, total: number, width: number, height: number) => {
  let rows = 2;
  if (total > 8) {
    rows = 3;
  }
  const col_width = width / (total / rows);
  const row_height = height / rows;
  const row_number = seq % rows;
  const col_number = Math.floor(seq / rows);

  return {
    x: col_number * col_width + col_width / 2,
    y: row_number * row_height + row_height / 2,
  };
};

export const graphPosToContainerPoint = (
  graph: Graph,
  graphPoint: IPosition,
  container: HTMLDivElement | null,
) => {
  const point = graph.localToPage({ x: graphPoint.x, y: graphPoint.y });
  const containerRect = container?.getBoundingClientRect();
  const y = point.y - (containerRect?.y || 0);
  const x = point.x - (containerRect?.x || 0);
  return { x, y };
};

export const centerContainer = (graph: Graph, fit?: boolean) => {
  // 画布内容 居中到容器中心
  const containerRect = graph.container.getBoundingClientRect();
  const rect = new Rectangle(
    containerRect.x,
    containerRect.y,
    containerRect.width,
    containerRect.height,
  );

  graph.centerPoint(rect.getCenter().x, rect.getCenter().y);
  const nodes = graph.getNodes();
  const content = graph.getContentArea();
  const p = content.getCenter().diff(rect.getCenter());
  nodes.forEach((n) => {
    const pos = n.position();
    n.position(pos.x - p.x, pos.y - p.y);
  });
  if (fit && (content.width > rect.width || content.height > rect.height)) {
    graph.zoomToFit();
  }
};
