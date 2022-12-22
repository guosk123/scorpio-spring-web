import type { IFilter, IFilterGroup } from '@/components/FieldFilter/typings';

/** 用来判断Group有无当前维度下不支持的条件 */
export function testFilterGroup(filters: IFilter | IFilterGroup, selects: string[]) {
  /** 判断是不是过滤器组 */
  if (!isFilterGroup(filters)) {
    // 单独过滤条件，判断是否支持选择条件
    return isSupportSelected(filters as IFilter, selects);
  }

  // 组条件，递归遍历
  const { group } = filters as IFilterGroup;
  // TODO: 调整方法
  for (let index = 0; index < group.length; index += 1) {
    const item = group[index];
    if (!testFilterGroup(item, selects)) {
      return false;
    }
  }
  return true;
}

/**
 * 用来对过滤器组遍历替换
 * 先序遍历树状结构
 * @param: filters 过滤器组根节点
 * @param: callback 遍历到每个节点要做的操作
 */
export function replaceFilterGroup(
  filters: IFilter | IFilterGroup,
  callback: (filter: IFilter) => IFilter | IFilterGroup,
): IFilterGroup | IFilter {
  /** 判断是不是过滤器组 */
  if (!isFilterGroup(filters)) {
    // 单独过滤条件，判断是否支持选择条件
    return callback(filters as IFilter);
  }

  // 组条件，递归遍历
  const { group, operator } = filters as IFilterGroup;
  const filterList = [];
  // TODO: 调整方法
  for (let index = 0; index < group.length; index += 1) {
    const item = group[index];
    filterList.push(replaceFilterGroup(item, callback));
  }
  return {
    group: filterList,
    operator,
  } as IFilterGroup;
}

/** 判断是否为过滤器组 */
export function isFilterGroup(filter: IFilter | IFilterGroup): boolean {
  return 'group' in filter;
}

/** 判断过滤器是否支持过滤选择 */
export function isSupportSelected(filter: IFilter, selects: string[]): boolean {
  if (filter && filter.field) {
    return selects.includes(filter.field);
  }
  return false;
}

// 判断是否是ipv4
export function isIpv4(ip_address: string) {
  return /(((\d{1,2})|(1\d{2})|(2[0-4]\d)|(25[0-5]))\.){3}((\d{1,2})|(1\d{2})|(2[0-4]\d)|(25[0-5]))/.test(
    ip_address,
  );
}
