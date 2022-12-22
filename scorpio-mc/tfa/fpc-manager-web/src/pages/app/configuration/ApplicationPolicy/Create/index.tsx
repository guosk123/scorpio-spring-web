import { Card } from 'antd';
import React from 'react';
import ApplicationPolicyForm from '../components/PolicyForm';

interface ICreateApplicationPolicyProps {}

const CreateApplicationPolicy: React.FC<ICreateApplicationPolicyProps> = () => {
  return (
    <Card bordered={false}>
      <ApplicationPolicyForm />
    </Card>
  );
};

export default CreateApplicationPolicy;
