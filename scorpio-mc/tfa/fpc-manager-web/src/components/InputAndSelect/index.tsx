import type { SelectProps } from 'antd';
import { Select } from 'antd';
import { useEffect, useState } from 'react';

interface Props extends Omit<SelectProps, 'value' | 'onChange'> {
  value?: string;
  onChange?: (value: string) => void;
}

const InputAndSelect = ({ value, onChange, options, ...restProps }: Props) => {
  const [data, setData] = useState<string | undefined>();
  const [newOption, setNewOption] = useState<{ label: string; value: string }>();

  useEffect(() => {
    setData(value);
  }, [value]);

  const handleSearch = (search: string) => {
    const find = options?.find((option) => option.value === search);

    if (find) {
      setData(find.value as string);
    } else {
      if (search) {
        setNewOption({ label: search, value: search });
      } else {
        setNewOption(undefined);
      }
    }
  };

  const handleChange = (newValue: string) => {
    if (onChange) {
      onChange(newValue);
    }
  };

  return (
    <Select
      {...restProps}
      options={options?.concat(newOption || [])}
      showSearch
      showArrow={false}
      value={data}
      onSearch={handleSearch}
      onChange={handleChange}
      notFoundContent={null}
    />
  );
};

export default InputAndSelect;
