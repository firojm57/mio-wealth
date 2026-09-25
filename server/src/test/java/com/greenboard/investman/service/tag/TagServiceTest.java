package com.greenboard.investman.service.tag;

import com.greenboard.investman.model.tag.Tag;
import com.greenboard.investman.repository.tag.TagRepository;
import com.greenboard.investman.service.tag.impl.TagServiceImpl;
import com.greenboard.investman.vo.tag.TagRequestVO;
import com.greenboard.investman.vo.tag.TagVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    private TagService tagService;

    @BeforeEach
    void setUp() {
        tagService = new TagServiceImpl(tagRepository);
    }

    @Test
    void testGetTagsReturnsSortedDomainTags() {
        when(tagRepository.findAllByDomainOrderByNameAsc("INVESTMENT"))
                .thenReturn(List.of(
                        Tag.builder().id("1").name("Dividend").domain("INVESTMENT").isSystem(true).build(),
                        Tag.builder().id("2").name("Growth").domain("INVESTMENT").isSystem(false).build()
                ));

        List<TagVO> tags = tagService.getTags("INVESTMENT");

        assertEquals(2, tags.size());
        assertEquals("Dividend", tags.get(0).getName());
        assertEquals("Growth", tags.get(1).getName());
    }

    @Test
    void testCreateTagSavesNewTag() {
        when(tagRepository.findByNameIgnoreCase("High-Yield")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> {
            Tag t = inv.getArgument(0);
            t.setId("new-id");
            return t;
        });

        TagVO created = tagService.createTag(TagRequestVO.builder().name("High-Yield").domain("INVESTMENT").build());

        assertNotNull(created);
        assertEquals("High-Yield", created.getName());
        assertEquals("INVESTMENT", created.getDomain());
        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    @Test
    void testEnsureTagsExistParsesAndSavesOnlyNonExistingTags() {
        when(tagRepository.existsByNameIgnoreCase("Existing")).thenReturn(true);
        when(tagRepository.existsByNameIgnoreCase("BrandNew")).thenReturn(false);

        tagService.ensureTagsExist("Existing, BrandNew,  ", "INVESTMENT");

        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository, times(1)).save(captor.capture());
        assertEquals("BrandNew", captor.getValue().getName());
        assertEquals("INVESTMENT", captor.getValue().getDomain());
    }
}
