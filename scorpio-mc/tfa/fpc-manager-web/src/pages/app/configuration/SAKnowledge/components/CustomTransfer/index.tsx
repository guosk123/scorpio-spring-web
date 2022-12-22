/* eslint-disable consistent-return */
import { Input, Tag, Transfer, Tree } from 'antd';
import type { ChangeEvent, FC } from 'react';
import React, { useEffect, useState } from 'react';
import type { TransferItem } from 'antd/lib/transfer';
import { SearchOutlined } from '@ant-design/icons';

import styles from './index.less';

export interface ITreeData {
  key: React.Key;
  title: string;
  disabled?: boolean;
  children?: ITreeData[];
}

const isChecked = (selectedKeys: string[], eventKey: string) =>
  selectedKeys.indexOf(eventKey) !== -1;

const generateTree = (
  treeNodes: ITreeData[] = [],
  checkedKeys: React.Key[] = [],
  deep = 1,
): ITreeData[] => {
  return treeNodes.map(({ children, ...props }) => {
    let disabled = false;
    if (!children || children?.length === 0) {
      // 第一层没有子节点，直接禁用
      if (deep === 1) {
        disabled = true;
      } else {
        disabled = checkedKeys.includes(props.key);
      }
    } else {
      // 检查字节点是否全部被选中
      const childrenKeys = children!.map((item) => item.key);
      // 判断交集
      const intersect = new Set([...checkedKeys].filter((x) => childrenKeys.includes(x)));
      if (intersect.size === childrenKeys.length) {
        disabled = true;
      }
    }
    return {
      ...props,
      disabled,
      children: generateTree(children, checkedKeys, deep + 1),
    };
  });
};

const getParentKey = (key: React.Key, tree: ITreeData[]): string => {
  let parentKey: string = '';
  for (let i = 0; i < tree.length; i += 1) {
    const node = tree[i];
    if (node.children) {
      if (node.children.some((item) => item.key === key)) {
        parentKey = node.key as string;
      } else if (getParentKey(key, node.children)) {
        parentKey = getParentKey(key, node.children);
      }
    }
  }
  return parentKey;
};

interface ICustomTransferProps {
  transferData: any[];
  treeData: ITreeData[];
  titles: [string, string];
  targetKeys?: string[];
  onChange?: (selectedKeys: string[]) => void;
}

const CustomTransfer: FC<ICustomTransferProps> = ({
  transferData,
  treeData,
  titles,
  targetKeys: initTargetKeys = [],
  onChange,
}) => {
  const [targetKeys, setTargetKeys] = useState<string[]>(initTargetKeys);
  // 可搜索树
  const [treeSearchValue, setTreeSearchValue] = useState<string>('');
  const [treeExpandedKeys, setTreeExpandedKeys] = useState<React.Key[]>([]);
  const [autoExpandParent, setAutoExpandParent] = useState<boolean>(true);

  useEffect(() => {
    if (onChange) {
      // 转换成真实ID
      const realKeys =
        targetKeys.length > 0
          ? targetKeys
              .filter((item) => item.split('_').length === 2)
              .map((item) => item.split('_').pop())
              .filter((item) => item)
          : [];
      onChange(realKeys as string[]);
    }
  }, [targetKeys]);

  const treeDataList: ITreeData[] = [];
  const generateList = (data: ITreeData[]) => {
    for (let i = 0; i < data.length; i += 1) {
      const node = data[i];
      treeDataList.push(node);
      if (node.children) {
        generateList(node.children);
      }
    }
  };
  generateList(treeData);

  const handleTransferChange = (keys: string[]) => {
    setTargetKeys(keys);
  };

  const handleTreeExpand = (expandedKeys: React.Key[]) => {
    setTreeExpandedKeys(expandedKeys);
    setAutoExpandParent(false);
  };

  const handleTreeSearchChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { value } = e.target;
    const expandedKeys = treeDataList
      .map((item) => {
        if ((item.title! as string).toLocaleLowerCase().indexOf(value.toLocaleLowerCase()) > -1) {
          return getParentKey(item.key, treeData);
        }
        return '';
      })
      .filter((item, i, self) => item && self.indexOf(item) === i);

    setTreeExpandedKeys(expandedKeys);
    setTreeSearchValue(value);
    setAutoExpandParent(true);
  };

  const loop = (data: ITreeData[]): ITreeData[] => {
    return data
      .map((item) => {
        const { title, children } = item;
        // 不存在搜索关键字
        if (!treeSearchValue) {
          if (children) {
            return { title, key: item.key, children: loop(children) };
          }
          return {
            title: title as string,
            key: item.key,
          };
        }

        if (!children) {
          return null as unknown as ITreeData;
        }

        // 如果子节点不包含搜索的名字，就不再显示
        const included =
          children?.filter(
            (el) => el.title.toLocaleLowerCase().indexOf(treeSearchValue.toLocaleLowerCase()) > -1,
          ) || [];
        if (included.length > 0) {
          return {
            title,
            key: item.key,
            children:
              children?.filter(
                (el) =>
                  el.title.toLocaleLowerCase().indexOf(treeSearchValue.toLocaleLowerCase()) > -1,
              ) || [],
          };
        }

        return null as unknown as ITreeData;
      })
      .filter((el) => !!el);
  };

  return (
    <Transfer<TransferItem>
      // filterOption={this.handleFilterOption}
      dataSource={transferData}
      // onSearch={() => {}}
      titles={[
        <Tag color="#2db7f5" style={{ marginRight: 0 }}>
          {titles[0]}
        </Tag>,
        <Tag color="#87d068" style={{ marginRight: 0 }}>
          {titles[1]}
        </Tag>,
      ]}
      showSearch
      filterOption={(inputValue, option) =>
        option.title?.toLocaleLowerCase().indexOf(inputValue.toLocaleLowerCase()) !== -1
      }
      showSelectAll={false}
      listStyle={{}}
      operations={['选择', '移除']}
      render={(item) => item.title as string}
      onChange={handleTransferChange}
      className={styles.transferWrap}
      targetKeys={targetKeys}
    >
      {({ direction, onItemSelect, onItemSelectAll, selectedKeys }) => {
        if (direction === 'left') {
          const checkedKeys = [...selectedKeys, ...targetKeys];
          return (
            <div className={styles.source}>
              <div className={styles.sourceSearch}>
                <Input
                  placeholder="请输入搜索内容"
                  allowClear
                  suffix={<SearchOutlined style={{ color: 'rgba(0, 0, 0, 0.25)' }} />}
                  onChange={handleTreeSearchChange}
                />
              </div>
              <Tree
                height={300}
                className={styles.sourceTree}
                blockNode
                checkable
                defaultExpandAll
                checkedKeys={checkedKeys}
                treeData={generateTree(loop(treeData), targetKeys)}
                onCheck={(_, { checked, node: { key, children } }) => {
                  if (children?.length === 0) {
                    onItemSelect(key as string, !isChecked(checkedKeys as string[], key as string));
                  } else {
                    // 点击父节点
                    const childrenKeys = children!
                      .filter((item) => !item.disabled)
                      .map((item) => item.key as string);
                    onItemSelectAll(childrenKeys, checked);
                  }
                }}
                onSelect={(_, { selected, node: { key, children } }) => {
                  if (children?.length === 0) {
                    onItemSelect(key as string, !isChecked(checkedKeys as string[], key as string));
                  } else {
                    // 点击父节点
                    const childrenKeys = children!
                      .filter((item) => !item.disabled)
                      .map((item) => item.key as string);
                    onItemSelectAll(childrenKeys, selected);
                  }
                }}
                onExpand={handleTreeExpand}
                expandedKeys={treeExpandedKeys}
                autoExpandParent={autoExpandParent}
              />
            </div>
          );
        }
      }}
    </Transfer>
  );
};

export default CustomTransfer;
