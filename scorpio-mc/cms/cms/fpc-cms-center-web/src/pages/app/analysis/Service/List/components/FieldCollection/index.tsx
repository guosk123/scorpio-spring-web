import type { IServiceStatData } from '@/pages/app/analysis/typings';
import { useEffect, useState } from 'react';
import Field from '../Field';
import type { IField } from '../../typings';
import { fieldList } from '../../typings';

interface IFieldCollectionProps {
  data: IServiceStatData;
  fieldIds: string[];
  fieldIdThresholdMap: Record<string, any[]>;
}
const FieldCollection: React.FC<IFieldCollectionProps> = ({
  fieldIds,
  fieldIdThresholdMap,
  data,
}) => {
  /** 默认显示的指标字段 */
  const [displayFields, setDisplayFields] = useState<IField[]>([]);

  useEffect(() => {
    // 从storage 中取临时保存的字段
    const displayList: IField[] = [];
    for (let index = 0; index < fieldIds.length; index += 1) {
      const fieldInfo = fieldList.find((el) => el.key === fieldIds[index]);
      if (fieldInfo) {
        displayList.push(fieldInfo);
      }
    }
    setDisplayFields(displayList);
  }, [fieldIds]);

  return (
    <>
      {displayFields.map((field) => (
        <Field
          key={field.key}
          label={field.label}
          value={data[field.key] || 0}
          valueType={field.valueType}
          thresholdConfig={fieldIdThresholdMap[field.key]}
        />
      ))}
    </>
  );
};

export default FieldCollection;
