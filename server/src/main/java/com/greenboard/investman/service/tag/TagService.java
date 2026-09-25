package com.greenboard.investman.service.tag;

import com.greenboard.investman.vo.tag.TagRequestVO;
import com.greenboard.investman.vo.tag.TagVO;

import java.util.List;

public interface TagService {

    List<TagVO> getTags(String domain);

    TagVO createTag(TagRequestVO request);

    void ensureTagsExist(String tagsString, String domain);
}
