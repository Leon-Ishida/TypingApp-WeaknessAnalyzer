package application.controller;

import org.springframework.web.bind.annotation.RestController;

import application.dto.RegistRequest;
import application.dto.RegistResponse;
import application.service.RegistService;

import application.exception.UserAlreadyExistsException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class AuthAPIController {
    private final RegistService registService;

    public AuthAPIController(RegistService registService) {
        this.registService = registService;
    }

    @PostMapping("/auth/regist")
    public ResponseEntity<RegistResponse> registUSer(@RequestBody RegistRequest request) {
        RegistResponse response = registService.regist(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
        public ResponseEntity<RegistResponse> conflictException(UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new RegistResponse("error", e.getMessage()));
        }
}
