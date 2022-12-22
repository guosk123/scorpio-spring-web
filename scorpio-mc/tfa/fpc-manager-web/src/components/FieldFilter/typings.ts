/**
 * @warning: 枚举类型的无法的在 d.ts 中导出
 * @see: https://stackoverflow.com/questions/38553097/how-to-import-an-enum
 */

/**
 * 字段的类型
 * @description 注意是做 ClickHouse 字段查询方法转换
 */
export enum EFieldType {
  'IP' = 'IP',
  'IPV4' = 'IPv4',
  'IPV6' = 'IPv6',
  'ARRAY' = 'Array',
  'ARRAY<IPv4>' = 'Array<IPv4>',
  'ARRAY<IPv6>' = 'Array<IPv6>',
  'Map' = 'Map',
}
/**
 * 操作数（字段值）类型
 */
export enum EFieldOperandType {
  /**
   * IP类型
   * @description IP类型不可用在转sql中
   */
  'IP' = 'IP',
  /** 单独IP 无cidr */
  'SINGLE_IP' = 'single_ip',
  /** 单独IPv4 无cidr */
  'SINGLE_IPV4' = 'single_ipv4',
  /** 单独IPv6 无cidr */
  'SINGLE_IPV6' = 'single_ipv6',
  /** 值是IPv4 */
  'IPV4' = 'IPv4',
  /** 值是IPv6 */
  'IPV6' = 'IPv6',
  /** 值是端口 */
  'PORT' = 'port',
  /** 值是数字 */
  'NUMBER' = 'number',
  /** 值是字符串 */
  'STRING' = 'string',
  /** 值是枚举值 */
  'ENUM' = 'enum',
  /** 值是BPF语句*/
  'BPF' = 'bpf',
}
/**
 * 过滤的基本的等式
 */
export type IFilter = {
  id?: string;
  /**
   * 字段值
   */
  field: string;
  /**
   * 字段的名字
   */
  fieldText?: string;
  type?: EFieldType;
  operator: EFilterOperatorTypes;
  operatorText?: string;
  /**
   * 操作数的值
   * 增加了新的操作符，所以值可能为空了
   */
  operand?: string | number;
  /**
   * 操作数的名字
   * 比如IP地址组的名字、应用名字等
   */
  operandText?: string;
  /** Filter是否可用 */
  disabled?: boolean;
};
/**
 * 过滤组
 */
export type IFilterGroup = {
  id?: string;
  operator: EFilterGroupOperatorTypes;
  group: (IFilterGroup | IFilter)[];
  /** Filter组是否可用 */
  disabled?: boolean;
};

export type IFilterCondition = (IFilterGroup | IFilter)[];

export enum EFilterGroupOperatorTypes {
  'AND' = 'AND',
  'OR' = 'OR',
  // 'NOT' = 'NOT',
}

/**
 * 操作符类型
 */
export enum EFilterOperatorTypes {
  EQ = '=',
  NEQ = '!=',
  GT = '>',
  EGT = '>=',
  LT = '<',
  ELT = '<=',
  LIKE = 'like',
  // IN = ' IN ',
  /**
   * 结果不为空
   */
  EXISTS = 'exists',
  /**
   * 结果为空
   */
  NOT_EXISTS = 'not_exists',
  /** 不为空字符串 */
  NOT_EMPTY_STRING = "!= ''",
  /** 为空字符串 */
  EMPTY_STRING = "= ''",
  MATCH = 'match',
}

export type IEnumObj = Record<string, string>;

export interface List {
  label: string;
  value: string;
}

/**
 * 表格里面的枚举值
 */
export interface IEnumValue {
  value: string | number;
  text: string;
}

export const DEFAULT_FIELD_DIRECTION_CONFIG = {
  appendPos: 'suffix',
  srcAppend: 'initiator',
  destAppend: 'responder',
};

/**
 * 过滤条件中的字段
 */
export interface IField {
  title: string;
  dataIndex: string;
  /**
   * 字段的类型
   */
  type?: EFieldType;
  // Map类型需要的子字段
  subFields?: IField[];
  /**
   * 操作数的类型
   */
  operandType?: EFieldOperandType;
  enumValue?: IEnumValue[];
  /** 当前过滤条件是否可用 */
  disabled?: boolean;

  operators?: EFilterOperatorTypes[];

  /** 操作数类型为数字时，增加数字范围约束 */
  ranges?: [number, number];
  /** 是否是区分 源/目的 的字段， */
  directionConfig?:
    | {
        appendPos: 'suffix' | 'prefix';
        srcAppend: 'initiator' | 'src';
        destAppend: 'responder' | 'dest';
      }
    | true;

  validator?: (value: string) => boolean | Promise<string>
}

export interface ISelectOptionWithLabel {
  label: string;
  value: string;
}

/**
 * 搜索历史
 */
export interface ISearchHistory {
  /**
   * 根据过滤条件计算出来的 hash 值
   */
  id: string;
  /**
   * 搜索历史对外显示的文字
   */
  name: string;
  /**
   * 过滤条件
   */
  filter: IFilterCondition;
}
