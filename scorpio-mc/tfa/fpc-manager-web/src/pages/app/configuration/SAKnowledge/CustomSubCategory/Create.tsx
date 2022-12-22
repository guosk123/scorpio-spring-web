import type { ConnectState } from '@/models/connect';
import { Button, Result, Skeleton } from 'antd';
import { connect, history } from 'umi';
import type { FC } from 'react';
import React from 'react';
import type { ICustomSubCategory } from '../typings';
import SubCategoryForm from './Form';
/**
 * 自定义子分类的最大数量限制
 */
export const MAX_CUSTOM_SUB_CATEGORY_LIMIT = 100;

interface ICreateSubCategoryProps {
  customSubCategoryList: ICustomSubCategory[];
  queryLoading: boolean;
}
const CreateSubCategory: FC<ICreateSubCategoryProps> = ({
  customSubCategoryList,
  queryLoading,
}) => {
  if (queryLoading) {
    return <Skeleton active />;
  }
  if (customSubCategoryList.length >= MAX_CUSTOM_SUB_CATEGORY_LIMIT) {
    return (
      <Result
        status="warning"
        title={`最多支持新建${MAX_CUSTOM_SUB_CATEGORY_LIMIT}个自定义分类
            `}
        extra={
          <Button type="primary" onClick={() => history.goBack()}>
            返回
          </Button>
        }
      />
    );
  }

  return <SubCategoryForm />;
};

export default connect(
  ({ loading, SAKnowledgeModel: { customSubCategoryList } }: ConnectState) => ({
    customSubCategoryList,
    queryLoading: loading.effects['SAKnowledgeModel/queryAllApplications'],
  }),
)(CreateSubCategory);
