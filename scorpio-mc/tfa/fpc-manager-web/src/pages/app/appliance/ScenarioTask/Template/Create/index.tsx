import { Card } from 'antd';
import ScenarioTaskTemplateForm from '../../components/ScenarioTemplateForm';

const CreateScenarioTaskTemplate = () => {
  return (
    <Card bordered={false}>
      <ScenarioTaskTemplateForm operateType="CREATE" />
    </Card>
  );
};

export default CreateScenarioTaskTemplate;
