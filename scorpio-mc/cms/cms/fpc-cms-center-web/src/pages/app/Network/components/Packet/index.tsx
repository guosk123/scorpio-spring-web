import Packet from '@/pages/app/appliance/Packet';
import { createContext } from 'react';

export const PacketContext = createContext([]);

export default function index() {
  return (
    <PacketContext.Provider value={[]}>
      <Packet />
    </PacketContext.Provider>
  );
}
