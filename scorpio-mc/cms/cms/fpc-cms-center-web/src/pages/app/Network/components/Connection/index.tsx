import { connectTabs } from './constant';
import EditTabs from '../EditTabs';
import { createContext } from 'react';

export const ConnectionContext = createContext([]);
export default function Connection() {
  return <EditTabs tabs={connectTabs} consumerContext={ConnectionContext} />;
}
