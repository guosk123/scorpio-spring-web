import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Card } from 'antd';

import ScenarioTaskForm from '../components/ScenarioTaskForm';

@connect((state) => {
  const { loading } = state;
  const { effects } = loading;
  return {
    submitLoading: effects['insideHostsModel/createInsideHost'],
  };
})
class CreateScenarioTaskForm extends PureComponent {
  componentDidMount() {}

  render() {
    const { submitLoading } = this.props;
    return (
      <Card bordered={false}>
        <ScenarioTaskForm operateType="CREATE" submitLoading={submitLoading} />
      </Card>
    );
  }
}

export default CreateScenarioTaskForm;
