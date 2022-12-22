import type { ReactNode } from 'react';

interface Props {
  children?: ReactNode;
  itemKey: string;
  selectedKey?: string;
  onClick?: any;
}

export default function HorizontalMenuItem(props: Props) {
  const { children, itemKey, selectedKey, onClick } = props;
  return (
    <li
      key={itemKey}
      style={{
        cursor: 'pointer',
        margin: '6px 18px 2px',
        borderBottomStyle: 'solid',
        borderBottomColor: itemKey === selectedKey ? '#1890ff' : 'white',
        color: itemKey === selectedKey ? '#1890ff' : 'black',
        borderBottomWidth: 2,
      }}
      onClick={() => {
        onClick(itemKey);
      }}
    >
      {children}
    </li>
  );
}
