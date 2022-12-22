import 'github-markdown-css';
import React, { Fragment, useEffect, useState } from 'react';
import ReactMarkdown from 'react-markdown';
import slimUsageMarkdownFile from './slimUsage.md';
import usageMarkdownFile from './usage.md';

export default ({ onlyFilter = false }) => {
  const [usageMardown, setUsageMarkdown] = useState('');

  const file = onlyFilter ? slimUsageMarkdownFile : usageMarkdownFile;

  useEffect(() => {
    fetch(file)
      .then((res) => res.text())
      .then((text) => setUsageMarkdown(text));
  }, []);

  return (
    <Fragment>
      <ReactMarkdown className="markdown-body" source={usageMardown} escapeHtml={false} />
    </Fragment>
  );
};
