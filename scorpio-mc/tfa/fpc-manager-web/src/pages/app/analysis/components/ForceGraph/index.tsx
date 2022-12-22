import { useClickAway } from 'ahooks';
import type { ZoomTransform } from 'd3';
import * as d3 from 'd3';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { v4 as uuidv4 } from 'uuid';
import { EActionMenuKey } from '../../Network/IpGraph';
import type { IAnyWhereContainerRefReturn } from './components/AnyWhereContainer';
import AnyWhereContainer from './components/AnyWhereContainer';
import type { IControlAction } from './components/Control';
import GraphControl from './components/Control';
import type { D3Edge, D3Node, D3Simulation, IEdge, IForceGraphProps, INode } from './typings';
import { EForceGraphIndex, EForceGraphIndexLabel } from './typings';
import { exportPng, formatApplications, formatBytes, formatNumber, getLinksInArea } from './utils';
import { defaultDarkTheme, defaultLightTheme } from './utils/theme';

const ForceGraph = ({
  height = 500,
  weightField,
  nodes,
  edges,
  id,
  theme = defaultLightTheme,

  nodeActions = [],
  onNodeClick,

  edgeActions = [],
  onEdgeClick,

  brushActions = [],
  onBrushEnd,
  onDeleteRalation,

  historyGraph = true,

  edgeIndex,
}: IForceGraphProps) => {
  const svgName = useMemo(() => {
    return `connections-graph-${uuidv4()}`;
  }, []);
  // 容器
  const container = useRef<d3.Selection<any, unknown, any, any> | undefined>();
  // 力导向图实例
  const simulation = useRef<D3Simulation>();
  const d3Zoom = useRef<d3.ZoomBehavior<Element, unknown>>();

  // d3中所有的 node 节点
  const d3Node = useRef<D3Node>();
  // d3中所有的边
  const d3Edge = useRef<D3Edge>();
  // d3中所有的节点标签
  const d3NodeLabel = useRef<any>();
  // 当前正在被拖拽的节点
  const draggingNode = useRef<any>();
  // svg.current 画布
  const svg = useRef<d3.Selection<any, unknown, any, any> | undefined>();
  /**
   * D3 zoom 放缩结果
   * @see https://www.d3js.org.cn/document/d3-zoom/
   */
  let zoomTransform: ZoomTransform = d3.zoomIdentity;

  const maxLog = Math.ceil(Math.pow(Math.E, 9));

  const idRegex = /[\[\]:. ]/g;

  /** 解锁所有的节点 */
  const unLockAllNode = () => {
    container.current?.selectAll('.node').each((d: any) => {
      d.fx = undefined;
      d.fy = undefined;
    });
    simulation.current?.alphaTarget(0.3).restart();
  };

  /** 固定所有的节点 */
  const lockAllNode = () => {
    container.current?.selectAll('.node').each((d: any) => {
      d.fx = d.x;
      d.fy = d.y;
    });
    // simulation.current!.alphaTarget(0.3).restart();
  };

  /** 节点拖拽 */
  const handleDrag = (forceSimulation?: D3Simulation, callback?: any) => {
    function dragstarted(event: any, d: any) {
      console.log('dragstarted', d);
      event.sourceEvent.stopPropagation();
      if (!event.active) {
        forceSimulation?.alphaTarget(0.1).restart();
      }
      draggingNode.current = d;
      d.fx = d.x;
      d.fy = d.y;
      nodeFocus(d);
    }

    function dragged(event: any, d: any) {
      d.fx = event.x;
      d.fy = event.y;
    }

    function dragended(event: any, d: any) {
      if (!event.active) {
        forceSimulation?.alphaTarget(0).stop();
      }

      draggingNode.current = undefined;
      d.fx = event.x;
      d.fy = event.y;
      nodeFocus(d);
      if (callback) {
        callback();
      }
    }

    return d3.drag().on('start', dragstarted).on('drag', dragged).on('end', dragended);
  };

  // highlighting helpers
  let linkedByIndex: Record<string, any> = {};
  /** 节点高亮 */
  function isConnected(a: INode, b: INode) {
    return linkedByIndex[a.id + ',' + b.id] || linkedByIndex[b.id + ',' + a.id] || a.id === b.id;
  }

  /** 节点聚焦 */
  function nodeFocus(d: INode) {
    // don't apply focus styles if dragging a node
    if (!draggingNode.current) {
      d3Node.current?.style('opacity', (o: INode) => {
        return isConnected(d, o) ? 1 : 0.1;
      });
      d3NodeLabel.current.attr('display', (o: INode) => {
        return isConnected(d, o) ? 'block' : 'none';
      });
      d3Edge.current?.style('opacity', (o) => {
        return (o.source as INode).index === d.index || (o.target as INode).index === d.index
          ? 1
          : 0.1;
      });
    }
  }

  /** 边聚焦 */
  function linkFocus(l: IEdge) {
    const sourceNode = l.source as unknown as INode;
    const targetNode = l.target as unknown as INode;
    if (!draggingNode.current) {
      d3Node.current?.style('opacity', (o: INode) => {
        // 把边连接的 2 个节点高亮
        return o.index === sourceNode.index || o.index === targetNode.index ? 1 : 0.1;
      });
      d3NodeLabel.current.attr('display', (o: INode) => {
        // 把边连接的 2 个节点的标签高亮
        return o.index === sourceNode.index || o.index === targetNode.index ? 'block' : 'none';
      });
      d3Edge.current?.style('opacity', (o) => {
        // 把这条边高亮
        return (o.source as INode).index === sourceNode.index &&
          (o.target as INode).index === targetNode.index
          ? 1
          : 0.1;
      });
    }
  }

  /** 接触所有的聚焦高亮 */
  function unfocus() {
    d3NodeLabel.current.attr('display', 'block');
    d3Node.current?.style('opacity', 1);
    d3Edge.current?.style('opacity', 1);
  }

  const [graphWidth, setGraphWidth] = useState(500);

  // 选中的节点
  const [selectedNode, setSelectedNode] = useState<INode>();
  // 选中的边
  const [selectedEdge, setSelectedEdge] = useState<IEdge>();

  const actionRef = useRef<IAnyWhereContainerRefReturn>(null);

  // 所有的link
  const [links, setLinks] = useState<any[]>([]);
  const updateD3Link = () => {
    // @ts-ignore
    if (!svg.current?._groups[0][0].getBoundingClientRect()) {
      return;
    }
    // @ts-ignore
    const { x: svgX, y: svgY } = svg.current?._groups[0][0].getBoundingClientRect();
    setLinks([
      ...// @ts-ignore
      d3Edge.current?._groups[0].map((link) => {
        const { x, y } = link.getBoundingClientRect();
        return {
          index: link?.__data__?.index,
          x: x - svgX,
          y: y - svgY,
          source: link?.__data__?.source,
          target: link?.__data__?.target,
        };
      }),
    ]);
  };

  const graphContainerRef = useCallback((node) => {
    if (node !== null) {
      setGraphWidth(node.getBoundingClientRect().width);
    }
  }, []);

  useEffect(() => {
    drawGraph(nodes, edges);
  }, [JSON.stringify(nodes), JSON.stringify(edges)]);

  useEffect(() => {
    return () => {
      console.log('卸载');
      // 卸载
      d3.zoom().on('zoom', null);
      if (simulation.current) {
        simulation.current.on('tick', null);
      }
      d3.drag().on('start', null).on('drag', null).on('end', null);
      if (svg.current) {
        d3Node.current?.on('mouseover', null).on('mouseout', null);
        d3Edge.current?.on('mouseover', null).on('mouseout', null);

        // remove svg.current elements
        d3Node.current?.exit().remove();
        d3Edge.current?.exit().remove();
        d3NodeLabel.current.exit().remove();
        svg.current.selectAll('.link').remove();
        svg.current.selectAll('.node').remove();
        svg.current.selectAll('.node-label').remove();
        container.current?.remove();
        svg.current.remove();
      }

      setTimeout(() => {
        // clean up global vars
        svg.current = undefined;
        container.current = undefined;

        d3Node.current = undefined;
        d3Edge.current = undefined;
        d3NodeLabel.current = undefined;
        zoomTransform = d3.zoomIdentity;

        draggingNode.current = undefined;
      });
    };
  }, []);

  useEffect(() => {
    // 动态变化主题
    svg.current?.style('background-color', theme.backgroundColor);
    d3Node.current?.attr('fill', theme.nodeColor);
    d3NodeLabel.current?.attr('fill', theme.nodeLabelColor);
    d3Edge.current?.attr('stroke', theme.edgeColor);
  }, [theme]);

  useEffect(() => {
    svg.current?.attr('height', height);
    simulation.current?.force('center', d3.forceCenter(graphWidth / 2, height / 2));
  }, [graphWidth, height]);

  useClickAway(() => {
    actionRef?.current?.updateVisible(false);
  }, []);

  const minMaxForScale = useMemo(() => {
    let nodeMax = 1;
    let nodeMin = 1;

    for (const n of nodes) {
      if (n[weightField] !== undefined) {
        if (n[weightField] > nodeMax) {
          nodeMax = n[weightField];
        }
        if (n[weightField] < nodeMin) {
          nodeMin = n[weightField];
        }
      }
    }

    let linkMax = 1;
    let linkMin = 1;

    for (const l of edges) {
      if (l[weightField] !== undefined) {
        if (l[weightField] > linkMax) {
          linkMax = l[weightField];
        }
        if (l[weightField] < linkMin) {
          linkMin = l[weightField];
        }
      }
    }

    let nodeScaleFactor = (nodeMax - nodeMin) / maxLog;
    if (nodeScaleFactor < 1) {
      nodeScaleFactor = 1;
    }
    let linkScaleFactor = (linkMax - linkMin) / maxLog;
    if (linkScaleFactor < 1) {
      linkScaleFactor = 1;
    }

    return {
      nodeMin,
      nodeMax,
      linkMin,
      linkMax,
      linkScaleFactor,
      nodeScaleFactor,
    };
  }, [weightField, JSON.stringify(nodes), JSON.stringify(edges)]);

  /**
   * 计算边的宽度
   * @param l 边
   * @returns
   */
  const calculateEdgeWeight = (l: IEdge) => {
    let val = weightField ? l[weightField] || 1 : 1;
    if (weightField) {
      val = Math.max(Math.log((val - minMaxForScale.linkMin) / minMaxForScale.linkScaleFactor), 0);
    }
    return 1 + val;
  };

  /**
   * 计算节点的大小
   * @param n 节点
   * @returns
   */
  const calculateNodeWeight = (n: INode) => {
    let val = weightField ? n[weightField] || 1 : 1;
    if (weightField) {
      val = Math.max(Math.log((val - minMaxForScale.nodeMin) / minMaxForScale.nodeScaleFactor), 0);
    }
    return 3 + val;
  };

  const calculateNodeLabelOffset = (nl: INode) => {
    const val = calculateNodeWeight(nl);
    return 2 + val;
  };

  const calculateCollisionRadius = (n: INode) => {
    const val = calculateNodeWeight(n);
    return 2 * val;
  };

  /** 显示 Edge 提示框 */
  const showEdgeInfo = (d: IEdge) => {
    console.log(d);
    svg.current
      ?.append('foreignObject')
      .attr('pointer-events', 'none')
      .style('user-select', 'none')
      .attr('x', 15)
      .attr('y', 15)
      .attr('width', 400)
      .attr('height', 250)
      .selectAll('.legend-table')
      .data(['legend-table'])
      .join('xhtml:table')
      .classed('legend-table', true)
      .html(() => {
        const returnHtml = `
        <style>
          .legend-table {
            width: auto;
            height: auto;
            padding: 4px;
            color:black;
            background: #ddd;
            pointer-events: none;
            border-radius: 10px;
            table-layout: fixed;
            border-collapse: separate;
            font-size: 12px;
          }

          .legend-table th, .legend-table td {
            line-height: 20px;
            text-align: right;
          }
          .legend-table th {
            width: 80px
          }
        </style>

        <table>
          <tr>
            <th>IP_A: </th>
            <td>${(d.source as INode).id}</td>
          </tr>
          <tr>
            <th>IP_B: </th>
            <td>${(d.target as INode).id}</td>
          </tr>
          ${edgeIndex
            ?.map((index: EForceGraphIndex) => {
              switch (index) {
                case EForceGraphIndex.TOTAL_BYTES:
                  return `<tr><th>${EForceGraphIndexLabel[index]}: </th><td>${formatBytes(
                    d.totalBytes,
                  )}</td></tr>`;
                case EForceGraphIndex.ESTABLISHEDSESSIONS:
                  return `<tr><th>${EForceGraphIndexLabel[index]}: </th><td>${formatNumber(
                    d[index],
                  )}</td></tr>`;
                case EForceGraphIndex.APPLICATIONS:
                  return `<tr><th>${EForceGraphIndexLabel[index]}: </th><td>${formatApplications(
                    d[index],
                  )}</td></tr>`;
                default:
                  return ``;
              }
            })
            .join('')}
        </table>`;
        return returnHtml;
      });
  };

  const removeLineInfo = () => {
    svg.current?.selectAll('foreignObject').remove();
  };

  // map which nodes are linked (for highlighting)
  const updateLinkedMap = (links: IEdge[]) => {
    linkedByIndex = {};
    links.forEach((d) => {
      linkedByIndex[d.source + ',' + d.target] = true;
    });
    console.log('linkedByIndex', linkedByIndex);
  };

  /** 绑定节点事件 */
  const bindNodeEvents = useCallback(() => {
    if (!d3Node.current) {
      return;
    }
    d3Node.current
      .on('mouseover', (e: any, d: INode) => {
        console.log('node mouseover', d);
        if (draggingNode.current) {
          return;
        }
        d3.select(e.currentTarget).style('cursor', 'pointer');
        nodeFocus(d);
      })
      .on('mouseout', (e: any) => {
        d3.select(e.currentTarget).style('cursor', 'default');
        unfocus();
      })
      .on('click', (event: any, d: INode) => {
        // 阻止冒泡
        event.stopPropagation();
        if (draggingNode.current) {
          return;
        }
        setSelectedEdge(undefined);
        setSelectedNode(d);
        nodeFocus(d);
        console.log('node click', d);

        if (nodeActions.length > 0) {
          // 显示节点操作菜单
          actionRef?.current?.updateVisible(true);
          // 更新弹出菜单的显示位置
          actionRef?.current?.updatePosition({
            left: (event.x as number) + 10,
            top: (event.y as number) + 10,
          });
        }
      })
      // @ts-ignore
      .call(handleDrag(simulation.current, updateD3Link));
  }, [simulation.current]);

  /** 绑定边事件 */
  const bindEdgeEvents = () => {
    if (!d3Edge.current) {
      return;
    }
    d3Edge.current
      .on('mouseover', (e: any, l: IEdge) => {
        console.log(e, l, d3Edge.current);
        if (draggingNode.current) {
          return;
        }
        d3.select(e.currentTarget).style('cursor', 'pointer');
        // 高亮这条边和 2 个节点
        linkFocus(l);
        showEdgeInfo(l);
      })
      .on('mouseout', (e: any) => {
        d3.select(e.currentTarget).style('cursor', 'default');
        unfocus();
        removeLineInfo();
      })
      .on('click', (event: any, d: IEdge) => {
        // 阻止冒泡
        event.stopPropagation();
        setSelectedNode(undefined);
        setSelectedEdge(d);
        console.log('edge click', event);
        console.log('edge click', d);

        if (nodeActions.length > 0) {
          // 显示节点操作菜单
          actionRef?.current?.updateVisible(true);
          // 更新弹出菜单的显示位置
          actionRef?.current?.updatePosition({
            left: (event.x as number) + 10,
            top: (event.y as number) + 10,
          });
        }
      });
  };

  /**
   * 画图
   */
  const drawGraph = (nodesData: INode[], linksData: IEdge[]) => {
    if (svg.current) {
      // remove any existing nodes
      d3Node.current!.exit().remove();
      d3Edge.current!.exit().remove();
      d3NodeLabel.current.exit().remove();
      svg.current.selectAll('.link').remove();
      svg.current.selectAll('.node').remove();
      svg.current.selectAll('.node-label').remove();
    }

    if (!nodesData.length) {
      return;
    }

    const nodesDataCopy = nodesData.map((d) => Object.create(d));
    const linksDataCopy = linksData.map((d) => Object.create(d));
    console.log(linksDataCopy[0].__proto__);
    updateLinkedMap(linksDataCopy);

    if (!svg.current) {
      svg.current = d3
        .select(`.${svgName}`)
        .attr('width', '100%')
        .attr('height', height)
        .attr('id', 'graphSvg');
    }

    // setup the force directed graph
    simulation.current = d3
      .forceSimulation(nodesDataCopy)
      //link froce(弹簧模型) 可以根据 link distance 将有关联的两个节点拉近或者推远。力的强度与被链接两个节点的距离成比例，类似弹簧力
      .force(
        'link',
        d3.forceLink(linksDataCopy).id((d) => {
          return (d as INode).id; // tell the links where to link
        }),
      )
      //作用力应用在所用的节点之间，当strength为正的时候可以模拟重力，当为负的时候可以模拟电荷力
      .force('charge', d3.forceManyBody().strength(-100).distanceMin(80))
      //设置节点碰撞半径>= 点半径避免重叠
      .force(
        'collision',
        d3.forceCollide().radius((n) => calculateCollisionRadius(n as INode)),
      )
      //centering 作用力可以使得节点布局开之后围绕某个中心
      .force('center', d3.forceCenter(graphWidth / 2, height / 2))
      // positioning force along x-axis for disjoint graph
      .force('x', d3.forceX())
      // positioning force along y-axis for disjoint graph
      .force('y', d3.forceY());

    if (!container.current) {
      // add container.current for zoomability
      // @ts-ignore
      container.current = svg.current.append('g');
    }
    svg.current
      .call(
        (d3Zoom.current = d3
          .zoom()
          .scaleExtent([0.5, 4])
          .on('zoom', (event) => {
            zoomTransform = event.transform;
            container.current!.attr('transform', event.transform);
            updateD3Link();
          })),
      )
      .on('click', () => {
        console.log('svg.current click');
        setSelectedNode(undefined);
        actionRef?.current?.updateVisible(false);
      });

    // add links
    d3Edge.current = container.current
      .append('g')
      .attr('class', 'link-wrap')
      .attr('stroke', theme.edgeColor)
      .attr('stroke-opacity', 0.4)
      .selectAll('line')
      .data(linksDataCopy)
      .enter()
      .append('line')
      .attr('class', 'link')
      .attr('id', function (d, i) {
        return 'link-path-' + i;
      })
      .attr('stroke-width', calculateEdgeWeight);

    // add link mouse listeners
    bindEdgeEvents();

    // add nodes
    d3Node.current = container.current
      .append('g')
      .attr('class', 'node-wrap')
      .selectAll('circle')
      .data(nodesDataCopy)
      .enter()
      .append('circle')
      .attr('class', 'node')
      .attr('id', (d) => {
        return 'id' + d?.id?.replace(idRegex, '_');
      })
      .attr('fill', () => {
        return theme.nodeColor;
      })
      .attr('r', calculateNodeWeight);
    // 节点外围的圆圈
    // .attr('stroke', theme.nodeColor)
    // .attr('stroke-width', 0.5);

    // Node 节点绑定事件
    bindNodeEvents();

    // add node labels
    d3NodeLabel.current = container.current
      .append('g')
      .attr('class', 'node-label-wrap')
      .selectAll('text')
      .data(nodesDataCopy)
      .enter()
      .append('text')
      .attr('dx', calculateNodeLabelOffset)
      .attr('id', (d) => {
        return 'id' + d?.id?.replace(idRegex, '_') + '-label';
      })
      .attr('dy', '2px')
      .attr('class', 'node-label')
      .attr('fill', theme.nodeLabelColor)
      .style('font-size', '12px')
      .style('font-weight', 'normal')
      .style('font-style', 'normal')
      .style('pointer-events', 'none') // to prevent mouseover/drag capture
      .text((d) => {
        return d.id;
      });

    // listen on each tick of the simulation.current's internal timer
    simulation.current?.on('tick', () => {
      // position links
      d3Edge.current
        ?.attr('x1', (d) => (d.source as INode).x!)
        .attr('y1', (d) => (d.source as INode).y!)
        .attr('x2', (d) => (d.target as INode).x!)
        .attr('y2', (d) => (d.target as INode).y!);

      // position nodes
      d3Node.current?.attr('cx', (d: INode) => d.x!).attr('cy', (d: INode) => d.y!);

      // position node labels
      d3NodeLabel.current?.attr('transform', (d: INode) => 'translate(' + d.x + ',' + d.y + ')');
      setTimeout(() => {
        updateD3Link();
      }, 100);
    });
  };

  const svgRef = useRef<any>();
  const svgRContainer = useRef<any>();
  /** brush相关 */
  const [brush, setBrush] = useState<boolean>(false);
  const [selectedLinks, setSelectedlinks] = useState<any[]>([]);
  /** 重制样式 */
  const resetD3Style = () => {
    d3Node.current?.style('opacity', (o: INode) => {
      return 1;
    });
    d3NodeLabel.current?.attr('display', (o: INode) => {
      return 'block';
    });
    d3Edge.current?.style('opacity', (o) => {
      return 1;
    });
    return;
  };

  /** 更新d3样式 */
  useEffect(() => {
    if (!brush) {
      resetD3Style();
    }
    d3Node.current?.style('opacity', (o: INode) => {
      if (selectedLinks.length === 0) {
        return 1;
      }
      for (const l of selectedLinks) {
        const sourceNode = l.source as unknown as INode;
        const targetNode = l.target as unknown as INode;
        if (o.index === sourceNode.index || o.index === targetNode.index) {
          return 1;
        }
      }
      return 0.1;
    });
    d3NodeLabel.current?.attr('display', (o: INode) => {
      if (selectedLinks.length === 0) {
        return 'block';
      }
      for (const l of selectedLinks) {
        const sourceNode = l.source as unknown as INode;
        const targetNode = l.target as unknown as INode;
        if (o.index === sourceNode.index || o.index === targetNode.index) {
          return 'block';
        }
      }
      return 'none';
    });
    d3Edge.current?.style('opacity', (o) => {
      if (selectedLinks.length === 0) {
        return 1;
      }
      for (const l of selectedLinks) {
        const sourceNode = l.source as unknown as INode;
        const targetNode = l.target as unknown as INode;
        if (
          (o.source as INode).index === sourceNode.index &&
          (o.target as INode).index === targetNode.index
        ) {
          return 1;
        }
      }
      return 0.1;
    });
  }, [selectedLinks]);

  const brushResult = useMemo(() => {
    const result: IEdge[] = [];
    const edgeList = d3Edge.current?.data() || [];
    selectedLinks.forEach((link) => {
      const edge = edgeList.find((e) => e.index === link.index);
      if (edge) {
        result.push({
          ...edge.__proto__,
          ipBAddress: edge.__proto__.source,
          ipAAddress: edge.__proto__.target,
        });
      }
    });
    return [...result];
  }, [selectedLinks]);

  const updateSelectedLink = useCallback(
    (event: any) => {
      const linksArea = [...getLinksInArea(event.selection, links)];
      setSelectedlinks(linksArea);
    },
    [links],
  );

  /** 开/关 brush功能 */
  const hanleBrush = useCallback(
    (svg: d3.Selection<any, unknown, any, any> | undefined, brush = true) => {
      if (!svg) {
        return;
      }

      if (brush) {
        d3Zoom.current!.on('zoom', (event) => {});
        svg!
          .append('g')
          .attr('class', 'brush')
          .call(
            d3
              .brush()
              .on('brush', updateSelectedLink)
              .on('end', (event) => {
                if (onBrushEnd && nodeActions.length > 0) {
                  // 显示节点操作菜单
                  actionRef?.current?.updateVisible(true);
                  // 更新弹出菜单的显示位置
                  actionRef?.current?.updatePosition({
                    left: (event.sourceEvent.clientX as number) + 10,
                    top: (event.sourceEvent.clientY as number) + 10,
                  });
                }
              }),
          );
        return;
      } else {
        resetD3Style();
        svg!.select('g.brush').remove();
        d3Zoom.current!.on('zoom', (event) => {
          zoomTransform = event.transform;
          container.current!.attr('transform', event.transform);
          updateD3Link();
        });
        setSelectedlinks([]);
        return;
      }
    },
    [updateSelectedLink, links, svg],
  );

  const handleControlClick = (action: IControlAction) => {
    switch (action.key) {
      case 'center': {
        if (brush) {
          hanleBrush(svg.current, false);
        }
        svg.current
          ?.transition()
          .duration(500)
          .call(d3Zoom.current!.translateTo, graphWidth / 2, height / 2);
        break;
      }
      case 'zoomIn':
        if (brush) {
          hanleBrush(svg.current, false);
        }
        svg.current?.transition().duration(500).call(d3Zoom.current!.scaleBy, 0.5);
        break;
      case 'zoomOut':
        if (brush) {
          hanleBrush(svg.current, false);
        }
        svg.current?.transition().duration(500).call(d3Zoom.current!.scaleBy, 2);
        break;
      case 'lock':
        if (brush) {
          hanleBrush(svg.current, false);
        }
        lockAllNode();
        break;
      case 'unlock':
        if (brush) {
          hanleBrush(svg.current, false);
        }
        unLockAllNode();
        break;
      case 'save':
        if (brush) {
          hanleBrush(svg.current, false);
        }
        exportPng(svgName);
        break;
      case 'brush':
        if (brush) {
          hanleBrush(svg.current, false);
        } else {
          hanleBrush(svg.current, true);
        }
        setBrush(!brush);
        break;
      default:
        break;
    }
  };

  return (
    <>
      <div
        ref={graphContainerRef}
        style={{
          position: 'relative',
          height: height,
        }}
      >
        <div ref={svgRContainer}>
          <svg className={svgName} ref={svgRef} />
        </div>

        <GraphControl onClick={handleControlClick} historyGraph={historyGraph} />
        <AnyWhereContainer ref={actionRef} style={{ padding: 0 }} theme={theme.mode}>
          {(() => {
            if (selectedLinks.length > 0 && brushActions.length > 0) {
              return (
                <ul>
                  {brushActions.map((action) => (
                    <li
                      id={`anywhere_li_${action.key}`}
                      key={action.key}
                      onClick={() => {
                        if (onBrushEnd && selectedLinks) {
                          // 传递 click
                          onBrushEnd(action, brushResult!);
                          // 移除菜单
                          actionRef?.current?.updateVisible(false);
                          setSelectedlinks([]);
                        }
                      }}
                    >
                      {action.label}
                    </li>
                  ))}
                </ul>
              );
            } else {
              return (
                <>
                  {selectedNode && nodeActions.length > 0 && (
                    <ul>
                      {nodeActions.map((action) => (
                        <li
                          key={action.key}
                          onClick={() => {
                            if (onNodeClick && selectedNode) {
                              if (action.key === EActionMenuKey.DELETE_NODE) {
                                if (onDeleteRalation) {
                                  onDeleteRalation(id, action, undefined, selectedNode.__proto__);
                                }
                              }
                              // 传递 click
                              onNodeClick(action, selectedNode!, id);
                              // 移除菜单
                              actionRef?.current?.updateVisible(false);
                              setSelectedNode(undefined);
                            }
                          }}
                        >
                          {action.label}
                        </li>
                      ))}
                    </ul>
                  )}
                  {selectedEdge && edgeActions.length > 0 && (
                    <ul>
                      {edgeActions.map((action) => (
                        <li
                          key={action.key}
                          onClick={() => {
                            if (onEdgeClick && selectedEdge) {
                              if (action.key === EActionMenuKey.DELETE_EDGE) {
                                if (onDeleteRalation) {
                                  onDeleteRalation(id, action, selectedEdge.__proto__, undefined);
                                }
                              }

                              // 传递 click
                              onEdgeClick(action, selectedEdge!, id);
                              // 移除菜单
                              actionRef?.current?.updateVisible(false);
                              setSelectedEdge(undefined);
                            }
                          }}
                        >
                          {action.label}
                        </li>
                      ))}
                    </ul>
                  )}
                </>
              );
            }
          })()}
        </AnyWhereContainer>
      </div>
    </>
  );
};

export * from './typings';
export { defaultLightTheme, defaultDarkTheme };
export default ForceGraph;
