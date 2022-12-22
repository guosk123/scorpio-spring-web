import { CloseSquareOutlined } from '@ant-design/icons';
import { Button, Card, Input, message, Popconfirm, Select } from 'antd';
import _ from 'lodash';
import type { ReactNode } from 'react';
import React, { Fragment, useEffect, useState } from 'react';
import type {
  FilterGroupOperatorTypes,
  FilterOperatorTypes,
  IAlertRule,
  IFilter,
  IFilterCondition,
  IFilterGroup,
} from '../../../typings';
import styles from './index.less';
import { enumObj2List } from '../RuleForm';

const InputGroup = Input.Group;
const { Option } = Select;

export const GROUP_OPERATOR_AND = 'and';
export const GROUP_OPERATOR_OR = 'or';
/**
 * 组合的逻辑关系
 */
export const groupOperatorEnum = {
  [GROUP_OPERATOR_AND]: '全部匹配（AND）',
  [GROUP_OPERATOR_OR]: '任意匹配（OR）',
};

export const RULE_OPERATOR_EQUAL = 'equal';
export const RULE_OPERATOR_NOT_EQUAL = 'not_equal';
/**
 * 基本的等式的逻辑关系
 */
export const ruleOperatorEnum = {
  [RULE_OPERATOR_EQUAL]: '满足（=）',
  [RULE_OPERATOR_NOT_EQUAL]: '不满足（!=）',
};

interface IComposeConditionProps {
  condition?: IFilterCondition;
  /**
   * 所有的子告警规则列表
   */
  alertSetings?: IAlertRule[];
  /**
   * 子告警最多数量限制
   */
  maxAlertCount?: number;
  extra?: string | ReactNode;
  loading?: boolean;
  readonly?: boolean;
  onChange?: (condition: IFilterCondition) => void;
}

const ComposeCondition: React.FC<IComposeConditionProps> = ({
  condition = {
    operator: GROUP_OPERATOR_AND,
    group: [],
  } as IFilterCondition,
  alertSetings = [],
  maxAlertCount = 8,
  loading = false,
  readonly = false,
  onChange,
}) => {
  // 组合条件
  const [filterCondition, setFilterCondition] = useState<IFilterCondition>(condition);
  // 子告警个数
  const [alertCount, setAlertCount] = useState<number>(0);

  /**
   * 深度查找组合规则里的值
   * @param dataSource 原始数据
   * @param deepStr 深度
   */
  const deepFind = function (deepStr: string): {
    target: any;
    parent: (IFilterGroup | IFilter)[];
    dataSource: IFilterCondition;
    lastDeepKey?: string | number;
  } {
    const dataSource = _.cloneDeep(filterCondition);

    if (!deepStr) {
      return { target: dataSource, dataSource, parent: [] };
    }
    const deepArr = deepStr.split('.').filter((item) => item !== '');
    let parent = null;
    let target = null;
    let lastDeepKey = '';
    for (let index = 0; index < deepArr.length; index += 1) {
      const parentKey = index > 0 ? deepArr[index - 1] : null;
      const key = deepArr[index];
      if (parentKey) {
        parent = (parent || dataSource)[parentKey];
      }
      target = (parent || dataSource)[key];
      lastDeepKey = key;
      if (!target) {
        break;
      }
    }
    return { parent, target, lastDeepKey, dataSource };
  };

  useEffect(() => {
    // 计算子告警数量
    const { alertTotalCount, validateStatus } = checkCondition();
    setAlertCount(alertTotalCount);

    if (onChange) {
      onChange(validateStatus ? filterCondition : ({} as IFilterCondition));
    }
  }, [filterCondition]);

  /**
   * 检查子告警的数量
   */
  const checkCondition = () => {
    const { group } = filterCondition;
    let alertTotalCount = 0;
    let validateStatus = true;
    const countAlert = (item: IFilter | IFilterGroup) => {
      // 检查是否设置了操作符
      if (!item.operator) {
        validateStatus = false;
      }
      if (item.hasOwnProperty('alertRef')) {
        // 存在子告警，计数+1
        alertTotalCount += 1;
        // 检查下是否设置了子告警
        const { alertRef } = item as IFilter;
        if (!alertRef) {
          validateStatus = false;
        }
      } else if (item.hasOwnProperty('group')) {
        const { group: itemGroup } = item as IFilterGroup;
        if (Array.isArray(itemGroup)) {
          itemGroup.forEach((el) => {
            countAlert(el);
          });
        }
      }
    };
    for (let i = 0; i < group.length; i += 1) {
      const item = group[i];
      countAlert(item);
    }

    return { alertTotalCount, validateStatus };
  };

  /**
   * 新增组合
   */
  const handleAddGroup = (deep: string) => {
    let {
      target,
      // eslint-disable-next-line prefer-const
      dataSource,
    }: { target: (IFilterGroup | IFilter)[]; dataSource: IFilterCondition } = deepFind(deep);
    if (!Array.isArray(target)) {
      target = [];
    }

    target.push({
      operator: GROUP_OPERATOR_AND,
      group: [],
    });
    setFilterCondition(dataSource);
  };

  /**
   * 更新组合的关联逻辑关系
   */
  const handleGroupOperatorChange = (operator: FilterGroupOperatorTypes, deep: string) => {
    const { target, dataSource }: { target: IFilterGroup; dataSource: IFilterCondition } =
      deepFind(deep);
    target.operator = operator;
    setFilterCondition(dataSource);
  };

  /**
   * 删除组合
   */
  const handleRemoveGroup = (deep: string) => {
    if (!deep) {
      // 清空重置
      setFilterCondition({
        operator: GROUP_OPERATOR_AND,
        group: [],
      });
    } else {
      const { parent, lastDeepKey, dataSource } = deepFind(deep);
      if (!lastDeepKey) {
        return;
      }
      parent.splice(+lastDeepKey, 1);
      setFilterCondition(dataSource);
    }
  };

  /**
   * 新增子告警
   */
  const handleAddRule = (deep: string) => {
    if (alertCount >= maxAlertCount) {
      message.warning(`最告警最多可支持${maxAlertCount}个`);
      return;
    }

    let {
      target,
      // eslint-disable-next-line prefer-const
      dataSource,
    }: { target: (IFilterGroup | IFilter)[]; dataSource: IFilterCondition } = deepFind(deep);
    if (!Array.isArray(target)) {
      target = [];
    }
    target.push({
      operator: RULE_OPERATOR_EQUAL,
      alertRef: '',
    });
    setFilterCondition(dataSource);
  };

  /**
   * 更新选中的子告警ID
   */
  const handleRuleChange = (alertId: string, deep: string) => {
    const { target, dataSource }: { target: IFilter; dataSource: IFilterCondition } =
      deepFind(deep);
    target.alertRef = alertId;
    setFilterCondition(dataSource);
  };

  /**
   * 更新子告警的逻辑关系
   */
  const handleRuleOperatorChange = (operator: FilterOperatorTypes, deep: string) => {
    const { target, dataSource }: { target: IFilter; dataSource: IFilterCondition } =
      deepFind(deep);
    target.operator = operator;
    setFilterCondition(dataSource);
  };

  /**
   * 移除子告警
   * @param index
   */
  const handleRemoveRule = (deep: string) => {
    const { parent, lastDeepKey, dataSource } = deepFind(deep);

    if (!lastDeepKey) {
      return;
    }
    parent.splice(+lastDeepKey, 1);
    setFilterCondition(dataSource);
  };

  const selectProps = {
    disabled: readonly,
    showArrow: !readonly,
  };

  /**
   * 子告警
   */
  const renderRule = ({ operator, alertRef }: IFilter, deep: string) => (
    <InputGroup
      compact
      style={{ marginBottom: 10 }}
      key={`${operator}_${alertRef}_${deep}`}
      data-deep={deep}
    >
      {/* 具体的告警 */}
      <Select<any>
        value={alertRef || undefined}
        style={{ width: 200 }}
        onChange={(value) => handleRuleChange(value, deep)}
        placeholder="请选择告警"
        notFoundContent="请先配置阈值告警"
        loading={loading}
        {...selectProps}
      >
        {alertSetings.map((item) => (
          <Option key={item.id} value={item.id}>
            {item.name}
          </Option>
        ))}
      </Select>
      {/* 等于不等于 */}
      <Select
        value={operator}
        style={{ width: 140 }}
        {...selectProps}
        onChange={(value: FilterOperatorTypes) => handleRuleOperatorChange(value, deep)}
      >
        {enumObj2List(ruleOperatorEnum).map((item) => (
          <Option key={item.value} value={item.value}>
            {item.label}
          </Option>
        ))}
      </Select>
      {!readonly && (
        <Popconfirm title="确认删除子告警吗？" onConfirm={() => handleRemoveRule(deep)}>
          <CloseSquareOutlined
            style={{ marginLeft: 10, height: 32, lineHeight: '32px', fontSize: 16 }}
          />
        </Popconfirm>
      )}
    </InputGroup>
  );

  /**
   * 告警组
   */
  const renderGroup = ({ operator, group }: IFilterCondition, deep: string) => {
    return (
      <Card
        size="small"
        title={
          <Select<any>
            style={{ width: 200 }}
            bordered={false}
            value={operator}
            onChange={(value) => handleGroupOperatorChange(value, deep)}
            placeholder="选择组合的逻辑关联关系"
            {...selectProps}
          >
            {enumObj2List(groupOperatorEnum).map((item) => (
              <Option key={item.value} value={item.value}>
                {item.label}
              </Option>
            ))}
          </Select>
        }
        style={{ marginBottom: 10 }}
        extra={
          readonly ? null : (
            <Popconfirm title="确认删除告警组合吗？" onConfirm={() => handleRemoveGroup(deep)}>
              <CloseSquareOutlined style={{ fontSize: 16 }} />
            </Popconfirm>
          )
        }
        data-deep={deep}
        className={`${styles.card} ${readonly ? styles.readonly : null}`}
      >
        {Array.isArray(group) &&
          group.map((groupItem, index: number) => {
            return (
              <div key={`${deep}.group.${index}`}>
                {/* 子告警 */}
                {groupItem.hasOwnProperty('alertRef') &&
                  renderRule(groupItem as IFilter, `${deep}.group.${index}`)}
                {/* 告警组 */}
                {groupItem.hasOwnProperty('group') &&
                  renderGroup(groupItem as IFilterGroup, `${deep}.group.${index}`)}
              </div>
            );
          })}
        {!readonly && (
          <Fragment>
            <Button
              disabled={alertCount >= maxAlertCount}
              size="small"
              type="primary"
              onClick={() => handleAddRule(`${deep}.group`)}
            >
              新增子告警
            </Button>
            <Button
              disabled={alertCount >= maxAlertCount}
              size="small"
              type="link"
              onClick={() => handleAddGroup(`${deep}.group`)}
              style={{ marginLeft: 10 }}
            >
              新增告警组合
            </Button>
          </Fragment>
        )}
      </Card>
    );
  };

  return renderGroup(filterCondition, '');
};

export default ComposeCondition;
