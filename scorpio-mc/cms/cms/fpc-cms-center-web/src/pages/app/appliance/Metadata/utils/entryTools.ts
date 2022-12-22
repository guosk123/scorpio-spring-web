import { EMetadataNetworkProtocolEntry, EMetadataScenarioProtocolEntry } from '../typings';

export function getEntryTag(pathname: string) {
  if (pathname.includes('network')) {
    return 'network';
  }

  if (pathname.includes('detection')) {
    return 'detection';
  }

  return undefined;
}

export function getEntryFromProtocol(entryTag: string | undefined, protocol: string | undefined) {
  if (entryTag && protocol) {
    if (entryTag === 'network') {
      return EMetadataNetworkProtocolEntry[protocol.toUpperCase()];
    }
    if (entryTag === 'detection') {
      return EMetadataScenarioProtocolEntry[protocol.toUpperCase()];
    }
  }
  return undefined;
}
