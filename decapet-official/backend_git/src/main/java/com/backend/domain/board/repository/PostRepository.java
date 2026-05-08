package com.backend.domain.board.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.board.entity.Post;
import com.backend.domain.board.entity.PostType;
import com.backend.domain.board.exception.PostNotFoundException;

public interface PostRepository extends JpaRepository<Post, String> {

    List<Post> findByUserId(String userId);

    @EntityGraph(attributePaths = {"user"})
    Page<Post> findByType(PostType type, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String title, String content, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Page<Post> findByTypeAndTitleContainingIgnoreCaseOrTypeAndContentContainingIgnoreCase(
            PostType type1, String title, PostType type2, String content, Pageable pageable);

    default Post findByIdOrThrow(String id) {
        return findById(id)
                .orElseThrow(PostNotFoundException::new);
    }

    @Query(value = "SELECT * FROM posts WHERE user_id = :userId", nativeQuery = true)
    List<Post> findAllByUserIdIncludingDeleted(@Param("userId") String userId);
}
