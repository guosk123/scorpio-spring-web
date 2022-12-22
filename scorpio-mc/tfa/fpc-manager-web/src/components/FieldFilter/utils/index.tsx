/* eslint-disable no-param-reassign */
import type { IFilterCondition, IFilter, IFilterGroup, IField } from '../typings';
import { v1 as uuidv1 } from 'uuid';
import { formatFilter } from '..';

export const formatFilterGroup = (condition: IFilterCondition, fields: IField[]) => {
  for (let index = 0; index < condition.length; index += 1) {
    if (!condition[index].id) {
      condition[index].id = uuidv1();
    }
    const item = condition[index];
    if (item.hasOwnProperty('operand')) {
      const newFilter = formatFilter(item as IFilter, fields);
      if (!newFilter) {
        condition.splice(index, 1);
      } else {
        condition[index] = newFilter;
      }
    } else if (item.hasOwnProperty('group')) {
      if (Array.isArray((item as IFilterGroup).group)) {
        formatFilterGroup((item as IFilterGroup).group, fields);
      } else {
        condition.splice(index, 1);
      }
    } else {
      condition.splice(index, 1);
    }
  }

  return condition;
};

export const getSampleFilterText = (filter: IFilter) => {
  const filter_text = `${filter.field}${filter.operator}${filter.operand}`;
  return filter_text;
};

export const getFilterGroupText = (group: IFilterGroup) => {
  const { operator } = group;
  let group_text = '';
  group.group.forEach((item) => {
    if (item.hasOwnProperty('operand')) {
      const f = item as IFilter;
      group_text += `${f.field}${f.operator}${f.operand}${operator}`;
    } else {
      const f = item as IFilterGroup;
      group_text += getFilterGroupText(f);
    }
  });
  return group_text;
};

export const deduplicateCondition: (
  conditions: IFilterCondition,
  set: Set<string>,
) => IFilterCondition = (conditions, set) => {
  const result = conditions
    .filter((item) => item)
    .reduce((pre, cur) => {
      // 类型：IFilter
      if (cur?.hasOwnProperty('operand')) {
        const text = getSampleFilterText(cur as IFilter);
        if (set.has(text)) {
          return [...pre];
        }
        set.add(text);
        return [...pre, cur];
      }

      const text = getFilterGroupText(cur as IFilterGroup);
      if (set.has(text)) {
        return [...pre];
      }
      set.add(text);
      return [...pre, cur];
    }, [] as IFilterCondition);

  // console.log(conditions, result, set);
  return result;
};
