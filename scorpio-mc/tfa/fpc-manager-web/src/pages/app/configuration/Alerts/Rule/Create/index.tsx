import { Card } from 'antd';
import { PureComponent } from 'react';
import AlertRuleForm from '../components/RuleForm';

class CreateAlertRule extends PureComponent {
  render() {
    return (
      <Card bordered={false}>
        <AlertRuleForm operateType="CREATE" />
      </Card>
    );
  }
}

export default CreateAlertRule;
