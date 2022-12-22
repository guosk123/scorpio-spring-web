import { Tooltip } from 'antd';
import type { CSSProperties } from 'react';


interface Props{
  style?: CSSProperties;
  children: React.ReactNode
}
export default function EllipsisCom(props: Props) {
  const { children, style } = props;
  return (
    <Tooltip title={children}>
      <div
        style={{
          overflow: 'hidden',
          whiteSpace: 'nowrap',
          textOverflow: 'ellipsis',
          ...style,
        }}
      >
        {children}
      </div>
    </Tooltip>
  );
}
