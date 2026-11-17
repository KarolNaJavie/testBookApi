package com.example.library.controllers;

import com.example.library.model.ClientDTO;
import com.example.library.model.SubscriptionDTO;
import com.example.library.services.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class ClientController {

    private final ClientService clientService;

    @PostMapping("/register")
    public ClientDTO register(@RequestBody ClientDTO clientDTO) {
        return clientService.register(clientDTO);
    }

    @PostMapping("/subscription")
    public void addSubscription(@RequestBody SubscriptionDTO dto) {
        clientService.addSubscription(dto);
    }

    @DeleteMapping("/subscription")
    public void removeSubscription(@RequestBody SubscriptionDTO dto) {
        clientService.removeSubscription(dto);
    }
}
