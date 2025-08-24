package com.myalgofax.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myalgofax.api.responce.ApiResponse;
import com.myalgofax.dto.ScripMasterDTO;
import com.myalgofax.service.ScripMasterService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/scrips")
public class ScripMasterController {

    @Autowired
    private ScripMasterService scripMasterService;

 
    
    @PostMapping("/getAll")
	public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getAllScrips(@RequestBody ScripMasterDTO request) {
		return scripMasterService.getAllScrips(request).map(data -> {
			String token = (String) data.get("jwtToken");

			ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", "User logged in successfully.",
					null, token, false);

			return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + token).body(response);
		}).onErrorResume(e -> {
			e.printStackTrace();
			ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
					"Login failed: " + e.getMessage(), null, null,false);
			return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
		});
	}
    
 
   
}