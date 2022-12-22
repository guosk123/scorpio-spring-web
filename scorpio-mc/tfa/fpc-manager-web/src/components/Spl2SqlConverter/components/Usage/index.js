import 'github-markdown-css';
import React from 'react';
import ReactMarkdown from 'react-markdown';
import fullUsageMarkdownFile from './usage.md';
import slimUsageMarkdownFile from './slimUsage.md';

export default ({ onlyFilter = false }) => {
  const file = onlyFilter ? slimUsageMarkdownFile : fullUsageMarkdownFile;
  return <ReactMarkdown className="markdown-body" source={file} escapeHtml={false} />;
};
