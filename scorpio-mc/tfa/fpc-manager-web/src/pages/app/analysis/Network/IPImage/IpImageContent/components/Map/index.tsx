import * as echarts from 'echarts/core';
import ReactEcharts from 'echarts-for-react';
import chinaMapJson from '@/components/ReactECharts/assets/china.json';
// import worldMapJson from '@/components/ReactECharts/assets/world.json';
import newWorldMapJson from '@/components/ReactECharts/assets/newWorldCountries.json';
import { MapDataType } from '../../../typings';
import { bytesToSize } from '@/utils/utils';
import usePieChartLabelColor from '@/utils/hooks/usePieChartLabelColor';
import { useMemo, useState } from 'react';
import AutoHeightContainer from '@/components/AutoHeightContainer';
// @ts-ignore
echarts.registerMap('china', chinaMapJson);
// @ts-ignore
// echarts.registerMap('world', worldMapJson);
// @ts-ignore
echarts.registerMap('worldMap', newWorldMapJson);

interface MapConfig {
  MapType: string;
  ShowedDataType: string;
  MapData: MapDataDelType[];
  MapMaxValue: number;
  isFull?: boolean;
}
interface MapDataDelType {
  name: string;
  value: number;
}

const Map: React.FC<MapConfig> = ({
  MapType,
  ShowedDataType,
  MapData,
  MapMaxValue,
  isFull = false,
}) => {
  const [mapHeight, setMapHeight] = useState(430);
  const handleHeightChange = (height: number) => {
    setMapHeight(height);
  };
  const labelColor = usePieChartLabelColor();
  const option = useMemo(() => {
    return {
      title: {
        x: 'center',
        textStyle: {
          color: '#9c0505',
        },
      },
      tooltip: {
        trigger: 'item',
        formatter: (params: any) => {
          // console.log(params,'params');
          if (ShowedDataType === MapDataType.FLOWCOUNT) {
            return `
          <div>${params.name}</div>
          <div>${bytesToSize(params.value)}</div>
          `;
          }
          return `
          <div>${params.name}</div>
          <div>${params.value}</div>
          `;
        },
      },
      series: [
        {
          type: 'map',
          map: MapType,
          data: MapData,
          label: {
            show: false,
            color: 'black',
            fontStyle: 10,
            align: 'center',
          },
          // zoom: 1, // 当前缩放比例
          // roam: true, // 是否支持拖拽
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
              label: {
                show: false,
              },
            },
          },
          emphasis: {
            // 高亮显示
            label: {
              color: 'black',
              fontSize: 10,
            },
            itemStyle: {
              areaColor: 'rgba(0,63,140,0.8)', // 区域高亮颜色
            },
          },
          select: {
            label: {
              show: false,
            },
          },
        },
      ],

      visualMap: [
        {
          type: 'continuous',
          left: 'right',
          min: 0,
          max: MapMaxValue,
          color: ['orangered', 'yellow', 'lightskyblue', 'white'],
          orient: 'horizontal',
          text: ['高', '低'],
          textStyle: {
            color: labelColor,
          },
          calculable: true,
          formatter: function (value: number) {
            if (ShowedDataType === MapDataType.FLOWCOUNT) {
              return bytesToSize(value);
            }
            return value;
          },
        },
      ],
      toolbox: {
        show: false,
        //orient: 'vertical',
        left: 'left',
        top: 'top',
        feature: {
          dataView: { readOnly: false },
          restore: {},
          saveAsImage: {},
        },
      },
    };
  }, [MapData, MapMaxValue, MapType, ShowedDataType, labelColor]);

  const currentMap = useMemo(() => {
    return (
      <ReactEcharts
        echarts={echarts}
        option={option}
        opts={{ width: 'auto', height: isFull ? mapHeight : 430 }}
      />
    );
  }, [option, isFull, mapHeight]);
  return (
    <>
      {isFull ? (
        <AutoHeightContainer autoHeight={true} onHeightChange={handleHeightChange}>
          {currentMap}
        </AutoHeightContainer>
      ) : (
        currentMap
      )}
    </>
  );
};

export default Map;
