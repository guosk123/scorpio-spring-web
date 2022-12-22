package com.machloop.fpc.manager.global.dao;

import java.util.List;

import org.elasticsearch.index.query.QueryBuilder;

/**
 * @author liyongjun
 *
 * create at 2019年9月27日, fpc-manager
 */
public interface ElasticIndexDao {

  long countDocument(String indexName, QueryBuilder query);

  int saveDocument(String indexName, String document);

  int batchSaveDocument(String indexName, List<String> documents);

  int deleteDocumentSilence(String indexName, QueryBuilder query);

  int addAlias(String indexName, String alias);

}
