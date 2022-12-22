import { createContext } from 'react';
import EditTabs from '../EditTabs';
import { retransmissionTabs } from './constant';

export const RetransmissionContext = createContext([]);
export default function Retransmission() {
  return <EditTabs tabs={retransmissionTabs} consumerContext={RetransmissionContext} />;
}
