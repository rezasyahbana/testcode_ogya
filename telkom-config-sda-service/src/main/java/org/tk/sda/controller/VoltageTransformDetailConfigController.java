package org.tk.sda.controller;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.tk.sda.config.mybatis.entity.VoltageTransformDetailConfig;
import org.tk.sda.service.VoltageTransformDetailConfigService;

import java.util.List;

@RestController
@RequestMapping(value = "/voltage-transform-detail-config")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
public class VoltageTransformDetailConfigController {

    @Autowired
    VoltageTransformDetailConfigService voltageTransformDetailConfigService;

    @GetMapping(path = "/get-all")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<VoltageTransformDetailConfig>> getAllVoltageTransformDetailConfig(){
        List<VoltageTransformDetailConfig> result = voltageTransformDetailConfigService.getAllVoltageTransformDetailConfig();
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping(path = "/get-by-id")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<VoltageTransformDetailConfig>> getVoltageTransformDetailConfigById(@RequestParam String id){
        List<VoltageTransformDetailConfig> result =  voltageTransformDetailConfigService.getVoltageTransformDetailConfigById(id);
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping(path = "/set")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String>  setVoltageTransformDetailConfig(@RequestBody List<VoltageTransformDetailConfig> body) {
        voltageTransformDetailConfigService.insertVoltageTransformDetailConfig(body);
        return ResponseEntity.ok("Success: inserted to database.");
    }

    @PutMapping(path = "/put")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> putVoltageVoltageTransformDetailConfig(@RequestBody VoltageTransformDetailConfig body){
        voltageTransformDetailConfigService.updateVoltageTransformDetailConfig(body);
        return ResponseEntity.ok("Success: updated to database.");
    }

    @DeleteMapping(path = "/delete-by-id")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> deleteVoltageTransformDetailConfig(@RequestParam String id){
        voltageTransformDetailConfigService.deleteVoltageTransformDetailConfigById(id);
        return ResponseEntity.ok("Success: deleted from database.");
    }

    @DeleteMapping(path = "/delete-by-id-and-json-name")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> deleteVoltageTransformDetailConfigByIdAndName(@RequestParam String id,@RequestParam String jsonPathName){
        voltageTransformDetailConfigService.deleteVoltageTransformDetailConfigByIdAndName(id,jsonPathName);
        return ResponseEntity.ok("Success: deleted from database.");
    }
}
