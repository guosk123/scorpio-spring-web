import { LeftOutlined, RightOutlined } from '@ant-design/icons';
import { useEffect, useState } from 'react';
import { CARD_HEIGHT } from '../../typing';
import styles from './index.less';

interface ISegmentContainerProps {
  /** 子元素 */
  children?: React.ReactNode;
  /** 自定义style */
  style?: Record<string, any>;
  /** 箭头style */
  arrowStyle?: Record<string, any>;
}

const SEGMENT_CONTAINER_ID = 'segment_container_id';

/** 分段分析容器 */
export default function SegmentContainer({
  children,
  style = {},
  arrowStyle = {},
}: ISegmentContainerProps) {
  const [showLeftArrow, setLeftArrow] = useState<boolean>(false);
  const [showRightArrow, setRightArrow] = useState<boolean>(false);

  const setArrows = (e: HTMLElement) => {
    const { scrollLeft, clientWidth, scrollWidth } = e;
    const scrollRight = scrollWidth - (clientWidth + scrollLeft);
    if (scrollLeft > 0) {
      setLeftArrow(true);
    } else {
      setLeftArrow(false);
    }
    if (scrollRight > 0) {
      setRightArrow(true);
    } else {
      setRightArrow(false);
    }
  };

  useEffect(() => {
    const c = document.getElementById(SEGMENT_CONTAINER_ID);
    const refresh = () => {
      if (c) {
        setArrows(c);
      }
    };
    setTimeout(() => {
      refresh();
    });
    window.addEventListener('resize', refresh);
    return () => {
      window.removeEventListener('resize', refresh);
    };
  }, []);

  useEffect(() => {
    const c = document.getElementById(SEGMENT_CONTAINER_ID);
    setTimeout(() => {
      if (c) {
        setArrows(c);
      }
    });
  }, [children]);

  return (
    <div style={{ position: 'relative' }}>
      <div
        className={styles['segment-conatiner']}
        style={{
          height: `${CARD_HEIGHT + 40}px`,
          marginBottom: '30px',
          ...style,
        }}
        id={SEGMENT_CONTAINER_ID}
        onScrollCapture={(e) => {
          setArrows(e.target as HTMLElement);
        }}
      >
        {children}
        {showLeftArrow ? (
          <div
            className={styles['segment-conatiner--left-arrow']}
            style={{
              top: `${(CARD_HEIGHT + 40) / 2 - 60}px`,
              ...arrowStyle,
            }}
            onClick={() => {
              const c = document.getElementById(SEGMENT_CONTAINER_ID);
              if (c) {
                c.scrollLeft -= c?.scrollWidth / 5;
              }
            }}
          >
            <LeftOutlined />
          </div>
        ) : (
          ''
        )}
        {showRightArrow ? (
          <div
            className={styles['segment-conatiner--right-arrow']}
            style={{
              top: `${(CARD_HEIGHT + 40) / 2 - 60}px`,
              ...arrowStyle,
            }}
            onClick={() => {
              const c = document.getElementById(SEGMENT_CONTAINER_ID);
              if (c) {
                c.scrollLeft += c?.scrollWidth / 5;
              }
            }}
          >
            <RightOutlined />
          </div>
        ) : (
          ''
        )}
      </div>
    </div>
  );
}
