import { Card, List } from 'antd';
import { useMemo } from 'react';
import type { TTheme } from 'umi';
import type { SegmentFormattedData, ISegmentItemFormattedType } from '../../typing';
import { CARD_HEIGHT } from '../../typing';
import { getSegmentBackgroundColor, getSegmentBorderColor, getSegmentFontColor } from '../../utils';
import { v4 as uuidv4 } from 'uuid';

interface ISegmentAnalysistProps {
  /** 不传id时 onClick不生效 */
  id?: string;
  /** 点击分段回调函数 不传则没有点击效果 */
  onClick?: (id: string, item: ISegmentItemFormattedType) => void;
  /** 数据源 */
  dataSource: SegmentFormattedData;
  /** 主题 */
  theme?: TTheme;
  /** 自定义style */
  style?: Record<string, any>;
}

/** 分段分析内容 */
export default function SegmentAnalysis({
  theme = 'light',
  dataSource,
  onClick,
  id,
  style = {},
}: ISegmentAnalysistProps) {
  /** 格式化后的数据 */
  const formattedData = useMemo(() => {
    return Object.keys(dataSource).map((key) => {
      return {
        ...dataSource[key],
        index: key,
      };
    });
  }, [dataSource]);

  /** 分段高度 */
  const segmentHeight = useMemo(() => {
    if (formattedData?.length === 0) {
      return `${CARD_HEIGHT}px`;
    }
    return `${(CARD_HEIGHT - formattedData.length * 5) / formattedData.length}px`;
  }, [formattedData.length]);

  /** 分段字体大小 */
  const segmentFontSize = useMemo(() => {
    if (formattedData?.length === 0) {
      return `12px`;
    }
    return `${1.6 * (12 - formattedData?.length)}px`;
  }, [formattedData?.length]);

  /** 分段字粗 */
  const segmentFontWidth = useMemo(() => {
    return formattedData?.length > 5 ? 'normal' : 'bold';
  }, [formattedData?.length]);

  return (
    <List
      size="small"
      style={{ width: '290px' }}
      dataSource={formattedData}
      renderItem={(item) => {
        return (
          <Card
            size="small"
            bordered
            hoverable={!!onClick}
            key={uuidv4()}
            style={{
              height: segmentHeight,
              border: getSegmentBorderColor(item.color!, theme),
              backgroundColor: getSegmentBackgroundColor(item.color!, theme),
              overflow: 'hidden',
              marginBottom: '4px',
              ...style,
            }}
            bodyStyle={{
              fontSize: segmentFontSize,
              height: '100%',
              fontWeight: segmentFontWidth,
              padding: '5px',
              display: 'flex',
              flexDirection: 'column',
              alignContent: 'space-around',
              justifyContent: 'center',
              color: getSegmentFontColor(theme),
            }}
            onClick={() => {
              if (onClick && id) {
                onClick(id, item);
              }
            }}
          >
            <div>{item.value}</div>
            <div>{item.label}</div>
          </Card>
        );
      }}
    />
  );
}
