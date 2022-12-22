import type { ECOption } from '@/components/ReactECharts';
import { customECharts } from '@/components/ReactECharts';
import worldMapJson from '@/components/ReactECharts/assets/world.json';
import ReactEchartsCore from 'echarts-for-react/lib/core';
import type { EChartsInstance } from 'echarts-for-react/lib/types';
import * as echarts from 'echarts/core';
import { useMemo } from 'react';
// @ts-ignore
echarts.registerMap('world', worldMapJson);

/** 地图用途 */
export enum EMapType {
  MAPGUN = 0,
  SELECTER = 1,
}

export interface ILineEffectPoint {
  coord: [string, string];
}

interface IWorldMapProps {
  /** 异常事件飞行线数据 */
  lineEffectData?: [ILineEffectPoint, ILineEffectPoint][];

  // 地图类型
  mapType?: EMapType;
  selectPosition?: [number, number];
  mousePosition?: [number, number];

  onChartRendered?: (instance: EChartsInstance) => void;
}
const WorldMap = ({
  mousePosition,
  selectPosition,
  mapType = EMapType.MAPGUN,
  onChartRendered,
  lineEffectData,
}: IWorldMapProps) => {
  const mapData = useMemo(() => {
    if (mapType === EMapType.SELECTER && selectPosition) {
      return [[{ coord: selectPosition || [] }, { coord: selectPosition || [] }]];
    }
    if (lineEffectData) {
      return lineEffectData;
    }
    return [];
  }, [lineEffectData, mapType, selectPosition]);

  const option: ECOption = {
    title: {
      left: 'center',
      textStyle: {
        color: '#fff',
      },
    },
    geo: [
      {
        map: 'world',
        zoom: 1.2, // 当前视角的缩放比例
        scaleLimit: {
          // 控制滚轮缩放大小
          max: 1.3,
          min: 1,
        },
        label: {
          emphasis: {
            show: false,
          },
        },
        roam: mapType === EMapType.SELECTER || false, // 是否开启平游或缩放
        itemStyle: {
          normal: {
            borderColor: 'rgba(0,63,140,0.2)',
            shadowColor: 'rgba(0,63,140,0.2)',
            // 地图板块的背景色
            areaColor: 'rgb(238 238 238)',
          },
          emphasis: {
            // 鼠标悬浮时的背景
            areaColor: 'rgb(238 238 238)',
          },
        },
      },
    ],
    tooltip: {
      trigger: 'item',
      formatter: () => {
        return mapType === EMapType.SELECTER
          ? `
          <div>经度: ${mousePosition ? mousePosition[0] : undefined}</div>
          <div>纬度: ${mousePosition ? mousePosition[1] : undefined}</div>
        `
          : '';
      },
    },
    series: [
      {
        name: '异常事件',
        type: 'lines',
        zlevel: 1,
        effect: {
          show: true, // 启用飞行效果
          period: 6, // 飞行速度
          trailLength: 0.7, // 飞行线的拖尾
          color: mapType === EMapType.SELECTER ? 'yellow' : 'red', // 飞行线的颜色
          symbolSize: mapType === EMapType.SELECTER ? 20 : 3, // 飞行线的宽度
        },
        lineStyle: {
          color: '#ffa022',
          width: 0,
          curveness: 0.2, // 飞行线的弯曲程度
        },
        data: mapData as any,
      },
    ],
  };

  return (
    <ReactEchartsCore
      onChartReady={(echart) => {
        if (onChartRendered) {
          onChartRendered(echart);
        }
      }}
      echarts={customECharts}
      option={option}
      style={{ height: '100%' }}
    />
  );
};

export default WorldMap;
