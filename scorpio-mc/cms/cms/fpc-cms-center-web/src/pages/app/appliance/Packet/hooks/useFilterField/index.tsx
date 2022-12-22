import type { IEnumValue } from '@/common/typings';
import type { EFieldType } from '@/components/FieldFilter/typings';
import { EFieldOperandType } from '@/components/FieldFilter/typings';
import type { EModelAlias } from '@/pages/app/analysis/components/fieldsManager';
import {
  EFieldEnumValueSource,
  getEnumValueFromModelNext,
} from '@/pages/app/analysis/components/fieldsManager';
import { validateBpfStr } from '@/utils/utils';
import type { IFieldList } from '../useFieldList/useFieldList';
import useFieldList from '../useFieldList/useFieldList';

interface IFilterField extends IFieldList {}

export default function useFilterField(props: IFilterField) {
  const { networkSelections } = props;

  const fieldList = useFieldList({ networkSelections: networkSelections });

  return fieldList
    .filter((field) => field.searchable)
    .map((field) => {
      const { dataIndex, name, filterOperandType, filterFieldType, enumSource, enumValue } = field;
      const isEnum = filterOperandType === EFieldOperandType.ENUM;
      const enumValueList: IEnumValue[] = [];
      if (isEnum) {
        if (
          enumSource === EFieldEnumValueSource.LOCAL ||
          enumSource === EFieldEnumValueSource.VARIABlES
        ) {
          enumValueList.push(...(enumValue as IEnumValue[]));
        } else {
          const modelData = getEnumValueFromModelNext(enumValue as EModelAlias);
          if (modelData !== null) {
            enumValueList.push(...modelData.list);
          }
        }
      }

      return {
        title: name,
        dataIndex,
        operandType: filterOperandType as EFieldOperandType,
        type: filterFieldType as EFieldType,
        ...(isEnum
          ? {
              enumValue: enumValueList,
            }
          : {}),
        //添加一个bpf过滤条件
        ...(dataIndex === 'bpf'
          ? { validator: validateBpfStr as (value: string) => Promise<any> }
          : {}),
      };
    });
}
