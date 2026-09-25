package com.greenboard.investman.repository.tag;

import com.greenboard.investman.model.tag.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, String> {

    List<Tag> findAllByDomainOrderByNameAsc(String domain);

    Optional<Tag> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
