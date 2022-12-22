import { request } from 'umi';

export function requestShowList() {
  return request('/api/showList')
}

export function requestTreeData() {
  return request('/api/showTree')
}

