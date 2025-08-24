package com.myalgofax.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myalgofax.api.responce.ApiResponse;
import com.myalgofax.dto.BrokerDto;
import com.myalgofax.dto.CancelBracketOrderDto;
import com.myalgofax.dto.CancelCoverOrderDto;
import com.myalgofax.dto.CancelOrderDto;
import com.myalgofax.dto.LimitsDto;
import com.myalgofax.dto.MarginDto;
import com.myalgofax.dto.ModifyOrderDto;
import com.myalgofax.dto.PlaceOrderDto;
import com.myalgofax.dto.PositionsDto;
import com.myalgofax.security.util.jwt.JwtUtil;
import com.myalgofax.service.BrokerService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/brokers/auth")
public class BrockerController {
	private static final Logger logger = LoggerFactory.getLogger(BrockerController.class);
	@Autowired
	private BrokerService brockerService;
	
	@Autowired
	private JwtUtil jwtUtil;
	

	@PostMapping("/broker-connect")
	public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> brokerConnect(@RequestBody BrokerDto request) {
		
		logger.info("enter in brokerConnet method");
		logger.info("request: {}", request);
		logger.debug("Raw request object fields - username: {}, brokerCode: {}, neoFinKey: {}", 
				request.getUsername(), request.getBrokerCode(), request.getNeoFinKey());
		logger.debug("Broker connection request received");
		
		// Validate required fields
		if (request.getClientId() == null || request.getClientId().trim().isEmpty() ||
		    request.getApiKey() == null || request.getApiKey().trim().isEmpty() ||
		    request.getApiSecret() == null || request.getApiSecret().trim().isEmpty() ||
		    request.getBrokerCode() == null || request.getBrokerCode().trim().isEmpty()) {
            logger.warn("Missing required fields in request: clientId={}, apiKey={}, apiSecret={}, brokerCode={}", 
                       request.getClientId(), 
                       request.getApiKey() != null ? "[PRESENT]" : "[MISSING]",
                       request.getApiSecret() != null ? "[PRESENT]" : "[MISSING]",
                       request.getBrokerCode());
            return Mono.just(ResponseEntity.badRequest()
                .body(new ApiResponse<>("error", "Missing required fields: clientId, apiKey, apiSecret, brokerCode", null, null, false)));
        }
		

		return brockerService.brokerlogin(request).map(data -> {
			String token = (String) data.get("jwtToken");
		
			ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", "broker logged in successfully.",
					data, token,true);

			return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + token).body(response);
		}).onErrorResume(e -> {
			logger.error("Broker login failed", e);
			ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
					"Login failed: " + e.getMessage(), null, null, false);
			return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
		});
	}
	
	@PostMapping("/verifyotp")
	public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> verifyOTP(@RequestBody BrokerDto request) {
		logger.info("enter in verify otp");
		
		return brockerService.verifyotp(request).map(data -> {
			String token = (String) data.get("jwtToken");
			ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", "User logged in successfully.",
					data, token,false);

			return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + token).body(response);
		}).onErrorResume(e -> {
			logger.error("OTP verification failed", e);
			ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
					"OTP verification failed", null, null,false);
			return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
		});
	}
	
	
	@PostMapping("/status")
	public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> brokerStatus(@RequestBody BrokerDto request) {
	    logger.info("Checking broker status for user");
	    
	    return brockerService.checkBrokerStatus(request).map(data -> {
	        String token = (String) data.get("jwtToken"); // optional: only if you want token in status
	        ApiResponse<Map<String, Object>> response = new ApiResponse<>(
	                "success",
	                "Broker status fetched successfully.",
	                data,
	                token,
	                true
	        );
	        return ResponseEntity.ok()
	                .header(HttpHeaders.AUTHORIZATION, token != null ? "Bearer " + token : "")
	                .body(response);
	    }).onErrorResume(e -> {
	        logger.error("Broker status check failed", e);
	        ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
	                "error",
	                "Failed to fetch broker status: " + e.getMessage(),
	                null,
	                null,
	                false
	        );
	        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
	    });
	}

	@PostMapping("/positions")
	public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getPositions(@RequestBody PositionsDto request) {
		logger.info("enter in getPositions method");
		
		return brockerService.getPositions(request).map(data -> {
			
			ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", "Positions fetched successfully.",
					data, null, true);

			return ResponseEntity.ok().body(response);
		}).onErrorResume(e -> {
			logger.error("Get positions failed", e);
			ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
					"Failed to fetch positions: " + e.getMessage(), null, null, false);
			return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
		});
	}

	@PostMapping("/holdings")
	public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getPortfolioHoldings(@RequestBody BrokerDto request) {
		logger.info("enter in getPortfolioHoldings method");
		
		return brockerService.getPortfolioHoldings(request).map(data -> {
			ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", "Portfolio holdings fetched successfully.",
					data, null, true);

			return ResponseEntity.ok().body(response);
		}).onErrorResume(e -> {
			logger.error("Get portfolio holdings failed", e);
			ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
					"Failed to fetch portfolio holdings: " + e.getMessage(), null, null, false);
			return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
		});
	}

	@PostMapping("/orders")
	public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getOrderBook(@RequestBody BrokerDto request) {
		logger.info("enter in getOrderBook method");
		
		return brockerService.getOrderBook(request).map(data -> {
			ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", "Order book fetched successfully.",
					data, null, true);

			return ResponseEntity.ok().body(response);
		}).onErrorResume(e -> {
			logger.error("Get order book failed", e);
			ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
					"Failed to fetch order book: " + e.getMessage(), null, null, false);
			return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
		});
	}

	@PostMapping("/orderhistory")
	public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getOrderHistory(@RequestBody BrokerDto request) {
		logger.info("enter in getOrderHistory method");
		
		return brockerService.getOrderHistory(request).map(data -> {
			ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", "Order history fetched successfully.",
					data, null, true);

			return ResponseEntity.ok().body(response);
		}).onErrorResume(e -> {
			logger.error("Get order history failed", e);
			ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
					"Failed to fetch order history: " + e.getMessage(), null, null, false);
			return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
		});
	}

	@PostMapping("/trades")
	public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getTradeBook(@RequestBody BrokerDto request) {
		logger.info("enter in getTradeBook method");
		
		return brockerService.getTradeBook(request).map(data -> {
			ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", "Trade book fetched successfully.",
					data, null, true);

			return ResponseEntity.ok().body(response);
		}).onErrorResume(e -> {
			logger.error("Get trade book failed", e);
			ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
					"Failed to fetch trade book: " + e.getMessage(), null, null, false);
			return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
		});
	}

	@PostMapping("/placeorder")
	public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> placeOrder(@RequestBody PlaceOrderDto request) {
		logger.info("enter in placeOrder method");
		
	
		
		return brockerService.placeOrder(request).map(data -> {
			ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", "Order placed successfully.",
					data, null, true);

			return ResponseEntity.ok().body(response);
		}).onErrorResume(e -> {
			logger.error("Place order failed", e);
			ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
					"Failed to place order: " + e.getMessage(), null, null, false);
			return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
		});
	}

	@PostMapping("/modifyorder")
	public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> modifyOrder(@RequestBody ModifyOrderDto request) {
		logger.info("enter in modifyOrder method");
		
		return brockerService.modifyOrder(request).map(data -> {
			ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", "Order modified successfully.",
					data, null, true);

			return ResponseEntity.ok().body(response);
		}).onErrorResume(e -> {
			logger.error("Modify order failed", e);
			ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
					"Failed to modify order: " + e.getMessage(), null, null, false);
			return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
		});
	}

	@PostMapping("/cancelorder")
	public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> cancelOrder(@RequestBody CancelOrderDto request) {
		logger.info("enter in cancelOrder method");
		
		return brockerService.cancelOrder(request).map(data -> {
			ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", "Order cancelled successfully.",
					data, null, true);

			return ResponseEntity.ok().body(response);
		}).onErrorResume(e -> {
			logger.error("Cancel order failed", e);
			ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
					"Failed to cancel order: " + e.getMessage(), null, null, false);
			return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
		});
	}

	@PostMapping("/exitcoverorder")
	public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> cancelCoverOrder(@RequestBody CancelCoverOrderDto request) {
		logger.info("enter in cancelCoverOrder method");
		
		return brockerService.cancelCoverOrder(request).map(data -> {
			ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", "Cover order cancelled successfully.",
					data, null, true);

			return ResponseEntity.ok().body(response);
		}).onErrorResume(e -> {
			logger.error("Cancel cover order failed", e);
			ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
					"Failed to cancel cover order: " + e.getMessage(), null, null, false);
			return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
		});
	}

	@PostMapping("/exitbracketorder")
	public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> cancelBracketOrder(@RequestBody CancelBracketOrderDto request) {
		logger.info("enter in cancelBracketOrder method");
		
		return brockerService.cancelBracketOrder(request).map(data -> {
			ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", "Bracket order cancelled successfully.",
					data, null, true);

			return ResponseEntity.ok().body(response);
		}).onErrorResume(e -> {
			logger.error("Cancel bracket order failed", e);
			ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
					"Failed to cancel bracket order: " + e.getMessage(), null, null, false);
			return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
		});
	}

	@PostMapping("/limits")
	public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getLimits(@RequestBody LimitsDto request) {
		logger.info("enter in getLimits method");
		
		return brockerService.getLimits(request).map(data -> {
			ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", "Limits fetched successfully.",
					data, null, true);

			return ResponseEntity.ok().body(response);
		}).onErrorResume(e -> {
			logger.error("Get limits failed", e);
			ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
					"Failed to fetch limits: " + e.getMessage(), null, null, false);
			return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
		});
	}

	@PostMapping("/checkmargin")
	public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> checkMargin(@RequestBody MarginDto request) {
		logger.info("enter in checkMargin method");
		
		return brockerService.checkMargin(request).map(data -> {
			ApiResponse<Map<String, Object>> response = new ApiResponse<>("success", "Margin checked successfully.",
					data, null, true);

			return ResponseEntity.ok().body(response);
		}).onErrorResume(e -> {
			logger.error("Check margin failed", e);
			ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("error",
					"Failed to check margin: " + e.getMessage(), null, null, false);
			return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
		});
	}

}
