import type { IEnumValue, IFilter } from '@/components/FieldFilter/typings';
import { EFieldOperandType, EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import type { EModelAlias } from '@/pages/app/analysis/components/fieldsManager';
import {
  EFieldEnumValueSource,
  getEnumValueFromModelNext,
} from '@/pages/app/analysis/components/fieldsManager';
import { bytesToSize } from '@/utils/utils';
import { message, Spin, Tree } from 'antd';
import type { DataNode, TreeProps } from 'antd/lib/tree';
import { connect } from 'dva';
import _ from 'lodash';
import numeral from 'numeral';
import { Fragment, useEffect, useMemo, useState } from 'react';
import { filterField } from '../..';
import type { IPacketRefine, IPacketRefineAggregationKey } from '../../typings';
import { fieldList } from '../PacketList';
import * as style from './index.less';

export interface IPacketRefineProps {
  data: IPacketRefine;

  /** 是否可点击，追加过滤条件 */
  clickable?: boolean;
  onTreeNodeClick: (filter: IFilter[]) => void;
  loading?: boolean;
}

const PacketRefine: React.FC<IPacketRefineProps> = ({
  data: { aggregations },
  clickable = true,
  onTreeNodeClick,
  loading,
}) => {
  const refinedLabel = [
    'countryId',
    'provinceId',
    'cityId',
    'applicationId',
    'ipProtocol',
    'l7ProtocolId',
  ];

  const refinedLableMap = filterField.filter((ele) => refinedLabel.includes(ele.dataIndex));

  const treeData = useMemo<DataNode[]>(() => {
    if (aggregations.length > 0) {
      const newTree: DataNode[] = aggregations.map((row) => {
        const parentKey = `${row.label}_${row.type}`;
        const itemLength = row.items.length;

        // 取枚举值展示
        const enumValueList: IEnumValue[] = [];
        // 从返回中的 type 取出字段信息
        const fieldInfo = fieldList.find((item) => item.dataIndex === row.type);
        if (fieldInfo && fieldInfo.filterOperandType === EFieldOperandType.ENUM) {
          if (fieldInfo.enumSource === EFieldEnumValueSource.LOCAL) {
            enumValueList.push(...(fieldInfo.enumValue as IEnumValue[]));
          } else {
            const modelData = getEnumValueFromModelNext(fieldInfo.enumValue as EModelAlias);
            if (modelData) {
              enumValueList.push(...modelData.list);
            }
          }
        }

        // 二级
        const childrenList = row.items
          .filter((sub) => {
            const itemInfo = enumValueList.find((ele) => String(ele.value) === String(sub.label));
            // 父节点为应用，子节点不能转译的丢弃
            if (
              (row.type === 'applicationId' && !itemInfo) ||
              (itemInfo?.text === '未知' && row.type === 'cityId') ||
              (['countryId', 'provinceId', 'cityId'].includes(row.type) && !itemInfo)
            ) {
              return false;
            }
            return true;
          })
          .map((item) => {
            const itemInfo = enumValueList.find((ele) => String(ele.value) === String(item.label));
            return {
              ...item,
              type: row.type,
              title: `${itemInfo?.text || item.label}(${bytesToSize(item.value)})`,
              key: `${parentKey}_${item.label}`,
              parent: parentKey,
            };
          });
        // 总数不超过返回 Top 的数量时，显示 More
        if (row.total && itemLength > 0 && row.total > itemLength) {
          childrenList.push({
            title: `+ ${numeral(row.total - itemLength).format('0,0')} more`,
            key: `${parentKey}_more`,
            type: '',
            parent: parentKey,
            disabled: true,
          } as any);
        }
        return {
          title: refinedLableMap.find((ele) => ele.dataIndex === row.type)?.title || row.label,
          key: parentKey,
          children: childrenList,
        };
      });
      return newTree;
    }
    return [];
  }, [aggregations, refinedLableMap]);

  const handelTreeNodeClick: TreeProps['onSelect'] = _.debounce((selectedKeys, { node }) => {
    // 在父节点上添加过滤条件
    // if (!clickable) {
    //   message.warning('Filter条件下可追加过滤条件');
    //   return;
    // }
    // 父节点不参与
    if (node.children) {
      return;
    }
    if (!node.type) {
      return;
    }

    const keys: IPacketRefineAggregationKey = node.keys || {};
    // 新追加的过滤条件
    const appendFilterList: IFilter[] = [];
    Object.keys(keys).forEach((fieldId) => {
      const value = keys[fieldId];
      appendFilterList.push({
        field: fieldId,
        operator: EFilterOperatorTypes.EQ,
        operand: value,
      } as IFilter);
    });

    if (appendFilterList.length > 0) {
      onTreeNodeClick(appendFilterList);
    }
  }, 200);

  const [expanedKeys, setExpanedKeys] = useState(
    Array.isArray(treeData) ? treeData.map((item) => item.key) : [],
  );

  useEffect(() => {
    setExpanedKeys(
      Array.isArray(aggregations) ? aggregations.map((item) => `${item.label}_${item.type}`) : [],
    );
  }, [aggregations]);

  // if (treeData.length === 0) {
  //   return <Fragment>{loading ? <Spin /> : <p className={style.empty}>暂无数据</p>}</Fragment>;
  // }

  return (
    <Fragment>
      {loading ? (
        <Spin />
      ) : (
        <Tree
          className={style.tree}
          showLine={{ showLeafIcon: false }}
          showIcon={false}
          defaultExpandAll
          expandedKeys={expanedKeys}
          onExpand={(keys, obj) => {
            console.log('expands keys', keys, obj);
            setExpanedKeys(keys);
          }}
          treeData={treeData || []}
          // 屏蔽选中树的样式，点击后直接刷新条件请求数据了
          selectedKeys={[]}
          onSelect={handelTreeNodeClick}
        />
      )}
    </Fragment>
  );
};

export default connect()(PacketRefine);
