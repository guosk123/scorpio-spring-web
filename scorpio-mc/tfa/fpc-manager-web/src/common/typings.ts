import type { EFieldOperandType, EFieldType, IEnumValue } from '@/components/FieldFilter/typings';
import type { TableColumnProps } from 'antd';

/** 分页封装器 */
export interface IPageFactory<T> {
  /** 当前页数 */
  number: number;
  /** 每页记录数 */
  size: number;
  /** 总页数 */
  totalPages: number;
  /** 总条数 */
  totalElements: number;
  /** 内容 */
  content: T[];
}

/** ajax 封装好的返回值 */
export interface IAjaxResponseFactory<T> {
  status?: number;
  success: boolean;
  result: T;
}

export interface IProTableData<T> {
  success: boolean;
  data: T;
  page: number;
  total: number;
}

export interface IPageParms {
  page?: number;
  pageSize?: number;
  sortProperty?: string;
  sortDirection?: 'ASC' | 'DESC';
}

export interface ITimeParams {
  startTime: string;
  endTime: string;
  interval: number;
}

// 支持配置过滤的列定义
export type ITableColumnProps<T> = TableColumnProps<T> & {
  searchable?: boolean;
  fieldType?: EFieldType;
  operandType?: EFieldOperandType;
  // 用于支持哪些仅支持过滤，但是不显示的字段
  show?: boolean;
  title: string;
  dataIndex: string;
  enumValue?: IEnumValue[];
};
