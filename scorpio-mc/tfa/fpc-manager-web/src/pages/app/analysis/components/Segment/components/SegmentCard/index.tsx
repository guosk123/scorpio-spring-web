import { Card, Col, Row } from 'antd';
import { useCallback, useMemo } from 'react';
import type { TTheme } from 'umi';
import useNetwork from '../../hooks/useNetworks';
import { CARD_WIDTH, SEGMENT_CARD_COLOR, SEGMENT_DARK_CARD_COLOR } from '../../typing';
import { getSegmentFontColor } from '../../utils';

export interface ISegmentCardProps {
  /** 填写后onFooterClick才生效 */
  id?: string;
  /** 主题 */
  theme?: TTheme;
  /** 对其方式 */
  align?: 'middle' | 'top' | 'bottom' | 'stretch';
  /** 是否加边框 */
  bordered?: boolean;
  /** 是否可点击 */
  hoverable?: boolean;
  /** 子元素 */
  children?: React.ReactNode;
  /** footer内容 */
  footer?: string;
  /** 是否转化footer的内容 */
  footerConverter?: boolean;
  /** 点击footer回调，当未传入id时，不生效 */
  onFooterClick?: (id: string) => void;
  /** 展示背景颜色 */
  showBackground?: boolean;
  /** 自定义宽度 */
  width?: string;
  /** 卡片被选中 */
  selectedId?: string;
  /** 自定义style */
  style?: Record<string, any>;
  /** 自定义bodystyle */
  bodyStyle?: Record<string, any>;
}

/** 分段分析卡片 */
export default function SegmentCard({
  id,
  onFooterClick,
  align = 'middle',
  children,
  footer,
  footerConverter = false,
  hoverable = false,
  bordered = true,
  theme = 'light',
  showBackground = true,
  width,
  selectedId = '',
  style = {},
  bodyStyle = {},
}: ISegmentCardProps) {
  /** 此页面是否被选中 */
  const isSelected = useMemo(() => {
    return selectedId && selectedId === id;
  }, [id, selectedId]);

  /** footer是否加下划线 */
  const footerDecoration = useMemo(() => {
    return isSelected ? 'underline' : '';
  }, [isSelected]);

  /** footer字体宽度 */
  const footerWeight = useMemo(() => {
    return isSelected ? 'bold' : 'normal';
  }, [isSelected]);

  /** 背景色 */
  const backgroundColor = useMemo(() => {
    if (showBackground) {
      return theme === 'dark' ? SEGMENT_DARK_CARD_COLOR : SEGMENT_CARD_COLOR;
    }
    return SEGMENT_DARK_CARD_COLOR;
  }, [showBackground, theme]);

  /** 光标悬浮 */
  const cursor = useMemo(() => {
    return hoverable ? 'pointer' : '';
  }, [hoverable]);

  const networkName = useNetwork(footer ?? '');

  /** 页脚渲染 */
  const rendeFooter = useCallback(() => {
    if (footer) {
      const showedFooter = footerConverter ? networkName : footer;
      return (
        <div
          style={{
            position: 'absolute',
            bottom: '-25px',
            left: '0px',
            width: `${CARD_WIDTH}px`,
            textDecoration: footerDecoration,
            fontWeight: footerWeight,
            color: getSegmentFontColor(theme),
          }}
        >
          {onFooterClick ? (
            <span
              style={{ cursor: 'pointer' }}
              onClick={(e) => {
                e.stopPropagation();
                if (id) {
                  onFooterClick(id);
                }
              }}
            >
              {showedFooter}
            </span>
          ) : (
            showedFooter
          )}
        </div>
      );
    }
    return '';
  }, [
    footer,
    footerConverter,
    footerDecoration,
    footerWeight,
    id,
    networkName,
    onFooterClick,
    theme,
  ]);

  return (
    <Card
      style={{
        height: `${CARD_WIDTH}px`,
        width: width || `${CARD_WIDTH}px`,
        display: 'inline-block',
        marginRight: '20px',
        position: 'relative',
        backgroundColor: backgroundColor,
        ...style,
      }}
      bodyStyle={{
        padding: '5px',
        height: '100%',
        cursor,
        overflow: 'hidden',
        ...bodyStyle,
      }}
      bordered={bordered}
      hoverable={hoverable}
    >
      <Row align={align} justify="center" style={{ height: '100%' }}>
        <Col>{children}</Col>
      </Row>
      {rendeFooter()}
    </Card>
  );
}
