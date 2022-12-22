import type { ConnectState } from '@/models/connect';
import { CopyOutlined, DeleteOutlined, EditOutlined } from '@ant-design/icons';
import type { Cell, Model } from '@antv/x6';
import { Addon, Edge, Graph, Node, Shape } from '@antv/x6';
import { Menu } from '@antv/x6-react-components';
import '@antv/x6-react-components/es/menu/style/index.css';
import '@antv/x6-react-shape';
import { useClickAway } from 'ahooks';
import { Empty } from 'antd';
import { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react';
import type { TTheme } from 'umi';
import { connect } from 'umi';
import AnyWhereContainer from '../AnyWhereContainer';
import ConfigPanel from './components/ConfigPanel';
import { GraphZoomTool } from './components/GraphZoomTool';
import ImageNode from './components/ImageNode';
import styles from './index.less';
import { portsConfig } from './shape';
import type { EventCallbacks, IPosition, NodeProperties, TopologyGraphOptions } from './typing';
import { centerContainer, graphPosToContainerPoint } from './utils';

Graph.registerReactComponent('image-node', <ImageNode />);

const initGraph = (containerId: string, editable: boolean) => {
  const graph = new Graph({
    container: document.getElementById(containerId) as HTMLElement,
    autoResize: true,
    grid: {
      size: 20,
      visible: true,
      // 背景颜色
      type: 'doubleMesh',
      args: [
        {
          color: '#f2f2f2', // 主网格线颜色
          thickness: 1, // 主网格线宽度
        },
        {
          color: '#e5e5e5', // 次网格线颜色
          thickness: 1.5, // 次网格线宽度
          factor: 10, // 主次网格线间隔
        },
      ],
    },
    // 点选/框选
    selecting: {
      enabled: true,
      multiple: editable,
      // 关闭框选
      rubberband: false,
      movable: editable,
    },
    scroller: {
      enabled: editable,
      // 开启画布平移能力
      pannable: editable,
    },
    interacting: () => {
      return {
        magnetConnectable: editable,
        edgeMovable: editable,
        nodeMovable: editable,
        nodeLabelMovable: false,
        edgeLabelMovable: false,
        arrowheadMovable: editable,
        vertexMovable: editable,
        vertexAddable: editable,
        vertexDeletable: editable,
      };
    },
    transforming: {
      clearAll: true, // 创建新组件的时候是否清除页面上存在的其他组件
      clearOnBlankMouseDown: true, // 点击空白区域的时候是否清除组件
    },
    connecting: {
      anchor: 'center',
      connectionPoint: 'anchor',
      allowBlank: false,
      highlight: true,
      snap: true,
      createEdge() {
        return new Shape.Edge({
          attrs: {
            line: {
              stroke: '#5F95FF',
              strokeWidth: 1,
              targetMarker: '',
            },
          },
          connector: {
            name: 'jumpover',
            args: {
              type: 'gap',
              radius: 12,
            },
          },
          label: {
            position: {
              distance: 0.5,
              offset: 40,
            },
          },
          // @see: https://x6.antv.vision/zh/docs/api/registry/router
          router: {
            name: 'manhattan',
          },
          zIndex: 0,
        });
      },
      validateConnection({ sourceView, targetView, sourceMagnet, targetMagnet }) {
        if (sourceView === targetView) {
          return false;
        }
        if (!sourceMagnet) {
          return false;
        }
        if (!targetMagnet) {
          return false;
        }
        return true;
      },
    },
    highlighting: {
      magnetAvailable: {
        name: 'stroke',
        args: {
          padding: 4,
          attrs: {
            strokeWidth: 4,
            stroke: 'rgba(223,234,255)',
          },
        },
      },
    },
    // 对齐线
    snapline: true,
    history: true,
    // 剪切板
    clipboard: {
      enabled: true,
    },
    keyboard: {
      enabled: true,
    },
    // 滚轮缩放
    mousewheel: {
      enabled: true,
      modifiers: ['ctrl', 'meta'],
    },
    embedding: {
      enabled: true,
      findParent({ node }) {
        const bbox = node.getBBox();
        return this.getNodes().filter((internalNode) => {
          // 只有 data.parent 为 true 的节点才是父节点
          const data = internalNode.getData<any>();
          if (data && data.parent) {
            const targetBBox = internalNode.getBBox();
            return bbox.isIntersectWithRect(targetBBox);
          }
          return false;
        });
      },
    },
  });

  return graph;
};

const initStencil = (graph: Graph, stencilContainerId: string) => {
  const stencil = new Addon.Stencil({
    target: graph,
    title: '可选网元',
    groups: [
      {
        name: 'basic',
        title: '可选网元',
        graphWidth: 200,
        graphHeight: 500,
        collapsable: false, // 分组是否可折叠，默认为 true
      },
    ],
  });
  const stencilContainer = document.getElementById(stencilContainerId);
  stencilContainer?.appendChild(stencil.container);
  return stencil;
};

// 初始化拓扑图中可使用的节点
const initShape = (graph: Graph, stencil: Addon.Stencil, nodeInfos: NodeProperties[]) => {
  const nodes: Node[] = [];
  nodeInfos.forEach((el) => {
    nodes.push(
      graph.createNode({
        width: 50,
        height: 70,
        shape: 'react-shape',
        data: {
          title: el.text,
          iconName: el.iconName,
        },
        ...portsConfig,
        component: 'image-node',
      }),
    );
  });

  stencil.load(nodes, 'basic');
};

const showPorts = (ports: NodeListOf<SVGAElement>, show: boolean) => {
  for (let i = 0, len = ports.length; i < len; i += 1) {
    // eslint-disable-next-line no-param-reassign
    ports[i].style.visibility = show ? 'visible' : 'hidden';
  }
};

const initEvent = (graph: Graph, graphEvents: EventCallbacks, editable: boolean) => {
  if (editable) {
    graph.bindKey('del', () => {
      const selectedCells = graph.getSelectedCells();
      if (selectedCells.length) {
        graph.removeCells(selectedCells);
      }
      return false;
    });
    graph.bindKey('backspace', () => {
      const selectedCells = graph.getSelectedCells();
      if (selectedCells.length) {
        graph.removeCells(selectedCells);
      }
      return false;
    });
    graph.bindKey('ctrl+c', () => {
      const selectedCells = graph.getSelectedCells();
      if (selectedCells.length) {
        graph.copy(selectedCells);
      }
      return false;
    });
    graph.bindKey('ctrl+v', () => {
      if (!graph.isClipboardEmpty()) {
        const pasteCells = graph.paste({ offset: 32 });
        graph.cleanSelection();
        graph.select(pasteCells);
      }
      return false;
    });
    graph.bindKey('ctrl+z', () => {
      graph.undo();
    });
  }

  graph.enableKeyboard();
  // 画布大小变化时， 将内容放置到 视口中心
  graph.on('resize', () => {
    centerContainer(graph);
  });
  graph.on('node:dblclick', ({ e, node }) => {
    if (graphEvents.nodeDbClick) {
      graphEvents.nodeDbClick(e, node);
    }
  });
  graph.on('blank:click', () => {
    if (graphEvents.blankClick) {
      graphEvents.blankClick();
    }
  });
  graph.on('node:mouseenter', ({ e, node }) => {
    if (editable) {
      const ports = graph.container.querySelectorAll('.x6-port-body') as NodeListOf<SVGAElement>;
      showPorts(ports, true);
    }
    if (graphEvents.nodeMouseEnter) {
      graphEvents.nodeMouseEnter(e, node);
    }
  });
  graph.on('node:mouseleave', ({ e, node }) => {
    if (editable) {
      const ports = document.querySelectorAll('.x6-port-body') as NodeListOf<SVGAElement>;
      showPorts(ports, false);
    }
    if (graphEvents.nodeMouseLeave) {
      graphEvents.nodeMouseLeave(e, node);
    }
  });
  graph.on('node:added', ({ node }) => {
    if (!editable) {
      const re = /(.*\/)*([^.]+)/i;
      const fileName = (node.getAttrs().image ? node.getAttrs().image['xlink:href'] : '') as string;
      // 填充默认的 app 图标，在测试环境下会出现 fileName 为空的报错
      const svg = fileName ? fileName.replace(re, '$2').split('.')[0] : 'app';
      import(`@/assets/icons/${svg}.svg`).then((res) => {
        node.setAttrs({
          image: {
            'xlink:href': res.default,
          },
        });
      });
    }
  });
  graph.on('edge:selected', ({ edge }) => {
    if (editable) {
      edge.attr('line/strokeDasharray', 5);
      edge.attr('line/style', {
        animation: 'ant-line 30s infinite linear',
      });
    }
  });
  graph.on('edge:unselected', ({ edge }) => {
    if (editable) {
      edge.attr('line/strokeDasharray', null);
      edge.attr('line/style', null);
    }
  });
  // 鼠标划过边时，显示可拖动的箭头
  // @see: https://x6.antv.vision/zh/docs/api/registry/edge-tool/
  graph.on('edge:mouseenter', ({ e, edge }) => {
    if (editable) {
      edge.addTools([
        {
          name: 'source-arrowhead',
          args: {
            attrs: {
              fill: 'red',
            },
          },
        },
        {
          name: 'target-arrowhead',
          args: {
            attrs: {
              fill: 'red',
            },
          },
        },
      ]);
    }
    if (graphEvents.edgeMouseEnter) {
      graphEvents.edgeMouseEnter(e, edge);
    }
  });
  graph.on('edge:mouseleave', ({ e, edge }) => {
    if (editable) {
      edge.removeTools();
    }
    if (graphEvents.edgeMouseLeave) {
      graphEvents.edgeMouseLeave(e, edge);
    }
  });
  graph.on('edge:click', ({ e, edge }) => {
    if (graphEvents.edgeClick) {
      graphEvents.edgeClick(e, edge);
    }
  });
  graph.on('resize', () => {
    if (graphEvents.graphResize) {
      graphEvents.graphResize();
    }
  });
};

export interface ITopologyGraphProps {
  id: string;
  theme: TTheme;
  nodes?: NodeProperties[];
  onGrapgReady: (graph: Graph) => void;
  cells: Model.ToJSONData;
  graphEvents?: EventCallbacks;
  options?: TopologyGraphOptions;
  cellLoaded?: () => void;
  fit?: boolean;
}

// 编辑用拓扑图
const EditableTopologyGraph = (props: ITopologyGraphProps) => {
  const { id, theme, nodes, onGrapgReady, cells, graphEvents, options } = props;

  const [graph, setGraph] = useState<Graph | null>(null);
  const [stencil, setStencil] = useState<Addon.Stencil | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const nodeMenuRef = useRef<HTMLDivElement>(null);
  const graphMenuRef = useRef<HTMLDivElement>(null);
  const [currentSelectedCell, setCurrentSelectedCell] = useState<Cell>();

  const [graphMenuDisplay, setGraphMenuDisplay] = useState(false);
  const [graphMenuPos, setGraphMenuPos] = useState<IPosition>({ x: -1, y: -1 });
  const [menuSelectedCell, setMenuSelectedCell] = useState<Cell | null>(null);
  const [nodeMenuPos, setNodeMenuPos] = useState<IPosition>({ x: -1, y: -1 });
  const [nodeMenuDisplay, setNodeMenuDisplay] = useState(false);

  const interId = useMemo(() => `${id}.editable`, [id]);

  useLayoutEffect(() => {
    const newGraph = initGraph(interId, true);
    const newStencil = initStencil(newGraph, 'stencil');
    setStencil(newStencil);
    setGraph(newGraph);
    newGraph.centerContent();
    onGrapgReady(newGraph);
  }, [interId, onGrapgReady]);

  useEffect(() => {
    if (graph && stencil) {
      initShape(graph, stencil, nodes || []);
    }
  }, [graph, nodes, stencil]);

  useClickAway(() => {
    setNodeMenuDisplay(false);
  }, []);

  useClickAway(() => {
    setGraphMenuDisplay(false);
  }, []);

  const resetNodeMenuStatus = useCallback(() => {
    setMenuSelectedCell(null);
    setNodeMenuDisplay(false);
  }, []);

  // 事件处理
  useEffect(() => {
    if (graph) {
      initEvent(graph, { ...graphEvents }, true);

      graph.on('node:contextmenu', (args) => {
        setNodeMenuPos(
          graphPosToContainerPoint(graph, { x: args.x, y: args.y }, containerRef.current),
        );
        setMenuSelectedCell(args.cell);

        setNodeMenuDisplay(true);
      });

      graph.on('blank:contextmenu', (args) => {
        setGraphMenuPos(
          graphPosToContainerPoint(graph, { x: args.x, y: args.y }, containerRef.current),
        );
        setGraphMenuDisplay(true);
      });

      graph.on('cell:click', ({ cell }) => {
        setCurrentSelectedCell(cell);
      });

      graph.on('blank:click', () => {
        setCurrentSelectedCell(undefined);
      });

      graph.centerContent();
    }
  }, [graph, graphEvents]);

  useEffect(() => {
    if (graph !== null && containerRef.current) {
      graph.clearCells();
      // 根据数据，初始化拓扑图
      const initNodes: Node[] = [];
      const edges: Edge[] = [];
      cells.cells.map((cell) => {
        const type = cell.shape;
        if (type) {
          if (Node.registry.exist(type)) {
            initNodes.push(Node.create(cell));
          }
          if (Edge.registry.exist(type)) {
            edges.push(Edge.create(cell));
          }
        }
        return null;
      });
      graph.addNodes(initNodes);
      graph.addEdges(edges);

      centerContainer(graph);
    }
  }, [cells, graph]);

  useEffect(() => {
    if (graph) {
      if (theme === 'dark') {
        graph.hideGrid();
      } else {
        graph.showGrid();
      }
    }
  }, [graph, theme]);

  const onNodeCopy = useCallback(() => {
    if (menuSelectedCell !== null) {
      graph!.copy([menuSelectedCell]);
    }
    resetNodeMenuStatus();
  }, [graph, menuSelectedCell, resetNodeMenuStatus]);
  const onNodeDelete = useCallback(() => {
    if (menuSelectedCell !== null) {
      graph!.removeCell(menuSelectedCell);
    }
    resetNodeMenuStatus();
  }, [graph, menuSelectedCell, resetNodeMenuStatus]);

  const onNodeRename = useCallback(() => {
    resetNodeMenuStatus();
  }, [resetNodeMenuStatus]);

  const onNodePaste = useCallback(() => {
    if (graph) {
      if (!graph.isClipboardEmpty()) {
        const pasteCells = graph.paste({ offset: 32 });
        graph.cleanSelection();
        graph.select(pasteCells);
      }
    }

    setGraphMenuDisplay(false);
  }, [graph]);

  const nodeMenu = (
    <div ref={nodeMenuRef}>
      <Menu hasIcon={true}>
        <Menu.Item onClick={onNodeCopy} icon={<CopyOutlined />} hotkey="Ctrl+C|Cmd+C" text="复制" />
        <Menu.Item
          onClick={onNodeDelete}
          icon={<DeleteOutlined />}
          hotkey="DEL|Backspace"
          text="删除"
        />
        <Menu.Item onClick={onNodeRename} icon={<EditOutlined />} text="重命名" />
      </Menu>
    </div>
  );

  const graphMenu = (
    <div ref={graphMenuRef}>
      <Menu hasIcon={true}>
        <Menu.Item
          onClick={onNodePaste}
          icon={<CopyOutlined />}
          hotkey="Ctrl+V | Cmd+V"
          text="粘贴"
        />
      </Menu>
    </div>
  );

  return (
    <>
      <div className={styles.wrap} ref={containerRef}>
        <div className={styles.content}>
          <div
            id="stencil"
            className={styles.sider}
            style={theme === 'light' ? { color: '#000' } : { color: '#fff' }}
          />
          <div
            id={interId}
            className={styles.graphContainer}
            style={theme === 'light' ? { color: '#000' } : { color: '#fff' }}
          />
          <div className={styles.configContainer}>
            {graph ? (
              <ConfigPanel cell={currentSelectedCell} options={options} />
            ) : (
              <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="选择节点或边进行配置" />
            )}
          </div>
        </div>
        <AnyWhereContainer
          top={nodeMenuPos.y}
          left={nodeMenuPos.x}
          style={{ padding: 0 }}
          display={nodeMenuDisplay}
          children={nodeMenu}
        />
        <AnyWhereContainer
          top={graphMenuPos.y}
          left={graphMenuPos.x}
          style={{ padding: 0 }}
          display={graphMenuDisplay}
          children={graphMenu}
        />
      </div>
    </>
  );
};

// 展示用拓扑图
const TopologyGraphPreview = (props: ITopologyGraphProps) => {
  const { id, graphEvents, onGrapgReady, cells, theme, cellLoaded, fit = false } = props;
  const graphRef = useRef<Graph | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const interId = useMemo(() => `${id}.notEditable`, [id]);

  useLayoutEffect(() => {
    const graph = initGraph(interId, false);
    graphRef.current = graph;
    onGrapgReady(graph);
  }, [interId, onGrapgReady]);

  useEffect(() => {
    if (graphRef.current) {
      const graph = graphRef.current;
      initEvent(graph, { ...graphEvents }, false);
    }
  }, [graphEvents]);

  useEffect(() => {
    if (graphRef.current !== null && containerRef.current) {
      const graph = graphRef.current;
      graph.clearCells();
      // 根据数据，初始化拓扑图
      const nodes: Node[] = [];
      const edges: Edge[] = [];
      cells.cells.map((cell) => {
        const type = cell.shape;
        if (type) {
          if (Node.registry.exist(type)) {
            nodes.push(Node.create(cell));
          }
          if (Edge.registry.exist(type)) {
            edges.push(Edge.create(cell));
          }
        }
        return null;
      });
      graph.addNodes(nodes);
      graph.addEdges(edges);
      if (cellLoaded) {
        cellLoaded();
      }

      centerContainer(graph, fit);
    }
  }, [cellLoaded, cells, fit]);

  useEffect(() => {
    if (graphRef.current) {
      if (theme === 'dark') {
        graphRef.current.hideGrid();
      } else {
        graphRef.current.showGrid();
      }
    }
  }, [theme]);

  const handleGraphZoom = useCallback(
    (action: 'in' | 'out' | 'fit' | 'real') => () => {
      if (graphRef.current) {
        switch (action) {
          // 缩放特定比例
          case 'in':
            graphRef.current.zoom(0.1);
            break;
          // 缩放特定比例
          case 'out':
            graphRef.current.zoom(-0.1);
            break;
          // 缩放到适应画布
          case 'fit':
            graphRef.current.zoomToFit({ padding: 12 });
            break;
          // 缩放到实际尺寸
          case 'real':
            graphRef.current.scale(1);
            graphRef.current.centerContent();
            break;
          default:
            break;
        }
      }
    },
    [graphRef],
  );

  return (
    <>
      <div className={styles.wrap}>
        <div className={styles.content}>
          <GraphZoomTool
            onZoomIn={handleGraphZoom('in')}
            onZoomOut={handleGraphZoom('out')}
            onFitContent={handleGraphZoom('fit')}
            onRealContent={handleGraphZoom('real')}
          />
          <div
            ref={containerRef}
            id={interId}
            className={styles.graphContainer}
            style={theme === 'light' ? { color: '#000' } : { color: '#fff' }}
          />
        </div>
      </div>
    </>
  );
};

const TopologyGraph = (props: ITopologyGraphProps & { editable: boolean }) => {
  const { editable, nodes, theme, ...restProps } = props;
  if (editable) {
    return <EditableTopologyGraph {...restProps} nodes={nodes} theme={theme} />;
  }
  return <TopologyGraphPreview {...restProps} theme={theme} />;
};

export default connect(({ settings }: ConnectState) => ({
  theme: settings?.theme,
}))(TopologyGraph);
