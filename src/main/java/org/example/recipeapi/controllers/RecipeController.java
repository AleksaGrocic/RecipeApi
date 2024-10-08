package org.example.recipeapi.controllers;

import lombok.RequiredArgsConstructor;
import org.example.recipeapi.domain.Recipe;
import org.example.recipeapi.services.RecipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.example.recipeapi.constants.Constant.IMAGE_DIRECTORY;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@RestController
@RequestMapping("/recipes")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;

    @GetMapping
    public ResponseEntity<List<Recipe>> getAllRecipes() {
        return ResponseEntity.ok().body(recipeService.getAllRecipes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getRecipe(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok().body(recipeService.getRecipe(id));
    }

    @GetMapping(path = "/image/{filename}", produces = { IMAGE_PNG_VALUE, IMAGE_JPEG_VALUE })
    public byte[] getImage(@PathVariable("filename") String filename) throws IOException {
        return Files.readAllBytes(Paths.get(IMAGE_DIRECTORY + filename));
    }

    @PostMapping
    public ResponseEntity<Recipe> createRecipe(@RequestBody Recipe recipe) {
        return ResponseEntity.created(URI.create("/recipes/ID"))
                .body(recipeService.createRecipe(recipe));
    }

    @PutMapping("/image")
    public ResponseEntity<String> uploadImage(
            @RequestParam("id") String id,
            @RequestParam("file") MultipartFile file)
    {
        return ResponseEntity.ok().body(recipeService.uploadImage(id, file));
    }

    @DeleteMapping("/{id}")
    public void deleteRecipe(@PathVariable(value = "id") String id) {
        recipeService.deleteRecipe(id);
    }
}
