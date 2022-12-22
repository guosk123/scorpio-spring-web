import { pageIsEmbed } from '@/utils/utils';
import { DoubleRightOutlined, LeftSquareOutlined } from '@ant-design/icons';
import type { TreeProps } from 'antd';
import { Affix, Button, Card, Input, Tooltip, Tree } from 'antd';
import React, { useCallback, useMemo, useState } from 'react';
import styles from './index.less';
import type { INetworkTreeData } from '@/models/app/network';
import AutoHeightContainer from '@/components/AutoHeightContainer';

// 展开状态保存在浏览器中的 key
export const SEARCH_TREE_COLLAPSED_KEY = 'search-tree-collapsed';
export interface ITreeData {
  key: string;
  title: string;
  disabled?: boolean;
  children?: ITreeData[];
}

export interface ISearchTreeProps extends Pick<TreeProps, 'onSelect'> {
  data: INetworkTreeData[];
  // onSelect: TreeProps['onSelect'];
  selectedKeys: string[];

  //
  collapsed: boolean;
  onToggleCollapsed: (collapsed: boolean) => void;
  multiple?: boolean;
}

const SearchTree: React.FC<ISearchTreeProps> = ({
  data = [],
  onSelect,
  selectedKeys = [],
  collapsed,
  onToggleCollapsed,
  multiple = false,
}) => {
  /** 树搜索关键字 */
  const [searchValue, setSearchValue] = useState<string>('');
  const [expandedKeys, setExpandedKeys] = useState<React.Key[]>([]);

  const treeKeysMap = useMemo(() => {
    // 整理树所有的 key
    // {父节点ID: [子节点ID]}
    const keysMap: Record<string, string[]> = {};
    data.forEach((ele) => {
      keysMap[ele.key] = ele.children?.map((item) => item.key) || [];
    });
    return keysMap;
  }, [data]);

  const defExpandKeys = useMemo(() => {
    const res = selectedKeys.map((key) => treeKeysMap[key] && key);
    Object.keys(treeKeysMap).forEach((ele) => {
      if (
        treeKeysMap[ele].filter((item) => selectedKeys.includes(item)).length &&
        !res.includes(ele)
      ) {
        res.push(ele);
      }
    });
    setExpandedKeys(res);
    return res;
  }, [selectedKeys, treeKeysMap]);

  const handleSearchChange = (e: any) => {
    const { value } = e.target;
    setSearchValue(value);
    // setAutoExpandParent(true);
  };

  const handleTreeExpand = (keys: React.Key[]) => {
    setExpandedKeys(keys);
    // setAutoExpandParent(false);
  };

  const handleCollapsed = useCallback(() => {
    if (onToggleCollapsed) {
      onToggleCollapsed(!collapsed);
    }
  }, [onToggleCollapsed, collapsed]);

  const filterData = useMemo(() => {
    if (!searchValue) {
      // setExpandedKeys(defExpandKeys);
      return data;
    }
    // 先遍历，一级菜单
    const nextExpandKeys: React.Key[] = [];
    const nextData: ITreeData[] = [];
    for (let index = 0; index < data.length; index += 1) {
      const node = data[index];
      const { children = [] } = node;
      // 父节点是否命中
      const parentFlag = node.title.indexOf(searchValue) > -1;
      // 命中的子节点列表
      const hitChildren = children.filter((el) => el.title.indexOf(searchValue) > -1);

      // 命中父节点，显示父节点及其子节点，展开父节点 （命中父节点,子节点不考虑）
      if (parentFlag) {
        nextData.push({
          ...node,
        });
      }

      // 只命中子节点
      if (!parentFlag && hitChildren.length > 0) {
        nextData.push({
          ...node,
          children: hitChildren,
        });
      }

      // 有命中该节点就展开
      if (parentFlag || hitChildren.length > 0) {
        nextExpandKeys.push(node.key);
      }
    }
    setExpandedKeys(nextExpandKeys);
    return nextData;
  }, [searchValue, data]);

  const offsetTop = pageIsEmbed() ? 10 : 80;

  return (
    <>
      {collapsed ? (
        <Affix key="miniBar" offsetTop={offsetTop}>
          <div onClick={handleCollapsed} className={styles.miniBar}>
            <Tooltip title="展开" placement="right">
              <div className={styles.barWrap}>
                <DoubleRightOutlined />
              </div>
            </Tooltip>
          </div>
        </Affix>
      ) : (
        <Affix key="searchTreeWrap" offsetTop={offsetTop}>
          <div className={styles.searchTreeWrap}>
            <Card bodyStyle={{ padding: 6 }}>
              <Button
                block
                type="primary"
                icon={<LeftSquareOutlined />}
                className={styles.collapsedBtn}
                onClick={handleCollapsed}
              >
                收起
              </Button>
              <Input
                placeholder="关键字搜索"
                allowClear
                onChange={handleSearchChange}
                className={styles.searchInput}
              />
              <AutoHeightContainer contentStyle={{ overflowY: 'auto', overflowX: 'scroll' }}>
                <Tree
                  multiple={multiple}
                  blockNode
                  showLine
                  defaultExpandedKeys={defExpandKeys}
                  onExpand={handleTreeExpand}
                  // defaultExpandAll
                  expandedKeys={expandedKeys}
                  autoExpandParent={true}
                  treeData={filterData}
                  {...(onSelect ? { onSelect } : {})}
                  selectedKeys={selectedKeys}
                />
              </AutoHeightContainer>
            </Card>
          </div>
        </Affix>
      )}
    </>
  );
};

export default SearchTree;
