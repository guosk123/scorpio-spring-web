import { createContext } from 'react';
import EditTabs from '../EditTabs';
import { alertMessageTabs } from './constant';

export const AlertContext = createContext([]);
export default function Alert() {
  return (
    <EditTabs
      tabsKey="alertMsg"
      destroyInactiveTabPane={false}
      tabs={alertMessageTabs}
      consumerContext={AlertContext}
    />
  );
}
