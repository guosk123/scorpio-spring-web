import classNames from 'classnames';
import { debounce } from 'lodash';
import ResizeObserver from 'rc-resize-observer';
import type { ReactNode } from 'react';
import { useEffect, useRef, useState } from 'react';
import styles from './index.less';

interface IAutoHeightContainerProps {
  autoHeight?: boolean;
  className?: string;
  headerRender?: ReactNode | string;
  /** 内容区域自定义样式 */
  contentStyle?: React.CSSProperties;
  onHeightChange?: (height: number) => void;
  fixHeight?: number;
}

const AutoHeightContainer: React.FC<IAutoHeightContainerProps> = ({
  headerRender,
  children,
  className,
  contentStyle,
  onHeightChange,
  autoHeight = true,
  fixHeight = 0,
}) => {
  // header容器的高度
  const [headerHeight, setHeaderHeight] = useState<number>(0);
  // 内容容器的高度
  const [contentHeight, setContentHeight] = useState<number>(200);

  const headerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    onHeightChange?.(contentHeight);
  }, [contentHeight, onHeightChange]);

  useEffect(() => {
    const func = () => {
      if (!autoHeight) {
        return;
      }
      const headerRect = headerRef?.current?.getBoundingClientRect();
      if (headerRect) {
        const newWrapHeight = window.innerHeight - headerHeight - headerRect.top - 10 - fixHeight;
        // 最后减10是外层的 margin-bottom
        setContentHeight(newWrapHeight);
      }
    };

    const updatePosition = debounce(func, 200);

    updatePosition();
    // 让resize事件触发handleResize
    window.addEventListener('resize', updatePosition);
    return () => {
      window.removeEventListener('resize', updatePosition);
    };
  }, [autoHeight, fixHeight, headerHeight]);

  if (!autoHeight) {
    return (
      <>
        {headerRender}
        {children}
      </>
    );
  }

  return (
    <div className={classNames(styles.autoHeightPageView, className)}>
      <ResizeObserver
        onResize={(size) => {
          setHeaderHeight(size.height);
        }}
      >
        <div className={styles.autoHeightPageView__header} ref={headerRef}>
          {headerRender}
        </div>
      </ResizeObserver>
      <div
        className={styles.autoHeightPageView__content}
        style={{ ...contentStyle, height: contentHeight }}
      >
        {children}
      </div>
    </div>
  );
};

export default AutoHeightContainer;
