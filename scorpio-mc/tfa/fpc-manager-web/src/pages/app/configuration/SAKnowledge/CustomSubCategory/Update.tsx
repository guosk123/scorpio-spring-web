import type { FC } from 'react';
import { useEffect } from 'react';
import CategoryForm from './Form';
import type { Dispatch } from 'umi';
import { connect } from 'dva';
import type { AppSubCategoryItem } from '../typings';
import { ECustomSAApiType } from '../typings';
import type { ConnectState } from '@/models/connect';
import { Empty, Skeleton } from 'antd';

interface IUpdateSubCategoryProps {
  match: {
    params: { subcategoryId: string };
  };
  dispatch: Dispatch;
  customSubCategoryDetail: AppSubCategoryItem;
  queryDetailLoading: boolean | undefined;
}
const UpdateSubCategory: FC<IUpdateSubCategoryProps> = ({
  match,
  dispatch,
  customSubCategoryDetail,
  queryDetailLoading,
}) => {
  const { subcategoryId } = match.params;

  useEffect(() => {
    dispatch({
      type: 'customSAModel/queryCustomSADetail',
      payload: { id: subcategoryId, type: ECustomSAApiType.SUB_CATEGORY },
    });
  }, [dispatch, subcategoryId]);

  if (queryDetailLoading) {
    return <Skeleton />;
  }
  if (!customSubCategoryDetail.id) {
    return <Empty description="自定义子分类不存在或已被删除" />;
  }

  return <CategoryForm detail={customSubCategoryDetail} />;
};

export default connect(({ loading, customSAModel: { customSubCategoryDetail } }: ConnectState) => ({
  customSubCategoryDetail,
  queryDetailLoading: loading.effects['customSAModel/queryCustomSADetail'],
}))(UpdateSubCategory);
