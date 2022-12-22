import React from 'react';

interface Props {
  onClick?: any;
  children?: any;
  selectedKey?: string;
}
export default function HorizontalMenu(props: Props) {
  const { onClick = () => {}, children, selectedKey } = props;

  return (
    <ul style={{ display: 'flex', margin: 0, padding: 0 }}>
      {/* {children} */}
      {children.map((ele: any) => {
        return React.cloneElement(ele, { itemKey: ele.props.itemKey, selectedKey, onClick });
      })}
    </ul>
  );
}
