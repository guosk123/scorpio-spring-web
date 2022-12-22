import { CaretDownOutlined, CaretUpOutlined, StepBackwardOutlined } from '@ant-design/icons';
import { Col, Divider, Input, Row, Spin, Tooltip } from 'antd';
import cls from 'classnames';
import debounce from 'lodash/debounce';
import type { ReactNode } from 'react';
import React, { forwardRef, Fragment, useEffect, useRef, useState } from 'react';
import type { ITerminalOptions } from 'xterm';
import { Terminal as XTerminal } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';
import { SearchAddon } from 'xterm-addon-search';
import { WebLinksAddon } from 'xterm-addon-web-links';
import 'xterm/css/xterm.css';
import styles from './index.less';

export type TerminalType = XTerminal;

interface ITerminalProps {
  /** Terminal title */
  title?: ReactNode;
  className?: string;
  terminalClassName?: string;
  /** defaultValue in Terminal */
  defaultValue?: string;
  /** terminal init event */
  onInit?: (ins: XTerminal, fitAddon: any) => void;
  /** https://xtermjs.org/docs/api/terminal/interfaces/iterminaloptions/ */
  config?: ITerminalOptions;
  onResize?: (ins: XTerminal) => void;
  /** terminal close */
  onClose?: () => void;
  [key: string]: any;
}
const TerminalComponent: React.FC<ITerminalProps> = forwardRef((props = {}, ref) => {
  const fitAddon = new FitAddon();
  const webLinksAddon = new WebLinksAddon();
  const searchAddon = new SearchAddon();

  // @ts-ignore
  const domContainer = useRef<HTMLDivElement>(ref || null);
  // @ts-ignore
  const [xterm, setXterm] = useState<XTerminal>(null);

  const [keyword, setKeyword] = useState<string | undefined>('');

  const {
    title,
    className,
    defaultValue,
    onInit,
    config = {},
    terminalClassName,
    onResize = () => {},
    onClose = () => {},
    // default use true
    visible = true,
    toolbar = true,
  } = props;

  useEffect(() => {
    const terminalOpts: ITerminalOptions = {
      allowTransparency: true,
      fontFamily: 'operator mono,SFMono-Regular,Consolas,Liberation Mono,Menlo,monospace',
      fontSize: 14,
      rows: 30,
      theme: {
        background: '#15171C',
        foreground: '#ffffff73',
      },
      cursorStyle: 'block',
      // 光标闪烁
      cursorBlink: true,
      // 是否应禁用输入
      disableStdin: true,
      ...(config || {}),
    };
    const terminal = new XTerminal(terminalOpts);
    setXterm(terminal);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const closeShortcut = (e: KeyboardEvent): boolean => {
    // Ctrl + D
    if (e.ctrlKey && e.keyCode === 68) {
      e.preventDefault();
      onClose();
      return false;
    }
    return true;
  };

  const findNext = () => {
    if (keyword) {
      searchAddon.activate(xterm);
      searchAddon.findNext(keyword);
    }
  };

  useEffect(() => {
    const handleTerminalInit = () => {
      if (domContainer.current && xterm) {
        xterm.loadAddon(fitAddon);
        xterm.loadAddon(webLinksAddon);
        xterm.loadAddon(searchAddon);
        // xterm.attachCustomKeyEventHandler(copyShortcut);
        xterm.attachCustomKeyEventHandler(closeShortcut);
        // last open
        xterm.open(domContainer.current);
        fitAddon.fit();
        if (onInit) {
          onInit(xterm, fitAddon);
        }
      }
    };
    handleTerminalInit();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [domContainer, xterm]);

  useEffect(() => {
    const hanldeResizeTerminal = debounce(() => {
      fitAddon.fit();
      onResize?.(xterm);
      xterm?.focus?.();
    }, 380);
    if (visible) {
      window.addEventListener('resize', hanldeResizeTerminal);
    }
    return () => {
      window.removeEventListener('resize', hanldeResizeTerminal);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [xterm, visible]);

  useEffect(() => {
    if (defaultValue) {
      xterm?.write?.(defaultValue.replace(/\n/g, '\r\n'));
    }
  }, [xterm, defaultValue]);

  useEffect(() => {
    if (keyword) {
      findNext();
    } else {
      searchAddon.dispose();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [keyword]);

  // === 滚动 S ===
  const toBottom = () => {
    xterm?.scrollToBottom?.();
  };
  const toTop = () => {
    xterm?.scrollToTop?.();
  };
  // === 滚动 E ===

  const handleEnterSearch = (e: React.KeyboardEvent<HTMLInputElement>) => {
    const newKeyword = e.currentTarget.value;
    if (newKeyword !== keyword) {
      setKeyword(newKeyword);
    } else {
      findNext();
    }
  };

  const findPrevious = () => {
    if (keyword) {
      searchAddon.activate(xterm);
      searchAddon.findPrevious(keyword);
    }
  };
  // === 搜素 E ===

  const wrapperCls = cls(
    styles.terminalWrapper,
    {
      [styles.toolbar]: !!toolbar,
    },
    className,
  );
  const terminalCls = cls(styles.logContainer, terminalClassName);

  return (
    <div className={wrapperCls}>
      {xterm ? (
        <>
          {toolbar && (
            <Row className={styles.titleWrapper}>
              <Col span={8} className={styles.formmatGroup}>
                {title}
              </Col>
              <Col span={16} className={styles.actionGroup}>
                <Input
                  className={styles.search}
                  placeholder="关键字搜索"
                  size="small"
                  style={{ width: 200 }}
                  allowClear
                  onChange={(e) => {
                    if (!e.target.value) {
                      setKeyword(undefined);
                    }
                  }}
                  onPressEnter={handleEnterSearch}
                  addonAfter={
                    <Fragment>
                      <span>
                        <Tooltip title="上一个">
                          <CaretUpOutlined onClick={findPrevious} />
                        </Tooltip>
                      </span>
                      <Divider type="vertical" />
                      <span>
                        <Tooltip title="下一个">
                          <CaretDownOutlined onClick={findNext} />
                        </Tooltip>
                      </span>
                    </Fragment>
                  }
                />
                <span className={styles.icon}>
                  <Tooltip title="至顶部">
                    <StepBackwardOutlined style={{ transform: 'rotate(90deg)' }} onClick={toTop} />
                  </Tooltip>
                </span>
                <span className={styles.icon}>
                  <Tooltip title="至底部">
                    <StepBackwardOutlined
                      type="step-backward"
                      style={{ transform: 'rotate(-90deg)' }}
                      onClick={toBottom}
                    />
                  </Tooltip>
                </span>
              </Col>
            </Row>
          )}
        </>
      ) : (
        <div style={{ textAlign: 'center' }}>
          <Spin size="small" />
        </div>
      )}
      <div ref={domContainer} className={terminalCls} />
    </div>
  );
});

export default TerminalComponent;
