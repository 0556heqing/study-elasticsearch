package com.heqing.elasticsearch.repository;

import com.heqing.elasticsearch.model.Item;
import org.springframework.data.elasticsearch.annotations.Highlight;
import org.springframework.data.elasticsearch.annotations.HighlightField;
import org.springframework.data.elasticsearch.annotations.HighlightParameters;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author heqing
 */
@Repository public interface ItemRepository extends ElasticsearchRepository<Item, String> {

    /**
     * 根据名字模糊查询
     * @param name
     * @return
     */
    List<Item> findByNameLike(String name);

    /**
     * 根据关键字查询
     * @param keywords
     * @return
     */
    @Query("{\"bool\" : {\"must\" : {\"match\" : {\"key-words\" : \"?0\"}}}}")
    List<Item> selectByKeywords(String keywords);

    /**
     * 根据名字高亮查询
     * @param name
     * @return
     */
    @Highlight(
            fields = @HighlightField(name = "name"),
            parameters = @HighlightParameters(
                    preTags = "<strong>",
                    postTags = "</strong>"
            )
    )
    List<SearchHit<Item>> findByName(String name);
}