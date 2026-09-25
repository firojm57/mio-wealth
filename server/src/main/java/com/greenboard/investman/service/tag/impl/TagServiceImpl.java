package com.greenboard.investman.service.tag.impl;

import com.greenboard.investman.model.tag.Tag;
import com.greenboard.investman.repository.tag.TagRepository;
import com.greenboard.investman.service.tag.TagService;
import com.greenboard.investman.vo.tag.TagRequestVO;
import com.greenboard.investman.vo.tag.TagVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    private static final Logger log = LoggerFactory.getLogger(TagServiceImpl.class);

    private final TagRepository tagRepository;

    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagVO> getTags(String domain) {
        String targetDomain = StringUtils.isNotBlank(domain)
                ? domain.trim().toUpperCase()
                : "INVESTMENT";

        List<Tag> tags = tagRepository.findAllByDomainOrderByNameAsc(targetDomain);
        if (CollectionUtils.isEmpty(tags)) {
            return Collections.emptyList();
        }

        return tags.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TagVO createTag(TagRequestVO request) {
        String name = request != null && request.getName() != null ? request.getName().trim() : "";
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Tag name cannot be empty");
        }

        String domain = (request != null && StringUtils.isNotBlank(request.getDomain()))
                ? request.getDomain().trim().toUpperCase()
                : "INVESTMENT";

        return tagRepository.findByNameIgnoreCase(name)
                .map(this::toVO)
                .orElseGet(() -> {
                    Tag tag = Tag.builder()
                            .name(name)
                            .domain(domain)
                            .isSystem(false)
                            .build();
                    Tag saved = tagRepository.save(tag);
                    log.info("Created custom tag '{}' for domain {}", saved.getName(), saved.getDomain());
                    return toVO(saved);
                });
    }

    @Override
    @Transactional
    public void ensureTagsExist(String tagsString, String domain) {
        if (StringUtils.isBlank(tagsString)) {
            return;
        }

        String targetDomain = StringUtils.isNotBlank(domain)
                ? domain.trim().toUpperCase()
                : "INVESTMENT";

        Arrays.stream(tagsString.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .forEach(tagName -> {
                    if (!tagRepository.existsByNameIgnoreCase(tagName)) {
                        Tag newTag = Tag.builder()
                                .name(tagName)
                                .domain(targetDomain)
                                .isSystem(false)
                                .build();
                        tagRepository.save(newTag);
                        log.info("Auto-registered tag '{}' for domain {}", tagName, targetDomain);
                    }
                });
    }

    private TagVO toVO(Tag tag) {
        return TagVO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .domain(tag.getDomain())
                .isSystem(tag.isSystem())
                .build();
    }
}
