package com.example.multitenant.tenantrepo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.multitenant.tenantmodel.Equipamento;

public interface EquipamentoRepository  extends JpaRepository<Equipamento, Long> {

}
