package org.example.recipeapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.recipeapi.domain.Recipe;
import org.example.recipeapi.repo.RecipeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.example.recipeapi.constants.Constant.IMAGE_DIRECTORY;

@Service
@Slf4j
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class RecipeService {
    private final RecipeRepository recipeRepository;

    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll(Sort.by("name"));
    }

    public Recipe getRecipe(String id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found."));
    }

    public Recipe createRecipe(Recipe recipe) {
        return recipeRepository.save(recipe);
    }

    private void deleteImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                Path imagePath = Paths.get(
                        IMAGE_DIRECTORY, imageUrl.substring(
                                imageUrl.lastIndexOf('/') + 1
                        ));
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                log.error("Failed to delete image file: {}", e.getMessage());
            }
        }
    }

    public void deleteRecipe(String id) {
        Recipe recipe = getRecipe(id);
        recipeRepository.deleteById(id);
        deleteImage(recipe.getImageUrl());
    }

    private final Function<String, String> fileExtension = filename -> Optional.of(filename).filter(name -> name.contains(".")).map(name -> "." + name.substring(filename.lastIndexOf(".") + 1)).orElse(".png");

    private final BiFunction<String, MultipartFile, String> imageFunction = (id, image) -> {
        String filename = id + fileExtension.apply(image.getOriginalFilename());
        try {
            Path fileStorageLocation = Paths.get(IMAGE_DIRECTORY).toAbsolutePath().normalize();
            if(!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            }
            Files.copy(image.getInputStream(), fileStorageLocation.resolve(filename), REPLACE_EXISTING);
            return ServletUriComponentsBuilder.fromCurrentContextPath().path("/recipes/image/" + id + fileExtension.apply(image.getOriginalFilename())).toUriString();
        } catch (Exception exception) {
            throw new RuntimeException("Unable to save image.");
        }
    };

    public String uploadImage(String id, MultipartFile file) {
        Recipe recipe = getRecipe(id);
        String imageUrl = imageFunction.apply(id, file);
        recipe.setImageUrl(imageUrl);
        recipeRepository.save(recipe);
        return imageUrl;
    }
}

