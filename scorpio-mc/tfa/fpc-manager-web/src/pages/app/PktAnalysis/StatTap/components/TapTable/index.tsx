import React, { memo } from 'react';
import BaseTable, { unflatten, AutoResizer } from 'react-base-table';
import 'react-base-table/styles.css';
import styles from './index.less';

type rowData = Record<string, any>;

type fieldsData = Record<string, string>;

const TapTable: React.FC<{
  title: string;
  fields: fieldsData;
  data: rowData[];
}> = ({ title, fields, data }) => {
  const fieldKeys = Object.keys(fields);

  const getFieldName = (field: any) => {
    const [enName, zhName] = fields[field].split('|');
    if (zhName) {
      return zhName;
    }
    return enName;
  };

  const tableColumns = fieldKeys.map((field) => ({
    key: field,
    dataKey: field,
    title: getFieldName(field),
    width: 150,
    minWidth: 100,
    resizable: true,
    // @ts-ignore
    className: ({ rowData }) => rowData.trClassName,
  }));

  return (
    <div className={styles.tableWrap}>
      <p className={styles.title}>{title}</p>
      <AutoResizer>
        {({ width }) => (
          <BaseTable
            headerHeight={40}
            rowHeight={36}
            width={width}
            height={500}
            fixed
            columns={tableColumns}
            data={unflatten(data)}
            emptyRenderer={<div className={styles.emptey}>暂无数据</div>}
          />
        )}
      </AutoResizer>
    </div>
  );
};

export default memo(TapTable);
