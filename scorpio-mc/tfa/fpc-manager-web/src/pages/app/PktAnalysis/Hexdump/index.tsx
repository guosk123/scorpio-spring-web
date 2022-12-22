import type { RadioChangeEvent } from 'antd';
import { Radio, Spin } from 'antd';
import React, { Fragment, useCallback, useEffect, useMemo, useState } from 'react';
import type { IHexdumpHighlight, IProtocolTree } from '../typings';
import styles from './index.less';

/**
 * 任意字符转十六进制，并补齐为固定长度
 * @param char 任意编码的字符
 * @param pad 补齐的固定长度
 * @returns 固定长度的 HEX 编码
 */
export function x2Hex(char: any, pad: number) {
  let hex = char.toString(16);
  while (char.length < pad) {
    hex = `0${hex}`;
  }
  return hex;
}

/**
 * Unicode code 转字符串（不可见的字符显示为点(.)）
 * @param code char code
 * @param preserveWrap 是否保留空格、换行
 * @returns 字符串
 */
export function ascii2Char(code: number, preserveWrap = false) {
  // 只返回可见字符
  // @see https://asecuritysite.com/coding/asc2
  // @see http://ascii.911cha.com/
  if (code > 0x1f && code < 0x7f) {
    return String.fromCharCode(code);
  }
  if (preserveWrap) {
    // 换行符
    if (code === 0x0a) {
      return '\n';
    }
    // 归位符
    if (code === 0x0d) {
      return '\r';
    }
    // 制表符
    if (code === 0x09) {
      return '\t';
    }
  }
  return '.';
}

/**
 * HTML 符号转义
 * @param char 字符串
 * @returns
 */
export function htmlEscape(char: string) {
  switch (char) {
    case '&':
      return '&amp;';
    case '<':
      return '&lt;';
    case '>':
      return '&gt;';
    default:
      return char;
  }
}

interface IHexdumpProps {
  decodeData: IProtocolTree;
  highlights: IHexdumpHighlight[];
  loading: boolean;
}
const Hexdump: React.FC<IHexdumpProps> = ({ decodeData, highlights = [], loading }) => {
  // Hex or Nits
  const [base, setBase] = useState(16);
  const [activeTab, setActiveTab] = useState(0);

  const renderHexdump = useCallback(
    (hex: {
      activeTab: number;
      base: number;
      decodeTree: IProtocolTree;
      highlights: IHexdumpHighlight[];
    }) => {
      const { activeTab: tab, base: basePos, decodeTree, highlights: highlightCodes } = hex;

      const bytesData = [];
      const tabNames = [];

      bytesData.push(window.atob(decodeTree.bytes));
      tabNames.push(`Frame (${bytesData[0].length} bytes)`);

      /* multiple data sources? */
      if (decodeTree.ds) {
        for (let i = 0; i < decodeTree.ds.length; i += 1) {
          bytesData.push(window.atob(decodeTree.ds[i].bytes));
          tabNames.push(decodeTree.ds[i].name);
        }
      }

      let html = ''; // 组装好的HTML结构
      let lineContent = ''; // 每一行的内容

      const pkt = bytesData[tab] || '';

      let padcount = 0;
      let limit = 0;
      if (basePos === 2) {
        padcount = 8;
        limit = 8;
      }
      if (basePos === 16) {
        padcount = 2;
        limit = 16;
      }

      let emptypadded = '  ';
      while (emptypadded.length < padcount) {
        emptypadded += emptypadded;
      }

      if (limit === 0) {
        return {
          html,
          tabNames,
        };
      }

      const fullLimit = limit;

      for (let i = 0; i < pkt.length; i += fullLimit) {
        const strOff = `<span class='${styles.hexdumpOffset}'>${x2Hex(i, 4)}</span>`;
        let strHex = '';
        let strAscii = '';

        let prevClass = '';

        if (i + limit > pkt.length) {
          limit = pkt.length - i;
        }

        for (let j = 0; j < limit; j += 1) {
          const code = pkt.charCodeAt(i + j);

          let curClass = '';

          for (let k = 0; k < highlightCodes.length; k += 1) {
            if (
              highlightCodes[k].tab === tab &&
              highlightCodes[k].start <= i + j &&
              i + j <= highlightCodes[k].end &&
              highlightCodes[k].style === 'regular_match_offset'
            ) {
              curClass = styles.regular_match_offset;
              break;
            } else if (
              highlightCodes[k].tab === tab &&
              highlightCodes[k].start <= i + j &&
              i + j < highlightCodes[k].end
            ) {
              curClass = styles.selected;
              break;
            }
          }

          if (prevClass !== curClass) {
            if (prevClass !== '') {
              /* close span for previous class */
              strAscii += '</span>';
              strHex += '</span>';
            }

            if (curClass !== '') {
              /* open span for new class */
              strHex += `<span class='${curClass}'>`;
              strAscii += `<span class='${curClass}'>`;
            }

            prevClass = curClass;
          }

          strAscii += htmlEscape(ascii2Char(code));

          let numpad = code.toString(basePos);
          while (numpad.length < padcount) {
            numpad = `0${numpad}`;
          }

          strHex += `${numpad} `;
        }

        if (prevClass !== '') {
          strAscii += '</span>';
          strHex += '</span>';
        }

        for (let j = limit; j < fullLimit; j += 1) {
          strHex += `${emptypadded} `;
          strAscii += ' ';
        }

        lineContent = `${strOff} ${strHex} ${strAscii}\n`;

        html += lineContent;
      }

      return {
        html,
        tabNames,
      };
    },
    [],
  );

  const handleChangeDisplayBase = (e: RadioChangeEvent) => {
    setBase(e.target.value);
  };

  const handleChangeTab = (e: RadioChangeEvent) => {
    setActiveTab(e.target.value);
  };

  const { html, tabNames } = useMemo(() => {
    if (!decodeData.bytes) {
      return { html: '', tabNames: [] };
    } else {
      const highLightList = [...highlights];
      const { comment } = decodeData as any;
      if (comment) {
        const { regularMatchOffset } = JSON.parse(comment || '{}');
        highLightList.push(
          ...regularMatchOffset.map(({ begin, end }: { begin: number; end: number }) => {
            return {
              tab: 0,
              start: begin,
              end: end,
              style: 'regular_match_offset',
            };
          }),
        );
      }
      return renderHexdump({ activeTab, base, decodeTree: decodeData, highlights: highLightList });
    }
  }, [activeTab, base, decodeData, highlights, renderHexdump]);

  useEffect(() => {
    if (tabNames.length - 1 < activeTab) {
      setActiveTab(0);
    }
  }, [activeTab, tabNames]);

  if (!decodeData.bytes) {
    return (
      <div className={styles.hexdumpWrap}>
        <p className={styles.emptyText}>选择数据包</p>
      </div>
    );
  }

  return (
    <Fragment>
      {loading ? (
        <Spin />
      ) : (
        <div className={styles.hexdumpWrap}>
          <div className={styles.tabWrap}>
            <Radio.Group
              value={activeTab}
              size="small"
              buttonStyle="solid"
              onChange={handleChangeTab}
            >
              {tabNames.map((tab, index) => (
                // eslint-disable-next-line react/no-array-index-key
                <Radio.Button value={index} key={`${tab}-${index}`}>
                  {tab}
                </Radio.Button>
              ))}
            </Radio.Group>
          </div>
          <div className={styles.displayBase}>
            <Radio.Group
              value={base}
              size="small"
              buttonStyle="solid"
              onChange={handleChangeDisplayBase}
            >
              <Radio.Button value={16}>Hex</Radio.Button>
              <Radio.Button value={2}>Bits</Radio.Button>
            </Radio.Group>
          </div>
          <div className={styles.asciiPanel} dangerouslySetInnerHTML={{ __html: html }} />
        </div>
      )}
    </Fragment>
  );
};

export default Hexdump;
