package com.heqing.elasticsearch;

import com.alibaba.fastjson.JSON;
import com.heqing.elasticsearch.config.SpringEsConfig;
import com.heqing.elasticsearch.model.Poem;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = SpringEsConfig.class
)
public class TestPoem {

    @Autowired
    RestHighLevelClient highLevelClient;

    private static final String INDEX = "demo_poem";

    /**
     * ????????????????????????
     * @throws IOException
     */
    @Test
    public void getIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest(INDEX);
        boolean exists = highLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    /**
     * ??????index
     * @throws IOException
     */
    @Test
    public void testCreateIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(INDEX);
        CreateIndexResponse indexResponse = highLevelClient.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(indexResponse);
    }

    /**
     * ????????????
     * @throws IOException
     */
    @Test
    public void delIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("demo_item");
        AcknowledgedResponse response = highLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(response.isAcknowledged());
    }

    /**
     * ????????????
     * @throws IOException
     */
    @Test
    public void addDoc() throws IOException {
        IndexRequest request = new IndexRequest(INDEX);
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));
        request.source(JSON.toJSONString(new Poem("??????", "??????", "????????????????????????????????????????????????????????????????????????")), XContentType.JSON);
        IndexResponse indexResponse = highLevelClient.index(request, RequestOptions.DEFAULT);
        System.out.println(indexResponse);
        System.out.println(indexResponse.status());
    }

    /**
     * ??????????????????
     * @throws IOException
     */
    @Test
    public void addBatchDoc() throws IOException {
        BulkRequest request = new BulkRequest(INDEX);
        request.timeout(TimeValue.timeValueSeconds(10));
        List<Poem> list = new ArrayList<>();
        list.add(new Poem("????????????", "??????", "????????????????????????????????????????????????????????????????????????"));
        list.add(new Poem("??????", "??????", "????????????????????????????????????????????????????????????????????????"));
        list.add(new Poem("????????????????????", "??????", "????????????????????????????????????????????????????????????????????????"));
        list.add(new Poem("??????", "??????", "????????????????????????????????????????????????????????????????????????"));
        list.add(new Poem("??????????????????", "?????????", "????????????????????????????????????????????????????????????????????????????????????????????????"));
        list.add(new Poem("??????", "?????????", "????????????????????????????????????????????????????????????????????????"));
        for (int i = 0; i < list.size(); i++) {
            request.add(new IndexRequest(INDEX)
                    .id((i+2)+"")
                    .source(JSON.toJSONString(list.get(i)), XContentType.JSON)
            );
        }
        BulkResponse bulk = highLevelClient.bulk(request, RequestOptions.DEFAULT);
        System.out.println(bulk.status());
        System.out.println(bulk.hasFailures());
    }

    /**
     * ????????????????????????
     * @throws IOException
     */
    @Test
    public void chkDocExist() throws IOException {
        GetRequest request = new GetRequest(INDEX);
        request.id("1");
        boolean exists = highLevelClient.exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    /**
     * ????????????
     * @throws IOException
     */
    @Test
    public void getDoc() throws IOException {
        GetRequest request = new GetRequest(INDEX);
        request.id("1");
        GetResponse documentFields = highLevelClient.get(request, RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(documentFields.getSource()));
    }

    /**
     * ????????????
     * @throws IOException
     */
    @Test
    public void updateDoc() throws IOException {
        UpdateRequest request = new UpdateRequest(INDEX, "1");
        request.timeout(TimeValue.timeValueSeconds(1));
        Poem poem = new Poem("????????????", "?????????", "????????????????????????????????????????????????????????????????????????");
        request.doc(JSON.toJSONString(poem), XContentType.JSON);
        UpdateResponse updateResponse = highLevelClient.update(request, RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(updateResponse.status()));
        System.out.println(updateResponse.getGetResult());
    }

    /**
     * ????????????
     * @throws IOException
     */
    @Test
    public void delDoc() throws IOException {
        DeleteRequest request = new DeleteRequest(INDEX, "1");
        DeleteResponse deleteResponse = highLevelClient.delete(request, RequestOptions.DEFAULT);
        System.out.println(deleteResponse.status());
    }

    /**
     * ??????
     * @throws IOException
     */
    @Test
    public void search() throws IOException {
        SearchRequest request = new SearchRequest(INDEX);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("content", "??????");
        SearchSourceBuilder query = sourceBuilder.query(matchQueryBuilder);
        request.source(query);
        SearchResponse search = highLevelClient.search(request, RequestOptions.DEFAULT);
        System.out.println(search.status());
        System.out.println(JSON.toJSONString(search));
    }

    /**
     * ??????-?????????????????????
     * @throws IOException
     */
    @Test
    public void test1()  throws IOException {
        SearchRequest searchRequest = new SearchRequest(INDEX);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        MatchQueryBuilder termQueryBuilder = QueryBuilders.matchQuery("content", "??????");
        SearchSourceBuilder query = sourceBuilder.query(termQueryBuilder);
        searchRequest.source(query);
        // ??????
        sourceBuilder.from(0);
        sourceBuilder.size(10);

        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .requireFieldMatch(false)
                .field("content")
                .preTags("<span style='color: red'>")
                .postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        SearchResponse searchResponse = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        List<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField content = highlightFields.get("content");
            if (content != null) {
                Text[] fragments = content.getFragments();
                String newCon = "";
                for (Text text : fragments) {
                    newCon += text;
                }
                sourceAsMap.put("content", newCon);
            }
            list.add(sourceAsMap);
        }

        System.out.println(JSON.toJSONString(list));
    }
}
