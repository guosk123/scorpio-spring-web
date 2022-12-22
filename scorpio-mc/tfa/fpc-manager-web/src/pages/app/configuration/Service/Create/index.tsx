import type { ConnectState } from '@/models/connect';
import Result from '@/pages/app/appliance/ScenarioTask/Result';
import { Button, Skeleton } from 'antd';
import { connect } from 'dva';
import ServiceForm from '../components/Form';
import type { IService } from '../typings';
import { MAX_CUSTOM_SERVICE_LIMIT } from '../typings';
import { history } from 'umi';

interface Props {
  allServices: IService[];
  loading?: boolean;
}

function CreateService(props: Props) {
  const { allServices, loading } = props;
  if (loading) {
    return <Skeleton active />;
  }
  if (allServices.length >= MAX_CUSTOM_SERVICE_LIMIT) {
    return (
      <Result
        status="warning"
        title={`最多支持业务${MAX_CUSTOM_SERVICE_LIMIT}个`}
        extra={
          <Button type="primary" key="console" onClick={() => history.goBack()}>
            返回
          </Button>
        }
      />
    );
  }
  return <ServiceForm />;
}
export default connect(({ serviceModel: { allServices }, loading }: ConnectState) => ({
  allServices,
  loading: loading.effects['serviceModel/queryAllServices'],
}))(CreateService);
