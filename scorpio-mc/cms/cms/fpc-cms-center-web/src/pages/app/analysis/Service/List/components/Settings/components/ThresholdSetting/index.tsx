import { InputNumber, Select } from 'antd';
import { useEffect, useState } from 'react';
import type { IField } from '../../../../typings';
import {
  ThresholdConfigOptions,
  ThresholdConfigType,
  FieldValue,
  FieldValueUnitMap,
} from '../../../../typings';

interface IThresholdSettingProps {
  field: IField;
  thresholdConfig: any[];
  onChange: (thresholdConfig: any[], key: string) => void;
}
function ThresholdSetting(props: IThresholdSettingProps) {
  const { field, thresholdConfig, onChange } = props;
  const configArr: any[] = thresholdConfig;

  const [threshold, setThreshold] = useState(configArr[0]);
  const [thresholdType, setThresholdType] = useState(configArr[1]);

  const handleThreshold = (value: number) => {
    setThreshold(value);
  };
  const handleThresholdType = (value: ThresholdConfigType) => {
    setThresholdType(value);
  };

  useEffect(() => {
    if (thresholdType || threshold) {
      if (threshold === null || thresholdType === ThresholdConfigType.NotExist) {
        onChange([0, '0'], field.key);
      } else {
        onChange([threshold, thresholdType], field.key);
      }
    }
  }, [field.key, thresholdType, threshold]);

  return (
    <span>
      <>
        {` 阈值${
          FieldValue.COUNT === field.valueType ? '' : '(' + FieldValueUnitMap[field.valueType] + ')'
        }: `}
        <Select
          size="small"
          placeholder="阈值配置"
          value={thresholdType}
          onChange={handleThresholdType}
          options={ThresholdConfigOptions}
        />
        {ThresholdConfigType.NotExist !== thresholdType && (
          <InputNumber size="small" min={0} value={threshold} onChange={handleThreshold} />
        )}
      </>
    </span>
  );
}
export default ThresholdSetting;
