package com.machloop.fpc.manager.global.dao.impl;

import java.io.IOException;
import java.util.List;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest.AliasActions;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.machloop.fpc.manager.global.dao.ElasticIndexDao;

/**
 * @author liyongjun
 *
 * create at 2019年9月27日, fpc-manager
 */
@Repository
public class ElasticIndexDaoImpl implements ElasticIndexDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(ElasticIndexDaoImpl.class);

  @Autowired
  private RestHighLevelClient restHighLevelClient;

  /**
   * @see com.machloop.fpc.manager.global.dao.ElasticIndexDao#countDocument(java.lang.String, org.elasticsearch.index.query.QueryBuilder)
   */
  @Override
  public long countDocument(String indexName, QueryBuilder query) {
    long count = 0L;
    try {
      CountRequest countRequest = new CountRequest(indexName);
      countRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);

      if (query != null) {
        countRequest.query(query);
      }
      CountResponse countResponse = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
      count = countResponse.getCount();
    } catch (IOException e) {
      LOGGER.warn("failed to count document, indexName: " + indexName + e);
    }
    return count;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.ElasticIndexDao#saveDocument(java.lang.String, java.lang.String)
   */
  @Override
  public int saveDocument(String indexName, String document) {

    int count = 0;

    Result indexResult = Result.CREATED;

    // 写入ES
    IndexRequest request = new IndexRequest(indexName).source(document, XContentType.JSON);

    try {
      IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
      indexResult = response.getResult();

      if (indexResult != Result.CREATED) {
        LOGGER.info("save document:{} result:{}, id:{}, shardInfo:{}, content:{}.", indexName,
            indexResult, response.getId(), response.getShardInfo(), document);
      } else {
        LOGGER.debug("save document:{} result:{}, id:{}, shardInfo:{}, content:{}.", indexName,
            indexResult, response.getId(), response.getShardInfo(), document);

        count++;
      }
    } catch (IOException e) {
      LOGGER.warn("failed to save document:" + indexName + " content:" + document, e);
    }

    return count;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.ElasticIndexDao#batchSaveDocument(java.lang.String, java.util.List)
   */
  @Override
  public int batchSaveDocument(String indexName, List<String> documents) {
    int batchSize = 5000;

    int successCount = 0;
    try {
      BulkRequest bulkRequest = new BulkRequest(indexName);
      for (String document : documents) {
        IndexRequest request = new IndexRequest().source(document, XContentType.JSON);
        bulkRequest.add(request);

        if (bulkRequest.numberOfActions() % batchSize == 0) {
          BulkResponse response = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
          if (response.hasFailures()) {
            LOGGER.warn("has failures when batch save document, failure message: {}",
                response.buildFailureMessage());
            for (BulkItemResponse itemResponse : response.getItems()) {
              if (itemResponse.isFailed()) {
                LOGGER.debug("failed to batch save document, failure message: [{}]",
                    itemResponse.getFailureMessage());
              } else {
                successCount += 1;
              }
            }
          } else {
            successCount += bulkRequest.numberOfActions();
          }

          bulkRequest = new BulkRequest(indexName);
        }
      }
      if (bulkRequest.numberOfActions() > 0) {
        BulkResponse response = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        if (response.hasFailures()) {
          LOGGER.warn("has failures when batch save document, failure message: {}",
              response.buildFailureMessage());
          for (BulkItemResponse itemResponse : response.getItems()) {
            if (itemResponse.isFailed()) {
              LOGGER.debug("failed to batch save document, failure message: [{}]",
                  itemResponse.getFailureMessage());
            } else {
              successCount += 1;
            }
          }
        } else {
          successCount += bulkRequest.numberOfActions();
        }
      }

      LOGGER.debug(
          "success to batch save documents, index: [{}], document count: [{}], success count: [{}].",
          indexName, documents.size(), successCount);
    } catch (IOException e) {
      LOGGER.warn(
          "failed to batch save document:" + indexName + " document count:" + documents.size(), e);
    }
    return successCount;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.ElasticIndexDao#deleteDocument(java.lang.String, org.elasticsearch.index.query.QueryBuilder)
   */
  @Override
  public int deleteDocumentSilence(String indexName, QueryBuilder query) {
    int total = 0;
    DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(indexName);
    deleteByQueryRequest.setQuery(query);
    try {
      BulkByScrollResponse deleteResponse = restHighLevelClient.deleteByQuery(deleteByQueryRequest,
          RequestOptions.DEFAULT);
      total = (int) deleteResponse.getTotal();
    } catch (IOException e) {
      LOGGER.warn("failed to delete document, index: [{}], query: [{}]", indexName, query);
    }
    return total;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.ElasticIndexDao#addAlias(java.lang.String, java.lang.String)
   */
  @Override
  public int addAlias(String indexName, String alias) {
    int success = 0;

    IndicesAliasesRequest request = new IndicesAliasesRequest();

    AliasActions aliasAction = new AliasActions(AliasActions.Type.ADD).index(indexName)
        .alias(alias);
    request.addAliasAction(aliasAction);
    try {
      AcknowledgedResponse indicesAliasesResponse = restHighLevelClient.indices()
          .updateAliases(request, RequestOptions.DEFAULT);
      if (indicesAliasesResponse.isAcknowledged()) {
        success = 1;
      }
    } catch (ElasticsearchStatusException ese) {
      if (ese.status() == RestStatus.NOT_FOUND) {
        LOGGER.debug("index not fount, {}", indexName);
      } else {
        LOGGER.warn("failed to add alias, {}", ese);
      }
    } catch (IOException e) {
      LOGGER.warn("failed to add alias, indexName is {}, alias is {}.", indexName, alias);
    }

    return success;
  }
}
