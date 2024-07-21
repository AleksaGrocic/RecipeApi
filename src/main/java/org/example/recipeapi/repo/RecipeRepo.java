package org.example.recipeapi.repo;

import org.example.recipeapi.domain.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecipeRepo extends JpaRepository<Recipe, String> {
    Optional<Recipe> findById(String id);
    void deleteById(String id);
}
