/* eslint-disable no-param-reassign */
import { bpfValid, ipV4Regex, ipV6Regex, isCidr, isObject } from '@/utils/utils';
import {
  ClearOutlined,
  CloseSquareOutlined,
  FilterOutlined,
  InfoCircleOutlined,
  PlusOutlined,
  SaveOutlined,
} from '@ant-design/icons';
import {
  Button,
  Cascader,
  Divider,
  Form,
  Input,
  InputNumber,
  Popconfirm,
  Select,
  Tag,
  Tooltip,
} from 'antd';
import type { Rule } from 'antd/lib/form';
import type { ReactNode } from 'react';
import React, { useEffect, useRef, useState } from 'react';
import { v1 as uuidv1 } from 'uuid';
import AdvancedFilter from './components/Advanced';
import type { ISearchHistoryRefReturn } from './components/History';
import SearchHistory, { FILTER_HISTORY_STORAGE_KEY } from './components/History';
import styles from './index.less';
import type {
  IEnumObj,
  IField,
  IFilter,
  IFilterCondition,
  IFilterGroup,
  ISearchHistory,
  List,
} from './typings';
import { DEFAULT_FIELD_DIRECTION_CONFIG } from './typings';
import {
  EFieldOperandType,
  EFieldType,
  EFilterGroupOperatorTypes,
  EFilterOperatorTypes,
} from './typings';
import lodash from 'lodash';
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';

const InputGroup = Input.Group;
const { Option } = Select;
const FormItem = Form.Item;

/**
 * 枚举值转描述
 * @param enumObj 枚举内容
 * @param enumValue 枚举值
 */
export const enumValue2Label = (enumObj: IEnumObj, enumValue: string) => {
  if (Object.prototype.toString.call(enumObj) !== '[object Object]') {
    return '[--]';
  }
  if (!enumObj[enumValue]) {
    return '[--]';
  }

  return enumObj[enumValue];
};

/**
 * 枚举对象转数组
 * @param enumObj
 */
export const enumObj2List = (enumObj: IEnumObj) => {
  if (Object.prototype.toString.call(enumObj) !== '[object Object]') {
    return [];
  }
  const result: List[] = [];
  Object.keys(enumObj).forEach((key) => {
    result.push({
      value: key,
      label: enumObj[key],
    });
  });

  return result;
};

/**
 * 组合的逻辑关系
 */
export const groupOperatorEnum = {
  [EFilterGroupOperatorTypes.AND]: '全部匹配（AND）',
  [EFilterGroupOperatorTypes.OR]: '任意匹配（OR）',
  // [EFilterGroupOperatorTypes.NOT]: '不匹配（NOT）',
};

const eqOperator = {
  [EFilterOperatorTypes.EQ]: '=',
};
const neqOperator = {
  [EFilterOperatorTypes.NEQ]: '!=',
};
const likeOperator = {
  [EFilterOperatorTypes.LIKE]: 'Like',
};
const matchOperator = {
  [EFilterOperatorTypes.MATCH]: 'Match',
};
const existOperator = {
  [EFilterOperatorTypes.NOT_EMPTY_STRING]: "!= ''",
  [EFilterOperatorTypes.EMPTY_STRING]: "= ''",
};
export const existsOperator = {
  // [EFilterOperatorTypes.EXISTS]: 'exists',
  // [EFilterOperatorTypes.NOT_EXISTS]: 'does not exist ',
  [EFilterOperatorTypes.EXISTS]: '存在',
  [EFilterOperatorTypes.NOT_EXISTS]: '不存在',
};

/**
 * 判断操作符是否是存在、不存在
 * @param operator 操作符
 */
export const isExistsOperator = (operator: EFilterOperatorTypes) => {
  return (
    operator === EFilterOperatorTypes.EXISTS ||
    operator === EFilterOperatorTypes.NOT_EXISTS ||
    operator === EFilterOperatorTypes.NOT_EMPTY_STRING ||
    operator === EFilterOperatorTypes.EMPTY_STRING
  );
};

/**
 * 数字类型可以用的运算
 */
const numberFieldOperatorEnum = {
  ...eqOperator,
  ...neqOperator,
  [EFilterOperatorTypes.GT]: '>',
  [EFilterOperatorTypes.EGT]: '>=',
  [EFilterOperatorTypes.LT]: '<',
  [EFilterOperatorTypes.ELT]: '<=',
};

/**
 * 基本的等式的逻辑关系
 */
export const stringFieldOperatorEnum = {
  ...eqOperator,
  ...neqOperator,
  ...likeOperator,
  ...matchOperator,
};

interface IFilterConditionProps {
  /**
   * 传递进来原始的过滤条件
   */
  condition: IFilterCondition;

  /**
   * 可以进行过滤的字段
   */
  fields: IField[];
  /**
   * 左侧操作区域
   */
  extra?: string | ReactNode;

  /**
   * 是否开启保存历史查询条件
   */
  openHistorySave?: boolean;
  /**
   * Local Storage 保存搜索历史的Key值
   */
  historyStorageKey: string;

  /**
   * 是否显示为简单的过滤器，屏蔽高级弹出框的组合关系
   * @description 默认为 false
   */
  simple?: boolean;

  /**
   * 是否简化操作符，只保留【等于】操作符
   * @description 默认为 false
   */
  simpleOperator?: boolean;

  /** 过滤条件变化 */
  onChange?: (condition: IFilterCondition) => void;
  /** 清空当前的过滤条件 */
  onClear?: () => void;
  /** 关闭标签的过滤条件 */
  onClose?: (item: IFilter | IFilterGroup) => void;
  /** 隐藏保存过滤 默认 false */
  hideSave?: boolean;
}

/**
 * 过滤条件自定义格式化
 */
export interface IFilterCustomFormatter {
  // eslint-disable-next-line no-unused-vars
  (filter: IFilter): string;
}

// 抽成函数方便逻辑服用
const getDsl = (__field: IField, __filter: IFilter) => {
  let __opernd = __filter.operand;
  const { operator: __operator } = __filter;
  let result = '';
  if (
    (!__field.operandType || __field.operandType === EFieldOperandType.STRING) &&
    typeof __opernd === 'string'
  ) {
    __opernd = (__opernd || '').replace(/'/g, "\\'").replace(/"/g, '\\"');
  }

  if (__field?.type) {
    result += `${__filter.field}<${__field.type}> ${__operator}`;
  } else {
    result += `${__filter.field} ${__operator}`;
  }
  if (!isExistsOperator(__operator)) {
    if (__field?.operandType === EFieldOperandType.ENUM) {
      result += typeof __opernd === 'number' ? ` ${__opernd}` : ` "${__opernd}"`;
    } else {
      result +=
        __field?.operandType === EFieldOperandType.NUMBER ? ` ${__opernd}` : ` "${__opernd}"`;
    }
  }
  return result;
};

/**
 * 获取单个过滤条件的内容
 * @param filter 过滤条件
 * @param dsl 是否返回 dsl
 * @param fields 过滤字段集合
 * @param customFormatter 自定义格式化方法
 */
export const getFilterContent = (
  filter: IFilter,
  dsl: boolean = false,
  fields: IField[] = [],
  customFormatter?: IFilterCustomFormatter,
) => {
  // console.log(filter, fields);
  if (customFormatter) {
    return customFormatter(filter);
  }
  const { operator } = filter;
  let r = '';

  const field = fields!.find((_field) => {
    let filterField = filter.field;
    if (filter.type === EFieldType.Map) {
      filterField = filter.field.split('.')[0];
    }
    return _field.dataIndex === filterField;
  });
  if (!field) {
    return '';
  }
  // 返回dsl
  if (dsl) {
    // 如果该字段存在：源/目的配置，则需要特殊处理
    const { directionConfig } = field;
    if (directionConfig) {
      let finnalFieldDirectionConfig = DEFAULT_FIELD_DIRECTION_CONFIG;
      if (directionConfig !== true) {
        finnalFieldDirectionConfig = directionConfig;
      }
      const { appendPos, srcAppend, destAppend } = finnalFieldDirectionConfig;
      const finnalFilter: IFilterGroup = {
        operator: EFilterGroupOperatorTypes.OR,
        group: [
          {
            field:
              appendPos === 'suffix'
                ? `${filter.field}_${srcAppend}`
                : `${srcAppend}_${filter.field}`,
            operand: filter.operand,
            operator: filter.operator,
          },
          {
            field:
              appendPos === 'suffix'
                ? `${filter.field}_${destAppend}`
                : `${destAppend}_${filter.field}`,
            operand: filter.operand,
            operator: filter.operator,
          },
        ],
      };

      r += finnalFilter.group
        .map((f) => {
          return getDsl(field, f as IFilter);
        })
        .join(' OR ');
    } else {
      r += getDsl(field, filter);
    }

    // 字符串类型，把里面的单引号、双引号进行转义
  } else {
    // 返回标签字符串

    let operandText: string | undefined;
    let fieldTitle: string | undefined = field?.title;

    if (field.type === EFieldType.Map) {
      fieldTitle += `.${filter.field.split('.')[1]}`;
    }

    if (field?.operandType === EFieldOperandType.ENUM) {
      if (field.type !== EFieldType.ARRAY) {
        operandText = field.enumValue?.find(
          (item) => String(item.value) === String(filter.operand),
        )?.text;
      } else {
        const operandValues = String(filter.operand)?.split(',');
        operandText = field.enumValue
          ?.filter((item) => operandValues.includes(String(item.value)))
          .map((i) => i.text)
          .join(', ');
      }
    }

    r += `${fieldTitle || filter.field}`;
    r += ` ${filter.operatorText || filter.operator}`;
    if (!isExistsOperator(operator)) {
      r += ` ${operandText || filter.operand}`;
    }
  }
  return r;
};
/**
 * 获取过滤组的内容
 * @param filterGroup 过滤条件组合
 * @param dsl true- 返回 dsl,false- 返回标签
 * @param fields 过滤字段集合
 * @param customFormatter 自定义格式化方法
 */
export const getFilterGroupContent = (
  filterGroup: IFilterGroup,
  dsl: boolean = false,
  fields: IField[] = [],
  customFormatter?: IFilterCustomFormatter,
) => {
  const { operator, group } = filterGroup;
  const operatorUpperCase = (operator || EFilterGroupOperatorTypes.AND).toLocaleUpperCase();
  const operatorAnd = EFilterGroupOperatorTypes.AND.toLocaleUpperCase();

  let groupText = '';
  const appendBrackets = group?.length > 1 && operatorUpperCase === operatorAnd;

  if (group?.length === 0) {
    return '';
  }

  // and 添加括号
  if (appendBrackets) {
    groupText += '(';
  }

  group?.forEach((groupItem, index) => {
    if (groupItem.hasOwnProperty('operand')) {
      groupText += ` ${getFilterContent(groupItem as IFilter, dsl, fields, customFormatter)} `;
    } else {
      groupText += ` ( ${getFilterGroupContent(
        groupItem as IFilterGroup,
        dsl,
        fields,
        customFormatter,
      )} ) `;
    }

    // 拼接逻辑关系
    if (index !== group?.length - 1) {
      groupText += ` ${operator} `;
    }
  });

  // and 添加括号
  if (appendBrackets) {
    groupText += ')';
  }

  return groupText;
};

/**
 * 过滤条件转SPL
 */
export const filterCondition2Spl = (
  filterCondition: IFilterCondition,
  fields: IField[],
  customFormatter?: IFilterCustomFormatter,
) => {
  const sqls: string[] = [];
  filterCondition.forEach((filter) => {
    // 如果不是filterGroup
    let r = '';
    if (filter.hasOwnProperty('operand')) {
      r = getFilterContent(filter as IFilter, true, fields, customFormatter);
    } else {
      r = getFilterGroupContent(filter as IFilterGroup, true, fields, customFormatter);
    }
    if (r) {
      sqls.push(`(${r})`);
    }
  });

  return sqls.join(' AND ');
};

/**
 * 遍历过滤条件
 */
export const filterTraversal = (
  filterCondition: IFilterCondition,
  callback: (filter: IFilter) => IFilter,
) => {
  const result: IFilterCondition = [];
  filterCondition.forEach((filter) => {
    if (filter.hasOwnProperty('operand')) {
      result.push(callback(filter as IFilter));
    } else {
      result.push({
        operator: (filter as IFilterGroup)?.operator,
        group: filterTraversal((filter as IFilterGroup)?.group || [], callback),
      });
    }
  });
  return result;
};

/**
 *
 * @param operandType 操作数类型
 * @param simple 是否简化为只保留 等于
 */
export const generateOperators = (
  operandType?: EFieldOperandType,
  fieldType?: EFieldType,
  simple?: boolean,
) => {
  let operatorEnum = {};

  if (simple) {
    operatorEnum = { ...eqOperator, ...neqOperator };
  } else if (operandType === EFieldOperandType.NUMBER || operandType === EFieldOperandType.PORT) {
    // 如果字段的类型是数字数组
    if (fieldType === EFieldType.ARRAY) {
      operatorEnum = { ...eqOperator };
    } else {
      operatorEnum = numberFieldOperatorEnum;
    }
  } else if (
    fieldType === EFieldType.ARRAY ||
    fieldType === EFieldType['ARRAY<IPv4>'] ||
    fieldType === EFieldType['ARRAY<IPv6>']
  ) {
    // 数组只支持等于，不再支持搜索
    operatorEnum = { ...eqOperator };
  } else if (
    operandType === EFieldOperandType.IP ||
    operandType === EFieldOperandType.IPV4 ||
    operandType === EFieldOperandType.IPV6
  ) {
    operatorEnum = { ...eqOperator, ...neqOperator };
  } else if (operandType === EFieldOperandType.ENUM) {
    operatorEnum = { ...eqOperator, ...neqOperator };
  } else {
    operatorEnum = { ...stringFieldOperatorEnum, ...existOperator };
  }

  // TODO: 根据字段类型，判断是否需要填充存在不存在的操作符
  operatorEnum = {
    ...operatorEnum,
    // ...existsOperator,
  };

  return enumObj2List(operatorEnum);
};

/**
 * 根据字段不同，动态渲染操作符的选择框
 * @param fieldId 字段
 * @param fields 字段集合
 * @param simple 是否简化为只保留 等于
 */
export const renderOperatorContent = (fieldId: string, fields: IField[], simple?: boolean) => {
  const { type, operandType, operators } =
    fields.find((field) => field.dataIndex === fieldId?.split(',')[0]) || {};
  let operatorList = [];

  if (operators) {
    operatorList = operators.map((operator) => {
      return {
        value: operator,
        label: EFilterOperatorTypes[operator],
      };
    });
  } else {
    if (type === EFieldType.ARRAY && operandType === EFieldOperandType.NUMBER) {
      operatorList = enumObj2List({ ...eqOperator });
    } else if (
      (type === EFieldType.ARRAY ||
        type === EFieldType['ARRAY<IPv4>'] ||
        type === EFieldType['ARRAY<IPv6>']) &&
      !simple
    ) {
      operatorList = enumObj2List({ ...eqOperator });
    } else {
      operatorList = generateOperators(operandType, type, simple);
    }
  }

  return operatorList.map((item) => (
    <Option key={item.value} value={item.value}>
      {item.label}
    </Option>
  ));
};

/**
 * 根据字段不同，动态渲染字段值的输入框
 * @param fieldId 当前选中的字段
 * @param fields 所有字段的列表
 * @param operator 当前选中的操作符
 * @param formFieldName form表单的name值
 * @param initialValue 初始值
 */
export const renderOperandContent = (
  fieldId: string,
  fields: IField[],
  operator: EFilterOperatorTypes,
  formFieldName: string = 'operand',
  initialValue?: string | number,
) => {
  const {
    dataIndex,
    operandType,
    enumValue = [],
    ranges,
    validator,
  } = fields.find((field) => field.dataIndex === fieldId.split('.')[0]) || {};

  if (
    isExistsOperator(operator) ||
    operator === EFilterOperatorTypes.NOT_EMPTY_STRING ||
    operator === EFilterOperatorTypes.EMPTY_STRING
  ) {
    return (
      <Form.Item
        className={styles.operandWrap}
        name={formFieldName}
        initialValue={{
          value: undefined,
        }}
      >
        <Input style={{ display: 'none' }} />
      </Form.Item>
    );
  }

  let rules: Rule[] = [{ required: true, message: '字段值不能为空' }];
  let operandDom = <Input className={styles.sourceInput} />;
  // 初始值
  let newInitValue: any = initialValue;
  // 数字类型
  if (operandType === EFieldOperandType.NUMBER) {
    if (ranges) {
      operandDom = (
        <InputNumber className={styles.sourceInput} min={ranges[0]} max={ranges[1]} precision={0} />
      );
    } else {
      operandDom = <InputNumber className={styles.sourceInput} min={0} precision={0} />;
    }
  }
  // 端口
  if (operandType === EFieldOperandType.PORT) {
    operandDom = <InputNumber className={styles.sourceInput} min={0} max={65535} precision={0} />;
  }
  /** 无cidr的ip */
  if (operandType === EFieldOperandType.SINGLE_IP) {
    rules = [
      {
        validator: async (rule, value) => {
          if (!value) {
            throw new Error('请输入正确的IPv4或IPv6地址');
          }
          if (!ipV4Regex.test(value) && !ipV6Regex.test(value)) {
            throw new Error('请输入正确的IPv4或IPv6地址');
          }
        },
      },
    ];
    operandDom = <Input className={styles.sourceInput} />;
  }
  /** 无cidr的ipv4 */
  if (operandType === EFieldOperandType.SINGLE_IPV4) {
    rules = [
      {
        validator: async (rule, value) => {
          if (!value) {
            throw new Error('请输入IPv4地址');
          }
          if (!ipV4Regex.test(value)) {
            throw new Error('请输入正确的IPv4地址');
          }
        },
      },
    ];
    operandDom = <Input className={styles.sourceInput} />;
  }
  /** 无cidr的ipv6 */
  if (operandType === EFieldOperandType.SINGLE_IPV6) {
    rules = [
      {
        validator: async (rule, value) => {
          if (!value) {
            throw new Error('请输入IPv6地址');
          }
          if (!ipV6Regex.test(value)) {
            throw new Error('请输入正确的IPv6地址');
          }
        },
      },
    ];
    operandDom = <Input className={styles.sourceInput} />;
  }
  // IPv6
  if (operandType === EFieldOperandType.IP) {
    rules = [
      {
        validator: async (rule, value) => {
          if (!value) {
            throw new Error('请输入正确的IPv4或IPv6地址');
          }
          if (
            !ipV4Regex.test(value) &&
            !isCidr(value, 'IPv4') &&
            !ipV6Regex.test(value) &&
            !isCidr(value, 'IPv6')
          ) {
            throw new Error('请输入正确的IPv4或IPv6地址');
          }
        },
      },
    ];
    operandDom = <Input className={styles.sourceInput} />;
  }
  // IPv4
  if (operandType === EFieldOperandType.IPV4) {
    rules = [
      {
        validator: async (rule, value) => {
          if (!value) {
            throw new Error('请输入IPv4地址');
          }
          if (!ipV4Regex.test(value) && !isCidr(value, 'IPv4')) {
            throw new Error('请输入正确的IPv4地址');
          }
        },
      },
    ];
    operandDom = <Input className={styles.sourceInput} />;
  }
  // IPv6
  if (operandType === EFieldOperandType.IPV6) {
    rules = [
      {
        validator: async (rule, value) => {
          if (!value) {
            throw new Error('请输入IPv6地址');
          }
          if (!ipV6Regex.test(value) && !isCidr(value, 'IPv6')) {
            throw new Error('请输入正确的IPv6地址');
          }
        },
      },
    ];
    operandDom = <Input className={styles.sourceInput} />;
  }
  if (operandType === EFieldOperandType.ENUM) {
    operandDom = (
      <Select
        className={styles.sourceInput}
        showSearch
        labelInValue
        filterOption={(input: any, option: any) =>
          option.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
        }
      >
        {enumValue.map((item) => (
          <Select.Option key={item.value} value={String(item.value)}>
            {item.text}
          </Select.Option>
        ))}
      </Select>
    );
    rules = [
      {
        validator: async (rule, value) => {
          if (value === undefined) {
            return Promise.reject('字段值不能为空');
          } else if (value.value === undefined) {
            return Promise.reject('字段值不能为空');
          }
          return Promise.resolve();
        },
      },
    ];

    newInitValue = {
      value:
        initialValue !== undefined && initialValue !== null ? String(initialValue) : initialValue,
    };
  }

  // if (operandType === EFieldOperandType.BPF) {
  //   const debouncedBpfValid = lodash.debounce(bpfValid, 300);
  //   operandDom = (
  //     <Input
  //       onPressEnter={(e) => e.preventDefault()}
  //       placeholder="请输入BPF语句"
  //       prefix={
  //         <Tooltip title="支持标准的BPF语法">
  //           <InfoCircleOutlined />
  //         </Tooltip>
  //       }
  //       className={styles.sourceInput}
  //     />
  //   );
  //   rules = [{ validator: debouncedBpfValid }];
  // }

  const props = {} as any;
  if (newInitValue !== '' && newInitValue !== undefined) {
    props.initialValue = newInitValue;
  }

  return (
    <Form.Item
      className={styles.operandWrap}
      name={formFieldName}
      rules={[
        ...rules,
        {
          validator: async (rule, value) => {
            const res = validator?.(value);
            if (typeof res === 'boolean') {
              if (!res) {
                return Promise.reject('校验失败，清重新填写!');
              }
              return Promise.resolve();
            }
            return res;
          },
        },
      ]}
      normalize={(value) => {
        // 去除字符串前后的空格
        // bpf不需要去除前后的空格
        if (typeof value === 'string' && dataIndex !== 'bpf') {
          return value.trim();
        }
        return value;
      }}
      {...props}
    >
      {operandDom}
    </Form.Item>
  );
};

export const formatFilter = (filter: IFilter, fields: IField[]) => {
  if (filter.field && filter.fieldText && filter.type && filter.operandText) {
    return filter;
  }

  if (!filter.field || !filter.operator) {
    return undefined as unknown as IFilter;
  }
  // 查找字段名称
  const fieldResult = fields.find((item) => {
    const { dataIndex, type } = item;
    let fieldKey = dataIndex;
    if (type) {
      fieldKey = fieldKey.replace(`<${type}>`, '');
    }

    if (type === EFieldType.Map) {
      return fieldKey === filter.field.split('.')[0] || dataIndex === filter.field.split('.')[0];
    }

    return fieldKey === filter.field || dataIndex === filter.field;
  });
  if (!fieldResult) {
    return undefined as unknown as IFilter;
  }

  // TODO: clickHouse数据库需求，暂时不需要
  // const { operator } = filter;
  // let operatorText: string = operator;
  // if (isExistsOperator(operator)) {
  //   operatorText = existsOperator[operator];
  // }

  let operandText = filter.operand;
  // 如果不是存在、不存在，查找操作数的值
  if (
    !isExistsOperator(filter.operator) &&
    fieldResult.operandType === EFieldOperandType.ENUM &&
    Array.isArray(fieldResult.enumValue)
  ) {
    const enumValue = fieldResult.enumValue?.find((item) => item.value === filter.operand);
    operandText = enumValue ? enumValue.text : filter.operand;
  }

  return {
    ...filter,
    field: filter.type === EFieldType.Map ? filter.field : fieldResult.dataIndex,
    fieldText: filter.type === EFieldType.Map ? filter.fieldText : fieldResult.title,
    type: fieldResult.type,
    operandText,
    // TODO: 暂时屏蔽
    // operatorText,
  } as IFilter;
};

const FilterCondition: React.FC<IFilterConditionProps> = ({
  condition = [] as (IFilterGroup | IFilter)[],
  fields = [],
  onChange,
  onClear,
  onClose,
  extra,
  // 默认开启保存历史记录
  openHistorySave = true,
  historyStorageKey = FILTER_HISTORY_STORAGE_KEY,
  simple = false,
  simpleOperator = false,
  hideSave = false,
}) => {
  const [form] = Form.useForm();
  // 过滤条件
  // condition 和 filterCondition 其实是一样的
  const [filterCondition, setFilterCondition] = useState<IFilterCondition>(condition);
  // 高级过滤弹出框
  const [modalVisible, setModalVisible] = useState<boolean>(false);
  // 高级过滤弹出框里面的内容
  const [currentFilterIndex, setCurrentFilterIndex] = useState<number | undefined>(undefined);
  const [advancedFilter, setAdvancedFilter] = useState<IFilterGroup | IFilter | undefined>(
    undefined,
  );

  const searchHistoryRef = useRef<ISearchHistoryRefReturn>(null);

  // condition改变了，会执行setFilterCondition(condition)，filterCondition发生改变
  useEffect(() => {
    setFilterCondition(condition);
  }, [condition, fields]);

  // 如果onChange发生了变化，那么用filterCondition重置了condition
  useEffect(() => {
    if (onChange) {
      onChange(filterCondition);
    }
    // @warning: 这里不要监听 onChange
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filterCondition]);

  /**
   * 打开/关闭 高级过滤弹出框
   */
  const handleToogleAdvancedFilter = (visible: boolean) => {
    setModalVisible(visible);
  };

  /**
   * 高级过滤添加完成
   * @param advancedFilter
   */
  // eslint-disable-next-line @typescript-eslint/no-shadow
  const handleAdvancedFilterOk = (advancedFilter: IFilterGroup) => {
    // 如果是编辑，就更新原来的过滤
    if (currentFilterIndex !== undefined) {
      let newFilter: (IFilterGroup | IFilter)[] = [...filterCondition];
      // 如果被删除了，直接删除原有的查询条件
      if (advancedFilter.group.length === 0) {
        newFilter = newFilter.filter((item, index) => index !== currentFilterIndex);
      } else {
        newFilter = newFilter.map((item, index) => {
          let tmp: any = advancedFilter;
          if (index !== currentFilterIndex) {
            tmp = item;
          } else if (
            advancedFilter.group.length === 1 &&
            advancedFilter.group[0]?.hasOwnProperty('field')
          ) {
            // eslint-disable-next-line prefer-destructuring
            tmp = advancedFilter.group[0];
          }
          return tmp;
        });
      }
      setFilterCondition(newFilter);
      setCurrentFilterIndex(undefined);
    } else {
      // 否则就追加
      setFilterCondition((prev) => {
        return [...prev, advancedFilter];
      });
    }
    // 清空
    setAdvancedFilter(undefined);
  };

  const handleAdvancedFilterCancel = () => {
    handleToogleAdvancedFilter(false);
    setAdvancedFilter(undefined);
  };

  const handleTagClick = (index: number) => {
    setCurrentFilterIndex(index);
    setAdvancedFilter(filterCondition[index]);
    setModalVisible(true);
  };

  // ====== 高级过滤 E=======

  /**
   * 校验过滤条件
   */
  const handleCheck = async () => {
    try {
      const values = await form.validateFields();
      // console.log(values);
      const filter = {
        id: uuidv1(),
        field: values.field.join('.'),
        fieldText: values.field.label,
        operator: values.operator,
        ...(values.field.length > 1 ? { type: EFieldType.Map } : {}),
      } as IFilter;

      if (
        values.operand === undefined ||
        typeof values.operand === 'string' ||
        typeof values.operand === 'number'
      ) {
        filter.operand = values.operand;
        filter.operandText = values.operand;
      } else {
        filter.operand = values.operand.value;
        filter.operandText = values.operand.label;
      }
      setFilterCondition((prev) => {
        return [...prev, filter];
      });
      // 清空原有字段
      form.resetFields();
      form.setFieldsValue({ operand: undefined, field: undefined });
    } catch (errorInfo) {
      // console.log('Failed:', errorInfo);
    }
  };

  /**
   * 删除某个过滤条件
   * @param filterIndex 索引序号
   */
  const removeFilterItem = (filterIndex: number) => {
    const newFilter = filterCondition.filter((item, index) => {
      if (filterIndex === index) {
        if (onClose) {
          onClose(item);
        }
        return false;
      }
      return true;
    });
    setFilterCondition(newFilter);
  };

  /**
   * 清空当前所有的过滤条件
   */
  const removeAllFilter = () => {
    if (onClear) {
      onClear();
      return;
    }
    setFilterCondition([]);
  };

  /**
   * 字段变化
   */
  const handleFieldChange = () => {
    // 重置其他的2个输入框
    form.setFieldsValue({
      operator: EFilterOperatorTypes.EQ,
      operand: undefined,
    });
  };

  /**
   * 操作符变化
   */
  const handleOperatorChange = (operator: EFilterOperatorTypes) => {
    // 存在不存在操作符时，把操作数清空
    if (isExistsOperator(operator)) {
      form.setFieldsValue({
        operand: undefined,
      });
    }
  };

  // =====搜索历史 S=====
  const handleHistoryClick = (history: ISearchHistory) => {
    setFilterCondition(history.filter);
  };

  // =====搜索历史 E=====
  const handleSaveHistory = () => {
    searchHistoryRef?.current?.save(condition);
  };

  if (fields.length === 0) {
    return <span>字段列表不能为空</span>;
  }

  return (
    <div className={styles.filterWrap}>
      <section className={styles.filterFormWrap}>
        <Form
          form={form}
          initialValues={{
            field:
              fields.filter((item) => !item.disabled).length > 0
                ? [fields.filter((item) => !item.disabled)[0].dataIndex]
                : undefined,
            operator: EFilterOperatorTypes.EQ,
            operand: undefined,
          }}
        >
          <FormItem>
            <InputGroup compact>
              {/* 字段 */}
              <FormItem noStyle name="field">
                <Cascader
                  style={{ width: 200 }}
                  dropdownMenuColumnStyle={{ width: 200 }}
                  onChange={handleFieldChange}
                  placeholder="请选择字段"
                  displayRender={(label) => label.join('.')}
                  options={fields.map((field) => {
                    if (field.type !== EFieldType.Map) {
                      return {
                        disabled: field.disabled,
                        label: field.title,
                        value: field.dataIndex,
                      };
                    }
                    return {
                      disabled: field.disabled,
                      label: field.title,
                      value: field.dataIndex,
                      isLeaf: false,
                      children: field.subFields?.map((subField) => {
                        return {
                          disabled: subField.disabled,
                          label: subField.title,
                          value: subField.dataIndex,
                        };
                      }),
                    };
                  })}
                  showSearch={{
                    filter: (input, options) =>
                      options.some(
                        (option) =>
                          (option.label as string).toLowerCase().indexOf(input.toLowerCase()) > -1,
                      ),
                  }}
                />
              </FormItem>
              {/* 操作符 */}
              <Form.Item
                noStyle
                shouldUpdate={(prevValues, currentValues) =>
                  prevValues.field !== currentValues.field
                }
              >
                {({ getFieldValue }) => {
                  const fieldId = (getFieldValue('field') as string[])?.join('.') || '';
                  return (
                    <FormItem noStyle name="operator" key={fieldId}>
                      <Select style={{ width: 90 }} onChange={handleOperatorChange}>
                        {renderOperatorContent(fieldId, fields, simpleOperator)}
                      </Select>
                    </FormItem>
                  );
                }}
              </Form.Item>
              {/* 字段值 */}
              <Form.Item noStyle dependencies={['field', 'operator']} rules={[]}>
                {({ getFieldValue }) => {
                  const fieldId = (getFieldValue('field') as string[])?.join('.') || '';
                  // console.log(fieldId, 'fieldId');
                  const operator: EFilterOperatorTypes = getFieldValue('operator');
                  return (
                    // 添加 Key，以保证输入值 Dom 不被 Diff 复用
                    <div key={fieldId}>
                      {renderOperandContent(fieldId, fields, operator, undefined)}
                    </div>
                  );
                }}
              </Form.Item>
              <Form.Item noStyle shouldUpdate>
                {({ getFieldValue }) => {
                  const operator = getFieldValue('operator');
                  const operand = getFieldValue('operand');
                  let filterBtnDisabled = false;
                  // 如果是存在或者不存在
                  if (
                    operator === EFilterOperatorTypes.EXISTS ||
                    operator === EFilterOperatorTypes.NOT_EXISTS ||
                    operator === EFilterOperatorTypes.NOT_EMPTY_STRING ||
                    operator === EFilterOperatorTypes.EMPTY_STRING
                  ) {
                    filterBtnDisabled = false;
                  } else if (operand === undefined) {
                    filterBtnDisabled = true;
                  } else if (isObject(operand) && operand.value === undefined) {
                    filterBtnDisabled = true;
                  }

                  return (
                    <FormItem noStyle>
                      <Button
                        icon={<PlusOutlined />}
                        type="primary"
                        onClick={handleCheck}
                        disabled={filterBtnDisabled}
                      >
                        添加过滤
                      </Button>
                      {!simple && (
                        <Button
                          className="btn-info"
                          icon={<FilterOutlined />}
                          onClick={() => handleToogleAdvancedFilter(true)}
                        >
                          高级过滤
                        </Button>
                      )}
                    </FormItem>
                  );
                }}
              </Form.Item>
            </InputGroup>
          </FormItem>
        </Form>
        <div className={styles.action}>{extra}</div>
      </section>

      {/* 当前的过滤条件展示 */}
      {filterCondition.length > 0 && (
        <section className={styles.filterTagWarp}>
          {!hideSave && openHistorySave && (
            <Tooltip title="保存成搜索历史">
              <Tag
                icon={<SaveOutlined />}
                color="#108ee9"
                className={styles.tag}
                onClick={() => handleSaveHistory()}
              >
                保存过滤
              </Tag>
            </Tooltip>
          )}
          <Popconfirm title="确认清空当前的过滤条件吗？" onConfirm={removeAllFilter}>
            <Tag
              icon={<ClearOutlined />}
              color="#f50"
              className={styles.tag}
              style={{ marginRight: 0 }}
            >
              清空过滤
            </Tag>
          </Popconfirm>
          <Divider type="vertical" />
          {filterCondition.map((filter, index) => {
            const content = filter.hasOwnProperty('operand')
              ? getFilterContent(filter as IFilter, false, fields)
              : getFilterGroupContent(filter as IFilterGroup, false, fields);

            if (!content) {
              return;
            }
            return (
              <Tag
                color={filter.disabled ? 'default' : 'blue'}
                style={{ whiteSpace: 'normal', cursor: filter.disabled ? 'default' : 'pointer' }}
                closable
                className={styles.tag}
                key={`${filter.operator}_${uuidv1()}`}
                onClick={() => {
                  if (filter.disabled) {
                    return '';
                  }
                  return handleTagClick(index);
                }}
                closeIcon={
                  <span className={styles.removeTagBtn} onClick={() => removeFilterItem(index)}>
                    <CloseSquareOutlined />
                  </span>
                }
                onClose={(e) => e.preventDefault()}
              >
                {content}
              </Tag>
            );
          })}
        </section>
      )}

      {/* 搜索历史 */}
      {openHistorySave && (
        <>
          <SearchHistory
            ref={searchHistoryRef}
            onClick={handleHistoryClick}
            fields={fields}
            localStorageKey={historyStorageKey}
          />
        </>
      )}
      {modalVisible && (
        <AdvancedFilter
          simple={simple}
          simpleOperator={simpleOperator}
          visible={modalVisible}
          fields={fields}
          condition={advancedFilter!}
          onFinish={handleAdvancedFilterOk}
          onCancel={handleAdvancedFilterCancel}
        />
      )}
    </div>
  );
};

export default FilterCondition;
