import { Card } from 'antd';
import { PureComponent } from 'react';
import TimeForm from '../components/TimeForm';
import { EPageMode } from '@/pages/app/appliance/TransmitTask/components/TransmitTaskForm';

export default class create extends PureComponent {
  render() {
    return (
       <Card bordered={false}>
          <TimeForm pageMode={EPageMode.Create}/>
       </Card>
    )
  }
}
