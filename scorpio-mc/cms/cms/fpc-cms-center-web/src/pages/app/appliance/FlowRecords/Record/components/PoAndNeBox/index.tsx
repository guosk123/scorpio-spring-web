import { Tag } from 'antd';
import React, { Fragment } from 'react';

export interface PN {
  po: string;
  ne: string;
}

export interface PNProps {
  pn: PN;
  showOnTable?: boolean;
}

export default function PoAndNeBox(props: PNProps) {
  const { pn } = props;
  const { po, ne } = pn;
  const style: React.CSSProperties = { minWidth: 50 };
  // style = { minWidth: 50, maxWidth: 50, overflow: 'hidden', textOverflow: 'ellipsis' };
  return (
    <Fragment>
      {po && (
        <Tag color="red" style={style}>
          {po || '+v'}
        </Tag>
      )}
      {ne && (
        <Tag color="cyan" style={style}>
          {ne || '-v'}
        </Tag>
      )}
    </Fragment>
  );
}
