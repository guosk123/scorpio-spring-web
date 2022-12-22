import type { ConnectState } from '@/models/connect';
import { Button, Result, Skeleton } from 'antd';
import type { FC } from 'react';
import React from 'react';
import { connect, history } from 'umi';
import type { ICustomCategory } from '../typings';
import CategoryForm from './Form';
/**
 * 自定义分类的最大数量限制
 */
export const MAX_CUSTOM_CATEGORY_LIMIT = 50;

interface ICreateCategoryProps {
  customCategoryList: ICustomCategory[];
  queryLoading: boolean;
}
const CreateCategory: FC<ICreateCategoryProps> = ({ customCategoryList, queryLoading }) => {
  if (queryLoading) {
    return <Skeleton active />;
  }
  if (customCategoryList.length >= MAX_CUSTOM_CATEGORY_LIMIT) {
    return (
      <Result
        status="warning"
        title={`最多支持新建${MAX_CUSTOM_CATEGORY_LIMIT}个自定义分类
            `}
        extra={
          <Button type="primary" onClick={() => history.goBack()}>
            返回
          </Button>
        }
      />
    );
  }
  return <CategoryForm />;
};

export default connect(({ loading, SAKnowledgeModel: { customCategoryList } }: ConnectState) => ({
  customCategoryList,
  queryLoading: loading.effects['SAKnowledgeModel/queryAllApplications'],
}))(CreateCategory);
