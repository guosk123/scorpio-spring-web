import type { IEnhancedTableProps } from '@/components/EnhancedTable';
import EnhancedTable from '@/components/EnhancedTable';
import { Menu, Tabs } from 'antd';
import type { ReactNode } from 'react';
import { useCallback, useMemo } from 'react';
import type { IFieldProperty } from '../fieldsManager';
import styles from './index.less';

type IndexColumn = {
  index: number;
};

export interface ITableMenuListProps<MenuKey> {
  title: string;
  key: MenuKey;
  excludeColumn: string[];
  overrideColumns?: Record<string, IFieldProperty>;
}

export interface IWithMenuTableProps<RecordType, MenuKey> extends IEnhancedTableProps<RecordType> {
  menuItemList: ITableMenuListProps<MenuKey>[];
  currentMenu: MenuKey;
  // eslint-disable-next-line no-unused-vars
  onMenuChange: (menuItemKey: MenuKey) => any;
  needHeight?: number;
}

function WithMenuTable<RecordType extends IndexColumn, MenuKey extends string>(
  props: IWithMenuTableProps<RecordType, MenuKey>,
) {
  const { tableKey, menuItemList, columns, onMenuChange, currentMenu, extraTool,needHeight, ...restProps } =
    props;

  const handleMenuClick = useCallback(
    (e: any) => {
      onMenuChange(e);
    },
    [onMenuChange],
  );

  const extraMenu = useMemo(() => {
    return (
      <div className={styles.extraWrap}>
        {/* <Menu mode="horizontal" onClick={handleMenuClick} selectedKeys={[currentMenu]}>
          {menuItemList.map((item) => {
            return <Menu.Item key={item.key}>{item.title}</Menu.Item>;
          })}
        </Menu> */}
        <Tabs
          onChange={handleMenuClick}
          tabBarStyle={{ margin: 0, height: 34 }}
          defaultActiveKey={currentMenu}
          size={'small'}
        >
          {menuItemList.map((item) => {
            return (
              <Tabs.TabPane tab={item.title} key={item.key}>
                <div />
              </Tabs.TabPane>
            );
          })}
        </Tabs>
        {extraTool}
      </div>
    );
  }, [currentMenu, extraTool, handleMenuClick, menuItemList]);

  const tables = useMemo<Record<MenuKey, ReactNode>>(() => {
    const resTables = {} as Record<MenuKey, ReactNode>;
    menuItemList.forEach((item) => {
      const exclude = item.excludeColumn;
      const override = item?.overrideColumns;
      const tmpColumns = columns
        .filter((col) => {
          return !exclude?.includes(col.dataIndex as string);
        })
        .map((col) => {
          const overrideProperty = override && override[col.dataIndex as string];
          return {
            ...col,
            title: overrideProperty ? overrideProperty.name : col.title,
          };
        });
      resTables[item.key as string] = (
        <EnhancedTable<IndexColumn & RecordType>
          {...restProps}
          tableKey={`${tableKey}-${currentMenu}`}
          key={`${tableKey}-${currentMenu}`}
          extraTool={extraMenu}
          columns={tmpColumns}
          fixHeight={needHeight}
        />
      );
    });
    return resTables;
  }, [columns, currentMenu, extraMenu, menuItemList, needHeight, restProps, tableKey]);

  return <>{tables[currentMenu]}</>;
}

export default WithMenuTable;
