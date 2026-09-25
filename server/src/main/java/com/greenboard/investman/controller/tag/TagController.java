package com.greenboard.investman.controller.tag;

import com.greenboard.investman.service.tag.TagService;
import com.greenboard.investman.vo.tag.TagRequestVO;
import com.greenboard.investman.vo.tag.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@Tag(name = "Tag Management", description = "Endpoints for managing financial tags per tenant")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    @Operation(summary = "Get tags for a domain (default: INVESTMENT)")
    public ResponseEntity<List<TagVO>> getTags(@RequestParam(required = false, defaultValue = "INVESTMENT") String domain) {
        List<TagVO> tags = tagService.getTags(domain);
        return ResponseEntity.ok(tags);
    }

    @PostMapping
    @Operation(summary = "Create custom tag")
    public ResponseEntity<TagVO> createTag(@Valid @RequestBody TagRequestVO request) {
        TagVO created = tagService.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
