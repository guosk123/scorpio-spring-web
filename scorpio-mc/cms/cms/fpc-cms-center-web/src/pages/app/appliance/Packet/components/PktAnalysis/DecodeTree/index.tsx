import { FilterTwoTone } from '@ant-design/icons';
import { Spin, Tree } from 'antd';
import type { EventDataNode } from 'antd/lib/tree';
import React, { Fragment, useEffect, useState } from 'react';
import type { IProtocolTree, IProtocolTreeNode } from '../typings';
import styles from './index.less';

const { TreeNode } = Tree;

interface IDecodeTreeProps {
  decodeData: IProtocolTree;
  onTreeClick: (node: IProtocolTreeNode) => void;
  onFilter: (filter: string) => void;
  loading: boolean;
  style?: React.CSSProperties | undefined;
}
const DecodeTree: React.FC<IDecodeTreeProps> = ({
  decodeData,
  onTreeClick,
  onFilter,
  loading,
  style,
}) => {
  const [selectedKey, setSelectedKey] = useState<string[]>([]);

  const handleTreeClick = (
    e: any,
    treeNode: EventDataNode & {
      props: {
        'data-key': string;
        'data-node': string;
      };
    },
  ) => {
    const nodeKey = treeNode.props['data-key'];
    if (selectedKey.indexOf(nodeKey) > -1) {
      return;
    }
    setSelectedKey([nodeKey]);

    const nodeDataString = treeNode.props['data-node'];
    let nodeData = {};
    try {
      nodeData = JSON.parse(nodeDataString);
    } catch (err) {
      nodeData = {};
    }
    onTreeClick(nodeData as IProtocolTreeNode);
  };

  useEffect(() => {
    setSelectedKey([]);
  }, [decodeData]);

  const handleFilter = (filter: string) => {
    if (onFilter) {
      onFilter(filter);
    }
  };

  /**
   * 渲染树
   */
  const renderTree = (tree: IProtocolTree['tree'], key?: string) => {
    if (!Array.isArray(tree)) {
      return null;
    }

    return tree.map((node, index) => {
      const nodeKey = key ? `${key}-${index}` : `${index}-0`;

      return (
        <TreeNode
          title={
            <Fragment>
              <span className={styles.filterWrap}>
                <FilterTwoTone onClick={() => handleFilter(node.f)} />
              </span>
              <span>{node.l}</span>
            </Fragment>
          }
          key={nodeKey}
          data-node={JSON.stringify(node)}
          data-key={nodeKey}
        >
          {node.n && renderTree(node.n, nodeKey)}
        </TreeNode>
      );
    });
  };

  return (
    <div style={style} className={styles.decodeTree}>
      <Spin spinning={loading}>
        {!decodeData.tree ? (
          <p className={styles.emptyText}>选择数据包</p>
        ) : (
          // @ts-ignore
          <Tree selectedKeys={selectedKey} onClick={handleTreeClick}>
            {renderTree(decodeData.tree)}
          </Tree>
        )}
      </Spin>
    </div>
  );
};

export default DecodeTree;
