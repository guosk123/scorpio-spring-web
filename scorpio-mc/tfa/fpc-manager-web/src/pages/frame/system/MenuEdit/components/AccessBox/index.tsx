import type { ReactNode } from 'react';
import React from 'react';

interface Props {
  children: ReactNode;
  access: boolean;
  style?: React.CSSProperties | undefined;
}

export default function AccessBox(props: Props) {
  const { children, access, style } = props;
  return <div style={access ? style : { display: 'none' }}>{children}</div>;
}
