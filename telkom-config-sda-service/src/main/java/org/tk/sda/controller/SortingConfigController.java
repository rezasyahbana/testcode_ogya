package org.tk.sda.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.tk.sda.config.mybatis.entity.SortingConfig;
import org.tk.sda.service.SortingConfigService;

import java.util.List;

@RestController
@RequestMapping(value = "/voltage-sorting-config")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
public class SortingConfigController {

    @Autowired
    SortingConfigService sortingConfigService;

    @GetMapping(path = "/get-all")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<SortingConfig>> getAllSortingConfig(){
        List<SortingConfig> result = sortingConfigService.getAllSortingConfig();
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping(path = "/get-by-id")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<SortingConfig> getSortingConfigById(@RequestParam String id){
        SortingConfig result =  sortingConfigService.getSortingConfigById(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping(path = "/set")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> setSortingConfig(@RequestBody List<SortingConfig> body){
        sortingConfigService.setSortingConfig(body);
        return ResponseEntity.ok("Success: inserted to database.");
    }

    @PutMapping(path = "/put")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> updateSortingConfig(@RequestBody SortingConfig body){
        sortingConfigService.updateSortingConfig(body);
        return ResponseEntity.ok("Success: updated to database.");
    }

    @DeleteMapping(path = "/delete-by-id")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> deleteSortingConfig(@RequestParam String id){
        sortingConfigService.deleteSortingConfig(id);
        return ResponseEntity.ok("Success: deleted from database.");
    }

}
