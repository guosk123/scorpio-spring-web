import storage from '@/utils/frame/storage';
import { parseArrayJson } from '@/utils/utils';
import { Typography } from 'antd';
import { isNil } from 'lodash';
import { EMPTY_DATAINDEX } from '.';

/**
 *
 * @param excludes 表格某些列名不要, index, action 等列已内置排除，不用传递
 * @returns 生成特定类型表格的获取列参数函数
 */
export const getColumnParamsFunc = (excludes: string[] = []) => {
  const finnalExcludes = ['indexNumber', 'action', 'index', EMPTY_DATAINDEX].concat(excludes);

  /**
   * @param cols 表格列名
   * @param tableKey 表格列本地存储的localStorage-key值
   * @param extra 额外的列参数
   * @param withExtra 是否返回extra，若为false，则不反悔extra, 主要解决会话详单 在查询时需要默认携带应用层id，但是导出时，不默认携带
   * @returns 返回请求时传给后端的列参数
   */
  return ({
    cols = [],
    tableKey,
    extra = [],
  }: {
    cols?: string[];
    tableKey?: string;
    extra?: string[];
  }) => {
    let finnalCols = [...cols];
    if (finnalCols.length === 0 && tableKey) {
      finnalCols = parseArrayJson(storage.get(`${tableKey}-order`) || '[]')
        .filter((item: any) => !item.hideInTable)
        .map((item: any) => item.dataIndex);
    }

    if (extra) {
      finnalCols = [...finnalCols, ...extra];
    }

    // 常用的非数据列，内置进函数，参数可以仅传其他特殊的不能当作参数的列
    return [...new Set(finnalCols)]
      ?.filter((item) => {
        return !finnalExcludes.includes(item);
      })
      .join(',');
  };
};

export const genEllipsis = (
  text: React.ReactNode | string,
  copyable?: boolean,
  stopPropagation?: boolean,
) => {
  let _text = isNil(text) ? '' : String(text);

  if (typeof text === 'string' && [null, undefined, ''].includes(text)) _text = '-';

  return (
    <Typography.Text
      style={{
        width: '100%',
        margin: 0,
        padding: 0,
        color: 'inherit',
      }}
      onClick={(e) => (stopPropagation ? e?.stopPropagation() : null)}
      title=" "
      copyable={
        copyable && typeof text === 'string' && text
          ? {
              text,
              tooltips: ['', ''],
            }
          : undefined
      }
      ellipsis={text ? { tooltip: text } : false}
    >
      {_text}
    </Typography.Text>
  );
};
