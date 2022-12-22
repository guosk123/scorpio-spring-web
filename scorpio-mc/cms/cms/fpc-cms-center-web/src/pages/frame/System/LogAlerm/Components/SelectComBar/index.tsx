import { Select } from "antd";
import { useEffect, useState } from "react";
import type { optionsType } from "../../Log/typings";

const SelectComBar: React.FC<{
  state: {
    type: number;
  };
  /** Value 和 onChange 会被自动注入 */
  value?: string;
  onChange?: (key: string) => void;
  needOptions: optionsType[];
}> = (props) => {
  const { state, needOptions } = props;

  const [innerOptions, setOptions] = useState<
    {
      label: React.ReactNode;
      key: string;
    }[]
  >([]);

  useEffect(() => {
    const { type } = state || {};
    if (type === 2) {
      setOptions([{ label: '全部', key: '' }, ...needOptions]);
    } else {
      setOptions(needOptions.filter((item) => item.key === '001001'));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [JSON.stringify(state)]);

  return (
    <Select
      showSearch
      filterOption={(input, option) =>
        // @ts-ignore
        option?.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
      }
      placeholder="请选择"
      value={props.value}
      onChange={props.onChange}
    >
      {innerOptions.map((item) => (
        <Select.Option key={item.key} id={item.key} value={item.key}>
          {item.label}
        </Select.Option>
      ))}
    </Select>
  );
};

export default SelectComBar;