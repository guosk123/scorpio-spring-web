import React, { PureComponent } from 'react';
import { Skeleton, Empty } from 'antd';
import { connect } from 'dva';

import CustomApplicationForm from './Form';
import { ECustomSAApiType } from '../typings';

@connect((state) => {
  const {
    loading,
    customSAModel: { customApplicationDetail },
  } = state;
  const { effects } = loading;
  return {
    detail: customApplicationDetail,
    queryDetailLoading: effects['customSAModel/queryCustomSADetail'],
  };
})
class UpdateCustomApplication extends PureComponent {
  componentDidMount() {
    const { dispatch, match } = this.props;
    const { applicationId } = match.params;
    dispatch({
      type: 'customSAModel/queryCustomSADetail',
      payload: {
        id: applicationId,
        type: ECustomSAApiType.APPLICATION,
      },
    });
  }

  render() {
    const { queryDetailLoading, detail } = this.props;
    return (
      <Skeleton loading={queryDetailLoading}>
        {detail.id ? (
          <CustomApplicationForm detail={detail} operateType="UPDATE" />
        ) : (
          <Empty description="自定义应用不存在或已被删除" />
        )}
      </Skeleton>
    );
  }
}

export default UpdateCustomApplication;
