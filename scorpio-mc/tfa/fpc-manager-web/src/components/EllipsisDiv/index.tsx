import { Tooltip } from 'antd';
import type { TooltipPlacement } from 'antd/lib/tooltip';
import type { CSSProperties, ReactNode } from 'react';
import { useMemo } from 'react';

interface Props {
  children?: ReactNode;
  style?: CSSProperties;
  placement?: TooltipPlacement;
}

export default function EllipsisDiv(props: Props) {
  const { children, style, placement } = props;
  const tooltipProps = useMemo(() => {
    const res: any = {};
    if (placement) {
      res.placement = placement;
    }
    return res;
  }, [placement]);
  return (
    <Tooltip {...tooltipProps} title={children}>
      <span
        style={{
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          cursor: 'pointer',
          display: 'block',
          whiteSpace: 'nowrap',
          height: 18,
          ...style,
        }}
      >
        {children}
      </span>
    </Tooltip>
  );
}
