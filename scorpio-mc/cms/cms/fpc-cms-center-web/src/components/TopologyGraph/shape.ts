import { Color, Graph } from '@antv/x6';

const WIDTH = 70;
const HEIGHT = 50;

export const portsConfig = {
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
            style: {
              visibility: 'hidden',
            },
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
            style: {
              visibility: 'hidden',
            },
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
            style: {
              visibility: 'hidden',
            },
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
            style: {
              visibility: 'hidden',
            },
          },
        },
      },
    },
    items: [
      {
        id: 't1',
        group: 'top',
      },
      {
        id: 'r1',
        group: 'right',
      },
      {
        id: 'b1',
        group: 'bottom',
      },
      {
        id: 'l1',
        group: 'left',
      },
    ],
  },
};

export const FlowChartImageRect = Graph.registerNode('flow-chart-image-rect', {
  inherit: 'rect',
  width: WIDTH,
  height: HEIGHT,
  x: 70,
  y: 50,
  attrs: {
    body: {
      strokeWidth: 0,
      fillOpacity: 0,
      pointerEvents: 'visiblePainted',
    },
    image: {
      width: WIDTH,
      height: HEIGHT,
      pointerEvents: 'none',
    },
    text: {
      rfontSize: 12,
      fill: 'rgba(0,0,0,0.85)',
      y: HEIGHT - 10,
      textWrap: {
        text: '',
        // width: -10,
        ellipsis: true,
        breakWord: true,
        height: 40,
      },
    },
  },
  attrHooks: {
    fill: {
      set(val) {
        const rgba = Color.fromString(val as string)?.toRGBA();
        if (rgba) {
          return Color.invert(rgba, false).toString();
        }
        return val as any;
      },
    },
  },
  markup: [
    {
      tagName: 'rect',
      selector: 'body',
    },
    {
      tagName: 'image',
      selector: 'image',
    },
    {
      tagName: 'text',
      selector: 'text',
    },
  ],
  ...portsConfig,
});
