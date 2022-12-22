import usePieChartLabelColor from '@/utils/hooks/usePieChartLabelColor';
import { bytesToSize, convertBandwidth } from '@/utils/utils';
import numeral from 'numeral';
import React, { useMemo } from 'react';
import { FieldValue, ThresholdConfigType } from '../../typings';
import styles from './index.less';

interface IField {
  label: string;
  value: number;
  valueType: FieldValue;
  thresholdConfig: any[];
}
const Field: React.FC<IField> = ({ label, value, valueType, thresholdConfig }) => {
  const textColor = usePieChartLabelColor();
  const displayValue = useMemo(() => {
    switch (valueType) {
      case FieldValue.BYTE:
        return bytesToSize(value);
      case FieldValue.BYTE_PS:
        return convertBandwidth(value * 8);
      case FieldValue.COUNT:
        return numeral(value).format('0,0');
      case FieldValue.TIME_DELAY:
        return `${numeral(value).format('0,0')}ms`;
      case FieldValue.RATE:
        return `${value}%`;
      default:
        return value;
    }
  }, [value, valueType]);

  const crossValueThresholdStyle = useMemo(() => {
    if (Object.prototype.toString.call(thresholdConfig) === '[object Array]') {
      const [threshold, thresholdType] = thresholdConfig;
      if (
        (ThresholdConfigType.MoreThan === thresholdType && value > threshold) ||
        (ThresholdConfigType.LessThan === thresholdType && value < threshold)
      ) {
        return { color: 'red' };
      }
    }
    return { color: textColor };
  }, [textColor, thresholdConfig, value]);

  return (
    <div className={styles.field}>
      <span title={label} className={styles.field__label}>
        {label}
      </span>
      <span className={styles.field__value} style={crossValueThresholdStyle}>
        {displayValue}
      </span>
    </div>
  );
};

export default Field;
