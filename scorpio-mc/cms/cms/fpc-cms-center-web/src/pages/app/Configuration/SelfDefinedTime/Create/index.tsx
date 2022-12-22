import { Card } from 'antd';
import TimeForm from '../components/TimeForm';
import { EPageMode } from '@/pages/app/GlobalSearch/PacketRetrieval/components/TransmitTaskForm';

export default function Create() {
    return (
       <Card bordered={false}>
          <TimeForm pageMode={EPageMode.Create}/>
       </Card>
    )
}
