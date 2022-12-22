import type { ConnectState } from '@/models/connect';
import { Empty, Skeleton } from 'antd';
import { connect } from 'dva';
import type { FC } from 'react';
import { useEffect } from 'react';
import type { Dispatch } from 'umi';
import type { AppCategoryItem } from '../typings';
import { ECustomSAApiType } from '../typings';
import CategoryForm from './Form';

interface IUpdateCategoryProps {
  match: {
    params: { categoryId: string };
  };
  dispatch: Dispatch;
  customCategoryDetail: AppCategoryItem;
  queryDetailLoading: boolean | undefined;
}
const UpdateCategory: FC<IUpdateCategoryProps> = ({
  match,
  dispatch,
  customCategoryDetail,
  queryDetailLoading,
}) => {
  const { categoryId } = match.params;

  useEffect(() => {
    dispatch({
      type: 'customSAModel/queryCustomSADetail',
      payload: { id: categoryId, type: ECustomSAApiType.CATEGORY },
    });
  }, [dispatch, categoryId]);

  if (queryDetailLoading) {
    return <Skeleton />;
  }
  if (!customCategoryDetail.id) {
    return <Empty description="自定义分类不存在或已被删除" />;
  }
  // @ts-ignore
  return <CategoryForm detail={customCategoryDetail} />;
};

export default connect(({ loading, customSAModel: { customCategoryDetail } }: ConnectState) => ({
  customCategoryDetail,
  queryDetailLoading: loading.effects['customSAModel/queryCustomSADetail'],
}))(UpdateCategory);
