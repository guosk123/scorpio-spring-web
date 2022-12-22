import type { ReactNode } from 'react';
import React, { useImperativeHandle, useState } from 'react';
import styles from './index.less';

interface IPosition {
  top?: number;
  left?: number;
}

interface IAnyWhereContainerProps extends IPosition {
  className?: string;
  style?: React.CSSProperties;
  children?: ReactNode;
  theme?: 'light' | 'dark';
}

export interface IAnyWhereContainerRefReturn {
  updatePosition: (pos: IPosition) => void;
  updateVisible: (visible: boolean) => void;
}

const AnyWhereContainer = React.forwardRef<IAnyWhereContainerRefReturn, IAnyWhereContainerProps>(
  (props, ref) => {
    const { top, left, children, className, style, theme } = props;

    const [position, setPosition] = useState<IPosition>({
      top: top !== undefined ? top : -999,
      left: left !== undefined ? left : -999,
    });

    const [visible, setVisible] = useState<boolean>(false);

    useImperativeHandle(ref, () => ({
      updatePosition: (pos: IPosition) => {
        return setPosition(pos);
      },
      updateVisible: (visible: boolean) => {
        return setVisible(visible);
      },
    }));

    if (!visible) {
      return null;
    }

    return (
      <>
        <div
          className={theme === 'dark' ? styles.container_dark : styles.container}
          style={{ ...style, ...position }}
        >
          {children}
        </div>
      </>
    );
  },
);

export default AnyWhereContainer;
