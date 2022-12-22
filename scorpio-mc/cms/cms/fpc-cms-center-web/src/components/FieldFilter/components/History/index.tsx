import storage from '@/utils/frame/storage';
import { ClearOutlined, CloseSquareOutlined } from '@ant-design/icons';
import { Divider, Form, Input, message, Modal, Popconfirm, Popover, Tag } from 'antd';
import hash from 'hash.js';
import React, { forwardRef, Fragment, useEffect, useImperativeHandle, useState } from 'react';
import { v1 as uuidv1 } from 'uuid';
import { getFilterContent, getFilterGroupContent } from '../..';
import type { IField, IFilter, IFilterGroup, ISearchHistory } from '../../typings';
import styles from './index.less';

export const FILTER_HISTORY_STORAGE_KEY = 'filter-history';

export interface ISearchHistoryRefReturn {
  save: (filter: (IFilter | IFilterGroup)[]) => void;
}

interface ISearchHistoryProps {
  history?: ISearchHistory[];
  localStorageKey: string;
  fields: IField[];
  onClick: (history: ISearchHistory) => void;
}
const SearchHistory = (
  { history = [], localStorageKey, onClick, fields }: ISearchHistoryProps,
  wrapperRef: any,
) => {
  const [searchHistory, setSearchHistory] = useState<ISearchHistory[]>(history);
  const [form] = Form.useForm();

  useEffect(() => {
    setSearchHistory(getSearchHistory());
  }, []);

  useEffect(() => {
    storage.put(localStorageKey, JSON.stringify(searchHistory));
  }, [searchHistory]);

  // 对外提供接口
  useImperativeHandle(wrapperRef, () => ({
    save: (filter: (IFilter | IFilterGroup)[]) => {
      handleSaveNewHistory(filter);
    },
  }));

  /**
   * 新增查询历史
   */
  const handleSaveNewHistory = (filter: (IFilter | IFilterGroup)[]) => {
    // 计算hash值
    const hashcode = hash.sha256().update(JSON.stringify(filter)).digest('hex');
    // 判断是否已经存在历史
    const findResult = searchHistory.find((item) => item.id === hashcode);
    if (findResult) {
      message.warning(`已存在相同过滤条件历史记录【${findResult.name}】`);
      return;
    }

    const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
      const { value } = event.target;
      saveHistoryModal.update({
        okButtonProps: {
          disabled: !value,
        },
      });
    };

    // 可以自定义查询历史的名字
    const saveHistoryModal = Modal.confirm({
      title: '保存历史记录',
      content: (
        <Fragment>
          <div style={{ marginBottom: 10 }}>自定义查询历史的显示名称：</div>
          <Form form={form} key={uuidv1()}>
            <Form.Item
              noStyle
              name="historyName"
              rules={[{ required: true, whitespace: true, message: '请输入历史记录的自定义名称' }]}
            >
              <Input
                autoFocus
                autoComplete="new-password"
                placeholder=""
                allowClear
                onChange={handleInputChange}
              />
            </Form.Item>
          </Form>
        </Fragment>
      ),
      okButtonProps: {
        disabled: true,
      },
      onOk: async () => {
        try {
          const { historyName } = await form.validateFields();
          setSearchHistory([
            ...searchHistory,
            {
              id: hashcode,
              // 这里不会出现未命名的情况
              name: historyName || '未命名',
              filter,
            },
          ]);
          return new Promise((resolve) => {
            resolve(true);
          });
        } catch (errorInfo) {
          return new Promise((resolve, reject) => {
            reject();
          });
        }
      },
      afterClose: () => {
        form.setFieldsValue({ historyName: undefined });
        saveHistoryModal.destroy();
      },
    });
  };

  /**
   * 删除一个查询历史
   */
  const handleRemoveHistoryItem = ({ id }: ISearchHistory) => {
    setSearchHistory(searchHistory.filter((item) => item.id !== id));
  };

  /**
   * 删除所有的查询历史
   */
  const handleRemoveAllHistory = () => {
    setSearchHistory([]);
  };

  /**
   * 点击某个查询历史，再次查询
   */
  const handleSearchAgain = (history: ISearchHistory) => {
    if (onClick) {
      onClick(history);
    }
  };

  /**
   * 获取 Local Storage 存储的历史
   */
  const getSearchHistory = () => {
    const historyJsonStr: null | string = storage.get(localStorageKey);
    let historyJson = [];

    if (historyJsonStr) {
      try {
        historyJson = JSON.parse(historyJsonStr);
      } catch (error) {
        historyJson = [];
      }
    }
    return historyJson;
  };

  if (searchHistory.length === 0) {
    return null;
  }

  return (
    <div className={styles.filterHistoryWrap}>
      <Popconfirm title="确认清空所有的历史记录吗？" onConfirm={handleRemoveAllHistory}>
        <Tag
          icon={<ClearOutlined />}
          color="#f50"
          className={styles.tag}
          style={{ marginRight: 0 }}
        >
          清空历史记录
        </Tag>
      </Popconfirm>
      <Divider type="vertical" />
      {searchHistory.map((history) => (
        <Popover
          key={history.id}
          trigger="hover"
          title={history.name}
          content={
            <div className={styles.historyPopover}>
              {history.filter.map((filterItem, index) => (
                <div className={styles.historyPane} key={`${history.id}_${index}`}>
                  {filterItem.hasOwnProperty('operand')
                    ? getFilterContent(filterItem as IFilter, false, fields)
                    : getFilterGroupContent(filterItem as IFilterGroup, false, fields)}
                </div>
              ))}
            </div>
          }
        >
          <Popconfirm title="确定再次查询吗？" onConfirm={() => handleSearchAgain(history)}>
            <Tag
              color="blue"
              closable
              className={styles.tag}
              key={history.id}
              closeIcon={
                <span className={styles.removeTagBtn}>
                  <Popconfirm
                    title="确定删除这个历史记录吗？"
                    onConfirm={() => handleRemoveHistoryItem(history)}
                  >
                    <CloseSquareOutlined />
                  </Popconfirm>
                </span>
              }
              onClose={(e) => e.preventDefault()}
            >
              {history.name || '未命名'}
            </Tag>
          </Popconfirm>
        </Popover>
      ))}
    </div>
  );
};

export default forwardRef(SearchHistory);
