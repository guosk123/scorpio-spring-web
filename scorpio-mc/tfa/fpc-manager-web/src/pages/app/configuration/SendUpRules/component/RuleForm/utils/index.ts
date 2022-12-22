import { TABLE_COMMENT_MAP } from '../dict';
import type { TCHFieldType, TFieldOperator } from '../typing';

// 比较操作符
const comparisonOperatorList: TFieldOperator[] = [
  'EQUALS',
  'NOT_EQUALS',
  'GREATER_THAN',
  'GREATER_THAN_OR_EQUAL',
  'LESS_THAN',
  'LESS_THAN_OR_EQUAL',
];

/**
 * 根据字段类型获取对应支持的操作符
 * @param fieldType 字段类型
 * @returns 该类型支持的操作符数组
 */
export function getOperatorByFieldType(fieldType: TCHFieldType): TFieldOperator[] {
  const type = fieldType.toLocaleLowerCase();

  // 单纯的 String，不包含 Atrray(String)
  if (type.includes('string') && !type.includes('array')) {
    return ['EQUALS', 'NOT_EQUALS', 'LIKE', 'EXISTS', 'NOT_EXISTS'];
  }

  // Array(IP4) or Array(IP6)
  if (type.includes('array') && type.includes('ipv')) {
    return ['EQUALS', 'EXISTS', 'NOT_EXISTS'];
  }

  // Array(T)
  if (type.includes('array') && !type.includes('ipv')) {
    return ['EQUALS', 'LIKE', 'EXISTS', 'NOT_EXISTS'];
  }

  // 单纯的 IP 类型
  // IPv4 or IPv6
  if (!type.includes('array') && type.includes('ipv')) {
    return ['EQUALS', 'NOT_EQUALS'];
  }

  if (
    // 时间
    type.includes('datetime') ||
    // 无符号数字
    type.includes('uint')
  ) {
    return comparisonOperatorList;
  }

  // 其他的类型的只返回等于号
  return ['EQUALS'];
}

/** 格式化title */
export const formatTitle = (title: string) => {
  if (title === 'mail-pop3') {
    return 'POP3';
  }
  if (title === 'mail-smtp') {
    return 'SMTP';
  }
  if (title === 'mail-imap') {
    return 'IMAP';
  }
  return (TABLE_COMMENT_MAP[title] || title).toUpperCase();
};

/** 驼峰转蛇形(忽略数字) */
export function snakeCaseIgnoreNumber(str: string) {
  let result = '';
  for (let i = 0; i < str.length; i++) {
    const c = str.charCodeAt(i);
    if (c >= 65 && c <= 90) {
      if (result === '') {
        result += String.fromCharCode(c).toLowerCase();
      } else {
        result += `_${String.fromCharCode(c).toLowerCase()}`;
      }
    } else {
      result += str[i];
    }
  }
  return result;
}
