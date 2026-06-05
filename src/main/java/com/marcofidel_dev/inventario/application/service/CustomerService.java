package com.marcofidel_dev.inventario.application.service;

import com.marcofidel_dev.inventario.application.dto.CustomerDTO;
import com.marcofidel_dev.inventario.domain.entity.AuditAction;
import com.marcofidel_dev.inventario.domain.entity.Customer;
import com.marcofidel_dev.inventario.domain.entity.Role;
import com.marcofidel_dev.inventario.infrastructure.repository.CustomerRepository;
import com.marcofidel_dev.inventario.infrastructure.security.Audited;
import com.marcofidel_dev.inventario.infrastructure.security.RequiresRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    /** Available for both roles: quick customer creation from the POS. */
    @Audited(action = AuditAction.CREATE, entity = "Customer")
    @Transactional
    public Customer crearRapido(String nombre, String telefono) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }
        Customer customer = Customer.builder()
                .name(nombre.trim())
                .phone(telefono != null && !telefono.isBlank() ? telefono.trim() : null)
                .active(true)
                .build();
        log.info("Creando cliente rápido: {}", nombre);
        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerDTO> buscarPorNombreOTelefono(String query) {
        List<Customer> customers = (query == null || query.isBlank())
                ? customerRepository.findByActiveTrue()
                : customerRepository.searchByNameOrPhone(query.trim());
        return customers.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public Optional<Customer> buscarPorId(Long id) {
        return customerRepository.findById(id);
    }

    @RequiresRole(Role.ADMIN)
    @Audited(action = AuditAction.UPDATE, entity = "Customer")
    @Transactional
    public Customer actualizar(Long id, String nombre, String telefono, String email, String notas) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + id));
        if (nombre != null && !nombre.isBlank()) customer.setName(nombre.trim());
        if (telefono != null) customer.setPhone(telefono.trim());
        if (email != null) customer.setEmail(email.trim());
        if (notas != null) customer.setNotes(notas);
        return customerRepository.save(customer);
    }

    @RequiresRole(Role.ADMIN)
    @Audited(action = AuditAction.DELETE, entity = "Customer")
    @Transactional
    public void desactivar(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + id));
        customer.setActive(false);
        customerRepository.save(customer);
        log.info("Cliente desactivado: id={} nombre={}", id, customer.getName());
    }

    public CustomerDTO toDTO(Customer c) {
        return new CustomerDTO(c.getId(), c.getName(), c.getPhone(), c.getEmail(), c.getNotes());
    }
}
