import type { operator_ids } from './dict';

/**
 * ClickHouse 常见的字段类型
 */
export type TCHFieldType =
  // String
  | 'String'
  | 'LowCardinality(String)'
  | 'LowCardinality(Nullable(String))'

  // 时间
  // 这里是时区不一定就是 UTC
  | "DateTime64(3, 'UTC')"
  | "DateTime('UTC')"

  // 无符号
  | 'UInt8'
  | 'UInt16'
  | 'UInt32'
  | 'UInt64'
  | 'UInt128'
  | 'UInt256'
  | 'UInt16'

  // IP 地址
  | 'IPv4'
  | 'Nullable(IPv4)'
  | 'IPv6'
  | 'Nullable(IPv6)'

  // 数组
  | 'Array(LowCardinality(String))'
  | 'Array(String)'
  | 'Array(IPv4)'
  | 'Array(IPv6)';

/** 字段操作符 */
export type TFieldOperator = typeof operator_ids[number];

export type IProperty = {
  comment: string;
  name: string;
  table: string;
  type: string;
  enum?: Map<string, any>;
};

export type IDataSourcesMap = Record<string, IProperty[]>;

export interface IRuleFormProps {
  id?: string;
  embed?: boolean;
  onSubmit?: (success: boolean) => void;
  onCancel?: any;
}
