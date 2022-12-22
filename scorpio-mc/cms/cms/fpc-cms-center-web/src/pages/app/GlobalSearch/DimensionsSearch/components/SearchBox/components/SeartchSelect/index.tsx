import { Select } from 'antd';
import type { CSSProperties } from 'react';
interface ISearchBoxProps {
  listItems: { text: string; value: string | number }[];
  onChange?: any;
  key?: string;
  style?: CSSProperties | undefined;
}
export default function index(props: ISearchBoxProps) {
  const { onChange, listItems, style, key } = props;
  return (
    <Select
      style={style}
      showSearch
      key={key}
      onChange={(a, b: any) => {
        onChange(b?.key);
      }}
    >
      {listItems.map((item) => (
        <Select.Option key={item.value} value={item.text}>
          {item.text}
        </Select.Option>
      ))}
    </Select>
  );
}
