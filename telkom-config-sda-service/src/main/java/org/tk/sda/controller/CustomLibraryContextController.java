package org.tk.sda.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.tk.sda.config.mybatis.entity.VoltageCustomLibraryContextConfig;
import org.tk.sda.service.CustomLibraryContextService;

import java.util.List;

@RestController
@RequestMapping(value = "/voltage-library-context-config")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
public class CustomLibraryContextController {
    @Autowired
    CustomLibraryContextService customLibraryContextService;

    @GetMapping(path = "/get-all")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<VoltageCustomLibraryContextConfig>> getAllVoltageCustomLibraryContextConfig(){
        List<VoltageCustomLibraryContextConfig> result = customLibraryContextService.getAllVoltageCustomLibraryContextConfig();
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping(path = "/set")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> setVoltageCustomLibraryContextConfig(@RequestBody List<VoltageCustomLibraryContextConfig> body){
        customLibraryContextService.setVoltageCustomLibraryContextConfig(body);
        return ResponseEntity.ok("Success: inserted to database.");
    }

    @PutMapping(path = "/put")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> putVoltageCustomLibraryContextConfig(@RequestBody VoltageCustomLibraryContextConfig body){
        customLibraryContextService.updateVoltageCustomLibraryContextConfig(body);
        return ResponseEntity.ok("Success: Updated to database.");
    }

    @DeleteMapping(path = "/delete")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> deleteVoltageCustomLibraryContextConfig(@RequestParam String id){
        customLibraryContextService.deleteVoltageCustomLibraryContextConfig(id);
        return ResponseEntity.ok("Success: Deleted from database.");
    }
}
