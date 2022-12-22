import clientIcon from '@/assets/icons/client.svg';
import cloudIcon from '@/assets/icons/cloud.svg';
import firewallICon from '@/assets/icons/firewall.svg';
import routerIcon from '@/assets/icons/router.svg';
import serverIcon from '@/assets/icons/server.svg';
import switchIcon from '@/assets/icons/switch.svg';
import appIcon from '@/assets/icons/app.svg';
import type { Edge, Node } from '@antv/x6';
import { v1 as uuidv1 } from 'uuid';
import type { IPosition, NodeProperties } from './typing';

export const generateEdge = (
  sourceNodeId: string,
  targetNodeId: string,
  sourcePort: string,
  targetPort: string,
  edgeMsg: string,
) => {
  // 边的数据结构
  const edgeId = uuidv1();
  const edgeConfig: Edge.Metadata = {
    shape: 'edge',
    attrs: { line: { stroke: '#5F95FF', strokeWidth: 1, targetMarker: '' } },
    id: edgeId,
    zIndex: 0,
    /** @see https://antv-x6.gitee.io/zh/docs/api/registry/router */
    router: { name: 'manhattan' },
    connector: { name: 'jumpover' },
    labels: [
      {
        attrs: {
          label: {
            text: edgeMsg,
          },
        },
      },
    ],
    source: {
      cell: sourceNodeId,
      port: sourcePort,
    },
    target: {
      cell: targetNodeId,
      port: targetPort,
    },
    tools: {
      name: 'button-remove',
    },
  };
  return edgeConfig;
};

export const generateNode = (
  id: string,
  nodeName: string,
  imagePath: string,
  position: IPosition,
) => {
  const nodeMeta: Node.Metadata = {
    shape: 'flow-chart-image-rect',
    id,
    ports: {
      groups: {
        top: {
          position: 'top',
          attrs: {
            circle: {
              r: 3,
              magnet: true,
              stroke: '#5F95FF',
              strokeWidth: 1,
              fill: '#fff',
              style: { visibility: 'hidden' },
            },
          },
        },
        right: {
          position: 'right',
          attrs: {
            circle: {
              r: 3,
              magnet: true,
              stroke: '#5F95FF',
              strokeWidth: 1,
              fill: '#fff',
              style: { visibility: 'hidden' },
            },
          },
        },
        bottom: {
          position: 'bottom',
          attrs: {
            circle: {
              r: 3,
              magnet: true,
              stroke: '#5F95FF',
              strokeWidth: 1,
              fill: '#fff',
              style: { visibility: 'hidden' },
            },
          },
        },
        left: {
          position: 'left',
          attrs: {
            circle: {
              r: 3,
              magnet: true,
              stroke: '#5F95FF',
              strokeWidth: 1,
              fill: '#fff',
              style: { visibility: 'hidden' },
            },
          },
        },
      },
      items: [
        { id: 't1', group: 'top' },
        { id: 'r1', group: 'right' },
        { id: 'b1', group: 'bottom' },
        { id: 'l1', group: 'left' },
      ],
    },
    attrs: {
      text: { textWrap: { text: nodeName } },
      image: {
        'xlink:href': imagePath,
      },
    },
    zIndex: 10,
    size: {
      width: 60,
      height: 60,
    },
    position,
  };
  return nodeMeta;
};

// TODO： 以后可能需要添加节点， 节点icon可能需要更改
export const clientUser: NodeProperties = {
  text: '终端用户',
  iconName: 'client',
  icon: clientIcon,
};

export const WAN: NodeProperties = {
  text: '广域网',
  iconName: 'cloud',
  icon: cloudIcon,
};

export const firewall: NodeProperties = {
  text: '防火墙',
  iconName: 'firewall',
  icon: firewallICon,
};

export const router: NodeProperties = {
  text: '路由器',
  iconName: 'router',
  icon: routerIcon,
};

export const server: NodeProperties = {
  text: '服务器',
  iconName: 'server',
  icon: serverIcon,
};

export const switchMachine: NodeProperties = {
  text: '交换机',
  iconName: 'switch',
  icon: switchIcon,
};

export const app: NodeProperties = {
  text: '应用',
  iconName: 'app',
  icon: appIcon,
};
