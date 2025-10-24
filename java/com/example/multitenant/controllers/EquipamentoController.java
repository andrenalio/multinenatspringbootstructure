package com.example.multitenant.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.multitenant.config.TenantContext;
import com.example.multitenant.tenantmodel.Equipamento;
import com.example.multitenant.tenantrepo.EquipamentoRepository;

@RestController
@RequestMapping("/api/equipamentos")
public class EquipamentoController {

	 private final EquipamentoRepository repository;

	    public EquipamentoController(EquipamentoRepository repository) {
	        this.repository = repository;
	    }

	    @GetMapping
	    public ResponseEntity<Object> listAll() {
	        String tenant = TenantContext.getCurrentTenant();
	        if (tenant == null) 
	        	return ResponseEntity.badRequest().build();
	        List<Equipamento> all = repository.findAll();
	        System.out.println("TENANT:" + tenant);
	        return ResponseEntity.ok(all);
	    }

	    @PostMapping
	    public ResponseEntity<Equipamento> create(@RequestBody Equipamento payload) {
	        String tenant = TenantContext.getCurrentTenant();
	        if (tenant == null) 
	        	return ResponseEntity.badRequest().build();
	        Equipamento saved = repository.save(payload);
	        return ResponseEntity.ok(saved);
	    }
}
