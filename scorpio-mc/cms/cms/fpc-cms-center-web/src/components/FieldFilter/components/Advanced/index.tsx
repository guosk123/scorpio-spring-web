/* eslint-disable prefer-const */
import { CloseSquareOutlined } from '@ant-design/icons';
import { Button, Card, Cascader, Form, Input, Modal, Popconfirm, Select } from 'antd';
import type { DefaultOptionType } from 'antd/lib/cascader';
import _ from 'lodash';
import React, { useState } from 'react';
import { v1 as uuidv1 } from 'uuid';
import {
  enumObj2List,
  groupOperatorEnum,
  isExistsOperator,
  renderOperandContent,
  renderOperatorContent,
} from '../../index';
import type { IField, IFilter, IFilterGroup } from '../../typings';
import { EFieldType, EFilterGroupOperatorTypes, EFilterOperatorTypes } from '../../typings';
import { formatFilterGroup } from '../../utils';
import styles from './index.less';

const { Option } = Select;
const InputGroup = Input.Group;

interface IAdvancedFilterProps {
  visible: boolean;
  fields: IField[];
  condition?: IFilterGroup | IFilter;
  simple?: boolean;
  simpleOperator?: boolean;
  onFinish: (filter: IFilterGroup) => void;
  onCancel: () => void;
}

const AdvancedFilter: React.FC<IAdvancedFilterProps> = ({
  visible,
  fields,
  condition,
  simpleOperator = false,
  simple = false,
  onFinish,
  onCancel,
}) => {
  const [form] = Form.useForm();
  // 判断是组合还是单个过滤
  let initCondition = {
    id: uuidv1(),
    operator: EFilterGroupOperatorTypes.AND,
    group: [],
  } as IFilterGroup;

  if (condition) {
    if (condition.hasOwnProperty('operand')) {
      initCondition.group.push(condition as IFilter);
    } else {
      initCondition = condition as IFilterGroup;
    }
  }

  const [advancedFilter, setAdvancedFilter] = useState<IFilterGroup>(() => {
    return formatFilterGroup([initCondition], fields)[0] as IFilterGroup;
  });

  // useEffect(() => {
  //   console.log(advancedFilter);
  // }, [advancedFilter]);

  const handleFinished = async () => {
    try {
      await form.validateFields();
      // console.log('Success:', advancedFilter);
      if (onFinish) {
        onFinish(advancedFilter);
        // 关闭窗口
        if (onCancel) {
          onCancel();
        }
      }
    } catch (errorInfo) {
      // console.log('Failed:', errorInfo);
    }
  };

  /**
   * 深度查找组合规则里的值
   * @param dataSource 原始数据
   * @param deepStr 深度
   */
  const deepFind = (
    deepStr: string,
  ): {
    target: any;
    parent: (IFilterGroup | IFilter)[];
    dataSource: IFilterGroup;
    lastDeepKey?: string | number;
  } => {
    const dataSource = _.cloneDeep(advancedFilter);

    if (!deepStr) {
      return { target: dataSource, dataSource, parent: [] };
    }
    const deepArr = deepStr.split('.').filter((item) => item !== '');
    let parent = null;
    let target = null;
    let lastDeepKey = '';
    // console.log(deepArr, deepStr);
    for (let index = 0; index < deepArr.length; index += 1) {
      const parentKey = index > 0 ? deepArr[index - 1] : null;
      const [no] = deepArr[index].split('_');
      if (parentKey) {
        const [parentNo] = parentKey.split('_');
        parent = (parent || dataSource)[parentNo];
      }
      target = (parent || dataSource)[no];
      lastDeepKey = no;
      if (!target) {
        break;
      }
    }

    // 这里修改 target,parent，都会到这dataSource修改
    // 个人感觉这俩设计的不是很好，违反了不可变的原则，导致后续的代码逻辑难以理解
    return { parent, target, lastDeepKey, dataSource };
  };

  /**
   * 新增组合
   */
  const handleAddGroup = (deep: string) => {
    let {
      target,
      dataSource,
    }: { target: (IFilterGroup | IFilter)[]; dataSource: IFilterGroup | IFilter } = deepFind(deep);
    if (!Array.isArray(target)) {
      target = [];
    }

    target.push({
      id: uuidv1(),
      operator: EFilterGroupOperatorTypes.AND,
      group: [],
    });
    setAdvancedFilter(dataSource);
  };

  /**
   * 更新组合的关联逻辑关系
   */
  const handleGroupOperatorChange = (operator: EFilterGroupOperatorTypes, deep: string) => {
    const { target, dataSource }: { target: IFilterGroup; dataSource: IFilterGroup } =
      deepFind(deep);
    target.operator = operator;
    setAdvancedFilter(dataSource);
  };

  /**
   * 删除组合
   */
  const handleRemoveGroup = (deep: string) => {
    if (!deep) {
      // 清空重置
      setAdvancedFilter({
        id: uuidv1(),
        operator: EFilterGroupOperatorTypes.AND,
        group: [],
      });
    } else {
      const { parent, lastDeepKey, dataSource } = deepFind(deep);
      if (!lastDeepKey) {
        return;
      }
      parent.splice(+lastDeepKey, 1);
      setAdvancedFilter(dataSource);
    }
  };

  /**
   * 新增过滤
   */
  const handleAddFilter = (deep: string) => {
    let { target, dataSource }: { target: (IFilterGroup | IFilter)[]; dataSource: IFilterGroup } =
      deepFind(deep);
    if (!Array.isArray(target)) {
      target = [];
    }

    // console.log('add filter', target, dataSource);

    const firstField = fields.length > 0 ? fields[0] : undefined;
    target.push({
      id: uuidv1(),
      field: firstField ? firstField.dataIndex : (undefined as unknown as string),
      fieldText: firstField ? firstField.title : '',
      operator: EFilterOperatorTypes.EQ,
      operand: undefined as unknown as string,
      operandText: '',
      type: firstField?.type,
    });

    form.setFieldsValue({
      [`${deep}___operand`]: undefined,
    });
    setAdvancedFilter(dataSource);
  };

  /**
   * 更新字段
   */
  const handleFilterFieldChange = (options: DefaultOptionType[], deep: string) => {
    const { target, dataSource }: { target: IFilter; dataSource: IFilterGroup } = deepFind(deep);

    const value = options.map((item) => item.value).join('.');
    const label = options.map((item) => item.label).join('.');

    const fieldType = fields.find((item) => item.dataIndex === value.split('.')[0])?.type;

    target.field = value;
    target.fieldText = label;
    target.operator = EFilterOperatorTypes.EQ;
    target.operand = undefined as unknown as any;
    target.operandText = '';
    target.type = fieldType;
    // console.log(dataSource);

    form.setFieldsValue({
      [`${deep}___field`]: options.map((item) => item.value),
      [`${deep}___operator`]: EFilterOperatorTypes.EQ,
      [`${deep}___operand`]: undefined,
    });
    setAdvancedFilter(dataSource);
  };

  /**
   * 更新逻辑关系
   */
  const handleFilterOperatorChange = (operator: EFilterOperatorTypes, deep: string) => {
    const { target, dataSource }: { target: IFilter; dataSource: IFilterGroup } = deepFind(deep);
    target.operator = operator;
    if (isExistsOperator(operator)) {
      target.operand = undefined;
    }
    setAdvancedFilter(dataSource);
  };

  /**
   * 表单更新触发
   * @param changedValues
   */
  const handleFormValuesChange = (changedValues: any) => {
    // {.group.0_operand: 30}
    // console.log(changedValues);
    const formFields = Object.keys(changedValues);
    formFields.forEach((field) => {
      const [deep, suffix] = field.split('___');
      if (suffix === 'operand') {
        const { target, dataSource }: { target: IFilter; dataSource: IFilterGroup } =
          deepFind(deep);
        const fieldValue = changedValues[field];
        if (typeof fieldValue === 'string' || typeof fieldValue === 'number') {
          target.operand = fieldValue;
          target.operandText = fieldValue as any;
        } else {
          target.operand = fieldValue.value;
          target.operandText = fieldValue.label;
        }

        setAdvancedFilter(dataSource);
      }
    });
  };

  /**
   * 移除filter
   * @param index
   */
  const handleRemoveFilter = (deep: string) => {
    const { parent, lastDeepKey, dataSource } = deepFind(deep);

    if (!lastDeepKey) {
      return;
    }
    parent.splice(+lastDeepKey, 1);
    setAdvancedFilter(dataSource);
  };

  /**
   * filter
   */
  const renderFilter = (
    { field, operator, operand }: IFilter,
    deep: string,
    showDelete: boolean = true,
  ) => {
    const key = `${deep}`;
    return (
      <InputGroup compact style={{ marginBottom: 10 }} key={key} data-deep={deep}>
        {/* 字段 */}
        <Form.Item name={`${key}___field`} initialValue={field.split('.') || []}>
          <Cascader
            style={{ width: 200 }}
            onChange={(value, option) => {
              handleFilterFieldChange(option, deep);
            }}
            placeholder="请选择字段"
            displayRender={(label) => label?.join('.')}
            options={fields.map((_field) => {
              if (_field.type !== EFieldType.Map) {
                return {
                  disabled: _field.disabled,
                  label: _field.title,
                  value: _field.dataIndex,
                };
              }
              return {
                disabled: _field.disabled,
                label: _field.title,
                value: _field.dataIndex,
                isLeaf: false,
                children: _field.subFields?.map((subField) => {
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
        </Form.Item>
        {/* 操作符 */}
        <Form.Item noStyle name={`${key}___operator`} initialValue={operator}>
          <Select
            value={operator}
            style={{ width: 80 }}
            onChange={(value: EFilterOperatorTypes) => handleFilterOperatorChange(value, deep)}
          >
            {renderOperatorContent(field, fields, simpleOperator)}
          </Select>
        </Form.Item>

        {/* 字段值 */}
        <Form.Item noStyle dependencies={[`${key}___field`, `${key}___operator`]}>
          {({ getFieldValue }) => {
            const operatorValue: EFilterOperatorTypes = getFieldValue(`${key}___operator`);
            return renderOperandContent(field, fields, operatorValue, `${key}___operand`, operand);
          }}
        </Form.Item>

        {showDelete && (
          <Popconfirm title="确认删除过滤条件吗？" onConfirm={() => handleRemoveFilter(deep)}>
            <CloseSquareOutlined
              style={{ marginLeft: 10, height: 32, lineHeight: '32px', fontSize: 16 }}
            />
          </Popconfirm>
        )}
      </InputGroup>
    );
  };

  /**
   * 告警组
   */
  const renderGroup = ({ operator, group }: IFilterGroup, deep: string) => {
    return (
      <Card
        size="small"
        title={
          !simple && (
            <Select<any>
              style={{ width: 200 }}
              bordered={false}
              value={operator}
              onChange={(value) => handleGroupOperatorChange(value, deep)}
              placeholder="选择组合的逻辑关联关系"
            >
              {enumObj2List(groupOperatorEnum).map((item) => (
                <Option key={item.value} value={item.value}>
                  {item.label}
                </Option>
              ))}
            </Select>
          )
        }
        style={{ marginBottom: 10 }}
        extra={
          !simple && (
            <Popconfirm title="确认删除过滤组合吗？" onConfirm={() => handleRemoveGroup(deep)}>
              <CloseSquareOutlined style={{ fontSize: 16 }} />
            </Popconfirm>
          )
        }
        data-deep={deep}
        className={styles.card}
      >
        {Array.isArray(group) &&
          group.map((groupItem, index: number) => {
            const { id } = groupItem;
            return (
              // eslint-disable-next-line react/no-array-index-key
              <div key={`${deep}.group.${index}_${id}`}>
                {/* 过滤条件 */}
                {groupItem.hasOwnProperty('field') &&
                  renderFilter(groupItem as IFilter, `${deep}.group.${index}_${id}`)}
                {/* 过滤组合 */}
                {groupItem.hasOwnProperty('group') &&
                  renderGroup(groupItem as IFilterGroup, `${deep}.group.${index}_${id}`)}
              </div>
            );
          })}
        {!simple && (
          <>
            <Button size="small" type="primary" onClick={() => handleAddFilter(`${deep}.group`)}>
              新增过滤条件
            </Button>
            <Button
              size="small"
              type="link"
              onClick={() => handleAddGroup(`${deep}.group`)}
              style={{ marginLeft: 10 }}
            >
              新增过滤组合
            </Button>
          </>
        )}
      </Card>
    );
  };

  return (
    <Modal
      width={800}
      title={simple ? 'Filter条件修改' : '高级过滤'}
      visible={visible}
      destroyOnClose
      keyboard={false}
      onCancel={onCancel}
      onOk={handleFinished}
    >
      <Form form={form} onValuesChange={handleFormValuesChange}>
        {renderGroup(advancedFilter, '')}
      </Form>
    </Modal>
  );
};

export default AdvancedFilter;
