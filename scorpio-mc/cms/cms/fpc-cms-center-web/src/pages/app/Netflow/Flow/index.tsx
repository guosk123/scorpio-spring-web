import type {
  EFieldOperandType,
  EFieldType,
  IEnumValue,
  IField,
  IFilterCondition,
} from '@/components/FieldFilter/typings';
import type { EModelAlias } from '@/pages/app/analysis/components/fieldsManager';
import type { IFlowLayout } from '../layouts/FlowLayout';
import {
  EFieldEnumValueSource,
  EFormatterType,
  getEnumValueFromModelNext,
} from '@/pages/app/analysis/components/fieldsManager';
import { fieldsMapping } from '../typing';
import { snakeCase } from '@/utils/utils';
import React, { useState } from 'react';
import { connect } from 'umi';
import FlowLayout from '../layouts/FlowLayout';
import FieldFilter from '@/components/FieldFilter';

interface IFilterContextType {
  filterCondition: IFilterCondition;
  filterField?: IField[];
  addConditionToFilter?: (condition: IFilterCondition) => void;
}

/** 过滤条件 */
// 所有维度的过滤条件
const allSelectValue = [
  'ip_address',
  'src_ip',
  'dest_ip',
  'port',
  'src_port',
  'dest_port',
  'protocol',
];
// 各个维度支持的过滤条件
// Filter上下文，用于给子组件传递数据
export const FilterContext = React.createContext<IFilterContextType>({ filterCondition: [] });

/** 过滤相关函数 */
// 获得该维度下的FilterFields
export function getFilterFields() {
  const fieldsList: IField[] = [];
  allSelectValue.forEach((field: string) => {
    const { formatterType, name, filterOperandType, filterFieldType, enumSource, enumValue } =
      fieldsMapping[field];
    const isEnum = formatterType === EFormatterType.ENUM;
    const enumValueList: IEnumValue[] = [];
    if (isEnum) {
      if (enumSource === EFieldEnumValueSource.LOCAL) {
        enumValueList.push(...(enumValue as IEnumValue[]));
      } else {
        const modelData = getEnumValueFromModelNext(enumValue as EModelAlias);
        if (modelData) {
          enumValueList.push(...modelData.list);
        }
      }
    }
    fieldsList.push({
      title: name,
      dataIndex: snakeCase(field),
      operandType: filterOperandType as EFieldOperandType,
      type: filterFieldType as EFieldType,
      disabled: false,
      ...(isEnum
        ? {
            enumValue: enumValueList,
          }
        : {}),
    });
  });
  return fieldsList;
}

const FlowAnalysis: React.FC<IFlowLayout> = ({ location: { pathname }, children, ...rest }) => {
  // 该维度支持的过滤条件
  const [filterCondition, setFilterCondition] = useState<IFilterCondition>([]);

  // 增加状态
  const addConditionToFilter = (condition: IFilterCondition) => {
    setFilterCondition([...filterCondition, ...condition]);
  };

  /** FieldFilter回调函数 */
  // 过滤条件改变回调函数
  const handleFilterChange = (newFilter: IFilterCondition) => {
    setFilterCondition(newFilter);
  };

  // 清空过滤条件的回调函数
  function handleTagClear() {
    setFilterCondition([]);
  }

  return (
    <>
      <div style={{ marginBottom: 10 }}>
        <FieldFilter
          fields={getFilterFields()}
          onChange={handleFilterChange}
          onClear={handleTagClear}
          condition={filterCondition}
          historyStorageKey="cms-netflow-flow-record-filter-history"
          simple={true}
        />
      </div>

      <FilterContext.Provider
        value={{ filterCondition, addConditionToFilter, filterField: getFilterFields() }}
      >
        <FlowLayout location={location} {...rest} children={children} />
      </FilterContext.Provider>
    </>
  );
};

export default connect()(FlowAnalysis);
