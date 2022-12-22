import DisplaySlider from '@/components/DisplaySlider';
import type { ECOption } from '@/components/ReactECharts';
import ReactECharts from '@/components/ReactECharts';
import { bytesToSize } from '@/utils/utils';
import { TinyLine } from '@ant-design/plots';
import { Slider, Spin } from 'antd';
import _ from 'lodash';
import numeral from 'numeral';
import React, { Fragment, memo, useEffect, useRef, useState } from 'react';
import type { IIntervalData } from '../typings';
import styles from './index.less';

interface IntervalAnalyseProps {
  intervals: IIntervalData;
  intervalTime: number;
  filterIntervals?: IIntervalData;
  startTime: number | undefined;
  endTime: number | undefined;
  loading: boolean;
  onTimeChange: (relativeTime: number[]) => void;
}

interface LineChartProps {
  intervals: IIntervalData;
}

const CHART_MARGIN_LEFT = 100;
const CHART_MARGIN_RIGHT = 80;

class LineChart extends React.Component<LineChartProps> {
  shouldComponentUpdate(nextProps: LineChartProps) {
    return !_.isEqual(this.props.intervals, nextProps.intervals);
  }

  render() {
    const { intervals } = this.props;

    const numList: string[] = [];
    const frameList: number[] = [];
    const byteList: number[] = [];

    if (Array.isArray(intervals.intervals)) {
      intervals.intervals.forEach((item) => {
        numList.push(item[0]);
        frameList.push(item[1]);
        byteList.push(item[2]);
      });
    }

    const option: ECOption = {
      color: ['#1890FF', '#2FC25B'],
      grid: {
        bottom: 0,
      },
      xAxis: [
        {
          show: false,
          data: numList,
        },
      ],
      yAxis: [
        {
          // 第一个 Y 轴，放置在左边（默认在坐标）
          min: 0,
          name: '字节数',
          axisLabel: {
            formatter(value: number) {
              return bytesToSize(value);
            },
          },
          splitLine: {
            // show: false,
          },
        },
        {
          // 第二个坐标轴，放置在右边
          min: 0,
          minInterval: 1, // 不显示小数
          name: '数据包数',
          splitLine: {
            show: false,
          },
        },
      ],
      tooltip: {
        formatter(params: any) {
          if (!Array.isArray(params)) {
            return '';
          }
          const pontName = params[0].name;
          let label = `<b>${pontName}</b><br/>`;
          params.forEach((point, index) => {
            label += point.marker;
            label += `<b>${point.seriesName}：</b>`;
            if (index === 0) {
              label += bytesToSize(point.value);
            } else {
              label += `${numeral(point.value).format('0,0')}`;
            }
            label += '<br/>';
          });
          return label;
        },
      },
      legend: {
        show: false,
      },
      series: [
        {
          type: 'line',
          smooth: true,
          name: '字节数',
          data: byteList,
        },
        {
          type: 'line',
          name: '数据包数',
          smooth: true,
          yAxisIndex: 1,
          data: frameList,
        },
      ],
    };

    return <ReactECharts option={option} opts={{ height: 100 }} />;
  }
}

const IntervalAnalyse = memo(
  ({
    intervals,
    intervalTime,
    startTime,
    endTime,
    loading,
    onTimeChange = () => {},
  }: IntervalAnalyseProps) => {
    // 滑动条
    const [sliderWidth, setSliderWidth] = useState(0);
    const chartRef = useRef<HTMLDivElement>(null);

    const maxPointTime = (intervals.last * intervalTime) / 1000;

    useEffect(() => {
      const updateNumList = [];
      const updateFrameList = [];
      const updateByteList = [];

      if (Array.isArray(intervals.intervals)) {
        intervals.intervals.forEach((item) => {
          updateNumList.push(item[0]);
          updateFrameList.push(item[1]);
          updateByteList.push(item[2]);
        });
      }
    }, [intervals]);

    const computeSlider = _.throttle(() => {
      if (chartRef.current) {
        const chartTotalWidth = chartRef.current.offsetWidth;
        const width = chartTotalWidth - CHART_MARGIN_LEFT - CHART_MARGIN_RIGHT;

        setSliderWidth(width);
      }
    }, 200);

    useEffect(() => {
      if (!chartRef.current) {
        return;
      }
      computeSlider();
      window.addEventListener('resize', computeSlider, false);
    }, [intervals]);

    useEffect(
      () => () => {
        window.removeEventListener('resize', computeSlider, false);
      },
      [],
    );

    const tiggerTimeChagne = _.debounce((value) => {
      onTimeChange(value);
    }, 200);

    // 防抖一下
    const onSliderChange = (value: any) => {
      if (!Array.isArray(value)) {
        return;
      }
      const [start, end] = value;
      if (end - start < 1) {
        return;
      }

      if (start === 0 && end === maxPointTime) {
        tiggerTimeChagne([]);
      } else {
        tiggerTimeChagne(value);
      }
    };

    //intervals.intervals
    const lineData: any = [];
    if (Array.isArray(intervals.intervals)) {
      intervals.intervals.forEach((item) => {
        // numList.push(item[0]);
        // frameList.push(item[1]);
        lineData.push(item[2]);
      });
    }
    const config = {
      autoFit: false,
      data: lineData,
      height: 30,
      width: 200,
      tooltip: false,
      smooth: true,
    };

    return (
      <div style={{ width: 206, marginRight: 4 }}>
        {loading ? (
          <Spin />
        ) : (
          <Fragment>
            <div
              style={{
                float: 'left',
                marginLeft: 3,
                border: '1px solid rgb(1,1,11,0.1)',
                borderRadius: 5,
              }}
            >
              <TinyLine {...config} />
            </div>
            {intervals.intervals && intervals.intervals.length > 2 && (
              <DisplaySlider
                range
                min={0}
                max={maxPointTime || 0}
                step={1} // 步长
                value={[startTime || 0, endTime || maxPointTime || 0]}
                onChange={onSliderChange}
                style={{
                  width: 200 - 3,
                  margin: '0px 6px',
                }}
              />
            )}
          </Fragment>
        )}
      </div>
    );
  },
);

export default IntervalAnalyse;
