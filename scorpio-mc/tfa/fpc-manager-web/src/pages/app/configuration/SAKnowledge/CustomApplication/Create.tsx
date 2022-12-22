import type { ConnectState } from '@/models/connect';
import { Button, Result, Skeleton } from 'antd';
import type { FC } from 'react';
import { connect, history } from 'umi';
import type { ApplicationItem } from '../typings';
import CustomApplicationForm from './Form';
/**
 * 自定义分类的最大数量限制
 */
export const MAX_CUSTOM_APPLICATION_LIMIT = 256;

interface ICreateApplicationProps {
  customApplicationList: ApplicationItem[];
  queryLoading?: boolean;
}
const CreateCustomApplication: FC<ICreateApplicationProps> = ({
  customApplicationList,
  queryLoading,
}) => {
  if (queryLoading) {
    return <Skeleton active />;
  }
  if (customApplicationList.length >= MAX_CUSTOM_APPLICATION_LIMIT) {
    return (
      <Result
        status="warning"
        title={`最多支持新建${MAX_CUSTOM_APPLICATION_LIMIT}个自定义应用
            `}
        extra={
          <Button type="primary" key="console" onClick={() => history.goBack()}>
            返回
          </Button>
        }
      />
    );
  }
  return <CustomApplicationForm operateType="CREATE" />;
};

export default connect(
  ({ loading, SAKnowledgeModel: { customApplicationList } }: ConnectState) => ({
    customApplicationList,
    queryLoading: loading.effects['SAKnowledgeModel/queryAllApplications'],
  }),
)(CreateCustomApplication);
