import { DownOutlined } from '@ant-design/icons';
import { Menu, Dropdown, Button } from 'antd';
import React, { memo } from 'react';
import type { ICustomStatTapData } from '../typings';
import analyseJson from './data/analyse';
import exportObjectJson from './data/exportObject';
import statsJson from './data/stats';
import styles from './index.less';

export interface ToolbarMenuProps {
  onClick: (node: ICustomStatTapData) => void;
}

interface MenuItemType {
  title: string;
  children: ICustomStatTapData[];
}

const menus: MenuItemType[] = [
  {
    title: '统计',
    children: statsJson || [],
  },
  {
    title: '分析',
    children: analyseJson || [],
  },
  {
    title: '对象导出',
    children: exportObjectJson || [],
  },
];

const ToolbarMenu: React.FC<ToolbarMenuProps> = ({ onClick = () => {} }) => {
  const renderSubMenu = (nodes: ICustomStatTapData[]) => (
    <Menu className={`${styles.subMenu} ${nodes.length > 0 && styles.maxHeight}`}>
      {nodes.map((node) => (
        <Menu.Item
          key={node.tap}
          onClick={() => {
            onClick(node);
          }}
        >
          {node.name_zh}
        </Menu.Item>
      ))}
    </Menu>
  );
  const renderMenu = (title: string, menuNodes: ICustomStatTapData[] = []) => (
    <span className={styles.menuItem} key={title}>
      <Dropdown
        overlayClassName={styles.dropdownMenu}
        overlay={() => renderSubMenu(menuNodes)}
        trigger={['click']}
      >
        <Button>
          {title} <DownOutlined />
        </Button>
      </Dropdown>
    </span>
  );

  return (
    <div style={{ display: 'flex' }} className={styles.menuWrap}>
      {menus.map(({ title, children }) => renderMenu(title, children))}
    </div>
  );
};

export default memo(ToolbarMenu);
