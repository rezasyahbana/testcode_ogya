package org.tk.sda.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.tk.sda.config.mybatis.entity.VoltageCustomFpeConfig;
import org.tk.sda.service.VoltageCustomFpeConfigService;

import java.util.List;

@RestController
@RequestMapping(value = "/voltage-fpe-config")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
public class VoltageCustomFpeConfigController {

    @Autowired
    VoltageCustomFpeConfigService voltageCustomFpeConfigService;

    @GetMapping(path = "/get-all")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<VoltageCustomFpeConfig>> getAllVoltageFpeConfig(){
        List<VoltageCustomFpeConfig> result = voltageCustomFpeConfigService.getAllVoltageCustomFpeConfig();
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping(path = "/set")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> setVoltageFpeConfig(@RequestBody List<VoltageCustomFpeConfig> body){
        voltageCustomFpeConfigService.insertVoltageCustomFpeConfig(body);
        return ResponseEntity.ok("Success: inserted to database.");
    }

    @PutMapping(path = "/put")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> putVoltageFpeConfig(@RequestBody VoltageCustomFpeConfig body){
        voltageCustomFpeConfigService.updateVoltageCustomFpeConfig(body);
        return ResponseEntity.ok("Success: updated to database.");
    }

    @DeleteMapping(path = "/delete")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> deleteVoltageFpeConfig(@RequestParam String id){
        voltageCustomFpeConfigService.deleteVoltageCustomFpeConfig(id);
        return ResponseEntity.ok("Success: deleted from database.");
    }

}
