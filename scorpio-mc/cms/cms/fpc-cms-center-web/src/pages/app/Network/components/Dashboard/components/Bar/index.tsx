import { CHART_COLORS } from '@/components/ReactECharts';
import EChartsMessage from '@/components/Message';
import { bytesToSize } from '@/utils/utils';
import numeral from 'numeral';
import type { CSSProperties, FC } from 'react';
import { EFormatterType } from '../fieldsManager';
import { v1 as uuidv1 } from 'uuid';
import styles from './index.less';
import type { ButtonProps } from 'antd';
import { Button } from 'antd';

interface IBarChartWrap {
  data: {
    label: string;
    value: number;
  }[];
  height?: number;
  formatterType?: EFormatterType;
  loading?: boolean;
  onClick?: ButtonProps['onClick'];
}
const Bar: FC<IBarChartWrap> = ({
  data,
  formatterType = EFormatterType.COUNT,
  height,
  loading = false,
  onClick,
}) => {
  if (loading) {
    return <EChartsMessage height={height} message="loading" />;
  }

  if (data.length === 0) {
    return <EChartsMessage height={height} message="暂无数据" />;
  }

  // 最大的值
  let maxValue = 0;
  // 计算标签的最大宽度
  let maxLengthLabel: string = '';
  for (let index = 0; index < data.length; index += 1) {
    const element = data[index];
    if (element.label.length > maxLengthLabel.length) {
      maxLengthLabel = element.label;
    }
    if (element.value > maxValue) {
      maxValue = element.value;
    }
  }

  return (
    <div className={styles.bar} style={{ height: height || 'auto' }}>
      {data.map((item, index) => {
        // 柱子的颜色
        const pillarColor = CHART_COLORS[index];
        // 计算柱子的宽度
        let pillarWidth: number | string = maxValue ? (item.value / maxValue) * 100 : 0;
        if (pillarWidth === 0) {
          pillarWidth = '5px';
        } else if (pillarWidth <= 2) {
          pillarWidth = '10px';
        }

        pillarWidth = typeof pillarWidth === 'number' ? `${pillarWidth}%` : pillarWidth;

        let value: any = item.value || 0;
        if (formatterType === EFormatterType.BYTE) {
          value = bytesToSize(value);
        } else {
          value = numeral(value).format('0,0');
        }
        const valueTextWidth = value.toString().length * 9 + 24;
        // 比较空区域是否可以放下数值
        const valueStyles: CSSProperties = {
          width: valueTextWidth,
          right: 4,
        };

        return (
          <div className={styles.row} key={uuidv1()}>
            <div className={styles.content}>
              <span className={styles.label}>
                {onClick ? (
                  <Button type="link" size="small" onClick={onClick} style={{ color: 'black' }}>
                    {item.label}
                  </Button>
                ) : (
                  <Button type="text" size="small" style={{ color: 'black' }}>
                    {item.label}
                  </Button>
                )}
              </span>
              <span
                className={styles.pillar}
                style={{ background: pillarColor, width: pillarWidth }}
              />
              <span className={styles.value} style={{ ...valueStyles }}>
                {value}
              </span>
            </div>
          </div>
        );
      })}
    </div>
  );
};

export default Bar;
